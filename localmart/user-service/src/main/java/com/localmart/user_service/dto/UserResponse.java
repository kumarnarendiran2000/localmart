package com.localmart.user_service.dto;

import com.localmart.user_service.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

// What the API returns to the caller.
// Does NOT include 'active' (internal soft-delete flag — not the caller's concern).
@Data
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Role role;
}
