# 12 医院制剂 MES V2.0 正式 Test Case 设计说明书 V1.0.15 FROZEN

共 61 条核心 Test Case。

### TC-IAM-001 — 有效用户登录
Requirement: `IAM-001` | Task: `MES-002-R2` | Module: IAM | Type: NORMAL
**Preconditions**：ACTIVE用户存在
**Steps**：提交正确用户名密码
**Expected**：200/JWT；身份正确
**Evidence**：API+security log
**Automation**：API integration

### TC-IAM-002 — 禁用用户拒绝登录
Requirement: `IAM-001` | Task: `MES-002-R2` | Module: IAM | Type: NEGATIVE
**Preconditions**：DISABLED用户
**Steps**：登录
**Expected**：401/403；无token
**Evidence**：API+security log
**Automation**：API integration

### TC-IAM-003 — 无权限API拒绝
Requirement: `IAM-002` | Task: `MES-002-R2` | Module: IAM | Type: PERMISSION
**Preconditions**：用户无master:material:view
**Steps**：GET /materials
**Expected**：403
**Evidence**：API
**Automation**：API integration

### TC-IAM-004 — 菜单按钮权限一致
Requirement: `IAM-002` | Task: `MES-002-R2` | Module: IAM | Type: PERMISSION
**Preconditions**：不同角色
**Steps**：登录并访问菜单/直接URL/API
**Expected**：UI隐藏且API仍403
**Evidence**：UI+API
**Automation**：E2E+API

### TC-MAT-001 — 创建完整物料Draft
Requirement: `MD-MAT-001` | Task: `MES-004-R2` | Module: Material | Type: NORMAL
**Preconditions**：单位存在
**Steps**：Create页填写完整字段保存
**Expected**：201；Draft；字段持久化
**Evidence**：UI+API+DB+Audit
**Automation**：E2E+API

### TC-MAT-002 — 重复物料编码
Requirement: `MD-MAT-001` | Task: `MES-004-R2` | Module: Material | Type: NEGATIVE
**Preconditions**：编码已存在
**Steps**：再次创建
**Expected**：409/422 MATERIAL_CODE_EXISTS；不新增
**Evidence**：API+DB
**Automation**：API integration

### TC-MAT-003 — 批准版本不可直接编辑
Requirement: `MD-MAT-001` | Task: `MES-004-R2` | Module: Material | Type: STATE
**Preconditions**：Approved版本
**Steps**：PUT普通编辑
**Expected**：409/422；提示创建新版本
**Evidence**：API+DB+Audit
**Automation**：API integration

### TC-MAT-004 — 新版本不改变历史引用
Requirement: `MD-MAT-001` | Task: `MES-004-R2` | Module: Material | Type: GXP
**Preconditions**：历史批引用V1
**Steps**：创建并批准V2
**Expected**：历史批仍引用V1
**Evidence**：DB+batch snapshot
**Automation**：Integration

### TC-MD-001 — 单位量纲不兼容
Requirement: `MD-002` | Task: `MES-003-R2` | Module: Master | Type: NEGATIVE
**Preconditions**：kg与L无物料专属换算
**Steps**：请求换算
**Expected**：422 UNIT_DIMENSION_MISMATCH
**Evidence**：API
**Automation**：Unit

### TC-EQP-001 — 过校准设备阻断
Requirement: `MD-EQP-001` | Task: `MES-003-R2` | Module: Equipment | Type: GATE
**Preconditions**：设备校准过期
**Steps**：尝试开始受控工序
**Expected**：422；requiredActions包含设备处理
**Evidence**：API+Audit
**Automation**：Integration

### TC-QUAL-001 — 无资格人员阻断
Requirement: `MD-QUAL-001` | Task: `MES-003-R2` | Module: Qualification | Type: GATE
**Preconditions**：操作员资格过期
**Steps**：开始工序/称量
**Expected**：422 QUALIFICATION_REQUIRED
**Evidence**：API+Audit
**Automation**：Integration

### TC-PROC-001 — 批准版不可变
Requirement: `PROC-001` | Task: `MES-006-R2` | Module: Process | Type: GXP
**Preconditions**：ProcessVersion APPROVED
**Steps**：PUT formula/route
**Expected**：409/422
**Evidence**：API+DB+Audit
**Automation**：Integration

