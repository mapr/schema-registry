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

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.commons.lang3.StringUtils;

public class SchemaRegistryDiscoveryConfig {
  public static final String SERVICE_ID_CONFIG
          = "schema.registry.service.id";
  public static final String SERVICE_ID_DEFAULT
          = "default_";
  public static final String SERVICE_ID_DOC
          = "Indicates the ID of the schema registry service.";

  public static final String DISCOVERY_TIMEOUT_CONFIG
          = "schema.registry.discovery.timeout";
  public static final int DISCOVERY_TIMEOUT_DEFAULT
          = 60_000;
  public static final String DISCOVERY_TIMEOUT_DOC
          = "The timeout in milliseconds for request to Schema Registry URL storage.";

  public static final String DISCOVERY_RETRIES_CONFIG
          = "schema.registry.discovery.retries";
  public static final int DISCOVERY_RETRIES_DEFAULT = 6;
  public static final String DISCOVERY_RETRIES_DOC =
          "The number of retries for Schema Registry URL discovery.";

  public static final String DISCOVERY_INTERVAL_CONFIG
          = "schema.registry.discovery.interval";
  public static final int DISCOVERY_INTERVAL_DEFAULT = 15_000;
  public static final String DISCOVERY_INTERVAL_DOC =
          "The interval in milliseconds between retries for Schema Registry URL discovery.";

  public static ConfigDef defineDiscoveryProperties(ConfigDef configDef) {
    return defineDiscoveryProperties(configDef, "");
  }

  public static ConfigDef defineDiscoveryProperties(ConfigDef configDef, String prefix) {
    prefix = prefixOrEmpty(prefix);
    return configDef
            .define(prefix + SERVICE_ID_CONFIG,
                    ConfigDef.Type.STRING,
                    SERVICE_ID_DEFAULT,
                    ConfigDef.Importance.MEDIUM,
                    SERVICE_ID_DOC)
            .define(prefix + DISCOVERY_TIMEOUT_CONFIG,
                    ConfigDef.Type.INT,
                    DISCOVERY_TIMEOUT_DEFAULT,
                    ConfigDef.Importance.MEDIUM,
                    DISCOVERY_TIMEOUT_DOC)
            .define(prefix + DISCOVERY_RETRIES_CONFIG,
                    ConfigDef.Type.INT,
                    DISCOVERY_RETRIES_DEFAULT,
                    ConfigDef.Importance.MEDIUM,
                    DISCOVERY_RETRIES_DOC)
            .define(prefix + DISCOVERY_INTERVAL_CONFIG,
                    ConfigDef.Type.INT,
                    DISCOVERY_INTERVAL_DEFAULT,
                    ConfigDef.Importance.MEDIUM,
                    DISCOVERY_INTERVAL_DOC);
  }

  private static String prefixOrEmpty(String prefix) {
    return prefix.isEmpty() ? "" : StringUtils.appendIfMissing(prefix, ".");
  }

  public static SchemaRegistryDiscoveryClient configureDiscoveryClient(AbstractConfig config) {
    return configureDiscoveryClient(config, "");
  }

  public static SchemaRegistryDiscoveryClient configureDiscoveryClient(AbstractConfig config,
                                                                       String prefix) {
    prefix = prefixOrEmpty(prefix);
    return new SchemaRegistryDiscoveryClient()
            .serviceId(config.getString(prefix + SERVICE_ID_CONFIG))
            .timeout(config.getInt(prefix + DISCOVERY_TIMEOUT_CONFIG))
            .retries(config.getInt(prefix + DISCOVERY_RETRIES_CONFIG))
            .retryInterval(config.getInt(prefix + DISCOVERY_INTERVAL_CONFIG));
  }

}
