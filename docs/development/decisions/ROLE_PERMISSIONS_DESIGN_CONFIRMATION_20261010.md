# 角色与权限：编辑页设计确认（2026-10-10）

## 确认来源与阶段
用户在本次对话最后一张设计图后明确回复“可以了”。本记录确认该设计图及以下明确交互，不代表可交互原型通过验收，更不代表正式 MES 已获集成或验收批准。
- 设计图：`a_clean_white_blue_corporate_admin_ui_screenshot_i.png`
- 会话附件 ID：`file_00000000b1b08230b2a9bd881811618b`
- 图像生成标识：`8afaad02-c42f-46e3-9656-a973e253b40e`
- SHA-256：`31d857342a2cc1c2f4dc024c5b390292126bcc04bd3060673352c26eacc8ec46`
- 原图保存在当前聊天附件；当前执行环境归档副本为 `/mnt/data/mes-role-permissions-approved/approved-role-permissions.png`。此路径不是用户电脑路径，不宣称原图已复制到本仓库。

## 已确认交互（不可擅自重画）
1. UI Template: T2，编辑角色独立页面，基本信息在左，菜单权限树在右；取消、保存位于页头右侧。
2. 菜单必须是树形结构；菜单名称前的 checkbox 本身表示授权该菜单可见。
3. 不得额外增加“可见”列，也不得增加第二个可见性 checkbox。
4. 每个业务菜单名称后，在同一行配置“只查看”“管理”两项勾选控件；不是点击菜单后另开右侧功能面板。
5. 从未勾选变为勾选菜单时，默认选中“管理”。已保存角色进入编辑时按原授权回显，不自动升级已有只查看权限。
6. “只查看”和“管理”互斥；管理包含该菜单目录中现有普通管理操作，不生成不存在的权限。
7. 取消菜单可见时清除该菜单关联的授权选择并禁用该行功能选择；共享底层权限的删除须保留仍被其他可见菜单需要的权限。
8. 审核、签名、放行等特殊权限仍独立配置，不因普通“管理”默认勾选而新增授权。

## 继承规范及尚待核对的图文差异
当前业务权威为 FINAL BASELINE v1.0.23 + 已批准 DCP；视觉仍为 Global UI V2 / T1–T6。用户此前强调的所有查询及表单标签与控件水平同行要求继续有效。
原图的上下排字段标签、示例模块名称、角色描述和创建/修改信息不能隐式修改上述约定或真实业务字段。正式原型开始前须逐项核对这些图文差异；不得把本次设计图确认扩展为新增数据库字段、菜单、API、权限或放宽 GxP 的授权，也不得宣称逐像素一致。

## 本次真实读取依据
`AGENTS.md`、`docs/development/MES_DEV_RULES.md`、`MES_TASKS.md` 相关系统管理索引、`docs/PROJECT_BASELINE.md` 权威入口、`docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md`、`docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md`、`docs/development/DCP-DATABASE-MENU-AND-USABILITY-001-APPROVED.md`、`frontend/mes-web/src/views/admin/RoleDetailView.vue`、`frontend/mes-web/src/layouts/navigation.ts`；另读取本机 mes-demo 的 AGENTS.md、App.vue、main.ts、style.css、package.json、vite.config.ts 和 CUAWright SKILL.md。
现有角色页业务字段为 roleCode / roleName / status，菜单与权限为 menuCodes / permissionCodes；沿用现有 /roles、/menus、/permissions 及 /roles/{id}/permissions 契约。

## 后续交付边界
原型位置：`D:\codex\_project\gmp\mes-demo`，复用现有 Vue 3 + Ant Design Vue 和 5174 原型环境、统一 Index/菜单入口；仅模拟数据，不执行正式业务写入。原型需覆盖树展开收起、菜单搜索、勾选与权限切换、保存回显、取消/离开提示、空态、错误态、只读状态及响应式检查。
原型须以具体源码哈希、截图及交互验证证据再次交用户确认；此前不得迁入正式 MES。

## 本次修改与验证
本次仅新增本设计确认记录，未修改任何原型或正式业务代码、数据库、迁移、权限及配置，未提交或推送 Git；保留全部既有未提交文件。已读取 main 的 git status，mes-demo 当前不是 Git 仓库，后续用源码内容哈希标识。
本次构建、交互验证、真实业务 API 写入、数据库持久化及正式验收均 NOT RUN。设计图为 HUMAN CONFIRMED；原型制作及最终验收尚未完成。运行任务状态仍以 MES_TASKS.md 为准，本记录不重开或关闭其他任务。
