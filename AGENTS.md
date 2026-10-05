# Hospital Pharmaceutical MES V2.0 Engineering Rules

These are repository-wide mandatory rules. `FINAL BASELINE COMPLETE v1.0.16` remains authoritative for frozen business, database, API, state-machine, UI, integration, and GxP contracts.

## Low-token task startup

For a request such as `完成 MES-XXX`:

1. Read this file and [`MES_TASKS.md`](MES_TASKS.md) only.
2. Locate exactly one task. Do not reimplement an `ACCEPTED` task.
3. Read that task's linked Task Card, then only the mandatory reference sections named by the card.
4. Inspect only affected modules, direct integration contracts, migrations, and tests. Use `rg`/`rg --files` with scoped paths.
5. Perform Dependency Check, Gap/Impact Analysis, and Integration Contract Review internally before editing.
6. Implement only the current task's Write Scope, run targeted validation, update `MES_TASKS.md`, and report briefly.

Do not scan the whole repository or read all `docs/`/baseline artifacts by default. Expand context only when a discovered dependency, failure, or frozen contract requires it.

## Mandatory engineering rules

- Use Java 21 and Spring Boot 3.x. Keep the backend a Maven modular monolith; `mes-boot` is the only executable module and dependencies must remain acyclic.
- Business modules use `api`, `application`, `domain`, and `infrastructure` boundaries. Cross-module notifications use domain events; complex cross-module reads use query services.
- Use MariaDB-compatible SQL. Use MyBatis-Plus for ordinary CRUD; explicit SQL/XML is limited to traceability, lineage, eBR aggregation, and reporting.
- Model business state changes with commands and domain methods. Never expose a generic `updateStatus` API.
- API response wrappers belong to the API layer; domain services must not return HTTP envelopes. Persistence entities and domain models may be separate.
- Never physically delete production or regulated records. Use optimistic locking for mutable regulated aggregates.
- Critical operations must include audit and electronic-signature concerns from the start.
- Do not make core production state machines depend on BPMN/workflow runtime availability.
- Dynamic form expressions must never execute arbitrary JavaScript, SQL, or SpEL.
- `RELEASED` means finished-product release, not production completion.
- Do not pre-implement future MES tasks, introduce temporary business models, duplicate platform infrastructure, or perform unrelated refactoring.
- Applicable CI contracts remain mandatory before merge; do not bypass failures or weaken assertions without an explicit reviewed CI-contract change.

## Mandatory UI governance

- The approved global visual baseline is [`docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md`](docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md), and approved business page structures are [`docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md`](docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md).
- Every new or substantially modified Vue business page must inherit the Global UI Design System and explicitly select exactly one T1–T6 page template. Do not invent a new business page structure, module-specific visual language, font/color/spacing system, or ad-hoc CRUD layout. If T1–T6 cannot represent the page without changing business meaning, stop with `DESIGN CHANGE REQUIRED`.
- The UI baseline is subordinate to FINAL BASELINE v1.0.15 and approved DCPs for business fields, states, permissions, routes, APIs, workflows and GxP controls. Visual work must never change those contracts implicitly.

## Context and token control

- Start from the task index, not repository history. Completed-task acceptance records are read only for an affected regression or consumed contract.
- Read the smallest useful file section. Do not load entire large specifications when the Task Card identifies a section, table, schema, or operation.
- Prefer targeted searches and focused diffs. Do not dump whole build logs, generated files, lockfiles, or large OpenAPI documents into context.
- On PASS, retain suite/count/duration/migration summaries only. On FAIL, inspect the failed test, root-cause neighborhood, relevant SQL, and relevant stack frames.
- Preserve user-owned and unrelated working-tree changes.

## Frozen design and stop conditions

Do not add or redesign business fields, tables, states, APIs, permissions, routes, or GxP controls outside the current frozen contract. Stop only for:

- `DESIGN CHANGE REQUIRED`: a real conflict requires changing a frozen contract.
- `USER DECISION REQUIRED`: multiple valid business choices require owner selection.
- `DESTRUCTIVE ACTION REQUIRED`: irreversible deletion, persistent DEV rebuild, or large-scale removal is required.
- `DEPENDENCY_NOT_READY`: a hard dependency or required produced contract is unavailable.

