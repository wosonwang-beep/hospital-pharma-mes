# MES-008A-R2 — Incoming Material Quality & Material Release

## 0. Authority

`FINAL BASELINE COMPLETE v1.0.6` and approved `DCP-MES-002-R2-001` are authoritative. Implement only this card; missing or conflicting specification outside the approved scope means STOP.

## 1. Requirement IDs

QMS-IN-001, QMS-SMP-001, QMS-TST-001, QMS-RPT-001, QMS-MREL-001, QMS-EXM-001, WMS-ELG-001

## 2. Mandatory design references

- PRD: `01_PRD_V1.0.6_FROZEN.md` — DCP-MES-002 incoming requirements.
- Architecture/Domain: `02_ARCHITECTURE_V1.0.6_FROZEN.md`, `04_DOMAIN_MODEL_DETAILED_V1.0.6_FROZEN.md`.
- Database: `05_DATABASE_DESIGN_V1.0.6_FROZEN.md` — incoming QMS tables, sample/result/release extensions, canonical `md_material_lot`.
- State: `07_STATE_MACHINE_DETAILED_V1.0.6_FROZEN.md` — incoming quality and controlled-record states.
- API/OpenAPI: `08_API_DETAILED_V1.0.6_FROZEN.md`, `08_OPENAPI_FULL_V1.0.6_FROZEN.yaml`.
- Functional/UI/GxP: `09_FUNCTIONAL_DETAILED_V1.0.6_FROZEN.md`, `10_UI_PAGE_DETAILED_V1.0.6_FROZEN.md`, `11_GMP_AUDIT_ESIGNATURE_V1.0.6_FROZEN.md`.
- Test/RTM: `12A_TEST_ACCEPTANCE_V1.0.6_FROZEN.md`, detailed catalog, RTM.
- Dependency/Integration: Section 15 v1.0.6 matrices and contracts.

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

