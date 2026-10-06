# 06 API接口详细设计说明书 V1.0.18 FROZEN

## 全局契约
Base `/api/v1`；JWT Bearer。写请求必须有 `Idempotency-Key`（创建/业务动作）；更新必须有 `If-Match` 或 `versionNo`。
统一错误：`requestId, code, message, fieldErrors[], allowedActions[]`。
HTTP：400格式；401未认证；403无权限；404不存在；409状态/并发/重复；422领域规则；500未知。
业务写入与AuditEvent同事务；外部事件经Outbox。

## DTO通则
ID使用string/long一致策略；Quantity使用decimal字符串或BigDecimal；时间ISO-8601；枚举只接受OpenAPI列举值。客户端不得传createdBy/updatedBy/audit actor/最终服务器时间。

## 核心接口
### POST /materials
Permission `master:material:create`。Request MaterialCreateRequest：materialCode/name/type/baseUnitId及PRD MD-MAT-001字段。Validation：编码唯一、单位存在、类型合法。Response MaterialResponse。201。事务：Material+Audit。
### POST /process-packages/{id}/versions/{versionId}/approve
Permission `process:package:approve`。要求SUBMITTED、lint pass、审批人职责分离。200 ProcessPackageVersionResponse；409 INVALID_STATE；422 LINT_FAILED。副作用：contentHash、Audit、Signature(若策略要求)。
### POST /main-batches
Permission `production:batch:create`。Request productionOrderId/productId/plannedQty/unitId/subBatchPlan(optional)。201 MainBatchResponse。
### POST /main-batches/{id}/release
Permission `production:batch:release`；Idempotency-Key必需；If-Match必需。Gate：定义APPROVED/EFFECTIVE、快照可生成。副作用：ProcessSnapshot、ExecutionUnits、Audit。422 BATCH_DEFINITION_NOT_READY。
### POST /operations/{id}/start
Permission `mes:operation:start`。Gate：READY、qualification、equipment、clearance、material prerequisites。200 OperationResponse；409 INVALID_OPERATION_STATE；422 PRECONDITION_FAILED。
### POST /weighings
Permission `mes:weigh:create`。Request executionUnitId/materialLotId/formulaItemId/targetQty/actualQty/unitId/scaleEquipmentId。Gate：lot RELEASED、BOM match、scale valid、qualification、tolerance。201 WeighingResponse。
### POST /weighings/{id}/verify
Permission `mes:weigh:verify`。Gate：independent verifier when required。200；422 SELF_VERIFICATION_FORBIDDEN。
### POST /material-charges
Permission `mes:charge:create`。Request executionUnitId/operationExecutionId/materialLotId/weighingRecordId/chargedQty/unitId。Gate：operation IN_PROGRESS、lot RELEASED、BOM match、quantity、verification。副作用同事务：MaterialCharge + QuantityEvent(CHARGE)+InventoryLedger(CONSUME)+Genealogy+Audit。201。422 MATERIAL_NOT_RELEASED/MATERIAL_NOT_IN_BOM/CHARGE_QTY_INVALID。
### PUT /forms/{id}/draft-values
Permission `ebr:form:edit`。If-Match=formRevision。Request values[{fieldCode,occurrencePath,value,unitId,sourceRef}]。服务端执行ON_CHANGE/ON_SAVE规则。409 REVISION_CONFLICT。
### POST /forms/{id}/submit
Permission `ebr:form:submit`。执行ON_SUBMIT rules；BLOCK失败422 EBR_RULE_BLOCKED。成功生成RuleExecution/Audit。
### POST /field-values/{id}/corrections
Permission `ebr:record:correct`。Request newValue,reason。追加Revision；失效受影响Review/Signature。200 CorrectionResponse。
### POST /records/{type}/{id}/sign
Permission `ebr:sign`。Request meaning,reauthCredential/token。服务端计算recordDigest。401 REAUTH_FAILED；409 RECORD_CHANGED。
### GET /main-batches/{id}/material-balance
Permission `balance:view`。从QuantityEvent计算/读取最新计算版本，不接受客户端汇总。
### POST /balances/{id}/investigations
Permission `balance:investigate`。创建调查；批准接口需`balance:approve`。
### POST /release-decisions
Permission `qa:release`。Request mainBatchId,decision,reason,signatureContext。Gate：MainBatch PENDING_QA；eBR/QC/Balance/Deviation/Signature PASS。副作用：immutable ReleaseDecision + Finished MaterialLot availability + Audit/Outbox。422 RELEASE_GATE_FAILED。

