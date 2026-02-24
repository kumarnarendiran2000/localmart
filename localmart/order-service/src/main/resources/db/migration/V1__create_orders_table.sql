-- V1: Create orders table
--
-- NO foreign keys to other services' databases.
-- Microservices own their data independently.
-- user_id references localmart_users.users.id — but enforced at app level (Feign call), not DB level.
-- shop_id and product_id are MongoDB ObjectId strings from shop-service.
--
-- Price snapshot pattern:
-- product_name, unit_price are copied at order time — never updated.
-- If shop owner changes price later, past orders are unaffected.
-- total_amount = unit_price × quantity, also locked at order time.

CREATE TABLE orders
(
    id           UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id      UUID           NOT NULL,
    shop_id      VARCHAR(50)    NOT NULL,
    product_id   VARCHAR(50)    NOT NULL,
    product_name VARCHAR(200)   NOT NULL,
    unit_price   NUMERIC(10, 2) NOT NULL,
    quantity     INT            NOT NULL CHECK (quantity > 0),
    total_amount NUMERIC(10, 2) NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP      NOT NULL DEFAULT now()
);

-- Index on user_id: customer views their own orders — most common read query
CREATE INDEX idx_orders_user_id ON orders (user_id);

-- Index on shop_id: shop owner views incoming orders
CREATE INDEX idx_orders_shop_id ON orders (shop_id);

-- Index on status: filtering by PENDING/CONFIRMED etc.
CREATE INDEX idx_orders_status ON orders (status);
