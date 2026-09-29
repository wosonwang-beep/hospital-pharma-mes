# Local development

## Prerequisites

Install Java 21, Maven 3.9 or newer, Node.js 22 or newer, npm, and PowerShell 7. Docker Compose is optional for source verification but required to run MariaDB, Redis, and MinIO locally.

## Environment

Copy `.env.example` to `.env` and replace every sample password. `.env` and `deploy/docker/minio.license` are ignored by Git. The local profile targets the persistent `hospital_pharma_mes_dev` database and persistent Redis instance. The production profile requires environment-backed database, Redis, and object-storage configuration. See [Development Database and Validation Strategy](database-and-validation-strategy.md) for mandatory DEV/TEST/PROD isolation and validation gates.

The maintained MinIO AIStor image requires a free-tier or commercial license file. Follow `deploy/docker/README.md`; never commit that file.

## Commands

- Backend tests: `mvn -B -ntp test`
- Frontend: run `npm ci`, `npm test -- --run`, `npm run typecheck`, and `npm run build` in `frontend/mes-web`
- Compose contract: `pwsh -File scripts/verify-compose.ps1`
- Repository and CI contracts: `pwsh -File scripts/verify-repository.ps1` and `pwsh -File scripts/verify-ci.ps1`
- Complete verification: `pwsh -File scripts/verify.ps1`

Start persistent DEV infrastructure with `docker compose up -d --wait mariadb redis`. Stop it with `docker compose down`; this retains the named volumes. Never use `docker compose down -v` unless the user explicitly requests `RESET DEVELOPMENT DATABASE`.

To run the backend, activate the `local` profile. There is deliberately no default profile and no embedded database fallback.

The one-time first-administrator procedure and temporary-password handling are in [IAM bootstrap](iam-bootstrap.md).

## Infrastructure integration tests

Integration tests use ephemeral MariaDB and Redis, not the persistent DEV services. Set `MES_DB_URL`, `MES_DB_USERNAME`, `MES_DB_PASSWORD`, `MES_REDIS_HOST`, `MES_REDIS_PORT`, and `MES_REDIS_PASSWORD` to task-scoped TEST containers, then run only the targeted integration selection required by the current gate. The `ci` profile requires these values and fails when either service is absent; it does not substitute an embedded database or in-memory Redis. A full `mvn -B -ntp -Pci-integration verify` remains a CI/key-gate/final-validation command rather than the default inner development loop.

GitHub Actions supplies temporary credentials and service containers to run this suite on every pull request and `main` push. Its other jobs test the backend, frontend, and repository contracts independently. Licensed MinIO AIStor is checked for configuration only. If local MariaDB and Redis are unavailable, the real integration tests remain unverified until the GitHub workflow runs. The local verification script checks Compose configuration but does not start containers.
