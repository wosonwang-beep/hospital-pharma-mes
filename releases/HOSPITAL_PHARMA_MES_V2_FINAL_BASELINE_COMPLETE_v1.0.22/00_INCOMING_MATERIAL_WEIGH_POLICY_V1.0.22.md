# Frozen production weighing policy completion

2026-10-03. Published supplement under the human-approved full incoming producer-contract completion. Effective in v1.0.22 after cross-document review. Previous releases remain immutable. DCP-MATERIAL-BASIC-001 retired advanced material maintenance; this supplement does not restore it or read nullable legacy weighing columns.

## Deployment producer

The controlled deployment operator supplies `mes.production.material-weighing-policies` as a list of records. Each record has exactly `organizationId` (positive ID string), `materialId` (positive ID string), `required` (explicit boolean), `precision` (positive DECIMAL(18,6) string expressed in the material's current base UOM), `tolerancePct` (nonnegative DECIMAL(9,6) string), and `policyVersion` (nonblank controlled identifier, maximum 80 characters). There must be exactly one matching organization/material pair. No fallback by material type, critical flag, material code, RBAC, unit precision or legacy columns is permitted. Precision and tolerance are required even when `required=false`, since an optional actual weighing still requires controlled validation. Duplicate pairs, incomplete values, unsupported precision, absent mappings and blank identifiers fail closed with `WEIGHING_POLICY_REQUIRED` and identify the affected material.

Configuration is controlled deployment evidence, not an editable business master, table, route or permission. Existing qualification mappings and scale-equipment-types remain separate controls. Test configuration binds the exact unique material ID created by its real fixture; test code does not substitute a permissive policy producer.

## Release snapshot and consumers

At MainBatch RELEASE, after validating the real published process/eBR and current usable material, `ProductionMaterialPolicyService.requirePolicy(orgId, materialId, baseUnitId)` validates the explicit mapping and produces the immutable object `{organizationId,materialId,required,precision,tolerancePct,policyVersion,baseUnitId,configurationHash}`. IDs and decimals are strings. The canonical configuration hash covers every preceding property, including the actual baseUnitId, excluding its own hash. Decimal strings are normalized without exponent notation. This service belongs to mes-production and consumes the basic material query; no reverse module dependency is added.

Store each material's existing basic snapshot with an additional server-owned `weighingPolicy` object in `prd_process_snapshot.snapshot_json.materials[]`. The enclosing process snapshot hash covers it. This is immutable consumed evidence, not a change to Material read/command DTOs or to receipt/lot material_snapshot_json. No extra migration or column is needed. Batch RELEASE is atomic: a missing mapping creates no released batch, execution, form instance or audit-success record. Idempotent replay returns original release without reading current configuration.

Weighing and charge resolve the material's frozen `weighingPolicy` from the batch snapshot only, never live deployment configuration or legacy material columns. Validate its exact organization/material/base-unit identity and canonical hash before use. Weighing precision is converted exactly from the frozen base UOM into the weighing UOM. Tolerance is the inclusive absolute deviation percentage already frozen in the weighing contract. Required=true forbids direct unweighed charge; required=false permits the existing exact-BOM direct-charge route. Every actual supplied weighing still requires independent signed VERIFY under v1.0.12. Missing, malformed or tampered historical policy fails closed; older snapshots are never backfilled or rewritten. A new draft batch released after a configuration change receives the new policy; existing released batches retain the old one.

## Contract, UI and verification

The generic ProcessSnapshot JSON read gains a typed `FrozenMaterialWeighingPolicy` under `materials[].weighingPolicy`; basic material APIs and UI remain unchanged. The existing release screen displays an actionable server blocker naming a material with missing deployment policy. Weighing workbench shows the frozen required flag, precision, tolerance and policy version from batch evidence and cannot edit them.

Required verification: missing/blank/duplicate/negative policy rejects release atomically; org and material identities cannot cross-bind; explicit required=false alone permits direct charge; precision/tolerance boundaries; real configured calibrated equipment; mandatory independent signed verification; real unique qualification record; config change after release leaves target/precision/tolerance/signature history unchanged; forged or absent frozen policy rejects consumption. Native receipt→QC→QA release→batch release→reservation→issue→weigh→verify→charge→reverse trace uses real producers, unique rollback-only fixtures and exact test deployment bindings. No mocked eligibility, policy, stock or qualification can establish acceptance.

Root publication must update affected Domain, API/read schema, UI, Integration, Test/RTM and frozen database snapshot descriptions together. No new business state, table, material field, mutation endpoint or permission is introduced.


## Approved functional closure delta — v1.0.22

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.22.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.22.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.
