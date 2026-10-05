# MES-013 UI targeted delivery — 2026-10-05

Authority: FINAL BASELINE COMPLETE v1.0.16, approved DCP-MES-012-013-CONTRACT-001. Read MES-013 Task Card, UI-QA-V / UI-REL-W, the completion contract and exact closed OpenAPI schemas. Visually inspected `ui-prototype/screens/UI-QA-RELEASE.png` before implementation. Backend service/API/provider handoff preceded frontend edits.

Implemented original QA checklist on the left and batch summary / allowed actions on the right, using existing Ant Design page and signature patterns. Desktop keeps two columns; mobile stacks the same panels. Original shell, typography, colors and spacing remain. Prototype example values are replaced with actual server facts. No new business fields/routes/permissions.

Routes: `/qa/batches/:id/review` (`qa:batch-review`) and `/qa/batches/:id/release` (`qa:release`), reached from the existing batch detail. Closed release command binds `QA_RELEASE_DECISION`, `batchId:currentVersion`, current record version and only RELEASE / REJECT, with exact displayed digest, quoted If-Match and idempotency key. Server allowedActions govern visible decisions. Explicit predecessor retained for immutable supersession.

eBR view presents original form/value revisions, reviews, rule executions, QC, balance, deviation, process snapshot, operations, charges, quantity/reversal facts, genealogy, signature envelopes and QA decision history. PDF archive table retains manifest versions. Pre-QA generation explicitly selects REVIEW_COPY; FINAL is offered only after a real QA decision state. Download resolves the exact batch manifest, reads the existing attachment content API and verifies SHA-256 bytes against immutable fileHash before saving.

Targeted checks:

- `npm run typecheck`: final PASS.
- `npx vitest run src/views/quality/finishedReleaseModel.test.ts`: 5 / 5 PASS, 1.00s.
- Original five browser flows × desktop and Pixel 5 mobile: 10 distinct cases PASS across first gate and failure-only rerun. First gate 5 / 10 PASS exposed a real stale-review modal issue; 409 now closes the signature dialog so the visible refresh action is usable while the reason remains. Test-only fixes wait for modal animation to settle before strict horizontal bounding-box checks and scope archive type assertions to the table. Only failed five cases rerun: 5 / 5 PASS, 21.4s.
- Download byte verification / tampering blockers × two viewports: additional 4 / 4 PASS, 16.3s. Total 14 distinct API-mocked browser cases.
- Browser plugin not available in the session; used the existing repository Playwright workflow. No new browser dependencies.
- Visually inspected durable `docs/acceptance/mes-013/screens/qa-release-desktop.png` / `qa-release-mobile.png` plus `qa-release-form-desktop.png` / `qa-release-form-mobile.png`: original panel hierarchy retained; no clipping or overlap. Query/action/signature/archive labels and controls remain horizontal. No page runtime errors in the normal signed release flow.

API-mocked browser tests prove UI/API bindings and layout, not native database, stock, audit/signature or PDF generation effects. Native evidence remains owned by the backend integration/E2E gate. No full frontend regression, database action, task acceptance or Git commit/push performed here.

Visual evidence retention: the later download-only Playwright gate cleaned transient test-results screenshots. Captured four durable desktop/mobile mock screenshots separately after the final gate, including actual six server gate code labels and the horizontal decision form. Capture only, no new business test cases and no rerun of the 14-case suite; runtime errors 0 on both viewports.