### TC-PROC-002 — Route死循环Lint
Requirement: `PROC-ROUTE-001` | Task: `MES-006-R2` | Module: Process | Type: NEGATIVE
**Preconditions**：构造循环路线
**Steps**：POST lint
**Expected**：422/Lint issue定位节点
**Evidence**：API
**Automation**：Unit+Integration

### TC-PROC-003 — BOM单位量纲校验
Requirement: `PROC-BOM-001` | Task: `MES-006-R2` | Module: Process | Type: NEGATIVE
**Preconditions**：物料质量单位
**Steps**：配置不兼容单位
**Expected**：422
**Evidence**：API
**Automation**：Unit

### TC-EBR-001 — 稳定Field ID唯一
Requirement: `EBR-001` | Task: `MES-007-R2` | Module: eBR | Type: NEGATIVE
**Preconditions**：同Form已有fieldCode
**Steps**：Designer复制为相同ID并Lint
**Expected**：Lint失败且定位字段
**Evidence**：UI+API
**Automation**：E2E

### TC-EBR-002 — 禁止任意脚本
Requirement: `EBR-003` | Task: `MES-007-R2` | Module: eBR | Type: SECURITY
**Preconditions**：Draft规则
**Steps**：输入JS/SQL/SpEL表达式
**Expected**：解析拒绝；不执行
**Evidence**：API+log
**Automation**：Unit+Security

### TC-EBR-003 — 批次冻结Snapshot
Requirement: `EBR-004` | Task: `MES-007-R2` | Module: eBR | Type: GXP
**Preconditions**：模板V1 Effective
**Steps**：Release MainBatch后发布V2
**Expected**：运行批仍读取V1 hash/snapshot
**Evidence**：API+DB
**Automation**：Integration

### TC-EBR-004 — Renderer不读最新模板
Requirement: `EBR-005` | Task: `MES-007-R2` | Module: eBR | Type: GXP
**Preconditions**：批次冻结V1且V2已发布
**Steps**：GET render-model
**Expected**：字段来自V1
**Evidence**：API
**Automation**：Integration

### TC-EBR-005 — BLOCK规则阻断提交
Requirement: `EBR-003` | Task: `MES-007-R2` | Module: eBR | Type: GATE
**Preconditions**：字段违反BLOCK规则
**Steps**：POST form submit
**Expected**：422 EBR_RULE_BLOCKED；RuleExecution保留
**Evidence**：API+DB+Audit
**Automation**：Integration

### TC-EBR-006 — 更正追加Revision
Requirement: `EBR-006` | Task: `MES-007-R2` | Module: eBR | Type: GXP
**Preconditions**：已有revision1
**Steps**：提交Correction
**Expected**：revision2创建；revision1不变；reason保留
**Evidence**：DB+Audit
**Automation**：Integration

### TC-EBR-007 — 更正使签名失效
Requirement: `EBR-007` | Task: `MES-007-R2` | Module: eBR | Type: GXP
**Preconditions**：记录已Review+Sign
**Steps**：更正受签字段
**Expected**：旧Review/Signature INVALIDATED；要求重签
**Evidence**：DB+Audit
**Automation**：Integration

### TC-EBR-008 — 独立复核禁止同人
Requirement: `EBR-007` | Task: `MES-007-R2` | Module: eBR | Type: PERMISSION
**Preconditions**：operator=A
**Steps**：A尝试verify
**Expected**：422 SELF_VERIFICATION_FORBIDDEN
**Evidence**：API+Audit
**Automation**：Integration

### TC-EBR-009 — 并发Form Revision
Requirement: `EBR-005` | Task: `MES-007-R2` | Module: eBR | Type: CONCURRENCY
**Preconditions**：两个客户端revision=3
**Steps**：A保存成功；B随后保存
**Expected**：B=409 REVISION_CONFLICT；A数据不丢
**Evidence**：API+DB
**Automation**：Concurrency

