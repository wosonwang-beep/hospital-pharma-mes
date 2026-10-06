# DCP-WMS-REQUEST-INVENTORY-RETURN-001

Date: 2026-10-06. **Scope approval: APPROVED** — the human replied “批准并补齐” to the identified formal material-request, issue linkage, inventory read-model and independent return-list gaps. **Detailed contract: APPROVED** — explicit human “确认” on 2026-10-06. Implementation verification is recorded in docs/acceptance/wms-request-management-2026-10-06; current readiness is maintained only in MES_TASKS.md and acceptance is not inferred. Current authority becomes FINAL BASELINE COMPLETE v1.0.19 only after this release consistency review PASS. This is an architectural change to an existing subsystem, not approval to implement unrelated WMS or MES features.

## 1. Approved boundary and source of truth

Receiving → quality disposition → inventory → MaterialRequest → MaterialIssue → weighing → independent verification → MaterialCharge. Receiving, MaterialLot, Reservation, IssueReturn and MaterialCharge remain their existing sources of truth. No separate inbound-management aggregate, no replacement MaterialIssue entity/table/API, no arbitrary status endpoint. Existing QA/quality eligibility, inventory freezing, expiry, frozen BOM, reservation, exact unit conversion, audit, signatures and charge/reversal controls remain mandatory.

The attached WMS board determines PC page composition, while Global UI V2 / T1–T6 determines visual language. It does not authorize mock facts, buttons without commands, new report exports, independent outbound-ledger facts, or an uncontracted approval workflow. Existing completed QC and execution pages are untouched. Workbench image/photo/planned-time/SOP proposals and UI V3 remain excluded.

## 2. Database and aggregate

New `wms_material_request` root uses the existing ScopedEntity metadata: `id BIGINT PK AUTO_INCREMENT`, `org_id BIGINT NOT NULL`, `created_by/updated_by BIGINT NOT NULL`, `created_at/updated_at DATETIME(3) NOT NULL`, `version_no BIGINT NOT NULL DEFAULT 0`. Business columns:

| Column | Definition | Constraint / origin |
|---|---|---|
| request_no | VARCHAR(80) NOT NULL | UNIQUE(org_id,request_no), manually supplied business number; no new numbering infrastructure |
| main_batch_id | BIGINT NOT NULL | FK prd_main_batch(id), same org |
| process_snapshot_id | BIGINT NOT NULL | FK prd_process_snapshot(id), the selected batch's existing immutable snapshot |
| status | VARCHAR(30) NOT NULL | DRAFT / SUBMITTED / PARTIALLY_ISSUED / FULFILLED / CANCELLED |
| submitted_by / submitted_at | BIGINT / DATETIME(3) NULL | server actor/time, both present after submit |
| cancelled_by / cancelled_at / cancellation_reason | BIGINT / DATETIME(3) / VARCHAR(1000) NULL | only populated by cancel |

Index `(org_id,main_batch_id,status,id)` and `(org_id,status,created_at,id)`. No product/BOM copy table: product and formula are read from process_snapshot_id. Snapshot reference and batch identity cannot change after creation; editing a draft changes its number and retained lines only.

New `wms_material_request_item` inherits the same ScopedEntity metadata. `request_id BIGINT NOT NULL FK wms_material_request(id)`, `formula_item_id BIGINT NOT NULL FK proc_formula_item(id)`, `material_id BIGINT NOT NULL FK md_material(id)`, `requested_qty DECIMAL(18,6) NOT NULL CHECK(requested_qty>0)`, `unit_id BIGINT NOT NULL FK md_unit(id)`. UNIQUE(org_id,request_id,formula_item_id). Server resolves material_id from the frozen formula; the client cannot substitute it. Every FK is additionally validated in the same organization. Draft edits retain existing item IDs; no physical removal. A draft can be cancelled and a corrected request created if a retained line is unwanted.

Extend existing `wms_material_issue` with nullable `material_request_id BIGINT FK wms_material_request(id)` and existing `wms_material_issue_item` with nullable `material_request_item_id BIGINT FK wms_material_request_item(id)`; indexes include org_id and linked IDs. Historical and existing API-created issues remain NULL and keep their current behavior. No backfill guessing. A new linked issue must have links on every item; parent, item, batch, material and frozen formula must agree. Request linkage is immutable after issue creation; a draft issue must not switch its identity or request line. Linked draft quantities/lots can be edited under existing controls.

