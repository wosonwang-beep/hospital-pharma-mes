# Hospital Pharmaceutical MES V2.0 Engineering Rules

These are repository-wide mandatory rules. `FINAL BASELINE COMPLETE v1.0.17` remains authoritative for frozen business, database, API, state-machine, UI, integration, and GxP contracts.

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
- Persistent DEV uses `hospital_pharma_mes_dev` and the named MariaDB/Redis volumes. Never delete, rebuild, clear, or run `docker compose down -v` unless the user explicitly requests `RESET DEVELOPMENT DATABASE`.
- Automated TEST infrastructure is ephemeral and isolated from DEV/PROD. Automated tests never connect to PROD.
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


## Authorized v1.0.17 incoming completion supplement

Human approval covers the bounded contract completion and full incoming acceptance tasks. For eBR runtime, issue returns, weighing verification and actual trace identities, the following precise supplements supersede generic or conflicting clauses in this chapter; unaffected contracts remain unchanged.

- [00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.17.md](00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.17.md)
- [00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.17.md](00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.17.md)
- [00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.17.md](00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.17.md)


## Authorized v1.0.17 material weighing policy producer

The approved full incoming acceptance scope includes this missing producer. [00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.17.md](00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.17.md) governs deployment configuration, immutable production snapshot, consumers and required tests. It does not restore retired material master fields or change material APIs. It supersedes earlier references to nullable legacy material weighing fields. No new table, permission, route or status.


## v1.0.17 approved trace-read completion

See [00_INCOMING_TRACE_STANDARD_V1.0.17.md](00_INCOMING_TRACE_STANDARD_V1.0.17.md) for exact frozen QC standard node and immutable signature-policy identity evidence. Unaffected contracts remain unchanged.


## Approved functional closure delta — v1.0.17

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.17.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.17.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.
