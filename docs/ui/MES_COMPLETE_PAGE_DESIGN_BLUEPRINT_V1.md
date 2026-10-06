# MES 全系统页面 UI 设计蓝图 V1

**状态：APPROVED DESIGN BLUEPRINT**  
**日期：2026-10-05**  
业务权威：FINAL BASELINE COMPLETE v1.0.18 + approved DCPs
视觉权威：`MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md`  
模板权威：`MES_PAGE_TEMPLATE_STANDARD_V2.md`

本文按 GitHub main 当前真实路由/功能设计页面，不授权新增字段、API、状态、权限、Migration 或流程。业务冲突时 FINAL BASELINE 优先。

## 1. 全局规则

信息优先级：**业务身份 → 状态 → 当前任务 → 业务事实/结果 → allowedActions → 次级证据**。

业务主页面默认不展示内部数据库 ID（已有业务编号/名称时）、versionNo/recordVersion/revisionNo、Hash/Digest、JSON key、技术签名 ID、通用 created/updated 元数据、Audit payload。受控动作始终为 server allowedActions ∩ permission。

T1 查询列表；T2 新增编辑；T3 业务单据详情；T4 360°聚合；T5 执行工作台；T6 决策工作台。Dashboard/Login/Designer/Trace/Audit 为特殊页面但继承 Global UI V2。

## 2. 登录 / 工作台 / IAM

- **登录**：品牌、环境、账号密码、单一登录动作。
- **修改密码 T2-lite**：当前/新/确认密码 + 密码规则。
- **工作台 Dashboard**：现有数据支持的 KPI、我的待办、生产/质量异常、近期受控活动；禁止造 KPI。
- **用户/角色列表 T1**：紧凑查询 + 表格。
- **用户/角色新增编辑 T2**：身份、状态、角色/权限；权限按业务域分组。
- **用户/角色详情 T3-lite**：身份/状态/角色/权限摘要。

## 3. 主数据

物料、供应商、组织、单位、单位换算、设备、人员资格统一：列表 T1；新增编辑 T2；详情 T3-lite。

- **物料详情**：Header（名称/编码/类型/状态）；Tabs 基本信息/质量控制/供应商/使用范围。基本信息含规格、单位、生产厂家、批号管理、生效失效、备注、是否入库必验。主页面不显示版本号。
- **单位换算**：From/To Unit 和物料使用现有 lookup，不优先 raw ID。
- **设备**：编码、名称、类型、位置、校准到期、状态；详情显示维护动作，不额外推断可生产。
- **人员资格**：人员、资格、有效期、状态；使用人员选择器。

## 4. 产品 / 工艺

- **产品**：T1/T2/T3-lite；详情为产品身份、剂型/规格、现有工艺关联。
- **工艺包**：列表 T1；编辑 T2；详情为 T4-like definition aggregate：工艺包头、状态、工序顺序、前置关系、设备类型、清场要求、已有 eBR/参数定义。定义页禁止做成执行页。

## 5. WMS

### 原辅料收货
T1：单号/供应商/仓库/状态/日期/物料摘要。  
T2：单据头 → 供应商仓库 → 收货明细 → 当前 Contract 的检查/附件。  
T3：单号状态、供应商仓库日期、物料/供应商批号/数量/单位、收货检查、次级附件、allowedActions。

### 发料
T1：发料单号/生产批/状态/日期。  
T2：生产上下文 → 发料明细。  
T3：单据身份/状态、生产批、明细、数量摘要、allowedActions。

### MaterialLot 360° — T4
采用用户确认的**第一版 T4**：顶部物料/批号/状态摘要 → 生命周期 **收货→请验→取样→检验→检验报告→物料放行** → Tabs（全流程/收货/质量/库存流转/使用记录/相关生产/相关文件）→ 六张紧凑来源单据卡。卡片只放业务单号、已有时间、状态、查看详情。禁止 version/hash/audit/revision；read model 没有的数据不伪造。

## 6. 来料质量

