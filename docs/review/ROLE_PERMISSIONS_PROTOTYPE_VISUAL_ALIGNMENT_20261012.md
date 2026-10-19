# 角色与权限原型视觉一致性回归修复 — 2026-10-12

## 1. 基线依据

- 已实际读取 `AGENTS.md`、`docs/development/MES_DEV_RULES.md`、`MES_TASKS.md`、`docs/PROJECT_BASELINE.md`、Global UI V2、T1/T2/T3 页面模板和 `docs/development/decisions/ROLE_PERMISSIONS_PROTOTYPE_FROZEN_20261011.md`。
- 冻结原型 `D:/codex/_project/gmp/mes-demo/src/features/roles/RolePermissionsApproved.vue` 实测 SHA256 为 `26216113176821780456FC9336CC6DBFB2C376DDF383EFC745654231D6592A97`，与 DESIGN LOCK 一致。
- 本轮为已验收角色权限功能的正式页面视觉回归修复；不重开业务设计，不修改原型、路由、权限码、API、数据库、迁移、审计或乐观锁契约。

## 2. 设计差异与修复

正式 T2 页面原实现使用通用卡片表单和 Ant Tree：角色状态落到第二行，权限区缺少冻结原型的三列表头/说明列，搜索和展开操作布局不同，底部缺少“保存并返回”。同视口肉眼对照确认未达到冻结原型效果，分类为实现错误。

本轮在 `frontend/mes-web/src/views/admin/RoleDetailView.vue` 内复用既有真实 API 与授权计算，只恢复冻结结构：返回标题、单行三字段基本信息、菜单权限标题/说明、右侧菜单搜索/展开收起、菜单名称/功能权限/操作说明三列、独立特殊权限区和取消/重置/保存并返回/保存操作条。900px 首轮复查发现旧媒体规则把标题与表单拆成左右两列，已以限定覆盖修正并复查。未显示权限警告保留，因为它是现有授权数据的真实安全提示，不以视觉对齐为由隐藏。

## 3. 修改范围

- `frontend/mes-web/src/views/admin/RoleDetailView.vue`
- `frontend/mes-web/src/views/admin/RoleDetailView.test.ts`
- 本记录和 `MES_TASKS.md` 当前回归段

未新增迁移；未写数据库；未保存角色或权限；CUAWright 写请求保护保持开启。工作区其他未提交修改均保留。当前源码 SHA256：Vue `400CFA579653C3C6331447C2AAD88F13B50B306DFF64A85269C49D576E8DAFC4`，专项测试 `B6DABC1B52E1616BA41879B9D1D6056156BB46CFD9E026133B36C2A4884AA7D9`。

## 4. 验收证据

- 冻结原型参考：`C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791795800120_role-prototype-1440-before.png`、`1791796338459_role-prototype-375-reference.png`。
- 正式页修复前：`1791795851014_role-formal-edit-1440-before.png`。
- 正式页修复后：`1791796568133_role-formal-edit-1440-final.png`、`1791796442615_role-formal-edit-1280-after.png`、`1791796497920_role-formal-edit-900-recheck.png`、`1791796326879_role-formal-edit-375-after.png`。
- 1440/1280/900/375 均为真实 5173 页面和真实 8080 读取；375/900/1280/1440 页面宽度均无横向溢出。标签与控件同行检查在四个视口均 PASS，三组中心线差值均为 0px。
- `npm run typecheck`：exit 0。
- `npm run test -- --run src/views/admin/RoleDetailView.test.ts`：11/11 PASS，0 失败/跳过；新增断言覆盖三列表头和“保存并返回”。
- `git diff --check -- frontend/mes-web/src/views/admin/RoleDetailView.vue frontend/mes-web/src/views/admin/RoleDetailView.test.ts`：exit 0，仅既有 CRLF 提示。
- `Codex SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max`：完成冻结稿/正式页视觉对照、四视口原图检查、表单同行 DOM 测量和一次对抗式复查；对抗复查发现并关闭 900px 标题/表单错列。完整状态矩阵、键盘/屏幕阅读器、服务失败/成功写入和正式 G5 manifest 本轮未执行，因此不声明 `READY FOR ACCEPTANCE`。

## 5. 结论

**TARGETED VISUAL REGRESSION FIX PASS / IN PROGRESS / HUMAN RECHECK REQUIRED / NOT NEWLY ACCEPTED。**

原型要求的主要视觉结构已迁入正式 T2 页面，现有角色权限业务逻辑和安全契约保持。浏览器控制台仍记录一次既有 401 资源请求；本轮页面数据均正常读取，但该控制台项未定因。正式 G5 完整审查清单、真实写入与非管理员正反例不在本轮目标范围，不能据此宣称全系统或重新验收通过。
