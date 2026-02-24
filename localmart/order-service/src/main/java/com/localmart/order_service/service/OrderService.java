package com.localmart.order_service.service;

import com.localmart.order_service.client.ProductInfo;
import com.localmart.order_service.client.ShopClient;
import com.localmart.order_service.client.UserClient;
import com.localmart.order_service.dto.OrderResponse;
import com.localmart.order_service.dto.PlaceOrderRequest;
import com.localmart.order_service.dto.UpdateOrderStatusRequest;
import com.localmart.order_service.exception.ResourceNotFoundException;
import com.localmart.order_service.exception.ServiceUnavailableException;
import com.localmart.order_service.model.Order;
import com.localmart.order_service.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShopClient shopClient;
    private final UserClient userClient;

    /**
     * Places a new order.
     *
     * Flow:
     *  1. Verify user exists (Feign → user-service). Circuit breaker guards this call.
     *  2. Fetch product details (Feign → shop-service). Circuit breaker guards this call.
     *  3. Check stock availability.
     *  4. Snapshot product name + unit price → calculate total → save order.
     *
     * "Snapshot" means we store the price at the time of ordering.
     * If the shop changes the price later, this order record is unaffected.
     */
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        // Step 1: verify the user exists — throws ServiceUnavailableException if user-service is down
        verifyUser(request.getUserId());

        // Step 2: fetch product — throws ServiceUnavailableException if shop-service is down
        ProductInfo product = fetchProduct(request.getShopId(), request.getProductId());

        // Step 3: check stock
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available: " + product.getStockQuantity()
                            + ", Requested: " + request.getQuantity()
            );
        }

        // Step 4: calculate total and save
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .userId(request.getUserId())
                .shopId(request.getShopId())
                .productId(request.getProductId())
                .productName(product.getName())       // snapshot
                .unitPrice(product.getPrice())         // snapshot
                .quantity(request.getQuantity())
                .totalAmount(total)
                .build();

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(UUID userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(order -> toResponse(order))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByShop(String shopId) {
        return orderRepository.findByShopId(shopId).stream()
                .map(order -> toResponse(order))
                .toList();
    }

    /**
     * Updates order status (e.g., PENDING → CONFIRMED, or → CANCELLED).
     * Only the status field changes — everything else stays as-is.
     */
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        order.setStatus(request.getStatus());
        return toResponse(orderRepository.save(order));
    }

    // --- Circuit-breaker-protected Feign calls ---

    /**
     * Calls user-service to verify the user exists.
     * If user-service is down or times out, Resilience4j calls the fallback.
     *
     * fallbackMethod name must match exactly — Resilience4j finds it by reflection.
     * The fallback receives the same arguments plus the Throwable that triggered it.
     */
    @CircuitBreaker(name = "user-service", fallbackMethod = "userServiceFallback")
    private void verifyUser(UUID userId) {
        userClient.getUser(userId);
        // We only care that the call succeeded — the response fields aren't used here.
        // If the user doesn't exist, Feign will throw a FeignException (404 from user-service)
        // which also triggers the circuit breaker's failure count.
    }

    @CircuitBreaker(name = "shop-service", fallbackMethod = "shopServiceFallback")
    private ProductInfo fetchProduct(String shopId, String productId) {
        return shopClient.getProduct(shopId, productId);
    }

    // Fallback methods — called by Resilience4j when the circuit is OPEN or call fails

    private void userServiceFallback(UUID userId, Throwable t) {
        throw new ServiceUnavailableException("User service is currently unavailable. Please try again shortly.");
    }

    private ProductInfo shopServiceFallback(String shopId, String productId, Throwable t) {
        throw new ServiceUnavailableException("Shop service is currently unavailable. Please try again shortly.");
    }

    // --- Mapping ---

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .shopId(order.getShopId())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .unitPrice(order.getUnitPrice())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
