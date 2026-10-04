# MES-009 contract extraction and unresolved conflicts

Read-only extraction from v1.0.9, 2026-10-03. This working document is NOT an authoritative baseline and does not authorize any design change. No product code, migration, Maven or database action performed.

## Existing operation inventory

All paths below retain original method, operationId and permission. Existing OpenAPI request/response are GenericRequest/GenericResponse; concretizing these from existing fields is not itself a design blocker.

|Method|Path|operationId|Permission|Request shape|Response shape|
|---|---|---|---|---|---|
|GET|/production-orders|listProductionOrders|production:order:view|page,size,keyword,productId,status|Page<Order>|
|POST|/production-orders|createProductionOrders|production:order:create|OrderCreate|Order|
|GET|/production-orders/{id}|getProductionOrders|production:order:view|id|Order|
|PUT|/production-orders/{id}|updateProductionOrders|production:order:update|OrderEdit|Order|
|GET|/main-batches|listMainBatches|production:batch:view|page,size,keyword,productId,productionOrderId,status|Page<MainBatch>|
|POST|/main-batches|createMainBatch|production:batch:create|MainBatchCreate|MainBatch|
|GET|/main-batches/{id}|getMainBatch|production:batch:view|id|MainBatch|
|POST|/main-batches/{id}/release|releaseMainBatch|production:batch:release|BatchRelease|MainBatch|
|POST|/main-batches/{id}/start|startMainBatch|production:batch:start|Transition|MainBatch|
|POST|/main-batches/{id}/complete-production|completeProduction|production:batch:complete|Transition|MainBatch|
|POST|/main-batches/{id}/submit-qa|submitBatchQa|qa:batch-review|Transition|MainBatch|
|GET|/main-batches/{id}/sub-batches|listSubBatches|production:batch:view|id|SubBatch[]|
|POST|/main-batches/{id}/sub-batches|createSubBatch|production:subbatch:create|SubBatchCreate|SubBatch|
|GET|/main-batches/{id}/execution-units|listExecutionUnits|mes:execution:view|id|ExecutionUnit[]|
|GET|/execution-units/{id}|getExecutionUnit|mes:execution:view|id|ExecutionUnit|

HTTP success is 200 in original OpenAPI for every operation; API detailed §POST /main-batches instead specifies 201. This success-code discrepancy requires explicit reconciliation in publication. Every mutation requires organization context, permission, reason, Idempotency-Key, same-transaction audit; updates/transitions require If-Match/versionNo. IDs are decimal strings, quantities decimal strings with precision 18/scale 6, record versions integers. Page fields follow established platform pagination. No client actor/status/organization accepted.

## Field-derived typed shapes (provisional pending conflicts below)

`OrderCreate { orderNo:string[1..80], productId:Id, plannedQty:DecimalPositive, unitId:Id, reason:string }`

`OrderEdit = OrderCreate + { versionNo:integer>=0 }`. Editable state and order lifecycle are unresolved below.

`Order = { id:Id, orderNo:string, productId:Id, plannedQty:Decimal, unitId:Id, status:string, versionNo:integer, createdAt:date-time, updatedAt:date-time }`.

`SubBatchPlan = { subBatchNo:string[1..80], sequenceNo:integer>0, plannedQty:DecimalPositive? }`; nullable quantity is frozen explicitly. No invented equality between each sub-batch amount and full batch amount.

`MainBatchCreate = { batchNo:string[1..80], productionOrderId:Id, productId:Id, plannedQty:DecimalPositive, unitId:Id, subBatchPlan:SubBatchPlan[]?, reason:string }`.

`Transition = { versionNo:integer>=0, reason:string }`.

`BatchRelease = Transition + { packageVersionId:Id, ebrTemplateVersionId:Id }`. Explicit version selectors are needed to address existing frozen FK targets; their placement at release versus create depends on the unresolved snapshot timing conflict.

`SubBatchCreate = Transition + SubBatchPlan` (parent version token).

`MainBatch = { id:Id, batchNo:string, productionOrderId:Id, productId:Id, processSnapshotId:Id, plannedQty:Decimal, unitId:Id, status:BatchStatus, releasedAt:date-time?, versionNo:integer, createdAt:date-time, updatedAt:date-time, subBatches:SubBatch[], executionUnits:ExecutionUnit[], processSnapshot:ProcessSnapshot? }`.

`BatchStatus = DRAFT | RELEASED | IN_PROGRESS | PRODUCTION_COMPLETED | PENDING_QA | QA_RELEASED | REJECTED`. RELEASED here is the explicitly frozen production dispatch state; QA_RELEASED is finished-product release, never inferred from production completion.

