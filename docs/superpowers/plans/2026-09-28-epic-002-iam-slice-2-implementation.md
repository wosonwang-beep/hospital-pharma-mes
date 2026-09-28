# EPIC-002 IAM Slice 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver backend employee/role administration, current-state role assignments, module-level RBAC, and minimal security events without building the frontend or production business.

**Architecture:** `mes-system` owns administration APIs, commands, MyBatis-Plus persistence, and security-event writes. `mes-security` continues to authenticate JWT/Redis sessions and explicitly authorizes the registered routes from the login-time permission snapshot. The original V003 upgrade plan below is superseded by the user's greenfield decision recorded next.

Greenfield correction (2026-09-28): There is no legacy business data. Define current-state assignment pairs and IAM menu permissions directly in V002, remove V003 and the upgrade-only test/dependency, and reset only the dedicated local MES development database. Earlier V003-specific steps below remain as historical plan text, not current implementation instructions.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring Security, MyBatis-Plus, MariaDB, Flyway, Redis, Maven, JUnit 5, MockMvc.

**Spec:** `docs/superpowers/specs/2026-09-28-epic-002-iam-slice-2-design.md` (also read the parent IAM design and `AGENTS.md`).

## Global Constraints

- Java 21, Spring Boot 3.x, MariaDB-compatible SQL, and MyBatis-Plus for ordinary CRUD.
- Do not edit V001 or V002; append V003 and test fresh installation plus V002 upgrade before using it on valued data.
- Relationship tables store current pairs only; security events remain append-only, minimal, and credential-free.
- Each IAM module has one `MENU` permission and one general `ACTION` permission; do not create per-button codes in this slice.
- Backend routes are explicitly allowlisted and default-denied; frontend visibility is not an authorization check.
- Existing Redis permission snapshots remain unchanged until next login; password reset is an explicit session revocation.
- Do not remove the last effective `SYSTEM_ADMIN`; no arbitrary permission creation, generic status update, or full-set assignment replacement.
- Preserve module dependency direction and existing Foundation API envelope/trace conventions.

## Review Focus

1. V002 databases containing both active and revoked pairs: upgrade keeps one copy of each active pair, drops revoked rows, and enforces uniqueness (Task 1).
2. Forged or stale authorization, including an unknown admin URL and action-only permission without its menu: return 401/403, never accidental access (Task 2).
3. Duplicate normalized login, oversized or invalid names, and temporary-password leakage: reject safely; no secret in log/event (Task 3).
4. Concurrent administrators removing the final effective admin or granting the same pair: one safe winner or conflict; never zero admins or duplicate pairs (Tasks 4–5).
5. Redis outage during password reset and stale sessions after routine role edits: do not treat reset as successful without revocation; routine edits take effect at next login (Tasks 3 and 5).

---

## File and interface map

- `backend/mes-boot/src/main/resources/db/migration/V003__iam_current_assignments.sql`: one-way schema/data conversion and IAM menu registration.
- `backend/mes-system/src/main/java/com/hospital/mes/system/infrastructure/`: existing identity mappers plus new `SysPermissionEntity`, `SysPermissionMapper`, and focused assignment queries.
- `backend/mes-system/src/main/java/com/hospital/mes/system/application/`: `UserAdministration`, `RoleAdministration`, `AssignmentAdministration`, `AdminSafetyGuard`, `PageResult<T>`, and their command/result records. Each service owns its transaction and calls one shared security-event writer.
- `backend/mes-system/src/main/java/com/hospital/mes/system/api/`: `UserAdminController`, `RoleAdminController`, `AdminExceptionAdvice`; DTOs stay in the API layer.
- `backend/mes-security/src/main/java/com/hospital/mes/security/SecurityConfiguration.java`: method/path allowlist with module-menu and module-action checks.
- `backend/mes-security/src/main/java/com/hospital/mes/security/password/TemporaryPasswordGenerator.java`: generates a cryptographically random 24-character temporary secret.
- `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/`: MariaDB/Redis/MockMvc tests for schema, authorization, commands, migration upgrade, and session behavior.

### Task 1: Current-state schema and login snapshot queries

**Files:** Create `backend/mes-boot/src/main/resources/db/migration/V003__iam_current_assignments.sql`; modify `backend/mes-system/src/main/java/com/hospital/mes/system/infrastructure/SysRolePermissionMapper.java`, `SysRoleMapper.java`; modify tests `IamSchemaIT.java`, `IdentityDirectoryIT.java`; create `IamUpgradeIT.java` in the existing integration test package; add a test-scoped MariaDB Testcontainers dependency to `backend/mes-boot/pom.xml`.

