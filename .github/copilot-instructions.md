# AquaConnect — GitHub Copilot Development Instructions

## Project

AquaConnect is an Intelligent Water Service & Incident Management Platform.

The project is a professional Java Full Stack application that connects:

- Citizens
- Control-room operators
- Operations managers
- Field engineers
- Administrators

The system supports two citizen channels:

1. Citizen Web/Mobile Dashboard
2. IVR / Toll-Free Voice Call

Both channels MUST use the same Spring Boot backend APIs and the same Incident domain.

Do NOT create a separate complaint system for IVR.

---

## Core Architecture

Use a modular monolith architecture.

Do NOT introduce microservices unless explicitly requested.

Backend:

- Java 21
- Spring Boot 3.5.5
- Maven
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- Jakarta Bean Validation
- PostgreSQL
- PostGIS
- Flyway
- Spring Boot Actuator
- OpenAPI / Swagger
- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers where appropriate

Frontend:

- React
- Vite
- TypeScript
- React Router
- Axios
- Leaflet
- OpenStreetMap
- Recharts

Local storage:

- MinIO

Development:

- Git
- GitHub
- Docker
- Docker Compose
- GitHub Actions

---

## Important Constraints

This is a software-only project.

Do NOT assume access to:

- private BWSSB databases
- private government APIs
- SCADA systems
- IoT sensors
- water-flow sensors
- private infrastructure data

Use synthetic, mock, or manually configured data where required.

Do not claim exact water-loss measurements without actual sensors.

The GIS/Digital Twin component is a simplified virtual infrastructure model, not a real-time physical digital twin.

---

## IVR Is a Core Feature

IVR must be considered from the beginning of the architecture.

However, do not implement real telephony integration until the core incident-management system is stable.

Target IVR flow:

Citizen calls
→ Language selection
→ Main menu
→ Report water problem
→ Issue selection
→ Voice/DTMF input
→ Location resolution
→ Confirmation
→ Same Incident Service
→ Incident created
→ Incident ID provided

Other IVR functions:

- Check existing complaint
- Water-supply information
- Speak with operator

Initial languages:

- English
- Kannada
- Hindi

Never assume a normal telephone call automatically provides GPS coordinates.

Location sources may include:

- GPS
- NETWORK
- REGISTERED_ADDRESS
- OPERATOR
- UNKNOWN

Real provider-specific functionality must be isolated behind an abstraction.

---

## Incident Lifecycle

The canonical lifecycle is:

SUBMITTED
→ UNDER_VERIFICATION
→ VERIFIED
→ ASSIGNED
→ IN_PROGRESS
→ REPAIR_COMPLETED
→ AUTHORITY_VERIFICATION
→ RESOLVED
→ CITIZEN_CONFIRMATION
→ CLOSED

If the citizen reports that the problem still exists:

RESOLVED
→ REOPENED
→ ASSIGNED

Citizens must never directly set an incident to RESOLVED.

The operational workflow determines RESOLVED.

---

## User Roles

CITIZEN

OPERATOR

OPERATIONS_MANAGER

FIELD_ENGINEER

ADMIN

Implement role-based authorization.

---

## Incident Sources

WEB

IVR

OPERATOR

All sources must use the same Incident domain.

---

## Incident Categories

Initial categories may include:

- WATER_LEAK
- PIPE_BURST
- NO_WATER_SUPPLY
- LOW_WATER_PRESSURE
- CONTAMINATED_WATER
- VALVE_ISSUE
- SEWER_OVERFLOW
- FLOODING
- INFRASTRUCTURE_DAMAGE
- OTHER

---

## Location

Incident location should support:

- latitude
- longitude
- locationSource
- locationAccuracy
- address

Use PostGIS for spatial functionality where appropriate.

---

## Priority

Start with a transparent rule-based priority system.

Possible inputs:

- severity
- duration
- supporting reports
- estimated affected users
- infrastructure importance
- location sensitivity
- operational impact

Possible levels:

LOW
MEDIUM
HIGH
CRITICAL

Do not invent exact measurements.

AI-based priority recommendations may be added later but must not replace transparent core business rules.

---

## Work Orders

Verified incidents can generate work orders.

Field engineers should be able to:

- view assigned work
- accept work
- start inspection
- upload before evidence
- record findings
- record repair details
- upload after evidence
- submit repair completion

