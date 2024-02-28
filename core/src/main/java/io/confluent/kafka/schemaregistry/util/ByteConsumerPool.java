/**
 * Copyright 2019 Confluent Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at</p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0</p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.</p>
 */

package io.confluent.kafka.schemaregistry.util;

import com.google.common.collect.ImmutableMap;
import io.confluent.kafka.schemaregistry.rest.exceptions.Errors;
import org.apache.hadoop.security.IdMappingServiceProvider;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ByteConsumerPool {
  private final ThreadLocal<Map<Integer, Consumer<byte[], byte[]>>> consumers
      = ThreadLocal.withInitial(ConcurrentHashMap::new);

  private final Map<String, Object> config;
  private final IdMappingServiceProvider idMapper;
  private final KafkaClientSupplier clientSupplier;

  public ByteConsumerPool(Map<String, Object> config,
                          KafkaClientSupplier clientSupplier,
                          IdMappingServiceProvider idMapper) {
    this.config = config;
    this.idMapper = idMapper;
    this.clientSupplier = clientSupplier;
  }

  public ConsumerRecords<byte[], byte[]> poll(String topic) {
    try {
      final String userName = UserGroupInformation.getCurrentUser().getUserName();
      final int userUid = idMapper.getUid(userName);
      final Consumer<byte[], byte[]> consumer = consumers.get()
          .computeIfAbsent(userUid, this::createConsumerWithGroup);
      Collection<TopicPartition> partitions = Collections.singletonList(
              new TopicPartition(topic, 0));
      consumer.assign(partitions);
      consumer.seekToBeginning(partitions);
      return consumer.poll(Duration.ofMillis(1000));
    } catch (Exception e) {
      throw Errors.serverLoginException(e);
    }
  }

  private Consumer<byte[], byte[]> createConsumerWithGroup(int userId) {
    final long threadId = Thread.currentThread().getId();
    final String groupId = String.format("t_%d_u%d", threadId, userId);
    return clientSupplier.getConsumer(ImmutableMap.<String, Object>builder()
        .putAll(this.config)
        .put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        .build());
  }
}
