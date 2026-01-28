package com.ecommerce.auth.exception;

/**
 * Excepción base para errores relacionados con user-service
 */
public class UserServiceException extends RuntimeException {

    private final int statusCode;

    public UserServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public UserServiceException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}