# MES-001-R2 Implementation Plan v2

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Plan status:** `STOPPED — MISSING SPECIFICATION: CURRENT ORGANIZATION CONTEXT`. Do not execute any task below until the Design Gate in §8.1 is resolved in writing and this plan is approved by the user.

**Goal:** Implement only MES-001-R2 platform AuditEvent, electronic-signature infrastructure, generic idempotency, integration inbox/outbox operations, the two frozen UI routes, and the twelve formal MES-001 tests without implementing MES-002–013 business capabilities.

**Architecture:** Keep the modular monolith. Add the frozen `audit` module as `mes-audit`; keep inbox/outbox in `mes-integration`; keep credential and Redis session mechanics in `mes-security`; compose them in `mes-boot`. Downstream modules consume application ports and never write platform evidence tables directly. All regulated database mutations use local MariaDB transactions, while external publication happens after the originating transaction.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring Security, MyBatis-Plus, Flyway, MariaDB, Redis, Vue 3, TypeScript, Ant Design Vue, Vitest, Maven Surefire/Failsafe.

**Spec:** `releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/` plus `00_DESIGN_CHANGE_PROPOSAL_DCP-MES-001-R2-001_APPROVED.md`; current repository state on 2026-09-29; supersedes the stopped v1 plan `docs/superpowers/plans/2026-09-29-mes-001-r2-implementation.md`.

## Global Constraints

- One MES task only: MES-001-R2. Do not begin MES-002 or create later business tables, APIs, states, permissions, or UI.
- Java 21 and Spring Boot 3.x only; MariaDB-compatible SQL only; ordinary CRUD uses MyBatis-Plus.
- Physical `V001__foundation_probe.sql` and `V002__iam_core.sql` are immutable forever. Create no migration until this plan is separately approved.
- Planned physical V003 creates exactly five tables; planned physical V004 seeds exactly four permissions. Section 15 migration labels remain logical groups.
- No hard delete or generic `updateStatus`; mutable regulated aggregates use optimistic locking.
- API wrappers stay in the API layer. Domain/application services do not return `ApiResponse`.
- Integration retry policy is centralized: maximum 8 failures; delays `PT1M, PT5M, PT15M, PT1H, PT4H, PT12H, PT24H`; stale claim threshold `PT15M`. Controllers, services, connectors, and downstream modules may not duplicate or override these values.
- Reauthentication TTL is exactly five minutes, single-use, and bound to user/session/org/object type/object ID/meaning/version. Consumption is atomic on first sign attempt and is never restored after transaction rollback. Credentials/tokens never enter logs, AuditEvent, or `auth_context_json`.
- Signature digest is lowercase SHA-256 over UTF-8 RFC 8785 JCS for `{schemaVersion,objectType,objectId,recordVersion,record,evidenceIds}`; evidence IDs are unique lexicographically sorted `TYPE:ID` strings.
- Formal routes are `/audit` and `/integration/operations`. `/platform/operations`, PLAT-001, and metrics without frozen APIs are forbidden.
- RPO, RTO, GxP retention, deployment site, approved time source, and validation evidence stay OPEN. They do not block MES-001 construction but must block production validation and go-live; no values/defaults may be invented.
- Run every formal MES-001 test plus affected integration and regression tests. Do not weaken CI assertions.

## Review Focus

1. Missing, forged, or cross-organization execution context must fail closed; no request parameter/header may select `org_id` unless a later approved contract says so. Covered by the Design Gate and Task 9 authorization/API integration tests.
2. Replayed, expired, differently bound, or rollback-consumed reauthentication tokens must return 401 without leaking secrets. Covered by Task 6 and TC-SIG-002/003.
3. Duplicate or concurrent Idempotency-Key use must replay an identical completed result, reject a changed digest with 409, and allow only one in-progress owner. Covered by Task 4 and Task 8.
4. Concurrent integration claims, eighth failure, stale claims, and manual retry must preserve a one-winner transition, frozen retry count, and audit transaction. Covered by Task 7/8 and TC-INT-002/003/004.
5. Audit update/delete attempts and failed regulated transactions must leave no modified evidence and no orphan event. Covered by Task 3 and TC-AUD-001/002.

---

## 1. Affected Dependency Check

### 1.1 Frozen task graph

| Check | Result |
|---|---|
| Hard dependency | None |
| Soft dependency | None |
| Contracts consumed | None in the logical task graph |
| Logical migration dependency | LG-001 `platform_base`; physical compatibility with existing V002 identity tables |
| Contracts produced | Profile A/M, Audit, Signature, Idempotency, Inbox/Outbox, controlled retry, protected UI contract |
| Downstream consumers | MES-002 through MES-013 |
| Task graph status | PASS |

