
# Transaction Processing service

# Overview

This project is a production-style distributed transaction processing and fraud detection platform built using modern microservice architecture principles.

The system simulates how enterprise-grade financial platforms process transactions securely while performing:

- real-time fraud detection
- risk evaluation
- behavioral analysis
- distributed event publishing
- resilient orchestration
- idempotent transaction handling
- secure service-to-service communication
---

# System Architecture

```text
                        +----------------------+
                        |      API Client      |
                        +----------+-----------+
                                   |
                                   v
                  +----------------------------------+
                  |      transaction-service         |
                  |----------------------------------|
                  | - Transaction Orchestration      |
                  | - Idempotency Handling           |
                  | - Kafka Outbox                  |
                  | - Feature Enrichment            |
                  | - Feign Integration             |
                  +----------------+----------------+
                                   |
                                   | Feign Sync Call
                                   v
                  +----------------------------------+
                  |      risk-decision-service       |
                  |----------------------------------|
                  | - Hard Rules Engine              |
                  | - Soft Rules Engine              |
                  | - ML Scoring Engine              |
                  | - Policy Decisioning             |
                  | - Fraud Evaluation               |
                  +----------------+----------------+
                                   |
                                   v
                          +----------------+
                          |     MySQL      |
                          +----------------+

                                   |
                                   v

                          +----------------+
                          |     Redis      |
                          +----------------+

                                   |
                                   v

                          +----------------+
                          |     Kafka      |
                          +----------------+
                                   |
                                   v
                    +-----------------------------+
                    | Async Downstream Consumers  |
                    +-----------------------------+
```

---

# Core Features

## Transaction Processing

- distributed transaction orchestration
- idempotent transaction execution
- transaction lifecycle management
- transactional consistency enforcement
- manual review workflow

---

## Fraud Detection

- hard-rule fraud detection
- soft-rule behavioral analysis
- ML probability scoring
- policy-based decisioning
- explainable fraud decisions

---

## Distributed Reliability

- Redis distributed locking
- retry-safe orchestration
- transactional outbox pattern
- Kafka retry handling
- dead-letter strategy
- graceful degradation

---

## Security

- OAuth2 Resource Server
- JWT authentication
- service-to-service authentication
- Keycloak integration
- role-based authorization
- audience validation

---

## Observability

- Micrometer metrics
- correlation ID propagation
- structured logging
- request tracing
- Prometheus monitoring
- Grafana-ready metrics

---

# Microservices

# 1. transaction-service

Handles:

- transaction orchestration
- idempotency coordination
- fraud-service integration
- Kafka event publishing
- lifecycle management

---

# Major Responsibilities

| Capability | Description |
|---|---|
| Transaction Creation | Creates transaction lifecycle |
| Redis Idempotency | Prevents duplicate processing |
| Feature Enrichment | Generates fraud features |
| Risk-Service Integration | Calls fraud engine |
| Transaction Finalization | Applies fraud decision |
| Kafka Publishing | Publishes finalized events |

---

# Core Components

| Component | Responsibility |
|---|---|
| TransactionOrchestratorService | Main orchestration engine |
| TransactionCommandService | Transaction state transitions |
| TransactionQueryService | Read/query layer |
| RedisIdempotencyService | Distributed locking |
| RiskEvaluationService | Feign fraud integration |
| OutboxPublisher | Kafka outbox publisher |

---

# 2. risk-decision-service

Handles:

- fraud evaluation
- risk scoring
- ML scoring
- policy decisioning
- fraud explainability

---

# Major Responsibilities

| Capability | Description |
|---|---|
| Hard Rules | Deterministic fraud detection |
| Soft Rules | Behavioral anomaly detection |
| ML Scoring | Logistic-regression fraud scoring |
| Policy Engine | Final decision orchestration |
| Audit Persistence | Explainable fraud traceability |

---

# Fraud Decision Flow

```text
Incoming Risk Request
        |
        v
Derived Feature Generation
        |
        v
Hard Rules Engine
        |
        v
Soft Rules Engine
        |
        v
ML Scoring
        |
        v
Policy Decision
        |
        v
RiskDecisionResponse
```

