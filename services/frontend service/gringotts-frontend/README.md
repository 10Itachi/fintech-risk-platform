# Gringotts Fraud Intelligence Platform — Frontend

> A production-grade React frontend for the Gringotts Fraud Intelligence Platform — a distributed financial fraud detection and AI-assisted investigation system built on Spring Boot microservices.

---

## Overview

The Gringotts frontend is a secure, role-based single-page application that connects to six Spring Boot microservices through an API Gateway. It provides fraud analysts and bank administrators with real-time access to transaction data, ML-powered risk decisions, and GenAI fraud investigation summaries.

---

## Tech Stack

| Layer          | Technology                                      |
| -------------- | ----------------------------------------------- |
| Framework      | React 18 + Vite 8                               |
| Routing        | React Router DOM v6                             |
| HTTP Client    | Axios                                           |
| Icons          | Lucide React                                    |
| Animation      | Framer Motion                                   |
| Auth           | Keycloak (OpenID Connect)                       |
| Token Strategy | In-memory JWT (never localStorage)              |
| AI Dev Tools   | Ollama (qwen2.5-coder:14b) + Continue (VS Code) |
| Node Version   | v24.16.0 via NVM                                |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Browser                          │
│                                                     │
│   React SPA (localhost:5173)                        │
│   ┌──────────┐  ┌────────────┐  ┌───────────────┐  │
│   │ AuthCtx  │  │ AppRouter  │  │ tokenManager  │  │
│   │ (global) │  │ (routing)  │  │ (JWT memory)  │  │
│   └──────────┘  └────────────┘  └───────────────┘  │
└────────────────────────┬────────────────────────────┘
                         │ HTTPS / JWT Bearer
                         ▼
┌─────────────────────────────────────────────────────┐
│            API Gateway (localhost:8087)              │
│                                                     │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │ JWT Validate│  │ Rate Limiter │  │  Circuit  │  │
│  │ (Keycloak)  │  │   (Redis)    │  │  Breaker  │  │
│  └─────────────┘  └──────────────┘  └───────────┘  │
└──────┬───────────────────────────────────┬──────────┘
       │                                   │
  ┌────┴─────┐                       ┌─────┴──────┐
  │  User    │  Transaction Service  │   Risk     │
  │ Service  │  (localhost:8082)     │  Decision  │
  │ :8081    │                       │  Service   │
  └──────────┘                       │  :8083     │
                                     └────────────┘
```

---

## Project Structure

```
src/
├── api/                    # All backend API calls
│   ├── axiosClient.js      # Axios instance with JWT interceptor
│   ├── authApi.js          # Keycloak login/logout
│   ├── userApi.js          # User service calls
│   ├── transactionApi.js   # Transaction service calls
│   └── riskApi.js          # Risk decision + AI summary calls
│
├── auth/
│   └── tokenManager.js     # JWT in-memory storage (never localStorage)
│
├── context/
│   └── AuthContext.jsx     # Global auth state provider
│
├── router/
│   ├── AppRouter.jsx       # Route definitions + layout wrapping
│   └── ProtectedRoute.jsx  # Role-based route guard
│
├── components/common/
│   ├── AppLayout.jsx       # Sidebar layout shell
│   └── MagicBackground.jsx # Hogwarts themed background + particles
│
├── pages/
│   ├── Login/              # Public login page
│   ├── Dashboard/          # Role-aware landing page
│   ├── Admin/              # Create User (ADMIN only)
│   ├── Transactions/       # Create + View All Transactions
│   └── Risk/               # Risk Decision + AI Fraud Summary
│
└── assets/
    └── gringotts-bank.avif # Hogwarts themed background image
```

---

## Authentication Flow

```
User enters credentials
        │
        ▼
POST /realms/gringotts/protocol/openid-connect/token
        │
        ▼
Keycloak validates → issues JWT
        │
        ▼
tokenManager.setToken(access_token)
  - Stored in JavaScript memory only
  - Never written to localStorage / sessionStorage / cookies
  - Decoded to extract roles and username
  - Cleared on logout or 401 response
        │
        ▼
AuthContext sets isAuthenticated = true
        │
        ▼
AppRouter reads role from tokenManager
  - ADMIN → sees Admin + Transaction + Risk pages
  - USER  → sees Create Transaction only
        │
        ▼
Every API call via axiosClient adds:
  Authorization: Bearer <token>
        │
        ▼
