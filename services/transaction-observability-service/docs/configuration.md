# configuration.md

# Transaction Observability Service — Configuration Architecture

# Overview

The `transaction-observability-service` uses:

```text
Environment-driven externalized configuration
```

through:

- `.env`
- `application.yaml`
- Spring Boot property injection

This architecture enables:

- environment portability
- cloud-native deployment
- secure secret management
- infrastructure flexibility
- scalable distributed configuration

---

# Configuration Goals

| Goal | Purpose |
|---|---|
| Environment Separation | Dev / QA / Prod configs |
| Externalized Secrets | Avoid hardcoded credentials |
| Runtime Flexibility | Easier deployments |
| Infrastructure Portability | Docker/K8s support |
| Operational Stability | Consistent runtime behavior |

---

# Configuration Sources

| Source | Purpose |
|---|---|
| `.env` | Environment variables |
| `application.yaml` | Spring Boot runtime config |

---

# Configuration Loading Flow

```text
.env Variables
      |
      v
Spring Environment
      |
      v
application.yaml
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
+----------------------+
|        .env          |
+----------------------+
            |
            v
+----------------------+
|   application.yaml   |
+----------------------+
            |
            v
+----------------------+
|    Spring Boot       |
|   Configuration      |
+----------------------+
            |
            v
+----------------------+
| Service Components   |
+----------------------+
```

---

# ==========================================
# 1. Server Configuration
# ==========================================

# Purpose

Defines:

- runtime port
- HTTP server configuration

---

# .env

```env
SERVER_PORT=8084
```

---

# application.yaml

```yaml
server:
  port: ${SERVER_PORT}
```

---

# Final Runtime

```text
transaction-observability-service runs on port 8084
```

---

# Why Externalized Port Matters

Enables:

- Docker deployments
- Kubernetes deployments
- environment portability
- multi-service orchestration

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
SPRING_APPLICATION_NAME=transaction-observability-service
```

---

# application.yaml

```yaml
spring:
  application:
    name: ${SPRING_APPLICATION_NAME}
```

---

# Why Application Name Matters

Used for:

- logging
- observability
- tracing
- monitoring
- distributed diagnostics

---

# ==========================================
# 3. MySQL Database Configuration
# ==========================================

# Purpose

Configures:

- datasource
- JPA persistence
- Hibernate integration

---

# Environment Variables

```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=gbank_transaction-observability-service
MYSQL_USERNAME=root
MYSQL_PASSWORD=******
```

---

# application.yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
```

---

# JDBC Flow

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

| Feature | Purpose |
|---|---|
| createDatabaseIfNotExist=true | Auto-create DB |
| useSSL=false | Local development |
| allowPublicKeyRetrieval=true | MySQL auth support |
| serverTimezone=UTC | Time consistency |

---

# Why UTC Matters

Prevents:

- timezone inconsistencies
- distributed timestamp drift
- analytics inaccuracies

---

# ==========================================
# 4. HikariCP Configuration
# ==========================================

# Purpose

Controls:

```text
Database connection pooling
```

---

# .env

```env
HIKARI_MAXIMUM_POOL_SIZE=10
```

---

# application.yaml

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: ${HIKARI_MAXIMUM_POOL_SIZE}
```

---

# Why Connection Pooling Matters

Improves:

- DB performance
- connection reuse
- application throughput

---

# Pool Flow

```text
Application Threads
        |
        v
Hikari Connection Pool
        |
        v
MySQL Database
```

---

# Why Pool Size Matters

Too small:

```text
Connection starvation
```

Too large:

```text
DB overload
```

---

# ==========================================
# 5. Hibernate / JPA Configuration
# ==========================================

# Purpose

Controls:

- ORM behavior
- schema generation
- SQL formatting

---

# Hibernate Config

```env
HIBERNATE_DDL_AUTO=create
HIBERNATE_SHOW_SQL=false
HIBERNATE_FORMAT_SQL=true
HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect
```

---

# application.yaml

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: ${HIBERNATE_DDL_AUTO}
```

---

# DDL Auto

```text
create
```

Meaning:

```text
Schema recreated during startup
```

---

# Important Note

Suitable ONLY for development.

Production should use:

```text
validate
```

or:

```text
none
```

---

# Why show-sql=false Is Good

Prevents:

- excessive console logs
- noisy production logging
- performance overhead

---

# Hibernate Dialect

```text
org.hibernate.dialect.MySQLDialect
```

Purpose:

```text
MySQL-specific SQL optimization
```

---

# ==========================================
# 6. OAuth2 Resource Server Configuration
# ==========================================

# Purpose

Configures:

```text
JWT authentication validation
```

---

# .env

```env
KEYCLOAK_JWK_SET_URI=http://localhost:8080/realms/gringotts/protocol/openid-connect/certs
```

---

# application.yaml

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${KEYCLOAK_JWK_SET_URI}
```

---

# Why jwk-set-uri Matters

Spring Security uses this endpoint for:

- JWT signature validation
- public-key retrieval
- issuer trust validation

---

# JWT Validation Flow

```text
Incoming JWT
        |
        v
JWK Retrieval
        |
        v
Signature Validation
```

---

# Why JWT Security Is Important

Protects:

- audit APIs
- export endpoints
- observability records
- failed-event data

---

# ==========================================
# 7. Kafka Configuration
# ==========================================

# Purpose

Configures:

- Kafka brokers
- consumer settings
- topic management
- deserialization

---

# Bootstrap Servers

```env
KAFKA_BOOTSTRAP_SERVERS=localhost:19092,localhost:19094,localhost:19096
```

---

# application.yaml

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
```

---

# Why Multiple Brokers Matter

Provides:

```text
Kafka cluster fault tolerance
```

---

# Kafka Topics

```env
KAFKA_TRANSACTION_FINALIZED_TOPIC=transaction.finalized.v1
KAFKA_TRANSACTION_FINALIZED_DLT_TOPIC=transaction.finalized.v1-dlt
```

---

# Topic Purpose

| Topic | Purpose |
|---|---|
| transaction.finalized.v1 | Main event topic |
| transaction.finalized.v1-dlt | Dead-letter topic |

---

# Why DLT Exists

Protects system from:

```text
Poison Kafka messages
```

---

# ==========================================
# 8. Kafka Consumer Configuration
# ==========================================

# Consumer Group

```env
KAFKA_CONSUMER_GROUP=transaction-observability-event
```

---

# Why Consumer Groups Matter

Enables:

- horizontal scaling
- partition balancing
- distributed processing

---

# Auto Offset Reset

```env
KAFKA_AUTO_OFFSET_RESET=earliest
```

Meaning:

```text
Consume from beginning if no offsets exist
```

---

# Why earliest Is Useful

Helpful for:

- local development
- event replay
- recovery testing

---

# Auto Commit Disabled

```env
KAFKA_ENABLE_AUTO_COMMIT=false
```

---

# Why This Is Important

Enables:

```text
Manual acknowledgment control
```

---

# Benefit

Prevents:

```text
Kafka offset commit before successful processing
```

---

# Max Poll Records

```env
KAFKA_MAX_POLL_RECORDS=20
```

Purpose:

```text
Limit batch processing size
```

---

# Why This Matters

Protects against:

- memory spikes
- oversized Kafka batches
- long processing delays

---

# Max Poll Interval

```env
KAFKA_MAX_POLL_INTERVAL_MS=10000
```

Purpose:

```text
Consumer heartbeat timeout protection
```

---

# Why Poll Interval Matters

Prevents Kafka from assuming:

```text
Consumer is dead
```

during long processing.

---

# ==========================================
# 9. Kafka Listener Configuration
# ==========================================

# Listener Concurrency

```env
KAFKA_LISTENER_CONCURRENCY=3
```

---

# application.yaml

```yaml
spring:
  kafka:
    listener:
      concurrency: ${KAFKA_LISTENER_CONCURRENCY}
```

---

# Why Concurrency Matters

Enables:

```text
Parallel Kafka partition processing
```

---

# Processing Flow

```text
Kafka Partitions
        |
        +----> Consumer Thread 1
        |
        +----> Consumer Thread 2
        |
        +----> Consumer Thread 3
```

