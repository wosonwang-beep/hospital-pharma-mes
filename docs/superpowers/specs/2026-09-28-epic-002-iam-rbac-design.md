# EPIC-002 IAM and RBAC Design

## 1. Purpose and agreed scope

EPIC-002 gives hospital staff a managed identity and functional authorization system for the MES. The first release uses MES-managed employee accounts. It does not integrate LDAP, Active Directory, SSO, public self-registration, organization or department data scopes, or electronic signatures.

Success means an administrator can create and disable employees, assign multiple roles, reset passwords, and manage each role's menu and action permissions. Staff can log in, see only authorized menus and buttons, and call only authorized backend APIs. The effective permission set is the union of a user's roles; there are no per-user permission overrides.

Account and role changes take effect at the user's next login. Existing sessions retain their login-time permission snapshot until they end. A separate explicit password reset or security response may revoke sessions for safety; routine role edits and account disablement do not implicitly revoke them.

## 2. Architecture choice

Use a signed short-lived JWT plus a Redis-backed login session. MariaDB is the source of truth for users, roles, permissions, assignments, password hashes, and security events. Redis stores the bounded session and its immutable login-time permission snapshot. The JWT contains a user identifier, session identifier, issue time, expiry, and signing key identifier, but not the full permission list.

This approach was selected over a single long-lived JWT containing permissions. It supports logout, bounded inactivity, explicit session revocation, and permission snapshots without enlarging every request token. Requests fail closed when the required Redis session cannot be read.

`mes-system` owns IAM accounts, roles, permission registry, assignments, administration APIs, and persistence. `mes-security` owns password verification, token issuance and verification, session handling, authentication filters, and authorization integration. A narrow identity-directory interface defined in `mes-security` is implemented by `mes-system`, so `mes-security` never depends on `mes-system`. `mes-boot` assembles both modules and owns Flyway migrations. No module depends on `mes-boot`.

The frontend owns login and account-administration views, current-user state, route guards, menu rendering, button visibility, and the API client. Its visibility rules help users navigate; the backend authorization check is authoritative.

## 3. Identity and permission model

The MariaDB model contains:

- `sys_user`: immutable ID, case-insensitive unique login name, display name, password hash, enabled state, failed-login count, lock-until time, first-login password-change flag, optimistic-lock version, and created/updated metadata;
- `sys_role`: immutable ID, stable unique role code, display name, enabled state, optimistic-lock version, and created/updated metadata;
- `sys_permission`: stable unique permission code, type (`MENU` or `ACTION`), display name, optional menu route and parent, enabled state, and metadata;
- `sys_user_role` and `sys_role_permission`: current effective assignments only. A grant creates a relationship and a revoke removes it. The later Slice 2 design supersedes the original historical-assignment proposal; security events remain append-only;
- `sys_security_event`: append-only record of login outcomes, password changes and resets, account changes, role changes, and permission-assignment changes.

The database uses MariaDB-compatible Flyway migrations appended after the Foundation migration. Accounts and roles are disabled rather than physically deleted. Unique constraints and optimistic locking prevent duplicate names and lost administrative updates.

Permission codes are registered by the application and versioned migrations. Administrators grant or revoke registered permissions from roles; they cannot invent an arbitrary permission code through an API. Future business modules add their own stable codes through reviewed migrations. Each functional module has a menu permission for navigation and read-only queries and one general action permission for ordinary mutations and their buttons. Separate action permissions are registered only when an exceptional operation needs independent control. Protected API mappings declare their required permissions; unregistered or unprotected business mappings fail an architecture check and are denied by default.

The initial registry includes only the MES shell and IAM administration operations needed in this release. It does not create placeholder production permissions for unbuilt business modules.

## 4. Account lifecycle and bootstrap

Only authorized administrators create accounts and assign roles. There is no anonymous account-creation endpoint. A new account receives a cryptographically generated temporary password shown once to the administrator for out-of-band delivery. The application does not log or persist that plaintext password. On first login, the employee must change it before accessing normal functions.

Administrators may enable or disable accounts, reset passwords, and assign roles. Disabling an account blocks its next login; an already active session follows the agreed session-snapshot lifetime. An administrative password reset revokes that user's active sessions as an explicit security operation, and the next login requires a password change.

The first system administrator is created by an explicit one-time bootstrap command against an initialized database. The command requires operator-provided identity and a generated or securely entered secret. It refuses to run after an administrator exists and does not place a default credential in source, configuration, seed SQL, CI, or logs.

Passwords are hashed with a current adaptive encoder, using BCrypt at a configured work factor of at least 12 for this release. Passwords are 12–128 characters and may contain spaces and Unicode; arbitrary character-class composition rules are not imposed. Five consecutive failed logins lock the account for 15 minutes. Client-facing login errors do not reveal whether the username exists, password was wrong, or account is disabled. Every outcome is recorded as a minimal security event without credential material.

## 5. Authentication and session lifecycle

