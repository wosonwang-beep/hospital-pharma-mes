# Design confirmation record — QC 质量标准新增/编辑页交互重构

Status: **DESIGN LOCK**（记录于 2026-11-02）。上级授权：
[DCP-QC-SPEC-BASIC-LIBRARY-001](../DCP-QC-SPEC-BASIC-LIBRARY-001-APPROVED.md)；
基础设计记录：[QC-STANDARD-BASIC-LIBRARY-DESIGN-LOCK.md](QC-STANDARD-BASIC-LIBRARY-DESIGN-LOCK.md)。
本记录只收窄 QC 质量标准**新增/编辑页**的交互形态，不改业务、数据、API、权限与列表页。

## 1. Human decision

用户在 2026-11-02 的对话中，对现有新增/编辑页给出的原话：

> 编缉的操作很不友好

> 适用物料/产品是一个列表，点击填加按钮出现弹窗选择物料或产品，不要维护记录。就是很简单的增删改查。

> 是修改检验标准的编缉或新增页面，检验标准的列表页面不用修改

据此确认的三条指令：

1. **只改新增 / 编辑页**（`/quality/specifications/create`、`/quality/specifications/:id`），
   QC 质量标准**列表页不改**。
2. **适用范围改为列表 + 添加按钮 + 选择弹窗**，不再使用多选下拉。
3. **页面不展示「维护记录」**；维护原因仍在保存时必填并提交后端（审计要求不变）。

同日第二轮指令（据此把页面改为页签式）：

> 适用范围做成一个页签，编缉和新增，没有删除标准，还有［底部操作条截图］这个不要

4. **新增页与编辑页统一改为页签式**。
5. **页面不提供「删除标准」**；删除能力只保留在后端接口与权限码，不在本页暴露入口。
6. **去掉底部操作条**（未保存状态点 + 维护原因输入 + 返回列表 + 保存标准全部移除）；
   保存入口只留页头一个主色按钮，维护原因改为点保存后弹出的确认窗内填写。

同日第三轮指令（据此把检验项目移出页签）：

> 检验项目是重要的，适用范围是次要的，适用范围可以做成页签，检验项目不需要

7. **检验项目常显**，不再作为页签；它紧跟页签区直接铺在页面上（表格 + 「＋ 添加检验项目」）。
8. **页签只保留两个**：`基本信息` / `适用范围`。检验项目是标准的核心内容，任何页签下都可见，
   不需要点页签才能看到；适用范围属于次要信息，收进页签。

## 2. 不动的东西（与上级 DCP 一致）

- 7 个端点、DTO、`If-Match` + `Idempotency-Key`、`recordVersion` 乐观锁、权限码
  `qms:specification:view/create/edit/delete`。
- `qc_specification` / `qc_specification_scope` / `qc_specification_revision` 表结构与
  `qc_specification_revision` 的写入（后端照旧记录 CREATE/UPDATE/DELETE，前端不再展示）。
- 检验项目的取值规则（数值需至少一个限值 + 单位；文本只能用文本合格标准；编码大小写不敏感唯一；
  小数 12 位整数 6 位小数）。
- 一个标准可同时挂多个物料与多个产品；至少一个适用主体。
- 列表页、路由、导航、下游快照与冻结语义。
- 页面模板仍为 **T2**。

## 3. 锁定的交互规格

### 3.1 页面结构（自上而下）

1. 页头：返回列表链接 / 标题 / 编码·依据·状态摘要；右侧「有未保存的修改」提示（仅改动后出现）
   + 主色「保存标准」。**无删除标准**。
2. 错误提示区：单条 `a-alert`，**同时把视口定位到出错区块**；错误落在页签内时先切到该页签，
   落在检验项目时保持当前页签、只滚动过去。
3. **页签栏（两项）**：`基本信息` / `适用范围`，默认停在基本信息。
4. 页签「基本信息」：卡片内表单 —— 标准编码、标准名称、标准的依据（字典）、依据说明、生效日期、状态、备注。
5. 页签「适用范围」：卡片内**适用物料**、**适用产品**两个列表，各自右上角一个主色「＋ 添加」按钮。
6. **检验项目（常显，不在页签内）**：页签区下方独立卡片，卡片内**表格**（一行一个项目）+
   卡片右上角「＋ 添加检验项目」；切换页签时该卡片始终可见。
7. **无底部操作条**；**不出现**「维护记录」卡片。
8. 点「保存标准」：先本地校验（失败则定位）；编辑态弹出「确认保存标准」小窗，
   填写维护原因后确认提交；新增态不弹窗直接创建（审计原因记为「新增标准」）。

### 3.2 适用范围列表