### 1.2 Current physical repository

- Existing physical migrations are only V001 and V002. Their immutable checksums and names satisfy the v1.0.1 compatibility decision.
- `sys_user(id)` exists and can satisfy explicit audit/signature actor foreign keys.
- `mes-security` supplies user ID, session ID, role and permission snapshots; it does **not** supply `org_id`.
- `mes-integration` exists but contains only object-storage configuration.
- Frozen architecture includes an `audit` module; no `mes-audit` Maven module exists.
- Adding `mes-audit -> mes-common`, `mes-security -> mes-audit`, `mes-integration -> mes-audit`, and `mes-boot -> all three` is acyclic. `mes-audit` must never depend on `mes-security` or `mes-integration`.
- Physical dependency status: PASS except for the organization-context Design Gate in §8.1.

## 2. Affected Gap Analysis

### 2.1 KEEP

- Root build/CI/deployment files and Java 21/Spring Boot dependency management.
- `backend/mes-boot/src/main/resources/db/migration/V001__foundation_probe.sql` and `V002__iam_core.sql`, byte-for-byte.
- `sys_foundation_probe` and every V002 IAM table; MES-001 performs no ALTER.
- Foundation controller/entity/mapper/tests and all existing IAM authentication/admin behavior.
- `sys_security_event` and `AdminSecurityEventWriter` as distinct IAM operational evidence; never alias them to GxP AuditEvent.
- `backend/mes-integration/src/main/java/com/hospital/mes/integration/storage/**` and its tests.
- All empty MES-003–013 modules and package markers.
- Existing success wrapper `ApiResponse(code,message,data,traceId)`.

### 2.2 MODIFY

- `backend/pom.xml`, `backend/mes-boot/pom.xml`, `backend/mes-security/pom.xml`, `backend/mes-integration/pom.xml` for the acyclic module graph and required platform libraries.
- `backend/mes-boot/src/test/java/com/hospital/mes/architecture/ModuleBoundaryTest.java` for 21 modules and exact dependency-direction assertions.
- `backend/mes-security/.../SecurityConfiguration.java` for the five frozen operations and four frozen authorities while retaining default deny.
- `SessionStore`/`RedisSessionStore` only for reauthentication token storage/atomic consumption; existing login/renewal behavior remains unchanged.
- Existing error writers/advices to emit frozen `ApiError`; success responses remain unchanged.
- `application.yml`, `application-prod.yml`, and test configuration only for controlled policy and enterprise-validation binding.
- Frontend router/layout/styles/App tests for the two frozen routes and prototype shell.
- Architecture/API/database/compliance README files only after implementation to point at v1.0.1 and the logical-to-physical ledger.

### 2.3 REWRITE

- Error behavior inside `GlobalExceptionHandler`, `SecurityErrorWriter`, `AuthExceptionAdvice`, and `AdminExceptionAdvice`: preserve existing status semantics but use `ApiError(requestId,code,message,fieldErrors,allowedActions)`.
- `frontend/mes-web/src/layouts/AppLayout.vue`, `styles.css`, and placeholder dashboard content to conform to the frozen prototype without inventing navigation/authentication behavior.

### 2.4 DELETE

- None. Do not delete/rename historical migrations, IAM security events, current foundation probe, user-owned baseline files, or prior plans.
- The forbidden `/platform/operations` and PLAT-001 do not exist in the repository, so there is no deletion action; add a regression scan preventing them.

### 2.5 MISSING

- `mes-audit` module and all audit/signature/idempotency boundaries and persistence.
- Five platform tables, append-only guards, four permission seeds, and ledger verification tests.
- Concrete audit/signature/reauth/integration API DTOs and operations.
- Redis-backed one-use reauthentication token contract.
- Central retry policy, inbox/outbox commands, claim/recovery/manual-retry logic, query API, and worker ports.
- Enterprise validation property/gate component with no enterprise values.
- `/audit` and `/integration/operations` frontend pages, API clients, types, component tests, and auth-context doubles.
- A trustworthy current organization source. This is the sole new implementation blocker (§8.1).

## 3. Existing Code Classification Summary

| Classification | Count/scope | Decision |
|---|---|---|
| KEEP | Existing application/business sources and V001/V002 | Preserve and regress |
| MODIFY | Four backend POMs, security/error/config composition, frontend shell/router/docs | Narrow changes only |
| REWRITE | Error serialization and current placeholder shell content | Behavior remains within frozen contracts |
| DELETE | 0 | No deletion authorized |
| MISSING | Audit module, five-table migrations, platform contracts/APIs/UI/tests, current org source | Add only after approval; org source needs design resolution |

