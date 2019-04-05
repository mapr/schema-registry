/**
 * Copyright 2014 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.confluent.kafka.schemaregistry.filter;


import static io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig.KAFKASTORE_STREAM_CONFIG;
import static java.lang.String.format;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

import io.confluent.kafka.schemaregistry.filter.util.KafkaConsumerPool;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.rest.exceptions.AuthorizationException;
import io.confluent.kafka.schemaregistry.storage.KafkaProducerPool;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import io.confluent.rest.impersonation.ImpersonationUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AuthorizationFilter implements ContainerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

  private static final String INTERNAL_TOPIC = "schema-registry-authorization-auxiliary-topic";

  private final KafkaConsumerPool kafkaConsumerPool;
  private final KafkaProducerPool kafkaProducerPool;
  private final String internalTopic;

  public AuthorizationFilter(SchemaRegistryConfig schemaRegistryConfig) {
    this.internalTopic = format("%s:%s", schemaRegistryConfig.getString(KAFKASTORE_STREAM_CONFIG),
        INTERNAL_TOPIC);
    this.kafkaConsumerPool = new KafkaConsumerPool(getConsumerProperties(), internalTopic);
    this.kafkaProducerPool = new KafkaProducerPool(getProducerProperties());
    this.initializeInternalTopicWithDummyRecord();
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String authentication = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
    String cookie = retrieveCookie(requestContext);
    try {
      ImpersonationUtils.runAsUser(() -> {
        checkPermissions(requestContext);
        return null;
      }, authentication, cookie);
    } catch (Exception e) {
      requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
          .entity(e.getMessage())
          .build());
    }
  }

  private void initializeInternalTopicWithDummyRecord() {
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

  private void checkPermissions(ContainerRequestContext requestContext) {
    try {
      String method = requestContext.getMethod();
      if (method.equals(GET)) {
        checkReadingPermissions();
      } else if (method.equals(POST) || method.equals(DELETE)) {
        checkWritingPermissions();
      }
    } catch (Exception e) {
      throw new AuthorizationException(
          "Access denied. This operation is not permitted for current user\n");
    }
  }

  private void checkWritingPermissions() throws ExecutionException, InterruptedException {
    ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(internalTopic, new byte[0]);
    kafkaProducerPool.send(record).get();
  }

  private void checkReadingPermissions() {
    ConsumerRecords<byte[], byte[]> records = kafkaConsumerPool.poll();
    if (records.count() < 1) {
      throw new AuthorizationException(
          "Access denied. This operation is not permitted for current user\n");
    }
  }

  private String retrieveCookie(ContainerRequestContext requestContext) {
    Map<String, Cookie> cookies = requestContext.getCookies();
    return cookies.values().stream().map(Cookie::getValue).findAny().orElse(null);
  }

  private Properties getProducerProperties() {
    Properties properties = new Properties();
    properties.put(ProducerConfig.ACKS_CONFIG, "-1");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        org.apache.kafka.common.serialization.ByteArraySerializer.class);
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        org.apache.kafka.common.serialization.ByteArraySerializer.class);
    properties.put(ProducerConfig.RETRIES_CONFIG, 0);
    properties.put("streams.buffer.max.time.ms", "0");
    return properties;
  }

  private Properties getConsumerProperties() {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        org.apache.kafka.common.serialization.ByteArrayDeserializer.class);
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        org.apache.kafka.common.serialization.ByteArrayDeserializer.class);
    return properties;
  }

}
