# 05 状态机详细设计说明书 V1.0.12 FROZEN

每个Transition统一返回：allowed / failureCode / message / requiredActions；服务端为最终裁决。

## MainBatch
|From|Action|To|Permission|Preconditions/Gate|Failure|Side Effects|
|---|---|---|---|---|---|---|
|DRAFT|release|RELEASED|production:batch:release|Product/Formula/Route/eBR均APPROVED；快照可生成；批号唯一|BATCH_DEFINITION_NOT_READY 422|创建ProcessSnapshot/ExecutionUnit/Audit|
|RELEASED|start|IN_PROGRESS|production:batch:start|至少一个ExecutionUnit READY|BATCH_NOT_READY 409|startedAt/Audit|
|IN_PROGRESS|completeProduction|PRODUCTION_COMPLETED|production:batch:complete|required ExecutionUnit全部完成；Balance operation gates通过|BATCH_EXECUTION_INCOMPLETE 422|完成时间/Audit|
|PRODUCTION_COMPLETED|submitQA|PENDING_QA|qa:batch-review|eBR完整；生产复核完成|EBR_INCOMPLETE 422|QA待办/Audit|
|PENDING_QA|release|QA_RELEASED|qa:release|QC/Balance/Deviation/eBR/Signature全部Gate PASS|RELEASE_GATE_FAILED 422|ReleaseDecision+Signature+FinishedLot可用|
|PENDING_QA|reject|REJECTED|qa:release|QA决定|—|ReleaseDecision+Signature；库存不可用|

## SubBatch
PENDING→READY→IN_PROGRESS→COMPLETED；可CANCELLED。无任何QA Release transition；调用release返回 SUBBATCH_RELEASE_FORBIDDEN 422。

## ExecutionUnit
PENDING→READY：资源/定义满足。READY→IN_PROGRESS：mes:operation:start。IN_PROGRESS↔PAUSED。IN_PROGRESS→COMPLETED：所有required operations完成。任一Gate可进入BLOCKED；解除原因后回原允许状态。

## OperationExecution
PENDING→READY：前置工序完成。READY→IN_PROGRESS：人员资格、设备、清场、物料状态通过。IN_PROGRESS→PAUSED/COMPLETED。完成Gate：required eBR fields、IPC、parameter limits、review/signature、balance checkpoint。

## MaterialLot Quality
QUARANTINE→SAMPLED→TESTING→RELEASED/REJECTED。RELEASED→BLOCKED：质量冻结。BLOCKED→RELEASED仅QA授权且原因关闭。REJECTED不得生产投料。

## Inventory Reservation/Issue
Reservation ACTIVE→CONSUMED/RELEASED。MaterialIssue DRAFT→CONFIRMED→CLOSED；CONFIRMED后更正用return/adjustment，不回写原issue。

## eBR Template
DRAFT→SUBMITTED(permission ebr:template:submit, lint pass)→APPROVED(独立审批)→EFFECTIVE→WITHDRAWN。非DRAFT禁止普通编辑。

## eBR FormInstance
DRAFT→SUBMITTED→VERIFIED→APPROVED。Correction产生新FieldValueRevision；若影响review/signature，状态回到需复核阶段并标记旧review/signature INVALIDATED。

## Deviation
OPEN→INVESTIGATING→DECIDED→CLOSED。关键Deviation未CLOSED/approved disposition时Release Gate失败。

## QC Sample/Test
Sample CREATED→SAMPLED→TESTING→COMPLETED。TestResult revision append-only；OOS状态触发investigation，不允许用新结果覆盖原OOS。

## Material Balance
CALCULATED_PASS→APPROVED/PASS；CALCULATED_FAIL→INVESTIGATING→APPROVED_EXCEPTION或RECALCULATED。未批准FAIL阻断operation/batch close。

## QA Release
无可变状态机表更新：MakeDecision创建immutable ReleaseDecision(RELEASED/REJECTED)。需要更正时创建superseding decision并保留旧决定。

# V1.0.12 Platform State Machines — DCP-MES-001-R2-001
## Signature

`VALID → INVALIDATED` only through the explicit invalidation command. INVALIDATED is terminal. Re-sign creates a new VALID record and never changes the historical row back to VALID.

## Integration Inbox

