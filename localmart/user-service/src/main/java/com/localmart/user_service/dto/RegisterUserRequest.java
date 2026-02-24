package com.localmart.user_service.dto;

import com.localmart.user_service.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterUserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    // Indian mobile numbers start with 6, 7, 8, or 9 and are exactly 10 digits.
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Role is required")
    // @NotNull (not @NotBlank) because Role is an enum, not a String.
    // @NotBlank only works on Strings.
    private Role role;
}
