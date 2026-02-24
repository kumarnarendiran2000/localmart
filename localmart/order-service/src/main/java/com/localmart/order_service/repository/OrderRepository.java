package com.localmart.order_service.repository;

import com.localmart.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Customer views their own order history
    List<Order> findByUserId(UUID userId);

    // Shop owner views incoming orders for their shop
    List<Order> findByShopId(String shopId);
}
