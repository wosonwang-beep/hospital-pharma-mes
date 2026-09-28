# EPIC-002 Identity and Session Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the first independently usable IAM vertical slice: bootstrap one administrator, log in with a managed account, change an initial password, renew and end a bounded session, and read the login-time identity/permission snapshot.

**Architecture:** `mes-system` owns MariaDB identity data and implements the narrow `IdentityDirectory` port declared in `mes-security`; `mes-security` owns credentials, JWT, Redis sessions, authentication, and authorization; `mes-boot` assembles both and owns Flyway and HTTP integration tests. No security-to-system or system-to-boot dependency. Role administration, general permission registration, and frontend screens belong to later slices.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring Security OAuth2 JOSE (HS256 with a base64-encoded external key of at least 256 bits), MyBatis-Plus, MariaDB/Flyway, Redis, JUnit 5/MockMvc, Maven.

**Spec:** [EPIC-002 IAM/RBAC design](../specs/2026-09-28-epic-002-iam-rbac-design.md)

## Global Constraints

- Work in the isolated `D:\codex\_project\gmp\hospital-pharma-mes` worktree on `feat/epic-002-iam`. Preserve existing unrelated files and append Flyway migrations; never edit V001.
- Before production code in each task, add a failing focused test, run it, then implement the minimum and rerun it. Run `mvn -B -ntp test` after every task; run the real-service `mvn -B -ntp -Pci-integration verify` where specified. Do not use H2 or fake Redis for integration acceptance.
- Preserve Foundation's API envelope and trace ID. Authentication/authorization failures require explicit 401/403 JSON handlers because the existing global exception handler does not handle security-filter failures.
- No default administrator, JWT signing key, plaintext credential, or reusable test secret in production configuration. Never log credentials, cookies, JWTs, password hashes, or full login request bodies.
- A routine account disable or role edit must not revoke an existing session. Password reset and an explicit security response may revoke it. First-login password change must revoke the old session and require fresh login.
- The first slice may create all IAM tables and minimal bootstrap role/permission rows, but it must not expose general administrator CRUD or frontend UI. A later slice extends the registry and guards.

## Dependency and interface map

1. `mes-security` declares `IdentityDirectory` with `findForLogin(normalizedLogin)`, `recordLoginFailure(userId, now)`, `recordLoginSuccess(userId, now)`, `changeOwnPassword(userId, expectedHash, replacementHash)`, `loadLoginSnapshot(userId)`, and `appendSecurityEvent(...)`. Its DTOs contain only identity/session fields, not persistence entities. `mes-system` implements this port transactionally; `mes-boot` depends on both modules.
2. `mes-security` declares `SessionStore` with `create`, `findById`, `renew`, `touch`, `revoke`, and `revokeAllForUser`. Its Redis implementation stores a serialized snapshot and only a digest of the renewal credential. Use atomic Redis operations (Lua or WATCH/MULTI) for renewal rotation and idle-deadline update so concurrent renewals cannot both succeed.
3. `AccessTokenCodec` signs/verifies a minimal `sub`, `sid`, `iat`, `exp`, `kid` JWT using an externally supplied key ring. `SessionAuthenticationFilter` resolves `sid` in Redis on every authenticated request and sets authorities from that snapshot; the token alone never authorizes access.
4. `AuthService` coordinates `IdentityDirectory`, password encoder, `SessionStore`, and `AccessTokenCodec`; `AuthController` owns `/api/v1/auth` request/response envelopes and cookie attributes. Do not return `ApiResponse` from domain/application collaborators.
5. Slice-1 protected routes are `GET /api/v1/auth/me`, `POST /api/v1/auth/change-password`, and `POST /api/v1/auth/logout`; `POST /api/v1/auth/login` and `POST /api/v1/auth/refresh` are anonymous. First-login sessions may only use `me`, `change-password`, `logout`, and `refresh`. All other `/api/v1/**` mappings stay default-denied. General `/api/v1/admin/**` permissions and menu endpoint are Slice 2.

## Review Focus

- Dependency direction and bean assembly; no `mes-security -> mes-system` or module cycle.
- Session rotation/replay, absolute vs idle expiry, Redis outage fail-closed, and external key validation.
- First-login gate, generic login errors, lockout boundary, and case-insensitive login uniqueness.
- Audit event atomicity and absence of secrets in database, logs, and API errors.
- No accidental opening of Foundation or future business endpoints while replacing the default-deny security chain.

---

## Task 1: Persist the IAM core schema without credentials in seed data

**Files:** Create `backend/mes-boot/src/main/resources/db/migration/V002__iam_core.sql`; create `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/IamSchemaIT.java`. Do not change V001. Follow the existing `FoundationInfrastructureIT` profile and Failsafe conventions.

