# security.md

# Security Architecture

# Security Overview

The service uses:

```text
Spring Security + OAuth2 Resource Server + JWT
```

The system follows stateless authentication architecture suitable for microservice deployments.

---

# Security Flow

```text
Incoming Request
        |
        v
JWT Authentication Filter
        |
        v
Token Validation
        |
        v
Security Context Initialization
        |
        v
Controller Access
```

---

# Authentication Mechanism

Authentication is performed using JWT bearer tokens.

Validation includes:

- signature validation
- issuer validation
- expiration validation
- claims validation

---

# Authorization Strategy

Endpoints are protected using role/authority-based access control.

Examples:

| Endpoint | Access |
|---|---|
| `/risk/evaluate` | AUTHENTICATED |
| `/risk/decisions/**` | INTERNAL_SERVICE |
| `/actuator/**` | ADMIN |

---

# Security Characteristics

- Stateless authentication
- No session storage
- Token-based authorization
- Scalable microservice security
- Gateway-compatible authentication

---

# Correlation ID Security

Each request carries:

```text
X-Correlation-Id
```

Purpose:

- audit linking
- request tracing
- operational debugging

---

# Sensitive Data Protection

Sensitive fields are protected through:

- structured logging
- partial masking
- DTO boundary validation
- secure serialization

---

# Validation Security

Request validation prevents:

- malformed payloads
- invalid transaction structures
- illegal input values
- schema violations

---

# Exception Security

Security exceptions are standardized.

Example:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid JWT token"
}
```

---

# Recommended Production Enhancements

- API gateway integration
- mTLS service communication
- secret vault integration
- rotating signing keys
- rate limiting
- WAF integration
- distributed authorization