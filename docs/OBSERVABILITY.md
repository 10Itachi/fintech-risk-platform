# OBSERVABILITY.md

# Gringotts Fraud Platform — Observability Architecture

## Overview

The Gringotts Fraud Platform implements a production-style observability architecture designed for:

* distributed microservice visibility
* operational diagnostics
* request tracing
* SLA monitoring
* failure analytics
* infrastructure monitoring
* asynchronous event visibility

The platform follows the three pillars of observability:

| Pillar     | Implementation                       |
| ---------- | ------------------------------------ |
| Metrics    | Micrometer + Prometheus              |
| Logging    | Structured Logging + Correlation IDs |
| Monitoring | Grafana Dashboards                   |

---

# Observability Goals

The platform observability architecture is designed to provide:

* distributed request tracing
* end-to-end transaction visibility
* asynchronous Kafka observability
* fraud-decision analytics
* operational debugging
* infrastructure health visibility
* performance monitoring
* SLA instrumentation
* failure diagnostics
* cloud-native operational readiness

---

# High-Level Observability Architecture

```text id="ecyxz2"
Client Request
        |
        v
Gateway Correlation Filter
        |
        v
Microservice Structured Logging
        |
        v
Micrometer Metrics
        |
        v
Prometheus Scraping
        |
        v
Grafana Dashboards
```

---

# Core Observability Stack

| Component           | Technology           |
| ------------------- | -------------------- |
| Metrics             | Micrometer           |
| Monitoring          | Prometheus           |
| Dashboards          | Grafana              |
| Logging             | SLF4J + Logback      |
| Correlation         | MDC                  |
| Health Monitoring   | Spring Boot Actuator |
| Distributed Tracing | Correlation IDs      |
| Async Observability | Kafka Metrics        |

---

# Distributed Correlation Architecture

## Overview

All distributed requests propagate:

```text id="wy9s5x"
X-Correlation-Id
```

Purpose:

* distributed tracing
* request reconstruction
* cross-service debugging
* incident investigation
* operational traceability

---

# Correlation Flow

```text id="jnyqvt"
Client Request
        |
        v
Gateway Service
        |
        v
CorrelationIdFilter
        |
        v
Feign/Kafka Propagation
        |
        v
Downstream Services
        |
        v
Correlated Logs
```

---

# Why Correlation IDs Matter

Without distributed correlation:

```text id="u2by5m"
debugging distributed systems becomes extremely difficult
```

Correlation IDs allow operators to trace:

* synchronous workflows
* asynchronous Kafka events
* fraud-decision flows
* retry propagation
* downstream failures

---

# Structured Logging Architecture

## Overview

All services implement:

```text id="k57g29"
structured production-grade logging
```

Logging characteristics:

| Characteristic | Purpose                     |
| -------------- | --------------------------- |
| Structured     | Searchable logs             |
| Correlated     | Distributed tracing         |
| Contextual     | Easier debugging            |
| Event-Oriented | Operational analytics       |
| Consistent     | Predictable troubleshooting |

---

# Common Logged Data

| Field         | Purpose                 |
| ------------- | ----------------------- |
| timestamp     | event timing            |
| correlationId | distributed tracing     |
| transactionId | business traceability   |
| request path  | API visibility          |
| status code   | operational diagnostics |
| latency       | SLA monitoring          |
| user identity | operational context     |

---

# Example Structured Log

```text id="9u6gsl"
event=transaction_created
correlationId=corr-9911
transactionId=txn-101
status=APPROVED
latencyMs=42
```

---

# Metrics Architecture

## Overview

The platform uses:

```text id="cmmf0w"
Micrometer + Prometheus
```

for metrics instrumentation and collection.

---

# Metrics Flow

