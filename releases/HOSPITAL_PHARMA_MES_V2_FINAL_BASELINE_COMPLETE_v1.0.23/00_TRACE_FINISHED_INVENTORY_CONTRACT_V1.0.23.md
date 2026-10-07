# DCP-TRACE-FINISHED-INVENTORY-001 — approved bounded read closure

Approval: user on 2026-10-06 selected audit gaps 1 and 4 only and explicitly replied “授权修改批准，投料记录是关键，不是称量。” This approves the previously presented read-contract/menu completion. Current parent FINAL BASELINE COMPLETE v1.0.21 remains immutable. Publish cumulative v1.0.23 only after cross-document consistency PASS. Runtime status is only MES_TASKS.md; approval is not acceptance.

## 1. Scope and invariants

Close raw-lot forward trace through actual MaterialCharge/Genealogy to each affected mother batch, its existing finished lot, receiving, quality/report, QA decision and shipment. MaterialCharge is the central production material fact. No new mandatory weighing step, weighing prerequisite, process rule, stock rule, QA/signature rule, request prerequisite or sample-container field. No database/migration, new business fact or permission.

Trace remains GET /trace with exactly one existing mainBatchId/materialLotId; do not add new root parameters. Preserve reverse trace and charge reversal history. Expand each actual downstream batch once; never link an uncharged lot to a batch through a plan, reservation or issue alone. Retain CHARGE→MATERIAL_LOT CONSUMED_FROM and actual batch/unit/operation/Genealogy validation. A missing required genealogy fails closed. No weighing is fabricated when a charge has none.

## 2. WMS/source trace projection

Use same-org existing receipt-item source snapshot for supplier identity/name and manufacturer text, not current master values or Material Master manufacturer. Supplier is an existing identity; manufacturer stays frozen source text, with no invented manufacturer entity/ID. Add trace node types SUPPLIER, MATERIAL_REQUEST, MATERIAL_ISSUE, MATERIAL_ISSUE_ITEM, ISSUE_RETURN. Use actual persisted request/issue/item/return IDs, states, numbers, and parent relationships. Return is immutable event evidence with nullable status, not a new return state. Never invent an exact charge→issue-item assignment: the existing entitlement is aggregate batch/lot. Graph relates requests/issues to their actual batch and issue items/returns to their actual lot/parent item. Source deep links enforce existing source-view rights; new WMS categories are omitted without the existing corresponding view rights.

Existing finished trace node types FINISHED_INBOUND_REQUEST, FINISHED_INSPECTION_REQUEST, FINISHED_SAMPLING_RECORD, FINISHED_REPORT, FINISHED_REPORT_REVIEW, PRODUCTION_TEST, RESULT_REVIEW, FINISHED_SHIPMENT and INVENTORY_LEDGER must be enumerated consistently in TraceNode. These already-emitted projections are not new entities. Existing finished-chain per-category permission filtering remains intact.

## 3. Finished inventory projection

Reuse GET /wms/inventory and wms:inventory:view. Add optional finishedOnly boolean (strict true/false, default false), productId and mainBatchId positive-string-ID query filters. Filter server-side before pagination; classify finished identity using the actual same-org prd_main_batch.finished_lot_id relationship, not naming or warehouse type. Product/batch filters exclude raw lots. Unknown/cross-org IDs match no rows. Preserve page/size, stable lot-ID order, complete lot totals and locationBalances semantics.

WmsInventoryLot adds nullable read-only mainBatchId, batchNo, productId, productCode, productName, productSpecification. These come from the actual batch/product relationship and same-org product master labels; they are current readable labels, not claimed historical product snapshots. Raw rows have null context. Do not change material snapshot facts. onHandQty/reservedQty/availableQty remain exact existing ledger/allocation computations; availableQty is unreserved book quantity, not permission to ship. QUARANTINE/BLOCKED/FROZEN/expired lots remain subject to existing shipment gates.

## 4. UI

Global UI V2 / T1–T6 remains authority. Add /finished/inventory T1 and 成品库存 in the finished group immediately before 成品发货出库, requiring existing wms:inventory:view. Reuse WmsManagementListView with immutable finishedOnly=true scope; reset/query/page/detail navigation cannot drop this server filter. Query uses product, production batch, warehouse/location, quality and inventory statuses plus keyword; horizontal labels and compact query actions. Columns: production batch, product/code, specification, finished lot, warehouse/location, lot on-hand/reserved/unreserved quantities, unit, quality state, inventory state. No unsupported KPI or assumed shipping entitlement. Link lot to existing MaterialLot T4, batch to existing Production Batch T4 with its view permission. Existing inbound confirmation/test/QA pages remain reused; no separate receipt/test/release aggregate/page redesign.

TraceView selects T4, displays new and existing finished node business names and permission-controlled source links. CHARGE links to the owning execution-unit charge workspace through actual OPERATION/EXECUTION_UNIT graph relationships. Return/item source links resolve to their actual original issue; no invented detail API. Retain actual IDs/versions as metadata. No new universal trace root or UI V3.

## 5. Integration, migration, tests and RTM

Boot composes same-org production/product inventory context and WMS trace producers via query ports; WMS/Trace do not import production persistence or create module cycles. Existing ledger and request/issue/return/source APIs remain write owners. No migration; native successful version031 unchanged.

TI-01: real charged raw lot→mother batch→finished receipt/request/sample/results/report/QA/shipment; shared endpoints/duplicate batches deduplicated and all graph endpoints present. Reverse trace unchanged and no invented weighing.
TI-02: actual linked request/issue/item/return/supplier/source projections, permissions and actual parent edges; no fabricated per-charge issue assignment.
TI-03: server finished-only/product/batch/quality/location filtering, before-pagination totals, exact quantities before QA/after shipment, raw-row nullable context, cross-org/no-match and invalid-filter rejection.
TI-04: closed OpenAPI projection/TraceNode validation and existing raw inventory/QA/shipment regressions.
TI-05: PC Chromium T1 finished inventory menu/query/reset/paging/lot detail; Trace T4 business names and source links, no console/page errors or horizontal page overflow.

Affected authoritative artifacts in v1.0.23: Database (no-schema/source mapping), Domain/State (no mutation), API/OpenAPI, UI/routes, permissions (existing only), GMP (unchanged), Test/RTM, Migration NONE, Integration and Task Dependency. All v1.0.21 parent hashes retained and checked before authority switch. No automatic acceptance, commit or push.
