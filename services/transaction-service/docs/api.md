# api.md

# Transaction Service API Specification

# Overview

The `transaction-service` exposes APIs for:

- transaction creation
- transaction retrieval
- fraud-decision visibility
- manual review workflows
- user transaction history

Base URL:

```text
http://localhost:8082
```

---

# API Characteristics

| Characteristic | Status |
|---|---|
| REST APIs | YES |
| JWT Secured | YES |
| Idempotent Transaction Creation | YES |
| Pagination Support | YES |
| Fraud-Aware | YES |
| Distributed-System Ready | YES |

---

# Authentication

All APIs are protected using:

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

# High-Level API Architecture

```text
Client
   |
   v
TransactionController
   |
   v
TransactionOrchestratorService
   |
   +-------------------------------+
   |                               |
   v                               v

Risk Decision Service        Kafka Outbox
```

---

# ==========================================
# 1. Create Transaction
# ==========================================

# Endpoint

```http
POST /api/v1/transactions/user/create
```

---

# Purpose

Creates:

```text
New financial transaction
```

with:

- fraud evaluation
- orchestration handling
- idempotency protection
- distributed persistence

---

# Headers

| Header | Required  | Description |
|---|-----------|---|
| Authorization | YES       | JWT token |
| Idempotency-Key | NOT Mand. | Retry-safe transaction key |

---

# Request Body

```json
{
  "amount": 5,
  "transactionType": "TRANSFER",
  "sourceAccount": "ACC-EFD-0818171016",
  "targetAccount": "ACC-KH-82533676",
  "channel": "UPI",
  "country": "IN",
  "deviceId": "DEV-IOS-36GVMG9POBGV1Q4DX",
  "transactionTime": "2026-05-26T11:44:07.226Z"
}
```

---

# Request Fields

| Field | Type | Description |
|---|---|---|
| userId | UUID | Transaction owner |
| userName | String | User display name |
| email | String | User email |
| deviceId | String | Device identifier |
| sourceAccount | String | Debit account |
| targetAccount | String | Credit account |
| amount | Decimal | Transaction amount |
| country | String | ISO country code |
| channel | ENUM | Transaction channel |
| transactionType | ENUM | Transaction category |

---

# Request Flow

```text
HTTP Request
      |
      v
TransactionController
      |
      v
TransactionOrchestratorService
      |
      +-------------------------------+
      |                               |
      v                               v

Risk Evaluation               Transaction Persistence
      |
      v
Outbox Event Persistence
      |
      v
Kafka Publish
```

---

# Response

```json
{
  "transactionId": "22222222-2222-2222-2222-222222222222",
  "status": "APPROVED",
  "internalStatus": "COMPLETED",
  "message": "Transaction processed successfully"
}
```

---

# Response Fields

| Field | Description |
|---|---|
| transactionId | Unique transaction identifier |
| status | Final business status |
| internalStatus | Internal workflow status |
| message | Processing response |

---

# Idempotency Protection

Uses:

```text
Idempotency-Key
```

to prevent:

- duplicate transactions
- retry amplification
- network retry duplication

---

# Why This API Is Important

Acts as:

```text
Primary financial transaction orchestration API
```

---

# ==========================================
# 2. Get Transaction By ID
# ==========================================

# Endpoint

```http
GET /api/v1/transactions/admin/riskdecision/:transactionId
```

---

# Purpose

Returns:

```text
Single transaction details
```

---

# Path Variable

| Parameter | Type | Required |
|---|---|---|
| transactionId | UUID | YES |

---

# Example Request

```http
GET /transactions/22222222-2222-2222-2222-222222222222
```

---

# Flow

```text
Controller
    |
    v
TransactionQueryService
    |
    v
MySQL Lookup
```

---

# Example Response

```json
{
  "transactionId": "22222222-2222-2222-2222-222222222222",
  "amount": 25000,
  "status": "APPROVED",
  "channel": "UPI",
  "transactionType": "TRANSFER"
}
```

---

# Why This API Exists

Supports:

- transaction lookup
- operational debugging
- audit workflows

---

# ==========================================
# 3. Get My Transactions
# ==========================================

# Endpoint

```http
GET /api/v1/transactions/shared/:transactionId
```

---

# Purpose

Returns:

```text
Authenticated user's transaction history
```

---

# Query Parameters

| Parameter | Type | Default |
|---|---|---|
| page | Integer | 0 |
| size | Integer | 10 |
| sort | String | createdAt,desc |

---

# Example Request

```http
GET /transactions/my?page=0&size=10
```

---

# Flow

```text
JWT User
    |
    v
Controller
    |
    v
User Transaction Query
    |
    v
Paginated Response
```

---

# Response

```json
{
  "content": [
    {
      "transactionId": "22222222-2222-2222-2222-222222222222",
      "amount": 25000,
      "status": "APPROVED"
    }
  ],
  "totalElements": 20,
  "totalPages": 2
}
```

---

# Why Pagination Exists

Protects against:

- large responses
- memory spikes
- DB overload

---

# ==========================================
# 4. Get Transactions By User ID
# ==========================================

# Endpoint

```http
GET /api/v1/transactions/admin/users/:userId?page=0&size=10&sort=createdAt,desc
```

---

# Purpose

Returns:

```text
Transaction history for a specific user
```

---

# Path Variable

| Parameter | Type |
|---|---|
| userId | UUID |

---

# Query Parameters

| Parameter | Type | Default |
|---|---|---|
| page | Integer | 0 |
| size | Integer | 10 |
| sort | String | createdAt,desc |

