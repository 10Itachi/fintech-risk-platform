# database-design.md

# Transaction Service — Database Design

# Overview

The `transaction-service` uses:

```text
MySQL
```

as its primary persistence layer for:

- transaction lifecycle management
- fraud-decision persistence
- Kafka outbox orchestration
- user management
- audit-safe financial processing

The database architecture is designed for:

```text
Financial-grade transactional consistency and distributed-system reliability
```

---

# Database Goals

| Goal | Purpose |
|---|---|
| Transaction Consistency | Reliable money movement |
| Fraud Traceability | Risk-decision persistence |
| Distributed Consistency | Kafka outbox reliability |
| Idempotency Support | Duplicate prevention |
| Historical Auditability | Transaction traceability |
| Operational Scalability | Distributed microservices |

---

# Database Technology

```text
MySQL
```

---

# Persistence Characteristics

| Characteristic | Status |
|---|---|
| ACID Transactions | YES |
| Distributed-Safe | YES |
| Idempotent | YES |
| Event-Driven | YES |
| Audit-Oriented | YES |

---

# High-Level Persistence Architecture

```text
Transaction Request
        |
        v
Transaction Persistence
        |
        +-------------------------------+
        |                               |
        v                               v

RiskDecision Persistence        Outbox Persistence

        |
        v
Kafka Event Publishing
```

---

# Core Tables

| Table | Purpose |
|---|---|
| transactions | Core transaction lifecycle |
| risk_decision | Fraud-decision persistence |
| risk_reason_codes | Fraud explainability |
| outbox_event | Transactional Kafka outbox |
| users | User management |

---

# ==========================================
# 1. transactions
# ==========================================

# Purpose

Acts as:

```text
Primary financial transaction table
```

Stores:

- transaction metadata
- lifecycle status
- fraud-decision status
- behavioral snapshots
- idempotency tracking

---

# Table Structure

| Column | Type | Nullable | Key | Purpose |
|---|---|---|---|---|
| transaction_id | VARCHAR(36) | NO | PRI | External transaction identifier |
| user_id | BINARY(16) | NO | | User UUID |
| amount | DECIMAL(38,2) | NO | | Transaction amount |
| country | VARCHAR(3) | NO | | ISO country code |
| device_id | VARCHAR(255) | NO | | Device identifier |
| email | VARCHAR(255) | NO | | User email |
| idempotency_key | VARCHAR(255) | YES | UNI | Retry-safe request key |
| source_account | VARCHAR(255) | NO | | Debit account |
| target_account | VARCHAR(255) | YES | | Credit account |
| user_name | VARCHAR(255) | NO | | User display name |
| total_amount_24h_snapshot | DECIMAL(38,2) | YES | | Behavioral snapshot |
| txn_count_24h_snapshot | INT | YES | | Behavioral snapshot |
| transaction_time | DATETIME(6) | NO | | Business transaction time |
| created_at | DATETIME(6) | NO | | Persistence timestamp |
| updated_at | DATETIME(6) | NO | | Last update timestamp |
| channel | ENUM | NO | | Transaction channel |
| internal_status | ENUM | NO | | Internal workflow state |
| transaction_status | ENUM | NO | | Final business decision |
| transaction_type | ENUM | NO | | Transaction category |

---

# Why transaction_id Is VARCHAR(36)

Stores:

```text
UUID string representation
```

for:

- external API compatibility
- cross-service interoperability
- easier debugging

---

# Why user_id Uses BINARY(16)

Benefits:

- reduced storage size
- optimized indexing
- faster joins

---

# Behavioral Snapshot Fields

| Field | Purpose |
|---|---|
| total_amount_24h_snapshot | Fraud analytics |
| txn_count_24h_snapshot | Behavioral scoring |

---

# Why Snapshots Are Stored

Captures:

```text
Fraud-evaluation context at transaction time
```

Even if future user activity changes.

---

# Internal Status vs Transaction Status

| Column | Purpose |
|---|---|
| internal_status | Workflow engine state |
| transaction_status | Final business outcome |

---

# Example Internal States

