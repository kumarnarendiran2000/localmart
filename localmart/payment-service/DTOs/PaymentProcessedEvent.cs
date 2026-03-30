namespace payment_service.DTOs;

// Published to "payment.processed" topic after processing payment.
// Consumed by order-service's PaymentEventConsumer — field names must match exactly.
//
// status: "SUCCESS" or "FAILED" — matches order-service's string comparison.
// reason: null on success, failure reason on failure.
public record PaymentProcessedEvent(
    Guid OrderId,
    string Status,
    string? Reason
);
