/**
 * Copyright (c) 2018 MapR, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package io.confluent.kafka.schemaregistry.filter.util;

import static java.util.Objects.isNull;

import io.confluent.kafka.schemaregistry.rest.exceptions.Errors;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;


public class KafkaConsumerPool {

    private final Map<UserGroupInformation, ThreadLocal<KafkaConsumer<byte[], byte[]>>> consumers =
            new ConcurrentHashMap<>();

    private final Properties properties;
    private final String stream;

    public KafkaConsumerPool(Properties properties, String stream) {
        this.properties = properties;
        this.stream = stream;
    }

    public ConsumerRecords<byte[], byte[]> poll() {
        try {
            UserGroupInformation user = UserGroupInformation.getCurrentUser();
            ThreadLocal<KafkaConsumer<byte[], byte[]>> threadLocal =
                    consumers.computeIfAbsent(user, info -> new ThreadLocal<>());

            KafkaConsumer<byte[], byte[]> kafkaConsumer = threadLocal.get();
            if (isNull(kafkaConsumer)) {
                kafkaConsumer = new KafkaConsumer<>(createPropsWithConsumerGroup(properties, user));
                kafkaConsumer.assign(Collections.singletonList(new TopicPartition(stream, 0)));
                threadLocal.set(kafkaConsumer);
            }

            return kafkaConsumer.poll(Duration.ofMillis(1000));
        } catch (Exception e) {
            throw Errors.serverLoginException(e);
        }
    }

    private Properties createPropsWithConsumerGroup(Properties consumerProps, UserGroupInformation user){
        Properties newProps = new Properties();
        newProps.putAll(consumerProps);
        String group = String.format("%d_%s", Thread.currentThread().getId(), user.getUserName());
        newProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, group);

        return newProps;
    }
}
