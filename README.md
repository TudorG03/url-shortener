# URL Shortener

A production-grade URL shortening service built with Spring Boot, PostgreSQL, and Redis. Designed as a learning project covering REST API design, caching, rate limiting, API key authentication, AWS IaaS deployment, and CI/CD automation.

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 25, Spring Boot 4 |
| Database | PostgreSQL 16 via Spring Data JPA + Hibernate |
| Cache / Rate Limiting | Redis 7 via Spring Data Redis + Lettuce |
| Rate Limiting Library | Bucket4j |
| Schema Migrations | Flyway |
| Security | Spring Security — API key authentication |
| API Documentation | SpringDoc OpenAPI 3 + Swagger UI |
| Infrastructure | Docker Compose (local), AWS EC2 + RDS + Redis on EC2 (production) |
| IaC | Terraform |
| CI/CD | GitHub Actions |

## Architecture

```
CLIENT
  └── REST API (Spring Boot on EC2)
        ├── Spring Web MVC
        ├── Spring Data JPA      → PostgreSQL (RDS)
        ├── Spring Cache         → Redis (EC2)
        └── Spring Security      → API key authentication

AWS INFRASTRUCTURE
  ├── EC2           — Spring Boot application
  ├── RDS           — managed PostgreSQL
  ├── Redis on EC2  — caching + rate limit counters
  └── VPC           — private network isolating RDS and Redis
```

## API

### Public

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/{shortCode}` | Redirect to original URL |

### Authenticated — requires `X-API-Key` header

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/urls` | Create a short URL |
| `GET` | `/api/v1/urls/{shortCode}` | Get URL stats |
| `DELETE` | `/api/v1/urls/{shortCode}` | Delete a short URL |

Full API documentation available at `/swagger-ui` when the application is running.

## Caching Strategy

| Cache | Key | TTL | Purpose |
|---|---|---|---|
| URL lookup | `url:{shortCode}` | 1 hour | Serve redirects without DB queries |
| Stats | `stats:{shortCode}` | 30 seconds | Reduce stats endpoint DB load |
| Rate limit counters | `ratelimit:{ip}:{window}` | 60 seconds | Distributed request counting |

## Rate Limiting

| Endpoint | Limit |
|---|---|
| Public redirect | 100 requests / minute / IP |
| Authenticated API | 30 requests / minute / API key |

Exceeded limits return `429 Too Many Requests` with a `Retry-After` header.

## Getting Started

### Prerequisites

- Java 25+
- Maven 3.9+
- Docker + Docker Compose

### Run Locally

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/url-shortener.git
cd url-shortener

# Start PostgreSQL and Redis
docker-compose up -d

# Run the application with the local profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The application starts on `http://localhost:8080`.

| Endpoint | URL |
|---|---|
| Health check | http://localhost:8080/actuator/health |
| Swagger UI | http://localhost:8080/swagger-ui |
| API docs | http://localhost:8080/api-docs |

### Environment Variables

Copy `.env.example` and fill in your values:

```bash
cp .env.example .env
```

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `REDIS_HOST` | Redis hostname |
| `REDIS_PORT` | Redis port (default: 6379) |
| `BASE_URL` | Public base URL (e.g. `https://your-domain.com`) |
| `PORT` | Application port (default: 8080) |

### Run Tests

```bash
./mvnw verify
```

## Project Structure

```
src/
├── main/
│   ├── java/eu/deic/urlshortener/
│   │   ├── config/          — Spring configuration beans
│   │   ├── controller/      — HTTP layer, request/response mapping
│   │   ├── domain/          — JPA entities
│   │   ├── dto/             — API request and response shapes
│   │   ├── exception/       — Custom exceptions + global error handler
│   │   ├── repository/      — Spring Data JPA interfaces
│   │   └── service/         — Business logic
│   └── resources/
│       ├── application.yml          — shared configuration
│       ├── application-local.yml    — local development overrides
│       ├── application-prod.yml     — production configuration (env vars)
│       └── db/migration/            — Flyway SQL migrations
└── test/
    └── java/eu/deic/urlshortener/
        ├── controller/      — controller layer tests
        ├── service/         — service layer unit tests
        └── integration/     — full stack integration tests
infrastructure/
├── manual/AWS_SETUP.md      — step by step AWS console setup
└── terraform/               — infrastructure as code
```

## Database Schema

### `urls`

| Column | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Primary key |
| `short_code` | `VARCHAR(10)` | Unique short identifier |
| `original_url` | `TEXT` | Target URL |
| `created_by` | `VARCHAR(255)` | API key owner |
| `click_count` | `BIGINT` | Total redirect count |
| `created_at` | `TIMESTAMP` | Creation time |
| `expires_at` | `TIMESTAMP` | Expiry time (nullable) |
| `active` | `BOOLEAN` | Soft delete flag |

### `api_keys`

| Column | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL` | Primary key |
| `key_hash` | `VARCHAR(255)` | BCrypt hash of the API key |
| `owner` | `VARCHAR(255)` | Human-readable owner name |
| `created_at` | `TIMESTAMP` | Creation time |
| `active` | `BOOLEAN` | Revocation flag |

## Deployment

### AWS Manual Setup

See [`infrastructure/manual/AWS_SETUP.md`](infrastructure/manual/AWS_SETUP.md) for a step-by-step guide covering VPC, RDS, EC2, and security group configuration.

### Terraform

```bash
cd infrastructure/terraform
terraform init
terraform plan
terraform apply
```

### CI/CD

Two GitHub Actions workflows:

| Workflow | Trigger | Steps |
|---|---|---|
| `ci.yml` | Pull request | Build → Test → Code quality checks |
| `cd.yml` | Merge to `main` | CI → Build Docker image → Push to ECR → Deploy to EC2 |

## Learning Objectives

This project was built to develop practical experience with:

- REST API design and HTTP semantics
- Spring Boot auto-configuration and the application context
- JPA / Hibernate — entity mapping, repositories, transactions
- Redis — caching patterns, TTL strategy, atomic counters
- Rate limiting — token bucket algorithm, distributed counters
- API key authentication with BCrypt hashing
- Database schema versioning with Flyway
- AWS IaaS — VPC, EC2, RDS, security groups, IAM
- Infrastructure as Code with Terraform
- CI/CD automation with GitHub Actions
- TDD — Red / Green / Refactor cycle
- Production configuration — profiles, environment variables, secrets management

## License

MIT
