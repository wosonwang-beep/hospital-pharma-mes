# Local development

## Prerequisites

Install Java 21, Maven 3.9 or newer, Node.js 22 or newer, npm, and PowerShell 7. Docker Compose is optional for source verification but required to run MariaDB, Redis, and MinIO locally.

## Environment

Copy `.env.example` to `.env` and replace every sample password. `.env` and `deploy/docker/minio.license` are ignored by Git. The local profile contains convenience defaults; the production profile requires environment-backed database, Redis, and object-storage configuration.

The maintained MinIO AIStor image requires a free-tier or commercial license file. Follow `deploy/docker/README.md`; never commit that file.

## Commands

- Backend tests: `mvn -B -ntp test`
- Frontend: run `npm ci`, `npm test -- --run`, `npm run typecheck`, and `npm run build` in `frontend/mes-web`
- Browser smoke test: run `npx playwright install chromium` once, then `npm run test:e2e` in `frontend/mes-web`. The default IAM smoke test starts Vite and uses simulated IAM API responses; it does not change MariaDB data. `live-unauth.spec.ts` checks the real backend proxy and 401 handling when a local backend is running (`MES_E2E_LIVE_BACKEND=true`). `live-iam.spec.ts` is opt-in: on a freshly bootstrapped test database, set `MES_E2E_ADMIN_LOGIN`, `MES_E2E_ADMIN_PASSWORD`, and `MES_E2E_NEW_PASSWORD`, then run only that spec. It changes the administrator password and requires the real backend plus MariaDB and Redis. Never run it against an account whose password must remain unchanged or commit these secrets.
- Compose contract: `pwsh -File scripts/verify-compose.ps1`
- Repository and CI contracts: `pwsh -File scripts/verify-repository.ps1` and `pwsh -File scripts/verify-ci.ps1`
- Complete verification: `pwsh -File scripts/verify.ps1`

To run the backend, activate the `local` profile. There is deliberately no default profile and no embedded database fallback.

The one-time first-administrator procedure and temporary-password handling are in [IAM bootstrap](iam-bootstrap.md).

## Infrastructure integration tests

Start MariaDB and Redis, then set `MES_DB_URL`, `MES_DB_USERNAME`, `MES_DB_PASSWORD`, `MES_REDIS_HOST`, `MES_REDIS_PORT`, and `MES_REDIS_PASSWORD` in the process environment. Point `MES_DB_URL` at a fresh, disposable test database, not the bootstrapped development database: the last-administrator safety tests require a clean IAM user set. Run `mvn -B -ntp -Pci-integration verify` from the repository root. The `ci` profile requires these values and fails when either service is absent; it does not substitute an embedded database or in-memory Redis. Most database tests roll back, but some transactional-boundary cases commit and clean up their own test rows. The Redis test deletes its namespaced key.

GitHub Actions supplies temporary credentials and service containers to run this suite on every pull request and `main` push. Its other jobs test the backend, frontend, and repository contracts independently. Licensed MinIO AIStor is checked for configuration only. If local MariaDB and Redis are unavailable, the real integration tests remain unverified until the GitHub workflow runs. The local verification script checks Compose configuration but does not start containers.
