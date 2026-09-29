# Development Database and Validation Strategy

## 1. Authority and scope

This is the repository-wide default engineering rule for MES-003-R2 through MES-013-R2 and subsequent maintenance. A task-specific rule may be stricter, but may not weaken this strategy without an explicit reviewed change. FINAL FROZEN design, approved Design Changes, task Write Scope, Section 15 dependency/integration contracts, append-only migration rules, GxP controls, and the CI contract remain authoritative.

The objective is `PERSISTENT DEV + ON-DEMAND TEST + TARGETED VALIDATION + KEY INTEGRATION GATES`: keep daily development fast without giving up real migration, transaction, integration, audit, or signature evidence.

## 2. Environment model

| Environment | Purpose | Lifecycle | Data rule |
|---|---|---|---|
| DEV | Daily development, backend/frontend/API/page debugging, manual inspection, current-task functional checks, continuous Flyway upgrades | Persistent | Database `hospital_pharma_mes_dev`; MariaDB and Redis use Docker named volumes |
| TEST | Automated integration, migration, failure, constraint, transaction, concurrency and idempotency tests | Ephemeral per gate | Create → Flyway → Test → Capture Result → Destroy |
| PROD | Future validated production environment | Controlled deployment | Never used as a DEV/TEST default; automated tests never connect to it |

DEV, TEST, and PROD must be physically and configurationally isolated. `application-local.yml` is DEV, `application-ci.yml` is TEST/CI, and `application-prod.yml` is PROD. There is no embedded-database or in-memory Redis fallback.

## 3. Persistent DEV database

DEV uses:

- MariaDB database: `hospital_pharma_mes_dev`
- MariaDB volume: `hospital-pharma-mes-mariadb-dev`
- Redis volume: `hospital-pharma-mes-redis-dev`
- Compose services: `mariadb` and `redis`

DEV is used for routine coding, backend debugging, frontend/API/page integration, manual data checks, current-task functional validation, and continuous Flyway upgrades. It is not deleted when a MES task ends.

Allowed lifecycle:

```text
docker compose up -d mariadb redis
docker compose down
docker compose up -d mariadb redis
```

`docker compose down` stops and replaces containers while preserving named volumes. The following operations are forbidden unless the user explicitly requests the exact intent `RESET DEVELOPMENT DATABASE`:

- `docker compose down -v`;
- deleting either DEV named volume;
- dropping or recreating `hospital_pharma_mes_dev`;
- erasing its Flyway history.

An explicitly authorized reset is a destructive action: resolve and report the exact volume/database targets before execution.

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

If a database contains the same physical version with a different description/checksum, stop migration of that database. Do not repair or overwrite it. Preserve it for investigation and provision an isolated compatible database/volume unless destructive recovery is explicitly authorized.

## 5. Ephemeral TEST rule

There is no fixed TEST database requiring manual upkeep. A test that requires MariaDB or Redis uses task-scoped infrastructure that is automatically created and destroyed. CI service containers already implement this model. A local task may use equivalent task-scoped Docker containers or future Testcontainers support, but must preserve the same isolation and cleanup contract.

TEST may be cleared and may contain rollback, abnormal, concurrent, constraint-violation, transaction-failure, and migration scenarios. Automated tests must not connect to `hospital_pharma_mes_dev`, except for a test explicitly named and documented as a DEV smoke test. Such a smoke test must be non-destructive and is never a substitute for isolated TEST evidence.

## 6. Default development loop

```text
Code
→ Compile / Type Check
→ Unit Test
→ Targeted Test
→ Continue
```

Do not perform `Create TEST DB → V001..latest → Full Integration → Destroy` after every changed file. Start real TEST infrastructure only when the behavior genuinely depends on MariaDB, Redis, Flyway, repository SQL, transactions, database constraints, concurrency, or idempotency.

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

Run the main ephemeral MariaDB/Redis integration gate once when ordinary task implementation is substantially complete. For a failure:

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

When provisioning or validating DEV, record the database name and service status, exact MariaDB/Redis named volume names, successful Flyway versions/descriptions, application connectivity, and a restart test. The restart test writes non-sensitive schema and Redis markers, runs `docker compose down` without `-v`, restarts, and verifies both markers persist.

Never place passwords, tokens, credentials, or full successful build logs in the evidence record.
