# Observability Documentation

# Overview

The Gateway Service includes foundational observability capabilities required for distributed systems and cloud-native deployments.

Implemented observability features:
- structured request logging
- correlation IDs
- health endpoints
- Prometheus metrics
- readiness/liveness probes

The observability layer enables:
- monitoring
- debugging
- traffic analysis
- operational diagnostics

---

# Observability Goals

The gateway observability architecture is designed to:
- trace requests across services
- diagnose failures quickly
- expose operational metrics
- support Kubernetes health checks
- enable future distributed tracing

---

# Correlation ID Architecture

Every request receives:

```text
X-Correlation-ID
```

Purpose:
- distributed request tracing
- log correlation
- debugging
- incident analysis

---

# CorrelationIdFilter

Implemented using:

```text
CorrelationIdFilter
```

Responsibilities:
- generate UUID
- attach correlation ID to request
- inject into response headers
- store inside MDC logging context

---

# Correlation ID Flow

```text
Incoming Request
  ↓
CorrelationIdFilter
  ↓
UUID Generated
  ↓
MDC Context Updated
  ↓
Request Routed
  ↓
Logs Correlated
```

---

# Example Header

```http
X-Correlation-ID: 51294e84-4a10-4b4a-8bc9-c8a0e3d18826
```

---

# Structured Logging

Structured logging is implemented using:

```text
RequestLoggingFilter
```

Purpose:
- operational visibility
- traffic diagnostics
- latency analysis
- debugging

---

# Logged Fields

The gateway logs:
- timestamp
- HTTP method
- request path
- response status
- latency
- authenticated user
- correlation ID
- client IP

---

# Example Log

```text
API_GATEWAY_REQUEST_LOG {
  timestamp=2026-05-26T08:54:37.172270Z,
  method=GET,
  path=/risk/api/v1/decisions,
  status=401,
  latencyMs=19,
  correlationId=Co-relationId:b82cb555-3faf-4f5a-a9e1-9c7548892df3,
  user=user1,
  clientIp=0:0:0:0:0:0:0:1
}
```

---

# Logging Goals

The logging strategy prioritizes:
- traceability
- low debugging time
- distributed diagnostics
- operational observability

---

# Spring Actuator

Spring Boot Actuator exposes operational endpoints.

Enabled endpoints:
- health
- metrics
- prometheus

---

# Health Endpoint

```http
GET /actuator/health
```

Purpose:
- service health verification
- uptime checks
- load balancer checks

---

# Health Response Example

```json
{
  "status": "UP"
}
```

---

# Liveness Probe

```http
GET /actuator/health/liveness
```

Purpose:
- Kubernetes liveness probe
- container restart decisions

---

# Readiness Probe

```http
GET /actuator/health/readiness
```

Purpose:
- traffic readiness validation
- deployment orchestration

---

# Prometheus Metrics

Metrics endpoint:

```http
GET /actuator/prometheus
```

Purpose:
- Prometheus scraping
- monitoring dashboards
- operational analytics

---

# Metrics Collected

The gateway exposes:
- request count
- request latency
- HTTP status metrics
- JVM metrics
- memory metrics
- thread metrics
- gateway traffic metrics

---

# Gateway Metrics Goals

Metrics help analyze:
- throughput
- latency
- traffic patterns
- failure rates
- downstream instability

---

# Monitoring Strategy

The observability strategy supports:
- reactive diagnostics
- distributed systems monitoring
- cloud-native operations

---

# Reactive Observability

Because the gateway uses:
```text
Spring WebFlux
```

observability must support:
- asynchronous request handling
- non-blocking execution
- reactive pipelines

Correlation IDs become critical in reactive systems because:
```text
requests do not remain on a single thread
```

---

# Security Observability

The gateway logs:
- unauthorized access attempts
- forbidden access attempts
- rate-limit violations
- downstream failures

Benefits:
- incident response
- abuse detection
- forensic analysis

---

# Future Observability Improvements

Planned:
- OpenTelemetry
- Jaeger
- Tempo
- Zipkin
- Grafana dashboards
- centralized logging
- ELK stack integration

---

# Distributed Tracing Roadmap

Planned architecture:

```text
Client
  ↓
Gateway
  ↓
OpenTelemetry Trace
  ↓
Microservices
  ↓
Jaeger/Tempo
```

Benefits:
- end-to-end tracing
- latency diagnostics
- service dependency visualization

---

# Cloud Readiness

The observability stack is designed for:
- Kubernetes
- AWS ECS/EKS
- distributed deployments
- container orchestration

Readiness/liveness probes enable:
- auto-healing
- rolling deployments
- load balancer integration