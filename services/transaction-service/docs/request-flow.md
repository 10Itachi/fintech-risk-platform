# request-flow.md

# Transaction Service Request Flow

# Overview

This document explains the complete end-to-end request processing flow inside the:

```text
transaction-service
```

The service acts as the:

```text
Primary transaction orchestration engine
```

responsible for:

- transaction validation
- distributed idempotency
- fraud-evaluation coordination
- lifecycle management
- Kafka event propagation
- resilience handling

---

# Complete Request Lifecycle

```text
Client Request
        |
        v
TransactionController
        |
        v
Request Validation
        |
        v
TransactionOrchestratorService
        |
        v
Redis Idempotency Lock
        |
        v
Business Validation
        |
        v
Transaction Persistence
        |
        v
Feature Enrichment
        |
        v
Feign Fraud Evaluation
        |
        v
risk-decision-service
        |
        v
Fraud Decision Response
        |
        v
Transaction Finalization
        |
        v
Outbox Event Creation
        |
        v
Kafka Publishing
        |
        v
HTTP Response
```

---

# 1. Client Request Entry

Client sends transaction request:

```http
POST /transactions
```

with:

- transaction payload
- idempotency key
- JWT authentication token

---

# Example Request

```json
{
  "sourceAccountId": "ACC-1001",
  "destinationAccountId": "ACC-2001",
  "amount": 25000,
  "currency": "INR",
  "transactionType": "TRANSFER"
}
```

---

# Required Headers

| Header | Purpose |
|---|---|
| Authorization | JWT authentication |
| Idempotency-Key | Duplicate protection |
| X-Correlation-Id | Distributed tracing |

---

# 2. Request Reaches TransactionController

Controller receives request.

Main responsibilities:

- endpoint exposure
- DTO validation
- header extraction
- orchestration delegation

---

# Controller Flow

```text
Incoming HTTP Request
        |
        v
@RequestBody Validation
        |
        v
Extract Headers
        |
        v
Call TransactionOrchestratorService
```

---

# Controller Validations

The controller validates:

- request body presence
- DTO schema correctness
- mandatory headers
- bean validation annotations

---

# Example Validations

| Validation | Purpose |
|---|---|
| amount > 0 | Prevent invalid transfers |
| non-null accounts | Mandatory fields |
| currency validation | Supported currencies |
| idempotency key presence | Duplicate safety |

---

# 3. Metrics & Observability Initialization

Inside orchestration layer:

```java
meterRegistry.counter(...).increment();
```

and

```java
Timer.Sample totalTimer
```

initialized.

---

# Purpose

Tracks:

- request count
- orchestration latency
- operational metrics
- SLA monitoring

---

# 4. Distributed Idempotency Lock

The service attempts Redis distributed lock acquisition.

---

# Redis Flow

```text
tryLock(idempotencyKey)
        |
   +----+----+
   |         |
SUCCESS    FAILURE
   |         |
   v         v
NEW      DUPLICATE
REQUEST  REQUEST
```

---

# Redis Key Format

```text
idm:txn:{idempotencyKey}
```

---

# Redis States

| State | Meaning |
|---|---|
| IN_PROGRESS | Transaction processing active |
| COMPLETED:{txnId} | Transaction finalized |

---

# Why Redis Lock Exists

Prevents:

- duplicate transaction creation
- double payments
- duplicate fraud scoring
- concurrent request race conditions

---

# 5. Duplicate Request Handling

If Redis detects duplicate request:

```text
Existing transaction lookup starts
```

---

# Duplicate Resolution Flow

```text
Redis COMPLETED State
        |
        v
Fetch Existing Transaction
        |
        v
Return Existing Response
```

---

# Why This Is Important

Enables:

```text
Retry-safe transaction APIs
```

Clients may safely retry requests.

---

# 6. Business Validation

Request enters:

```text
TransactionBusinessValidator
```

---

# Validation Responsibilities

| Validation | Purpose |
|---|---|
| Amount Validation | Prevent invalid transfers |
| Currency Validation | Supported currencies |
| Account Validation | Ensure valid accounts |
| Business Rules | Prevent illegal flows |

---

# Failure Flow

If validation fails:

```text
Validation Exception
        |
        v
Global Exception Handler
        |
        v
400 Bad Request
```

---

# 7. Initial Transaction Creation

The orchestration layer builds transaction entity.

Then calls:

```text
TransactionCommandService.createInitialTransaction()
```

---

# Initial Transaction State

```text
PENDING_RISK
```

---

# Persistence Flow

```text
Transaction Entity
        |
        v
JPA Repository
        |
        v
MySQL
```

---

# Why Initial Persistence Happens Early

Ensures:

- transaction durability
- lifecycle traceability
- recovery support
- audit consistency

---

# 8. Fraud Feature Enrichment

The service generates:

```text
Behavioral fraud intelligence
```

before fraud evaluation.

---

# Main Feature Service

```text
TransactionRiskFeatureService
```

---

# Generated Features

| Feature | Purpose |
|---|---|
| TotalAmountLast24h | Spending anomaly detection |
| TransactionCountLast24h | Velocity fraud detection |
| Device Familiarity | Behavioral analysis |

---

# Feature Enrichment Flow

```text
Historical Transaction Lookup
        |
        v
Aggregation Queries
        |
        v
Behavioral Features
```

---

# Why Behavioral Features Matter

Fraud systems depend heavily on:

- historical patterns
- spending behavior
- transaction velocity
- anomaly detection

---

# 9. Fraud Evaluation Request Creation

Internal transaction converted into:

```text
RiskDecisionRequest
```

using:

```text
RiskRequestMapper
```

---

# Risk Request Contains

- transaction metadata
- behavioral features
- account information
- device information
- transaction context

---

