# Frontend Architecture — Gringotts Fraud Intelligence Platform

---

## Overview

The Gringotts frontend is a React single-page application (SPA) that connects to a distributed Spring Boot microservices backend through a single API Gateway entry point. It implements role-based access control, in-memory JWT authentication, and a production-grade folder structure separating concerns cleanly.

---

## Layer Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI Layer                                │
│   Pages (Login, Dashboard, Admin, Transactions, Risk)           │
│   Components (AppLayout, MagicBackground, Sidebar)              │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                       State Layer                               │
│   AuthContext (global auth state)                               │
│   tokenManager (JWT in memory)                                  │
│   Local useState (per-page form/data state)                     │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      Routing Layer                              │
│   AppRouter (route definitions)                                 │
│   ProtectedRoute (auth + role guard)                            │
│   AppLayout (sidebar shell for protected pages)                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                       API Layer                                 │
│   axiosClient (JWT interceptor, base URL, error handling)       │
│   authApi / userApi / transactionApi / riskApi                  │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP + JWT Bearer
┌────────────────────────────▼────────────────────────────────────┐
│                    API Gateway :8087                             │
│   JWT validation → Rate limiting → Circuit breaker → Routing    │
└──────┬──────────────┬──────────────┬───────────────┬────────────┘
       │              │              │               │
   :8081          :8082          :8083           :8080
 User Svc    Transaction     Risk Decision     Keycloak
              Service          Service
```

---

## Authentication Flow

```
┌─────────┐         ┌──────────┐         ┌──────────┐
│ Browser │         │ Gateway  │         │ Keycloak │
└────┬────┘         └────┬─────┘         └────┬─────┘
     │                   │                    │
     │  POST /realms/gringotts/...token       │
     │──────────────────────────────────────►│
     │                   │                    │
     │◄──────────────────────────────────────│
     │     access_token (JWT, 5min TTL)       │
     │                   │                    │
     │  tokenManager.setToken(access_token)   │
     │  (stored in JS memory only)            │
     │                   │                    │
     │  GET /api/v1/users/profile             │
     │  Authorization: Bearer <token>         │
     │──────────────────►│                   │
     │                   │ validate JWT       │
     │                   │──────────────────►│
     │                   │◄──────────────────│
     │                   │  valid            │
     │                   │ route to service  │
     │◄──────────────────│                   │
     │    response        │                   │
```

---

## Role-Based Access Control Flow

```
User navigates to any URL
          │
          ▼
    AppRouter matches route
          │
          ▼
    ProtectedRoute
          │
          ├── isAuthenticated? ──No──► redirect /login
          │
          ▼ Yes
          │
          ├── requiredRole set? ──No──► render page (any auth user)
          │
          ▼ Yes
          │
          ├── tokenManager.hasRole(requiredRole)? ──No──► redirect /login
          │
          ▼ Yes
          │
    AppLayout wraps page
          │
          ▼
    Page renders with sidebar
```

---

## Request Lifecycle

Every API call goes through this pipeline:

```
Page calls API function (e.g. createUser(form))
          │
          ▼
userApi.js — builds the request
          │
          ▼
axiosClient REQUEST INTERCEPTOR
  → reads token from tokenManager
  → adds Authorization: Bearer <token> header
          │
          ▼
HTTP request sent to Gateway :8087
          │
          ▼
Gateway pipeline:
  1. JWT signature validation (Keycloak public key)
  2. JWT expiry check
  3. Audience validation (aud claim)
  4. Rate limiter (Redis token bucket)
  5. Circuit breaker check
  6. Route to microservice
          │
          ▼
Microservice processes request
  → Role check (@PreAuthorize)
  → Business logic
  → Database operation
          │
          ▼
Response returns to axiosClient RESPONSE INTERCEPTOR
  → 200-299: return response.data to page
  → 401: clearToken() → redirect /login
  → other errors: reject promise → page catches error
          │
          ▼