---

# ACK Mode

```env
KAFKA_ACK_MODE=MANUAL
```

---

# Why MANUAL ACK Matters

Application explicitly controls:

```text
Kafka offset acknowledgment
```

---

# Benefits

| Benefit | Purpose |
|---|---|
| Safer Processing | Prevent premature commits |
| Better Failure Handling | Retry-safe consumption |
| Controlled Offset Management | Operational reliability |

---

# ==========================================
# 10. Kafka Deserialization Configuration
# ==========================================

# Purpose

Controls:

- event deserialization
- payload safety
- schema conversion

---

# Value Deserializer

```yaml
value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
```

---

# Why ErrorHandlingDeserializer Matters

Protects against:

```text
Malformed Kafka payloads
```

---

# Delegate Deserializer

```yaml
spring.deserializer.value.delegate.class:
  org.springframework.kafka.support.serializer.JsonDeserializer
```

---

# Trusted Packages

```yaml
spring.json.trusted.packages:
  com.gringotts.kafkaevents
```

---

# Why Trusted Packages Matter

Prevents:

```text
Unsafe arbitrary deserialization attacks
```

---

# Default Event Type

```yaml
spring.json.value.default.type:
  com.gringotts.kafkaevents.TransactionFinalizedEvent
```

---

# Why Explicit Event Type Matters

Ensures:

```text
Consistent Kafka event mapping
```

---

# ==========================================
# 11. Management / Actuator Configuration
# ==========================================

# Purpose

Exposes:

- health endpoints
- metrics
- Prometheus scraping

---

# application.yaml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

---

# Exposed Endpoints

| Endpoint | Purpose |
|---|---|
| /actuator/health | Service health |
| /actuator/metrics | Micrometer metrics |
| /actuator/prometheus | Prometheus scraping |

---

# Why Observability Endpoints Matter

Supports:

- monitoring
- alerting
- operational analytics

---

# ==========================================
# 12. Logging Configuration
# ==========================================

# Purpose

Provides:

```text
Structured distributed logging
```

---

# Logging Pattern

```yaml
[%X{correlationId}]
```

---

# Example Log

```text
2026-05-18 10:30:00 [corr-9911] INFO TransactionEventConsumer - kafka_message_received
```

---

# Why correlationId Matters

Enables:

```text
Distributed request tracing
```

---

# Logging Categories

| Category | Purpose |
|---|---|
| com.gringotts | Business logs |
| Spring | Framework logs |
| Hibernate | DB logs |
| Kafka | Messaging logs |
| Hikari | Connection-pool logs |

---

# Why Granular Logging Matters

Enables:

- targeted debugging
- operational visibility
- cleaner production logs

---

# Kafka Logging Tuning

Several Kafka internals reduced to:

```text
WARN / ERROR
```

to avoid:

- excessive noise
- broker spam
- rebalance flooding

---

# ==========================================
# Configuration Characteristics
# ==========================================

| Characteristic | Status |
|---|---|
| Externalized | YES |
| Cloud-Ready | YES |
| Distributed Safe | YES |
| Secure | YES |
| Observable | YES |
| Kafka-Optimized | YES |

---

# Why This Configuration Is Enterprise-Grade

This configuration architecture demonstrates patterns used in:

- banking-event platforms
- fintech analytics systems
- distributed Kafka architectures
- observability microservices

including:

- externalized configuration
- secure JWT validation
- distributed Kafka consumers
- observability instrumentation
- controlled consumer orchestration

---

# Future Enhancements

- Spring Cloud Config
- Vault secret management
- Kubernetes ConfigMaps
- dynamic config refresh
- distributed tracing integration
- Kafka schema registry

---

# Final Summary

The configuration architecture inside:

```text
transaction-observability-service
```

implements:

```text
Production-grade distributed microservice configuration
```

through:

- environment-driven configuration
- Kafka consumer tuning
- JWT security integration
- structured logging
- observability instrumentation
- scalable runtime orchestration

The system is designed for:

```text
Reliable event-driven financial observability workloads
```