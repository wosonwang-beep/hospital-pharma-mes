# Approved functional closure contract

Approval: user explicitly replied “批准方案” on 2026-10-04. DCP-MES-008-011-FUNCTIONAL-CLOSURE-001. The following approved additive contract supersedes missing/generic freeze, IPC and clearance contracts only. Previous releases are immutable.

# DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 — 功能闭环补齐提案

日期：2026-10-04。状态：**APPROVED**。本累计发布内的契约已经用户明确批准。

## 1. 目的、授权边界与推荐方案

用户要求“补齐功能”，目标是完成 MES-008、008A、010、011 的实际功能闭环，避免仅增加测试而长期保留缺失入口。

推荐在 v1.0.14 已有收货、来料检验、独立 QA 放行和生产执行基础上，补齐三个缺失契约：**库存冻结/解除冻结、生产 IPC、受控清场**。保留六类来料记录及其单一事实源，保留已验收 MES-007 和 MES-009 已有生产行为。

备选方案及取舍：只保持 fail-closed 无法完成所需正向流程；等待外部系统提供证据需要新的外部生产者契约和部署依赖。本提案选择本项目内的受控记录生产者，沿用平台审计、签名、幂等和组织隔离。

批准对象是本文规定的三个功能、明确列出的契约扩展、MES-012 的 IPC 最小生产者范围以及本文实施顺序。批准不包含整个 MES-012 的 CAPA、物料平衡、完整生产质量调查，也不包含 MES-013 成品 QA 放行。若发现新增需求超出本文，重新提出设计变更。

## 2. Baseline Review：已有依据和真实缺口

正式依据目录：`releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14/`。

| 功能 | 正式依据 | 现有实现/缺口分类 |
|---|---|---|
| 冻结 | PRD WMS-004、WMS-ELG-001；Domain InventoryAggregate / MaterialEligibilityService；DB `inventory_status=FROZEN`；Incoming Chain §7 | MATCH：最终预留/发料/称量/投料资格检查。DESIGN GAP：无冻结业务 API、决定记录、签名点、权限和 UI 动作。直接 SQL 设置 FROZEN 的旧测试不代表业务入口已实现。 |
| IPC | PRD QMS-IPC-001；Functional FD-QMS-001；DB `qms_ipc_instance`；OpenAPI GET/POST `/ipc`；TC-OP-002、TC-QMS-004 | MISSING：实例生产者与结果修订；DESIGN GAP：GenericRequest/Response 无完整字段契约，缺工艺冻结定义和必检项目集。现有 completionRule 不允许 IPC 叶子，不能自行添加。 |
| 清场 | Domain OperationExecution；State Machine 工序启动 Gate；DB `proc_operation_def.clearance_required`、`mes_equipment_usage.clearance_status`；UI-EXEC-W | MATCH：要求清场时拒绝启动。DESIGN GAP：没有真实记录生产者、复核、签名和适用范围；客户端填 PASSED 不构成证据。 |
| 任务依赖 | MES-012-R2 §4A/4D；MES-010-R2；已批准 incoming production contract | BASELINE CONFLICT：MES-010 消费 IPC，但 IPC 归 MES-012、MES-012 又依赖 MES-010。本提案仅拆出最小 IPC 生产者阶段，其他依赖保持。 |
| 来料结果表 | V016 与正式来料扩展契约 | `qms_test_result_revision` 的 sample/item/execution 非空 FK 及不可变触发器适用于来料。不得通过假 Sample、放松原 FK 或写通用结果覆盖来承载生产 IPC。 |

## 3. 库存冻结：明确拟补契约

这里冻结的是库存使用资格，与 QA 的物料质量结论分别受控。`inventory_status=FROZEN` 阻断所有生产使用；**不得用库存解除冻结替代 QA 放行**。正式质量状态机中的质量 BLOCKED/再次 QA 放行仍由原质量决定链负责；本文不增加第二套质量决定。

