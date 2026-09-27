# Hospital Pharmaceutical MES V2.0 Foundation Design

## 1. Purpose

This specification defines EPIC-001 Project Foundation for Hospital Pharmaceutical MES V2.0. The deliverable is a public Apache-2.0 licensed Monorepo that builds successfully and establishes stable technical and module boundaries for later IAM/RBAC, audit, electronic signature, master data, and production execution work.

The Foundation phase implements MES-001 through MES-011 only. It does not implement production business behavior, user authentication flows, RBAC data, master data, audit records, or electronic signatures.

## 2. Repository and Licensing

- Repository name: `hospital-pharma-mes`
- Visibility: public
- License: Apache License 2.0
- Default branch: `main`
- Repository model: modular-monolith Monorepo

The root repository coordinates documentation, backend and frontend builds, local infrastructure, database assets, deployment assets, and developer scripts.

## 3. Technology Baseline

### Backend

- Java 21
- Spring Boot 3.x
- Maven
- Spring Security with a JWT extension boundary
- MyBatis-Plus
- MariaDB JDBC
- Flyway
- springdoc OpenAPI 3
- Redis
- MinIO

### Frontend

- Vue 3
- Vite
- TypeScript
- Ant Design Vue
- Pinia
- Vue Router
- Axios
- ECharts

### Local infrastructure

- MariaDB
- Redis
- MinIO
- Docker Compose

Exact dependency versions must be pinned centrally and chosen from current stable, mutually compatible releases at implementation time. Spring Boot must remain on the 3.x line and the compiler release must remain Java 21.

## 4. Monorepo Structure

```text
hospital-pharma-mes/
├── AGENTS.md
├── LICENSE
├── README.md
├── .gitignore
├── docker-compose.yml
├── pom.xml
├── backend/
│   ├── pom.xml
│   ├── mes-boot/
│   ├── mes-common/
│   ├── mes-security/
│   ├── mes-system/
│   ├── mes-masterdata/
│   ├── mes-product/
│   ├── mes-process/
│   ├── mes-form/
│   ├── mes-ebr/
│   ├── mes-wms/
│   ├── mes-production/
│   ├── mes-execution/
│   ├── mes-equipment/
│   ├── mes-qc/
│   ├── mes-qms/
│   ├── mes-release/
│   ├── mes-workflow/
│   ├── mes-traceability/
│   ├── mes-integration/
│   └── mes-reporting/
├── frontend/mes-web/
├── database/
│   ├── migration/
│   ├── seed/
│   └── docs/
├── docs/
│   ├── architecture/
│   ├── database/
│   ├── api/
│   ├── compliance/
│   ├── development/
│   └── superpowers/
├── deploy/
│   ├── docker/
│   └── nginx/
└── scripts/
```

The root Maven project aggregates `backend`. The backend parent owns backend dependency and plugin management. Only `mes-boot` is an executable Spring Boot application; all other backend modules are libraries.

## 5. Backend Boundaries

`mes-common` contains framework-neutral shared contracts and narrowly scoped web infrastructure: API response records, page response records, exception types, error codes, and trace-context support. Domain services must not return API response wrappers.

`mes-security` defines the Spring Security and JWT integration boundary. Foundation may configure a development-safe security chain and extension interfaces, but login, users, roles, permissions, refresh tokens, password policy, and authorization data belong to EPIC-002.

`mes-system` reserves the boundary for organization, IAM-adjacent system administration, audit integration, and platform configuration. Foundation adds no business schema to this module.

`mes-masterdata` reserves the boundary for products, materials, units, suppliers, sites, areas, lines, and equipment classification. Foundation adds no master-data behavior.

The remaining business modules compile independently with explicit Maven dependencies. They must not form dependency cycles. Cross-module notifications will later use domain events; complex read models will use dedicated query services rather than reverse dependencies.

Business modules follow this package model when implementation begins:

```text
api/{controller,request,response}
application/{service,command,query}
domain/{model,service,repository,event}
infrastructure/{persistence,integration}
```

Persistence entities and domain models are not required to be the same class.

## 6. Foundation Runtime

`mes-boot` assembles the application and owns runtime configuration. Configuration must use environment variables for credentials and endpoints, provide safe local defaults where appropriate, and never commit real secrets.

The runtime includes:

- MariaDB datasource configuration
- Flyway startup migration
- MyBatis-Plus configuration
- Redis connectivity configuration
- MinIO client configuration
- OpenAPI metadata and `/api/v1` convention
- health endpoints for application and infrastructure diagnostics
- trace ID propagation and structured logging context
- centralized exception-to-response mapping

The initial Flyway migration creates only a minimal Foundation verification table or equivalent technical object. Production, IAM, audit, and master-data tables are outside this phase. Shared migrations are immutable after publication: corrections use new migration versions.

## 7. API and Error Contract

API routes use `/api/v1`.

Successful and failed HTTP responses use a stable envelope:

```java
public record ApiResponse<T>(
    String code,
    String message,
    T data,
    String traceId
) {}
```

Pagination uses:

```java
public record PageResponse<T>(
    long page,
    long size,
    long total,
    List<T> records
) {}
```

