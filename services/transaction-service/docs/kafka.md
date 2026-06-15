# kafka.md

# Kafka Architecture

# Overview

The `transaction-service` uses Kafka for:

```text
Reliable asynchronous transaction-finalized event publishing
```

after transaction processing completes.

Kafka integration is implemented using:

```text
Transactional Outbox Pattern
```

to guarantee:

- reliable event delivery
- distributed consistency
- retry-safe publishing
- fault-tolerant event propagation
- scalable asynchronous integration

---

# Why Kafka Is Used

Kafka enables:

- asynchronous downstream processing
- event-driven architecture
- scalable transaction propagation
- decoupled microservices
- eventual consistency

---

# Main Kafka Use Cases

| Use Case | Purpose |
|---|---|
| Notification Processing | SMS / email notifications |
| Analytics Pipelines | Transaction analytics |
| Audit Processing | Compliance tracking |
| Fraud Monitoring | Risk monitoring |
| Reporting Systems | BI integrations |

---

# Very Important Architectural Decision

This service does NOT directly publish Kafka messages
inside transaction orchestration logic.

Instead it uses:

```text
Transactional Outbox Pattern
```

which is the CORRECT production-grade design.

---

# Why Direct Kafka Publishing Is Dangerous

Without outbox:

```text
DB transaction succeeds
BUT
Kafka publish fails
```

Result:

```text
Distributed inconsistency
```

Example:

```text
Transaction finalized
BUT
No downstream event published
```

Very dangerous in financial systems.

---

# Transactional Outbox Pattern

The system first persists:

```text
OutboxEvent
```

inside the SAME database transaction.

Then a background publisher asynchronously publishes events to Kafka.

---

# High-Level Kafka Architecture

```text
Transaction Finalized
        |
        v
OutboxService
(build OutboxEvent)
        |
        v
OutboxEvent persisted in DB
(status=PENDING)
        |
        v
OutboxPublisher Scheduler
        |
        v
KafkaProducerService
        |
        v
Kafka Topic
(transaction.finalized.v1)
        |
        v
Downstream Consumers
```

---

# Kafka Components

| Component | Responsibility |
|---|---|
| KafkaConfig | Topic configuration |
| KafkaProducerConfig | Producer infrastructure |
| KafkaProducerService | Kafka publishing |
| OutboxService | Outbox event creation |
| OutboxPublisher | Background event publisher |

---

# ==========================================
# 1. KafkaConfig
# ==========================================

# Purpose

Creates and configures Kafka topics.

Acts as:

```text
Kafka infrastructure initialization layer
```

---

# Configured Topic

```text
transaction.finalized.v1
```

---

# Topic Purpose

Stores:

```text
Finalized transaction events
```

for downstream consumers.

---

# Topic Configuration

| Property | Value | Purpose |
|---|---|---|
| partitions | 3 | Parallel processing |
| replicas | 3 | Fault tolerance |
| min.insync.replicas | 2 | Reliable writes |

---

# Topic Flow

```text
Transaction Finalized
        |
        v
Kafka Topic
transaction.finalized.v1
        |
        v
Async Consumer Services
```

---

# Why partitions = 3

```java
.partitions(3)
```

Enables:

- parallel consumption
- scalability
- higher throughput

---

# Why replicas = 3

```java
.replicas(3)
```

Provides:

```text
Fault tolerance
```

If one broker crashes:

```text
Kafka still survives
```

---

# Why min.insync.replicas = 2

Ensures:

```text
At least 2 brokers acknowledge write
```

before Kafka confirms success.

Improves:

- durability
- consistency
- reliability

---

# ==========================================
# 2. KafkaProducerConfig
# ==========================================

# Purpose

Configures:

- Kafka ProducerFactory
- KafkaTemplate
- reliability settings
- retry behavior
- idempotent publishing

Acts as:

```text
Kafka producer infrastructure layer
```

---

# Producer Flow

```text
Spring Config
      |
      v
ProducerFactory
      |
      v
KafkaTemplate
      |
      v
Kafka Publishing
```

---

# Important Producer Configurations

