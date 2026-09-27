# Hospital Pharmaceutical MES

Hospital Pharmaceutical MES V2.0 is a modular-monolith foundation for hospital preparation manufacturing execution. This repository currently implements MES-001 through MES-011 only: project structure, application bootstrap, database migration, Redis and object-storage boundaries, API conventions, trace IDs, OpenAPI, security extension points, a Vue shell, and local infrastructure.

IAM/RBAC, audit trails and electronic signatures, master-data behavior, workflows, and production execution are intentionally deferred.

## Technology baseline

- Java 21, Maven 3.9+, Spring Boot 3.x
- Spring Security, MyBatis-Plus, MariaDB, Flyway, Redis, MinIO Java SDK, springdoc/OpenAPI
- Vue 3, Vite, TypeScript, Ant Design Vue, Pinia, Vue Router, Axios, ECharts
- Docker Compose for local MariaDB, Redis, and licensed MinIO AIStor

## Quick start

Prerequisites: Java 21, Maven 3.9+, Node.js 22+, npm, and optionally Docker Compose.

1. Copy `.env.example` to `.env`, replace the sample passwords, and obtain the MinIO free-tier license described in `deploy/docker/README.md`.
2. Start infrastructure with `docker compose up -d --wait` when Docker is available.
3. Start the backend with `mvn -B -ntp -pl backend/mes-boot -am spring-boot:run -Dspring-boot.run.profiles=local`.
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

The script runs backend tests, performs a clean frontend install followed by tests, type checking and production build, and validates the Compose contract. Docker-dependent checks are reported as unavailable when Docker is not installed.

More detail is available in `docs/development/local-development.md`, `docs/architecture/module-boundaries.md`, and `docs/api/foundation-api.md`.

## License

Apache License 2.0. MinIO AIStor is a separately licensed runtime dependency; see `deploy/docker/README.md`.
