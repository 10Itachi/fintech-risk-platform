# configuration.md

# Configuration Architecture

# Overview

The `transaction-service` uses:

```text
Environment-driven externalized configuration
```

through:

- `.env`
- `application.yml`
- Spring Boot property binding
- environment variable injection

This architecture enables:

- environment portability
- secure secret management
- cloud-native deployment
- infrastructure flexibility
- centralized runtime configuration

---

# Configuration Goals

| Goal | Purpose |
|---|---|
| Environment Separation | Dev / QA / Prod configs |
| Externalized Secrets | Avoid hardcoded credentials |
| Runtime Flexibility | Dynamic deployments |
| Cloud Readiness | Container compatibility |
| Centralized Config | Easier maintenance |

---

# Configuration Sources

| Source | Purpose |
|---|---|
| `.env` | Environment variables |
| `application.yml` | Spring Boot runtime configuration |

---

# Configuration Loading Flow

```text
.env Variables
      |
      v
Spring Environment
      |
      v
application.yml
(property placeholders)
      |
      v
Spring Boot Runtime
      |
      v
Application Components
```

---

# High-Level Configuration Architecture

```text
+--------------------+
|      .env          |
+--------------------+
           |
           v
+--------------------+
| application.yml    |
+--------------------+
           |
           v
+--------------------+
| Spring Boot        |
| Configuration      |
+--------------------+
           |
           v
+--------------------+
| Service Components |
+--------------------+
```

---

# ==========================================
# 1. Server Configuration
# ==========================================

# Purpose

Controls:

- application port
- runtime server settings

---

# .env

```env
SERVER_PORT=8082
```

---

# application.yml

```yaml
server:
  port: ${SERVER_PORT}
```

---

# Final Runtime Value

```text
transaction-service runs on port 8082
```

---

# Why Externalized Port Matters

Enables:

- Docker deployments
- Kubernetes portability
- multi-environment deployments
- container orchestration flexibility

---

# ==========================================
# 2. Spring Application Configuration
# ==========================================

# Purpose

Defines:

```text
Spring application identity
```

---

# .env

```env
SPRING_APPLICATION_NAME=transaction-service
```

---

# application.yml

```yaml
spring:
  application:
    name: ${SPRING_APPLICATION_NAME}
```

---

# Why Application Name Matters

Used for:

- logging
- distributed tracing
- monitoring
- service discovery
- observability

---

# ==========================================
# 3. MySQL Database Configuration
# ==========================================

# Purpose

Configures:

- database connectivity
- datasource settings
- JPA persistence
- Hibernate behavior

---

# Database Environment Variables

```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=gbank_Transactions
MYSQL_USERNAME=root
MYSQL_PASSWORD=******
```

---

# Datasource Configuration

```yaml
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
```

---

# JDBC Connection Flow

```text
Spring Boot
      |
      v
Datasource
      |
      v
MySQL JDBC Driver
      |
      v
MySQL Database
```

---

# JDBC URL Features

| Parameter | Purpose |
|---|---|
| createDatabaseIfNotExist=true | Auto-create DB |
| useSSL=false | Local development |
| allowPublicKeyRetrieval=true | MySQL auth support |
| serverTimezone=UTC | Timezone consistency |

---

# Hibernate Configuration

# DDL Auto

```env
HIBERNATE_DDL_AUTO=create
```

Meaning:

```text
Schema recreated during startup
```

---

# Important Note

```text
create
```

is suitable ONLY for development.

Production should use:

```text
validate
```

or:

```text
none
```

---

# SQL Logging

```env
HIBERNATE_SHOW_SQL=false
HIBERNATE_FORMAT_SQL=false
HIBERNATE_HIGHLIGHT_SQL=false
```

Purpose:

- cleaner logs
- lower console noise
- better production performance

---

# Hibernate Dialect

```env
HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect
```

Purpose:

```text
MySQL-specific SQL generation
```

---

# ==========================================
# 4. Kafka Configuration
# ==========================================

# Purpose

Configures:

- Kafka broker connectivity
- distributed event publishing

---

# Kafka Bootstrap Servers

```env
KAFKA_BOOTSTRAP_SERVERS=localhost:19092,localhost:19094,localhost:19096
```

---

