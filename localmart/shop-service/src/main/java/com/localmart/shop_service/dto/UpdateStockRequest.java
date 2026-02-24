package com.localmart.shop_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Schema(description = "Request body for updating product stock")
@Data
public class UpdateStockRequest {

    @Schema(description = "New stock quantity", example = "150")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Max(value = 100000, message = "Stock quantity cannot exceed 100,000")
    private int quantity;
}
