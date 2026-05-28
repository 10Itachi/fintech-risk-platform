# security.md

# Security Architecture

# Overview

The `transaction-service` implements:

```text
Production-grade stateless microservice security
```

using:

- Spring Security
- OAuth2 Resource Server
- JWT authentication
- role-based authorization
- machine-to-machine authentication
- secure Feign propagation

The security architecture is designed for:

- secure API access
- trusted inter-service communication
- distributed authentication
- stateless authorization
- scalable microservice security

---

# Security Goals

| Goal | Purpose |
|---|---|
| API Protection | Prevent unauthorized access |
| JWT Validation | Verify trusted requests |
| Role-Based Authorization | Restrict API access |
| Service Authentication | Secure inter-service communication |
| Stateless Security | Horizontal scalability |
| Distributed Trust | Secure microservice ecosystem |

---

# High-Level Security Architecture

```text
Client / Service Request
        |
        v
Spring Security Filter Chain
        |
        v
JWT Authentication Filter
        |
        v
JWT Validation
        |
        v
JwtAuthConverter
        |
        v
Granted Authorities
        |
        v
SecurityContext
        |
        v
Controller Access
```

---

# Security Components

| Component | Responsibility |
|---|---|
| SecurityConfig | Main security configuration |
| JwtAuthConverter | JWT-to-authority mapping |
| SecurityUtils | Authentication helper utilities |
| OpenApiConfig | Swagger/OpenAPI security configuration |

---

# ==========================================
# 1. SecurityConfig
# ==========================================

# Purpose

Acts as:

```text
Central Spring Security configuration layer
```

Responsible for:

- authentication rules
- authorization policies
- JWT resource-server setup
- stateless security configuration
- endpoint protection

---

# Main Responsibilities

| Responsibility | Purpose |
|---|---|
| HTTP Security Rules | API access control |
| JWT Resource Server | Token validation |
| Stateless Sessions | Scalable security |
| Endpoint Authorization | Route protection |
| Swagger Security Rules | API documentation access |

---

# High-Level Flow

```text
Incoming Request
        |
        v
SecurityFilterChain
        |
        v
JWT Validation
        |
        v
Authority Mapping
        |
        v
Authorization Check
        |
        v
Controller Access
```

---

# Stateless Security

The service uses:

```java
SessionCreationPolicy.STATELESS
```

Meaning:

```text
No server-side session storage
```

Each request must independently provide JWT token.

---

# Why Stateless Security Is Important

Benefits:

- horizontal scalability
- no session replication
- cloud-native architecture
- simpler distributed deployment

---

# Protected Endpoints

Most APIs require:

```text
Authenticated JWT token
```

---

# Public Endpoints

Typically allowed publicly:

- Swagger UI
- OpenAPI docs
- health endpoints

---

# Example Security Flow

```text
HTTP Request
      |
      v
Authorization Header
      |
      v
Bearer JWT Token
      |
      v
JWT Validation
      |
      v
Role Extraction
      |
      v
Controller Authorization
```

---

# OAuth2 Resource Server

The service acts as:

```text
OAuth2 JWT Resource Server
```

Meaning:

```text
Service validates JWT tokens
```

instead of creating user sessions.

---

# JWT Validation Responsibilities

The resource server validates:

| Validation | Purpose |
|---|---|
| Signature Validation | Prevent token tampering |
| Expiration Validation | Reject expired tokens |
| Issuer Validation | Ensure trusted issuer |
| Audience Validation | Ensure intended target |
| Role Extraction | Authorization |

---

# Why JWT Is Used

JWT enables:

- stateless authentication
- scalable distributed security
- machine-to-machine trust
- secure microservice communication

---

# ==========================================
# 2. JwtAuthConverter
# ==========================================

# Purpose

Acts as:

```text
JWT-to-Spring-Authority mapper
```

Responsible for:

- extracting roles from JWT
- converting claims into authorities
- integrating JWT claims with Spring Security

---

# Why This Class Exists

JWT tokens contain:

```text
Raw claims
```

Spring Security requires:

```text
GrantedAuthority objects
```

This class bridges that gap.

---

# High-Level Flow

```text
JWT Token
      |
      v
Extract Claims
      |
      v
Extract Roles
      |
      v
Convert to GrantedAuthority
      |
      v
Attach to SecurityContext
```

---

# Example JWT Roles

```json
{
  "roles": [
    "ADMIN",
    "RISKCALLER"
  ]
}
```

---

# Conversion Result

Converted into:

```text
ROLE_ADMIN
ROLE_RISKCALLER
```

used by Spring Security.

---

# Why ROLE_ Prefix Matters

Spring Security internally expects:

```text
ROLE_*
```

format for role-based authorization.

---

# Authorization Flow

```text
JWT Claims
      |
      v
JwtAuthConverter
      |
      v
GrantedAuthority List
      |
      v
Spring Authorization Engine
```

---

# Example Authorization Check

```java
hasRole("ADMIN")
```

internally becomes:

```text
ROLE_ADMIN
```

---

# Why This Is Important

Without converter:

```text
JWT roles would not integrate correctly
with Spring Security authorization.
```

---

# Benefits

| Benefit | Purpose |
|---|---|
| Role Extraction | Authorization support |
| Spring Integration | Security compatibility |
| JWT Claim Mapping | Cleaner auth model |
| Centralized Authority Logic | Reusable security |

---

# ==========================================
# 3. SecurityUtils
# ==========================================

# Purpose

Acts as:

```text
SecurityContext helper utility layer
```

Provides helper methods for:

- retrieving authenticated user details
- extracting JWT claims
- accessing current authentication
- simplifying controller/service security logic

---

# Why This Class Exists

Without utility methods:

