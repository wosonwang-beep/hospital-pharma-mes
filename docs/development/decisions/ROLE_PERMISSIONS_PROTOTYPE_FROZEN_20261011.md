# 角色权限确认交互原型冻结及正式集成 — 2026-10-11

状态：APPROVED / FROZEN (design), IMPLEMENTATION IN PROGRESS (formal MES).
确认来源：用户 2026-10-11 再次明确角色权限功能原型已经确认，要求原型确认后立即补设计文档/基线、编码、测试、验收。该确认覆盖 2026-10-10 仅设计图已确认但原型尚待审的历史状态。参见 ROLE_PERMISSIONS_DESIGN_CONFIRMATION_20261010.md。

## 冻结原型
- 本机 Vue 3 交互原型：D:/codex/_project/gmp/mes-demo/src/features/roles/RolePermissionsApproved.vue，SHA256 26216113176821780456FC9336CC6DBFB2C376DDF383EFC745654231D6592A97。
- 相关壳样式：D:/codex/_project/gmp/mes-demo/src/role-approved-shell.css，SHA256 D7B3C4A93C42DEBB38DC9175E13600CD46B6881B225527B79C8FBEE013929C7E。
- 原型路由 http://127.0.0.1:5174/#/roles/edit，列表 #/roles，查看 #/roles/view；正式 MES 保留现有 /admin/roles、/admin/roles/{id}/edit 和 /admin/roles/{id} 路由。冻结原型为设计权威，正式真实 API 不使用 DEMO 编码或 localStorage 业务数据。

## 必须实现
- T2 角色编辑页：顶部返回标题，角色名称/编码/状态分组表单；角色编码编辑时只读；字段标签与控件同行。已有 T1 查询、T3 查看保留；统一 Global UI V2。
- 一个菜单权限树；每项菜单名称前的复选框就是可见授权，无额外“可见”列；菜单名后同行一组“只查看/管理”互斥权限。初次勾选该菜单，管理只包含已存在的普通管理权限，不能推导不存在的权限。
- 既有已勾选菜单的旧只查看授权按原样回显，不自动升级；重新勾选时默认管理；取消菜单可见时清理该菜单独占权限，不删除其它可见菜单共用权限或未展示历史权限。
- 独立“特殊操作权限”区域，审核/签名/放行/导出等特殊权限**必须显式单独选择**，普通管理不自动授予。现有服务端菜单必需权限 Gate 不放宽；若某菜单真实要求特殊权限，保存前提示需要显式勾选，不隐式授权也不忽略授权失败。
- 权限树展开/收起、菜单搜索、已选展示、变更预览、编辑保存/回显、取消离开确认、空态/接口失败/只读禁用。角色信息与菜单/功能权限同一个页面编辑，复用既有 /roles 和 /roles/{id}/permissions 操作，服务器审计、乐观锁、组织隔离和既有安全控制不改变。不得新建 API/状态/权限/迁移。
- T2 保存/取消与原型操作区同位置，表单水平标签。所有列表和表单 1440/1280/900/375 检验。

## 范围与门禁
- 改动白名单：frontend/mes-web/src/views/admin/RoleDetailView.vue、RoleDetailView.test.ts、少量直属样式，如不必要不新增；本设计记录、docs/PROJECT_BASELINE.md 角色补充、MES_TASKS.md 当前状态、定向验收记录。原型源/共享 shell/其它用户本地未提交代码保留。
- 权威：FINAL BASELINE v1.0.23 + 已批准 DCP、MES_GLOBAL_UI_DESIGN_SYSTEM_V2、MES_PAGE_TEMPLATE_STANDARD_V2 与 ROLE_PERMISSIONS_DESIGN_CONFIRMATION_20261010.md；菜单业务独立 DCP-MENU-CONFIGURATION-001 保留。
- 验收：新勾选默认管理但特殊操作绝不自动授予；旧只查看不自动升级；取消授权保留共享/未展示授权；真实 MariaDB/API 来源，管理员和无权限差异；定向前端测试、typecheck、构建、CUAWright 四视口及保存回读。最终 ACCEPTED 仅经用户明确确认。
- 限制：普通 UI 实施十分钟预算、最多两轮代码修复；超限或需要新增后端契约即停止并报告，不以更改验收断言掩盖。
