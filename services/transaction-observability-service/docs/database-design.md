# database-design.md

# Transaction Observability Service — Database Design

# Overview

The `transaction-observability-service` uses:

```text
MySQL
```

as its primary persistence layer for:

- immutable transaction audit storage
- Kafka idempotency tracking
- failed-event recovery
- operational observability persistence

The database architecture is optimized for:

```text
Financial-grade auditability and asynchronous event persistence
```

---

# Database Goals

| Goal | Purpose |
|---|---|
| Immutable Audit Persistence | Historical transaction traceability |
| Kafka Idempotency | Duplicate-event prevention |
| Failure Recovery | Persist failed events |
| Query Optimization | Analytics & export support |
| Operational Visibility | Observability support |

---

# Database Technology

```text
MySQL
```

---

# Persistence Characteristics

| Characteristic | Status |
|---|---|
| Immutable Persistence | YES |
| Event-Driven | YES |
| Audit-Oriented | YES |
| Query-Optimized | YES |
| Retry-Safe | YES |

---

# High-Level Persistence Architecture

```text
Kafka Event
      |
      v
Processing Service
      |
      +------------------------------+
      |                              |
      v                              v

TransactionEventRecord      ProcessedEvent
(Audit Persistence)         (Idempotency)

      |
      v

FailedTransactionEvent
(Failure Recovery)
```

---

# Core Tables

| Table | Purpose |
|---|---|
| transaction_event_records | Immutable transaction audit records |
| processed_events | Kafka idempotency registry |
| failed_transaction_events | Failed-event recovery persistence |

---

# ==========================================
# 1. transaction_event_records
# ==========================================

# Purpose

Acts as:

```text
Primary immutable transaction audit table
```

Stores:

- finalized transaction events
- raw payloads
- integrity checksums
- event metadata
- historical observability records

---

# Table Structure

| Column | Type | Nullable | Key | Purpose |
|---|---|---|---|---|
| id | BIGINT | NO | PRI | Internal DB identifier |
| event_id | BINARY(16) | NO | UNI | Kafka event UUID |
| transaction_id | BINARY(16) | NO | UNI | Transaction UUID |
| checksum | VARCHAR(64) | NO | | SHA-256 integrity hash |
| event_type | VARCHAR(255) | NO | | Kafka event type |
| source_service | VARCHAR(255) | NO | | Producer microservice |
| payload | TEXT | NO | | Immutable raw payload |
| event_version | INT | NO | | Event schema version |
| occurred_at | DATETIME(6) | NO | MUL | Business event timestamp |
| recorded_at | DATETIME(6) | NO | MUL | Persistence timestamp |

---

# Why BINARY(16) Is Used

UUIDs stored as:

```text
BINARY(16)
```

instead of VARCHAR.

---

# Benefits

| Benefit | Purpose |
|---|---|
| Lower Storage Size | Reduced DB footprint |
| Faster Indexing | Better query performance |
| Better Cache Locality | Optimized lookup |

---

# Why Immutable Audit Persistence Matters

Financial systems require:

```text
Historical transaction traceability
```

without payload mutation.

---

# Audit Persistence Flow

```text
Kafka Event
      |
      v
Serialized Payload
      |
      v
Checksum Generation
      |
      v
transaction_event_records
```

---

# Checksum Purpose

```text
SHA-256 integrity validation
```

supports:

- tamper detection
- forensic auditing
- immutable verification

---

# Why event_version Exists

Supports:

```text
Event schema evolution
```

---

# Example

Future event versions:

```text
v1 -> v2 -> v3
```

can coexist safely.

---

# occurred_at vs recorded_at

| Column | Meaning |
|---|---|
| occurred_at | When business event happened |
| recorded_at | When event persisted |

---

# Why Both Timestamps Matter

Supports:

- event-lag analysis
- operational observability
- distributed tracing

---

# Indexing Strategy

| Index | Purpose |
|---|---|
| event_id | Duplicate prevention |
| transaction_id | Fast transaction lookup |
| occurred_at | Time-range queries |
| recorded_at | Export/reporting queries |

---

# Query Examples

# Transaction Lookup

```sql
SELECT * 
FROM transaction_event_records
WHERE transaction_id = ?;
```

---

# Historical Range Query

```sql
SELECT *
FROM transaction_event_records
WHERE occurred_at BETWEEN ? AND ?;
```

---

# ==========================================
# 2. processed_events
# ==========================================

# Purpose

Acts as:

```text
Kafka idempotency registry
```

Tracks already-processed Kafka events.

---

# Why This Exists

Kafka guarantees:

```text
At-least-once delivery
```

Meaning duplicates MAY happen.

This table enables:

```text
Exactly-once business persistence
```

---

# Table Structure

| Column | Type | Nullable | Key | Purpose |
|---|---|---|---|---|
| event_id | BINARY(16) | NO | PRI | Kafka event UUID |
| processed_at | DATETIME(6) | NO | MUL | Processing timestamp |

---

# Processing Flow

```text
Kafka Event
      |
      v
Check processed_events
      |
   +--+---+
   |      |
FOUND   NOT FOUND
   |      |
   v      v
SKIP   PROCESS
```

