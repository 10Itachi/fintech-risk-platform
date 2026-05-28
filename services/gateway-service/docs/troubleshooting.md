# Troubleshooting Documentation

# Overview

This document captures major implementation issues encountered during development of the Gateway Service.

It includes:
- root causes
- debugging observations
- fixes
- lessons learned

---

# Bean Conflict Issue

## Error

```text
The bean 'ipKeyResolver' could not be registered.
A bean with that name has already been defined.
```

---

# Root Cause

Multiple `KeyResolver` beans used the same bean name.

Spring Boot does not allow duplicate bean names by default.

---

# Failed Attempts

Attempted:
- re-running application
- changing YAML references only

Did not fix issue because:
```text
actual bean names still conflicted
```

---

# Final Fix

Created centralized:

```text
RateLimiterConfig
```

with:
- userKeyResolver
- ipKeyResolver

Used:
```java
@Primary
```

for default resolver.

---

# Lesson Learned

Reactive gateway infrastructure heavily depends on:
- bean uniqueness
- bean naming consistency

---

# Security Header Issue

## Problem

Security headers were missing in Postman.

Observed:
- response headers appeared empty

---

# Root Cause

Headers were hidden inside:
```text
hidden headers
```

section in Postman.

Additionally:
- duplicate headers appeared due to multiple header writers.

---

# Observed Headers

```http
X-Frame-Options
X-Content-Type-Options
Cache-Control
Referrer-Policy
```

---

# Final Fix

Implemented:
- Spring Security headers
- SecurityHeadersFilter verification

Validated using:
- Postman hidden headers
- browser inspection

---

# Lesson Learned

Postman UI can hide:
- framework-generated headers
- duplicated response headers

Always inspect:
```text
full response headers
```

---

# ROLE_ADMIN Authorization Issue

## Problem

Admin JWT still returned:
```http
401 Unauthorized
```

when accessing:
```http
/risk/api/**
```

---

# Root Cause

The issue was NOT:
- gateway authorization
- role extraction
- route predicates

Actual issue:
```text
audience validation failure in risk-service
```

---

# Observed Behavior

Gateway logs showed:
- request authenticated
- user identified correctly
- request routed

But downstream service rejected JWT.

---

# Debugging Discovery

JWT contained:

```json
"aud": [
  "user-service",
  "account"
]
```

but risk-service expected:

```json
"risk-decision-service"
```

---

# Final Fix

Configured:
```text
Keycloak Audience Mapper
```

for:
```text
risk-decision-service
```

---

# Lesson Learned

Gateway authentication success does NOT guarantee:
```text
downstream authorization success
```

Sensitive services may implement:
- audience validation
- azp validation
- service identity validation

---

# JWT Audience Confusion

## Problem

Audience mapper existed in Keycloak but JWT still lacked:
```json
risk-decision-service
```

---

# Root Cause

Discovered:
- mapper configuration mismatch
- audience propagation behavior misunderstanding

Also learned:
```text
Keycloak automatically injects 'account'
```

as built-in audience.

---

# Important Discovery

Mapper NAME does NOT matter.

Critical field:
```text
Included Client Audience
```

must match expected audience exactly.

---

# Lesson Learned

Keycloak audience configuration is:
```text
extremely sensitive to exact client IDs
```

---

# Instant Serialization Failure

## Problem

Gateway returned:
```http
500 Internal Server Error
```

during fallback/error handling.

---

# Root Cause

Custom error serialization attempted to serialize:
```java
Instant
```

without:
```text
JavaTimeModule
```

support.

---

# Failed Attempt

Added dependency:

```xml
jackson-datatype-jsr310
```

alone.

Issue persisted because:
```text
custom ObjectMapper bypassed Spring configuration
```

---

# Final Fix

Used:
```text
Spring-managed ObjectMapper
```

instead of manually instantiated mapper.

---

# Lesson Learned

Spring Boot auto-configures:
- JavaTimeModule
- serializers
- JSON support

Manually creating ObjectMapper can bypass:
```text
framework auto-configuration
```

---

# Placeholder Resolution Failure

## Error

```text
Could not resolve placeholder 'SPRING_APPLICATION_NAME'
```

during tests.

---

# Root Cause

`.env` values were not available during:
```text
@SpringBootTest bootstrap phase
```

Gateway startup accessed:
```yaml
spring.application.name
```

very early in bootstrap lifecycle.

---

# Why Other Services Worked

Other services:
- simpler bootstrap
- fewer early placeholders
- less reactive infrastructure

Gateway initialized:
- metrics
- WebFlux
- gateway infra
- logging
- actuator

much earlier.

---

# Final Fix

Used fallback defaults:

```yaml
${SPRING_APPLICATION_NAME:gateway-service}
```

---

# Lesson Learned

Spring Boot does NOT automatically load:
```text
.env
```

without external tooling/plugins.

---

# Retry Strategy Bug Risk

## Potential Problem

Retries on:
- POST
- PUT
- PATCH

could create:
- duplicate payments
- duplicate transactions

---

# Final Design Decision

Retries enabled ONLY for:
```text
GET requests
```

---

# Lesson Learned

Financial systems require:
```text
idempotency awareness
```

before retries are implemented.

---

# Circuit Breaker Warning

## Warning

```text
No timeLimiterConfig found
```

---

# Root Cause

Resilience4j used:
```text
default timeout configuration
```

because no explicit TimeLimiter config existed.

---

# Final Decision

Warning accepted temporarily.

Reason:
- circuit breaker still functional
- timeout fallback defaults acceptable

---

# Lesson Learned

Resilience4j components:
- CircuitBreaker
- TimeLimiter

are separate configurations.

---

# YAML Deprecated Property Warnings

## Warning

Deprecated gateway properties:
- connect-timeout
- response-timeout

---

# Root Cause

Spring Cloud Gateway configuration keys changed.

---

# Final Fix

Updated to:
```yaml
spring.cloud.gateway.server.webflux.httpclient
```

and:
```yaml
spring.http.codecs.max-in-memory-size
```

---

# Lesson Learned

Spring ecosystem upgrades frequently:
- rename properties
- deprecate configurations
- change reactive infrastructure paths

---

# Correlation ID Logging Issue

## Problem

Correlation ID appeared duplicated:

```text
Co-relationId:Co-relationId:uuid
```

---

# Root Cause

Header prefix added:
- both in filter
- and logging pattern

---

# Final Fix

Standardized:
```text
raw UUID storage
```

inside MDC.

---

# Lesson Learned

Structured logging requires:
```text
strict log formatting consistency
```

---

# Reactive Debugging Complexity

## Observation

Reactive request flows are harder to debug because:
- requests jump threads
- execution is asynchronous
- stack traces become fragmented

---

# Final Mitigation

Implemented:
- correlation IDs
- structured logs
- centralized error handling

---

# Overall Engineering Lessons

Major lessons learned:
- reactive systems require strong observability
- JWT audience validation is complex
- gateway security differs from service security
- retries must consider idempotency
- centralized gateway architecture simplifies downstream services
- distributed systems require resilience-first thinking