Page updates UI state
```

---

## Component Tree

```
AppRouter
├── /login → LoginPage (public, no layout)
│
└── /* (protected) → ProtectedRoute → AppLayout
    │
    ├── Sidebar (role-aware navigation)
    │   ├── ADMIN nav: Dashboard, Create User,
    │   │              All Transactions, Risk Decision,
    │   │              AI Fraud Summary
    │   └── USER nav: Dashboard, Create Transaction
    │
    └── Main Content (page-specific)
        ├── / → DashboardPage
        ├── /admin/create-user → CreateUserPage
        ├── /transactions/all → AllTransactionsPage
        ├── /transactions/create → CreateTransactionPage
        ├── /risk/decision → RiskDecisionPage
        └── /risk/ai-summary → AISummaryPage
```

---

## State Management

No external state library (Redux, Zustand) is used. State is managed at two levels:

### Global State — AuthContext

```javascript
{
  isAuthenticated: boolean,    // is user logged in
  isLoading: boolean,          // login request in flight
  error: string | null,        // login error message
  handleLogin(username, password),
  handleLogout(),
  tokenManager                 // exposed for role checks anywhere
}
```

### Local State — Per Page

Each page manages its own data:
```javascript
// Example: AllTransactionsPage
const [data, setData] = useState(null);       // API response
const [currentPage, setCurrentPage] = useState(0);
const [sortField, setSortField] = useState('createdAt');
const [sortDir, setSortDir] = useState('desc');
const [isLoading, setIsLoading] = useState(false);
const [error, setError] = useState(null);
```

---

## API Module Design

Each API module maps to one microservice:

```
authApi.js       → Keycloak (/realms/gringotts/...)
userApi.js       → User Service (/api/v1/users/...)
transactionApi.js→ Transaction Service (/api/v1/transactions/...)
riskApi.js       → Risk Service (/risk/...)
```

All calls go through `axiosClient` which:
- Sets `baseURL` from `VITE_GATEWAY_URL` environment variable
- Injects `Authorization` header automatically
- Handles `401` globally (logout + redirect)
- Sets 10 second timeout

---

## Environment Configuration

```
.env.local (development)         .env.production (AWS)
─────────────────────────        ─────────────────────────────────
VITE_GATEWAY_URL=                VITE_GATEWAY_URL=
  http://localhost:8087            https://api.yourdomain.com

VITE_KEYCLOAK_URL=               VITE_KEYCLOAK_URL=
  http://localhost:8080            https://auth.yourdomain.com

VITE_KEYCLOAK_REALM=gringotts   VITE_KEYCLOAK_REALM=gringotts
VITE_KEYCLOAK_CLIENT_ID=        VITE_KEYCLOAK_CLIENT_ID=
  gringotts-frontend               gringotts-frontend
```

Same codebase runs in both environments. Only the `.env` file changes.

---

## AI Development Architecture

```
Claude.ai (architecture + security + review)
          │
          ▼ specifications and prompts
Continue Extension (VS Code)
          │
          ▼ code generation
Ollama local models
  ├── qwen2.5-coder:14b  → chat, code generation, edit
  └── qwen2.5-coder:1.5b → autocomplete (fast, low memory)
          │
          ▼ generated code
Claude.ai (security audit + correction)
          │
          ▼ corrected code
Developer pastes into file
```

Zero paid AI API. Zero code leaves the machine during generation.

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| JWT in memory only | XSS cannot steal tokens from localStorage |
| No Redux/Zustand | Auth context + local state is sufficient for this scope |
| Axios over fetch | Interceptors make JWT injection and 401 handling clean |
| Single axiosClient | One place to change auth logic — no duplication |
| Role check client + server | Client-side is UX; server-side is actual security |
| Environment variables | Same code works locally and in production |
| Generic error messages | Never expose server internals to the browser |
| Vite strictPort | Prevents silent port switch that breaks Keycloak CORS |

---

*Gringotts Fraud Intelligence Platform — Architecture Documentation*
