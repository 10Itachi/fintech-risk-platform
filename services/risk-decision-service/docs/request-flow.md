# Request Flow Documentation

# End-to-End Transaction Risk Evaluation Flow

```text
Client Request
      |
      v
JWT Authentication Filter
      |
      v
Correlation ID Filter
      |
      v
RiskEvaluationController
      |
      v
Request Validation
      |
      v
RiskDecisionApplicationService
      |
      v
Redis Idempotency Validation
      |
      v
RiskDecisionContext Creation
      |
      v
Derived Feature Generation
      |
      v
Hard Rule Evaluation
      |
      v
Soft Rule Evaluation
      |
      v
ML Scoring Evaluation
      |
      v
Policy Decision Engine
      |
      v
Decision Trace Persistence
      |
      v
Response Mapping
      |
      v
Client Response
```

---

# End-to-End AI Investigation Summary Flow

```text
Admin User
      |
      v
JWT Authentication Filter
      |
      v
Role Validation (ROLE_ADMIN)
      |
      v
Correlation ID Filter
      |
      v
AiInvestigationController
      |
      v
TransactionId Validation
      |
      v
AiInvestigationService
      |
      v
Redis Cache Lookup
      |
      v
RiskDecisionTrace Lookup
      |
      v
Reason Code Retrieval
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
Fraud Investigation Summary Generation
      |
      v
Response Mapping
      |
      v
Admin Response
```

---

# Detailed Processing Stages

# Transaction Risk Evaluation Pipeline

---

# Stage 1 — Authentication

Incoming requests are authenticated using:

```text
OAuth2 Resource Server + JWT
```

Security validation includes:

- JWT signature validation
- token expiration validation
- authority validation
- security context creation

---

# Stage 2 — Correlation ID Initialization

A correlation ID is generated or propagated.

Purpose:

- request traceability
- distributed logging
- operational debugging
- audit trace linking

---

# Stage 3 — Request Validation

Controller validates:

- required fields
- transaction amount
- transaction identifiers
- timestamp format
- request schema

Invalid requests are rejected before orchestration begins.

---

# Stage 4 — Idempotency Validation

Redis-backed idempotency validation prevents duplicate processing.

Validation checks:

- existing transaction ID
- replay request detection
- retry-safe execution

---

# Stage 5 — Context Initialization

The orchestration layer creates:

```text
RiskDecisionContext
```

This acts as the centralized mutable transaction state.

Stores:

- request metadata
- derived features
- ML score
- rule violations
- policy state
- reason codes
- final decision

---

# Stage 6 — Derived Feature Generation

Behavioral intelligence features are generated.

Examples:

| Feature            | Purpose                              |
| ------------------ | ------------------------------------ |
| Device Familiarity | Detect unknown device usage          |
| Time-Based Risk    | Detect suspicious transaction timing |

---

# Stage 7 — Hard Rule Evaluation

Hard rules are deterministic fraud blockers.

Examples:

- blocked country
- amount limit rule
- invalid amount
- unsupported channel
- invalid transaction time rule
- card withdrawal limit
- daily total amount rule

Characteristics:

- deterministic
- explainable
- compliance-safe
- high-confidence fraud detection

---

# Stage 8 — Soft Rule Evaluation

Soft rules detect combination anomalies.

Examples:

- amount and new device
- channel high amount rule
- geo device anomaly rule
- high velocity burst
- velocity odd hour rule
- withdrawal high amount

Characteristics:

- heuristic-based
- weighted scoring
- behavioral intelligence

---

# Stage 9 — ML Scoring

The ML scoring engine performs probabilistic fraud prediction.

Pipeline:

```text
Feature Vector Creation
        |
        v
Feature Scaling
        |
        v
Logistic Regression Evaluation via Python Model
        |
        v
Fraud Probability Score
```

Generated outputs:

- fraud probability
- confidence metadata
- scoring explanation metadata

---

# Stage 10 — Policy Decisioning

The policy engine combines:

- hard rule results
- ML probability
- soft rule results
- derived behavioral intelligence

Final decisions:

