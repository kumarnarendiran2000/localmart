package com.localmart.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    // email and role are NOT updatable:
    // - email: changing email requires a verification flow
    // - role: role changes are an admin operation, not a self-service update
}
