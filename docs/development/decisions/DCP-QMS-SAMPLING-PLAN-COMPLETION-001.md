# DCP-QMS-SAMPLING-PLAN-COMPLETION-001 — bounded DEV workflow repair

Status: APPROVED BY USER — 2026-11-10 (current conversation, explicit response: “批准”).
Approval scope: exact bounded additive change and acceptance criteria in this DCP; no additional business, schema, permission, qualification, workflow, migration, UI or unrelated source changes are authorized.
Baseline: FINAL BASELINE COMPLETE v1.0.23, AGENTS.md, MES_DEV_RULES.md. The explicit approval above authorizes only the bounded additive repair described here for the DEV end-to-end test.

## Problem evidence
Existing DEV incoming requests 721–726 are ACCEPTED. Sampling tasks 689–694 were created by POST /api/v1/quality/sampling-tasks and assigned to qualified DEV sampler 11566. Current SamplingTaskCommand and createSampling persist samplingPlan and requiredPackageCount only. They do not persist plannedSamplingQuantity or plannedUnitId. However submit-to-warehouse later converts actual sample details into plannedUnitId and compares to plannedSamplingQuantity. This makes those tasks impossible to submit to warehouse and causes a null-dependent failure. Do not update these rows through SQL or bypass warehouse confirmation.

## Bounded change
1. Add POST /api/v1/quality/sampling-tasks/{id}/plan with If-Match and Idempotency-Key.
2. Add strict SamplingPlanUpdateCommand fields: versionNo, plannedSamplingQuantity, plannedUnitId, reason.
3. Permission: existing qms:sampling:assign (QC analyst). Only PLANNED or ASSIGNED tasks may be changed; never after start or sampling detail creation.
4. Convert planned quantity to request unit using existing MasterQueryService conversion, require positive valid quantity <= original inspection request quantity; require planned unit compatible with the material. Existing requiredPackageCount must remain within request package count.
5. Persist plannedSamplingQuantity and plannedUnitId using existing mutation/audit/optimistic-lock/idempotency pipeline. No signature is added because this is a pre-execution configuration command; subsequent sampler submit-to-warehouse and warehouse outbound retain their existing signatures and cross-role checks.
6. The existing six tasks 689–694 can be repaired via this API without deletion/recreation. Set 0.125 kg per material as an explicitly identified DEV-only sample plan, with the source paper records remaining provenance for product and batch quantities; the sample amount itself is a DEV execution fixture, not historical measured evidence.
7. No change to incoming QC state machine, permissions, qualifications, inventory movement, physical sample outbound, QC test results, QA material release, eBR or production. No DB migration or schema change.

## File write scope
- backend/mes-qms/src/main/java/com/hospital/mes/qms/api/IncomingQualityController.java
- backend/mes-qms/src/main/java/com/hospital/mes/qms/application/IncomingQualityService.java
- backend/mes-qms/src/main/resources/incoming-command-schemas.json
- backend/mes-security/src/main/java/com/hospital/mes/security/SecurityConfiguration.java — bind the new route to the already-approved qms:sampling:assign authority only; no new permission code or role grant
- backend/mes-security/src/test/java/com/hospital/mes/security/IncomingCrossroleRouteAuthorizationTest.java — prove the existing permission is required and anonymous/other-authority calls are rejected
- backend/mes-boot/src/test/java/com/hospital/mes/incoming/IncomingCrossroleBatchBIT.java (or the narrow existing sampling integration test)
- MES_TASKS.md and this DCP evidence

Implementation clarification (within the approved permission contract): the new endpoint must be registered in the ordered HTTP security matcher using the same existing qms:sampling:assign authority enforced by the application service. This is route wiring, not a permission/role/qualification change.

## Acceptance
- Unit/contract: schema rejects missing/extra fields, invalid quantity and unknown unit.
- MariaDB integration: valid update from ASSIGNED persists with version increment; quantity above request is rejected; task started or details already present cannot be edited; wrong permission rejected; replay idempotent; no audit/row partial writes on failure.
- Runtime DEV: update six existing tasks using QC analyst; sampler starts and records DEV sample detail, submits with valid signature; warehouse independently confirms sample outbound with valid signature; QC analyst receives sample and binds generated task. Never call complete as a substitute for warehouse outbound.
- No direct SQL writes, no role/qualification changes, no test assertion weakening, no worktree, no migration, no Git stage/commit/push.
