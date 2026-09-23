# Smart Desk Booking System

A backend-focused Smart Desk Booking System built with **Java 21, Spring Boot, PostgreSQL, JPA/Hibernate, Flyway, Redis, and Spring Security with JWT**.

The system is designed around practical workplace desk-booking requirements such as desk availability, nearby desk recommendation, team booking quotas, transactional booking, concurrency control, timezone-aware booking windows, no-show handling, authentication, caching, and application monitoring.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Requirements / Prerequisites](#requirements--prerequisites)
- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Database Setup](#database-setup)
- [Redis Setup](#redis-setup)
- [Configuration](#configuration)
- [Installation and Setup](#installation-and-setup)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
- [Booking Flow](#booking-flow)
- [Concurrency and Transactions](#concurrency-and-transactions)
- [Caching](#caching)
- [Timezone and Booking Window](#timezone-and-booking-window)
- [No-Show and Check-In](#no-show-and-check-in)
- [Monitoring](#monitoring)
- [HTTP Status Codes](#http-status-codes)
- [Git / Development Workflow](#git--development-workflow)
- [Future Improvements](#future-improvements)

---

## Project Overview

The Smart Desk Booking System allows employees to:

- Register and authenticate.
- Search for available desks.
- Get a nearby/recommended desk.
- Book a desk for a requested time interval.
- Check in to a confirmed booking.
- Automatically release bookings as no-shows when the employee does not check in within the configured grace period.

The backend also handles:

- Team-level booking quotas.
- Concurrent booking attempts.
- Database transactions.
- Pessimistic locking for critical booking operations.
- Timezone-aware booking validation.
- Redis caching for relatively static metadata.
- Bean validation.
- Centralized exception handling.
- JWT-based authentication.
- Application metrics and health monitoring.

The database remains the **source of truth for dynamic desk availability and bookings**.

---

# Features

## Authentication and Security

- User registration.
- User login.
- BCrypt password hashing.
- JWT access-token authentication.
- Stateless Spring Security configuration.
- Protected application APIs.
- User identity derived from the authenticated JWT rather than accepting `userId` from the booking request.
- JWT secret supplied through an environment variable.

## Desk Search

- Search desks by floor.
- Search based on a requested time interval.
- Only active desks are considered available.
- Existing confirmed bookings are checked for interval overlap.

## Desk Recommendation

- Recommend a desk near a preferred desk.
- Return the selected desk and its calculated distance.

## Booking

- Create a desk booking.
- Validate the requested interval.
- Prevent overlapping confirmed bookings.
- Enforce team/floor booking quotas.
- Use database locking and transactions for concurrent booking requests.

## Check-In and No-Show

- Employees can check in to their own confirmed booking.
- Check-in timestamp is recorded.
- A scheduled process identifies bookings that passed the no-show grace period.
- Eligible bookings are marked as `NO_SHOW`.

## Validation

- Bean Validation on API request DTOs.
- Business validation in the service layer.
- Centralized exception handling.

## Caching

Redis is used for metadata that does not change as frequently as booking availability, such as:

- Desk metadata.
- Floor metadata.
- Team metadata.

Dynamic availability is intentionally **not cached** because stale availability could cause incorrect booking decisions.

## Monitoring

The project uses:

- Spring Boot Actuator.
- Micrometer business metrics.
- Application logging.

Prometheus and Grafana are not part of the current implementation.

---

# Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application language/runtime |
| Spring Boot 4.1.1 | Backend framework |
| Spring Web | REST APIs |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| PostgreSQL | Primary relational database |
| Flyway | Database migrations |
| Redis | Application caching |
| Spring Cache | Cache abstraction |
| Spring Security | API security |
| JWT / JJWT | Authentication tokens |
| BCrypt | Password hashing |
| Bean Validation | Request validation |
| Micrometer | Application/business metrics |
| Spring Boot Actuator | Health and monitoring endpoints |
| Maven | Build and dependency management |

---

# Requirements / Prerequisites

Before running the project, install:

### 1. Java 21

Verify:

```bash
java -version
```

The project intentionally uses **Java 21**.

### 2. PostgreSQL

PostgreSQL must be installed and running.

Default local configuration used by the example:

```text
Host: localhost
Port: 5432
Database: smart_desk_booking
Username: postgres
Password: <your-password>
```

### 3. Redis

Redis must be installed and running locally.

Default configuration:

```text
Host: localhost
Port: 6379
```

### 4. Git

Verify:

```bash
git --version
```

### 5. Maven

The project includes the Maven Wrapper, so Maven does not need to be installed separately.

On Windows:

```powershell
.\mvnw.cmd --version
```

### 6. IDE (Optional)

Recommended:

- IntelliJ IDEA
- Spring Tool Suite
- Eclipse

---

# Architecture Overview

The application follows a layered backend architecture:

```text
Client
  |
  v
REST Controllers
  |
  v
Service Layer
  |
  v
Repository Layer
  |
  v
PostgreSQL
```

Supporting components:

```text
                    +----------------+
                    |     Redis      |
                    |    Caching     |
                    +----------------+
                           ^
                           |
Client -> Controller -> Service -> Repository -> PostgreSQL
             ^
             |
      Spring Security
             |
        JWT Filter
```

## Main Layers

### Controller Layer

Responsible for:

- HTTP request handling.
- Request DTOs.
- Response DTOs.
- Bean Validation.
- Extracting authenticated user identity.

### Service Layer

Contains business logic such as:

- Booking validation.
- Desk availability checks.
- Team quota checks.
- Desk placement.
- Authentication.
- Check-in.
- No-show processing.
- Cache operations.

### Repository Layer

Responsible for:

- Database queries.
- Availability queries.
- Booking overlap checks.
- Pessimistic locking queries.
- Team quota queries.

### PostgreSQL

The relational database is the source of truth for:

- Users.
- Teams.
- Floors.
- Desks.
- Bookings.
- Booking state.

### Redis

Redis supports caching of relatively stable metadata.

Booking availability is not cached.

---

# Project Structure

The project follows a structure similar to:

```text
src/
└── main/
    ├── java/
    │   └── smartdesk/
    │       └── booking/
    │           ├── config/
    │           ├── controller/
    │           ├── dto/
    │           ├── entity/
    │           ├── exception/
    │           ├── repository/
    │           ├── security/
    │           ├── service/
    │           └── monitoring/
    │
    └── resources/
        ├── application.properties
        └── db/
            └── migration/
```

Database schema changes are managed through Flyway migrations.

---

# Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE smart_desk_booking;
```

Then configure the application with your PostgreSQL credentials.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/smart_desk_booking
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```

Flyway migrations run automatically when the application starts.

You do not need to manually create the application's tables if the Flyway configuration is working correctly.

---

# Redis Setup

Start Redis locally.

The default configuration is:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Verify that Redis is available on port `6379`.

Redis is required for the application's configured caching functionality.

---

# Configuration

The application uses environment variables for sensitive configuration such as the JWT secret.

Example `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/smart_desk_booking
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

spring.data.redis.host=localhost
spring.data.redis.port=6379

jwt.secret=${JWT_SECRET}
jwt.access-token-expiration=900000

management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
management.metrics.tags.application=smart-desk-booking
```

## JWT Secret

The JWT secret must be sufficiently long for the configured HMAC algorithm.

### Windows PowerShell

```powershell
$env:JWT_SECRET="your-32-character-or-longer-secret"
```

Example:

```powershell
$env:JWT_SECRET="8fK2mP9xQ7vL4nR6sT1yU3wE5aB8cD0z"
```

Do not commit a real secret to Git.

For a persistent environment variable on Windows, configure it through the operating system environment-variable settings instead of committing it to the repository.

---

# Installation and Setup

## 1. Clone the repository

```bash
git clone <repository-url>
```

Move into the project:

```bash
cd smart-desk-booking
```

Replace `<repository-url>` with the actual GitHub repository URL.

## 2. Start PostgreSQL

Make sure PostgreSQL is running.

Create:

```text
smart_desk_booking
```

if it does not already exist.

## 3. Start Redis

Make sure Redis is running on:

```text
localhost:6379
```

## 4. Configure database credentials

Update:

```text
src/main/resources/application.properties
```

with your local PostgreSQL credentials.

## 5. Configure JWT secret

PowerShell:

```powershell
$env:JWT_SECRET="your-32-character-or-longer-secret"
```

## 6. Build the project

Windows:

```powershell
.\mvnw.cmd clean install
```

## 7. Run tests

```powershell
.\mvnw.cmd test
```

## 8. Start the application

```powershell
.\mvnw.cmd spring-boot:run
```

The application should start on:

```text
http://localhost:8080
```

You can also run the main Spring Boot application class directly from IntelliJ IDEA.

---

# Running the Application

Once the application is running:

```text
Application:
http://localhost:8080
```

Actuator health endpoint:

```text
http://localhost:8080/actuator/health
```

If the health endpoint is available, the application is running and Actuator is enabled.

---

# API Documentation

## Base URL

```text
http://localhost:8080
```

## Authentication Endpoints

| Method | Endpoint | Authentication |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |

## Desk Endpoints

| Method | Endpoint | Authentication |
|---|---|---|
| GET | `/api/desks/available` | JWT required |
| GET | `/api/desks/recommend` | JWT required |

## Booking Endpoints

| Method | Endpoint | Authentication |
|---|---|---|
| POST | `/api/bookings` | JWT required |
| POST | `/api/bookings/{bookingId}/check-in` | JWT required |

---

# Authentication

## Register

```http
POST /api/auth/register
Content-Type: application/json
```

Example request:

```json
{
  "employeeCode": "EMP001",
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "teamId": 1
}
```

Successful registration returns an authentication response containing the user information and access token.

## Login

```http
POST /api/auth/login
Content-Type: application/json
```

Example:

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

The response contains an access token.

For protected endpoints, send:

```http
Authorization: Bearer <access-token>
```

The authenticated user's ID is extracted from the JWT.

The booking API therefore does not accept a `userId` in the request body.

---

# Desk Search

## Find Available Desks

```http
GET /api/desks/available
```

Query parameters:

```text
floorId
startTime
endTime
```

Example:

```text
/api/desks/available?floorId=1&startTime=2026-09-25T10:00:00Z&endTime=2026-09-25T12:00:00Z
```

Response:

```json
[
  {
    "id": 1,
    "floorId": 1,
    "status": "ACTIVE"
  }
]
```

Availability is determined using the database rather than a Redis cache.

---

# Desk Recommendation

```http
GET /api/desks/recommend
```

Query parameters:

```text
floorId
startTime
endTime
preferredDeskId
```

Example:

```text
/api/desks/recommend?floorId=1&startTime=2026-09-25T10:00:00Z&endTime=2026-09-25T12:00:00Z&preferredDeskId=10
```

Response:

```json
{
  "deskId": 11,
  "deskNumber": "D011",
  "distance": 1
}
```

The distance represents the calculated desk distance used for the recommendation.

---

# Create Booking

```http
POST /api/bookings
Authorization: Bearer <access-token>
Content-Type: application/json
```

Request:

```json
{
  "deskId": 10,
  "startTime": "2026-09-25T10:00:00Z",
  "endTime": "2026-09-25T12:00:00Z"
}
```

The request contains:

- `deskId`
- `startTime`
- `endTime`

It does **not** contain `userId`.

The user ID is extracted from the authenticated JWT.

Response:

```json
{
  "bookingId": 100,
  "userId": 1,
  "deskId": 10,
  "startTime": "2026-09-25T10:00:00Z",
  "endTime": "2026-09-25T12:00:00Z",
  "status": "CONFIRMED"
}
```

---

# Check In

```http
POST /api/bookings/{bookingId}/check-in
Authorization: Bearer <access-token>
```

No request body is required.

Example:

```text
POST /api/bookings/100/check-in
```

A user can only check in to their own booking.

Successful check-in changes the booking status to:

```text
CHECKED_IN
```

and records the check-in timestamp.

---

# Booking Flow

The booking process follows this general sequence:

```text
Client
  |
  v
POST /api/bookings
  |
  v
JWT authentication
  |
  v
Validate request
  |
  v
Load user
  |
  v
Lock desk
  |
  v
Validate booking window
  |
  v
Check desk status
  |
  v
Check overlapping booking
  |
  v
Check team/floor quota
  |
  v
Create booking
  |
  v
Commit transaction
```

The final booking operation revalidates the important constraints inside the transaction.

This is important because the availability returned by a previous search is only a snapshot and another request may change the database before the booking is created.

---

# Concurrency and Transactions

Concurrency is one of the important parts of this project.

Two users may attempt to book the same desk at approximately the same time.

The project uses:

- `@Transactional`
- PostgreSQL database transactions
- Pessimistic locking
- Database-backed availability checks
- Team quota locking

## Desk Locking

The booking flow uses a pessimistic write lock when loading the desk:

```java
findByIdForUpdate(...)
```

This serializes competing booking transactions for the same desk.

## Booking Overlap

The system checks whether an existing confirmed booking overlaps the requested interval.

The overlap condition is:

```text
existing.start < requested.end
AND
existing.end > requested.start
```

This allows adjacent bookings.

For example:

```text
Booking A: 10:00 - 11:00
Booking B: 11:00 - 12:00
```

These do not overlap.

## Team Quota

The project also protects the team/floor quota against concurrent requests.

The concurrency test verifies that when multiple requests compete for a quota of one, only one request succeeds.

## Concurrency Tests

The project includes tests covering:

### Same Desk Concurrency

Multiple concurrent requests attempt to book the same desk for the same interval.

Expected behavior:

```text
1 successful booking
remaining requests rejected
```

### Team Quota Concurrency

Multiple requests attempt to consume a team/floor quota concurrently.

Expected behavior:

```text
quota is not exceeded
```

---

# Caching

Redis is used for relatively stable metadata.

Examples include:

- Desk metadata.
- Floor metadata.
- Team metadata.

The application uses Spring Cache with Redis as the cache implementation.

## Why Availability Is Not Cached

Desk availability is highly dynamic.

A cache could contain:

```text
Desk D104 = available
```

while another user books D104 immediately afterward.

Returning stale availability from Redis could therefore lead to incorrect decisions.

For this reason:

```text
PostgreSQL = source of truth for dynamic booking state
Redis       = supporting cache for metadata
```

Explicit cache eviction is used when cached metadata changes.

---

# Timezone and Booking Window

Booking timestamps are represented using:

```java
Instant
```

The floor's timezone is used when local booking-time validation is required.

The booking window currently enforces:

### Minimum Advance

A booking must be made at least:

```text
30 minutes
```

before its start time.

### Maximum Horizon

A booking cannot be made more than:

```text
14 days
```

in advance.

### Timezone Handling

The system converts the requested `Instant` to the floor's configured `ZoneId` for timezone-aware validation.

The database continues to store the booking timestamps as absolute instants.

---

# No-Show and Check-In

A confirmed booking can be checked in by its owner.

If the employee does not check in within the configured grace period, the booking can be marked as a no-show.

Current grace period:

```text
15 minutes
```

The no-show scheduler runs periodically:

```text
Every 60 seconds
```

The scheduler identifies confirmed bookings where:

- Start time has passed the grace-period cutoff.
- `checkedInAt` is still null.

Those bookings are changed to:

```text
NO_SHOW
```

Booking statuses currently include:

```text
CONFIRMED
CHECKED_IN
NO_SHOW
CANCELLED
```

---

# Monitoring

The project uses Spring Boot Actuator, Micrometer metrics, and application logging.

Prometheus and Grafana are intentionally not included in the current implementation.

## Actuator

Exposed endpoints include:

```text
/actuator/health
/actuator/info
/actuator/metrics
```

Example:

```text
http://localhost:8080/actuator/health
```

## Business Metrics

The application tracks booking-related metrics such as:

```text
booking.created
booking.conflict
booking.quota.exceeded
booking.checkin
booking.no_show
```

These metrics help observe important business events without introducing a full Prometheus/Grafana monitoring stack.

## Logging

Important application events can be logged using SLF4J, including:

- Successful booking.
- Booking conflicts.
- Quota rejection.
- Check-in.
- No-show processing.
- Authentication events.

Sensitive information such as passwords, JWT secrets, and tokens should never be logged.

---

# Validation and Exception Handling

The API uses Jakarta Bean Validation.

Examples:

- Required fields use `@NotNull` / `@NotBlank`.
- Email uses `@Email`.
- Password uses `@Size`.
- Controllers use `@Valid`.

Business rules remain in the service layer.

Examples:

```text
Inactive user
Invalid booking interval
Desk unavailable
Booking conflict
Quota exceeded
Invalid booking window
Resource not found
```

A global exception handler converts these exceptions into appropriate HTTP responses.

Validation errors return HTTP `400 Bad Request` with field-level messages.

---

# HTTP Status Codes

Common responses include:

| Status | Meaning |
|---|---|
| `200 OK` | Successful request |
| `201 Created` | Resource successfully created |
| `400 Bad Request` | Invalid request or business validation failure |
| `401 Unauthorized` | Missing/invalid authentication |
| `404 Not Found` | Requested resource does not exist |
| `409 Conflict` | Booking/resource conflict |

---

# Running Tests

Run the complete test suite:

### Windows

```powershell
.\mvnw.cmd test
```

### Other platforms

```bash
./mvnw test
```

Build and test:

```powershell
.\mvnw.cmd clean test
```

The project includes tests for areas such as:

- Authentication.
- Password hashing.
- JWT validation.
- Booking.
- Desk availability.
- Team quota.
- Concurrency.
- Booking window validation.
- No-show processing.
- API validation.

All tests should pass before committing major changes.

---

# Git / Development Workflow

A typical development workflow is:

```bash
git status
```

Review changed files.

Stage only the intended changes:

```bash
git add <files>
```

Commit:

```bash
git commit -m "feat: describe the change"
```

Push:

```bash
git push origin main
```

Example feature commit:

```bash
git commit -m "feat: implement API security and JWT authentication"
```

For a focused change, avoid blindly staging unrelated files with `git add .` when you want precise commits.

---

# Future Improvements

Potential future improvements include:

- More extensive API integration testing.
- Role-based authorization for administrative operations.
- Advanced fairness/anti-hoarding policies.
- More detailed observability.
- Prometheus/Grafana integration.
- More sophisticated desk-placement algorithms.
- Distributed deployment.
- Horizontal scaling.
- More advanced notification mechanisms.

These are outside the current core implementation.

---

# Current Project Scope

The current implementation focuses on:

```text
Authentication
        +
Desk Search
        +
Desk Recommendation
        +
Booking
        +
Team Quotas
        +
Transactions
        +
Pessimistic Locking
        +
Timezone-Aware Booking Windows
        +
Check-In
        +
No-Show Processing
        +
Redis Metadata Caching
        +
Validation
        +
Exception Handling
        +
Actuator / Metrics / Logging
```

The design intentionally keeps **PostgreSQL as the source of truth for dynamic booking state** and uses Redis as a supporting cache for metadata.

---

# Quick Start

For an experienced developer, the shortest setup path is:

```powershell
# 1. Clone
git clone <repository-url>
cd smart-desk-booking

# 2. Configure PostgreSQL and Redis

# 3. Set JWT secret
$env:JWT_SECRET="your-32-character-or-longer-secret"

# 4. Run tests
.\mvnw.cmd clean test

# 5. Start application
.\mvnw.cmd spring-boot:run
```

Then open:

```text
http://localhost:8080
```

and verify:

```text
http://localhost:8080/actuator/health
```

---

## License

Add the project's chosen license here if one is selected for the repository.
