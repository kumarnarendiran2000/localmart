package com.localmart.order_service.client;

import lombok.Data;

import java.util.UUID;

// Local class used only to deserialize the response from user-service.
// Only fields needed by order-service are declared here.
@Data
public class UserInfo {
    private UUID id;
    private String name;
    private String email;
}