---

# Example Request

```http
GET /transactions/users/11111111-1111-1111-1111-111111111111
```

---

# Why This API Exists

Supports:

- admin analytics
- fraud investigations
- operational queries

---

# ==========================================
# 5. Get All Transactions
# ==========================================

# Endpoint

```http
GET /api/v1/transactions/admin/allUsers?page=0&size=10&sort=createdAt,desc
```

---

# Purpose

Returns:

```text
Paginated system-wide transaction history
```

---

# Query Parameters

| Parameter | Type | Default |
|---|---|---|
| page | Integer | 0 |
| size | Integer | 10 |
| sort | String | createdAt,desc |

---

# Example Request

```http
GET /api/v1/transactions/admin/allUsers?page=0&size=10&sort=createdAt,desc
```

---

# Why This API Exists

Used for:

- operational dashboards
- analytics
- admin monitoring

---

# ==========================================
# 6. Get Risk Decision By Transaction ID
# ==========================================

# Endpoint

```http
GET /api/v1/transactions/admin/riskdecision/:transactionId
```

---

# Purpose

Returns:

```text
Fraud-decision details for a transaction
```

---

# Path Variable

| Parameter | Type |
|---|---|
| transactionId | UUID |

---

# Example Request

```http
GET /transactions/riskdecision/22222222-2222-2222-2222-222222222222
```

---

# Response

```json
{
  "decision": "REVIEW",
  "riskScore": 85,
  "fraudProbability": 0.92,
  "modelName": "fraud-model-v2"
}
```

---

# Why This API Exists

Supports:

- fraud explainability
- operational review
- audit traceability

---

# ==========================================
# 7. Approve Review Transaction
# ==========================================

# Endpoint

```http
POST /api/v1/transactions/admin/:transactionId/review/approve
```

---

# Purpose

Manually approves:

```text
REVIEW state transaction
```

---

# Path Variable

| Parameter | Type |
|---|---|
| transactionId | UUID |

---

# Flow

```text
Manual Review Request
        |
        v
Review Service
        |
        v
Transaction Update
        |
        v
Kafka Event Publish
```

---

# Response

```json
{
  "transactionId": "22222222-2222-2222-2222-222222222222",
  "status": "APPROVED"
}
```

---

# Why Manual Review Exists

High-risk transactions may require:

```text
Human approval workflows
```

---

# ==========================================
# 8. Reject Review Transaction
# ==========================================

# Endpoint

```http
POST /api/v1/transactions/admin/:transactionId/review/reject
```

---

# Purpose

Manually rejects:

```text
REVIEW state transaction
```

---

# Path Variable

| Parameter | Type |
|---|---|
| transactionId | UUID |

---

# Example Response

```json
{
  "transactionId": "22222222-2222-2222-2222-222222222222",
  "status": "DECLINED"
}
```

---

# Why This API Exists

Supports:

- fraud operations
- manual fraud intervention
- operational controls

---

# ==========================================
# Transaction Status Model
# ==========================================

# Final Business Status

| Status | Meaning |
|---|---|
| INITIATED | Transaction created |
| APPROVED | Successfully approved |
| DECLINED | Fraud rejected |
| REVIEW | Requires manual review |

---

# Internal Workflow Status

| Status | Meaning |
|---|---|
| INITIATED | Request accepted |
| PENDING_RISK | Fraud evaluation pending |
| RISK_APPROVED | Fraud approved |
| RISK_REJECTED | Fraud rejected |
| LEDGER_PENDING | Ledger processing pending |
| LEDGER_SUCCESS | Ledger success |
| COMPLETED | Fully completed |
| FAILED | Workflow failure |

---

# ==========================================
# Common HTTP Status Codes
# ==========================================

| Status Code | Meaning |
|---|---|
| 200 | Success |
| 201 | Transaction created |
| 400 | Invalid request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Transaction not found |
| 409 | Duplicate idempotency request |
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
JWT Validation
      |
      v
Role Extraction
      |
      v
Controller Authorization
```

---

# Security Characteristics

| Characteristic | Status |
|---|---|
| JWT-Based | YES |
| Stateless | YES |
| OAuth2 Resource Server | YES |
| Role-Based Authorization | YES |

---

# ==========================================
# Distributed-System Integration
# ==========================================

# External Integrations

| Integration | Purpose |
|---|---|
| Risk Decision Service | Fraud evaluation |
| Kafka | Async event publishing |
| Transaction Observability Service | Audit persistence |

---

# Event Flow

```text
Transaction Created
        |
        v
Outbox Event
        |
        v
Kafka Publish
        |
        v
Observability Service
```

---

# Why This API Design Is Enterprise-Grade

This API architecture demonstrates patterns used in:

- banking systems
- payment gateways
- fintech fraud platforms
- distributed transaction systems

including:

- idempotent transaction APIs
- fraud-aware orchestration
- distributed consistency
- manual-review workflows
- event-driven architecture

---

# Future Enhancements

- async transaction APIs
- replay APIs
- advanced filtering
- bulk transaction exports
- OpenTelemetry tracing
- GraphQL analytics APIs

---

# Final Summary

The `transaction-service` APIs provide:

```text
Production-grade distributed financial transaction orchestration
```

supporting:

- secure transaction processing
- fraud-decision integration
- idempotent request handling
- manual review workflows
- distributed Kafka publishing
- historical transaction retrieval

The APIs are designed for:

```text
Enterprise-scale financial transaction platforms
```