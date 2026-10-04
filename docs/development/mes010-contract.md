# MES-010 implementation contract review — pending design reconciliation

This is a development handoff, not an authoritative baseline and not approval to change a frozen contract. v1.0.9 remains authoritative until the parent completes the authorized release review. User authorization covers implementing MES-009/010; the separate incoming-quality DCP does not automatically authorize the new conflicts below. No product code, migration, database operation, or test execution was performed for this review.

## Scope and physical allocation

- MES-010 owns OperationExecution, EquipmentUsage, EquipmentRun and ParameterValue; only `backend/mes-execution/**`, its boot integration tests and new physical V017 are assigned to this implementer.
- Main thread confirmed successful physical history V001–V013. Allocation: V014 incoming quality, V015 production, V016 eBR runtime (parent), V017 execution. Existing migrations remain immutable.
- V017 creates the four existing execution tables and installs/validates `ebr_form_instance.operation_execution_id -> mes_operation_execution.id`. It must fail on orphan references rather than silently remove or rewrite them. No production, weighing, charge, IPC, balance or final QA implementation is included.
- MES-003/006 producer contracts exist. Real MES-009 ExecutionUnit/ProcessSnapshot and MES-007 runtime contracts are being developed; their availability is a prerequisite to the corresponding implementation gates. Future required IPC/balance evidence is fail-closed until its real producer exists; this does not justify a permissive substitute or inventing new state-event APIs.

## Exact existing API and UI surface

The companion JSON records seven existing operations and their original permissions. All seven currently use GenericRequest/GenericResponse in the v1.0.9 OpenAPI. Its `MES-010-R2;MES-011-R2` tag also appears on weighing/material-charge operations; those belong to MES-011 and are excluded here.

| Method/path | Permission | Purpose |
|---|---|---|
| GET /execution-units/{id}/operations | mes:operation:view | Existing unit's ordered operation records, gates, equipment usages/runs, parameter definitions/values |
| POST /operations/{id}/start | mes:operation:start | READY to IN_PROGRESS after real gates |
| POST /operations/{id}/pause | mes:operation:pause | IN_PROGRESS to PAUSED |
| POST /operations/{id}/resume | mes:operation:start | PAUSED to IN_PROGRESS after gates |
| POST /operations/{id}/complete | mes:operation:complete | IN_PROGRESS to COMPLETED after completion gates |
| POST /operations/{id}/equipment-usages | mes:equipment:bind | Controlled equipment binding using existing usage fields |
| POST /operations/{id}/parameter-values | mes:param:record | Append parameter evidence against a frozen operation parameter definition |

Responses retain HTTP 200 and ApiResponse envelopes. All mutations require Idempotency-Key and operation If-Match/versionNo, nonblank reason and same-transaction audit. IDs/decimals are strings, versionNo integer, timestamps UTC ISO-8601. Unknown fields are rejected; server owns actors, operation state, timestamps for user commands, qualification conclusions and parent/definition relationships. 400 malformed, 403 permission, 404 absent/cross-org, 409 state/version/idempotency conflict, 422 failed gate/reference. Runtime errors must not disclose other-organization data.

Formal UI is UI-EXEC-W, `/mes/execution/:id`, `mes:operation:view`; UI mapping calls GET `/execution-units/{id}/operations`. The screenshot's decorative `/execution-units/...` and `mes:execution:view` labels are inconsistent with the formal route/mapping and must not generate a new route or permission. Keep its operation navigation, current-operation parameter panel and control panel; labels/control pairs remain horizontal. Show only actual server-backed metrics/evidence. The displayed live availability percentage has no current producer and must not be fabricated. Equipment inputs use existing equipment lookup; parameter definitions and bounds come from the batch's frozen snapshot.

## Existing persistence contract

All rows carry existing org/actor/time/version metadata. No hard deletes or generic updateStatus API.

- `mes_operation_execution`: execution_unit_id FK, operation_def_id FK, operation_seq, status, started_at nullable, completed_at nullable, operator_id nullable FK.
- `mes_equipment_usage`: execution_unit_id FK, operation_execution_id FK, equipment_id FK, usage_role, clearance_status nullable, qualification_status nullable, bound_at.
- `mes_equipment_run`: equipment_usage_id FK, run_no unique, status indexed, started_at, ended_at nullable, source_message_id nullable indexed.
- `mes_parameter_value`: operation_execution_id FK, parameter_def_id FK, source_mode indexed, raw_value decimal(24,8) nullable, text_value varchar(1000) nullable, unit_id nullable FK, source_message_id varchar(100) nullable indexed, captured_at indexed.

References are organization-scoped and validated against the exact batch snapshot; no latest-definition queries determine execution behavior. Parameter values retain raw source/time/message evidence. Values are appended, never overwritten; reject fractional precision loss rather than silently rounding. Source-message deduplication must compare content and preserve one business value for a replay. Final unique-key scope requires the bounded ruling below.

## Producer/consumer coordination without a Maven cycle

