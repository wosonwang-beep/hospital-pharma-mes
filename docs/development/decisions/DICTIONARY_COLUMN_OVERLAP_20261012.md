# 数据字典列表文字重叠修复

需求来源：2026-10-12 用户截图并要求“处理这个问题”。截图 `codex-clipboard-0251d457-090c-4198-9f34-b4fbc8c642b6.png`（1452×618）显示长 CODE 越界覆盖字典名称。本项是已授权局部缺陷修复，不新增或重做业务页面，适用 MES_DEV_RULES §2A.7，无新原型集成。

## 基线及设计锁定

实际读取：AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md 的 V039 字典记录、docs/PROJECT_BASELINE.md Authority、docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md §8、docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md T1、docs/development/DCP-DATABASE-MENU-AND-USABILITY-001-APPROVED.md，以及 design/系统管理/数据字典/数据字典-功能与字段设计.md 和 review/实施审查与验收记录.md。业务仍为 v1.0.23 + 已批准限定补充，视觉仍为 Global UI V2 / T1。历史设计草稿的待实现字段说明不是新增授权，以当前正式页面及其已实施契约为准。无业务设计冲突。

页面 `/admin/dictionaries` 保持标题、查询区、工具栏、字段顺序、分页、独立查看/编辑路由及字典项弹窗。只将列表 CODE 列从150px调至260px、名称列从126px调至160px，并为两列开启已有 Ant Design Vue 单元格省略及完整文本 title。列表最小滚动宽从1000px调整为1320px，保障其余列不被挤压。桌面与窄屏均在表格内部横向滚动，字段组标签/控件保持同行；不调整查询布局。

字段直接来自既有 DictionaryType.code/name，复用现有 a-table 与 ellipsis 能力。查询仍使用现有 `/dictionaries` 参数、分页和权限；不新增数据、组件、API、权限、路由、表、迁移、状态、审计或签名控制。仅显示截断，原始值保留并可通过 title 查看。

## 写入白名单及验证计划

- frontend/mes-web/src/views/admin/DictionaryListView.vue：上述局部表格修复。
- 本设计记录及 docs/review/DICTIONARY_COLUMN_OVERLAP_20261012.md：设计依据和验证证据。
- MES_TASKS.md：追加本次缺陷修复状态，保留现有内容。

验收：真实5173/8080字典列表，CODE/名称单元格不得越界覆盖相邻列；1452截图参考宽度及1440/1280/900/375视口DOM、截图、查询标签配对；查询/重置及字典项只读打开；前端typecheck/build。业务写路径、数据库集成及全量回归不适用本次纯显示修复。CUAWright优先，工具缺失文件属于环境故障，记录恢复与工具限制，不能以mock验收。实际证据另存本机。

普通UI自动实施预算10分钟，最多2轮修复，重复根因即停止；首次实现不计修复轮。保留所有已有未提交修改和业务数据；不提交推送。
