# LocalMart — Local Shop Marketplace

A microservices platform connecting neighborhood shops with local customers.
Built with Java 21 + Spring Boot 4 + .NET 8.

---

## Architecture

```
  CLIENT (Bruno / Postman)
        │
        ▼
  API GATEWAY  :8080  (Spring Cloud Gateway)
        │
  ┌─────┼─────────────┐
  ▼     ▼             ▼
shop  user          order
:8081 :8082         :8083
MongoDB PostgreSQL  PostgreSQL
              │
         calls shop-service (Feign + Circuit Breaker)
         calls user-service (Feign + Circuit Breaker)

[Coming soon] order → Kafka → payment-service → Kafka → notification-service
```

---

## Services

| Service              | Port | Language     | Database   | Status       |
|----------------------|------|--------------|------------|--------------|
| discovery-server     | 8761 | Java/Spring  | —          | Live         |
| api-gateway          | 8080 | Java/Spring  | —          | Live         |
| shop-service         | 8081 | Java/Spring  | MongoDB    | Live         |
| user-service         | 8082 | Java/Spring  | PostgreSQL | Live         |
| order-service        | 8083 | Java/Spring  | PostgreSQL | Live         |
| payment-service      | 8084 | .NET 8       | PostgreSQL | Coming soon  |
| notification-service | 8085 | .NET 8       | MongoDB    | Coming soon  |

---

## Prerequisites

- Java 21
- Maven 3.9+
- Docker Desktop (for databases)
- Bruno (API client) — https://www.usebruno.com

---

## Setup

### 1. Start databases

```bash
docker compose -f docker-compose.infra.yml up -d
```

This starts PostgreSQL (port 5432) and MongoDB (port 27017).

### 2. Create PostgreSQL databases

```bash
docker exec -it localmart-postgres psql -U localmart -c "CREATE DATABASE localmart_users;"
docker exec -it localmart-postgres psql -U localmart -c "CREATE DATABASE localmart_orders;"
```

> MongoDB: shop-service creates its own collections automatically on first run.

### 3. Build all Java services

```bash
mvn clean install -DskipTests
```

### 4. Start services (each in its own terminal)

```bash
mvn spring-boot:run -pl discovery-server
mvn spring-boot:run -pl api-gateway
mvn spring-boot:run -pl shop-service
mvn spring-boot:run -pl user-service
mvn spring-boot:run -pl order-service
```

**Start order**: discovery-server first → then the rest in any order.

---

## Verify Everything Is Up

| URL                                   | What you should see                    |
|---------------------------------------|----------------------------------------|
| http://localhost:8761                 | Eureka dashboard — all services listed |
| http://localhost:8081/swagger-ui.html | Shop Service API docs                  |
| http://localhost:8082/swagger-ui.html | User Service API docs                  |
| http://localhost:8083/swagger-ui.html | Order Service API docs                 |
| http://localhost:8081/actuator/health | `{"status":"UP"}`                      |

---

## API Endpoints

### Shop Service — `/api/shops`

| Method | Path                                             | Description           |
|--------|--------------------------------------------------|-----------------------|
| POST   | `/api/shops`                                     | Register a shop       |
| GET    | `/api/shops`                                     | List all active shops |
| GET    | `/api/shops/{shopId}`                            | Get shop by ID        |
| DELETE | `/api/shops/{shopId}`                            | Soft-delete a shop    |
| POST   | `/api/shops/{shopId}/products`                   | Add product to shop   |
| GET    | `/api/shops/{shopId}/products`                   | List shop products    |
| GET    | `/api/shops/{shopId}/products/{productId}`       | Get product by ID     |
| PATCH  | `/api/shops/{shopId}/products/{productId}/stock` | Update stock          |
| DELETE | `/api/shops/{shopId}/products/{productId}`       | Delete product        |

### User Service — `/api/users`

