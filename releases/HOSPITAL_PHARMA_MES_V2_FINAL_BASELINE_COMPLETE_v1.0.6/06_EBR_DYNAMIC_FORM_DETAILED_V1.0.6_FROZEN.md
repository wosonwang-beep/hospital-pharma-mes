# 医院制剂 MES V2.0 — eBR + 动态表单引擎详细设计说明书

版本：V1.0 Frozen Candidate  
定位：MES 核心引擎施工规格；供 GPT-5.6 Sol Design Review 与 Codex 实现。

## 1. 设计目标
同一引擎支持不同产品、处方、工艺路线和工序，无代码配置电子批记录。定义经过审批发布后不可原地修改；生产批下达时冻结完整定义快照。运行数据结构化保存，PDF仅为受控阅读副本。

## 2. 四层模型
1. Process Definition：工艺包、路线、工序、参数。
2. eBR Definition：模板、Section、Group、Form、Field、Option、Rule、Signature/Review策略。
3. Runtime Instance：Batch Snapshot、OperationExecution、FormInstance、FieldValueRevision、RuleExecution、Review、Signature。
4. Evidence：AuditEvent、Attachment、DeviceMessage、Deviation/IPC、PDF Manifest。

## 3. 定义层数据库
### ebr_template_version
id, org_id, package_version_id, template_code, version, status(DRAFT/SUBMITTED/APPROVED/WITHDRAWN), content_hash, effective_from, approved_by, approved_at, version_no.

### ebr_section_def
id, template_version_id, section_code(稳定ID), title, sequence_no, repeat_mode(NONE/LIST), visibility_rule_id, page_break_flag.

### ebr_group_def
id, section_def_id, group_code(稳定ID), title, sequence_no, layout_columns, repeat_mode, min_occurs, max_occurs.

### ebr_form_def
id, template_version_id, operation_def_id, form_code(稳定ID), form_name, schema_version, schema_json, sequence_no, status.

### ebr_field_def
id, form_def_id, group_def_id, field_code(稳定ID), label, field_type, source_type, data_type, unit_id, precision_scale, required_flag, readonly_flag, default_expr, placeholder, help_text, sequence_no, validation_json.

字段类型：NUMBER/TEXT/TEXTAREA/ENUM/MULTI_ENUM/BOOLEAN/DATE/TIME/DATETIME/BARCODE/MATERIAL_LOT/CONTAINER/EQUIPMENT/PERSON/ATTACHMENT/IMAGE/TIMER/CALCULATED/INSTRUMENT_VALUE/SIGNATURE_PLACEHOLDER。

source_type：MANUAL/BARCODE/INSTRUMENT/SYSTEM/DERIVED。

### ebr_option_def
id, field_def_id, option_code, option_label, option_value, sequence_no, active_flag.

### ebr_rule_def
id, template_version_id, form_def_id nullable, field_def_id nullable, rule_code, rule_type, trigger_point, expression, severity, error_code, message_template, deviation_trigger, active_flag.

rule_type：VALIDATION/CALCULATION/VISIBILITY/BRANCH/COMPLETION/SIGNATURE/REVIEW/DEVIATION。
trigger_point：ON_CHANGE/ON_SAVE/ON_SUBMIT/ON_OPERATION_COMPLETE/ON_BATCH_CLOSE。

### ebr_signature_rule
id, template_version_id, object_scope, object_code, meaning, required_role, reauth_required, sequence_no, invalidate_on_change.

### ebr_review_rule
id, template_version_id, object_scope, object_code, review_type(VERIFY/APPROVE), required_role, independent_user_required, sequence_no.

## 4. Schema 规范
schema_json 是可移植定义快照，但数据库规范化表是设计/查询/审批主模型。schema_json 由服务端发布器生成，禁止前端直接构造最终发布版本。

示例：
```json
{
  "schemaVersion": "1.0",
  "formCode": "WEIGHING_CONFIRM",
  "fields": [
    {
      "fieldCode": "actual_weight",
      "type": "NUMBER",
      "source": "INSTRUMENT",
      "unit": "kg",
      "precision": 3,
      "required": true
    }
  ],
  "rules": ["WEIGHT_WITHIN_TOLERANCE"]
}
```

