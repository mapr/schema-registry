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

package io.confluent.kafka.schemaregistry.rest.resources;

import com.sun.security.auth.module.UnixSystem;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.rest.exceptions.Errors;
import org.apache.hadoop.security.UserGroupInformation;

import java.nio.charset.Charset;
import java.security.PrivilegedAction;
import java.util.Base64;

public class ImpersonationUtils {

    private static boolean isImpersonationEnabled;

    public static void initialize(SchemaRegistryConfig config) {
        isImpersonationEnabled = config.getBoolean(SchemaRegistryConfig.SCHEMAREGISTRY_IMPERSONATION);
    }

    private static String getUserNameFromAuthentication(String auth){
        if(auth != null && auth.startsWith("Basic")){
            int basicLen = 5;
            String base64Credentials = auth.substring(basicLen).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials),
                    Charset.forName("UTF-8"));
            final String[] values = credentials.split(":",2);
            return values[0];
        }
        return null;
    }

    static long getCurrentUserId() {
        return new UnixSystem().getUid();
    }

    static <T> T runActionWithAppropriateUser(PrivilegedAction<T> action, String auth) {
        if (isImpersonationEnabled) {
            try {
                String uname = getUserNameFromAuthentication(auth);
                UserGroupInformation ugi = UserGroupInformation.createProxyUser(uname, UserGroupInformation.getLoginUser());
                return ugi.doAs(action);
            } catch (Exception e) {
                throw Errors.schemaRegistryException("Operation is not permitted for this user", e);
            }
        } else {
            return action.run();
        }
    }
}