```text
INITIATED
PENDING_RISK
RISK_APPROVED
LEDGER_PENDING
LEDGER_SUCCESS
COMPLETED
FAILED
```

---

# Why Separate Internal State Exists

Supports:

- orchestration visibility
- workflow tracking
- distributed consistency

---

# Example Transaction Flow

```text
INITIATED
    |
    v
PENDING_RISK
    |
    v
RISK_APPROVED
    |
    v
LEDGER_SUCCESS
    |
    v
COMPLETED
```

---

# Idempotency Protection

Uses:

```text
idempotency_key UNIQUE constraint
```

---

# Why This Is Critical

Prevents:

- duplicate transactions
- retry amplification
- double processing

---

# Indexing Strategy

| Index | Purpose |
|---|---|
| transaction_id | Primary transaction lookup |
| idempotency_key | Retry-safe deduplication |
| created_at | Historical queries |
| user_id | User transaction history |

---

# Example Queries

# Transaction Lookup

```sql
SELECT *
FROM transactions
WHERE transaction_id = ?;
```

---

# User History

```sql
SELECT *
FROM transactions
WHERE user_id = ?
ORDER BY created_at DESC;
```

---

# ==========================================
# 2. risk_decision
# ==========================================

# Purpose

Acts as:

```text
Fraud-decision persistence table
```

Stores:

- ML scoring results
- fraud decisions
- latency metrics
- model metadata

---

# Table Structure

| Column | Type | Nullable | Key | Purpose |
|---|---|---|---|---|
| id | BINARY(16) | NO | PRI | Risk-decision UUID |
| transaction_id | BINARY(16) | NO | | Related transaction |
| fraud_probability | DOUBLE | YES | | ML fraud score |
| risk_score | INT | YES | | Aggregated risk score |
| latency_ms | BIGINT | YES | | Fraud-engine latency |
| model_name | VARCHAR(255) | YES | | ML model name |
| model_version | VARCHAR(255) | YES | | ML model version |
| policy_version | VARCHAR(255) | YES | | Fraud-policy version |
| evaluated_at | DATETIME(6) | NO | | Evaluation timestamp |
| decision | ENUM | NO | | Fraud decision |

---

# Why Model Metadata Stored

Supports:

- ML explainability
- audit traceability
- model rollback analysis

---

# Why latency_ms Matters

Measures:

```text
Fraud-service performance
```

---

# Fraud Decision Flow

```text
Transaction
      |
      v
Risk Evaluation
      |
      v
risk_decision Persistence
```

---

# Example Decisions

```text
APPROVED
DECLINED
REVIEW
```

---

# ==========================================
# 3. risk_reason_codes
# ==========================================

# Purpose

Provides:

```text
Fraud explainability persistence
```

---

# Table Structure

| Column | Type | Nullable | Key | Purpose |
|---|---|---|---|---|
| risk_decision_id | BINARY(16) | NO | MUL | Related risk decision |
| reason_code | VARCHAR(255) | YES | | Fraud explanation |

---

# Why Explainability Matters

Financial fraud systems require:

```text
Transparent fraud decisions
```

---

# Example Reason Codes

```text
HIGH_AMOUNT
NEW_DEVICE
HIGH_RISK_COUNTRY
VELOCITY_EXCEEDED
```

---

# Relationship Flow

```text
risk_decision
      |
      v
risk_reason_codes
```

---

# Why Separate Table Used

Supports:

- multiple reason codes
- normalized storage
- scalable explainability

---

# ==========================================
# 4. outbox_event
# ==========================================

# Purpose

Acts as:

```text
Transactional Kafka outbox table
```

This is one of the MOST IMPORTANT tables.

---

# Why Outbox Exists

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

# Table Structure

| Column | Type | Nullable | Key | Purpose |
|---|---|---|---|---|
| id | BINARY(16) | NO | PRI | Outbox-event UUID |
| aggregate_id | BINARY(16) | NO | MUL | Related aggregate UUID |
| aggregate_type | VARCHAR(100) | NO | | Aggregate category |
| event_type | VARCHAR(100) | NO | | Kafka event type |
| payload | LONGTEXT | NO | | Serialized Kafka payload |
| event_version | INT | NO | | Event schema version |
| retry_count | INT | NO | | Kafka retry attempts |
| created_at | DATETIME(6) | NO | MUL | Event creation timestamp |
| next_retry_at | DATETIME(6) | YES | | Scheduled retry timestamp |
| sent_at | DATETIME(6) | YES | | Kafka publish timestamp |
| status | ENUM | NO | MUL | Outbox lifecycle state |

