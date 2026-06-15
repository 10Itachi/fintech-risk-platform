# Risk Decision Service Architecture

# Architectural Style

The service follows:

* Layered Microservice Architecture
* Domain-Oriented Design
* Context-Driven Risk Evaluation
* Synchronous Fraud Decision Pipeline
* Explainable Decision Architecture
* AI-Assisted Fraud Investigation Architecture

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
│ - RiskDecisionAdminController        │
│ - AiInvestigationController          │
└──────────────────────────────────────┘
                    |
                    v
┌──────────────────────────────────────┐
│ Application Orchestration Layer      │
│ - RiskDecisionApplicationService     │
│ - AiInvestigationService             │
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
│ - MySQL                              │
│ - Redis                              │
│ - RiskDecisionTraceEntity            │
│ - RiskDecisionReason                 │
└──────────────────────────────────────┘
                    |
                    v
┌──────────────────────────────────────┐
│ AI Investigation Layer               │
│ - FraudPromptBuilder                 │
│ - Spring AI                          │
│ - Ollama                             │
│ - Qwen Model                         │
└──────────────────────────────────────┘
                    |
                    v
┌──────────────────────────────────────┐
│ Investigation Summary Generation     │
│ - Investigation Summary              │
│ - Risk Indicators                    │
│ - Recommended Actions                │
└──────────────────────────────────────┘
```

---

# Architectural Layers

## 1. Controller Layer

Responsible for:

* HTTP API exposure
* Request validation
* Response mapping
* API contract management
* Security enforcement

Primary Components:

```text
RiskEvaluationController
RiskDecisionAdminController
AiInvestigationController
```

---

## 2. Application Orchestration Layer

Coordinates complete risk evaluation lifecycle.

Responsibilities:

* Transaction orchestration
* Idempotency validation
* Resilience coordination
* Pipeline execution
* Trace persistence
* AI investigation orchestration

Primary Components:

```text
RiskDecisionApplicationService
AiInvestigationService
```

---

## 3. Context Management Layer

Maintains centralized mutable transaction state.

Primary Object:

```text
RiskDecisionContext
```

Stores:

* Request metadata
* Derived features
* ML scores
* Reason codes
* Rule matches
* Final decision

---

## 4. Hard Rule Engine

Evaluates deterministic fraud conditions.

Characteristics:

* Compliance-safe
* Deterministic
* Explainable
* Blocking-oriented

Examples:

* Blocked device
* Blacklisted account
* Sanctions violation
* Impossible geography

---

## 5. Soft Rule Engine

Evaluates heuristic behavioral anomalies.

Characteristics:

* Additive scoring
* Heuristic-based
* Partially explainable
* Behavior-focused

Examples:

* New device
* Suspicious timing
* Risky geography
* Unusual amount patterns

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

* Fraud probability
* Model metadata
* Scoring audit data

---

## 7. Policy Decision Layer

Applies governance thresholds.

Decision Matrix:

| Risk Level  | Decision |
| ----------- | -------- |
| Low Risk    | APPROVED |
| Medium Risk | REVIEW   |
| High Risk   | DECLINED |

---

## 8. Persistence Layer

Handles:

* Audit trace persistence
* Historical lookups
* Device history
* Operational storage
* AI investigation caching

Technologies:

* MySQL
* Spring Data JPA
* Redis

Stored Audit Data:

```text
transactionId
finalStatus
mlProbability
reasonCodes
modelName
modelVersion
trainedAt
policyVersion
evaluatedAt
```

---

## 9. AI Investigation Layer

Provides analyst-oriented explainability using Generative AI.

Purpose:

Transform machine-generated fraud decisions into business-readable investigation reports.

Primary Components:

```text
AiInvestigationService
FraudPromptBuilder
Spring AI ChatClient
Ollama
```

---

### FraudPromptBuilder

Responsibilities:

* Prompt construction
* Context enrichment
* Standardized analyst instructions

Inputs:

```text
transactionId
finalStatus
mlProbability
reasonCodes
policyVersion
modelName
modelVersion
```

---

### AiInvestigationService

Responsibilities:

* Load risk decision traces
* Build investigation prompts
* Invoke LLM
* Generate fraud investigation summaries
* Cache investigation results
* Collect AI observability metrics

---

### LLM Integration

Technology Stack:

```text
Spring AI
Ollama
Qwen Model
```

Characteristics:

* Local model execution
* No external API dependency
* No token consumption costs
* Offline-capable inference

---

## 10. Infrastructure Layer

Provides:

* Security
* Correlation IDs
* Redis integration
* Configuration management
* Exception handling

---

## 11. Observability Layer

Provides:

* Metrics
* Request tracing
* Monitoring
* Operational visibility
* AI inference monitoring

Technologies:

```text
Micrometer
Prometheus
Grafana
```

---

# AI Investigation Flow

```text
Admin User
      |
      v
POST /admin/risk-decisions/{transactionId}/investigation-summary
      |
      v
AiInvestigationController
      |
      v
AiInvestigationService
      |
      v
Load RiskDecisionTraceEntity
      |
      v
FraudPromptBuilder
      |
      v
Spring AI ChatClient
      |
      v
Ollama
      |
      v
Generated Investigation Summary
      |
      v
JSON Response
```

---

# AI Investigation Endpoint

Endpoint:

```http
POST /admin/risk-decisions/{transactionId}/investigation-summary
```

Access Control:

```text
ROLE_ADMIN
```

Supported Decisions:

```text
REVIEW
DECLINED
```

Excluded Decisions:

```text
APPROVED
```

Response:

```json
{
  "transactionId": "2e3e92f4-8cf8-4ba5-a97b-7a177812389e",
  "status": "REVIEW",
  "summary": "The transaction was flagged for manual review due to elevated fraud risk indicators..."
}
```

---

# AI Observability

Metrics:

```text
risk.ai.investigation.requests
risk.ai.investigation.success
risk.ai.investigation.failure
risk.ai.investigation.latency
```

Logged Events:

```text
ai_investigation_started
ai_investigation_entity_found
ai_investigation_completed
ai_investigation_failed
```

Monitored Attributes:

```text
Transaction Id
Decision Id
Inference Latency
Success Rate
Failure Rate
```

---

# Architectural Principles

## Separation of Concerns

Each layer owns a distinct responsibility.

---

## Explainable Decisioning

Every decision is traceable through:

* Reason codes
* ML metadata
* Policy thresholds
* Audit records

---

## AI-Assisted Explainability

The AI layer never participates in transaction approval or rejection.

Responsibilities:

* Explain decisions
* Summarize fraud indicators
* Recommend analyst actions

The authoritative fraud decision remains within:

```text
Hard Rules
ML Scoring
Soft Rules
Policy Decision Engine
```

---

## Extensibility

New rules, models, AI providers, and investigation workflows can be integrated without redesigning orchestration.

---

## Stateless Scalability

The service is horizontally scalable.

---

## Operational Reliability

Resilience, idempotency, caching, and observability mechanisms ensure production-grade behavior.

---

# Reliability Characteristics

* Graceful degradation
* Retry-safe processing
* Replay protection
* Deterministic orchestration
* Centralized exception handling
* AI response caching

---

# Scalability Characteristics

* Stateless orchestration
* Cache-enabled architecture
* Horizontally deployable
* Infrastructure decoupled
* AI provider abstraction ready
* Local or cloud LLM compatible

```
```