fieldCode/formCode/sectionCode 在同一模板生命周期中必须稳定。改标签不等于改ID；语义改变需要新字段ID或新模板版本。

## 5. 规则引擎
规则表达式必须使用白名单 DSL，不执行 JavaScript、SQL、SpEL 或任意代码。
允许函数示例：value(fieldCode), exists(fieldCode), sum(fieldCode), abs(x), round(x,n), convert(value,from,to), status(object), hasRole(role), now()。
禁止：网络访问、文件访问、反射、数据库任意查询、动态类加载。

规则执行返回：
ruleCode, passed, severity, message, inputSnapshot, outputValue, executedAt, engineVersion, ruleVersion。

BLOCK 规则失败时服务端拒绝提交/完工；WARN 可继续但必须保留证据；deviation_trigger=true 时创建受控异常/偏差入口。

## 6. 运行实例
### ebr_batch_snapshot
main_batch_id, template_version_id, definition_hash, snapshot_json, frozen_at。一个已开工批次永远读取自己的快照。

### ebr_form_instance
operation_execution_id, form_def_snapshot_id/form_code, occurrence_no, status(DRAFT/SUBMITTED/VERIFIED/APPROVED), revision, submitted_by, submitted_at.

### ebr_field_value_revision
form_instance_id, field_code, occurrence_path, revision_no, previous_revision_id, raw_value_json, normalized_value_json, unit_id, source_type, source_ref, recorded_by, recorded_at, change_reason, superseded_flag。
只 INSERT，不 UPDATE 历史值。

### ebr_rule_execution
form_instance_id, rule_code, trigger_point, passed, severity, input_snapshot_json, output_json, engine_version, executed_at。

### ebr_review_record
object_type, object_id, review_type, reviewer_id, role_snapshot, decision, comment, reviewed_at, signature_id, invalidated_at, invalidation_reason。

### ebr_attachment
object_type, object_id, field_code, file_id, file_hash, mime_type, captured_at, uploaded_by。

## 7. 版本冻结
发布流程：DRAFT→SUBMITTED→APPROVED→EFFECTIVE。APPROVED/EFFECTIVE 不允许普通更新；任何变更复制为新版本。
批次下达事务中校验 Product/Formula/Route/eBR/Rules 均已批准有效，生成 snapshot_json + SHA-256 definition_hash。
运行期只引用 snapshot stable IDs；新模板发布不影响在制批。工艺变更必须走 Change Control，禁止静默迁移。

## 8. 电子签名
签名不是图片。gxp_signature 保存 signer_id、meaning、object_type/id、record_digest、signed_at、auth_context、revoked_signature_id。
record_digest = SHA-256(canonicalized signed record + version + relevant child evidence IDs)。
签名前服务端重认证（策略可配置）；签名后任何影响摘要的数据更正使相关 review/signature 进入 INVALIDATED，必须按规则重签。
同一关键动作如要求独立复核，reviewer_id != operator_id。

## 9. 异常与更正
误录：创建新的 FieldValueRevision，previous_revision_id 指向旧值，必须 reason；旧值永久保留。
越限：保存原始值→执行规则→BLOCK/WARN→必要时创建Deviation→批准后决定是否允许继续。
设备重发：source_message_id 幂等；重复消息不得重复写业务值。
跳步：模板必须显式允许；记录 skip_reason、权限、签名。
作废：使用VOID/REVERSED业务状态和反向事件，不物理删除。
更正影响签名：自动计算 affected evidence graph，失效对应复核/签名并产生AuditEvent。

## 10. Designer
前端 Designer 分区：
- 左：组件库（基础字段、GMP字段、业务引用、设备值、计算值）
- 中：Section/Group/Form画布，拖拽排序
- 右：属性、单位/精度、数据源、校验、显示条件、规则、签名/复核
- 顶部：版本、保存草稿、规则测试、模拟运行、提交审批、差异比较、发布

Designer 只编辑 Draft。保存采用 optimistic locking。发布前服务端执行 lint：
稳定ID唯一、必填字段合法、单位量纲一致、规则引用存在、分支可达、无循环、签名角色存在、设备映射有效、所有表达式通过白名单解析。

