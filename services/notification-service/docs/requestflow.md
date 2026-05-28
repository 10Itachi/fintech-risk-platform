# request-flow.md

# Notification Service Request Flow

# Overview

This document explains the complete:

```text
End-to-end notification processing lifecycle
```

inside the:

```text
notification-service
```

---

# High-Level Request Flow

```text
transaction-service
        |
        v
Kafka Notification Event
        |
        v
NotificationConsumer
        |
        v
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
        |
        v
User Email Inbox
```

---

# 1. Kafka Event Creation

The flow begins when:

```text
transaction-service
```

publishes notification event.

---

# Example Events

- transaction approved
- transaction declined
- review initiated
- account notifications

---

# 2. NotificationConsumer Flow

# Step 1 — Kafka Event Received

Kafka broker delivers message to:

```java
@KafkaListener
```

inside:

```text
NotificationConsumer
```

---

# Step 2 — Structured Logging

Logs:

```text
event=notification_event_received
```

including:

- eventId
- transactionId
- notificationType

---

# Step 3 — Delegate To Processing Service

Calls:

```java
notificationProcessingService.process(...)
```

---

# Consumer Flow

```text
Kafka Event
      |
      v
NotificationConsumer
      |
      v
NotificationProcessingService
```

---

# 3. NotificationProcessingService Flow

# Purpose

Acts as:

```text
Central notification orchestration engine
```

---

# Step 1 — Validate Event

Validates:

- email
- payload
- notification type
- event integrity

---

# Step 2 — Initialize Metrics & Logging

Records:

- notification counters
- processing timers
- operational logs

---

# Step 3 — Determine Notification Channel

Routes request to:

```text
NotificationSender
```

---

# Step 4 — Send Notification

Calls:

```java
emailNotificationSender.send(...)
```

---

# Processing Flow

```text
Notification Event
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

# Step 5 — Success Logging

Logs:

```text
event=notification_sent_successfully
```

---

# 4. EmailNotificationSender Flow

# Purpose

Provides:

```text
Concrete email delivery implementation
```

---

# Step 1 — Build Email Request

Constructs:

- recipient
- subject
- email body
- metadata

---

# Step 2 — Delegate To SES Client

Calls:

```java
sesEmailClient.sendEmail(...)
```

---

# Flow

```text
EmailNotificationSender
        |
        v
SesEmailClient
```

---

# 5. SesEmailClient Flow

# Purpose

Acts as:

```text
AWS SES integration layer
```

---

# Step 1 — Build SES Request

Creates AWS SES SDK request.

---

# Step 2 — Invoke AWS SES

Calls:

```text
AWS SES API
```

---

# Step 3 — Receive SES Response

SES returns:

- message ID
- delivery acknowledgment
- failure exception

---

# SES Flow

```text
SesEmailClient
      |
      v
AWS SES
      |
      v
SMTP Infrastructure
      |
      v
User Inbox
```

---

# 6. Failure Flow

# If Email Sending Fails

```text
Exception
      |
      v
FailedNotificationEventService
```

---

# Failure Handling Steps

# Step 1 — Structured Error Logging

Logs:

```text
event=notification_send_failed
```

---

# Step 2 — Persist Failure Details

Stores:

- payload
- recipient
- error message
- timestamp

---

# Step 3 — Metrics Increment

Tracks:

- failure count
- retry metrics
- operational alerts

---

# Failure Flow

```text
Notification Failure
        |
        v
FailedNotificationEventService
        |
        v
Operational Recovery
```

---

# 7. REST Operational API Flow

# Request Entry

Client calls:

```http
GET /notifications/...
```

---

# Flow

```text
HTTP Request
      |
      v
NotificationController
      |
      v
NotificationOperationsService
```

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

# Complete End-to-End Flow

```text
transaction-service
        |
        v
Kafka Event
        |
        v
NotificationConsumer
        |
        v
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
        |
        v
User Email Inbox
```

---

# Operational Characteristics

| Characteristic | Status |
|---|---|
| Async Processing | YES |
| Event-Driven | YES |
| Failure Recovery | YES |
| JWT Secured | YES |
| AWS Integrated | YES |
| Distributed Ready | YES |

---

# Final Summary

The `notification-service` request flow implements:

```text
Production-grade asynchronous notification orchestration
```

through:

- Kafka-driven event consumption
- abstracted notification delivery
- AWS SES integration
- operational failure handling
- distributed observability
- secure REST operations

The system is designed for:

```text
Reliable distributed financial communication workflows
```
