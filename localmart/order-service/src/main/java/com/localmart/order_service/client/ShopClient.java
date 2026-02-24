package com.localmart.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Feign generates the HTTP call at runtime — no implementation class needed.
// name = "shop-service" → Feign asks Eureka for the IP, never hardcoded.
@FeignClient(name = "shop-service")
public interface ShopClient {

    // Must match exactly: GET /api/shops/{shopId}/products/{productId} in ShopController
    @GetMapping("/api/shops/{shopId}/products/{productId}")
    ProductInfo getProduct(@PathVariable String shopId,
                           @PathVariable String productId);
}
