package com.localmart.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    // GenerationType.UUID: Hibernate asks the DB to generate a UUID via gen_random_uuid().
    // Available since Hibernate 6 (bundled with Spring Boot 3+).
    // Alternative would be GenerationType.AUTO, but UUID is explicit and correct here.
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    // unique = true: Hibernate validates this constraint exists on startup (ddl-auto=validate).
    // The actual UNIQUE constraint was created by Flyway in V1 migration.
    private String email;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false, length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    // EnumType.STRING: stores the name() of the enum — "CUSTOMER" or "SHOP_OWNER".
    // EnumType.ORDINAL (default) stores 0 or 1 — dangerous because adding a new enum
    // value in the middle shifts all ordinals.
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
    // Soft delete flag — never hard delete a user.
    // deleteUser() sets this to false. All queries filter active = true.

    @Column(nullable = false, updatable = false)
    // updatable = false: created_at is set once at insert time, never updated.
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
