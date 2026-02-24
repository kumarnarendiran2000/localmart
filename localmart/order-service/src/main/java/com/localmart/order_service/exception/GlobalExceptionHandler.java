package com.localmart.order_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catches exceptions from any @RestController and maps them to
 * RFC 7807 ProblemDetail responses — consistent JSON error shape across all services.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Order or user not found → 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        return problem;
    }

    // Downstream service down (circuit breaker fallback threw this) → 503
    @ExceptionHandler(ServiceUnavailableException.class)
    public ProblemDetail handleServiceUnavailableException(ServiceUnavailableException ex) {
        log.warn("Downstream service unavailable: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problem.setTitle("Service Unavailable");
        return problem;
    }

    // @Valid failures on request DTOs → 400 with per-field errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                (FieldError error) -> errors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more fields have invalid values");
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    // Safety net — catch-all for anything unexpected → 500
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }
}
