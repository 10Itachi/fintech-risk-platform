# API Integration — Gringotts Frontend

Documents every backend endpoint the frontend calls, the request/response shape, and which page uses it.

---

## Base Configuration

All API calls go through a single Axios instance:

```javascript
// src/api/axiosClient.js
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL,  // http://localhost:8087
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
});
```

JWT is injected automatically via request interceptor. `401` responses trigger automatic logout via response interceptor.

---

## Authentication

### Login

```
POST /realms/gringotts/protocol/openid-connect/token
```

Note: This endpoint bypasses the gateway and calls Keycloak directly via `VITE_KEYCLOAK_URL`.

**Content-Type:** `application/x-www-form-urlencoded`

**Request:**
```
grant_type=password
client_id=gringotts-frontend
username=<username>
password=<password>
```

**Response:**
```json
{
  "access_token": "eyJ...",
  "expires_in": 300,
  "refresh_token": "eyJ...",
  "token_type": "Bearer"
}
```

**Used by:** `LoginPage.jsx` via `authApi.js`

**Security:**
- `access_token` stored in memory only via `tokenManager`
- Token expires in 5 minutes
- `refresh_token` not used — user re-authenticates on expiry

---

## User Service

Base path: `/api/v1/users`

### Create User

```
POST /api/v1/users/admin/createUser
Authorization: Bearer <token>
Role required: ADMIN
```

**Request:**
```json
{
  "userName": "john_doe",
  "password": "securePassword123",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "role": "USER"
}
```

Allowed roles: `USER`, `ADMIN`

**Response:**
```json
{
  "userId": "a00386ec-9cac-4cd3-ac7e-b09ed925da38",
  "userName": "john_doe",
  "isActive": "ACTIVE"
}
```

**Used by:** `CreateUserPage.jsx` via `userApi.js`

---

## Transaction Service

Base path: `/api/v1/transactions`

### Create Transaction

```
POST /api/v1/transactions/user/createa
Authorization: Bearer <token>
Role required: USER
```

**Request:**
```json
{
  "amount": 400,
  "transactionType": "TRANSFER",
  "sourceAccount": "ACC-EW-653680515847",
  "targetAccount": "ACC-PGU-01453213993",
  "channel": "UPI",
  "country": "IN",
  "deviceId": "DEV-ANDROID-MOBILE",
  "transactionTime": "2026-06-07T09:47:07.544Z"
}
```

**Account format validation (frontend):**
```
Pattern: ^ACC-[A-Z]{2,3}-[0-9]{6,12}$
Example: ACC-EW-123456
```

**Device ID mapping (frontend generates based on channel):**
| Channel | Device ID |
|---------|-----------|
| UPI | `DEV-ANDROID-MOBILE` |
| CARD | `DEV-WEB-ATM001` |
| NET_BANKING | `DEV-WEB-LAPTOP` |

**Transaction Time:** Generated as `new Date().toISOString()` at submit time.

**Response:**
```json
{
  "transactionId": "39c7e98a-d7f8-4119-a9e0-6da7ba024d85",
  "userName": "user2",
  "amount": 400.00,
  "sourceAccount": "ACC-EW-653680515847",
  "targetAccount": "ACC-PGU-01453213993",
  "transactionType": "TRANSFER",
  "channel": "UPI",
  "country": "IN",
  "deviceId": "DEV-ANDROID-MOBILE",
  "transactionTime": "2026-06-07T09:47:07.544Z",
  "createdAt": "2026-06-07T09:54:03.863380Z",
  "transactionStatus": "REVIEW"
}
```

**Used by:** `CreateTransactionPage.jsx` via `transactionApi.js`

---

### Get All Transactions (Paginated)

```
GET /api/v1/transactions/admin/allUsers?page=0&size=10&sort=createdAt,desc
Authorization: Bearer <token>
Role required: ADMIN
```

**Query Parameters:**
| Parameter | Default | Description |
|-----------|---------|-------------|
| page | 0 | Page number (0-indexed) |
| size | 10 | Records per page |
| sort | createdAt,desc | Field and direction |

