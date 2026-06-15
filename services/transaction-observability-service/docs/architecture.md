# architecture.md

# Transaction Observability Service Architecture

# Overview

The `transaction-observability-service` is an:

```text
Asynchronous event-driven audit and analytics microservice
```

responsible for:

- consuming finalized transaction events
- persisting immutable audit records
- providing historical transaction analytics
- supporting export/reporting workflows
- maintaining forensic-grade observability

The service is intentionally separated from:

```text
transaction-service
```

to isolate:

- operational analytics
- heavy query workloads
- export generation
- audit persistence

from core transactional workflows.

---

# Architectural Goals

| Goal | Purpose |
|---|---|
| Async Processing | Decouple analytics from transactions |
| Immutable Auditability | Financial traceability |
| Scalable Analytics | Separate read-heavy workloads |
| Fault Tolerance | Reliable Kafka processing |
| Operational Visibility | Observability support |
| Export Support | Reporting workflows |

---

# High-Level Architecture

```text
transaction-service
        |
        v
Kafka Topic
(transaction.finalized.v1)
        |
        v
transaction-observability-service
        |
        +------------------------------+
        |                              |
        v                              v

Audit Persistence          Failed Event Persistence

        |
        v
Query APIs
        |
        v
Excel Export Generation
```

---

# Architectural Style

The system follows:

```text
Event-Driven Microservice Architecture
```

using:

- asynchronous Kafka communication
- immutable event persistence
- CQRS-style workload separation
- observability-focused processing

---

# Core Architectural Layers

```text
Controller Layer
        |
        v
Query Orchestration Layer
        |
        v
Processing Services
        |
        v
Persistence Layer
        |
        v
MySQL Database
```

---

# Main Components

| Component | Responsibility |
|---|---|
| TransactionEventConsumer | Kafka event ingestion |
| TransactionObservabilityProcessingService | Event orchestration |
| TransactionObservabilityQueryService | Query orchestration |
| FailedEventService | Failure persistence |
| ExcelServiceImpl | Report generation |
| ChecksumUtil | Integrity validation |

---

# ==========================================
# 1. Kafka Ingestion Layer
# ==========================================

# Purpose

Consumes:

```text
TransactionFinalizedEvent
```

from Kafka.

---

# Main Component

```text
TransactionEventConsumer
```

---

# Responsibilities

| Responsibility | Purpose |
|---|---|
| Kafka Listening | Event ingestion |
| Retry Integration | Failure recovery |
| Event Delegation | Service orchestration |

---

# Kafka Flow

```text
Kafka Topic
        |
        v
TransactionEventConsumer
        |
        v
Processing Service
```

---

# Why Kafka Is Used

Provides:

- asynchronous decoupling
- scalable event distribution
- retry-safe communication
- distributed integration

---

# ==========================================
# 2. Processing Layer
# ==========================================

# Main Component

```text
TransactionObservabilityProcessingService
```

---

# Purpose

Acts as:

```text
Central event-processing orchestration engine
```

---

# Responsibilities

| Responsibility | Purpose |
|---|---|
| Idempotency Validation | Prevent duplicates |
| Payload Serialization | Raw-event persistence |
| Checksum Generation | Tamper detection |
| Audit Persistence | Immutable storage |
| Metrics Collection | Observability |

---

# Processing Flow

```text
Kafka Event
      |
      v
Duplicate Validation
      |
      v
Checksum Generation
      |
      v
Audit Persistence
```

---

# Why Idempotency Matters

Kafka provides:

```text
At-least-once delivery
```

meaning duplicates MAY occur.

The service guarantees:

```text
Exactly-once business persistence
```

through idempotency validation.

---

# ==========================================
# 3. Query Layer
# ==========================================

# Main Component

```text
TransactionObservabilityQueryService
```

---

# Purpose

Provides:

```text
Historical transaction observability APIs
```

---

# Responsibilities

| Responsibility | Purpose |
|---|---|
| Transaction Queries | Historical lookup |
| Failed Event Queries | Operational debugging |
| Export Coordination | Reporting |
| Pagination | Scalable querying |

---

# Query Flow

```text
REST API
      |
      v
Query Service
      |
      v
Repositories
      |
      v
MySQL
```

---

# Why Separate Query Layer Exists

Separates:

```text
Read orchestration
```

from:

```text
Kafka event processing
```

---