- [ ] **RED:** `IamSchemaIT` asserts the six tables (`sys_user`, `sys_role`, `sys_permission`, `sys_user_role`, `sys_role_permission`, `sys_security_event`), expected unique constraints, version columns, assignment grant/revoke metadata, and no seeded user. Run `mvn -B -ntp -pl backend/mes-boot -am -Pci-integration verify`; expect the new schema assertions to fail against real MariaDB.
- [ ] **GREEN:** Add MariaDB-compatible tables, indexes, foreign keys, optimistic versions, and append-only event columns. Model active assignment using nullable `revoked_at`; enforce one active assignment per pair in transaction/application logic because MariaDB unique indexes treat NULL specially. Store login name in a normalized unique column; preserve display spelling separately. Seed only stable `MENU` shell and `ACTION` IAM-admin permission codes plus an enabled `SYSTEM_ADMIN` role; no user or password.
- [ ] Rerun the integration command and `mvn -B -ntp test`; inspect Flyway history and table constraints. Commit this task separately.

## Task 2: Identity port, account repository, password and lockout rules

**Files:** Create `backend/mes-security/src/main/java/com/hospital/mes/security/identity/{IdentityDirectory,LoginIdentity,LoginSnapshot,SecurityEvent}.java` and password policy/encoder configuration under `.../security/password/`; create `backend/mes-system/src/main/java/com/hospital/mes/system/{domain,infrastructure}/...` account/role/assignment/event types and MyBatis-Plus mappers; update `backend/mes-system/pom.xml` to depend on `mes-security` and MyBatis-Plus, and `backend/mes-security/pom.xml` for its required libraries; create focused unit tests in both modules and `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/IdentityDirectoryIT.java`.

- [ ] **RED:** Tests prove case-insensitive lookup, unique normalized login, BCrypt cost >=12, 12–128 Unicode/space password acceptance, 11/129-character rejection, five consecutive bad passwords causing a 15-minute lock, success resetting failure count, disabled/locked account denial, and permission union across two enabled roles excluding disabled/revoked assignments. An integration test checks each persisted login outcome has a minimal `sys_security_event` with no credential material. Run focused module tests and real-service integration; expect failure before implementation.
- [ ] **GREEN:** Implement the port in `mes-system` with transactional MyBatis-Plus CRUD, conditional versioned updates for counters/lock state, and an append-only security-event writer. Provide a dummy BCrypt verification path for unknown names and one generic public login-failure message; keep detailed reasons only in limited event types. Define a stable immutable snapshot DTO shared across the port. Avoid an API dependency from `mes-system`.
- [ ] Rerun focused tests, `mvn -B -ntp test`, and integration verification. Commit separately.

## Task 3: Redis session snapshot and renewal credential rotation

**Files:** Create `backend/mes-security/src/main/java/com/hospital/mes/security/session/{SessionStore,SessionSnapshot,RedisSessionStore}.java` and configuration; update `backend/mes-security/pom.xml`; create unit tests for deadline arithmetic and `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/RedisSessionIT.java`. Move Redis dependency to `mes-security` if needed; do not make it depend on `mes-boot`.

- [ ] **RED:** Real-Redis tests check snapshot/permissions round-trip, opaque credential digest storage (plaintext absent), single-use rotation (replay fails), concurrent refresh yielding at most one success, logout/revokeAll, idle expiry at 30 minutes, absolute expiry at 8 hours despite activity, and fail-closed behavior on Redis errors. Use an injectable clock and namespaced test keys with cleanup. Expect failure before code.
- [ ] **GREEN:** Generate renewal credentials from a cryptographic RNG, hash with SHA-256/HMAC or an equivalent fixed-length one-way digest, and make check-and-rotate atomic. Keep TTL <= the earlier absolute/idle deadline; update both recorded idle deadline and Redis TTL on authenticated activity, never the absolute deadline. Index sessions per user for explicit revocation; expire/clean that index safely. Do not re-query MariaDB on refresh.
- [ ] Rerun focused and full tests plus real-service integration. Commit separately.

## Task 4: Signed access tokens and default-deny authentication filter

**Files:** Create `backend/mes-security/src/main/java/com/hospital/mes/security/jwt/AccessTokenCodec.java` and implementation; create `.../security/web/{SessionAuthenticationFilter,SecurityErrorWriter}.java`; update `SecurityConfiguration.java` and replace or adapt the unused `JwtAuthenticationProvider.java`; update `application-{local,ci,prod}.yml` only for explicit key-ring binding and local cookie override. Create focused security tests and `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/AuthFilterIT.java`.