```java
SecurityContextHolder.getContext()
```

logic would repeat everywhere.

This class centralizes security-access logic.

---

# High-Level Flow

```text
SecurityContext
        |
        v
SecurityUtils
        |
        v
Current Authentication Info
```

---

# Main Responsibilities

| Responsibility | Purpose |
|---|---|
| Current User Lookup | Access authenticated user |
| JWT Claim Access | Read token metadata |
| Role Inspection | Authorization support |
| Authentication Access | Simplify security logic |

---

# Typical Operations

- get current username
- get JWT subject
- get authenticated principal
- check current roles

---

# Authentication Retrieval Flow

```text
SecurityContextHolder
        |
        v
Authentication Object
        |
        v
JWT Principal
        |
        v
Extract Claims
```

---

# Why SecurityContext Is Important

Spring stores authenticated request identity inside:

```text
SecurityContext
```

This acts as:

```text
Current request security state
```

---

# Benefits

| Benefit | Purpose |
|---|---|
| Cleaner Code | Avoid repeated boilerplate |
| Centralized Security Logic | Easier maintenance |
| Simplified Authentication Access | Developer productivity |
| Consistent Security Access | Standardized usage |

---

# ==========================================
# 4. OpenApiConfig
# ==========================================

# Purpose

Configures:

```text
Swagger/OpenAPI security integration
```

for API documentation.

---

# Main Responsibilities

| Responsibility | Purpose |
|---|---|
| Swagger Security Scheme | JWT support |
| API Metadata | Documentation |
| Bearer Token Configuration | Secure API testing |
| OpenAPI Integration | Developer experience |

---

# Why This Exists

Allows developers to:

```text
Authenticate Swagger requests using JWT
```

inside Swagger UI.

---

# Swagger Security Flow

```text
Swagger UI
      |
      v
Authorize Button
      |
      v
JWT Token Entered
      |
      v
Authorization Header Added
      |
      v
Protected API Access
```

---

# Security Scheme

Configured as:

```text
HTTP Bearer Authentication
```

using:

```text
Authorization: Bearer <jwt>
```

---

# Why Swagger JWT Integration Matters

Without it:

```text
Protected APIs cannot be tested easily
```

through Swagger UI.

---

# Developer Benefits

| Benefit | Purpose |
|---|---|
| Easier API Testing | Faster development |
| JWT API Testing | Secure endpoint testing |
| Interactive Documentation | Better developer UX |
| Integrated Security Docs | Clear authentication flow |

---

# JWT Authentication Flow

# Complete Flow

```text
Client Request
        |
        v
Authorization Header
(Bearer JWT)
        |
        v
Spring Security Filter Chain
        |
        v
JWT Resource Server
        |
        v
JWT Signature Validation
        |
        v
JwtAuthConverter
        |
        v
Granted Authorities
        |
        v
SecurityContext
        |
        v
Authorization Rules
        |
        v
Controller Access
```

---

# JWT Claims Example

```json
{
  "sub": "transaction-service",
  "roles": ["ADMIN"],
  "iss": "keycloak",
  "aud": "transaction-service",
  "exp": 1715000000
}
```

---

# Authorization Model

The service uses:

```text
Role-Based Access Control (RBAC)
```

---

# Example Roles

| Role | Purpose |
|---|---|
| ADMIN | Administrative access |
| RISKCALLER | Fraud-service communication |
| USER | Standard API access |

---

# Endpoint Authorization Example

| Endpoint | Access |
|---|---|
| `/transactions/**` | Authenticated users |
| `/admin/**` | ADMIN role |
| `/swagger-ui/**` | Public |
| `/v3/api-docs/**` | Public |

---

# Why JWT-Based Security Is Good

Compared to server-side sessions:

| JWT | Session-Based |
|---|---|
| Stateless | Stateful |
| Horizontally scalable | Session replication required |
| Cloud-native | Harder distributed scaling |
| Lightweight | Session management overhead |
| Microservice-friendly | Monolith-oriented |

---

# Security Characteristics

| Characteristic | Status |
|---|---|
| Stateless | YES |
| JWT-Based | YES |
| Role-Based | YES |
| OAuth2 Resource Server | YES |
| Distributed Safe | YES |
| Microservice Ready | YES |

---

# Security Best Practices Implemented

| Practice | Purpose |
|---|---|
| Stateless Authentication | Scalability |
| JWT Validation | Request trust |
| Role-Based Authorization | Fine-grained access |
| Bearer Authentication | Standardized auth |
| Public Swagger Rules | Developer usability |
| Centralized Security Logic | Maintainability |

---

# Security Flow for Feign Calls

Service-to-service communication uses:

```text
Machine JWT Authentication
```

---

# Feign Security Flow

```text
transaction-service
        |
        v
JWT Generated
        |
        v
Authorization Header Added
        |
        v
Feign Request
        |
        v
risk-decision-service
        |
        v
JWT Validation
```

---

# Why Machine Authentication Matters

Prevents:

```text
Unauthorized services
```

from calling fraud APIs.

Only trusted services allowed.

---

# Observability & Security

Security events support:

- correlation IDs
- structured logging
- distributed tracing
- auditability

---

# Future Enhancements

- Keycloak role management
- fine-grained permissions
- refresh-token support
- token revocation
- OpenTelemetry tracing
- centralized audit logging
- API gateway integration

---

# Final Summary

The `transaction-service` security architecture implements:

```text
Production-grade stateless JWT-based microservice security
```

providing:

- secure API protection
- distributed authentication
- role-based authorization
- scalable stateless security
- secure inter-service trust
- OAuth2 resource-server integration

The design follows enterprise security practices commonly used in:

- banking systems
- fintech platforms
- payment gateways
- distributed microservice ecosystems