# MES-008A-R2 — Incoming Material Quality & Material Release

## 0. Authority

`FINAL BASELINE COMPLETE v1.0.18` and approved `DCP-MES-002-R2-001` are authoritative. Implement only this card; missing or conflicting specification outside the approved scope means STOP.

## 1. Requirement IDs

QMS-IN-001, QMS-SMP-001, QMS-TST-001, QMS-RPT-001, QMS-MREL-001, QMS-EXM-001, WMS-ELG-001

## 2. Mandatory design references

- PRD: `01_PRD_V1.0.18_FROZEN.md` — DCP-MES-002 incoming requirements.
- Architecture/Domain: `02_ARCHITECTURE_V1.0.18_FROZEN.md`, `04_DOMAIN_MODEL_DETAILED_V1.0.18_FROZEN.md`.
- Database: `05_DATABASE_DESIGN_V1.0.18_FROZEN.md` — incoming QMS tables, sample/result/release extensions, canonical `md_material_lot`.
- State: `07_STATE_MACHINE_DETAILED_V1.0.18_FROZEN.md` — incoming quality and controlled-record states.
- API/OpenAPI: `08_API_DETAILED_V1.0.18_FROZEN.md`, `08_OPENAPI_FULL_V1.0.18_FROZEN.yaml`.
- Functional/UI/GxP: `09_FUNCTIONAL_DETAILED_V1.0.18_FROZEN.md`, `10_UI_PAGE_DETAILED_V1.0.18_FROZEN.md`, `11_GMP_AUDIT_ESIGNATURE_V1.0.18_FROZEN.md`.
- Test/RTM: `12A_TEST_ACCEPTANCE_V1.0.18_FROZEN.md`, detailed catalog, RTM.
- Dependency/Integration: Section 15 v1.0.18 matrices and contracts.

## 3. Dependency Check

- Hard Dependencies: MES-001, MES-002, MES-003, MES-004, MES-005, MES-008
- Soft Dependencies: —
- Every hard dependency must be ACCEPTED or its required frozen produced contract must be physically available. Otherwise STOP with `DEPENDENCY_NOT_READY`.

## 4. Scope and ownership

Implement the 请验单、取样记录、检验记录、检验报告、物料放行记录 and the incoming-material quality states. Freeze the chain:

`MaterialLot → InspectionRequest → SamplingTask → SamplingDetail → Sample → InspectionTask → InspectionItem → TestExecution → TestResultRevision → InspectionReport → ReleaseDecision → AVAILABLE`.

Required-inspection lots must complete this chain. Inspection-exempt lots create none of the intermediate QMS records; they must validate MaterialVersion, Supplier, approved Material-Supplier relationship, receipt checks and the frozen policy before creating `ReleaseDecision(INCOMING_MATERIAL, RELEASED, INSPECTION_EXEMPT, SYSTEM_RULE)`.

## 5. Contracts consumed

Platform Audit/eSignature/idempotency; IAM permissions; Org/UOM/person qualification; MaterialVersion; Supplier and approved Material-Supplier relation; MaterialReceipt/MaterialLot/InventoryLedger.

## 6. Contracts produced

InspectionRequest; SamplingTask/Detail; Sample; InspectionTask/Item; TestExecution; TestResultRevision; InspectionReport; incoming ReleaseDecision; MaterialEligibilityService.

Downstream consumers: MES-008 Reservation/Issue, MES-011 Weighing/Charge, MES-012 IPC/QMS investigations, MES-013 unified finished-product release.

## 7. UI and permissions

- UI: quality workbench, inspection requests, sampling tasks, samples, inspection tasks/records, inspection reports, material-lot QA review/release, MaterialLot 360° timeline.
- Permissions: `qms:inspection-request:*`, `qms:sampling:*`, `qms:test:*`, `qms:report:*`, `qa:material-release:view`, `qa:material-release:decide` as frozen in API design.

## 8. Required tests

TC-QMS-IN-001; TC-QMS-SMP-001..003; TC-QMS-TST-001..003; TC-QMS-RPT-001..002; TC-QMS-MREL-001..004; TC-WMS-006..008; TC-ELG-001..004.

Tests must cover normal/abnormal paths, state, permission, optimistic locking, idempotency, transaction rollback, append-only revisions, audit/signature, exempt non-fabrication, and concurrency.

## 9. Forbidden

