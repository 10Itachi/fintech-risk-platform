# request-flow.md

# Request Flow Documentation

# End-to-End Transaction Flow

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

# Detailed Processing Stages

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

| Feature | Purpose |
|---|---|
| Device Familiarity | Detect unknown device usage |
| Time-Based Risk | Detect suspicious transaction timing |

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
- daily total amount rule etc.

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
- withdrawal high amount etc.

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
Logistic Regression Evaluation via python model
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

| Decision | Meaning |
|---|---|
| APPROVED | Low risk |
| REVIEW | Medium risk |
| DECLINED | High risk |

---

# Stage 11 — Trace Persistence

Complete evaluation trace is persisted.

Stored information:

- request payload
- rule matches
- ML score
- final decision
- timestamps
- correlation ID

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
- correlation ID