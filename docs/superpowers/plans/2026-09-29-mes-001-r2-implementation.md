# MES-001-R2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Plan status:** `STOPPED — MISSING SPECIFICATION / DESIGN CONFLICT`. This document is an implementation-ready gap/impact map, but coding must not begin until the Design Gate in Section 8 is resolved in writing and this plan is revised/approved.

**Goal:** Deliver only MES-001-R2: the platform audit/eSignature/inbox-outbox foundation, its frozen APIs/UI surface, and TC-AUD-001, TC-SIG-001, TC-INT-001, and TC-INT-002 without pre-implementing MES-002–013 business capabilities.

**Architecture:** Keep the modular monolith. Add a dedicated `mes-audit` business module with `api`, `application`, `domain`, and `infrastructure` boundaries. Extend the existing `mes-integration` module with the same boundaries. Downstream modules consume narrow application contracts; they do not write GxP evidence tables directly. Audit and outbox writes join the caller's local database transaction. External systems never join that transaction.

**Tech Stack:** Java 21, Spring Boot 3.5.x, Spring Security, MyBatis-Plus, Flyway, MariaDB, Vue 3, TypeScript, Ant Design Vue, Vitest, Maven Surefire/Failsafe.

**Authoritative source:** `HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v2.zip`, release 2026-09-29, FINAL/FROZEN. Manifest verification on 2026-09-29: 74 entries checked by byte length and SHA-256; 0 mismatches.

---

## 1. Design Index

Authority order follows the frozen `14_AGENTS.md`: PRD → detailed domain → database → eBR → state machine → API/OpenAPI → functional design → UI V1.1 → GMP → tests → RTM → current task card.

| Area | Authoritative artifact | MES-001-R2 use |
|---|---|---|
| Release/manifest | `00_FINAL_RELEASE_V1.0.md`, `00_MANIFEST_FINAL_FROZEN.md`, integrity JSON files | Package status, counts, hashes, execution gate |
| Development baseline | `00_医院制剂_MES_V2.0_FINAL_BASELINE_COMPLETE_v1.0_最终正式版.docx`, `00_医院制剂_MES_V2.0_FINAL_DEVELOPMENT_BASELINE说明.docx` | Sole-baseline declaration and development protocol |
| Requirements | `01_PRD_V1.0_FROZEN.md` | AUD-001, SIG-001, INT-001, NFR-001/002/003 |
| Architecture | `02_ARCHITECTURE_V1.0_FROZEN.md` | Modular monolith and module boundaries |
| Domain | `03_DOMAIN_OVERVIEW_V1.0_FROZEN.md`, `04_DOMAIN_MODEL_DETAILED_V1.0_FROZEN.md` | Audit source of truth, append-only evidence, same-transaction rules, signature invalidation, outbox |
| Database | `05_DATABASE_DESIGN_V1.0_FROZEN.md` | Four MES-001 tables, common columns, frozen signature additions |
| eBR/signature | `06_EBR_DYNAMIC_FORM_DETAILED_V1.0_FROZEN.md` | Digest formula, reauthentication, invalidation and re-sign rules |
| State machines | `07_STATE_MACHINE_DETAILED_V1.0_FROZEN.md` | VALID/INVALIDATED behavior and no generic status mutation |
| API | `08_API_DETAILED_V1.0_FROZEN.md` | `/api/v1`, JWT, idempotency/version headers, error model, sign request |
| Full OpenAPI | `08_OPENAPI_FULL_V1.0_FROZEN.yaml`, endpoint catalog and interface guide | `queryAuditEvents`, `queryIntegrationMessages`, `retryIntegrationMessage`, `signRecord` assignment |
| Functional design | `09_FUNCTIONAL_DETAILED_V1.0_FROZEN.md` | FD-AUD-001 services, permissions, transactions, acceptance |
| UI V1.1 | `10_UI_PAGE_DETAILED_V1.1_FROZEN.md`, route matrix | `UI-AUD-Q`, route `/audit`, permission `audit:view` |
| Frozen UI prototype | `ui-prototype/DESIGN_SYSTEM.md`, `UI_MAPPING.csv`, `prototype/*`, `screens/UI-PLATFORM-OPS.png`, QA evidence | Visual reference and conflicting `/platform/operations` mapping |
| GMP | `11_GMP_AUDIT_ESIGNATURE_V1.0_FROZEN.md` | No hard delete/overwrite; signature evidence fields |
| Verification | `12_TEST_CASE_*`, `12_TEST_COVERAGE_MATRIX.csv`, `12A_TEST_ACCEPTANCE_V1.0_FROZEN.md` | Formal tests TC-AUD-001, TC-SIG-001, TC-INT-001, TC-INT-002 |
| Traceability | `13_REQUIREMENT_TRACEABILITY_MATRIX_V1.0_FROZEN.csv` | Requirement → DB/API/UI/task/test mapping |
| Engineering rules | `14_AGENTS.md` plus repository `AGENTS.md` | STOP rules, Java/MariaDB/MyBatis/Flyway/GMP constraints |
| Dependency/migration/contracts | Section 15 DOCX, MD, and three matrices | Logical V001, no upstream dependency, produced platform contracts |
| Construction increments | `tasks/MES-001-R2.md` through `tasks/MES-013-R2.md` | Full downstream awareness; only MES-001 is in write scope |

