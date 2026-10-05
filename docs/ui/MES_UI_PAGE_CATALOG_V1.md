# MES UI PAGE CATALOG V1

状态：APPROVED IMPLEMENTATION CATALOG｜业务权威：FINAL BASELINE v1.0.17｜上位规范：Global UI V2 + T1–T6 + Complete Page Blueprint V1。

本目录是 Codex 逐页施工契约，只覆盖 main 已有功能；不授权新增 API/DTO/DB/状态/权限。业务页默认隐藏内部ID（已有业务号时）、versionNo/recordVersion/revisionNo、Hash/Digest、技术签名ID和Audit payload。所有写动作=server allowedActions∩permission。PC遵循Global UI V2；Mobile单列，宽表仅Card内横滚。

## A Auth / Dashboard / IAM
|ID|页面|模板|页面结构与字段|
|---|---|---|---|
|A01|登录 /login|Special|品牌/环境→账号/密码→登录Primary；错误就地Alert。PC 400–440卡，Mobile 24px边距|
|A02|修改密码|T2-lite|密码要求→当前/新/确认密码→保存|
|A03|工作台 /|Dashboard|欢迎→真实KPI→我的待办→生产/质量异常→近期活动；禁止造KPI|
|A04|用户列表|T1|查询→登录名/姓名、状态、角色摘要、操作→分页；Header新增|
|A05|用户新增编辑|T2|账户身份→基本信息→角色/状态→Save/Cancel；PC两列|
|A06|用户详情|T3-lite|姓名/登录名+状态→账户→角色→权限摘要→编辑|
|A07|角色列表|T1|查询→代码/名称/状态/权限摘要/操作|
|A08|角色编辑详情|T2/T3|身份→按业务域分组权限→保存；详情身份/权限/成员摘要|

## B Master
|ID|页面|模板|页面结构与字段|
|---|---|---|---|
|B01|物料列表|T1|Header+新增；查询关键字/类型/状态；列编码、名称、类型、规格、基本单位、状态、操作|
|B02|物料新增编辑|T2|基本信息→单位/规格→管理属性→入库必验→备注→操作；PC两列|
|B03|物料详情|T3|Header名称/编码/类型/状态；Tabs基本信息/质量控制/供应商/使用范围；禁止业务版本号|
|B04|供应商|T1/T2/T3|列表编码/名称/资格有效期/资格状态；编辑身份/有效期；详情资格/物料关系|
|B05|组织|T1/T2/T3|编码/名称/类型/上级/状态；编辑身份/类型/上级|
|B06|单位|T1/T2/T3|编码/名称/量纲/精度/操作|
|B07|单位换算|T1/T2/T3|查询原/目标单位；列单位名称/因子/专属物料；编辑用Unit/Material lookup|
|B08|设备|T1/T2/T3|编码/名称/类型/位置/校准到期/状态；详情身份→校准/位置→状态→维护动作|
|B09|人员资格|T1/T2/T3|人员/资格编码/有效期/状态；编辑用人员Picker|

## C Product / Process
|ID|页面|模板|页面结构|
|---|---|---|---|
|C01|产品列表|T1|查询→业务编号/名称、剂型/规格(存在时)、状态、操作|
|C02|产品新增编辑|T2|产品身份→规格/剂型→Contract属性→操作|
|C03|产品详情|T3-lite|Header身份/状态→产品属性→已有工艺包入口|
|C04|工艺包列表|T1|编号/名称、产品、状态、操作|
|C05|工艺包编辑|T2|身份→产品/版本上下文→工序定义→保存|
|C06|工艺包详情|T4-like|Header→路线概览→工序顺序→前置关系→设备类型/清场→已有eBR/参数定义；禁止运行时按钮|

## D WMS
|ID|页面|模板|页面结构|
|---|---|---|---|
|D01|收货列表|T1|查询单号/状态/供应商/仓库；列单号、供应商、仓库、日期、物料/供应商批号摘要、数量、状态、操作|
|D02|收货编辑|T2|单据头→供应商/仓库→收货明细→已有检查/附件→保存/确认|
|D03|收货详情|T3|单号/状态→供应商仓库日期→明细表→收货检查→次级附件→actions|
|D04|发料列表|T1|发料单号、生产批、日期、状态、操作|
|D05|发料编辑|T2|生产批→物料/批号/数量/单位明细→原因→操作|
|D06|发料详情|T3|单号/状态→生产批→明细→数量摘要→actions|
|D07|MaterialLot 360°|T4|严格已确认第一版：Header物料/批号/质量库存状态→生命周期收货→请验→取样→检验→检验报告→物料放行→Tabs全流程/收货/质量/库存/使用/生产/文件→六张来源单据卡；禁止version/hash/audit/revision|

