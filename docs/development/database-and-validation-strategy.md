# Development Database and Validation Strategy

## 1. Authority and scope

This is the repository-wide default engineering rule for MES-003-R2 through MES-013-R2 and subsequent maintenance. A task-specific rule may be stricter, but may not weaken this strategy without an explicit reviewed change. FINAL FROZEN design, approved Design Changes, task Write Scope, Section 15 dependency/integration contracts, append-only migration rules, GxP controls, and the CI contract remain authoritative.

The objective is `PERSISTENT DEV + ON-DEMAND TEST + TARGETED VALIDATION + KEY INTEGRATION GATES`: keep daily development fast without giving up real migration, transaction, integration, audit, or signature evidence.

## 2. Environment model

The explicit 2026-10-08 user decision supersedes any older local isolation/staging practices. Edit, build, run and test in the same `D:\codex\_project\gmp\hospital-pharma-mes` checkout on `main` without copied or shadow workspaces. The single local frontend is `127.0.0.1:5173` and backend is `127.0.0.1:8080`; do not silently use a second port. Local development and database tests share the native Windows MariaDB `hospital_pharma_mes_dev` at `localhost:3306` and Redis `localhost:6379`. All project tables and Flyway history belong to this database. Do not create another local TEST database or start a MariaDB/Redis container for this project.

`application-local.yml` is the single local runtime configuration. The test `application-ci.yml` imports it directly; both resolve the same ignored root `.env` from `D:\codex\_project\gmp\hospital-pharma-mes`. Explicit environment/command-line overrides must not redirect local tests to a different database or Redis. Hosted CI injects its own credentials because GitHub runners cannot connect to the local workstation.

Hosted GitHub CI keeps its existing service-container configuration because it cannot reach this workstation's localhost. PROD is never a development or test target. There is no embedded-database or in-memory Redis fallback.

## 3. Persistent local database

- Native Windows service: `MariaDB`.
- Host and port: `localhost:3306`.
- Database: `hospital_pharma_mes_dev`.
- Application account: `mes`; administrative credentials are used only for provisioning.
- Credentials: ignored root `.env`; never commit passwords.
- Local development and database tests share these tables. No alternate local database is provisioned.

Preserve existing data and Flyway history. Never drop, recreate, clear, reset, or repair this database without the user's explicit authorization. Existing Compose files remain available for hosted/optional infrastructure, but their MariaDB service is not the local project database.
## 4. DEV migration rule

DEV follows the repository's real physical Flyway chain from V001 through the current version. For every new task:

1. Query the target DEV database's `flyway_schema_history`.
2. Identify the highest successful physical version.
3. Allocate the next physical version after it.
4. Add a new migration; never rewrite history.

Section 15 identifiers such as V001, V002, or V007A are **Logical Migration Groups**, not physical Flyway versions. `Logical Migration Group != Physical Flyway Version` is a frozen rule.

Forbidden:

- editing an executed migration;
- renaming or reordering an old migration;
- altering a historical checksum;
- using `flyway repair` to hide a real mismatch;
- manually creating or altering formal business tables outside a new physical Flyway migration.

If a database contains the same physical version with a different description/checksum, stop migration of that database. Do not repair or overwrite it. Preserve it for investigation and request an approved append-only migration/source recovery plan. Do not silently create an isolated local database/volume.

## 5. Local test rule

Local database tests connect to the same `hospital_pharma_mes_dev` database using the root `.env`. Do not create a second local test database, use Testcontainers for MariaDB, clear tables, reset migrations, or run Flyway clean.

Use transactions with rollback or uniquely identified test records. Cleanup must target only records created by that test. Tests that modify shared seed roles, permissions, or existing users must be reviewed and made safe before running against this persistent database. Read-only checks are preferred for schema/history verification. Tests never connect to PROD.

Hosted CI retains its existing isolated service containers; this is not an additional database on the user's workstation.
## 6. Default development loop

```text
Code
→ Compile / Type Check
→ Unit Test
→ Targeted Test
→ Continue
```

Use the shared local database for targeted tests when behavior depends on MariaDB, Flyway, repository SQL, transactions, constraints, concurrency, or idempotency. Preserve data and migration history between tasks. Run Redis-dependent checks only when Redis is available.

## 7. Fast Task Validation