- 新增不可变记录 `wms_material_lot_inventory_decision`：`id` PK，`org_id`，`material_lot_id` FK，`previous_decision_id` 自引用 FK 可空，`action`（FREEZE/UNFREEZE），`previous_inventory_status`，`resulting_inventory_status`，`reason`，`decided_by` FK，`decided_at`，`signature_id` FK，`signature_evidence_json`，`created_by/created_at`。每个前驱最多一个后继；禁止业务 UPDATE/DELETE。
- 只允许 AVAILABLE→FROZEN、FROZEN→AVAILABLE。BLOCKED 不允许通过该功能变成 AVAILABLE；过期、复验到期、当前 QA 决定无效或不为 RELEASED 时不能解除冻结。冻结不更改质量状态、批号、数量或历史库存账。
- 沿用 MaterialLot 的 versionNo 乐观锁；锁批次后检查当前决定，再追加记录、更新库存状态、签名及审计，全部同事务。未使用该功能的既有批次不回填虚构历史。
- 新增 POST `/material-lots/{id}/freeze`、POST `/material-lots/{id}/unfreeze`，HTTP 200；请求严格为 `versionNo, reason, signature`，要求 If-Match 和 Idempotency-Key。`signature` 完全沿用现有平台再认证封套，不增加密码存储。
- 新增 `qa:material-inventory:freeze`、`qa:material-inventory:unfreeze`；查看沿用 `wms:inventory:view`。权限须显式授权，不隐式赋予现有角色。
- Lot 360° 现有 GET 响应补充只读 `inventoryDecisions`、服务器计算的动作集合；在现有 `/wms/material-lots/:id` 页面显示历史和两个受权按钮，签名弹窗沿用现有组件。
- 所有最终使用动作与冻结共享现有批次行锁；先冻结则使用拒绝，先使用则保留已发生事实并阻断其后的新使用，不能追溯撤销已提交投料。

## 4. IPC：冻结定义、实例、不可覆盖结果及工序 Gate

### 4.1 定义和数据库

- `proc_operation_def` 新增 `ipc_definitions_json`，默认空数组。每项：`ipcCode, name, required, resultType(NUMERIC/TEXT), lowerLimit, upperLimit, expectedText, unitId, methodReference`。NUMERIC 采用适用上下界；TEXT 采用明确期望值；类型互斥、单位存在、编码唯一、数值精度按现有 QC 数值规范 lint。方法引用保留当时版本引用。
- 定义仅在可编辑工艺版本维护；发布 lint；MainBatch 的既有不可变 process snapshot 冻结完整数组。既有快照缺少该字段解释为空，不补写已执行快照。completionRule 原有闭合语法不变；IPC 必检检查是独立、不可被 ANY 绕过的完成 Gate。
- 创建正式已有 `qms_ipc_instance`：平台 mutable profile（PK id、org_id、创建/修改人时间、version_no）；`operation_execution_id` FK、`ipc_code`、`status`（PENDING/TESTING/COMPLETED）、`result`（PASS/FAIL/INVALID，可空）、`completed_at`、`definition_snapshot_json`、`current_result_revision_id` FK 可空。唯一 `(org_id,operation_execution_id,ipc_code)`。result/current 指针是原始结果聚合投影，不作为可单独编辑的事实源。
- 新增不可变 `qms_ipc_result_revision`：平台 immutable profile；`ipc_instance_id` FK、`revision_no`、`previous_revision_id` 自引用 FK、`result_numeric` 或 `result_text`、`result_conclusion`、`definition_snapshot_json`、`reason_for_change`、`recorded_by/recorded_at`、`signature_id/signature_evidence_json`。唯一实例/修订号及前驱后继；无业务物理删除。结论由冻结标准服务器计算，用户不能指定 PASS。
- 新增不可变 `qms_ipc_review`：平台 immutable profile；`ipc_result_revision_id` FK、`disposition`（CONFIRMED/INVALIDATED）、`reason`、`reviewed_by/reviewed_at`、`signature_id/signature_evidence_json`；每个修订最多一次复核。复核人不能是该结果记录人。
- INVALIDATED 是带原因的结果有效性决定，不能删除或将原 FAIL 改为 PASS。原 FAIL 仍有效时始终阻断；批准失效后才能追加下一次实测修订。CONFIRMED FAIL 仍阻断。本轮不提供 CAPA、完整调查或关闭原有阻断性偏差的捷径。

### 4.2 动作、接口和权限

沿用 GET `/ipc`（200）、POST `/ipc`（保留既有 200），将 Generic schema 替换为完整 DTO。GET 支持分页及 operationExecutionId/ipcCode/status；POST 只接收 operationExecutionId/ipcCode/reason，必须与该工序冻结定义一致，同码已有实例返回受控重复响应，不得手工追加未冻结项目。

新增 GET `/ipc/{id}`，POST `/ipc/{id}/submit-result`，POST `/ipc/{id}/review-result`，均 200。

- submit-result：`versionNo, previousRevisionId, resultNumeric/resultText, reason, signature`；首次前驱空，后续精确引用已 INVALIDATED 的前驱；If-Match/幂等键必需。
- review-result：`versionNo, resultRevisionId, disposition, reason, signature`；必须复核当前精确修订，使用 If-Match/幂等键。
- 查看沿用 `qms:ipc:view`，创建/记录沿用 `qms:ipc:record`；新增 `qms:ipc:review`。API 必须组织隔离、严格字段校验、独立角色和服务器动作集合。

### 4.3 生产者和消费者

Operation 初始化时同步、同事务初始化冻结 IPC 实例；任一失败回滚批次下达、Operation、IPC、审计及幂等事实。通过 Execution API 的初始化事件和质量 Gate port 连接 QMS，消费者不依赖 QMS 的实现类或表。

