# DCP-QA-PLAN-QUALIFICATION-001 — PROPOSED / NOT AUTHORIZED

**Date:** 2026-10-08  
**Authority:** FINAL BASELINE COMPLETE v1.0.23. This note does not modify the frozen GxP contract.  
**Disposition:** DESIGN CHANGE REQUIRED. Do not implement without explicit design authority approval.

## Verified current behavior

The approved production quality plan is created by `ProductionQualityPlanService`. In `approve()` it checks:

- Permission `qms:plan:approve`;
- `DRAFT` plan and `DRAFT` production batch, with exact optimistic version;
- Independent approver (neither creator nor last updater);
- Selectable finished material and independently approved frozen QC standard;
- Valid frozen QC-standard signature;
- One-time reauthentication and `ebr:sign` in `SignedRecordSupport.sign()`;
- Frozen digest, approval signature, audit and immutable dispatch snapshot.

The inspected approval path contains **no explicit personnel-qualification check** comparable to `IncomingActorAdapter.requireQualified(..., "qa-release")` or `FinishedReleaseService.authorizedQa()`. `SignedRecordSupport` checks `ebr:sign` and the reauthentication token, not a specific QA plan-approval qualification.

This is an **identified qualification-policy gap**, not proof of a successfully executed unauthorized approval. Current role configuration may assign the permission only to qualified employees, but that administrative convention is not itself a service-level qualification gate.

## Proposed bounded change (pending approval)

1. Define whether independent approval of the *production quality plan* requires a separately registered `qa-plan-approve` qualification or reuses the formally approved `qa-release` qualification. Prefer a distinct competency code if the roles may differ.
2. On `ProductionQualityPlanService.approve()`, enforce an active, date-valid, organization-scoped QA approval qualification for the actual actor, before signing and state change. Missing mapping must fail closed, as other qualification gates do.
3. Preserve all current independent reviewer, signature, frozen QC standard, content hash, optimistic-locking, idempotency and audit controls.
4. Align the read-model `allowedActions` with role/qualification and independent approval constraints; the UI can continue to suppress actions but must not be relied upon as the only gate.
5. Do not rewrite already approved quality plans, their signatures, historic qualification state, or process snapshots.

## Required negative/positive tests

- Authorized `qms:plan:approve` + `ebr:sign` **without valid qualification** -> forbidden; plan stays `DRAFT`, no approval signature.
- Expired, inactive or wrong-organization qualification -> forbidden.
- Qualified plan creator/last editor attempting self-approval -> `INDEPENDENT_REVIEW_REQUIRED`.
- Qualified independent QA with valid reauthentication and approved QC standard -> approves once, signs, audits and freezes; replay obeys idempotency.
- Missing mapping -> fail closed, with clear business error.
- Frozen plan cannot be edited or reapproved after successful approval.

## Approval and document consistency

Before implementation, approve a Design Change ID and a chosen qualification-code policy; then update impacted PRD, Domain/GxP rules, permission/qualification matrix, API/error contract, state-machine guards, UI, Test Cases and RTM together. No migration or new permission should be introduced unless justified by the approved solution.

Related existing HIGH review items remain separate:
- `docs/review/MES_DEMO_STRENGTH_FORMULA_INCONSISTENCY_2026-10-08.md` — frozen formula versus labeled strength;
- `docs/review/MES_DEMO_ROLE_EVIDENCE_INCONSISTENCY_2026-10-08.md` — historical demo record duties.
