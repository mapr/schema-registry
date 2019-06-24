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
import org.I0Itec.zkclient.ZkClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import kafka.utils.ZkUtils;
import java.io.IOException;
import java.util.List;

public class UrlUtils {

  private static final String SCHEMAREGISTRY_ZK_NAMESPACE_PREFIX = "schema_registry_";
  private static final String SCHEMAREGISTRY_ZK_URLS_DIR = "/sr_urls";

  public static String extractSchemaRegistryUrlFromZk(String schemaRegistryServiceId,
                                                      int zkSessionTimeoutMs,
                                                      boolean zkAcl) throws IOException {
    if (schemaRegistryServiceId == null
            ||
            schemaRegistryServiceId.isEmpty()) {
      throw new IllegalArgumentException("schemaRegistryServiceId should not be empty");
    }

    final String schemaRegistryZkNamespace =
            SCHEMAREGISTRY_ZK_NAMESPACE_PREFIX + schemaRegistryServiceId;
    String srClusterZkUrl = getZkUrl();

    int kafkaNamespaceIndex = srClusterZkUrl.indexOf("/");
    String zkConnForNamespaceCreation = kafkaNamespaceIndex > 0
            ? srClusterZkUrl.substring(0, kafkaNamespaceIndex)
            : srClusterZkUrl;

    final String schemaRegistryNamespace = "/" + schemaRegistryZkNamespace;
    final String schemaRegistryZkUrl = zkConnForNamespaceCreation + schemaRegistryNamespace;

    ZkUtils zkUtils = ZkUtils.apply(
            schemaRegistryZkUrl,
            zkSessionTimeoutMs,
            zkSessionTimeoutMs,
            zkAcl
    );


    if (!zkUtils.pathExists(SCHEMAREGISTRY_ZK_URLS_DIR)) {
      return "";
    }
    final ZkClient zkClient = zkUtils.zkClient();
    List<String> children = zkClient.getChildren(SCHEMAREGISTRY_ZK_URLS_DIR);
    if (children == null
            || children.isEmpty()) {
      return "";
    }


    final String res = children.stream()
            .map((child) -> {
              final String fullPath = SCHEMAREGISTRY_ZK_URLS_DIR
                      + "/" + child;
              return zkClient.readData(fullPath).toString();
            })
            .reduce("", (x, y) -> x + "," + y);
    zkUtils.close();

    return res.substring(1);
  }

  private static String getZkUrl() throws IOException {
    MapRFileSystem mfs = (MapRFileSystem) FileSystem.get(new Configuration());
    return mfs.getZkConnectString();
  }
}
