/**
 * Copyright 2014 Confluent Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at</p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0</p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.</p>
 */

package io.confluent.kafka.schemaregistry.filter;


import io.confluent.kafka.schemaregistry.util.ByteConsumerPool;
import io.confluent.kafka.schemaregistry.rest.exceptions.AuthorizationException;
import io.confluent.kafka.schemaregistry.util.ByteProducerPool;
import io.confluent.rest.impersonation.ImpersonationUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class AuthorizationFilter implements ContainerRequestFilter {

  private ByteConsumerPool consumerPool;
  private ByteProducerPool producerPool;
  private final String internalTopic;

  @Context
  protected ResourceInfo resourceInfo;

  public AuthorizationFilter(ByteConsumerPool consumerPool,
                             ByteProducerPool producerPool,
                             String internalTopic) {
    this.internalTopic = internalTopic;
    this.consumerPool = consumerPool;
    this.producerPool = producerPool;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    try {
      String userName = getUsername(requestContext);
      ImpersonationUtils.executor().runAs(userName, () -> {
        checkPermissions();
        return null;
      });
    } catch (Exception e) {
      requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
          .entity(e.getMessage())
          .build());
    }
  }

  private String getUsername(ContainerRequestContext containerRequestContext) {
    return Optional.ofNullable(containerRequestContext)
            .map(ContainerRequestContext::getSecurityContext)
            .map(SecurityContext::getUserPrincipal)
            .map(Principal::getName)
            .orElseThrow(() -> new NoSuchElementException(
                    "Could not get username from requestContext"));
  }

  public void initialize() {
    try {
      /** The method below is used to write initial record to INTERNAL_TOPIC.
       * It will not fail because authorization filter is created as cluster admin user.
       * Cluster admin user has appropriate permissions to send records to internal stream.
       */
      this.checkWritingPermissions();
    } catch (ExecutionException | InterruptedException e) {
      throw new KafkaException(e);
    }
  }

  private void checkPermissions() {
    try {
      Class<?> resourceClass = resourceInfo.getResourceClass();
      Method resourceMethod = resourceInfo.getResourceMethod();
      Permission permission = Permission.at(resourceClass, resourceMethod);
      switch (permission) {
        case READ:
          checkReadingPermissions();
          break;
        case MODIFY:
          checkWritingPermissions();
          break;
        default:
          break;
      }
    } catch (ExecutionException | InterruptedException e) {
      throw new AuthorizationException(
          "Access denied. This operation is not permitted for current user\n");
    }
  }

  private void checkWritingPermissions() throws ExecutionException, InterruptedException {
    ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(internalTopic,
            "k".getBytes(StandardCharsets.UTF_8), "v".getBytes(StandardCharsets.UTF_8));
    producerPool.send(record).get();
  }

  private void checkReadingPermissions() {
    ConsumerRecords<byte[], byte[]> records = consumerPool.poll(internalTopic);
    if (records.count() < 1) {
      throw new AuthorizationException(
          "Access denied. This operation is not permitted for current user\n");
    }
  }
}