Existing `wms_issue_return_item` is the sole immutable return fact. No new return header, invented return number/status, duplicated return quantity or return ledger. Independent return list displays its actual ID as “退料记录编号”. Schema change uses a new physical Flyway file, allocated only after reading the highest successful native history; V001–V028 are unchanged. Candidate documentation is not an executed migration.

## 3. State machine and quantities

`create → DRAFT`; `edit` only DRAFT; `submit: DRAFT → SUBMITTED`; successful linked issue confirmation makes the request PARTIALLY_ISSUED or FULFILLED in the same transaction. `cancel: DRAFT/SUBMITTED → CANCELLED` is permitted only when there is no linked draft or confirmed issue. PARTIALLY_ISSUED/FULFILLED/CANCELLED cannot be edited, resubmitted or cancelled. There is no review/approval state: the source request is production's material demand; existing warehouse issue confirmation is the controlled handover.

Submitting validates active released/in-progress batch, unchanged process snapshot, nonempty distinct formula lines, positive exact quantities and recognized compatible units. It does **not** reserve stock or bypass Reservation. requestedQty means total planned demand for that request line; a request may exceed currently available stock, but issue confirmation may not. No new BOM tolerance or formula quantity limit is invented.

`issuedQty` on a request line is the sum of quantities of linked CONFIRMED/CLOSED issue items, converted exactly to that request line's unit. `remainingQty = requestedQty - issuedQty`. Draft issue rows do not consume this quota. Every linked confirmation must keep each remainingQty >= 0. All lines zero remaining means FULFILLED; a positive total issue with remaining lines means PARTIALLY_ISSUED. These sums are server-derived, never client writable or independently persisted. Returned quantities do not reopen a fulfilled request: additional demand needs a new request. Existing net issue entitlement continues to subtract returns for charge authorization.

Lock order for linked issue mutations: existing production batch root → request root → issue root → ascending MaterialLot IDs. Reservation/charge consumers retain existing batch-first locking. Request create/submit/cancel use batch → request. Confirmation revalidates request and existing eligibility/reservation/stock gates under these locks, atomically inserts no duplicate facts, updates request/issue versions, and writes both audits. A rejected/stale/idempotency-conflicting command rolls back all changes. Duplicate successful replay returns the original response, not recalculated new state. No self-invocation may bypass transaction boundaries.

## 4. Inventory read-model contract

Retain existing GET /inventory and its location/container bucket quantities unchanged. Add GET /wms/inventory as a **lot-level** paginated projection for the new management screen. One row per MaterialLot/unit (the existing frozen base unit). onHandQty is the sum of the existing ledger delta quantities across all locations/containers. reservedQty uses the existing WmsProductionService outstanding-reservation calculation: active reservation total per batch minus actual charged quantity, floored at zero per batch, converted exactly. availableQty is onHandQty minus reservedQty. A negative/inconsistent result must be reported as an integrity error, never silently floored or shown as eligible stock.

availableQty is unreserved book quantity, **not production eligibility**. A positive quantity on QUARANTINE/LOCKED/EXPIRED/BLOCKED stock remains unavailable for production under existing gates. The UI must show qualityStatus and inventoryStatus beside quantities and explain this distinction.

Each lot row has locationBalances[] containing actual warehouse/location/container references and bucket onHandQty. warehouse/location filters match whether the lot has a nonzero bucket there; the returned lot totals still span the complete lot and are explicitly labeled “批次在库量 / 批次预留量 / 批次可用量”. Never repeat global reservations as a location's reservation. Multiple locations/warehouses show their real names, with location breakdown accessible in the row/MaterialLot T4. No client-side total inference from a single paginated page. No screenshot KPI cards unless a supported response supplies their aggregate; this contract supplies no separate system-wide KPI.

Warehouse, location, material, unit and batch labels are resolved by same-org existing reads. Expose no secrets or cross-org names. Sorting is fixed stable lot ID ascending; no new arbitrary sort syntax. Filters: page (0-based), size (1–100), keyword (material name/code or lotNo), materialId, materialLotId, warehouseId, locationId, qualityStatus, inventoryStatus. Unknown filters and invalid enums/IDs fail validation. Unknown/cross-org references yield no records, not leaked names.

## 5. API and DTOs

