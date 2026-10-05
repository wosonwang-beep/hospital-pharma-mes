# CURRENT STATE CONFIRMATION — GitHub audit, 2026-10-05

Authority: FINAL BASELINE COMPLETE v1.0.16 + approved DCPs. Code at b55b1a5.
This supersedes obsolete audit timing/scope assumptions, not frozen business contracts.

| Issue | Current confirmation | Evidence / remaining difference |
|---|---|---|
| HIGH-01 | STILL VALID | MaterialLotView.vue overview still says quality is a later module and production is unavailable. |
| HIGH-02 | STILL VALID | WmsListView.vue still unconditionally warns production/eligibility Gates unavailable. |
| HIGH-03 | STILL VALID | MaterialLotView.vue only overview/stock tabs. Existing TraceService.trace materialLotId returns incoming lineage and production-use graph; use this read source without new facts/API. |
| HIGH-04 | STILL VALID | ProductionListView.vue orders expose keyword/status only; ProductionService.page and normative OpenAPI already support productId/plannedDateFrom/plannedDateTo. |
| HIGH-05 | STILL VALID | Batch list lacks product/order/date inputs and route query restoration. Backend already accepts productionOrderId but normative OpenAPI omits it while UI-BAT-Q requires order filtering: real contract gap. |
| HIGH-06 | STILL VALID | Batch half ALREADY FIXED by Phase2A b55b1a5; order status still arbitrary input. MainBatch/Order schemas contain different canonical enums. |
| HIGH-07 | STILL VALID | IncomingDetailView.available duplicates state/action map. IncomingQualityService.view/releaseReview do not expose allowedActions; Incoming schemas omit this read contract. Backend transitions remain authoritative. |
| HIGH-08 | STILL VALID | ExecutionService.openRuns inserts RUNNING without equipment-identity locking/check. closeRuns ends records on pause/complete. EquipmentQueryService.requireUsable reads status/calibration only; V019/V024 have no equivalent active-occupancy uniqueness; existing segment test has no competing equipment users. |
| MEDIUM-01 | STILL VALID | No direct list Edit navigation; independent edit routes/UPDATE permission and backend actions already exist. Not separately implemented this round unless required by a HIGH fix. |
| MEDIUM-02 | STILL VALID | TraceView uses raw root IDs. No current picker. Out of remediation scope this round. |
| MEDIUM-03 | STILL VALID | AppLayout desktop/mobile menus remain flat. Out of remediation scope this round. |
| MEDIUM-04 | STILL VALID | AppLayout duplicates desktop/mobile definitions. Out of remediation scope this round. |
| MEDIUM-05 | STILL VALID | QA/trace/audit header links ALREADY FIXED, full related-record navigation still missing (HIGH-03). |

No complete issue above can honestly be marked ALREADY FIXED or SUPERSEDED. Partial fixes are explicitly identified. The original audit's MES-012 IN PROGRESS/MES-013 skeleton/prohibition and v1.0.15 authority assumptions are SUPERSEDED by v1.0.16 and human-accepted MES-012/013; no task reimplementation is authorized by this audit.

Business contract boundaries: productionOrderId query publication, incoming read allowedActions and new equipment occupancy invariant need bounded Design Change approval before frozen artifact changes. Proposed concrete scope: ../development/DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001-PROPOSED.md. Safe existing-contract UI corrections may proceed independently.

## Remediation result — existing-contract subset