The MES-012 scope conflict is recorded in `MES_TASKS.md`; agents must not resolve it implicitly.

## Authorized Design Change Governance

The frozen authoritative baseline remains the default Source of Truth. A request to complete or implement an `MES-XXX` task does not, by itself, authorize a design change.

Without explicit authorization from the user or Design Authority, Codex must not add, delete, rename, or redesign frozen:

- business fields, entities, database tables, columns, constraints, or relationships;
- aggregate boundaries, state machines, transitions, or domain invariants;
- APIs, OpenAPI contracts, permissions, menus, routes, or integration contracts;
- audit, electronic-signature, revision, lineage, or other GxP controls; or
- task boundaries, hard/soft dependencies, produced contracts, or consumed contracts.

When a real requirement conflicts with the frozen baseline and no approval exists, stop with `DESIGN CHANGE REQUIRED`.

After the user or Design Authority explicitly approves a Design Change, Codex may modify frozen design only within the approved scope. Every authorized change must:

1. identify the approval and bounded Design Change scope;
2. update every affected authoritative document in the same release;
3. keep Database, Domain, State Machine, API/OpenAPI, UI, Permission, Test, RTM, Migration, Integration Contract, and Task Dependency artifacts consistent;
4. preserve prior frozen releases as immutable history;
5. never edit, rename, reorder, checksum-change, or conceal an executed Flyway migration;
6. never create a second Source of Truth or silently rewrite regulated history;
7. preserve Audit, electronic-signature, Revision, and Lineage evidence;
8. avoid implementation outside the approved change boundary;
9. complete and record a cross-document consistency review before switching the authoritative baseline pointer; and
10. stop again with `DESIGN CHANGE REQUIRED` if a newly discovered issue is outside the approved scope.

## Database and environments

- All formal schema changes use a new physical Flyway migration. Executed migrations are append-only: never edit, rename, reorder, checksum-change, or conceal problems with `flyway repair`.
- Physical versions continue from the highest successful value in `flyway_schema_history`; Section 15 logical migration groups are not physical versions.
- Local development and local database tests use the same native MariaDB at `localhost:3306`, database `hospital_pharma_mes_dev`. Run from `D:\codex\_project\gmp\hospital-pharma-mes`; the local and ci profiles load the ignored root `.env`. Do not create another local database or start a Docker database for this project.
- Keep local database configuration files and credentials on this workstation. Do not stage, commit, or push `.env` or local changes to database configuration files, including `application-local.yml` and `application-ci.yml`, unless the user explicitly changes this instruction. Existing tracked configuration files must not be removed from GitHub implicitly.
- Keep all project tables and Flyway history in this database. Local tests must preserve existing data, use uniquely identified test records, and roll back or remove only records created by that test. Review tests that modify shared seed records before running them against this persistent database. Never delete, rebuild, clear, or reset the database without explicit user authorization.
- Automated tests never connect to PROD. Hosted CI retains its existing service-container configuration; it cannot connect to this workstation's localhost database.
- Full lifecycle details are in [`docs/development/database-and-validation-strategy.md`](docs/development/database-and-validation-strategy.md); read it only for database/environment/validation work.

## Validation policy

Default to the smallest gate that proves the change:

1. Compile or typecheck affected modules.
2. Run current-task unit and required test cases.
3. Run targeted integration tests only when MariaDB, Redis, Flyway, SQL, transactions, constraints, concurrency, or idempotency are involved.
4. Run direct contract and affected regression tests.
5. Fix all CRITICAL/HIGH findings; record non-blocking MEDIUM/LOW debt.

Do not run full regression by default. It is reserved for defined milestones/key integration gates, major cross-module changes, final system validation, or explicit user instruction.

## Task status and completion report

`MES_TASKS.md` is the only task-status index. Codex may move an authorized task from `NOT STARTED` to `IN PROGRESS` and, after required verification, to `READY FOR ACCEPTANCE`. Only explicit human approval may set `ACCEPTED`.

After work, report only: implementation summary, migration status, targeted test result, review result, remaining technical debt, and task readiness/status. Never start the next MES task automatically.
