# Low-Level Design (LLD)

## 1. Overview

This document describes the internal class-level design of the User Service, including responsibilities, interactions, and design decisions.

The system follows a layered architecture with strict separation of concerns:

```text id="lld1"
Controller → Service → Integration → Repository → Database
                        ↓
                     Cache
```

---

## 2. Class Responsibilities

---

### 2.1 UserController (Presentation Layer)

Responsibilities:

* Handles HTTP requests
* Applies authorization rules using `@PreAuthorize`
* Delegates processing to UserService
* Returns DTO responses

Key Characteristics:

* No business logic
* Thin layer for request routing

---

### 2.2 UserService (Core Orchestration Layer)

Implemented in

Responsibilities:

* Coordinates operations between DB and Keycloak
* Implements business logic
* Handles data consistency
* Applies caching strategy
* Tracks metrics

---

#### Key Methods

##### createUser()

```text id="lld2"
1. Call Keycloak to create user
2. Map DTO → Entity
3. Save user in DB
4. Handle failures with rollback
```

Key Design:

* Uses `@Transactional` for DB consistency
* Implements compensating transaction for Keycloak rollback
* Detects unique constraint violations

---

##### rollbackKeycloakUser()

```text id="lld3"
Retry deletion up to 3 times with backoff
```

Design Decision:

* Ensures eventual consistency
* Prevents orphan users in Keycloak

---

##### getUserByUsername() / getUserById()

* Annotated with `@Cacheable`
* Uses cache-aside pattern

---

##### deactivateUser() / reactivateUser()

```text id="lld4"
1. Update DB status
2. Call Keycloak to sync state
3. Evict cache
```

---

### 2.3 KeycloakService (Integration Layer)

Implemented in

Responsibilities:

* Communicates with Keycloak Admin API
* Applies resilience patterns
* Handles identity operations

---

#### Key Methods

##### createUser()

```text id="lld5"
1. Add traceId (MDC)
2. Apply Retry + CircuitBreaker
3. Execute with timeout
4. Create user
5. Assign role
6. Handle failures
```

---

##### executeWithTimeout()

* Executes operations asynchronously
* Enforces timeout (3 seconds)

Design Decision:

* Prevents thread blocking
* Protects system from slow external calls

---

##### assignRole()

* Assigns Keycloak realm roles

---

##### safeDelete()

* Deletes user with retry protection

---

##### updateUserStatus()

* Enables or disables user in Keycloak

---

### 2.4 KeycloakConfig (Configuration Layer)

Implemented in

Responsibilities:

* Configures Keycloak client
* Uses client credentials flow
* Sets connection pool and timeouts

---

### 2.5 SecurityConfig (Security Layer)

Implemented in

Responsibilities:

* Configures Spring Security filter chain
* Enables JWT authentication
* Enforces stateless session policy
* Defines authorization rules

---

#### Key Configurations

* CSRF disabled (stateless API)
* Session policy: STATELESS
* OAuth2 Resource Server enabled
* Custom error handling (401/403)

---

### 2.6 KeycloakJwtConverter (Security Adapter)

Implemented in

Responsibilities:

* Converts Keycloak JWT roles to Spring Security authorities

---

#### Transformation

```text id="lld6"
realm_access.roles → ROLE_<ROLE>
```

---

### 2.7 RedisConfig (Cache Configuration)

Implemented in

Responsibilities:

* Configures Redis cache
* Sets TTL (1 minute)
* Configures JSON serialization

---

### 2.8 User Entity (Domain Model)

Implemented in

Responsibilities:

* Represents user data in DB
* Maps Keycloak user via keycloakUserId

---

#### Key Fields

* userId (PK)
* keycloakUserId
* userName
* email
* phoneNumber
* role
* isActive
* createdAt

---

### 2.9 UserRepository (Persistence Layer)

Implemented in

Responsibilities:

* Provides database access
* Supports filtered queries
* Implements pagination

---

### 2.10 DTO Layer

#### UserRequestDto

* Input validation
* Captures user creation data

#### UserResponseDto

* Output projection
* Hides internal fields

---

## 3. Class Interaction Flow

---

### 3.1 Create User Flow

```text id="lld7"
Controller
  ↓
UserService
  ↓
KeycloakService
  ↓
Keycloak
  ↓
UserService
  ↓
UserRepository
  ↓
Database
```

---

### 3.2 Read User Flow

```text id="lld8"
Controller
  ↓
UserService
  ↓
Cache (Redis)
  ↓
DB (if cache miss)
```

---

### 3.3 Deactivate User Flow

```text id="lld9"
Controller
  ↓
UserService
  ↓
Database Update
  ↓
KeycloakService
  ↓
Cache Eviction
```

---

## 4. Design Patterns Used

| Pattern                  | Implementation                    |
| ------------------------ | --------------------------------- |
| Layered Architecture     | Controller → Service → Repository |
| Cache-Aside              | Redis caching                     |
| Circuit Breaker          | KeycloakService                   |
| Retry                    | KeycloakService                   |
| Timeout                  | ExecutorService                   |
| Compensating Transaction | rollbackKeycloakUser              |
| DTO Pattern              | Request/Response separation       |

---

## 5. Key Design Decisions

### 5.1 Externalized Authentication

* Keycloak handles credentials
* Service remains stateless

---

### 5.2 Resilience at Integration Layer

* Retry and CircuitBreaker applied only in KeycloakService
* Keeps business logic clean

---

### 5.3 Cache at Service Layer

* Cache DB reads, not external calls
* Avoid stale identity data

---

### 5.4 Compensation Logic

* Ensures consistency across systems
* Handles partial failures

---

### 5.5 Stateless Security

* No session storage
* Every request carries JWT

---

## 6. Error Handling Strategy

* Domain-specific exceptions:

    * UserAlreadyExistsException
    * UserNotFound
    * UserServiceException
    * IdentityProviderException

* Clear separation between:

    * DB errors
    * External service errors

---

## 7. Observability

* Logging with SLF4J
* Trace ID using MDC
* Metrics tracking via UserMetrics

---

## 8. Extensibility

The system is designed for extension:

* Additional services can reuse IdentityProviderService
* New cache strategies can be plugged in
* Security rules can be extended via annotations
* Event-driven architecture can be added later

---

## 9. Summary

The design emphasizes:

* Clear separation of concerns
* Strong consistency guarantees
* Resilient external communication
* Optimized read performance
* Secure and scalable architecture

This design reflects real-world backend engineering practices used in distributed systems.
