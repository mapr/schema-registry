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

import com.mapr.fs.MapRFileSystem;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class SchemaRegistryDiscoveryClient {
  private static final Logger LOG = LoggerFactory.getLogger(SchemaRegistryDiscoveryClient.class);

  protected static final String SCHEMAREGISTRY_ZK_NAMESPACE_PREFIX = "schema_registry_";
  protected static final String SCHEMAREGISTRY_ZK_URLS_DIR = "/sr_urls";

  private String serviceId = SchemaRegistryDiscoveryConfig.SERVICE_ID_DEFAULT;
  private int timeoutMs = SchemaRegistryDiscoveryConfig.DISCOVERY_TIMEOUT_DEFAULT;
  private int retries = SchemaRegistryDiscoveryConfig.DISCOVERY_RETRIES_DEFAULT;
  private long intervalMs = SchemaRegistryDiscoveryConfig.DISCOVERY_INTERVAL_DEFAULT;

  public List<String> discoverUrls() {
    String schemaRegistryZkUrl = getSchemaRegistryZkUrl();
    LOG.info("Discovering Schema Registry from Zookeeper {} ({} retries with interval {} ms)",
             schemaRegistryZkUrl, retries, intervalMs);
    int retries = this.retries;
    do {
      retries--;
      try {
        List<String> urls = readSchemaRegistryUrlsFromZookeeper(schemaRegistryZkUrl);
        if (!urls.isEmpty()) {
          LOG.info("Discovered Schema Registry urls: {}", urls);
          return urls;
        } else if (retries > 0) {
          LOG.warn("Schema Registry Urls are not available, retrying after the interval {} ms "
                           + "(attempts left: {})",
                   intervalMs, retries);
          Utils.sleep(intervalMs);
        } else {
          throw new IllegalStateException(String.format(
                  "Schema Registry Discovery failed with service id '%s'",
                  serviceId
          ));
        }
      } catch (Exception e) {
        if (retries > 0) {
          LOG.warn("Failed to discover Schema Registry, retrying after the interval {} ms "
                           + "(attempts left: {})",
                   intervalMs, retries, e);
        } else {
          throw e;
        }
      }
    } while (true);
  }

  private List<String> readSchemaRegistryUrlsFromZookeeper(String zkUrl) {
    ZkClient zkClient = createZkClient(zkUrl);
    try {
      if (!zkClient.exists(SCHEMAREGISTRY_ZK_URLS_DIR)) {
        return Collections.emptyList();
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

  public String getSchemaRegistryZkUrl() {
    String srZkNamespace = SCHEMAREGISTRY_ZK_NAMESPACE_PREFIX + serviceId;
    String srClusterZkUrl = getZkUrl();
    String zkConnForNamespaceCreation = substringBefore(srClusterZkUrl, "/");
    return zkConnForNamespaceCreation + "/" + srZkNamespace;
  }

  public ZkClient createZkClient(String zkUrl) {
    return new ZkClient(zkUrl, timeoutMs, timeoutMs, new ZKStringSerializer());
  }

  protected String getZkUrl() {
    try {
      MapRFileSystem mfs = (MapRFileSystem) FileSystem.get(new Configuration());
      return Objects.requireNonNull(mfs.getZkConnectString());
    } catch (Exception e) {
      throw new IllegalStateException("Cannot receive Zookeeper URL from MapR-FS", e);
    }
  }

  public SchemaRegistryDiscoveryClient serviceId(String serviceId) {
    if (isEmpty(serviceId)) {
      throw new IllegalArgumentException("serviceId should not be empty");
    }
    this.serviceId = serviceId;
    return this;
  }

  public SchemaRegistryDiscoveryClient timeout(int timeoutMs) {
    this.timeoutMs = timeoutMs;
    return this;
  }

  public SchemaRegistryDiscoveryClient retries(int retries) {
    this.retries = retries;
    return this;
  }

  public SchemaRegistryDiscoveryClient retryInterval(long intervalMs) {
    this.intervalMs = intervalMs;
    return this;
  }

  private static class ZKStringSerializer implements ZkSerializer {

    @Override
    public byte[] serialize(Object data) throws ZkMarshallingError {
      return data.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
      if (bytes == null) {
        return null;
      } else {
        return new String(bytes, StandardCharsets.UTF_8);
      }
    }
  }
}
