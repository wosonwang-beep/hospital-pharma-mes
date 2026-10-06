> Historical UI-only checkpoint. The subsequently approved database/source contract and final verification supersede the pending gaps below. See [current closeout](SOURCE-CLOSEOUT.md).

# Material Master UI Field Cleanup — Controlled Fix

Date: 2026-10-06. Starting main HEAD: `15d4741`. Business authority: FINAL BASELINE COMPLETE v1.0.17. UI authority: Global UI Design System V2 / T1–T6. Explicit approval: the user's MATERIAL MASTER UI FIELD CLEANUP — CONTROLLED FIX instruction authorizes presentation cleanup only.

## Delivered scope

- T1: materialCode, materialName, materialType, specification, baseUnitName, storage environment availability, status, actions. Manufacturer and updatedAt removed from default material columns only. Other master-resource lists unchanged. Existing query/filter/pagination/sort endpoints and parameters unchanged; storage availability has no unsupported sorter.
- T2: four logical sections, ordered as specified: basic identity/specification/grade; base unit/pack unit/pack specification; incoming-inspection flag/storage availability; full-row remark. Existing V2 card/form structure retained. Change reason, audit, optimistic version, idempotency, disable and unit/supplier controls retained.
- T3: default Basic tab displays all eleven requested labels; no manufacturer, appearance, effective dates, lotControlled or technical ID/version/time fields. Existing quality/supplier/usage tabs and audit entry preserved.
- Hidden fields remain in the unchanged materialFields write-contract mapping. Existing record values are loaded and preserved in update payloads; create retains existing defaults. No property is removed from API/DTO/DB, no storageCondition is injected into a closed write DTO.
- Supplier relationship already returns qualificationStatus. Display it with the current material's name alongside existing supplier, active relationship, preferred and validity facts. No supplier-material code or manufacturer is invented.

## Contract check / DESIGN CHANGE REQUIRED for unavailable data

| Requested fact | Actual current producer | Result |
|---|---|---|
| Storage environment | MaterialEntity has storageCondition, but MaterialService.view explicitly excludes it; Create/Update commands do not accept it. | Read-only availability message; editing/persisting it is BLOCKED by current contract. |
| Supplier manufacturer / manufacturerId / manufacturerName | SupplierEntity and MaterialSupplierEntity have no manufacturer association; relationship read enriches supplier code/name/qualification only. | No manufacturer column invented. |
| Receipt source manufacturer | ReceiptItem has manufacturerLotNo; materialSnapshot includes legacy material-master manufacturerName. | Manufacturer lot number is not a manufacturer identity; material-master snapshot is not a supplier-source association. Existing source records unchanged. |
| MaterialLot source manufacturer | MaterialLot carries detached materialSnapshot and receipt linkage; no independent source manufacturer relation. | No inferred manufacturer identity or relabelled master snapshot. |
| Supplier material code | Current relationship has no supplier-material code. | Not displayed. |

The frozen DCP-MATERIAL-BASIC-001 uses closed flat create/update DTOs and detached consumer snapshots. Adding storage read/write support or true supplier-source manufacturer ownership requires a separately approved bounded contract change and consistency review. This fix does not switch the baseline, implement unapproved workbench proposals or reopen accepted MES tasks.

## Targeted verification

- npm run typecheck: PASS.
- Affected material/resources unit suites: 9 tests PASS, 2 files, 1.04s. Hidden manufacturer/appearance/false lot control/effective dates retained; closed DTO has no storageCondition.
- npm run build: PASS, Vite 9.66s; inherited LOW bundle-size warning remains.
- Existing Playwright Chromium desktop: 1 scoped T1 → T3 → Supplier → T2 Edit/save → T2 Create flow PASS, 10.1s. Exact default columns/field order, absence of retired UI values, real qualification facts, unchanged default sort and hidden payload retention verified; no page/console errors or document overflow.
- Initial test-only selector ambiguity (nested main and multiple change-reason labels) and ISO millisecond normalization expectation corrected; production assertions retained. No full regression/native DB test needed. Browser plugin not available; project Playwright used. Existing API-shaped fixtures render the actual Vue application; no live regulated data was created.
- Screenshots: [T1 list](material-list.png), [T2 create](material-create.png), [T2 edit](material-edit.png), [T3 detail](material-detail.png), [supplier relationship](material-suppliers.png). List capture follows query interaction and includes transient table loading tint. Create/detail screenshots visually inspected: horizontal label/control alignment, two columns, full-row remark, no unsupported source data.

## Review / status

Existing-contract field cleanup: READY FOR ACCEPTANCE. Unavailable storage write and manufacturer source ownership: DESIGN CHANGE REQUIRED, not implemented. API/DTO/database/migration/state/permission/signature/frozen baseline unchanged. No remaining CRITICAL/HIGH introduced by the delivered cleanup; unavailable-data scope is explicitly limited above. Local database configuration changes preserved. No commit/push requested or performed for this fix.
