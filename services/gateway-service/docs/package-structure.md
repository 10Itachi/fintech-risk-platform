# Package Structure Documentation

# Overview

The Gateway Service follows a layered package structure focused on:

- separation of concerns
- maintainability
- centralized infrastructure logic
- reactive gateway architecture

The gateway intentionally avoids:
- business logic
- persistence layers
- domain-heavy services

---

# Current Package Structure

```text
gateway_service/
│
├── config/
├── controller/
├── exception/
├── filters/
├── ratelimit/
├── routes/
├── security/
```

---

# config/

Contains centralized framework configuration.

Classes:
- SecurityConfig
- CorsConfig
- JwtAuthConverter

---

# SecurityConfig

Purpose:
- configure Spring Security
- configure OAuth2 Resource Server
- configure route authorization
- configure security headers

Responsibilities:
- JWT validation
- RBAC
- public/protected route rules
- stateless security

---

# CorsConfig

Purpose:
- configure cross-origin access

Responsibilities:
- allowed origins
- allowed methods
- allowed headers

---

# JwtAuthConverter

Purpose:
- extract roles from JWT
- convert Keycloak roles into Spring authorities

Responsibilities:
- parse realm_access.roles
- generate ROLE_* authorities

---

# controller/

Contains lightweight HTTP endpoints.

Classes:
- FallbackController

---

# FallbackController

Purpose:
- graceful degradation
- fallback responses

Responsibilities:
- service unavailable responses
- circuit breaker fallback handling

---

# exception/

Contains centralized exception handling.

Classes:
- ErrorResponse
- GlobalErrorWebExceptionHandler

---

# ErrorResponse

Purpose:
- standardized API error responses

Fields:
- timestamp
- status
- message
- correlationId

---

# GlobalErrorWebExceptionHandler

Purpose:
- centralized reactive exception handling

Responsibilities:
- hide stack traces
- return consistent responses
- prevent internal exception leakage

---

# filters/

Contains gateway request/response filters.

Classes:
- CorrelationIdFilter
- RequestLoggingFilter

---

# CorrelationIdFilter

Purpose:
- request tracing
- distributed debugging

Responsibilities:
- generate correlation ID
- attach response header
- update MDC logging context

---

# RequestLoggingFilter

Purpose:
- structured request logging

Responsibilities:
- latency tracking
- request diagnostics
- operational visibility

---

# ratelimit/

Contains Redis-backed rate limiting configuration.

Classes:
- RateLimiterConfig

---

# RateLimiterConfig

Purpose:
- configure RequestRateLimiter key resolvers

Responsibilities:
- user-based throttling
- IP-based throttling

Key resolvers:
- userKeyResolver
- ipKeyResolver

---

# routes/

Reserved for:
- custom route locator logic
- dynamic route registration

Current status:
- package exists for future extensibility

---

# security/

Reserved for:
- future advanced security implementations
- custom authentication logic
- security utilities

Current status:
- package exists for future extensibility

---

# Why Separation Matters

The package structure isolates:
- security concerns
- observability concerns
- resilience concerns
- exception concerns

Benefits:
- easier maintenance
- clearer ownership
- simpler debugging
- scalable architecture

---

# Reactive Architecture Considerations

The gateway uses:
```text
Spring WebFlux
```

Therefore:
- filters are reactive
- exception handling is reactive
- security chain is reactive

Blocking patterns should be avoided.

---

# Architectural Philosophy

The package structure prioritizes:
- infrastructure-focused design
- centralized gateway responsibilities
- operational simplicity
- production maintainability