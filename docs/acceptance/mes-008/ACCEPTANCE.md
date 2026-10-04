# MES-008 current-stage verification evidence

Task status is maintained only in `MES_TASKS.md`. Evidence covers the approved receipt/inventory stage; it does not represent original full-task acceptance.

## Implemented

- 35 frozen operations (original 34 plus approved receipt PUT), warehouse/location/container persistence and commands.
- Multi-line receipt create/draft edit/confirm and direct receive, immutable confirmation facts, current approved supplier relationship (including nonpreferred source), six explicit receipt checks, exact UOM conversion and receipt-time material/inspection snapshots.
- One lot and RECEIVE ledger entry per item, both required/exempt materials QUARANTINE/BLOCKED, append-only ledger, inventory balances, paired moves and signed adjustments; nonnegative stock and frozen/expiry/retest safeguards.
- Existing receipt/issue/lot routes and permission-filtered actions, query restoration, 409 reload / 422 input retention and desktop/mobile horizontal labels. Real stock and zero-balance adjustment operate in frozen lot-detail route; no invented `/wms/inventory` route/menu.
- Original future production write APIs return DEPENDENCY_NOT_READY; generic lot writes return RECEIPT_REQUIRED, as approved. No fake batch or permissive eligibility provider.

## Migration and targeted coverage

Physical V013 applied successfully after V012: ten tables, original permission/menu seeds, frozen indexes and ledger UPDATE/DELETE rejection triggers. receipt_item_id remains nullable per database contract; current receipt-created lots always populate it. Reservation/issue main_batch_id FKs remain the explicit LG-009A responsibility. No invalid producer references are written. Executed migrations are now immutable.

`WmsRulesTest` 6 PASS (0.068s). `WmsIT` 9 distinct cases passed: eight in the first run, the date/API case in one focused retest. Covered receipt save/confirm/direct command, required and exempt blocked policy, nonpreferred approved supplier and expiry, snapshot retention, unit precision, ledger recomputation/immutability, idempotency, stale and two-transaction locking, org/permission/closed DTOs, future gate refusal and audit rollback.

TC-WMS-002/003/008 receipt-ledger-stage evidence is present. Current blocked writes do not prove real production eligibility. TC-WMS-001 final production use, 004 FEFO, 005 issue-vs-charge, 006/007 real exempt release and TC-ELG-001/002 remain required when MES-008A and MES-009 physically produce their contracts. Generic lot writes' full acceptance is also deferred. No QMS request/sampling/testing/release implementation is included.

Screenshots: `receipt-create-{desktop,mobile}.png`, `lot-inventory-{desktop,mobile}.png` (API-mocked UI evidence, visually inspected).

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
