package com.localmart.shop_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Request body for registering a new shop")
@Data
public class CreateShopRequest {

    @Schema(description = "Shop name", example = "Murugan Stores")
    @NotBlank(message = "Shop name is required")
    @Size(max = 100, message = "Shop name must not exceed 100 characters")
    private String name;

    @Schema(description = "Owner full name", example = "Murugan Krishnan")
    @NotBlank(message = "Owner name is required")
    @Size(max = 100, message = "Owner name must not exceed 100 characters")
    private String ownerName;

    @Schema(description = "City where the shop is located", example = "Chennai")
    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @Schema(description = "10-digit Indian mobile number starting with 6-9", example = "9876543210")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String phone;
}
