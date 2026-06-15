# Security Architecture — Gringotts Frontend

This document describes the security model implemented in the Gringotts frontend and the reasoning behind each decision.

---

## Security Principles

1. **Never trust the client** — all security decisions are enforced server-side independently
2. **Minimal exposure** — tokens live only as long as needed, in the smallest possible scope
3. **Defence in depth** — multiple layers of protection, no single point of failure
4. **Fail securely** — on any auth error, clear state and redirect to login

---

## JWT Token Security

### Storage Strategy

```
❌ localStorage        — persists after tab close, accessible via XSS
❌ sessionStorage      — accessible via XSS
❌ Cookies (httpOnly)  — not used (Keycloak public client)
✅ JavaScript memory   — cleared on tab close, inaccessible via XSS
```

### Implementation

```javascript
// tokenManager.js
let accessToken = null;  // lives only in JS memory

setToken(token) {
  accessToken = token;
  // Decode JWT payload to extract roles — NOT for verification
  // Signature verification happens on the backend gateway
}

clearToken() {
  accessToken = null;
  userRoles = [];
  userName = null;
}
```

### Security Trade-off
Token is lost on page refresh — user must re-authenticate. This is an intentional security decision. A banking/fraud platform should require re-authentication rather than persist sessions indefinitely.

---

## Token Lifecycle

```
Login
  │
  ▼
Keycloak issues JWT (expires in 5 minutes)
  │
  ▼
tokenManager stores in memory
  │
  ├── On logout → clearToken() → redirect /login
  ├── On 401 response → clearToken() → redirect /login (axiosClient interceptor)
  ├── On tab close → memory cleared automatically by browser
  └── On page refresh → memory cleared → user re-authenticates
```

---

## Route Protection

### Client-Side Guards

```
User navigates to /admin/create-user
        │
        ▼
ProtectedRoute checks isAuthenticated
  → false → redirect /login
        │
        ▼
ProtectedRoute checks tokenManager.hasRole('ADMIN')
  → false → redirect /login
        │
        ▼
Page renders
```

### Server-Side Enforcement (Independent Layer)

Even if client-side guards are bypassed, the gateway independently validates:
- JWT signature (Keycloak public key)
- JWT expiry
- JWT issuer
- Audience claim (`aud` must include the target service)
- Role claim (`realm_access.roles`)

Client-side protection is UX. Server-side protection is security.

---

## Environment Variables

### What Goes in .env Files

```env
# .env.local — never committed to git
VITE_GATEWAY_URL=http://localhost:8087
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=gringotts
VITE_KEYCLOAK_CLIENT_ID=gringotts-frontend
```

### What NEVER Goes in .env Files
- Passwords
- Client secrets
- API keys
- Database credentials
- Any value that grants access to a system

### Git Protection

```
# .gitignore — these files are never committed
.env
.env.local
.env.production
.env.*.local
```

### Why VITE_ Prefix
Only variables prefixed with `VITE_` are included in the browser bundle. Variables without this prefix remain server-side only. This prevents accidental exposure of sensitive config.

---

## CORS Configuration

CORS is configured at the **gateway level** — not the frontend. The gateway only allows:

```java
config.setAllowedOrigins(List.of("http://localhost:5173"));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-ID"));
config.setAllowCredentials(true);
```

In production, `allowedOrigins` must be updated to the production domain. Wildcard (`*`) is never used.

---

## Keycloak Client Security

### Public Client (No Secret)
`gringotts-frontend` is a **public client** — no client secret. This is correct for browser applications because:
- Client secrets cannot be kept secret in a browser
- The browser bundle is visible to anyone who opens DevTools
- Security relies on JWT validation, not client authentication

### Direct Access Grants
Enabled to allow username/password login (same as Postman). In production, consider disabling this and using Authorization Code Flow with PKCE for stronger security.

### Audience Validation
The `Api-audience-scope` client scope ensures the JWT includes all microservice audiences. The risk-decision-service additionally validates the `azp` (authorized party) claim to ensure only trusted clients call it.

---

## What is Visible in Browser DevTools

### Network Tab
- Request URLs — intentional, gateway is the only entry point
- JWT token in the `/token` response — expected, HTTPS encrypts in transit
- API responses — protected by JWT, useless without valid token

### Application Tab
- localStorage — **empty** (by design)
- sessionStorage — **empty** (by design)
- Cookies — no auth cookies set

### Sources Tab
- React component names — harmless
- API path patterns — harmless, gateway enforces auth
- `VITE_` env variables compiled into bundle — only non-sensitive config values

### What is NOT Visible
- Passwords (never stored after form submission)
- JWT after the login network request completes
- Other users' data
- Backend service internals

---

## Input Validation

### Account Format (Frontend)
```javascript
const ACCOUNT_REGEX = /^ACC-[A-Z]{2,3}-[0-9]{6,12}$/;
```
Validated before API call to prevent unnecessary requests and give immediate feedback.

### Backend Validation (Independent)
The transaction service independently validates all fields with Java Bean Validation annotations. Frontend validation is UX. Backend validation is security.

---

## Error Handling Security

### Generic Error Messages
```javascript
// Never expose raw server errors
catch {
  setError('Invalid username or password');        // login
  setError('Failed to create user. Please try again.');  // create user
  setError('Transaction failed. Please try again.');     // transaction
}
```

Raw error messages from the server are never shown to the user. They could reveal:
- Database structure
- Service names and versions
- Internal error codes
- Stack traces

---

## Production Security Checklist

Before deploying to AWS:

- [ ] `.env.production` uses HTTPS URLs only
- [ ] Keycloak Web Origins updated to production domain
- [ ] Gateway CORS `allowedOrigins` updated to production domain
- [ ] All services behind VPC — not publicly accessible except gateway
- [ ] Gateway behind HTTPS load balancer (AWS ALB with SSL certificate)
- [ ] Keycloak behind HTTPS
- [ ] Token expiry reviewed (currently 5 minutes)
- [ ] `console.log` audit — confirm no sensitive data logged
- [ ] `.env.production` excluded from git (verify with `git status`)

---

*Gringotts Fraud Intelligence Platform — Security Documentation*
