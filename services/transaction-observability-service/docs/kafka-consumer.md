# kafka-consumer.md

# Kafka Consumer Architecture

# Overview

The `transaction-observability-service` consumes:

```text
TransactionFinalizedEvent
```

from Kafka to provide:

- immutable audit persistence
- asynchronous observability
- analytics ingestion
- operational traceability
- failure recovery

The Kafka consumer architecture is designed for:

```text
Reliable idempotent financial-event consumption
```

---

# Kafka Consumer Goals

| Goal | Purpose |
|---|---|
| Reliable Event Consumption | Stable Kafka processing |
| Duplicate Prevention | Idempotent handling |
| Retry Handling | Recover transient failures |
| Failure Isolation | Prevent event loss |
| Operational Visibility | Monitoring & debugging |
| Scalable Consumption | Distributed processing |

---

# Core Kafka Components

| Component | Responsibility |
|---|---|
| TransactionEventConsumer | Main Kafka listener |
| ConsumerRetryConfig | Retry-topic & DLT configuration |
| TransactionObservabilityProcessingService | Event-processing orchestration |

---

# High-Level Consumer Architecture

```text
Kafka Topic
(transaction.finalized.v1)
        |
        v
TransactionEventConsumer
        |
        v
TransactionObservabilityProcessingService
        |
   +----+----+
   |         |
SUCCESS    FAILURE
   |         |
   v         v
ACK       RETRY TOPIC
                |
                v
          Dead Letter Topic
```

---

# ==========================================
# 1. TransactionEventConsumer
# ==========================================

# Purpose

Acts as:

```text
Primary Kafka event-consumption layer
```

Responsible for:

- Kafka event listening
- deserialization intake
- observability logging
- delegation to processing service
- retry triggering

---

# High-Level Flow

```text
Kafka Message
      |
      v
@KafkaListener
      |
      v
Structured Logging
      |
      v
TransactionObservabilityProcessingService
      |
   +--+---+
   |      |
SUCCESS  FAILURE
```

---

# Kafka Listener

Uses:

```java
@KafkaListener(...)
```

to subscribe to Kafka topic.

---

# Consumed Topic

```text
transaction.finalized.v1
```

---

# Consumer Group

Configured using:

```text
transaction-observability-group
```

---

# Why Consumer Groups Matter

Enables:

- horizontal scalability
- partition balancing
- distributed consumption

---

# Event Consumption Flow

```text
Kafka Broker
      |
      v
Consumer Poll
      |
      v
TransactionFinalizedEvent
      |
      v
Consumer Method
```

---

# consume(TransactionFinalizedEvent event)

# Step-by-Step Internal Flow

---

# 1. Kafka Message Received

Kafka broker delivers event.

---

# 2. Structured Logging

Logs:

```text
event=kafka_message_received
```

Includes:

- transactionId
- eventId
- topic

---

# Why This Matters

Provides:

- operational visibility
- distributed debugging
- audit traceability

---

# 3. Delegate to Processing Service

Calls:

```java
transactionObservabilityProcessingService.process(event)
```

---

# Why Delegation Exists

Separates:

| Concern | Layer |
|---|---|
| Kafka Consumption | Consumer Layer |
| Business Processing | Service Layer |

---

# Benefits

- cleaner architecture
- easier testing
- separation of concerns

---

# 4. Success Flow

If processing succeeds:

```text
Kafka offset acknowledged
```

---

# ACK Flow

```text
Event Processed
      |
      v
Consumer Commit
      |
      v
Kafka Offset Saved
```

---

# Why Offset Commit Matters

Prevents:

```text
Reprocessing already-consumed events
```

---

# 5. Failure Flow

If exception occurs:

```text
Retry infrastructure activated
```

---

# Failure Flow

```text
Processing Failure
        |
        v
Exception Thrown
        |
        v
Retry Topic
```

---

# Why Exceptions Are Re-thrown

Consumer intentionally propagates exceptions so:

```text
Spring Kafka retry infrastructure
```

can handle retries automatically.

---

# Benefits

| Benefit | Purpose |
|---|---|
| Retry Automation | Transient recovery |
| Failure Isolation | Safe processing |
| Clean Separation | Infrastructure-managed retries |

---

# ==========================================
# 2. ConsumerRetryConfig
# ==========================================

# Purpose

Acts as:

```text
Kafka retry and dead-letter orchestration layer
```

This is the MOST IMPORTANT resilience component in Kafka consumption.

---

# Main Responsibilities

| Responsibility | Purpose |
|---|---|
| Retry Topic Creation | Retry orchestration |
| DLT Configuration | Dead-letter handling |
| Backoff Policies | Retry safety |
| Failure Isolation | Operational stability |

