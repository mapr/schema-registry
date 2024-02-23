/*
 * Copyright 2018 Confluent Inc.
 *
 * <p>Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at</p>
 *
 * <p>http://www.confluent.io/confluent-community-license
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.</p>
 */

package io.confluent.kafka.schemaregistry.client.rest.utils;

import java.io.IOException;
import java.util.List;

@Deprecated
/**
 * @deprecated instead use {@link SchemaRegistryDiscoveryClient}
 */
public class UrlUtils {

  /**
   * @param zkAcl is deprecated for read operations
   */
  @SuppressWarnings({"unused", "RedundantThrows"})
  public static String extractSchemaRegistryUrlFromZk(String schemaRegistryServiceId,
                                                      int zkTimeoutMs,
                                                      boolean zkAcl) throws IOException {
    List<String> list = new SchemaRegistryDiscoveryClient()
            .serviceId(schemaRegistryServiceId)
            .timeout(zkTimeoutMs)
            .discoverUrls();
    return String.join(",", list);
  }

  @SuppressWarnings("RedundantThrows")
  public static List<String> extractSchemaRegistryUrlsFromZk(String schemaRegistryServiceId,
                                                             int zkTimeoutMs) throws IOException {
    return new SchemaRegistryDiscoveryClient()
            .serviceId(schemaRegistryServiceId)
            .timeout(zkTimeoutMs)
            .discoverUrls();
  }
}
