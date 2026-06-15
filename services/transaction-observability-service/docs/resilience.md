# resilience.md

# Transaction Observability Service — Resilience Architecture

# Overview

The `transaction-observability-service` implements:

```text
Asynchronous event-processing resilience architecture
```

designed to guarantee:

- duplicate-safe Kafka processing
- immutable audit persistence
- failure recovery
- controlled memory usage
- fault-tolerant export generation
- operational stability

This service is optimized for:

```text
Reliable financial audit-event processing
```

where:

```text
No finalized transaction event should disappear silently
```

---

# Resilience Goals

| Goal | Purpose |
|---|---|
| Kafka Duplicate Protection | Prevent duplicate persistence |
| Failure Recovery | Persist failed events |
| Tamper Protection | Immutable audit integrity |
| Export Safety | Prevent memory exhaustion |
| Controlled Persistence | Protect DB stability |
| Graceful Failure Handling | Operational continuity |

---

# High-Level Resilience Architecture

```text
Kafka Event
      |
      v
Idempotency Validation
      |
      v
Checksum Validation
      |
      v
Immutable Persistence
      |
   +--+---+
   |      |
SUCCESS  FAILURE
   |      |
   v      v
Audit DB  FailedEventService
```

---

# Core Resilience Components

| Component | Responsibility |
|---|---|
| TransactionObservabilityProcessingService | Idempotent event processing |
| FailedEventService | Failure recovery persistence |
| ChecksumUtil | Payload integrity |
| ProcessedEventRepository | Duplicate prevention |
| ExcelServiceImpl | Memory-safe exports |

---

# ==========================================
# 1. Kafka Idempotency Resilience
# ==========================================

# Purpose

Protects against:

```text
Duplicate Kafka event delivery
```

---

# Why This Is Critical

Kafka guarantees:

```text
At-least-once delivery
```

Meaning duplicates MAY happen.

Without idempotency:

```text
Duplicate audit records possible
```

Very dangerous in financial systems.

---

# High-Level Flow

```text
Kafka Event Received
        |
        v
Check eventId
        |
   +----+----+
   |         |
NEW EVENT  DUPLICATE
   |         |
   v         v
PROCESS     SKIP
```

---

# Idempotency Registry

Uses:

```text
ProcessedEventRepository
```

to persist processed event IDs.

---

# Why This Is Good Design

Enables:

```text
Exactly-once business processing
```

even though Kafka itself provides:

```text
At-least-once delivery
```

---

# Duplicate Flow

```text
Duplicate Event
        |
        v
duplicateCounter.increment()
        |
        v
Skip Processing
```

---

# Benefits

| Benefit | Purpose |
|---|---|
| Duplicate Prevention | Safe Kafka retries |
| Idempotent Persistence | Audit consistency |
| Replay Safety | Event recovery support |

---

# ==========================================
# 2. Failed Event Recovery Resilience
# ==========================================

# Purpose

Ensures:

```text
Failed Kafka events never disappear silently
```

---

# Why This Matters

Without failure persistence:

```text
Operational debugging becomes impossible
```

and:

```text
Events may be permanently lost
```

---

# Failure Flow

```text
Kafka Processing Failure
        |
        v
FailedEventService.persist()
        |
        v
FailedTransactionEvent
        |
        v
MySQL Persistence
```

---

# Persisted Failure Data

| Field | Purpose |
|---|---|
| payload | Raw failed event |
| error | Failure reason |
| topic | Kafka topic |
| failedAt | Failure timestamp |

---

# Why Raw Payload Is Stored

Enables:

- event replay
- forensic debugging
- manual recovery
- operational investigation

---

# Failure Recovery Benefits

| Benefit | Purpose |
|---|---|
| No Silent Failure | Operational safety |
| Replay Support | Recovery workflows |
| Debugging Support | Root-cause analysis |
| Audit Safety | Immutable failure trace |

---

# ==========================================
# 3. Payload Integrity Resilience
# ==========================================

# Purpose

Provides:

```text
Immutable audit-event integrity verification
```

---

# Component

```text
ChecksumUtil
```

---

# Why Checksums Matter

Protects against:

- payload tampering
- audit corruption
- storage inconsistencies

---

# High-Level Flow

```text
Serialized Payload
        |
        v
SHA-256 Hash
        |
        v
Checksum Persistence
```

---

# Why SHA-256 Is Used

Benefits:

- deterministic hashing
- cryptographic stability
- tamper detection
- JVM-native support

---

# Example Use Case

If payload modified later:

```text
Checksum mismatch detected
```

---

# Resilience Benefits

| Benefit | Purpose |
|---|---|
| Tamper Detection | Immutable auditability |
| Payload Integrity | Financial-grade auditing |
| Forensic Validation | Compliance safety |

---

# ==========================================
# 4. Export Memory Resilience
# ==========================================

# Purpose

Protects against:

```text
Heap-memory exhaustion
```

during large Excel exports.

---

# Component

```text
ExcelServiceImpl
```

---

# Streaming Workbook

Uses:

```java
SXSSFWorkbook
```

instead of standard workbook.

---

# Why Streaming Workbook Matters

Prevents:

