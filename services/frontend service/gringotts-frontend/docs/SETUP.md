# Local Development Setup Guide

Complete step-by-step guide to run the Gringotts frontend locally.

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| macOS | Any recent | Development OS |
| Homebrew | Latest | macOS package manager |
| Node.js | v18+ (v24 recommended) | JavaScript runtime |
| NVM | Latest | Node version manager |
| Git | Any | Version control |
| VS Code | Latest | IDE |
| Ollama | Latest | Local AI models |
| Docker | Latest | Keycloak container |

---

## Step 1 — Install Homebrew

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

Follow the "Next steps" output to add brew to PATH.

---

## Step 2 — Install Node.js via NVM

```bash
# Install NVM
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash

# Restart terminal, then:
nvm install --lts
nvm use --lts
nvm alias default node

# Verify
node --version   # v20.x or v24.x
npm --version    # 10.x or 11.x
```

---

## Step 3 — Install Ollama and AI Models

```bash
# Install Ollama
brew install ollama

# Pull coding models
ollama pull qwen2.5-coder:14b    # chat + code generation
ollama pull qwen2.5-coder:1.5b   # autocomplete (fast)
ollama pull nomic-embed-text     # codebase indexing

# Verify
ollama list
```

---

## Step 4 — Install Continue Extension in VS Code

1. Open VS Code
2. Press `Cmd + Shift + X`
3. Search: `Continue`
4. Install the Continue extension (purple icon)
5. Configure `~/.continue/config.yaml`:

```yaml
name: Local Config
version: 1.0.0
schema: v1
models:
  - name: Qwen2.5-Coder 14B
    provider: ollama
    model: qwen2.5-coder:14b
    roles:
      - chat
      - edit
      - apply
  - name: Qwen2.5-Coder 1.5B
    provider: ollama
    model: qwen2.5-coder:1.5b-base
    roles:
      - autocomplete
  - name: Nomic Embed
    provider: ollama
    model: nomic-embed-text:latest
    roles:
      - embed
allowAnonymousTelemetry: false
```

---

## Step 5 — Start Backend Services

Ensure all Spring Boot services are running in IntelliJ:

| Service | Port |
|---------|------|
| gateway-service | 8087 |
| user-service | 8081 |
| transaction-service | 8082 |
| risk-decision-service | 8083 |
| notification-service | 8085 |
| transaction-observability-service | 8084 |

And infrastructure:

| Service | Port |
|---------|------|
| Keycloak (Docker) | 8080 |
| Redis | 6379 |
| MySQL | 3306 |

---

## Step 6 — Configure Keycloak

### Create Frontend Client

1. Open `http://localhost:8080`
2. Login as admin → select `gringotts` realm
3. Clients → Create client

| Field | Value |
|-------|-------|
| Client ID | `gringotts-frontend` |
| Client authentication | OFF |
| Direct access grants | ON |
| Valid redirect URIs | `http://localhost:5173/*` |
| Web origins | `http://localhost:5173` |

### Assign Audience Scope

Clients → `gringotts-frontend` → Client Scopes → Add `Api-audience-scope` → Default

### Verify Token

```bash
curl -X POST http://localhost:8080/realms/gringotts/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=gringotts-frontend" \
  -d "username=YOUR_USERNAME" \
  -d "password=YOUR_PASSWORD"
```

Should return `access_token`.

---

## Step 7 — Install and Run Frontend

```bash
# Clone and navigate
git clone <your-repo>
cd gringotts-frontend

# Install dependencies
npm install

# Create environment file
cat > .env.local << 'EOF'
VITE_GATEWAY_URL=http://localhost:8087
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=gringotts
VITE_KEYCLOAK_CLIENT_ID=gringotts-frontend
EOF

# Start development server
npm run dev
```

Open `http://localhost:5173`

---

## Step 8 — Verify Everything Works

| Check | Expected |
|-------|---------|
| Login page loads | Hogwarts background visible |
| Login with admin user | Redirects to dashboard |
| Dashboard shows ADMIN role | 4 nav items in sidebar |
| Create User works | Shows user ID + status card |
| All Transactions loads | Paginated table with sort |
| Risk Decision lookup | Returns ML probability + status |
| AI Fraud Summary | Generates after a few seconds |
| Logout | Returns to login, token cleared |

---

## Common Issues

### Port 5173 Already in Use
```bash
lsof -i :5173          # find the process
kill -9 <PID>          # kill it
npm run dev            # restart
```

### CORS Error on Login
Check Keycloak `gringotts-frontend` client → Web Origins includes `http://localhost:5173`

Use `+` as Web Origins value to automatically trust all redirect URIs.

### 401 on Risk/AI Summary
Ensure `Api-audience-scope` is assigned to `gringotts-frontend` client in Keycloak.

### AI Summary Returns 503
Gateway timeout — ensure `risk-service-ai-summary` route exists in `application.yml` with `metadata: response-timeout: 60000`

### Token Shows Wrong Role
Logout and login again — role is read from token at login time.

---

## Production Build

```bash
# Update production env
cat > .env.production << 'EOF'
VITE_GATEWAY_URL=https://api.yourdomain.com
VITE_KEYCLOAK_URL=https://auth.yourdomain.com
VITE_KEYCLOAK_REALM=gringotts
VITE_KEYCLOAK_CLIENT_ID=gringotts-frontend
EOF

# Build
npm run build

# Preview build locally
npm run preview
```

Output in `dist/` — deploy to AWS S3 + CloudFront or any static host.

---

*Gringotts Fraud Intelligence Platform — Setup Guide*