All endpoints use existing ApiResponse, string IDs/decimal quantities, UTC timestamps, server org/actor context and controlled errors. Existing command Idempotency-Key is required; mutable aggregate commands require versionNo and honor If-Match if supplied (must match body). State is never client-writable. New GETs are scoped, stable and paginated. Create returns 201, ordinary reads/actions 200. Invalid input 400, forbidden 403, absent scoped identity 404, stale/state/idempotency conflicts 409, business gate 422. Existing endpoint compatibility is retained.

| Endpoint | Purpose | Permission |
|---|---|---|
| GET /material-requests | filters keyword/requestNo/mainBatchId/productId/status/createdFrom/createdTo; page/size | wms:request:view |
| POST /material-requests | requestNo,mainBatchId,reason,items[{formulaItemId,requestedQty,unitId}] | wms:request:create |
| GET /material-requests/{id} | root, immutable batch/product/BOM references, item requested/issued/remaining quantities, linkedIssues and allowedActions | wms:request:view |
| PUT /material-requests/{id} | requestNo,reason,versionNo,retained items[{id? for existing,formulaItemId,requestedQty,unitId}] | wms:request:update |
| POST /material-requests/{id}/submit | versionNo,reason | wms:request:submit |
| POST /material-requests/{id}/cancel | versionNo,reason | wms:request:cancel |
| GET /wms/inventory | lot-level balances defined above | existing wms:inventory:view |
| GET /material-returns | flat immutable return facts plus original issue/item/batch/product/material/lot/unit context; filters issueId,mainBatchId,materialId,materialLotId,returnedFrom,returnedTo,keyword; page/size | existing wms:issue:view |

Existing POST/PUT /material-issues gain optional materialRequestId and per-item materialRequestItemId. Omitted/null on legacy flow remains unlinked; fields on a linked issue must be consistently present. PUT cannot change linkage and rejects attempts. GET list/detail adds nullable request ID/no, batch no and frozen product ID/name as resolved context. Existing list adds filters materialRequestId/productId and issuedFrom/issuedTo without removing current filters. Existing issue command schema retains all its old required fields and reason/version behavior.

MaterialRequest read: id,versionNo,createdBy,createdAt,updatedBy,updatedAt,requestNo,mainBatchId,batchNo,processSnapshotId,formulaVersionId,productId,productName,status,submittedBy,submittedAt,cancelledBy,cancelledAt,cancellationReason,items,linkedIssues,allowedActions. Each item: id,formulaItemId,materialId,materialCode,materialName,requestedQty,issuedQty,remainingQty,unitId,unitCode,unitName. Names and computed quantities are read-only. Server determines UPDATE/SUBMIT/CANCEL from both permission and current state/linkage, not UI assumptions.

MaterialReturn read: existing id,issueId,issueItemId,quantity,unitId,reason,returnedBy,returnedAt,recordVersion, plus issueNo,mainBatchId,batchNo,productId,productName,materialId,materialCode,materialName,materialLotId,lotNo,originalIssuedQty,issuedUnitId,returnedQty,remainingReturnQty,unitName,issuedUnitName. returnedQty and remainingReturnQty are the current aggregate values in the **original issue item's unit**; quantity/unitId describe this immutable return event. Display both units, never compare mismatched units. No per-event mutable status or implicit return approval. GET /material-returns is read only; execute return only through existing POST /material-issues/{id}/returns and existing version/reason/remaining/charged-entitlement gates. New history list does not relax batch state requirements on that command.

## 6. UI routes, permissions and signatures

Warehousing menu order: 原辅料收货记录 → 库存管理 → 领料申请 → 出库管理 → 退料管理. Existing receipts/issues routes and MaterialLot T4 preserved. New routes:

- `/wms/inventory`: T1, existing wms:inventory:view; every lot link navigates existing `/wms/material-lots/:id`.
- `/wms/requests`: T1; `/wms/requests/create` and `/:id/edit`: T2; `/:id`: T3; use matching view/create/update rights. Submit/cancel additionally require their own permission and allowedActions.
- `/wms/returns`: T1 immutable history; `/wms/returns/create`: T2 execute existing return after selecting a CONFIRMED source issue (wms:issue:view plus wms:issue:return). Source details show original amount, prior returns, current remaining amount and reason. Successful command returns to scoped history; no invented draft return record or save button. Detail links open original issue and retained returns, without new return-detail write API.