**Interfaces:** Produces unique current `(user_id, role_id)` and `(role_id, permission_id)` pairs, `menu:iam:users`, `menu:iam:roles`, and `target_permission_id` on `sys_security_event`. Existing `IdentityDirectory.loadLoginSnapshot(long)` remains the consumer-facing interface.

- [ ] **Step 1: Write failing schema and snapshot tests.** `IamSchemaIT.currentAssignmentsAreUniqueAndHaveNoRevokeColumns()` asserts neither relationship table has `revoked_at`/`revoked_by` and both pair indexes are unique; `IdentityDirectoryIT.snapshotUnionsOnlyCurrentEnabledAssignments()` inserts current pairs, deletes one, and asserts its permission disappears on a new snapshot. Assert both IAM menus are granted to `SYSTEM_ADMIN`.
- [ ] **Step 2: Run the tests red.** From repository root run `mvn -B -ntp -pl backend/mes-boot -am -Pci-integration -Dit.test=IamSchemaIT,IdentityDirectoryIT verify` with the existing CI MariaDB/Redis environment; expect assertions about historical columns/current pairs to fail.
- [ ] **Step 3: Implement V003 and mapper updates.** In V003 delete revoked rows, deduplicate active pairs by keeping the lowest ID, drop the V002 grantor/revoker foreign keys and grant/revoke metadata columns, add pair uniqueness and `target_permission_id`, then insert/grant the two menus. Keep V002 unchanged. Remove `revoked_at IS NULL` from active-role and active-permission joins and administrator count; make count require enabled users and roles.
- [ ] **Step 4: Add upgrade proof.** `IamUpgradeIT.upgradeFromV002PreservesOnlyActivePairs()` runs Flyway through V002 in a disposable MariaDB Testcontainer, seeds active/revoked/duplicate-active pairs, upgrades through V003, and asserts one current row per active pair plus new menus. Stop the isolated container after the test; never run this fixture against the configured application schema.
- [ ] **Step 5: Run Task 1 tests green.** Repeat the Task 1 integration command; expect no failures. Commit `feat(iam): migrate assignments to current state`.

### Task 2: Explicit backend route authorization

**Files:** Modify `backend/mes-security/src/main/java/com/hospital/mes/security/SecurityConfiguration.java`; create `backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/AdminAuthorizationIT.java`; update `AuthFilterIT.java` only if its unknown-route assertion must target a truly unknown path.

**Interfaces:** Consumes login snapshot authorities. Produces an explicit method/path authorization matrix: user reads need `menu:iam:users`; user writes need that menu plus `action:iam:user.manage`; role/catalog reads need `menu:iam:roles`; role writes need that menu plus `action:iam:role.manage`. No general `/api/v1/admin/**` permit rule.

- [ ] **Step 1: Write failing MockMvc authorization tests.** `AdminAuthorizationIT.menuOnlyCanReadButCannotMutate()` checks GET user list passes the security filter (404 before Task 3 adds the controller; 200 afterward) while POST create is 403. `actionWithoutMenuIsForbidden()` checks POST create is 403. `unknownAdminPathIsForbidden()` checks 403 even with all IAM permissions. `missingAndInvalidTokensAreUnauthorized()` checks 401 and trace ID. `mustChangePasswordCannotUseAdminRoutes()` checks 403 despite IAM permissions until the password is changed.
- [ ] **Step 2: Run tests red.** Run `mvn -B -ntp -pl backend/mes-boot -am -Pci-integration -Dit.test=AdminAuthorizationIT verify`; expect the allowed read route to be denied before route rules exist.
- [ ] **Step 3: Implement the exact endpoint allowlist.** Add request matchers for the paths and methods in the spec, ordered before `.anyRequest().denyAll()`. Require both authorities for writes using a Spring Security authorization manager; do not rely on button visibility or broad authentication-only matching. Keep the existing auth/foundation paths unchanged.
- [ ] **Step 4: Run tests green.** Repeat the Task 2 command; expect all cases to pass. Commit `feat(iam): authorize administration routes by module`.

### Task 3: User administration and password reset

