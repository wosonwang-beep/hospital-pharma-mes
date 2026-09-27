# MES-012 Continuous Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a read-only GitHub Actions pipeline that reproducibly validates the backend, frontend, real MariaDB/Redis integrations, and repository contracts.

**Architecture:** One workflow runs four independently diagnosable jobs on pull requests, pushes to `main`, and manual dispatch. Maven Failsafe isolates real-infrastructure tests from the normal reactor suite; PowerShell contract scripts keep repository-policy checks reusable locally and in GitHub Actions.

**Tech Stack:** GitHub Actions, Java 21, Maven 3.9+, Spring Boot 3.5.x, JUnit 5, MyBatis-Plus, Flyway, MariaDB 11.8, Redis 8.2, Node.js 22, npm, Vitest, Vite, PowerShell 7.

**Spec:** `docs/superpowers/specs/2026-09-27-mes-012-ci-design.md`

## Global Constraints

- Keep Java on 21 and Spring Boot on the existing 3.x line.
- Use MariaDB-compatible SQL and real MariaDB for persistence integration tests; do not add H2.
- Use real Redis for Redis integration tests; do not add an in-memory substitute.
- Keep workflow permissions read-only and do not publish, deploy, or modify repository contents.
- Do not place real secrets or the MinIO/AIStor license in the repository or workflow.
- Pin container images and third-party GitHub Actions; full commit SHAs are required for actions, with the human-readable release in a comment.
- Keep normal backend tests independent of external services; run infrastructure tests only through the dedicated Maven profile.
- Stay within MES-012 and do not implement IAM/RBAC, audit, master data, or production behavior.

## Review Focus

- A missing MariaDB or Redis service must fail integration verification instead of activating a substitute; covered by Task 1 profile and context assertions.
- Re-running integration tests must not fail because data from an earlier run remains; covered by Task 1 database rollback and Redis cleanup assertions.
- Pull requests from forks must not gain write permissions or require secrets; covered by Task 3 workflow-contract assertions.
- A superseded run for the same branch must be cancelled without cancelling a different branch; covered by Task 3 concurrency-group assertions.
- CI must not require an AIStor license merely to validate repository contracts; covered by Task 2 and Task 3 contract-job assertions.

---

### Task 1: Real MariaDB and Redis integration-test boundary

**Files:**
- Modify: `pom.xml`
- Modify: `backend/mes-boot/pom.xml`
- Create: `backend/mes-boot/src/main/resources/application-ci.yml`
- Create: `backend/mes-boot/src/test/java/com/hospital/mes/foundation/integration/FoundationInfrastructureIT.java`

**Interfaces:**
- Consumes: environment variables `MES_DB_URL`, `MES_DB_USERNAME`, `MES_DB_PASSWORD`, `MES_REDIS_HOST`, `MES_REDIS_PORT`, and `MES_REDIS_PASSWORD`.
- Produces: Maven profile `ci-integration`; Failsafe-selected `*IT` tests executed by `mvn -B -ntp -Pci-integration verify`.

- [ ] **Step 1: Write the failing infrastructure integration test**

Create `FoundationInfrastructureIT` with `@SpringBootTest`, `@ActiveProfiles("ci")`, and tests named `flywayMigratesFoundationTable`, `mybatisPlusWritesAndReadsProbe`, and `redisWritesReadsAndCleansNamespacedValue`. Assert the Flyway-created `sys_foundation_probe` table exists, mapper insert/select returns the generated row, and `StringRedisTemplate` round-trips a key under `mes:ci:` and removes it in cleanup.

- [ ] **Step 2: Run the intended profile and verify the test cannot yet be selected/configured**

Run: `mvn -B -ntp -Pci-integration verify`

Expected: FAIL because the profile or CI runtime configuration is not defined, or because the required services are unavailable; it must not report the new integration tests as a successful substituted run.

- [ ] **Step 3: Add the dedicated Maven integration-test profile**

Pin `maven-failsafe-plugin` alongside Surefire in the root build. In `backend/mes-boot/pom.xml`, define `ci-integration` so Failsafe runs `**/*IT.java` during `integration-test` and `verify`, while Surefire's normal `test` phase continues to exclude `*IT` tests.

- [ ] **Step 4: Add strict CI runtime configuration**

Create `application-ci.yml` with MariaDB and password-protected Redis properties sourced from the six required environment variables. Do not provide credential defaults or an alternate datastore. Give test-only MinIO client properties inert non-secret values so the licensed service is not contacted during these tests.

