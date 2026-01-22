package com.ecommerce.auth.exception;

/**
 * Excepción cuando un usuario no se encuentra en user-service
 */
public class UserNotFoundException extends UserServiceException {

    public UserNotFoundException(String message) {
        super(message, 404);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, 404, cause);
    }
}