An ordinary MES task blocks completion on:

1. Backend build PASS.
2. Frontend build/typecheck PASS when frontend is affected.
3. The current task's required test cases PASS.
4. The current task's targeted integration tests PASS.
5. Direct consumed/produced integration contracts PASS.
6. Affected regression PASS.
7. Code review has `CRITICAL = 0`.
8. Code review has `HIGH = 0`.

Run the relevant safe MariaDB/Redis integration gate once when ordinary task implementation is substantially complete. For a failure:

```text
FAIL
→ Root Cause
→ Minimal Fix
→ Targeted Retest
→ Affected Regression
```

Do not default to a full-system rerun for one failure.

## 8. Key integration gates

The following tasks receive an enhanced gate: clean migration, upgrade migration, cross-module integration, contract regression, and affected regression.

| Gate | Required chain |
|---|---|
| MES-006-R2 | Material → BOM → Process / Route |
| MES-009-R2 | Process + eBR + WMS → MainBatch / ExecutionUnit |
| MES-011-R2 | Material + BOM + WMS + Batch + Operation → Weighing + MaterialCharge + QuantityEvent + InventoryLedger + Genealogy |
| MES-013-R2 | eBR + QC + Deviation + Material Balance + Signature → QA Release |

Outside these gates, ordinary tasks must not repeatedly run system-wide full regression.

## 9. Final System Validation

After MES-013, perform one Final System Validation containing clean and upgrade migrations; full backend, frontend, integration, and regression suites; security and permission matrix; concurrency and idempotency; audit and electronic signature; eBR, WMS, genealogy, material balance, and QA release; full E2E and UI Prototype conformance; RTM traceability and cross-module integration; and final code review.

This intensity must not be copied into each ordinary MES task.

## 10. Test logs and token efficiency

For successful tests, retain and report only suite name, passed/failed/skipped counts, duration, and relevant physical migration version. Do not send or re-analyze complete successful logs by default.

When a test fails, inspect the smallest sufficient context: the failed test, exception, root-cause neighborhood, relevant SQL, and relevant stack trace. After repair, prefer targeted retest plus affected regression.

```text
Backend Unit: 28/28 PASS
Integration: 12/12 PASS
Migration: 2/2 PASS (Physical V008)
Frontend: 15/15 PASS
```

## 11. Technical debt

Record MEDIUM/LOW findings under Technical Debt / Follow-up when they do not affect the current requirement, data integrity, security, required tests, integration contracts, migrations, or critical business rules. They do not block the current task. CRITICAL/HIGH findings must be fixed before readiness or acceptance.

## 12. Autonomous engineering

Codex resolves ordinary compile/type/test failures, Spring bean wiring, MariaDB/Redis compatibility, Flyway SQL syntax, frontend builds, Testcontainers, Docker development configuration, and in-scope refactoring without repeated user confirmation.

Stop only for:

- `DESIGN CHANGE REQUIRED`: a real conflict in FROZEN business/database/API/state-machine/integration design;
- `USER DECISION REQUIRED`: multiple reasonable business choices require Product/System Owner selection;
- `DESTRUCTIVE ACTION REQUIRED`: irreversible data deletion, persistent DEV rebuild, or large-scale removal is required.

## 13. Scope control

The current task writes only its current Write Scope. Read/Integration Scope allows reading and testing dependencies, not implementing them. Prohibited work includes unrelated refactors, speculative features, future-task implementation, temporary business models, duplicate platform infrastructure, and redesign of frozen contracts.

## 14. Existing CI relationship

The GitHub Actions MariaDB/Redis service containers are ephemeral TEST infrastructure and remain the merge CI contract. Applicable CI jobs still must pass before merge. Task-time targeted validation reduces repeated local work; it does not authorize bypassing or weakening CI assertions. Changes to CI coverage require an explicit reviewed CI-contract change.

## 15. Operational evidence

When validating local DEV, record the existing native MariaDB database, Redis and application service status, highest successful Flyway version, current-source build/launch identity, and application connectivity on the single approved ports. Do not create container volumes, run Docker Compose for this local verification, or stop/restart persistent services solely to prove persistence. Only perform a restart check when required and safe for active work; preserve existing records and migrations.

Never place passwords, tokens, credentials, or full successful build logs in the evidence record.