- [ ] **Step 5: Prove normal tests remain infrastructure-independent**

Run: `mvn -B -ntp test`

Expected: PASS with no MariaDB or Redis process required and with `FoundationInfrastructureIT` absent from the executed test set.

- [ ] **Step 6: Prove integration tests against local services when available**

Run with the six environment variables pointing to MariaDB and Redis: `mvn -B -ntp -Pci-integration verify`

Expected: PASS; logs show Flyway migration and all three `FoundationInfrastructureIT` tests. If local Docker remains unavailable, record this check as pending for the GitHub workflow run rather than substituting a datastore.

- [ ] **Step 7: Commit the integration-test boundary**

```text
git add pom.xml backend/mes-boot/pom.xml backend/mes-boot/src/main/resources/application-ci.yml backend/mes-boot/src/test/java/com/hospital/mes/foundation/integration/FoundationInfrastructureIT.java
git commit -m "test: add Foundation infrastructure integration suite"
```

### Task 2: Reusable repository contract verification

**Files:**
- Create: `scripts/verify-repository.ps1`
- Modify: `scripts/verify.ps1`
- Test: `scripts/verify-repository.ps1`

**Interfaces:**
- Consumes: Git index, `.gitignore`, `.env.example`, `docker-compose.yml`, and required repository directories.
- Produces: zero exit code with `Repository contract: PASS`, or a non-zero exit code naming the violated contract.

- [ ] **Step 1: Define and observe the missing contract check**

Run: `pwsh -NoProfile -File scripts/verify-repository.ps1`

Expected: FAIL because the script does not exist.

- [ ] **Step 2: Implement the repository contract script**

Verify required root/backend/frontend/database/docs/deploy/scripts paths, required ignore entries, and absence from the Git index of `.env`, `deploy/docker/minio.license`, `target`, `node_modules`, `dist`, `*.tsbuildinfo`, and generated Vite configuration output. Reuse `scripts/verify-compose.ps1` for Compose and AIStor configuration checks; do not start services or require a license file.

- [ ] **Step 3: Add the repository contract to unified local verification**

Update `scripts/verify.ps1` to invoke `verify-repository.ps1` after frontend verification. Avoid invoking `verify-compose.ps1` twice after the new script owns that call.

- [ ] **Step 4: Run contract and unified verification**

Run: `pwsh -NoProfile -File scripts/verify-repository.ps1`

Expected: PASS and no MinIO license requirement.

Run: `pwsh -NoProfile -File scripts/verify.ps1`

Expected: backend and frontend checks PASS; repository contract PASS; Docker runtime may be reported unavailable on this host.

- [ ] **Step 5: Commit repository verification**

```text
git add scripts/verify-repository.ps1 scripts/verify.ps1
git commit -m "build: add reusable repository contract checks"
```

### Task 3: Four-job GitHub Actions workflow

**Files:**
- Create: `.github/workflows/ci.yml`
- Create: `scripts/verify-ci.ps1`

**Interfaces:**
- Consumes: Maven reactor, `frontend/mes-web/package-lock.json`, `ci-integration` profile, and repository contract script.
- Produces: GitHub jobs `backend`, `frontend`, `infrastructure-integration`, and `repository-contract`.

- [ ] **Step 1: Write a failing workflow contract script**

Create `scripts/verify-ci.ps1` that asserts the workflow file exists and contains triggers for pull requests, `main` pushes, and manual dispatch; top-level read-only contents permission; branch-aware concurrency with cancellation; exactly the four expected job identifiers; Java 21, Node.js 22, MariaDB and Redis services; the `ci-integration` Maven command; frontend install/test/typecheck/build/audit commands; repository-contract invocation; and no MinIO service or secret reference.

- [ ] **Step 2: Run the workflow contract and verify it fails**

Run: `pwsh -NoProfile -File scripts/verify-ci.ps1`

Expected: FAIL because `.github/workflows/ci.yml` is missing.

- [ ] **Step 3: Implement the workflow shell and security policy**

Create `ci.yml` with `pull_request`, `push` limited to `main`, and `workflow_dispatch`; top-level `permissions: contents: read`; and concurrency grouped by workflow plus pull-request head reference or Git reference, with `cancel-in-progress: true`. Pin each official action to a full commit SHA and document its supported release in a comment.

- [ ] **Step 4: Implement backend and frontend jobs**

The backend job runs `mvn -B -ntp test` on Java 21 with Maven dependency caching. The frontend job uses Node.js 22 and npm caching keyed by `frontend/mes-web/package-lock.json`, then runs clean install, non-watch tests, type checking, production build, and `npm audit --omit=dev --audit-level=high`.