| Decision | Meaning     |
| -------- | ----------- |
| APPROVED | Low risk    |
| REVIEW   | Medium risk |
| DECLINED | High risk   |

---

# Stage 11 — Trace Persistence

Complete evaluation trace is persisted.

Stored information:

- transaction ID
- final decision
- ML probability
- reason codes
- model metadata
- policy version
- timestamps

Tables:

```text
risk_decision_trace
risk_decision_reason
```

Purpose:

- auditability
- explainability
- analytics
- compliance
- debugging

---

# Stage 12 — Response Mapping

Final response is transformed into API contract response DTO.

Response contains:

- transaction ID
- final decision
- risk score
- reason codes
- model metadata

---

# AI Investigation Summary Pipeline

The AI investigation pipeline operates independently from the fraud decision pipeline.

Important:

```text
The AI layer never participates in transaction approval,
review, or decline decisions.
```

The fraud decision is already finalized before AI is invoked.

The AI layer only explains existing decisions.

---

# Stage 13 — Admin Authorization

AI investigation requests are restricted to:

```text
ROLE_ADMIN
```

Purpose:

- prevent customer access
- prevent unauthorized analysis
- restrict investigation capabilities to fraud analysts

---

# Stage 14 — Cached Investigation Lookup

The service first checks Redis.

Cache Key:

```text
investigationSummaryByTransactionId
```

Purpose:

- avoid repeated LLM calls
- reduce response latency
- reduce infrastructure cost

Flow:

```text
Cache Hit
      |
      v
Return Cached Summary

Cache Miss
      |
      v
Continue Investigation Pipeline
```

---

# Stage 15 — Decision Trace Retrieval

The service loads:

```text
RiskDecisionTraceEntity
```

using:

```text
transactionId
```

Retrieved Data:

- transactionId
- finalStatus
- mlProbability
- reasonCodes
- policyVersion
- modelName
- modelVersion

Only transactions with status:

```text
REVIEW
DECLINED
```

are eligible for investigation.

---

# Stage 16 — Prompt Construction

FraudPromptBuilder converts technical risk data into analyst context.

Input:

```text
Transaction Metadata
ML Probability
Reason Codes
Policy Metadata
Model Metadata
```

Output:

```text
Structured Fraud Investigation Prompt
```

Purpose:

- standardize AI behavior
- improve response consistency
- enforce investigation format

---

# Stage 17 — LLM Inference

The generated prompt is sent to:

```text
Spring AI ChatClient
        |
        v
Ollama
        |
        v
Qwen Model
```

Purpose:

- interpret fraud indicators
- generate investigation narrative
- recommend analyst actions

---

# Stage 18 — Investigation Summary Generation

The LLM produces:

- Investigation Summary
- Key Risk Indicators
- Recommended Action

Example:

```text
Investigation Summary:
Transaction exceeded policy thresholds and was declined.

Key Risk Indicators:
AMOUNT_LIMIT_EXCEEDED
DAILY_AMOUNT_LIMIT_EXCEEDED

Recommended Action:
Review customer transaction limits and verify authorization.
```

---

# Stage 19 — Investigation Response Mapping

The generated response is mapped into:

```json
{
  "transactionId": "...",
  "status": "DECLINED",
  "summary": "..."
}
```

and returned to the administrator.

---

# AI Observability Flow

Every AI request emits metrics and logs.

Metrics:

```text
risk.ai.investigation.requests
risk.ai.investigation.success
risk.ai.investigation.failure
risk.ai.investigation.latency
```

Logs:

```text
ai_investigation_started
ai_investigation_entity_found
ai_investigation_completed
ai_investigation_failed
```

Purpose:

- latency monitoring
- operational visibility
- failure tracking
- AI service health monitoring

---

# Architectural Principle

The Risk Decision Engine remains the authoritative decision-maker.

```text
Hard Rules
+
Soft Rules
+
ML Scoring
+
Policy Engine
=
Final Decision
```

The AI layer acts only as an explainability and investigation assistant.

```text
Final Decision
      |
      v
AI Investigation Layer
      |
      v
Business Readable Explanation
```

This preserves deterministic fraud decisions while providing Generative AI-powered fraud investigation capabilities.