```text
Entire Excel dataset loading into memory
```

---

# ROW_ACCESS_WINDOW_SIZE

```java
100
```

Meaning:

```text
Only 100 rows remain in memory
```

at once.

---

# Why This Is Critical

Enables:

```text
Large dataset exports safely
```

without:

- heap spikes
- OutOfMemoryError
- JVM instability

---

# Export Flow

```text
Large Dataset
      |
      v
Streaming Workbook
      |
      v
Incremental Row Writing
      |
      v
Compressed Temp Files
```

---

# Export Record Limit

```java
MAX_EXPORT_RECORDS = 10_000
```

---

# Why Export Limit Exists

Protects against:

- abusive exports
- memory overload
- giant file generation

---

# Benefits

| Benefit | Purpose |
|---|---|
| Memory Safety | Stable JVM |
| Large Export Support | Scalability |
| Controlled Resource Usage | Operational protection |

---

# ==========================================
# 5. Payload Truncation Resilience
# ==========================================

# Purpose

Protects database and memory systems from:

```text
Oversized payload persistence
```

---

# Payload Limits

| Type | Max Length |
|---|---|
| payload | 50,000 chars |
| error | 2,000 chars |

---

# Why Truncation Exists

Prevents:

- DB bloat
- huge stacktrace persistence
- excessive memory usage
- storage abuse

---

# Truncation Flow

```text
Large Payload
      |
      v
Length Validation
      |
   +--+---+
   |      |
VALID   TOO LARGE
   |      |
   v      v
SAVE   TRUNCATE
```

---

# Benefits

| Benefit | Purpose |
|---|---|
| Storage Protection | DB stability |
| Memory Protection | JVM safety |
| Controlled Persistence | Predictable storage |

---

# ==========================================
# 6. Exception Handling Resilience
# ==========================================

# Purpose

Provides:

```text
Controlled operational failure handling
```

---

# Failure Handling Flow

```text
Exception
      |
      v
Structured Logging
      |
      v
failureCounter.increment()
      |
      v
FailedEvent Persistence
```

---

# Why Metrics During Failures Matter

Enables:

- operational alerting
- failure visibility
- production monitoring

---

# Benefits

| Benefit | Purpose |
|---|---|
| Failure Visibility | Operational awareness |
| Metrics Tracking | Reliability monitoring |
| Recovery Support | Safer debugging |

---

# ==========================================
# 7. Database Persistence Resilience
# ==========================================

# Purpose

Ensures:

```text
Reliable immutable audit persistence
```

---

# Persistence Characteristics

| Characteristic | Purpose |
|---|---|
| Immutable Records | Audit safety |
| Transactional Persistence | Consistency |
| Event Replayability | Recovery support |

---

# Persistence Flow

```text
Kafka Event
      |
      v
Immutable Entity
      |
      v
MySQL Persistence
```

---

# Why Immutable Persistence Matters

Protects against:

- audit corruption
- accidental updates
- trace inconsistency

---

# ==========================================
# 8. Metrics-Based Operational Resilience
# ==========================================

# Purpose

Provides:

```text
Operational visibility into failures
```

---

# Key Metrics

| Metric | Purpose |
|---|---|
| processedCounter | Successful events |
| duplicateCounter | Duplicate Kafka deliveries |
| failureCounter | Processing failures |
| payloadSizeMetric | Payload growth monitoring |
| exportFailureCounter | Export failures |

---

# Why Metrics Improve Resilience

Metrics allow operators to detect:

- Kafka instability
- duplicate spikes
- abnormal payload growth
- DB slowdowns

before system failure escalates.

---

# ==========================================
# 9. Graceful Failure Characteristics
# ==========================================

| Failure Scenario | Handling |
|---|---|
| Duplicate Kafka delivery | Skip processing |
| Serialization failure | Persist failed event |
| Oversized payload | Truncate safely |
| Large export request | Streaming workbook |
| Memory pressure | Export limits |
| Payload tampering | Checksum validation |

---

# Resilience Characteristics

| Characteristic | Status |
|---|---|
| Idempotent Processing | YES |
| Failure Recovery | YES |
| Tamper Detection | YES |
| Memory Safe | YES |
| Graceful Degradation | YES |
| Operational Visibility | YES |

---

# Why This Architecture Is Enterprise-Grade

This service demonstrates resilience patterns used in:

- banking audit systems
- fintech observability platforms
- compliance-event pipelines
- distributed event-processing systems

including:

- Kafka idempotency
- immutable persistence
- forensic audit validation
- failure-event recovery
- streaming exports
- operational protection

---

# Future Enhancements

- dead-letter Kafka topics
- retry queues
- replay pipelines
- distributed tracing
- adaptive backpressure
- OpenTelemetry spans
- object-storage archival

---

# Final Summary

The resilience architecture inside:

```text
transaction-observability-service
```

implements:

```text
Production-grade fault-tolerant audit-event processing
```

through:

- idempotent Kafka handling
- immutable persistence
- failed-event recovery
- memory-safe exports
- checksum validation
- operational protection

The system is designed to guarantee:

```text
Reliable financial-event auditability
even during infrastructure failures
```