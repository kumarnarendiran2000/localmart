# LocalMart — Dev Quick Reference

---

## Services

| Service              | Port | Base URL                    | Swagger UI                               | Health                                        |
| -------------------- | ---- | --------------------------- | ---------------------------------------- | --------------------------------------------- |
| discovery-server     | 8761 | http://localhost:8761       | —                                        | —                                             |
| api-gateway          | 8080 | http://localhost:8080       | —                                        | http://localhost:8080/actuator/health         |
| shop-service         | 8081 | http://localhost:8081       | http://localhost:8081/swagger-ui.html    | http://localhost:8081/actuator/health         |
| user-service         | 8082 | http://localhost:8082       | http://localhost:8082/swagger-ui.html    | http://localhost:8082/actuator/health         |
| order-service        | 8083 | http://localhost:8083       | http://localhost:8083/swagger-ui.html    | http://localhost:8083/actuator/health         |

---

## Databases

### PostgreSQL

| Property | Value             |
| -------- | ----------------- |
| Host     | `localhost`       |
| Port     | `5432`            |
| Username | `localmart`       |
| Password | `localmart123`    |

| Database          | Used by       |
| ----------------- | ------------- |
| `localmart_users`  | user-service  |
| `localmart_orders` | order-service |

### MongoDB

| Property | Value            |
| -------- | ---------------- |
| Host     | `localhost`      |
| Port     | `27017`          |
| Username | `localmart`      |
| Password | `localmart123`   |

| Database / Collection | Used by      |
| --------------------- | ------------ |
| `shop-service`        | shop-service (created automatically on first run) |

---

## Database GUIs

| Tool          | URL                       | Username             | Password   |
| ------------- | ------------------------- | -------------------- | ---------- |
| pgAdmin 4     | http://localhost:5050     | `admin@localmart.dev` | `admin123` |
| mongo-express | http://localhost:8091     | `admin`              | `pass`     |

> **pgAdmin — register server once:**
> Right-click Servers → Register → Server
> Host: `localmart-postgres` · Port: `5432` · Username: `localmart` · Password: `localmart123`

---

## Kafka

| Item            | Value                                          |
| --------------- | ---------------------------------------------- |
| Broker (host)   | `localhost:9092`                               |
| Broker (Docker) | `localmart-kafka:29092`                        |
| Kafka UI        | [http://localhost:8090](http://localhost:8090) |

**Topics:**

| Topic               | Published by    | Consumed by      |
| ------------------- | --------------- | ---------------- |
| `order.placed`      | order-service   | payment-service  |
| `payment.processed` | payment-service | order-service    |

> **Why two ports?**
> Host apps (Java services running locally) use `localhost:9092`.
> Docker containers talking to each other use `localmart-kafka:29092`.
> `localhost` inside a container refers to the container itself, not the Kafka container.

---

## Eureka Dashboard

http://localhost:8761 — shows all registered services and their instance status.

---

## Circuit Breaker Actuator (order-service)

| Endpoint                                                                   | What it shows                              |
| -------------------------------------------------------------------------- | ------------------------------------------ |
| http://localhost:8083/actuator/circuitbreakers                             | State of all CB instances (CLOSED / OPEN)  |
| http://localhost:8083/actuator/circuitbreakerevents/user-service           | Recent call events for user-service CB     |
| http://localhost:8083/actuator/circuitbreakerevents/shop-service           | Recent call events for shop-service CB     |

---

## Docker

```bash
# Start all infra containers
docker compose -f docker-compose.infra.yml up -d

# Start only DB GUI containers (if infra already running)
docker compose -f docker-compose.infra.yml up -d pgadmin mongo-express

# Stop all
docker compose -f docker-compose.infra.yml down

# Create PostgreSQL databases (first time only)
docker exec -it localmart-postgres psql -U localmart -c "CREATE DATABASE localmart_users;"
docker exec -it localmart-postgres psql -U localmart -c "CREATE DATABASE localmart_orders;"
```

---

## Maven

```bash
# Build all services
mvn clean install -DskipTests

# Build single service
mvn clean install -DskipTests -pl order-service -am

# Run a service
mvn spring-boot:run -pl shop-service

# Start order: discovery-server first, then the rest in any order
```