The complete baseline was read, including all 13 R2 task cards and all 13 prototype screenshots. No attached-document instruction was treated as a user command outside the authority model stated above.

---

## 2. Repository Audit Baseline

### 2.1 Current state

- Branch: `feat/epic-002-iam`, 15 commits ahead of its remote tracking branch at audit time.
- Existing user-owned untracked files were not touched: the baseline ZIP and the earlier baseline DOCX at repository root.
- Maven reactor: 20 backend modules plus two aggregator projects; 80 main Java files and 28 Java test files.
- Persistence currently covers only `sys_foundation_probe` and the IAM/security tables.
- Flyway history in source contains `V001__foundation_probe.sql` and `V002__iam_core.sql`; MES-001's frozen four tables do not exist.
- `mes-integration` contains only MinIO object-storage configuration; it has no inbox/outbox domain, persistence, API, or retry worker.
- No `mes-audit` module exists.
- Frontend contains one `/` dashboard route and nine source files. It does not contain audit/integration APIs, permission-aware routes, or the frozen platform-operations visual structure.
- Current frontend shell is 72 px header / 268 px white sidebar; the prototype reference uses a 58 px navy top bar / 216 px navy sidebar and different information architecture.
- Current OpenAPI is generated from implemented controllers; it is not the frozen full interface contract and exposes none of the MES-001 operations.

### 2.2 Verification evidence before implementation

Run on 2026-09-29 against the audited working tree:

- `mvn -B -ntp test` — PASS, all 22 reactor projects successful.
- `npm test -- --run` — PASS, 1 test.
- `npm run typecheck` — PASS.
- `npm run build` — PASS; warning only: main JS chunk 1,537.33 kB before gzip.
- `pwsh -NoProfile -File scripts/verify-repository.ps1` — PASS.
- `pwsh -NoProfile -File scripts/verify-ci.ps1` — PASS.
- `pwsh -NoProfile -File scripts/tests/verify-ci.Tests.ps1` — PASS.
- MariaDB/Redis Failsafe integration profile was not run during this read-only planning audit; it remains mandatory after implementation.

### 2.3 Material current-vs-frozen findings

1. `sys_security_event` is an IAM operational security log. Its fields and semantics do not satisfy `gxp_audit_event`; it must remain a separate record type.
2. `ApiResponse(code,message,data,traceId)` is acceptable only as the existing success envelope. Error responses do not satisfy the frozen `requestId,code,message,fieldErrors[],allowedActions[]` contract.
3. `V001__foundation_probe.sql` occupies the Flyway version that Section 15 logically assigns to MES-001. Repository rules prohibit rewriting an applied/shared migration.
4. `V002__iam_core.sql` implements MES-002 before the MES-001 platform contract and differs from the frozen IAM field dictionary. MES-001 must not rewrite it.
5. `ModuleBoundaryTest` hard-codes exactly 20 modules and therefore blocks the frozen architecture's missing audit module.
6. Spring Security ends in `anyRequest().denyAll()` and contains no MES-001 route permissions.
7. Existing IAM administrative events do not provide old/new digests, reason, organization, or transaction ID and cannot be silently reclassified as GxP audit records.

---

## 3. MES-001-R2 Dependency Check

### 3.1 Frozen dependency result

- Hard dependencies: none.
- Soft dependencies: none.
- Contracts consumed: none.
- Logical migration dependency: Section 15 `V001 platform_base`, depends on none.
- Dependency status: **PASS at the task graph level**.

### 3.2 Physical repository dependency result

- The repository has already applied/created logical successors before the MES-001 platform migration.
- A new audit module can be added without a Maven cycle if its dependency direction is `mes-audit → mes-common`; downstream modules depend on `mes-audit`, never the inverse.
- The existing `sys_user` table can physically satisfy `actor_id` and `signer_id` foreign keys, but that is only because current V002 precedes the missing MES-001 migration. This is opposite to frozen Section 15 ordering.
- Permission enforcement can reuse existing JWT authorities, but current IAM storage names differ from the frozen database dictionary.

**Dependency conclusion:** implementation is not blocked by an absent upstream feature, but is blocked by migration-contract and API/UI specification conflicts listed in Section 8.

---

## 4. Gap Analysis and File Classification

### 4.1 KEEP

Keep unchanged unless a later approved plan explicitly says otherwise:

- `pom.xml` — Java 21, Spring Boot 3.x and dependency-enforcement baseline.
- `AGENTS.md`, `.editorconfig`, `.env.example`, `.gitignore`, `docker-compose.yml`, `deploy/docker/**`.
- `.github/workflows/ci.yml`, `scripts/verify*.ps1`, `scripts/tests/**` — current CI contract is passing and must not be weakened.
- `backend/mes-boot/src/main/resources/db/migration/V001__foundation_probe.sql` — immutable history despite version collision.
- `backend/mes-boot/src/main/resources/db/migration/V002__iam_core.sql` — immutable history; MES-002 reconciliation is outside this task.
- `backend/mes-boot/src/main/java/com/hospital/mes/foundation/**` and its tests — unrelated probe; retain for regression.
- `backend/mes-system/src/main/java/com/hospital/mes/system/infrastructure/SysSecurityEventEntity.java`.
- `backend/mes-system/src/main/java/com/hospital/mes/system/infrastructure/SysSecurityEventMapper.java`.
- `backend/mes-system/src/main/java/com/hospital/mes/system/application/AdminSecurityEventWriter.java` — keep as distinct IAM security evidence in MES-001; do not alias it to GxP audit.
- Other `mes-security` and `mes-system` domain/application/persistence code, except exact API/error/security files listed under MODIFY.
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/storage/**` and its test.
- Empty future business modules MES-003–011 — no pre-implementation.
- `frontend/mes-web/package.json`, `package-lock.json`, `src/main.ts`, `src/stores/index.ts`, `vite.config.ts`, TypeScript configs.

### 4.2 MODIFY

These are the only existing files proposed for MES-001 writes after the Design Gate:

- `backend/pom.xml` — register `mes-audit`.
- `backend/mes-boot/pom.xml` — add runtime dependency on `mes-audit`; keep `mes-integration`.
- `backend/mes-integration/pom.xml` — add only the dependencies required for common API types, Spring transactions, MyBatis-Plus, and tests.
- `backend/mes-security/pom.xml` — only if the approved signature reauthentication adapter is hosted here; dependency direction must remain acyclic.
- `backend/mes-boot/src/test/java/com/hospital/mes/architecture/ModuleBoundaryTest.java` — expect 21 modules and assert the new allowed dependency direction.
- `backend/mes-security/src/main/java/com/hospital/mes/security/SecurityConfiguration.java` — add exact frozen authorities and routes; retain default deny.
- `backend/mes-security/src/main/java/com/hospital/mes/security/web/SecurityErrorWriter.java` — return frozen `ApiError`.
- `backend/mes-security/src/main/java/com/hospital/mes/security/api/AuthExceptionAdvice.java` — return frozen `ApiError`; do not change auth behavior.
- `backend/mes-system/src/main/java/com/hospital/mes/system/api/AdminExceptionAdvice.java` — return frozen `ApiError`; do not change IAM behavior.
- `backend/mes-common/src/main/java/com/hospital/mes/common/web/GlobalExceptionHandler.java` — replace error envelope/mappings only.
- `backend/mes-common/src/test/java/com/hospital/mes/common/web/GlobalExceptionHandlerTest.java` — frozen error fields and 400/401/403/404/409/422/500 regression.
- `README.md`, `docs/api/README.md`, `docs/database/README.md`, `docs/compliance/README.md`, `docs/architecture/module-boundaries.md` — replace obsolete claims only after code exists; label earlier foundation plans historical, not authoritative.
- `frontend/mes-web/src/router/index.ts` — add only the approved frozen route and permission metadata.
- `frontend/mes-web/src/layouts/AppLayout.vue` — conform the common shell used by the MES-001 page to the frozen prototype.
- `frontend/mes-web/src/styles.css` — frozen colors, dimensions, density, focus and responsive rules.
- `frontend/mes-web/src/views/dashboard/DashboardView.vue` — retain route purpose but remove the non-frozen "基础服务已就绪" placeholder or redirect to the approved page; final action depends on UI route decision.
- `frontend/mes-web/src/App.test.ts` — replace placeholder-only assertions with shell, route, permission and accessibility assertions.

### 4.3 REWRITE

- `backend/mes-common/src/main/java/com/hospital/mes/common/web/GlobalExceptionHandler.java` is a behavioral rewrite within the existing file: it must produce `ApiError` and preserve the frozen status taxonomy.
- `frontend/mes-web/src/layouts/AppLayout.vue` and `frontend/mes-web/src/styles.css` require a visual/layout rewrite because the current shell materially conflicts with the frozen UI prototype.
- `frontend/mes-web/src/views/dashboard/DashboardView.vue` requires a content rewrite if the approved route keeps `/` as an entry point.

No migration, IAM table, or regulated-history implementation may be rewritten.

### 4.4 DELETE

- **None.** No current code must be deleted to implement MES-001 safely.
- In particular, do not delete or rename V001/V002, `sys_security_event`, user-owned untracked baseline files, or historical plans. Obsolete documentation must be marked superseded rather than erased without a separate reviewed cleanup.

### 4.5 MISSING

The following artifacts are absent and are proposed, subject to Section 8:

**Migration**

- `backend/mes-boot/src/main/resources/db/migration/V003__mes_001_platform_base.sql` — proposed physical repair version; final name requires migration decision.

**Common API error contract**

- `backend/mes-common/src/main/java/com/hospital/mes/common/api/ApiError.java`
- `backend/mes-common/src/main/java/com/hospital/mes/common/api/FieldError.java`
- `backend/mes-common/src/test/java/com/hospital/mes/common/api/ApiErrorTest.java`

**Audit/eSignature module**

- `backend/mes-audit/pom.xml`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/api/AuditEventController.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/api/SignatureController.java` — only if MES-001 ownership is approved.
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/api/dto/AuditEventQuery.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/api/dto/AuditEventResponse.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/api/dto/SignRecordRequest.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/api/dto/SignatureResponse.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/application/AuditService.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/application/SignatureService.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/application/port/RecordDigestProvider.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/application/port/ReauthenticationPort.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/domain/AuditEvent.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/domain/AuditCommand.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/domain/RecordSignature.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/domain/AuditEventRepository.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/domain/SignatureRepository.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/infrastructure/persistence/GxpAuditEventEntity.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/infrastructure/persistence/GxpAuditEventMapper.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/infrastructure/persistence/MybatisAuditEventRepository.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/infrastructure/persistence/GxpSignatureEntity.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/infrastructure/persistence/GxpSignatureMapper.java`
- `backend/mes-audit/src/main/java/com/hospital/mes/audit/infrastructure/persistence/MybatisSignatureRepository.java`

**Integration module**

- `backend/mes-integration/src/main/java/com/hospital/mes/integration/api/IntegrationMessageController.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/api/dto/IntegrationMessageQuery.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/api/dto/IntegrationMessageResponse.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/api/dto/RetryIntegrationMessageRequest.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/application/InboxService.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/application/OutboxService.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/application/IntegrationMessageQueryService.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/application/OutboxDispatchService.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/application/port/ExternalMessagePublisher.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/domain/InboxMessage.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/domain/OutboxMessage.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/domain/InboxRepository.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/domain/OutboxRepository.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/infrastructure/persistence/IntegrationInboxEntity.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/infrastructure/persistence/IntegrationInboxMapper.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/infrastructure/persistence/MybatisInboxRepository.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/infrastructure/persistence/IntegrationOutboxEntity.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/infrastructure/persistence/IntegrationOutboxMapper.java`
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/infrastructure/persistence/MybatisOutboxRepository.java`

**Frontend**

- `frontend/mes-web/src/api/http.ts`
- `frontend/mes-web/src/api/audit.ts`
- `frontend/mes-web/src/api/integration.ts`
- `frontend/mes-web/src/types/audit.ts`
- `frontend/mes-web/src/types/integration.ts`
- `frontend/mes-web/src/views/audit/AuditQueryView.vue` — higher-authority UI V1.1 candidate.
- `frontend/mes-web/src/views/platform/PlatformOperationsView.vue` — prototype candidate; mutually exclusive until route conflict is resolved.
- `frontend/mes-web/src/components/audit/AuditEventTable.vue`
- `frontend/mes-web/src/components/integration/IntegrationMessageTable.vue`
- `frontend/mes-web/src/components/integration/RetryMessageAction.vue`
- corresponding `*.test.ts` files.

---

## 5. Impact Analysis

### 5.1 Database and migration

- Append four regulated/platform tables; no existing table or row may be dropped.
- Physical migration must be append-only. Proposed `V003` is a repository-history repair while preserving Section 15's logical group name `V001/platform_base` in the migration comment and documentation.
- `actor_id` and `signer_id` target existing `sys_user(id)`; `revoked_signature_id` is a self-reference.
- Add four permission codes only after deciding how the frozen permission dictionary maps onto the current V002 physical schema.
- Flyway migration, clean install, upgrade from current V002, and schema-index assertions are mandatory.

### 5.2 Backend/module graph

- Backend module count changes from 20 to 21.
- `mes-audit` depends only on `mes-common`, Spring transaction/web contracts, MyBatis-Plus, and test libraries.
- `mes-integration` depends on `mes-common` and persistence/transaction contracts, not on business modules.
- `mes-boot` composes both modules.
- If reauthentication is implemented by `mes-security`, dependency is `mes-security → mes-audit` for the port implementation; `mes-audit` must not depend on `mes-security`.
- No dependency from `mes-audit` or `mes-integration` to MES-002–013 modules.

### 5.3 API/security

- Add `GET /api/v1/audit-events` with `audit:view`.
- Add `GET /api/v1/integration/messages` with `integration:view`.
- Add `POST /api/v1/integration/messages/{id}/retry` with `integration:retry`, `Idempotency-Key`, `If-Match`, and an audit event.
- `POST /api/v1/records/{type}/{id}/sign` with `ebr:sign` is blocked on task ownership and signable-record specification.
- Global error responses change shape. All existing auth/admin/foundation controller tests are regression scope.
- Default-deny security behavior remains.

### 5.4 Audit, signature, and transaction behavior

- `AuditService.record(AuditCommand)` must require an existing transaction (`MANDATORY`) for critical business writes. It cannot start an independent transaction that could leave an orphan audit row.
- Downstream callers provide actor/action/object/old-new/reason/client/transaction data; the client must not supply server actor/time.
- `OutboxService.enqueue(...)` must join the caller transaction.
- Inbox deduplication and the local business effect must commit atomically.
- External publishing occurs outside the originating business transaction; retry-state changes use optimistic locking through `version_no`.
- Signature creation and its audit event commit together. Signature invalidation appends evidence/state transition; it never deletes or overwrites the signed fact.

### 5.5 Frontend

- Common shell changes affect every current and future route; this is visual regression scope.
- Permission-aware display must not replace backend enforcement.
- No prototype metric may be hard-coded as production data. The 171 endpoint count and example health numbers are illustrative unless a frozen API supplies them.
- Query/Create/View/Edit separation remains mandatory; MES-001 adds no CRUD modal shortcut.

### 5.6 Downstream consumers

- MES-002–013 will depend on the audit/outbox contracts. Contract interfaces and database shape become append-only after acceptance.
- Existing IAM work was built before these contracts. Retrofitting IAM operations to GxP audit belongs to the approved MES-002 integration/regression scope unless the human reviewer explicitly expands MES-001's write scope.
- Signature record providers are owned by later business modules, especially MES-007/013; MES-001 must not invent their canonical records.

---

## 6. Contracts Consumed, Produced, and Regression Scope

### 6.1 Contracts Consumed

Frozen Section 15 says **none**. Current-repository runtime compatibility uses, but does not redefine:

- authenticated principal and authority resolution from `mes-security`;
- `sys_user(id)` solely as the physical signer/actor identity reference;
- common trace/request ID support;
- the current MariaDB/Flyway transaction manager.

These compatibility points are not permission to alter MES-002's frozen domain contract.

### 6.2 Contracts Produced

1. **Migration contract:** `gxp_audit_event`, `gxp_signature`, `integration_inbox`, `integration_outbox`, with the frozen common-column policy.
2. **Audit contract:** append audit evidence in the same local transaction as the critical write; query via `/audit-events`; no direct downstream table writes.
3. **Signature contract:** server identity, meaning, time, object and digest; VALID/INVALIDATED evidence; reauthentication; no image signature.
4. **Inbox contract:** preserve source/message identity and raw payload; idempotent single business effect.
5. **Outbox contract:** business commit plus message enqueue atomically; asynchronous failure/retry without duplicate business effect.
6. **Error contract:** `requestId, code, message, fieldErrors[], allowedActions[]` and frozen HTTP status mapping.
7. **Permissions:** `audit:view`, `ebr:sign`, `integration:view`, `integration:retry`.

Downstream consumers: MES-002 through MES-013.

### 6.3 Regression Scope

- All existing Maven unit tests and architecture tests.
- Existing IAM authentication, session, user, role, assignment and security-event tests.
- Existing API response/error and security 401/403 tests.
- Flyway clean migration and upgrade from current V002 on MariaDB.
- Redis/MariaDB `ci-integration` Failsafe suite.
- Existing MinIO object-storage configuration test.
- Frontend shell, router, permission visibility, typecheck and build.
- Repository/Compose/CI contract scripts and production dependency audit.
- Explicit checks that no later MES table/API/state/UI is introduced.

---

## 7. Proposed Database Contract

The following is the maximum exact schema derivable from the frozen baseline. Cells marked `UNSPECIFIED` are coding blockers, not invitations to infer values.

### 7.1 `gxp_audit_event`

| Column | Frozen definition |
|---|---|
| `id` | `BIGINT` primary key |
| `org_id` | `BIGINT`, indexed |
| `created_by`, `created_at`, `updated_by`, `updated_at`, `version_no` | required by common-column rule; types/nullability/defaults are UNSPECIFIED |
| `actor_id` | `BIGINT NOT NULL`, FK to user identity |
| `actor_role` | `VARCHAR(100) NULL` |
| `action` | `VARCHAR(80) NOT NULL`, indexed |
| `object_type` | `VARCHAR(80) NOT NULL`, indexed |
| `object_id` | `VARCHAR(100) NOT NULL`, indexed |
| `old_value_digest` | `TEXT NULL` |
| `new_value_digest` | `TEXT NULL` |
| `reason` | `VARCHAR(1000) NULL` |
| `client_info` | `VARCHAR(500) NULL` |
| `occurred_at` | `DATETIME(3) NOT NULL`, indexed |
| `transaction_id` | `VARCHAR(100) NOT NULL`, indexed |

### 7.2 `gxp_signature`

| Column | Frozen definition |
|---|---|
| common columns | as above; unresolved physical details |
| `signer_id` | `BIGINT NOT NULL`, FK to user identity |
| `meaning` | `VARCHAR(100) NOT NULL` |
| `object_type` | `VARCHAR(80) NOT NULL`, indexed |
| `object_id` | `VARCHAR(100) NOT NULL`, indexed |
| `record_digest` | `VARCHAR(128) NOT NULL` |
| `signed_at` | `DATETIME(3) NOT NULL` |
| `revoked_signature_id` | `BIGINT NULL`, self-FK |
| `status` | `VARCHAR(20) NOT NULL`; known values VALID/INVALIDATED, full constraint/default UNSPECIFIED |
| `invalidated_at` | `DATETIME(3) NULL` |
| `invalidation_reason` | `VARCHAR(1000) NULL` |
| `auth_context_json` | `LONGTEXT NULL` |

### 7.3 `integration_outbox`

| Column | Frozen definition |
|---|---|
| common columns | required; unresolved physical details |
| `event_type` | `VARCHAR(80)`, indexed; nullability UNSPECIFIED |
| `aggregate_type` | `VARCHAR(80)`; nullability UNSPECIFIED |
| `aggregate_id` | `VARCHAR(100)`, indexed; nullability UNSPECIFIED |
| `payload_json` | `LONGTEXT`; nullability UNSPECIFIED |
| `status` | `VARCHAR(20)`, indexed; allowed values/default UNSPECIFIED |
| `retry_count` | `INT`; nullability/default UNSPECIFIED |
| `next_retry_at` | `DATETIME(3) NULL` |

### 7.4 `integration_inbox`

| Column | Frozen definition |
|---|---|
| common columns | required; unresolved physical details |
| `source_system` | `VARCHAR(80)`; nullability UNSPECIFIED |
| `message_id` | `VARCHAR(128)`, unique; nullability UNSPECIFIED |
| `payload_json` | `LONGTEXT`; nullability UNSPECIFIED |
| `status` | `VARCHAR(20)`, indexed; allowed values/default UNSPECIFIED |
| `received_at` | `DATETIME(3)`; nullability/default UNSPECIFIED |

No additional table, column, deletion flag, lock column, error column, published timestamp, or composite unique key may be added without an approved frozen design change.

---

## 8. Design Gate — Required Resolutions Before Coding

### DG-001 — DESIGN CONFLICT: Flyway order and identity foreign keys

- Section 15 assigns MES-001 to V001 and MES-002 IAM to V002.
- The repository already contains immutable V001 foundation probe and V002 IAM.
- Frozen MES-001 `actor_id`/`signer_id` are FKs to users, but in the frozen order the user table does not exist until V002.

**Recommended resolution:** approve physical `V003__mes_001_platform_base.sql` as an append-only repair while documenting it as logical migration group V001. Create FKs because current V002 is already present. Do not edit V001/V002.

### DG-002 — MISSING SPECIFICATION: common physical columns

The database baseline names common columns but does not freeze their types, nullability, defaults, precision, update behavior, index composition, or whether immutable evidence omits update columns. The same document says these details require review before coding.

**Required resolution:** issue an approved physical column dictionary for the four tables.

### DG-003 — DESIGN CONFLICT: signature API task ownership

- MES-001 task card, FD-AUD-001 and RTM assign `/records/*/sign` and TC-SIG-001 to MES-001.
- Full OpenAPI assigns `POST /records/{type}/{id}/sign` to `MES-007/013-R2`.

**Recommended resolution:** MES-001 creates table, repository, digest/verification contracts and unit/security test harness; public sign endpoint and concrete record providers remain MES-007/013. If TC-SIG-001 must pass in MES-001 with DB+API evidence, explicitly reassign the endpoint to MES-001 and define a signable object provider.

### DG-004 — MISSING SPECIFICATION: signature canonicalization and reauthentication

The formula is only `SHA-256(canonicalized signed record + version + relevant child evidence IDs)`. Canonical serialization, field ordering, number/time normalization, child ordering, object-type registry, current-record lookup, reauthentication token semantics, auth-context contents, and invalidation trigger API are absent.

**Required resolution:** freeze the canonical byte representation and reauthentication/record-provider contract. Do not invent it in code.

### DG-005 — MISSING SPECIFICATION: audit/integration DTOs and retry semantics

The full OpenAPI uses `GenericRequest`/`GenericResponse`. It does not define audit filters, integration filters, pagination shape, retry body, response fields, message direction, statuses, retry eligibility, retry limit, backoff, claim/concurrency behavior, or terminal state.

**Required resolution:** freeze request/response schemas and state/retry rules for all three MES-001 operations.

### DG-006 — DESIGN CONFLICT: permissions

- MES-001 card lists `audit:view, ebr:sign`.
- Full OpenAPI additionally requires `integration:view, integration:retry` for operations assigned to MES-001.

**Resolution by authority:** include all four permissions; OpenAPI governs operation permissions. Human approval should acknowledge the task-card omission.

### DG-007 — DESIGN CONFLICT / MISSING SPECIFICATION: UI route and data

- UI V1.1 freezes `UI-AUD-Q` at `/audit`, requirement AUD-001.
- Prototype mapping freezes `/platform/operations` against nonexistent requirement `PLAT-001` and shows metrics/service health not backed by a frozen API.
- The package states that the prototype does not alter routes, API or requirements.

**Recommended resolution:** implement `/audit` as the authoritative route and use `UI-PLATFORM-OPS` only as visual composition guidance. Do not add `/platform/operations`, PLAT-001, or fabricated metrics. Freeze how integration monitoring appears on `/audit`, or issue a reviewed design change.

### DG-008 — MISSING SPECIFICATION: NFR acceptance values

NFR-002 RPO/RTO and NFR-003 retention period are explicitly pending enterprise/SOP confirmation. Performance, deployment location, backup/restore procedure and objective security-test acceptance are also not frozen.

**Required resolution:** freeze measurable values and evidence procedures. MES-001 cannot claim NFR-001/002/003 acceptance without them.

### DG-009 — MISSING SPECIFICATION: MES-001 UI authentication dependency

The task graph says MES-001 has no dependency, but its UI/API require JWT and permissions while IAM is MES-002. The current repository happens to contain backend IAM but no frontend login/token lifecycle.

**Recommended resolution:** scope MES-001 frontend tests to mocked authenticated route/API behavior and defer login/token UI to MES-002, or formally declare the IAM runtime contract consumed. Do not pre-implement MES-002 UI in MES-001.

---

## 9. Implementation Tasks After Design Gate Approval

Each task below begins with a failing test and ends with focused verification. Do not start this section until all applicable DG items are resolved and the plan is re-approved.

### Task 1: Freeze the physical migration and module graph

**Files:**

- Create: `backend/mes-boot/src/main/resources/db/migration/V003__mes_001_platform_base.sql`
- Create: `backend/mes-audit/pom.xml`
- Modify: `backend/pom.xml`
- Modify: `backend/mes-boot/pom.xml`
- Modify: `backend/mes-integration/pom.xml`
- Modify: `backend/mes-boot/src/test/java/com/hospital/mes/architecture/ModuleBoundaryTest.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/platform/integration/Mes001MigrationIT.java`

**Steps:**

1. Add failing architecture assertions for exactly 21 modules and no dependency cycle.
2. Add failing MariaDB integration assertions for all approved columns, indexes, unique keys, FKs and Flyway version.
3. Create the smallest module POMs/dependencies.
4. Add the approved append-only migration; do not edit V001/V002.
5. Verify both clean database creation and upgrade from a schema at V002.
6. Run `mvn -B -ntp -pl backend/mes-boot -am test` and the focused Failsafe migration test.
7. Commit only this task, e.g. `feat(platform): add MES-001 schema and audit module boundary`.

### Task 2: Implement the frozen API error contract

**Files:**

- Create: `backend/mes-common/src/main/java/com/hospital/mes/common/api/ApiError.java`
- Create: `backend/mes-common/src/main/java/com/hospital/mes/common/api/FieldError.java`
- Modify/Rewrite: common/security/auth/admin error handlers listed in Section 4.2
- Create/Modify: corresponding common, security and system tests

**Steps:**

1. Write failing serialization tests for all five error fields, including empty arrays rather than missing fields.
2. Write failing status-mapping tests for 400/401/403/404/409/422/500.
3. Implement `ApiError` only in API/web layers; domain/application services must not return wrappers.
4. Preserve `ApiResponse` for existing success responses unless the resolved DTO contract says otherwise.
5. Run `mvn -B -ntp -pl backend/mes-common,backend/mes-security,backend/mes-system,backend/mes-boot -am test`.
6. Commit, e.g. `fix(api): align errors with frozen MES contract`.

### Task 3: Implement append-only audit persistence and query

**Files:**

- Create: audit domain/application/persistence/API files listed in Section 4.5, excluding signature files
- Create: `backend/mes-audit/src/test/java/com/hospital/mes/audit/application/AuditServiceTest.java`
- Create: `backend/mes-audit/src/test/java/com/hospital/mes/audit/api/AuditEventControllerTest.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/platform/integration/AuditTransactionIT.java`

**Steps:**

1. Write failing tests that reject client-supplied actor/time and require an outer transaction for critical audit writes.
2. Write failing query tests from the approved `AuditEventQuery` schema.
3. Implement `AuditEvent` domain construction and `AuditEventRepository`.
4. Implement MyBatis-Plus persistence; expose no update/delete method through the domain repository.
5. Implement `AuditService` with transaction propagation `MANDATORY` for critical-write recording and read-only query transaction for searches.
6. Implement `GET /api/v1/audit-events`; enforce `audit:view` in `SecurityConfiguration`.
7. Implement TC-AUD-001 using an approved MES-001 critical write: success commits business row + audit; forced rollback leaves neither. Do not create a fake production aggregate.
8. Run audit unit/API tests and `AuditTransactionIT` against MariaDB.
9. Commit, e.g. `feat(audit): add transactional GxP audit foundation`.

### Task 4: Implement signature foundation or endpoint according to DG-003

**Files:**

- Create: signature domain/application/persistence files listed in Section 4.5
- Conditional create: `SignatureController.java`, sign DTOs, security reauthentication adapter
- Create: `backend/mes-audit/src/test/java/com/hospital/mes/audit/application/SignatureServiceTest.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/platform/integration/SignatureTamperIT.java`

**Steps:**

1. Encode the approved canonical byte/digest contract as deterministic fixtures; write failing tests first.
2. Write failing tests for reauthentication failure, record version change, VALID → INVALIDATED, and re-sign lineage via `revoked_signature_id`.
3. Implement append-only signature persistence with optimistic locking and no generic `updateStatus` API.
4. Implement explicit commands/domain methods: sign, verify digest, invalidate, re-sign.
5. If endpoint ownership is approved for MES-001, implement `POST /api/v1/records/{type}/{id}/sign` with `Idempotency-Key`, `If-Match`, `ebr:sign`, 401/409/422, and the approved provider registry.
6. Implement TC-SIG-001 with DB+API evidence. If the endpoint remains MES-007/013, mark the MES-001 test as a foundation contract test only and obtain an explicit acceptance amendment; do not falsely claim the formal case passed.
7. Run signature unit/security/integration tests.
8. Commit, e.g. `feat(signature): add deterministic electronic-signature foundation`.

### Task 5: Implement inbox idempotency and outbox transaction contract

**Files:**

- Create: integration domain/application/persistence files listed in Section 4.5
- Create: `backend/mes-integration/src/test/java/com/hospital/mes/integration/application/InboxServiceTest.java`
- Create: `backend/mes-integration/src/test/java/com/hospital/mes/integration/application/OutboxServiceTest.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/platform/integration/InboxIdempotencyIT.java`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/platform/integration/OutboxRetryIT.java`