`SubBatch = { id:Id, mainBatchId:Id, subBatchNo:string, sequenceNo:integer, plannedQty:Decimal?, status:PENDING|READY|IN_PROGRESS|COMPLETED|CANCELLED, versionNo:integer }`.

`ExecutionUnit = { id:Id, mainBatchId:Id, subBatchId:Id?, unitType:DIRECT|SUB_BATCH, executionNo:string[1..100], status:PENDING|READY|IN_PROGRESS|PAUSED|COMPLETED|BLOCKED, versionNo:integer }`.

`ProcessSnapshot = { id:Id, packageVersionId:Id, formulaVersionId:Id, routeVersionId:Id, ebrTemplateVersionId:Id, snapshotHash:string, snapshot: { process: ProcessQueryService.requireUsable result, ebr: EbrQueryService.requirePublished result, materials: MaterialQueryService.requireUsable snapshots[] } }`. Formula/route source IDs must be obtained from an upstream query extension because existing public snapshot contains authored formula/route values but not physical formula/route IDs. No legacy MaterialVersion consumed.

## Physical mapping and cross-module contract

- prd_production_order: order_no, product_id→md_product, planned_qty, unit_id→md_unit, status plus frozen common mutable metadata.
- prd_main_batch: batch_no, production_order_id→prd_production_order, product_id→md_product, process_snapshot_id→prd_process_snapshot (currently NOT NULL), planned_qty, unit_id→md_unit, status, released_at plus mutable metadata.
- prd_sub_batch: main_batch_id→prd_main_batch, sub_batch_no, sequence_no, planned_qty nullable, status plus mutable metadata.
- prd_execution_unit: main_batch_id→prd_main_batch, sub_batch_id→prd_sub_batch nullable, unit_type, execution_no, status plus mutable metadata. DIRECT requires null sub_batch_id; SUB_BATCH requires a same-parent child.
- prd_process_snapshot: package_version_id, formula_version_id, route_version_id, ebr_template_version_id, snapshot_json, snapshot_hash plus immutable common metadata. No later master/definition edit rewrites a snapshot.
- MES009 installs delayed WMS reservation/issue MainBatch FKs; proposed approved008A handoff also installs QMS MainBatch FKs. Physical version allocated only after parent checks successful history. No old migrations modified.

Proposed Java producer signatures (implementation-only design, no new public HTTP):

```java
ProductionQueryService.BatchContext batch(long org, long batchId);
ProductionQueryService.BatchContext lockBatch(long org, long batchId);
ProductionQueryService.ExecutionContext execution(long org, long executionId);
ProductionQueryService.ExecutionContext lockExecution(long org, long executionId);
record BatchContext(long mainBatchId, String status, JsonNode snapshot) {}
record ExecutionContext(long mainBatchId, long executionUnitId, Long subBatchId,
                        String batchStatus, String executionStatus, JsonNode snapshot) {}
record BatchReleased(long organizationId, long mainBatchId, List<Long> executionUnitIds) {}
```

Consumers receive organization-scoped detached snapshot trees. Lock variants require active transaction and lock parent before child. mes-execution may depend on mes-production; production must not depend on execution. Synchronous BatchReleased listener creates real operations/runtime in the release transaction; any listener/audit failure rolls back all state. Completion feedback uses named domain commands/events rather than generic updateStatus. WMS boot adapter consumes BatchContext and actual eligibility; no permissive fallback. eBR runtime uses consumer port implemented in boot to avoid production↔ebr dependency cycle. Production completion and QA submission must fail closed for absent future balance/QMS gates; MES011+ is not preimplemented.

## Real frozen conflicts requiring bounded owner decision

