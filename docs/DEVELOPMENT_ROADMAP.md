# AquaConnect Development Roadmap

## Project

AquaConnect
Intelligent Water Service & Incident Management Platform

---

# PHASE 0 — FOUNDATION

- Repository structure
- Git configuration
- README
- Java 21
- Spring Boot 3.5.5
- Maven
- React/Vite preparation
- Development conventions

Status: COMPLETED / IN PROGRESS

---

# PHASE 1 — BACKEND HEALTH

- Spring Boot startup
- GET /api/health
- Health controller test
- Verify Java 21
- Verify Maven

Status: COMPLETED

Expected:

GET /api/health

{
  "status": "UP",
  "service": "AquaConnect Backend"
}

---

# PHASE 2 — POSTGRESQL FOUNDATION

- Verify PostgreSQL installation
- Verify PostgreSQL service
- Create aquaconnect database
- Configure local connection
- Verify Spring Boot database connection
- Avoid unnecessary schema creation

Deliverable:

Spring Boot successfully connects to PostgreSQL.

---

# PHASE 3 — DATABASE MIGRATION

- Add Flyway
- Create first migration
- Establish migration conventions
- Verify migration execution

---

# PHASE 4 — BACKEND ARCHITECTURE

Create appropriate package organization:

- config
- controller
- dto
- entity
- enums
- exception
- repository
- security
- service
- mapper

Do not create empty speculative packages/classes unnecessarily.

---

# PHASE 5 — USER AND ROLE MODEL

Implement:

- User
- Role
- user-role relationship
- database migrations
- repositories
- validation

Roles:

- CITIZEN
- OPERATOR
- OPERATIONS_MANAGER
- FIELD_ENGINEER
- ADMIN

---

# PHASE 6 — AUTHENTICATION

Implement:

- registration
- login
- password hashing
- JWT
- authentication filter
- authorization
- protected APIs
- authentication tests

---

# PHASE 7 — INCIDENT DOMAIN

Implement:

- Incident entity
- Incident category
- Incident source
- Incident status
- Incident number
- location
- timestamps
- repositories
- DTOs
- services
- validation
- database migration

Sources:

- WEB
- IVR
- OPERATOR

---

# PHASE 8 — INCIDENT LIFECYCLE

Implement:

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

Also:

RESOLVED
→ REOPENED
→ ASSIGNED

Implement:

- status transition rules
- status history
- authorization
- tests

---

# PHASE 9 — CITIZEN INCIDENT API

Implement:

- create incident
- view own incidents
- view incident details
- status timeline
- incident history
- resolution confirmation
- reopen

Citizen must not directly set RESOLVED.

---

# PHASE 10 — DUPLICATE / SUPPORTING REPORTS

Implement:

- nearby incident detection
- time-window matching
- possible duplicate detection
- supporting citizen reports
- traceability

Never silently delete duplicate reports.

---

# PHASE 11 — PRIORITY ENGINE

Implement transparent rule-based prioritization.

Inputs may include:

- severity
- duration
- supporting reports
- estimated affected users
- infrastructure importance
- location sensitivity
- operational impact

Priority:

LOW
MEDIUM
HIGH
CRITICAL

---

# PHASE 12 — WORK ORDERS

Implement:

- WorkOrder
- assignment
- engineer relationship
- status
- priority
- instructions
- findings
- repair details
- timestamps
- APIs
- tests

---

# PHASE 13 — FIELD ENGINEER WORKFLOW

Implement:

- assigned jobs
- accept job
- start inspection
- before evidence
- findings
- repair details
- after evidence
- completion

---

# PHASE 14 — EVIDENCE STORAGE

Implement:

- MinIO
- object storage abstraction
- upload API
- metadata
- file validation
- authorization
- citizen evidence
- before/after repair evidence

---

# PHASE 15 — AUTHORITY VERIFICATION

Implement:

- repair review
- approve repair
- reject/rework
- authority verification
- RESOLVED transition
- audit records

---

# PHASE 16 — CITIZEN CONFIRMATION

Citizen sees:

RESOLVED

Citizen options:

- Confirm solved → CLOSED
- Issue still exists → REOPENED

Implement tests for both paths.

---

# PHASE 17 — NOTIFICATIONS

Implement notification abstraction.

Initial:

- in-app notifications
- development email

Later:

- SMS
- other channels

Do not tightly couple notifications to providers.

---

# PHASE 18 — GIS / POSTGIS

Implement:

- PostGIS
- coordinates
- spatial queries
- incident map
- zones
- location search

Frontend:

- Leaflet
- OpenStreetMap

---

# PHASE 19 — INFRASTRUCTURE MODEL

Create simplified synthetic infrastructure:

- Water zones
- Pipelines
- Reservoirs
- Pumps
- Valves
- Water assets

Allow incidents to associate with relevant infrastructure.

---

# PHASE 20 — REACT FOUNDATION

Implement:

- React
- Vite
- TypeScript
- React Router
- Axios
- API layer
- authentication state
- layouts
- protected routes

---

# PHASE 21 — CITIZEN DASHBOARD

Implement:

- registration
- login
- dashboard
- report incident
- GPS/location
- photo upload
- incident history
- incident details
- timeline
- map
- notifications
- resolution confirmation

---

# PHASE 22 — OPERATOR DASHBOARD

Implement:

