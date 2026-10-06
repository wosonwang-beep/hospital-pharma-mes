# DCP-MES-002-R2-001 — APPROVED

## 1. Authority

- Decision: **APPROVED by user / Design Authority**
- Target baseline: `FINAL BASELINE COMPLETE v1.0.18`
- Supersedes: `FINAL BASELINE COMPLETE v1.0.1`
- Subject: Authorized Design Change Governance + Incoming Material Quality Lifecycle
- Prior baseline handling: v1.0.1 is retained unchanged as immutable history.

## 2. Approved scope

This Design Change authorizes only the following frozen-contract changes:

1. Add repository-wide Authorized Design Change Governance.
2. Add versioned `MaterialVersion.requiresIncomingInspection`, default `true`, and snapshot it to `MaterialLot` on receipt.
3. Add the six controlled records: 原辅料收货记录、请验单、取样记录、检验记录、检验报告、物料放行记录.
4. Freeze the chain `MaterialReceipt → MaterialLot → InspectionRequest → SamplingTask → SamplingDetail → Sample → InspectionTask → InspectionItem → TestExecution → TestResultRevision → InspectionReport → ReleaseDecision → AVAILABLE`.
5. Define the inspection-exempt path without fabricated sampling or inspection records.
6. Standardize the physical material-lot table name as `md_material_lot`.
7. Generalize `qms_release_decision` for `INCOMING_MATERIAL` and `FINISHED_PRODUCT` while preserving it as the only quality-release Source of Truth.
8. Separate `quality_status`, `inventory_status`, and `record_status`.
9. Add `MaterialEligibilityService` as the shared Reservation/Issue/Weighing/Charge gate.
10. Update MES-004-R2, MES-008-R2, MES-011-R2, MES-012-R2, MES-013-R2 and add MES-008A-R2, including their dependency graph.
11. Synchronize all affected product, architecture, domain, database, state, API/OpenAPI, permission, functional, UI, GxP, test, RTM, migration, integration, and task artifacts.

## 3. Explicit exclusions

- No business code is implemented.
- No physical Flyway migration is created or executed.
- No executed Flyway migration is edited, renamed, reordered, or checksum-changed.
- No v1.0.1 artifact is modified or deleted.
- No second inventory, test-result, material-charge, or release Source of Truth is introduced.

## 4. Frozen system invariants

- Physical receipt never means production eligibility.
- Inspection-required material reaches `AVAILABLE` only after approved inspection evidence and a valid QA `ReleaseDecision`.
- Inspection-exempt material skips sampling/testing records but still requires valid MaterialVersion, Supplier, approved Material-Supplier relationship, receipt checks, and a system-rule `ReleaseDecision`.
- `QC_PASSED != RELEASED`; `quality_status=RELEASED` and `inventory_status=AVAILABLE` are separate effects.
- Reservation, Issue, Weighing, and Charge consume the same `MaterialEligibilityService` result and fail with `422 MATERIAL_NOT_ELIGIBLE` when the gate is not satisfied.

