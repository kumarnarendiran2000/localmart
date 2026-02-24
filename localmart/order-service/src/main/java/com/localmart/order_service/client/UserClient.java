package com.localmart.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

// name = "user-service" → Feign asks Eureka for the IP, never hardcoded.
@FeignClient(name = "user-service")
public interface UserClient {

    // Matches: GET /api/users/{id} in UserController
    @GetMapping("/api/users/{id}")
    UserInfo getUser(@PathVariable UUID id);
}