# application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
```

---

# Why Multiple Brokers Exist

Provides:

```text
Kafka cluster fault tolerance
```

---

# Kafka Flow

```text
transaction-service
        |
        v
Kafka Bootstrap Servers
        |
        v
Kafka Cluster
```

---

# Benefits

| Benefit | Purpose |
|---|---|
| High Availability | Broker fault tolerance |
| Scalability | Distributed messaging |
| Reliability | Durable publishing |

---

# ==========================================
# 5. OAuth2 Resource Server Configuration
# ==========================================

# Purpose

Configures:

```text
JWT authentication validation
```

for incoming requests.

---

# Keycloak Issuer URI

```env
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/gringotts
```

---

# application.yml

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI}
```

---

# Why issuer-uri Is Important

Spring Security uses this for:

- JWT signature validation
- issuer trust verification
- JWK retrieval
- token metadata discovery

---

# Security Flow

```text
Incoming JWT
        |
        v
Issuer Validation
        |
        v
JWK Retrieval
        |
        v
Signature Validation
```

---

# ==========================================
# 6. Service-to-Service Keycloak Configuration
# ==========================================

# Purpose

Configures:

```text
Machine-to-machine authentication
```

for Feign communication.

---

# Configuration

```env
KEYCLOAK_TOKEN_URL=...
KEYCLOAK_CLIENT_ID=transaction-service
KEYCLOAK_CLIENT_SECRET=******
```

---

# application.yml

```yaml
keycloak:
  token-url: ${KEYCLOAK_TOKEN_URL}
  client-id: ${KEYCLOAK_CLIENT_ID}
  client-secret: ${KEYCLOAK_CLIENT_SECRET}
```

---

# Authentication Flow

```text
transaction-service
        |
        v
Keycloak Token Endpoint
        |
        v
JWT Token
        |
        v
Feign Authorization Header
```

---

# Why This Exists

Enables:

```text
Secure service-to-service trust
```

between:

- transaction-service
- risk-decision-service

---

# ==========================================
# 7. Outbox Configuration
# ==========================================

# Purpose

Controls:

- Kafka outbox publishing
- retry scheduling
- batch processing

---

# Configuration

```env
OUTBOX_BATCH_SIZE=10
OUTBOX_MAX_RETRY=3
OUTBOX_SCHEDULER_DELAY_MS=3000
```

---

# application.yml

```yaml
outbox:
  batch-size: ${OUTBOX_BATCH_SIZE}
  max-retry: ${OUTBOX_MAX_RETRY}
  scheduler-delay-ms: ${OUTBOX_SCHEDULER_DELAY_MS}
```

---

# Configuration Meaning

| Property | Purpose |
|---|---|
| batch-size | Events processed per batch |
| max-retry | Maximum Kafka retries |
| scheduler-delay-ms | Scheduler polling interval |

---

# Outbox Flow

```text
Scheduler
      |
      v
Fetch Pending Events
      |
      v
Kafka Publishing
```

---

# Why Batch Processing Matters

Prevents:

- memory overload
- Kafka flooding
- excessive DB load

---

# ==========================================
# 8. Risk Service Configuration
# ==========================================

# Purpose

Defines:

```text
Feign fraud-service endpoint
```

---

# Configuration

```env
RISK_SERVICE_URL=http://localhost:8082
```

---

# application.yml

```yaml
risk:
  service:
    url: ${RISK_SERVICE_URL}
```

---

# Why Externalized URL Matters

Enables:

- environment switching
- container deployment
- Kubernetes service discovery
- cloud portability

---

# ==========================================
# 9. Feign Configuration
# ==========================================

# Purpose

Controls:

- HTTP timeouts
- Feign behavior
- logging

---

# Configuration

```env
FEIGN_CONNECT_TIMEOUT=200
FEIGN_READ_TIMEOUT=500
FEIGN_LOGGER_LEVEL=basic
```

---

# application.yml

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: ${FEIGN_CONNECT_TIMEOUT}
        readTimeout: ${FEIGN_READ_TIMEOUT}
        loggerLevel: ${FEIGN_LOGGER_LEVEL}
```

---

# Configuration Meaning

| Property | Purpose |
|---|---|
| connectTimeout | TCP connection timeout |
| readTimeout | Response wait timeout |
| loggerLevel | Feign logging verbosity |

---

# Why Timeouts Matter

Protects against:

- hanging services
- thread exhaustion
- distributed system failures

---

# OkHttp Enabled

```yaml
feign:
  okhttp:
    enabled: true
