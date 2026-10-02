# AquaConnect API and Developer Guide

## API documentation

Start the backend, then open:

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Application health: `http://localhost:8080/api/health`
- Actuator health: `http://localhost:8080/actuator/health`

Swagger UI and OpenAPI are public documentation endpoints. Protected API operations use JWT bearer authentication. The provider IVR gateway is not JWT-authenticated; it has its own trust headers.

## Roles and authentication

| Role | Intended access |
|---|---|
| `CITIZEN` | Create and view own incidents; submit resolution confirmation for own incidents |
| `OPERATOR` | Operational incident, work-order, verification, IVR, escalation, and analytics functions as authorized by each endpoint |
| `OPERATIONS_MANAGER` | Manager dashboard and operational functions as authorized by each endpoint |
| `FIELD_ENGINEER` | Assigned work orders and evidence operations as authorized by each endpoint |
| `ADMIN` | Administrative role provisioning and the APIs that explicitly allow administrators |

`POST /api/auth/register` creates a citizen account. `POST /api/auth/login` returns a JWT, user ID, email, and role names. Send the token as `Authorization: Bearer <token>` to protected endpoints. The API is stateless. Missing/invalid authentication returns HTTP 401; an authenticated user without the required role returns HTTP 403. Resource ownership is enforced by the backend, not by client-supplied IDs.

Public registration does not grant privileged roles. Privileged role assignment is restricted to `ADMIN` through `POST /api/admin/users/{userId}/roles`.

## Endpoint catalog

The OpenAPI document contains request and response schemas generated from the current controllers and DTOs. The summaries below describe the current route implementation, not planned APIs.

### Authentication

| Method and path | Access | Purpose |
|---|---|---|
| `POST /api/auth/register` | Public | Register a citizen. Email must be valid; password is 8–72 characters and must contain upper-case, lower-case, and numeric characters. Optional name fields are limited to 100 characters. Returns HTTP 201 with authentication response. |
| `POST /api/auth/login` | Public | Authenticate with email/password. Returns JWT and current role names; invalid credentials return HTTP 401. |

### Citizen incidents and confirmation

| Method and path | Access | Purpose |
|---|---|---|
| `POST /api/incidents` | `CITIZEN` | Create an incident for the authenticated citizen. Category and nonblank description are required; description max 2,000 characters. Optional latitude/longitude must be within geographic bounds; optional location accuracy must be nonnegative. Source/status/priority are controlled by backend behavior. Returns HTTP 201. |
| `GET /api/incidents` | `CITIZEN` | List only the authenticated citizen's incidents. |
| `GET /api/incidents/{id}` | `CITIZEN` | Retrieve an owned incident; another citizen's incident is not disclosed. |
| `POST /api/incidents/{id}/resolution-confirmation` | `CITIZEN` | Submit the existing confirmation decision/comment DTO for an owned incident. Citizens do not set the operational `RESOLVED` status directly. |

Incident categories currently include `PIPE_LEAK`, `PIPE_BURST`, `NO_WATER_SUPPLY`, `LOW_WATER_PRESSURE`, `CONTAMINATED_WATER`, `VALVE_ISSUE`, and `OTHER`. Location sources are `GPS`, `NETWORK`, `REGISTERED_ADDRESS`, `OPERATOR`, and `UNKNOWN`.

The configured operational lifecycle is `SUBMITTED → UNDER_VERIFICATION → VERIFIED → ASSIGNED → IN_PROGRESS → REPAIR_COMPLETED → AUTHORITY_VERIFICATION → RESOLVED → CITIZEN_CONFIRMATION → CLOSED`, with the supported reopen path returning the issue to verification. Only backend workflow operations advance operational status.

### Operations, work orders, evidence, and notifications

