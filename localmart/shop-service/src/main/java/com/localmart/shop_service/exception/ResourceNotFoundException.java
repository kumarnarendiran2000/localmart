package com.localmart.shop_service.exception;

/**
 * Thrown when a requested resource doesn't exist (or is soft-deleted).
 * The GlobalExceptionHandler catches this and returns HTTP 404.
 *
 * Extends RuntimeException — Spring handles unchecked exceptions cleanly.
 * You don't need to declare "throws ResourceNotFoundException" everywhere.
 *
 * Single exception class for both shops and products:
 *   throw new ResourceNotFoundException("Shop", id)
 *   throw new ResourceNotFoundException("Product", id)
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, String id) {
        super(resource + " not found with id: " + id);
    }
}
