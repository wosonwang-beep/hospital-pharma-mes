# FINAL BASELINE COMPLETE v1.0.4 — Frozen Manifest

## Authority

- Status: **FINAL / FROZEN**
- Approved Design Change: `DCP-MES-004-005-R2-001`; cumulative over DCP-MES-003-R2-001; cumulative over DCP-MES-002-R2-001 and DCP-MES-001-R2-001.
- Supersedes active authority: `FINAL BASELINE COMPLETE v1.0.3`
- Historical rule: v1.0.3, v1.0.2 and prior releases remain immutable and available.
- Repository entry points: root `AGENTS.md`, root `MES_TASKS.md`, and `docs/PROJECT_BASELINE.md`.

## Source-of-truth order

1. This manifest, `00_DESIGN_CHANGE_PROPOSAL_DCP-MES-004-005-R2-001_APPROVED.md`, and `00_DESIGN_CHANGE_PROPOSAL_DCP-MES-003-R2-001_APPROVED.md` (bounded MES-003 delta); inherited approved DCP/change traces remain applicable to their prior scopes.
2. Numbered PRD, Architecture, Domain, Database, State, API/OpenAPI, Functional, UI, GxP, Test, RTM and Section 15 artifacts in this directory.
3. Task Cards in `tasks/` for implementation boundaries and dependency gates.
4. Root `MES_TASKS.md` for task status only.

No prototype, generated screenshot, old Word bundle, business code, or migration may override a frozen textual/API contract.

## Authoritative artifacts

- `00_DESIGN_CHANGE_PROPOSAL_DCP-MES-004-005-R2-001_APPROVED.md`

- `00_DESIGN_CHANGE_PROPOSAL_DCP-MES-003-R2-001_APPROVED.md`
- `00_CROSS_CONSISTENCY_REVIEW_V1.0.4.md`

- `00_DESIGN_CHANGE_PROPOSAL_DCP-MES-002-R2-001_APPROVED.md`
- `00_DESIGN_CHANGE_TRACE_DCP-MES-002-R2-001_V1.0.4.md`
- `00_CROSS_CONSISTENCY_REVIEW_V1.0.4.md`
- `01_PRD_V1.0.4_FROZEN.md`
- `02_ARCHITECTURE_V1.0.4_FROZEN.md`
- `03_DOMAIN_OVERVIEW_V1.0.4_FROZEN.md`
- `04_DOMAIN_MODEL_DETAILED_V1.0.4_FROZEN.md`
- `05_DATABASE_DESIGN_V1.0.4_FROZEN.md`
- `06_EBR_DYNAMIC_FORM_DETAILED_V1.0.4_FROZEN.md`
- `07_STATE_MACHINE_DETAILED_V1.0.4_FROZEN.md`
- `08_API_DETAILED_V1.0.4_FROZEN.md`
- `08_OPENAPI_FULL_V1.0.4_FROZEN.yaml`
- `08_OPENAPI_ENDPOINT_CATALOG_V1.0.4_FROZEN.csv`
- `09_FUNCTIONAL_DETAILED_V1.0.4_FROZEN.md`
- `10_UI_PAGE_DETAILED_V1.0.4_FROZEN.md`
- `10_UI_PAGE_ROUTE_MATRIX_V1.0.4_FROZEN.csv`
- `11_GMP_AUDIT_ESIGNATURE_V1.0.4_FROZEN.md`
- `12A_TEST_ACCEPTANCE_V1.0.4_FROZEN.md`
- `12_TEST_CASE_CATALOG_V1.0.4_FROZEN.csv`
- `12_TEST_CASE_DETAILED_V1.0.4_FROZEN.md`
- `12_TEST_COVERAGE_MATRIX_V1.0.4.csv`
- `13_REQUIREMENT_TRACEABILITY_MATRIX_V1.0.4_FROZEN.csv`
- `14_AGENTS.md`
- `15_MODULE_DEPENDENCY_MIGRATION_INTEGRATION_CONTRACT_V1.0.4_FROZEN.md`
- `15_TASK_DEPENDENCY_MATRIX_V1.0.4_FROZEN.csv`
- `15_MIGRATION_DEPENDENCY_MATRIX_V1.0.4_FROZEN.csv`
- `15_LOGICAL_TO_PHYSICAL_MIGRATION_LEDGER_V1.0.4.csv`
- `15_INTEGRATION_CONTRACT_MATRIX_V1.0.4_FROZEN.csv`
- `tasks/MES-001-R2.md` through `tasks/MES-013-R2.md`, including `tasks/MES-008A-R2.md`.

## Frozen DCP-MES-002 invariants

- Canonical lot table: `md_material_lot`.
- Versioned policy: `MaterialVersion.requiresIncomingInspection`, default true, snapshotted at receipt.
- Unified release Source of Truth: `qms_release_decision`, scoped to incoming material or finished product.
- Required chain: `MaterialReceipt → MaterialLot → InspectionRequest → SamplingTask → SamplingDetail → Sample → InspectionTask → InspectionItem → TestExecution → TestResultRevision → InspectionReport → ReleaseDecision → AVAILABLE`.
- Exempt chain: validated receipt/qualification/policy → immutable `INSPECTION_EXEMPT`/`SYSTEM_RULE` ReleaseDecision → `RELEASED + AVAILABLE`; no fabricated inspection evidence.
- `quality_status`, `inventory_status`, and `record_status` are separate.
- Reservation, Issue, Weighing, and Charge share `MaterialEligibilityService`.

## Migration statement

Section 15 migration identifiers are logical groups. The approved design is implemented by separate new physical Flyway files V007/V008, with V009 completing frozen IDX declarations. The ledger records successful DEV execution after V006. Executed V001–V009 remain immutable; no repair, clean, or rebuild was used.

## Integrity

See `00_SHA256SUMS_V1.0.4.txt` and the JSON integrity checks. Cross-consistency result is PASS.