# ACKS = all

```java
ACKS_CONFIG = all
```

Meaning:

```text
ALL replicas must acknowledge message
```

before success returned.

Provides:

```text
Strong durability guarantees
```

---

# Idempotent Producer Enabled

```java
ENABLE_IDEMPOTENCE_CONFIG = true
```

Purpose:

```text
Prevent duplicate Kafka messages
```

especially during retries.

---

# Retries Configured

```java
RETRIES_CONFIG = 2
```

Purpose:

```text
Retry transient broker/network failures
```

---

# Retry Backoff

```java
RETRY_BACKOFF_MS_CONFIG = 100
```

Prevents:

```text
Aggressive retry storms
```

---

# Linger MS

```java
LINGER_MS_CONFIG = 5
```

Purpose:

```text
Small batching optimization
```

Improves throughput.

---

# Request Timeout

```java
REQUEST_TIMEOUT_MS_CONFIG = 2000
```

Purpose:

```text
Fail fast on broker delays
```

---

# Delivery Timeout

```java
DELIVERY_TIMEOUT_MS_CONFIG = 8000
```

Purpose:

```text
Maximum retry duration
```

---

# Why This Producer Config Is Good

This setup prioritizes:

```text
Reliability > Raw Throughput
```

which is CORRECT for financial systems.

---

# ==========================================
# 3. KafkaProducerService
# ==========================================

# Purpose

Acts as:

```text
Thin Kafka publishing abstraction layer
```

Responsible ONLY for:

```text
Publishing events to Kafka
```

---

# High-Level Flow

```text
Payload
    |
    v
KafkaTemplate.send()
    |
    v
Kafka Broker
    |
    v
ACK Response
```

---

# Publishing Flow

1. payload received

2. Kafka topic determined

3. Kafka key assigned

4. KafkaTemplate.send() executed

5. producer waits for acknowledgment

6. success/failure determined

---

# Why Kafka Key Is Important

Kafka key:

```text
transactionId
```

ensures:

```text
Same transaction events go to same partition
```

which preserves ordering.

---

# Why .get() Is Used

```java
.get(5, TimeUnit.SECONDS)
```

forces synchronous acknowledgment waiting.

Meaning:

```text
Wait until Kafka CONFIRMS message accepted
```

before marking event SENT.

---

# Why This Matters

Without acknowledgment waiting:

```text
Fire-and-forget publishing
```

could silently lose events.

Very dangerous.

---

# Failure Flow

If Kafka publish fails:

```java
RuntimeException thrown
```

This triggers:

```text
Outbox retry handling
```

---

# ==========================================
# 4. OutboxService
# ==========================================

# Purpose

Creates:

```text
Transactional Outbox Events
```

inside the SAME database transaction
as transaction finalization.

---

# Main Responsibility

Converts finalized transaction into:

```text
OutboxEvent
```

for reliable Kafka publishing.

---

# Outbox Flow

```text
Finalized Transaction
        |
        v
Build Domain Event
        |
        v
Serialize JSON Payload
        |
        v
Create OutboxEvent
        |
        v
Persist PENDING Event
```

---

# OutboxEvent Contains

| Field | Purpose |
|---|---|
| aggregateId | Transaction identifier |
| aggregateType | Entity type |
| eventType | Event classification |
| payload | Serialized JSON |
| status | Publishing status |
| retryCount | Retry tracking |
| nextRetryAt | Retry scheduling |

---

# Event Statuses

| Status | Meaning |
|---|---|
| PENDING | Awaiting publishing |
| SENT | Successfully published |
| FAILED | Publishing failed |
| DEAD | Retry exhausted |

---

# Why JSON Payload Is Used

Payload serialized into JSON:

```java
objectMapper.writeValueAsString(...)
```

Benefits:

- portability
- replay support
- language independence
- auditability

---

# Why eventVersion Exists

```java
eventVersion(1)
```

supports:

```text
Schema evolution
```

for future event changes.

---

# ==========================================
# 5. OutboxPublisher
# ==========================================

# Purpose

Acts as:

