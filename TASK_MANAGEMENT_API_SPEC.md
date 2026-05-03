# Task Management API Specification

## Overview

| Property       | Value                          |
|----------------|--------------------------------|
| API Name       | Task Management API            |
| Base Path      | `/api/v1`                      |
| Version        | 1.0.0                          |
| Format         | JSON                           |
| Auth           | None (public for demo scope)   |

---

## Domain Model

### Task Entity

| Field         | Type     | Constraints                              | Description                          |
|---------------|----------|------------------------------------------|--------------------------------------|
| `id`          | Long     | Auto-generated, read-only                | Unique identifier                    |
| `title`       | String   | Required, 1–100 characters               | Short title of the task              |
| `description` | String   | Optional, max 500 characters             | Detailed description                 |
| `status`      | Enum     | Required: `TODO`, `IN_PROGRESS`, `DONE`  | Current status of the task           |
| `priority`    | Enum     | Required: `LOW`, `MEDIUM`, `HIGH`        | Task priority level                  |
| `createdAt`   | DateTime | Auto-generated, read-only (ISO 8601)     | Creation timestamp                   |
| `updatedAt`   | DateTime | Auto-generated, read-only (ISO 8601)     | Last update timestamp                |

### Enum Definitions

#### TaskStatus
```
TODO        - Task has been created but not yet started
IN_PROGRESS - Task is actively being worked on
DONE        - Task has been completed
```

#### TaskPriority
```
LOW    - Low priority, can be addressed when time permits
MEDIUM - Standard priority
HIGH   - Urgent, must be addressed as soon as possible
```

---

## API Endpoints

### 1. Create a Task

| Property      | Value              |
|---------------|--------------------|
| Method        | `POST`             |
| Path          | `/api/v1/tasks`    |
| Description   | Creates a new task |

#### Request Body
```json
{
  "title": "Fix login bug",
  "description": "Users cannot log in with Google OAuth",
  "status": "TODO",
  "priority": "HIGH"
}
```

#### Field Rules

| Field         | Required | Constraints            |
|---------------|----------|------------------------|
| `title`       | Yes      | 1–100 characters       |
| `description` | No       | Max 500 characters     |
| `status`      | Yes      | `TODO`, `IN_PROGRESS`, `DONE` |
| `priority`    | Yes      | `LOW`, `MEDIUM`, `HIGH`|

#### Response — `201 Created`
```json
{
  "id": 1,
  "title": "Fix login bug",
  "description": "Users cannot log in with Google OAuth",
  "status": "TODO",
  "priority": "HIGH",
  "createdAt": "2026-05-03T10:00:00Z",
  "updatedAt": "2026-05-03T10:00:00Z"
}
```

---

### 2. Get All Tasks

| Property    | Value                                       |
|-------------|---------------------------------------------|
| Method      | `GET`                                       |
| Path        | `/api/v1/tasks`                             |
| Description | Retrieves a paginated list of all tasks     |

#### Query Parameters

| Parameter  | Type    | Required | Default | Description                                         |
|------------|---------|----------|---------|-----------------------------------------------------|
| `page`     | Integer | No       | `0`     | Zero-based page index                               |
| `size`     | Integer | No       | `20`    | Number of items per page (max 100)                  |
| `sort`     | String  | No       | `createdAt,desc` | Sort field and direction (e.g. `priority,asc`) |
| `status`   | String  | No       | —       | Filter by status: `TODO`, `IN_PROGRESS`, `DONE`     |
| `priority` | String  | No       | —       | Filter by priority: `LOW`, `MEDIUM`, `HIGH`         |

#### Response — `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "title": "Fix login bug",
      "description": "Users cannot log in with Google OAuth",
      "status": "TODO",
      "priority": "HIGH",
      "createdAt": "2026-05-03T10:00:00Z",
      "updatedAt": "2026-05-03T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

### 3. Get a Task by ID

| Property    | Value                         |
|-------------|-------------------------------|
| Method      | `GET`                         |
| Path        | `/api/v1/tasks/{id}`          |
| Description | Retrieves a single task by ID |

#### Path Parameters

| Parameter | Type | Required | Description      |
|-----------|------|----------|------------------|
| `id`      | Long | Yes      | Task identifier  |

#### Response — `200 OK`
```json
{
  "id": 1,
  "title": "Fix login bug",
  "description": "Users cannot log in with Google OAuth",
  "status": "TODO",
  "priority": "HIGH",
  "createdAt": "2026-05-03T10:00:00Z",
  "updatedAt": "2026-05-03T10:00:00Z"
}
```

---

### 4. Update a Task (Full Update)

| Property    | Value                               |
|-------------|-------------------------------------|
| Method      | `PUT`                               |
| Path        | `/api/v1/tasks/{id}`                |
| Description | Fully replaces all fields of a task |

#### Path Parameters

| Parameter | Type | Required | Description     |
|-----------|------|----------|-----------------|
| `id`      | Long | Yes      | Task identifier |

#### Request Body
```json
{
  "title": "Fix login bug",
  "description": "Updated: OAuth and password login affected",
  "status": "IN_PROGRESS",
  "priority": "HIGH"
}
```

#### Field Rules

| Field         | Required | Constraints                          |
|---------------|----------|--------------------------------------|
| `title`       | Yes      | 1–100 characters                     |
| `description` | No       | Max 500 characters                   |
| `status`      | Yes      | `TODO`, `IN_PROGRESS`, `DONE`        |
| `priority`    | Yes      | `LOW`, `MEDIUM`, `HIGH`              |

