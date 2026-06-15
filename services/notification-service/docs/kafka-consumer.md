# kafka-consumer.md

# Kafka Consumer Architecture

# Overview

The `notification-service` consumes:

```text
Notification-related Kafka events
```

to asynchronously send:

- transaction emails
- fraud notifications
- review notifications
- operational alerts

---

# Main Kafka Component

```text
NotificationConsumer
```

---

# Kafka Goals

| Goal | Purpose |
|---|---|
| Async Delivery | Decouple notifications |
| Fault Isolation | Prevent transaction blocking |
| Distributed Consumption | Scalable processing |
| Retry Safety | Recover transient failures |
| Operational Visibility | Kafka monitoring |

---

# High-Level Kafka Flow

```text
Kafka Topic
      |
      v
NotificationConsumer
      |
      v
NotificationProcessingService
      |
      v
Email Delivery
```

---

# NotificationConsumer Responsibilities

| Responsibility | Purpose |
|---|---|
| Kafka Listening | Consume events |
| Event Logging | Observability |
| Event Delegation | Orchestration |
| Failure Propagation | Retry support |

---

# Event Consumption Flow

# Step 1 — Kafka Poll

Consumer polls Kafka broker.

---

# Step 2 — Event Deserialization

Spring Kafka converts payload into:

```text
Notification event DTO
```

---

# Step 3 — Structured Logging

Logs:

```text
event=notification_kafka_received
```

---

# Step 4 — Delegate To Processing Service

Calls:

```java
notificationProcessingService.process(...)
```

---

# Step 5 — Offset Commit

If successful:

```text
Kafka offset committed
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
Retry Infrastructure
```

---

# Why Exceptions Are Re-thrown

Allows:

```text
Spring Kafka retry infrastructure
```

to manage retries automatically.

---

# Kafka Retry Architecture

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
```

---

# Dead Letter Flow

```text
Retry Exhausted
        |
        v
Dead Letter Topic
```

---

# Why DLT Matters

Prevents:

```text
Infinite poison-message retries
```

---

# Consumer Group Architecture

Supports:

- partition balancing
- horizontal scaling
- distributed processing

---

# Parallel Processing Flow

```text
Kafka Partitions
        |
        +----> Consumer Thread 1
        |
        +----> Consumer Thread 2
```

---

# Kafka Observability

Metrics include:

- consumed events
- failed events
- retry counts
- processing latency

---

# Operational Characteristics

| Characteristic | Status |
|---|---|
| Retry-Safe | YES |
| Distributed | YES |
| Async | YES |
| Fault-Tolerant | YES |
| Scalable | YES |

---

# Final Summary

The Kafka consumer architecture inside:

```text
notification-service
```

implements:

```text
Production-grade asynchronous event-consumption architecture
```

through:

- Kafka-driven communication
- retry-safe event handling
- distributed consumer scaling
- operational visibility
- resilient notification processing