- [ ] **Step 5: Implement infrastructure integration job**

Add pinned MariaDB `11.8.9-noble` and Redis `8.2.9-bookworm` service containers with health checks and temporary job-local credentials. Export the six environment values expected by `application-ci.yml`, then run `mvn -B -ntp -Pci-integration verify`. Do not define a MinIO service or reference repository secrets.

- [ ] **Step 6: Implement repository-contract job and failure diagnostics**

Run `scripts/verify-repository.ps1` under PowerShell 7. Upload Maven test reports only on failure, with a short retention period, using a commit-SHA-pinned artifact action. Do not upload frontend build output or database dumps.

- [ ] **Step 7: Validate workflow contracts and syntax**

Run: `pwsh -NoProfile -File scripts/verify-ci.ps1`

Expected: PASS.

Run an available GitHub Actions YAML validator (prefer `actionlint`) against `.github/workflows/ci.yml`.

Expected: no syntax, expression, event, or shell errors.

- [ ] **Step 8: Commit the workflow**

```text
git add .github/workflows/ci.yml scripts/verify-ci.ps1
git commit -m "ci: validate Foundation on GitHub Actions"
```

### Task 4: CI policy and developer documentation

**Files:**
- Modify: `README.md`
- Modify: `AGENTS.md`
- Modify: `docs/development/local-development.md`
- Modify: `scripts/verify.ps1`

**Interfaces:**
- Consumes: completed workflow and verification commands.
- Produces: accurate contributor guidance and a `main` CI status badge.

- [ ] **Step 1: Add the CI contract check to unified verification**

Update `scripts/verify.ps1` so the reusable local run also executes `scripts/verify-ci.ps1`.

- [ ] **Step 2: Document the public CI behavior**

Add the `main` workflow status badge to `README.md`. Describe the four jobs, explain that GitHub validates MariaDB/Redis while licensed AIStor remains a configuration-only CI check, and retain accurate local Docker limitations.

- [ ] **Step 3: Freeze the merge rule**

Add an `AGENTS.md` rule requiring applicable CI jobs to pass before merge. State that bypassing a failing job or weakening it to make a change pass is prohibited without an explicit reviewed change to the CI contract.

- [ ] **Step 4: Update local-development guidance**

Document normal verification, integration-profile environment variables and command, the absence of datastore fallbacks, and how GitHub Actions supplies temporary service credentials.

- [ ] **Step 5: Run complete local verification**

Run: `pwsh -NoProfile -File scripts/verify.ps1`

Expected: backend, frontend, repository, Compose, and CI contract checks PASS; Docker runtime may remain explicitly unavailable.

- [ ] **Step 6: Commit the documentation and unified check**

```text
git add README.md AGENTS.md docs/development/local-development.md scripts/verify.ps1
git commit -m "docs: document MES-012 CI policy"
```

### Task 5: Publish and verify the real GitHub run

**Files:**
- Verify only: `.github/workflows/ci.yml`
- Verify only: GitHub Actions run for the implementation branch or pull request

**Interfaces:**
- Consumes: pushed implementation commits.
- Produces: one successful public GitHub Actions run with all four jobs green.

- [ ] **Step 1: Recheck branch and local state**

Run: `git status --short --branch` and `git log --oneline main..HEAD`.

Expected: clean implementation branch containing only the reviewed MES-012 commits beyond `main`.

- [ ] **Step 2: Push the implementation branch**

Push the current MES-012 branch to `origin` without force. Do not modify branch protection or repository permissions as part of this task.

- [ ] **Step 3: Observe the triggered workflow**

Open or query the public Actions run and wait for completion.

Expected: `backend`, `frontend`, `infrastructure-integration`, and `repository-contract` all succeed. In particular, the integration job must show real MariaDB and Redis service health plus passing `FoundationInfrastructureIT` tests.

- [ ] **Step 4: Diagnose any CI-only failure before changing code**

If a job fails, preserve its logs, identify the failing layer, reproduce locally where possible, and make the smallest test-backed correction. Do not weaken assertions, disable a job, add secrets, or introduce a datastore substitute.

- [ ] **Step 5: Run final local verification after any correction**

Run: `pwsh -NoProfile -File scripts/verify.ps1`

Expected: PASS with the same explicit Docker availability report as before.

- [ ] **Step 6: Record completion**

Confirm the successful workflow URL and exact commit SHA in the final handoff. No further code commit is required unless CI diagnosis produced a correction.
