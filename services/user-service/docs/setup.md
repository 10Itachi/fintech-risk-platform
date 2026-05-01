# Setup Guide — User Service

## 1. Overview

This document describes how to set up and run the **User Service** locally using Docker.

The service depends on external systems:

* MySQL (database)
* Redis (caching)
* Keycloak (authentication / identity provider)

Ensure these dependencies are running before starting the service.

---

## 2. Prerequisites

Install the following:

* Java 17+
* Maven (or use `mvnw`)
* Docker

Optional (for local infra):

* MySQL
* Redis
* Keycloak

---

## 3. Environment Configuration

### Step 1: Create environment file

```bash
cp .env.example .env
```

---

### Step 2: Update `.env`

Edit values according to your local setup:

```text
USER_SERVICE_PORT=8081

USER_DB_URL=jdbc:mysql://localhost:3306/gbank_user_db
USER_DB_USERNAME=root
USER_DB_PASSWORD=your-password

USER_ISSUER_URI=http://localhost:8080/realms/gringotts
USER_KEYCLOAK_SERVER_URL=http://localhost:8080

REDIS_HOST=localhost
REDIS_PORT=6379
```

---

### Notes

* Use `localhost` when running dependencies on your machine
* In Docker-based setups, replace with service names (e.g., `mysql`, `redis`, `keycloak`)

---

## 4. Running the Service (Docker)

### Step 1: Build the application

```bash
mvn clean package
```

This generates the JAR file in `target/`.

---

### Step 2: Build Docker image

```bash
docker build -t user-service .
```

---

### Step 3: Run container

```bash
docker run -p 8081:8080 --env-file .env user-service
```

---

### Step 4: Verify

```bash
curl http://localhost:8081/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

---

## 5. Running Without Docker (Optional)

```bash
cp .env.example .env
mvn spring-boot:run
```

---

## 6. Required Dependencies

Ensure the following services are available:

---

### MySQL

* Port: `3306`
* Database: `gbank_user_db`

---

### Redis

* Port: `6379`

---

### Keycloak

* URL: `http://localhost:8080`
* Realm: `gringotts`
* Client: `user-service`

---

## 7. Common Issues

---

### Issue: Database connection failure

**Cause:**
Incorrect DB URL or MySQL not running

**Fix:**

* Verify MySQL is running
* Check credentials in `.env`

---

### Issue: Keycloak authentication failure

**Cause:**
Incorrect issuer or client config

**Fix:**

* Validate realm and client in Keycloak
* Check `USER_ISSUER_URI`

---

### Issue: Redis not connecting

**Cause:**
Wrong host or port

**Fix:**

* Ensure Redis is running
* Validate `REDIS_HOST` and `REDIS_PORT`

---

### Issue: Port already in use

**Fix:**

```bash
lsof -i :8081
kill -9 <PID>
```

---

## 8. Logs

View container logs:

```bash
docker logs <container-id>
```

---

## 9. Development Workflow

```bash
# Update code
mvn clean package

# Rebuild image
docker build -t user-service .

# Restart container
docker run -p 8081:8080 --env-file .env user-service
```

---

## 10. Security Notes

* Do NOT commit `.env` to Git
* Rotate secrets before sharing
* Use `.env.example` for configuration reference

---

## 11. Summary

* `.env.example` → template
* `.env` → runtime configuration
* Docker → executes service
* External services → must be running

The service is fully self-contained and can be run independently by any developer using the steps above.
