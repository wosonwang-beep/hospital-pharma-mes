# ROLE-ID / 电子签名验收 — 2026-10-12

状态：**READY FOR ACCEPTANCE / CORE FUNCTION + SECURITY + FORMAL UI G5 PASS / NOT HUMAN ACCEPTED**

## 范围
- ROLE-ID 冻结设计：`ROLE_ID_SIGNING_DESIGN_LOCK_20261011.md`。
- 电子签名：角色 ID 绑定、当前签名权限、二次认证、一次性令牌、签名事务。
- 真实 5173 / 8080 / 本机 Redis 6379。

## ROLE-ID UI 与权限
- 真实页面：`/ebr/templates/291/designer` → “规则与签名”。
- 1440 / 1280 / 900 / 375 四视口真实截图完成。
- 14 组角色标签/选择器均存在；标签与控件同行，实测垂直偏差约 1.75px。
- 四视口 document 均无页面级横向溢出。
- 角色显示中文名称 + 稳定角色编码，例如 QC复核岗位 / QA岗位。
- 管理员 `/ebr/role-options`：真实 HTTP 200。
- 现有非管理员真实登录成功、确认无 `ebr:template:view` 后访问 `/ebr/role-options`：真实 HTTP **403 FORBIDDEN**。此前“nonadmin 403 NOT RUN”缺口已关闭。
- RolePicker：8/8 PASS；本次前端定向运行总计 57/57 PASS。

## ROLE-ID 后端
- `EbrRoleSchemaTest`：12/12 PASS（含 requiredRoleId、禁止 code fallback、冻结 snapshot 等）。
- `RoleIdSessionCompatibilityTest`：3/3 PASS。
- `CurrentSigningAuthorityTest`：6/6 PASS。

## 电子签名 / 二次认证
- Signature API / canonical / transaction：13/13 PASS。
- Reauthentication contract/error：2/2 PASS。
- 真实本机 Redis 二次认证：11/11 PASS：
  - 5 分钟 TTL；
  - 一次性消费；
  - binding mismatch 失败并烧毁 token；
  - 过期拒绝；
  - 8 并发消费者严格 1 个成功；
  - 非法 method 不写 Redis。
- 8080 health：HTTP 200。

## 旧 guarded deployment harness 说明
本轮尝试重跑 `EbrRoleIdApprovedDeploymentIT` 时无法启动测试 ApplicationContext，原因是测试硬编码 `server.port=8080`，而正式 backend 正在真实占用 8080。失败发生在端口绑定阶段、未进入任何业务断言，因此**不是产品回归失败**。历史 one-shot deployment guard PASS 证据保留，本轮不停止正式 backend 仅为了重复该一次性部署 harness。

## Formal UI G5 复验
- Global UI V2 控件边框已使用独立 token `#8895a5`，对白色约 **3.048:1**，WCAG 2.2 1.4.11 PASS。
- ROLE-ID 角色目录新增 `role=status + aria-live=polite`，真实页面可播报“正在加载角色目录 / 已加载 9 个可用角色 / 加载失败”。
- 1440 / 1280 / 900 / 375 四视口重新采集：14 组标签/选择器继续同行，垂直差约 1.75px，无页面级横向溢出。
- ui-ux-pro-max 8 组查询、真实正反权限、专项测试均已留证。
- manifest validator：**PASS，verdict=READY FOR ACCEPTANCE**。
- Evidence: `docs/review/evidence/role-id-acceptance-20261012/audit-manifest.json`

## 结论
ROLE-ID 与电子签名的核心业务、安全、权限隔离、二次认证、Redis 一次性令牌、非管理员 403、四视口 UI 与 Formal G5 均通过。当前状态为 **READY FOR ACCEPTANCE / NOT HUMAN ACCEPTED**。
