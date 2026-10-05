# MES v1.0.15 GitHub Audit Fix List

- Audit date: 2026-10-04
- Repository: `wosonwang-beep/hospital-pharma-mes`
- Code baseline: GitHub `main`
- Authoritative design: `FINAL BASELINE COMPLETE v1.0.15`
- Purpose: targeted remediation by Codex after read-only review.
- Priority rule: FINAL BASELINE v1.0.15 and approved DCPs override this review when any wording conflicts.
- Scope rule: this review does **not** authorize MES-013 or unrelated refactoring.

## 1. Confirmed PASS — do not regress

1. MainBatch/SubBatch compatibility is implemented:
   - no sub-batch => release creates one `DIRECT` ExecutionUnit;
   - sub-batches => release creates one `SUB_BATCH` ExecutionUnit per SubBatch.
2. SubBatch is an internal execution unit and has no independent finished-product release path.
3. Operation predecessor gates are enforced server-side; successors are promoted only after predecessors complete.
4. MaterialEligibility is consumed by real WMS/production flows.
5. Original FAIL inspection results cannot be changed to PASS through normal correction; controlled investigation/signature evidence is required.
6. Test result history is append/revision based.
7. Weighing independent verification uses reauthentication/e-signature.
8. Material charge and reversal are linked to inventory ledger CONSUME / REVERSE_CONSUME.
9. Query/Create/View/Edit routing is generally separated.
10. `list-edit.css` enforces horizontal label/control layout; desktop uses two-column forms and mobile retains horizontal label/control pairing.
11. `genericName`, `englishName`, `aliasName` are retired from current Material commands/UI/search per v1.0.15 while legacy physical data is retained.
12. Approved MES-012 IPC stage is implemented; the rest of MES-012 remains IN PROGRESS.

## 2. HIGH issues — must fix

### HIGH-01 — MaterialLot contains obsolete stage text
**Evidence:** `frontend/mes-web/src/views/wms/MaterialLotView.vue`

Current UI still says quality inspection/release is a later module and the lot cannot be used in production. This contradicts the accepted MES-008A/MES-011 implementation.

**Fix**
- Remove the obsolete stage statement.
- Replace it with current business truth: production eligibility is controlled by material quality release, inventory status/freeze, expiry/retest rules, and production-context eligibility.
- Do not imply incoming quality is unimplemented.

**Acceptance**
- No obsolete “later quality module / cannot be used in production at this stage” text remains.
- Text matches actual v1.0.15 gates.

### HIGH-02 — Material issue list contains obsolete “Gate not ready” warning
**Evidence:** `frontend/mes-web/src/views/wms/WmsListView.vue`

The issue page always displays that production batch/material eligibility gates are not ready and issue writes are unavailable. Backend now implements MainBatch, eligibility, reservation and inventory gates.

**Fix**
- Remove the fixed obsolete warning.
- If guidance is retained, describe actual dynamic prerequisites instead of claiming the function is unavailable.

**Acceptance**
- Valid users are no longer told that an implemented function is unavailable.

### HIGH-03 — MaterialLot 360° view is incomplete
**Evidence:** `frontend/mes-web/src/views/wms/MaterialLotView.vue`

Current page exposes only Overview and Inventory although the system now has receipt, inspection request, sampling, sample, inspection, report, release, inventory, production-use, trace and audit facts.

**Fix**
- Build a read-oriented MaterialLot 360° aggregation without creating a second Source of Truth.
- Prefer existing APIs/trace data and existing detail routes.
- Provide navigation/visibility for at least:
  Receipt → Inspection Request → Sampling → Sample → Inspection → Inspection Report → QA Release → Inventory → Production Use → Audit/Trace.
- Controlled actions remain on their dedicated work pages.

**Acceptance**
- From one MaterialLot the user can inspect or navigate through the full incoming-quality lineage and downstream usage.
- No duplicated mutable business state is introduced.

### HIGH-04 — Production order query contract incomplete
**Evidence:** `frontend/mes-web/src/views/production/ProductionListView.vue`

Current filters are only keyword + status. Backend already supports `productId`, `plannedDateFrom`, `plannedDateTo`.

**Fix**
- Add order number/keyword, product, planned-date range and status.
- Use business pickers/enums.
- Preserve filters across paging/navigation.

### HIGH-05 — Main batch query contract incomplete
**Evidence:** `frontend/mes-web/src/views/production/ProductionListView.vue`

**Fix**
- Add batch number/keyword, product, production order, planned-date range and status.
- Use business pickers/enums.
- Preserve filters across paging/navigation.

### HIGH-06 — Production status filter is free text
**Evidence:** `frontend/mes-web/src/views/production/ProductionListView.vue`

**Fix**
- Replace arbitrary text status input with frozen status enum Select.
- Order and MainBatch must use their applicable state sets.
- Display localized labels but submit canonical enum values.

### HIGH-07 — Incoming Quality UI duplicates the state machine
**Evidence:** `frontend/mes-web/src/views/quality/IncomingDetailView.vue`

`available(action)` hard-codes action/state mappings in Vue, creating a second state-machine Source of Truth.

**Fix**
- Prefer server-returned `allowedActions` plus permission checks.
- If read DTOs do not yet expose complete `allowedActions`, add them centrally on the server.
- Do not retain a full duplicate state machine in Vue.

