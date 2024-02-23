/**
 * Copyright 2019 Confluent Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at</p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0</p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.</p>
 */

package io.confluent.kafka.schemaregistry.rest.exceptions;

import io.confluent.rest.exceptions.RestServerErrorException;
import javax.ws.rs.core.Response;

/**
 * Indicates an invalid ticket of schema registry server principal
 */
public class RestServerLoginException extends RestServerErrorException {

  private static final int DEFAULT_ERROR_CODE =
      Response.Status.SERVICE_UNAVAILABLE.getStatusCode();

  public RestServerLoginException(Throwable cause) {
    super("Login fails", DEFAULT_ERROR_CODE, cause);
  }
}