## 4. Impact Analysis

### Database/migration

- Planned V003 appends five tables and no ALTER/DROP. Planned V004 appends permission rows/assignments against V002 and must be rerunnable without duplicates.
- Profile A is used only for `gxp_audit_event`; Profile M is used for the other four tables.
- MariaDB triggers reject UPDATE/DELETE on `gxp_audit_event`; application/runtime privileges remain INSERT/SELECT.
- Clean install and upgrade-from-V002 must both pass; Flyway checksums for V001/V002 must stay identical.

### Backend/module graph

- Reactor count changes from 20 to 21.
- `mes-audit` becomes a produced contract used by security, integration, and later modules; it depends only on common/framework contracts.
- `mes-security` owns credential verification and Redis token lifecycle but implements the reauthentication port defined by `mes-audit`.
- `mes-integration` consumes audit/idempotency application ports; audit must not depend back on integration.

### Transactions and evidence

- `AuditApplicationService.append(AuditCommand)` and `OutboxApplicationService.enqueue(EnqueueOutboxCommand)` require an existing transaction for regulated business writes.
- Manual retry, its AuditEvent, and idempotency completion commit together.
- Reauthentication token consumption happens before the signature database transaction and is outside its rollback boundary.
- Network publication never occurs while the originating local business transaction is open.

### API/security

- Add `POST /api/v1/auth/reauth`, `POST /api/v1/records/{type}/{id}/sign`, `GET /api/v1/audit-events`, `GET /api/v1/integration/messages`, and `POST /api/v1/integration/messages/{messageRef}/retry` exactly as frozen.
- Retry requires both `integration:view` and `integration:retry`; sign/reauth require `ebr:sign`; audit query requires `audit:view`; message query requires `integration:view`.
- Error-envelope changes affect every existing controller test and security 401/403 path.

### Frontend

- Common shell changes can affect all routes and requires visual/responsive regression.
- UI auth is an injected context double in MES-001; MES-002 owns real login/navigation lifecycle.
- UI tables may display only fields returned by the concrete frozen APIs; raw payload and invented operational metrics are prohibited.

### MES-002–013

- MES-002 consumes permission/auth/navigation contracts and must not reinterpret platform evidence.
- MES-003–006 use common profiles and generic audit/idempotency/outbox, not local variants.
- MES-007 supplies EBR signable providers and correction-driven invalidation.
- MES-008–012 consume stable integration message identities and retry policy.
- MES-013 supplies the QA release provider and release transaction orchestration.

## 5. Contracts Consumed / Produced / Regression Scope

### Contracts consumed

- Logical task graph: none.
- Physical compatibility only: authenticated `LoginSnapshot`, session ID, role/permission snapshot, `sys_user(id)`, request/trace ID, MariaDB transaction manager, Redis.
- **Not available:** principal-derived organization ID. This is not permission to derive or default one.

### Contracts produced

1. Profile A/M and logical-to-physical migration ledger.
2. `AuditApplicationService`, `AuditQueryService`, append-only `gxp_audit_event`, and concrete audit API.
3. `SignableObjectProvider`, `SignatureCanonicalizer`, `ReauthenticationPort`, sign/verify/invalidate/re-sign services, and generic APIs.
4. `PlatformIdempotencyService` backed by `platform_idempotency_record`.
5. Inbox/outbox commands, stable IDs, state machines, query/retry APIs, worker claim/recovery ports, and `IntegrationRetryPolicy`.
6. Frozen `ApiError` and four permission codes.
7. Protected `/audit` and `/integration/operations` components and UI auth-context interface.
8. Production enterprise-validation gate without enterprise values.

### Regression scope

- All Maven unit tests and module-boundary tests.
- IAM login/session/password/user/role/assignment/security-event integration tests.
- Existing foundation and MinIO tests.
- Flyway clean and V002-upgrade paths on MariaDB; Redis integration profile.
- All error response, 401/403, default-deny, trace/request ID tests.
- Frontend component tests, router scan, typecheck, build, responsive/visual comparison.
- Repository/CI scripts and dependency/cycle enforcement.
- Negative inventory scan proving no MES-002–013 database/API/state/UI was introduced.

## 6. Frozen Physical Database Plan

### Planned `V003__mes_001_platform_base.sql`