| Method and path | Access | Purpose |
|---|---|---|
| `POST /api/work-orders` | `OPERATOR`, `OPERATIONS_MANAGER`, `ADMIN` | Create a work order from an incident ID. |
| `GET /api/work-orders` | `OPERATOR`, `OPERATIONS_MANAGER`, `FIELD_ENGINEER`, `ADMIN` | List work orders visible to the authenticated actor. |
| `GET /api/work-orders/{id}` | Same operational roles | Retrieve a work order subject to actor/assignment checks. |
| `PATCH /api/work-orders/{id}/assign` | `OPERATOR`, `OPERATIONS_MANAGER`, `ADMIN` | Assign the work order to an engineer ID. |
| `PATCH /api/work-orders/{id}/status` | Operational work-order roles | Request a state transition; service-level actor and transition rules apply. |
| `POST /api/work-orders/{id}/accept` | Operational work-order roles | Accept assigned work subject to service authorization. |
| `POST /api/work-orders/{id}/start` | Operational work-order roles | Start work subject to service authorization. |
| `POST /api/work-orders/{id}/complete` | Operational work-order roles | Submit findings/repair notes, each constrained by the request DTO. |
| `POST /api/work-orders/{id}/verify` | `OPERATOR`, `OPERATIONS_MANAGER`, `ADMIN` | Approve or reject completed repair through the authority verification DTO. |
| `POST /api/work-orders/{id}/evidence` | `FIELD_ENGINEER` | Multipart upload with `type` and `file` parts. Work-order access and evidence validation apply. |
| `GET /api/work-orders/{id}/evidence` | `OPERATOR`, `OPERATIONS_MANAGER`, `FIELD_ENGINEER`, `ADMIN` | List authorized evidence metadata. |
| `GET /api/evidence/{id}` | Same evidence roles | Download authorized evidence bytes. |
| `GET /api/notifications` | Authenticated | List only the current user's notifications. |
| `GET /api/notifications/unread` | Authenticated | List the current user's unread notifications. |
| `POST /api/notifications/{id}/read` | Authenticated | Mark an owned notification as read. |
| `GET /api/infrastructure/assets` | `OPERATOR`, `OPERATIONS_MANAGER`, `FIELD_ENGINEER`, `ADMIN` | List active configured infrastructure assets. |
| `GET /api/infrastructure/zones` | Same infrastructure roles | List configured water zones. |
| `GET /api/manager/dashboard` | `OPERATIONS_MANAGER`, `ADMIN` | Retrieve the existing manager dashboard response. |
| `GET /api/analytics/overview` | `OPERATOR`, `OPERATIONS_MANAGER`, `ADMIN` | Retrieve database-derived analytics aggregates, including the fields represented by the current analytics response DTO. |

### Administration

All routes below require `ADMIN`:

| Method and path | Purpose |
|---|---|
| `GET /api/admin/users` | List administrative user summaries and roles. |
| `GET /api/admin/roles` | List configured roles. |
| `GET /api/admin/zones` | List administrative water zones. |
| `GET /api/admin/infrastructure/assets` | List administrative infrastructure asset records. |
| `POST /api/admin/users/{userId}/roles` | Assign an allowed non-citizen role. Public registration cannot use this operation. |

### Authenticated IVR workflow

The `/api/ivr/sessions...` workflow is for authenticated `OPERATOR`, `OPERATIONS_MANAGER`, or `ADMIN` use, including the development simulator.

| Method and path | Purpose |
|---|---|
| `POST /api/ivr/sessions` | Start a session. Optional caller identifier is limited to 100 characters. Returns HTTP 201. |
| `POST /api/ivr/sessions/{id}/language` | Select `ENGLISH`, `KANNADA`, or `HINDI`. |
| `POST /api/ivr/sessions/{id}/menu` | Submit a menu integer from 1 through 9; only implemented choices are accepted by the session service. |
| `POST /api/ivr/sessions/{id}/details` | Submit a category and nonblank description up to 2,000 characters. |
| `POST /api/ivr/sessions/{id}/location` | Submit optional coordinates, required location source, and optional nonnegative accuracy. Coordinates are range-validated. |
| `POST /api/ivr/sessions/{id}/confirmation?confirmed={true\|false}` | Confirm shared incident creation or cancel/fail the session. |
| `GET /api/ivr/sessions/{id}` | Retrieve the authenticated workflow's session. |
| `GET /api/ivr/sessions/{id}/complaints` | Retrieve complaint records associated with that session's caller. |
| `POST /api/ivr/sessions/{sessionId}/escalate?callerIdentifier=...&language=...&reason=...` | Create an escalation through the authenticated operational route. Reason must be nonblank and no longer than 1,000 characters. |
| `GET /api/ivr/escalations/pending` | List pending operator escalations. |
| `POST /api/ivr/escalations/{id}/accept` | Accept an escalation. The assigned operator comes from the authenticated JWT principal, not a query parameter. |
| `POST /api/ivr/escalations/{id}/complete` | Complete an in-progress escalation. |
| `POST /api/ivr/escalations/{id}/cancel` | Cancel an eligible escalation. |

### Trusted IVR gateway

The `/api/ivr/gateway/...` routes are public at the Spring Security URL layer but are not anonymous callbacks: each operation validates `X-IVR-Trust` and `X-IVR-Caller-Id` in the controller. The trust token is configured through `AQUACONNECT_IVR_TRUST_TOKEN` in production. Never include its value in a client bundle, repository, logs, examples, or documentation.

| Method and path | Required headers | Purpose |
|---|---|---|
| `POST /api/ivr/gateway/sessions` | `X-IVR-Trust`, `X-IVR-Caller-Id` | Create a provider session. Body `callerIdentifier` may be omitted or match the trusted header; a mismatch is rejected with HTTP 400. The stored caller always comes from the header. |
| `GET /api/ivr/gateway/sessions/{id}` | Same | Return only the session owned by the trusted caller. |
| `GET /api/ivr/gateway/sessions/{id}/complaints` | Same | Return caller-safe complaint status only for the session owner. |
| `POST /api/ivr/gateway/sessions/{id}/escalate?language=...&reason=...` | Same | Queue an escalation for the active owner session. |

