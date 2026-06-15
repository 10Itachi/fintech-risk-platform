# API_REFERENCE.md

# Gringotts Fraud Platform — API Reference

## Overview

This document provides a high-level API reference for the Gringotts distributed fraud detection and transaction processing platform.

The platform exposes secured REST APIs through a centralized API Gateway and follows:

- stateless JWT authentication
- OAuth2 Resource Server security
- role-based authorization
- machine-to-machine trust
- distributed microservice orchestration

---

# API Gateway

All external client traffic enters through:

```text
Gateway Service
```

The gateway provides:

- centralized authentication
- authorization
- request routing
- resilience handling
- rate limiting
- observability propagation

---

# Authentication

The platform uses:

```text
OAuth2 + JWT + Keycloak
```

Authorization header format:

```http
Authorization: Bearer <access_token>
```

---

# Security Model

| Role                    | Responsibility                   |
| ----------------------- | -------------------------------- |
| ROLE_ADMIN              | Administrative operations        |
| ROLE_USER               | Standard user operations         |
| Internal Service Tokens | Machine-to-machine communication |

---

# Service API Overview

| Service                           | Base Path               | Responsibility               |
| --------------------------------- | ----------------------- | ---------------------------- |
| user-service                      | `/api/v1/users`         | User lifecycle management    |
| transaction-service               | `/api/v1/transactions`  | Transaction orchestration    |
| risk-decision-service             | `/risk/api/v1`          | Fraud evaluation             |
| notification-service              | `/api/v1/notifications` | Notification operations      |
| transaction-observability-service | `/api/v1/observability` | Audit & analytics            |
| gateway-service                   | `/`                     | Centralized routing/security |

---

# 1. User Service APIs

## Overview

The User Service manages:

- user lifecycle operations
- profile retrieval
- activation/deactivation
- Keycloak synchronization
- ownership-based access control

---

## Base Path

```text
/api/v1/users
```

---

## Core APIs

### Create User

```http
POST /api/v1/users
```

Authorization:

```text
ROLE_ADMIN
```

Purpose:

- create user in Keycloak
- persist local user record
- assign roles

---

### Get Current User Profile

```http
GET /api/v1/users/profile
```

Authorization:

```text
Authenticated User
```

Purpose:

- fetch currently authenticated user profile

---

### Get User By ID

```http
GET /api/v1/users/{userId}
```

Authorization:

```text
ROLE_ADMIN or Resource Owner
```

Purpose:

- retrieve specific user details

---

### Get All Users

```http
GET /api/v1/users
```

Supports:

- cursor pagination
- size-based pagination

Authorization:

```text
ROLE_ADMIN
```

---

### Deactivate User

```http
PATCH /api/v1/users/{userId}/deactivate
```

Authorization:

```text
ROLE_ADMIN
```

Purpose:

- disable user locally
- disable Keycloak account

---

### Activate User

```http
PATCH /api/v1/users/{userId}/activate
```

Authorization:

```text
ROLE_ADMIN
```

Purpose:

- reactivate previously disabled user

---

# 2. Transaction Service APIs

## Overview

The Transaction Service acts as the distributed orchestration engine of the platform.

Responsibilities include:

- transaction lifecycle orchestration
- idempotent transaction handling
- fraud-service integration
- Kafka event publishing

---

## Base Path

```text
/api/v1/transactions
```

---

## Core APIs

### Create Transaction

```http
POST /api/v1/transactions
```

Purpose:

- initiate transaction processing
- trigger fraud evaluation
- orchestrate transaction lifecycle

Features:

- Redis idempotency
- distributed locking
- retry-safe execution

---

### Get Transaction By ID

```http
GET /api/v1/transactions/{transactionId}
```

Purpose:

- fetch transaction details
- retrieve transaction lifecycle state

---

### Get User Transactions

```http
GET /api/v1/transactions/user/{userId}
```

Purpose:

- retrieve transaction history