---

# Why Retry Infrastructure Exists

Kafka consumers may fail because of:

- temporary DB outages
- serialization failures
- transient infrastructure issues
- network instability

Retries allow:

```text
Automatic transient recovery
```

---

# High-Level Retry Flow

```text
Kafka Event
      |
      v
Consumer Failure
      |
      v
Retry Topic
      |
      v
Retry Consumer
      |
   +--+---+
   |      |
SUCCESS  FAILURE
   |      |
   v      v
ACK      DLT
```

---

# Retry Topics

Spring Kafka automatically creates:

```text
transaction.finalized.v1-retry
```

style topics.

---

# Dead Letter Topic (DLT)

Final failed events routed to:

```text
transaction.finalized.v1-dlt
```

---

# Why DLT Matters

Without DLT:

```text
Poison messages may retry forever
```

Very dangerous operationally.

---

# Retry Backoff Strategy

Uses:

```text
Exponential backoff
```

---

# Example Retry Delays

| Retry Attempt | Delay |
|---|---|
| 1 | 1 sec |
| 2 | 2 sec |
| 3 | 4 sec |

---

# Why Exponential Backoff Matters

Prevents:

```text
Retry storms
```

during outages.

---

# Retry Flow

```text
Failure
   |
   v
Wait
   |
   v
Retry
```

---

# Retryable Exceptions

Typical retry candidates:

- transient DB issues
- temporary infrastructure outages
- network instability

---

# Non-Retryable Exceptions

Typical non-retryable failures:

- malformed payloads
- invalid schema
- permanent validation failures

---

# DLT Flow

```text
Retry Exhausted
        |
        v
Dead Letter Topic
        |
        v
Manual Investigation
```

---

# Why DLT Is Enterprise-Grade

DLT provides:

- poison-message isolation
- operational recovery
- forensic debugging

---

# ==========================================
# Kafka Consumption Lifecycle
# ==========================================

# Complete Flow

```text
Kafka Topic
(transaction.finalized.v1)
        |
        v
TransactionEventConsumer
        |
        v
Processing Service
        |
   +----+----+
   |         |
SUCCESS    FAILURE
   |         |
   v         v
OFFSET     RETRY TOPIC
COMMIT         |
                v
          RETRY CONSUMER
                |
           +----+----+
           |         |
        SUCCESS     FAILURE
           |         |
           v         v
         COMMIT     DLT
```

---

# ==========================================
# Idempotent Consumer Design
# ==========================================

# Very Important Design Principle

Kafka provides:

```text
At-least-once delivery
```

meaning duplicates CAN occur.

---

# Solution

Consumer architecture implements:

```text
Idempotent processing
```

using:

```text
ProcessedEventRepository
```

---

# Why This Is Critical

Protects against:

- duplicate Kafka delivery
- consumer restart reprocessing
- rebalance duplication

---

# Duplicate Flow

```text
Duplicate Event
        |
        v
existsByEventId()
        |
        v
Skip Processing
```

---

# ==========================================
# Operational Characteristics
# ==========================================

| Characteristic | Status |
|---|---|
| Idempotent | YES |
| Retry-Safe | YES |
| Fault-Tolerant | YES |
| Dead-Letter Support | YES |
| Distributed Consumer | YES |
| Horizontally Scalable | YES |

---

# ==========================================
# Failure Scenarios Handled
# ==========================================

| Failure Scenario | Handling |
|---|---|
| Temporary DB outage | Retry topic |
| Kafka duplicate delivery | Idempotent skip |
| Poison message | DLT |
| Consumer crash | Kafka rebalancing |
| Retry exhaustion | Dead-letter isolation |

---

# Why This Architecture Is Enterprise-Grade

This Kafka consumer architecture demonstrates patterns used in:

- banking-event pipelines
- fintech audit systems
- distributed observability platforms
- compliance-event processing systems

including:

- idempotent consumers
- retry-topic orchestration
- dead-letter topics
- exponential backoff
- distributed consumption
- operational isolation

---

# Future Enhancements

- Kafka lag monitoring
- OpenTelemetry tracing
- replay tooling
- schema registry integration
- Avro serialization
- consumer autoscaling
- distributed tracing dashboards

---

# Final Summary

The Kafka consumer architecture inside:

```text
transaction-observability-service
```

implements:

```text
Production-grade fault-tolerant asynchronous event consumption
```

through:

- idempotent Kafka processing
- retry-topic orchestration
- dead-letter isolation
- distributed consumer scaling
- operational observability
- resilient event recovery

The design ensures:

```text
Reliable financial-event ingestion
even during partial infrastructure failures
```