---

# Why event_id Is Primary Key

Guarantees:

```text
Single processing per event
```

---

# Why processed_at Exists

Supports:

- operational observability
- duplicate analysis
- event-processing metrics

---

# Example Query

```sql
SELECT EXISTS(
    SELECT 1
    FROM processed_events
    WHERE event_id = ?
);
```

---

# Benefits

| Benefit | Purpose |
|---|---|
| Duplicate Prevention | Idempotent Kafka handling |
| Replay Safety | Safe reprocessing |
| Operational Metrics | Processing visibility |

---

# ==========================================
# 3. failed_transaction_events
# ==========================================

# Purpose

Acts as:

```text
Failed Kafka-event recovery table
```

Stores events that failed processing.

---

# Why This Table Exists

Without failure persistence:

```text
Kafka processing failures may disappear silently
```

Very dangerous operationally.

---

# Table Structure

| Column | Type | Nullable | Key | Purpose |
|---|---|---|---|---|
| id | BINARY(16) | NO | PRI | Failed-event UUID |
| transaction_id | VARCHAR(255) | YES | MUL | Transaction identifier |
| source_topic | VARCHAR(255) | YES | | Kafka source topic |
| error_message | TEXT | YES | | Failure reason |
| payload | LONGTEXT | YES | | Raw failed payload |
| failed_at | DATETIME(6) | YES | MUL | Failure timestamp |

---

# Why LONGTEXT Used For payload

Failed payloads may contain:

- large Kafka messages
- serialized audit records
- nested event structures

---

# Failure Flow

```text
Kafka Processing Failure
        |
        v
FailedTransactionEvent
        |
        v
MySQL Persistence
```

---

# Why transaction_id Indexed

Supports:

```text
Fast operational debugging
```

---

# Example Failure Query

```sql
SELECT *
FROM failed_transaction_events
WHERE transaction_id = ?;
```

---

# Example Operational Query

```sql
SELECT *
FROM failed_transaction_events
WHERE failed_at > NOW() - INTERVAL 1 DAY;
```

---

# Why failed_at Indexed

Supports:

- incident investigations
- operational analytics
- failure dashboards

---

# Benefits

| Benefit | Purpose |
|---|---|
| Failure Recovery | Replay workflows |
| Operational Debugging | Root-cause analysis |
| Audit Safety | No silent event loss |

---

# ==========================================
# Entity Relationship Overview
# ==========================================

```text
TransactionFinalizedEvent
        |
        +------------------------------+
        |                              |
        v                              v

transaction_event_records     processed_events
(Audit Persistence)           (Idempotency)

        |
        v

failed_transaction_events
(Failure Recovery)
```

---

# ==========================================
# Persistence Flow
# ==========================================

# Successful Flow

```text
Kafka Event
      |
      v
Idempotency Validation
      |
      v
Persist transaction_event_records
      |
      v
Persist processed_events
```

---

# Failure Flow

```text
Kafka Processing Failure
        |
        v
Persist failed_transaction_events
```

---

# ==========================================
# Query Patterns
# ==========================================

# Main Query Types

| Query Type | Purpose |
|---|---|
| Transaction Lookup | Audit investigation |
| Historical Queries | Analytics |
| Failed Event Lookup | Operational debugging |
| Export Queries | Excel generation |

---

# Why Query Separation Matters

This database optimized for:

```text
Read-heavy observability workloads
```

separate from:

```text
Transactional write workloads
```

---

# ==========================================
# Database Optimization Strategies
# ==========================================

# 1. UUID Binary Storage

Uses:

```text
BINARY(16)
```

instead of VARCHAR UUIDs.

---

# 2. Indexed Timestamps

Supports:

- export performance
- historical queries
- analytics

---

# 3. Immutable Records

Prevents:

- accidental mutation
- audit corruption

---

# 4. Separate Failure Table

Improves:

- operational isolation
- debugging workflows

---

# ==========================================
# Operational Characteristics
# ==========================================

| Characteristic | Status |
|---|---|
| Immutable Auditability | YES |
| Idempotent Persistence | YES |
| Failure Recovery | YES |
| Historical Query Support | YES |
| Export Optimized | YES |

---

# Why This Database Design Is Enterprise-Grade

This persistence architecture demonstrates patterns used in:

- banking audit systems
- fintech observability platforms
- distributed event-processing systems
- compliance analytics systems

including:

- immutable audit storage
- Kafka idempotency registries
- failure-event persistence
- binary UUID optimization
- historical observability persistence

---

# Future Enhancements

- table partitioning
- archival strategies
- cold-storage offloading
- Elasticsearch indexing
- materialized analytics views
- replay-event pipelines

---

# Final Summary

The database architecture inside:

```text
transaction-observability-service
```

implements:

```text
Production-grade immutable audit-event persistence
```

through:

- historical transaction storage
- Kafka idempotency tracking
- failure-event recovery
- checksum-based integrity validation
- query-optimized observability persistence

The system is designed for:

```text
Enterprise-scale financial observability
and forensic audit workflows
```