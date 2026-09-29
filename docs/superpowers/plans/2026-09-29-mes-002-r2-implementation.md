# MES-002-R2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete MES-002-R2 authentication/RBAC, frozen IAM administration APIs and UI, menu/permission consistency, MES-001 auth/navigation handoff, and TC-IAM-001 through TC-IAM-004 without starting MES-003.

**Architecture:** Preserve the accepted JWT plus Redis session implementation and the existing physical V002 compatibility slice. Append physical V005 for the missing frozen menu model and official `iam:*` permission registry, expose the frozen `/api/v1/users`, `/roles`, `/permissions`, and `/menus` contracts through application services, and retain early `/api/v1/admin/**` routes only as compatibility endpoints. New frozen mutations use the MES-001 idempotency and AuditEvent infrastructure in the same database transaction. Vue owns login/session renewal, permission-filtered navigation, route/button guards, and separate query/create/view/edit pages.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring Security, MyBatis-Plus, MariaDB/Flyway, Redis, Vue 3, TypeScript, Pinia, Ant Design Vue, Vitest, Playwright.

**Spec:** `releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/tasks/MES-002-R2.md` and its mandatory references in FINAL BASELINE COMPLETE v1.0.1.

## Global Constraints

- V001 through V004 are immutable; physical Flyway continues with V005.
- Implement only IAM-001 and IAM-002. Do not add MES-003 business data or behavior.
- Keep Java 21, Spring Boot 3.x, MariaDB SQL, MyBatis-Plus CRUD, and existing module direction without dependency cycles.
- Do not expose a generic status update API or physically delete users, roles, permissions, menus, regulated evidence, or security events.
- Existing V002 column names are a grandfathered physical compatibility model; map them to the frozen logical names at API/domain boundaries.
- API JSON renders IDs as strings. Frozen paths and permission codes are authoritative.
- All frozen mutating IAM endpoints require `Idempotency-Key`; versioned updates/assignments require quoted decimal `If-Match`.
- IAM administration changes append `sys_security_event` and GxP AuditEvent in the same MariaDB transaction; credentials, tokens, temporary passwords, and request bodies never enter either event.
- Frontend list/create/view/edit routes remain exactly those in UI V1.1; permissions/menus are maintained within role detail/edit, not through invented routes.
- Login and navigation complete the MES-001 handoff for `/audit` and `/integration/operations`.

## Review Focus

1. Disabled or locked accounts must never receive a token, while failure responses remain non-enumerating and secret-free.
2. Menu visibility, direct-route authorization, buttons, and backend authorization must use the same official `iam:*` codes; hiding UI must never substitute for API checks.
3. Stale `If-Match`, reused idempotency keys with different requests, and concurrent assignment updates must fail without partial assignment or audit evidence.
4. Every IAM administrative mutation must roll back together with both security and GxP audit evidence on database failure.
5. The frontend must keep access tokens memory-only, coalesce refresh, stop after one retry, and route successfully from login to accepted MES-001 pages when authorized.

---

### Task 1: V005 IAM compatibility migration

**Files:**
- Create: `backend/mes-boot/src/main/resources/db/migration/V005__mes_002_rbac_contract.sql`
- Modify: `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/IamSchemaIT.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/Mes002MigrationContractTest.java`

**Interfaces:**
- Consumes: immutable physical V001–V004 and logical migration group LG-002.
- Produces: `sys_menu`, `sys_role_menu`, optimistic version support for the grandfathered permission registry, and official `iam:*` permissions assigned to `SYSTEM_ADMIN`.

- [ ] Write schema/migration tests that prove V001/V002 checksums are unchanged, V005 is next, required menu fields/indexes exist, and official permissions are idempotently seeded.
- [ ] Run the tests and observe failure because V005/tables/codes do not exist.
- [ ] Implement MariaDB-compatible V005 without changing or renaming historical migrations.
- [ ] Run the migration tests green and commit.

### Task 2: Frozen IAM application contracts and GxP audit

**Files:**
- Modify: `backend/mes-system/pom.xml`
- Create/modify focused classes under `backend/mes-system/src/main/java/com/hospital/mes/system/{application,infrastructure}/`
- Add focused unit tests under `backend/mes-system/src/test/java/com/hospital/mes/system/application/`

**Interfaces:**
- Consumes: `AuditApplicationService`, `PlatformIdempotencyService`, `CurrentPlatformContext`, existing user/role/security services and V002 entities.
- Produces: filtered user/role queries, permission/menu CRUD, role-menu synchronization, quoted-version parsing, idempotent mutation executor, and canonical IAM AuditEvent writer.

- [ ] Write failing unit tests for status mapping, filters, quoted versions, canonical digest exclusion of secrets, idempotency replay/conflict, and menu-permission invariants.
- [ ] Run tests red for missing application components.
- [ ] Implement minimal domain/application/repository changes with transaction boundaries and optimistic locks.
- [ ] Run the focused tests green and commit.