**Files:** Create `mes-system` `application/UserAdministration.java`, `application/AdminSafetyGuard.java`, `application/AdminSecurityEventWriter.java`, `application/LoginNameNormalizer.java`, `api/UserAdminController.java`, `api/AdminExceptionAdvice.java`; create `mes-security` `password/TemporaryPasswordGenerator.java`; extend `SysUserMapper.java`, `SysUserEntity.java`, `SysSecurityEventEntity.java`, and `SystemIdentityDirectory.java`; create `UserAdministrationIT.java` and `TemporaryPasswordGeneratorTest.java` under `mes-security/src/test`.

**Interfaces:** Define `CreateUser`, `CreatedUser`, `UserView`, and `TemporaryPassword` as application records in `UserAdministration.java`, plus `PageResult<T>(List<T> items, long total, int page, int size)` in its own file. `UserAdministration.create(CreateUser command, long actorId, String traceId) -> CreatedUser`; `profile(long userId, String displayName, long expectedVersion, long actorId, String traceId) -> UserView`; `setEnabled(long userId, boolean enabled, long expectedVersion, long actorId, String traceId) -> UserView`; `resetPassword(long userId, long actorId, String traceId) -> TemporaryPassword`; `list(int page, int size) -> PageResult<UserView>` and `get(long userId) -> UserView`. `AdminSafetyGuard.runPreservingEffectiveAdmin(Runnable change) -> void` locks the system-role row, runs the change in the caller's transaction, and rejects/rolls it back if no effective admin remains. `AdminSecurityEventWriter.append(String type, Long actorId, Long targetUserId, Long targetRoleId, Long targetPermissionId, String traceId) -> void` inserts the minimal event in the caller's transaction. `CreatedUser` and `TemporaryPassword` expose plaintext only to the controller response with `Cache-Control: no-store`; persistence receives only a `PasswordService.hash` result.

- [ ] **Step 1: Write failing user tests.** `UserAdministrationIT` covers create/read/page/profile/enable/disable, case-insensitive duplicate login, invalid and overlong input, stale version 409, missing user 404, refusal to disable the final effective admin, one-time 24-character password with only a hash stored, `no-store`, security events without secret material, and rollback when event insertion fails. A generator unit test asserts cryptographic randomness and length.
- [ ] **Step 2: Run tests red.** Run `mvn -B -ntp -pl backend/mes-boot -am -Pci-integration -Dit.test=UserAdministrationIT verify` and the generator unit test; expect missing endpoints/components.
- [ ] **Step 3: Implement user commands and controller.** Map exactly the user endpoints in the spec; extract the existing NFKC/strip/lowercase login normalization into `LoginNameNormalizer.normalize(String) -> String` and use it in both user creation and `SystemIdentityDirectory`. Use MyBatis-Plus for CRUD and version-checked updates, and write the event in the same MariaDB transaction. Use `AdminSafetyGuard` before disable and `AdminSecurityEventWriter` for events; keep immutable login/ID out of profile updates. Limit pages and return stable ID order. Centralize 400/404/409/503 error mapping with trace IDs in `AdminExceptionAdvice`.
- [ ] **Step 4: Implement password reset fail-closed behavior.** Generate/hash a new temporary password, revoke all Redis sessions before declaring reset success, then persist the hash, first-login-change flag, and event. If Redis revocation fails, do not commit the password/event and return 503. If the later database transaction fails, revoked sessions remain revoked as a safe side effect; do not report a successful reset. Test this outage and the successful next-login requirement.
- [ ] **Step 5: Run tests green.** Repeat Task 3 tests and `mvn -B -ntp test`; expect no failures. Commit `feat(iam): add employee administration`.

### Task 4: Role administration and registered catalog

**Files:** Create `mes-system` `application/RoleAdministration.java`, `api/RoleAdminController.java`, `infrastructure/SysPermissionEntity.java`, `infrastructure/SysPermissionMapper.java`; extend `SysRoleMapper.java`, `SysRoleEntity.java`; create `RoleAdministrationIT.java`.

**Interfaces:** Define `CreateRole`, `RoleView`, and `PermissionView` as application records in `RoleAdministration.java`. `RoleAdministration.create(CreateRole command, long actorId, String traceId) -> RoleView`; `rename(long roleId, String displayName, long expectedVersion, long actorId, String traceId) -> RoleView`; `setEnabled(long roleId, boolean enabled, long expectedVersion, long actorId, String traceId) -> RoleView`; `list(int page, int size) -> PageResult<RoleView>`; `get(long roleId) -> RoleView`; `catalog() -> List<PermissionView>`. Role codes are immutable; catalog is read-only.

