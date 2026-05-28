# architecture.md

# Transaction Service Architecture

# Overview

The `transaction-service` is a production-style financial transaction orchestration microservice responsible for:

- transaction lifecycle management
- distributed idempotency handling
- fraud-evaluation coordination
- Kafka event publishing
- resilient transaction processing
- secure service-to-service communication

This service acts as the:

```text
Primary transaction orchestration engine
```

within the distributed transaction platform.

The service does NOT perform fraud scoring internally.

Instead, it synchronously communicates with:

```text
risk-decision-service
```

for fraud evaluation.

---

# Core Responsibilities

| Capability | Description |
|---|---|
| Transaction Orchestration | Coordinates full transaction lifecycle |
| Redis Idempotency | Prevents duplicate processing |
| Fraud Coordination | Calls risk-decision-service |
| Feature Enrichment | Generates behavioral fraud features |
| Lifecycle Management | Enforces state transitions |
| Kafka Publishing | Publishes transaction-finalized events |
| Manual Review Processing | Handles REVIEW workflows |
| Distributed Traceability | Correlation ID propagation |

---

# High-Level Architecture

```text
                        +----------------------+
                        |      API Client      |
                        +----------+-----------+
                                   |
                                   v
                +--------------------------------------+
                |     TransactionController            |
                +----------------+---------------------+
                                   |
                                   v
                +--------------------------------------+
                | TransactionOrchestratorService       |
                +----------------+---------------------+
                                   |
         +-------------------------+--------------------------+
         |                         |                          |
         v                         v                          v

+----------------+      +--------------------+      +--------------------+
| Redis Locking  |      | Feature Generation |      | Business Validation |
+----------------+      +--------------------+      +--------------------+

                                   |
                                   v

                +--------------------------------------+
                | RiskEvaluationService                |
                +----------------+---------------------+
                                   |
                                   v
                +--------------------------------------+
                | Feign RiskServiceClient              |
                +----------------+---------------------+
                                   |
                                   v
                +--------------------------------------+
                | risk-decision-service                |
                +--------------------------------------+

                                   |
                                   v

                +--------------------------------------+
                | TransactionCommandService            |
                +----------------+---------------------+
                                   |
                                   v

         +-------------------------+-------------------------+
         |                                                   |
         v                                                   v

+----------------------+                     +-------------------------+
| MySQL Persistence    |                     | Transactional Outbox    |
+----------------------+                     +-------------------------+
                                                           |
                                                           v
                                               +----------------------+
                                               | OutboxPublisher      |
                                               +----------------------+
                                                           |
                                                           v
                                               +----------------------+
                                               | Kafka                |
                                               +----------------------+
```

---

# Architectural Style

The service follows:

- layered microservice architecture
- orchestration-based workflow architecture
- transactional consistency boundaries
- CQRS-inspired separation
- event-driven integration architecture
- distributed systems design principles

---

# Architectural Layers

# 1. API Layer

Responsible for:

- REST endpoint exposure
- request validation
- response handling
- DTO mapping
- API contract management

---

# Main Components

| Component | Responsibility |
|---|---|
| TransactionController | Transaction APIs |
| ManualReviewController | Admin review APIs |

---

# Request Flow

```text
Client Request
      |
      v
Controller Validation
      |
      v
Orchestration Layer
```

---

# 2. Orchestration Layer

Acts as:

```text
Central transaction workflow coordinator
```

Responsible for:

- idempotency handling
- distributed lock coordination
- fraud-service orchestration
- lifecycle progression
- resilience coordination
- observability integration

---

# Main Component

```text
TransactionOrchestratorService
```

---

# Main Responsibilities

| Responsibility | Purpose |
|---|---|
| Request Coordination | Full workflow execution |
| Redis Lock Handling | Duplicate prevention |
| Fraud Coordination | Risk-service integration |
| Metrics Collection | Operational visibility |
| Failure Recovery | Retry-safe orchestration |

---

# Orchestration Flow

```text
Incoming Request
        |
        v
Redis Idempotency Lock
        |
        v
Business Validation
        |
        v
Feature Enrichment
        |
        v
Risk-Service Call
        |
        v
Transaction Finalization
        |
        v
Kafka Outbox Creation
```

