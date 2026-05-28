# Notification Service API

## Overview

This API provides endpoints for managing notification processing failures and retrieving processed notification event details.

- OpenAPI Version: `3.1.0`
- API Version: `v0`
- Base URL: `http://localhost:8085`

---

# Base URL

```http
http://localhost:8085
````

---

# Endpoints

## 1. Get Failed Notifications

Retrieve paginated failed notification events with optional date filtering.

### Endpoint

```http
GET /api/v1//notifications/failed
```

### Query Parameters

| Parameter | Type     | Required | Description                                 |
| --------- | -------- | -------- | ------------------------------------------- |
| from      | datetime | No       | Filter failed notifications from timestamp  |
| to        | datetime | No       | Filter failed notifications until timestamp |
| page      | integer  | Yes      | Page number (starts from 0)                 |
| size      | integer  | Yes      | Number of records per page                  |
| sort      | string[] | No       | Sorting fields                              |

### Example Request

```http
GET /api/v1//notifications/failed?page=0&size=10&sort=failedAt,desc
```

### Example Response

```json
{
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "size": 10,
  "content": [
    {
      "id": "1b1c0f4e-3df0-4e73-9aef-d6cf6dcb5d10",
      "transactionId": "TXN-10001",
      "sourceTopic": "payment-events",
      "errorMessage": "Kafka publish failed",
      "payload": "{...}",
      "failedAt": "2026-05-18T10:30:00Z"
    }
  ],
  "number": 0,
  "numberOfElements": 1,
  "empty": false
}
```

### Response Codes

| Status Code | Description         |
| ----------- | ------------------- |
| 200         | Successful response |

---

## 2. Get Failed Notification By ID

Retrieve a single failed notification event by ID.

### Endpoint

```http
GET /api/v1//notifications/failed/{id}
```

### Path Parameters

| Parameter | Type | Required | Description            |
| --------- | ---- | -------- | ---------------------- |
| id        | UUID | Yes      | Failed notification ID |

### Example Request

```http
GET /api/v1//notifications/failed/1b1c0f4e-3df0-4e73-9aef-d6cf6dcb5d10
```

### Example Response

```json
{
  "id": "1b1c0f4e-3df0-4e73-9aef-d6cf6dcb5d10",
  "transactionId": "TXN-10001",
  "sourceTopic": "payment-events",
  "errorMessage": "Kafka publish failed",
  "payload": "{...}",
  "failedAt": "2026-05-18T10:30:00Z"
}
```

### Response Codes

| Status Code | Description         |
| ----------- | ------------------- |
| 200         | Successful response |

---

## 3. Retry Failed Notification

Retry processing a failed notification event.

### Endpoint

```http
POST /api/v1//notifications/failed/{id}/retry
```

### Path Parameters

| Parameter | Type | Required | Description            |
| --------- | ---- | -------- | ---------------------- |
| id        | UUID | Yes      | Failed notification ID |

### Example Request

```http
POST /api/v1//notifications/failed/1b1c0f4e-3df0-4e73-9aef-d6cf6dcb5d10/retry
```

### Example Response

```json
{
  "status": "SUCCESS",
  "message": "Notification retry initiated"
}
```

### Response Codes

| Status Code | Description                  |
| ----------- | ---------------------------- |
| 200         | Retry triggered successfully |

---

## 4. Get Processed Notification Event

Retrieve processed notification event details.

### Endpoint

```http
GET /api/v1//notifications/events/{eventId}
```

### Path Parameters

| Parameter | Type | Required | Description        |
| --------- | ---- | -------- | ------------------ |
| eventId   | UUID | Yes      | Processed event ID |

### Example Request

```http
GET /api/v1//notifications/events/1b1c0f4e-3df0-4e73-9aef-d6cf6dcb5d10
```

### Example Response

```json
{
  "eventId": "1b1c0f4e-3df0-4e73-9aef-d6cf6dcb5d10",
  "processedAt": "2026-05-18T11:00:00Z"
}
```

### Response Codes

| Status Code | Description         |
| ----------- | ------------------- |
| 200         | Successful response |

---

# Data Models

---

## FailedNotificationEvent

```json
{
  "id": "uuid",
  "transactionId": "string",
  "sourceTopic": "string",
  "errorMessage": "string",
  "payload": "string",
  "failedAt": "datetime"
}
```

| Field         | Type     | Description            |
| ------------- | -------- | ---------------------- |
| id            | UUID     | Unique failed event ID |
| transactionId | String   | Transaction identifier |
| sourceTopic   | String   | Kafka topic/source     |
| errorMessage  | String   | Failure reason         |
| payload       | String   | Original event payload |
| failedAt      | datetime | Failure timestamp      |

---

## NotificationProcessed

```json
{
  "eventId": "uuid",
  "processedAt": "datetime"
}
```

| Field       | Type     | Description                     |
| ----------- | -------- | ------------------------------- |
| eventId     | UUID     | Processed event ID              |
| processedAt | datetime | Processing completion timestamp |

---

# Pagination Structure

```json
{
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": false,
  "size": 10,
  "content": [],
  "number": 0,
  "numberOfElements": 0,
  "empty": false
}
```

---

# Notes

* All timestamps use ISO-8601 datetime format.
* UUID fields must be valid UUID strings.
* Pagination follows Spring Pageable conventions.
* Sorting supports multiple fields.

---

# Suggested Improvements

## Recommended Enhancements

* Add authentication and authorization.
* Add standard error response schema.
* Add API rate limiting.
* Add OpenAPI descriptions for all fields.
* Add request/response examples for error scenarios.
* Add retry status tracking endpoint.
* Add Swagger tags grouping by domain.

---

# Tech Stack Assumptions

Based on schema structure, this service appears to use:

* Spring Boot
* Spring Web
* Spring Data Pageable
* Kafka/Event-Driven Architecture
* UUID-based entity tracking

---

```
```