## 11. Renderer
Renderer 输入只有 Batch Snapshot + Runtime Data + Allowed Actions，不读取“最新模板”。
渲染协议：
fieldCode, label, type, value, displayValue, unit, required, readonly, visible, validationState, source, provenance, allowedActions。
前端只做即时体验校验；最终校验全部在服务端。
支持重复Group、条件显示、动态分支、扫码、设备自动填充、计时器、附件、电子签名入口。
每次保存携带 formRevision；冲突返回409并要求重新加载/合并。

## 12. API
GET /ebr/templates/{id}
POST /ebr/templates
POST /ebr/templates/{id}/versions
POST /ebr/versions/{id}/submit
POST /ebr/versions/{id}/approve
POST /ebr/versions/{id}/publish
POST /ebr/versions/{id}/lint
POST /ebr/versions/{id}/simulate
GET /execution-units/{id}/forms
GET /forms/{id}/render-model
PUT /forms/{id}/draft-values
POST /forms/{id}/submit
POST /forms/{id}/reviews
POST /records/{type}/{id}/sign
POST /field-values/{id}/corrections
GET /main-batches/{id}/ebr
POST /main-batches/{id}/ebr/pdf

写接口使用 Idempotency-Key；实例更新使用 If-Match/formRevision。

## 13. 权限
ebr:template:view/create/edit/submit/approve/publish
ebr:designer:edit
ebr:form:view/edit/submit
ebr:record:correct
ebr:review:verify/approve
ebr:sign
ebr:pdf:generate
ebr:audit:view
审批人不得等于提交人（按策略）；独立复核不得等于执行人。

## 14. PDF与归档
PDF生成从结构化实例、修订链、规则结果、物料/设备/IPC/偏差/签名读取。包含 definition_hash、生成时间、PDF hash、生成版本。
重生PDF产生新manifest版本，不覆盖旧PDF。PDF不是原始记录真源。

## 15. 测试
必须覆盖：模板版本不可变、批次冻结、字段稳定ID、规则计算确定性、BLOCK/WARN、条件显示、分支、重复组、单位换算、设备重发幂等、并发409、更正修订链、签名摘要、签名失效重签、独立复核、偏差触发、PDF可复现、旧批不受新模板影响。

## 16. Codex 禁止事项
不得用一个JSON大字段代替所有运行值；不得UPDATE覆盖历史FieldValue；不得前端决定最终校验；不得执行任意脚本规则；不得让运行批读取最新模板；不得把签名当图片；不得删除GxP证据；不得绕过规则/签名/审计。

# V1.0.6 Electronic Signature Binding — DCP-MES-001-R2-001
MES-001 supplies `gxp_signature`, the generic sign/verify/invalidate/re-sign services, the provider registry, canonicalization, reauthentication and the generic signing API. MES-007 supplies production providers for `EBR_FORM_INSTANCE` and `EBR_REVIEW_RECORD`; only `VERIFY` and `APPROVE` are permitted. MES-013 supplies `QA_RELEASE_DECISION`; only `RELEASE` and `REJECT` are permitted. The generic sign endpoint does not release a batch.

Each provider exposes stable `objectType`, string `objectId`, current `recordVersion`, provider-owned `canonicalRecord`, sorted `evidenceIds`, closed `allowedMeanings`, `loadForSignature` and `validateSignable`. The exact envelope is:

```json
{"schemaVersion":"1.0","objectType":"EBR_FORM_INSTANCE","objectId":"123","recordVersion":7,"record":{},"evidenceIds":["ATTACHMENT:9","FIELD_REVISION:81"]}
```

The server computes `lowercaseHex(SHA-256(UTF8(JCS(envelope))))` using RFC 8785. The client never submits a digest. Reauthentication is exactly five minutes, single-use, bound to user/session/org/object/meaning/version, and atomically consumed on the first sign attempt. Consumption is not rolled back if the database transaction fails. Credential/token values never enter logs, AuditEvent or `auth_context_json`.

A correction that changes signed evidence invalidates affected VALID signatures in the correction transaction and records reason/time/audit. Re-sign inserts a new row; `revoked_signature_id` references the immediately superseded invalidated signature for the same object/meaning. Historical invalidated evidence remains queryable.



## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.