---

# Outbox Status Lifecycle

```text
PENDING
   |
   v
SENT
```

Failure path:

```text
PENDING
   |
   v
FAILED
   |
   v
DEAD
```

---

# Why retry_count Exists

Tracks:

```text
Kafka retry attempts
```

---

# Why next_retry_at Exists

Supports:

```text
Exponential backoff scheduling
```

---

# Why LONGTEXT Used

Kafka payloads may contain:

- nested JSON
- event metadata
- audit payloads

---

# Outbox Flow

```text
Transaction Commit
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

# Why This Is Enterprise-Grade

Guarantees:

```text
No Kafka event loss after DB commit
```

---

# Indexing Strategy

| Index | Purpose |
|---|---|
| status | Pending-event polling |
| aggregate_id | Aggregate lookup |
| created_at | Scheduler ordering |

---

# Example Query

```sql
SELECT *
FROM outbox_event
WHERE status = 'PENDING'
ORDER BY created_at
LIMIT 10;
```

---

# ==========================================
# 5. users
# ==========================================

# Purpose

Acts as:

```text
User authentication and identity table
```

---

# Table Structure

| Column | Type | Nullable | Key | Purpose |
|---|---|---|---|---|
| user_id | BIGINT | NO | PRI | User identifier |
| user_name | VARCHAR(255) | NO | | Username |
| email | VARCHAR(255) | NO | | User email |
| password_hash | VARCHAR(255) | NO | | Encrypted password |
| phone_number | VARCHAR(255) | NO | | Contact number |
| created_at | DATETIME(6) | NO | | User creation timestamp |
| is_active | TINYINT | NO | | User active flag |
| role | ENUM | NO | | Authorization role |

---

# Why password_hash Stored

Passwords NEVER stored in plaintext.

Only:

```text
Secure hashed passwords
```

stored.

---

# Example Roles

```text
ADMIN
USER
```

---

# Why role Exists

Supports:

```text
Role-Based Access Control (RBAC)
```

---

# ==========================================
# Entity Relationship Overview
# ==========================================

```text
users
   |
   v
transactions
   |
   +-----------------------------+
   |                             |
   v                             v

risk_decision              outbox_event
   |
   v
risk_reason_codes
```

---

# ==========================================
# Transaction Lifecycle Persistence Flow
# ==========================================

```text
Create Transaction
        |
        v
Persist transactions
        |
        v
Fraud Evaluation
        |
        v
Persist risk_decision
        |
        v
Persist risk_reason_codes
        |
        v
Persist outbox_event
        |
        v
Kafka Publish
```

---

# ==========================================
# Distributed-System Design Characteristics
# ==========================================

| Characteristic | Status |
|---|---|
| Transactional Consistency | YES |
| Kafka Consistency | YES |
| Fraud Explainability | YES |
| Retry-Safe | YES |
| Event-Driven | YES |
| Audit-Oriented | YES |

---

# Why This Database Design Is Enterprise-Grade

This persistence architecture demonstrates patterns used in:

- banking systems
- payment gateways
- fintech fraud platforms
- distributed transaction systems

including:

- transactional outbox
- fraud explainability persistence
- distributed consistency
- idempotent transaction design
- audit-safe workflows

---

# Future Enhancements

- table partitioning
- archival storage
- event sourcing
- materialized analytics views
- cold-storage offloading
- ledger reconciliation tables

---

# Final Summary

The database architecture inside:

```text
transaction-service
```

implements:

```text
Production-grade distributed financial persistence architecture
```

through:

- transaction lifecycle persistence
- fraud-decision traceability
- Kafka outbox consistency
- idempotent transaction protection
- operational auditability
- distributed-system reliability

The system is designed for:

```text
Enterprise-scale financial transaction orchestration
```