## E Incoming Quality
|ID|页面|模板|页面结构/表格列|
|---|---|---|---|
|E01|请验单列表|T1|查询关键字/状态/物料批；列请验号、物料批、类型、QC标准、状态、日期、操作|
|E02|请验单新增|T2|物料批→QC标准→类型→数量/单位/件数→日期/优先级→原因→提交|
|E03|请验单详情|T3|单号/状态→物料批/QC标准/类型→数量件数日期优先级原因→提交/接收事实→actions|
|E04|取样列表|T1|取样号、请验单、指派人、状态、完成时间、操作|
|E05|取样新增|T2|请验单→方案→要求件数→指派→操作|
|E06|取样详情/执行|T3|单号/状态→请验/物料批/取样人→方案→明细(容器/位置/数量/时间/密封)→样品分配；Execute当前输入优先|
|E07|样品列表|T1|样品号、物料批、类型、数量/单位、状态、操作|
|E08|样品详情|T3-lite|样品号/状态→来源请验/取样/物料批→类型数量单位→取样/接收时间→储存位置→Receive/Retain/Dispose/Label|
|E09|检验记录列表|T1|记录号、请验单、样品、指派人、状态、操作|
|E10|检验记录新增|T2|当前Contract字段；引用必须Picker|
|E11|检验记录详情|T3|已人工确认，禁止重新设计：Header记录号/状态→摘要(请验/样品/物料批/QC标准/检验人/时间)→主表项目/标准要求/实际结果/单位/判定→结论与复核→actions；禁止版本/ID/修订/Audit/Hash|
|E12|检验执行|T3-execution|上下文→项目列表→当前项目执行/结果录入→actions；不铺全部历史|
|E13|检验报告列表|T1|报告号、请验单、综合结论、状态、批准时间、操作|
|E14|检验报告创建|T2-controlled|选择/确认请验单与可汇总结果→生成；禁止UI重算|
|E15|检验报告详情|T3|已人工确认，禁止重新设计：Header报告号/结论→基本信息(请验/样品或物料批/QC标准/日期/检验人)→结果表→结论→审批信息(检验人/复核人如有/审批人/日期状态)→actions；不要独立方法和仪器区块|
|E16|来料调查列表|T1|调查号、类型、物料批、严重度、状态、操作|
|E17|来料调查详情|T3|身份/状态/严重度→受影响检验/原始结果→描述→调查结论→决定处置→最终选定结果→关闭事实→actions|
|E18|物料放行|T6|物料批/质量库存状态→Release条件→报告/调查摘要→Decision Summary→Release/Reject|

## F QC Specification
F01 标准列表 T1：代码/名称、适用对象(已有时)、状态、有效版本、操作。  
F02 标准创建/详情 T2/T3：身份/适用对象/状态→有效版本→actions。  
F03 标准版本 T2 Definition Editor：项目代码/名称/必检/结果类型/上下限或文本标准/单位/Contract支持的方法引用；Approved/Frozen只读。

## G Production
|ID|页面|模板|页面结构|
|---|---|---|---|
|G01|生产订单列表|T1|订单号、产品、计划数量/单位/日期、状态、操作|
|G02|生产订单编辑|T2|订单身份→产品/计划→日期→操作|
|G03|生产订单详情|T3|订单号/状态→产品/计划→关联批次摘要→actions|
|G04|生产批列表|T1|查询关键字/批号、产品、生产订单、状态、计划日期范围(按v1.0.17实现)；列批号、产品、订单、计划量/单位/日期、状态、操作|
|G05|生产批编辑|T2|生产订单/产品→计划量单位日期→工艺上下文→操作|
|G06|生产批详情|T4|批号/状态/产品→计划摘要→执行单元/工序进度→物料准备→质量→物料平衡→QA放行→T5/T6入口|
|G07|物料平衡|T3 analytical|批次/产品→投入/产出/损耗/差异→公式结果→阈值状态→关联偏差→actions|