### TC-EBR-010 — PDF版本不覆盖
Requirement: `EBR-008` | Task: `MES-013-R2` | Module: eBR | Type: GXP
**Preconditions**：已生成PDF v1
**Steps**：再次生成
**Expected**：生成v2 manifest；v1仍可取；hash记录
**Evidence**：DB+file metadata
**Automation**：Integration

### TC-WMS-001 — 待验物料不可领用
Requirement: `WMS-001` | Task: `MES-008-R2` | Module: WMS | Type: GATE
**Preconditions**：MaterialLot QUARANTINE
**Steps**：预留/发料
**Expected**：422 MATERIAL_NOT_RELEASED
**Evidence**：API
**Automation**：Integration

### TC-WMS-002 — 库存流水可重算
Requirement: `WMS-002` | Task: `MES-008-R2` | Module: WMS | Type: DATA
**Preconditions**：多次收发移退
**Steps**：按ledger汇总
**Expected**：结果等于查询库存；无历史UPDATE
**Evidence**：DB query
**Automation**：Integration

### TC-WMS-003 — 库存幂等
Requirement: `WMS-002` | Task: `MES-008-R2` | Module: WMS | Type: IDEMPOTENCY
**Preconditions**：同Idempotency-Key
**Steps**：重复receive两次
**Expected**：仅一条业务movement
**Evidence**：API+DB
**Automation**：Integration

### TC-WMS-004 — FEFO预留
Requirement: `WMS-003` | Task: `MES-008-R2` | Module: WMS | Type: NORMAL
**Preconditions**：两个RELEASED批次不同效期
**Steps**：自动预留
**Expected**：优先较早效期且未过期
**Evidence**：API+DB
**Automation**：Integration

### TC-WMS-005 — 发料不等于投料
Requirement: `WMS-004` | Task: `MES-008-R2` | Module: WMS | Type: DOMAIN
**Preconditions**：已发10kg未投料
**Steps**：查询Charge/Balance
**Expected**：Charge=0；不能计实际消耗
**Evidence**：DB+API
**Automation**：Integration

### TC-BAT-001 — Release冻结定义
Requirement: `PRD-002` | Task: `MES-009-R2` | Module: Production | Type: NORMAL
**Preconditions**：工艺/eBR有效
**Steps**：POST batch release
**Expected**：RELEASED；snapshot/ExecutionUnit/Audit同事务
**Evidence**：API+DB+Audit
**Automation**：Integration

### TC-BAT-002 — SubBatch禁止放行
Requirement: `PRD-003` | Task: `MES-009-R2` | Module: Production | Type: DOMAIN
**Preconditions**：SubBatch完成
**Steps**：尝试Release SubBatch
**Expected**：422 SUBBATCH_RELEASE_FORBIDDEN
**Evidence**：API
**Automation**：Integration

### TC-BAT-003 — DIRECT执行单元无SubBatch
Requirement: `PRD-004` | Task: `MES-009-R2` | Module: Production | Type: DOMAIN
**Preconditions**：不分亚批
**Steps**：Release batch
**Expected**：ExecutionUnit unitType=DIRECT, subBatchId=null
**Evidence**：DB
**Automation**：Integration

### TC-BAT-004 — SUB_BATCH执行单元必须有SubBatch
Requirement: `PRD-004` | Task: `MES-009-R2` | Module: Production | Type: DOMAIN
**Preconditions**：配置亚批
**Steps**：Release batch
**Expected**：每个SUB_BATCH unit关联subBatch
**Evidence**：DB
**Automation**：Integration

### TC-OP-001 — 非法工序状态转换
Requirement: `MES-OP-001` | Task: `MES-010-R2` | Module: MES | Type: STATE
**Preconditions**：Operation PENDING
**Steps**：直接complete
**Expected**：409 INVALID_OPERATION_STATE
**Evidence**：API
**Automation**：Integration

### TC-OP-002 — IPC未完成阻断工序
Requirement: `MES-OP-001` | Task: `MES-010-R2` | Module: MES | Type: GATE
**Preconditions**：required IPC pending
**Steps**：complete operation
**Expected**：422；Operation不变
**Evidence**：API+DB
**Automation**：Integration

### TC-PAR-001 — 设备消息重发幂等
Requirement: `MES-PAR-001` | Task: `MES-010-R2` | Module: MES | Type: IDEMPOTENCY
**Preconditions**：相同sourceMessageId
**Steps**：提交两次参数消息
**Expected**：仅一个业务值
**Evidence**：DB
**Automation**：Integration

