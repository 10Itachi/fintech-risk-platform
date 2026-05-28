# observability.md

# Observability Architecture

# Overview

The `transaction-service` implements:

```text
Production-grade observability architecture
```

to provide:

- distributed tracing
- operational visibility
- request correlation
- latency monitoring
- failure diagnostics
- system health insights

The observability stack is designed for:

- microservice debugging
- production monitoring
- incident investigation
- SLA tracking
- operational analytics

---

# Observability Goals

| Goal | Purpose |
|---|---|
| Distributed Tracing | Cross-service request visibility |
| Metrics Collection | Performance monitoring |
| Structured Logging | Operational debugging |
| Correlation Tracking | Request reconstruction |
| Failure Visibility | Root-cause analysis |
| SLA Monitoring | Latency tracking |

---

# High-Level Observability Architecture

```text
Incoming Request
        |
        v
Correlation ID
        |
        v
Structured Logging
        |
        v
Micrometer Metrics
        |
        v
Feign Trace Propagation
        |
        v
Kafka Event Tracking
        |
        v
Prometheus / Grafana
```

---

# Core Observability Components

| Component | Responsibility |
|---|---|
| Micrometer | Metrics collection |
| SLF4J Logging | Structured logs |
| Correlation IDs | Distributed tracing |
| FeignCorrelationConfig | Cross-service trace propagation |
| Timers & Counters | Latency analytics |
| MDC Context | Request-scoped logging |

---

# ==========================================
# 1. Correlation ID Architecture
# ==========================================

# Purpose

Provides:

```text
End-to-end distributed request tracing
```

across:

- controllers
- services
- Feign calls
- Kafka workflows

---

# Why Correlation IDs Matter

Without correlation IDs:

```text
Distributed debugging becomes extremely difficult
```

---

# High-Level Flow

```text
Incoming Request
        |
        v
Generate/Read Correlation ID
        |
        v
MDC Context
        |
        v
Logs + Metrics + Feign Headers
```

---

# Header Used

```text
X-Correlation-Id
```

---

# Example Flow

```text
Client Request
CorrelationId = corr-9911
        |
        v
transaction-service logs
        |
        v
Feign request
        |
        v
risk-decision-service logs
```

All logs linked using SAME correlation ID.

---

# Why MDC Is Important

Correlation IDs stored in:

```text
Mapped Diagnostic Context (MDC)
```

allowing automatic inclusion in logs.

---

# Logging Pattern

```yaml
[%X{correlationId}]
```

---

# Example Log

```text
2026-05-18 10:30:00 [corr-9911] INFO TransactionOrchestratorService - transaction_created
```

---

# Benefits

| Benefit | Purpose |
|---|---|
| Distributed Tracing | Cross-service visibility |
| Easier Debugging | Request reconstruction |
| Production Support | Faster issue diagnosis |
| Auditability | Traceable workflows |

---

# ==========================================
# 2. Structured Logging Architecture
# ==========================================

# Purpose

Provides:

```text
Production-readable operational logs
```

---

# Logging Characteristics

| Characteristic | Purpose |
|---|---|
| Correlation-Aware | Distributed tracing |
| Structured | Easier parsing |
| Contextual | Rich operational info |
| Consistent | Predictable debugging |

---

# Logging Layers

| Layer | Example Logs |
|---|---|
| Controller | Request entry |
| Orchestration | Workflow progression |
| Redis | Lock acquisition |
| Feign | Fraud-service calls |
| Kafka | Event publishing |
| Failure Handling | Exceptions |

---

# Example Orchestration Logs

```text
transaction_creation_started
duplicate_transaction_request
risk_service_invocation_started
transaction_finalized
outbox_event_published
```

---

# Why Structured Logging Matters

Enables:

- log aggregation
- distributed debugging
- observability tooling
- production monitoring

---

# ==========================================
# 3. Micrometer Metrics Architecture
# ==========================================

# Purpose

Provides:

```text
Application performance metrics
```

---

# Main Metrics Types

| Type | Purpose |
|---|---|
| Counters | Event frequency |
| Timers | Latency tracking |
| Gauges | Runtime values |

---

# High-Level Metrics Flow

```text
Application Event
        |
        v
Micrometer Metrics
        |
        v
Prometheus
        |
        v
Grafana Dashboards
```

---

# ==========================================
# 4. Request Counters
# ==========================================

# Purpose

Tracks:

```text
Transaction processing volume
```

---

# Example Metrics

```java
transaction.create.request
transaction.completed
transaction.failure
transaction.duplicate
```

---

# Why Counters Matter

Used for:

- traffic analytics
- SLA reporting
- operational dashboards
- incident monitoring

---

# Example Flow

```text
Incoming Transaction
        |
        v
Counter Increment
```

---

# ==========================================
# 5. Latency Metrics
# ==========================================

# Purpose

Measures:

```text
Performance characteristics
```

of critical workflows.

---

# Main Timers

| Timer | Purpose |
|---|---|
| transaction.orchestration.latency | Full workflow latency |
| transaction.risk.latency | Fraud-service latency |
| kafka.publish.latency | Kafka publishing time |

---

# Timer Flow

```text
Start Timer
      |
      v
Business Operation
      |
      v
Stop Timer
```

---

# Why Latency Tracking Matters

Enables:

- SLA validation
- bottleneck detection
- performance optimization
- operational alerting

---

# Example Metrics

