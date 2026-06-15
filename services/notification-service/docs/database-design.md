# Notification Service Database Design

## Overview

This document describes the database schema design for the Notification Service.

The system is designed to support:

- Failed notification event persistence
- Failed transaction event persistence
- Retry workflows
- Event auditing
- Processed notification tracking
- Kafka/event-driven failure recovery

---

# Database Tables

## 1. failed_notification_events

Stores notification events that failed during processing or publishing.

---

### Table Structure

| Column Name | Data Type | Nullable | Key | Description |
|---|---|---|---|---|
| id | binary(16) | NO | PRIMARY KEY | Unique event identifier (UUID stored in binary format) |
| transaction_id | varchar(100) | YES | INDEXED | Business transaction identifier |
| source_topic | varchar(150) | NO |  | Kafka/source topic name |
| error_message | longtext | YES |  | Detailed failure message |
| payload | longtext | YES |  | Original event payload |
| failed_at | datetime(6) | NO | INDEXED | Failure timestamp |

---

### Purpose

This table acts as a Dead Letter Queue (DLQ) persistence layer for failed notification events.

Typical use cases:

- Retry failed notifications
- Debug production failures
- Audit event delivery failures
- Manual replay support

---

### Recommended Index Strategy

| Index | Columns | Purpose |
|---|---|---|
| PRIMARY KEY | id | Unique row lookup |
| IDX_FAILED_NOTIFICATION_TRANSACTION | transaction_id | Search by transaction |
| IDX_FAILED_NOTIFICATION_FAILED_AT | failed_at | Time-based filtering |
| IDX_FAILED_NOTIFICATION_TOPIC | source_topic | Topic-level analysis |

---

### Example Record

```json
{
  "id": "e3f7d7b7-8f0d-4f4c-9d8d-8e7a9f2a1234",
  "transaction_id": "TXN-10001",
  "source_topic": "notification-events",
  "error_message": "Kafka publish timeout",
  "payload": "{...}",
  "failed_at": "2026-05-18T10:30:00"
}
```

---

# 2. failed_transaction_events

Stores failed transaction-related events.

---

### Table Structure

| Column Name | Data Type | Nullable | Key | Description |
|---|---|---|---|---|
| id | binary(16) | NO | PRIMARY KEY | Unique failed transaction event ID |
| source_topic | varchar(255) | YES |  | Kafka/source topic |
| transaction_id | varchar(255) | YES |  | Business transaction identifier |
| error_message | text | YES |  | Failure reason |
| payload | longtext | YES |  | Original transaction payload |
| failed_at | datetime(6) | YES |  | Failure timestamp |

---

### Purpose

Acts as a persistence layer for transaction processing failures.

Typical use cases:

- Transaction replay
- Financial event reconciliation
- Failure diagnostics
- Async processing recovery

---

### Recommended Index Strategy

| Index | Columns | Purpose |
|---|---|---|
| PRIMARY KEY | id | Unique lookup |
| IDX_FAILED_TRANSACTION_TRANSACTION | transaction_id | Transaction search |
| IDX_FAILED_TRANSACTION_FAILED_AT | failed_at | Failure time queries |
| IDX_FAILED_TRANSACTION_TOPIC | source_topic | Topic filtering |

---

### Example Record

```json
{
  "id": "2af6d9b4-2f73-42fd-a5c2-6dbbc7f7a111",
  "source_topic": "transaction-events",
  "transaction_id": "TXN-20001",
  "error_message": "Database deadlock detected",
  "payload": "{...}",
  "failed_at": "2026-05-18T11:00:00"
}
```

---

# 3. notification_processed

Tracks successfully processed notification events.

---

### Table Structure

| Column Name | Data Type | Nullable | Key | Description |
|---|---|---|---|---|
| event_id | binary(16) | NO | PRIMARY KEY | Processed event UUID |
| processed_at | datetime(6) | NO | INDEXED | Processing completion timestamp |

---

### Purpose

Used for idempotency and processed-event tracking.

Typical use cases:

- Prevent duplicate processing
- Event deduplication
- Audit successful processing
- Kafka exactly-once semantics support

---

### Recommended Index Strategy

| Index | Columns | Purpose |
|---|---|---|
| PRIMARY KEY | event_id | Idempotency lookup |
| IDX_NOTIFICATION_PROCESSED_AT | processed_at | Time filtering |

---

### Example Record

```json
{
  "event_id": "d7f5e1f0-c0a9-4d8f-b0a8-4dcf4a112233",
  "processed_at": "2026-05-18T12:15:00"
}
```

---

# Schema Design Decisions

## Why `binary(16)` for UUID?