### TC-WGH-001 — 正确物料称量
Requirement: `MES-WGH-001` | Task: `MES-011-R2` | Module: Weighing | Type: NORMAL
**Preconditions**：lot RELEASED/BOM匹配/秤有效
**Steps**：创建称量
**Expected**：201；actual/target/偏差正确
**Evidence**：API+DB+Audit
**Automation**：E2E+Integration

### TC-WGH-002 — 错误物料阻断
Requirement: `MES-WGH-001` | Task: `MES-011-R2` | Module: Weighing | Type: NEGATIVE
**Preconditions**：lot不属于BOM
**Steps**：称量
**Expected**：422 MATERIAL_NOT_IN_BOM
**Evidence**：API
**Automation**：Integration

### TC-WGH-003 — 超称量偏差阻断
Requirement: `MES-WGH-001` | Task: `MES-011-R2` | Module: Weighing | Type: GATE
**Preconditions**：actual超tolerance
**Steps**：确认
**Expected**：422 WEIGHING_OUT_OF_TOLERANCE
**Evidence**：API+Audit
**Automation**：Integration

### TC-CHG-001 — 投料原子事务
Requirement: `MES-CHG-001` | Task: `MES-011-R2` | Module: Charge | Type: TRANSACTION
**Preconditions**：有效称量/lot/operation
**Steps**：POST material-charge
**Expected**：Charge+QuantityEvent+Ledger+Genealogy+Audit全部成功
**Evidence**：DB+Audit
**Automation**：Integration

### TC-CHG-002 — 投料事务回滚
Requirement: `MES-CHG-001` | Task: `MES-011-R2` | Module: Charge | Type: TRANSACTION
**Preconditions**：模拟Genealogy写失败
**Steps**：POST material-charge
**Expected**：整个事务回滚；无部分消耗
**Evidence**：DB
**Automation**：Integration

### TC-CHG-003 — 重复提交幂等
Requirement: `MES-CHG-001` | Task: `MES-011-R2` | Module: Charge | Type: IDEMPOTENCY
**Preconditions**：同key
**Steps**：POST两次
**Expected**：只有一次Charge/Consume
**Evidence**：API+DB
**Automation**：Integration

### TC-TRC-001 — 正反向追溯
Requirement: `TRC-001` | Task: `MES-011-R2` | Module: Trace | Type: NORMAL
**Preconditions**：已有Charge/Genealogy
**Steps**：按成品批/物料批查询
**Expected**：双向链路一致
**Evidence**：UI+API
**Automation**：E2E

### TC-BAL-001 — 从QuantityEvent计算
Requirement: `BAL-002` | Task: `MES-012-R2` | Module: Balance | Type: DATA
**Preconditions**：存在Charge/Return/Output/Loss
**Steps**：计算
**Expected**：Expected/Actual/Difference按冻结规则
**Evidence**：API+DB
**Automation**：Unit+Integration

### TC-BAL-002 — FAIL阻断结批
Requirement: `BAL-003` | Task: `MES-012-R2` | Module: Balance | Type: GATE
**Preconditions**：Balance FAIL未调查
**Steps**：Complete/Submit QA
**Expected**：422 BALANCE_GATE_FAILED
**Evidence**：API
**Automation**：Integration

### TC-BAL-003 — 批准调查后Gate
Requirement: `BAL-003` | Task: `MES-012-R2` | Module: Balance | Type: NORMAL
**Preconditions**：FAIL调查已批准
**Steps**：重新计算/继续
**Expected**：按规则允许下一状态；调查证据保留
**Evidence**：DB+Audit
**Automation**：Integration

### TC-QMS-001 — 检验结果不可覆盖
Requirement: `QMS-001` | Task: `MES-012-R2` | Module: QMS | Type: GXP
**Preconditions**：result revision1
**Steps**：修订结果
**Expected**：revision2新增；revision1保留
**Evidence**：DB+Audit
**Automation**：Integration

