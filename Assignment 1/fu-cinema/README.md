# FUCinema Booking System — MSS301 Assignment 1

This project contains three Spring Boot services and an API Gateway:

| Application | Port | Database |
|---|---:|---|
| Customer Service | 8081 | SQL Server (`cinema_customer`) |
| Movie Service | 8082 | MongoDB (`cinema_movie`) |
| Booking Service | 8083 | MySQL (`cinema_booking`) |
| API Gateway | 9000 | Routes requests to the services |

## Requirements

- JDK 21
- Maven 3.9 or later
- Docker Desktop
- Postman Desktop (for API integration tests)

## Run the application

Run these commands from the `mss301-lab` repository root.

Start the databases and check their status:

```powershell
docker compose -f "Assignment 1/fu-cinema/docker-compose.yml" up -d
docker compose -f "Assignment 1/fu-cinema/docker-compose.yml" ps -a
```

Start each application in a separate terminal:

```powershell
mvn -f "Assignment 1/fu-cinema/customer-service/pom.xml" spring-boot:run
```

```powershell
mvn -f "Assignment 1/fu-cinema/movie-service/pom.xml" spring-boot:run
```

```powershell
mvn -f "Assignment 1/fu-cinema/booking-service/pom.xml" spring-boot:run
```

```powershell
mvn -f "Assignment 1/fu-cinema/api-gateway/pom.xml" spring-boot:run
```

The Postman requests go through the Gateway at `http://localhost:9000`. Check that it is ready at <http://localhost:9000/actuator/health>.

## Test accounts

| Role | Email | Password | Status |
|---|---|---|---|
| Admin | `admin@fucinema.com` | `@@abc123@@` | Admin account |
| Customer | `an@gmail.com` | `123456` | Active |
| Customer | `binh@gmail.com` | `123456` | Active |
| Customer | `chi@gmail.com` | `123456` | Inactive; login returns 403 |

## Postman integration test

Import `postman/FUCinemaBookingSystem.postman_collection.json` and `postman/FUCinema-Local.postman_environment.json`. Select the `FUCinema-Local` environment, then run the full collection in order.

The collection creates test customers, movies, showtimes, and bookings in the local databases. It also updates a customer profile and cancels test bookings.

## Assignment files

- [Detailed implementation and Postman guide](docs/Assignment1_Guide.md)
- [Assignment report](docs/TuPhucNguyen-assignment1.docx)
