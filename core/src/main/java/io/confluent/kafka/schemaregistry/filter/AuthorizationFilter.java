/**
 * Copyright 2014 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.confluent.kafka.schemaregistry.filter;


import static io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig.KAFKASTORE_STREAM_CONFIG;
import static io.confluent.kafka.schemaregistry.rest.resources.ImpersonationUtils.getUserNameFromAuthentication;
import static java.lang.String.format;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

import com.mapr.streams.Admin;
import com.mapr.streams.StreamDescriptor;
import com.mapr.streams.Streams;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.rest.exceptions.AuthorizationException;
import java.io.IOException;
import java.util.Arrays;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationFilter implements ContainerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

  private static final String COLON_DELIMITER = ":";
  private static final String PIPE_DELIMITER = "|";
  private static final String PERMIT_ALL_PERMISSION = "p";

  private SchemaRegistryConfig schemaRegistryConfig;

  public AuthorizationFilter(SchemaRegistryConfig schemaRegistryConfig) {
    this.schemaRegistryConfig = schemaRegistryConfig;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String authentication = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
    String username = getUserNameFromAuthentication(authentication);
    logger.debug("Authorization for user: {}", username);

    StreamDescriptor descriptor = getStreamDescriptor();
    String method = requestContext.getMethod();
    checkUserPermissions(username, method, GET, descriptor.getConsumePerms());
    checkUserPermissions(username, method, POST, descriptor.getProducePerms());
  }

  private void checkUserPermissions(String username, String currentMethod, String method,
      String permissions) {
    if (currentMethod.equals(method) && !checkExpression(permissions, username)) {
      throwAuthorizationException(username);
    }
  }

  private StreamDescriptor getStreamDescriptor() throws IOException {
    Admin streamAdmin = Streams.newAdmin(new Configuration());
    String streamName = schemaRegistryConfig.getString(KAFKASTORE_STREAM_CONFIG);
    return streamAdmin.getStreamDescriptor(streamName);
  }

  private void throwAuthorizationException(String username) {
    logger.error(format("Access denied for user %s", username));
    throw new AuthorizationException(format("Access denied for user %s", username));
  }

  // TODO: Find out the way to parse complex expression
  private boolean checkExpression(String expression, String username) {
    return expression.equals(PERMIT_ALL_PERMISSION) || Arrays.stream(expression.split(PIPE_DELIMITER))
        .map(text -> text.split(COLON_DELIMITER)[1])
        .anyMatch(text -> text.equals(username));
  }
}