## H Execution / eBR
H01 **生产执行 T5**：已批准，禁止重新设计。Batch Summary+Current Operation/Progress→横向工艺→三栏工序导航/中央工作区/Quick Actions→Tabs执行记录/物料/设备/工艺参数/IPC/异常→首屏物料表/参数表。无真实照片Contract不放静态图；不支持动作隐藏。  
H02 称量 T5-mode：同一Header/工序上下文；中央为称量任务、计划/实际量、单位、物料批、受控动作。  
H03 投料 T5-mode：同一工作台；投料记录、物料批、计划/实际量、时间、操作人、状态。  
H04 eBR Runtime T5-linked：Batch/Operation/Form上下文→动态表单→validation→签名动作；修订机制不铺主表单。  
H05 eBR模板列表 T1：代码/名称、产品/工艺、状态、版本上下文、操作。  
H06 eBR模板创建/查看 T2/T3：身份、适用范围、状态、版本、actions。  
H07 eBR Designer Specialized：左Palette/中Canvas/右属性规则；顶部模板身份+现有Preview/Save/Publish。

## I Production Quality / QA
I01 生产质量计划列表 T1：计划号、批次/工艺上下文、状态、责任、操作。  
I02 计划编辑 T2：适用批次/工序→检查计划→责任→操作。  
I03 计划详情 T3：身份/状态→适用范围→检查项→责任→actions。  
I04 生产检验列表 T1：检验号/批次/工序、结论、状态、操作。  
I05 生产检验新增/详情 T2/T3：上下文→项目/结果→结论/复核→actions；沿用精简检验原则。  
I06 IPC T5 Tab：定义/限值→当前结果→结论→当前动作。  
I07 生产偏差 T3：偏差号/状态/严重度→批次/工序→描述→调查/决定→actions；无Contract不造创建按钮。  
I08 **QA批审/放行 T6**：已验收Decision Workbench；批次Header→6 Gate进度/Checklist→Decision Summary→actions→次级PDF/证据。FINAL eBR PDF是QA决定后归档，不是第7 Gate。

## J Trace / Audit / Integration
J01 Trace Specialized：搜索/过滤→lineage graph/timeline→节点Drawer→legend→Deep links；只读。  
J02 Audit Specialized：Filters→Event表/时间线→Detail Drawer(before/after/canonical evidence)。技术密度不得复制到业务页。  
J03 Integration T1 Console：Inbox/Outbox→Contract过滤→消息Key/方向/状态/尝试/时间→Detail Drawer payload/error/retry；Retry/Replay仅现有动作。

## K Responsive / Action Rules
T1 Mobile查询单列，表格Card内横滚；T2单列且Label/Control一致；T3 Header上下重排、结果表内滚；T4生命周期可横滚、来源卡2列转1列；T5非PC-only时工序导航→工作区→actions纵向且Tabs横滚不截字；T6 Checklist→Summary→Actions→Evidence纵向。页面级Create/Edit在Header右；T3动作集中在Header或结论动作区；T5动作只在Quick Actions；T6 Release/Reject语义分离。

## L Codex施工与验收
1. 每次按页面族实施，不允许全仓自由重构。
2. 实施前声明模板；已有“人工确认/禁止重新设计”页面只做必要兼容修复。
3. 每族完成：typecheck + affected tests + build + Chromium Desktop/Mobile（PC-only除外）。
4. 截图保存 `docs/acceptance/ui-v2-complete/<family>/`，禁止只放TEMP。
5. 验收检查：标题、区域顺序、字段组、表格列、按钮位置、状态语义、隐藏技术字段、PC/Mobile。
6. 需要新字段/API/状态/权限/allowedAction或与FINAL BASELINE冲突时，立即 `DESIGN CHANGE REQUIRED`。
7. 不得用示例图中的虚构值、照片、按钮补齐真实Contract。

推荐顺序：其他来料质量T3 → MaterialLot T4 → Production Batch T4 → 剩余T1 → 剩余T2 → T6收口 → eBR → Trace/Audit/Integration → Dashboard/IAM/Master/WMS/Production一致性 → Full UI Consistency Audit。
