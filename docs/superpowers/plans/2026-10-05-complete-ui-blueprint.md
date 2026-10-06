# Complete UI Blueprint Implementation Plan

**Goal:** Implement the remaining approved UI blueprint, preserving FINAL BASELINE v1.0.17 business contracts.
**Architecture:** Work sequentially by page family. Reuse existing read models, lookups, command owners and shared Global UI V2 styles; place technical evidence in secondary read-only areas.
**Tech Stack:** Vue 3, Ant Design Vue, TypeScript, existing Playwright Chromium.
**Spec:** docs/ui/MES_COMPLETE_PAGE_DESIGN_BLUEPRINT_V1.md

## Constraints
- InspectionRecordOverview.vue and InspectionReportOverview.vue are human-approved and protected.
- T5 PC structure is protected; remove static illustration and unsupported controls as the blueprint requires, then freeze.
- No backend/API/DTO/database/state/permission/signature changes or new facts.
- Preserve local database configuration and unrelated working-tree changes.
- Check each family in Chromium and save screenshots before moving on; T5 is PC-only.
- No automatic commit/push under this implementation request.

## Ordered work and evidence ledger
- [x] 0. Freeze T5: ExecutionOverview.vue; execution-workbench-visual.spec.ts, desktop.
- [x] 1. Other incoming T3: IncomingDetailView.vue, IncomingDocument.vue and presentation helper; incoming-detail-presentation.spec.ts, desktop/mobile.
- [x] 2. MaterialLot T4: MaterialLotView.vue, MaterialLotLineage.vue; targeted inventory/lineage tests, desktop/mobile.
- [x] 3. Production Batch T4: ProductionDetailView.vue and subordinate presentation; batch tests, desktop/mobile.
- [x] 4. Remaining T1: existing master/process/WMS/quality/production/eBR/IAM lists; scoped list tests and screenshots.
- [x] 5. Remaining T2: existing create/edit routes, form grouping and stable actions; scoped form tests and screenshots.
- [x] 6. T6: material release and finished release final consistency, existing gate semantics, desktop/mobile.
- [x] 7. eBR template/designer/runtime: existing palette/canvas/properties and controlled runtime; targeted eBR tests/screenshots.
- [x] 8. Trace/Audit/Integration: existing read-only lineage, event details and operational console; targeted tests/screenshots.
- [x] 9. Dashboard/IAM/master/WMS/production remaining consistency: route inventory and affected regressions/screenshots.
- [x] 10. Whole UI consistency audit: typecheck, affected frontend tests, build, route/family evidence coverage, remaining debt.

## Review focus
Permission-limited lookups keep truthful fallback references; original FAIL and selected results remain distinguishable; technical evidence remains reachable; mobile tables scroll within cards; regulated writes retain allowedActions, optimistic versions, reasons and signatures.

## Progress
2026-10-05: Fast-forward main cd3443c -> 0fed5be. Only existing local database configuration changes present. All four required governance documents read. The explicit approved blueprint and user implementation authorization provide the design scope; no further design approval needed.

2026-10-05 final: All ordered page families implemented and inspected in real Playwright Chromium. 25 T1 routes and 24 T2 create routes captured on desktop/mobile; T3/T4/T6/eBR/platform/closeout cases also captured. T5 preserved PC-only scope. Protected inspection record/report components, router/API modules and frozen baseline unchanged.
Validation: final typecheck PASS; affected Vitest 15 files / 61 tests PASS (8.89s); build PASS (9.73s, existing large-chunk warning). Broad affected browser run: 77 PASS; the sole mobile T5 reference-layout failure was outside explicit PC-only scope and subsequently made an explicit skip. Changed trace/platform/permission/command cases rerun after review fixes; issuing command PC/mobile PASS; password command PC/mobile PASS; T5 PC PASS. Independent review's 2 HIGH and 3 MEDIUM findings corrected and re-reviewed PASS. No outstanding HIGH/CRITICAL finding in reviewed scope.
Evidence uses mocked existing API responses rendered by the actual application, not live database acceptance. Browser plugin not available; existing Playwright Chromium used. No migration, business-contract change, commit or push. User-owned local/ci database configuration preserved. Status READY FOR ACCEPTANCE.