## 审计要求
所有状态转换、批准、签名、更正、库存调整、称量/投料、质量结果修订、Release均产生AuditEvent。GET默认不产生GxP Audit，敏感导出可产生AccessAudit。

# V1.0.18 Concrete Platform API — DCP-MES-001-R2-001
## POST /auth/reauth

Permission `ebr:sign`. `ReauthenticateForSignatureRequest` contains `objectType`, `objectId`, `meaning`, `recordVersion`, `credential`. Success returns `ReauthenticationResponse(reauthToken, expiresAt)`. TTL is exactly five minutes. The opaque token is single-use and bound to current user, session, org, object, meaning and version. It is consumed atomically at the first sign attempt and is not restored on rollback. Credentials/tokens are excluded from logs, audit and auth context.

## POST /records/{type}/{id}/sign

Owned by MES-001 platform; MES-007/MES-013 only supply providers. Requires `ebr:sign`, `Idempotency-Key`, `If-Match` and provider authorization. `SignRecordRequest` contains `meaning` and `reauthToken`. `SignatureResponse` contains `id`, `signerId`, `meaning`, `objectType`, `objectId`, `recordDigest`, `status`, `signedAt`, `revokedSignatureId`, `versionNo`. Errors include 401 invalid/expired/replayed token, 403 authorization, 409 version/idempotency conflict and 422 unsupported object/meaning/state.

## GET /audit-events

Permission `audit:view`; organization is principal-derived. Filters: `actorId`, `action`, `objectType`, paired `objectId`, `source`, `transactionId`, `requestId`, inclusive `occurredFrom`, exclusive `occurredTo`, `page` default 0 and `size` default 50/range 1–200. Sort is fixed `occurred_at DESC,id DESC`. Response data is `AuditEventPageResponse(items,page,size,total)` with concrete `AuditEventResponse`; no public create/update/delete endpoint exists.

## GET /integration/messages

Permission `integration:view`. Filters: `direction`, `system`, `messageId`, `eventType`, `aggregateType`, `aggregateId`, repeated `status`, time bounds and page/size. Response uses `IntegrationMessagePageResponse`. Payload JSON is excluded from the list response.

## POST /integration/messages/{messageRef}/retry

`messageRef` matches `^(INBOX|OUTBOX):[1-9][0-9]*$`. Requires `integration:view` and `integration:retry`, `Idempotency-Key`, `If-Match`, and `RetryIntegrationMessageRequest.reason` length 1–1000. It is allowed only from RETRY_WAIT or DEAD_LETTER and returns the concrete message response. Version/claim/idempotency conflicts are 409; ineligible state is 422. The transition and `INTEGRATION_MESSAGE_RETRY_REQUESTED` AuditEvent commit in one transaction.

All success DTOs are carried by the API-layer `ApiResponse(code,message,data,traceId)` wrapper. `ApiError(requestId,code,message,fieldErrors,allowedActions)` is used for errors. Full schemas are normative in the v1.0.18 OpenAPI.

## DCP-MES-002-R2-001 — Incoming Material APIs

All paths are under `/api/v1`. Material-version create/read DTOs include `requiresIncomingInspection` (default `true`).

