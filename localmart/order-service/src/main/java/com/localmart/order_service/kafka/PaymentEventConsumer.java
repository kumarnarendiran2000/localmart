package com.localmart.order_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmart.order_service.event.PaymentProcessedEvent;
import com.localmart.order_service.model.Order;
import com.localmart.order_service.model.OrderStatus;
import com.localmart.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Consumes "payment.processed" events from Kafka.
 *
 * Published by: payment-service after processing a payment.
 *
 * On SUCCESS → order moves to CONFIRMED (payment went through, shop will prepare order).
 * On FAILED  → order moves to CANCELLED (Saga compensation — undo the PENDING state).
 *
 * Why StringDeserializer + ObjectMapper here?
 *   payment-service is .NET — it doesn't add Spring's __TypeId__ header.
 *   Spring's JsonDeserializer would fail without that header.
 *   So we receive the raw JSON string and parse it ourselves with ObjectMapper.
 *   This is the standard cross-language Kafka pattern.
 *
 * Why no @Transactional here?
 *   Spring's @KafkaListener runs in the Kafka listener container thread, not a Spring
 *   transaction scope. @Transactional on a Kafka listener method doesn't work reliably.
 *   We delegate the save to OrderRepository directly — JPA auto-manages the session per call.
 *
 * What if the message is malformed?
 *   We catch the exception and log it. We do NOT rethrow — rethrowing would cause
 *   Kafka to retry the same bad message forever. The order stays PENDING in this case
 *   (a DLQ — Dead Letter Queue — will handle this properly in a later stage).
 *
 * What if the orderId doesn't exist?
 *   Logged as a warning and skipped — could happen if order was deleted between
 *   publishing and consuming (edge case, safe to skip).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.processed", groupId = "order-service-group")
    public void onPaymentProcessed(String message) {
        log.debug("Received payment.processed message: {}", message);

        PaymentProcessedEvent event;
        try {
            event = objectMapper.readValue(message, PaymentProcessedEvent.class);
        } catch (Exception e) {
            log.error("Failed to deserialize payment.processed message — skipping. Raw: {}. Error: {}", message, e.getMessage());
            return;
        }

        Optional<Order> maybeOrder = orderRepository.findById(event.orderId());
        if (maybeOrder.isEmpty()) {
            log.warn("Received payment.processed for unknown orderId={} — skipping", event.orderId());
            return;
        }

        Order order = maybeOrder.get();

        if ("SUCCESS".equals(event.status())) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order {} CONFIRMED after successful payment", event.orderId());
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.warn("Order {} CANCELLED — payment failed. Reason: {}", event.orderId(), event.reason());
        }
    }
}
