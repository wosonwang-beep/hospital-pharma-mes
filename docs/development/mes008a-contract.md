# MES-008A incoming quality implementation contract

Preparation input for the cumulative v1.0.10 release, under explicit approval of DCP-MES-008A-CONTRACT-001. This working document is not an independent authority. Product implementation waits for the root agent's baseline publication. Existing v1.0.9 and executed V001–V013 remain immutable.

`mes008a-contract.json` contains the 32 existing operations, exact existing permissions and 36 fully specified OpenAPI schemas. IDs and decimals are strings, UTC instants use date-time, page size is at most 100. API response envelopes remain in controllers. No GenericRequest or GenericResponse is used. POST commands require Idempotency-Key; commands on existing aggregates require versionNo/If-Match agreement. Create returns 201, other commands 200; authorization 403, not found 404, state/version/replay conflict 409, failed gates 422. Every list/detail is organization-scoped. List filters are page, size, keyword, status and materialLotId where applicable.

## Physical mapping for V014

All names below are physical tables. Camel-case schema properties map to snake-case columns. IDs are BIGINT; amounts DECIMAL(20,6); booleans TINYINT(1); dates DATE; instants DATETIME(3); bounded strings use the OpenAPI maximum. Header Profile M includes id/org_id/created_by/created_at/updated_by/updated_at/version_no; immutable evidence uses Profile A. A Profile A response may expose versionNo=0 and updated metadata equal to creation as a projection, never mutable stored evidence. `allowedActions`, nested arrays and computed views are not columns. Stable numbers have organization-scoped uniqueness. Parent references, material/unit/equipment/actor/signature references receive FKs to existing physical producers. Parent-child same-org validation is mandatory before insertion.

| Table | Physical business columns / source |
|---|---|
| qms_inspection_request | request_no, request_type, receipt_id, requested_date, priority, status, record_status; IncomingRequest |
| qms_inspection_request_item | inspection_request_id, material_lot_id, quality_standard_version, specification_version, requested_qty, unit_id, package_count; IncomingRequestItem |
| qms_sampling_task | task_no, inspection_request_item_id, material_lot_id, sampling_plan, sop_version, qualification_code, assigned_to, status, record_status, signature_id |
| qms_sampling_detail | sampling_task_id, container_id, sampling_point, sample_no, sample_type, sample_qty, unit_id, tool, conditions, reseal_passed, storage_condition, collected_by, collected_at; Profile A |
| qms_sample | sample_no, sample_scope, main_batch_id nullable, material_lot_id, sampling_task_id, sampling_detail_id, inspection_request_item_id, sample_type, sample_status, status (legacy-compatible same value), sample_qty, unit_id, collected_by, collected_at, sampled_at (same collection instant), received_by nullable, received_at nullable, storage_condition |
| qms_inspection_task | task_no, sample_id, material_lot_id, qualification_code, assigned_to, status, record_status, signature_id |
| qms_inspection_item | inspection_task_id, test_code, test_name, method_version, criteria, lower_limit nullable, upper_limit nullable, unit_id nullable, required |
| qms_test_execution | inspection_item_id, instrument_id nullable, reagent nullable, reference_standard nullable, observations_json, calculations_json, attachment_ids_json, performed_by, performed_at; Profile A |
| qms_test_result_revision | sample_id, test_code, inspection_item_id, test_execution_id, revision_no, previous_revision_id nullable, numeric_value nullable, text_value nullable, result_value (compatible original display), unit_id nullable, conclusion, result_status (compatible conclusion), change_reason, recorded_by, recorded_at, reviewed_by nullable, reviewed_at nullable, signature_id; Profile A; UNIQUE(inspection_item_id,revision_no) |
| qms_inspection_report | report_no, inspection_task_id, material_lot_id, record_status, conclusion, reviewed_by nullable, reviewed_at nullable, approved_by nullable, approved_at nullable, signature_id nullable |
| qms_inspection_report_item | inspection_report_id, inspection_item_id, result_revision_id, conclusion; Profile A |
| qms_release_decision | release_scope, main_batch_id nullable, finished_lot_id nullable, material_lot_id nullable, inspection_report_id nullable, decision, release_basis, decision_source, reason, decision_by, decision_at, signature_id nullable, supersedes_decision_id nullable, decision_digest, rule_version nullable, qualification_snapshot_json nullable; Profile A |

