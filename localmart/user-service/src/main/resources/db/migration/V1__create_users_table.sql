-- V1: Create users table
--
-- UUID primary key: better than auto-increment int for microservices.
-- UUIDs are globally unique — no collision risk when merging data across services.
-- We use gen_random_uuid() which is built into PostgreSQL 13+.
--
-- role column: stored as VARCHAR, not a PostgreSQL ENUM type.
-- Reason: PostgreSQL ENUMs are hard to alter later (need ALTER TYPE which locks the table).
-- Application-level enum (Java) validates the value; DB just stores the string.
--
-- active column: soft delete flag.
-- We never hard delete users — set active = false instead.
-- All queries filter by active = true.

CREATE TABLE users
(
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    phone      VARCHAR(15)  NOT NULL,
    address    VARCHAR(500) NOT NULL,
    role       VARCHAR(20)  NOT NULL, -- 'CUSTOMER' or 'SHOP_OWNER'
    active     BOOLEAN      NOT NULL DEFAULT true,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

-- Index on role: allows filtering "get all shop owners" or "get all customers" efficiently.
CREATE INDEX idx_users_role ON users (role);

-- Index on active: most queries filter active = true — this speeds them up.
CREATE INDEX idx_users_active ON users (active);
