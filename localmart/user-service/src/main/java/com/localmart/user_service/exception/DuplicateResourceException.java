package com.localmart.user_service.exception;

// Thrown when a user with the same email or phone already exists.
// GlobalExceptionHandler catches this and returns HTTP 409 Conflict.
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