---

# 3. Business Validation Layer

Responsible for:

- business rule validation
- transaction request validation
- illegal transaction rejection

---

# Main Component

```text
TransactionBusinessValidator
```

---

# Example Validations

- invalid amount rejection
- unsupported currency rejection
- malformed request rejection
- illegal transaction combination detection

---

# 4. Feature Engineering Layer

Responsible for:

```text
Behavioral fraud feature generation
```

before fraud evaluation.

---

# Main Component

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

# Feature Flow

```text
Transaction
      |
      v
Historical Lookup
      |
      v
Aggregation
      |
      v
Fraud Features
```

---

# 5. Fraud Integration Layer

Responsible for:

```text
Synchronous fraud evaluation communication
```

with:

```text
risk-decision-service
```

---

# Main Components

| Component | Responsibility |
|---|---|
| RiskEvaluationService | Fraud orchestration |
| RiskServiceClient | Feign HTTP client |
| FeignConfig | Timeout/retry configuration |
| FeignCorrelationConfig | Correlation propagation |

---

# Fraud Evaluation Flow

```text
Transaction Service
        |
        v
Feign Client
        |
        v
risk-decision-service
        |
        v
Fraud Decision Response
```

---

# Why Synchronous Communication Is Used

Fraud decision is:

```text
Transaction-critical
```

Transaction cannot finalize until fraud evaluation completes.

Therefore:

```text
Blocking synchronous orchestration
```

is correct.

---

# 6. Redis Idempotency Layer

Responsible for:

- duplicate request prevention
- distributed locking
- retry-safe transaction handling

---

# Main Component

```text
RedisIdempotencyService
```

---

# Redis Lock Flow

```text
Incoming Request
        |
        v
tryLock(idempotencyKey)
        |
   +----+----+
   |         |
SUCCESS    FAILURE
   |         |
   v         v
PROCESS   DUPLICATE
REQUEST   REQUEST
```

---

# Redis States

| State | Meaning |
|---|---|
| IN_PROGRESS | Transaction processing |
| COMPLETED:{txnId} | Transaction finalized |

---

# Why Redis Is Critical

Prevents:

- duplicate transactions
- double processing
- duplicate fraud scoring
- race conditions

---

# 7. Command Layer

Responsible for:

```text
WRITE OPERATIONS
```

including:

- transaction creation
- lifecycle transitions
- finalization logic
- manual review processing
- outbox event persistence

---

# Main Component

```text
TransactionCommandService
```

---

# Responsibilities

| Responsibility | Purpose |
|---|---|
| State Transitions | Workflow consistency |
| Finalization | Apply fraud decisions |
| Outbox Creation | Reliable Kafka publishing |
| Manual Review | Human verification flow |

---

# Lifecycle State Machine

```text
INITIATED
    |
    v
PENDING_RISK
    |
    +--------------------+
    |         |          |
    v         v          v
APPROVED  DECLINED   REVIEW_PENDING
```

---

# Why Lifecycle Policy Exists

Protects against invalid transitions.

Example invalid transitions:

```text
APPROVED -> PENDING_RISK
DECLINED -> REVIEW_PENDING
```

---

# 8. Query Layer

Responsible for:

```text
READ OPERATIONS
```

---

# Main Component

```text
TransactionQueryService
```

---

# Responsibilities

- transaction retrieval
- historical queries
- DTO mapping
- query filtering

---

# Why Query Layer Is Separate

Implements:

```text
CQRS-style separation
```

between:

- writes
- reads

Improves maintainability and scalability.

---

# 9. Persistence Layer

Responsible for:

- transaction persistence
- fraud-decision persistence
- outbox-event persistence
- historical analytics support

---

# Database

```text
MySQL
```

---

# Core Tables

| Table | Purpose |
|---|---|
| transactions | Main transaction records |
| risk_decision_trace | Fraud evaluation results |
| outbox_event | Kafka outbox persistence |
| user_device_history | Behavioral analysis |

---

# Persistence Flow

```text
Application Service
        |
        v
Spring Data JPA
        |
        v
MySQL
```

