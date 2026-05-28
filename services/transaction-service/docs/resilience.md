# resilience.md

# Resilience Architecture

# Overview

The `transaction-service` implements:

```text
Production-grade distributed-system resilience patterns
```

to ensure:

- fault-tolerant transaction processing
- retry-safe orchestration
- graceful degradation
- distributed consistency
- failure isolation
- operational stability

The architecture is designed to survive:

- Redis outages
- Kafka failures
- network instability
- fraud-service latency
- partial infrastructure failures
- transient distributed-system errors

---

# Resilience Goals

| Goal | Purpose |
|---|---|
| Duplicate Prevention | Retry-safe processing |
| Graceful Degradation | Continue operating during failures |
| Distributed Consistency | Prevent partial failures |
| Failure Isolation | Prevent cascading outages |
| Automatic Recovery | Self-healing behavior |
| Operational Stability | Production reliability |

---

# High-Level Resilience Architecture

```text
Incoming Transaction
        |
        v
Redis Idempotency Lock
        |
        v
Business Validation
        |
        v
Risk Evaluation
(Feign + Retry + Circuit Breaker)
        |
        v
Transaction Finalization
        |
        v
Transactional Outbox
        |
        v
Kafka Retry Publisher
```

---

# Core Resilience Components

| Component | Responsibility |
|---|---|
| RedisIdempotencyService | Duplicate protection |
| Resilience4j Retry | Transient failure recovery |
| Circuit Breaker | Failure isolation |
| Transactional Outbox | Kafka consistency |
| OutboxPublisher | Retry-safe publishing |
| Kafka Producer Reliability | Durable messaging |

---

# ==========================================
# 1. Redis Idempotency Resilience
# ==========================================

# Purpose

Provides:

```text
Distributed duplicate request protection
```

---

# Why This Is Critical

Without idempotency:

```text
Duplicate client retries
could create duplicate transactions
```

Very dangerous in financial systems.

---

# High-Level Flow

```text
Incoming Request
        |
        v
tryLock(idempotencyKey)
        |
   +----+----+
   |         |
SUCCESS    DUPLICATE
   |         |
   v         v
PROCESS   RETURN EXISTING
```

---

# Redis States

| State | Meaning |
|---|---|
| IN_PROGRESS | Transaction processing |
| COMPLETED:{txnId} | Processing completed |

---

# TTL-Based Recovery

Redis locks use:

```text
Automatic expiration (TTL)
```

---

# Why TTL Matters

If application crashes during processing:

```text
IN_PROGRESS lock automatically expires
```

Preventing:

- permanent deadlocks
- stuck transaction processing

---

# Graceful Redis Degradation

If Redis unavailable:

```text
System continues processing
```

using:

```text
DB unique constraints
```

as fallback protection.

---

# Why This Is Good Design

Redis outage should NOT fully stop:

```text
Transaction processing
```

---

# Redis Failure Flow

```text
Redis Failure
      |
      v
Catch Exception
      |
      v
Log Failure
      |
      v
Fallback to DB Safety
```

---

# Benefits

| Benefit | Purpose |
|---|---|
| Duplicate Prevention | Safe retries |
| Crash Recovery | TTL cleanup |
| Distributed Locking | Concurrency safety |
| Graceful Degradation | Continued operation |

---

# ==========================================
# 2. Feign Retry Resilience
# ==========================================

# Purpose

Protects against:

```text
Transient fraud-service failures
```

during synchronous risk evaluation.

---

# Retry Configuration

```yaml
resilience4j:
  retry:
    instances:
      riskService:
```

---

# Configured Retries

| Property | Purpose |
|---|---|
| max-attempts | Retry limit |
| wait-duration | Delay between retries |
| exponential-backoff | Controlled retry scaling |

---

# Retry Flow

```text
Feign Request
      |
      v
Failure
      |
      v
Retry Logic
      |
   +--+---+
   |      |
SUCCESS  FAIL
```

---

# Retryable Exceptions

Configured retries include:

- IOException
- SocketTimeoutException
- RetryableException