### 请验单
T1；Create T2；View T3：单号/状态、物料批、QC标准、数量/单位/件数/日期/优先级、提交接收事实、allowedActions。

### 取样记录
T1；Create T2；View T3：单号/状态、请验/物料批、取样人/时间、容器/位置/数量/密封明细、生成样品摘要。Execute突出当前取样操作。

### 样品
T1；View T3-lite：样品号/状态、来源请验/取样/物料批、类型、数量单位、取样/接收时间、储存位置、当前动作。

### 检验记录
T1；Create T2；Execute 为 execution-oriented T3。View 固定为：
1. Header：检验记录号 + 状态；
2. 摘要：请验单、样品、物料/批号、QC标准、检验人、检验时间；
3. **主体表格：检验项目 / 标准要求 / 实际结果 / 单位 / 判定**；
4. 结论与复核：当前结论、关联OOS/偏差、复核状态/人/时间；
5. 当前 allowedActions。
方法/仪器/原始观察按项目需要时查看，不作为主页面。默认禁止版本号、内部ID、修订历史、Audit、Hash、技术签名信息。

### 检验报告
T1；Create为受控生成；View T3：
- Header：报告编号 + 结论/状态；
- 基本信息：请验单、样品/物料批上下文、QC标准、检验日期、**检验人**；
- 表格：项目/标准要求/实际结果/单位/判定；
- 检验结论；
- 审批信息：**检验人、复核人（Contract有时）、审批人、审批日期/状态**；
- 当前 allowedAction。
**不要独立“检验方法和仪器”区块**；不显示版本号、内部ID、修订历史、Audit payload。

### 来料调查/OOS
T1/T2/T3：调查号/状态/严重度、受影响物料/检验/结果、描述、调查结论、决定/处置、最终选定结果、关闭事实、allowedActions。原始FAIL保持可追溯但技术机制不铺主页面。

### 物料放行
T6：物料批/质量库存状态 → Release条件清单 → 检验报告/调查摘要 → Decision Summary → Release/Reject。

## 7. QC质量标准

标准列表 T1；创建/详情 T2/T3。标准版本编辑为 T2 definition editor，主体是检验项目定义：代码/名称/必检/结果类型/上下限或文本标准/单位/当前Contract支持的方法引用。Approved/Frozen只读。

## 8. 生产管理

- **生产订单**：T1/T2/T3；详情显示订单号状态、产品、计划数量单位日期、关联批次、allowedActions。
- **生产批**：T1 使用 v1.0.17 当前完整查询能力；T2新增编辑；T4详情显示批号状态产品计划、执行单元、工序进度、物料准备、质量状态、物料平衡、QA放行以及 T5/T6入口。
- **物料平衡**：T3 analytical：批次产品 → 投入/产出/损耗/差异 → 当前公式结果 → 阈值状态 → 关联偏差 → 受控动作。

## 9. 生产执行 — T5

采用已批准 T5参考：紧凑 Batch Summary + Current Operation/Overall Progress；横向工艺进度；主体三栏为窄工艺步骤 / 最宽中央执行区 / 窄Quick Actions；Tabs固定 **执行记录/物料信息/设备信息/工艺参数/IPC/异常记录**；当前工序为视觉主体；物料清单和关键参数 Desktop首屏尽量可见；称量/投料复用同一WorkBench。无真实照片Contract时不放静态示意图；不支持的动作直接隐藏。

## 10. eBR

- 模板列表 T1。
- 模板创建/查看 T2/T3。
- Designer：左组件Palette / 中Canvas / 右属性规则；顶部模板身份 + 当前支持的Preview/Save/Publish。
- Runtime：执行上下文 + 动态表单主体 + Validation + 明确签名动作。修订机制后台保留，不铺在操作员主表单。

## 11. 生产质量

- 生产质量计划：T1/T2/T3。
- 生产检验：T1/T2/T3，沿用“检验记录精简原则”。
- IPC：主要作为 T5 Tab，显示定义/限值、当前结果、结论、当前动作。
- 生产偏差：T3；没有现有创建Contract时不得造“记录偏差”。

