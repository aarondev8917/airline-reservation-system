# Airline Reservation System

A comprehensive airline reservation management system built with Spring Boot for managing flights, bookings, passengers, airports, seats, and payments. Includes JWT authentication, Redis caching, and Aviationstack API integration for real-time external flight data.

---

## Table of Contents

- [Project Information](#project-information)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Postman Collection](#postman-collection)
- [API Reference](#api-reference)
- [Authentication (JWT)](#authentication-jwt)
- [Testing](#testing)
- [Docker](#docker)
- [Troubleshooting](#troubleshooting)
- [Project Structure](#project-structure)

---

## Project Information

| | |
|---|---|
| **Version** | 1.0.0 |
| **Framework** | Spring Boot 3.2.0 |
| **Java** | 21 |
| **Build Tool** | Maven |
| **Base URL** | `http://localhost:8080` |

---

## Technology Stack

- **Backend:** Spring Boot 3.2.0, Java 21
- **Database:** MySQL (H2 for tests)
- **ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security, JWT (JJWT)
- **Caching:** Redis
- **External API:** Aviationstack (real-time flight data)
- **Build:** Maven
- **Libraries:** Lombok, ModelMapper, Jakarta Validation

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java | 21 (recommended; 17+ supported) |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Redis | 6+ (optional; app works without it, caching disabled) |

---

## Setup

### Step 1: Clone or Download

```bash
cd airline-reservation-system
```

### Step 2: Install Dependencies

```bash
mvn clean install
```

### Step 3: MySQL Database

1. Install MySQL and ensure it is running.
2. The app auto-creates the database `airline_reservation_db` on first run.
3. Or create manually: `CREATE DATABASE airline_reservation_db;`

### Step 4: Redis (Optional)

Redis is used for caching. The app runs without Redis (cache is disabled).

**Docker:**
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

**Windows:** [Redis for Windows](https://github.com/microsoftarchive/redis/releases) or WSL  
**macOS:** `brew install redis && brew services start redis`  
**Linux:** `sudo apt install redis-server`

### Step 5: Aviationstack API Key (Optional)

For real-time external flight data:

1. Sign up at [aviationstack.com](https://aviationstack.com/signup) (free tier: 100 req/month).
2. Copy `application-local.properties.example` to `application-local.properties`.
3. Set `external.flights.api-key=your_key` and `external.flights.use-mock=false`.

Without an API key, the app uses built-in mock flight data.

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# MySQL (default)
spring.datasource.url=jdbc:mysql://localhost:3306/airline_reservation_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root

# Server
server.port=8080

# Redis (optional)
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### H2 (No MySQL)

To use in-memory H2 instead of MySQL:

```properties
spring.datasource.url=jdbc:h2:mem:airline_reservation_db
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

---

## Running the Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using JAR

```bash
mvn clean package
java -jar target/airline-reservation-system-1.0.0.jar
```

### Using IDE

1. Open project in IntelliJ IDEA (or Eclipse).
2. Enable **Annotation Processing** (Settings → Build → Compiler → Annotation Processors).
3. Run `AirlineReservationSystemApplication.java`.

Application starts at **http://localhost:8080**. With `dev` profile (default), sample airports, passengers, and flights are seeded.

---

## Postman Collection

A Postman collection is provided for easier API testing.

**File:** `Airline-Reservation-System.postman_collection.json`

### Import

1. Open Postman.
2. **Import** → **Upload Files** → select `Airline-Reservation-System.postman_collection.json`.
3. Set collection variables:
   - `baseUrl`: `http://localhost:8080`
   - `token`: leave empty initially; run **Login** and paste the JWT from the response into `data.token`, then set `token` in collection variables.

### Usage

- **Auth** folder: Register and Login. Use Login to get a JWT.
- **Airports, Flights, Bookings, Passengers, Payments, Seats** folders: All CRUD and search APIs.
- Protected endpoints use `Authorization: Bearer {{token}}` automatically.
- Login as `admin@airline.com` / `admin123` for admin actions (create/update flights, airports, import external flights).

---

## API Reference

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register user | Public |
| POST | `/api/auth/login` | Login, get JWT | Public |

### Airports

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/airports` | Get all airports | Public |
| GET | `/api/airports/{id}` | Get by ID | Public |
| GET | `/api/airports/code/{code}` | Get by code (e.g. JFK) | Public |
| GET | `/api/airports/city/{city}` | Get by city | Public |
| POST | `/api/airports` | Create airport | Admin |
| PUT | `/api/airports/{id}` | Update airport | Admin |
| DELETE | `/api/airports/{id}` | Delete airport | Admin |

### Flights

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/flights` | Get all flights | Public |
| GET | `/api/flights/{id}` | Get by ID | Public |
| GET | `/api/flights/number/{flightNumber}` | Get by flight number | Public |
| POST | `/api/flights/search` | Search (internal) | Public |
| POST | `/api/flights/search-unified?includeExternal=true` | Search (internal + external) | Public |
| POST | `/api/flights` | Create flight | Admin |
| PATCH | `/api/flights/{id}/status?status={status}` | Update status | Admin |
| DELETE | `/api/flights/{id}` | Delete flight | Admin |
| GET | `/api/flights/external` | Get external flights (Aviationstack/mock) | Public |
| GET | `/api/flights/external/number/{flightNumber}` | External by number | Public |
| GET | `/api/flights/external/{id}` | External by ID | Public |
| POST | `/api/flights/import-from-external` | Import external → internal | Admin |

### Bookings

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/bookings` | Get all bookings | User |
| GET | `/api/bookings/{id}` | Get by ID | User |
| GET | `/api/bookings/reference/{ref}` | Get by reference | User |
| GET | `/api/bookings/passenger/{passengerId}` | Get by passenger | User |
| POST | `/api/bookings` | Create booking | User |
| PATCH | `/api/bookings/{id}/confirm` | Confirm booking | User |
| DELETE | `/api/bookings/{id}` | Cancel booking | User |

### Passengers

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/passengers` | Get all passengers | User |
| GET | `/api/passengers/{id}` | Get by ID | User |
| GET | `/api/passengers/email/{email}` | Get by email | User |
| POST | `/api/passengers` | Create passenger | User |
| PUT | `/api/passengers/{id}` | Update passenger | User |
| DELETE | `/api/passengers/{id}` | Delete passenger | User |

### Payments

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/payments` | Get all payments | User |
| GET | `/api/payments/{id}` | Get by ID | User |
| GET | `/api/payments/transaction/{txnId}` | Get by transaction ID | User |
| GET | `/api/payments/booking/{bookingId}` | Get by booking | User |
| POST | `/api/payments` | Process payment | User |

### Seats

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/seats/{id}` | Get by ID | Public |
| GET | `/api/seats/flight/{flightId}` | Get all seats for flight | Public |
| GET | `/api/seats/flight/{flightId}/available` | Get available seats | Public |

---

## Authentication (JWT)

### Default Users (dev profile)

| Role | Email | Password |
|------|-------|----------|
| ADMIN | admin@airline.com | admin123 |
| USER | user@airline.com | user1234 |

### Usage

1. **Login:** `POST /api/auth/login` with `{"email":"admin@airline.com","password":"admin123"}`.
2. Copy `data.token` from the response.
3. For protected requests, add header: `Authorization: Bearer <token>`.

### Route Summary

- **Public:** Auth, GET flights/airports/seats, flight search.
- **Admin:** Create/update/delete flights and airports, import external flights.
- **User:** Bookings, passengers, payments (requires authentication).

---

## Testing

### Run Unit Tests

```bash
mvn test
```

Tests use the `test` profile: H2 database, no Redis, mock Aviationstack data. No config changes needed when switching between running the app and running tests.

### Quick API Test (PowerShell)

```powershell
# Login and save token
$r = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body '{"email":"admin@airline.com","password":"admin123"}'
$TOKEN = $r.data.token

# Use token
Invoke-RestMethod -Uri "http://localhost:8080/api/airports" -Headers @{ Authorization = "Bearer $TOKEN" }
```

### Sample Booking Flow

1. **Search flights:** `POST /api/flights/search` with `{"departureAirportCode":"DEL","arrivalAirportCode":"BOM","departureDate":"2025-12-01"}`.
2. **Get available seats:** `GET /api/seats/flight/1/available`.
3. **Create booking:** `POST /api/bookings` with `{"passengerId":1,"flightId":1,"seatId":15}`.
4. **Process payment:** `POST /api/payments` with `{"bookingId":1,"paymentMethod":"CREDIT_CARD"}`.
5. **Confirm booking:** `PATCH /api/bookings/1/confirm`.

---

## Docker

### Build

```bash
docker build -t airline-reservation-system .
```

### Run

Requires MySQL and Redis (or skip Redis; app will run with cache disabled).

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/airline_reservation_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  -e SPRING_DATA_REDIS_HOST=host.docker.internal \
  airline-reservation-system
```

On Linux, replace `host.docker.internal` with the host IP or use `--network host`.

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **MySQL connection failed** | Ensure MySQL is running; verify credentials in `application.properties`. |
| **Port 8080 in use** | Change `server.port` in `application.properties`. |
| **Lombok/IDE errors** | Enable Annotation Processing (IntelliJ: Settings → Compiler → Annotation Processors). |
| **Java version mismatch** | Use Java 21; set Project SDK and Maven JDK to 21 in IDE. |
| **Redis connection failed** | App runs without Redis; caching is disabled. Start Redis for caching. |
| **Aviationstack empty/errors** | Check API key and quota; app falls back to mock data on failure. |

---

## Project Structure

```
airline-reservation-system/
├── src/main/java/com/airline/reservation/
│   ├── configs/          # Cors, JPA, Cache, Security, Auth
│   ├── controllers/      # REST controllers
│   ├── dtos/             # Request/response DTOs
│   ├── exceptions/       # GlobalExceptionHandler, custom exceptions
│   ├── models/           # JPA entities
│   ├── repositories/     # Spring Data repositories
│   ├── security/         # JWT, SecurityConfig, filters
│   ├── services/         # Business logic
│   └── AirlineReservationSystemApplication.java
├── src/main/resources/
│   └── application.properties
├── src/test/             # Unit and controller tests
├── pom.xml
├── Dockerfile
├── .dockerignore
├── application-local.properties.example
└── Airline-Reservation-System.postman_collection.json
```

---

## Additional Details

- **Sample data:** With `spring.profiles.active=dev` (default), 10 airports, 5 passengers, 8 flights, and seats are seeded.
- **Redis caching:** Airports (1 hr), Flights (15 min), Seats (2 min), External flights (10 min). Caches evict on create/update/delete.
- **Error responses:** Standard `{"success": false, "message": "...", "data": null}` format.
- **Payment methods:** CREDIT_CARD, DEBIT_CARD, NET_BANKING, UPI, WALLET.

---

**Note:** This is an educational/demonstration project. For production use, add stronger security, HTTPS, rate limiting, and further testing.
