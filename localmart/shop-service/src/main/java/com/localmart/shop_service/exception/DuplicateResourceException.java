package com.localmart.shop_service.exception;

/**
 * Thrown when an attempt is made to create a resource that already exists.
 *
 * Extends RuntimeException — unchecked exception.
 * No need to declare it in method signatures (no 'throws' needed).
 *
 * Caught by GlobalExceptionHandler → returns HTTP 409 Conflict.
 *
 * This is the service-level duplicate check (Option A).
 * The MongoDB unique index (Option B) is the DB-level safety net below it.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
