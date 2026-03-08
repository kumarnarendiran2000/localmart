package com.localmart.order_service.service;

import com.localmart.order_service.client.DownstreamServiceClient;
import com.localmart.order_service.client.ProductInfo;
import com.localmart.order_service.dto.OrderResponse;
import com.localmart.order_service.dto.PlaceOrderRequest;
import com.localmart.order_service.dto.UpdateOrderStatusRequest;
import com.localmart.order_service.event.OrderPlacedEvent;
import com.localmart.order_service.exception.ResourceNotFoundException;
import com.localmart.order_service.model.Order;
import com.localmart.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DownstreamServiceClient downstreamServiceClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    /**
     * Places a new order.
     *
     * Flow:
     *  1. Verify user exists (Feign → user-service via DownstreamServiceClient).
     *  2. Fetch product details (Feign → shop-service via DownstreamServiceClient).
     *  3. Check stock availability.
     *  4. Snapshot product name + unit price → calculate total → save order.
     *
     * Circuit breaker protection lives in DownstreamServiceClient, not here.
     * Spring AOP proxies only intercept calls from outside a class — not self-calls (this.method()).
     */
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        // Step 1: verify user exists — throws ServiceUnavailableException if user-service is down
        downstreamServiceClient.verifyUser(request.getUserId());

        // Step 2: fetch product — throws ServiceUnavailableException if shop-service is down
        ProductInfo product = downstreamServiceClient.fetchProduct(request.getShopId(), request.getProductId());

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

        // Publish event to Kafka — payment-service consumes this and processes payment.
        // The orderId is used as the message key so all events for the same order
        // go to the same Kafka partition (preserves ordering per order).
        //
        // Note: kafkaTemplate.send() is async — it queues the message and returns immediately.
        // If Kafka is down, the order is still saved but payment won't be triggered.
        // The Outbox Pattern (coming soon) will guarantee reliable delivery.
        OrderPlacedEvent event = new OrderPlacedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getShopId(),
                saved.getProductId(),
                saved.getProductName(),
                saved.getUnitPrice(),
                saved.getQuantity(),
                saved.getTotalAmount()
        );
        kafkaTemplate.send("order.placed", saved.getId().toString(), event);
        log.debug("Published order.placed event for orderId={}", saved.getId());

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
