package com.localmart.order_service.exception;

/**
 * Thrown inside a circuit breaker fallback method when a downstream
 * service (shop-service or user-service) is unreachable.
 *
 * GlobalExceptionHandler catches this and returns HTTP 503 Service Unavailable,
 * so the caller gets a clean error instead of a raw Feign exception.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