Supports:

- pagination
- filtering

---

### Get Transaction Status

```http
GET /api/v1/transactions/{transactionId}/status
```

Purpose:

- fetch transaction processing state

---

# Transaction Lifecycle

```text
INITIATED
    |
    v
PENDING_RISK
    |
    +-----------------------+
    |          |            |
    v          v            v
APPROVED   DECLINED   REVIEW_PENDING
```

---

# 3. Risk Decision Service APIs

## Overview

The Risk Decision Service performs:

- fraud detection
- behavioral analysis
- ML scoring
- policy decisioning
- AI-powered fraud investigation
- fraud decision explainability

---

## Base Path

```text
/risk/api/v1
```

---

## Core APIs

### Evaluate Transaction Risk

```http
POST /risk/evaluate
```

Purpose:

- evaluate transaction fraud risk
- execute hard rules
- execute soft rules
- perform ML scoring
- return policy decision

---

## Sample Request

```json
{
  "transactionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": "USER-1001",
  "amount": 120000,
  "transactionType": "WITHDRAWAL",
  "channel": "UPI",
  "country": "IN",
  "deviceId": "DEVICE-991"
}
```

---

## Sample Response

```json
{
  "transactionStatus": "REVIEW",
  "riskScore": 72,
  "reasonCodes": ["HIGH_AMOUNT", "NEW_DEVICE"],
  "fraudProbability": 0.82
}
```

---

### Get All Decisions

```http
GET /risk/api/v1/decisions
```

Purpose:

- retrieve historical fraud decisions

---

### Get Decision By ID

```http
GET /risk/api/v1/decisions/{decisionId}
```

Purpose:

- fetch specific fraud decision details

---

### Get Decision By Transaction ID

```http
GET /risk/api/v1/decisions/transaction/{transactionId}
```

Purpose:

- fetch fraud decision linked to transaction

---

### Generate AI Investigation Summary

```http
POST /risk/{transactionId}/investigation-summary
```

Purpose:

- generate AI-powered fraud investigation reports
- explain fraud decisions in business-readable language
- summarize risk indicators
- recommend analyst actions
- assist fraud investigation teams
- improve operational explainability

Access:

```text
ROLE_ADMIN
```

Only administrators and fraud analysts can access this endpoint.

---

### AI Investigation Flow

```text
Risk Decision Trace
        |
        v
FraudPromptBuilder
        |
        v
Spring AI ChatClient
        |
        v
Ollama LLM
        |
        v
Investigation Summary
```

---

### Sample Request

```http
POST /risk/2e3e92f4-8cf8-4ba5-a97b-7a177812389e/investigation-summary
```

---

### Sample Response

```json
{
  "transactionId": "2e3e92f4-8cf8-4ba5-a97b-7a177812389e",
  "status": "DECLINED",
  "summary": "Investigation Summary: The transaction with ID 2e3e92f4-8cf8-4ba5-a97b-7a177812389e was declined due to exceeding both the single transaction amount limit and the daily transaction amount limit. The machine learning probability for fraud is zero, indicating low suspicion of fraudulent activity.\n\nKey Risk Indicators: AMOUNT_LIMIT_EXCEEDED, DAILY_AMOUNT_LIMIT_EXCEEDED\n\nRecommended Action: Review the customer's account settings to ensure that the transaction limits are appropriate for their spending habits. If necessary, contact the customer to adjust the limits or verify if this was an authorized transaction. No further action is required based on current risk indicators."
}
```

---

### AI Investigation Characteristics

Technology Stack:

```text
Spring AI
Ollama
Qwen Model
Redis Cache
Micrometer
```

Capabilities:

- AI-assisted fraud investigation
- business-readable decision explanations
- risk indicator summarization
- analyst recommendation generation
- cached investigation summaries
- inference latency monitoring

---

# Fraud Decision Outcomes

