# Security Documentation

# Overview

The Gateway Service acts as the primary security boundary for the Gringotts platform.

Responsibilities:
- authentication
- authorization
- JWT validation
- route protection
- security hardening

All external traffic must pass through gateway security.

---

# Authentication Architecture

Authentication uses:

- OAuth2 Resource Server
- JWT Bearer Tokens
- Keycloak

The gateway validates:
- JWT signature
- JWT issuer
- JWT expiration

before routing requests.

---

# Keycloak Integration

Keycloak acts as:
- Identity Provider (IdP)
- OAuth2 Authorization Server

Client login flow:

```text
Client
  ↓
Keycloak Login
  ↓
JWT Issued
  ↓
JWT Sent To Gateway
  ↓
Gateway Validation
```

---

# JWT Validation

JWT validation uses:

```text
NimbusJwtDecoder
```

Configured through:

```yaml
spring.security.oauth2.resourceserver.jwt.issuer-uri
```

---

# JWT Claims Used

| Claim | Purpose |
|---|---|
| sub | Internal identity |
| preferred_username | Display/logging |
| realm_access.roles | Authorization |
| aud | Audience validation |
| azp | Client validation |

---

# Why sub Claim Is Used

The gateway and services use:

```text
jwt.getSubject()
```

for internal identity.

Reason:
- immutable
- globally unique
- username-independent

Benefits:
- username changes do not break identity mapping
- safer database relationships

---

# Role Extraction

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

Mapped into Spring authorities:

```text
ROLE_ADMIN
ROLE_USER
```

---

# Authorization Model

Authorization is route-based.

---

# Route Protection Rules

| Route Pattern | Access |
|---|---|
| /api/v1/users/admin/** | ROLE_ADMIN |
| /api/v1/users/user/** | ROLE_USER |
| /api/v1/users/shared/** | Authenticated |
| /risk/api/** | ROLE_ADMIN |

---

# Security Filter Chain

Implemented using:

```text
SecurityWebFilterChain
```

Responsibilities:
- disable stateful auth
- JWT validation
- route authorization
- security headers

---

# Stateless Authentication

The gateway stores:
- no sessions
- no request state
- no authentication state

Authentication is fully JWT-based.

Benefits:
- horizontal scalability
- cloud-native deployment
- load balancer compatibility

---

# Security Headers

Implemented headers:

| Header | Purpose |
|---|---|
| X-Frame-Options | Prevent clickjacking |
| X-Content-Type-Options | Prevent MIME sniffing |
| Cache-Control | Disable sensitive caching |
| HSTS | Enforce HTTPS |

---

# HSTS Configuration

Configured using:

```text
Strict-Transport-Security
```

Purpose:
- enforce HTTPS usage
- prevent downgrade attacks

---

# Route Isolation

Sensitive routes are intentionally hidden.

Examples:
- Kafka admin APIs
- internal actuator APIs
- service internals

Only gateway-approved routes are exposed.

---

# Audience Validation

risk-service implements additional:
```text
audience validation
```

Purpose:
- zero-trust service security
- prevent unauthorized token reuse

Example:

```json
"aud": [
  "risk-decision-service"
]
```

---

# azp Validation

Sensitive services validate:

```json
"azp"
```

Purpose:
- verify requesting client identity
- restrict unauthorized service access

---

# Service-to-Service Security

risk-service validates:
- service audience
- service roles
- calling service identity

This protects internal fraud-analysis APIs.

---

# Why Retry Is Limited To GET

Retries are enabled ONLY for:
- GET requests
- idempotent APIs

Retries are NOT enabled for:
- payments
- financial mutations
- transactional writes

Purpose:
- prevent duplicate transactions

---

# Correlation ID Security Benefits

Correlation IDs help:
- forensic debugging
- request tracing
- incident analysis
- distributed diagnostics

---

# Error Response Security

Global exception handling ensures:
- no stack traces exposed
- no internal exceptions leaked
- consistent API responses

Example:

```json
{
  "timestamp": "...",
  "status": 401,
  "message": "Unauthorized",
  "correlationId": "..."
}
```

---

# Threat Mitigations

Implemented protections:

| Threat | Protection |
|---|---|
| Brute-force login | Rate limiting |
| Token replay | JWT expiration |
| Unauthorized access | RBAC |
| Clickjacking | X-Frame-Options |
| MIME attacks | X-Content-Type-Options |
| Cascading failures | Circuit breakers |

---

# Security Architecture Goals

The security design prioritizes:
- centralized enforcement
- stateless authentication
- cloud readiness
- distributed trust boundaries
- zero-trust principles
- operational simplicity