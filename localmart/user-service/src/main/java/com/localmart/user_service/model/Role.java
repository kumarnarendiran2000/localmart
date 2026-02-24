package com.localmart.user_service.model;

/**
 * Defines the two types of users in LocalMart.
 *
 * Stored as a VARCHAR in PostgreSQL (not a DB ENUM type).
 * The User entity uses @Enumerated(EnumType.STRING) to store "CUSTOMER" or "SHOP_OWNER"
 * as text, not as an ordinal integer (which would break if enum order ever changed).
 *
 * When authentication is added, these roles will also exist in the identity provider.
 * JWT claims will map to this enum for authorization checks.
 */
public enum Role {
    CUSTOMER,
    SHOP_OWNER
}