### TC-QMS-002 — 关键偏差阻断Release
Requirement: `QMS-DEV-001` | Task: `MES-012-R2` | Module: QMS | Type: GATE
**Preconditions**：Critical Deviation OPEN
**Steps**：QA Release
**Expected**：422 RELEASE_GATE_FAILED
**Evidence**：API
**Automation**：Integration

### TC-QMS-003 — 重测需审批
Requirement: `QMS-DEV-001` | Task: `MES-012-R2` | Module: QMS | Type: GXP
**Preconditions**：OOS结果
**Steps**：直接新增替代PASS
**Expected**：拒绝或进入批准流程；OOS不消失
**Evidence**：API+DB
**Automation**：Integration

### TC-REL-001 — 所有Gate通过放行
Requirement: `REL-001` | Task: `MES-013-R2` | Module: Release | Type: NORMAL
**Preconditions**：PENDING_QA且eBR/QC/Balance/Deviation/Signature PASS
**Steps**：POST release decision RELEASED
**Expected**：immutable ReleaseDecision；finished lot可用；Audit/Signature
**Evidence**：API+DB+Audit
**Automation**：E2E+Integration

### TC-REL-002 — QC PASS不能直接放行
Requirement: `REL-001` | Task: `MES-013-R2` | Module: Release | Type: DOMAIN
**Preconditions**：QC PASS但无ReleaseDecision
**Steps**：查询库存/批次
**Expected**：仍非QA_RELEASED/不可正式可用
**Evidence**：DB+API
**Automation**：Integration

### TC-REL-003 — Gate变化阻断签名提交
Requirement: `REL-001` | Task: `MES-013-R2` | Module: Release | Type: CONCURRENCY
**Preconditions**：打开Release Dialog后偏差新建
**Steps**：提交Release
**Expected**：409/422；Dialog展示变化；无Decision
**Evidence**：UI+API+DB
**Automation**：E2E

### TC-REL-004 — ReleaseDecision不可修改
Requirement: `REL-001` | Task: `MES-013-R2` | Module: Release | Type: GXP
**Preconditions**：已有RELEASED decision
**Steps**：PUT/直接更新
**Expected**：禁止；需superseding decision
**Evidence**：API+DB
**Automation**：Integration

### TC-AUD-001 — 业务与审计同事务
Requirement: `AUD-001` | Task: `MES-001-R2` | Module: Audit | Type: TRANSACTION
**Preconditions**：关键写操作
**Steps**：执行成功/失败
**Expected**：成功有Audit；业务回滚时不出现孤立Audit
**Evidence**：DB
**Automation**：Integration

### TC-SIG-001 — 摘要篡改检测
Requirement: `SIG-001` | Task: `MES-001-R2` | Module: Signature | Type: SECURITY
**Preconditions**：已签名记录
**Steps**：改变受签内容/验证
**Expected**：digest不匹配；签名无效/阻断
**Evidence**：DB+API
**Automation**：Security

### TC-INT-001 — Inbox消息幂等
Requirement: `INT-001` | Task: `MES-001-R2` | Module: Integration | Type: IDEMPOTENCY
**Preconditions**：同source+messageId
**Steps**：接收两次
**Expected**：只处理一次
**Evidence**：DB
**Automation**：Integration

### TC-INT-002 — Outbox失败重试
Requirement: `INT-001` | Task: `MES-001-R2` | Module: Integration | Type: RESILIENCE
**Preconditions**：外部服务失败
**Steps**：发送/重试
**Expected**：业务已提交；outbox重试；不重复业务
**Evidence**：DB+logs
**Automation**：Integration

### TC-UI-001 — 物料查询/新增/详情/修改独立路由
Requirement: `MD-MAT-001` | Task: `MES-004-R2` | Module: UI | Type: UI
**Preconditions**：有权限
**Steps**：从列表分别导航Create/View/Edit
**Expected**：路由独立；列表无CRUD Modal/Drawer
**Evidence**：E2E screenshot/route
**Automation**：E2E

### TC-UI-002 — 批次查询与详情分离
Requirement: `PRD-002` | Task: `MES-009-R2` | Module: UI | Type: UI
**Preconditions**：批次存在
**Steps**：列表→详情
**Expected**：详情独立Route；复杂状态动作只在详情
**Evidence**：E2E
**Automation**：E2E

