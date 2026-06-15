# Configuration Documentation

# Overview

The Gateway Service configuration is centralized using:

- application.yaml
- environment variables
- .env files

The configuration strategy supports:
- local development
- Docker deployments
- Kubernetes deployments
- AWS deployments
- CI/CD pipelines

---

# Configuration Philosophy

Configuration is externalized to:
- avoid hardcoded secrets
- simplify deployments
- support multiple environments
- enable infrastructure portability

---

# Core Configuration Areas

---

# Server Configuration

```yaml
server:
  port:
```

Purpose:
- configure gateway HTTP port

Example:

```yaml
server:
  port: ${GATEWAY_PORT:8087}
```

---

# Request Size Limits

```yaml
spring:
  http:
    codecs:
      max-in-memory-size:
```

Purpose:
- prevent oversized request payloads
- memory protection

---

# Redis Configuration

```yaml
spring:
  data:
    redis:
```

Purpose:
- distributed rate limiting
- token bucket storage

Example:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
```

---

# OAuth2 Resource Server Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
```

Purpose:
- JWT validation
- issuer verification
- OAuth2 integration

---

# Keycloak Configuration

Keycloak issuer URI:

```yaml
issuer-uri:
```

Purpose:
- JWT issuer validation
- OpenID configuration discovery

---

# Gateway Route Configuration

Routes are configured using:

```yaml
spring:
  cloud:
    gateway:
      routes:
```

Each route contains:
- predicates
- filters
- destination URI

---

# Route Predicates

Example:

```yaml
predicates:
  - Path=/risk/api/**
```

Purpose:
- route matching
- request forwarding

---

# Gateway Filters

Implemented filters:
- RequestRateLimiter
- CircuitBreaker
- Retry

---

# RequestRateLimiter Configuration

Example:

```yaml
redis-rate-limiter.replenishRate:
redis-rate-limiter.burstCapacity:
```

Purpose:
- request throttling
- abuse prevention

---

# Circuit Breaker Configuration

Configured using:

```yaml
resilience4j:
  circuitbreaker:
```

Purpose:
- failure isolation
- graceful degradation

---

# Retry Configuration

Configured:
- retry count
- retry statuses
- backoff policy

Retries enabled only for:
- GET requests
- idempotent operations

---

# HTTP Client Configuration

Configured:
- connect timeout
- response timeout

Purpose:
- prevent hanging connections
- improve resilience

---

# Logging Configuration

```yaml
logging:
  pattern:
```

Purpose:
- structured logs
- correlation ID support

---

# Management Configuration

Configured:
- health endpoints
- metrics
- Prometheus exposure

Example:

```yaml
management:
  endpoints:
```

---

# Environment Variables

The gateway supports environment-driven configuration.

Example:

```yaml
${SPRING_APPLICATION_NAME:gateway-service}
```

Benefits:
- deployment portability
- safer CI/CD
- easier containerization

---

# Fallback Defaults

Fallback defaults are implemented because:
- local startup should not fail
- tests should remain stable
- CI/CD pipelines should be resilient

Example:

```yaml
${GATEWAY_PORT:8087}
```

---

# Security Configuration

Security configuration includes:
- JWT issuer
- route authorization
- OAuth2 Resource Server

Configured inside:
```text
SecurityConfig
```

---

# Redis Dependency

Redis is required for:
- distributed throttling
- rate limiting

If Redis is unavailable:
- gateway startup may fail
- rate limiting becomes unavailable

---

# Configuration Goals

The configuration strategy prioritizes:
- cloud readiness
- deployment portability
- operational simplicity
- security
- scalability

---

# Planned Future Configuration Improvements

Planned:
- centralized config server
- Kubernetes ConfigMaps
- Vault integration
- secret management
- dynamic refresh