Production implementer proposed `ProductionQueryService.execution(org, executionId)` and `lockExecution(org, executionId)` returning `ExecutionContext(mainBatchId, executionUnitId, subBatchId?, batchStatus, executionStatus, snapshot)`. Snapshot keys are `process`, `ebr`, `materials`; exact nested shape must match the producer's frozen contract. `ProductionEvents.BatchReleased(org,batchId,executionUnitIds)` is synchronous and initializes real operation/runtime rows in the same release transaction. No placeholder unit/operation rows.

Dependency direction: mes-execution -> mes-production; production does not depend on mes-execution. Named `startExecution`/`completeExecution` production operations may be invoked only after actual operation gates. Keep any production/eBR initialization coordination in consumer events/shared interfaces to avoid an eBR/execution/production cycle. The parent owns eBR runtime adapters. The eBR operation-completion gate must consume required field, rule, review and signature facts; it must not infer completion from form existence.

Process snapshot supplies operationCode/name, sequenceNo, predecessorCodes, requiredRole, requiredEquipmentType, clearanceRequired, completionRule and parameter definitions. The already-approved completionRule grammar is null, leaf EBR_COMPLETE/PARAMETERS_WITHIN_LIMITS/REVIEW_COMPLETE, or ALL/ANY trees. Do not add IPC/balance leaf names or execute arbitrary expression text. Required future IPC/balance checks remain fail-closed consumers, not new MES-010 business implementations.

## New frozen conflicts for one consolidated DCP

1. **Failed gate state conflicts.** PRD MES-OP-001, FD-OP-001 and integration matrix specify failed gate -> BLOCKED; TC-OP-002 explicitly expects 422 with unchanged Operation. Evidence: PRD lines 309–317; FD lines 117–129; test lines 277–283; integration row Operation. Minimal proposed ruling: rejected commands leave state unchanged, consistently returning failed-gate evidence. Any BLOCKED semantics require explicit reconciliation of the existing state contract, not a new unblock/event API invented here.
2. **Clearance fact authority is absent.** `proc_operation_def.clearance_required` and `mes_equipment_usage.clearance_status` exist, and start requires server-side clearance, but no clearance producer, evidence lifecycle or command is defined. Current EquipmentQueryService validates only equipment status/calibration. Minimal alternatives for owner selection: explicitly recognize a controlled clearance observation using the existing binding field with audited actor/time and server validation, or name the real existing clearance producer; until then, clearance-required operations fail closed. A client-supplied `PASSED` flag alone is not established evidence.
3. **EquipmentRun lifecycle lacks an authoritative control contract.** TC-EQP-002 requires start/pause/resume/end and an unoverwritten usage/run chain; the API has no equipment-run command, and run status enums/lifecycle are undefined. Minimal proposal for approval: use the existing operation start/pause/resume/complete commands to own run intervals, append a new interval on resume, preserve closed intervals and bind lineage to EquipmentUsage; no new endpoint/table. Independently controlled equipment runs would require a separately approved API change.
4. **Role-to-qualification selection is undefined.** Operations have requiredRole; personnel qualifications use qualification_code. There is no frozen mapping that makes these identical or identifies required qualification codes per operation. Do not silently use role code as qualification code. The owner must specify the exact producer lookup rule, retaining independent role and usable qualification checks; missing required evidence fails closed.
5. **Parameter device-message identity needs a bounded uniqueness ruling.** Existing fields/API suffice for an authorized device producer to send frozen parameterDefId/sourceMessageId/sourceMode/rawValue/unitId/capturedAt; no new mapping table is inherently required. But sourceMessageId is only indexed, while TC-PAR-001 mandates one value on replay. Decide whether a message identifies one value or may contain multiple parameter values, and freeze the uniqueness scope (e.g. org + operation + parameter + source message). Identical replay returns the existing evidence, different contents conflict. Do not deduplicate unrelated parameter values merely because one device message carried both.

These are contract decisions, not completed implementation findings. Generic DTOs alone, optional future MES-011/012 gates and missing future module runtime are not additional design blockers.

## Required targeted evidence after approval/implementation

- TC-OP-001 illegal transition -> 409 INVALID_OPERATION_STATE.
- TC-OP-002 required IPC evidence missing -> 422 with the authoritative post-DCP state behavior; actual IPC producer integration remains a later gate.
- TC-EQP-002 usage/run lineage, usable equipment/calibration gate, no rewriting closed run evidence.
- TC-PAR-001 same source message replay creates one value; conflicting replay, separate parameter identities, source/time preservation.
- Same-org snapshot/reference enforcement, roles/qualification/clearance failure, prerequisite ordering, immutable frozen definitions, required eBR fields/review/signatures, exact parameter limits/UOM conversion, audit rollback, optimistic version/idempotency and two-transaction operation contention.
- Existing database is preserved; rollback unique test records where possible. No full regression, executed-migration edit, database reset or automated PROD access.

Task status remains in MES_TASKS.md only. This document makes no READY/ACCEPTED claim.
