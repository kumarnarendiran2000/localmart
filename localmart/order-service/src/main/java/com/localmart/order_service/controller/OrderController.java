package com.localmart.order_service.controller;

import com.localmart.order_service.dto.OrderResponse;
import com.localmart.order_service.dto.PlaceOrderRequest;
import com.localmart.order_service.dto.UpdateOrderStatusRequest;
import com.localmart.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders → place a new order → 201 Created
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(request);
    }

    // GET /api/orders/{id} → get one order by its UUID
    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable UUID id) {
        return orderService.getOrderById(id);
    }

    // GET /api/orders?userId={uuid} → all orders for a customer
    @GetMapping
    public List<OrderResponse> getOrders(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String shopId) {

        if (userId != null) {
            return orderService.getOrdersByUser(userId);
        }
        if (shopId != null) {
            return orderService.getOrdersByShop(shopId);
        }
        // If neither filter is provided, return empty list — avoids returning all orders globally
        return List.of();
    }

    // PATCH /api/orders/{id}/status → update status (CONFIRMED, CANCELLED, DELIVERED)
    @PatchMapping("/{id}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(id, request);
    }
}