| Issue | Result after this increment |
|---|---|
| HIGH-01 | FIXED: truthful material-production eligibility guidance. |
| HIGH-02 | FIXED: actual dynamic issue prerequisites replace permanent unavailability warning. |
| HIGH-03 | FIXED: T4 tabs cover receipt/request/sampling/sample/test/report/release/stock/production/audit-trace. Existing /trace graph only, source detail links intersect with current permissions, no new API/facts; no trace permission means no trace request. Existing stock/freeze controls retained, no controlled action copied into new lineage views. |
| HIGH-04 | FIXED: product/date/status queries and navigation restoration through existing contracts. |
| HIGH-05 | UI/backend consumption FIXED; formal productionOrderId publication BLOCKED pending proposed DCP approval. Frozen UI-BAT-Q already requires this filter; backend already implements it. No OpenAPI/baseline rewrite performed. |
| HIGH-06 | FIXED: Order and MainBatch state selectors use their separate frozen enum sets; existing canonical values preserved. |
| HIGH-07 | BLOCKED / DESIGN CHANGE REQUIRED: awaiting approval of read allowedActions schema closure; no action/state/signature rule changed. |
| HIGH-08 | BLOCKED / DESIGN CHANGE REQUIRED: awaiting approval of new active-equipment occupancy invariant; no lock/gate/schema/migration change performed. |
| MEDIUM-05 | FIXED as direct HIGH-03 companion: source-record/QA/trace/audit navigation unified on lot page. |
| MEDIUM-01..04 | STILL VALID; outside HIGH-first remediation, no unrelated edit/trace/navigation redesign performed. |

## Changed files

- frontend/mes-web/src/views/production/ProductionListView.vue (T1)
- frontend/mes-web/src/views/production/ProductionDetailView.vue (return with list query only)
- frontend/mes-web/src/views/wms/MaterialLotView.vue (T4)
- frontend/mes-web/src/views/wms/MaterialLotLineage.vue (read-only existing graph)
- frontend/mes-web/src/views/wms/WmsListView.vue (guidance only)
- frontend/mes-web/src/views/quality/incomingModel.ts (+business order/batch identifier presentation only)
- frontend/mes-web/src/views/quality/incomingModel.test.ts
- frontend/mes-web/src/global-ui-v2.css (scoped production T1 query wrapping)
- frontend/mes-web/e2e/audit-high-existing.spec.ts
- this report / bounded DCP proposal / MES_TASKS.md readiness record

## Verification

- Typecheck and final build PASS; inherited LOW >500kB chunk warning only.
-15 affected frontend unit tests PASS,3 files,1.14s. Includes business order/batch display number with exact source-ID submission and previous signature/form safeguards.
-10 distinct audit Chromium desktop/mobile cases PASS:8 cases from final suite plus2 lot-chain targeted locator retests (final lot-chain targeted retest2 PASS/10.3s; permission subset4 PASS/11.7s). Earlier combined audit+Phase2A run14 PASS in23.1s;6 Phase2A regressions included. No full regression.
- Final lot locator correction scopes the type column of actual .ant-table-row, excluding the Ant table hidden measurement row. Both source type and source-description correctly say 收货记录; no application assertion weakened.
- Actual Vue browser rendering with API fixtures, not native backend/DB integration evidence. No native database writes/test, backend implementation, migration or baseline change this increment.
- No Console/Page Error or viewport overflow in checked flows. Desktop1280/mobile393. Source records, source links, permission absence, query parameter/date/product/order/page restoration and Reset checked.
- Six populated screenshots saved at C:/Users/Administrator/AppData/Local/Temp/mes-audit-high-existing, visually inspected (orders/batches/lot desktop+mobile). Browser plugin not available; existing Playwright Chromium used.
- Scoped implemented subset review CRITICAL0/HIGH0. Overall outstanding HIGH05 contract publication/HIGH07/HIGH08 remain; do not call the complete remediation accepted or finished.
- Existing accepted MES tasks and v1.0.16 immutable baseline remain unchanged. No commit/push. User-owned local database configurations preserved.


## Approved continuation / final result — 2026-10-05

The earlier BLOCKED/pending rows describe the pre-approval increment. The user explicitly approved the concrete DCP and its bounded v1.0.17 publication. HIGH-05/07/08 are now FIXED and verified; all HIGH-01–08 and MEDIUM-05 are READY FOR ACCEPTANCE. MEDIUM-01–04 remains STILL VALID. Current authority is cumulative FINAL BASELINE COMPLETE v1.0.17 after consistency PASS; immutable v1.0.16 and the initial confirmation evidence are preserved. See [final implementation, native/browser proof and debt report](MES_V1.0.17_AUDIT_HIGH_CLOSURE_REPORT.md). No human acceptance or commit/push is inferred.