V014 installs no MainBatch table. qms_sample.main_batch_id and qms_release_decision.main_batch_id stay nullable and incoming commands require NULL. MES-009 installs the deferred MainBatch FKs using V015 or its own next reserved migration after creating its actual producer. finished_lot_id references md_material_lot.id and is NULL for incoming scope. Scope/target CHECK enforces INCOMING_MATERIAL: material_lot_id non-null and main_batch_id/finished_lot_id null; FINISHED_PRODUCT: main_batch_id non-null and material_lot_id null, finished_lot_id optional canonical lot reference. Human decisions require a signature; system decisions require technical actor, rule version/digest/snapshot and NULL signature. Result revisions, executions, sampling details, report items and release decisions reject UPDATE/DELETE at database level. Mutable controlled rows reject DELETE and use optimistic writes.

Result rows remain immutable including reviewer columns: review evidence is represented by signed immutable inspection-task content referencing exact revisions. A result row's nullable review columns are not retroactively updated. Report generation resolves only the final revisions included in a valid approved inspection review. Approved report content cannot change. Later correction makes prior review non-current and invalidates its signature without editing the historical result. Historical approved report references remain evidence; release rechecks current revisions and rejects stale evidence. OOS/OOT history is never erased; unresolved failed evidence cannot be hidden by a later PASS.

## Application and integration contracts

Dependencies: mes-qms -> mes-masterdata, mes-wms, mes-audit; mes-release -> mes-qms, mes-wms, mes-masterdata, mes-audit. WMS must not depend on qms/release. Production adapters in the executable composition root combine actual batch and material gates.

Existing consumed queries:

```java
JsonNode MaterialQueryService.requireUsable(long org, long materialId, Instant at);
JsonNode ApprovedSupplierQueryService.requireApproved(long org, long materialId, long supplierId, Instant at);
void MasterQueryService.requireQualification(long org, long user, String code, Instant at);
JsonNode WmsQueryService.receipt(long organizationId, long id);
JsonNode WmsQueryService.receiptItem(long organizationId, long id);
JsonNode WmsQueryService.materialLot(long organizationId, long id);
List<JsonNode> WmsQueryService.ledger(long organizationId, long materialLotId);
```

Produced queries, typed records (no HTTP envelopes):

```java
record IncomingQualityEvidence(long materialLotId, Long reportId,
  String reportStatus, String conclusion, boolean currentResults,
  boolean completeLineage, boolean unresolvedFailure, String digest) {}
IncomingQualityEvidence IncomingQualityQueryService.releaseEvidence(long organizationId, long lotId);
record MaterialEligibility(long materialLotId, boolean eligible, Long releaseDecisionId,
  String qualityStatus, String inventoryStatus, Instant evaluatedAt, List<Gate> gates) {}
record Gate(String code, boolean passed, String message, List<String> requiredActions) {}
MaterialEligibility MaterialEligibilityService.evaluate(long organizationId, long materialLotId, Instant at);
void MaterialEligibilityService.requireEligible(long organizationId, long materialLotId, Instant at);
```

WMS-owned domain events must be synchronized within the initiating transaction, never AFTER_COMMIT for regulated state. Proposed event signatures for root integration:

```java
record QualityLotStateChanged(long organizationId, long materialLotId,
  long expectedVersion, String qualityStatus, String inventoryStatus,
  long actorId, String reason, String idempotencyKey) {}
record MaterialReceiptConfirmed(long organizationId, long receiptId,
  List<Long> materialLotIds, long actorId, String requestId) {}
```