| 项 | 规格 |
| --- | --- |
| 列 | 名称 / 编码 / 操作（移除） |
| 空态 | 「尚未添加适用物料」等说明性空态 |
| 添加 | 点「＋ 添加」打开选择弹窗：关键字搜索 + 多选表格 + 分页（每页 10）；已选中的行默认勾选 |
| 确定 | 合并进当前列表，重复项不重复添加 |
| 移除 | 行内「移除」，即时移除（保存时统一提交） |
| 权限 | 无 `master:material:view` / `master:product:view` 时禁用添加并说明 |
| 数据 | 沿用 `GET /materials`、`GET /products`；已选但不在搜索结果内的主体按 id 回读补全名称 |

### 3.3 检验项目表格

| 列 | 内容 |
| --- | --- |
| 序号 | 1..n |
| 项目编码 | 原文 |
| 项目名称 | 原文 |
| 必检 | 是 / 否 |
| 结果类型 | 数值 / 文本 |
| 合格标准 | 数值：`下限 ~ 上限 单位`（尾零归一，单边限值显示 `≥` / `≤`）；文本：文本合格标准摘要 |
| 方法 | `方法编码 v方法版本` |
| 操作 | 编辑（行内弹窗）/ 移除（二次确认） |

- 「＋ 添加检验项目」位于卡片标题栏右侧，主色按钮；表格下方不重复该按钮。
- 行编辑用弹窗（宽 720）：项目编码（已存项目只读并说明原因）、项目名称、是否必检、结果类型、
  数值组或文本组、方法编码、方法版本。弹窗内做字段级校验，未通过不放行。
- 已存项目的 `itemCode` 保持只读（后端以编码为业务键）。

### 3.4 保存与校验

- 保存前本地校验：标准编码/名称、依据、依据说明、至少一个适用范围、每个检验项目。
- 校验失败：`a-alert` 显示首个错误 + **定位到出错区块并 1.5s 高亮**；错误在页签内则先切页签，
  错误在检验项目则保持当前页签只滚动。
- 编辑态保存：弹「确认保存标准」窗，维护原因必填（空则提示「请填写维护原因」并保持弹窗打开）。
- 新增态保存：不弹原因窗，`reason` 记为「新增标准」，创建成功后跳转详情页。
- 保存仍按「表头 → 范围 → 项目」三段提交，仅提交发生变化的段；版本号逐段推进。
- 无改动点保存时提示「没有需要保存的修改」，不发请求、不弹原因窗。
- 新建成功后先把表单快照同步为已保存状态再跳转，避免被「放弃未保存内容」的离开守卫拦下。
- 409 保持既有冲突提示。

## 4. Write scope

- `frontend/mes-web/src/views/quality/QcSpecificationDetailView.vue`
- `frontend/mes-web/src/views/quality/QcScopePicker.vue`
- `frontend/mes-web/src/views/quality/QcSpecificationItems.vue`
- `frontend/mes-web/src/views/quality/QcSpecificationModel.test.ts`（仅在必要时补断言）

禁止修改：`QcSpecificationListView.vue`、`src/api/qcSpecifications.ts` 的契约部分、
router、任何后端模块、任何 Flyway 迁移、上级 DCP 与既有设计锁定。

## 5. Acceptance assertions

1. 新增页与编辑页的字段集合、字段来源、提交参数与改造前完全一致（仅呈现与交互变化）。
2. 适用范围不再出现多选下拉；添加走弹窗；一个标准可挂多物料 + 多产品。
3. 页面内不存在「维护记录」展示；`qc_specification_revision` 仍由后端写入。
4. 检验项目以表格呈现，**常显于页签区下方**，行编辑在弹窗内完成，校验规则与 `qcItemPayload` 一致。
5. 保存按钮在页头常显；页面不出现底部操作条与删除标准入口。
6. 校验失败能定位到具体区块（必要时切页签），不再只依赖页面顶部的文字提示。
7. `vue-tsc` 通过；QC 模型测试通过；真实 5173/8080 环境可完成「新增 → 保存 → 再编辑 → 保存」。
8. 列表页零改动（`git diff` 不含 `QcSpecificationListView.vue`）。

## 6. Recorded evidence (2026-11-02)

| 断言 | 结果 | 证据 |
| --- | --- | --- |
| 1 字段与提交参数不变 | PASS | 仅 3 个 `.vue` 文件变更；`qcCreatePayload`/`qcItemPayload`/3 段 PUT 调用未改 |
| 2 适用范围为列表 + 弹窗 | PASS | `v3-scope-added.png`、`v3-scope-picker.png`；真实保存后后端 scopes 增加 2 个物料 |
| 3 无维护记录展示 | PASS | 卡片标题仅「标准基本信息 / 适用范围 / 检验项目」，`hasRevisionCard=false` |
| 4 项目表格 + 弹窗校验 | PASS | `v3-item-dialog.png`、`v3-item-validation.png`；清空限值被拦「数值项目至少填写一个限值」 |
| 5 操作条可见可发现 | PARTIAL | 底部操作条改为内容流末尾（不再吸附，避免遮挡卡片标题），页头新增主色「保存标准」作为常显入口 |
| 6 校验可定位 | PASS | 缺维护原因提示出现于顶部 alert，同时滚动并高亮对应区块 |
| 7 类型/测试/真实链路 | PASS | `vue-tsc` exit0；`vitest src/views/quality` 12/53 通过；真实保存 `versionNo 2→3→4` 并复原 scopes |
| 8 列表页零改动 | PASS | `QcSpecificationListView.vue` mtime 11:34（早于本次 12:39 开工） |