Existing new-issue T2 presents a submitted/partially-issued MaterialRequest picker, production batch/product and frozen formula line facts, plus eligible MaterialLot allocation. Readable names accompany IDs. Current API's unlinked issues remain readable/editable/confirmable under existing controls; legacy create compatibility is retained, not silently revoked. No MES weighing/review/charge controls in WMS. No independent 入库管理. Existing inventory ledger detail can support read-only history; no extra menu/entity promised.

New permissions only wms:request:view/create/update/submit/cancel. Seed existing SYSTEM_ADMIN; business-role assignments are explicit IAM actions, not guessed grants to all existing roles. Menu filtering reflects actual permissions. No new signature requirement for request create/submit/cancel; audit, actor, UTC time, version and reason are mandatory. Existing Issue confirmation/return and MES/QA signature rules are unchanged. No regulated business physical deletion.

## 7. Test / RTM / integration acceptance

| Requirement | Required case | Gate |
|---|---|---|
| WMS-REQ-001 | TC-WMS-REQ-001: create/edit/submit immutable batch snapshot and formula identity; IDs, units and org isolation | unit + native |
| WMS-REQ-002 | TC-WMS-REQ-002: stale/replay/changed-payload/permission/state/cancel-linked rejection | native + API |
| WMS-REQ-003 | TC-WMS-REQ-003: partial and full issues; concurrent issues cannot exceed requestedQty; atomic rollback | native |
| WMS-REQ-004 | TC-WMS-REQ-004: different unit conversions, duplicate formula rows and foreign request line rejection | unit + native |
| WMS-ISS-REQUEST-001 | TC-WMS-ISS-REQUEST-001: unlinked historical flow unchanged; linked batch/BOM/request consistency | targeted existing issue regression |
| WMS-INV-READ-001 | TC-WMS-INV-READ-001: multi-location lot balance, outstanding reservations/charges, no duplicated reservation or page totals | native read |
| WMS-INV-READ-002 | TC-WMS-INV-READ-002: quality/freeze states visible; positive availableQty never bypasses eligibility | native + frontend |
| WMS-RET-READ-001 | TC-WMS-RET-READ-001: global scoped history, original issue lineage, mixed units, cumulative/remaining quantities | native read |
| WMS-RET-READ-002 | TC-WMS-RET-READ-002: existing return bounds/charged entitlement/replay remain; no request reopen | existing return regression |
| UI-WMS-001 | TC-UI-WMS-001: five menu entries, T1/T2/T3, live pickers, same-org labels, MaterialLot link, exact payloads | Chromium PC |
| UI-WMS-002 | TC-UI-WMS-002: return execution and history, no fake number/status, errors/allowedActions/permissions | Chromium PC |
| WMS-FLOW-001 | TC-WMS-FLOW-001: confirmed receiving→QUARANTINE; QA release→reservation→request→partial/full outbound→weigh/verify/charge→bounded return; end-to-end source/audit trace | uniquely identified persistent-native records |

Targeted gate only: affected Java unit/compile, new append-only migration on native MariaDB after history check, native transactional/read tests, direct Contract regressions, frontend typecheck/affected unit/build, targeted PC Chromium with screenshot and console/page-error evidence. Preserve shared seed/data and local configs. No full regression or acceptance inferred from a contract review. Current accepted MES tasks remain accepted; this authorized maintenance has its own readiness entry in MES_TASKS.md.

Consumers: WMS management reads new MaterialRequest; linked MaterialIssue consumes same frozen process context and Reservation gates; Return reads issue facts; existing MES charge consumes net confirmed issue entitlement unchanged. No cross-module cycles or new workflow runtime. Implement in order: Database/Flyway → Domain/state → Mapper → Application/locks → API → Audit/permission → UI → targeted tests → evidence/RTM. Physical migration, role seeding, runtime build and test completion are separate from candidate design consistency.

## 8. Executable acceptance definitions (not execution evidence)

All cases use a unique test prefix and the existing organization's legitimate setup; counterpart-org cases use explicit test-owned fixtures. Current shared seed records must not be edited. Cleanup is limited to test-owned fixtures under the database validation policy; immutable evidence is retained where required. All command cases assert status, returned versions, persisted facts, audit actor/reason and absence of partial writes on rejection.

