/**
 * Copyright 2014 Confluent Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at</p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0</p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.</p>
 */

package io.confluent.kafka.schemaregistry.filter;

import io.confluent.kafka.schemaregistry.util.ByteConsumerPool;
import io.confluent.kafka.schemaregistry.util.UnixUserIdUtils;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.util.ByteProducerPool;
import org.apache.hadoop.security.IdMappingServiceProvider;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import io.confluent.kafka.schemaregistry.util.KafkaClientSupplier;

import java.util.HashMap;
import java.util.Map;

public class AuthorizationFilterProvider {
  private static final String INTERNAL_TOPIC = "schema-registry-authorization-auxiliary-topic";

  public static AuthorizationFilter configure(SchemaRegistryConfig schemaRegistryConfig) {
    final KafkaClientSupplier clientSupplier = new KafkaClientSupplier();
    final IdMappingServiceProvider idMapper = UnixUserIdUtils.getUnixIdMapper();
    final ByteConsumerPool consumerPool = createConsumerPool(clientSupplier, idMapper);
    final ByteProducerPool producerPool = createProducerPool(clientSupplier, idMapper);
    final String internalTopic = getConfiguredAuxiliaryTopic(schemaRegistryConfig);
    final AuthorizationFilter filter = new AuthorizationFilter(
        consumerPool, producerPool, internalTopic
    );
    filter.initialize();
    return filter;
  }

  private static String getConfiguredAuxiliaryTopic(SchemaRegistryConfig schemaRegistryConfig) {
    String kafkaStoreStream = schemaRegistryConfig.getKafkaStoreStream();
    return String.format("%s:%s", kafkaStoreStream, INTERNAL_TOPIC);
  }

  private static ByteProducerPool createProducerPool(KafkaClientSupplier clientSupplier,
                                                     IdMappingServiceProvider idMapper) {
    final Map<String, Object> properties = new HashMap<>();
    properties.put(ProducerConfig.ACKS_CONFIG, "-1");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    properties.put(ProducerConfig.RETRIES_CONFIG, 0);
    properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
    properties.put("streams.buffer.max.time.ms", "0");
    return new ByteProducerPool(properties, clientSupplier, idMapper);
  }

  private static ByteConsumerPool createConsumerPool(KafkaClientSupplier clientSupplier,
                                                     IdMappingServiceProvider idMapper) {
    final Map<String, Object> properties = new HashMap<>();
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    return new ByteConsumerPool(properties, clientSupplier, idMapper);
  }

}