- incoming incidents
- search
- filters
- incident details
- verification
- duplicate reports
- map
- IVR incident visibility
- operator escalation

---

# PHASE 23 — OPERATIONS MANAGER DASHBOARD

Implement:

- priority queue
- incident map
- assignment
- work orders
- field workload
- repair verification
- operational analytics

---

# PHASE 24 — FIELD ENGINEER UI

Implement:

- assigned jobs
- job details
- mobile-friendly workflow
- navigation
- inspection
- evidence upload
- repair completion

---

# PHASE 25 — ADMIN UI

Implement:

- users
- roles
- zones
- infrastructure
- configuration
- audit information

---

# PHASE 26 — API DOCUMENTATION

Implement:

- OpenAPI
- Swagger UI
- request documentation
- response documentation
- authentication documentation
- error documentation

---

# PHASE 27 — TESTING HARDENING

Implement:

- unit tests
- service tests
- repository tests
- API tests
- authentication tests
- authorization tests
- integration tests
- Testcontainers
- complete incident lifecycle tests

---

# PHASE 28 — SECURITY HARDENING

Review:

- authentication
- authorization
- validation
- CORS
- file uploads
- secrets
- logging
- error handling
- API exposure

---

# PHASE 29 — ANALYTICS

Implement meaningful metrics:

- incidents by category
- incidents by status
- incidents by zone
- verification time
- assignment time
- repair time
- resolution rate
- reopened incidents
- WEB vs IVR reports
- priority distribution
- field workload

Clearly distinguish measured values from estimates.

---

# PHASE 30 — IVR BACKEND FOUNDATION

Implement IVR abstraction.

Create provider-independent concepts for:

- call session
- language
- menu
- DTMF
- speech input
- text-to-speech
- call transfer
- webhook

IVR must call the same Incident Service.

---

# PHASE 31 — SIMULATED IVR

Create a mock/sandbox IVR flow.

Test:

Call
→ Language
→ Menu
→ Issue
→ Description
→ Location
→ Confirmation
→ Incident creation

No real telephone provider yet.

---

# PHASE 32 — IVR STATUS CHECK

Implement:

Call
→ Citizen verification
→ Incident number
→ Retrieve incident
→ Read current status

Do not expose another citizen's data.

---

# PHASE 33 — IVR OPERATOR ESCALATION

Implement provider abstraction for:

- operator transfer
- operator context
- operator dashboard

Test using mocks.

---

# PHASE 34 — SPEECH SUPPORT

Introduce abstractions for:

- speech-to-text
- text-to-speech

Use mocked providers during development.

Do not tightly couple speech technology to the Incident domain.

---

# PHASE 35 — REAL IVR PROVIDER

Select and integrate a real provider based on:

- India availability
- DTMF
- speech support
- TTS
- webhooks
- call transfer
- pricing
- development/testing support

Do not claim the provider is free unless verified.

Store credentials using environment variables.

---

# PHASE 36 — REAL IVR WEBHOOK

Implement:

Provider webhook
→ request validation
→ normalization
→ AquaConnect Incident Service
→ database
→ IVR response

Secure webhook endpoints.

---

# PHASE 37 — REAL END-TO-END IVR CALL

Perform an actual phone call.

Expected:

Phone
→ IVR number
→ language
→ report problem
→ issue input
→ description
→ location handling
→ confirmation
→ Spring Boot
→ PostgreSQL
→ incident created
→ incident visible in dashboard

This phase is not complete until the real call is tested.

---

# PHASE 38 — COMPLETE SYSTEM TEST

Test all roles:

Citizen
Operator
Operations Manager
Field Engineer
Admin

Test:

WEB incident
IVR incident
verification
priority
assignment
repair
evidence
authority verification
RESOLVED
citizen confirmation
CLOSED
REOPENED

---

# PHASE 39 — DEPLOYMENT

Implement:

- Docker
- Docker Compose
- production configuration
- PostgreSQL/PostGIS
- object storage
- frontend deployment
- backend deployment
- environment variables
- HTTPS
- GitHub Actions

---

# PHASE 40 — FINAL DOCUMENTATION

Create:

- README
- architecture diagram
- database ER diagram
- API documentation
- local setup guide
- deployment guide
- IVR setup guide
- testing guide
- role/permission matrix
- incident lifecycle documentation
- technology stack documentation

---

# FINAL ACCEPTANCE CRITERIA

AquaConnect is considered complete when:

1. Citizen can register/login.
2. Citizen can report an incident through the web dashboard.
3. Incident is stored in PostgreSQL.
4. Incident follows the defined lifecycle.
5. Operator can verify incidents.
6. Manager can prioritize and assign work.
7. Field engineer can perform repair workflow.
8. Evidence can be uploaded.
9. Authority can verify repair.
10. Citizen sees RESOLVED.
11. Citizen can confirm resolution.
12. Citizen can reopen an unresolved issue.
13. GIS displays incident/infrastructure information.
14. Notifications work through supported channels.
15. All important APIs are documented.
16. Important workflows are tested.
17. Security is implemented.
18. IVR uses the same Incident system.
19. Simulated IVR works.
20. Real IVR provider integration works.
21. A real end-to-end phone call creates an AquaConnect incident.
22. The IVR-created incident appears in the same citizen/operator/manager workflows.
23. The complete system can be built and deployed using documented steps.

Never mark an acceptance criterion complete without evidence.