**Acceptance**
- Button availability matches server executable actions after state transitions.
- Focused tests cover inspection request, sampling, sample, inspection, report and material release actions.

### HIGH-08 — Evidence for cross-operation/sub-batch equipment mutual exclusion is insufficient
**Evidence:** `backend/mes-execution/src/main/java/com/hospital/mes/execution/application/ExecutionService.java`

Review found equipment usability/type/calibration checks, but no clear transaction-level proof that one equipment item cannot be RUNNING simultaneously in conflicting operations/execution units. `openRuns` creates RUNNING records for bound usages.

**Fix procedure**
1. First search the complete current code/database constraints/tests for an equivalent equipment occupancy gate.
2. If equivalent protection already exists, close this item with exact code + test evidence; do not duplicate it.
3. If missing, implement minimal server-side transaction/concurrency protection based on equipment identity.
4. It must prevent race conditions, not merely hide buttons in UI.
5. Add a concurrency test: two different operations/sub-batches attempt to run the same equipment; at most one succeeds.
6. Pause/complete must release active occupancy so the equipment can subsequently be used.

**Migration rule**
- Never edit an executed Flyway migration.
- If a DB-level addition is genuinely required, use the next physical migration only after confirming it is within approved v1.0.15 design governance.

## 3. MEDIUM issues

### MEDIUM-01 — Production list lacks direct Edit navigation
**Evidence:** `ProductionListView.vue`

When permission + server action/state allow editing, expose Edit navigation to the independent `/edit` route. Do not add inline/modal editing.

### MEDIUM-02 — Trace uses raw IDs
**Evidence:** `frontend/mes-web/src/views/production/TraceView.vue`

Replace raw MainBatch/MaterialLot ID inputs with business pickers supporting batch/lot/product/material search while preserving the “exactly one root” rule.

### MEDIUM-03 — Navigation information architecture is too flat
**Evidence:** `frontend/mes-web/src/layouts/AppLayout.vue`

Group navigation into business domains such as Master Data, Production, WMS, Quality, MES Execution, QA and Platform. Preserve route and permission semantics.

### MEDIUM-04 — Desktop/mobile navigation definitions are duplicated
**Evidence:** `frontend/mes-web/src/layouts/AppLayout.vue`

Use one navigation configuration rendered by both desktop and mobile menus to prevent future drift.

### MEDIUM-05 — MaterialLot direct navigation should be unified
As part of HIGH-03, provide consistent navigation to QA release, trace and audit and the related incoming-quality records. Do not move controlled actions into the aggregation page.

## 4. Explicitly out of scope

1. **Do not start MES-013.** Final two-level batch review, finished-product Release Gate and final eBR/PDF archive remain formal MES-013 scope. Current `mes-release` is only a skeleton.
2. Do not modify executed Flyway migrations.
3. Do not restore retired material name fields to current Material UI/API.
4. Do not collapse Query/Create/View/Edit back into Modal/Drawer CRUD.
5. Do not bypass audit, e-signature, idempotency, If-Match/optimistic locking, qualification or MaterialEligibility gates.
6. Do not refactor unrelated modules.
7. Visual/Taste guidance never overrides FINAL BASELINE v1.0.15.

## 5. Required execution order

### Phase A — low-risk UI corrections
- HIGH-01
- HIGH-02
- HIGH-04
- HIGH-05
- HIGH-06
- MEDIUM-01
- MEDIUM-02

STOP after targeted validation and report.

### Phase B — MaterialLot 360°
- HIGH-03
- MEDIUM-05

Prefer existing APIs/Trace; no second Source of Truth.

STOP after targeted validation and report.

### Phase C — action/state consistency
- HIGH-07

Confirm/complete server `allowedActions`, then remove duplicated Vue state-machine logic.

STOP after targeted validation and report.

### Phase D — GMP equipment occupancy
- HIGH-08

First prove whether equivalent protection already exists. Implement only if missing. Must include concurrency validation.

STOP after targeted validation and report.

### Phase E — navigation IA
- MEDIUM-03
- MEDIUM-04

No business-route/API/permission semantic change.

## 6. Minimum validation per phase

- Frontend build and affected focused unit tests.
- Affected desktop/mobile browser flows; static compilation alone is insufficient.
- Backend changes require targeted domain/integration tests.
- HIGH-08 requires a real concurrency test.
- Report CRITICAL/HIGH findings after each phase.
- Do not infer human acceptance.
- Do not start the next phase automatically.

## 7. Codex execution instruction

Before changing a listed issue, perform a **CURRENT STATE CONFIRMATION**. If the current repository already contains an equivalent correct implementation, close the item with exact code/test evidence and do not duplicate it.

Execute only the explicitly authorized phase. Keep FINAL BASELINE v1.0.15 and approved DCPs authoritative. Preserve existing Source of Truth boundaries, permissions, GxP controls, audit/e-signature/idempotency/optimistic-lock behavior. No broad repository refactor.

After the phase, output a focused report containing:
- Issue ID → FIXED / ALREADY CORRECT / BLOCKED
- changed files
- targeted tests/build/browser flows
- remaining risks
- CRITICAL/HIGH remaining
- whether the next phase is technically ready

Then STOP and wait for human instruction.