| Table | Columns | Constraints/indexes |
|---|---|---|
| `gxp_audit_event` (Profile A) | `id BIGINT AI PK`; `org_id BIGINT NN`; `created_by BIGINT NN`; `created_at DATETIME(3) NN DEFAULT CURRENT_TIMESTAMP(3)`; `actor_id BIGINT NN`; `actor_role VARCHAR(100)`; `action VARCHAR(80) NN`; `object_type VARCHAR(80) NN`; `object_id VARCHAR(100) NN`; `old_value_digest TEXT`; `new_value_digest TEXT`; `reason VARCHAR(1000)`; `client_info VARCHAR(500)`; `occurred_at DATETIME(3) NN`; `transaction_id VARCHAR(100) NN`; `request_id VARCHAR(100)`; `source VARCHAR(30) NN`; `idempotency_key VARCHAR(128)` | FK `actor_id -> sys_user(id)`; source CHECK; indexes `ix_gxp_audit_org_time(org_id,occurred_at,id)`, actor/time, object/time, action/time, transaction, request; BEFORE UPDATE/DELETE triggers signal SQLSTATE 45000 |
| `gxp_signature` (Profile M) | common M columns; `signer_id BIGINT NN`; `meaning VARCHAR(100) NN`; `object_type VARCHAR(80) NN`; `object_id VARCHAR(100) NN`; `record_digest CHAR(64) NN`; `signed_at DATETIME(3) NN`; `status VARCHAR(20) NN`; `invalidated_at DATETIME(3)`; `invalidation_reason VARCHAR(1000)`; `auth_context_json LONGTEXT`; `revoked_signature_id BIGINT` | FK signer/user and self-reference; status CHECK VALID/INVALIDATED; indexes `(org_id,object_type,object_id,status,id)`, `(signer_id,signed_at,id)`; controlled update columns only in repository SQL |
| `integration_inbox` (Profile M) | common M columns; `source_system VARCHAR(80) NN`; `message_id VARCHAR(128) NN`; `payload_json LONGTEXT NN`; `status VARCHAR(20) NN`; `received_at DATETIME(3) NN DEFAULT CURRENT_TIMESTAMP(3)`; `processed_at DATETIME(3)`; `retry_count INT NN DEFAULT 0`; `next_retry_at DATETIME(3)`; `last_error_code VARCHAR(100)`; `last_error_message VARCHAR(1000)` | status/retry CHECKs; unique `(org_id,source_system,message_id)`; claim and received indexes |
| `integration_outbox` (Profile M) | common M columns; `message_id VARCHAR(128) NN`; `target_system VARCHAR(80) NN`; `event_type VARCHAR(80) NN`; `aggregate_type VARCHAR(80) NN`; `aggregate_id VARCHAR(100) NN`; `payload_json LONGTEXT NN`; `status VARCHAR(20) NN`; `retry_count INT NN DEFAULT 0`; `next_retry_at DATETIME(3)`; `published_at DATETIME(3)`; `last_error_code VARCHAR(100)`; `last_error_message VARCHAR(1000)` | status/retry CHECKs; unique `(org_id,message_id)`; claim, aggregate, target/created indexes |
| `platform_idempotency_record` (Profile M) | common M columns; `actor_id BIGINT NN`; `operation_code VARCHAR(100) NN`; `idempotency_key VARCHAR(128) NN`; `request_digest CHAR(64) NN`; `state VARCHAR(20) NN`; `http_status SMALLINT`; `response_json LONGTEXT`; `resource_type VARCHAR(80)`; `resource_id VARCHAR(100)`; `expires_at DATETIME(3) NN` | FK actor/user; state CHECK IN_PROGRESS/COMPLETED; unique `(org_id,actor_id,operation_code,idempotency_key)` |

Every Profile M common column is NOT NULL; `updated_by=created_by` on insert; `version_no=0`; application explicitly updates `updated_at/updated_by`; no `ON UPDATE` clause.

### Planned `V004__mes_001_platform_permissions.sql`

- Insert exactly `audit:view`, `ebr:sign`, `integration:view`, and `integration:retry` using the existing V002 `sys_permission` shape.
- Assign the four permissions idempotently to existing `SYSTEM_ADMIN`; create no role/table and alter no V002 column.
- Preserve the logical ledger mapping LG-001 -> physical V003 + V004.

## 7. File Map

### Build/composition

- Modify `backend/pom.xml`, `backend/mes-boot/pom.xml`, `backend/mes-security/pom.xml`, `backend/mes-integration/pom.xml`.
- Create `backend/mes-audit/pom.xml` and `backend/mes-audit/src/main/java/com/hospital/mes/audit/package-info.java`.
- Modify `backend/mes-boot/src/test/java/com/hospital/mes/architecture/ModuleBoundaryTest.java`.

