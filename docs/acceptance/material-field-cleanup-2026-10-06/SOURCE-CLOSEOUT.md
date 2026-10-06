# Material storage and supplier source controlled maintenance — Closeout

Date: 2026-10-06. Starting HEAD: `15d4741`. Status is maintained in [MES_TASKS.md](../../../MES_TASKS.md). Human acceptance and commit/push to main were explicitly authorized on 2026-10-06: “验收，提交和推送”.

## Approval and implementation

User approved database modification and selected manufacturer ownership on each material–supplier relationship, with receiving source frozen. Approved scope: DCP-MATERIAL-STORAGE-SOURCE-001; current cumulative business authority: **FINAL BASELINE COMPLETE v1.0.18**. Global UI V2 / T1–T6 remain unchanged.

- Material T1/T2/T3 default presentation follows the eleven-field whitelist and requested order. Hidden legacy values remain in the contract and retain their existing update semantics.
- Existing material storageCondition is exposed through the approved read/write DTO extension, validated to 500 characters and persisted with audit and optimistic locking.
- Manufacturer belongs to the material–supplier relationship, is nullable and validated to 200 characters. Supplier qualification, preferred relationship and validity controls are retained.
- Draft receipts do not freeze source. CONFIRM and direct RECEIVE freeze relationship/material/supplier identities, supplier code/name and manufacturer on the receipt item, in the same transaction as receiving facts, lot and ledger.
- MaterialLot reads the same source through its existing receiptItemId relationship. Historical absent source is explicitly unrecorded; no live relationship fallback or inferred legacy material-master manufacturer is presented.
- Existing quality state, production restrictions, permission, signature and command action semantics are retained.

## Migration and historical preservation

New physical Flyway **V028** applied successfully to the configured native development database. Flyway validates 28 migrations; schema version 028, failed history entries 0. Existing executed migrations were not modified or repaired. No backfill, deletion or database reset.

Two uniquely identified committed MVCC probe materials (`CURM1fd13971d742`, `CURM2414955158e3`) are retained and disabled through an audited command. Other targeted fixtures follow existing transaction rollback policy. Shared seed data is not changed.

The cumulative release consistency gate passes: all **114 v1.0.17 files are hash-unchanged**. Approved DTO/schema/UI/test/RTM/task-card maintenance is recorded in v1.0.18. Public API paths, route matrix and task dependency matrix remain unchanged. Current AGENTS, MES_TASKS, PROJECT_BASELINE and UI business-authority pointers agree on v1.0.18. Historical v1.0.17 human acceptance remains historical; this bounded maintenance has now received explicit human acceptance; no new full-system regression is inferred.

## Targeted verification

| Gate | Result |
|---|---|
| Backend unit | MaterialSupplierRulesTest: 5 PASS |
| Native MariaDB integration | 7 distinct methods across scoped runs PASS: storage persistence/audit/detachment; relationship manufacturer; receipt freezing/lot read; confirmation snapshot/idempotency; direct receive/exemption; audit rollback; MVCC current-read/fail-closed |
| Frontend typecheck | PASS |
| Affected frontend unit | 9 tests / 2 suites PASS |
| Frontend build | PASS, final 9.09 seconds |
| Playwright Chromium PC | 3 distinct scoped scenarios PASS: material list/detail/relationship/edit/create; receipt/lot frozen source; existing MaterialLot T4 read-chain regression |
| Source concurrency review | HIGH stale snapshot read fixed; final independent review: 0 CRITICAL / HIGH / MEDIUM |
| Cross-document consistency | PASS |

The first Maven invocation omitted the ci-integration profile and was compile/unit evidence only; it is not counted as native integration. The actual native gates were rerun using `-Pci-integration`. MariaDB 13 can reject a locking current read after a stale consistent snapshot: the test accepts only the explicit fail-closed conflict and proves a fresh transaction reads the latest maker. Original audit rollback assertions remain in place. Source audit digest and direct RECEIVE source are explicitly checked.

Actual Vue rendering uses API-shaped Playwright fixtures; this is not a claim of a complete live-database browser regression. Browser plugin unavailable, so existing project Playwright Chromium was used. No unrelated full regression or mobile redesign was performed.

## Screenshots

- [Material query](material-list.png)
- [Material create](material-create.png)
- [Material detail](material-detail.png)
- [Material edit](material-edit.png)
- [Supplier relationships](material-suppliers.png)
- [Receipt frozen source](receipt-source.png)
- [MaterialLot frozen source](material-lot-source.png)

## Remaining debt and exclusions

Inherited LOW frontend bundle-size warning remains. Optional manufacturer can be absent and is displayed as unrecorded. No newly identified CRITICAL/HIGH issue remains in this bounded scope.

Material Master Image, Operation Execution Photo, Planned Operation Time and SOP/Method Reference remain **PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED**. Global UI V3 is **NOT IN CURRENT SCOPE**. No other MES task was started. Existing local database configuration changes are preserved and excluded from this maintenance.
