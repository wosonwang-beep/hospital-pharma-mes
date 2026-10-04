# 08 OpenAPI Full Interface Contract V1.0.10 FROZEN

## Coverage
- Paths: 120
- Operations: 171
- Duplicate operationId: 0
- Write operations missing Idempotency-Key: 0
- Operations missing permission metadata: 0

## Contract rules
All state-changing operations use explicit command endpoints; clients must not update status fields directly.
All writes require `Idempotency-Key`. Version-sensitive updates/actions use `If-Match`.
Every operation carries `x-permission` and `x-mes-task`; GxP writes carry `x-audit-required`.
403=permission, 409=state/concurrency, 422=domain rule.

## Modules
IAM; Master Data; Process/BOM/Route; eBR Definition/Runtime; WMS; Production; MES Execution; QMS; Material Balance; QA Release; Traceability; Audit; Integration.

## Source of truth constraints
`POST /material-charges` confirms actual material consumption and drives QuantityEvent/InventoryLedger/Genealogy.
`POST /release-decisions` creates immutable QA decision; QC endpoints never release inventory.
eBR runtime endpoints operate on frozen batch snapshot, not latest template.

# V1.0.10 Integrity and Ownership
The full OpenAPI contains 158 paths and 214 operations. It is serialized as JSON-compatible YAML 1.2 for deterministic integrity checking. The concrete MES-001 schemas replace Generic DTOs for audit query, integration query/retry and generic signing, and add `/auth/reauth`. DCP-MES-002 adds concrete incoming-material policy, receipt, request, sampling, inspection execution/result, report, scoped release, and eligibility schemas and operations. Generic signing is owned by MES-001; provider ownership is annotated for MES-007, MES-008A, and MES-013. API success bodies use the platform API wrapper; errors use the frozen `ApiError`.

`ReleaseDecisionCommand` is shared but scope is selected by the resource path: `/qa/material-lots/{lotId}/release-decisions` is `INCOMING_MATERIAL`; `/qa/main-batches/{batchId}/release-decisions` is `FINISHED_PRODUCT`. `SYSTEM_RULE` is server-owned and only valid for the frozen inspection-exempt evaluation. Clients cannot use a generic release endpoint to bypass scope-specific gates.



## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.

## DCP-MES-006-R2-001 current contract

The approved 00_DESIGN_CHANGE_DCP-MES-006-R2-001_APPROVED.md is normative for MES-006 and supersedes prior contradictory process/product/eBR scope prose. Product is owned here; material has no business version; eBR is independently owned by MES-007. Process signature binds immutable business version and definition content. New physical V011 only. Test requirements include TC-PROC-004.


## DCP-MES-007-008-SEQUENCING-001 authoritative delta

Read `00_DESIGN_CHANGE_DCP-MES-007-008-SEQUENCING-001_APPROVED.md` and the stage contract appendices. Explicit human approval preserves the full MES-007/008 functionality and required tests, while separating current implementation from later real integration. LG-007A definition/Designer/DSL/published contracts is the current MES-007 gate; LG-007B remains MES-009 after LG-009A, with its operation_execution_id FK installed and validated by LG-010. LG-008 reservation/issue main_batch_id FKs are installed and validated by LG-009A. Before these physical producers exist, dependent writes fail closed; no placeholder batch/operation rows, bypassed qualification, or mutable regulated history. MES-008A owns actual MaterialEligibilityService/release evidence; no quantity-only eligibility.

The existing independent receipt edit UI is retained. `PUT /wms/receipts/{id}` uses `wms:receipt:update`, edits only DRAFT authored receipt fields, requires optimistic version, reason, idempotency and same-transaction audit, and returns 200; Confirmed receipt facts (recordStatus APPROVED) cannot be edited. Formal contracts are the concrete OpenAPI and stage appendices. All current labels/control pairs stay horizontal on desktop/mobile.

Current targeted tests prove only current-stage capabilities; deferred runtime/eligibility/batch tests remain required. MES-007 and MES-008 remain IN PROGRESS until every original applicable gate passes against real producer contracts. The approved stage does not authorize implementation of MES-008A/009/010 or marking tasks ACCEPTED.


## DCP-MES-008A-CONTRACT-001 approved bounded delta

Read `00_DESIGN_CHANGE_DCP-MES-008A-CONTRACT-001_APPROVED.md`. Its six independent create/execute routes, existing workbench mapping, nullable incoming MainBatch references, MES-009 delayed FK ownership and canonical finished_lot_id target supersede conflicting inherited wording only within this boundary. Existing fields, API/permissions, state machines and GxP requirements remain unchanged. Unresolved incoming implementation-map gaps are not resolved by this release.