---

# 10. Kafka Outbox Layer

Responsible for:

```text
Reliable asynchronous event publishing
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

# Main Components

| Component | Responsibility |
|---|---|
| OutboxService | Creates OutboxEvent |
| OutboxPublisher | Background Kafka publisher |
| KafkaProducerService | Kafka communication |
| KafkaConfig | Topic configuration |

---

# Kafka Flow

```text
Transaction Finalized
        |
        v
OutboxEvent Persisted
        |
        v
OutboxPublisher
        |
        v
KafkaProducerService
        |
        v
Kafka Topic
```

---

# Kafka Reliability Features

| Feature | Purpose |
|---|---|
| Idempotent Producer | Duplicate prevention |
| Retry Handling | Fault tolerance |
| Exponential Backoff | Retry control |
| Dead-letter Strategy | Failure isolation |
| Batch Publishing | Throughput optimization |

---

# 11. Security Layer

Responsible for:

- JWT validation
- service authentication
- Feign authorization injection
- distributed trust validation

---

# Security Architecture

```text
Transaction Service
        |
        v
ServiceTokenGenerator
        |
        v
JWT Token
        |
        v
Feign Interceptor
        |
        v
Authorization Header
```

---

# Security Components

| Component | Responsibility |
|---|---|
| ServiceTokenGenerator | Service JWT generation |
| ServiceAuthFeignInterceptor | Authorization injection |
| ServiceJwtProperties | JWT configuration |
| Spring Security | JWT validation |

---

# Security Features

- machine-to-machine authentication
- stateless security
- audience validation
- role-based authorization
- secure inter-service trust

---

# 12. Observability Layer

Responsible for:

- metrics collection
- request tracing
- structured logging
- distributed correlation tracking

---

# Main Technologies

| Technology | Purpose |
|---|---|
| Micrometer | Metrics |
| SLF4J | Logging |
| Correlation IDs | Distributed tracing |
| Prometheus | Monitoring |

---

# Observability Flow

```text
Incoming Request
        |
        v
Correlation ID
        |
        v
Metrics + Logs
        |
        v
Distributed Traceability
```

---

# Key Observability Metrics

| Metric | Purpose |
|---|---|
| transaction.create.request | Request count |
| transaction.completed | Success tracking |
| transaction.failure | Failure monitoring |
| transaction.risk.latency | Fraud-service latency |
| transaction.orchestration.latency | End-to-end latency |

---

# Resilience Characteristics

| Capability | Purpose |
|---|---|
| Redis TTL Recovery | Automatic stale cleanup |
| Retry-safe Orchestration | Duplicate safety |
| Feign Timeouts | Hanging-call protection |
| Kafka Retries | Reliable publishing |
| Graceful Degradation | Redis failure handling |

---

# Architectural Characteristics

| Characteristic | Status |
|---|---|
| Distributed Safe | YES |
| Idempotent | YES |
| Event-Driven | YES |
| Retry-Safe | YES |
| Observable | YES |
| Horizontally Scalable | YES |
| Resilient | YES |
| Secure | YES |

---

# Design Patterns Used

| Pattern | Purpose |
|---|---|
| Transactional Outbox | Reliable Kafka publishing |
| CQRS-style Separation | Read/write isolation |
| Distributed Locking | Idempotency |
| Service-to-Service JWT | Secure microservice communication |
| Orchestration Pattern | Workflow coordination |
| State Machine | Lifecycle consistency |

---

# Future Enhancements

- Kafka consumer services
- Kubernetes deployment
- distributed tracing with OpenTelemetry
- circuit breakers
- Redis cluster mode
- saga orchestration
- stream processing
- feature-store integration

---

# Final Summary

The `transaction-service` acts as:

```text
A production-grade distributed transaction orchestration microservice
```

responsible for:

- reliable transaction execution
- fraud-service coordination
- distributed consistency
- resilient workflow orchestration
- secure inter-service communication
- event-driven transaction propagation

The architecture demonstrates enterprise backend engineering concepts commonly used in:

- fintech systems
- banking platforms
- payment gateways
- real-time authorization systems