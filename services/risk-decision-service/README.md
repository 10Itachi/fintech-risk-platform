# security.md

# Security Architecture

# Security Overview

The `risk-decision-service` uses a production-style stateless security architecture built using:

```text
Spring Security + OAuth2 Resource Server + JWT
```

The service supports both:

- service-to-service authentication
- user-context authentication through API gateway

The implementation includes:

- JWT signature validation
- issuer validation
- audience validation
- client validation
- role-based authorization
- custom JWT validation
- custom JWT authority extraction
- centralized authentication failure handling

---

# Security Architecture Flow

```text
Incoming Request
        |
        v
OAuth2 Resource Server
        |
        v
JWT Signature Validation
        |
        v
Issuer Validation
        |
        v
CustomJwtValidator
        |
        v
JwtAuthConverter
        |
        v
Spring Security Authorization
        |
        v
Controller Access
```

---

# Security Components

| Component | Responsibility |
|---|---|
| RiskServiceSecurityConfig | Main Spring Security configuration |
| CustomJwtValidator | Custom token validation |
| JwtAuthConverter | JWT role extraction |
| JwtDecoder | JWT decoding and validation |
| AuthenticationEntryPoint | Handles authentication failures |
| AccessDeniedHandler | Handles authorization failures |

---

# Authentication Architecture

The service acts as:

```text
OAuth2 Resource Server
```

JWT tokens are validated using:

```text
NimbusJwtDecoder
```

Validation sources:

- issuer URI
- JWK Set URI

---

# JWT Validation Pipeline

The JWT validation process contains two layers.

---

# 1. Default Spring Security Validation

Handled automatically using:

```java
JwtValidators.createDefaultWithIssuer()
```

Performs:

| Validation | Purpose |
|---|---|
| Signature Validation | Verifies token authenticity |
| Expiry Validation | Rejects expired tokens |
| Issuer Validation | Ensures trusted issuer |

---

# 2. Custom JWT Validation

Implemented using:

```text
CustomJwtValidator
```

Adds domain-specific validation logic.

---

# Custom JWT Validation Rules

# Audience Validation

Ensures token audience contains:

```text
risk-decision-service
```

Purpose:

- prevents token misuse
- prevents cross-service token replay
- ensures token intended for this service

---

# Client Validation

The validator extracts:

```text
azp
```

claim from JWT.

Allowed clients:

| Client | Purpose |
|---|---|
| api-gateway | User-context requests |
| transaction-service | Service-to-service requests |

Unknown clients are rejected.

---

# User Token Validation

When client:

```text
api-gateway
```

is detected:

Validation checks:

- subject presence
- authenticated user existence

User roles are later validated through Spring Security authorization.

---

# Service Token Validation

When client:

```text
transaction-service
```

is detected:

The validator checks:

```text
resource_access
```

claim.

Expected role:

```text
RISKCALLER
```

Required structure:

```json
{
  "resource_access": {
    "risk-decision-service": {
      "roles": ["RISKCALLER"]
    }
  }
}
```

---

# Invalid Token Conditions

The following conditions reject authentication:

| Condition | Error |
|---|---|
| Missing audience | invalid_audience |
| Missing azp | invalid_token |
| Missing subject | invalid_user |
| Missing service role | invalid_role |
| Unknown client | invalid_client |

---

# Authorization Architecture

Authorization is configured using:

```java
authorizeHttpRequests()
```

---

# Endpoint Authorization Rules

| Endpoint | Access Rule |
|---|---|
| `/v3/api-docs/**` | Permit All |
| `/swagger-ui/**` | Permit All |
| `/actuator/**` | Permit All |
| `/risk/evaluate` | ROLE_RISKCALLER |
| `/risk/admin/**` | ROLE_ADMIN |
| All Other APIs | Authenticated |

---

# JWT Authority Extraction

Implemented using:

```text
JwtAuthConverter
```

The converter extracts authorities from:

- realm roles
- client roles

---

# Realm Role Extraction

Extracted from:

```json
{
  "realm_access": {
    "roles": ["USER", "ADMIN"]
  }
}
```

Purpose:

- user authorization
- admin access control

---

# Client Role Extraction

Extracted from:

```json
{
  "resource_access": {
    "risk-decision-service": {
      "roles": ["RISKCALLER"]
    }
  }
}
```

Purpose:

- service authorization
- microservice communication security

---

# Spring Authority Mapping

Extracted roles are converted into:

```text
ROLE_<ROLE_NAME>
```

Examples:

| JWT Role | Spring Authority |
|---|---|
| ADMIN | ROLE_ADMIN |
| USER | ROLE_USER |
| RISKCALLER | ROLE_RISKCALLER |

---

# Security Filter Chain

```text
Incoming Request
        |
        v
Bearer Token Extraction
        |
        v
JWT Decoder
        |
        v
Default Validators
        |
        v
CustomJwtValidator
        |
        v
JwtAuthConverter
        |
        v
Authorization Rules
        |
        v
Controller
```

---

# Authentication Failure Handling

Authentication failures are handled using:

```text
AuthenticationEntryPoint
```

Handles:

- invalid tokens
- expired tokens
- malformed JWTs
- audience failures
- issuer failures

---

# Example 401 Response

```json
{
  "status": 401,
  "message": "Invalid or expired token"
}
```

---

# Authorization Failure Handling

Authorization failures are handled using:

```text
AccessDeniedHandler
```

Handles:

- insufficient roles
- forbidden access
- privilege violations

---

# Example 403 Response

```json
{
  "status": 403,
  "message": "Insufficient permissions"
}
```

---

# Stateless Security Design

The service is fully stateless.

Characteristics:

- no HTTP session storage
- token-driven authentication
- horizontally scalable
- gateway-compatible
- cloud-native friendly

---

# Security Logging

The security layer logs:

- security initialization
- JWT decoder configuration
- unauthorized access attempts
- forbidden access attempts

Purpose:

- auditability
- operational debugging
- security monitoring

---

# Security Configuration Flow

```text
application.yml
        |
        v
Issuer URI
JWK Set URI
        |
        v
JwtDecoder
        |
        v
Spring Security Resource Server
```

---

# Security Strengths

| Capability | Benefit |
|---|---|
| Audience Validation | Prevents token misuse |
| Client Validation | Prevents unauthorized services |
| Role Validation | Fine-grained authorization |
| Stateless Design | Horizontal scalability |
| OAuth2 Resource Server | Enterprise-standard security |
| JWT-based Security | Decoupled authentication |

---

# Production Security Recommendations

## Recommended Enhancements

- API gateway rate limiting
- mTLS internal communication
- rotating signing keys
- secret vault integration
- distributed authorization
- centralized audit logging
- WAF integration
- token revocation strategy
- SIEM integration

---

# Security Characteristics Summary

| Characteristic | Status |
|---|---|
| Stateless | YES |
| OAuth2 Compatible | YES |
| JWT Based | YES |
| Audience Validation | YES |
| Service Authorization | YES |
| User Authorization | YES |
| Custom Validators | YES |
| Role-Based Access | YES |
| Production Ready | YES |