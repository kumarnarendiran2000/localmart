package com.localmart.shop_service.repository;

import com.localmart.shop_service.model.Shop;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * MongoRepository<Shop, String>
 *   - Shop   → the document class this repository manages
 *   - String → the type of the @Id field (our id is a String / ObjectId)
 *
 * Spring Data MongoDB reads this interface at startup and generates
 * a complete implementation automatically — you write zero SQL/queries.
 *
 * FREE methods you get without writing anything:
 *   save(shop)           → INSERT or UPDATE
 *   findById(id)         → SELECT WHERE _id = ?
 *   findAll()            → SELECT *
 *   delete(shop)         → DELETE (we won't use this — we use soft delete)
 *   existsById(id)       → SELECT COUNT(*) WHERE _id = ?
 *   count()              → SELECT COUNT(*)
 *
 * Custom methods below follow Spring Data's "derived query" convention:
 * Method name → Spring Data parses it → generates the MongoDB query.
 * No @Query annotation needed for simple conditions.
 */
public interface ShopRepository extends MongoRepository<Shop, String> {

    /**
     * findByActive(true)  → db.shops.find({ active: true })
     * Returns only shops that haven't been soft-deleted.
     * This is what the public listing API will call.
     */
    List<Shop> findByActiveTrue();

    /**
     * findByIdAndActiveTrue("abc123")
     * → db.shops.find({ _id: "abc123", active: true })
     * Used when fetching a single shop — don't show soft-deleted shops.
     */
    Optional<Shop> findByIdAndActiveTrue(String id);

    /**
     * findByLocation("Chennai")
     * → db.shops.find({ location: "Chennai", active: true })
     * Used to list all active shops in a city.
     */
    List<Shop> findByLocationAndActiveTrue(String location);

    /**
     * existsByNameAndOwnerNameAndPhoneAndLocation(...)
     * → db.shops.find({ name: "...", ownerName: "...", phone: "...", location: "..." }).limit(1)
     * Service-level duplicate check (Option A) — runs before save() in createShop().
     * Returns true only when ALL four fields match an existing shop document.
     * Changing even one field → returns false → save proceeds.
     */
    boolean existsByNameAndOwnerNameAndPhoneAndLocation(
            String name, String ownerName, String phone, String location);
}
