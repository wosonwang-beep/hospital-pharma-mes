# First administrator bootstrap

This is a one-time, operator-run procedure for a newly migrated MES database. It creates the first system-managed administrator. It is **not** a public registration endpoint. The command exits without starting the HTTP server.

1. Provision MariaDB and run the application with the intended database configuration (`MES_DB_URL`, `MES_DB_USERNAME`, `MES_DB_PASSWORD`). Flyway creates the IAM tables and the `SYSTEM_ADMIN` role. Use an isolated terminal: the generated temporary password is displayed there once. Do not redirect or retain terminal output in build logs.
2. Build the backend: `mvn -B -ntp -pl backend/mes-boot -am -DskipTests package`.
3. Run the packaged application from the repository root, for example:

   ```powershell
   java -jar backend/mes-boot/target/mes-boot-0.1.0-SNAPSHOT.jar --spring.profiles.active=prod --mes.bootstrap-admin=true --mes.bootstrap-login=admin --mes.bootstrap-display="System Administrator"
   ```

   For local development, use `--spring.profiles.active=local` instead. No JWT signing key or Redis connection is needed for this one-shot command. Never pass a password or secret as a command-line option or environment default.
4. Save the displayed temporary password in an approved secret store, then close/clear the terminal. The database stores only a BCrypt hash. The account must change its password on first login. A second bootstrap against the same database fails, even with a different login name; do not delete the existing administrator to work around this.

For the HTTP app, provision `MES_JWT_KID` and `MES_JWT_KEYS` separately; the bootstrap command does not mint tokens. Production requests require HTTPS so the `Secure` renewal cookie is transmitted. The `local` profile alone permits a non-Secure renewal cookie when the request host is loopback (`localhost`, `127.0.0.1`, or `::1`), for local testing. Never enable that exception in production.

The first slice exposes login, current-user snapshot, password change, renewal, and logout at `/api/v1/auth`. It does not yet expose general user/role administration, menus, or production-business APIs.

## Signing keys and a manual check

The HTTP service refuses to start without a valid `MES_JWT_KID` and `MES_JWT_KEYS`. Generate a fresh random key with an approved secret-management tool (at least 32 bytes), base64-encode it, and configure `MES_JWT_KEYS` as `kid:base64key` (comma-separated for multiple keys); set `MES_JWT_KID` to the identifier used to sign new tokens. Do not use the committed test-only key in a deployed service. To rotate, add the new key alongside the old one, make its identifier active, wait at least the 15-minute access-token lifetime, and only then remove the old key. Redis sessions are still checked for every request.

After starting the app, verify the following with a same-origin client. Log in using the bootstrap name and temporary password. The response contains a 15-minute bearer access token and an `HttpOnly; Secure; SameSite=Strict` renewal cookie scoped to `/api/v1/auth`. `GET /api/v1/auth/me` with the bearer token should show `mustChangePassword=true`; unrelated protected routes stay forbidden. `POST /api/v1/auth/change-password` with the bearer token, old password, and a new 12–128-character password revokes that session. A new login should show `mustChangePassword=false`. `POST /api/v1/auth/refresh` must carry the renewal cookie and a matching `Origin` header; its old cookie must fail if replayed. Finally `POST /api/v1/auth/logout` with the current bearer token clears the cookie and invalidates that session. Never paste tokens, cookies, or passwords into shared logs or issue trackers.

If MariaDB is unavailable during login, the API returns a temporary-unavailability error. If Redis is unavailable, new sessions and renewals fail closed, and bearer authentication is rejected. Account disable or role changes are deliberately reflected at the next login, not mid-session; an explicit security response or password change can revoke sessions.
