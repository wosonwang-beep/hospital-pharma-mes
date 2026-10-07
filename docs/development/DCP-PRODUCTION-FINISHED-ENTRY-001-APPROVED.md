# DCP-PRODUCTION-FINISHED-ENTRY-001 — Final production / finished entry family

Status: APPROVED — explicit user reply “批准”, 2026-10-07, to the read-contract supplementation proposal. Implementation readiness is maintained only in MES_TASKS.md.

Source: user fixed Production to 生产订单 / 生产批 / 生产执行 / eBR模板 / 物料平衡 and Finished to 成品入库申请 / 成品生产入库（待检） / 成品请验 / 成品取样 / 成品检验 / QA批审核 / 成品放行 / 成品库存 / 成品发货出库. All normal/T4/T5 shells must use the same shared navigation source. Inherited business authority: FINAL BASELINE COMPLETE v1.0.22 and approved DCPs; UI authority remains Global UI V2 / T1–T6.

## Actual current contract findings

- Production execution: `/mes/execution/:id` takes a real execution-unit id. Its record GET and batch execution-unit GET require `mes:execution:view`; operations use existing `mes:operation:view`. A parameterless menu destination cannot silently guess an id.
- Material balance: `/production/batches/:id/balance` takes a real main-batch id, using existing balance read/actions; its current page also reads the batch. Existing route requires `balance:view`.
- Warehouse receipt: existing inbound request CONFIRM action creates the actual receipt/inventory fact. No independent finished-receipt aggregate is needed. GET `/finished-inbound-requests` supports keyword/status/mainBatchId; CONFIRMED receipt history remains represented by its actual inbound request and embedded lot/batch facts.
- Finished tests: `ProductionQualityService.list` currently filters status/mainBatchId, not sampleId or finished origin. `FinishedInspectionService` creates real samples with `sourceRef = FinishedInspectionRequest:<id>` and real FinishedSamplingRecord.sampleId. Finished tests must be selected by this existing relation, not by a new business enum, guessed product type, or client-side filtering after pagination.
- QA detail and release: `/qa/batches/:id/review` and `/qa/batches/:id/release` operate on the actual main batch. Current page reads actual QA review/evidence. There is no independent finished QA/release global queue in the current route matrix. A global production-batch list may require permissions not held by an otherwise authorized QA user.
- Existing report list/detail remains a source of derived evidence and QC approval. It must remain reachable from finished inspection/request contexts even though it is not a separate item in the final nine-menu list.

## Approved bounded route / page mapping

| Menu | Proposed entry | Existing actual destination / source | Template |
|---|---|---|---|
| 生产订单 | `/production/orders` unchanged | Existing orders | T1 |
| 生产批 | `/production/batches` unchanged | Existing main batches | T1 |
| 生产执行 | `/production/execution` new entry | Select actual execution unit, then `/mes/execution/:id`; never a no-op or fixed fixture id | T1 → existing T5 |
| eBR模板 | `/ebr/templates` unchanged | Existing templates | Existing template structure |
| 物料平衡 | `/production/balances` new entry | Select actual batch, then `/production/batches/:id/balance` | T1 → existing T3 |
| 成品入库申请 | `/finished/inbound` unchanged | Existing inbound requests/create/details | Existing T1/T2/T3 |
| 成品生产入库（待检） | `/finished/receiving` new entry | Existing submitted receipt work and confirmed receipt facts; CONFIRM remains existing controlled command | T1 → existing T3 |
| 成品请验 | `/finished/requests` unchanged | Existing finished requests | Existing T1/T2/T3 |
| 成品取样 | `/finished/sampling` unchanged | Existing finished sampling records and real samples | Existing T1/T3 |
| 成品检验 | `/finished/tests` new entry | Existing production test instances linked to real finished samples; detail/runtime reused; reports linked using existing requests/reports | T1 → existing detail |
| QA批审核 | `/finished/qa-reviews` new entry | Main-batch QA review queue, then existing `/qa/batches/:id/review` | T1 → existing T6 |
| 成品放行 | `/finished/releases` new entry | Actual main-batch release context/decisions, then existing `/qa/batches/:id/release` | T1 → existing T6 |
| 成品库存 | `/finished/inventory` unchanged | Existing server-filtered finished inventory | Existing T1 |
| 成品发货出库 | `/finished/shipments` unchanged | Existing shipments/create/details | Existing T1/T2/T3 |

These are UI entry projections/context selectors, not new regulated business entities or parallel facts. A queue must not re-execute receipt/release or create invented independent statuses. Existing source report routes remain present and accessible through relevant context links.

## Approved read-only contract supplementation

1. Production execution index: organization-scoped, paginated execution-unit projection with actual unit number and batch context; reuse current view permissions. The workbench additionally checks existing execution/operation access. No widening of existing permission grants.
2. Batch context selectors: use existing batch read where authorized. For balance/QA/release users without generic production-list permission, define explicit scoped selector reads using the relevant existing view permission; return only necessary existing batch facts. Do not grant production access implicitly or fabricate records.
3. Finished receiving projection: existing inbound read and states/commands reused. If a dedicated server read is needed for correct queue/history pagination, define only a projection over existing records and confirmed facts, inheriting current receiving read permission.
4. Finished test list: add a reviewed finished-only server filter/read over existing TestInstance → Sample → FinishedSamplingRecord → FinishedInspectionRequest relations, organization-scoped before pagination. Enforce current QC read permission. Include original attempts/controlled retests according to current source facts; no duplicate results or mutable copied summaries. Any sample filter exposed in this scoped entry must be actually applied by the server.
5. QA/release selectors: scope to actual finished main batches and existing QA/evidence/decision facts; enforce current QA review/release read controls separately. Display business numbers/names and actual statuses, not invented queue states or database ids as business numbers.

