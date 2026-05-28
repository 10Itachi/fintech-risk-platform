# request-flow.md

# Transaction Observability Service Request Flow

# Overview

This document explains the complete:

```text
End-to-end request and event-processing lifecycle
```

inside the:

```text
transaction-observability-service
```

The service processes:

- Kafka transaction events
- historical query requests
- Excel export requests
- failed-event investigations

---

# High-Level System Flow

```text
transaction-service
        |
        v
Kafka Topic
(transaction.finalized.v1)
        |
        v
TransactionEventConsumer
        |
        v
TransactionObservabilityProcessingService
        |
        +------------------------------+
        |                              |
        v                              v

Audit Persistence          Failed Event Persistence

        |
        v
REST Query APIs
        |
        v
Excel Export APIs
```

---

# ==========================================
# 1. Kafka Event Consumption Flow
# ==========================================

# Event Source

The flow begins when:

```text
transaction-service
```

publishes:

```text
TransactionFinalizedEvent
```

to Kafka.

---

# Kafka Topic

```text
transaction.finalized.v1
```

---

# Kafka Flow

```text
Transaction Finalized
        |
        v
Kafka Topic
        |
        v
TransactionEventConsumer
```

---

# ==========================================
# 2. TransactionEventConsumer Flow
# ==========================================

# Step 1 — Kafka Message Received

Kafka broker delivers message to:

```java
@KafkaListener
```

inside:

```text
TransactionEventConsumer
```

---

# Step 2 — Structured Logging

Logs:

```text
event=kafka_message_received
```

including:

- transactionId
- eventId
- topic

---

# Step 3 — Delegate to Processing Service

Consumer calls:

```java
transactionObservabilityProcessingService.process(event)
```

---

# Consumer Flow

```text
Kafka Message
      |
      v
Consumer Listener
      |
      v
Processing Service
```

---

# ==========================================
# 3. TransactionObservabilityProcessingService Flow
# ==========================================

# Purpose

Acts as:

```text
Primary event-processing orchestration engine
```

---

# Step 1 — Start Processing Timer

```java
long startTime = System.currentTimeMillis();
```

Purpose:

```text
Track event-processing latency
```

---

# Step 2 — Extract Event Metadata

Extracts:

- eventId
- transactionId
- finalStatus

---

# Step 3 — Duplicate Validation

Checks:

```java
processedEventRepository.existsByEventId(eventId)
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

# Why This Exists

Kafka guarantees:

```text
At-least-once delivery
```

so duplicates are possible.

---

# Step 4 — Serialize Event Payload

Uses:

```java
objectMapper.writeValueAsString(event)
```

---

# Purpose

Generate immutable raw payload storage.

---

# Step 5 — Record Payload Size Metric

```java
payloadSizeMetric.record(...)
```

Purpose:

```text
Monitor event-size growth
```

---

# Step 6 — Generate SHA-256 Checksum

Calls:

```java
ChecksumUtil.sha256(payload)
```

---

# Checksum Flow

```text
Serialized Payload
        |
        v
SHA-256 Hash
```

---

# Why This Matters

Provides:

- tamper detection
- forensic validation
- immutable audit integrity

---

# Step 7 — Build TransactionEventRecord

Creates immutable audit entity.

---

# Stored Fields

| Field | Purpose |
|---|---|
| transactionId | Transaction traceability |
| eventId | Kafka identity |
| payload | Immutable raw event |
| checksum | Integrity validation |
| finalStatus | Final transaction state |

---

# Step 8 — Persist Audit Record

```text
TransactionEventRecord
        |
        v
MySQL Persistence
```

---

# Step 9 — Persist ProcessedEvent

Stores processed event marker.

---

# Why This Exists

Acts as:

```text
Kafka idempotency registry
```

---

# Step 10 — Metrics Increment

```java
processedCounter.increment()
```

Tracks successful processing.

---

# Step 11 — Success Logging

Logs:

```text
event=transaction_event_processed
```

---

# ==========================================
# 4. Failure Flow
# ==========================================

# If Processing Fails

```text
Exception
      |
      v
failureCounter.increment()
      |
      v