环境前置：8080 原进程加载改造前打包（`mes-security` 含 `approve|retire`），PUT/DELETE 一律 403；重新打包并重启后链路打通。该修复不涉及源码改动。

截图与可复跑脚本：`artifacts/qc-spec-edit-review-20261102/`（`v4-edit-1280.png`、`v4-create-1280.png`、`v3-*`、`verify-flow.mjs`）。

## 7. 页签化改造证据（2026-11-02 第二轮）

| 断言 | 结果 | 证据 |
| --- | --- | --- |
| 三页签：基本信息 / 适用范围 / 检验项目 | PASS | `v7-tab-basic.png`、`v7-tab-scope.png`、`v7-tab-items.png`；`tabTexts` 三项 |
| 新增页与编辑页同构 | PASS | `v5-create.png`、`v6-created.png`；create 路由同样渲染三页签 |
| 无「删除标准」入口 | PASS | 两页 `hasDeleteButton=false` |
| 无底部操作条 | PASS | `hasSavebar=false`；页面高度 1280 视口下由 1586px 降到 900px |
| 校验失败自动切页签 | PASS | 清空标准名称后点保存 → `alert=请填写标准名称`、`activeTab=基本信息`、基本信息区块可见 |
| 编辑保存（原因弹窗） | PASS | 弹窗标题「确认保存标准」，`versionNo 5→6`；复原后 `7→8`，`remark` 回到 null |
| 新增保存（不弹原因窗） | PASS | 创建 `E2E-TABS-540188`（versionNo 0，scopes `PRODUCT:HYTEA100`，items `E2E_ASSAY`），自动跳转详情页；测试数据已 DELETE 清理 |
| 类型 / 单测 | PASS | `vue-tsc` exit0；`vitest src/views/quality` 12 文件 / 53 用例通过 |
| 列表页零改动 | PASS | 本轮未触碰 `QcSpecificationListView.vue` |

改造中发现并修复的缺陷：新增保存成功后 `router.replace` 被
`onBeforeRouteLeave`（`dirty=true` 时 `window.confirm`）拦截，导致创建成功却停在新增页；
改为创建成功后先同步 `original` 快照再跳转。

## 8. 检验项目常显改造证据（2026-11-02 第三轮）

| 断言 | 结果 | 证据 |
| --- | --- | --- |
| 页签只剩两项：基本信息 / 适用范围 | PASS | `tabTexts=["基本信息","适用范围"]`；`v8-edit-basic.png` |
| 检验项目常显、位于页签区下方 | PASS | 基本信息页签下 `itemsVisible=true`、`itemsTop=565 > tabsBottom=549`、1 行数据 |
| 切换页签后检验项目不消失 | PASS | 切到「适用范围」：`itemsVisible=true`、`itemsRows=1`、`v8-edit-scope.png` |
| 校验定位（页签内字段） | PASS | 清空名称且停在适用范围 → `alert=请填写标准名称`、`activeTab=基本信息`、`qc-block-error` 落在基本信息卡片；`v8-validation-basic2.png` |
| 校验定位（检验项目） | PASS | 移除全部项目保存 → `alert=至少需要一个检验项目`、不切页签、检验项目卡片滚入视口 |
| 新增页同构 | PASS | 两页签 + 空态「尚未维护检验项目…」+ 无删除标准 + 无底部条；`v8-create-basic.png` |
| 编辑保存 | PASS | 改备注保存 `versionNo 8→9`，复原 `9→10`，`remark` 回 null，scopes `PRODUCT:KCL30`、items 1 条不变 |
| 新增保存 | PASS | 创建 `E2E-TABS-922638`（scopes `PRODUCT:HYTEA100`、items `E2E_ASSAY`）自动跳详情页，DELETE 清理返回 200 |
| 类型 / 单测 | PASS | `vue-tsc` exit0；`vitest src/views/quality` 12 文件 / 53 用例通过 |
| 列表页零改动 | PASS | 本轮只改 `QcSpecificationDetailView.vue` |

证据脚本：`artifacts/qc-spec-edit-review-20261102/verify-items-persist.mjs`、
`verify-edit-save.mjs`、`verify-tabs-flow.mjs`（已适配“检验项目常显”）。

