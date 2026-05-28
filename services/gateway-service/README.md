# Gateway Service

## Overview

The Gateway Service acts as the centralized entry point for the Gringotts microservices platform.

It is implemented using:

- Spring Cloud Gateway
- Spring WebFlux
- Spring Security
- OAuth2 Resource Server
- Keycloak
- Redis
- Resilience4j

The gateway is responsible for:

- authentication
- authorization
- request routing
- rate limiting
- circuit breaking
- retry handling
- request logging
- observability
- security hardening

The service is designed as a stateless, horizontally scalable, cloud-ready API gateway.

---

# Technology Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3.x |
| Gateway | Spring Cloud Gateway |
| Reactive Stack | Spring WebFlux |
| Security | Spring Security |
| Authentication | OAuth2 Resource Server |
| IAM | Keycloak |
| JWT Validation | Nimbus JWT Decoder |
| Rate Limiting | Redis |
| Resilience | Resilience4j |
| Monitoring | Spring Actuator |
| Metrics | Prometheus |
| Build Tool | Maven |
| Logging | SLF4J + Logback |
| Cloud Target | AWS ECS/EKS |

---

# Core Features

## Security

- JWT validation
- Role-based access control
- OAuth2 Resource Server
- Security headers
- Internal route isolation
- Stateless authentication

## Resilience

- Circuit breakers
- Retry policies
- Timeout handling
- Fallback endpoints
- Redis-backed rate limiting

## Observability

- Correlation IDs
- Structured request logging
- Health endpoints
- Prometheus metrics
- Readiness/liveness probes

## Scalability

- Reactive architecture
- Stateless design
- Horizontal scaling ready
- Cloud-native routing

---

# Services Routed Through Gateway

| Service | Purpose |
|---|---|
| user-service | User management |
| transaction-service | Transaction processing |
| risk-decision-service | Fraud/risk analysis |
| notification-service | Notification processing |
| observability-service | Monitoring APIs |
| Keycloak | Authentication provider |

---

# Gateway Responsibilities

The gateway acts as:

- centralized security boundary
- centralized traffic controller
- centralized routing layer
- centralized observability layer
- centralized resilience layer

---

# Local Startup

## Prerequisites

- Java 17+
- Maven
- Redis
- Keycloak

---

# Run Redis

```bash
redis-server
```

---

# Run Gateway

```bash
mvn spring-boot:run
```

---

# Default Port

```text
8087
```

---

# Main Endpoints

| Endpoint | Purpose |
|---|---|
| /actuator/health | Health check |
| /actuator/prometheus | Metrics |
| /fallback/service-unavailable | Fallback endpoint |

---

# Security Model

Authentication:
- JWT Bearer tokens
- Keycloak OAuth2

Authorization:
- ROLE_ADMIN
- ROLE_USER

---

# Reactive Architecture

The gateway uses Spring WebFlux because gateway workloads are:

- network-heavy
- IO-bound
- concurrency-heavy

Benefits:
- non-blocking IO
- lower thread consumption
- higher throughput
- better scalability

---

# Production Features

Implemented:
- JWT validation
- role extraction
- route authorization
- rate limiting
- retries
- circuit breakers
- correlation tracing
- structured logging
- Prometheus metrics
- readiness/liveness probes

---

# Future Improvements

Planned:
- OpenTelemetry
- Jaeger
- Grafana dashboards
- Kubernetes deployment
- centralized logging
- service discovery
- config server
- mTLS
- AWS deployment