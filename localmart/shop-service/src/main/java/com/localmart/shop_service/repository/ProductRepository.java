package com.localmart.shop_service.repository;

import com.localmart.shop_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * MongoRepository<Product, String>
 *   - Product → the document class this repository manages
 *   - String  → the type of the @Id field
 *
 * Same derived query convention as ShopRepository.
 * Spring Data reads the method name and generates the MongoDB query.
 */
public interface ProductRepository extends MongoRepository<Product, String> {

    /**
     * findByShopId("abc123")
     * → db.products.find({ shopId: "abc123" })
     * No active filter — products are hard-deleted, so all documents in the
     * collection are live. Returns all products belonging to the given shop.
     */
    List<Product> findByShopId(String shopId);

    /**
     * findByShopIdAndId("shopId", "productId")
     * → db.products.find({ shopId: "...", _id: "..." })
     * Fetch a product by its ID — and verify it belongs to the given shop.
     * Prevents one shop from reading or modifying another shop's product.
     * findById() is inherited from MongoRepository but doesn't check shopId.
     */
    Optional<Product> findByShopIdAndId(String shopId, String id);

    /**
     * existsByShopIdAndName("shopId", "Ponni Rice")
     * → db.products.find({ shopId: "...", name: "Ponni Rice" }).limit(1)
     * Service-level duplicate check (Option A) — runs before save().
     * Returns true if a product with this name already exists in the shop.
     */
    boolean existsByShopIdAndName(String shopId, String name);
}
