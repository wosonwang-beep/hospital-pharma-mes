# EPIC-002 Slice 2: IAM Role Administration and API Authorization

## Purpose and boundary

This slice lets authorized hospital MES administrators manage employee accounts, roles, and functional permissions through backend APIs. It extends the completed identity/session core without building the production modules or frontend administration screens. Frontend IAM remains Slice 3. The first release uses system-managed staff accounts, multiple roles per user, and no per-user permission overrides or data scopes.

Success means an administrator can manage users and roles, grant and revoke registered relationships, and observe consistent 401/403 responses. Menu visibility and ordinary operations follow each functional module's permissions. The backend enforces every API independently of whether the frontend shows a button. Routine account/role/permission changes appear in an existing login session only after the user logs in again. Explicit password reset may revoke sessions as defined by Slice 1.

The earlier `2026-09-28-epic-002-iam-rbac-design.md` established the overall IAM architecture. This document supersedes its historical-assignment proposal and refines the permission granularity and Slice 2 interfaces. The overall JWT/Redis session architecture and other Slice 1 decisions remain unchanged.

## Chosen permission model

Each functional module has a `MENU` permission and one general `ACTION` permission. The menu permission allows navigation and read-only module queries; the general action permission allows ordinary mutations in that module and the corresponding buttons. A user may have several roles; the union of their enabled roles' enabled permissions is captured at login. There is no implicit privilege from merely knowing an API URL.

For the initial IAM modules, register `menu:iam:users` and `menu:iam:roles` in addition to the existing `action:iam:user.manage` and `action:iam:role.manage`. The existing `menu:home` remains. Grant both new menu permissions to the seeded `SYSTEM_ADMIN` role during migration. Do not create one permission per ordinary button. A sensitive operation may later be explicitly registered with a dedicated permission when its separate authorization need is agreed; it must also require the module's general action permission. Slice 2 registers no such exception. The permission catalog is versioned by reviewed migrations; administration APIs cannot create arbitrary permission codes.

For a module read endpoint, require the corresponding menu permission. For a mutation endpoint, require both the module menu permission and its general action permission. The frontend in Slice 3 must consume the same codes to hide routes and controls; it is not the security boundary. Unrecognized API routes remain default-denied. Authenticated self-service endpoints, such as `/api/v1/auth/me` and password change, retain their existing rules and do not require IAM administration permissions.

The selected model favors a small, stable permission registry over per-button codes. A single global operation permission was rejected because it would let one role mutate unrelated modules. Automatically creating an action code for every button was rejected because it would burden assignment and invite inconsistent frontend/backend coverage.

## API components and contracts

`mes-system` owns the administration controllers, application commands, registered permission lookup, persistence, and security-event writes. `mes-security` continues to own session authentication and Spring Security integration. The API layer uses the Foundation response/error conventions. No new reverse module dependency or generic `updateStatus` endpoint is introduced.

Under `/api/v1/admin/users`, provide paginated list and detail reads; explicit create and editable-profile commands; enable and disable commands; password reset; and single-role grant and revoke commands. The immutable user ID and normalized login name are not changed by the profile command. A new account receives a one-time temporary password through the established Slice 1 handling; it is not logged or persisted as plaintext. User list/detail require `menu:iam:users`; all user mutation endpoints require that menu permission plus `action:iam:user.manage`.

Under `/api/v1/admin/roles`, provide paginated list and detail reads; explicit create and editable-name commands; enable and disable commands; and single-permission grant and revoke commands. Stable role codes cannot be changed. Role list/detail and a read-only registered-permission catalog require `menu:iam:roles`; mutations require that menu permission plus `action:iam:role.manage`. Catalog results indicate permission type and module but do not allow arbitrary catalog editing.

The endpoint inventory is fixed for this slice:

| Method and path | Purpose |
| --- | --- |
| `GET /api/v1/admin/users`, `GET /api/v1/admin/users/{userId}` | Paginated list and detail, including current role IDs |
| `POST /api/v1/admin/users` | Create an employee and return a one-time temporary password |
| `PATCH /api/v1/admin/users/{userId}/profile` | Change editable profile fields only |
| `POST /api/v1/admin/users/{userId}/enable`, `POST /api/v1/admin/users/{userId}/disable` | Explicit account state commands |
| `POST /api/v1/admin/users/{userId}/password-reset` | Reset password, revoke that user's sessions, and return a one-time temporary password |
| `POST /api/v1/admin/users/{userId}/roles/{roleId}`, `DELETE /api/v1/admin/users/{userId}/roles/{roleId}` | Grant or revoke one role |
| `GET /api/v1/admin/roles`, `GET /api/v1/admin/roles/{roleId}` | Paginated list and detail, including current permission codes |
| `POST /api/v1/admin/roles`, `PATCH /api/v1/admin/roles/{roleId}/name` | Create a role or change its display name |
| `POST /api/v1/admin/roles/{roleId}/enable`, `POST /api/v1/admin/roles/{roleId}/disable` | Explicit role state commands |
| `POST /api/v1/admin/roles/{roleId}/permissions/{permissionCode}`, `DELETE /api/v1/admin/roles/{roleId}/permissions/{permissionCode}` | Grant or revoke one registered permission |
| `GET /api/v1/admin/permissions` | Read the registered permission catalog |