Exact additional endpoint/query/read DTO sections must be published in the approved successor contract before implementation. This proposal does not authorize unknown fields or arbitrary permissions; discovered changes outside these read/index/entry projections require a further decision.

## Invariants / exclusions

- Database, migration, production/quality state machines, quantity consumption, QA Release Gates, audit/electronic signature, original results and immutable lineage: NO CHANGE.
- WMS routes remain `/wms/receipts`, `/wms/inventory`, `/wms/requests`, `/wms/issues`, `/wms/returns`; backend MaterialIssue unchanged, UI uses 出库.
- 称量 / 复核 / 投料 remain internal execution capabilities. No mandatory weighing is added to the overall batch lifecycle; actual MaterialCharge remains the production-consumption/trace fact.
- Accepted inspection-record/report designs and T5 business layout are reused, not redesigned. No fake fields, fixed ids, dummy buttons, new business states or new permission codes.
- FINAL BASELINE v1.0.22 frozen files remain unchanged. After explicit approval, publish a consistent successor/additional approved contract including routes, reads, permissions mapping, UI, tests, RTM and integration/dependency impact before switching any authority pointer. No current pointer is switched by this proposal.

## Required verification after approval

Shared-menu explicit order and permission tests; actual execution-unit selection → real T5; batch balance navigation; receipt queue/history source correctness; finished test list excludes IPC and applies origin/sample filtering before pagination; tenant isolation and denied-source access; QA/release queues bind actual batch and existing controlled actions; report evidence links remain accessible; PC Chromium normal/T4/T5 navigation/screenshots with no console errors/overflow; targeted frontend/backend contract tests/typecheck/build. No default full regression or destructive database work.

## Exact read/API contract

All new reads use existing ApiResponse/PageData wrappers, organization scope, stable id DESC order, page 0..1000000 and size 1..100. Reject unknown query keys/invalid contexts. No write, stock synchronization, audit append, signature or business transition is performed by index reads.

| API | Permission | Filters / response |
|---|---|---|
| GET /navigation/executions | mes:execution:view | page,size,keyword (executionNo),status,mainBatchId. ExecutionEntry: id,executionNo,mainBatchId,batchNo,unitType,status. Filter before pagination; no full batch snapshot. |
| GET /navigation/batches | context=balance: balance:view; qa-review: qa:batch-review; release: qa:release AND qa:batch-review | context required; page,size,keyword (batchNo),status,mainBatchId. BatchEntry: id,mainBatchId,batchNo,productId,plannedQty,unitId,status,finishedLotId,versionNo. QA/release restrict to finishedLotId present and existing PRODUCTION_COMPLETED/PENDING_QA/QA_RELEASED/REJECTED statuses; no invented queue status. |
| GET /navigation/batches/{id} | same context permissions | context only. Same BatchEntry; organization and context scope checked. Balance detail consumes this scoped read instead of requiring generic production-list access. |
| GET /quality/finished-tests | qms:test:view | page,size,keyword (testCode/sampleNo/inspectionRequestNo),status,mainBatchId,sampleId. FinishedTestEntry: id,testCode,sampleId,sampleNo,mainBatchId,batchNo,inspectionRequestId,inspectionRequestNo,status,attemptNo. Derive only actual relationally proven finished samples; paginate after filters. |
| GET /quality/finished-tests/{id} | qms:test:view | No query. Verify actual finished sampling/request relation and organization, then existing ProductionTestInstance read model; commands continue using existing /quality/production-tests/{id}/... endpoints. |
| GET /finished-inbound-requests (existing) | wms:finished-inbound:view | Add optional receivingOnly=true/false. true restricts to existing SUBMITTED/CONFIRMED before pagination; same existing closed response. Other filters and default behavior unchanged. |

Finished origin proof requires actual org-scoped FinishedSamplingRecord.sampleId and InspectionRequest, plus Sample.sampleScope=PRODUCTION, matching sourceRef=FinishedInspectionRequest:<id>, mainBatchId and materialLotId across actual sources. Include actual attempts/retests for those samples; no classification by transient UI labels. No changes to ordinary production-test list filtering are included.

Menu permissions: production execution additionally requires mes:operation:view to enter the existing T5; release requires both qa:release and qa:batch-review because its unchanged T6 reads the existing review model. No new permission codes/grants. Inaccessible entries are hidden, not bypassed. QA/balance selector reads do not require production:batch:view and do not grant access to generic production reads. Existing ancillary evidence remains permission-gated.

Finished receiving T1 uses receivingOnly=true and existing source details/actions; its title does not imply every confirmed record is still QUARANTINE. Actual quality status comes only from current lot facts. Finished test detail route /finished/tests/:id is an alias using the validated finished read and existing command/detail component. Reports remain reachable from the finished-test entry and actual request context, with qms:finished-report:view checks. A global report button navigates the existing real report list; no second report source.

Successor release must include inherited database/state/GxP no-change assertions, exact endpoint/query/closed DTO definitions, routes/menu permission matrix, UI T1 entry references, targeted TC/RTM and unchanged migration/source boundaries. Parent v1.0.22 stays immutable.
