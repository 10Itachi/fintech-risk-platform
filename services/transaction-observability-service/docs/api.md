# api.md

# Transaction Observability Service API Specification

# Overview

The `transaction-observability-service` exposes APIs for:

- historical transaction observability
- immutable audit retrieval
- failed Kafka-event investigation
- Excel export generation
- operational analytics

Base URL:

```text
http://localhost:8084
```

---

# API Characteristics

| Characteristic | Status |
|---|---|
| RESTful APIs | YES |
| JWT Secured | YES |
| Paginated APIs | YES |
| Export APIs | YES |
| Read-Optimized | YES |
| Audit-Focused | YES |

---

# High-Level API Architecture

```text
Client
   |
   v
TransactionObservabilityController
   |
   v
TransactionObservabilityQueryService
   |
   v
MySQL Audit Database
```

---

# Authentication

The APIs are protected using:

```text
OAuth2 Resource Server + JWT Authentication
```

---

# Required Header

```http
Authorization: Bearer <JWT_TOKEN>
```

---

# Content Type

```http
Content-Type: application/json
```

---

# ==========================================
# 1. Get Transactions
# ==========================================

# Endpoint

```http
GET /api/v1/observability/transactions
```

---

# Purpose

Returns:

```text
Paginated historical transaction audit records
```

---

# Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| from | LocalDateTime | NO | Start timestamp filter |
| to | LocalDateTime | NO | End timestamp filter |
| page | Integer | YES | Page number |
| size | Integer | YES | Page size |
| sort | String[] | NO | Sorting fields |

---

# Example Request

```http
GET /api/v1/observability/transactions?from=2026-05-01T00:00:00&to=2026-05-18T23:59:59&page=0&size=20
```

---

# Request Flow

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
TransactionEventRecordRepository
      |
      v
MySQL
```

---

# Response

```json
{
  "totalElements": 120,
  "totalPages": 6,
  "first": true,
  "last": false,
  "size": 20,
  "number": 0,
  "content": [
    {
      "id": 1,
      "eventId": "11111111-1111-1111-1111-111111111111",
      "transactionId": "22222222-2222-2222-2222-222222222222",
      "eventType": "TRANSACTION_FINALIZED",
      "eventVersion": 1,
      "payload": "{...}",
      "checksum": "2a6f71baf8e23a9d",
      "occurredAt": "2026-05-18T10:30:00",
      "recordedAt": "2026-05-18T10:30:02",
      "sourceService": "transaction-service"
    }
  ]
}
```

---

# Response Fields

| Field | Description |
|---|---|
| eventId | Kafka event identifier |
| transactionId | Transaction identifier |
| payload | Immutable raw payload |
| checksum | SHA-256 payload checksum |
| occurredAt | Event occurrence timestamp |
| recordedAt | Persistence timestamp |
| sourceService | Event producer service |

---

# Why This API Exists

Supports:

- audit investigation
- operational analytics
- historical transaction tracing
- compliance workflows

---

# ==========================================
# 2. Get Transaction By ID
# ==========================================

# Endpoint

```http
GET /api/v1/observability/transactions/{txnId}
```

---

# Purpose

Returns:

```text
Single transaction audit record
```

---

# Path Variable

| Parameter | Type | Required | Description |
|---|---|---|---|
| txnId | UUID | YES | Transaction identifier |

---

# Example Request

```http
GET /api/v1/observability/transactions/22222222-2222-2222-2222-222222222222
```

---

# Request Flow

```text
Controller
    |
    v
Query Service
    |
    v
Repository
    |
    v
MySQL Lookup
```

---

# Example Response

```json
{
  "id": 1,
  "eventId": "11111111-1111-1111-1111-111111111111",
  "transactionId": "22222222-2222-2222-2222-222222222222",
  "eventType": "TRANSACTION_FINALIZED",
  "eventVersion": 1,
  "payload": "{...}",
  "checksum": "2a6f71baf8e23a9d",
  "occurredAt": "2026-05-18T10:30:00",
  "recordedAt": "2026-05-18T10:30:02",
  "sourceService": "transaction-service"
}
```

---

# Why This API Exists

Used for:

- single-transaction investigation
- audit lookup
- forensic validation
- payload verification

---

# ==========================================
# 3. Export Transactions
# ==========================================

# Endpoint

```http
GET /api/v1/observability/transactions/export
```

---

# Purpose

Generates:

```text
Excel transaction report
```

---

# Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| from | LocalDateTime | YES | Export start timestamp |
| to | LocalDateTime | YES | Export end timestamp |

---

# Example Request

```http
GET /api/v1/observability/transactions/export?from=2026-05-01T00:00:00&to=2026-05-18T23:59:59
```

---

# Export Flow

```text
HTTP Request
      |
      v
Controller
      |
      v
Query Service
      |
      v
ExcelServiceImpl
      |
      v
SXSSFWorkbook
      |
      v
