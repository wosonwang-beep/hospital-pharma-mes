# 08 OpenAPI Full Interface Contract V1.0.18 FROZEN

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

# V1.0.18 Integrity and Ownership
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


## Approved incoming full-chain completion in v1.0.18

Read `00_DESIGN_CHANGE_DCP-INCOMING-QUALITY-GAPS-001_APPROVED.md` and the four `00_INCOMING_*_CONTRACT_V1.0.18.md` appendices. Their explicit columns, state guards, API schemas, permissions, signing envelopes, UI fields, tests and dependency ownership supersede contradictory inherited text within the approved scope only. Former DG-01..08 and BC-01/02 now have implementation contracts; delivery is still subject to actual code and required validation. No full workflow PASS follows from publication.


## Authorized v1.0.18 incoming completion supplement

Human approval covers the bounded contract completion and full incoming acceptance tasks. For eBR runtime, issue returns, weighing verification and actual trace identities, the following precise supplements supersede generic or conflicting clauses in this chapter; unaffected contracts remain unchanged.

- [00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.18.md](00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.18.md)
- [00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.18.md](00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.18.md)
- [00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.18.md](00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.18.md)


## Authorized v1.0.18 material weighing policy producer

The approved full incoming acceptance scope includes this missing producer. [00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.18.md](00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.18.md) governs deployment configuration, immutable production snapshot, consumers and required tests. It does not restore retired material master fields or change material APIs. It supersedes earlier references to nullable legacy material weighing fields. No new table, permission, route or status.


## v1.0.18 approved trace-read completion

See [00_INCOMING_TRACE_STANDARD_V1.0.18.md](00_INCOMING_TRACE_STANDARD_V1.0.18.md) for exact frozen QC standard node and immutable signature-policy identity evidence. Unaffected contracts remain unchanged.


## Approved functional closure delta — v1.0.18

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.18.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.18.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.