FailedEventService.persist()
```

---

# FailedEventService Flow

# Step 1 — Truncate Oversized Payload

Protects DB from huge payloads.

---

# Step 2 — Build FailedTransactionEvent

Stores:

- payload
- topic
- error
- failedAt

---

# Step 3 — Persist Failure Record

```text
FailedTransactionEvent
        |
        v
MySQL Persistence
```

---

# Why This Matters

Ensures:

```text
No Kafka event silently disappears
```

---

# ==========================================
# 5. Query API Request Flow
# ==========================================

# Request Entry

Client calls:

```http
GET /observability/transactions
```

or related APIs.

---

# Flow

```text
HTTP Request
      |
      v
TransactionObservabilityController
      |
      v
TransactionObservabilityQueryService
      |
      v
Repository Layer
      |
      v
MySQL
```

---

# Query Responsibilities

| Responsibility | Purpose |
|---|---|
| Historical Queries | Transaction lookup |
| Failed Event Queries | Operational debugging |
| Pagination | Scalable querying |

---

# ==========================================
# 6. Excel Export Flow
# ==========================================

# Export Request

Client calls export API.

---

# Flow

```text
Export Request
        |
        v
Controller
        |
        v
Query Service
        |
        v
ExcelServiceImpl
```

---

# ExcelServiceImpl Flow

# Step 1 — Start Export Timer

Tracks export latency.

---

# Step 2 — Create Streaming Workbook

Uses:

```java
SXSSFWorkbook
```

---

# Why Streaming Workbook Matters

Prevents:

```text
Heap-memory spikes
```

during large exports.

---

# Step 3 — Generate Excel Headers

Creates report structure.

---

# Step 4 — Populate Transaction Rows

Maps:

```text
TransactionEventRecord
```

into Excel rows.

---

# Step 5 — Write Workbook

Workbook written into:

```java
ByteArrayOutputStream
```

---

# Step 6 — Return File Response

Controller returns Excel file.

---

# ==========================================
# 7. Security Flow
# ==========================================

# Security Architecture

Uses:

```text
OAuth2 Resource Server + JWT Authentication
```

---

# Security Flow

```text
HTTP Request
      |
      v
JWT Validation
      |
      v
Role Extraction
      |
      v
Controller Authorization
```

---

# ==========================================
# 8. Kafka Retry Flow
# ==========================================

# Retry Architecture

```text
Kafka Failure
      |
      v
Retry Topic
      |
      v
Retry Consumer
      |
   +--+---+
   |      |
SUCCESS  FAILURE
   |      |
   v      v
ACK      DLT
```

---

# Why DLT Exists

Protects system from:

```text
Poison messages
```

---

# ==========================================
# 9. Complete End-to-End Flow
# ==========================================

```text
transaction-service
        |
        v
Kafka Topic
(transaction.finalized.v1)
        |
        v
TransactionEventConsumer
        |
        v
TransactionObservabilityProcessingService
        |
        +------------------------------+
        |                              |
        v                              v

ChecksumUtil                 FailedEventService

        |
        v
TransactionEventRecord
        |
        v
MySQL Audit Storage
        |
        v
Query APIs
        |
        v
Excel Export APIs
```

---

# Operational Characteristics

| Characteristic | Status |
|---|---|
| Async Event Processing | YES |
| Idempotent Kafka Handling | YES |
| Immutable Persistence | YES |
| Retry-Safe | YES |
| Failure Recovery | YES |
| Memory-Safe Exports | YES |

---

# Why This Architecture Is Enterprise-Grade

This request-processing architecture demonstrates patterns used in:

- banking audit systems
- fintech observability platforms
- distributed analytics systems
- compliance-event pipelines

including:

- asynchronous event processing
- Kafka idempotency
- immutable audit persistence
- retry orchestration
- export scalability
- operational observability

---

# Final Summary

The `transaction-observability-service` request flow implements:

```text
Production-grade asynchronous financial-event observability processing
```

through:

- Kafka-driven ingestion
- immutable audit persistence
- failure recovery workflows
- scalable analytics querying
- streaming Excel exports
- operational instrumentation

The system is designed to provide:

```text
Reliable financial auditability
and distributed observability at scale
```