# Global UI V2 Phase 1.1 — scoped refinement plan

Approval: user explicitly requests FIX-01..04 only, existing templates T1/T2/T6 and existing Playwright Chromium. No Phase 2, new MES task, backend/DTO/API/permission/state/signature/database/baseline changes.

Contract review: v1.0.16 approved DCP and completion contract §5 say FINAL is generated only after a valid finished QA decision; PENDING_QA can print REVIEW_COPY. Final PDF is a post-decision archive step, not a prerequisite in server Release Gates. Existing server review-model contains six gates. Display those alone in checklist/progress; move archive presentation to an independent post-decision section without changing actions.

1. Capture before evidence through the real Vue app with existing Chromium fixtures; inspect T1/T2/T6/drawer.
2. Narrow material-only selectors: compact T1 filters/actions at desktop, natural height around110–125px; compact empty table; mobile single column. T2 form max-width1100px with two columns and95px mobile labels; preserve field order/long text spans.
3. QA-only display label formatter on the recursive evidence renderer; Chinese section/metadata labels, same source values and histories; default renderer behavior elsewhere unchanged.
4. Separate post-decision archive presentation; keep server gate list/count, canDecide and generation/signature controls unchanged.
5. Targeted unit/component checks, npm run typecheck/build and existing QA/material browser regressions plus refinement screenshots/measurements, desktop/mobile and wide desktop. Inspect saved after images against before. Report metrics, limits, immutable contract proof and readiness. Stop.
