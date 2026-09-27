# Hospital Pharmaceutical MES V2.0 Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build and publish a runnable, testable Monorepo foundation for MES-001 through MES-011 without implementing production business behavior.

**Architecture:** A root Maven reactor aggregates a backend modular monolith whose only executable artifact is `mes-boot`; the remaining backend projects are dependency-controlled library modules. A separate Vue application and a Compose-managed MariaDB/Redis/MinIO environment complete the development foundation.

**Tech Stack:** Java 21, Spring Boot 3.5.16, Maven, Spring Security, MyBatis-Plus 3.5.17, MariaDB, Flyway, Redis, MinIO Java SDK 9.0.3, springdoc-openapi 2.9.1, Vue 3, Vite, TypeScript, Ant Design Vue 4.x, Pinia, Vue Router, Axios, ECharts, Vitest, Docker Compose.

**Spec:** `docs/superpowers/specs/2026-09-27-foundation-design.md`

## Global Constraints

- Repository is public, named `hospital-pharma-mes`, licensed under Apache License 2.0, and uses `main` as its default branch.
- Java compiler release is exactly 21; Spring Boot stays on the 3.x line.
- SQL is MariaDB-compatible. Do not substitute H2 for database compatibility checks.
- Only `mes-boot` is executable; business modules are libraries and may not form Maven dependency cycles.
- Foundation scope is MES-001 through MES-011. MES-012 CI and later business epics are deferred.
- API routes use `/api/v1`; API envelopes never leak into domain services.
- Shared Flyway migrations are append-only after publication.
- Real credentials, `.env`, build output, IDE state, and runtime data are never committed.
- Production and regulated records may not be designed for physical deletion.
- The MinIO server image must be pinned to a maintained 2026 release, not an unmaintained floating legacy image; the Java SDK is Apache-2.0 version 9.0.3.

## Review Focus

1. An invalid incoming trace ID must be replaced, while a valid safe trace ID must be preserved and echoed consistently.
2. Unexpected exceptions must produce a stable generic response without stack traces or secret-bearing exception messages.
3. A clean MariaDB schema must migrate once, and a second startup must not reapply or mutate the migration.
4. Missing production credentials must fail startup clearly; local documented defaults must not activate under the production profile.
5. A module dependency regression must be caught by a reactor/enforcer verification before it can introduce a cycle or forbidden executable module.

---

### Task 1: Repository Policy and Root Reactor (MES-001)

**Files:**
- Create: `LICENSE`, `.gitignore`, `.editorconfig`, `.gitattributes`, `README.md`, `AGENTS.md`, `pom.xml`
- Create: `backend/pom.xml`
- Create: `docs/{architecture,database,api,compliance,development}/README.md`
- Create: `database/{migration,seed,docs}/README.md`, `deploy/{docker,nginx}/README.md`, `scripts/README.md`

**Interfaces:**
- Consumes: approved Foundation design.
- Produces: root Maven reactor `com.hospital.mes:hospital-pharma-mes:0.1.0-SNAPSHOT`; backend parent `com.hospital.mes:mes-backend`; repository rules inherited by all later tasks.

- [ ] **Step 1: Add a reactor policy test fixture**

Create a temporary minimal child module named `backend/mes-common` with a test-only POM assertion executed by Maven Enforcer: Java version `[21,22)`, Maven version `[3.9,)`, dependency convergence, upper-bound dependencies, and no snapshot dependencies outside this reactor.

- [ ] **Step 2: Run the reactor and verify RED**

Run: `mvn -B -ntp validate`

Expected: FAIL because root and backend aggregators do not yet declare the child/module policy consistently.

- [ ] **Step 3: Add repository files and aggregator POMs**

Declare UTF-8, Java 21, `0.1.0-SNAPSHOT`, centralized plugin management, reproducible build output timestamps, Apache-2.0 metadata, and the full backend module list. Write `AGENTS.md` with all 15 frozen rules from the spec and document Foundation scope in `README.md`.

- [ ] **Step 4: Run reactor policy verification**

Run: `mvn -B -ntp validate`

Expected: PASS with the root and backend projects discovered.

- [ ] **Step 5: Commit**

```text
git add .
git commit -m "build: establish MES monorepo foundation"
```

