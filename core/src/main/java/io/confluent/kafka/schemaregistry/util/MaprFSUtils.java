/**
 * Copyright 2018 Confluent Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at</p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0</p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.</p>
 **/

package io.confluent.kafka.schemaregistry.util;

import com.mapr.fs.MapRFileAce;
import com.mapr.fs.MapRFileSystem;
import io.confluent.kafka.schemaregistry.exceptions.SchemaRegistryStreamsException;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.mapr.tools.KafkaMaprStreams;
import org.apache.kafka.mapr.tools.KafkaMaprTools;
import org.apache.kafka.mapr.tools.KafkaMaprfs;
import org.apache.kafka.mapr.tools.MaprfsPermissions;

import java.io.IOException;
import java.util.Optional;

public class MaprFSUtils {

  public static void createKafkaStoreInternalStreamIfNotExist(SchemaRegistryConfig config) {
    KafkaMaprfs maprfs = KafkaMaprTools.tools().maprfs();
    maprfs.requireExisting(SchemaRegistryConfig.SCHEMAREGISTRY_SERVICES_COMMON_FOLDER);

    String internalFolder = config.getKafkaStoreStreamFolder();
    if (!maprfs.exists(internalFolder)) {
      maprfs.mkdirs(internalFolder);
      maprfs.setPermissions(internalFolder, MaprfsPermissions.permissions()
              .put(MapRFileAce.AccessType.READDIR, MaprfsPermissions.PUBLIC)
              .put(MapRFileAce.AccessType.LOOKUPDIR, MaprfsPermissions.PUBLIC)
              .put(MapRFileAce.AccessType.ADDCHILD, MaprfsPermissions.STARTUP_USER)
              .put(MapRFileAce.AccessType.DELETECHILD, MaprfsPermissions.STARTUP_USER)
              .put(MapRFileAce.AccessType.WRITEFILE, MaprfsPermissions.STARTUP_USER));
    }

    try (KafkaMaprStreams maprStreams = KafkaMaprTools.tools().streams()) {
      maprStreams.createStreamForAllUsers(config.getKafkaStoreStream());
      maprStreams.ensureStreamLogCompactionIsEnabled(config.getKafkaStoreStream());
    }
  }

  public static String getZKQuorum() {
    try {
      MapRFileSystem mfs = KafkaMaprTools.tools().getMapRFileSystem();
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