- Receipt: `GET|POST /wms/receipts`, `GET /wms/receipts/{id}`, `POST /wms/receipts/{id}/confirm`.
- Material lot: `GET /wms/material-lots`, `GET /wms/material-lots/{id}`, `GET /wms/material-lots/{id}/timeline`, `GET /wms/material-lots/{id}/eligibility`.
- Inspection request: `GET|POST /quality/inspection-requests`, `GET /quality/inspection-requests/{id}`, `POST .../{id}/submit`, `POST .../{id}/accept`.
- Sampling: `GET|POST /quality/sampling-tasks`, `GET .../{id}`, `POST .../{id}/assign|start|details|complete`.
- Samples: `GET /quality/samples`, `GET /quality/samples/{id}`, `POST /quality/samples/{id}/label`.
- Inspection: `GET|POST /quality/inspection-tasks`, `GET .../{id}`, `POST .../{id}/assign|start|submit-review|review`, `POST /quality/inspection-items/{id}/executions`, `POST /quality/inspection-items/{id}/results`, `POST /quality/test-results/{revisionId}/revisions`.
- Report: `GET|POST /quality/inspection-reports`, `GET .../{id}`, `POST .../{id}/review|approve`.
- Incoming QA: `GET /qa/material-lots/{lotId}/release-review`, `POST /qa/material-lots/{lotId}/release-decisions`.
- Finished QA remains scoped separately: `GET /qa/main-batches/{batchId}/release-review`, `POST /qa/main-batches/{batchId}/release-decisions`.

State-changing operations require appropriate permission, optimistic version, reason/eSignature where specified, idempotency for create/confirm/transition commands, audit evidence, and transactional rollback. Ineligible production use returns `422 MATERIAL_NOT_ELIGIBLE`; stale state/version returns 409.

Permissions: `wms:receipt:view|create|confirm`, `qms:inspection-request:view|create|submit|accept`, `qms:sampling:view|create|assign|execute|complete`, `qms:test:view|execute|correct|review`, `qms:report:view|create|review|approve`, `qa:material-release:view|decide`.




## DCP-MES-003-R2-001 approved delta

MES-003 typed POST/PUT schemas, commands, list filters, version headers, gate failures and responses follow DCP-MES-003-R2-001 and the updated OpenAPI. Generic schemas no longer govern MES-003.


## DCP-MES-004-005-R2-001 approved contract completion

Material creation 201, complete typed DTOs, explicit version target/root optimistic token, Material root DRAFT/APPROVED/INACTIVE and version DRAFT/SUBMITTED/APPROVED, Supplier UNAPPROVED/APPROVED/INACTIVE and named commands, historical relationship revocation, and LG-004 delayed conversion-material FK follow 00_DESIGN_CHANGE_PROPOSAL_DCP-MES-004-005-R2-001_APPROVED.md. No existing table/column/path/permission/task dependency is added or removed. All previous unrelated contracts remain applicable.


## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.

## DCP-MES-006-R2-001 current contract

The approved 00_DESIGN_CHANGE_DCP-MES-006-R2-001_APPROVED.md is normative for MES-006 and supersedes prior contradictory process/product/eBR scope prose. Product is owned here; material has no business version; eBR is independently owned by MES-007. Process signature binds immutable business version and definition content. New physical V011 only. Test requirements include TC-PROC-004.


## DCP-MATERIAL-NAMES-UI-001 current correction
See `00_DESIGN_CHANGE_DCP-MATERIAL-NAMES-UI-001_APPROVED.md`. The three alternate material-name fields are retired from current API/UI/search/consumer snapshots; legacy physical values and audits are preserved. Labels and controls remain side by side on all viewport sizes. This supersedes inherited inconsistent descriptions within that boundary.


## DCP-MES-007-008-SEQUENCING-001 authoritative delta

Read `00_DESIGN_CHANGE_DCP-MES-007-008-SEQUENCING-001_APPROVED.md` and the stage contract appendices. Explicit human approval preserves the full MES-007/008 functionality and required tests, while separating current implementation from later real integration. LG-007A definition/Designer/DSL/published contracts is the current MES-007 gate; LG-007B remains MES-009 after LG-009A, with its operation_execution_id FK installed and validated by LG-010. LG-008 reservation/issue main_batch_id FKs are installed and validated by LG-009A. Before these physical producers exist, dependent writes fail closed; no placeholder batch/operation rows, bypassed qualification, or mutable regulated history. MES-008A owns actual MaterialEligibilityService/release evidence; no quantity-only eligibility.

The existing independent receipt edit UI is retained. `PUT /wms/receipts/{id}` uses `wms:receipt:update`, edits only DRAFT authored receipt fields, requires optimistic version, reason, idempotency and same-transaction audit, and returns 200; Confirmed receipt facts (recordStatus APPROVED) cannot be edited. Formal contracts are the concrete OpenAPI and stage appendices. All current labels/control pairs stay horizontal on desktop/mobile.

