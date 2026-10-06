# Material Product Design implementation QA

2026-10-06. Selected target: last displayed design (third image, 分栏阅读), exec-cfc5aab3-5224-4b85-9081-64c334ef49af.png. Existing Global UI V2 / FINAL BASELINE v1.0.18 retained. T3 detail / T2 create-edit.

Source and rendered detail captured at the same **1487 × 1058** dimensions and combined in design-comparison.png for inspection. Actual Vue with API-shaped fixtures; no live database browser acceptance asserted.

Iteration: initial read-only labels inherited 34px line-height, making the audit strip fall below the target frame. Override only material read-only labels to 22px; final detail fits the same frame. Initial create/edit checkbox was centered within its grid track; now starts at the same control line. Remarks use a readable 100px textarea. All changes scoped to material-detail-page.

Final result: **PASS for bounded UI V2 / T2-T3 implementation**. No outstanding P0/P1/P2 issue identified in the bounded page implementation. This is not a pixel-identical claim: existing global shell, approved font/control sizes, real permission-driven controls and standard empty icon remain authoritative. The design's staggered right-column divider is aligned with the left-column divider for a stable shared form grid. These minor visual differences are retained, not new business design.

Main surface: left basic identity then unit/packaging; right management/quality then remarks. Eleven fields and original DOM/tab order remain. Name/code/status header; existing tabs; compact conversion empty state; existing audit/disable area aligned horizontally. Create/edit share sections; change reason remains full row. Existing footer submit targets the same form and payload. No backend/API/DTO/database/migration/permission/state/signature/allowedActions/authority change.

Typecheck PASS; 9 affected unit tests PASS; final targeted Chromium desktop flow 1 PASS (10.3s); final build PASS (8.96s). Explicit section position assertions, existing field order, hidden payload, supplier facts and save/navigation regression retained. Same-size capture of all three pages has no horizontal overflow or page errors. No unrelated full regression. Inherited LOW build chunk-size warning remains.

Evidence: 01-detail.png, 02-edit.png, 03-create.png, design-comparison.png. Production implementation: MaterialDetailView.vue; targeted regression: material-field-cleanup.spec.ts. Human acceptance on 2026-10-06: “验收，提交和推送”; current acceptance status is maintained only in MES_TASKS.md. Commit/push to main explicitly authorized. Earlier taste refinement evidence is a historical checkpoint superseded visually by this selected design.