### Task 2: Backend Module Skeleton and Dependency Boundaries (MES-001)

**Files:**
- Create: `backend/mes-{boot,common,security,system,masterdata,product,process,form,ebr,wms,production,execution,equipment,qc,qms,release,workflow,traceability,integration,reporting}/pom.xml`
- Create: each library module's `src/main/java/com/hospital/mes/<module>/package-info.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/architecture/ModuleBoundaryTest.java`

**Interfaces:**
- Consumes: Maven coordinates and module list from Task 1.
- Produces: 20 resolvable backend artifacts; `ModuleBoundaryTest` checks that only `mes-boot` applies the Spring Boot repackage goal and that module dependencies are acyclic.

- [ ] **Step 1: Write `ModuleBoundaryTest`**

First create the declarative module POM skeletons needed to execute tests. Then write the test to load every `backend/*/pom.xml`, assert the exact 20 module names, assert only `mes-boot` contains the Spring Boot repackage goal, build the internal dependency graph, and fail when a synthetic cycle is introduced in the graph helper.

- [ ] **Step 2: Run the boundary test and verify RED**

Run: `mvn -B -ntp -pl backend/mes-boot -am -Dtest=ModuleBoundaryTest test`

Expected: FAIL because module POMs and the graph helper do not exist.

- [ ] **Step 3: Add the module POMs and graph helper**

Library modules use `jar` packaging and minimal dependencies. `mes-boot` depends only on the Foundation modules needed at runtime. Do not add speculative business-module dependencies.

- [ ] **Step 4: Run boundary and reactor verification**

Run: `mvn -B -ntp test`

Expected: PASS, including the synthetic-cycle rejection assertion.

- [ ] **Step 5: Commit**

```text
git add backend pom.xml
git commit -m "build: add controlled backend module boundaries"
```

### Task 3: Spring Boot Application and Profile Configuration (MES-002)

**Files:**
- Create: `backend/mes-boot/src/main/java/com/hospital/mes/MesApplication.java`
- Create: `backend/mes-boot/src/main/resources/application.yml`
- Create: `backend/mes-boot/src/main/resources/application-local.yml`
- Create: `backend/mes-boot/src/main/resources/application-prod.yml`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/MesApplicationTest.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/config/ProductionConfigurationTest.java`

**Interfaces:**
- Consumes: module reactor from Tasks 1-2.
- Produces: `MesApplication.main(String[])`; profiles `local` and `prod`; actuator health base path `/actuator`.

- [ ] **Step 1: Write application and production-profile tests**

`MesApplicationTest` asserts the context starts with infrastructure health contributors disabled through test properties. `ProductionConfigurationTest` starts an isolated application context with `prod` and no credentials and asserts a clear binding/startup failure naming the missing environment-backed property.

- [ ] **Step 2: Run tests and verify RED**

Run: `mvn -B -ntp -pl backend/mes-boot -am -Dtest=MesApplicationTest,ProductionConfigurationTest test`

Expected: FAIL because the application and profile resources do not exist.

- [ ] **Step 3: Implement application and profiles**

Use environment placeholders for database, Redis, and object-storage credentials. Permit documented local defaults only in `application-local.yml`; declare no default active profile and no embedded database fallback.

- [ ] **Step 4: Run tests and verify GREEN**

Run: `mvn -B -ntp -pl backend/mes-boot -am -Dtest=MesApplicationTest,ProductionConfigurationTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

```text
git add backend/mes-boot
git commit -m "feat: bootstrap Spring Boot application"
```

### Task 4: API Contracts and Exception Mapping (MES-008, MES-009)

