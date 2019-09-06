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

package io.confluent.kafka.schemaregistry.client.rest.utils;

import com.mapr.fs.MapRFileSystem;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.substringBefore;

public class UrlUtils {

  private static final String SCHEMAREGISTRY_ZK_NAMESPACE_PREFIX = "schema_registry_";
  private static final String SCHEMAREGISTRY_ZK_URLS_DIR = "/sr_urls";

  /**
   * @param zkAcl is deprecated for read operations
   */
  @SuppressWarnings("unused")
  public static String extractSchemaRegistryUrlFromZk(String schemaRegistryServiceId,
                                                      int zkTimeoutMs,
                                                      boolean zkAcl) throws IOException {
    List<String> urls = extractSchemaRegistryUrlsFromZk(schemaRegistryServiceId, zkTimeoutMs);
    return String.join(",", urls);
  }

  public static List<String> extractSchemaRegistryUrlsFromZk(String schemaRegistryServiceId,
                                                             int zkTimeoutMs) throws IOException {
    String zkUrl = getSchemaRegistryZkUrl(schemaRegistryServiceId);
    return readSchemaRegistryUrlsFromZookeeper(zkUrl, zkTimeoutMs);
  }

  private static String getSchemaRegistryZkUrl(String schemaRegistryServiceId) throws IOException {
    if (isEmpty(schemaRegistryServiceId)) {
      throw new IllegalArgumentException("schemaRegistryServiceId should not be empty");
    }

    String srZkNamespace = SCHEMAREGISTRY_ZK_NAMESPACE_PREFIX + schemaRegistryServiceId;
    String srClusterZkUrl = getZkUrl();
    String zkConnForNamespaceCreation = substringBefore(srClusterZkUrl, "/");
    return zkConnForNamespaceCreation + "/" + srZkNamespace;
  }

  private static List<String> readSchemaRegistryUrlsFromZookeeper(String zkUrl, int zkTimeout) {
    ZkClient zkClient = ZkUtils.createZkClient(zkUrl, zkTimeout, zkTimeout);
    try {
      if (!zkClient.exists(SCHEMAREGISTRY_ZK_URLS_DIR)) {
        return new LinkedList<>();
      }
      return zkClient.getChildren(SCHEMAREGISTRY_ZK_URLS_DIR)
              .stream()
              .map(child -> SCHEMAREGISTRY_ZK_URLS_DIR + "/" + child)
              .map((fullPath) -> zkClient.readData(fullPath).toString())
              .collect(Collectors.toList());
    } finally {
      zkClient.close();
    }
  }

  private static String getZkUrl() throws IOException {
    MapRFileSystem mfs = (MapRFileSystem) FileSystem.get(new Configuration());
    return Optional.ofNullable(mfs.getZkConnectString())
            .orElseThrow(() -> new IOException("Cannot receive Zookeeper URL from MapR-FS"));
  }
}
