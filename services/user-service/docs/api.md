# API Documentation

## 1. Overview

This document describes the REST APIs exposed by the User Service, including request/response formats, authorization requirements, and behavior.

Base URL:

```text
/api/v1/users
```

Authentication:

* All endpoints require a valid JWT (except public endpoints like Swagger/Actuator)
* JWT must be sent in:

```http
Authorization: Bearer <access_token>
```

---

## 2. Endpoints

---

### 2.1 Create User

```http
POST /api/v1/users
```

Authorization:

* Role: ADMIN

Request Body:

```json
{
  "userName": "john_doe",
  "password": "securePassword",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "role": "USER"
}
```

Response:

```json
{
  "userId": 1,
  "userName": "john_doe",
  "isActive": "ACTIVE"
}
```

---

### 2.2 Get Current User Profile

```http
GET /api/v1/users/profile
```

Authorization:

* Any authenticated user

Response:

```json
{
  "userId": 1,
  "userName": "john_doe",
  "isActive": "ACTIVE"
}
```

---

### 2.3 Get User by ID

```http
GET /api/v1/users/{userId}
```

Authorization:

* ADMIN OR resource owner

Access Rule:

```text
hasRole('ADMIN') OR userId == JWT.subject
```

Response:

```json
{
  "userId": 1,
  "userName": "john_doe",
  "isActive": "ACTIVE"
}
```

---

### 2.4 Get All Users (Paginated)

```http
GET /api/v1/users?lastId={lastId}&size={size}
```

Authorization:

* ADMIN

Query Parameters:

| Parameter | Description           |
| --------- | --------------------- |
| lastId    | Cursor for pagination |
| size      | Number of records     |

Response:

```json
[
  {
    "userId": 2,
    "userName": "alice",
    "isActive": "ACTIVE"
  }
]
```

---

### 2.5 Deactivate User

```http
PATCH /api/v1/users/{userId}/deactivate
```

Authorization:

* ADMIN

Behavior:

* Marks user inactive in DB
* Disables user in Keycloak

Response:

```json
{
  "message": "User deactivated successfully"
}
```
### 2.6 get User By Id 

```http
GET /api/v1/users/shared/getUserById/:userId
```

Authorization:

* ADMIN and any valid resource owner

Behavior:

* gets user by id

Response:

```json
{
  "userId": "787c8a73-5674-42aa-a84d-5ba32f894e66",
  "userName": "user2",
  "isActive": "ACTIVE"
}
```
---

### 2.6 Activate User

```http
PATCH /api/v1/users/{userId}/activate
```

Authorization:

* ADMIN

Behavior:

* Updates DB status
* Enables user in Keycloak

Response:

```json
{
  "message": "User activated successfully"
}
```

---

## 3. Error Responses

### 401 Unauthorized

```json
{
  "message": "Unauthorized"
}
```

---

### 403 Forbidden

```json
{
  "message": "Forbidden"
}
```

---

### 400 Bad Request

Validation errors:

```json
{
  "message": "Invalid input"
}
```

---

### 404 Not Found

```json
{
  "message": "User not found"
}
```

---

## 4. Security Notes

* JWT must be valid (signature, expiry, issuer)
* Roles are extracted from Keycloak token
* Ownership-based access enforced via JWT claims

---

## 5. Pagination Strategy

* Cursor-based pagination using `lastId`
* Avoids performance issues of offset pagination
* Scales better for large datasets

---

## 6. API Design Principles

* RESTful conventions
* Clear separation of DTOs and entities
* Stateless interactions
* Consistent response structure