**Files:**
- Create: `backend/mes-common/src/main/java/com/hospital/mes/common/api/ApiResponse.java`
- Create: `backend/mes-common/src/main/java/com/hospital/mes/common/api/PageResponse.java`
- Create: `backend/mes-common/src/main/java/com/hospital/mes/common/exception/{MesException,BusinessException,ValidationException,StateTransitionException,PermissionException,ResourceConflictException,ComplianceException}.java`
- Create: `backend/mes-common/src/main/java/com/hospital/mes/common/web/GlobalExceptionHandler.java`
- Create: `backend/mes-common/src/main/java/com/hospital/mes/common/trace/TraceIdProvider.java`
- Create: `backend/mes-common/src/test/java/com/hospital/mes/common/api/ApiResponseTest.java`
- Create: `backend/mes-common/src/test/java/com/hospital/mes/common/web/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Consumes: no later task.
- Produces: `ApiResponse<T>(String code, String message, T data, String traceId)`; `PageResponse<T>(long page, long size, long total, List<T> records)`; `MesException(String code, String message)`; `TraceIdProvider.currentTraceId()`; REST exception handler.

- [ ] **Step 1: Write contract tests**

Assert record field order and values, immutable page records, each exception's code/message, HTTP mappings for validation/conflict/permission/compliance/state/business errors, and generic code `INTERNAL_ERROR` for an unexpected exception. Assert generic handling excludes the original exception message and stack trace.

- [ ] **Step 2: Run tests and verify RED**

Run: `mvn -B -ntp -pl backend/mes-common -Dtest=ApiResponseTest,GlobalExceptionHandlerTest test`

Expected: FAIL because contracts and handler are absent.

- [ ] **Step 3: Implement minimal contracts and handler**

Keep web-specific classes out of domain packages. Map stable exception categories to explicit HTTP status codes and source trace IDs only from an injected `TraceIdProvider`.

- [ ] **Step 4: Run tests and verify GREEN**

Run: `mvn -B -ntp -pl backend/mes-common -Dtest=ApiResponseTest,GlobalExceptionHandlerTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

```text
git add backend/mes-common
git commit -m "feat: add API and exception contracts"
```

### Task 5: Trace ID and Logging Context (MES-010)

**Files:**
- Create: `backend/mes-common/src/main/java/com/hospital/mes/common/trace/TraceContext.java`
- Create: `backend/mes-common/src/main/java/com/hospital/mes/common/trace/MdcTraceIdProvider.java`
- Create: `backend/mes-common/src/main/java/com/hospital/mes/common/web/TraceIdFilter.java`
- Create: `backend/mes-common/src/test/java/com/hospital/mes/common/web/TraceIdFilterTest.java`
- Modify: `backend/mes-boot/src/main/resources/application.yml`
- Modify: `backend/mes-common/src/main/java/com/hospital/mes/common/web/GlobalExceptionHandler.java`

**Interfaces:**
- Consumes: Servlet filter chain and SLF4J MDC.
- Produces: request/response header `X-Trace-Id`; MDC key `traceId`; `TraceContext.currentTraceId()`; default `TraceIdProvider` implementation.

- [ ] **Step 1: Write trace behavior tests**

