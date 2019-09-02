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

import com.mapr.fs.MapRFileAce;
import com.mapr.fs.MapRFileSystem;
import io.confluent.kafka.schemaregistry.exceptions.SchemaRegistryStreamsException;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.streams.mapr.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class MaprFSUtils {

  public static void createKafkaStoreInternalStreamIfNotExist(SchemaRegistryConfig config) {
    try {
      FileSystem fs = FileSystem.get(new Configuration());
      if (!Utils.maprFSpathExists(fs, SchemaRegistryConfig.SCHEMAREGISTRY_SERVICES_COMMON_FOLDER)) {
        throw new SchemaRegistryStreamsException(
                SchemaRegistryConfig.SCHEMAREGISTRY_SERVICES_COMMON_FOLDER + " doesn't exist");
      }
      String currentUser = UserGroupInformation.getCurrentUser().getUserName();
      String errorMessage =
              String.format(
                      "User: %s has no permissions to run Schema Registry service with ID: %s",
                      currentUser,
                      config.getString(SchemaRegistryConfig.SCHEMA_REGISTRY_SERVICE_ID_CONFIG));
      if (!Utils.maprFSpathExists(fs, config.getKafkaStoreStreamFolder())) {
        // Creation of application forler with appropriate aces
        ArrayList<MapRFileAce> aceList = new ArrayList<MapRFileAce>();

        MapRFileAce ace = new MapRFileAce(MapRFileAce.AccessType.READDIR);
        ace.setBooleanExpression("p");
        aceList.add(ace);
        ace = new MapRFileAce(MapRFileAce.AccessType.ADDCHILD);
        ace.setBooleanExpression("u:" + currentUser);
        aceList.add(ace);
        ace = new MapRFileAce(MapRFileAce.AccessType.LOOKUPDIR);
        ace.setBooleanExpression("p");
        aceList.add(ace);
        ace = new MapRFileAce(MapRFileAce.AccessType.DELETECHILD);
        ace.setBooleanExpression("u:" + currentUser);
        aceList.add(ace);

        Utils.maprFSpathCreate(fs, config.getKafkaStoreStreamFolder(),
                aceList, currentUser, errorMessage);
      } else {
        Utils.validateDirectoryPerms(fs, config.getKafkaStoreStreamFolder(),
                currentUser, errorMessage);
      }

      final String kafkaStoreInternalStream = config.getKafkaStoreStream();
      Utils.createStreamWithPublicPerms(kafkaStoreInternalStream);
      Utils.enableLogCompactionForStreamIfNotEnabled(kafkaStoreInternalStream);
    } catch (IOException e) {
      throw new KafkaException(e);
    }
  }

  public static String getZKQuorum() {
    try {
      MapRFileSystem mfs = (MapRFileSystem) FileSystem.get(new Configuration());
      return Optional.ofNullable(mfs.getZkConnectString())
          .orElseThrow(() -> new IOException("Cannot receive Zookeeper URL from MapR-FS"));
    } catch (RuntimeException e) {
      throw new SchemaRegistryStreamsException(
              "Zookeeper cannot be reached", e);
    } catch (IOException e) {
      throw new KafkaException(e);
    }
  }
}
