# LocalMart — Bruno Collection

API collection for the LocalMart microservices platform.
Covers shop-service, user-service, and order-service with happy-path and validation requests.

---

## Setup

1. Open Bruno → **Open Collection** → select the `localmart/bruno` folder
2. Select environment: **Local**
3. Make sure all services are running (see project README)

---

## Environment Variables

These are stored in `environments/Local.bru` and shared across all requests.

| Variable        | Value                   | Set by                          |
|-----------------|-------------------------|---------------------------------|
| `base_url`      | http://localhost:8081   | manual                          |
| `user_base_url` | http://localhost:8082   | manual                          |
| `order_base_url`| http://localhost:8083   | manual                          |
| `gateway_url`   | http://localhost:8080   | manual                          |
| `muruganShopId` | *(auto-set)*            | `01-Create Murugan Stores`      |
| `sriLakshmiShopId` | *(auto-set)*         | `02-Create Sri Lakshmi Stores`  |
| `ammaShopId`    | *(auto-set)*            | `03-Create Amma Departmental`   |
| `lastProductId` | *(auto-set)*            | any product creation request    |
| `lastUserId`    | *(auto-set)*            | any user registration request   |
| `muruganUserId` | *(auto-set)*            | `02-Register Shop Owner Murugan`|
| `lastOrderId`   | *(auto-set)*            | `01-Place Order` in order-service |

Variables marked *auto-set* are saved by `script:post-response` blocks in each request — run the requests in sequence and the IDs flow forward automatically.

---

## Recommended Run Order

Run these in sequence to build up the full dataset before testing orders.

### Step 1 — Create shops

```
shop-service / shops /
  01-Create Murugan Stores        → sets muruganShopId
  02-Create Sri Lakshmi Stores    → sets sriLakshmiShopId
  03-Create Amma Departmental     → sets ammaShopId
```

### Step 2 — Add products

```
shop-service / products /
  01-Add Ponni Rice to Murugan    → sets lastProductId  ← use this for orders
  02-Add Filter Coffee to Murugan
  03-Add Toor Dal to Sri Lakshmi
  04-Add Coconut Oil to Sri Lakshmi
  05-Add Idli Batter to Amma
```

### Step 3 — Register users

```
user-service / users /
  01-Register Customer (Ravi)     → sets lastUserId  ← use this for orders
  02-Register Shop Owner (Murugan)→ sets muruganUserId
  03-Register Customer (Priya)
```

### Step 4 — Place and manage orders

```
order-service / orders /
  01-Place Order (Ravi buys Ponni Rice)   → needs lastUserId + muruganShopId + lastProductId
                                          → sets lastOrderId
  02-Get Order by ID                      → uses lastOrderId
  03-Get Orders by User                   → uses lastUserId
  04-Get Orders by Shop                   → uses muruganShopId
  05-Update Order Status (Confirm)        → uses lastOrderId
```

---

## Validation Requests

Each service has a `validation/` folder with requests that test error handling.
These should return structured RFC 7807 ProblemDetail errors — never raw stack traces.

### shop-service / validation
| Request | Expected response |
|---------|------------------|
| Invalid Phone Number | 400 — validation errors map |
| Missing Required Fields | 400 — validation errors map |
| Negative Price | 400 — validation errors map |
| Shop Not Found | 404 — Resource Not Found |

### user-service / validation
| Request | Expected response |
|---------|------------------|
| Invalid Email | 400 — validation errors map |
| Invalid Phone | 400 — validation errors map |
| Duplicate Email | 409 — Duplicate Resource |
| User Not Found | 404 — Resource Not Found |

### order-service / validation
| Request | Expected response |
|---------|------------------|
| Invalid Quantity (Zero) | 400 — validation errors map |
| Order Not Found | 404 — Resource Not Found |
| Invalid User ID | 503 — Service Unavailable (circuit breaker) or 404 from user-service |

---

## Expected Error Shape (RFC 7807)

All error responses follow this format:

```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Order not found with id: 00000000-0000-0000-0000-000000000000",
  "instance": "/api/orders/00000000-0000-0000-0000-000000000000"
}
```

Validation errors include an additional `errors` field:

```json
{
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields have invalid values",
  "errors": {
    "quantity": "must be greater than or equal to 1",
    "userId": "must not be null"
  }
}
```

---

## Demo: Circuit Breaker in Action

1. Stop user-service (`Ctrl+C` in its terminal)
2. Send `01-Place Order` from Bruno
3. You get HTTP 503 with `"title": "Service Unavailable"`
4. Restart user-service
5. After a few seconds, the circuit closes — orders go through again

This demonstrates the OPEN → HALF_OPEN → CLOSED circuit breaker cycle.

---

## Tips

- The `gateway_url` variable routes through the API Gateway on port 8080. You can swap `base_url` for `gateway_url` in any request to test routing.
- If a variable like `lastProductId` is empty, run the corresponding creation request first.
- Bruno saves environment variables between sessions — you don't need to re-run setup requests every time unless you restart fresh databases.
