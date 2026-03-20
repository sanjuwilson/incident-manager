# ⚡ Incident Manager

> Event-Driven Incident Management & Alerting Backend

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event--Driven-black?style=flat-square)
![Keycloak](https://img.shields.io/badge/Keycloak-OAuth2%20%2F%20JWT-blue?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-336791?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square)

---

## Overview

Incident Manager is a production-style backend platform inspired by tools like **PagerDuty** and **OpsGenie**. It detects service failures, evaluates configurable rules, creates and manages incidents through a full lifecycle, and delivers email notifications — all driven by an event-based architecture.

The system is **backend-only by design**. A lightweight HTML login page is included exclusively for development and testing purposes.

---

## Architecture

```
Any Microservice  ──►  failure-events (Kafka)  ──►  incident-service
                                                           │
                                               Rule evaluation engine
                                                           │
                                               Incident created
                                                           │
                                       notification_topic (Kafka)
                                                           │
                                               notification-service
                                                           │
                                               Email delivered
```

### Services

| Service | Port | Responsibility |
|---|---|---|
| `incident-service` | 6567 | Core engine. Consumes failure events, evaluates rules, manages incident lifecycle, exposes REST APIs |
| `notification-service` | 2345 | Delivery layer. Consumes incident events, sends emails, tracks delivery status |
| `Keycloak` | 8080 | Identity provider. Issues and validates JWT tokens. Manages users and roles |
| `PostgreSQL` | Supabase | Cloud-hosted. Separate databases per service |
| `Kafka` | 9092 | Message broker. Two topics: `failure-events` and `notification_topic` |

### Kafka Topics

| Topic | Producer | Consumer | Payload |
|---|---|---|---|
| `failure-events` | Any microservice | `incident-service` | `FailureRecord` |
| `notification_topic` | `incident-service` | `notification-service` | `IncidentNotification` |

---

## Incident Engine

### Failure Detection

Services publish a `FailureRecord` to Kafka when exceptions occur. The `incident-service` consumes these events, persists them, and runs rule evaluation on every event.

```json
{
  "sourceService": "order-service",
  "failureType": "TIMEOUT",
  "operation": "POST /place-order",
  "dependency": "product-service",
  "occurredAt": "2026-01-18T19:00:05Z",
  "correlationId": "req-123"
}
```

**Supported failure types:** `TIMEOUT` `SERVICE_UNAVAILABLE` `DB_FAILURE` `AUTH_FAILURE` `VALIDATION_ERROR` `UNKNOWN`

### Rule Evaluation

Rules are stored in the database and seeded automatically via Flyway on startup. Each rule defines a threshold and time window — if enough failures occur within the window, an incident is created.

| Failure Type | Threshold | Window | Action | Severity |
|---|---|---|---|---|
| `TIMEOUT` | 3 | 60s | CREATE_INCIDENT | MEDIUM |
| `SERVICE_UNAVAILABLE` | 2 | 30s | CREATE_INCIDENT | HIGH |
| `DB_FAILURE` | 1 | 60s | CREATE_INCIDENT | CRITICAL |
| `AUTH_FAILURE` | 5 | 120s | CREATE_INCIDENT | HIGH |
| `VALIDATION_ERROR` | 10 | 300s | LOG_ONLY | LOW |
| `UNKNOWN` | 10 | 300s | IGNORE | LOW |

Rules are **wildcard-based** — a rule with no specific service applies to all services. Service-specific rules can be added via the API as overrides.

### Incident Lifecycle

```
OPEN  ──►  ACKNOWLEDGED  ──►  RESOLVED  ──►  CLOSED
```

| Status | Description |
|---|---|
| `OPEN` | Created automatically when failure threshold is crossed |
| `ACKNOWLEDGED` | Engineer takes ownership. `assignedTo` is set from the JWT `preferred_username` claim |
| `RESOLVED` | Root cause fixed. `resolvedAt` timestamp recorded |
| `CLOSED` | Incident finalized. A new incident can now be created for the same service and failure type |

### Incident Deduplication

Only one active (non-CLOSED) incident is allowed per `(sourceService, failureType)` combination. Enforced at both the application level and the database level:

```sql
CREATE UNIQUE INDEX uniq_active_incident
ON incidents (source_service, failure_type)
WHERE status <> 'CLOSED';
```

---

## Notification Service

When an incident is created, `incident-service` publishes an `IncidentNotification` to the `notification_topic` Kafka topic. The `notification-service` consumes this event and:

1. Saves a `Notification` record with status `PENDING`
2. Marks it `PROCESSING`
3. Sends an email via JavaMailSender and Thymeleaf template
4. Updates status to `SENT` or `FAILED`

Failed notifications can be retried via the REST API.

---

## Security

### Keycloak OAuth2

Both microservices are secured as **OAuth2 Resource Servers**. Every request must include a valid JWT Bearer token issued by Keycloak. Tokens are validated locally using the realm's public key — no Keycloak call is made on every request.

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/Incident-Manager
```

### Roles

| Role | Access |
|---|---|
| `ADMIN` | Full access. Manage rules, close incidents, retry notifications, view everything |
| `USER` | Read access. View incidents and notifications only |

Roles are defined as **realm roles** in Keycloak and extracted from the JWT via a custom `JwtAuthConvertor`. The converter reads from `realm_access.roles` and prefixes each with `ROLE_` for Spring Security compatibility.

### Session Management

Keycloak manages session lifecycle. Recommended settings for an incident management platform:

| Setting | Value | Reason |
|---|---|---|
| Access Token Lifespan | 5 minutes | Short-lived for security |
| SSO Session Idle | 8 hours | Covers a full on-call shift |
| SSO Session Max | 12 hours | Covers extended overnight shifts |

The development login page performs **silent token refresh** 30 seconds before each access token expires, keeping the session alive automatically up to the SSO Session Max limit.

---

## REST API Reference

### Incident Service — `/incidents`

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/incidents` | USER, ADMIN | List all incidents. Supports filters: `status`, `severity`, `service` |
| GET | `/incidents/{id}` | USER, ADMIN | Get incident by ID |
| PATCH | `/incidents/{id}/acknowledge` | USER, ADMIN | Acknowledge. Sets `assignedTo` from JWT username |
| PATCH | `/incidents/{id}/resolve` | USER, ADMIN | Mark resolved |
| PATCH | `/incidents/{id}/close` | ADMIN | Close incident |

### Incident Service — `/rules`

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/rules` | USER, ADMIN | List all rules |
| POST | `/rules` | ADMIN | Create a new rule |
| PATCH | `/rules/{id}` | ADMIN | Update an existing rule |
| DELETE | `/rules/{id}` | ADMIN | Delete a rule |

### Notification Service — `/notifications`

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/notifications` | USER, ADMIN | List all notifications. Supports filters: `status`, `incidentId` |
| GET | `/notifications/{id}` | USER, ADMIN | Get notification by ID |
| GET | `/notifications/incident/{incidentId}` | USER, ADMIN | All notifications for a specific incident |
| POST | `/notifications/{id}/retry` | ADMIN | Retry a failed notification |

---

## Getting Started

### Prerequisites

- Java 17+
- Docker and Docker Compose
- Node.js (for the development login page)
- Keycloak instance running on port 8080
- PostgreSQL databases on Supabase (connection strings configured in `application.yml`)

### 1. Start Infrastructure

All infrastructure (Kafka, Zookeeper) is managed via Docker Compose. PostgreSQL runs on Supabase cloud — no local database setup required.

```bash
docker-compose up -d
```

### 2. Run the Services

```bash
# incident-service (port 6567)
cd services/incident-service
mvn spring-boot:run

# notification-service (port 2345)
cd services/notification-service
mvn spring-boot:run
```

### 3. Run the Development Login Page

> ⚠️ **Development only.** The HTML login page is a testing tool, not a production frontend. See the [Security Note](#security-note-on-the-login-page) below.

```bash
cd frontend
npx serve . -p 3000
```

Then visit `http://localhost:3000`

### 4. Seed Test Incidents

Rules are seeded automatically via Flyway on startup. To trigger test incidents:

```bash
POST http://localhost:6567/incidents/seed
Authorization: Bearer <your-access-token>
```

This triggers failures across 5 different services covering TIMEOUT, SERVICE_UNAVAILABLE, DB_FAILURE, and AUTH_FAILURE types. **Remove this endpoint before any production deployment.**

---

## Keycloak Setup

1. Create realm: `Incident-Manager`
2. Create client: `incident-manager`
   - Client authentication: ON
   - Standard flow: ON
   - Valid redirect URIs: `http://localhost:3000/*`
   - Web origins: `http://localhost:3000`
3. Create realm roles: `admin`, `user`
4. Create users and assign roles accordingly

---

## Security Note on the Login Page

The included `index.html` login page uses **client authentication** (confidential client with a client secret). This is intentional for a development testing tool where the secret is acceptable in source code.

A production frontend would use a **public Keycloak client with PKCE** (Proof Key for Code Exchange):

- Client authentication OFF (no client secret)
- `code_verifier` generated randomly per login attempt
- `code_challenge` (SHA256 hash of verifier) sent to Keycloak
- `code_verifier` sent during token exchange — no secret ever in the browser

This is the OAuth 2.1 standard for browser-based applications.

---

## Design Decisions

**No UI** — Incident management platforms are internal ops tools. The core value is the backend engine. A Postman collection and development login page are sufficient for demonstration.

**No Eureka or API Gateway** — Services integrate through Kafka rather than synchronous HTTP. Kafka removes the need for service discovery — producers and consumers do not need to know where each other runs.

**Realm roles over client roles** — ADMIN and USER apply equally across both microservices. There is no need for per-client role separation. Realm roles are simpler to manage and match the actual access control requirements.

**PostgreSQL on Supabase** — Removes local database management. Each service has its own isolated database, matching microservice data ownership principles.

---

## Technology Stack

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security, OAuth2 Resource Server, Keycloak |
| Messaging | Apache Kafka, Spring Kafka |
| Database | PostgreSQL (Supabase), Spring Data JPA, Hibernate |
| Migrations | Flyway |
| Email | Spring Mail, Thymeleaf |
| Infrastructure | Docker, Docker Compose |
| Build | Maven |
| Dev login page | Vanilla HTML + JavaScript, npx serve |

---

## Future Improvements

- Escalation policies — increase severity if not acknowledged within a configurable time
- Auto-resolution — close incidents when failure rate drops below threshold
- PKCE authentication — replace client secret with PKCE for a production frontend
- Distributed tracing — correlation IDs across services for full request visibility
- Metrics and observability — Prometheus and Grafana integration
- Dead letter queue — handle failed Kafka messages gracefully
- Multi-channel notifications — Slack and webhook support alongside email
- Kubernetes deployment — Helm charts for production deployment
