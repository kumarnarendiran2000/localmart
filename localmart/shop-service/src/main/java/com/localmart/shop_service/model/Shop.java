package com.localmart.shop_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @Document — tells Spring Data MongoDB:
 *   "this class maps to the 'shops' collection in MongoDB"
 *   When you call shopRepository.save(shop), Spring Data creates/updates
 *   a document in this collection automatically.
 *
 * @CompoundIndex — creates a MongoDB index across multiple fields together.
 *   def      → JSON string: field names + sort order (1 = ascending)
 *   unique   → true: MongoDB rejects any document where all 4 fields match an existing one
 *   name     → the index name as it appears in MongoDB (optional but good for debugging)
 *
 *   Business rule: a shop is a duplicate only when name + ownerName + phone + location
 *   are ALL identical. Changing even one field → different shop → allowed.
 */
@Document(collection = "shops")
@CompoundIndex(
        def = "{'name': 1, 'ownerName': 1, 'phone': 1, 'location': 1}",
        unique = true,
        name = "unique_shop_identity"
)

/**
 * Lombok annotations — these generate boilerplate code at compile time.
 *
 * @Data           = @Getter + @Setter + @ToString + @EqualsAndHashCode
 * @Builder        = generates a builder: Shop.builder().name("Murugan Stores").build()
 * @NoArgsConstructor = generates: new Shop()  (needed by MongoDB deserializer)
 * @AllArgsConstructor = generates constructor with all fields (needed by @Builder)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

    /**
     * @Id — marks this field as MongoDB's primary key (_id in the document).
     * MongoDB auto-generates a unique string value (ObjectId) if you don't set it.
     * Example value: "67b9f3a2c1d4e5f6a7b8c9d0"
     */
    @Id
    private String id;

    private String name;         // "Murugan Stores"
    private String ownerName;    // "Murugan"
    private String location;     // "Chennai"
    private String phone;        // "9876543210"

    /**
     * active flag — instead of deleting a shop, we set active=false.
     * This is called "soft delete" — data is preserved, just hidden from results.
     * Common production pattern: never hard-delete business data.
     */
    @Builder.Default
    private boolean active = true;

    /**
     * @Builder.Default — when using the builder pattern, fields need this annotation
     * to have a default value. Without it, builder ignores the = LocalDateTime.now().
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
