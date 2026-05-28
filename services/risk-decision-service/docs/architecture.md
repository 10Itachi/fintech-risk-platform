# architecture.md

# Risk Decision Service Architecture

# Architectural Style

The service follows:

- Layered Microservice Architecture
- Domain-Oriented Design
- Context-Driven Risk Evaluation
- Synchronous Fraud Decision Pipeline
- Explainable Decision Architecture

---

# High-Level System Architecture

```text
┌──────────────────────────────────────┐
│ External Clients                     │
└──────────────────────────────────────┘
                    |
                    v
┌──────────────────────────────────────┐
│ API Layer                            │
│ - RiskEvaluationController           │
└──────────────────────────────────────┘
                    |
                    v
┌──────────────────────────────────────┐
│ Application Orchestration Layer      │
│ - RiskDecisionApplicationService     │
└──────────────────────────────────────┘
                    |
                    v
┌──────────────────────────────────────┐
│ Context & Decision Coordination      │
│ - RiskDecisionContext                │
│ - DerivedFeatureBuilder              │
│ - Policy Engine                      │
└──────────────────────────────────────┘
                    |
┌──────────────────────────────────────┐
│ 1. Hard Rules (Gatekeeper)           │
│ - Instant Denials / Knockouts        │
└──────────────────────────────────────┘
                    | (If Passed)
                    v
┌──────────────────────────────────────┐
│ 2. ML Scoring Engine                 │
│ - Generates Probability Score        │
└──────────────────────────────────────┘
                    |
                    v
┌──────────────────────────────────────┐
│ 3. Soft Rules (Contextual Knobs)     │
│ - Evaluates ML Score + Adjustments   │
└──────────────────────────────────────┘
                    |
                    v
┌──────────────────────────────────────┐
│ Final Policy Decision Engine         │
└──────────────────────────────────────┘
                    |
                    v
┌──────────────────────────────────────┐
│ Persistence & Audit Layer            │
│ - MySql                         │
│ - Redis                              │
└──────────────────────────────────────┘
```

---

# Architectural Layers

## 1. Controller Layer

Responsible for:

- HTTP API exposure
- request validation
- response mapping
- API contract management

Primary component:

```text
RiskEvaluationController
```

---

## 2. Application Orchestration Layer

Coordinates complete risk evaluation lifecycle.

Responsibilities:

- transaction orchestration
- idempotency validation
- resilience coordination
- pipeline execution
- trace persistence

Primary component:

```text
RiskDecisionApplicationService
```

---

## 3. Context Management Layer

Maintains centralized mutable transaction state.

Primary object:

```text
RiskDecisionContext
```

Stores:

- request metadata
- derived features
- ML scores
- reason codes
- rule matches
- final decision

---

## 4. Hard Rule Engine

Evaluates deterministic fraud conditions.

Characteristics:

- compliance-safe
- deterministic
- explainable
- blocking-oriented

Examples:

- blocked device
- blacklisted account
- sanctions violation
- impossible geography

---

## 5. Soft Rule Engine

Evaluates heuristic behavioral anomalies.

Characteristics:

- additive scoring
- heuristic-based
- partially explainable
- behavior-focused

Examples:

- new device
- suspicious timing
- risky geography
- unusual amount patterns

---

## 6. ML Scoring Engine

Performs probabilistic fraud prediction.

Pipeline:

```text
Feature Vector Creation
        |
        v
Feature Scaling
        |
        v
Logistic Regression Inference
        |
        v
Probability Score Generation
```

Outputs:

- fraud probability
- model metadata
- scoring audit data

---

## 7. Policy Decision Layer

Applies governance thresholds.

Decision matrix:

| Risk Level | Decision |
|---|---|
| Low Risk | APPROVED |
| Medium Risk | REVIEW |
| High Risk | DECLINED |

---

## 8. Persistence Layer

Handles:

- audit trace persistence
- historical lookups
- device history
- operational storage

Technologies:

- MySQL
- Spring Data JPA

---

## 9. Infrastructure Layer

Provides:

- security
- correlation IDs
- Redis integration
- configuration
- exception handling

---

## 10. Observability Layer

Provides:

- metrics
- request tracing
- monitoring
- operational visibility

Technologies:

- Micrometer
- Prometheus
- Grafana

---

# Architectural Principles

## Separation of Concerns

Each layer owns a distinct responsibility.

---

## Explainable Decisioning

Every decision is traceable through:

- reason codes
- ML metadata
- policy thresholds
- audit records

---

## Extensibility

New rules and ML models can be integrated without redesigning orchestration.

---

## Stateless Scalability

The service is horizontally scalable.

---

## Operational Reliability

Resilience and idempotency mechanisms ensure production-grade behavior.

---

# Reliability Characteristics

- Graceful degradation
- Retry-safe processing
- Replay protection
- Deterministic orchestration
- Centralized exception handling

---

# Scalability Characteristics

- Stateless orchestration
- Cache-ready architecture
- Horizontally deployable
- Infrastructure decoupled