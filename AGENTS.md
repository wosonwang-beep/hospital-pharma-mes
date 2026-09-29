# Hospital Pharmaceutical MES V2.0 Development Rules

1. Use Java 21 only and remain on Spring Boot 3.x.
2. Write MariaDB-compatible SQL only.
3. Use MyBatis-Plus for ordinary CRUD. Explicit SQL/XML is allowed for traceability, lineage, eBR aggregation, and reporting.
4. Never introduce Maven dependency cycles.
5. Model business state changes with commands and domain methods; never expose a generic updateStatus API.
6. Keep API response wrappers in the API layer; domain services must not return them.
7. Persistence entities and domain models may be separate types.
8. Never physically delete production or regulated records.
9. Use optimistic locking for mutable regulated aggregates.
10. Design critical operations for audit events and electronic signatures from the beginning.
11. Treat shared Flyway migrations as append-only; fix history with new migrations.
12. Do not make the core production state machine depend on BPMN or workflow runtime availability.
13. Dynamic form expressions must not execute arbitrary JavaScript, SQL, or SpEL.
14. RELEASED means finished-product release, not production completion.
15. Foundation work is limited to MES-001 through MES-011; MES-012 adds CI only. Do not pre-implement later business epics.
16. Applicable CI jobs must pass before merging. Do not bypass a failing job or weaken its assertions without an explicit, reviewed change to the CI contract.

Business modules use `api`, `application`, `domain`, and `infrastructure` boundaries. Cross-module notifications use domain events and complex cross-module reads use query services.

## Development Database & Validation Strategy

The repository-wide default engineering strategy is defined in [`docs/development/database-and-validation-strategy.md`](docs/development/database-and-validation-strategy.md). It applies to MES-003-R2 through MES-013-R2 and all later maintenance unless a task-approved rule is stricter. It does not weaken FINAL FROZEN design, task scope, migration, design-change, GxP, or CI rules.

1. DEV is persistent: use database `hospital_pharma_mes_dev`, persistent Docker named volumes for MariaDB and Redis, and continuously upgrade it through real physical Flyway migrations. Normal `docker compose down` must retain volumes. Never run `docker compose down -v` or otherwise reset DEV unless the user explicitly requests `RESET DEVELOPMENT DATABASE`.
2. Physical Flyway versions continue from the highest successful version in the target database's `flyway_schema_history`. Section 15 numbers are logical migration groups, not physical versions. Never modify, rename, reorder, repair, or checksum-change an executed migration, and never create formal business tables manually outside Flyway.
3. TEST is ephemeral and physically/configurationally isolated from DEV: create task-scoped MariaDB and Redis, migrate, test, capture the result, and destroy them. Automated tests must not connect to `hospital_pharma_mes_dev` unless explicitly designated as a DEV smoke test, and must never connect to PROD.
4. The normal loop is Code → Compile/Type Check → Unit Test → Targeted Test → Continue. Start a real TEST environment only for MariaDB, Redis, Flyway, repository SQL, transaction, constraint, concurrency, or idempotency behavior. Do not recreate it after every file change.
5. Ordinary MES tasks use Fast Task Validation: backend build; affected frontend build/typecheck; required task cases; targeted integration; direct integration contracts; affected regression; `CRITICAL = 0`; `HIGH = 0`. Run the main ephemeral database gate once near task completion, then diagnose failures with minimal logs, make the smallest fix, run a targeted retest, and run affected regression.
6. MES-006-R2, MES-009-R2, MES-011-R2, and MES-013-R2 are key integration gates and add clean/upgrade migration, cross-module integration, contract regression, and affected regression for the module chains defined in the detailed strategy. Do not copy system-wide full regression into ordinary tasks.
7. After MES-013, run one Final System Validation covering clean and upgrade migration, full backend/frontend/integration/regression/security/GxP/E2E/UI/RTM/cross-module review as defined in the detailed strategy.
8. On PASS, retain only suite counts, failures, skips, duration, and relevant migration version. On FAIL, inspect only the failed test, exception/root-cause neighborhood, relevant SQL, and relevant stack trace. Prefer targeted retest plus affected regression.
9. Record MEDIUM/LOW findings as Technical Debt / Follow-up when they do not threaten the current requirement, integrity, security, required tests, integration contracts, migrations, or critical business rules. CRITICAL/HIGH findings block completion.
10. Codex handles ordinary compile, type, test, wiring, MariaDB/Redis, Flyway syntax, frontend build, Testcontainers, Docker-development, and in-scope refactoring problems autonomously. Stop only for `DESIGN CHANGE REQUIRED`, `USER DECISION REQUIRED`, or `DESTRUCTIVE ACTION REQUIRED`.
11. A task may read and test its integration scope but may write only its current Write Scope. No unrelated refactor, speculative feature, future-task implementation, temporary business model, duplicate platform infrastructure, or frozen-contract redesign.
12. DEV, TEST, and PROD are distinct environments. PROD settings are never DEV/TEST defaults.
