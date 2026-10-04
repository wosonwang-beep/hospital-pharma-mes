# DCP-MES-004-005-R2-001 — proposed material/supplier contract completion

Status: PROPOSED, not approved, not authoritative. The 2026-10-03 approval covers four MES-003 gaps only. This proposal identifies newly discovered MES-004/MES-005 gaps; it does not permit implementation or baseline switching.

## Verified conflict and missing contracts

In active v1.0.3, `08_API_DETAILED_V1.0.3_FROZEN.md` POST /materials and `12_TEST_CASE_DETAILED_V1.0.3_FROZEN.md` TC-MAT-001 require 201, but `08_OPENAPI_FULL_V1.0.3_FROZEN.yaml` POST /materials declares 200 only. Its request is GenericRequest + the inspection-policy boolean, and material actions/version/supplier-assignment requests are GenericRequest. Full typed material/supplier status enums are not defined, although the API DTO rule requires OpenAPI-declared enums. The Material aggregate names submit/approve/version commands; the functional design says DRAFT→APPROVED→INACTIVE, without defining submitted-version status or the version selected by /materials/{id}/approve.

## Proposed bounded resolution

1. POST /materials returns 201 to match detailed API and TC-MAT-001; update OpenAPI accordingly.
2. Type all existing material/material-version/supplier/material-supplier request and response schemas from the existing frozen physical fields. IDs and decimals are strings; complete quality/storage/production objects retain all frozen controls, including requiresIncomingInspection default true. No new table/column, API path, permission or task dependency is proposed.
3. Material root states DRAFT/APPROVED/INACTIVE; material-version states DRAFT/SUBMITTED/APPROVED. Existing submit/approve actions take explicit versionId and optimistic version. Root identity/common descriptive fields are edited only while DRAFT; approved rules remain immutable and changes use /materials/{id}/versions. Each version read uses its explicit ID; submit/approve target that ID, never silently another version. Disable changes the root to INACTIVE while preserving approved version records and historical references. Material type remains a required controlled string (max 30) with existing FINISHED meaning, without inventing extra business categories.
4. Supplier qualification states UNAPPROVED/APPROVED/INACTIVE. Existing PUT /suppliers/{id} uses named UPDATE/QUALIFY/DISABLE commands; no generic updateStatus. Qualified availability checks status and inclusive UTC validTo. PUT /materials/{id}/suppliers retains its existing frozen master:material:update permission and maintains approved boolean/validTo relationships, using optimistic locking, audit and idempotency; omitted historical relationships are revoked, never deleted. No new approval/signature point is introduced.
5. The already approved LG-004 handoff adds the physical md_unit_conversion.material_id FK after creating md_material; material-specific conversions then validate same-org material existence. MES-005 consumes the complete physical Material contract; WMS receives an ApprovedSupplier query contract checking same-org material, supplier qualification and relationship expiry at use time.

## Governance and verification if approved

Create v1.0.4 retaining v1.0.2/v1.0.3 history. Update affected PRD, Domain, State, API/OpenAPI, UI/form fields, GxP, Test/RTM and Section 15 contracts together, review consistency, then switch authority pointers. All schema implementation remains new physical Flyway migrations after actual V006; no executed migration changes. Implement MES-004 then MES-005 only and run the required targeted cases once, with failure-specific retests. Preserve the local native database and unrelated changes.
