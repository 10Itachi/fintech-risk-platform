# security.md

# Notification Service Security Architecture

# Overview

The `notification-service` implements:

```text
Stateless JWT-based microservice security
```

using:

- Spring Security
- OAuth2 Resource Server
- JWT authentication
- role-based authorization

---

# Main Security Components

| Component | Responsibility |
|---|---|
| NotificationSecurityConfig | Spring Security configuration |
| JwtAuthConverter | JWT authority mapping |

---

# Security Goals

| Goal | Purpose |
|---|---|
| JWT Authentication | Secure APIs |
| Stateless Security | Distributed scalability |
| Role Authorization | Restrict operations |
| Service Trust | Secure microservice calls |

---

# Security Flow

```text
HTTP Request
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
Authorization Rules
```

---

# 1. NotificationSecurityConfig

# Purpose

Acts as:

```text
Central Spring Security configuration layer
```

---

# Responsibilities

- SecurityFilterChain configuration
- JWT validation
- endpoint authorization
- stateless authentication
- OAuth2 resource-server setup

---

# Stateless Authentication

Uses:

```java
SessionCreationPolicy.STATELESS
```

---

# Meaning

```text
No server-side session persistence
```

Each request must provide JWT.

---

# OAuth2 Resource Server

Configured as:

```text
OAuth2 JWT Resource Server
```

---

# JWT Validation Responsibilities

| Validation | Purpose |
|---|---|
| Signature Validation | Prevent tampering |
| Expiration Validation | Reject expired tokens |
| Issuer Validation | Trusted identity provider |
| Authority Extraction | Authorization support |

---

# Public Endpoints

Typically exposed:

- actuator endpoints
- Swagger docs
- health endpoints

---

# Protected Endpoints

Operational APIs require:

```text
Authenticated JWT token
```

---

# 2. JwtAuthConverter

# Purpose

Acts as:

```text
JWT-to-Spring-Security authority mapper
```

---

# Responsibilities

- extract JWT roles
- convert claims into authorities
- integrate JWT with Spring Security

---

# Role Conversion Flow

```text
JWT Claims
      |
      v
Extract Roles
      |
      v
ROLE_* Authorities
```

---

# Example Roles

```json
{
  "roles": ["ADMIN", "OPS"]
}
```

---

# Converted Authorities

```text
ROLE_ADMIN
ROLE_OPS
```

---

# Why ROLE_ Prefix Matters

Spring Security internally expects:

```text
ROLE_*
```

format.

---

# Security Characteristics

| Characteristic | Status |
|---|---|
| Stateless | YES |
| JWT-Based | YES |
| OAuth2 Resource Server | YES |
| Role-Based | YES |
| Distributed Safe | YES |

---

# Final Summary

The security architecture inside:

```text
notification-service
```

implements:

```text
Production-grade stateless microservice security
```

through:

- JWT authentication
- OAuth2 resource-server validation
- role-based authorization
- distributed service trust
- secure operational APIs