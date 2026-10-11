# 角色权限设计图：UI UX Designer 草案 B

日期：2026-10-10。阶段：PENDING HUMAN DESIGN IMAGE APPROVAL。
本文件记录本次设计图提案，不替代已有确认记录，不授权正式 MES 集成。

## 1. 基线依据

本次实际读取 AGENTS.md、docs/development/MES_DEV_RULES.md（含 2A、6A）、MES_TASKS.md 系统管理相关索引、docs/PROJECT_BASELINE.md 权威入口、docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md、docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md，以及 docs/development/DCP-DATABASE-MENU-AND-USABILITY-001-APPROVED.md。
业务权威：FINAL BASELINE v1.0.23 + 已批准限定范围 DCP。视觉权威：Global UI V2；本页选择 T2 独立编辑表单。
同时读取 docs/development/decisions/ROLE_PERMISSIONS_DESIGN_CONFIRMATION_20261010.md 和已有角色权限设计说明。该记录中的先前设计图确认保持原状；本次图不自动取代先前确认。
合同来源：frontend/mes-web/src/views/admin/RolesView.vue、RoleDetailView.vue、src/layouts/navigation.ts 和 src/global-ui-v2.css。
应用技能：UI UX Designer 的 ui-ux-designer、high-fidelity-mockups，以及 imagegen。

## 2. 设计差异与交互约束

- 页头返回角色列表，右侧取消、保存；保留已有编辑角色入口。
- 左侧仅 roleCode（编辑只读）、roleName（必填）、status（ACTIVE / INACTIVE）；所有标签与控件水平同行。
- 右侧单一菜单树；菜单名称前唯一 checkbox 表示菜单授权，不增加第二个可见性列。
- 业务菜单同一行配置“只查看 / 管理”两个互斥选择；目录节点仅显示树展开与目录勾选，普通权限列留空。
- 首次勾选菜单默认选择其现有普通管理权限；已保存授权按原数据回显，不能升级已有只查看权限。
- “管理”仅表示菜单权限目录中实际存在的普通操作集合，不新增权限编码；共享权限须保留仍被其他已选菜单依赖的授权。
- 特殊权限独立选择，示例“电子签名”沿用现有权限，不纳入默认普通管理授权。
- 图中的 QUALITY_OPERATOR、质量管理操作员、选中菜单和新增/移除数量都是示例，不代表实际数据库事实。
- 不添加角色描述、部门、数据范围、审计面板、人工变更原因或版本操作。
- 延续七域导航顺序、水平搜索、34px 控件目标、228px 侧栏目标、56px 顶栏目标、24px 内容间距及现有 V2 色彩。

字段/API 绑定：roleCode / roleName / status 来自现有 Role；menuCodes 来自 /menus；permissionCodes 来自 /permissions；读取 /roles/{id}，角色信息保存 PUT /roles/{id}，授权保存 POST /roles/{id}/permissions。沿用现有权限校验、组织隔离、If-Match 与幂等契约。

## 3. 修改范围

仅新增当前目录下本说明和角色权限-UIUXDesigner-草案B-20261010.png。已有设计、确认记录、正式 Vue/Java、数据库及本机配置均保留，无提交/推送。
迁移：无。业务数据写入：无。

## 4. 设计图与证据

生成方式：内置 image_gen，初次生成后进行 1 次定点视觉修正，移除目录行误画的普通权限 checkbox。
文件：角色权限-UIUXDesigner-草案B-20261010.png。
SHA-256：F032B482EB1E012699B53B44B0D2C3D03B30B796BAE5F79BA6D3E9279156B330。
原始生成图保留在本机 .codex/generated_images 中。

生成提示概要：正视、清晰中文、桌面医院制剂 MES T2 角色编辑页；严格遵循现有 V2 token 和七域导航；左侧三项真实字段，右侧树状菜单同行普通权限、独立特殊授权，禁止新增字段和业务权限。定点修正仅清除目录行的只查看/管理框。

人工图像检查：已查看生成图，字段与搜索标签水平同行、菜单行权限位置、目录行空权限列及独立特殊权限符合本次草案要求。栅格图不是浏览器 DOM 证据，不宣称精确尺寸或响应式验收 PASS。
静态构建、单元测试、Vue 原型交互、375/900/1280/1440 浏览器几何检查、真实 5173/8080 HTTP、数据库和多角色验收：全部 NOT RUN；本次仅设计图预览。

## 5. 结论

设计图交付完成，等待人工评审；系统管理运行状态仍 IN PROGRESS，以 MES_TASKS.md 为准。
没有原型源码哈希批准或新的 APPROVED / FROZEN DESIGN LOCK，不能声称 READY FOR ACCEPTANCE / ACCEPTED，也不能开始正式集成。
后续原型须在既有 mes-demo Vue 3 / Ant Design Vue 的 5174 环境完成交互、响应式和异常状态，并交付与源码哈希绑定的截图与标注说明。
