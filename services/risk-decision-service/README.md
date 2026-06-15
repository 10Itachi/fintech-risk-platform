# Risk Decision Service

Production-grade fraud detection and risk decisioning microservice built using Spring Boot, Machine Learning, Redis, OAuth2 Security, Observability, and Generative AI.

---

# Overview

The Risk Decision Service evaluates financial transactions and determines whether they should be:

- APPROVED
- REVIEWED
- DECLINED

The service combines:

- Deterministic Fraud Rules
- Behavioral Risk Analysis
- Machine Learning Fraud Scoring
- Policy-Based Decisioning
- Redis Idempotency Protection
- AI-Powered Fraud Investigation Summaries

The platform is designed using microservice architecture principles and production-grade operational practices including observability, resilience, security, and auditability.

---

# Key Features

## Fraud Detection Engine

- Hard Rule Evaluation
- Soft Rule Evaluation
- Behavioral Risk Analysis
- Policy-Based Decisioning
- Explainable Reason Codes

---

## Machine Learning Integration

- Logistic Regression Fraud Model
- Feature Engineering Pipeline
- Probability-Based Fraud Scoring
- Model Metadata Tracking
- Versioned Model Support

---

## AI-Powered Fraud Investigation

Integrated Spring AI with locally hosted Ollama models to generate business-readable fraud investigation summaries.

Capabilities:

- Explain fraud decisions
- Summarize risk indicators
- Generate analyst recommendations
- Convert technical reason codes into business language
- Cache investigation reports using Redis

Example:

```text
Decision:
DECLINED

Reason Codes:
AMOUNT_LIMIT_EXCEEDED
DAILY_AMOUNT_LIMIT_EXCEEDED

AI Output:
Investigation Summary
Key Risk Indicators
Recommended Action
```

---

## Security

Implemented using:

```text
Spring Security
OAuth2 Resource Server
JWT Authentication
```

Features:

- Stateless Authentication
- JWT Validation
- Role-Based Authorization
- Correlation ID Tracing
- Secure API Access

AI Investigation APIs are restricted to:

```text
ROLE_ADMIN
```

---

## Redis Idempotency Protection

Redis protects the evaluation pipeline from duplicate execution.

Features:

- Distributed Locking
- Replay Protection
- Retry-Safe Processing
- Transaction State Management
- Concurrent Request Prevention

---

## Observability

Operational visibility implemented using:

```text
Micrometer
Prometheus
Grafana
Spring Boot Actuator
```

Metrics include:

- Request Volume
- Latency
- Error Rates
- Fraud Decision Distribution
- ML Scoring Performance
- AI Investigation Latency

---

## Resilience

Production-grade fault tolerance through:

- Idempotency Protection
- Timeout Protection
- Centralized Exception Handling
- Retry-Safe Architecture
- Correlation-Based Failure Tracking

---

# Architecture

```text
Client
   |
   v
API Layer
   |
   v
Application Service Layer
   |
   v
Risk Decision Context
   |
   v
Hard Rules
   |
   v
Soft Rules
   |
   v
ML Scoring
   |
   v
Policy Engine
   |
   v
Decision Persistence
   |
   v
AI Investigation Layer
   |
   v
Investigation Summary
```

---

# Risk Evaluation Flow

```text
Incoming Transaction
        |
        v
Authentication
        |
        v
Request Validation
        |
        v
Redis Idempotency Check
        |
        v
Feature Generation
        |
        v
Hard Rules
        |
        v
Soft Rules
        |
        v
ML Scoring
        |
        v
Policy Engine
        |
        v
Decision Persistence
        |
        v
Response
```

---

# AI Investigation Flow

```text
Admin User
      |
      v
Investigation Endpoint
      |
      v
Risk Decision Trace Lookup
      |
      v
FraudPromptBuilder
      |
      v
Spring AI ChatClient
      |
      v
Ollama LLM
      |
      v
Investigation Summary
```

---

# Technology Stack

## Backend

```text
Java 21
Spring Boot
Spring Security
Spring Data JPA
Spring Validation
Spring AI
```

## Data Layer

```text
MySQL
Redis
```

## Machine Learning

```text
Python
Logistic Regression
Feature Scaling
```

## Observability

```text
Micrometer
Prometheus
Grafana
Spring Boot Actuator
```

## Security

```text
OAuth2
JWT
Keycloak
```

## Infrastructure

```text
Docker
Docker Compose
```

---

# Core APIs

## Risk Evaluation

```http
POST /risk/evaluate
```

Evaluates transaction fraud risk.

---

## Historical Decisions

```http
GET /risk/api/v1/decisions
```

Returns paginated fraud decisions.

---

## Decision By ID

```http
GET /risk/api/v1/decisions/{decisionId}
```

Returns a specific decision trace.

---

## Decision By Transaction ID

```http
GET /risk/api/v1/decisions/transaction/{transactionId}
```

Returns decision details for a transaction.

---

## AI Investigation Summary

```http
POST /risk/{transactionId}/investigation-summary
```

Generates AI-powered fraud investigation reports.

Access:

```text
ROLE_ADMIN
```

---

# Persistence Model

## Risk Decision Trace

Stores:

- Transaction ID
- Final Status
- ML Probability
- Model Metadata
- Policy Version
- Evaluation Timestamp

---

## Risk Decision Reason

Stores:

- Reason Codes
- Audit Trail Data
- Explainability Metadata

---

# Observability Metrics

## Risk Evaluation Metrics

```text
Request Count
Error Rate
Latency
Decision Distribution
ML Scoring Time
```

## AI Investigation Metrics

```text
risk.ai.investigation.requests
risk.ai.investigation.success
risk.ai.investigation.failure
risk.ai.investigation.latency
```

---

# Design Principles

## Explainability

Every decision is traceable through:

- Rule Matches
- Reason Codes
- ML Metadata
- Policy Versions
- Audit Records

---

## AI-Assisted Investigation

The AI layer never participates in fraud decisions.

Responsibilities:

- Explain decisions
- Summarize risk indicators
- Recommend analyst actions

The authoritative decision remains within:

```text
Hard Rules
Soft Rules
ML Scoring
Policy Engine
```

---

## Scalability

- Stateless Processing
- Redis-Based Coordination
- Horizontal Scaling Support
- Cloud-Ready Deployment

---

## Reliability

- Replay Protection
- Distributed Locking
- Graceful Failure Handling
- Centralized Error Management

---

# Future Enhancements

- Circuit Breakers
- Bulkhead Isolation
- Distributed Retries
- AI Provider Abstraction
- Behavioral Feature Caching
- Advanced Fraud Models
- Real-Time Fraud Streaming
- Multi-Model Risk Scoring

---

# Project Highlights

- Production-Style Microservice Architecture
- ML-Powered Fraud Detection
- Redis Distributed Idempotency
- OAuth2/JWT Security
- Prometheus + Grafana Observability
- AI-Powered Fraud Investigation Summaries
- Spring AI + Ollama Integration
- Role-Based Security Controls
- Fully Auditable Fraud Decision Pipeline

This service demonstrates modern backend engineering practices for building scalable, secure, observable, and explainable fraud detection systems.
