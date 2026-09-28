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
