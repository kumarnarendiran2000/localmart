package com.localmart.shop_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * What the API returns to the caller — does NOT include 'active'.
 * 'active' is an internal soft-delete flag, not the caller's concern.
 *
 * Rule: model fields = what MongoDB stores
 *       response DTO fields = what the API exposes
 */
@Data
@Builder
public class ShopResponse {
    private String id;
    private String name;
    private String ownerName;
    private String location;
    private String phone;
    private LocalDateTime createdAt;
}
