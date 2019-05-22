/**
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package io.confluent.kafka.schemaregistry.util;

import io.confluent.kafka.schemaregistry.exceptions.SchemaRegistryStreamsException;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.streams.mapr.Utils;

import java.io.IOException;

public class MaprFSUtils {

  public static void createAppDirAndInternalStreamsIfNotExist(SchemaRegistryConfig config) {
    try {
      FileSystem fs = FileSystem.get(new Configuration());
      if (!Utils.maprFSpathExists(fs, SchemaRegistryConfig.SCHEMAREGISTRY_SERVICES_COMMON_FOLDER)) {
        throw new SchemaRegistryStreamsException(
                SchemaRegistryConfig.SCHEMAREGISTRY_SERVICES_COMMON_FOLDER + " doesn't exist");
      }
      Utils.createStream(config.getCommandsStream());
    } catch (IOException e) {
      throw new KafkaException(e);
    }
  }
}
