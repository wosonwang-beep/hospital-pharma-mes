# MES-006 targeted delivery evidence — 2026-10-03

## Authority and scope

Original MES-006 task card, mandatory PRD/domain/database/API/functional/UI prototype and v1.0.6 list/edit style were re-read before implementation continued. Explicit “确认授权” approves DCP-MES-006-R2-001, now cumulative v1.0.7. No additional redesign or later MES task was undertaken. Task status is maintained only in MES_TASKS.md; no human acceptance was inferred.

## Implementation

- mes-process: products, process packages and business versions; BOM/material-unit validation; route predecessor cycle/reachability checks; operation completion rules and MANUAL/AUTO/HYBRID parameter limits; immutable submitted/approved/effective definitions; copy or empty draft.
- Existing 17 APIs, organization/permission guards, optimistic version locks, idempotent writes and transactional audits. Approval reuses reauthentication/signature infrastructure with stable business-version binding and separation of duties; publication verifies the signed content. ProcessQueryService provides detached snapshots and controlled usable definitions, with named lifecycle events.
- Eight independent product/package UI routes and permission navigation. Existing list/edit styling, inline query labels, named selectors, BOM/route/parameter/compare tabs, lint/submit/approve/publish, retained unsaved content on conflicts, safe rule editing and version copy. No material version approval was reintroduced.

## Migration and database preservation

V011__mes_006_process.sql applied once to native localhost:3306/hospital_pharma_mes_dev from successful V010. Product plus seven process tables, approved route columns, constraints/indexes and existing-role permission/menu seeds. Flyway validated 11 migrations; history 001–011 all successful. No repair/reset/rebuild, no old migration changes. Final read-only query: process test actors=0, process test products=0. Ordinary tests rolled back; committed concurrency fixtures were identified and individually cleaned up.

## Targeted verification

- Backend compilation/package PASS (Java 21, Maven modular build).
- ProcessRulesTest: 5/5 PASS, final 0.147s. Covers required state/immutable behavior, cycles/dangling references, quantities/limits/modes, safe completion rules.
- ProcessIT: 9 distinct integration cases PASS across focused gates, including TC-PROC-001/002/003/004, actual signature verification after publication, copy/snapshot preservation, incompatible and material-specific UOM, organization and API permissions, closed DTO/200 response, replay/stale writes/retained rows/draft uniqueness, creator separation, reauthentication failure, audit rollback, physical migration and two simultaneous transactions. Final focused gate for API/lifecycle/concurrency passed 3/3; subsequent leaf-JSON/lifecycle check passed 1/1 (10.16s includes context startup).
- Direct consumed regression: MasterResourcesIT#tcMd001ConversionRoundTripEvidenceAndDimensionFailure 1/1 PASS (7.202s including context startup).
- Frontend vue-tsc + Vite build PASS, final 12.56s. No broad frontend unit rerun was needed; browser cases exercise this task's behavior.
- Playwright: 6 distinct cases PASS across desktop and Pixel 5 (3 flows each). Initial gate 5/6; product async-load race fixed and only product cases rerun 2/2 PASS (8.5s). Final affected process lifecycle cases 2/2 PASS (16.2s), including rule rename/normalization and preserving the other tab during partial save. API responses mocked in browser; actual DB/API/audit/signature behavior verified separately by native MariaDB integration tests.
- OpenAPI: all 1,424 local references valid, MES-006 retains 17 closed operation contracts. Scoped diff whitespace check PASS. Final desktop/mobile screenshots inspected.

Initial failures were corrected rather than assertions weakened: repository proxy field access → accessor methods; missing task-local request error mapping → reused existing API advice; concurrent-test generic type compilation → explicit Long; UI rule canonicalization/rename/busy guard and product load race → scoped fixes. Failed/affected gates only were rerun, not full regression.

## Review

Fresh read-only MES-006 reviewer identified one HIGH completion-rule round-trip issue and two MEDIUM data-loss risks (rename and edits during save). All corrected and covered by scoped browser/lifecycle verification. Self-review also fixed product loading race. Remaining CRITICAL=0, HIGH=0, required MEDIUM=0. No additional review loop or unrelated refactor.

## Remaining limitations/debt

- Existing hosted clean-database migration/CI gates must pass before merge; they were not run against a second local database. Local upgrade/chain integrity passed.
- Existing frontend main-bundle warning persists (LOW). Process list currently loads version definitions per package; query aggregation is a performance follow-up for large volumes (LOW), not a functional blocker.
- Future eBR/production consumers remain in their respective tasks. No commit/push/deployment or local credential/config staging performed.

## Evidence locations

Code: backend/mes-process; backend/mes-boot/src/test/java/com/hospital/mes/process/integration/ProcessIT.java; frontend/mes-web/src/views/process; frontend/mes-web/e2e/mes006.spec.ts. Migration: backend/mes-boot/src/main/resources/db/migration/V011__mes_006_process.sql. Local detailed command logs: workstation TEMP mes006-*.log. Screenshots: process-effective-desktop.png and process-effective-mobile.png in this directory.


## Current-baseline UI recheck — 2026-10-03

The follow-up MES-006/007/008 request triggered a fresh comparison with v1.0.8 UI sections 1–3 and the approved horizontal-label deltas. Corrected three missed implementation requirements: dedicated GxP approval dialog with meaning/object/business version/identity/reason/reauthentication; existing filtered Audit routes; preserved product/package query state through create, edit, version changes, transitions and return. Also corrected uppercase-T keyword restoration. These implement existing contracts; no frozen baseline or backend migration changed.

Fresh verification: vue-tsc exit 0; existing mes006.spec.ts 6/6 PASS (14.8s), now checking dialog and query preservation; two focused desktop/mobile signature geometry/cancel-clears-credential cases PASS. Screenshot capture was refined to disable the opening animation; no product failure was hidden. Initial extra-case invocation had a working-directory mistake and then a missing test closure; both test setup mistakes were corrected. No backend/DB rerun because only UI changed. Existing actual DB/signature/transaction evidence above remains prior evidence.

Screenshots: process-signature-desktop.png and process-signature-mobile.png. Fresh self-review: no remaining blocking finding in this correction; independent baseline reviewers covered MES-007/008 document contracts, not this UI patch. No claim of whole-system acceptance. Existing CI gates and low-priority performance debt remain.