---

# Why Controlled Retries Matter

Retries help recover:

- temporary network instability
- short outages
- transient infrastructure failures

---

# Why Excessive Retries Are Dangerous

Too many retries can cause:

```text
Retry storms
```

which may overload downstream services.

---

# ==========================================
# 3. Circuit Breaker Resilience
# ==========================================

# Purpose

Implements:

```text
Failure isolation
```

for fraud-service communication.

---

# Why Circuit Breakers Matter

Without circuit breakers:

```text
Failing downstream service
can exhaust all threads
```

leading to:

```text
Cascading system failure
```

---

# High-Level Flow

```text
Feign Calls
      |
      v
Failure Monitoring
      |
   +--+---+
   |      |
HEALTHY  FAILURE_THRESHOLD_EXCEEDED
   |      |
   v      v
CLOSED   OPEN
```

---

# Circuit Breaker States

| State | Meaning |
|---|---|
| CLOSED | Normal operation |
| OPEN | Requests blocked |
| HALF_OPEN | Recovery testing |

---

# OPEN State Behavior

When failure threshold exceeded:

```text
Calls immediately rejected
```

Protecting:

- application threads
- transaction throughput
- system stability

---

# HALF_OPEN State

After wait duration:

```text
Small number of test requests allowed
```

If successful:

```text
Circuit closes again
```

---

# Configured Properties

| Property | Purpose |
|---|---|
| sliding-window-size | Failure tracking window |
| minimum-calls | Minimum calls before evaluation |
| failure-rate-threshold | Open threshold |
| wait-duration-open-state | Recovery wait period |

---

# Benefits

| Benefit | Purpose |
|---|---|
| Failure Isolation | Prevent cascading outages |
| Resource Protection | Prevent thread exhaustion |
| Faster Recovery | Controlled healing |
| Distributed Stability | System resilience |

---

# ==========================================
# 4. Transactional Outbox Resilience
# ==========================================

# Purpose

Provides:

```text
Reliable Kafka publishing
```

through:

```text
Transactional Outbox Pattern
```

---

# Why This Is Critical

Without outbox:

```text
DB commit succeeds
BUT
Kafka publish fails
```

causing:

```text
Distributed inconsistency
```

---

# High-Level Flow

```text
Transaction Finalized
        |
        v
Persist OutboxEvent
        |
        v
Background Publisher
        |
        v
Kafka Publish
```

---

# Why This Is Resilient

OutboxEvent stored FIRST in DB.

Even if Kafka fails:

```text
Event not lost
```

---

# Benefits

| Benefit | Purpose |
|---|---|
| Guaranteed Event Persistence | No lost events |
| Async Recovery | Retry publishing later |
| Distributed Consistency | DB + Kafka reliability |
| Fault Tolerance | Safe messaging |

---

# ==========================================
# 5. Kafka Retry Resilience
# ==========================================

# Purpose

Provides:

```text
Reliable eventual Kafka publishing
```

through:

- retries
- exponential backoff
- dead-letter handling

---

# High-Level Flow

```text
Kafka Publish
      |
   +--+---+
   |      |
SUCCESS  FAILURE
   |      |
   v      v
SENT    RETRY
```

---

# Retry Flow

If Kafka publish fails:

1. retryCount incremented

2. nextRetryAt calculated

3. scheduler retries later

---

# Exponential Backoff

Retry delay:

```text
2^retry seconds
```

Example:

| Retry | Delay |
|---|---|
| 1 | 2 sec |
| 2 | 4 sec |
| 3 | 8 sec |

---

# Why Exponential Backoff Matters

Prevents:

```text
Kafka retry storms
```

during outages.

---

# DEAD State

If retries exceed limit:

```text
Event marked DEAD
```

Meaning:

```text
Manual intervention required
```

---

# Why DEAD State Is Important

Prevents:

```text
Infinite retry loops
```

---

# ==========================================
# 6. Kafka Producer Reliability
# ==========================================

# Purpose

Ensures:

```text
Reliable message durability
```

---

# Reliability Configurations

| Configuration | Purpose |
|---|---|
| ACKS=all | Strong durability |
| enable.idempotence=true | Duplicate prevention |
| retries | Transient recovery |
| replication | Broker fault tolerance |

---

# Why ACKS=all Matters

Producer waits until:

```text
ALL replicas acknowledge message
```

before success returned.

---

# Why Idempotent Producer Matters

Protects against:

```text
Duplicate Kafka events
```

during retries.

---

# ==========================================
# 7. Transaction Lifecycle Resilience
# ==========================================

# Purpose

Protects:

```text
Workflow consistency
```

through strict state-machine validation.

---

# Lifecycle Policy

Only legal transitions allowed.

---

# Example Invalid Transitions

```text
APPROVED -> PENDING_RISK
DECLINED -> REVIEW_PENDING
```

rejected immediately.

---

# Why This Matters

Protects against:

- corrupted transaction states
- invalid workflow execution
- inconsistent financial records

---

# ==========================================
# 8. Exception Handling Resilience
# ==========================================

# Purpose

Centralized failure handling.

---

# Failure Flow

```text
Exception
      |
      v
Structured Logging
      |
      v
Metrics Collection
      |
      v
Redis Cleanup
      |
      v
Error Response
```

---

# Redis Cleanup

If orchestration fails:

```text
releaseLock(idempotencyKey)
```

removes stale:

```text
IN_PROGRESS
```

locks.

---

# Why This Matters

Allows:

```text
Safe retries after failures
```

---

# ==========================================
# 9. Timeout Protection
# ==========================================

# Purpose

Protects against:

- hanging fraud-service calls
- thread exhaustion
- slow downstream systems

---

# Timeout Configurations

| Timeout | Purpose |
|---|---|
| connectTimeout | TCP connection protection |
| readTimeout | Response wait protection |
| requestTimeout | Kafka broker protection |

---

# Why Timeouts Matter

Without timeouts:

```text
Threads may block indefinitely
```

Very dangerous in production systems.

---

# ==========================================
# 10. Graceful Degradation Strategy
# ==========================================

# Purpose

Ensures:

```text
Partial infrastructure failure
does NOT fully stop system
```

---

# Examples

| Failure | Degradation Strategy |
|---|---|
| Redis outage | DB uniqueness fallback |
| Kafka outage | Outbox retry persistence |
| Fraud-service slowdown | Retry + circuit breaker |
| Temporary network issue | Retry recovery |

---

# Resilience Characteristics

| Characteristic | Status |
|---|---|
| Retry-Safe | YES |
| Distributed Safe | YES |
| Eventual Consistency | YES |
| Fault-Tolerant | YES |
| Graceful Degradation | YES |
| Self-Healing | YES |
| Failure Isolation | YES |

---

# Production Failure Scenarios Handled

| Scenario | Handling |
|---|---|
| Duplicate Requests | Redis idempotency |
| Redis Crash | TTL recovery |
| Kafka Failure | Outbox retries |
| Fraud-Service Timeout | Retry + CB |
| Network Instability | Retry handling |
| Partial Outage | Graceful degradation |

---

# Why This Architecture Is Enterprise-Grade

This setup demonstrates patterns used in:

- banking systems
- payment gateways
- fintech platforms
- distributed transaction systems

including:

- transactional outbox
- distributed locking
- retry orchestration
- failure isolation
- eventual consistency

---

# Future Enhancements

- bulkhead isolation
- rate limiting
- distributed tracing
- chaos engineering
- dead-letter topics
- adaptive retry strategies
- fallback fraud policies

---

# Final Summary

The resilience architecture inside:

```text
transaction-service
```

implements:

```text
Production-grade distributed-system fault tolerance
```

through:

- Redis-backed idempotency
- retry-safe orchestration
- circuit-breaker isolation
- transactional outbox consistency
- reliable Kafka retries
- graceful degradation strategies

The system is designed to:

```text
Continue operating safely
even during partial infrastructure failures
```