| Case | Setup and actions | Required assertions |
|---|---|---|
| TC-WMS-REQ-001 | Released active batch with immutable process snapshot and two distinct frozen formula lines. Create demand, edit a draft retaining line IDs, submit; read it again after unrelated master changes. | Batch/snapshot/line material identities preserved; number uniqueness scoped by org; submitted actor/time present; demand is not a Reservation; post-submit edit rejected. |
| TC-WMS-REQ-002 | Replay each command with the same key/body; reuse key with changed body; stale version; identity from another org; missing view/create/update/submit/cancel permissions; cancel draft and unissued submitted request; attempt cancel with a linked draft/confirmed/CLOSED issue. | Successful replay has the same response and one mutation/audit; conflicts do not mutate facts; no cross-org data; only allowed state/permission transitions; linked request is not cancelled. |
| TC-WMS-REQ-003 | Submit one line requesting 10 in its unit, reserve at least 10 under existing gates. Confirm linked issues 4 then 6; concurrently confirm two 6-unit issues against a separate request of 10. | First request SUBMITTED→PARTIALLY_ISSUED→FULFILLED with remaining 6→0; concurrent request can accept only one 6-unit confirmation; rejected confirmation leaves its issue draft and request quota unchanged. |
| TC-WMS-REQ-004 | Use a known exact conversion factor with request/issue units differing. Try zero/negative/excess precision, incompatible units, duplicate formula IDs, wrong material/foreign formula/request-item identities and a switched draft issue link. | Exact sums and bounds; invalid identities/quantities reject without rounding, link reassignment or duplicated lines. |
| TC-WMS-ISS-REQUEST-001 | Execute existing unlinked create/edit/confirm payloads and read a historical NULL-linked issue; execute a correctly linked issue, then wrong batch and wrong frozen formula cases. | Legacy required fields/commands/eligibility unchanged; unknown historical linkage stays NULL; linked identities and line quota are enforced. |
| TC-WMS-INV-READ-001 | One lot in two locations/containers, known ledger quantities, active reservations in two batches, and a partial actual charge. Query all, then warehouse/location filters and multiple pages. | One complete lot total; outstanding reservation subtracts per-batch charge exactly; filtered location match does not truncate lot totals; location details contain only true bucket amounts; stable pagination and no fake KPI total. |
| TC-WMS-INV-READ-002 | QUARANTINE, LOCKED/blocked and released eligible lots with positive unreserved book quantities, plus an inconsistent test-owned reservation/ledger setup. | Quality/inventory state remains visible; ineligible stock cannot confirm production issue or charge; inconsistent negative free quantity is rejected as integrity error rather than clipped to zero. |
| TC-WMS-RET-READ-001 | Several immutable return events from two original issue items, a known exact alternative return unit, and a separate org. Query filtered history and pagination. | Actual IDs/events, original issue/batch/product/lot context, original-unit cumulative and remaining amounts; event unit preserved; no invented return number/status; no cross-org facts. |
| TC-WMS-RET-READ-002 | Partially consume an issued entitlement, return only the unused portion, replay return, attempt excess/consumed return; inspect linked demand and ledger. | Existing quantity/replay/version/state protections hold; no extra physical-stock increase; gross fulfilled demand stays fulfilled; remaining charge entitlement uses existing net issued quantity. |
| TC-UI-WMS-001 | Authenticate an authorized PC user; inspect five menu entries; create/submit a request; select it on issue creation; click a lot from inventory; inspect denied-role UI. | T1/T2/T3 and inline labels follow V2; real names and exact command IDs/amounts; actions require permission and server allowedActions; existing MaterialLot T4 route; no console/page error. |
| TC-UI-WMS-002 | Open global return history; execute a return from a confirmed source, read fresh history, navigate source detail; force an API rejection. | Exact existing return payload; prior/original/current amounts and unit labels; rejection retains input; successful fact appears once; no fake draft/save/status; no console/page error. |
| TC-WMS-FLOW-001 | Unique real receiving record → lot QUARANTINE; attempt issue and fail; complete existing QA disposition and stock eligibility; reserve, submit request, confirm linked partial/full issues, execute existing weighing/independent verification, partially charge, return unused entitlement. | No QUARANTINE or merely QC-PASS production handover; no new double deduction on issue or double increase on return; request/issue/charge/return/lot linkage and actor/time/audit facts reconstruct the whole exercised flow. |