QMS publishes QualityLotStateChanged; WMS owns its optimistic update and audit. WMS publishes MaterialReceiptConfirmed after creating its lots/ledger but before transaction completion. Release handles only exemption evaluation using the snapshot plus current material/supplier/approved relationship and receipt checks. Both event types live in mes-wms.domain. The exemption handler uses an existing registered technical principal, verified org/active identity, never a forged user or synthetic signature. The implementation registers/resolves the technical identity through existing IAM infrastructure (no interactive password or role grant) before evaluating exemption. It does not assume bootstrap administrator is a technical actor. Infrastructure failure retains BLOCKED; no fabricated identity is accepted. Root owns the idempotent registration integration using the existing sys_user identity infrastructure; this is not an added business approval or required manual configuration step. Repeated receipt delivery cannot create a second effective decision; lock lot and resolve existing decision/supersession.

MaterialEligibilityService is owned by mes-release; it checks current immutable decision/supersession, material lot RELEASED, inventory AVAILABLE, not frozen, expiry/retest and on-hand evidence. It fails 422 MATERIAL_NOT_ELIGIBLE. WMS's ProductionMaterialGate also checks real MainBatch via the root adapter; no placeholder is used. Gate evaluation must occur while the final consumer transaction holds the material-lot lock so concurrent freeze cannot race a production write.

## Controlled behavior

- Request submit changes DRAFT→SUBMITTED; acceptance SUBMITTED→ACCEPTED and lot QUARANTINE→PENDING_SAMPLING. Sampling task creation requires an accepted matching request item. Assign/start/complete follow the frozen states. Details require assigned qualified actor and active task; generate traceable sample tied to detail. Failed reseal blocks completion. Sampling completion signs the task and exact details; no direct lot→sample API exists.
- Sample receipt/IN_TEST changes occur atomically under the existing inspection create/start commands, recording receiver/time; no unapproved sample mutation endpoint is added. Test task assignment/start require qualified assignee. Inspection item definitions are frozen on creation, execution records preserve raw observations/calculations/attachments. Required items must all have final results before submit-review. Result confirmation/correction signs the exact new immutable revision; correction must target current revision and preserve previous revision/signature. Read/query returns full history.
- Review requires a qualified actor distinct from every performer/result recorder. PASS sets QC_PASSED only, never AVAILABLE; FAIL/OOS/OOT sets QC_FAILED/PENDING_DISPOSITION and remains blocked. Disposition is an MES-012 consumed contract; absence cannot authorize release. Report creation requires signed current inspection review, copies references rather than values, review is distinct from preparer, approval distinct from preparer/reviewer, approval signs complete report lineage and moves lot to PENDING_QA_RELEASE.
- USER_QA release/reject signs immutable decision, refreshes exact report/revisions/gates and lot version, and atomically records decision/audit/lot/outbox. QA must not be an evidence performer/reviewer whose own work is being released. Signed methods require existing ebr:sign in addition to business permission. Result correction may not rewrite approved report or release history. Supersession always explicitly names the latest decision; stale targets fail 409.
- SYSTEM_RULE exemption creates no request/task/sample/execution/result/report. It uses receipt inspection-policy snapshot, requires all receipt checks, current material and approved supplier relationship, unexpired/unfrozen lot, deterministic rule/snapshot digest and registered technical actor. It creates the sole scoped decision and moves quality/inventory atomically to RELEASED/AVAILABLE with no human signature.
- All writes use MasterMutation/shared platform idempotency, same-transaction audit and shared signature provider registry. Signature meaning mappings use existing VERIFY/APPROVE/RELEASE/REJECT meanings (confirmation/correction use VERIFY; no new SignatureMeaning enum); providers bind object ID/version/content. Credential/token never enters snapshots/audit. Permission, org, version, qualification, independence and state gates are rechecked on final commands.

