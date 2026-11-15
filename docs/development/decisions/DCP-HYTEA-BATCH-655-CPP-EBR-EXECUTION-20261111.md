# DCP-HYTEA-BATCH-655-CPP-EBR-EXECUTION-20261111

Status: USER-DIRECTED DEV EXECUTION SCOPE; no change to frozen product rules.
User instruction (2026-11-11): "喉咽茶生产批655的质量计划666 QA签署、批次下达，随后进行八道工序的CPP/eBR执行验证。"
Authority: FINAL BASELINE COMPLETE v1.0.23, AGENTS.md, MES_DEV_RULES.md, existing approved production/eBR/CPP decisions and user instruction.
Environment: local hospital_pharma_mes_dev only, existing main workspace.

## Exact scope
1. QA user 11569 independently approves/signs production quality plan 666 for main batch 655 (created by user 11556), using the existing quality-plan approval command and user-entered reauthentication. Verify signatureId/contentHash/status before release.
2. Production user 11572 releases batch 655 using the currently active matching process package 2 and published matching EBR template version 497, only after fresh GET and version checks. The release freezes the process, material and quality-plan snapshots and initializes one direct execution unit with eight operation/form instances.
3. Before starting operation 1, inspect actual batch-655 snapshot and material eligibility. If required by the existing gate, reserve and confirm issues for the six frozen BOM lines using eligible lots, then create/independently verify weighing records and charge materials to the correct operation. Do not bypass WMS, weighing, qualification, stock or material-balance gates.
4. Execute the eight frozen operations sequentially, respecting predecessor order. Each operation requires its own signed clearance record and independent signed clearance review before start. Record frozen route CPP values through the existing manual parameter API, run the frozen CPP/eBR validation rules, fill and submit the matching EBR form, complete its required independent review/signature, then complete the operation.
5. Stop after the eight operations and EBR/CPP evidence have been verified. Do not mark batch production complete, submit finished QA, release finished stock or archive the batch unless separately requested.
## Data identity and limits
- This is a DEV software execution exercise. If no contemporaneous plant measurements exist, any constructed CPP/eBR observations must be identified only in an external evidence manifest as synthetic DEV fixture values; never claim actual physical production, real measured process parameters, actual employee work, or GMP release.
- Business fields must retain normal business values and must not be populated with "test/demo/simulated" labels. Do not reuse historic batch 582 records or measurements as batch 655 execution.
- Preserve original material quantities, formula, operation sequence, CPP limits, eBR form definitions, role/qualification rules, audit, idempotency, optimistic version locks and electronic signatures.
- No direct SQL business writes, no schema migration, no permission/qualification changes without a separately approved DCP, no second workspace/worktree, no Git stage/commit/push.

## Required identities
- QA approver: user 11569, independent from plan creator 11556.
- Production executor: user 11572, MES_PRODUCTION.
- Warehouse confirmation: existing independent warehouse role if required by material issue flow.
- Independent clearance/weighing/EBR reviewer: existing authorized reviewer, different from recorder/operator.
- Passwords must be entered by the user in the local application only; do not read, log, or copy credentials into scripts or notes.

## Acceptance
- Plan 666 APPROVED with valid signature and immutable content hash; batch 655 RELEASED with snapshot referencing plan 666, process package 2, EBR template 497; exactly one direct execution unit.
- Eight operations in frozen order, each required clearance passed by independent signature, CPP values and rule outcomes persisted, eight matching EBR instances submitted/reviewed/signed as required, all operations completed and predecessor chain correct.
- Real REST readback and audit/signature evidence recorded. Tests alone do not constitute batch 655 execution evidence.
- Any missing role permission, qualification, stock, equipment, frozen field, review signer, or EBR requirement is a stop condition; report the exact blocker instead of modifying IAM or frozen design.