**Steps:**

1. Write failing tests from the approved inbox/outbox state and retry specification.
2. Implement inbox insert/deduplicate so the approved identity key and local business effect share one transaction.
3. Implement outbox enqueue with transaction propagation `MANDATORY`.
4. Implement dispatch/retry without holding the originating business transaction open across the external call.
5. Use `version_no` optimistic locking for mutable outbox state; expose explicit retry methods, never generic status update.
6. Implement TC-INT-001: duplicate source/message input produces one business effect and one durable inbox identity.
7. Implement TC-INT-002: business + outbox commit; publisher failure schedules/records retry; eventual retry does not duplicate business effect.
8. Run focused unit and MariaDB integration tests.
9. Commit, e.g. `feat(integration): add idempotent inbox and reliable outbox`.

### Task 6: Implement integration query/retry API and permissions

**Files:**

- Create: integration controller/DTO/query-service files listed in Section 4.5
- Modify: `backend/mes-security/src/main/java/com/hospital/mes/security/SecurityConfiguration.java`
- Add the approved append-only permission seed migration if permission rows are not included in Task 1; next physical version only
- Create: integration controller/security tests in `mes-integration` and `mes-boot`

**Steps:**

1. Write failing contract tests for the approved DTO schemas, headers, permissions, and 403/409/422 errors.
2. Implement `GET /api/v1/integration/messages` with `integration:view`.
3. Implement explicit `POST /api/v1/integration/messages/{id}/retry` with `integration:retry`, `Idempotency-Key`, `If-Match`, optimistic conflict handling and same-transaction AuditEvent.
4. Seed `audit:view`, `ebr:sign`, `integration:view`, `integration:retry` using an approved mapping to current IAM storage; never create a second permission vocabulary.
5. Verify unprivileged/authenticated/authorized cases and idempotent repeated retry.
6. Commit, e.g. `feat(integration): expose frozen message monitoring APIs`.