- No second release, inventory, result, or material-charge Source of Truth.
- No generic status update endpoint and no physical deletion.
- No direct `MaterialLot → Sample` creation without SamplingTask/Detail lineage.
- No mutation of approved reports, result history, or ReleaseDecision; use append/supersede.
- No modification of executed Flyway migrations and no implementation outside this card.

## 10. Required evidence and stop condition

Provide new-migration diff, backend/frontend files, targeted test results, contract/integration review, and unresolved risk list. When acceptance and affected regression pass, set only `READY FOR ACCEPTANCE` in the root task index and STOP; do not start MES-011.



## DCP-MES-007-008-SEQUENCING-001 authoritative delta

Read `../00_DESIGN_CHANGE_DCP-MES-007-008-SEQUENCING-001_APPROVED.md` and the stage contract appendices. Explicit human approval preserves the full MES-007/008 functionality and required tests, while separating current implementation from later real integration. LG-007A definition/Designer/DSL/published contracts is the current MES-007 gate; LG-007B remains MES-009 after LG-009A, with its operation_execution_id FK installed and validated by LG-010. LG-008 reservation/issue main_batch_id FKs are installed and validated by LG-009A. Before these physical producers exist, dependent writes fail closed; no placeholder batch/operation rows, bypassed qualification, or mutable regulated history. MES-008A owns actual MaterialEligibilityService/release evidence; no quantity-only eligibility.

The existing independent receipt edit UI is retained. `PUT /wms/receipts/{id}` uses `wms:receipt:update`, edits only DRAFT authored receipt fields, requires optimistic version, reason, idempotency and same-transaction audit, and returns 200; Confirmed receipt facts (recordStatus APPROVED) cannot be edited. Formal contracts are the concrete OpenAPI and stage appendices. All current labels/control pairs stay horizontal on desktop/mobile.

Current targeted tests prove only current-stage capabilities; deferred runtime/eligibility/batch tests remain required. MES-007 and MES-008 remain IN PROGRESS until every original applicable gate passes against real producer contracts. The approved stage does not authorize implementation of MES-008A/009/010 or marking tasks ACCEPTED.


## DCP-MES-008A-CONTRACT-001 approved bounded delta

Read `../00_DESIGN_CHANGE_DCP-MES-008A-CONTRACT-001_APPROVED.md`. Its six independent create/execute routes, existing workbench mapping, nullable incoming MainBatch references, MES-009 delayed FK ownership and canonical finished_lot_id target supersede conflicting inherited wording only within this boundary. Existing fields, API/permissions, state machines and GxP requirements remain unchanged. Unresolved incoming implementation-map gaps are not resolved by this release.


## Approved incoming full-chain completion in v1.0.18

Read `../00_DESIGN_CHANGE_DCP-INCOMING-QUALITY-GAPS-001_APPROVED.md` and the four `00_INCOMING_*_CONTRACT_V1.0.18.md` appendices. Their explicit columns, state guards, API schemas, permissions, signing envelopes, UI fields, tests and dependency ownership supersede contradictory inherited text within the approved scope only. Former DG-01..08 and BC-01/02 now have implementation contracts; delivery is still subject to actual code and required validation. No full workflow PASS follows from publication.


## v1.0.18 approved trace-read completion

See [00_INCOMING_TRACE_STANDARD_V1.0.18.md](../00_INCOMING_TRACE_STANDARD_V1.0.18.md) for exact frozen QC standard node and immutable signature-policy identity evidence. Unaffected contracts remain unchanged.


## Approved bounded functional closure

Read ../00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.18.md §§3–8. This approved delta adds inventory decision commands (MES-008), eligibility integration (MES-008A/011), clearance and IPC consumers (MES-010), and only the staged IPC producer (MES-012). Actual physically available upstream contracts satisfy this staged producer dependency; other MES-012 scope remains unchanged.


## Approved audit HIGH delta — v1.0.18

[00_AUDIT_HIGH_CONTRACT_V1.0.18.md](../00_AUDIT_HIGH_CONTRACT_V1.0.18.md) governs only productionOrderId query publication, incoming read-only allowedActions and exclusive active equipment occupancy. All other inherited contracts remain unchanged. No schema/migration, new permission/route/state or signature/release-rule change.


## Approved audit HIGH delta — v1.0.18

[00_AUDIT_HIGH_CONTRACT_V1.0.18.md](../00_AUDIT_HIGH_CONTRACT_V1.0.18.md) governs only productionOrderId query publication, incoming read-only allowedActions and exclusive active equipment occupancy. All other inherited contracts remain unchanged. No schema/migration, new permission/route/state or signature/release-rule change.
