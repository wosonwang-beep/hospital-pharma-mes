# 08 OpenAPI Full Interface Contract V1.0.1 FROZEN

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

# V1.0.1 Integrity and Ownership
The full OpenAPI contains 121 paths and 172 operations. It is serialized as JSON-compatible YAML 1.2 for deterministic integrity checking. The concrete MES-001 schemas replace Generic DTOs for audit query, integration query/retry and generic signing, and add `/auth/reauth`. Generic signing is owned by MES-001; provider ownership is annotated for MES-007 and MES-013. API success bodies use the platform API wrapper; errors use the frozen `ApiError`.
