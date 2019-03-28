/**
 * Copyright 2019 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.confluent.kafka.schemaregistry.rest.resources;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mapr.fs.proto.Security;
import com.mapr.security.MutableInt;
import com.sun.security.auth.module.UnixSystem;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.rest.exceptions.Errors;
import io.confluent.rest.exceptions.RestServerErrorException;
import java.io.IOException;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.security.PrivilegedAction;
import java.util.List;
import org.apache.hadoop.security.UserGroupInformation;

public class ImpersonationUtils {

  private static boolean isImpersonationEnabled;

  public static void initialize(SchemaRegistryConfig config) {
    isImpersonationEnabled = config.getBoolean(SchemaRegistryConfig.SCHEMAREGISTRY_IMPERSONATION);
  }

  private static String getUserNameFromAuthenticationOrCookie(String auth, String cookie) {
    if (auth != null) {
      if (auth.startsWith("Basic")) {
        int basicLen = 5;
        String base64Credentials = auth.substring(basicLen).trim();
        String credentials = new String(java.util.Base64.getDecoder().decode(base64Credentials),
            Charset.forName("UTF-8"));
        final String[] values = credentials.split(":", 2);
        return values[0];
      }
      if (auth.startsWith("MAPR-Negotiate")) {
        String authorization = auth.substring("MAPR-Negotiate".length()).trim();
        try {
          byte[] base64decoded = org.apache.commons.codec.binary.Base64.decodeBase64(authorization);
          Security.AuthenticationReqFull req = Security.AuthenticationReqFull
              .parseFrom(base64decoded);
          if (req != null && req.getEncryptedTicket() != null) {
            byte[] encryptedTicket = req.getEncryptedTicket().toByteArray();
            MutableInt err = new MutableInt();
            Security.Ticket decryptedTicket = com.mapr.security.Security
                .DecryptTicket(encryptedTicket, err);
            if (err.GetValue() == 0 && decryptedTicket != null) {
              Security.CredentialsMsg userCreds = decryptedTicket.getUserCreds();
              return userCreds.getUserName();
            } else {
              String decryptError = "Error while decrypting ticket and key " + err.GetValue();
              throw Errors.schemaRegistryException(decryptError, null);
            }
          } else {
            String clientRequestError = "Malformed client request";
            throw Errors.schemaRegistryException(clientRequestError, null);
          }
        } catch (InvalidProtocolBufferException e) {
          String serverKeyError = "Bad server key";
          throw Errors.schemaRegistryException(serverKeyError, e);
        }
      }
    } else {
      if (cookie != null) {
        List<HttpCookie> cookies = HttpCookie.parse(cookie);
        for (HttpCookie httpCookie : cookies) {
          if (httpCookie.getName().equals("hadoop.auth")) {
            String[] parameters = httpCookie.getValue().split("&");
            for (String parameter : parameters) {
              if (parameter.startsWith("u=")) {
                return parameter.substring("u=".length());
              }
            }
          }
        }
      }
    }
    return null;
  }

  static long getCurrentUserId() {
    return new UnixSystem().getUid();
  }

  public static <T> T runActionWithAppropriateUser(PrivilegedAction<T> action, String auth,
      String cookie) {
    if (isImpersonationEnabled) {
      try {
        String username = getUserNameFromAuthenticationOrCookie(auth, cookie);
        UserGroupInformation ugi = UserGroupInformation
            .createProxyUser(username, UserGroupInformation.getLoginUser());
        return ugi.doAs(action);
      } catch (IOException e) {
        throw Errors.serverLoginException(e);
      } catch (RestServerErrorException e) {
        throw Errors.schemaRegistryException("It is not possible to do this operation", e);
      }
    } else {
      return action.run();
    }
  }
}
