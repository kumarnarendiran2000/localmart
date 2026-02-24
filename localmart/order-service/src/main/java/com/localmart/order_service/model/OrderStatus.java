package com.localmart.order_service.model;

// Stored as VARCHAR in PostgreSQL via @Enumerated(EnumType.STRING) on the Order entity.
// Flow: PENDING → CONFIRMED → DELIVERED
//                          ↘ CANCELLED
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    DELIVERED,
    CANCELLED
}
