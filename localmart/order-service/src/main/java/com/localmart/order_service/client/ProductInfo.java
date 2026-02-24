package com.localmart.order_service.client;

import lombok.Data;

import java.math.BigDecimal;

// Local class to deserialize shop-service's product response.
// Only fields needed by order-service — Jackson ignores the rest.
@Data
public class ProductInfo {
    private String id;
    private String shopId;
    private String name;
    private BigDecimal price;
    private String unit;
    private int stockQuantity;
}
