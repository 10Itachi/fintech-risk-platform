# redis.md

# Redis Architecture

# Redis Overview

The `risk-decision-service` uses Redis primarily for:

- idempotency protection
- duplicate transaction prevention
- distributed request coordination
- retry-safe transaction processing

Redis acts as a lightweight distributed state manager protecting the fraud evaluation pipeline from duplicate execution.

---

# Redis Use Cases

| Use Case | Purpose |
|---|---|
| Idempotency Locking | Prevent duplicate transaction processing |
| Distributed Coordination | Prevent concurrent duplicate execution |
| Retry Safety | Ensure safe client retries |
| Temporary Transaction State | Track processing lifecycle |

---

# Redis Integration Architecture

```text
Incoming Request
        |
        v
RiskDecisionApplicationService
        |
        v
RedisIdempotencyService
        |
        v
Redis
```

---

# Why Redis Is Used

Fraud evaluation contains expensive operations:

- hard rule evaluation
- soft rule evaluation
- ML scoring
- database persistence

Without idempotency protection:

- duplicate payments may occur
- duplicate fraud traces may be persisted
- ML resources may be wasted
- concurrent requests may corrupt consistency

Redis solves this problem using atomic distributed locking.

---

# Idempotency Flow

```text
Incoming Transaction
        |
        v
Redis Lock Attempt
        |
   +----+----+
   |         |
SUCCESS    FAILURE
   |         |
   v         v
Process    Duplicate Request
Request    Rejected
```

---

# Redis Key Strategy

# Key Format

```text
risk:txn:{transactionId}
```

Example:

```text
risk:txn:TXN-1001
```

Purpose:

- transaction-level uniqueness
- distributed request coordination
- deterministic lookup

---

# Transaction State Lifecycle

The service maintains two transaction states.

| State | Meaning |
|---|---|
| IN_PROGRESS | Request currently being processed |
| COMPLETED:{decisionId} | Request successfully completed |

---

# State Transition Flow

```text
NEW REQUEST
      |
      v
SET IN_PROGRESS
      |
      v
Risk Evaluation Pipeline
      |
      v
SET COMPLETED:{decisionId}
```

---

# Lock Acquisition Logic

Implemented using:

```java
setIfAbsent(key, value, ttl)
```

Equivalent Redis command:

```text
SET key value NX EX ttl
```

---

# Atomic Lock Behavior

| Condition | Result |
|---|---|
| Key does not exist | Lock acquired |
| Key already exists | Duplicate request rejected |

This guarantees:

- single transaction execution
- distributed consistency
- concurrent request safety

---

# Lock Timeout Strategy

# IN_PROGRESS TTL

```text
5 minutes
```

Purpose:

- prevents stale locks
- avoids deadlocks
- allows recovery after crash

---

# COMPLETED TTL

```text
10 minutes
```

Purpose:

- supports retry-safe clients
- prevents immediate reprocessing
- reduces duplicate workload

---

# Request Processing Flow

```text
Client Request
      |
      v
tryLock(transactionId)
      |
      +----------------------+
      |                      |
 LOCK SUCCESS          LOCK FAILURE
      |                      |
      v                      v
Risk Evaluation       Duplicate Request
      |
      v
Persist Decision
      |
      v
markCompleted(transactionId, decisionId)
```

---

# Duplicate Transaction Protection

If duplicate requests arrive concurrently:

```text
Request A -> acquires lock
Request B -> rejected
```

This prevents:

- duplicate fraud decisions
- repeated ML execution
- duplicate database writes
- inconsistent audit traces

---

# RedisIdempotencyService Responsibilities

| Method | Purpose |
|---|---|
| tryLock() | Acquire distributed processing lock |
| markCompleted() | Mark transaction completed |
| getState() | Retrieve current transaction state |
| getCompletedDecisionId() | Extract stored decision ID |
| isInProgress() | Check active processing |
| releaseLock() | Remove stale lock |

---

# Example Transaction State

# During Processing

```text
risk:txn:TXN-1001 -> IN_PROGRESS
```

---

# After Completion

```text
risk:txn:TXN-1001 -> COMPLETED:9912
```

Where:

```text
9912 = decisionId
```

---

# Recovery Characteristics

If service crashes during processing:

- Redis TTL automatically expires
- stale lock removed
- request becomes retryable again

This prevents permanent deadlocks.

---

# Concurrency Protection

Redis provides distributed concurrency safety across:

- multiple application instances
- container replicas
- Kubernetes pods
- horizontally scaled deployments

---

# Distributed System Benefits

| Benefit | Description |
|---|---|
| Distributed Locking | Prevents concurrent duplicates |
| Retry Safety | Safe client retries |
| Stateless Scaling | Works across multiple pods |
| Automatic Recovery | TTL-based stale cleanup |
| Fast Access | In-memory low-latency operations |

---

# Redis and Resilience Integration

Redis works together with:

- resilient ML scoring
- resilient soft rule execution
- centralized exception handling

If evaluation fails:

- lock may be released
- transaction becomes retryable
- operational consistency preserved

---

# Integration with Resilient Components

```text
Redis Lock
      |
      v
Hard Rules
      |
      v
Resilient Soft Rules
      |
      v
Resilient ML Scoring
      |
      v
Decision Persistence
```

---

# Failure Scenarios

# Scenario 1 — Duplicate Concurrent Requests

```text
Request A -> SUCCESS
Request B -> BLOCKED
```

Result:

```text
No duplicate execution
```

---

# Scenario 2 — Service Crash

```text
IN_PROGRESS expires automatically
```

Result:

```text
Transaction retry becomes possible
```

---

# Scenario 3 — ML Timeout

```text
RiskEvaluationException raised
```

Result:

```text
Request safely terminates
```

---

# Redis Operational Characteristics

| Characteristic | Status |
|---|---|
| Distributed Safe | YES |
| Atomic Locking | YES |
| Retry Safe | YES |
| Low Latency | YES |
| Horizontally Scalable | YES |
| TTL Recovery | YES |

---

# Redis Deployment Recommendations

# Production Recommendations

- dedicated Redis cluster
- persistence enabled
- replica configuration
- eviction monitoring
- connection pooling
- TLS-enabled Redis
- Redis Sentinel or Cluster mode

---

# Recommended Future Enhancements

- Redisson distributed locks
- Lua-script atomic workflows
- Redis Streams integration
- request replay cache
- distributed rate limiting
- sliding-window fraud counters
- behavioral feature caching

---

# Redis Design Summary

| Capability | Purpose |
|---|---|
| Idempotency | Prevent duplicates |
| Distributed Locking | Concurrent safety |
| TTL Recovery | Automatic stale cleanup |
| Retry Safety | Reliable client retries |
| Fast State Lookup | Low-latency orchestration |
| Horizontal Scalability | Multi-instance safety |

---

# Final Redis Architecture Flow

```text
Incoming Transaction
        |
        v
Redis Idempotency Lock
        |
        v
Risk Evaluation Pipeline
        |
        +-------------------+
        |                   |
        v                   v
Evaluation Success    Evaluation Failure
        |                   |
        v                   v
Mark COMPLETED       Release / Expire Lock
        |
        v
Retry-Safe State
```