```text id="kzy8tw"
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

# Metrics Categories

| Category        | Purpose                      |
| --------------- | ---------------------------- |
| Request Metrics | API throughput               |
| Latency Metrics | SLA visibility               |
| Failure Metrics | Reliability tracking         |
| Kafka Metrics   | Async observability          |
| Redis Metrics   | Cache/idempotency visibility |
| Fraud Metrics   | ML analytics                 |
| Export Metrics  | Reporting visibility         |
| JVM Metrics     | Runtime monitoring           |

---

# Common Metrics

## Request Metrics

Tracks:

* request throughput
* API traffic
* request volume

Examples:

```text id="x3v9kg"
transaction.create.request
user.create.request
risk.evaluate.request
```

---

## Latency Metrics

Measures:

* end-to-end processing latency
* fraud-service latency
* Kafka publish latency
* export latency
* database write latency

Supports:

```text id="st4s3r"
P50, P95, P99 latency visibility
```

---

## Failure Metrics

Tracks:

* Kafka failures
* fraud-service failures
* Redis failures
* notification failures
* export failures

Purpose:

* operational alerting
* incident visibility
* root-cause analysis

---

# Gateway Observability

## Overview

The Gateway Service acts as the centralized observability entry point.

Responsibilities:

* request correlation
* traffic logging
* latency instrumentation
* security visibility
* rate-limit monitoring

---

# Gateway Observability Features

| Feature               | Purpose                 |
| --------------------- | ----------------------- |
| CorrelationIdFilter   | request tracing         |
| RequestLoggingFilter  | structured traffic logs |
| Rate-Limit Visibility | abuse monitoring        |
| Health Endpoints      | operational readiness   |
| Prometheus Metrics    | gateway monitoring      |

---

# Gateway Metrics

Tracks:

* request count
* response latency
* HTTP status distribution
* downstream failures
* rate-limit violations

---

# Transaction Service Observability

## Overview

The transaction-service implements:

```text id="sx3r7f"
production-grade distributed transaction observability
```

Capabilities include:

* distributed tracing
* orchestration visibility
* Kafka observability
* Feign instrumentation
* idempotency visibility

---

# Transaction Metrics

| Metric                | Purpose                     |
| --------------------- | --------------------------- |
| transaction.completed | successful transactions     |
| transaction.failure   | failed workflows            |
| transaction.duplicate | idempotency violations      |
| kafka.publish.success | successful async publishing |
| kafka.publish.failure | async failures              |

---

# Feign Observability

Tracks:

* fraud-service latency
* retry visibility
* timeout analytics
* downstream instability

---

# Kafka Observability

Tracks:

* outbox backlog
* publish failures
* retry storms
* duplicate deliveries

---

# Risk Decision Service Observability

## Overview

The risk-decision-service provides observability into:

* fraud scoring
* ML inference
* policy decisioning
* fraud analytics

---

# Risk Metrics

| Metric                | Purpose               |
| --------------------- | --------------------- |
| fraud decision count  | fraud analytics       |
| ML scoring latency    | model performance     |
| decision distribution | fraud visibility      |
| error rate            | operational stability |

---

# Risk Decision Logging

Logs include:

* transaction IDs
* fraud reason codes
* ML probabilities
* policy versions
* evaluation latency

---

# Notification Service Observability

## Overview

The notification-service implements:

```text id="k0v9up"
Kafka-driven notification observability
```

Capabilities include:

* email delivery visibility
* SES monitoring
* retry instrumentation
* notification failure analytics

---

# Notification Metrics

| Metric               | Purpose               |
| -------------------- | --------------------- |
| consumed events      | Kafka throughput      |
| failed notifications | delivery failures     |
| SES latency          | AWS performance       |
| retry count          | reliability analytics |

---

# Notification Logging

Example events:

```text id="qvx1k6"
event=notification_received
event=email_sent
event=notification_failed
```

---

# Transaction Observability Service Observability

## Overview

The transaction-observability-service acts as the:

```text id="p8s0tk"
centralized audit observability layer
```

Capabilities include:

* Kafka event visibility
* payload analytics
* export instrumentation
* audit persistence metrics
* failed-event monitoring

---

# Audit Metrics

| Metric           | Purpose                |
| ---------------- | ---------------------- |
| processedCounter | processed Kafka events |
| duplicateCounter | duplicate deliveries   |
| exportCounter    | successful exports     |
| failureCounter   | processing failures    |

---

# Payload Observability

Tracks:

* payload-size growth
* serialization anomalies
* memory pressure risks

Purpose:

* capacity planning
* operational forecasting
* anomaly detection

---

# Export Observability

Tracks:

* export latency
* export failures
* generated file size
* report-generation demand

---

# User Service Observability

## Overview

The user-service provides observability into:

* user operations
* Keycloak integration
* cache efficiency
* external dependency health

---

# User-Service Metrics

Tracks:

* user creation count
* request latency
* Keycloak latency
* cache hit/miss ratios
* error rates

---

# External Dependency Monitoring

Keycloak integration visibility includes:

* request latency
* failure rate
* circuit-breaker state
* timeout visibility

---

# Kafka Observability Architecture

## Overview

Kafka observability provides visibility into:

* asynchronous event pipelines
* retry storms
* duplicate deliveries
* processing latency
* consumer failures

---

# Kafka Observability Flow

```text id="j4v0xy"
Kafka Event
      |
      v
