package com.localmart.order_service.controller;

import com.localmart.order_service.dto.OrderResponse;
import com.localmart.order_service.dto.PlaceOrderRequest;
import com.localmart.order_service.dto.UpdateOrderStatusRequest;
import com.localmart.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Orders", description = "Place and manage customer orders")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place a new order")
    @ApiResponse(responseCode = "201", description = "Order placed successfully")
    @ApiResponse(responseCode = "400", description = "Validation failed or insufficient stock")
    @ApiResponse(responseCode = "503", description = "shop-service or user-service is unavailable")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        log.debug("POST /api/orders - userId: {}, shopId: {}, productId: {}",
                request.getUserId(), request.getShopId(), request.getProductId());
        return orderService.placeOrder(request);
    }

    @Operation(summary = "Get an order by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{id}")
    public OrderResponse getOrderById(
            @Parameter(description = "Order UUID") @PathVariable UUID id) {
        log.debug("GET /api/orders/{}", id);
        return orderService.getOrderById(id);
    }

    @Operation(summary = "Get orders by customer or shop",
               description = "Pass either userId or shopId as a query param. Returns empty list if neither is provided.")
    @ApiResponse(responseCode = "200", description = "Orders returned (empty array if none found)")
    @GetMapping
    public List<OrderResponse> getOrders(
            @Parameter(description = "Filter by customer UUID") @RequestParam(required = false) UUID userId,
            @Parameter(description = "Filter by shop ID") @RequestParam(required = false) String shopId) {
        log.debug("GET /api/orders - userId: {}, shopId: {}", userId, shopId);

        if (userId != null) {
            return orderService.getOrdersByUser(userId);
        }
        if (shopId != null) {
            return orderService.getOrdersByShop(shopId);
        }
        return List.of();
    }

    @Operation(summary = "Update order status",
               description = "Valid transitions: PENDING → CONFIRMED, PENDING → CANCELLED, CONFIRMED → DELIVERED")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @PatchMapping("/{id}/status")
    public OrderResponse updateOrderStatus(
            @Parameter(description = "Order UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.debug("PATCH /api/orders/{}/status - status: {}", id, request.getStatus());
        return orderService.updateOrderStatus(id, request);
    }
}
