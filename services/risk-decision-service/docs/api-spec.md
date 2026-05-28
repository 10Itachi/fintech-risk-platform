# api-spec.md

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

# API Overview

| Endpoint                                             | Method | Purpose |
|------------------------------------------------------|---|---|
| `/risk/evaluate`                                     | POST | Evaluate transaction risk |
| `/risk/api/v1/decisions`                                    | GET | Fetch paginated historical decisions |
| `/risk/api/v1/decisions/{decisionId}`                | GET | Fetch decision by internal ID |
| `/risk/api/v1/decisions/transaction/{transactionId}` | GET | Fetch decision by transaction ID |

---

# POST /risk/evaluate

# Description

Evaluates incoming transaction risk using:

- hard rules
- ML scoring
- soft rules
- policy decisioning

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
  "reasonCodes": [
    "string"
  ],
  "fraudProbability": 0.1,
  "policyVersion": "string",
  "modelName": "string",
  "modelVersion": "string",
  "trainedAt": "string"
}
```

---

# Decision Semantics

| Decision | Meaning |
|---|---|
| APPROVED | Transaction considered low risk |
| REVIEW | Requires manual investigation |
| DECLINED | Transaction blocked |

---

# GET /risk/api/v1/decisions

# Description

Returns paginated historical decision traces.

---

# Query Parameters

| Parameter | Type | Description |
|---|---|---|
| page | integer | Page number |
| size | integer | Page size |
| from | datetime | Start timestamp |
| to | datetime | End timestamp |

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
      "reasonCodes": [
        "string"
      ],
      "evaluatedAt": "2026-05-16T08:10:55.827Z",
      "modelName": "string",
      "modelVersion": "string",
      "trainedAt": "string"
    }
  ],
  "number": 1073741824,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "numberOfElements": 1073741824,
  "pageable": {
    "offset": 9007199254740991,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "unpaged": true,
    "pageNumber": 1073741824,
    "pageSize": 1073741824
  },
  "empty": true
}
```

---

# GET /risk/api/v1/decisions/{decisionId}

# Description

Fetches decision details using internal decision ID.

---

# Path Variable

| Variable | Type |
|---|---|
| decisionId | long |

---

# Response Example

```json
{
  "id": 9007199254740991,
  "transactionId": "string",
  "finalStatus": "INITIATED",
  "softRiskScore": 1073741824,
  "mlProbability": 0.1,
  "reasonCodes": [
    "string"
  ],
  "evaluatedAt": "2026-05-16T08:11:53.627Z",
  "modelName": "string",
  "modelVersion": "string",
  "trainedAt": "string"
}
```

---

# GET /risk/api/v1/decisions/transaction/{transactionId}

# Description

Fetches decision using transaction ID.

---

# Path Variable

| Variable | Type |
|---|---|
| transactionId | string |

---

# Error Response

```json
{
  "id": 9007199254740991,
  "transactionId": "string",
  "finalStatus": "INITIATED",
  "softRiskScore": 1073741824,
  "mlProbability": 0.1,
  "reasonCodes": [
    "string"
  ],
  "evaluatedAt": "2026-05-16T08:12:15.781Z",
  "modelName": "string",
  "modelVersion": "string",
  "trainedAt": "string"
}
```

---

# Status Codes

| Status | Meaning |
|---|---|
| 200 | Success |
| 400 | Validation failure |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Resource not found |
| 500 | Internal server error |