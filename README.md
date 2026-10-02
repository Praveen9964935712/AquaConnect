# AquaConnect

AquaConnect is an Intelligent Water Service & Incident Management Platform. It provides a shared incident workflow for citizen-submitted reports, operational verification and field repair, with an IVR session model and a development simulator.

## What is implemented

- **Backend:** Java 21, Spring Boot 3.5.5, Spring Web, Spring Security, JWT, Jakarta Validation, Spring Data JPA, Hibernate Spatial, and Actuator.
- **Database:** PostgreSQL/PostGIS with Flyway migrations and Hibernate schema validation (`ddl-auto=validate`). The current schema migrations are V1–V16.
- **Web application:** React, TypeScript, Vite, React Router, Axios, and Leaflet dependencies. Role-aware dashboards and the IVR development simulator use the existing REST API.
- **Incident operations:** citizen incident reporting and ownership, lifecycle transitions, work orders, field findings/evidence, authority verification, resolution confirmation, notifications, infrastructure data, and analytics.
- **Access control:** roles are `CITIZEN`, `OPERATOR`, `OPERATIONS_MANAGER`, `FIELD_ENGINEER`, and `ADMIN`.
- **API docs:** Springdoc OpenAPI and Swagger UI are available while the backend is running.

## Architecture

The application is a modular monolith. The React client calls Spring Boot REST controllers; services contain workflow logic; repositories use PostgreSQL/PostGIS. Web and IVR incident reporting share the same incident domain and service. See [docs/API_AND_DEVELOPER_GUIDE.md](docs/API_AND_DEVELOPER_GUIDE.md) for endpoint, setup, and security details. The project roadmap remains in [docs/DEVELOPMENT_ROADMAP.md](docs/DEVELOPMENT_ROADMAP.md).

## Requirements

- Java 21
- PostgreSQL 17 with PostGIS available in the target database
- Node.js and npm (versions compatible with the frontend dependencies)
- For production-profile use: environment-provided datasource, JWT, IVR trust, object-storage, and CORS configuration

Docker is not required for local development. No Docker Compose deployment or GitHub Actions CI workflow is currently provided.

## Database setup

Create a PostgreSQL database named `aquaconnect` and enable the PostGIS extension for the database. The backend runs Flyway migrations automatically at startup, then validates the schema with Hibernate. Do not edit migrations that have already been applied; add a new sequential migration only when a schema change is required.

The local configuration defaults to `localhost:5432` and username `postgres`. Set `SPRING_DATASOURCE_PASSWORD` in the process environment to the password configured for your local PostgreSQL account. Never commit credentials.

## Run locally

Start PostgreSQL first, then in one terminal:

```powershell
cd C:\Users\prave\Desktop\AquaConnect\backend
.\mvnw.cmd spring-boot:run
```

The backend listens on port 8080. Local fallbacks for JWT and IVR trust configuration are for development only; they are not production credentials. In another terminal:

```powershell
cd C:\Users\prave\Desktop\AquaConnect\frontend
npm install
npm run dev
```

The Vite development server defaults to `http://localhost:5173`. The operator IVR simulator is at `/ivr-simulator` and is a development/testing interface, not a telephone network.

## Production configuration

Activate Spring's `prod` profile with `SPRING_PROFILES_ACTIVE=prod`. The production profile requires these environment variables and has no values embedded in the profile file:

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Database account |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `AQUACONNECT_JWT_SECRET` | JWT HMAC signing key; at least 32 bytes for the configured algorithm |
| `AQUACONNECT_JWT_EXPIRATION` | Optional JWT lifetime in seconds; base configuration defaults to 3600 |
| `AQUACONNECT_IVR_TRUST_TOKEN` | Trusted IVR provider boundary credential |
| `AQUACONNECT_MINIO_ENDPOINT` | S3-compatible/MinIO endpoint |
| `AQUACONNECT_MINIO_ACCESS_KEY` | Object-storage access key |
| `AQUACONNECT_MINIO_SECRET_KEY` | Object-storage secret key |
| `AQUACONNECT_MINIO_BUCKET` | Evidence bucket name |
| `AQUACONNECT_CORS_ALLOWED_ORIGINS` | Comma-separated allowed browser origins |

Supply secrets using the deployment environment or secret manager. Do not put production secrets in source files, documentation, shell history, or Git. The `prod` profile suppresses detailed Actuator health output, disables Open-In-View, and does not enable SQL statement logging.

## API and security

When the backend is running:

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Basic health: `http://localhost:8080/actuator/health`

Protected REST APIs use a stateless JWT bearer token. Registration creates a citizen account; privileged role assignment is admin-only. Citizen incident and notification resources are scoped to the authenticated owner. The IVR gateway is a distinct provider trust boundary: it requires `X-IVR-Trust` and `X-IVR-Caller-Id`; caller identity is taken from the validated header and cannot be overridden by the body. Never put an IVR trust token in browser code.

See the [API and developer guide](docs/API_AND_DEVELOPER_GUIDE.md) for routes, roles, request fields, lifecycle behavior, and security details.

## IVR implementation truth

- **Implemented:** session/menu workflows, shared incident creation, caller-owned complaint status, escalation state handling, trust/caller validation, and the authenticated operator workflow.
- **Development/testing simulator:** frontend simulation of the existing authenticated IVR session APIs. It does not place calls.
- **Provider-neutral abstractions:** deterministic speech-to-text, text-to-speech, and intent components are development implementations, not verified production speech services.
- **Future/optional:** real telecom provider/webhook integration, network-derived caller location, real phone GPS, and production STT/TTS providers. Ordinary phone calls do not automatically provide GPS coordinates.

No live municipal SCADA/sensor telemetry or real-time physical digital twin is claimed. Infrastructure data is configured/synthetic. Do not interpret analytics as measurements the system does not collect.

## Tests and builds

Backend tests:

```powershell
cd C:\Users\prave\Desktop\AquaConnect\backend
.\mvnw.cmd test
```

Focused backend tests can be run with Surefire, for example:

```powershell
.\mvnw.cmd "-Dtest=IvrPhase45IntegrationTest,IvrGatewayAndAdminProvisioningIntegrationTest" test
```

Frontend tests and production build:

```powershell
cd C:\Users\prave\Desktop\AquaConnect\frontend
npm test
npm run build
```

Tests use an isolated H2 profile. Runtime PostgreSQL/PostGIS and Flyway verification must be performed separately against a configured database.

## Current operational limitations

- No real telephony provider or production IVR callback verification is configured.
- Speech and intent implementations are deterministic/provider-neutral development components.
- Location is supplied metadata; no provider/network location lookup is claimed.
- MinIO credentials and endpoint must be configured for evidence storage in a deployed environment.
- No Dockerfiles, Compose deployment, cloud deployment, or CI workflow is currently supplied.