工序 COMPLETE 锁定生产根/工序并查询同事务当前 IPC：每个 required 定义都有唯一实例、当前有效 PASS 修订及独立 CONFIRMED 复核；PENDING、缺失、未复核、FAIL、INVALID、未处理阻断偏差均拒绝。工序完成后拒绝新结果/复核变更，保证已完成工序依据不被静默替换。

模块依赖保持单向：QMS 可依赖 Execution 公开 port/event/query 契约，Execution 不依赖 QMS；启动层装配实现。拒绝以组件扫描顺序、异步最终一致性或缺少 Bean 时放行替代初始化/Gate。

UI：现有工艺操作编辑页增加 IPC 定义区；既有 UI-EXEC-W 增加 IPC 实例、冻结标准、原始结果/修订/复核区。记录和复核在工作台受权动作完成，不新增独立菜单/路由，不增加第二套结果录入。

## 5. 清场：执行记录与独立复核

- 新增不可变 `mes_clearance_record`：平台 immutable profile；`operation_execution_id` FK，`equipment_usage_id` FK 可空，`previous_record_id` FK 可空，`outcome`（PASS/FAIL），`reason`，`performed_by/performed_at`，`signature_id/signature_evidence_json`。无设备工序记录工序范围；有设备工序须对每个实际绑定 usage 各有记录，不能复用其他工序/设备的清场。
- 新增不可变 `mes_clearance_review`：平台 immutable profile；`clearance_record_id` FK 唯一、`decision`（APPROVED/REJECTED）、`reason`、`reviewed_by/reviewed_at`、`signature_id/signature_evidence_json`。记录人和复核人分离。
- 不设置清场有效期等未定义参数。证据只适用于精确 Operation + 当前 equipment binding；换绑设备、新增 binding、追加 FAIL/REJECTED 后旧证据不能作为当前通过证据。失败重做追加后继记录，保留全部失败和复核历史。
- 新增 POST `/operations/{id}/record-clearance`：`versionNo,equipmentUsageId,previousRecordId,outcome,reason,signature`；POST `/operations/{id}/review-clearance`：`versionNo,clearanceRecordId,decision,reason,signature`；均 200、If-Match/Idempotency-Key 必需。新增 `mes:clearance:record`、`mes:clearance:review`；查看沿用 `mes:operation:view`。
- START/RESUME 在同一根/工序锁内验证清场；clearanceRequired=true 时所有适用范围均须最新 PASS + APPROVED，完整签名与组织关联一致。PENDING/READY/PAUSED 可记录和复核；运行/完成后拒绝改写启动依据。`mes_equipment_usage.clearance_status` 仅服务端投影，不接受绑定接口的客户端状态。
- 现有 UI-EXEC-W 增加清场记录和复核动作、原始历史、明确阻断原因；不另建清场菜单、重新设计页面或让操作员用确认框跳过 Gate。

## 6. 通用契约、UI 和 GxP

所有新受控动作复用现有签名再认证、AuditEvent、幂等和事务机制。审计记录操作者/时间、前后值、原因和版本；签名绑定确切不可变记录/内容 hash。签名、审计或幂等失败全事务回滚。拒绝通用 PUT status、业务删除、原始结果覆盖和通过旧版本签名授权新事实。

接口延续 403 权限、409 乐观锁/状态/幂等冲突、422 业务 Gate；采用完整请求响应 schema，绝不使用无限制 GenericRequest 填业务字段。跨模块锁顺序继承现有生产根→工序/相关事实→MaterialLot；不得逆序形成新死锁。

全部 UI 使用 v1.0.14 DESIGN_SYSTEM、列表/编辑批准样式、UI-EXEC-W 和 Lot 360° 布局：查询条件及表单**标签和输入框同行**；字重、尺寸、间距、按钮色和签名弹窗沿用现有组件。只增加上述已枚举的区块、字段和按钮，不再设计新视觉风格。

## 7. 任务、文档与实施顺序

批准后建立新累计正式版本（候选 v1.0.21），v1.0.14 及之前发布保持不可变。逐项同步 PRD、Domain、DB/数据字典、State Machine、API/OpenAPI/Catalog、Functional、UI详细/路由矩阵/原型、Permission、GMP/Audit、Test、RTM、Migration、Integration/Task Dependency、Task Cards。完成跨文档一致性检查后才切换 PROJECT_BASELINE 指针。

MES-008 拥有库存冻结记录和动作；MES-008A 消费库存/质量分离后的资格契约，来料六类记录不改。MES-010 拥有清场与 IPC Gate 消费者。MES-012 仅拆出本提案的 IPC 定义消费/实例/结果/复核生产者阶段，基于已物理存在 Operation/eBR/Charge 契约开发，不以整个 MES-010 已验收为前提。MES-011 消费最终库存 Gate。MES-012 其他范围及依赖、MES-013 均保持原限制。

