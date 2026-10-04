# MES-007 current-stage verification evidence

2026-10-04: full-task runtime/consumer completion and TC-EBR-001..009 evidence supersede the historical staged limitation below. See [current closeout](CLOSEOUT-2026-10-04.md). MES_TASKS.md now records READY FOR ACCEPTANCE, not human ACCEPTED.

Task status is maintained only in `MES_TASKS.md`. Evidence covers the approved LG-007A definition stage; it does not represent original full-task acceptance.

## Implemented

- Nine normalized eBR definition tables, stable-code retention, closed DTOs and all 11 original template/designer APIs.
- Independent template list/create/detail and three-column Designer, typed fields/options/sections/groups/rules/signature/review settings; horizontal label/control pairs on desktop and mobile.
- DRAFT → SUBMITTED → APPROVED → EFFECTIVE, independent approval, optimistic/family locks, idempotency and transaction-bound audit. Published canonical hash and organization-scoped query consumer contract; published data cannot be ordinarily edited.
- Bounded whitelist DSL, reference/cycle/calculation checks, deterministic simulation with server identity/time/UOM context, lint and version comparison. No runtime production evidence is fabricated.
- Existing process operation and enabled IAM role read-only integration; original permissions, menu and SYSTEM_ADMIN assignment.

## Migration and targeted coverage

Physical V012 applied successfully after native V011. Nine definition tables and original permission/menu seeds; runtime LG-007B is not created. Executed migrations are now immutable.

`EbrDslTest` 6 PASS (0.023s), `EbrDefinitionRulesTest` 5 PASS (0.266s), `EbrEngineTest` 3 PASS (0.061s), `EbrIT` 8 PASS (9.305s). Native evidence includes real upstream Process creation/approval/publication, lifecycle/hash/portable schema, independent approval, org/permission/reference checks, retained rows and ownership, replay/stale commands, audit rollback and real two-transaction family locking.

TC-EBR-001/002 are exercised at definition level; BLOCK/WARN and dependency behavior are simulation evidence. TC-EBR-003/004/005 production submission, 006/007/008/009 runtime revision/signature/review/concurrency and required runtime/PDF coverage remain scheduled with real LG-009A/LG-007B/LG-010 producers. Definition simulation is not a substitute for these gates. No runtime signature provider, batch, operation or PDF implementation is pre-created.

Screenshots: `ebr-designer-desktop.png`, `ebr-designer-mobile.png` (API-mocked UI evidence, visually inspected).

## Validation execution

2026-10-03, Java 21 / native MariaDB `hospital_pharma_mes_dev`, root `.env`, no alternate DB or reset. Commands from repository root:

```powershell
mvn -q -pl backend/mes-boot -am verify -Pci-integration '-Dtest=EbrDslTest,EbrDefinitionRulesTest,EbrEngineTest,WmsRulesTest' '-Dsurefire.failIfNoSpecifiedTests=false' '-Dit.test=EbrIT,WmsIT'
mvn -q -pl backend/mes-boot -am verify -Pci-integration '-Dtest=NoUnitSelected' '-Dsurefire.failIfNoSpecifiedTests=false' '-Dit.test=WmsIT#organizationPermissionClosedDtoAndFutureWritesFailClosed'
```

First gate compiled/packaged affected modules, passed 20 unit cases and 16/17 integration cases. One test reused an existing receiptNo while testing an invalid date: the earlier duplicate check correctly returned 409. Only that test input was changed to a unique receiptNo; the one-case retest passed (7.272s including context startup). Final distinct results: 20 unit + 17 native integration PASS. No assertions weakened; no full regression. Test-produced ordinary records roll back; two-transaction cases use uniquely identified fixtures and precise finally cleanup, with no ledger/audit deletion. Post-gate read-only checks: test actor/unit residue 0, migration history 13 successful / 0 failed, two ledger append-only triggers present.

Frontend: `vue-tsc -b` PASS, 6 focused model unit cases PASS (0.94s), 12 distinct Playwright desktop/mobile cases PASS across focused runs (eBR 6 + WMS 6). Browser APIs are mocked; real persistence/API/filter/audit evidence comes from the native tests above. Initial browser failures were selector ambiguity from AntD spaced two-character text and combobox labels; explicit accessible selectors fixed them. Review additions cover numeric simulation, nullable text, and zero-stock adjustment. Latest focused six-case run 24.5s; final four main-flow screenshot cases 22.8s. No claim of real-browser-to-native full E2E or complete future-task acceptance.

## Scoped review and boundaries

Fresh independent static review covered new eBR/WMS backend, physical migrations, module wiring, 46 exact security permission pairs, direct Process/Identity producer additions, and new UI/routes. Fixed before completion: WMS constructor syntax and malformed dates; missing frozen indexes; eBR permission/menu seeds, saved ownership protection and duplicate calculation target detection; simulation numeric types, nullable textarea clearing and zero-stock adjustment UI. Current CRITICAL/HIGH/necessary MEDIUM = 0. No full-repository review claimed.

Remaining LOW: process-version picker loads the first 100 process packages without search/pagination; inventory/definition aggregation makes per-row queries and needs production-volume profiling later. Existing MariaDB 13 versus Flyway tested-version warning remains; current migrations validated successfully. Hosted CI/clean-install milestone gates are not replaced by this DEV upgrade verification.

Authority is the frozen v1.0.9 release, including the approved sequencing DCP and stage appendices. Development handoff JSON/Markdown are preparation records, not another source of truth. Local ref validation: 1,558 OpenAPI refs, 46 operation/permission pairs, acyclic 21-module Maven graph; v1.0.8 80/80 and v1.0.9 83/83 manifest hashes verified. Existing changes, credentials and historical migrations preserved; no commit/push/config staging.


## Human acceptance — 2026-10-04

User explicitly confirmed “MES-007 确认验收”. MES-007-R2 is now ACCEPTED in MES_TASKS.md, the sole task-status index. The completed scope and required TC-EBR-001..009 evidence are recorded in CLOSEOUT-2026-10-04.md; preceding readiness statements are historical and superseded by this acceptance. Existing LOW debt and MES-013 PDF/archive boundary remain. No code/schema change, test rerun or other-task acceptance.