Assert a missing ID generates a UUID, a valid `[A-Za-z0-9._-]{1,64}` ID is preserved, invalid/oversized/control-character IDs are replaced, response and MDC use the same ID during the chain, and MDC is cleared after success and exceptions.

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -B -ntp -pl backend/mes-common -Dtest=TraceIdFilterTest test`

Expected: FAIL because trace classes are absent.

- [ ] **Step 3: Implement trace context and filter**

Use `OncePerRequestFilter`, secure validation, UUID generation, a `finally` block for MDC cleanup, and the configured logging pattern.

- [ ] **Step 4: Run common tests and verify GREEN**

Run: `mvn -B -ntp -pl backend/mes-common test`

Expected: PASS, including exception responses carrying the trace ID.

- [ ] **Step 5: Commit**

```text
git add backend/mes-common backend/mes-boot/src/main/resources/application.yml
git commit -m "feat: propagate request trace identifiers"
```

### Task 6: MariaDB, Flyway, and MyBatis-Plus (MES-003, MES-004)

**Files:**
- Create: `backend/mes-boot/src/main/resources/db/migration/V001__foundation_probe.sql`
- Create: `backend/mes-boot/src/main/java/com/hospital/mes/foundation/persistence/FoundationProbeEntity.java`
- Create: `backend/mes-boot/src/main/java/com/hospital/mes/foundation/persistence/FoundationProbeMapper.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/foundation/persistence/FoundationPersistenceIT.java`
- Create: `backend/mes-boot/src/test/resources/application-test.yml`
- Modify: `backend/mes-boot/pom.xml`
- Modify: `backend/mes-boot/src/main/resources/application.yml`

**Interfaces:**
- Consumes: MariaDB JDBC settings from Task 3.
- Produces: table `sys_foundation_probe(id BIGINT, probe_key VARCHAR(64), created_at TIMESTAMP)` with unique `probe_key`; `FoundationProbeMapper extends BaseMapper<FoundationProbeEntity>`.

- [ ] **Step 1: Write MariaDB integration test**

Using Testcontainers only when a Docker-compatible runtime is available, assert Flyway applies `V001` once, a mapper inserts/selects `probe_key='foundation-ready'`, duplicate keys fail, and a second context reports the migration as already successful rather than reapplying it.

- [ ] **Step 2: Run test and verify RED**

Run: `mvn -B -ntp -pl backend/mes-boot -am -Dtest=FoundationPersistenceIT test`

Expected: FAIL because migration, entity, mapper, and dependencies are absent. If Docker is unavailable, record the RED setup result and continue only with explicit runtime-verification status.

- [ ] **Step 3: Implement datasource, migration, entity, and mapper**

Use MariaDB Connector/J, Flyway MariaDB support, MyBatis-Plus Spring Boot 3 starter, snake-case mapping, and no schema-generation framework.

- [ ] **Step 4: Run integration and unit suites**

Run: `mvn -B -ntp -pl backend/mes-boot -am test`

Expected: PASS when Docker is available; otherwise unit tests PASS and the MariaDB integration test is explicitly SKIPPED with a documented reason.

- [ ] **Step 5: Commit**

```text
git add backend/mes-boot
git commit -m "feat: integrate MariaDB Flyway and MyBatis-Plus"
```

### Task 7: Redis and MinIO Client Boundaries (MES-005, MES-006)

**Files:**
- Create: `backend/mes-integration/src/main/java/com/hospital/mes/integration/storage/ObjectStorageProperties.java`
- Create: `backend/mes-integration/src/main/java/com/hospital/mes/integration/storage/ObjectStorageConfiguration.java`
- Create: `backend/mes-integration/src/test/java/com/hospital/mes/integration/storage/ObjectStorageConfigurationTest.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/config/RedisConfigurationTest.java`
- Modify: `backend/mes-integration/pom.xml`, `backend/mes-boot/pom.xml`
- Modify: `backend/mes-boot/src/main/resources/application.yml`, `application-local.yml`, `application-prod.yml`

**Interfaces:**
- Consumes: environment-backed configuration from Task 3.
- Produces: validated prefix `mes.storage`; bean `MinioClient`; Spring Boot managed `RedisConnectionFactory`.

- [ ] **Step 1: Write configuration tests**

Assert valid storage properties create a `MinioClient`; blank endpoint/access/secret values fail binding; production does not inherit local secrets; Redis host, port, username, password, SSL, and timeout bind through standard Spring properties.

- [ ] **Step 2: Run tests and verify RED**

Run: `mvn -B -ntp -pl backend/mes-integration,backend/mes-boot -am -Dtest=ObjectStorageConfigurationTest,RedisConfigurationTest test`

Expected: FAIL because configuration classes and dependencies are absent.

- [ ] **Step 3: Implement client boundaries**

Add MinIO SDK 9.0.3 to `mes-integration` and Redis starter to `mes-boot`. Do not implement bucket creation, upload APIs, caching policy, or business services.

- [ ] **Step 4: Run tests and verify GREEN**

Run: `mvn -B -ntp -pl backend/mes-integration,backend/mes-boot -am test`

Expected: PASS.

- [ ] **Step 5: Commit**

```text
git add backend/mes-integration backend/mes-boot
git commit -m "feat: configure Redis and object storage clients"
```

### Task 8: Security Extension Boundary and OpenAPI (MES-002, MES-007)

**Files:**
- Create: `backend/mes-security/src/main/java/com/hospital/mes/security/SecurityConfiguration.java`
- Create: `backend/mes-security/src/main/java/com/hospital/mes/security/jwt/JwtAuthenticationProvider.java`
- Create: `backend/mes-security/src/test/java/com/hospital/mes/security/SecurityConfigurationTest.java`
- Create: `backend/mes-boot/src/main/java/com/hospital/mes/config/OpenApiConfiguration.java`
- Create: `backend/mes-boot/src/main/java/com/hospital/mes/foundation/api/FoundationController.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/foundation/api/FoundationControllerTest.java`
- Modify: `backend/mes-security/pom.xml`, `backend/mes-boot/pom.xml`

**Interfaces:**
- Consumes: API envelope and trace context from Tasks 4-5.
- Produces: `GET /api/v1/foundation/status`; OpenAPI `/v3/api-docs`; injectable `JwtAuthenticationProvider` interface without token issuance implementation.

- [ ] **Step 1: Write MVC and security tests**

Assert the Foundation status endpoint returns `code='OK'`, a trace ID, and no business data; `/v3/api-docs` includes the endpoint and MES metadata; actuator health and OpenAPI are accessible in local/test profiles; unspecified API routes remain protected; no login/token endpoint exists.

- [ ] **Step 2: Run tests and verify RED**

Run: `mvn -B -ntp -pl backend/mes-security,backend/mes-boot -am -Dtest=SecurityConfigurationTest,FoundationControllerTest test`

Expected: FAIL because endpoint, OpenAPI, and security boundary are absent.

- [ ] **Step 3: Implement minimal security and OpenAPI**

Configure stateless security, explicit public Foundation documentation/health routes, deny-by-default API behavior, and no generated development user. Keep JWT implementation as a later-epic interface boundary.

- [ ] **Step 4: Run tests and verify GREEN**

Run: `mvn -B -ntp -pl backend/mes-security,backend/mes-boot -am test`

Expected: PASS.

- [ ] **Step 5: Commit**

```text
git add backend/mes-security backend/mes-boot
git commit -m "feat: expose Foundation OpenAPI boundary"
```

### Task 9: Vue Application Shell (Frontend Foundation)

**Files:**
- Create: `frontend/mes-web/package.json`, `package-lock.json`, `vite.config.ts`, `tsconfig*.json`, `index.html`
- Create: `frontend/mes-web/src/{main.ts,App.vue}`
- Create: `frontend/mes-web/src/router/index.ts`, `src/stores/index.ts`, `src/layouts/AppLayout.vue`
- Create: `frontend/mes-web/src/views/dashboard/DashboardView.vue`
- Create: placeholder `.gitkeep` files under approved API/component/hook/permission/view/designer directories
- Create: `frontend/mes-web/src/App.test.ts`

**Interfaces:**
- Consumes: browser runtime only; no backend authentication behavior.
- Produces: npm scripts `dev`, `build`, `typecheck`, `test`; route `/`; registered Pinia; Ant Design Vue shell; ECharts dependency available for later features.

- [ ] **Step 1: Write application-shell test**

Create only `package.json`, TypeScript/Vite/Vitest configuration, and `App.test.ts`, then assert the wished-for app renders the hospital MES title, sidebar placeholder, top bar, breadcrumb, dashboard route, and no login or production-action UI.

- [ ] **Step 2: Install and run test to verify RED**

Run: `npm ci && npm test -- --run`

Working directory: `frontend/mes-web`

Expected: FAIL because the Vue application does not exist.

- [ ] **Step 3: Implement minimal typed shell**

Use Vue 3 Composition API, Vue Router, Pinia, Ant Design Vue 4.x, Axios, and ECharts. Keep visuals intentionally minimal and defer dynamic menus and permissions.

- [ ] **Step 4: Verify frontend**

Run: `npm test -- --run && npm run typecheck && npm run build`

Expected: all commands PASS without TypeScript errors.

- [ ] **Step 5: Commit**

```text
git add frontend/mes-web
git commit -m "feat: add Vue application shell"
```

### Task 10: Docker Compose Infrastructure (MES-011)

**Files:**
- Create: `docker-compose.yml`, `.env.example`
- Create: `deploy/docker/README.md`
- Create: `scripts/verify-compose.ps1`
- Create: `scripts/tests/verify-compose.Tests.ps1`

**Interfaces:**
- Consumes: application connection settings from Tasks 3, 6, and 7.
- Produces: services `mariadb`, `redis`, `minio`; networks and named volumes; health checks; deterministic environment-variable contract.

- [ ] **Step 1: Write Compose contract tests**

Pester assertions require pinned images, no real credentials, MariaDB/Redis/MinIO health checks, named volumes, local port overrides, and application-compatible variable names. Assert floating `latest` tags and the archived legacy MinIO image are rejected.

- [ ] **Step 2: Run test and verify RED**

Run: `Invoke-Pester scripts/tests/verify-compose.Tests.ps1 -CI`

Expected: FAIL because Compose files and validation script are absent.

- [ ] **Step 3: Implement Compose topology**

Pin MariaDB and Redis maintained releases. Pin the maintained 2026 MinIO/AIStor-compatible server release required by the approved MinIO topology. Document licensing/production-support implications and local-only sample credentials.

- [ ] **Step 4: Validate and, when available, run services**

Run: `Invoke-Pester scripts/tests/verify-compose.Tests.ps1 -CI`

Then, if Docker is available: `docker compose config` followed by `docker compose up -d --wait` and `docker compose down`.

Expected: static tests PASS; Docker config and health checks PASS when Docker is installed. Otherwise report runtime verification as unavailable.

- [ ] **Step 5: Commit**

```text
git add docker-compose.yml .env.example deploy/docker scripts
git commit -m "build: add local infrastructure topology"
```

### Task 11: Developer Documentation and Unified Verification

**Files:**
- Modify: `README.md`
- Create: `docs/architecture/module-boundaries.md`
- Create: `docs/development/local-development.md`
- Create: `docs/api/foundation-api.md`
- Create: `scripts/verify.ps1`
- Create: `scripts/tests/verify-script.Tests.ps1`

**Interfaces:**
- Consumes: all build/test commands from Tasks 1-10.
- Produces: one local verification entry point and complete setup/run documentation.

- [ ] **Step 1: Write verification-script contract test**

Assert `scripts/verify.ps1` stops on the first failing command, runs Maven tests, frontend clean install/test/typecheck/build, and Compose static validation, and clearly labels Docker-dependent checks as verified or unavailable.

- [ ] **Step 2: Run test and verify RED**

Run: `Invoke-Pester scripts/tests/verify-script.Tests.ps1 -CI`

Expected: FAIL because the script is absent.

- [ ] **Step 3: Implement documentation and verification script**

Document prerequisites, environment variables, build, test, local startup, OpenAPI URL, health URL, module ownership, deferred scope, and Docker-unavailable behavior.

- [ ] **Step 4: Run full local verification**

Run: `pwsh -File scripts/verify.ps1`

Expected: backend and frontend verification PASS; Compose static verification PASS; Docker runtime checks either PASS or are explicitly reported unavailable.

- [ ] **Step 5: Commit**

```text
git add README.md docs scripts
git commit -m "docs: document Foundation development workflow"
```

### Task 12: Publish the Public GitHub Repository

**Files:**
- Modify only if required by final verification findings.

**Interfaces:**
- Consumes: clean, verified local `main` branch.
- Produces: public GitHub repository `hospital-pharma-mes`, Apache-2.0 license visible, remote `origin`, pushed `main` branch.

- [ ] **Step 1: Perform release preflight**

Run: `git status --short --branch`, `git log --oneline --decorate -12`, and secret scanning over tracked files. Confirm no pending changes, no credentials, and every prior task commit is present.

- [ ] **Step 2: Run fresh verification before publication**

Run: `pwsh -File scripts/verify.ps1`

Expected: all locally available checks PASS with Docker-dependent status stated explicitly.

- [ ] **Step 3: Create the public repository without generated files**

Use the authenticated GitHub web session because GitHub CLI is unavailable. Create `hospital-pharma-mes` as public with no auto-generated README, license, or `.gitignore`, so local history remains authoritative.

- [ ] **Step 4: Add remote and push**

Add the exact repository URL returned by GitHub as `origin`, push `main`, and set upstream. Do not force-push or rewrite history.

- [ ] **Step 5: Verify remote state**

Confirm repository visibility is public, default branch is `main`, Apache-2.0 is detected, the latest local commit matches remote, and the README renders.

- [ ] **Step 6: Report**

Provide the public repository URL, local path, test/build evidence, exact Docker verification status, and deferred work list.
