# Architecture Documentation

## 1. System Overview

The User Service is a stateless microservice responsible for managing user lifecycle operations while delegating authentication and identity management to an external identity provider (Keycloak).

The service follows a layered architecture with clear separation of concerns and integrates caching, resilience, and security at appropriate boundaries.

---

## 2. High-Level Architecture

```text
Client
  ↓
User Service (Spring Boot)
   ├── Controller Layer
   ├── Service Layer (Orchestration)
   ├── Integration Layer (Keycloak)
   ├── Persistence Layer (MySQL)
   ├── Cache Layer (Redis)
   └── Security Layer (JWT)
```

---

## 3. Component Architecture

### 3.1 Controller Layer

* Exposes REST APIs
* Enforces authorization using method-level security (`@PreAuthorize`)
* Delegates requests to the service layer

---

### 3.2 Service Layer (Core Orchestration)

Implemented in

Responsibilities:

* Orchestrates operations across database and identity provider
* Implements compensating transactions (rollback logic)
* Applies caching strategy for read operations
* Maintains business logic and consistency

---

### 3.3 Integration Layer (Identity Provider)

Implemented in

Responsibilities:

* Communicates with Keycloak Admin API
* Applies resilience patterns:

    * Retry
    * Circuit Breaker
    * Timeout
* Handles user creation, deletion, role assignment, and status updates

---

### 3.4 Persistence Layer

#### Entity

Defined in

* Stores user metadata
* References Keycloak user via `keycloakUserId`
* Does not store passwords

#### Repository

Defined in

* Provides CRUD operations
* Supports filtered queries (active users)
* Enables cursor-based pagination

---

### 3.5 Cache Layer

Configured in

Strategy: Cache-Aside Pattern

* Cache read-heavy endpoints
* TTL-based eviction (1 minute)
* Manual eviction on updates

---

### 3.6 Security Layer

Configured in

Responsibilities:

* Stateless JWT authentication
* OAuth2 Resource Server configuration
* Role-based access control

Role mapping handled in

---

### 3.7 Keycloak Configuration

Defined in

* Uses Client Credentials flow
* Establishes secure communication with Keycloak
* Configures connection pooling and timeouts

---

## 4. Authentication and Authorization Flow

```text
Client Request (JWT)
   ↓
SecurityFilterChain
   ↓
BearerTokenAuthenticationFilter
   ↓
JwtDecoder (auto-configured)
   ↓
KeycloakJwtConverter (maps roles)
   ↓
SecurityContextHolder
   ↓
Authorization (URL + Method Level)
   ↓
Controller Execution
```

Key Points:

* Stateless authentication (no sessions)
* JWT validated using Keycloak public key
* Roles extracted from `realm_access.roles`

---

## 5. User Creation Flow (Distributed Transaction)

```text
1. Request received
2. Call Keycloak to create user
3. Persist user in database
4. If DB fails:
      → rollback Keycloak user
```

Implementation in

This ensures consistency across systems using compensating transactions.

---

## 6. Read Flow with Caching

```text
Request
  ↓
Check Redis Cache
  ↓
Cache Hit → Return
  ↓
Cache Miss
  ↓
Fetch from DB
  ↓
Store in Cache
  ↓
Return
```

Implemented using `@Cacheable` in

---

## 7. Update Flow (Deactivate / Activate)

```text
1. Fetch user from DB
2. Update status
3. Call Keycloak to enable/disable user
4. Evict cache entries
```

Ensures cache consistency and external system sync.

---

## 8. Resilience Architecture

Applied in the integration layer

### Patterns Used:

* Retry (transient failures)
* Circuit Breaker (fault isolation)
* Timeout (latency control)
* Fallback handling

### Execution Flow:

```text
UserService
  ↓
KeycloakService
  ↓
Retry → CircuitBreaker → Timeout
  ↓
Keycloak API
```

---

## 9. Data Model

### User Entity Fields

* userId (Primary Key)
* keycloakUserId (External reference)
* userName
* email
* phoneNumber
* role
* isActive
* createdAt

---

## 10. Design Principles

* Stateless architecture for scalability
* Externalized authentication via identity provider
* Clear separation of concerns
* Resilience at integration boundaries
* Cache optimization for read-heavy operations
* Consistency via compensating transactions

---

## 11. External Dependencies

* Keycloak (Identity Provider)
* Redis (Caching)
* MySQL (Primary database)

---

## 12. Deployment View

```text
Docker Compose
   ├── User Service
   ├── Redis
   ├── Keycloak
   └── MySQL
```

---

## 13. Key Architectural Decisions

* Use Keycloak for authentication instead of in-service auth
* Use JWT for stateless security
* Apply resilience at integration layer, not service layer
* Avoid caching identity provider data
* Use cache-aside pattern for DB reads

---

## 14. Future Architecture Enhancements

* API Gateway layer for centralized routing and security
* Distributed tracing (OpenTelemetry)
* Centralized logging (ELK)
* Event-driven cache invalidation
* Rate limiting

---

## 15. Summary

The system is designed as a secure, scalable, and fault-tolerant microservice with:

* Stateless JWT-based authentication
* Externalized identity management
* Resilient external integrations
* Optimized read performance using caching
* Strong consistency guarantees using compensating transactions

This architecture aligns with modern backend engineering practices used in distributed systems.
