# Resilience Documentation

# Overview

The Gateway Service implements production-grade resilience mechanisms to protect downstream services and improve platform stability.

Implemented resilience patterns:
- rate limiting
- retries
- circuit breakers
- timeout protection
- graceful degradation

---

# Resilience Goals

The resilience layer protects against:
- traffic spikes
- downstream instability
- cascading failures
- abusive clients
- temporary outages

---

# Rate Limiting

Rate limiting is implemented using:

- Redis
- Spring Cloud Gateway RequestRateLimiter
- token bucket algorithm

---

# Why Redis

Redis provides:
- distributed throttling
- centralized counters
- horizontal scalability
- low-latency operations

---

# Token Bucket Algorithm

The token bucket algorithm controls:
- request bursts
- sustained throughput

Configuration:
- replenishRate
- burstCapacity

---

# Key Resolvers

---

# ipKeyResolver

Purpose:
- login endpoint throttling
- brute-force prevention

Key:
```text
client IP address
```

Used for:
```text
Keycloak login APIs
```

---

# userKeyResolver

Purpose:
- authenticated user throttling

Key:
```text
authenticated principal name
```

Used for:
- user-service APIs
- transaction APIs
- risk APIs

---

# Circuit Breakers

Implemented using:

```text
Resilience4j
```

Purpose:
- failure isolation
- prevent cascading failures
- graceful degradation

---

# Protected Services

Circuit breakers protect:
- user-service
- transaction-service
- risk-service

---

# Circuit Breaker States

---

# CLOSED

Normal operation.

Requests flow normally.

---

# OPEN

Failures exceeded threshold.

Requests immediately fail.

Fallback endpoint returned.

---

# HALF_OPEN

Gateway tests whether downstream service recovered.

Limited requests allowed.

---

# Circuit Breaker Flow

```text
Downstream Failures
  ↓
Failure Threshold Exceeded
  ↓
Circuit Opens
  ↓
Traffic Blocked
  ↓
Fallback Returned
  ↓
Cooldown Period
  ↓
Half-Open Recovery Testing
```

---

# Circuit Breaker Configuration

| Property | Purpose |
|---|---|
| slidingWindowSize | failure evaluation sample size |
| failureRateThreshold | open threshold |
| minimumNumberOfCalls | minimum evaluation requests |
| waitDurationInOpenState | cooldown period |
| permittedNumberOfCallsInHalfOpenState | recovery testing |

---

# Retry Mechanism

Retries implemented using:

```text
Spring Cloud Gateway Retry Filter
```

---

# Retry Rules

Retries enabled ONLY for:
- GET requests
- idempotent APIs

Retry Conditions:
- BAD_GATEWAY
- GATEWAY_TIMEOUT

---

# Why POST/PUT Retries Are Disabled

Retries on transactional APIs can cause:
- duplicate payments
- duplicate transactions
- inconsistent state

Therefore:
```text
financial mutations are never retried
```

---

# Retry Backoff Strategy

Implemented:
- exponential backoff

Purpose:
- reduce retry storms
- reduce downstream pressure

---

# Retry Flow

```text
Request Fails
  ↓
Retry Attempt 1
  ↓
Retry Attempt 2
  ↓
Failure Persists
  ↓
Circuit Breaker Evaluates
```

---

# Timeout Protection

Gateway HTTP client timeout configuration prevents:
- hanging requests
- thread exhaustion
- latency amplification

Configured:
- connect timeout
- response timeout

---

# Fallback Strategy

Fallback endpoint:

```text
/fallback/service-unavailable
```

Purpose:
- graceful degradation
- controlled error responses
- stable client behavior

---

# Failure Isolation

Circuit breakers isolate:
- unstable services
- overloaded services
- network failures

Benefits:
- platform stability
- predictable degradation
- reduced cascading failures

---

# Resilience Philosophy

The gateway assumes:
```text
distributed systems fail constantly
```

Therefore:
- failures are expected
- failures are isolated
- failures are controlled

---

# Operational Benefits

Implemented resilience mechanisms improve:
- uptime
- reliability
- fault tolerance
- traffic stability
- downstream protection

---

# Cloud Readiness

The resilience architecture is designed for:
- Kubernetes
- AWS ECS/EKS
- distributed deployments
- horizontally scaled environments

Redis-backed throttling supports:
- cluster-wide rate limiting
- multi-instance coordination