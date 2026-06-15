# System Architecture

The Gateway Service acts as the centralized edge service for the Gringotts microservices platform.

All external client traffic must pass through the gateway.

Internal services remain isolated from direct public exposure.

---

# High-Level Architecture

```text
Client
  ↓
API Gateway
  ↓
Authentication
  ↓
Authorization
  ↓
Rate Limiter
  ↓
Circuit Breaker
  ↓
Retry Logic
  ↓
Service Routing
  ↓
Downstream Microservice
```

---

# Core Responsibilities

The gateway is responsible for:

- JWT validation
- request routing
- authorization
- traffic throttling
- resilience handling
- observability
- centralized security

The gateway intentionally contains:
- no business logic
- no persistence logic
- no session state

---

# Reactive Architecture

The gateway uses Spring WebFlux instead of Spring MVC.

Reasons:

- non-blocking IO
- better scalability
- lower thread usage
- gateway workloads are network-bound
- efficient under high concurrency

---

# Stateless Design

The gateway stores:
- no sessions
- no request state
- no user state

Authentication is fully JWT-based.

Benefits:
- horizontal scalability
- Kubernetes readiness
- container orchestration compatibility
- load balancer compatibility

---

# Request Lifecycle

```text
Incoming Request
  ↓
CorrelationIdFilter
  ↓
Spring Security Filter Chain
  ↓
JWT Validation
  ↓
Role Authorization
  ↓
RequestRateLimiter
  ↓
CircuitBreaker Filter
  ↓
Retry Filter
  ↓
Route Predicate Match
  ↓
Downstream Service
  ↓
RequestLoggingFilter
  ↓
Response Returned
```

---

# Authentication Flow

```text
Client Login
  ↓
Keycloak issues JWT
  ↓
Client sends JWT to Gateway
  ↓
Gateway validates JWT
  ↓
Gateway extracts roles
  ↓
Gateway authorizes route
  ↓
Gateway forwards request
```

---

# Authorization Model

| Route Type | Access |
|---|---|
| /admin/** | ROLE_ADMIN |
| /user/** | ROLE_USER |
| /shared/** | Any authenticated user |

---

# Gateway Filters

Implemented filters:

| Filter | Purpose |
|---|---|
| CorrelationIdFilter | Request tracing |
| RequestLoggingFilter | Structured logging |
| RequestRateLimiter | Traffic throttling |
| CircuitBreaker | Failure isolation |
| Retry | Safe retry handling |

---

# Resilience Architecture

Implemented:
- Redis rate limiting
- Resilience4j circuit breakers
- retry policies
- timeout protection
- fallback endpoints

---

# Zero Trust Security

Sensitive services validate:
- issuer
- audience
- service identity
- service roles

Example:
risk-service validates:
- audience claim
- azp claim
- service roles

This prevents unauthorized service access.

---

# Security Boundary Strategy

The gateway acts as:
- first security layer
- centralized authorization layer
- traffic control boundary

Benefits:
- reduced downstream load
- centralized enforcement
- simpler downstream services

---

# Observability Architecture

Implemented:
- correlation IDs
- structured logs
- Prometheus metrics
- health endpoints
- readiness/liveness probes

Planned:
- OpenTelemetry
- Jaeger
- Grafana
- distributed tracing

---

# Deployment Strategy

Designed for:
- Docker
- Kubernetes
- AWS ECS/EKS

Public exposure:
- gateway-service only

Internal-only:
- transaction-service
- risk-service
- notification-service

---

# Scalability Considerations

The gateway supports:
- horizontal scaling
- distributed throttling
- reactive request handling
- stateless deployment

Redis enables:
- distributed rate limiting
- cluster-wide traffic coordination

---

# Failure Isolation

Circuit breakers prevent:
- cascading failures
- downstream overload
- latency amplification

Fallback endpoints provide:
- graceful degradation
- controlled failure responses

---

# Architecture Goals

The gateway architecture prioritizes:

- security
- scalability
- resilience
- observability
- cloud readiness
- operational simplicity