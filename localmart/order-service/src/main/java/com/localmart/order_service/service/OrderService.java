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
import feign.FeignException;
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
     * Verifies the user exists by calling user-service.
     *
     * 4xx from user-service (e.g. 404 user not found) = business error, not an infrastructure failure.
     * We convert it to ResourceNotFoundException, which Resilience4j is configured to IGNORE
     * (see ignore-exceptions in application.yaml) — so it does NOT count toward circuit breaker failures.
     *
     * 5xx / timeout / connection refused = real infrastructure failure.
     * These propagate as FeignException → Resilience4j counts them → eventually opens circuit → fallback runs.
     */
    @CircuitBreaker(name = "user-service", fallbackMethod = "userServiceFallback")
    private void verifyUser(UUID userId) {
        try {
            userClient.getUser(userId);
        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                // Downstream service is healthy — it returned a 4xx business error
                throw new ResourceNotFoundException("User", userId);
            }
            throw e; // 5xx or connection error — let circuit breaker count it
        }
    }

    /**
     * Fetches product details from shop-service.
     * Same 4xx vs 5xx split as verifyUser above.
     */
    @CircuitBreaker(name = "shop-service", fallbackMethod = "shopServiceFallback")
    private ProductInfo fetchProduct(String shopId, String productId) {
        try {
            return shopClient.getProduct(shopId, productId);
        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                throw new ResourceNotFoundException("Product", productId);
            }
            throw e;
        }
    }

    // Fallback methods — called by Resilience4j only when circuit is OPEN or a 5xx/connection failure occurs

    @SuppressWarnings("unused")
    private void userServiceFallback(UUID userId, Throwable t) {
        throw new ServiceUnavailableException("User service is currently unavailable. Please try again shortly.");
    }

    @SuppressWarnings("unused")
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
