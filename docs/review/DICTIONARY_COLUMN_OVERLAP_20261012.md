# 数据字典 CODE / 名称重叠局部修复证据

## 1. 基线依据

读取路径、v1.0.23 / Global UI V2 / T1 约束和写入白名单见 [单一设计锁定](../development/decisions/DICTIONARY_COLUMN_OVERLAP_20261012.md)。用户截图（1452×618）是缺陷来源，保留既有列表布局；本次不是页面重设计，不更改历史设计或验收记录。

## 2. 设计差异及实际修改

原 CODE 150px / 名称126px，在全局不换行样式下，长 CODE 越界覆盖名称。现 CODE260px / 名称160px，两列复用 Ant Design Vue ellipsis/title；最小表格滚动宽1320px。字段顺序、业务值、查询、分页、按钮、路由和权限保持。真实1452px截图中 PRINT_BUSINESS_DOCUMENT、SUPPLIER_QUALIFICATION_STATUS 均独立完整显示，名称与其保持间距；更长文本保留完整title。

## 3. 修改范围 / 迁移 / 数据

产品源码只有 frontend/mes-web/src/views/admin/DictionaryListView.vue 三处变更；新增本设计锁定、本证据记录，并在MES_TASKS.md追加状态。无Flyway或数据库操作，无业务新增/修改/删除；保留所有已有工作树修改，未提交推送。运行沿用原checkout、5173/8080，未重启服务或创建替代服务。

## 4. 验收证据

本机证据：`C:/Users/Administrator/AppData/Local/Temp/mes-dictionary-overlap-20261012/`，含result.json、width-1452/1440/1280/900/375.png、items.png。`after.png`抓到弹窗关闭动画，不能作为稳定列表证明，以width-1452.png为稳定列表证据。截图1452×900与用户1452×618来源相比，高度/滚动位置不同，但列表列重叠可直接核对，不宣称整页像素还原。

- 静态/构建 PASS：`npm run build`（vue-tsc -b + vite build）exit0，Vite25.57s；日志 `C:/Users/Administrator/AppData/Local/Temp/mes-dictionary-overlap-build.log`。既有大包警告保留。
- 范围diff审查 PASS：仅三处局部显示变更；`git diff --check -- frontend/mes-web/src/views/admin/DictionaryListView.vue` exit0，无本修复新增CRITICAL/HIGH。
- 实机表格几何 PASS：5视口×7行×2列=70个单元格，文字在单元格内或受ellipsis裁切，完整title与原业务值一致。5视口分别1452/1440/1280/900/375，表格内部横向滚动。
- 功能 PASS：6项断言（页面身份、真实7条记录、CODE查询到1条、重置恢复7条、查看实际3项字典、无框架错误覆盖层）。4次字典读取HTTP200（列表/查询/重置/字典项）。原8080健康检查UP。
- 查询标签配对：1452/1440/1280各5对 PASS；900/375 FAIL，系既有查询区布局问题，900创建日期标签换行；375标签/控件拥挤且documentWidth384>375。当前局部修复未触碰查询/外壳，未改断言或隐瞒失败。完整页面视觉门禁FAIL，表格局部门禁PASS。
- Console：保留1条既有认证恢复401资源日志；随后认证/字典读取200，未出现页面JS异常或Vite覆盖层。不声明console全空。
- 数据库/业务写操作、持久化、非管理员权限正反例及全系统回归 NOT RUN / 本次纯显示修复不适用；未关闭既有字典整页验收。

命令：`C:/Users/Administrator/.codex/tools/cuawright-venv/Scripts/python.exe C:/Users/Administrator/AppData/Local/Temp/mes-dictionary-overlap-check.py`，输出日志 `mes-dictionary-overlap-check.log`；6功能断言和70单元格检查通过，原始窄屏失败另列。优先CUAWright MCP起初因缺失Chromium1243文件失败，经官方`python -m playwright install chromium --only-shell`恢复（exit0）后正常登录、页面/截图检查。通用标签工具未识别本页span标签，返回total0/passfalse；没有改工具或阈值，补充脚本复用server_local的登录/只读网络guard及同一浏览器profile，测量本页实际标签。business_mutations_allowed保持false。

自动实施始于设计锁定落盘（本机时间2026-10-12 12:23:44），验证结果于12:25:44落盘，实施至验证约2分钟；自动修复轮0，未反复补丁。环境恢复不修改产品契约；失败分类：原工具缺失=环境错误、窄屏查询=既有实现布局缺陷。未为其扩大修改范围。

## 5. 结论

截图所示 CODE/名称重叠已修复并真实验证。整体页面因既有900/375查询布局失败，不能标记整页READY/PASS/ACCEPTED；本次状态在MES_TASKS.md记为 IN PROGRESS / TABLE_FIX_VERIFIED / EXISTING_RESPONSIVE_LAYOUT_FAIL。保留既有大包/认证恢复日志和窄屏布局债务，不自动开展重设计或下一任务。

## 6. 人工确认记录 — 2026-10-12

用户在本次修复交付说明与截图后明确回复“验收”。确认范围仅为CODE / 名称重叠局部修复；上述第5节保留为实施阶段历史，最新运行状态只维护于MES_TASKS.md。本次登记前重新读取AGENTS.md、docs/development/MES_DEV_RULES.md、相关任务记录及本设计锁定/验证记录。仅更新任务索引和追加本人工确认证据，无新源码、测试、浏览器、服务、数据库或迁移操作，不提交推送。原900/375布局失败、认证恢复401和bundle警告均保持，不将局部人工确认扩展为整页或全系统验收。