Current targeted tests prove only current-stage capabilities; deferred runtime/eligibility/batch tests remain required. MES-007 and MES-008 remain IN PROGRESS until every original applicable gate passes against real producer contracts. The approved stage does not authorize implementation of MES-008A/009/010 or marking tasks ACCEPTED.


## DCP-MES-008A-CONTRACT-001 approved bounded delta

Read `00_DESIGN_CHANGE_DCP-MES-008A-CONTRACT-001_APPROVED.md`. Its six independent create/execute routes, existing workbench mapping, nullable incoming MainBatch references, MES-009 delayed FK ownership and canonical finished_lot_id target supersede conflicting inherited wording only within this boundary. Existing fields, API/permissions, state machines and GxP requirements remain unchanged. Unresolved incoming implementation-map gaps are not resolved by this release.


## Approved incoming full-chain completion in v1.0.18

Read `00_DESIGN_CHANGE_DCP-INCOMING-QUALITY-GAPS-001_APPROVED.md` and the four `00_INCOMING_*_CONTRACT_V1.0.18.md` appendices. Their explicit columns, state guards, API schemas, permissions, signing envelopes, UI fields, tests and dependency ownership supersede contradictory inherited text within the approved scope only. Former DG-01..08 and BC-01/02 now have implementation contracts; delivery is still subject to actual code and required validation. No full workflow PASS follows from publication.


## Authorized v1.0.18 incoming completion supplement

Human approval covers the bounded contract completion and full incoming acceptance tasks. For eBR runtime, issue returns, weighing verification and actual trace identities, the following precise supplements supersede generic or conflicting clauses in this chapter; unaffected contracts remain unchanged.

- [00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.18.md](00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.18.md)
- [00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.18.md](00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.18.md)
- [00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.18.md](00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.18.md)


## Authorized v1.0.18 material weighing policy producer

The approved full incoming acceptance scope includes this missing producer. [00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.18.md](00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.18.md) governs deployment configuration, immutable production snapshot, consumers and required tests. It does not restore retired material master fields or change material APIs. It supersedes earlier references to nullable legacy material weighing fields. No new table, permission, route or status.


## v1.0.18 approved trace-read completion

See [00_INCOMING_TRACE_STANDARD_V1.0.18.md](00_INCOMING_TRACE_STANDARD_V1.0.18.md) for exact frozen QC standard node and immutable signature-policy identity evidence. Unaffected contracts remain unchanged.


## Approved functional closure delta — v1.0.18

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.18.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.18.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.


## Verified functional closure recovery — 2026-10-04

The approved bounded delta and one-time V024 exception are recorded in [functional closure §§9–10](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.18.md). V024 is successful; V001–V023 remain immutable. Trace inventory decisions use INVENTORY_DECISION / INVENTORY_CONTROL with original SIGNED_EVIDENCE, decimal signing values are exact strings, and mandatory missing headers return 400. Scope-level native verification PASS does not waive other formal task RTM or authorize full MES-012/013.


## Approved MES-012/013 completion — v1.0.18

Read [00_MES_012_013_COMPLETION_CONTRACT_V1.0.18.md](00_MES_012_013_COMPLETION_CONTRACT_V1.0.18.md), DCP-MES-012-013-CONTRACT-001, approved 2026-10-04. It supersedes missing/generic production balance, investigation/CAPA, finished QA and PDF contracts only. BalanceResult remains immutable PASS/FAIL; approval belongs to the separate investigation. Original incoming and IPC contracts stay unchanged. No runtime readiness follows from publication.


## Approved audit HIGH delta — v1.0.18

[00_AUDIT_HIGH_CONTRACT_V1.0.18.md](00_AUDIT_HIGH_CONTRACT_V1.0.18.md) governs only productionOrderId query publication, incoming read-only allowedActions and exclusive active equipment occupancy. All other inherited contracts remain unchanged. No schema/migration, new permission/route/state or signature/release-rule change.


## Approved storage/source maintenance — v1.0.18

[00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.18.md](00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.18.md) governs the bounded storageCondition, relationship manufacturerName, receipt-source snapshot, UI field cleanup and V028 delta. Other inherited contracts unchanged.