The base exception hierarchy is:

```text
MesException
├── BusinessException
├── ValidationException
├── StateTransitionException
├── PermissionException
├── ResourceConflictException
└── ComplianceException
```

Every request receives or preserves a trace ID. The same trace ID appears in logging context, response headers, and `ApiResponse.traceId`. Internal stack traces and sensitive connection details are never returned to clients.

## 8. Frontend Foundation

`frontend/mes-web` is a typed Vue application with router, Pinia store registration, Ant Design Vue, Axios, and ECharts installed. It establishes directories for APIs, assets, components, layouts, router, stores, hooks, utilities, permission handling, business views, and the future process/form/eBR designers.

Foundation delivers a minimal application shell and placeholder dashboard sufficient to prove build and routing. It does not implement login behavior, dynamic menus, permissions, or production pages. Those features are delivered by their later epics.

## 9. Docker Compose Development Environment

The Compose topology includes MariaDB, Redis, and MinIO with named volumes, health checks, explicit ports, and environment-variable overrides. A checked-in example environment file documents local variables; real `.env` files remain ignored.

Application containers are not required for MES-011. The backend and frontend may run from developer tooling against containerized infrastructure. Dockerfiles may be placed under `deploy/docker` only when they support a verified Foundation workflow.

## 10. Frozen Engineering Rules in AGENTS.md

The repository guidance must include these rules:

1. Java 21 and Spring Boot 3.x only.
2. MariaDB-compatible SQL only.
3. MyBatis-Plus for ordinary CRUD; explicit SQL/XML is allowed for traceability, lineage, eBR aggregation, and reporting queries.
4. No Maven dependency cycles.
5. Business state changes use explicit commands and domain methods, never generic `updateStatus` APIs.
6. API wrappers remain in the API layer.
7. Domain models and persistence entities may differ.
8. Production and regulated records are not physically deleted.
9. Optimistic locking is required for mutable regulated aggregates.
10. Critical operations must be designed for audit events and electronic signatures from the beginning.
11. Flyway migrations are append-only after entering a shared environment.
12. Workflow/BPMN support must not become a runtime dependency of the core production state machine.
13. Dynamic form expressions must not execute arbitrary JavaScript, SQL, or SpEL.
14. `RELEASED` means finished-product release, not merely production completion.
15. Foundation work must not pre-implement later business epics.

## 11. MES-001 Through MES-011 Scope

| Task | Foundation deliverable |
|---|---|
| MES-001 | Maven multi-module aggregation and module boundary skeletons |
| MES-002 | Java 21 Spring Boot executable module and configuration profiles |
| MES-003 | MariaDB datasource and Flyway baseline migration |
| MES-004 | MyBatis-Plus integration and mapper verification |
| MES-005 | Redis configuration and connectivity boundary |
| MES-006 | MinIO configuration and client boundary |
| MES-007 | springdoc OpenAPI configuration and documented health/demo endpoint |
| MES-008 | Exception hierarchy and centralized HTTP exception mapping |
| MES-009 | API and page response contracts |
| MES-010 | Trace ID filter/interceptor and logging context |
| MES-011 | MariaDB, Redis, and MinIO Docker Compose environment |

MES-012 CI is explicitly deferred unless separately authorized.

## 12. Testing and Acceptance

Implementation follows test-first development for behavior. Generated scaffolding and declarative configuration are verified through build, configuration, and integration checks.

Acceptance requires:

- the complete Maven reactor compiles and all backend tests pass on Java 21;
- the frontend installs reproducibly, type-checks, tests, and builds;
- application-context tests prove module assembly;
- tests prove API envelope, exception mapping, and trace ID behavior;
- a mapper integration test proves MyBatis-Plus wiring against MariaDB when Docker is available;
- Flyway validates and migrates a clean MariaDB instance when Docker is available;
- OpenAPI JSON is exposed when the application runs;
- Compose configuration validates, and services become healthy when Docker is available;
- no secrets or local build artifacts are tracked;
- README contains setup, build, test, and local-run instructions;
- the implementation stays within MES-001 through MES-011.

If Docker is unavailable on the implementation host, Compose runtime and MariaDB integration checks must be reported as unverified rather than represented as passing. Static Compose validation may use an available compatible validator, but it does not replace a real Docker run.

## 13. Failure Handling and Operational Safety

Startup must fail clearly when required production configuration is missing. Local development configuration may use documented defaults but must not silently fall back to embedded databases or in-memory substitutes that hide MariaDB incompatibilities.

Infrastructure outages must surface through health information and concise logs. API failures use stable error codes and do not disclose stack traces. Trace IDs make a client-visible failure correlatable with server logs.

## 14. Deferred Work

The following are intentionally deferred:

- MES-012 CI
- IAM/RBAC implementation
- login and JWT issuance/refresh flows
- audit persistence and electronic signatures
- master-data schema and CRUD
- Flowable process definitions
- production, warehouse, equipment, quality, eBR, release, and traceability behavior
- deployment to a shared or production environment

These exclusions prevent Foundation from freezing premature business models while preserving the module and integration points those epics require.