```

Purpose:

```text
Use OkHttp as HTTP client
```

Benefits:

- better performance
- connection pooling
- efficient HTTP handling

---

# ==========================================
# 10. Resilience4j Retry Configuration
# ==========================================

# Purpose

Controls:

- retry handling
- transient-failure recovery
- fault tolerance

---

# Keycloak Retry Config

```env
KEYCLOAK_RETRY_MAX_ATTEMPTS=3
KEYCLOAK_RETRY_WAIT_DURATION=300ms
```

---

# Risk Service Retry Config

```env
RISK_RETRY_MAX_ATTEMPTS=2
RISK_RETRY_WAIT_DURATION=100ms
RISK_RETRY_EXPONENTIAL_BACKOFF=2
```

---

# application.yml

```yaml
resilience4j:
  retry:
    instances:
```

---

# Retry Flow

```text
Feign Request
      |
      v
Failure
      |
      v
Retry Logic
      |
   +--+---+
   |      |
SUCCESS  FAIL
```

---

# Retry Exceptions

Configured retries for:

```yaml
retryExceptions:
  - java.io.IOException
  - feign.RetryableException
  - java.net.SocketTimeoutException
```

---

# Why Controlled Retries Matter

Retries help recover:

- temporary outages
- transient network issues
- broker hiccups

But excessive retries can cause:

```text
Retry storms
```

---

# ==========================================
# 11. Circuit Breaker Configuration
# ==========================================

# Purpose

Protects against:

```text
Cascading distributed failures
```

---

# Configuration

```env
RISK_CB_SLIDING_WINDOW_SIZE=10
RISK_CB_MINIMUM_CALLS=5
RISK_CB_FAILURE_THRESHOLD=50
RISK_CB_WAIT_DURATION_OPEN_STATE=10s
RISK_CB_HALF_OPEN_CALLS=3
```

---

# application.yml

```yaml
resilience4j:
  circuitbreaker:
    instances:
      riskService:
```

---

# Circuit Breaker Flow

```text
Feign Calls
      |
      v
Failure Monitoring
      |
   +--+---+
   |      |
HEALTHY  FAILURE_THRESHOLD_EXCEEDED
   |      |
   v      v
CLOSED   OPEN
```

---

# Circuit Breaker States

| State | Meaning |
|---|---|
| CLOSED | Normal operation |
| OPEN | Calls blocked |
| HALF_OPEN | Recovery testing |

---

# Why Circuit Breakers Matter

Prevents:

- thread exhaustion
- cascading failures
- distributed outages

---

# ==========================================
# 12. Logging Configuration
# ==========================================

# Purpose

Controls:

- structured logging
- distributed tracing
- correlation visibility

---

# Logging Pattern

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%X{correlationId}] %-5level %logger{36} - %msg%n"
```

---

# Why correlationId Is Important

Enables:

```text
Distributed request tracing
```

across microservices.

---

# Logging Flow

```text
Incoming Request
        |
        v
Correlation ID
        |
        v
MDC Context
        |
        v
Structured Logs
```

---

# Example Log

```text
2026-05-18 10:30:00 [corr-9911] INFO TransactionOrchestratorService - transaction_created
```

---

# Configuration Characteristics

| Characteristic | Status |
|---|---|
| Externalized | YES |
| Environment-Driven | YES |
| Cloud-Ready | YES |
| Secure | YES |
| Distributed-System Ready | YES |
| Resilience-Oriented | YES |

---

# Why Externalized Configuration Is Important

Without externalized config:

```text
Application changes require recompilation
```

Very bad for production systems.

This setup enables:

- Docker deployments
- Kubernetes deployments
- CI/CD pipelines
- cloud-native operations

---

# Future Enhancements

- Spring Cloud Config
- Vault secret management
- Kubernetes ConfigMaps
- centralized configuration server
- dynamic config refresh
- encrypted secret storage

---

# Final Summary

The configuration architecture inside:

```text
transaction-service
```

implements:

```text
Production-grade externalized distributed-system configuration
```

supporting:

- secure infrastructure management
- scalable deployments
- resilient runtime behavior
- distributed system reliability
- cloud-native application architecture