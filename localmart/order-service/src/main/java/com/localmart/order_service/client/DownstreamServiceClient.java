package com.localmart.order_service.client;

import com.localmart.order_service.exception.ResourceNotFoundException;
import com.localmart.order_service.exception.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Wraps all outbound Feign calls with Resilience4j circuit breakers.
 *
 * Why a separate class and not methods in OrderService?
 * Spring AOP (which powers @CircuitBreaker) works through proxies.
 * A method calling another method on "this" bypasses the proxy entirely —
 * the annotation is never seen. Moving these methods here means OrderService
 * calls them as an injected bean, which goes through the proxy correctly.
 */
@Component
@RequiredArgsConstructor
public class DownstreamServiceClient {

    private final ShopClient shopClient;
    private final UserClient userClient;

    /**
     * Verifies the user exists via user-service.
     *
     * 4xx → ResourceNotFoundException (business error, ignored by circuit breaker — see application.yaml)
     * 5xx / connection error → propagates as FeignException → counted as failure → fallback on threshold
     */
    @Retry(name = "user-service")
    @CircuitBreaker(name = "user-service", fallbackMethod = "userServiceFallback")
    public void verifyUser(UUID userId) {
        try {
            userClient.getUser(userId);
        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                throw new ResourceNotFoundException("User", userId);
            }
            throw e;
        }
    }

    /**
     * Fetches product details from shop-service.
     * Same 4xx vs 5xx split as verifyUser.
     */
    @Retry(name = "shop-service")
    @CircuitBreaker(name = "shop-service", fallbackMethod = "shopServiceFallback")
    public ProductInfo fetchProduct(String shopId, String productId) {
        try {
            return shopClient.getProduct(shopId, productId);
        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                throw new ResourceNotFoundException("Product", productId);
            }
            throw e;
        }
    }

    // Fallbacks — called by Resilience4j when circuit is OPEN or failure threshold is reached

    @SuppressWarnings("unused")
    private void userServiceFallback(UUID userId, Throwable t) {
        throw new ServiceUnavailableException("User service is currently unavailable. Please try again shortly.");
    }

    @SuppressWarnings("unused")
    private ProductInfo shopServiceFallback(String shopId, String productId, Throwable t) {
        throw new ServiceUnavailableException("Shop service is currently unavailable. Please try again shortly.");
    }
}
