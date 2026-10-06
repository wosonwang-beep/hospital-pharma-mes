# Workbench functional gap follow-up — 2026-10-06

Starting HEAD: f235bdedb105c6c6c715f53893c37e4676cfd0c4. Human instruction accepts the four preceding pending UI/audit deliveries and authorizes functional completion plus commit/push. Acceptance is recorded in MES_TASKS.md. New functionality is not automatically accepted.

## Verified existing-contract increment — READY FOR ACCEPTANCE

- T5 PC quick action 记录偏差 uses the existing production deviation create route/command. It supplies PRODUCTION, mainBatchId, operationExecutionId and DEVIATION; the operator still supplies severity, description and reason. No creation privilege hides the shortcut; final QA_RELEASED/REJECTED roots hide it consistently with the existing backend guard. Backend organization, lineage and final-quality checks remain authoritative.
- T5 parameter table shows 范围核对 using exact decimal arithmetic and the existing frozen definition. Same-unit inclusive bounds produce 范围内/超范围. No limits, absent definitions, invalid numbers or different units remain neutral. This is presentation, not a persisted QC conclusion, new gate or approximation of server unit conversion. Original values and full evidence stay available.
- Production creation formerly hit an unrelated incoming-form leave guard after a successful save. The parent now delegates production dirty/saved protection to QualityCommandForm. Chromium proves dismissing an unsaved leave keeps the entered form, and successful creation navigates normally. Incoming inspection record/report visual components are unchanged.

## Targeted verification

- Typecheck PASS; final build includes fresh vue-tsc and passes in 13.15s.
- Vitest: workbenchFacts + productionQualityModel, 9 tests PASS, 1.15s. Exact decimal boundaries, negative values, nonnumeric observations, incompatible units and invalid route context covered.
- Final Chromium PC workbench case PASS, 14.7s: 1280×720 geometry, source facts, all three charge evidence records, controlled completion ownership, pending-operation restrictions, permission-filtered deviation navigation, exact posted command context, unsaved-input protection, successful navigation, final QA freeze, no lookup errors, horizontal overflow or console/page errors.
- Direct shared production regressions: sample scope endpoint and CAPA exact signature/history, 2 cases PASS, 10.4s.
- Earlier browser attempts exposed two test selector mismatches and the actual saved-navigation bug; they are not counted as passing evidence. The final fixture preserves paginated API response shape and has no failed lookup presentation.
- Actual Vue in Chromium with API fixtures. This frontend-only increment does not claim new live MariaDB or native backend acceptance.
- Scoped review: no remaining CRITICAL/HIGH in the delivered existing-contract increment. Existing LOW bundle-size warning remains. Full-system audit/regression not run.

## Screenshots

- [PC workbench with deviation privilege](workbench-with-deviation-pc.png)
- [Deviation create with current batch/operation context](deviation-context-pc.png)
- [PC workbench without deviation privilege](workbench-pc-1280x720.png)
- [Full PC workbench](workbench-chromium-desktop.png)

## Remaining gaps — NOT IMPLEMENTED

Material-master photo association, operation photo association/large preview, expected operation planning times and frozen SOP method reference lack corresponding v1.0.17 contracts. A specific data ownership choice has been requested. [Bounded proposal](../../development/DCP-WORKBENCH-GAPS-001-PROPOSED.md) records the recommended ownership and subsequent cross-document gate. The pending new-contract portion remains IN PROGRESS / USER DECISION REQUIRED; this is not complete functional gap closure. Screenshot sample data has not been persisted or fabricated.

Backend/API/DTO/permissions/allowedActions/signatures/QA Release rules changed: NO. Database/migration changed: NO. FINAL BASELINE changed: NO. Inspection record/report redesign: NO. PC-only T5 retained. Existing local database configurations are preserved and excluded from staging.