执行顺序：Database/Flyway → Domain → State Machine → Repository/Mapper → Application Service → API → Audit/Signature → UI → Unit → targeted Integration → E2E → RTM/验收记录。每项必须形成实际可调用业务入口；不得以 SQL 测试 fixture 或 mock 生产者替代。

迁移新增且仅追加，物理版本从实施时原生 MariaDB 最高成功版本续号，当前观测最高 V023；不修改 V001–V023，不 flyway repair、不重建 DEV、不回填虚构历史。新增记录禁止业务物理删除；本地测试遵循回滚/既有批准的独立保留 fixture 规则。

## 8. 最小必要验证和验收边界

1. 真实 signed freeze/unfreeze 正向链；未 QA 放行、日期阻断不能解冻；冻结与 Reserve/Issue/Weigh/Charge 两种提交顺序验证无绕过，失败无额外账/业务事实。
2. 工艺发布/下达冻结 IPC，V2 不改变 V1 批次；实际 required PENDING 实例阻断 COMPLETE；完成全部有效 PASS+独立签名复核后允许 COMPLETE。FAIL 原始修订始终保留，未批准失效/未复核/旧版本/跨组织阻断；结果与 COMPLETE 并发无静默覆盖。
3. clearanceRequired 工序无证据阻断；实际记录 PASS、独立 APPROVED 后允许 START/RESUME；FAIL、REJECTED、换绑、错范围、自审、跨组织及旧版本拒绝；记录与 START 并发只接受同一受控事实。
4. 新动作精确签名/审计/幂等回滚、相同请求重放/不同请求冲突；编译涉及模块、前端 typecheck、变更页面交互和同行布局检查。只运行上述及直接受影响回归，不重复全量测试。
5. 从生产投料反查 Lot 的冻结决定、QA/来料链；从 Operation 反查清场、IPC 冻结定义、原始/后继结果、复核和签名。

以上完成仅表示这三个缺口闭合。各 MES 任务仍须逐条核对剩余 RTM 和直接回归，达到要求后由 Codex 标记 READY FOR ACCEPTANCE；只有用户明确验收才标记 ACCEPTED，不因批准提案而提前宣称全部任务完成。

## 9. 批准记录和发布状态

用户于 2026-10-04 明确回复“批准方案”，批准本文件 §§1–8 的完整边界。此审批仅授权开发，任务验收另行记录。

本发布累计继承不可变 v1.0.14。用户另行明确批准 DCP-MIGRATION-V024-RECOVERY-001 的一次性例外：仅修正失败 V024、保留原失败证据、执行一次 repair 并正常迁移。V024 已成功，V001–V023 全部成功历史字段和脚本校验值不变。此例外不扩大为今后改写已执行迁移的授权；本发布经跨文档一致性复核后启用，发布不代表整项 MES 验收。

嵌入式 IPC 明细同样要求 `qms:ipc:view`；无该权限时工作台返回空 IPC 明细，服务端 Gate 仍消费全部事实。所有 API 的 PK/FK/签名/人员 ID 为字符串，记录版本及修订号为整数。


## 10. 验收后的精确契约说明

签名中的 BigDecimal 使用精确十进制字符串，禁止二进制浮点；历史签名封套不重新规范化或覆盖。动作 HTTP 的路径/分页参数显式命名，缺少必需头返回 INVALID_REQUEST/400；权限逐条对应正式动作，无角色隐式授权。

生产投料→MaterialLot 的追溯查询从库存决定事实表读取原事实，形成 INVENTORY_DECISION 节点、MATERIAL_LOT→INVENTORY_DECISION 的 INVENTORY_CONTROL 关系，以及决定→SIGNATURE 的既有 SIGNED_EVIDENCE 关系。PK/签名 ID 均为字符串。库存状态独立于 QA 质量状态，图中不维护另一套库存决定。

本轮完成 11 项功能/GxP/HTTP 原生用例、10 项真实竞争实例及 5 项直接回归；3 项 IPC 单元与前端 typecheck 通过。生产冻结 Gate 覆盖 Reserve/ConfirmIssue/Weigh/Charge 两种提交顺序。MariaDB 1020 按既有受控 409 返回，新事务重新读取后必须严格返回实际业务拒绝；拒绝请求无成功幂等/额外账事实。V2 IPC 定义不改变既有 V1 批次。

再认证 port 在原生夹具中使用 mock，签名、审计、SQL、业务动作与锁均真实；不能称为完整登录或真实令牌到期验收。并发试验沿用已批准的独立保留夹具，失败历史不删除；试验物料最终 REJECTED 且库存 BLOCKED/FROZEN，均禁止生产。本轮闭合三个授权缺口，其他正式 RTM 未闭合行不因此豁免。
