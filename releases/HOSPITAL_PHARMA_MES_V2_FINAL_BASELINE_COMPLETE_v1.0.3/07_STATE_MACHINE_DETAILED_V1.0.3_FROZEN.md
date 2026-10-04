# 05 状态机详细设计说明书 V1.0.3 FROZEN

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

# V1.0.3 Platform State Machines — DCP-MES-001-R2-001
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