### Audit/idempotency module

- API: `AuditEventController`, `SignatureController`; DTOs `AuditEventQuery`, `AuditEventResponse`, `AuditEventPageResponse`, `SignRecordRequest`, `SignatureResponse`.
- Application: `AuditApplicationService`, `AuditQueryService`, `SignatureApplicationService`, `PlatformIdempotencyService`.
- Ports: `AuditEventRepository`, `SignatureRepository`, `IdempotencyRepository`, `SignableObjectProvider`, `ReauthenticationPort`, `CurrentPlatformContext` (exact organization source blocked by §8.1).
- Domain: `AuditCommand`, `AuditEvent`, `AuditSource`, `Signature`, `SignatureMeaning`, `SignatureStatus`, `SignableObject`, `SignatureCanonicalizer`, `SignCommand`, `InvalidateSignatureCommand`.
- Infrastructure: entity/mapper/repository pairs for `gxp_audit_event`, `gxp_signature`, `platform_idempotency_record`; `Rfc8785SignatureCanonicalizer`; provider registry.

### Security adapter

- Create API DTOs `ReauthenticateForSignatureRequest`, `ReauthenticationResponse` and add `/auth/reauth` to `AuthController` or a focused `ReauthenticationController` under the same base path.
- Create `ReauthenticationService`, `RedisReauthenticationTokenStore`, and tests.
- Modify `SecurityConfiguration`, `SessionStore`, `RedisSessionStore`, security error/advice tests.

### Integration module

- API: `IntegrationMessageController`; DTOs `IntegrationMessageQuery`, `IntegrationMessageResponse`, `IntegrationMessagePageResponse`, `RetryIntegrationMessageRequest`.
- Application: `InboxApplicationService`, `OutboxApplicationService`, `IntegrationMessageQueryService`, `IntegrationRetryApplicationService`, `IntegrationDispatchService`, `AbandonedClaimRecoveryService`.
- Policy/ports: `IntegrationRetryPolicy`, `ControlledIntegrationRetryPolicy`, `ExternalMessagePublisher`, `InboxMessageHandler`.
- Domain: `InboxMessage`, `OutboxMessage`, direction/status enums and explicit commands.
- Infrastructure: inbox/outbox entity, mapper, repository implementations; claim/failure explicit SQL where conditional atomic updates are required.

### Common/error/config

- Create `backend/mes-common/src/main/java/com/hospital/mes/common/api/ApiError.java` and `FieldError.java`.
- Modify the four existing error writers/advices and their tests.
- Create `EnterpriseValidationProperties` and production validation gate; modify `application.yml`, `application-prod.yml`, test configuration.

### Frontend

- Create `src/auth/PlatformAuthContext.ts`, `src/api/http.ts`, `src/api/audit.ts`, `src/api/integration.ts`, `src/types/audit.ts`, `src/types/integration.ts`.
- Create `views/audit/AuditQueryView.vue`, `components/audit/AuditEventTable.vue`, `views/integration/IntegrationOperationsView.vue`, `components/integration/IntegrationMessageTable.vue`, `components/integration/RetryMessageAction.vue` and focused tests.
- Modify `router/index.ts`, `AppLayout.vue`, `styles.css`, `App.test.ts`; do not create a `/platform/operations` view.

## 8. Design and Execution Gates

### 8.1 BLOCKER ORG-CTX-001 — current organization source

**Conflict:** Every new table requires non-null `org_id`; audit query says organization is principal-derived; reauthentication binds to org. The current repository `LoginSnapshot(userId,loginName,displayName,roleCodes,permissionCodes,mustChangePassword)` and Redis session hash contain no organization. V001/V002 contain no organization membership/source.

**Why implementation must stop:** Any default constant, request header/path, user-ID substitution, unrestricted cross-org query, or V002 ALTER would invent a field/config/API/security rule or violate the approved migration boundary.

**Required user-approved resolution:** Freeze one authoritative `CurrentPlatformContext` source and its lifecycle before execution, including how `org_id` enters the authenticated session, how users switch/lose organization scope, which MES task owns persistence, and the fail-closed API behavior when absent. The resolution must not change V001/V002 in place.

**Recommended direction for a follow-up design clarification:** Define an explicit security-to-platform organization context contract and an append-only physical migration owned by the appropriate IAM/organization task. Do not accept client-supplied organization scope. If MES-001 is allowed to ship infrastructure before that adapter, explicitly approve test-only organization doubles and define whether production routes remain disabled or fail closed until the adapter exists.

