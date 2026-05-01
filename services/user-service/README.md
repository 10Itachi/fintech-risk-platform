# Gringotts User Service

## Overview

The User Service is a Spring Boot–based microservice responsible for managing user lifecycle operations. It integrates with an external identity provider (Keycloak) for authentication and authorization, uses Redis for caching, and applies resilience patterns for fault tolerance.

The service follows a stateless architecture and enforces security using JWT-based authentication.

---

## Architecture Summary

The system is designed as a modular microservice with clear separation of concerns:

```
Client
  ↓
User Service (Spring Boot)
   ├── Controller Layer
   ├── Service Layer (Business Logic)
   ├── Repository Layer (MySQL)
   ├── Cache Layer (Redis)
   ├── Keycloak Integration Layer
   └── Security Layer (JWT Validation)
```

---

## Core Responsibilities

* User creation with Keycloak integration
* User retrieval with caching optimization
* User activation and deactivation
* Role-based and ownership-based access control
* Fault-tolerant communication with external identity provider

---

## Technology Stack

* Java 17
* Spring Boot 3.x
* Spring Security (OAuth2 Resource Server)
* Keycloak (OAuth2 + OIDC)
* Redis (Caching)
* Resilience4j (Retry, Circuit Breaker, Timeout)
* Micrometer (Metrics)
* Swagger/OpenAPI

---

## API Endpoints

Base Path: `/api/v1/users`

### Create User

```
POST /api/v1/users
Authorization: Required (ADMIN)
```

Creates a new user in both Keycloak and the local database.

---

### Get Current User Profile

```
GET /api/v1/users/profile
Authorization: Required (Authenticated User)
```

Fetches the profile of the currently authenticated user.

---

### Get All Users (Paginated)

```
GET /api/v1/users?lastId={lastId}&size={size}
Authorization: Required (ADMIN)
```

Retrieves users using cursor-based pagination.

---

### Get User by ID

```
GET /api/v1/users/{userId}
Authorization: Required (ADMIN or OWNER)
```

Accessible by admin or the user who owns the resource.

---

### Deactivate User

```
PATCH /api/v1/users/{userId}/deactivate
Authorization: Required (ADMIN)
```

Marks a user as inactive and disables the user in Keycloak.

---

### Activate User

```
PATCH /api/v1/users/{userId}/activate
Authorization: Required (ADMIN)
```

Reactivates a previously deactivated user.

---

## Security

* Stateless authentication using JWT
* OAuth2 Resource Server configuration
* Integration with Keycloak for identity management
* Role-based authorization using `@PreAuthorize`
* Ownership-based access control using JWT claims

### Authentication Flow

```
Client Request (JWT)
   ↓
Spring Security Filter Chain
   ↓
JWT Validation (Signature, Expiry, Issuer)
   ↓
Role Mapping (Keycloak → Spring Security)
   ↓
SecurityContext Population
   ↓
Authorization Check
   ↓
Controller Execution
```

---

## Keycloak Integration

The service integrates with Keycloak as an external identity provider.

Key responsibilities handled in the integration layer:

* User creation
* Role assignment
* User enable/disable operations
* Timeout handling for external calls
* Retry and circuit breaker protection

The integration uses resilience patterns to ensure system stability during failures.

---

## Data Consistency Strategy

The service ensures consistency between Keycloak and the local database using compensating transactions.

### User Creation Flow

```
1. Create user in Keycloak
2. Persist user in database
3. If database operation fails:
   → Rollback Keycloak user
```

This approach prevents orphaned users in the identity provider.

---

## Caching Strategy

The service uses a cache-aside pattern with Redis.

### Characteristics

* Cache applied on read operations
* Data stored by userId and username
* Cache eviction on updates

### Flow

```
Request
  ↓
Check Cache
  ↓
Cache Hit → Return
  ↓
Cache Miss
  ↓
Fetch from DB
  ↓
Store in Cache
```

---

## Resilience Strategy

Resilience is implemented at the Keycloak integration layer.

### Patterns Used

* Retry for transient failures
* Circuit Breaker for fault isolation
* Timeout for external calls
* Fallback mechanisms for graceful degradation

### Execution Flow

```
UserService
  ↓
KeycloakService
  ↓
Retry → Circuit Breaker → Timeout
  ↓
Keycloak API
```

---

## Observability

* Metrics collection using Micrometer
* Structured logging with trace identifiers (MDC)
* Logging integrated across service and integration layers

---

## Setup and Run

### Prerequisites

* Java 17+
* Maven
* Redis
* Keycloak server
* MySQL database

---

### Run Application

```
mvn clean install
mvn spring-boot:run
```

---

### Required Configuration

```
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/{realm}
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

---

## Design Principles

* Stateless architecture for scalability
* Externalized authentication via identity provider
* Clear separation of concerns
* Resilience at integration boundaries
* Optimized read performance through caching

---

## Future Enhancements

* API Gateway integration
* Distributed tracing
* Centralized logging
* Event-driven cache invalidation
* Rate limiting

---

## Conclusion

This service demonstrates a production-oriented backend architecture with:

* Secure authentication and authorization
* Fault-tolerant external integrations
* Efficient caching strategy
* Clean layered design

It reflects real-world practices used in scalable microservices systems.