Excel File Response
```

---

# Response Type

```http
application/octet-stream
```

---

# Response

```text
Binary Excel File (.xlsx)
```

---

# Why Streaming Workbook Is Used

Uses:

```java
SXSSFWorkbook
```

to prevent:

```text
Heap-memory spikes
```

during large exports.

---

# Export Features

| Feature | Purpose |
|---|---|
| Streaming Workbook | Memory-safe export |
| Large Dataset Support | Scalable reporting |
| Immutable Audit Export | Compliance workflows |

---

# ==========================================
# 4. Get Failed Events
# ==========================================

# Endpoint

```http
GET /api/v1/observability/failed-events
```

---

# Purpose

Returns:

```text
Paginated failed Kafka-processing events
```

---

# Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| from | LocalDateTime | NO | Start timestamp filter |
| to | LocalDateTime | NO | End timestamp filter |
| page | Integer | YES | Page number |
| size | Integer | YES | Page size |

---

# Example Request

```http
GET /api/v1/observability/failed-events?page=0&size=20
```

---

# Request Flow

```text
Controller
    |
    v
Query Service
    |
    v
FailedEventRepository
    |
    v
MySQL
```

---

# Example Response

```json
{
  "totalElements": 12,
  "totalPages": 1,
  "content": [
    {
      "id": "33333333-3333-3333-3333-333333333333",
      "transactionId": "22222222-2222-2222-2222-222222222222",
      "sourceTopic": "transaction.finalized.v1",
      "errorMessage": "Database timeout",
      "payload": "{...}",
      "failedAt": "2026-05-18T11:10:00"
    }
  ]
}
```

---

# Why This API Exists

Supports:

- operational debugging
- Kafka failure investigation
- replay workflows
- forensic recovery

---

# ==========================================
# 5. Get Failed Event By ID
# ==========================================

# Endpoint

```http
GET /api/v1/observability/failed-events/{id}
```

---

# Purpose

Returns:

```text
Single failed Kafka-processing event
```

---

# Path Variable

| Parameter | Type | Required | Description |
|---|---|---|---|
| id | UUID | YES | Failed-event identifier |

---

# Example Request

```http
GET /api/v1/observability/failed-events/33333333-3333-3333-3333-333333333333
```

---

# Example Response

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "transactionId": "22222222-2222-2222-2222-222222222222",
  "sourceTopic": "transaction.finalized.v1",
  "errorMessage": "Database timeout",
  "payload": "{...}",
  "failedAt": "2026-05-18T11:10:00"
}
```

---

# Why This API Exists

Used for:

- root-cause analysis
- failure debugging
- payload inspection
- operational recovery

---

# ==========================================
# Pagination Model
# ==========================================

# Pageable Request Structure

```json
{
  "page": 0,
  "size": 20,
  "sort": [
    "recordedAt,desc"
  ]
}
```

---

# Paginated Response Structure

| Field | Purpose |
|---|---|
| totalElements | Total records |
| totalPages | Total pages |
| first | First-page indicator |
| last | Last-page indicator |
| size | Page size |
| number | Current page number |
| content | Actual records |

---

# Why Pagination Matters

Protects against:

- massive memory usage
- oversized responses
- DB overload

---

# ==========================================
# Common HTTP Status Codes
# ==========================================

| Status Code | Meaning |
|---|---|
| 200 | Success |
| 400 | Invalid request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Record not found |
| 500 | Internal server error |

---

# ==========================================
# Security Model
# ==========================================

# Authentication Flow

```text
Client Request
      |
      v
JWT Token
      |
      v
Spring Security
      |
      v
Authorization Validation
      |
      v
Controller Access
```

---

# Authorization Characteristics

| Characteristic | Status |
|---|---|
| JWT-Based | YES |
| Stateless | YES |
| OAuth2 Resource Server | YES |
| Role-Based Access | YES |

---

# ==========================================
# Operational Characteristics
# ==========================================

| Characteristic | Status |
|---|---|
| Read-Optimized APIs | YES |
| Immutable Audit APIs | YES |
| Streaming Export APIs | YES |
| Paginated Queries | YES |
| Secure APIs | YES |
| Distributed Ready | YES |

---

# Why This API Design Is Enterprise-Grade

This API architecture demonstrates patterns used in:

- banking audit platforms
- fintech observability systems
- compliance-reporting platforms
- distributed analytics systems

including:

- paginated APIs
- immutable audit retrieval
- export streaming
- operational failure querying
- secure observability endpoints

---

# Future Enhancements

- filtering by transaction status
- advanced search APIs
- replay APIs
- async export jobs
- signed export URLs
- OpenTelemetry tracing
- GraphQL analytics APIs

---

# Final Summary

The `transaction-observability-service` APIs provide:

```text
Production-grade audit and analytics endpoints
```

supporting:

- immutable transaction retrieval
- Kafka failure investigation
- scalable Excel exports
- historical audit querying
- operational observability
- forensic transaction analysis

The APIs are designed for:

```text
Enterprise-scale financial observability workflows
```