#### Response — `200 OK`
```json
{
  "id": 1,
  "title": "Fix login bug",
  "description": "Updated: OAuth and password login affected",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "createdAt": "2026-05-03T10:00:00Z",
  "updatedAt": "2026-05-03T11:30:00Z"
}
```

---

### 5. Partially Update a Task

| Property    | Value                                    |
|-------------|------------------------------------------|
| Method      | `PATCH`                                  |
| Path        | `/api/v1/tasks/{id}`                     |
| Description | Updates only the provided fields of a task |

#### Path Parameters

| Parameter | Type | Required | Description     |
|-----------|------|----------|-----------------|
| `id`      | Long | Yes      | Task identifier |

#### Request Body (only include fields to update)
```json
{
  "status": "DONE"
}
```

#### Response — `200 OK`
```json
{
  "id": 1,
  "title": "Fix login bug",
  "description": "Updated: OAuth and password login affected",
  "status": "DONE",
  "priority": "HIGH",
  "createdAt": "2026-05-03T10:00:00Z",
  "updatedAt": "2026-05-03T12:00:00Z"
}
```

---

### 6. Delete a Task

| Property    | Value                      |
|-------------|----------------------------|
| Method      | `DELETE`                   |
| Path        | `/api/v1/tasks/{id}`       |
| Description | Permanently deletes a task |

#### Path Parameters

| Parameter | Type | Required | Description     |
|-----------|------|----------|-----------------|
| `id`      | Long | Yes      | Task identifier |

#### Response — `204 No Content`

No response body.

---

## Validation Rules Summary

| Field         | Rule                                                   |
|---------------|--------------------------------------------------------|
| `title`       | Must not be blank; length between 1 and 100 characters |
| `description` | Optional; if provided, must not exceed 500 characters  |
| `status`      | Must be one of: `TODO`, `IN_PROGRESS`, `DONE`          |
| `priority`    | Must be one of: `LOW`, `MEDIUM`, `HIGH`                |
| `id`          | Read-only; auto-generated by the server                |
| `createdAt`   | Read-only; set automatically on creation               |
| `updatedAt`   | Read-only; updated automatically on every modification |

---

## Error Handling

All errors follow a standard error response format.

### Error Response Format
```json
{
  "timestamp": "2026-05-03T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "title",
      "message": "Title must not be blank"
    }
  ],
  "path": "/api/v1/tasks"
}
```

### HTTP Status Codes

| Status Code | Scenario                                                  |
|-------------|-----------------------------------------------------------|
| `200 OK`           | Successful GET, PUT, or PATCH                      |
| `201 Created`      | Task successfully created via POST                 |
| `204 No Content`   | Task successfully deleted                          |
| `400 Bad Request`  | Validation error or malformed request body         |
| `404 Not Found`    | Task with the specified `id` does not exist        |
| `405 Method Not Allowed` | HTTP method not supported on this endpoint   |
| `422 Unprocessable Entity` | Business rule violation (e.g. invalid enum) |
| `500 Internal Server Error` | Unexpected server-side error              |

### Common Error Examples

#### 400 — Validation Error
```json
{
  "timestamp": "2026-05-03T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    { "field": "title", "message": "Title must not be blank" },
    { "field": "priority", "message": "Priority must be LOW, MEDIUM, or HIGH" }
  ],
  "path": "/api/v1/tasks"
}
```

#### 404 — Task Not Found
```json
{
  "timestamp": "2026-05-03T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Task with id 99 not found",
  "details": [],
  "path": "/api/v1/tasks/99"
}
```

---

## Data Contract Summary

### TaskRequest (Create / Full Update)

```json
{
  "title": "string (required, 1–100 chars)",
  "description": "string (optional, max 500 chars)",
  "status": "TODO | IN_PROGRESS | DONE",
  "priority": "LOW | MEDIUM | HIGH"
}
```

### TaskPatchRequest (Partial Update)

```json
{
  "title": "string (optional, 1–100 chars)",
  "description": "string (optional, max 500 chars)",
  "status": "TODO | IN_PROGRESS | DONE (optional)",
  "priority": "LOW | MEDIUM | HIGH (optional)"
}
```

### TaskResponse

```json
{
  "id": "Long (read-only)",
  "title": "string",
  "description": "string | null",
  "status": "TODO | IN_PROGRESS | DONE",
  "priority": "LOW | MEDIUM | HIGH",
  "createdAt": "ISO 8601 DateTime (read-only)",
  "updatedAt": "ISO 8601 DateTime (read-only)"
}
```

---

## Endpoint Summary

| Method   | Path                  | Description              | Request Body      | Response         |
|----------|-----------------------|--------------------------|-------------------|------------------|
| `POST`   | `/api/v1/tasks`       | Create a task            | `TaskRequest`     | `201 TaskResponse` |
| `GET`    | `/api/v1/tasks`       | List all tasks (paged)   | —                 | `200 Page<TaskResponse>` |
| `GET`    | `/api/v1/tasks/{id}`  | Get a task by ID         | —                 | `200 TaskResponse` |
| `PUT`    | `/api/v1/tasks/{id}`  | Full update of a task    | `TaskRequest`     | `200 TaskResponse` |
| `PATCH`  | `/api/v1/tasks/{id}`  | Partial update of a task | `TaskPatchRequest`| `200 TaskResponse` |
| `DELETE` | `/api/v1/tasks/{id}`  | Delete a task            | —                 | `204 No Content` |

