package com.localmart.shop_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * What the API returns for a product — does NOT include 'active'.
 */
@Data
@Builder
public class ProductResponse {
    private String id;
    private String shopId;
    private String name;
    private BigDecimal price;
    private String unit;
    private int stockQuantity;
    private LocalDateTime createdAt;
}
