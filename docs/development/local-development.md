# Local development

## Prerequisites

Install Java 21, Maven 3.9 or newer, Node.js 22 or newer, npm, and PowerShell 7. Local MariaDB is the native Windows service on port 3306. Docker is not required for this database.

## Environment

Use `D:\codex\_project\gmp\hospital-pharma-mes` as the only local project directory. Keep credentials in the ignored root `.env`. Both local development and local database tests use `jdbc:mariadb://localhost:3306/hospital_pharma_mes_dev`; the application account is `mes`. All project tables and Flyway history belong to this database. The local and ci profiles import `.env` when run from the project root. Environment variables or explicit command-line overrides can take precedence; do not override the local database target. See [Development Database and Validation Strategy](database-and-validation-strategy.md).

The maintained MinIO AIStor image requires a free-tier or commercial license file. Follow `deploy/docker/README.md`; never commit that file.

## Commands

- Backend tests: `mvn -B -ntp test`
- Frontend: run `npm ci`, `npm test -- --run`, `npm run typecheck`, and `npm run build` in `frontend/mes-web`
- Compose contract: `pwsh -File scripts/verify-compose.ps1`
- Repository and CI contracts: `pwsh -File scripts/verify-repository.ps1` and `pwsh -File scripts/verify-ci.ps1`
- Complete verification: `pwsh -File scripts/verify.ps1`

Use the running native `MariaDB` Windows service on port 3306. Do not start a second MariaDB container or create another local development/test database. Redis and object storage are separate application dependencies; they must be configured when the selected workflow needs them. Never clear or rebuild the project database without explicit user authorization.

To run the backend, activate the `local` profile. There is deliberately no default profile and no embedded database fallback.

The one-time first-administrator procedure and temporary-password handling are in [IAM bootstrap](iam-bootstrap.md).

## Infrastructure integration tests

Local database integration tests use the same `hospital_pharma_mes_dev` database and the credentials in root `.env`. Test records must be uniquely identified and rolled back or cleaned up without touching existing records. Review tests that modify shared seed roles or permissions before running them on this persistent database. Do not run database reset/clean operations or introduce another local TEST database. Redis-dependent tests additionally require the configured Redis service. A full `mvn -B -ntp -Pci-integration verify` remains a key-gate/final-validation command rather than the default inner development loop.

GitHub Actions supplies temporary credentials and service containers to run this suite on every pull request and `main` push. Its other jobs test the backend, frontend, and repository contracts independently. Licensed MinIO AIStor is checked for configuration only. If local MariaDB and Redis are unavailable, the real integration tests remain unverified until the GitHub workflow runs. The local verification script checks Compose configuration but does not start containers.