### Task 3: Frozen backend APIs and authorization

**Files:**
- Create: focused controllers/DTOs under `backend/mes-system/src/main/java/com/hospital/mes/system/api/`
- Modify: `backend/mes-security/src/main/java/com/hospital/mes/security/SecurityConfiguration.java`
- Modify: `backend/mes-security/src/main/java/com/hospital/mes/security/api/AuthController.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/Mes002ApiIT.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/Mes002OpenApiIT.java`

**Interfaces:**
- Consumes: Task 2 application commands and login snapshot permissions.
- Produces: frozen operations on `/api/v1/users`, `/roles`, `/permissions`, `/menus`, string-ID current-user DTO, exact operation IDs, and official permission enforcement.

- [ ] Write failing MockMvc tests for all frozen methods, DTOs, idempotency, `If-Match`, errors, string IDs, operation IDs, and 401/403 behavior.
- [ ] Run tests red because frozen routes are absent.
- [ ] Implement DTO-only API adapters and exact Spring Security matchers; retain early admin routes as compatibility only.
- [ ] Run API/OpenAPI tests green and commit.

### Task 4: Formal IAM acceptance and integration behavior

**Files:**
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/Mes002AcceptanceIT.java`
- Modify only affected existing IAM integration tests where the official permission contract adds assertions.

**Interfaces:**
- Consumes: Tasks 1–3 plus MES-001 AuditEvent and route permissions.
- Produces: automated evidence for TC-IAM-001, TC-IAM-002, TC-IAM-003, and backend portion of TC-IAM-004.

- [ ] Write/confirm failing acceptance tests for active login, disabled login, unauthorized `/materials`, menu/API permission consistency, rollback, concurrency, and audit evidence.
- [ ] Run red for the missing final integration behavior.
- [ ] Apply the minimum fixes in application/security persistence.
- [ ] Run formal acceptance plus affected IAM and MES-001 authorization regression green and commit.

### Task 5: Vue authentication and authorization handoff

**Files:**
- Create/modify: `frontend/mes-web/src/api/`, `stores/auth.ts`, `auth/`, `router/index.ts`, `layouts/AppLayout.vue`, auth views and tests.
- Modify: `frontend/mes-web/package.json`, lockfile, and Vite config only as required by tests.

**Interfaces:**
- Consumes: `/api/v1/auth/login|refresh|logout|me|change-password` and official permission codes.
- Produces: memory-only token lifecycle, coalesced one-retry refresh, login/password-change flow, protected route guards, and authorized navigation including MES-001 routes.

- [ ] Write failing Vitest tests for login, refresh coalescing, session loss, direct-route denial, buttons, and `/audit` plus `/integration/operations` navigation.
- [ ] Run tests red because real authentication handoff is absent.
- [ ] Implement the auth client/store/guards/navigation and remove the MES-001 injected auth double from production flow.
- [ ] Run focused frontend tests and typecheck green and commit.

### Task 6: Frozen IAM UI pages

**Files:**
- Create/modify: `frontend/mes-web/src/views/admin/` user and role query/create/view/edit views, IAM API/types, styles, and component tests.

**Interfaces:**
- Consumes: frozen Task 3 APIs and Task 5 authorization store.
- Produces: UI V1.1 routes `/admin/users`, `/admin/users/create`, `/admin/users/:id`, `/admin/users/:id/edit`, and corresponding role routes; permission/menu controls live in role view/edit.

- [ ] Write failing component tests for prototype-aligned user query layout, URL query persistence, separate CRUD routes, status/role rendering, permission-gated controls, and conflict handling.
- [ ] Run tests red because the pages are absent.
- [ ] Implement responsive Ant Design Vue pages matching UI-IAM-USR-Q and the frozen page separation rules.
- [ ] Run component tests, typecheck, and production build green and commit.

### Task 7: Targeted E2E, regression, review, and closure

**Files:**
- Create/modify: `frontend/mes-web/e2e/mes-002-r2.spec.ts`, Playwright config, and acceptance record after verification.

**Interfaces:**
- Consumes: all earlier tasks.
- Produces: TC-IAM-004 frontend E2E evidence, affected regression evidence, review findings, and MES-002 acceptance checkpoint.

- [ ] Write the Playwright journey first and observe failure before the frontend is wired.
- [ ] Run login → user page, permission denial, button visibility, and login → `/audit`/`/integration/operations` journeys green.
- [ ] Run targeted backend unit/integration tests, affected MES-001 authorization regression, frontend tests/typecheck/build/E2E, migration checksum checks, and task-boundary scans.
- [ ] Perform whole-branch code review. Fix all CRITICAL/HIGH findings with failing tests first; record MEDIUM/LOW as technical debt.
- [ ] Rerun the affected verification set, commit closure evidence, stop MES-002 temporary containers, and stop at `MES-002-R2 READY FOR ACCEPTANCE`.
