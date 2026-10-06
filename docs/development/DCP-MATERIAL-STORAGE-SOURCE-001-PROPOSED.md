# DCP-MATERIAL-STORAGE-SOURCE-001 — APPROVED

Scope approval: “授权修改数据库”; ownership decision: “推荐每条‘物料—供应商’关系指定厂家，收货冻结来源”, 2026-10-06. The approved cumulative contract is [v1.0.18](../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.18.md); consistency PASS precedes pointer switch. This historical PROPOSED filename is retained for link continuity; current status is APPROVED, not a new Source of Truth. Formal receiving freeze point is CONFIRM/direct RECEIVE; current locked reads and a retryable concurrency conflict govern stale snapshots. Workbench image/time/SOP proposals remain unapproved.

## Retained proposal history

# DCP-MATERIAL-STORAGE-SOURCE-001 — authorized scope, ownership decision pending

Date: 2026-10-06. Current authority: FINAL BASELINE COMPLETE v1.0.17. User explicitly replies “授权修改数据库” to the Material Master field cleanup's two contract gaps. This authorizes bounded database correction; it does not approve arbitrary manufacturer ownership, unapproved image/planning/SOP contracts, destructive rebuild or migration-history edits.

## Storage environment — bounded approach

Reuse existing nullable md_material.storage_condition VARCHAR(500); no duplicate storage column/table. Publish storageCondition in material read DTO and existing closed Create/Update commands, validate existing nullable length, persist/audit via existing Material aggregate and optimistic-lock/reason/idempotency handling. T1/T2/T3 label is 存储环境. No client status, new route/permission/state/release/signature rule. Prior detached consumer snapshots remain unchanged. The existing migration and legacy storage values must be preserved.

## Manufacturer source — USER DECISION REQUIRED

Recommended: each existing material-supplier relationship owns one nullable manufacturerName. Alternative: each Supplier owns one fixed manufacturer shared by all supplied materials. A choice is required before finalizing physical schema or public write contracts; these are different business ownership models, not interchangeable display labels.

For the recommended choice, extend the existing relationship rather than create an unrequested manufacturer master subsystem. Existing supplier/material identity, qualification, preferred and validity facts remain authoritative. Receipt source freezes the approved relationship's manufacturer at the controlled receipt lifecycle point; the final lifecycle point must be stated in the approved contract. MaterialLot reuses its existing receiptItemId provenance for reads rather than maintain a second editable manufacturer identity. Empty historical source remains unknown; do not backfill it from current material master, supplier changes or a manufacturer lot number. The legacy material-master manufacturerName is retained in existing contracts but hidden in Material Master UI and never relabelled as verified receipt-source manufacturer.

## Consistency and implementation gate

After the ownership answer, finalize the bounded design and required field/PK-FK/lifecycle/read-write mapping, then create a cumulative release preserving v1.0.17 unchanged. Align Database, Domain, State Machine constraints, API/OpenAPI, UI T1/T2/T3/source context, Permission, Audit/Signature, Test/RTM, Migration and direct integration/task dependency artifacts; unchanged areas are explicitly recorded. Review consistency before changing authority pointers.

Query native hospital_pharma_mes_dev flyway_schema_history before allocating a new physical Flyway version. All new schema deltas use a new migration; no repair, reset or old migration edit. Existing storage column needs no duplicate migration. Tests use rollback/unique fixtures; prove storage persistence, hidden-field preservation, source snapshot stability after relationship edits, historical unknown sources, org isolation and existing audit/concurrency/preferred constraints. Run affected compiler/unit/native integration and targeted Chromium only, not full system regression.

## Current disposition

Scope authorization recorded. Manufacturer ownership question is pending. No new product code, schema migration, database write or authority switch performed in this preparation. Previous UI-only cleanup remains uncommitted and preserved. Material Master Image, Operation Execution Photo, Planned Operation Time, SOP/Method Reference remain PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED. Global UI V3 remains NOT IN CURRENT SCOPE.
