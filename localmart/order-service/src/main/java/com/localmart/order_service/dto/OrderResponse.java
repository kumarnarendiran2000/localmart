package com.localmart.order_service.dto;

import com.localmart.order_service.model.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private UUID userId;
    private String shopId;
    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