- [ ] **Step 1: Write failing role/catalog tests.** Cover list/detail/create/rename/enable/disable; duplicate role code, missing ID, invalid name, stale version, stable pagination, security events, and refusal to disable the last effective system role. Assert arbitrary permission-creation routes remain 403 and both IAM menu/action codes appear in catalog.
- [ ] **Step 2: Run tests red.** Run `mvn -B -ntp -pl backend/mes-boot -am -Pci-integration -Dit.test=RoleAdministrationIT verify`; expect missing endpoints.
- [ ] **Step 3: Implement role/catalog reads and commands.** Map the exact role and catalog endpoints, use version-checked updates and the Task 3 exception envelope, never delete roles, and append the minimal event in the transaction. Use `AdminSafetyGuard` before disabling `SYSTEM_ADMIN`.
- [ ] **Step 4: Run tests green.** Repeat Task 4 tests; expect no failures. Commit `feat(iam): add role administration and permission catalog`.

### Task 5: Single-pair grants, revokes, and last-admin guard

**Files:** Create `mes-system` `application/AssignmentAdministration.java`; extend `SysUserRoleMapper.java`, `SysRolePermissionMapper.java`, `SysPermissionMapper.java`, `SysSecurityEventEntity.java`, `SystemIdentityDirectory.java` or a shared event writer; add the four assignment routes to the controllers; create `AssignmentAdministrationIT.java` and `AdminSessionSnapshotIT.java`.

**Interfaces:** `AssignmentAdministration.grantUserRole(long userId, long roleId, long actorId, String traceId) -> void`, `revokeUserRole(...) -> void`, `grantRolePermission(long roleId, String permissionCode, long actorId, String traceId) -> void`, and `revokeRolePermission(...) -> void`. Controllers return `ApiResponse<Void>`; application services never return API envelopes. The Task 3 `AdminSafetyGuard.runPreservingEffectiveAdmin(Runnable change)` serializes last-admin-sensitive changes and tests the post-change enabled user/role/permission union. Assignment events use the Task 3 writer with `targetPermissionId` when applicable.

- [ ] **Step 1: Write failing assignment/guard tests.** Cover grant/revoke both pair types; duplicate/absent pair 409; unknown or unregistered target 404; event actor/target/permission ID and rollback on event failure; no revoked relationship row; multi-role union. Concurrent revocation/disable tests assert at least one effective administrator remains. `AdminSessionSnapshotIT` proves routine edits keep old Redis authorities until a new login.
- [ ] **Step 2: Run tests red.** Run `mvn -B -ntp -pl backend/mes-boot -am -Pci-integration -Dit.test=AssignmentAdministrationIT,AdminSessionSnapshotIT verify`; expect missing routes or guard.
- [ ] **Step 3: Implement pair commands and guard.** Use one-pair insert/delete and unique constraints, validated registered codes, event insertion in the same transaction, and the Task 3 guard for candidate last-admin changes. The guard counts effective admins by enabled user, active system-role assignment, enabled role, and permissions sufficient for both IAM modules; return 409 on a forbidden final removal. Do not revoke Redis snapshots for routine edits.
- [ ] **Step 4: Run tests green.** Repeat Task 5 tests; expect no failures. Commit `feat(iam): manage current role assignments safely`.

### Task 6: Whole-slice verification and contract review

**Files:** Modify tests and `README.md` only for verified operational/API guidance; add no frontend IAM or production-business code.

**Interfaces:** Produces a passing, reviewable backend Slice 2 and an operator note: back up valued MariaDB data before V003 because revoked relationship rows are removed.

- [ ] **Step 1: Run all backend unit tests.** From repository root run `mvn -B -ntp test`; expect all tests to pass.
- [ ] **Step 2: Run real integration tests.** With MariaDB and Redis configured for `ci`, run `mvn -B -ntp -Pci-integration verify`; expect schema upgrade, API, authorization, event, and session tests to pass.
- [ ] **Step 3: Check repository policy and migration history.** Run `pwsh -NoProfile -File scripts/verify-repository.ps1` and `pwsh -NoProfile -File scripts/verify-ci.ps1`. Confirm `git diff --check`, no edits to V001/V002, no credentials in events/log fixtures, and only the intended files changed.
- [ ] **Step 4: Review the branch.** Request a code review before merge. Resolve findings, rerun affected tests and the complete integration suite, then follow the approved branch-integration method. Do not push or merge without the user's direction.