Missing/invalid trust is rejected; caller/session mismatch is forbidden. The gateway does not accept a JWT instead of the provider trust headers. Real provider signature verification/webhook integration is not claimed by the current gateway.

## Data and location semantics

Incident responses include identifiers, category, description, source, optional latitude/longitude, location source/accuracy, report/create/update timestamps, current status, and priority. Coordinates are supplied metadata. A normal telephone call does not automatically provide GPS. Any real provider/network location needs a compatible provider, permissions, and integration.

Infrastructure records are configured data. They are not a live SCADA feed or measured physical digital twin. Analytics responses are generated from stored application data; no synthetic values should be presented as measured municipal data.

## IVR simulator and provider boundaries

The operator-only `/ivr-simulator` page is a **development/testing simulator** using the authenticated IVR session APIs. It does not place or receive telephone calls. Menu/session support does not mean an external carrier is connected.

Speech-to-text, text-to-speech, and intent components are provider-neutral/deterministic development implementations. No production speech service or autonomous database mutation is claimed. IVR-created incidents use the same incident service as the web workflow. Real telephony, real phone/network location, and production speech providers remain future/optional integrations.

## Local developer setup

1. Install Java 21, Node.js/npm, and PostgreSQL with PostGIS.
2. Create the `aquaconnect` database and enable PostGIS.
3. Configure `SPRING_DATASOURCE_PASSWORD` in the backend process environment for the local PostgreSQL account. Avoid placing credentials in command history or source files.
4. Run backend tests or start the backend:

   ```powershell
   cd C:\Users\prave\Desktop\AquaConnect\backend
   .\mvnw.cmd test
   .\mvnw.cmd spring-boot:run
   ```

5. In another terminal, run the web client:

   ```powershell
   cd C:\Users\prave\Desktop\AquaConnect\frontend
   npm install
   npm run dev
   ```

6. Browse to `http://localhost:5173`; backend API defaults to port 8080. The configured local CORS origin defaults to the Vite development origin.

Flyway runs on application startup and Hibernate validates, rather than creates, the schema. Existing migrations must not be edited after application; use a new sequential migration for future schema changes.

## Configuration

The base `application.properties` supports local development fallbacks. Its JWT fallback is explicitly development-only; IVR and MinIO fallbacks are likewise not production credentials. The `prod` profile (`SPRING_PROFILES_ACTIVE=prod`) requires the following variables with no profile-file defaults:

| Environment variable | Required for production profile | Description |
|---|---:|---|
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | PostgreSQL password |
| `AQUACONNECT_JWT_SECRET` | Yes | JWT HMAC signing secret; minimum 32 bytes |
| `AQUACONNECT_IVR_TRUST_TOKEN` | Yes | IVR provider trust credential |
| `AQUACONNECT_MINIO_ENDPOINT` | Yes | S3-compatible object storage endpoint |
| `AQUACONNECT_MINIO_ACCESS_KEY` | Yes | Object storage access key |
| `AQUACONNECT_MINIO_SECRET_KEY` | Yes | Object storage secret key |
| `AQUACONNECT_MINIO_BUCKET` | Yes | Evidence bucket |
| `AQUACONNECT_CORS_ALLOWED_ORIGINS` | Yes | Comma-separated browser origins |
| `AQUACONNECT_JWT_EXPIRATION` | No | Token lifetime in seconds; base configuration default is 3600 |

Provide production values through the runtime environment/secret manager. Never copy real values into `.properties`, README, test fixtures, or Git.

## Testing and build commands

Backend tests (Maven wrapper is in `backend/`):

```powershell
cd C:\Users\prave\Desktop\AquaConnect\backend
.\mvnw.cmd test
```

Focused integration tests can use Surefire's `-Dtest=...` selector. Frontend scripts are defined in `frontend/package.json`:

```powershell
cd C:\Users\prave\Desktop\AquaConnect\frontend
npm test
npm run build
```

Tests use isolated H2 configuration where configured; passing H2 tests do not replace live PostgreSQL/PostGIS/Flyway verification.

## Implemented, simulated, and future

| Capability | Current classification |
|---|---|
| REST application, JWT, RBAC, role provisioning | Implemented |
| PostgreSQL/PostGIS and Flyway-backed schema | Implemented; runtime requires configured services |
| Shared web/IVR incident domain and IVR session workflow | Implemented |
| IVR operator simulator | Development/testing simulation only |
| STT/TTS and intent interfaces | Provider-neutral/deterministic development components |
| Real carrier/telephony provider and signed provider webhook integration | Future/optional; not claimed as deployed |
| Phone GPS or provider/network location lookup | Future/optional; no automatic GPS assumption |
| Physical SCADA/sensor telemetry | Not implemented |
| Production cloud deployment, Docker Compose, CI workflow | Not currently provided |
| Production email/SMS provider | Not claimed unless separately configured and verified |

## Project structure

```text
backend/    Spring Boot API, migrations, and tests
frontend/   React/Vite client and tests
database/   Reserved project database directory
 docs/      Development roadmap and API/developer guide
```