On successful login, the server merges enabled roles and enabled permissions, creates a Redis session snapshot, returns a 15-minute access JWT, and sets an opaque high-entropy renewal credential in an `HttpOnly`, `Secure`, `SameSite=Strict` cookie scoped to `/api/v1/auth`. Production requires TLS. Local development may disable the cookie's `Secure` flag only in the local profile over localhost.

The frontend holds the access JWT in memory. On page reload or access-token expiry, it calls the renewal endpoint; the cookie is not readable by JavaScript. Renewal validates the Redis session and opaque credential and issues a fresh JWT without reloading roles from MariaDB. The renewal credential is stored as a hash in Redis, never as plaintext. Authentication requests that rely on the cookie validate the request origin. No business mutation uses the renewal cookie as its sole authorization mechanism.

Each session has an absolute lifetime of eight hours and an inactivity timeout of 30 minutes. Authenticated activity extends only the inactivity deadline; it never extends the absolute deadline. Logout removes the current Redis session and clears the renewal cookie. Invalid, expired, missing, or revoked sessions yield HTTP 401. An authenticated user lacking an action permission receives HTTP 403. The API error envelope and trace ID follow Foundation conventions.

The JWT signing key comes from external configuration; no production key or usable default is committed. Keys have identifiers to permit controlled rotation. Tokens, cookies, password hashes, temporary passwords, and connection credentials are never written to application logs or security events.

## 6. Backend API and authorization

The `/api/v1/auth` surface supports login, renewal, logout, current-user details, authorized menu data, and password change. The `/api/v1/admin` surface supports employee listing/details, creation, enable/disable, password reset, role assignment, role listing/details, role creation/update/enable/disable, and granting/revoking registered permissions. These are explicit commands rather than generic status-update or unrestricted object-update endpoints.

Only login and renewal are anonymous. Current-user and password-change operations require an authenticated session. Administrator reads require the corresponding module menu permission; mutations also require its general action permission. The authentication filter checks JWT signature and expiry, resolves the Redis session, and puts its snapshot authorities into the Spring Security context. Backend API authorization uses those authorities. Unrecognized `/api/v1` routes remain denied by default; a new business endpoint must be registered with a permission requirement and tested before it is opened.

Mutating administration commands validate input, use transactions, enforce uniqueness and optimistic locks, and append a security event in the same MariaDB transaction as the successful change. A failed command does not emit a successful-change event. Security events contain event type, outcome, actor/target IDs when known, timestamp, trace ID, and limited request context; they do not contain secrets or full request payloads. Full GMP audit trails and electronic signatures remain a separate later capability.

## 7. Frontend behavior

The Vue application adds a login view, password-change view, authorized application navigation, and administrator views for employees and roles. The current-user store contains identity and permission codes from the active session. Router guards redirect unauthenticated users to login and reject unauthorized menu routes. Button controls consume the same action codes used by the backend API. A 401 response attempts one renewal and retries the original request once; a failed renewal clears in-memory identity and redirects to login. A 403 response shows a permission-denied state without retrying.

The client does not persist the access JWT in local storage or session storage. Multiple simultaneous 401 responses share one in-flight renewal request to avoid duplicate retries. The UI never treats hidden controls as the sole authorization boundary.

## 8. Error handling and operational safety

MariaDB failure prevents login and administration. Redis failure prevents authentication and renewal; it does not silently bypass the session check. The API returns stable errors with trace IDs and no stack traces or secret details. Login rate limiting and account lockout avoid user enumeration in their public responses. Administrative conflicts return a clear conflict response and do not overwrite newer data.

All URLs and cookies follow same-origin deployment in production. Local Vite development uses a proxy to the backend so credentialed renewal does not require wildcard CORS. Health information may distinguish database/Redis outages without exposing credentials or internal connection details to anonymous clients.

## 9. Delivery slices and verification

Implement EPIC-002 in three independently testable slices, each with its own plan and review:

1. **Identity and session core:** MariaDB schema and identity repository, bootstrap administrator, password policy, login/renewal/logout/current-user API, Redis session snapshot, JWT filter, and backend integration tests.
2. **Role administration and API authorization:** registered permission catalog, user and role commands, current-state assignments, security events, backend guards, and database/Redis integration tests. See `2026-09-28-epic-002-iam-slice-2-design.md` for the approved permission and interface details.
3. **Frontend IAM:** login and password-change flows, user/role administration screens, authorized menus/routes/buttons, and frontend interaction tests.

Unit and integration tests must cover password hashing and lockout, first-login change, session absolute and idle expiry, renewal and logout, multi-role permission union, stale snapshots until next login, account disable at next login, explicit password-reset revocation, 401/403 behavior, default-deny API mappings, optimistic conflicts, and security-event persistence. GitHub CI runs the backend and frontend suites plus real MariaDB/Redis integration tests. No test substitutes H2 or an in-memory Redis implementation for the required integration checks.

## 10. Deferred work

SSO/LDAP/AD, organization and department data scopes, per-user permission overrides, self-registration, password recovery by email/SMS, production business permissions, workflow authorization, complete GMP audit trails, electronic signatures, and deployment policy are outside EPIC-002. The module interfaces and event records must permit these later capabilities without weakening the current authorization rules.