```text
P50 latency
P95 latency
P99 latency
Average latency
```

---

# ==========================================
# 6. Feign Observability
# ==========================================

# Purpose

Tracks:

```text
Cross-service communication visibility
```

---

# Feign Observability Flow

```text
Transaction Service
        |
        v
Feign Request
        |
        v
Correlation ID Propagation
        |
        v
risk-decision-service
```

---

# Feign Metrics

| Metric | Purpose |
|---|---|
| risk-service latency | Fraud-engine performance |
| retry count | Retry visibility |
| timeout count | Downstream instability |
| failure count | Availability tracking |

---

# Why Feign Observability Matters

Fraud evaluation is:

```text
Transaction-critical
```

Monitoring its latency is extremely important.

---

# ==========================================
# 7. Kafka Observability
# ==========================================

# Purpose

Tracks:

```text
Asynchronous event reliability
```

---

# Kafka Metrics

| Metric | Purpose |
|---|---|
| outbox.pending.count | Queue backlog |
| outbox.failed.count | Publish failures |
| kafka.publish.success | Successful publishes |
| kafka.publish.failure | Failed publishes |

---

# Kafka Flow

```text
Outbox Event
      |
      v
Kafka Publisher
      |
      v
Metrics Recorded
```

---

# Why Kafka Observability Matters

Detects:

- stuck events
- retry storms
- Kafka outages
- delayed publishing

---

# ==========================================
# 8. Failure Observability
# ==========================================

# Purpose

Provides:

```text
Production failure visibility
```

---

# Failure Metrics

| Metric | Purpose |
|---|---|
| transaction.failure | Transaction failures |
| redis.failure | Redis instability |
| feign.timeout | Fraud-service issues |
| kafka.publish.failure | Event failures |

---

# Failure Flow

```text
Exception
      |
      v
Structured Log
      |
      v
Failure Counter
      |
      v
Alerting Systems
```

---

# Why Failure Metrics Matter

Enables:

- operational alerts
- root-cause analysis
- incident detection
- reliability monitoring

---

# ==========================================
# 9. Health Monitoring
# ==========================================

# Purpose

Provides:

```text
Infrastructure health visibility
```

---

# Monitored Dependencies

| Dependency | Purpose |
|---|---|
| MySQL | Persistence availability |
| Redis | Idempotency availability |
| Kafka | Messaging availability |
| Feign/Risk Service | Fraud-system health |

---

# Health Flow

```text
Health Endpoint
      |
      v
Dependency Checks
      |
      v
Aggregated Health Status
```

---

# Example States

| State | Meaning |
|---|---|
| UP | Healthy |
| DOWN | Failure detected |
| DEGRADED | Partial issue |

---

# ==========================================
# 10. Distributed Tracing Architecture
# ==========================================

# Purpose

Tracks:

```text
Full request lifecycle
```

across services.

---

# Trace Flow

```text
Client Request
        |
        v
transaction-service
        |
        v
Feign Request
        |
        v
risk-decision-service
        |
        v
Kafka Event
```

---

# Why Distributed Tracing Matters

Enables:

- end-to-end debugging
- latency decomposition
- request reconstruction
- distributed analytics

---

# ==========================================
# 11. Operational Visibility
# ==========================================

# Main Operational Insights

| Insight | Purpose |
|---|---|
| Transaction Throughput | Traffic visibility |
| Fraud Latency | Risk-service performance |
| Retry Rates | Infrastructure stability |
| Duplicate Requests | Client retry analysis |
| Kafka Backlog | Async health |
| Failure Rates | Reliability tracking |

---

# ==========================================
# 12. Monitoring Stack
# ==========================================

# Technologies

| Technology | Purpose |
|---|---|
| Micrometer | Metrics instrumentation |
| Prometheus | Metrics scraping |
| Grafana | Dashboards |
| SLF4J | Logging |
| MDC | Correlation tracking |

---

# Monitoring Architecture

```text
Application Metrics
        |
        v
Micrometer
        |
        v
Prometheus
        |
        v
Grafana Dashboards
```

---

# Recommended Dashboards

| Dashboard | Purpose |
|---|---|
| Transaction Latency | SLA monitoring |
| Fraud-Service Health | Feign visibility |
| Kafka Reliability | Async monitoring |
| Redis Health | Idempotency visibility |
| Failure Rates | Operational incidents |

---

# Observability Characteristics

| Characteristic | Status |
|---|---|
| Distributed Traceability | YES |
| Correlation-Aware | YES |
| Metrics-Driven | YES |
| Structured Logging | YES |
| Production Monitoring | YES |
| SLA Visibility | YES |

---

# Why This Architecture Is Enterprise-Grade

This setup demonstrates observability patterns used in:

- banking systems
- fintech platforms
- distributed transaction systems
- cloud-native microservices

including:

- correlation tracing
- distributed metrics
- structured logging
- operational analytics
- SLA monitoring

---

# Future Enhancements

- OpenTelemetry integration
- Jaeger distributed tracing
- centralized ELK logging
- Grafana alerting
- distributed span tracing
- Kafka consumer observability

---

# Final Summary

The observability architecture inside:

```text
transaction-service
```

implements:

```text
Production-grade distributed-system visibility
```

through:

- correlation-aware logging
- Micrometer metrics
- distributed tracing
- Kafka observability
- failure analytics
- SLA monitoring

The system is designed for:

```text
Operational transparency,
production debugging,
and distributed-system monitoring
```