# ==========================================
# 4. Excel Export Layer
# ==========================================

# Main Component

```text
ExcelServiceImpl
```

---

# Purpose

Generates:

```text
Streaming Excel reports
```

for analytics exports.

---

# Export Flow

```text
Transaction Records
        |
        v
SXSSFWorkbook
        |
        v
Excel File
```

---

# Why Streaming Workbook Is Used

Uses:

```java
SXSSFWorkbook
```

to avoid:

```text
Large heap-memory spikes
```

during large exports.

---

# ==========================================
# 5. Failure Recovery Layer
# ==========================================

# Main Component

```text
FailedEventService
```

---

# Purpose

Provides:

```text
Failure persistence and recovery
```

---

# Failure Flow

```text
Processing Failure
        |
        v
FailedEventService
        |
        v
FailedTransactionEvent
```

---

# Why This Matters

Ensures:

```text
No Kafka event disappears silently
```

---

# ==========================================
# 6. Integrity Validation Layer
# ==========================================

# Main Component

```text
ChecksumUtil
```

---

# Purpose

Provides:

```text
SHA-256 payload integrity validation
```

---

# Flow

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

# Why Checksums Matter

Supports:

- forensic auditing
- tamper detection
- immutable traceability

---

# ==========================================
# Database Architecture
# ==========================================

# Main Tables

| Table | Purpose |
|---|---|
| transaction_event_record | Immutable transaction audits |
| processed_event | Kafka idempotency registry |
| failed_transaction_event | Failure recovery persistence |

---

# Persistence Characteristics

| Characteristic | Purpose |
|---|---|
| Immutable Persistence | Audit safety |
| Idempotent Writes | Duplicate prevention |
| Historical Traceability | Operational analytics |

---

# ==========================================
# Security Architecture
# ==========================================

Uses:

```text
OAuth2 Resource Server + JWT Authentication
```

---

# Security Features

| Feature | Purpose |
|---|---|
| Stateless JWT Security | Scalable auth |
| Role-Based Authorization | Secure APIs |
| OAuth2 Validation | Trusted requests |

---

# ==========================================
# Observability Architecture
# ==========================================

Supports:

- Micrometer metrics
- structured logging
- processing latency tracking
- failure analytics
- export instrumentation

---

# Monitoring Flow

```text
Application Metrics
        |
        v
Micrometer
        |
        v
Prometheus
        |
        v
Grafana
```

---

# ==========================================
# Resilience Architecture
# ==========================================

Supports:

- Kafka retry topics
- dead-letter topics
- idempotent processing
- failure persistence
- export safety limits

---

# Failure Flow

```text
Kafka Failure
      |
      v
Retry Topic
      |
      v
DLT
```

---

# ==========================================
# Distributed-System Interaction
# ==========================================

# End-to-End Flow

```text
transaction-service
        |
        v
Kafka Event
        |
        v
transaction-observability-service
        |
        +------------------------------+
        |                              |
        v                              v

Audit Storage                Export APIs
```

---

# Why This Architecture Is Good

Separates:

| Concern | Service |
|---|---|
| Transaction Processing | transaction-service |
| Audit & Analytics | observability-service |

---

# Benefits

- scalability
- workload isolation
- operational safety
- analytics optimization

---

# Operational Characteristics

| Characteristic | Status |
|---|---|
| Event-Driven | YES |
| Idempotent | YES |
| Immutable Auditability | YES |
| Distributed Ready | YES |
| Fault-Tolerant | YES |
| Export Scalable | YES |

---

# Why This Architecture Is Enterprise-Grade

This architecture demonstrates patterns used in:

- banking observability platforms
- fintech audit systems
- distributed analytics systems
- event-driven compliance architectures

including:

- CQRS-style separation
- immutable event persistence
- Kafka-driven integration
- audit-focused architecture
- scalable export generation

---

# Future Enhancements

- OpenTelemetry tracing
- event replay tooling
- Kafka lag dashboards
- Elasticsearch integration
- object-storage archival
- real-time analytics streaming

---

# Final Summary

The `transaction-observability-service` architecture implements:

```text
Production-grade asynchronous financial-event observability architecture
```

through:

- Kafka-driven event ingestion
- immutable audit persistence
- scalable analytics querying
- streaming export generation
- forensic traceability
- operational observability

The system is designed to support:

```text
Enterprise-grade financial audit and analytics workflows
```