**Rejected implicit options:** `org_id=1`, `org_id=user_id`, unverified `X-Org-Id`, cross-org reads, nullable org, or altering V001/V002.

### 8.2 Approval gate

After ORG-CTX-001 is resolved, update only the affected context interfaces/files below, rerun dependency/gap analysis, and obtain explicit approval of this plan. Plan approval does not approve any later MES task.

## 9. Implementation Tasks (not authorized for execution)

### Task 1: Module graph and frozen API error contract

**Files:** build/composition files in §7; create `ApiError.java`, `FieldError.java`; modify existing error writers/advices and tests.

**Interfaces:** Produce `ApiError(String requestId,String code,String message,List<FieldError> fieldErrors,List<String> allowedActions)`; retain `ApiResponse<T>` for success only.

- [ ] Write failing tests for 400/401/403/404/409/422/500 error shape and existing success shape.
- [ ] Add `mes-audit`, exact acyclic dependencies, and module-boundary assertions.
- [ ] Implement the frozen error DTO/mappings without changing auth/admin business behavior.
- [ ] Run `mvn -B -ntp -pl backend/mes-common,backend/mes-security,backend/mes-system,backend/mes-boot -am test`; expect all tests PASS.
- [ ] Commit only Task 1 files.

### Task 2: Physical migrations and migration ledger verification

**Files:** create `V003__mes_001_platform_base.sql`, `V004__mes_001_platform_permissions.sql`; create `Mes001SchemaIT.java`, `Mes001MigrationUpgradeIT.java`.

**Interfaces:** Produce exactly the schema in §6; consume existing V002 `sys_user`, `sys_permission`, `sys_role`, `sys_role_permission`.

- [ ] Capture and assert V001/V002 source checksums before adding migrations.
- [ ] Write failing MariaDB clean/upgrade/schema/index/trigger/permission tests for TC-MIG-001.
- [ ] Implement V003, then V004; no ALTER/DROP and no other table/permission.
- [ ] Run the `ci-integration` Flyway tests against clean DB and a DB stopped at V002; expect head V004 and unchanged history.
- [ ] Commit only the two migrations and migration tests.

### Task 3: Audit append/query contract

**Files:** audit domain/application/API/persistence files from §7; focused unit and MariaDB integration tests.

**Interfaces:** `void append(AuditCommand command)` with transaction propagation MANDATORY; `AuditEventPage query(long orgId, AuditEventQuery query)`; repository exposes insert/query only and no update/delete method.

- [ ] Write failing domain validation and fixed-sort/filter tests.
- [ ] Write failing same-transaction rollback and database UPDATE/DELETE guard tests for TC-AUD-001/002.
- [ ] Implement append-only domain, MyBatis persistence, and exact `GET /api/v1/audit-events` DTO/API.
- [ ] Assert actor/time/org come from approved server context, never request DTOs; redact secrets/raw payloads.
- [ ] Run audit unit/API/MariaDB tests; expect all PASS.
- [ ] Commit only Task 3 files.

### Task 4: Generic platform idempotency

**Files:** idempotency domain/application/persistence files in `mes-audit`; unit and concurrency integration tests.

**Interfaces:** `begin(IdempotencyCommand): IdempotencyDecision`; `complete(IdempotencyHandle,int,String,String,String)` inside the operation transaction. Decisions are OWNER, REPLAY, CONFLICT, IN_PROGRESS_CONFLICT.

- [ ] Write failing tests for identical replay, changed digest `IDEMPOTENCY_KEY_REUSED`, concurrent one-owner behavior, and rollback.
- [ ] Implement canonical request digest, unique-key claim, optimistic completion, and stored response replay.
- [ ] Verify no downstream/module-local generic idempotency table exists.
- [ ] Run unit and MariaDB concurrency tests; expect all PASS.
- [ ] Commit only Task 4 files.

### Task 5: Signature provider registry and RFC 8785 digest

**Files:** signature domain/application/provider-registry/canonicalizer files; RFC 8785 test vectors and provider tests.

**Interfaces:** `SignableObjectProvider.objectType()`, `allowedMeanings()`, `loadForSignature(orgId,objectId)`, `validateSignable(context,object)`; `SignableObject(objectType,objectId,recordVersion,canonicalRecord,evidenceIds)`; `String digest(SignableObject)`.