Authority verifies the repair before the incident becomes RESOLVED.

---

## Evidence

Evidence may include:

- citizen photos
- before-repair photos
- after-repair photos
- documents

Use object storage rather than storing large binary files directly in PostgreSQL.

Local development should use MinIO.

---

## Security

Never:

- store plaintext passwords
- expose passwords
- commit secrets
- hard-code production credentials
- expose JWT secrets
- bypass authorization

Use:

- secure password hashing
- JWT
- role-based authorization
- validation
- DTOs
- secure configuration
- environment variables

---

## Backend Design Rules

Use clean separation of:

- controllers
- DTOs
- services
- repositories
- entities
- enums
- configuration
- security
- exceptions
- mappers
- utilities where justified

Controllers must not contain business logic.

Use constructor injection.

Use DTOs rather than exposing JPA entities directly.

Use global exception handling.

Use proper HTTP status codes.

Validate request data.

---

## Database Rules

Use PostgreSQL.

Use PostGIS for geospatial requirements.

Use Flyway for schema migrations after the database foundation is established.

Never casually modify an already-applied Flyway migration.

Never drop production data.

Use:

- primary keys
- foreign keys
- unique constraints
- indexes
- appropriate constraints

---

## Testing Rules

Every important feature must include tests.

Use:

- JUnit 5
- Mockito
- Spring Boot Test
- MockMvc
- Testcontainers where appropriate

Test:

- business logic
- validation
- authorization
- API behavior
- database behavior
- incident lifecycle
- important integrations

Do not consider a feature complete until appropriate tests pass.

---

## API Rules

Use RESTful APIs.

Use:

- DTOs
- validation
- pagination
- filtering
- sorting
- consistent error responses
- OpenAPI documentation

Potential API areas:

/api/auth/*
/api/users/*
/api/incidents/*
/api/work-orders/*
/api/evidence/*
/api/infrastructure/*
/api/zones/*
/api/notifications/*
/api/ivr/*
/api/admin/*

Do not create endpoints without a clear requirement.

---

## Frontend Rules

Use React + TypeScript.

Keep frontend organized into reusable components.

Handle:

- loading states
- errors
- empty states
- authentication
- authorization
- API failures
- validation

Citizen UI must be responsive.

Field engineer UI should be mobile-friendly.

---

## GIS Rules

Use:

- Leaflet
- OpenStreetMap
- PostGIS

Support:

- incident locations
- zones
- water assets
- pipelines
- reservoirs
- pumps
- valves

Use synthetic infrastructure data.

Do not expose sensitive infrastructure information unnecessarily to citizens.

---

## AI/ML Rules

AI/ML is optional.

Do not introduce AI merely for demonstration.

Potential future AI features:

- image classification
- duplicate detection
- priority recommendation
- predictive maintenance
- demand forecasting
- AI operator assistant

If Python is used, it should be a supporting service.

Core business logic remains in Java Spring Boot.

---

## External Integrations

External providers must be isolated behind interfaces/abstractions.

This applies to:

- IVR
- SMS
- email
- object storage
- AI/ML services
- maps where applicable

Do not tightly couple business logic to a provider.

Never claim an external integration works without actually testing it.

---

## Development Rules

Before changing code:

1. Inspect the existing project.
2. Inspect related files.
3. Understand existing behavior.
4. Avoid duplicate implementations.
5. Preserve working functionality.

Implement only the requested phase.

Do not implement future phases automatically.

Do not add unnecessary dependencies.

Do not introduce technologies without justification.

After implementation:

1. Run appropriate tests.
2. Report files changed.
3. Report commands executed.
4. Report test results.
5. Report any remaining issues.
6. Stop.

Never claim that a command was executed unless it actually was.

Never claim an external service works without evidence.

---

## Git Rules

Use meaningful commits such as:

feat: add incident domain

feat: add JWT authentication

feat: add citizen incident reporting

feat: add work order workflow

feat: add IVR webhook integration

test: add incident lifecycle tests

fix: correct incident status transition

Do not commit:

- target/
- node_modules/
- .env
- secrets
- credentials

---

## Current Development Principle

AquaConnect must be developed incrementally.

The complete roadmap is stored separately in:

docs/DEVELOPMENT_ROADMAP.md

Always follow the current phase from that roadmap.

Never jump ahead without explicit instruction.

After completing a phase, stop and wait for confirmation.
