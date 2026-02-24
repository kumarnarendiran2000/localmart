package com.localmart.shop_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @RestControllerAdvice — applied globally to all @RestController classes.
 * Any exception thrown from a controller or service bubbles up here
 * instead of crashing with a raw error page.
 *
 * ProblemDetail — RFC 7807 standard error format, built into Spring Boot 3.x+.
 * No extra library needed. All errors return the same JSON shape.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles: shop not found, product not found.
     * Thrown by: ShopService.findActiveShop(), updateStock(), deleteProduct()
     * Returns: HTTP 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Resource Not Found");
        return problem;
    }

    /**
     * Handles: attempt to create a shop or product that already exists.
     * Thrown by: ShopService (service-level check — runs first).
     * Returns: HTTP 409 Conflict with a specific message.
     *
     * Example: "Product 'Ponni Rice' already exists in this shop."
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResourceException(DuplicateResourceException ex) {
        log.warn("Duplicate resource: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("Duplicate Resource");
        return problem;
    }

    /**
     * Handles: MongoDB unique index violation (E11000 duplicate key error).
     *
     * This is the DB-level safety net — triggered only if two concurrent requests
     * both pass the service-level check before either one saves (race condition).
     * Spring Data wraps MongoDB's raw exception as DuplicateKeyException.
     *
     * Returns: HTTP 409 Conflict with a generic message (the raw MongoDB error
     * message is technical and not safe to expose to API callers).
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleDuplicateKeyException(DuplicateKeyException ex) {
        log.warn("MongoDB duplicate key violation: {}", ex.getMessage());

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "A duplicate entry already exists."
        );
    }

    /**
     * Handles: @Valid failures on request DTOs.
     * Thrown by: Spring MVC automatically when @Valid annotation is present
     *            on a controller method parameter and validation fails.
     * Returns: HTTP 400 with field-level error details.
     *
     * Example response:
     * {
     *   "status": 400,
     *   "title": "Validation Failed",
     *   "detail": "One or more fields have invalid values",
     *   "errors": {
     *     "name": "Shop name is required",
     *     "phone": "Enter a valid 10-digit Indian mobile number"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        // Extract each field error into a simple map: fieldName → errorMessage
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                (FieldError error) -> errors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more fields have invalid values"
        );
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", errors);  // attaches the field errors map to the response
        return problem;
    }

    /**
     * Catches any unexpected exception not handled above.
     * Prevents raw stack traces leaking to the API caller.
     * Returns: HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
    }
}