**Sortable fields:** `createdAt`, `amount`, `transactionStatus`

**Response (Spring Page object):**
```json
{
  "content": [
    {
      "transactionId": "39c7e98a-...",
      "userName": "user2",
      "amount": 400.00,
      "sourceAccount": "ACC-EW-653680515847",
      "targetAccount": "ACC-PGU-01453213993",
      "transactionType": "TRANSFER",
      "channel": "UPI",
      "country": "IN",
      "deviceId": "DEV-ANDROID-MOBILE",
      "transactionTime": "2026-06-07T09:47:07.544Z",
      "createdAt": "2026-06-07T09:54:03.863380Z",
      "transactionStatus": "REVIEW"
    }
  ],
  "totalElements": 16,
  "totalPages": 2,
  "number": 0,
  "first": true,
  "last": false,
  "empty": false
}
```

**Transaction Status Colors (UI):**
| Status | Background | Text |
|--------|-----------|------|
| APPROVED | `#064e3b` | `#10b981` |
| REVIEW | `#78350f` | `#f59e0b` |
| REJECTED | `#7f1d1d` | `#ef4444` |

**Used by:** `AllTransactionsPage.jsx` via `transactionApi.js`

---

## Risk Decision Service

Base path: `/risk`

### Get Risk Decision

```
GET /risk/api/v1/decisions/transaction/{transactionId}
Authorization: Bearer <token>
Role required: ADMIN
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| transactionId | UUID | Transaction to look up |

**Response:**
```json
{
  "id": 2,
  "transactionId": "52edd868-9dde-4408-8392-079e34b9591f",
  "finalStatus": "APPROVED",
  "softRiskScore": null,
  "mlProbability": 0.36471671166140707,
  "reasonCodes": [],
  "evaluatedAt": "2026-06-03T15:22:13.446871Z",
  "modelName": "fraud_detection_logistic_regression",
  "modelVersion": "2.0.0-20260527-223628",
  "trainedAt": null
}
```

**ML Probability Color Coding (UI):**
| Range | Color | Meaning |
|-------|-------|---------|
| < 40% | `#10b981` (green) | Low fraud risk |
| 40-70% | `#f59e0b` (amber) | Medium fraud risk |
| > 70% | `#ef4444` (red) | High fraud risk |

**Used by:** `RiskDecisionPage.jsx` via `riskApi.js`

---

### Get AI Investigation Summary

```
GET /risk/{transactionId}/investigation-summary
Authorization: Bearer <token>
Role required: ADMIN
```

**Note:** This endpoint has a dedicated gateway route with 60-second timeout override because AI generation takes longer than the standard 5-second gateway timeout.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| transactionId | UUID | Transaction to summarise |

**Response:**
```json
{
  "transactionId": "2e3e92f4-8cf8-4ba5-a97b-7a177812389e",
  "status": "DECLINED",
  "summary": "Investigation Summary:\nThe transaction was declined due to exceeding limits...\n\nKey Risk Indicators:\n...\n\nRecommended Action:\n..."
}
```

**Summary Rendering:** The `\n` separated text is parsed into sections:
- Lines ending with `:` are rendered as gold section headers
- Other lines are rendered as indented paragraphs with a left border

**Used by:** `AISummaryPage.jsx` via `riskApi.js`

---

## Error Responses

All error responses from the gateway follow this format:

```json
{
  "timestamp": "2026-06-07T12:38:21.708723Z",
  "status": 401,
  "message": "Invalid or expired token",
  "correlationId": "Co-relationId:e3992be2-..."
}
```

| Status | Frontend Behaviour |
|--------|-------------------|
| 401 | Auto logout + redirect to /login (axiosClient interceptor) |
| 403 | Show generic "Access denied" error on page |
| 429 | Show generic "Too many requests" error on page |
| 503 | Show generic "Service temporarily unavailable" error on page |

Raw error messages are never shown to the user. Always generic.

---

*Gringotts Fraud Intelligence Platform — API Integration Documentation*