- [ ] Write failing JCS/SHA-256 vectors, decimal/timestamp normalization tests, evidence sort/dedup tests, and unsupported provider/meaning tests.
- [ ] Implement registry and canonicalizer; client digest input remains impossible.
- [ ] Register only a test-profile provider for TC-SIG-001/003; create no production placeholder object/table.
- [ ] Run signature unit tests; expect deterministic lowercase 64-hex output.
- [ ] Commit only Task 5 files.

### Task 6: Reauthentication and signature transaction lifecycle

**Files:** security reauth API/service/Redis files; audit signature application/API/persistence files; integration tests.

**Interfaces:** `ReauthenticationChallenge issue(ReauthenticationRequest,PlatformContext)` and `ConsumedReauthentication consume(token,ExpectedBinding)`; `SignatureResponseData sign(SignCommand)`; `invalidate(InvalidateSignatureCommand)`; `verify(SignatureId)`.

- [ ] Write failing tests for exact PT5M TTL, seven bindings, atomic first-attempt consumption, replay/expiry 401, redaction, and rollback non-restoration.
- [ ] Write failing tests for sign/idempotency/version/provider authorization, digest tamper detection, VALID->INVALIDATED only, and re-sign self-link rules.
- [ ] Implement Redis token hashes/Lua atomic consume outside the sign DB transaction; never persist/log credential/token.
- [ ] Implement signature insert plus AuditEvent in one transaction and controlled invalidation metadata/version update only.
- [ ] Expose exact reauth/sign APIs and DTOs; run TC-SIG-001/002/003.
- [ ] Commit only Task 6 files.

### Task 7: Inbox/outbox state machines and central retry policy

**Files:** integration domain/policy/application/persistence/port files and tests from §7; application configuration.

**Interfaces:** explicit receive/process/enqueue/claim/publish/fail/recover/manual-retry commands; `IntegrationRetryPolicy.maximumAttempts()=8`, `delayAfterFailure(1..7)`, `claimTimeout()=PT15M`; publisher/handler ports.

- [ ] Write failing state-transition, immutability, retry schedule, eighth-failure, and no-reset tests.
- [ ] Write failing concurrent inbox dedup and one-winner claim tests.
- [ ] Bind all retry values in one controlled platform policy component; reject per-module override paths.
- [ ] Implement MyBatis CRUD plus explicit conditional SQL for claims/recovery; network calls occur after claim transaction.
- [ ] Run TC-INT-001/002/003 domain and MariaDB tests.
- [ ] Commit only Task 7 files.

### Task 8: Integration query and authorized manual retry API

**Files:** integration API/query/retry application files; audit/idempotency consumers; API/MariaDB tests.

**Interfaces:** `query(PlatformContext,IntegrationMessageQuery)` returns payload-free page; `manualRetry(PlatformContext,messageRef,reason,ifMatch,idempotencyKey)` returns concrete message DTO.

- [ ] Write failing filter/page/messageRef tests and verify payload exclusion.
- [ ] Write failing dual-permission, reason length, If-Match, idempotency replay/conflict, ineligible state 422, concurrency 409, stale recovery, and audit rollback tests.
- [ ] Implement exact GET and POST operations; retry maps INBOX to RECEIVED and OUTBOX to PENDING without resetting counters/error evidence.
- [ ] Commit transition, AuditEvent, and idempotency completion in one transaction.
- [ ] Run TC-AUD-001 and TC-INT-004 API/DB tests.
- [ ] Commit only Task 8 files.

### Task 9: Security authorization and current context adapter

**Files:** `SecurityConfiguration.java`, approved current-context adapter files, security/API integration tests.

**Interfaces:** Resolve actor ID, role snapshot, session ID, permissions, and the **approved** organization source into `CurrentPlatformContext`; no client-selected org.

- [ ] After §8.1 resolution, write failing tests for missing/forged/cross-org context and all five operation permissions.
- [ ] Implement the approved adapter only; retain `anyRequest().denyAll()`.
- [ ] Verify retry requires both authorities and that organization filters cannot be widened by requests.
- [ ] Run existing IAM suite plus new API authorization tests; expect all PASS.
- [ ] Commit only Task 9 files.

### Task 10: Enterprise validation gate

**Files:** `EnterpriseValidationProperties`, production validator, YAML/profile tests, open-register reference documentation.

**Interfaces:** bind `mes.continuity.rpo`, `mes.continuity.rto`, `mes.compliance.gxp-retention`, `mes.deployment.site-id`, `mes.time.approved-source`; values remain absent from source.

