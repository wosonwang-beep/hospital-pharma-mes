# Hospital Pharmaceutical MES

[![CI](https://github.com/wosonwang-beep/hospital-pharma-mes/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/wosonwang-beep/hospital-pharma-mes/actions/workflows/ci.yml)

Hospital Pharmaceutical MES V2.0 is a modular-monolith foundation for hospital preparation manufacturing execution. This repository implements MES-001 through MES-012 and the first two backend IAM slices: managed employee login, Redis-backed sessions, role-based functional permissions, and administration APIs for current user-role and role-permission assignments.

Frontend IAM screens, complete GMP audit trails and electronic signatures, master-data behavior, workflows, and production execution are intentionally deferred.

## Technology baseline

- Java 21, Maven 3.9+, Spring Boot 3.x
- Spring Security, MyBatis-Plus, MariaDB, Flyway, Redis, MinIO Java SDK, springdoc/OpenAPI
- Vue 3, Vite, TypeScript, Ant Design Vue, Pinia, Vue Router, Axios, ECharts
- Docker Compose for local MariaDB, Redis, and licensed MinIO AIStor

## Quick start

Prerequisites: Java 21, Maven 3.9+, Node.js 22+, npm, and optionally Docker Compose.

1. Copy `.env.example` to `.env`, replace the sample passwords, and obtain the MinIO free-tier license described in `deploy/docker/README.md`.
2. Start persistent DEV MariaDB and Redis with `docker compose up -d --wait mariadb redis`. The default database is `hospital_pharma_mes_dev`; normal `docker compose down` keeps both DEV volumes.
3. Build the backend with `mvn -B -ntp -pl backend/mes-boot -am -DskipTests package`, then start it with `java -jar backend/mes-boot/target/mes-boot-0.1.0-SNAPSHOT.jar --spring.profiles.active=local`.
4. In `frontend/mes-web`, run `npm ci` and `npm run dev`.

Useful endpoints:

- Foundation status: `http://localhost:8080/api/v1/foundation/status`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- Frontend: `http://localhost:5173`

## Verification

Run the full local verification from the repository root:

```powershell
pwsh -File scripts/verify.ps1
```

The script runs backend tests, performs a clean frontend install followed by tests, type checking and production build, and validates the Compose, repository, and CI workflow contracts. It does not start containers; real MariaDB and Redis checks run in GitHub Actions or through the separate local integration command.

GitHub Actions runs four jobs on pull requests and pushes to `main`: backend tests, frontend tests/build/audit, real MariaDB and Redis integration tests, and repository contract checks. The integration job verifies Flyway migrations and MyBatis-Plus against MariaDB, plus a Redis round trip. CI validates the MinIO AIStor configuration contract but does not start the licensed service. See `docs/development/local-development.md` for the integration-test command and environment variables.

More detail is available in `docs/development/local-development.md`, [`docs/development/database-and-validation-strategy.md`](docs/development/database-and-validation-strategy.md), `docs/architecture/module-boundaries.md`, and `docs/api/foundation-api.md`.

## IAM database baseline

The current Flyway baseline creates user-role and role-permission relationships as unique current-state pairs. It does not retain assignment history in those tables. This project has no legacy business data to migrate. Minimal security events remain separate from the relationship tables; they are not a complete GMP audit trail.

IAM administration APIs live under `/api/v1/admin/users`, `/api/v1/admin/roles`, and the read-only `/api/v1/admin/permissions` catalog. Menu permissions allow reads; the corresponding module action permission is also required for mutations. Routine role and permission edits become effective on a user's next login. Password reset explicitly revokes that user's sessions.

## License

Apache License 2.0. MinIO AIStor is a separately licensed runtime dependency; see `deploy/docker/README.md`.
