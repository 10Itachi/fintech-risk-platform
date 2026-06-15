# Risk Decision Service API Documentation

# API Specification

# Base URL

```text
http://localhost:8083
```

---

# Authentication

The APIs are protected using:

```text
OAuth2 Resource Server + JWT Authentication
```

---

# Authorization Model

| Role  | Access                       |
| ----- | ---------------------------- |
| USER  | Risk Evaluation API          |
| ADMIN | Historical Decisions APIs    |
| ADMIN | AI Investigation Summary API |

---

# API Overview

| Endpoint                                             | Method | Purpose                                         |
| ---------------------------------------------------- | ------ | ----------------------------------------------- |
| `/risk/evaluate`                                     | POST   | Evaluate transaction risk                       |
| `/risk/api/v1/decisions`                             | GET    | Fetch paginated historical decisions            |
| `/risk/api/v1/decisions/{decisionId}`                | GET    | Fetch decision by internal ID                   |
| `/risk/api/v1/decisions/transaction/{transactionId}` | GET    | Fetch decision by transaction ID                |
| `/risk/{transactionId}/investigation-summary`        | POST   | Generate AI-powered fraud investigation summary |

---

# POST /risk/evaluate

# Description

Evaluates incoming transaction risk using:

- Hard Rules
- ML Scoring
- Soft Rules
- Policy Decisioning

---

# Request Flow

```text
Client
   |
   v
Controller Validation
   |
   v
Application Service
   |
   v
Derived Features
   |
   v
Hard Rules
   |
   v
ML Scoring
   |
   v
Soft Rules
   |
   v
Policy Decision
   |
   v
Audit Persistence
   |
   v
Response
```

---

# Request Example

```json
{
  "transactionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": "string",
  "amount": 0,
  "totalAmountLast24h": 0,
  "txnCountLast24h": 1073741824,
  "transactionType": "WITHDRAWAL",
  "channel": "UPI",
  "sourceAccount": "string",
  "targetAccount": "string",
  "country": "string",
  "deviceId": "string",
  "transactionTime": "2026-05-16T08:09:57.071Z"
}
```

---

# Response Example

```json
{
  "transactionStatus": "INITIATED",
  "riskScore": 1073741824,
  "reasonCodes": ["string"],
  "fraudProbability": 0.1,
  "policyVersion": "string",
  "modelName": "string",
  "modelVersion": "string",
  "trainedAt": "string"
}
```

---

# Decision Semantics

| Decision | Meaning                         |
| -------- | ------------------------------- |
| APPROVED | Transaction considered low risk |
| REVIEW   | Requires manual investigation   |
| DECLINED | Transaction blocked             |

---

# GET /risk/api/v1/decisions

# Description

Returns paginated historical decision traces.

---

# Query Parameters

| Parameter | Type     | Description     |
| --------- | -------- | --------------- |
| page      | integer  | Page number     |
| size      | integer  | Page size       |
| from      | datetime | Start timestamp |
| to        | datetime | End timestamp   |

---

# Response Example

```json
{
  "totalElements": 9007199254740991,
  "totalPages": 1073741824,
  "first": true,
  "last": true,
  "size": 1073741824,
  "content": [
    {
      "id": 9007199254740991,
      "transactionId": "string",
      "finalStatus": "INITIATED",
      "softRiskScore": 1073741824,
      "mlProbability": 0.1,
      "reasonCodes": ["string"],
      "evaluatedAt": "2026-05-16T08:10:55.827Z",
      "modelName": "string",
      "modelVersion": "string",
      "trainedAt": "string"
    }
  ],
  "number": 1073741824,
  "numberOfElements": 1073741824
}
```

---

# GET /risk/api/v1/decisions/{decisionId}

# Description

Fetches decision details using internal decision ID.

---

# Path Variable

| Variable   | Type |
| ---------- | ---- |
| decisionId | long |

---

# Response Example