Consumer Processing
      |
      v
Structured Logs
      |
      v
Metrics Recording
      |
      v
Operational Dashboards
```

---

# Why Kafka Observability Matters

Enables operators to detect:

* event-processing instability
* broker failures
* retry amplification
* consumer lag
* duplicate-event spikes

---

# Health Monitoring

## Spring Boot Actuator

All services expose:

```http id="ymns9j"
/actuator/health
/actuator/prometheus
/actuator/metrics
```

---

# Health Characteristics

Tracks:

* MySQL availability
* Redis availability
* Kafka connectivity
* downstream-service health
* application readiness

---

# Kubernetes Readiness

Supports:

* liveness probes
* readiness probes
* rolling deployments
* auto-healing

---

# Prometheus Integration

## Overview

Prometheus scrapes metrics from all services.

Scrape targets include:

* gateway-service
* transaction-service
* risk-decision-service
* notification-service
* user-service
* transaction-observability-service

---

# Prometheus Responsibilities

| Responsibility        | Purpose                |
| --------------------- | ---------------------- |
| metrics scraping      | centralized collection |
| time-series storage   | historical analytics   |
| alert rule evaluation | incident detection     |

---

# Grafana Dashboards

## Recommended Dashboards

| Dashboard              | Purpose                |
| ---------------------- | ---------------------- |
| Transaction Throughput | traffic visibility     |
| Fraud Analytics        | risk monitoring        |
| Kafka Reliability      | async visibility       |
| API Latency            | SLA monitoring         |
| Failure Analytics      | operational debugging  |
| Export Monitoring      | report visibility      |
| Redis Health           | idempotency monitoring |

---

# SLA Monitoring

## Operational SLA Metrics

The platform tracks:

* request latency
* fraud-evaluation latency
* Kafka processing time
* export-generation time
* notification delivery time

Supports:

```text id="mcn8m8"
enterprise-grade SLA visibility
```

---

# Distributed Tracing Roadmap

## Planned Enhancements

Future observability improvements include:

* OpenTelemetry
* Jaeger
* Tempo
* Zipkin
* distributed spans
* trace visualization

---

# Planned Tracing Architecture

```text id="m0t6lr"
Client
   |
   v
Gateway
   |
   v
OpenTelemetry Trace
   |
   v
Microservices
   |
   v
Jaeger / Tempo
```

---

# Cloud-Native Readiness

The observability stack is designed for:

* Kubernetes
* AWS ECS/EKS
* Docker deployments
* distributed scaling
* container orchestration

---

# Enterprise Observability Characteristics

| Characteristic      | Status |
| ------------------- | ------ |
| Distributed Tracing | YES    |
| Structured Logging  | YES    |
| Kafka Visibility    | YES    |
| SLA Monitoring      | YES    |
| Metrics-Driven      | YES    |
| Failure Analytics   | YES    |
| Cloud-Native Ready  | YES    |

---

# Why This Architecture Is Enterprise-Grade

The observability setup demonstrates patterns used in:

* banking systems
* fintech platforms
* distributed event systems
* fraud-detection platforms
* cloud-native microservices

including:

* distributed tracing
* Kafka instrumentation
* structured operational logging
* SLA instrumentation
* async observability
* operational analytics

---

# Future Enhancements

Planned future improvements:

* OpenTelemetry tracing
* centralized ELK logging
* Kafka consumer-lag monitoring
* Grafana alerting
* distributed span analytics
* real-time anomaly detection
* cloud-native observability pipelines

---

# Final Summary

The Gringotts platform implements:

```text id="g0z95z"
Production-grade distributed observability architecture
```

through:

* structured logging
* correlation-aware tracing
* Micrometer metrics
* Prometheus monitoring
* Grafana dashboards
* Kafka observability
* SLA instrumentation
* operational analytics

The architecture is designed for:

```text id="nhz0q6"
enterprise-scale distributed financial systems
```

providing:

* operational transparency
* failure visibility
* distributed tracing
* production diagnostics
* scalable monitoring