## 12. QA成品批审核/放行 — T6

保留已验收 Decision Workbench：批次Header/状态 → Release Gate进度 → 6个权威Gate → Decision Summary → Allowed Actions。**最终eBR PDF是QA决定后的归档步骤，不是第7个放行前Gate。** PDF历史/证据目录次级，禁止内部Evidence key。

## 13. 完整追溯

特殊 Trace Explorer：搜索/过滤 → 中央 lineage graph/timeline → Selected-node Drawer → Relationship legend → Deep link 到 T3/T4/T5/T6。只读，不成为第二套事实源。

## 14. GMP Audit Trail

Audit是允许高技术密度的专门页面：按现有API提供对象/Actor/Action/时间过滤；事件表/时间线；Detail Drawer展示后端已有 before/after/canonical evidence。**Audit页面的技术密度不得复制到业务详情页。**

## 15. Integration Inbox/Outbox

运维 T1 Console：Inbox/Outbox Tabs；现有Contract支持的状态/方向/类型/时间过滤；消息业务Key、方向、状态、尝试次数、时间；Detail Drawer显示payload/error/retry facts；Retry/Replay仅现有权限/动作允许时显示。

## 16. 页面模板矩阵

| 功能 | 模板 |
|---|---|
| 工作台 | Dashboard |
| 用户/角色列表 | T1 |
| 用户/角色编辑 | T2 |
| 主数据列表 | T1 |
| 主数据编辑 | T2 |
| 主数据详情 | T3-lite |
| 产品列表/编辑/详情 | T1/T2/T3 |
| 工艺包列表/编辑/详情 | T1/T2/T4-like |
| 收货/发料列表 | T1 |
| 收货/发料编辑 | T2 |
| 收货/发料详情 | T3 |
| MaterialLot 360° | **T4** |
| 请验/取样/样品 | T1/T2/T3 |
| 检验记录 | T1/T2/T3 |
| 检验报告 | T1/T3 |
| OOS/调查 | T1/T2/T3 |
| 物料放行 | **T6** |
| QC标准 | T1/T2/T3 |
| 生产订单 | T1/T2/T3 |
| 生产批 | T1/T2/**T4** |
| 物料平衡 | T3 analytical |
| 生产执行/称量/投料 | **T5** |
| eBR模板 | T1/T2/T3 |
| eBR Designer | Specialized |
| eBR Runtime | T5-linked |
| 生产质量/检验/偏差 | T1/T2/T3 |
| QA批审核/放行 | **T6** |
| 完整追溯 | Specialized |
| Audit Trail | Specialized |
| Integration Ops | T1 Console |

## 17. Codex实施规则

每次只按一个页面族实施，禁止一次全仓UI重构。实施前声明 `UI Template: T1–T6/Specialized`。必须保留业务Contract、Router、Permission、allowedActions、签名和GxP语义。

推荐实施顺序：
1. 冻结已完成 T5。
2. 检验记录 + 检验报告 T3。
3. 其他来料质量 T3。
4. MaterialLot T4。
5. Production Batch T4。
6. 剩余 T1列表推广。
7. T2表单统一。
8. T6最终精修。
9. eBR Designer/Runtime。
10. Trace/Audit/Integration。
11. 全系统 UI Consistency Audit。

每个页面族完成后必须实际 Chromium Desktop/Mobile检查（PC-only批准页面除外），保存验收截图，再进入下一族。

## 18. Stop Conditions

出现以下情况立即停止并报告：
- 设计需要不存在的业务字段/API；
- 需要新增状态/权限/allowedAction；
- 视觉稿与 FINAL BASELINE业务语义冲突；
- T1–T6无法表达且需要新模板；
- 为“看起来完整”而必须伪造数据。

此时输出 **DESIGN CHANGE REQUIRED**，不得自行扩展业务Contract。
