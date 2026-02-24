package com.localmart.shop_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "Request body for adding a product to a shop")
@Data
public class AddProductRequest {

    @Schema(description = "Product name", example = "Ponni Rice")
    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String name;

    @Schema(description = "Price in INR", example = "65.00")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Schema(description = "Unit of measurement", example = "kg", allowableValues = {"kg", "litre", "piece", "packet", "dozen"})
    @NotBlank(message = "Unit is required")
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;

    @Schema(description = "Available stock count", example = "100")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Max(value = 100000, message = "Stock quantity cannot exceed 100,000")
    private int stockQuantity;
}
