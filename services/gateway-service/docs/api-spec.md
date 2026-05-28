# Gateway API Specification

# Overview

This document defines the routes exposed through the API Gateway.

All external traffic enters through the gateway.

Authentication:
```http
Authorization: Bearer <access_token>
```

---

# Route Inventory

---

# Authentication APIs

## Keycloak Login

```http
POST /realms/gringotts/protocol/openid-connect/token
```

Purpose:
- obtain JWT access token

Rate Limited:
- yes

Authentication:
- public

---

# User Service APIs

## Route Prefix

```text
/api/v1/users/**
```

---

## Authorization Rules

| Route Pattern | Access |
|---|---|
| /admin/** | ROLE_ADMIN |
| /user/** | ROLE_USER |
| /shared/** | Authenticated |

---

## Example Endpoints

### Create User

```http
POST /api/v1/users/admin/createUser
```

Access:
- ROLE_ADMIN

---

### Current User Profile

```http
GET /api/v1/users/user/profile
```

Access:
- ROLE_USER

---

### Ping Endpoint

```http
GET /api/v1/users/shared/ping
```

Access:
- authenticated users

---

# Transaction Service APIs

## Route Prefix

```text
/api/v1/transactions/**
```

Authentication:
- required

Authorization:
- role-based

---

# Risk Service APIs

## Route Prefix

```text
/risk/api/**
```

Authorization:
- ROLE_ADMIN

Additional Validation:
- audience validation
- azp validation
- service validation

---

## Example Endpoint

```http
GET /risk/api/v1/decisions
```

Purpose:
- fetch risk decisions

Security:
- admin only

---

# Notification Service APIs

## Route Prefix

```text
/api/v1/notifications/**
```

Authorization:
- ROLE_ADMIN

---

# Observability APIs

## Route Prefix

```text
/api/v1/observability/**
```

Authorization:
- ROLE_ADMIN

---

# Actuator Endpoints

## Health

```http
GET /actuator/health
```

Access:
- public

---

## Prometheus Metrics

```http
GET /actuator/prometheus
```

Access:
- protected internally

---

# Common Headers

## Request Headers

| Header | Purpose |
|---|---|
| Authorization | JWT token |
| X-Correlation-ID | request tracing |

---

## Response Headers

| Header | Purpose |
|---|---|
| X-Frame-Options | clickjacking protection |
| X-Content-Type-Options | MIME protection |
| Cache-Control | cache prevention |
| X-Correlation-ID | tracing |

---

# Error Responses

## 401 Unauthorized

```json
{
  "timestamp": "...",
  "status": 401,
  "message": "Invalid or expired token",
  "correlationId": "..."
}
```

---

## 403 Forbidden

```json
{
  "timestamp": "...",
  "status": 403,
  "message": "Access denied",
  "correlationId": "..."
}
```

---

## 429 Too Many Requests

```json
{
  "timestamp": "...",
  "status": 429,
  "message": "Rate limit exceeded",
  "correlationId": "..."
}
```

---

## 503 Service Unavailable

```json
{
  "timestamp": "...",
  "status": 503,
  "message": "Service temporarily unavailable",
  "correlationId": "..."
}
```

---

# Retry Policy

Retries are enabled ONLY for:
- GET requests
- idempotent APIs

Retries are NOT enabled for:
- payments
- money movement
- transactional mutations

---

# Circuit Breaker Strategy

Protected services:
- user-service
- transaction-service
- risk-service

Fallback endpoint:
```text
/fallback/service-unavailable
```

---

# Rate Limiting Strategy

Rate limiting implemented using:
- Redis
- token bucket algorithm

Protection types:
- IP throttling
- user throttling

---

# JWT Claims Used

| Claim | Purpose |
|---|---|
| sub | internal identity |
| preferred_username | display/logging |
| realm_access.roles | authorization |
| aud | audience validation |
| azp | client validation |

---

# Security Model

The gateway acts as:
- centralized authentication boundary
- centralized authorization layer
- centralized traffic controller

Sensitive services additionally validate:
- audience
- client identity
- service roles