# 全系统 T1 / T2 UI 统一审查与整改 — 2026-10-13

状态：**UI G5 READY FOR ACCEPTANCE / NOT HUMAN ACCEPTED**

## 用户确认的全局规则
- 普通基础功能查询/列表页：统一 T1。
- 普通新增/编辑页：统一 T2。
- 普通只读详情：T3。
- 特殊业务页面允许采用专用布局，但必须继承 Global UI V2：
  - T4 360°聚合详情；
  - T5 执行工作台；
  - T6 决策工作台；
  - 工序表单设计器、打印设计器、打印数据源映射、菜单权限配置、电子批记录模板组合编辑器等保留其批准的专用结构。
- T1 查询动作统一：**重置 → 查询**。
- 普通 T2 操作统一：**取消 → 保存/提交**。
- 标签与输入/选择控件在所有视口保持同一水平行，整组换行，不允许标签单独压到输入框上方。

## 本轮实际整改
1. 用户、角色、部门、数据字典、菜单、打印模板、打印数据源等系统管理列表统一 T1。
2. 普通业务列表沿用同一 T1 查询卡/结果卡/总数分页节奏。
3. 用户、角色、部门、产品、质量、生产、WMS、基础资料等普通 create/edit 收口到 T2 的取消→主动作顺序和右对齐动作区。
4. 部门管理原有列表内编辑弹窗不符合“普通编辑=T2”规则，本轮已改为独立路由：
   - /admin/departments/create
   - /admin/departments/:id/edit
   - /admin/departments/:id 为 T3 查看
5. 打印数据源列表回归标准 T1；复杂数据源映射编辑器保留专用 T2。
6. 生产执行工作台模板标识从错误 T4 修正为 **T5**，补齐 <=900px 响应式重排。
7. 手机端不再挂载桌面 sider，修复隐藏桌面菜单仍产生 Ant submenu popup、在页面底部形成幽灵菜单的问题。
8. 控件边界继续使用 Global UI V2 独立 token #8895a5，对白色约 3.048:1；卡片/表格结构线保持浅色，不把全系统边框加重。

## 电子批记录模板当前设计
正式路由使用：
- /ebr/book-templates → BookTemplateListView（T1）
- /ebr/book-templates/create、/:id/edit → BookTemplateEditorView（专用 T2）

当前电子批记录模板不是第二套原业务单据，也不是旧“总模板/章节”模型。它按完整生产批组织引用：
1. 批次基本信息；
2. 生产准备与物料；
3. 生产工序记录（随所选生产工艺动态列出各实际工序，并引用该工序 1-N 份受控工序表单）；
4. 检验与质量记录；
5. 物料平衡及生产总结；
6. 批审核与放行；
7. 附件与补充记录。

当前可引用的业务原始记录包括生产指令、领料申请、原辅料出库、投料、请验、取样、检验记录、检验报告、偏差/OOS/CAPA、物料平衡、QA放行和受控附件。编辑器明确执行：**只引用各业务模块实际产生的原始记录，不在 EBR 中重复创建业务单据。**

旧 BookTemplateView.vue 没有正式路由，已标记为 legacy unrouted compatibility reference，禁止把旧“章节/册模板”称谓重新带回正式页面。

## 正式 5173 四视口矩阵
最终权威证据：`docs/review/evidence/global-t1-t2-20261013/full-route-matrix-v4.json`

- T1：49 个列表入口 × 1440/1280/900/375 = **196/196 PASS**（补齐 GMP Audit Trail 与 Integration Operations）
- 普通 T2：30 个 create/edit 入口 × 4 视口 = **120/120 PASS**
- 特殊页面：8 个代表入口 × 4 视口 = **32/32 PASS**
- pageerror：**0**
- mobile shell：**PASS**
- 列表查询动作：重置→查询 PASS
- T2 动作几何：取消与主动作同一基线、取消在左 PASS
- 页面级横向溢出：0；需要宽表时仅使用组件内部滚动

特殊页面代表：
- 菜单配置：T2 专用权限配置
- 生产工艺：T2 专用工艺编辑器
- 电子批记录模板：T2 专用组合编辑器
- 打印数据源：T2 专用映射编辑器
- 工序表单设计器：T2 专用设计器
- 生产批详情：T4
- 生产执行：T5
- QA 批审核：T6

## 视觉证据 / G5
Evidence：`docs/review/evidence/global-t1-t2-20261013/`

- ui-ux-pro-max 八组知识查询已执行。
- 最终代表截图 5 类页面 × 4 视口 = 20 张。
- 另有用户/字典/打印数据源/角色/产品/批次/QC/WMS/EBR/工艺等专项截图。
- audit manifest validator：**PASS**
- manifest verdict：**READY FOR ACCEPTANCE**
- `git diff --check` 未发现 whitespace error；只有仓库既有 CRLF→LF warning。

## 构建与定向测试
- `npm run typecheck`：PASS
- `npm run build`：PASS（仅既有 chunk-size warning）
- navigation：3/3 PASS
- RoleDetailView 独立运行：11/11 PASS
- AppLayout.auth 在兼容 matchMedia mock 修正后：4/4 PASS
- 真实浏览器 5173 路由矩阵：PASS

## 仓库全量前端单测说明
全量 Vitest 首轮主动复查暴露了 4 类问题，本轮没有掩盖：
1. AppLayout 的移动断点监听与测试 matchMedia mock 不兼容；
2. Integration Operations 的人工重试原因输入丢失、状态筛选测试不可驱动，并且该页面 T1 查询按钮顺序仍是旧顺序；
3. App.test 仍断言旧登录页标题；
4. platform-contract 对全 src 使用 `payload` 一词做过宽禁用，误伤 materialPayload/runtimePayload 等正式业务函数；RoleDetail 两项在全量并行下触发 5 秒超时。

本轮逐项修正后重新执行：
- AppLayout.auth：4/4 PASS；
- PlatformViews（Audit + Integration）：11/11 PASS；
- navigation + AppLayout.auth + RoleDetailView 定向：18/18 PASS；
- **最终全量前端：44/44 test files PASS，186/186 tests PASS。**

platform-contract 的修正没有放宽“Integration UI 不得暴露原始消息载荷”的业务约束，只把检查范围收回到 Integration UI/API 本身；RoleDetail 仅为两项已知长耗时测试增加 10 秒超时，业务断言未改变。

因此：**本轮全局 UI 统一的真实页面矩阵、构建、类型检查和全量前端单测均已通过。**

## 结论
普通基础页面的 T1/T2 规则已从“约定”变成正式代码、路由和响应式基线；特殊业务页面按职责保留 T4/T5/T6/专用设计器，不再被强行套普通 CRUD。当前 UI 统一任务可进入人工验收；后续页面新增/修改必须继续遵守本次冻结决策。
