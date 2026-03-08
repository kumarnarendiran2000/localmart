package com.localmart.order_service.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published to Kafka topic "order.placed" after an order is saved.
 *
 * Consumed by: payment-service (picks it up and processes the payment)
 *
 * Message key: orderId (string) — ensures all events for the same order
 * go to the same Kafka partition, preserving ordering per order.
 */
public record OrderPlacedEvent(
        UUID orderId,
        UUID userId,
        String shopId,
        String productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal totalAmount
) {}
