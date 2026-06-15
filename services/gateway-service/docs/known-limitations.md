# Known Limitations

# Overview

This document captures current limitations of the Gateway Service implementation.

These limitations are acknowledged architectural gaps planned for future improvement.

---

# Distributed Tracing Not Yet Implemented

Current observability includes:
- correlation IDs
- structured logs
- Prometheus metrics

Missing:
- OpenTelemetry
- Jaeger
- Tempo
- Zipkin

Impact:
- no true end-to-end distributed tracing

---

# No Service Discovery

Current routing uses:
```yaml
hardcoded service URIs
```

Example:

```yaml
uri: http://localhost:8081
```

Missing:
- Eureka
- Consul
- Kubernetes service discovery

Impact:
- limited dynamic scalability

---

# No Centralized Config Server

Configuration currently uses:
- application.yaml
- environment variables

Missing:
- Spring Cloud Config Server
- centralized config management

Impact:
- config synchronization must be manual

---

# Docker Deployment Not Fully Implemented

Architecture is Docker-ready but:
- final Dockerfiles not created
- Compose orchestration not finalized

Impact:
- local orchestration still manual

---

# Kubernetes Manifests Not Implemented

Readiness/liveness probes exist but:
- manifests
- Helm charts
- ingress configs

are not implemented yet.

---

# No Distributed Cache Strategy

Redis currently used only for:
- rate limiting

Missing:
- distributed caching
- shared cache invalidation

---

# No API Versioning Strategy

Current routes:
```text
/api/v1/**
```

Future versioning governance not implemented.

Impact:
- future breaking changes may require restructuring

---

# No Centralized Logging Platform

Current logging:
- console logs
- structured logs

Missing:
- ELK stack
- Loki
- centralized aggregation

Impact:
- multi-instance debugging harder

---

# No Load Testing Yet

Gateway not yet validated under:
- high concurrency
- production-scale traffic

Missing:
- JMeter
- Gatling
- k6 testing

---

# No Chaos Testing

Failure simulation not implemented for:
- network failures
- Redis failures
- downstream instability

---

# Limited Automated Testing

Current testing primarily:
- Postman-based
- manual integration testing

Missing:
- integration tests
- reactive test suites
- Testcontainers
- contract tests

---

# No mTLS

Current service security uses:
- JWT
- audience validation

Missing:
- mutual TLS

Impact:
- service identity still token-based only

---

# No WAF Integration

Gateway currently lacks:
- Web Application Firewall
- advanced traffic inspection

---

# No API Analytics

Missing:
- request analytics
- usage dashboards
- API monetization support

---

# No Dynamic Route Management

Routes currently configured statically in:
```yaml
application.yaml
```

Missing:
- dynamic route registration
- centralized route management

---

# No Multi-Region Strategy

Current architecture assumes:
- single-region deployment

Missing:
- geo-redundancy
- failover routing

---

# Limited Operational Automation

Missing:
- CI/CD pipelines
- automated rollback
- deployment automation

---

# No Service Mesh

Missing:
- Istio
- Linkerd
- Envoy mesh integration

Impact:
- advanced traffic shaping unavailable

---

# Summary

The gateway currently demonstrates:
- strong foundational architecture
- production-grade patterns
- cloud-ready design

But still lacks:
- enterprise-scale operational infrastructure
- advanced distributed systems tooling