### Task 7: Implement the approved frozen UI surface

**Files:**

- Modify/Rewrite: router, layout, styles, dashboard and App test listed in Section 4.2
- Create: approved audit/platform view, API/type modules, components and component tests listed in Section 4.5

**Steps:**

1. Write failing route tests for the single approved route and `audit:view` access.
2. Write failing component tests for loading, empty, error, pagination/filter behavior from the approved DTOs, and retry visibility under `integration:retry`.
3. Rebuild the shell to match the frozen screenshot/design tokens: 58 px top bar, 216 px sidebar, navy palette, `#1677FF` actions, `#F4F7FB` background, white surfaces, 32 px controls and 44 px table rows.
4. Implement audit/integration tables using real API responses only. Do not hard-code prototype health figures.
5. Make retry an explicit action with version/confirmation/error feedback; backend remains authoritative.
6. Verify keyboard focus, labels, contrast, 1440×1024 screenshot comparison, and responsive behavior.
7. Run `npm test -- --run`, `npm run typecheck`, and `npm run build`.
8. Commit, e.g. `feat(web): add frozen audit and integration operations view`.

### Task 8: NFR evidence, full integration and regression

**Files:**

- Create/update only approved NFR evidence under `docs/compliance/` and operational configuration under existing deploy paths
- Modify: README/API/database/architecture documentation listed in Section 4.2
- Do not change CI assertions unless the reviewed CI contract itself changes