```json
{
  "id": 46,
  "transactionId": "2e3e92f4-8cf8-4ba5-a97b-7a177812389e",
  "finalStatus": "DECLINED",
  "mlProbability": 0.0,
  "reasonCodes": ["AMOUNT_LIMIT_EXCEEDED", "DAILY_AMOUNT_LIMIT_EXCEEDED"],
  "evaluatedAt": "2026-06-03T21:54:42Z",
  "modelName": "FraudModel",
  "modelVersion": "2.0.0",
  "trainedAt": "2026-05-27"
}
```

---

# GET /risk/api/v1/decisions/transaction/{transactionId}

# Description

Fetches decision details using transaction ID.

---

# Path Variable

| Variable      | Type   |
| ------------- | ------ |
| transactionId | string |

---

# Response Example

```json
{
  "id": 46,
  "transactionId": "2e3e92f4-8cf8-4ba5-a97b-7a177812389e",
  "finalStatus": "DECLINED",
  "mlProbability": 0.0,
  "reasonCodes": ["AMOUNT_LIMIT_EXCEEDED", "DAILY_AMOUNT_LIMIT_EXCEEDED"],
  "evaluatedAt": "2026-06-03T21:54:42Z",
  "modelName": "FraudModel",
  "modelVersion": "2.0.0",
  "trainedAt": "2026-05-27"
}
```

---

# POST /risk/{transactionId}/investigation-summary

# Description

Generates an AI-powered fraud investigation report for suspicious transactions.

The endpoint:

- Loads risk decision trace data
- Builds an investigation prompt
- Sends the prompt to Ollama using Spring AI
- Generates a business-readable investigation report
- Returns recommendations for fraud analysts

This endpoint is restricted to:

```text
ROLE_ADMIN
```

Only REVIEW and DECLINED transactions are eligible for AI investigation.

APPROVED transactions are excluded.

---

# Request Flow

```text
Admin User
      |
      v
AI Investigation Endpoint
      |
      v
Load Risk Decision Trace
      |
      v
Prompt Builder
      |
      v
Spring AI
      |
      v
Ollama (Qwen Model)
      |
      v
Generated Investigation Summary
      |
      v
Response
```

---

# Path Variable

| Variable      | Type   |
| ------------- | ------ |
| transactionId | string |

---

# Example Request

```http
POST /risk/2e3e92f4-8cf8-4ba5-a97b-7a177812389e/investigation-summary
```

---

# Example Response

```json
{
  "transactionId": "2e3e92f4-8cf8-4ba5-a97b-7a177812389e",
  "status": "DECLINED",
  "summary": "Investigation Summary: The transaction with ID 2e3e92f4-8cf8-4ba5-a97b-7a177812389e was declined due to exceeding both the single transaction amount limit and the daily transaction amount limit. The machine learning probability for fraud is zero, indicating low suspicion of fraudulent activity.\n\nKey Risk Indicators: AMOUNT_LIMIT_EXCEEDED, DAILY_AMOUNT_LIMIT_EXCEEDED\n\nRecommended Action: Review the customer's account settings to ensure that the transaction limits are appropriate for their spending habits. If necessary, contact the customer to adjust the limits or verify if this was an authorized transaction. No further action is required based on current risk indicators."
}
```

---

# AI Investigation Response Fields

| Field         | Type   | Description                       |
| ------------- | ------ | --------------------------------- |
| transactionId | string | Transaction identifier            |
| status        | string | REVIEW or DECLINED                |
| summary       | string | AI-generated investigation report |

---

# AI Investigation Observability

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

---

# Status Codes

| Status | Meaning                 |
| ------ | ----------------------- |
| 200    | Success                 |
| 400    | Invalid request         |
| 401    | Unauthorized            |
| 403    | Forbidden               |
| 404    | Risk decision not found |
| 500    | Internal server error   |

---

# Technology Stack

```text
Spring Boot
Spring Security
Spring Data JPA
MySQL
Redis
Spring AI
Ollama
Micrometer
Prometheus
```
