# Security Documentation

## 1. Overview

The User Service enforces stateless, token-based security using OAuth2 and OpenID Connect (OIDC). Authentication is delegated to an external identity provider (Keycloak). The service validates JWT access tokens and applies authorization using role-based and ownership-based rules.

The service operates as an OAuth2 Resource Server and does not store user credentials.

---

## 2. Security Architecture

```text id="sec-arch"
Client
  ↓
(Obtains token from Keycloak)
  ↓
User Service (Resource Server)
   ├── Security Filter Chain
   ├── JWT Validation (signature, expiry, issuer)
   ├── Role Mapping (Keycloak → Spring Security)
   └── Authorization (URL + Method level)
```

Key properties:

* Stateless authentication (no server-side sessions)
* Externalized identity (Keycloak)
* Local token validation (no runtime dependency on Keycloak for each request)

---

## 3. Authentication Model

### 3.1 Protocols

* OAuth2 (authorization framework)
* OIDC (authentication layer)

### 3.2 Tokens Used

* Access Token (JWT): used for API authorization
* Refresh Token: used by client to obtain new access tokens (never sent to APIs)
* ID Token: contains identity claims (not used for API authorization)

---

## 4. JWT Validation Flow

Configured in

```text id="sec-flow"
Incoming Request (Authorization: Bearer <token>)
  ↓
SecurityFilterChain
  ↓
BearerTokenAuthenticationFilter
  ↓
JwtAuthenticationProvider
  ↓
JwtDecoder (auto-configured via issuer-uri)
  ↓
Signature validation (Keycloak public key)
  ↓
Expiry and issuer validation
  ↓
KeycloakJwtConverter (map roles)
  ↓
SecurityContextHolder populated
  ↓
Authorization checks
```

Key points:

* JwtDecoder is auto-configured from `issuer-uri`
* Public keys are fetched from Keycloak JWK endpoint and cached
* Validation is local and does not require a network call per request

---

## 5. Role Mapping

Implemented in

Keycloak provides roles in:

```json id="sec-roles-json"
"realm_access": {
  "roles": ["ADMIN", "USER"]
}
```

Spring Security expects:

```text id="sec-roles-spring"
ROLE_ADMIN, ROLE_USER
```

The converter transforms:

```text id="sec-role-map"
realm_access.roles → ROLE_<ROLE>
```

This enables:

```java id="sec-preauth"
@PreAuthorize("hasRole('ADMIN')")
```

---

## 6. Authorization Model

### 6.1 URL-Level Authorization

Configured in

* Public endpoints:

    * Swagger
    * Actuator health

* Protected endpoints:

    * All APIs require authentication
    * `/actuator/**` restricted to ADMIN

---

### 6.2 Method-Level Authorization

Enabled via `@EnableMethodSecurity`

Examples:

* Role-based:

  ```java id="sec-role"
  @PreAuthorize("hasRole('ADMIN')")
  ```

* Ownership-based:

  ```java id="sec-owner"
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.subject")
  ```

---

## 7. Session Management

Configured as stateless in

```text id="sec-stateless"
SessionCreationPolicy.STATELESS
```

Implications:

* No HTTP sessions
* Each request must carry a valid JWT
* Horizontally scalable architecture

---

## 8. Error Handling

Defined in

* 401 Unauthorized:

    * Missing or invalid token
* 403 Forbidden:

    * Valid token but insufficient permissions

Custom JSON responses are returned for API consistency.

---

## 9. Keycloak Integration Security

Configured in

* Uses Client Credentials flow
* Service authenticates to Keycloak using clientId and clientSecret
* Connection pooling and timeouts are configured

Security characteristics:

* No user credentials stored in service
* All identity operations delegated to Keycloak
* Role assignment handled centrally

---

## 10. Data Security

### 10.1 Password Handling

* Passwords are never stored in the service database
* All credential management is handled by Keycloak

### 10.2 Sensitive Data

* JWT tokens must not be logged
* Logs use trace identifiers (MDC) without exposing credentials

### 10.3 Transport Security

* All communication must use HTTPS
* Prevents token interception (MITM attacks)

---

## 11. Threat Model and Mitigation

### 11.1 CSRF (Cross-Site Request Forgery)

* Not applicable to JWT in Authorization header
* CSRF protection disabled safely due to stateless design

---

### 11.2 XSS (Cross-Site Scripting)

Risk:

* Token theft if stored insecurely

Mitigation:

* Avoid exposing tokens in browser storage where possible
* Sanitize inputs at frontend

---

### 11.3 Token Theft / Replay

Risk:

* JWT is a bearer token

Mitigation:

* HTTPS enforced
* Short access token lifespan
* Refresh token rotation handled by Keycloak

---

### 11.4 Token Expiry

* Expired tokens are rejected by JwtDecoder
* Clients must use refresh tokens to obtain new access tokens

---

### 11.5 Keycloak Downtime

* Existing valid tokens continue to work
* New logins and token refresh fail

Mitigation:

* Deploy Keycloak in high availability mode
* Configure appropriate token lifetimes

---

## 12. Security Best Practices Applied

* Stateless authentication
* External identity provider
* Local JWT validation
* Role-based access control
* Method-level security
* No credential storage in service
* Secure integration with identity provider

---

## 13. Known Trade-offs

* JWT cannot be easily revoked before expiry
* Token leakage risk if not handled securely
* Dependency on external identity provider for authentication

---

## 14. Future Improvements

* API Gateway-level authentication and rate limiting
* Token introspection or revocation strategy
* Integration with centralized secrets management
* Distributed tracing for security auditing
* Fine-grained authorization (ABAC)

---

## 15. Summary

The system implements a modern, secure, and scalable authentication and authorization model by:

* Delegating identity to Keycloak
* Using JWT for stateless authorization
* Applying role and ownership-based access control
* Protecting against common web security threats

This approach aligns with industry best practices for microservices security.
