package com.localmart.order_service.event;

import java.util.UUID;

/**
 * Event consumed from Kafka topic "payment.processed".
 *
 * Published by: payment-service after processing a payment.
 *
 * status: "SUCCESS" → order moves to CONFIRMED
 *         "FAILED"  → order moves to CANCELLED (Saga compensation)
 *
 * reason: null on success, human-readable failure reason on failure
 * (e.g. "Insufficient funds", "Card declined")
 */
public record PaymentProcessedEvent(
        UUID orderId,
        String status,
        String reason
) {}