**Steps:**

1. Execute the approved NFR-001 security checks: least privilege, transport/secret configuration, time-source evidence, dependency audit.
2. Execute the approved NFR-002 backup/restore drill against frozen RPO/RTO and retain auditable evidence.
3. Execute the approved NFR-003 retention/retrieval test without physical deletion.
4. Run `mvn -B -ntp test`.
5. Run `mvn -B -ntp -Pci-integration verify` with MariaDB and authenticated Redis.
6. Run frontend tests, typecheck, production build and `npm audit --omit=dev --audit-level=high`.
7. Run repository, Compose, CI policy and CI negative-test scripts.
8. Verify formal TC-AUD-001, TC-SIG-001, TC-INT-001 and TC-INT-002 evidence and all affected IAM/frontend regressions.
9. Produce a changed-file list, migration diff, test log summary and unresolved-risk statement.
10. Request code review using `superpowers:requesting-code-review`; do not merge, weaken tests, or start MES-002.
11. Commit documentation/evidence only after tests pass, e.g. `test(platform): verify MES-001 acceptance and regression`.

---

## 10. Acceptance Matrix

| Requirement | Required implementation evidence | Formal test |
|---|---|---|
| AUD-001 | `gxp_audit_event`, same-transaction service, query API, `audit:view`, rollback proof | TC-AUD-001 |
| SIG-001 | deterministic server digest, reauth, append-only signature, invalidation/re-sign, ownership-resolved API | TC-SIG-001 |
| INT-001 | inbox identity, outbox enqueue/dispatch/retry, query/retry API, optimistic lock, audit | TC-INT-001, TC-INT-002 |
| NFR-001 | measurable approved security evidence | NFR verification |
| NFR-002 | approved RPO/RTO and backup/restore evidence | NFR verification |
| NFR-003 | approved retention/retrieval evidence with no physical deletion | NFR verification |

Definition of done: every applicable design gate is resolved, all formal and regression tests pass, no frozen contract is changed, no later MES epic is pre-implemented, and a reviewer confirms the produced contracts for MES-002–013. Then STOP.

---

## 11. Approval Boundary

Approval must explicitly resolve DG-001 through DG-009 or accept the stated recommendation for each. A generic "approve" cannot authorize invention of the missing physical schema, DTOs, signature canonicalization, retry state machine, UI route/data contract, or NFR values.

**STOP — no implementation may begin from this draft.**
