package com.localmart.order_service.exception;

/**
 * Thrown when an order (or any resource) doesn't exist.
 * GlobalExceptionHandler catches this and returns HTTP 404.
 *
 * Uses Object so the id can be a UUID or a String — both work:
 *   throw new ResourceNotFoundException("Order", orderId)   // UUID
 *   throw new ResourceNotFoundException("User", userId)     // UUID
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id);
    }
}