| Method | Path               | Description               |
|--------|--------------------|---------------------------|
| POST   | `/api/users`       | Register a user           |
| GET    | `/api/users`       | List all active users     |
| GET    | `/api/users/{id}`  | Get user by ID            |
| PUT    | `/api/users/{id}`  | Update name/phone/address |
| DELETE | `/api/users/{id}`  | Soft-delete a user        |

### Order Service — `/api/orders`

| Method | Path                          | Description                   |
|--------|-------------------------------|-------------------------------|
| POST   | `/api/orders`                 | Place an order                |
| GET    | `/api/orders/{id}`            | Get order by ID               |
| GET    | `/api/orders?userId={uuid}`   | Get all orders for a customer |
| GET    | `/api/orders?shopId={shopId}` | Get all orders for a shop     |
| PATCH  | `/api/orders/{id}/status`     | Update order status           |

---

## Key Design Decisions

**No foreign keys across services** — `userId` in the orders table has no DB-level FK to user-service's database. Referential integrity is enforced at the application level via Feign calls.

**Price snapshot** — when an order is placed, `productName` and `unitPrice` are copied from the product at that moment. If a shop changes its price later, existing orders are unaffected.

**Soft delete** — shops and users are never physically deleted. An `active` flag is set to `false`. Hard delete is used for products (so the same product name can be re-added later).

**Circuit Breaker** — order-service uses Resilience4j to protect Feign calls to shop-service and user-service. If a downstream service is down, the circuit opens and callers get HTTP 503 instead of waiting for a timeout.

**Error format** — all services return [RFC 7807 ProblemDetail](https://datatracker.ietf.org/doc/html/rfc7807) errors. Consistent shape across all endpoints.

**UTC everywhere** — all timestamps stored in UTC. `TimeZone.setDefault(UTC)` runs before Spring starts in each service.

---

## Databases

| Database         | Used by                   | Credentials                           |
|------------------|---------------------------|---------------------------------------|
| shop-service     | shop-service (MongoDB)    | user: localmart / pass: localmart123  |
| localmart_users  | user-service (PostgreSQL) | user: localmart / pass: localmart123  |
| localmart_orders | order-service (PostgreSQL)| user: localmart / pass: localmart123  |

> Flyway manages schema migrations automatically on startup for PostgreSQL services.
> Migration files live in `src/main/resources/db/migration/`.

---

## Build Commands

```bash
# Build all services
mvn clean install -DskipTests

# Build a single service (and its dependencies)
mvn clean install -DskipTests -pl order-service -am

# Run a specific service
mvn spring-boot:run -pl shop-service

# Run tests
mvn test -pl order-service
```

---

## Roadmap

Currently live: service discovery, API gateway, shop catalog, user profiles, order management with sync inter-service calls and circuit breaking.

Coming soon: payment processing, email notifications, distributed tracing, metrics dashboards, authentication, full containerization, and cloud deployment.

---

## FAQs

**Q: Why does discovery-server need to start first?**
All other services register with Eureka on startup. If Eureka isn't running, they log warnings and retry — but routes via the gateway won't resolve until registration succeeds.

**Q: Why Spring Boot 4.x and not 3.x?**
Spring Boot 4.x is the current stable release. It requires Java 17+ (we use 21). The web starter was renamed from `spring-boot-starter-web` to `spring-boot-starter-webmvc` in this version.

**Q: What is `ddl-auto: validate`?**
Hibernate checks that the database schema matches the entity definitions on every startup, but does not modify the schema. Flyway owns all schema changes via migration files.

**Q: Can I call services directly without going through the gateway?**
Yes — during development, direct calls to `localhost:8081`, `8082`, `8083` work fine. The gateway (`localhost:8080`) is the production entry point and will later enforce authentication.

**Q: What does `process-aot` do? Should I run it?**
AOT (Ahead of Time) is for building GraalVM native binaries — not used in this project's current setup. Never run `spring-boot:process-aot` during normal development. Use `spring-boot:run` or `clean install` only.
v-i