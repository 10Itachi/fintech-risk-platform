# Notification Service Architecture

# Overview

The `notification-service` follows:

```text
Event-Driven Microservice Architecture
```

responsible for:

- asynchronous notification processing
- distributed communication handling
- transactional email delivery
- notification failure recovery
- operational observability

The service consumes Kafka events and transforms them into:

```text
User-facing communication actions
```

---

# Architectural Goals

| Goal | Purpose |
|---|---|
| Async Communication | Decouple notification delivery |
| Fault Isolation | Prevent email failures affecting transactions |
| Scalability | Independent communication scaling |
| Distributed Reliability | Event-driven delivery |
| Operational Visibility | Notification monitoring |

---

# High-Level Architecture

```text
Kafka Topics
      |
      v
NotificationConsumer
      |
      v
NotificationProcessingService
      |
      +-----------------------------+
      |                             |
      v                             v

NotificationSender          FailedNotificationEventService
      |
      v
EmailNotificationSender
      |
      v
SesEmailClient
      |
      v
AWS SES
```

---

# Architectural Layers

```text
Controller Layer
        |
        v
Operational Service Layer
        |
        v
Notification Processing Layer
        |
        v
Communication Integration Layer
        |
        v
AWS SES
```

---

# Main Components

| Component | Responsibility |
|---|---|
| NotificationConsumer | Kafka event ingestion |
| NotificationProcessingService | Processing orchestration |
| NotificationSender | Notification abstraction |
| EmailNotificationSender | Email implementation |
| SesEmailClient | AWS SES communication |
| FailedNotificationEventService | Failure recovery |
| NotificationOperationsService | Operations/query APIs |

---

# 1. Kafka Consumption Layer

# Purpose

Consumes:

```text
Notification-related Kafka events
```

---

# Main Component

```text
NotificationConsumer
```

---

# Responsibilities

- Kafka event listening
- event deserialization
- delegation to processing service
- retry-safe event handling
- structured logging

---

# Flow

```text
Kafka Broker
      |
      v
NotificationConsumer
      |
      v
NotificationProcessingService
```

---

# 2. Notification Processing Layer

# Main Component

```text
NotificationProcessingService
```

---

# Purpose

Acts as:

```text
Central notification orchestration engine
```

---

# Responsibilities

| Responsibility | Purpose |
|---|---|
| Event Validation | Safe processing |
| Notification Routing | Channel selection |
| Sender Delegation | Delivery execution |
| Failure Handling | Retry & persistence |
| Observability | Metrics & logs |

---

# Processing Flow

```text
Kafka Event
      |
      v
Validation
      |
      v
NotificationSender
      |
      v
EmailNotificationSender
```

---

# 3. Communication Layer

# Main Components

```text
NotificationSender
EmailNotificationSender
SesEmailClient
```

---

# Purpose

Provides:

```text
Abstracted communication delivery
```

---

# Responsibilities

| Component | Responsibility |
|---|---|
| NotificationSender | Generic notification contract |
| EmailNotificationSender | Email notification implementation |
| SesEmailClient | AWS SES integration |

---

# Email Delivery Flow

```text
NotificationProcessingService
        |
        v
EmailNotificationSender
        |
        v
SesEmailClient
        |
        v
AWS SES
```

---

# Why Abstraction Exists

Supports future channels:

- SMS
- push notifications
- WhatsApp
- in-app notifications

without changing orchestration logic.

---

# 4. Failure Recovery Layer

# Main Component

```text
FailedNotificationEventService
```

---

# Purpose

Provides:

```text
Notification failure persistence and recovery
```

---

# Failure Flow

```text
Notification Failure
        |
        v
FailedNotificationEventService
        |
        v
Persistence / Logging
```

---

# Why This Matters

Ensures:

```text
No notification failure disappears silently
```

---

# 5. Security Layer

# Main Components

```text
NotificationSecurityConfig
JwtAuthConverter
```

---

# Purpose

Provides:

- JWT authentication
- role-based authorization
- stateless security
- OAuth2 resource-server validation

---

# Security Flow

```text
HTTP Request
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

# 6. REST Operations Layer

# Main Components

```text
NotificationController
NotificationOperationsService
```

---

# Purpose

Provides:

- operational notification APIs
- health operations
- failure investigation APIs
- monitoring support

---

# Operational Flow

```text
REST Request
      |
      v
NotificationController
      |
      v
NotificationOperationsService
```

---

# Distributed-System Interaction

```text
transaction-service
        |
        v
Kafka Notification Event
        |
        v
notification-service
        |
        v
AWS SES
        |
        v
End User Email
```

---

# Architectural Characteristics

| Characteristic | Status |
|---|---|
| Event-Driven | YES |
| Async Communication | YES |
| Distributed Safe | YES |
| Fault Isolated | YES |
| Cloud-Native | YES |
| AWS Integrated | YES |

---

# Final Summary

The `notification-service` architecture implements:

```text
Production-grade asynchronous distributed communication architecture
```

through:

- Kafka-driven event ingestion
- abstracted notification delivery
- AWS SES integration
- operational failure recovery
- JWT-secured APIs
- distributed observability

The architecture mirrors communication platforms used in:

- fintech systems
- banking notification platforms
- enterprise event-driven systems
- cloud-native distributed applications