# Observability Documentation

## 1. Overview

The User Service implements observability across three pillars:

* Logging (structured logs with correlation IDs)
* Metrics (Micrometer + Prometheus)
* Visualization and monitoring (Grafana dashboards)

The system is instrumented to provide visibility into API performance, cache efficiency, and external dependency behavior.

---

## 2. Logging

### Implementation

* Logging via SLF4J
* Correlation using MDC (traceId)

Example log pattern:

```text id="obs-log"
traceId=abc123 | action=createUser | status=success
```

### Purpose

* Correlate logs across service boundaries
* Debug request flows
* Support centralized logging systems (ELK or cloud logging)

---

## 3. Metrics (Micrometer + Prometheus)

### Instrumentation

The service uses Micrometer to expose metrics via the Spring Boot Actuator endpoint:

```text id="obs-metrics-endpoint"
/actuator/prometheus
```

Metrics are automatically exposed in Prometheus format.

---

### Key Metrics

* HTTP request latency
* Request count per endpoint
* Error rates (4xx/5xx)
* Cache hits and misses
* External API latency (Keycloak calls)

---

### Custom Metrics

User-related metrics are tracked via MeterRegistry, for example:

* User creation count
* User activation/deactivation events

---

## 4. Prometheus Integration

### Configuration

Prometheus is configured to scrape metrics from the service:

```text id="obs-prom-flow"
Prometheus → /actuator/prometheus → User Service
```

### docker-compose Integration

Prometheus is deployed alongside the service using docker-compose and configured via environment variables in `.env`.

Example responsibilities:

* Scrape interval configuration
* Target service registration
* Metrics storage

---

## 5. Grafana Integration

### Purpose

Grafana is used for:

* Metrics visualization
* Dashboard creation
* Real-time monitoring

---

### Data Source

Grafana is connected to Prometheus as its data source.

---

### Dashboards (Recommended)

* API Latency Dashboard
* Request Throughput Dashboard
* Error Rate Dashboard
* Cache Hit/Miss Dashboard
* Keycloak Integration Latency

---

### Example Flow

```text id="obs-grafana-flow"
User Service → Prometheus → Grafana Dashboard
```

---

## 6. Cache Observability

Metrics include:

* Cache hit count
* Cache miss count
* Cache eviction count

Purpose:

* Evaluate caching effectiveness
* Identify performance bottlenecks

---

## 7. External Dependency Monitoring

Keycloak integration is monitored via:

* Request latency
* Failure rate
* Circuit breaker state (open/closed)

This helps identify:

* Downstream failures
* Network latency issues
* External system instability

---

## 8. Tracing

### Current State

* Trace identifiers propagated using MDC

---

### Future Enhancement

* Integration with OpenTelemetry
* Visualization via Jaeger or Zipkin

---

### Flow

```text id="obs-trace-flow"
Request → User Service → Keycloak → Response
         ↓ traceId propagated across layers
```

---

## 9. Alerting Strategy

Alerts should be configured for:

* High error rate (e.g., >5%)
* High latency (e.g., >2 seconds)
* Circuit breaker open state
* Cache failure or high miss ratio
* Keycloak timeout/failure spikes

---

## 10. Configuration Overview

Observability components are configured via:

* `application.yml` (Actuator and metrics exposure)
* `.env` (environment variables for services)
* `docker-compose.yml` (Prometheus and Grafana setup)

---

## 11. Monitoring Goals

* Ensure system reliability
* Detect failures early
* Measure performance trends
* Provide operational visibility

---

## 12. Summary

The system provides a production-ready observability setup:

* Structured logging with trace correlation
* Metrics exposure via Micrometer
* Metrics collection via Prometheus
* Visualization using Grafana

This enables effective monitoring, debugging, and performance tuning in a distributed environment.
