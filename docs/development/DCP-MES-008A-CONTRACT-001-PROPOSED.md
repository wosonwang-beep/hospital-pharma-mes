# DCP-MES-008A-CONTRACT-001 — PROPOSED

批准记录（2026-10-03）：用户明确“批准该补齐方案，并将 MES-009/010 纳入本轮开发，以完成所要求的集成”。下文保留原提案及提出时的背景；A/B 已获批准，009/010 开发范围亦已授权。尚未发布累积新基线，v1.0.9 仍是权威，本文件不另立设计事实来源。

用户本次请求：完成 MES-007、MES-008、MES-008A。MES-006 已验收，MES-007/008 当前阶段已交付；本次明确授权启动 MES-008A，但没有明确改写冻结路由、外键时序或提前开发其他任务的授权。

## 1. 请批准的当前最小补齐范围

### A. 补足已有业务的独立创建与执行页面

现有 UI 详细规范第4–9行要求 Create 独立路由、View只读、禁止列表弹窗完成新增。当前 route matrix 第63–74行只有来料记录的查询/详情与QA工作页，现有业务 POST 已有，但正式创建/取样录入/检验录入页面未闭合。

新增下列前端路由，沿用既有业务/API/权限，不添加新业务实体：

| 页面 | 路由 | 既有权限 |
|---|---|---|
| 请验新增 | /quality/inspection-requests/create | qms:inspection-request:create |
| 取样任务新增 | /quality/sampling-tasks/create | qms:sampling:create |
| 取样执行 | /quality/sampling-tasks/:id/execute | qms:sampling:execute；完成另校验 qms:sampling:complete |
| 检验任务新增 | /quality/inspection-tasks/create | qms:test:execute |
| 检验执行与结果修订 | /quality/inspection-tasks/:id/execute | qms:test:execute；修订另校验 qms:test:correct |
| 检验报告生成 | /quality/inspection-reports/create | qms:report:create |

质量工作台 /quality/workbench 已由详细UI规范第375行定义，补入映射清单，不视为新增业务路由。保留现有查询/只读详情/QA审核放行页面。所有文字标签和控件在桌面与手机保持同行，沿用既有原型和样式；状态动作与电子签名沿用既有受控规则。

### B. 补足 QMS → 正式批外键的安装时序

05数据库第227行要求 qms_sample.main_batch_id FK；第246、800行要求 qms_release_decision.main_batch_id FK（incoming delta 已将其改成 nullable）。LG-008A 在 LG-009A 之前，当前正式批表尚不存在；此前批准只延后 WMS/eBR 的指定外键，没有覆盖 QMS。

- MES-008A 新迁移保留上述 nullable 列；来料 scope 必须 main_batch_id=NULL，不能写入不存在的批次。
- MES-009 建立真实正式批后，用新的物理迁移校验引用并补齐这两条 FK；不永久省略、不创建临时批表、不修改 V001–V013。
- 保留 ReleaseDecision 单一事实来源与 scope/target 约束。原 finished_lot_id 的兼容引用拟明确为 md_material_lot.id，仅用于 FINISHED_PRODUCT scope；INCOMING_MATERIAL scope 的 finished_lot_id 必须为空。与 main_batch_id 同步保留唯一适用目标约束，不新增第二套 FinishedLot 表。本项作为显式待批准的目标裁决，不声称原文已指明该目标。
- 物理版本以执行前真实 Flyway history 为准；本次启动尚未执行迁移。

## 2. 已有授权内可直接实现的事项

请验、取样、样品、检验任务/项目、执行、结果追加修订、报告、人工/免验放行和 MaterialEligibilityService 的业务链已由基线设计。具体DTO/物理字段从已有逻辑模型、功能规范和API字段导出；GenericRequest不是单独阻塞原因。不恢复物料版本审批或停用名称。

电子签名复用现有平台与可扩展 provider；SYSTEM_RULE免验无伪造人工签名；更正保留原结果/签名证据；免验重新校验物料、合格供应商/关系和收货检查。无需等MES-013建立另一套签名系统。

## 3. MES-007/008完整完成的现存依赖

当前批准的 sequencing DCP 明确：LG-007B 归 MES-009，工序引用归 MES-010；WMS预留/发料须真实 MainBatch + 008A eligibility。完成008A只能解除质量资格一侧，不能代替正式批或工序。

本方案 A/B 不隐含授权 MES-009/010 业务开发。若要求本轮将007/008做到完整运行集成，需要明确把真实MES-009/010生产者任务纳入开发范围并沿原设计实施。008A测试清单还引用后续称量/投料消费场景，实际生产消费验证应在其对应任务集成门记录，不用mock声称整套验收通过。任何最终状态都以原必测项实际证据为准。

## 4. 批准后实施与一致性要求

发布累积新基线并保留v1.0.9原件；同步UI/route/prototype映射、DB、API、状态/权限、签名、测试/RTM、迁移/依赖/集成及任务卡，跨文档一致性检查通过后再切指针。复用现有实现，不重建已验收MES006，不重写执行过的迁移。只做当前变更的编译、定向单元/数据库/界面验证和一次范围审查，不跑反复全量回归。

本次仅完成契约核对与此提案，未开始新的业务代码、迁移或数据库测试。