Lists use bounded page size and stable ID ordering. Profile/name/state changes require an expected entity version and fail on a stale value. Assignment commands rely on a unique pair constraint and transactional checks; they do not accept a full replacement array. Creation and password-reset responses expose the generated temporary password once through the authenticated API response only, with `Cache-Control: no-store`.

Grant and revoke APIs address exactly one user-role or role-permission pair per request. There is no initial full-set replacement endpoint, avoiding accidental revocation of omitted assignments. A duplicate grant, a revoke of an absent pair, a stale optimistic version, or a concurrent uniqueness race returns a conflict response. Missing users, roles, or registered permissions return not found. Invalid inputs return validation errors. Unauthenticated calls return 401 and authenticated callers lacking a required permission receive 403. Errors follow the existing envelope and trace-ID conventions and do not reveal secrets.

`SYSTEM_ADMIN` is not an authorization bypass: its effective privileges still come from enabled assignments. An effective system administrator is an enabled user with an active `SYSTEM_ADMIN` assignment to the enabled system role and, through the union of enabled roles, the menu and action permissions needed to administer both IAM modules. Disabling the last such user, revoking that user's `SYSTEM_ADMIN` role, disabling the system role, or removing permission assignments so no effective administrator remains is forbidden. The guard is checked transactionally against concurrent administration. Disabling or removing other administrators is permitted when at least one effective administrator remains. Routine changes do not refresh or revoke existing login-time permission snapshots.

## Current-state assignment storage and migration

`sys_user_role` and `sys_role_permission` store only current effective pairs. Grant inserts a pair; revoke deletes that pair. No revoked row or grant/revoke metadata is retained in those relationship tables. Accounts and roles themselves remain disable-only, not physically deleted. `sys_security_event` stays append-only and holds a minimal record of successful assignment changes, including actor, target, event type, time, and trace ID, with a registered role/permission identifier where applicable. It never stores secrets or a copy of past relationship rows. These events are distinct from the current-state relationship tables and do not implement a full GMP audit trail.

Do not edit `V002__iam_core.sql`, which may already have run. A new `V003` migration converts the two relationship tables to current-state form, preserving active assignments, removing previously revoked rows, enforcing unique `(user_id, role_id)` and `(role_id, permission_id)` pairs, and registering/granting the two IAM menu permissions. If the security-event schema needs a structured permission target, add it in `V003`. Migration design must account for MariaDB DDL behavior and be tested against both a fresh database and a database initialized through `V002`. Before applying to a database containing valued data, back it up and verify the planned removal of revoked relationship rows.

The rule against physically deleting production or regulated records still applies to users, roles, and security events. These two assignment tables are deliberately defined as current-state relationships, not retained audit records. Their revocation deletion and one-time migration cleanup implement the user's explicit no-relationship-history requirement. No other record type may be deleted under this exception.

## Command flow and event consistency

Administration commands validate registered targets, actor authority, state, and optimistic versions before changing data. Successful user, role, grant, and revoke commands write a minimal security event in the same MariaDB transaction as the data change; both commit or both roll back. Duplicate or failed commands must not emit a successful-change event. No credentials, access tokens, temporary passwords, or full request payloads enter security events or application logs.

The login-time role/permission union continues to be calculated by `mes-system` for `mes-security`. Its queries must switch from `revoked_at IS NULL` filtering to current-pair joins after `V003`. Redis snapshots remain unchanged until a new login, except for the previously approved explicit password-reset revocation. Redis failure does not bypass authentication or authorization.

## Verification and non-goals

Backend tests cover permission union across multiple roles; menu-only read versus action-protected mutation; default-deny routes; grant, duplicate grant, revoke, and absent revoke; 401/403/404/conflict outcomes; transactional security-event persistence; stale session snapshots until next login; and last-effective-`SYSTEM_ADMIN` protection under concurrency. MariaDB integration tests verify both fresh migrations and `V002` to `V003` upgrade, including active-pair preservation, revoked-row removal, uniqueness, and registered IAM menus. Redis integration tests verify authorization against real session snapshots. CI must run the relevant backend suites.

This slice does not implement frontend views, arbitrary permission creation, per-button permissions by default, per-user overrides, organization/data scopes, SSO, complete GMP audit trails, electronic signatures, or production-business authorization. A later Slice 3 may show the registered menu and action permissions but may not invent permissions in the client.