Gateway validates JWT → routes to microservice
```

---

## Pages and Access Control

| Page               | Route                  | Role              | Backend Endpoint                                       |
| ------------------ | ---------------------- | ----------------- | ------------------------------------------------------ |
| Login              | `/login`               | Public            | `POST /realms/gringotts/protocol/openid-connect/token` |
| Dashboard          | `/`                    | Any authenticated | —                                                      |
| Create User        | `/admin/create-user`   | ADMIN             | `POST /api/v1/users/admin/createUser`                  |
| All Transactions   | `/transactions/all`    | ADMIN             | `GET /api/v1/transactions/admin/allUsers`              |
| Create Transaction | `/transactions/create` | USER              | `POST /api/v1/transactions/user/createa`               |
| Risk Decision      | `/risk/decision`       | ADMIN             | `GET /risk/api/v1/decisions/transaction/{id}`          |
| AI Fraud Summary   | `/risk/ai-summary`     | ADMIN             | `GET /risk/{id}/investigation-summary`                 |

---

## Security Implementation

### JWT Token Strategy

- Token stored **in JavaScript memory only** — never in `localStorage`, `sessionStorage`, or cookies
- Token is automatically cleared on logout or any `401` response from the backend
- Token expires in 5 minutes (configured in Keycloak)
- On page refresh, token is lost — user must re-authenticate (intentional security trade-off)

### Route Protection

- Every non-public route is wrapped in `ProtectedRoute`
- Role check happens client-side AND server-side (gateway enforces roles independently)
- Typing a protected URL directly in the browser redirects to `/login`

### Environment Variables

- All URLs and config stored in `.env.local` (local) and `.env.production` (cloud)
- All env vars prefixed with `VITE_` — only these are exposed to the browser bundle
- `.env` files are excluded from git via `.gitignore`
- No secrets, API keys, or credentials are hardcoded anywhere in source code

### Error Messages

- All error messages shown to the user are generic — raw server errors are never exposed
- No `console.log` of sensitive data anywhere in the codebase

---

## Local Development Setup

### Prerequisites

- macOS with Homebrew
- Node.js v18+ via NVM
- Ollama with `qwen2.5-coder:14b` model
- Continue extension in VS Code
- All 6 backend microservices running
- Keycloak running on Docker (`localhost:8080`)

### Install and Run

```bash
# Clone the repo
git clone <your-repo-url>
cd gringotts-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

App runs at `http://localhost:5173`

### Environment Configuration

Create `.env.local` in the project root:

```env
VITE_GATEWAY_URL=http://localhost:8087
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=gringotts
VITE_KEYCLOAK_CLIENT_ID=gringotts-frontend
```

---

## Keycloak Configuration

### Client Settings (`gringotts-frontend`)

| Setting               | Value                     |
| --------------------- | ------------------------- |
| Client Type           | OpenID Connect            |
| Client Authentication | OFF (public client)       |
| Direct Access Grants  | ON                        |
| Valid Redirect URIs   | `http://localhost:5173/*` |
| Web Origins           | `http://localhost:5173`   |

### Required Client Scope

Assign `Api-audience-scope` to `gringotts-frontend` with **Default** type.

This adds all microservice audiences to the JWT:

- `risk-decision-service`
- `transaction-service`
- `user-service`
- `notification-service`
- `transaction-observability-service`

---

## AI Development Workflow

This project was built using a **zero-cost local AI development stack**:

```
Claude.ai (free tier)
  → Architecture decisions
  → Security design
  → Code review and audit
  → Bug diagnosis

Continue Extension (VS Code) + Ollama (local)
  → Code generation inside the editor
  → Autocomplete (qwen2.5-coder:1.5b)
  → Chat assistance (qwen2.5-coder:14b)
  → Zero data leaves the machine
```

No paid AI API was used. All code generation ran locally via Ollama.

---

## Production Build

```bash
npm run build
```

Output goes to `dist/` directory. Configure `.env.production` before building:

```env
VITE_GATEWAY_URL=https://api.yourdomain.com
VITE_KEYCLOAK_URL=https://auth.yourdomain.com
VITE_KEYCLOAK_REALM=gringotts
VITE_KEYCLOAK_CLIENT_ID=gringotts-frontend
```

---

## Related Repositories

| Service                             | Description                                         |
| ----------------------------------- | --------------------------------------------------- |
| `gateway-service`                   | Spring Cloud Gateway — auth, routing, rate limiting |
| `user-service`                      | User management — Keycloak integration              |
| `transaction-service`               | Transaction processing — event driven               |
| `risk-decision-service`             | ML fraud scoring + GenAI summaries                  |
| `notification-service`              | Event-driven notifications                          |
| `transaction-observability-service` | Metrics and audit trail                             |

---

_Gringotts Fraud Intelligence Platform — Built with Spring Boot, React, Keycloak, Redis, and Local AI_