1. **Snapshot timing/nullability.** Database lines 100 and domain lines 29 require every MainBatch to reference an immutable snapshot (NOT NULL). State machine lines 8 and TC-BAT-001 lines 237–243 require DRAFT→release to create that snapshot atomically with ExecutionUnits/Audit. Thus DRAFT is explicitly MainBatch state, not merely order state. No approved v109 delta supersedes these clauses. Creating the final snapshot at draft creation changes the frozen release timing and either freezes draft definitions prematurely or replaces an already immutable snapshot at release. Minimal proposed correction: allow process_snapshot_id null only while DRAFT, require non-null once released, keep final immutable snapshot creation solely in release transaction. This changes a frozen constraint and requires approval.
2. **Batch edit route has no save API.** UI detailed lines 258–261 and route matrix UI-BAT-E freeze /production/batches/:id/edit and production:batch:update. OpenAPI /main-batches/{id} lines 5466–5539 contains only GET; no PUT anywhere for this resource. Minimal proposed correction: add PUT /main-batches/{id} using existing edit permission and only DRAFT fields/reason/version/idempotency/audit, 200. This is a new API and requires approval; do not silently remove the edit route instead.
3. **Order lifecycle absent.** PRD-001 lines 269–277 acceptance requires correct order state, table lines 448–454 has status, UI allows create/edit/state actions, but state document defines no ProductionOrder states/transitions and Domain has no ProductionOrder aggregate lifecycle. Minimum owner decision is the actual order status enum plus derivation/transition semantics and edit boundary. Do not invent DRAFT→RELEASED or permanently DRAFT implementation.
4. **Planned dates have no business source.** UI lines 222–223 and 244–245 require planned-date filters/result columns, but order/batch tables have no plan date column; batch only released_at. Common created_at is not a planned date. Owner must approve bounded planned-date fields or explicit UI removal; implementation must not relabel createdAt.
5. **SubBatch forbidden-release test has no route.** TC-BAT-002 explicitly requires an API attempt returning 422 SUBBATCH_RELEASE_FORBIDDEN, while no sub-batch release operation exists in OpenAPI. Parent should decide whether existing domain rejection/API type misuse coverage satisfies test without adding a path, or specify an approved negative-route contract; no new release capability is proposed.

The first four are business/schema/API conflicts, not GenericDTO concerns. Required producer ProcessQueryService/EbrQueryService/MaterialQueryService/MasterQueryService implementations are physically available; module mes-production currently contains only pom.xml. Scoped tests have not run because implementation has not begun.

## Concrete minimal approval proposal (not yet authorized)

- **Draft selections:** keep package/eBR version selection solely in BatchRelease request; order and batch draft hold no new version-selector columns. Release dialog chooses explicit usable packageVersionId and published ebrTemplateVersionId, requires matching product and eBR process package version. Consumer query retrieves formula/route physical IDs for these existing snapshot FK columns. Thus a draft needs no temporary snapshot, JSON draft surrogate, or second source of truth.
- **Snapshot:** nullable process_snapshot_id with CHECK `(status = 'DRAFT' AND process_snapshot_id IS NULL) OR (status <> 'DRAFT' AND process_snapshot_id IS NOT NULL)`; only named release may set it once. Immutable snapshot hash/content never updated. Snapshot identity hash must include mainBatchId or immutable snapshot may be shared for equal content: choose shared same-org exact-content snapshot, serialize lookup/insert by hash and verify content equality. This preserves existing unique hash and avoids adding non-designed content.
- **Batch edit:** add PUT /main-batches/{id}; accept batchNo, productionOrderId, productId, plannedQty, unitId, plannedDate, versionNo, reason; only DRAFT and no immutable snapshot. Existing sub-batches are retained; their own plannedQty is independent, never overwritten with parent plannedQty. Existing POST sub-batches only while DRAFT; parent lock and parent version protect changes.
- **Dates:** add nullable `planned_date DATE` to prd_production_order and prd_main_batch, request/response `plannedDate: YYYY-MM-DD|null`, exact date filtering as `plannedDateFrom`/`plannedDateTo`. It means intended production date, not creation/release date, is independently authored for order and each batch, editable only under their draft rules. UI production-date column uses real batch releasedAt labelled 下达时间 unless owner instead requires a distinct actual start timestamp; no fabricated start/completion timestamp is added. State-machine start/completion timing can be evidenced by immutable transition AuditEvent timestamps, but if UI/API require stored startedAt/completedAt that needs explicit field approval.
- **Order state:** minimum useful enum DRAFT, IN_PROGRESS, COMPLETED. Create is DRAFT. Order remains DRAFT while no child batch has been released and its basic authored values remain editable. First child release atomically sets IN_PROGRESS; this permanently closes ordinary editing. When sum of child MainBatch planned quantities converted to order UOM equals order plannedQty and every child is PRODUCTION_COMPLETED/PENDING_QA/QA_RELEASED/REJECTED, set COMPLETED. For allocation safety sum of child planned quantities cannot exceed order quantity; adding/editing a draft batch locks order to prevent concurrent over-allocation. This defines production order completion only, never QA release. Parent should include this exact business choice in approval or replace it explicitly; none is implemented yet.
- **Negative sub-batch release:** no new public release endpoint; domain command attempting ReleaseMainBatch against an explicit SUB_BATCH target must reject 422, but existing endpoint accepts untyped numeric MainBatch ID so cross-table ID collisions make such targeting ambiguous. Prefer approve TC-BAT-002 as domain negative test plus proof that no sub-batch QA release route exists, rather than add an artificial public route.
- **Status codes:** reconcile createMainBatch to 201 per existing detailed API; retain other original 200 operations. No global success-code normalization.
