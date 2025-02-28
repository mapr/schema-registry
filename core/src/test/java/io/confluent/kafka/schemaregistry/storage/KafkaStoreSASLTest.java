/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.confluent.kafka.schemaregistry.storage;

import io.confluent.kafka.schemaregistry.SASLClusterTestHarness;
import io.confluent.kafka.schemaregistry.storage.exceptions.StoreInitializationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// tests SASL with ZooKeeper and Kafka.
public class KafkaStoreSASLTest extends SASLClusterTestHarness {
  @Test
  public void testInitialization() throws Exception {
    KafkaStore<String, String> kafkaStore = StoreUtils.createAndInitSASLStoreInstance(brokerList);
    kafkaStore.close();
  }

  @Test
  public void testDoubleInitialization() throws Exception {
    assertThrows(StoreInitializationException.class, () -> {
      KafkaStore<String, String> kafkaStore = StoreUtils.createAndInitSASLStoreInstance(brokerList);
      try {
        kafkaStore.init();
      } finally {
        kafkaStore.close();
      }
    });
  }

  @Test
  public void testSimplePut() throws Exception {
    KafkaStore<String, String> kafkaStore = StoreUtils.createAndInitSASLStoreInstance(brokerList);
    String key = "Kafka";
    String value = "Rocks";
    try {
      kafkaStore.put(key, value);
      String retrievedValue = kafkaStore.get(key);
      assertEquals(value, retrievedValue, "Retrieved value should match entered value");
    } finally {
      kafkaStore.close();
    }
  }
}
