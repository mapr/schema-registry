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