UUIDs are stored using `binary(16)` instead of `varchar(36)` for:

- Reduced storage size
- Faster indexing
- Better query performance
- Lower memory footprint

### Benefits

| Type | Storage |
|---|---|
| varchar(36) UUID | 36 bytes |
| binary(16) UUID | 16 bytes |

This optimization is important for:

- Kafka event systems
- High-throughput event ingestion
- Large-scale retry/event tables

---

# Architectural Role of Tables

---

## Event Failure Flow

```text
Kafka/Event Source
        ↓
Notification Consumer
        ↓
Processing Failure
        ↓
failed_notification_events
        ↓
Retry Mechanism
        ↓
Success
        ↓
notification_processed
```

---

## Transaction Failure Flow

```text
Transaction Event
        ↓
Transaction Processor
        ↓
Failure
        ↓
failed_transaction_events
        ↓
Replay / Retry
```

---

# Data Lifecycle Recommendations

## Retention Policy

| Table | Suggested Retention |
|---|---|
| failed_notification_events | 30–90 days |
| failed_transaction_events | 60–180 days |
| notification_processed | 30–365 days |

---

## Archival Strategy

Recommended approaches:

- Move old records to archive tables
- Export historical failures to S3/object storage
- Partition tables by month
- Scheduled cleanup jobs

---

# Recommended Production Enhancements

## 1. Add Retry Metadata

Suggested columns:

```sql
retry_count INT DEFAULT 0,
last_retry_at DATETIME(6),
retry_status VARCHAR(50)
```

---

## 2. Add Processing Status

```sql
status VARCHAR(50)
```

Possible values:

- FAILED
- RETRYING
- PROCESSED
- DEAD_LETTERED

---

## 3. Add Correlation IDs

```sql
correlation_id VARCHAR(255)
```

Useful for:

- Distributed tracing
- Microservice debugging
- Observability

---

## 4. Add Tenant Support (Multi-Tenant Systems)

```sql
tenant_id VARCHAR(100)
```

---

## 5. Add Soft Delete Support

```sql
is_deleted BOOLEAN DEFAULT FALSE
```

---

# Recommended DDL (Optimized)

## failed_notification_events

```sql
CREATE TABLE failed_notification_events (
    id BINARY(16) PRIMARY KEY,
    transaction_id VARCHAR(100),
    source_topic VARCHAR(150) NOT NULL,
    error_message LONGTEXT,
    payload LONGTEXT,
    failed_at DATETIME(6) NOT NULL,

    INDEX idx_transaction_id (transaction_id),
    INDEX idx_failed_at (failed_at),
    INDEX idx_source_topic (source_topic)
);
```

---

## failed_transaction_events

```sql
CREATE TABLE failed_transaction_events (
    id BINARY(16) PRIMARY KEY,
    source_topic VARCHAR(255),
    transaction_id VARCHAR(255),
    error_message TEXT,
    payload LONGTEXT,
    failed_at DATETIME(6),

    INDEX idx_transaction_id (transaction_id),
    INDEX idx_failed_at (failed_at),
    INDEX idx_source_topic (source_topic)
);
```

---

## notification_processed

```sql
CREATE TABLE notification_processed (
    event_id BINARY(16) PRIMARY KEY,
    processed_at DATETIME(6) NOT NULL,

    INDEX idx_processed_at (processed_at)
);
```

---

# Performance Considerations

## Potential Bottlenecks

### Large Payload Storage

`LONGTEXT` payloads can increase:

- Disk usage
- Backup size
- Query latency

### Recommendations

- Compress payloads
- Store payload externally (S3/Blob Storage)
- Store only metadata in DB

---

## High Throughput Event Systems

Recommended:

- Batch inserts
- Table partitioning
- Async cleanup jobs
- Kafka DLQ integration
- Read/write separation

---

# Security Considerations

## Sensitive Payload Data

Payloads may contain:

- PII
- Transaction details
- Internal metadata

Recommended:

- Encrypt sensitive payloads
- Mask confidential data
- Apply RBAC
- Enable audit logging

---

# Future Scalability Enhancements

## Recommended Next Steps

### Horizontal Scaling

- Sharded event tables
- Distributed retry workers

### Observability

- Prometheus metrics
- Retry dashboards
- DLQ monitoring

### Reliability

- Exponential backoff retry
- Retry queues
- Poison message handling

---

# Summary

This schema is designed for an event-driven microservices architecture focused on:

- Failure recovery
- Retry orchestration
- Retry orchestration
- Event tracking
- Idempotent processing
- Kafka-driven workflows

The current design is clean and production-aligned for small-to-medium scale systems and can be evolved further for enterprise-grade distributed systems.
