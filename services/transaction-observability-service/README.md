# README.md

# Transaction Observability Service

# Overview

The `transaction-observability-service` is an asynchronous audit and analytics microservice responsible for:

- consuming finalized transaction events from Kafka
- persisting immutable transaction audit records
- maintaining forensic-grade event traceability
- supporting operational analytics
- generating Excel-based transaction reports
- storing failed event-processing records
- enabling observability and compliance workflows

The service acts as:

```text
Centralized transaction audit and observability platform
```

inside the distributed transaction ecosystem.

---

# High-Level Responsibilities

| Responsibility | Description |
|---|---|
| Kafka Consumption | Consume finalized transaction events |
| Immutable Audit Persistence | Store transaction history |
| Duplicate Protection | Idempotent Kafka processing |
| Failed Event Recovery | Persist failed events |
| Excel Export | Generate analytics reports |
| Payload Integrity | SHA-256 checksum validation |
| Operational Metrics | Observability instrumentation |

---

# High-Level Architecture

```text
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

Audit Persistence            Failed Event Persistence

        |
        v
TransactionObservabilityQueryService
        |
        v
Excel Export APIs
```

---

# Core Technologies

| Technology | Purpose |
|---|---|
| Java 17 | Core language |
| Spring Boot | Application framework |
| Spring Kafka | Kafka consumption |
| Spring Data JPA | Persistence |
| MySQL | Audit database |
| Spring Security | JWT security |
| OAuth2 Resource Server | Authentication |
| Micrometer | Metrics |
| Apache POI | Excel generation |

---

# Core Features

# 1. Kafka-Based Event Consumption

Consumes:

```text
TransactionFinalizedEvent
```

from Kafka asynchronously.

Supports:

- retry-safe processing
- dead-letter handling
- distributed consumption
- fault-tolerant ingestion

---

# 2. Immutable Audit Persistence

Persists:

```text
TransactionEventRecord
```

containing:

- transaction metadata
- raw payload
- checksum
- processing timestamps
- final transaction status

---

# 3. Kafka Idempotency

Prevents duplicate event persistence using:

```text
ProcessedEventRepository
```

---

# 4. Failure Recovery

Stores failed Kafka-processing events using:

```text
FailedEventService
```

Enables:

- replay workflows
- operational debugging
- forensic recovery

---

# 5. SHA-256 Payload Validation

Uses:

```text
ChecksumUtil
```

to generate:

```text
Immutable event-integrity checksums
```

---

# 6. Excel Export Support

Generates:

```text
Streaming Excel transaction reports
```

using:

```text
SXSSFWorkbook
```

for memory-safe exports.

---

# Core Components

| Component | Responsibility |
|---|---|
| TransactionEventConsumer | Kafka event listener |
| TransactionObservabilityProcessingService | Event orchestration |
| TransactionObservabilityQueryService | Query orchestration |
| FailedEventService | Failure persistence |
| ExcelServiceImpl | Excel generation |
| ChecksumUtil | Payload integrity |

---

# Security

The service uses:

```text
OAuth2 Resource Server + JWT Authentication
```

Supports:

- stateless authentication
- role-based authorization
- machine-to-machine trust

---

# Kafka Architecture

Kafka consumer architecture supports:

- retry topics
- dead-letter topics
- idempotent processing
- exponential backoff

---

# Observability

Instrumentation includes:

- Micrometer metrics
- structured logging
- payload metrics
- failure analytics
- processing latency monitoring

---

# Example Kafka Flow

```text
Transaction Finalized
        |
        v
Kafka Topic
(transaction.finalized.v1)
        |
        v
TransactionEventConsumer
        |
        v
Audit Persistence
        |
        v
Analytics / Export APIs
```

---

# Example Export Flow

```text
Client Export Request
        |
        v
TransactionObservabilityController
        |
        v
TransactionObservabilityQueryService
        |
        v
ExcelServiceImpl
        |
        v
Excel File Response
```

---

# Project Structure

```text
transaction-observability-service
│
├── controller
├── service
├── kafka
├── security
├── repository
├── entity
├── dto
├── config
├── util
└── exception
```

---

# Operational Characteristics

| Characteristic | Status |
|---|---|
| Idempotent Kafka Processing | YES |
| Immutable Audit Persistence | YES |
| Retry-Safe | YES |
| Memory-Safe Exports | YES |
| Distributed Ready | YES |
| Fault-Tolerant | YES |

---

# Why This Service Exists

The main transaction service should NOT handle:

- heavy analytics queries
- audit exports
- reporting workloads
- observability persistence

This service separates:

```text
Transactional processing
```

from:

```text
Observability & analytics workloads
```

improving:

- scalability
- reliability
- operational isolation

---

# Future Enhancements

- OpenTelemetry tracing
- Grafana dashboards
- event replay tooling
- object-storage archival
- real-time analytics pipelines
- Kafka lag monitoring

---

# Final Summary

The `transaction-observability-service` implements:

```text
Production-grade asynchronous transaction audit and observability architecture
```

providing:

- immutable transaction traceability
- fault-tolerant Kafka consumption
- scalable analytics exports
- operational monitoring
- forensic audit capabilities
- distributed observability support

The architecture closely resembles observability platforms used in:

- banking systems
- fintech audit pipelines
- distributed payment systems
- compliance-event architectures