- [ ] Write failing production-context tests for missing/malformed keys and passing non-production tests with omissions.
- [ ] Implement presence/syntax validation under production profile only; do not assert approval or implement purge.
- [ ] Run TC-NFR-001; expect production validation failure without external values and non-prod startup PASS.
- [ ] Commit only Task 10 files.

### Task 11: Frozen Audit and Integration UI

**Files:** frontend API/types/auth/components/views/router/layout/style/test files listed in §7.

**Interfaces:** audit client maps only AuditEventPageResponse; integration client maps only IntegrationMessagePageResponse and retry operation; auth context exposes permission checks for component tests.

- [ ] Write failing route/component tests for `/audit`, `/integration/operations`, separate permissions, retry visibility, loading/empty/error/403 states, and payload exclusion.
- [ ] Write a source scan that fails on `/platform/operations`, `PLAT-001`, unsupported metric labels/constants, or hard-coded sample production data.
- [ ] Implement the two pages from `UI-AUD-Q.png` and `UI-INT-OPS.png`; add no login/auth navigation flow.
- [ ] Run `npm test -- --run`, `npm run typecheck`, and `npm run build`; expect PASS.
- [ ] Perform responsive and screenshot comparison against the frozen references; document deviations as failures.
- [ ] Commit only Task 11 files.

### Task 12: Full MES-001 acceptance and regression

**Files:** integration fixtures/test-only provider, verification evidence, narrow documentation updates.

**Interfaces:** No new production contract; validates Tasks 1–11 as one MES-001 increment.

- [ ] Run formal cases: TC-MIG-001, TC-AUD-001/002, TC-SIG-001/002/003, TC-INT-001/002/003/004, TC-UI-003, TC-NFR-001.
- [ ] Run `mvn -B -ntp test` and MariaDB/Redis `ci-integration`; expect all reactor modules PASS.
- [ ] Run frontend tests/typecheck/build and all repository/CI verification scripts; expect PASS.
- [ ] Verify OpenAPI operation/schema/permission metadata against v1.0.1 and check no later-task artifacts were added.
- [ ] Recompute migration checksums and compare V001/V002 with the pre-task capture; expect exact equality.
- [ ] Update documentation and ledger evidence without changing the frozen baseline package.
- [ ] Request code review; correct only MES-001 findings; rerun the complete gate.
- [ ] Commit acceptance evidence and STOP. Do not begin MES-002.

## 10. Formal Test Allocation

| Test case | Owning task | Required evidence |
|---|---:|---|
| TC-MIG-001 | 2 | clean + V002 upgrade, immutable checksums, V003/V004/ledger |
| TC-AUD-001 | 3/8 | regulated transition + audit same commit/rollback |
| TC-AUD-002 | 3 | filters/fixed sort, concrete DTO, DB guards, no secrets |
| TC-SIG-001 | 5/6 | test provider, digest tamper detection |
| TC-SIG-002 | 6 | exact five minutes, seven bindings, replay 401, redaction |
| TC-SIG-003 | 5/6 | rollback token stays consumed, independent JCS digest |
| TC-INT-001 | 7 | concurrent duplicate inbox, one row/effect |
| TC-INT-002 | 7 | committed business/outbox, external failure/retry, stable ID |
| TC-INT-003 | 7 | seven delays, eighth dead-letter, no reset |
| TC-INT-004 | 8/9 | dual permission, reason, version, idempotency, one winner, audit |
| TC-UI-003 | 11 | exact routes/permissions/API states, forbidden route/metrics absent |
| TC-NFR-001 | 10 | production config gate; no fabricated values |

## 11. Self-Review

- Spec coverage: AUD-001, SIG-001, INT-001, NFR-001/002/003 and all twelve formal test cases are allocated.
- Type consistency: API DTO names match the v1.0.1 OpenAPI; persistence names match the five-table design; signature/integration states match the frozen state machine.
- Dependency consistency: no cycle; audit owns platform contracts, security implements reauth, integration consumes audit/idempotency.
- Prohibited scope: no existing migration edits, no MES-002 execution, no later business provider, no generic status API, no invented metrics or NFR values.
- Open gap: current organization scope is neither in the principal nor persisted. This plan intentionally does not invent its source. Until §8.1 is resolved, the plan is not implementation-ready and no Task 1–12 may start.

## 12. Approval/STOP

Implementation Plan v2 is generated but remains `STOPPED — MISSING SPECIFICATION: CURRENT ORGANIZATION CONTEXT`. Resolve ORG-CTX-001 through approved design clarification, revise the affected plan lines, and obtain explicit human approval before any code or migration work. This document does not authorize V003/V004 creation, business-code changes, or MES-002.