# 10. Feign Fraud Evaluation Call

The service performs synchronous fraud evaluation.

---

# Fraud Evaluation Flow

```text
Transaction Service
        |
        v
RiskEvaluationService
        |
        v
Feign RiskServiceClient
        |
        v
risk-decision-service
```

---

# Why Synchronous Communication Is Used

Fraud decision is:

```text
Transaction-critical
```

Transaction cannot finalize until fraud decision completes.

---

# Feign Infrastructure

| Component | Responsibility |
|---|---|
| RiskServiceClient | HTTP client |
| FeignConfig | Timeouts/retries |
| FeignCorrelationConfig | Trace propagation |

---

# Security During Feign Call

Before request leaves:

```text
ServiceAuthFeignInterceptor
```

injects:

```text
Authorization: Bearer <jwt>
```

---

# JWT Purpose

Enables:

```text
Machine-to-machine authentication
```

between microservices.

---

# Correlation ID Propagation

```text
X-Correlation-Id
```

automatically propagated across services.

This enables:

- distributed tracing
- operational debugging
- request reconstruction

---

# 11. risk-decision-service Processing

The fraud engine performs:

- hard-rule evaluation
- soft-rule evaluation
- ML scoring
- policy decisioning

---

# Fraud Engine Flow

```text
Risk Request
        |
        v
Hard Rules
        |
        v
Soft Rules
        |
        v
ML Scoring
        |
        v
Policy Engine
        |
        v
Risk Decision
```

---

# Possible Fraud Decisions

| Decision | Meaning |
|---|---|
| APPROVED | Low risk |
| DECLINED | Fraud detected |
| REVIEW | Manual review required |

---

# 12. Fraud Response Returned

Feign receives:

```text
RiskDecisionResponse
```

containing:

- fraud decision
- ML probability
- reason codes
- policy metadata

---

# 13. Transaction Finalization

The orchestration layer calls:

```text
TransactionCommandService.finalizeTransaction()
```

---

# Finalization Responsibilities

| Responsibility | Purpose |
|---|---|
| Apply Fraud Decision | Final transaction status |
| Persist Risk Trace | Auditability |
| Update Lifecycle | Workflow consistency |
| Create Outbox Event | Kafka publishing |

---

# Lifecycle State Transitions

```text
PENDING_RISK
    |
    +--------------------+
    |         |          |
    v         v          v
APPROVED  DECLINED   REVIEW_PENDING
```

---

# Lifecycle Policy Validation

The service validates:

```text
Only legal transitions allowed
```

Prevents invalid workflow corruption.

---

# 14. Outbox Event Creation

After transaction finalization:

```text
OutboxService
```

creates:

```text
TransactionFinalizedEvent
```

---

# Why Outbox Pattern Exists

Without outbox:

```text
DB commit succeeds
Kafka publish fails
```

causing inconsistency.

Outbox guarantees:

```text
Reliable eventual publishing
```

---

# Outbox Flow

```text
Transaction Finalized
        |
        v
OutboxEvent Persisted
(status=PENDING)
```

---

# 15. Kafka Publishing

Background scheduler:

```text
OutboxPublisher
```

fetches pending events.

---

# Kafka Publishing Flow

```text
OutboxPublisher
        |
        v
KafkaProducerService
        |
        v
Kafka Topic:
transaction.finalized.v1
```

---

# Kafka Reliability Features

| Feature | Purpose |
|---|---|
| Idempotent Producer | Prevent duplicates |
| Retry Handling | Fault tolerance |
| Exponential Backoff | Retry safety |
| Dead-letter Strategy | Failure isolation |

---

# 16. Successful HTTP Response

Final transaction response returned to client.

---

# Example Response

```json
{
  "transactionId": "txn-991",
  "transactionStatus": "APPROVED",
  "amount": 25000,
  "currency": "INR"
}
```

---

# 17. Failure Handling Flow

If any orchestration failure occurs:

```text
Exception
      |
      v
Redis Lock Cleanup
      |
      v
Failure Metrics
      |
      v
Structured Logging
      |
      v
Global Exception Handler
```

---

# Redis Failure Recovery

If transaction fails:

```text
releaseLock(idempotencyKey)
```

removes:

```text
IN_PROGRESS
```

allowing safe retries.

---

# Graceful Redis Degradation

If Redis unavailable:

```text
DB unique constraints become fallback protection
```

System still operates.

---

# Request Observability

Every request includes:

```text
X-Correlation-Id
```

Used for:

- distributed tracing
- debugging
- auditability
- operational analysis

---

# Metrics Collected

| Metric | Purpose |
|---|---|
| transaction.create.request | Request count |
| transaction.completed | Success tracking |
| transaction.failure | Failure tracking |
| transaction.risk.latency | Fraud-service latency |
| transaction.orchestration.latency | Full request latency |

---

# End-to-End Request Flow Summary

```text
Client
   |
   v
TransactionController
   |
   v
Redis Idempotency
   |
   v
Business Validation
   |
   v
Initial Persistence
   |
   v
Feature Enrichment
   |
   v
Feign Fraud Evaluation
   |
   v
risk-decision-service
   |
   v
Fraud Decision
   |
   v
Transaction Finalization
   |
   v
Outbox Persistence
   |
   v
Kafka Publishing
   |
   v
HTTP Response
```

---

# Final Summary

The `transaction-service` request flow demonstrates:

- distributed transaction orchestration
- synchronous fraud coordination
- Redis-backed idempotency
- lifecycle-consistent processing
- reliable Kafka event publishing
- resilient microservice communication
- enterprise-grade financial workflow design

The architecture is designed to simulate:

```text
Production-style fintech transaction processing systems
```

with strong focus on:

- reliability
- consistency
- scalability
- fraud protection
- distributed systems safety