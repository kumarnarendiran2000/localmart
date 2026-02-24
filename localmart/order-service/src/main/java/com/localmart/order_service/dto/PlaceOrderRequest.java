package com.localmart.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PlaceOrderRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Shop ID is required")
    private String shopId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
