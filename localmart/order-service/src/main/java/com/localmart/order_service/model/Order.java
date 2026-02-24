package com.localmart.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;
    // No FK constraint — user exists in a separate DB (localmart_users).
    // Validated at application level via Feign call to user-service before saving.

    @Column(nullable = false, length = 50)
    private String shopId;
    // MongoDB ObjectId string from shop-service — stored as plain VARCHAR.

    @Column(nullable = false, length = 50)
    private String productId;
    // MongoDB ObjectId string — stored as plain VARCHAR.

    @Column(nullable = false, length = 200)
    private String productName;
    // Price snapshot: copied from shop-service at order time. Never updated.

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    // Price snapshot: locked at order time. Shop owner price changes don't affect this.

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    // unitPrice × quantity, computed and stored at order time.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
