# BookXShow — API Contracts

This document describes the REST API contracts for the BookXShow Seat Booking
Service (v1). All endpoints are served under the base path `/api/v1`.

---

## Table of Contents

- [Common Conventions](#common-conventions)
- [1. Book a Seat](#1-book-a-seat)
- [2. Get Booking by Reference](#2-get-booking-by-reference)
- [3. List Seats for a Show](#3-list-seats-for-a-show)
- [4. Get Seat Status](#4-get-seat-status)
- [5. Sync Show from Admin Service](#5-sync-show-from-admin-service)
- [Error Response Format](#error-response-format)
- [Enumerations](#enumerations)

---

## Common Conventions

| Item             | Value                                 |
|------------------|---------------------------------------|
| Base URL         | `http://{host}:{port}/api/v1`         |
| Content-Type     | `application/json`                    |
| Date format      | ISO 8601 (`2026-03-15T19:30:00`)      |
| Instant format   | ISO 8601 UTC (`2026-03-15T19:30:00Z`) |
| Error contract   | Uniform `ErrorResponseDto` (see below)|

---

## 1. Book a Seat

Attempts to book a seat for a user. Executes the lock → pay → confirm flow.

### Request

```
POST /api/v1/bookings
Content-Type: application/json
```

#### Body — `BookingRequestDto`

| Field    | Type      | Required | Validation                     | Description                        |
|----------|-----------|----------|--------------------------------|------------------------------------|
| `showId` | `Long`    | Yes      | `@NotNull`                     | Primary key of the show            |
| `seatId` | `Long`    | Yes      | `@NotNull`                     | Primary key of the seat            |
| `userId` | `String`  | Yes      | `@NotBlank`                    | Unique user identifier             |
| `amount` | `BigDecimal` | Yes   | `@NotNull`, `>= 0.01`         | Payment amount                     |

**Example:**

```json
{
  "showId": 1,
  "seatId": 10,
  "userId": "alice",
  "amount": 99.99
}
```

### Response — `BookingResponseDto`

| Status | Condition                      |
|--------|--------------------------------|
| `201`  | Seat successfully booked       |
| `200`  | Booking attempted but not confirmed (see `outcome`) |
| `400`  | Validation error               |
| `404`  | Seat or show not found         |

#### Body

| Field              | Type            | Nullable | Description                                      |
|--------------------|-----------------|----------|--------------------------------------------------|
| `bookingReference` | `String`        | Yes      | Unique ref (e.g. `BXS-a3f8…`); null if not booked |
| `seatId`           | `Long`          | No       | The seat primary key                             |
| `seatNumber`       | `String`        | No       | Human-readable seat label (e.g. `A01`)           |
| `showId`           | `Long`          | No       | The show primary key                             |
| `userId`           | `String`        | No       | The requesting user ID                           |
| `outcome`          | `BookingOutcome`| No       | Result of the attempt (see [Enumerations](#enumerations)) |
| `seatStatus`       | `SeatStatus`    | No       | Current seat status after the attempt            |

**Example (success):**

```json
{
  "bookingReference": "BXS-a3f8e120-4f5b-4a1c-8d6e-1234abcd5678",
  "seatId": 10,
  "seatNumber": "A01",
  "showId": 1,
  "userId": "alice",
  "outcome": "BOOKED",
  "seatStatus": "BOOKED"
}
```

**Example (seat unavailable):**

```json
{
  "bookingReference": null,
  "seatId": 10,
  "seatNumber": "A01",
  "showId": 1,
  "userId": "bob",
  "outcome": "SEAT_UNAVAILABLE",
  "seatStatus": "LOCKED"
}
```

---

## 2. Get Booking by Reference

Retrieves an existing booking by its unique reference code.

### Request

```
GET /api/v1/bookings/{bookingReference}
```

| Path Parameter     | Type     | Description                         |
|--------------------|----------|-------------------------------------|
| `bookingReference` | `String` | Unique booking ref (e.g. `BXS-…`)  |

### Response — `BookingResponseDto`

| Status | Condition          |
|--------|--------------------|
| `200`  | Booking found      |
| `404`  | Booking not found  |

**Example:**

```json
{
  "bookingReference": "BXS-a3f8e120-4f5b-4a1c-8d6e-1234abcd5678",
  "seatId": 10,
  "seatNumber": "A01",
  "showId": 1,
  "userId": "alice",
  "outcome": "BOOKED",
  "seatStatus": "BOOKED"
}
```

---

## 3. List Seats for a Show

Returns all seats for a given show with their current status.

### Request

```
GET /api/v1/shows/{showId}/seats
```

| Path Parameter | Type   | Description             |
|----------------|--------|-------------------------|
| `showId`       | `Long` | Primary key of the show |

### Response — `SeatStatusDto[]`

| Status | Condition         |
|--------|-------------------|
| `200`  | Seats returned    |
| `404`  | Show not found    |

#### `SeatStatusDto` Fields

| Field        | Type         | Nullable | Description                            |
|--------------|--------------|----------|----------------------------------------|
| `seatId`     | `Long`       | No       | Seat primary key                       |
| `seatNumber` | `String`     | No       | Human-readable label (e.g. `A01`)      |
| `showId`     | `Long`       | No       | Show primary key                       |
| `status`     | `SeatStatus` | No       | Current status                         |
| `lockedBy`   | `String`     | Yes      | User ID of lock holder (null if not locked) |
| `lockExpiry` | `Instant`    | Yes      | Lock expiry time (null if not locked)  |

**Example:**

```json
[
  {
    "seatId": 10,
    "seatNumber": "A01",
    "showId": 1,
    "status": "AVAILABLE",
    "lockedBy": null,
    "lockExpiry": null
  },
  {
    "seatId": 11,
    "seatNumber": "A02",
    "showId": 1,
    "status": "BOOKED",
    "lockedBy": null,
    "lockExpiry": null
  }
]
```

---

## 4. Get Seat Status

Retrieves the current status of a single seat.

### Request

```
GET /api/v1/shows/{showId}/seats/{seatId}
```

| Path Parameter | Type   | Description             |
|----------------|--------|-------------------------|
| `showId`       | `Long` | Primary key of the show |
| `seatId`       | `Long` | Primary key of the seat |

### Response — `SeatStatusDto`

| Status | Condition        |
|--------|------------------|
| `200`  | Seat found       |
| `404`  | Seat not found   |

**Example:**

```json
{
  "seatId": 10,
  "seatNumber": "A01",
  "showId": 1,
  "status": "LOCKED",
  "lockedBy": "alice",
  "lockExpiry": "2026-03-15T19:30:30Z"
}
```

---

## 5. Sync Show from Admin Service

Triggers a synchronisation of show and seat data from the external Admin
Service into the local database. Intended for admin/back-office use.

### Request

```
POST /api/v1/admin/shows/{externalShowId}/sync
```

| Path Parameter   | Type     | Description                              |
|------------------|----------|------------------------------------------|
| `externalShowId` | `String` | Admin-Service-assigned show identifier   |

### Response

| Status | Condition             |
|--------|-----------------------|
| `200`  | Sync completed        |
| `404`  | Show not found in Admin Service |

**Example (success):**

```
"Show SHOW-001 synced successfully"
```

---

## Error Response Format

All errors return a uniform JSON body:

### `ErrorResponseDto`

| Field       | Type      | Description                             |
|-------------|-----------|-----------------------------------------|
| `timestamp` | `Instant` | When the error occurred                 |
| `status`    | `int`     | HTTP status code                        |
| `error`     | `String`  | Short label (e.g. `Not Found`)          |
| `message`   | `String`  | Detailed human-readable message         |
| `path`      | `String`  | Request path that triggered the error   |

**Example:**

```json
{
  "timestamp": "2026-03-15T19:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Seat not found with id: 999",
  "path": "/api/v1/bookings"
}
```

### Handled Exceptions

| Exception                      | HTTP Status | Description                           |
|--------------------------------|-------------|---------------------------------------|
| `SeatNotFoundException`        | `404`       | Seat ID does not exist                |
| `ShowNotFoundException`        | `404`       | Show ID does not exist                |
| `BookingNotFoundException`     | `404`       | Booking reference not found           |
| `SeatUnavailableException`    | `409`       | Seat is already locked or booked      |
| `PaymentProcessingException`  | `502`       | Payment gateway processing error      |
| `MethodArgumentNotValidException` | `400`   | Bean validation failure               |
| `Exception` (fallback)        | `500`       | Unexpected server error               |

---

## Enumerations

### `BookingOutcome`

Describes the result of a booking attempt.

| Value              | Description                                       |
|--------------------|---------------------------------------------------|
| `BOOKED`           | Seat confirmed — payment succeeded                |
| `SEAT_UNAVAILABLE` | Seat already locked or booked by another user      |
| `PAYMENT_FAILED`   | Payment gateway declined; seat released            |
| `LOCK_EXPIRED`     | Payment took longer than lock TTL; seat reclaimed  |

### `SeatStatus`

Lifecycle states of a bookable seat.

| Value       | Description                                          |
|-------------|------------------------------------------------------|
| `AVAILABLE` | Open for booking                                     |
| `LOCKED`    | Temporarily held while payment is processing         |
| `BOOKED`    | Confirmed and reserved for a user                    |

### `BookingStatus`

Status of a persisted booking record.

| Value       | Description                                          |
|-------------|------------------------------------------------------|
| `CONFIRMED` | Booking is active                                    |
| `CANCELLED` | Booking was cancelled                                |

### `PaymentResult`

Outcome from the external payment gateway.

| Value     | Description                        |
|-----------|------------------------------------|
| `SUCCESS` | Payment authorized                 |
| `FAILURE` | Payment declined or errored        |
| `TIMEOUT` | Gateway did not respond in time    |
