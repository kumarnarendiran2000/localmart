package com.localmart.user_service.exception;

// Thrown when a user is not found by id (or is soft-deleted).
// GlobalExceptionHandler catches this and returns HTTP 404.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id);
    }
}