`RECEIVED → PROCESSING → PROCESSED`; `PROCESSING → RETRY_WAIT → PROCESSING`; `PROCESSING → DEAD_LETTER`; manual retry maps `RETRY_WAIT|DEAD_LETTER → RECEIVED`.

## Integration Outbox

`PENDING → DISPATCHING → PUBLISHED`; `DISPATCHING → RETRY_WAIT → DISPATCHING`; `DISPATCHING → DEAD_LETTER`; manual retry maps `RETRY_WAIT|DEAD_LETTER → PENDING`.

Failure count includes automatic and manual failures. Failures 1–7 use PT1M, PT5M, PT15M, PT1H, PT4H, PT12H, PT24H; failure 8 enters DEAD_LETTER. Manual retry does not reset the counter or erase error evidence. No generic status-update API exists.

## DCP-MES-002-R2-001 — Incoming Material States

### Inspection-required MaterialLot quality

`QUARANTINE → PENDING_SAMPLING → SAMPLING → SAMPLED → TESTING → PENDING_QC_REVIEW → QC_PASSED → PENDING_QA_RELEASE → RELEASED`.

Failure branch: `TESTING|PENDING_QC_REVIEW → QC_FAILED → PENDING_DISPOSITION → REJECTED|PENDING_QA_RELEASE` according to an approved investigation/disposition. `QC_PASSED` never makes inventory available.

### Inspection-exempt MaterialLot

`QUARANTINE → exemption evaluation → RELEASED`; the transition is atomic with creation of the system-rule ReleaseDecision and `BLOCKED → AVAILABLE`. The states `SAMPLED`, `TESTING`, and `QC_PASSED` must not be fabricated.

### Inventory

`BLOCKED → AVAILABLE` requires an effective RELEASED decision and released quality. `AVAILABLE → FROZEN` and `FROZEN → AVAILABLE|BLOCKED` require controlled freeze/unfreeze rules and may not erase release history.

### Controlled records

- InspectionRequest: `DRAFT → SUBMITTED → ACCEPTED → IN_PROGRESS → COMPLETED`.
- SamplingTask: `PLANNED → ASSIGNED → IN_PROGRESS → COMPLETED`.
- Sample: `CREATED → COLLECTED → RECEIVED → IN_TEST → TEST_COMPLETED → RETAINED|DISPOSED`.
- InspectionTask: `CREATED → ASSIGNED → IN_PROGRESS → PENDING_REVIEW → QC_PASSED|QC_FAILED`.
- InspectionReport: `DRAFT → REVIEWED → APPROVED`; approved content is superseded, never edited in place.




## DCP-MES-003-R2-001 approved delta

Organization/Equipment/Qualification states and named transitions are defined in DCP-MES-003-R2-001; generic updateStatus is forbidden.


## DCP-MES-004-005-R2-001 approved contract completion

Material creation 201, complete typed DTOs, explicit version target/root optimistic token, Material root DRAFT/APPROVED/INACTIVE and version DRAFT/SUBMITTED/APPROVED, Supplier UNAPPROVED/APPROVED/INACTIVE and named commands, historical relationship revocation, and LG-004 delayed conversion-material FK follow 00_DESIGN_CHANGE_PROPOSAL_DCP-MES-004-005-R2-001_APPROVED.md. No existing table/column/path/permission/task dependency is added or removed. All previous unrelated contracts remain applicable.


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


## Approved incoming full-chain completion in v1.0.12

Read `00_DESIGN_CHANGE_DCP-INCOMING-QUALITY-GAPS-001_APPROVED.md` and the four `00_INCOMING_*_CONTRACT_V1.0.12.md` appendices. Their explicit columns, state guards, API schemas, permissions, signing envelopes, UI fields, tests and dependency ownership supersede contradictory inherited text within the approved scope only. Former DG-01..08 and BC-01/02 now have implementation contracts; delivery is still subject to actual code and required validation. No full workflow PASS follows from publication.


## Authorized v1.0.12 incoming completion supplement

Human approval covers the bounded contract completion and full incoming acceptance tasks. For eBR runtime, issue returns, weighing verification and actual trace identities, the following precise supplements supersede generic or conflicting clauses in this chapter; unaffected contracts remain unchanged.

- [00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.12.md](00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.12.md)
- [00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.12.md](00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.12.md)
- [00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.12.md](00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.12.md)
