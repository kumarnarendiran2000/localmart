package com.localmart.shop_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Document — maps to the 'products' collection in MongoDB.
 * Each product document belongs to exactly one shop (via shopId).
 * This is MongoDB's way of doing a "foreign key" — we store the
 * shop's ID inside the product document.
 *
 * @CompoundIndex on (shopId, name):
 *   A shop cannot have two products with the same name.
 *   "Ponni Rice" can exist in shop-1 AND shop-2 — different shopId, allowed.
 *   "Ponni Rice" twice in shop-1 — same shopId + name → rejected.
 *
 * Products use HARD DELETE (no active flag).
 * Deleting a product removes it entirely from MongoDB.
 * Re-adding the same product name after deletion is allowed — the document is gone.
 */
@Document(collection = "products")
@CompoundIndex(
        def = "{'shopId': 1, 'name': 1}",
        unique = true,
        name = "unique_product_per_shop"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    private String shopId;       // which shop this product belongs to — "67b9f3a2c1d4..."
    private String name;         // "Ponni Rice", "Coconut Oil", "Toor Dal"

    /**
     * BigDecimal — used for money, NOT double or float.
     * double/float have floating-point precision issues (e.g. 0.1 + 0.2 = 0.30000000000000004).
     * BigDecimal is exact — critical for prices and financial calculations.
     * Example: new BigDecimal("149.50") → exactly ₹149.50
     */
    private BigDecimal price;    // ₹149.50

    /**
     * unit — how the product is measured/sold.
     * "kg", "litre",, "piece" "packet", "dozen"
     * Example: Rice = "kg", Coconut Oil = "litre", Parle-G = "packet"
     */
    private String unit;         // "kg", "litre", "piece", "packet"

    /**
     * stockQuantity — how many units are currently available.
     * When order-service later checks inventory, it reads this field.
     * If 0, product is out of stock.
     */
    private int stockQuantity;   // 50 (meaning 50 kg of rice available)

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