### TC-SUP-001 — 合格供应商关系
Requirement: `MD-SUP-001` | Task: `MES-005-R2` | Module: Supplier | Type: NORMAL
**Preconditions**：物料/供应商有效
**Steps**：批准物料-供应商关系
**Expected**：关系有效并可用于收货校验
**Evidence**：API+DB+Audit
**Automation**：Integration

### TC-SUP-002 — 过期供应商阻断
Requirement: `MD-SUP-001` | Task: `MES-005-R2` | Module: Supplier | Type: GATE
**Preconditions**：关系validTo已过期
**Steps**：收货该物料
**Expected**：422 SUPPLIER_NOT_APPROVED
**Evidence**：API+Audit
**Automation**：Integration

# V1.0.15 MES-001 Controlled Test Details — DCP-MES-001-R2-001
## TC-MIG-001

Verify upgrade from the actual physical V001/V002 repository and clean migration to the planned head without modifying either historical checksum. Confirm the ledger maps logical LG-001 to planned physical V003/V004 and that no SQL is present in this design-only release.

## TC-AUD-002

Query concrete filters/pagination/fixed sort, actor/role/request/source correlation and append-only DB guards. Verify no secret or raw payload appears. A forced rollback of a manual retry persists neither transition nor audit event.

## TC-SIG-002

Reauthenticate and verify exactly five-minute TTL, one-time consumption, all seven bindings, replay rejection, redaction, and first-attempt atomic consumption.

## TC-SIG-003

Force the sign database transaction to roll back after token consumption. Verify no signature is committed and the token remains consumed. Reauthenticate, sign again and reproduce the RFC 8785/SHA-256 digest independently.

## TC-INT-003

Drive failures 1–8 and assert delays PT1M, PT5M, PT15M, PT1H, PT4H, PT12H, PT24H then DEAD_LETTER. Verify policy values come from the centralized component and manual retry does not reset count/error evidence.

## TC-INT-004

Verify retry eligibility, both permissions, reason, If-Match, idempotency replay/conflict, one-winner claim and same-transaction audit. Verify stale claims at 15 minutes recover through the explicit command.

## TC-UI-003

Verify `/audit` and `/integration/operations`, their distinct permissions and API-backed states. Assert `/platform/operations`, PLAT-001 and unsupported metrics do not exist. Component tests use auth doubles; MES-002 owns login/navigation E2E regression.

## TC-NFR-001

Production validation fails when any required enterprise configuration key is absent or malformed. Non-production may omit them. The test does not assert or invent RPO/RTO/retention values.


## V1.0.15 Cross-Review Coverage Closures

- `TC-MD-002`: organization hierarchy closure and historical-reference behavior for MD-001.
- `TC-PROC-004`: process-parameter definition and published-version immutability for PROC-PARAM-001.
- `TC-EBR-011`: dynamic field types and stable identifiers for EBR-002.
- `TC-BAT-005`: production-order creation and frozen batch references for PRD-001.
- `TC-EQP-002`: append-only equipment usage/run sequence and qualification gates for MES-EQP-001.
- `TC-BAL-004`: quantity-event/reversal-driven material balance for BAL-001.
- `TC-QMS-004`: IPC result revision and deviation gate for QMS-IPC-001.

## DCP-MES-002-R2-001 Detailed Acceptance Set

- `TC-MAT-005` and `TC-WMS-008`: prove the versioned policy and receipt-time snapshot remain historically stable.
- `TC-WMS-006..007`: prove the exempt path creates a system-rule release only when every qualification and receipt check is valid, with no fabricated QMS intermediates.
- `TC-QMS-IN-001`: prove the formal WMS→QMS request handoff and request state transition.
- `TC-QMS-SMP-001..003`: prove container-level lineage, completeness gates, and separate test/retention samples.
- `TC-QMS-TST-001..003`: prove original execution facts, append-only revisions, and required-item review gates.
- `TC-QMS-RPT-001..002`: prove report items reference approved revisions and approved reports cannot be edited in place.
- `TC-QMS-MREL-001..004`: prove QC-pass blocking, signed QA release/reject, atomic inventory effect, and superseding decisions.
- `TC-ELG-001..004`: prove Reservation, Issue, Weighing, and Charge all use the same final-boundary eligibility rule under normal and concurrent freeze/supersession conditions.

