# redis.md

# Redis Architecture

# Overview

The `transaction-service` uses Redis for:

```text
Distributed idempotency management
```

to guarantee:

- duplicate request prevention
- retry-safe transaction execution
- distributed locking
- concurrency safety
- resilient transaction orchestration

Redis acts as:

```text
Fast distributed coordination layer
```

between multiple application instances.

---

# Why Redis Is Used

Financial systems MUST prevent:

- duplicate payments
- double transaction execution
- concurrent duplicate requests
- retry amplification

Redis provides:

```text
Low-latency distributed synchronization
```

required for safe transaction orchestration.

---

# Core Redis Responsibilities

| Responsibility | Purpose |
|---|---|
| Distributed Locking | Prevent concurrent execution |
| Idempotency Tracking | Retry-safe APIs |
| Duplicate Detection | Avoid duplicate payments |
| Temporary Coordination | Lightweight orchestration state |
| TTL Recovery | Automatic stale-lock cleanup |

---

# High-Level Redis Architecture

```text
Incoming Request
        |
        v
RedisIdempotencyService
        |
        v
Redis Distributed Lock
        |
   +----+----+
   |         |
SUCCESS    FAILURE
   |         |
   v         v
PROCESS   DUPLICATE
REQUEST   REQUEST
```

---

# Redis Components

| Component | Responsibility |
|---|---|
| RedisConfig | Redis infrastructure configuration |
| RedisIdempotencyService | Distributed idempotency handling |

---

# ==========================================
# 1. RedisConfig
# ==========================================

# Purpose

Acts as:

```text
Redis infrastructure configuration layer
```

Responsible for:

- Redis connection setup
- RedisTemplate creation
- serialization configuration
- distributed Redis access

---

# High-Level Flow

```text
Spring Boot
      |
      v
RedisConfig
      |
      v
RedisConnectionFactory
      |
      v
RedisTemplate
      |
      v
Redis Operations
```

---

# Main Responsibilities

| Responsibility | Purpose |
|---|---|
| Redis Connection | Connect to Redis server |
| RedisTemplate Bean | Redis operation abstraction |
| Serialization | Key/value conversion |
| Infrastructure Wiring | Spring integration |

---

# RedisTemplate

The application uses:

```java
RedisTemplate<String, String>
```

Meaning:

- Redis keys stored as strings
- Redis values stored as strings

---

# Why String-Based Redis Is Used

Benefits:

- simpler debugging
- easier operational visibility
- lightweight storage
- lower serialization complexity

---

# Redis Serialization

Uses:

```text
String serialization
```

for:

- keys
- values

---

# Example Redis Entry

```text
Key:
idm:txn:abc-123

Value:
IN_PROGRESS
```

---

# Why Centralized RedisConfig Is Important

Without centralized config:

```text
Redis setup duplicated everywhere
```

This class provides:

```text
Reusable Redis infrastructure
```

---

# ==========================================
# 2. RedisIdempotencyService
# ==========================================

# Purpose

Acts as:

```text
Distributed idempotency coordination engine
```

This is the MOST IMPORTANT Redis component.

---

# Main Responsibilities

| Responsibility | Purpose |
|---|---|
| Lock Acquisition | Prevent duplicate processing |
| Completion Tracking | Store finalized transaction |
| Duplicate Detection | Safe retries |
| Lock Release | Failure recovery |
| TTL Management | Automatic cleanup |

---

# Why This Service Exists

Clients may retry requests because of:

- network failures
- timeouts
- frontend retries
- mobile reconnects

Without idempotency:

```text
Retries could create duplicate transactions
```

Very dangerous.

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
SUCCESS    FAILURE
   |         |
   v         v
PROCESS   DUPLICATE
REQUEST   REQUEST
```

---

# Redis Key Design

Keys follow format:

```text
idm:txn:{idempotencyKey}
```

---

# Why Namespaced Keys Matter

Benefits:

- avoids collisions
- operational clarity
- easier Redis debugging
- scalable key organization

---

# Example Key

```text
idm:txn:REQ-991122
```

---

# Redis State Machine

# IN_PROGRESS

Meaning:

```text
Transaction currently processing
```

---

# COMPLETED:{transactionId}

Meaning:

```text
Transaction successfully finalized
```

---

# Example States

| Redis Value | Meaning |
|---|---|
| IN_PROGRESS | Active processing |
| COMPLETED:txn-991 | Successfully completed |

---

# ==========================================
# tryLock() Flow
# ==========================================

# Purpose

Attempts distributed lock acquisition.

---

# Internal Flow

```text
Request Arrives
        |
        v
Generate Redis Key
        |
        v
SETNX Operation
        |
   +----+----+
   |         |
SUCCESS    FAILURE
```

---

# What SETNX Means

Redis internally performs:

```text
SET if NOT EXISTS
```

Atomic operation.

---

# Why Atomic Operations Matter

Protects against:

```text
Race conditions
```

when multiple application instances process same request.

---

# Success Scenario

If key absent:

```text
Lock acquired
```

Redis stores:

```text
IN_PROGRESS
```

---

# Failure Scenario

If key already exists:

```text
Duplicate request detected
```

---

# TTL Configuration

Lock stored with:

```text
Expiration timeout
```

---

# Why TTL Is Critical

If application crashes:

```text
IN_PROGRESS lock eventually expires
```

preventing:

- permanent deadlocks
- stuck transactions
- infinite lock retention

---

# Example Failure Recovery

```text
Request Started
        |
        v