Qualification codes are existing organization-scoped master identifiers associated with the controlled sampling SOP/test method, not newly invented SAMPLING/QC_TEST/QC_REVIEW/QA_RELEASE enum semantics. The qualificationCode fields select such existing identifiers; server validation ties them to the frozen plan/method and rechecks qualifications at assignment/execution/review. No default code or administrator bypass is allowed.

## Verification ownership

Parent executes gates after baseline publication and product implementation. Quality tests will cover the required incoming chain, nonfabricated exemption, wrong lineage, invalid qualifications/expiry, independent signatures, immutable revision history, stale report rejection, same-key replay/mismatch, audit rollback, stale optimistic writes, scoped reads, failure/OOS blocking and real WMS availability transition. Test fixtures use unique business numbers and rollback in native persistent MariaDB. V001–V013 history is only validated, never repaired. Production weigh/charge consumption remains its actual owner gate; it cannot be claimed passing by mocks.


## User workflow clarification — 2026-10-03

用户明确了供应商送货到MES投料的六类来料记录链。本节记录实现输入和核对结果，不替代冻结基线，不表示功能已经开发或验收。依据：用户本轮流程图；v1.0.9功能设计246–257行、数据库790–820行、状态机63–73行。

| 环节 | 必须保留的关联和控制 | 当前核对结果 |
|---|---|---|
| ① 原辅料收货记录 | 物料、供应商、供应商批号、到货数量、生产日期/有效期、包装检查、COA等随货资料；确认后生成内部MaterialLot并入待验库存 | WMS代码已有收货→lot/库存事务及QUARANTINE/BLOCKED；当前receipt DTO、entity、页面未发现COA随货文件关联，不能以包装检查通过替代附件证据，需在后续累计契约中明确复用文件能力的存储/访问/审计映射。 |
| ② 请验单 | 关联收货及MaterialLot、请验数量、检验类型、适用质量标准版本；QC接收后开展取样 | 与既有设计一致；标准版本为质量标准引用，不恢复已取消的物料版本审批。 |
| ③ 取样记录 | Lot、位置、数量、方法/SOP、取样人、时间；SamplingDetail生成Sample；区分检验样和留样 | 既有sample_type字段承载用途，留样不伪造检验结果；同一取样来源、数量、单位及储存信息可追溯；不得直接Lot→Sample跳过取样记录。 |
| ④ 检验记录 | 项目、方法、冻结标准范围、原始结果、仪器、检验人、复核人 | 超标结果保留并阻断；OOS调查未关闭不得通过追加PASS遮盖失败历史；结果更正追加revision。所有必需项目的当前有效结果齐全并经复核后才能生成报告。 |
| ⑤ 检验报告 | 汇总确切检验记录及result revision、适用标准版本、项目结果、综合结论、QC审核 | QC合格只进入后续QA审核，不自动AVAILABLE；报告不重新手抄不可追溯结果。 |
| ⑥ 物料放行记录 | QA检查报告及OOS/偏差处置证据，作出带审计/签名的RELEASED或REJECTED决定 | 放行决定与lot质量状态、库存可用状态同事务；REJECTED保持BLOCKED，隔离/退货/处置保留独立受控证据，不物理删除。 |
| 下游领料/称量/投料 | 在实际消费事务重新检查有效放行决定、冻结、有效期/复验期、库存和真实生产上下文 | 库存有数量不代表可用；质量模块正常链可独立于MES009/010契约问题推进，实际预留/发料仍需真实批生产者。MES011称量/投料、MES012调查仍保留其原任务所有权，图示不被当作自动扩张所有后续任务的授权。 |

本图按必验来料主流程落实。没有把图中未绘制的既有受控免验分支解释为删除授权；免验仍须真实规则/资格/收货证据及独立ReleaseDecision，绝不伪造请验、取样、检验或报告。

当前核对不构成MES009/010新状态/字段/API补齐提案的批准。此前已批准的MES008A页面与外键补齐继续有效。全部页面标签与控件仍按既有规范保持同行。
