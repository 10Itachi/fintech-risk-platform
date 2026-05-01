z# Architectural Decisions

## 1. External Identity Provider (Keycloak)

Decision:
Use Keycloak instead of in-service authentication.

Reason:

* Centralized identity management
* Supports OAuth2 and OIDC standards
* Enables SSO and role management

Trade-off:

* Dependency on external system

---

## 2. JWT-Based Stateless Authentication

Decision:
Use JWT instead of session-based authentication.

Reason:

* Enables horizontal scaling
* No server-side session storage
* Reduces DB dependency

Trade-off:

* Token revocation is difficult
* Requires careful expiry handling

---

## 3. Resilience at Integration Layer

Decision:
Apply Retry, Circuit Breaker, and Timeout in KeycloakService.

Reason:

* Failures occur at external boundaries
* Keeps business logic clean
* Prevents cascading failures

Trade-off:

* Slight increase in complexity

---

## 4. Cache-Aside Strategy (Redis)

Decision:
Use cache-aside pattern for read operations.

Reason:

* Improves performance for read-heavy endpoints
* Reduces DB load

Trade-off:

* Requires explicit cache eviction
* Risk of stale data

---

## 5. No Caching for Identity Provider

Decision:
Do not cache Keycloak responses.

Reason:

* Identity data must be consistent
* Avoid stale authentication state

---

## 6. Compensation-Based Consistency

Decision:
Use compensating transaction for user creation.

Reason:

* Distributed transaction (Keycloak + DB)
* Prevents orphan users

Trade-off:

* Requires rollback logic

---

## 7. Method-Level Security

Decision:
Use `@PreAuthorize` instead of only URL-based rules.

Reason:

* Fine-grained control
* Supports ownership checks

---

## 8. Client Credentials for Service Integration

Decision:
Use client credentials flow for Keycloak communication.

Reason:

* Secure service-to-service authentication
* No user context required

---

## 9. Stateless Session Policy

Decision:
Disable HTTP sessions.

Reason:

* Required for scalable microservices
* Aligns with JWT model

---

## 10. Short Cache TTL (1 Minute)

Decision:
Use short TTL for Redis cache.

Reason:

* Balance between performance and consistency
* Limits stale data window

---

## 11. Use of DTO Pattern

Decision:
Separate request/response DTOs from entities.

Reason:

* Prevents exposure of internal structure
* Improves API clarity

---

## 12. Logging with MDC

Decision:
Use MDC for traceId propagation.

Reason:

* Enables correlation of logs
* Prepares system for distributed tracing

---

## Summary

The system prioritizes:

* Scalability (stateless design)
* Security (external identity provider)
* Resilience (fault isolation)
* Performance (caching)
* Consistency (compensation logic)