```text
Background Kafka publishing engine
```

This is the MOST IMPORTANT Kafka orchestration component.

---

# High-Level Flow

```text
Scheduler Trigger
        |
        v
Fetch PENDING/FAILED events
        |
        v
Publish to Kafka
        |
   +----+----+
   |         |
SUCCESS    FAILURE
   |         |
   v         v
SENT      RETRY/DEAD
```

---

# Scheduler

```java
@Scheduled(...)
```

runs continuously.

Purpose:

```text
Background asynchronous publishing
```

---

# Batch Processing

Uses:

```java
PageRequest.of(0, batchSize)
```

Purpose:

- controlled memory usage
- scalable processing
- throughput optimization

---

# Event Fetching

Publisher loads:

```text
PENDING
FAILED
```

events eligible for retry.

---

# Success Flow

1. publish event to Kafka

2. wait for broker acknowledgment

3. mark event SENT

4. update sentAt timestamp

5. persist updated state

---

# Failure Flow

If Kafka publish fails:

```java
handleFailure(event, ex)
```

executes.

---

# Retry Logic

retryCount incremented.

nextRetryAt calculated.

---

# Exponential Backoff

Retry delay:

```text
2^retry seconds
```

Example:

| Retry | Delay |
|---|---|
| 1 | 2 sec |
| 2 | 4 sec |
| 3 | 8 sec |

Purpose:

```text
Prevent retry storms
```

---

# Dead Letter Handling

If retries exceed limit:

```text
Event marked DEAD
```

Meaning:

```text
Manual intervention required
```

---

# Why DEAD State Is Important

Prevents:

```text
Infinite retry loops
```

during persistent failures.

---

# Complete Kafka Flow

```text
Transaction Finalized
        |
        v
OutboxService
(build event)
        |
        v
OutboxEvent persisted
(status=PENDING)
        |
        v
OutboxPublisher Scheduler
        |
        v
KafkaProducerService.send()
        |
        v
Kafka Topic:
transaction.finalized.v1
        |
        v
Consumer Services
```

---

# Kafka Event Example

```json
{
  "eventType": "TRANSACTION_FINALIZED",
  "eventVersion": 1,
  "transactionId": "txn-991",
  "userId": "USER-1001",
  "transactionStatus": "APPROVED",
  "amount": 25000,
  "currency": "INR",
  "createdAt": "2026-05-18T10:30:00"
}
```

---

# Kafka Reliability Features

| Feature | Purpose |
|---|---|
| Transactional Outbox | Distributed consistency |
| Idempotent Producer | Duplicate prevention |
| Retry Handling | Fault tolerance |
| Exponential Backoff | Retry safety |
| Dead-letter Strategy | Failure isolation |
| ACKS=all | Strong durability |
| Replication | Broker fault tolerance |

---

# Why This Architecture Is Enterprise-Grade

Most beginner Kafka systems directly call:

```java
kafkaTemplate.send(...)
```

inside service methods.

That is NOT reliable.

This project correctly implements:

```text
Transactional Outbox Pattern
```

used in:

- banking systems
- fintech platforms
- payment gateways
- distributed financial systems

---

# Architectural Characteristics

| Characteristic | Status |
|---|---|
| Reliable Publishing | YES |
| Distributed Safe | YES |
| Retry-Safe | YES |
| Eventually Consistent | YES |
| Event-Driven | YES |
| Fault-Tolerant | YES |
| Horizontally Scalable | YES |

---

# Future Enhancements

- Kafka consumer services
- schema registry integration
- Avro serialization
- distributed tracing
- exactly-once processing
- Kafka Streams processing
- dead-letter topics
- event replay tooling

---

# Final Summary

The Kafka architecture inside:

```text
transaction-service
```

implements:

```text
Production-grade reliable event-driven transaction propagation
```

using:

- transactional outbox pattern
- resilient Kafka publishing
- retry-safe processing
- asynchronous integration
- distributed consistency guarantees

The design closely resembles real-world enterprise Kafka architectures used in:

- banking systems
- payment processors
- fintech platforms
- distributed transaction systems