For every case, assert database facts, API result, allowed actions, audit evidence, signature binding when applicable, optimistic version behavior, and transaction rollback. The inspection-exempt tests must explicitly assert zero rows for InspectionRequest, SamplingTask/Detail, Sample, InspectionTask/Item, TestExecution/ResultRevision, and InspectionReport.



## DCP-MES-003-R2-001 approved delta

MES-003 required tests and additional contract checks follow DCP-MES-003-R2-001. TC-EQP-001/TC-QUAL-001 validate the producer gates directly; full operation/weighing orchestration remains assigned to downstream tasks.


## DCP-MES-004-005-R2-001 approved contract completion

Material creation 201, complete typed DTOs, explicit version target/root optimistic token, Material root DRAFT/APPROVED/INACTIVE and version DRAFT/SUBMITTED/APPROVED, Supplier UNAPPROVED/APPROVED/INACTIVE and named commands, historical relationship revocation, and LG-004 delayed conversion-material FK follow 00_DESIGN_CHANGE_PROPOSAL_DCP-MES-004-005-R2-001_APPROVED.md. No existing table/column/path/permission/task dependency is added or removed. All previous unrelated contracts remain applicable.


## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.

## DCP-UI-LIST-EDIT-001 current presentation contract

white page header/cards on #f0f2f5 background, #2c5cdc primary buttons and 3px section title bar, 4px corner radius, compact 13px tables, 18px page titles, three-column desktop query controls, and two-column edit forms with 130px right-aligned labels. Query label/control remain inline on all viewport sizes; mobile edit forms have one column with label/control remaining side by side. Existing navigation chrome retained. Existing technical contract-strip metadata removed from master list presentation; operation controls and contract behavior retained.


Validation: affected module typecheck/build plus existing desktop/mobile query, edit/save, permission and conflict-preservation flows. No backend/data regression required for this CSS/template-only change.

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


## Approved incoming full-chain completion in v1.0.15

Read `00_DESIGN_CHANGE_DCP-INCOMING-QUALITY-GAPS-001_APPROVED.md` and the four `00_INCOMING_*_CONTRACT_V1.0.15.md` appendices. Their explicit columns, state guards, API schemas, permissions, signing envelopes, UI fields, tests and dependency ownership supersede contradictory inherited text within the approved scope only. Former DG-01..08 and BC-01/02 now have implementation contracts; delivery is still subject to actual code and required validation. No full workflow PASS follows from publication.


## Authorized v1.0.15 incoming completion supplement

Human approval covers the bounded contract completion and full incoming acceptance tasks. For eBR runtime, issue returns, weighing verification and actual trace identities, the following precise supplements supersede generic or conflicting clauses in this chapter; unaffected contracts remain unchanged.

- [00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.15.md](00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.15.md)
- [00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.15.md](00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.15.md)
- [00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.15.md](00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.15.md)


## Authorized v1.0.15 material weighing policy producer

The approved full incoming acceptance scope includes this missing producer. [00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.15.md](00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.15.md) governs deployment configuration, immutable production snapshot, consumers and required tests. It does not restore retired material master fields or change material APIs. It supersedes earlier references to nullable legacy material weighing fields. No new table, permission, route or status.


## v1.0.15 approved trace-read completion

See [00_INCOMING_TRACE_STANDARD_V1.0.15.md](00_INCOMING_TRACE_STANDARD_V1.0.15.md) for exact frozen QC standard node and immutable signature-policy identity evidence. Unaffected contracts remain unchanged.


## Approved functional closure delta — v1.0.15

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.


## Verified functional closure recovery — 2026-10-04

The approved bounded delta and one-time V024 exception are recorded in [functional closure §§9–10](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md). V024 is successful; V001–V023 remain immutable. Trace inventory decisions use INVENTORY_DECISION / INVENTORY_CONTROL with original SIGNED_EVIDENCE, decimal signing values are exact strings, and mandatory missing headers return 400. Scope-level native verification PASS does not waive other formal task RTM or authorize full MES-012/013.
