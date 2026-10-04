# 08 OpenAPI Full Interface Contract V1.0.5 FROZEN

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

# V1.0.5 Integrity and Ownership
The full OpenAPI contains 158 paths and 214 operations. It is serialized as JSON-compatible YAML 1.2 for deterministic integrity checking. The concrete MES-001 schemas replace Generic DTOs for audit query, integration query/retry and generic signing, and add `/auth/reauth`. DCP-MES-002 adds concrete incoming-material policy, receipt, request, sampling, inspection execution/result, report, scoped release, and eligibility schemas and operations. Generic signing is owned by MES-001; provider ownership is annotated for MES-007, MES-008A, and MES-013. API success bodies use the platform API wrapper; errors use the frozen `ApiError`.

`ReleaseDecisionCommand` is shared but scope is selected by the resource path: `/qa/material-lots/{lotId}/release-decisions` is `INCOMING_MATERIAL`; `/qa/main-batches/{batchId}/release-decisions` is `FINISHED_PRODUCT`. `SYSTEM_RULE` is server-owned and only valid for the frozen inspection-exempt evaluation. Clients cannot use a generic release endpoint to bypass scope-specific gates.



## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.
