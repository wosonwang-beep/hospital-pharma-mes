# Local development

## Prerequisites

Install Java 21, Maven 3.9 or newer, Node.js 22 or newer, npm, and PowerShell 7. Docker Compose is optional for source verification but required to run MariaDB, Redis, and MinIO locally.

## Environment

Copy `.env.example` to `.env` and replace every sample password. `.env` and `deploy/docker/minio.license` are ignored by Git. The local profile contains convenience defaults; the production profile requires environment-backed database, Redis, and object-storage configuration.

The maintained MinIO AIStor image requires a free-tier or commercial license file. Follow `deploy/docker/README.md`; never commit that file.

## Commands

- Backend tests: `mvn -B -ntp test`
- Frontend: run `npm ci`, `npm test -- --run`, `npm run typecheck`, and `npm run build` in `frontend/mes-web`
- Compose contract: `pwsh -File scripts/verify-compose.ps1`
- Complete verification: `pwsh -File scripts/verify.ps1`

To run the backend, activate the `local` profile. There is deliberately no default profile and no embedded database fallback. When Docker is unavailable, the MariaDB integration path and container health checks remain unverified; the verification script reports that status explicitly.