- [ ] **RED:** Tests reject altered/expired/unknown-`kid` tokens, missing or revoked Redis sessions, and Redis faults with 401; confirm missing permission gives 403 with Foundation envelope/trace ID, while an unknown API route and `/api/v1/admin/**` remain denied. Test 15-minute access expiry and that no permission list appears in JWT claims. Verify startup fails if no valid signing key is configured in non-test app profiles. Expect failure first.
- [ ] **GREEN:** Bind a signing-key ring from external environment, validate strength/active `kid` at startup, issue/verify JWT with the agreed claims only, and resolve the session snapshot on every authorized request. Put its permission codes in Spring authorities, preserve existing public Foundation/docs/health paths, and explicitly allow only the Slice-1 auth endpoints. Return 401 for bad/missing authentication and 403 for authenticated insufficient permission without stack details. Keep CSRF protection scoped to cookie-based renewal via strict origin validation; bearer-authenticated mutations do not rely solely on cookies.
- [ ] Rerun focused tests and `mvn -B -ntp test`; inspect the security matcher order. Commit separately.

## Task 5: Bootstrap and complete auth HTTP vertical slice

**Files:** Create `backend/mes-security/src/main/java/com/hospital/mes/security/application/AuthService.java`; create `backend/mes-security/src/main/java/com/hospital/mes/security/api/AuthController.java` or an equivalent API package without `mes-boot` dependency; add `mes-common` to `backend/mes-security/pom.xml` for the API envelope; create an explicit bootstrap command in `backend/mes-boot/src/main/java/com/hospital/mes/bootstrap/...` and operator instructions under `docs/development/`; add `mes-system` dependency to `backend/mes-boot/pom.xml`; update `MesApplication.java` only if mapper/bean scanning needs it. Add `AuthFlowIT.java` in `mes-boot`.

- [ ] **RED:** HTTP integration tests prove one-time bootstrap succeeds then refuses a second administrator; generated/entered temporary secret never appears in logs or DB; login returns a 15-minute JWT and renewal cookie with `HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth`; first-login access to normal APIs is blocked; authenticated `me` shows immutable role/permission snapshot; password change requires old secret, clears first-login flag, revokes old session and requires new login; refresh rotates cookie and preserves snapshot; logout clears cookie and invalidates session; bad login is non-enumerating and records an event. Add an explicit local-profile localhost cookie exception test. Expect failure first.
- [ ] **GREEN:** Implement request/response validation and `AuthService` orchestration. Bootstrap must be an explicitly invoked one-shot mode, read secret securely from interactive stdin or generate and display it once to the operator (never command-line argument, environment default, source, or logs), and exit without starting the HTTP server; refuse if an administrator already exists. Do not add public signup. For password change, verify the old secret, revoke sessions successfully before committing the hash/event change, then require fresh login; if Redis revocation fails, do not commit the password change. If the subsequent database transaction fails, a revoked session is acceptable and the user logs in again with the unchanged password. Define the current-user response and stable error codes.
- [ ] Rerun HTTP tests and full real-service integration. Commit separately.

## Task 6: CI, operator verification, and first-slice review gate

**Files:** Update `.github/workflows/ci.yml` to provide a CI-only test signing key through the integration job environment (never a production default); update `docs/development/` with bootstrap, key provisioning/rotation, local TLS-cookie exception, and manual login/renew/logout checks; add or extend `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/AuthFailureIT.java`.

- [ ] **RED:** Add tests for MariaDB unavailable at login, Redis unavailable at authentication/refresh, expired absolute/idle deadlines, invalid origin on cookie renewal, refresh replay, account disabled preventing next login while existing session remains valid, and permission changes visible only after new login. If role editing is not yet exposed, change assignments directly in a transaction in the test to prove snapshot semantics. Expect failure where behavior is not yet covered.
- [ ] **GREEN:** Close any gaps with minimal changes, document operational setup and explicit deferred Slice-2 APIs. Run `mvn -B -ntp test`, `mvn -B -ntp -Pci-integration verify` with real MariaDB/Redis, `npm test -- --run`, `npm run typecheck`, `npm run build`, and the repository/CI contract scripts. Review test reports and diff; do not claim green without recorded command results.
- [ ] Request code review using `superpowers:requesting-code-review`; fix findings with `superpowers:receiving-code-review` and rerun affected tests. Only then use `superpowers:verification-before-completion` and `superpowers:finishing-a-development-branch` to decide PR/integration with the user. Commit the final docs/CI task separately.

## Out of this plan

The second slice will implement general user/role administration, registered permission management, per-operation API authorization and its architecture check, password reset/revoke-all, menu data, and full administration events. The third slice will implement Vue login, routing, button guards, renew-on-401, and admin screens. Do not silently include those features in this first-slice PR.
