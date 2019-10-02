/**
 * Copyright 2019 Confluent Inc.
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

package io.confluent.kafka.schemaregistry.storage;

import com.google.common.annotations.VisibleForTesting;
import io.confluent.kafka.schemaregistry.rest.exceptions.Errors;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.apache.hadoop.security.UserGroupInformation;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class KafkaProducerPool {

  private Map<UserGroupInformation, KafkaProducer<byte[], byte[]>> producers =
      new ConcurrentHashMap<>();

  private Properties properties;

  @VisibleForTesting
  private Function<Properties, KafkaProducer<byte[], byte[]>> producerFactory;

  public KafkaProducerPool(Properties properties) {
    this.properties = properties;
    this.producerFactory = KafkaProducer::new;
  }

  public Future<RecordMetadata> send(ProducerRecord<byte[], byte[]> record) {
    try {
      UserGroupInformation user = UserGroupInformation.getCurrentUser();

      KafkaProducer<byte[], byte[]> producer = producers.computeIfAbsent(user,
          info -> producerFactory.apply(properties));

      return producer.send(record);
    } catch (Exception e) {
      throw Errors.serverLoginException(e);
    }
  }

  public void close() {
    for (KafkaProducer producer : producers.values()) {
      producer.close();
    }
  }
}