| Decision | Meaning                            |
| -------- | ---------------------------------- |
| APPROVED | Low-risk transaction               |
| REVIEW   | Manual review required             |
| DECLINED | Fraud detected or policy violation |

---

# AI Investigation Eligibility

| Decision | Investigation Supported |
| -------- | ----------------------- |
| APPROVED | No                      |
| REVIEW   | Yes                     |
| DECLINED | Yes                     |

---

# AI Investigation Metrics

```text
risk.ai.investigation.requests
risk.ai.investigation.success
risk.ai.investigation.failure
risk.ai.investigation.latency
```

# 4. Notification Service APIs

## Overview

The Notification Service manages:

- asynchronous communication
- email delivery
- notification recovery
- operational visibility

---

## Base Path

```text
/api/v1/notifications
```

---

## Core APIs

### Get Notification Status

```http
GET /api/v1/notifications/{notificationId}
```

Purpose:

- retrieve notification delivery status

---

### Get Failed Notifications

```http
GET /api/v1/notifications/failed
```

Purpose:

- operational visibility into failed deliveries

---

### Retry Failed Notification

```http
POST /api/v1/notifications/retry/{notificationId}
```

Purpose:

- replay failed notification delivery

---

# Notification Architecture

Notification processing is:

```text
Kafka-driven asynchronous event processing
```

The service integrates with:

```text
AWS SES
```

for email delivery.

---

# 5. Transaction Observability Service APIs

## Overview

The Transaction Observability Service provides:

- immutable audit persistence
- analytics exports
- transaction traceability
- operational reporting

---

## Base Path

```text
/api/v1/observability
```

---

## Core APIs

### Get Transaction Audit Record

```http
GET /api/v1/observability/transactions/{transactionId}
```

Purpose:

- retrieve immutable transaction audit record

---

### Export Transactions

```http
GET /api/v1/observability/export
```

Purpose:

- generate Excel transaction reports

Features:

- memory-safe streaming exports
- analytics-ready reporting

---

### Get Failed Events

```http
GET /api/v1/observability/failed-events
```

Purpose:

- operational recovery visibility

---

# Asynchronous Event APIs

The platform heavily uses Kafka-based asynchronous workflows.

Primary topics include:

| Topic                    | Purpose                      |
| ------------------------ | ---------------------------- |
| transaction.finalized.v1 | finalized transaction events |
| notification.events.v1   | notification workflows       |
| observability.events.v1  | audit persistence            |

---

# Common Response Characteristics

Responses may include:

- correlation IDs
- timestamps
- reason codes
- fraud metadata
- policy metadata
- pagination metadata

---

# Correlation IDs

All distributed requests support:

```http
X-Correlation-Id
```

Purpose:

- distributed tracing
- operational debugging
- audit linkage

---

# Error Response Model

Standardized error handling includes:

- validation failures
- authorization failures
- fraud rejections
- downstream service failures
- retry-safe responses

---

# Observability Endpoints

All services expose:

```http
/actuator/health
/actuator/prometheus
```

Used for:

- Prometheus scraping
- health monitoring
- operational dashboards

---

# Security Characteristics

The platform implements:

- stateless JWT authentication
- OAuth2 resource server security
- role-based authorization
- ownership-based authorization
- gateway-enforced protection

---

# API Design Principles

The APIs are designed around:

- stateless communication
- distributed-safe workflows
- retry-safe processing
- eventual consistency
- operational observability
- fault-tolerant orchestration

---

# Future API Enhancements

Planned improvements include:

- OpenAPI aggregation
- schema registry integration
- event replay APIs
- GraphQL analytics layer
- streaming APIs
- distributed tracing exposure

---

# Final Summary

The Gringotts platform APIs implement:

```text
Production-style distributed financial microservice APIs
```

designed to support:

- secure transaction orchestration
- ML-powered fraud evaluation
- asynchronous event-driven workflows
- operational observability
- scalable distributed communication
- resilient backend processing
