# Request Flow Documentation

# Overview

This document explains the complete lifecycle of requests flowing through the Gateway Service.

The gateway acts as:

- authentication boundary
- authorization layer
- resilience layer
- traffic controller
- observability layer

All external requests must pass through the gateway.

---

# High-Level Request Lifecycle

```text
Client
  ↓
Gateway
  ↓
CorrelationIdFilter
  ↓
Spring Security
  ↓
JWT Validation
  ↓
Role Authorization
  ↓
Rate Limiter
  ↓
Circuit Breaker
  ↓
Retry Filter
  ↓
Route Matching
  ↓
Downstream Service
  ↓
Gateway Logging
  ↓
Client Response
```

---

# Step-by-Step Request Flow

---

# 1. Client Sends Request

Example:

```http
GET /risk/api/v1/decisions
Authorization: Bearer <jwt>
```

The request enters the gateway.

---

# 2. Correlation ID Generation

Handled by:

```text
CorrelationIdFilter
```

Purpose:
- request tracing
- distributed debugging
- log correlation

Behavior:
- generates UUID
- injects `X-Correlation-ID`
- stores ID in MDC logging context

Example:

```http
X-Correlation-ID: 51294e84-4a10-4b4a-8bc9-c8a0e3d18826
```

---

# 3. Spring Security Filter Chain

The request enters:

```text
SecurityWebFilterChain
```

Responsibilities:
- JWT extraction
- JWT validation
- role extraction
- route authorization

---

# 4. JWT Validation

The gateway validates:

- signature
- issuer
- expiration

Using:

```text
OAuth2 Resource Server
```

with:

```text
NimbusJwtDecoder
```

---

# 5. Role Extraction

Handled by:

```text
JwtAuthConverter
```

Roles extracted from:

```json
realm_access.roles
```

Example:

```json
"roles": [
  "ADMIN",
  "USER"
]
```

Converted into:

```text
ROLE_ADMIN
ROLE_USER
```

---

# 6. Route Authorization

Authorization rules:

| Route Type | Required Role |
|---|---|
| /admin/** | ROLE_ADMIN |
| /user/** | ROLE_USER |
| /shared/** | Authenticated |

Unauthorized requests return:
- 401 Unauthorized
- 403 Forbidden

---

# 7. Rate Limiting

Handled by:

```text
RequestRateLimiter
```

Backed by:
- Redis
- token bucket algorithm

---

# Rate Limiting Strategies

## Login APIs

Uses:
```text
ipKeyResolver
```

Purpose:
- brute-force protection

---

## User APIs

Uses:
```text
userKeyResolver
```

Purpose:
- per-user throttling
- abuse prevention

---

# 8. Circuit Breaker Protection

Implemented using:

```text
Resilience4j
```

Purpose:
- prevent cascading failures
- isolate downstream instability

Protected services:
- user-service
- transaction-service
- risk-service

---

# Circuit Breaker Flow

```text
Repeated Failures
  ↓
Failure Threshold Reached
  ↓
Circuit Opens
  ↓
Requests Short-Circuited
  ↓
Fallback Returned
```

---

# 9. Retry Filter

Retries enabled ONLY for:

- GET requests
- idempotent operations

Reason:
- prevent duplicate financial operations

Retry Conditions:
- BAD_GATEWAY
- GATEWAY_TIMEOUT

---

# Retry Flow

```text
GET Request Fails
  ↓
Retry Attempt 1
  ↓
Retry Attempt 2
  ↓
Failure Persists
  ↓
Circuit Breaker Evaluates
```

---

# 10. Route Matching

Gateway route predicates determine destination service.

Example:

```yaml
Path=/risk/api/**
```

Routes request to:

```text
risk-service
```

---

# 11. Downstream Service Validation

Sensitive services implement additional validation.

Example:
risk-service validates:

- audience
- azp claim
- service identity
- service roles

This implements:
```text
zero-trust service security
```

---

# 12. Response Processing

Response passes back through:
- logging filters
- header filters
- security headers

Headers added:
- X-Frame-Options
- X-Content-Type-Options
- Cache-Control
- X-Correlation-ID

---

# 13. Request Logging

Handled by:

```text
RequestLoggingFilter
```

Logs:
- timestamp
- path
- latency
- status
- user
- correlation ID
- client IP

Example:

```text
API_GATEWAY_REQUEST_LOG {
  timestamp=...,
  method=GET,
  path=/risk/api/v1/decisions,
  status=200,
  latencyMs=12,
  correlationId=...
}
```

---

# Failure Handling

---

# Authentication Failure

Example:
- expired JWT
- invalid signature

Response:

```http
401 Unauthorized
```

---

# Authorization Failure

Example:
- USER accessing admin route

Response:

```http
403 Forbidden
```

---

# Rate Limit Failure

Example:
- request burst exceeded

Response:

```http
429 Too Many Requests
```

---

# Downstream Failure

Example:
- service unavailable

Response:

```http
503 Service Unavailable
```

via fallback endpoint.

---

# Fallback Strategy

Fallback endpoint:

```text
/fallback/service-unavailable
```

Purpose:
- graceful degradation
- controlled failure handling

---

# Reactive Execution Model

The gateway uses:
```text
non-blocking reactive execution
```

Benefits:
- efficient concurrency
- lower thread consumption
- scalable routing

The gateway does NOT allocate one thread per request.

---

# Cloud Readiness

The request flow is designed for:
- Kubernetes
- AWS ECS/EKS
- horizontal scaling
- distributed deployment

Stateless JWT authentication enables:
- load balancer compatibility
- auto scaling
- container orchestration