---

# Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot |
| Security | Spring Security + OAuth2 |
| Database | MySQL |
| Cache | Redis |
| Messaging | Kafka |
| ORM | Spring Data JPA |
| Metrics | Micrometer |
| Monitoring | Prometheus + Grafana |
| Service Communication | OpenFeign |
| Authentication | Keycloak |
| Containerization | Docker |

---

# Redis Architecture

Redis is used for:

- distributed locking
- idempotency handling
- retry-safe orchestration
- duplicate request prevention

---

# Redis Flow

```text
Incoming Request
        |
        v
Redis tryLock()
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

# Kafka Architecture

Kafka is used for:

```text
Asynchronous transaction-finalized event publishing
```

The system implements:

```text
Transactional Outbox Pattern
```

to guarantee reliable event delivery.

---

# Kafka Event Flow

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
transaction.finalized.v1
```

---

# Feign Communication

Feign is used for:

```text
Synchronous fraud evaluation communication
```

between:

```text
transaction-service
        |
        v
risk-decision-service
```

---

# Feign Flow

```text
TransactionOrchestratorService
        |
        v
RiskEvaluationService
        |
        v
RiskServiceClient
        |
        v
risk-decision-service
```

---

# Keycloak Security Architecture

The project uses:

```text
JWT-based machine-to-machine authentication
```

for secure inter-service communication.

---

# Security Flow

```text
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
        |
        v
risk-decision-service
```

---

# Transaction Lifecycle

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

# Fraud Decision Outcomes

| Decision | Meaning |
|---|---|
| APPROVED | Low risk |
| DECLINED | Fraud detected |
| REVIEW | Requires manual verification |

---

# Resilience Features

| Feature | Purpose |
|---|---|
| Redis Idempotency | Duplicate prevention |
| Kafka Retry Handling | Reliable event publishing |
| TTL-based Recovery | Stale lock cleanup |
| Feign Timeout Protection | Prevent hanging calls |
| Transactional Outbox | Distributed consistency |
| Retry-safe Orchestration | Fault tolerance |

---

# Observability Features

| Feature | Purpose |
|---|---|
| Correlation IDs | Distributed tracing |
| Micrometer Metrics | Performance monitoring |
| Structured Logging | Operational debugging |
| Timers & Counters | SLA tracking |
| Health Endpoints | Service monitoring |

---

# Testing Strategy

The project includes:

- unit testing
- orchestration testing
- fraud-rule testing
- resilience testing
- lifecycle validation

---

# Major Testing Areas

| Test Area | Purpose |
|---|---|
| Fraud Rule Tests | Rule correctness |
| Orchestration Tests | Workflow validation |
| Redis Tests | Idempotency safety |
| Kafka Tests | Event reliability |
| Lifecycle Tests | State consistency |

---

# Production-Grade Concepts Implemented

This project demonstrates enterprise backend engineering concepts including:

- distributed systems
- event-driven architecture
- transactional outbox pattern
- CQRS separation
- idempotent APIs
- distributed locking
- machine authentication
- fraud evaluation pipelines
- behavioral fraud analysis
- resilient orchestration
- eventual consistency
- synchronous + asynchronous integration

---

# Future Enhancements

- Kubernetes deployment
- distributed tracing with OpenTelemetry
- schema registry integration
- Kafka consumer services
- Redis cluster mode
- circuit breakers
- ML model serving
- feature store integration
- stream processing
- real-time analytics

---

# Learning Outcomes

This project demonstrates practical implementation of:

- enterprise Java backend engineering
- microservice orchestration
- distributed transaction coordination
- fraud detection architecture
- Kafka event-driven systems
- Redis distributed systems
- secure service communication
- resilient financial workflows

---

# Final Summary

This project is a:

```text
Production-style distributed financial transaction platform
```

designed to simulate how modern fintech systems handle:

- secure transaction processing
- real-time fraud detection
- distributed consistency
- event-driven communication
- scalable microservice orchestration
- resilient backend workflows
````
