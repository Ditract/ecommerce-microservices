package com.ecommerce.auth.exception;

/**
 * Excepción cuando user-service no está disponible
 */
public class ServiceUnavailableException extends UserServiceException {

    public ServiceUnavailableException(String message) {
        super(message, 503);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, 503, cause);
    }
}