Redis Lock Created
        |
        v
Application Crash
        |
        v
TTL Expiration
        |
        v
Lock Removed Automatically
```

---

# ==========================================
# markCompleted() Flow
# ==========================================

# Purpose

Marks transaction as:

```text
Successfully finalized
```

---

# Internal Flow

```text
Transaction Finalized
        |
        v
Redis Value Updated
        |
        v
COMPLETED:{txnId}
```

---

# Example

```text
COMPLETED:txn-991
```

---

# Why Completion State Matters

Enables:

```text
Safe duplicate retries
```

---

# Duplicate Retry Flow

```text
Duplicate Request
        |
        v
Redis COMPLETED State
        |
        v
Existing Transaction Lookup
        |
        v
Return Existing Response
```

---

# Why This Is Important

Clients can safely retry requests without:

- duplicate DB writes
- duplicate Kafka events
- duplicate fraud scoring

---

# ==========================================
# isCompleted() Flow
# ==========================================

# Purpose

Checks whether request already completed.

---

# Internal Logic

```text
Read Redis Value
        |
        v
Starts With:
COMPLETED:
```

---

# Result

If completed:

```text
Existing transaction returned
```

instead of reprocessing.

---

# ==========================================
# getCompletedTransactionId() Flow
# ==========================================

# Purpose

Extracts:

```text
Previously completed transaction ID
```

from Redis state.

---

# Example

Redis value:

```text
COMPLETED:txn-991
```

Method extracts:

```text
txn-991
```

---

# Why This Exists

Allows orchestration layer to:

```text
Fetch existing transaction response
```

during retries.

---

# ==========================================
# releaseLock() Flow
# ==========================================

# Purpose

Removes Redis lock during failure scenarios.

---

# Failure Flow

```text
Transaction Failure
        |
        v
releaseLock()
        |
        v
Redis Key Deleted
```

---

# Why This Is Critical

Without cleanup:

```text
Retries permanently blocked
```

---

# Example Failure Scenario

```text
Lock Acquired
        |
        v
Fraud-Service Failure
        |
        v
Exception Thrown
        |
        v
releaseLock()
        |
        v
Retry Allowed
```

---

# ==========================================
# Graceful Redis Degradation
# ==========================================

# Very Important Design Decision

If Redis becomes unavailable:

```text
Transaction-service STILL operates
```

---

# Fallback Strategy

Fallback protection uses:

```text
Database unique constraints
```

---

# Failure Flow

```text
Redis Failure
      |
      v
Catch Exception
      |
      v
Warning Log
      |
      v
Continue Processing
```

---

# Why This Is Enterprise-Grade

Redis should improve:

```text
Safety + performance
```

but should NOT become:

```text
Single point of failure
```

---

# ==========================================
# Distributed Concurrency Protection
# ==========================================

# Problem Solved

Multiple application instances may process same request simultaneously.

---

# Example Scenario

```text
Load Balancer
      |
      +----> Instance A
      |
      +----> Instance B
```

Both receive same retry request.

---

# Redis Solution

```text
Atomic distributed lock
```

ensures ONLY ONE instance processes request.

---

# Concurrency Flow

```text
Instance A -> tryLock() -> SUCCESS
Instance B -> tryLock() -> FAILURE
```

---

# Why This Is Critical

Prevents:

- duplicate transactions
- double spending
- inconsistent financial states

---

# ==========================================
# Redis Failure Scenarios Handled
# ==========================================

| Failure Scenario | Handling |
|---|---|
| Duplicate client retry | Existing transaction returned |
| Concurrent requests | Distributed lock |
| Application crash | TTL expiration |
| Redis outage | DB fallback |
| Fraud-service failure | Lock cleanup |
| Partial network failure | Retry-safe orchestration |

---

# ==========================================
# Redis Operational Characteristics
# ==========================================

| Characteristic | Status |
|---|---|
| Distributed Safe | YES |
| Retry-Safe | YES |
| Fault-Tolerant | YES |
| Graceful Degradation | YES |
| Horizontally Scalable | YES |
| Low Latency | YES |

---

# Redis Data Lifecycle

```text
Request Arrives
        |
        v
IN_PROGRESS
        |
        +--------------------+
        |                    |
SUCCESSFUL              FAILURE
PROCESSING              PROCESSING
        |                    |
        v                    v
COMPLETED:{txnId}       releaseLock()
```

---

# Why Redis Is Better Than DB Locking Here

| Redis | DB Locking |
|---|---|
| Extremely fast | Slower |
| Lightweight | Heavy transactions |
| Distributed-friendly | Harder scaling |
| TTL support | Manual cleanup |
| Lower DB contention | DB bottlenecks |

---

# Security Considerations

Redis stores ONLY:

- orchestration coordination state
- temporary idempotency metadata

No sensitive financial payloads stored.

---

# Future Enhancements

- Redis cluster mode
- Redisson distributed locks
- Redis Sentinel failover
- distributed cache metrics
- Lua-script atomic workflows
- Redis monitoring dashboards

---

# Final Summary

The Redis architecture inside:

```text
transaction-service
```

implements:

```text
Production-grade distributed idempotency orchestration
```

through:

- atomic distributed locking
- retry-safe transaction handling
- duplicate prevention
- TTL-based recovery
- graceful degradation
- concurrency protection

The design closely resembles Redis orchestration patterns used in:

- payment gateways
- banking systems
- fintech platforms
- distributed transaction systems