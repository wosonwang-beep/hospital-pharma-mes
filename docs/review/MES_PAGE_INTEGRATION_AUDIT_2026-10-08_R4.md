# 医院制剂 MES — 真实集成审查（2026-10-08，R4：岗位隔离、质量计划）

**Scope:** 本机开发环境，real `demo.*` session、Spring Security、质量计划 T3 页面及 QA 独立批准。保持 FINAL BASELINE v1.0.23 与原受控业务数据不变。

## 1. 真实 RBAC 接口负向测试

Playwright 文件（本机未提交）：`frontend/mes-web/e2e/role-http-permission-matrix.tmp.spec.ts` 与 `role-post-denial.tmp.spec.ts`。这些 DEV-only 审查脚本已添加 `MES_LOCAL_ROLE_AUDIT=1` 显式运行门槛，防止普通 CI/全系统 Playwright 回归误用本机演示账号。`playwright.audit.reuse.tmp.config.ts` 复用本机现有 Vite `4174`，不需要额外启动服务。

已使用 `demo.production`、`demo.warehouse`、`demo.sampler`、`demo.qc.analyst`、`demo.qc.reviewer`、`demo.qa`、`demo.master` **分别独立真实登录**。

| 维度 | 结果 |
| --- | --- |
| 每个岗位允许的 GET 查询 | 7 / 7 为 HTTP 200 |
| 每个岗位不允许的跨岗位 GET 查询 | 14 / 14 为 HTTP 403 |
| 每个岗位不允许的 POST 控制动作 | 7 / 7 为 HTTP 403 |
| Playwright | 两个测试用例均 PASS |
| 数据影响 | 仅只读接口与携空载荷的禁止接口；所有 POST 均在 URL 授权层被拒绝，无受控业务写入 |

说明：第一次手写 `fetch` 未携带 Bearer token，得到 401，属**审查脚本问题**。已修为捕获当前登录 `/auth/me` 的真实 Bearer token 并通过相同认证机制请求，复测全绿。测试复用已有 `4174` Vite 服务器，不增开 Redis/Docker。

## 2. 生产质量计划的真实岗位审核

- 基于现有草稿计划 `qms_production_plan.id=565`（`mainBatchId=13`），创作者/最后修改者为 `demo.production`，审核对象未作任何变更。
- `demo.production`：允许读取质量计划，**不可见“批准”**。
- `demo.qa`：可以读取冻结质量标准名称，**可见“批准”**，没有提交审批/电子签名。
- 真实浏览器脚本 `quality-plan-role-ui.tmp.spec.ts` PASS。Ant Design 按钮可访问名称含字符间距，脚本改用 `/批\s*准/`，属于测试选择器问题。
- 发现生产岗位无 `qms:specification:view` 时，质量计划详情原展示 `来源引用 34`。仅在 `ProductionQualityDetail.vue` 为该字段设置权限感知的业务占位“已关联冻结质量标准”；**未授予生产岗位 QC 标准目录读取权限**，QA 有权限仍可看到完整名称。

## 3. 受控 GxP 设计变更候选（未授权）

`ProductionQualityPlanService.approve()` 已校验独立批准、已批准 QC 质量标准签名、受控电子签名、冻结哈希、版本和审计；但代码中**未显式核验该批准人的生产质量计划批准资格**（不同于最终 QA 放行的独立资质 Gate）。

证据与拟议处理另见：`docs/review/DCP-QA-PLAN-QUALIFICATION-001-PROPOSED.md`。

**不可未经授权修改当前状态机/GxP Gate**；应由 Design Authority 决定是复用 `qa-release` 资格还是独立 `qa-plan-approve`，并一次性同步基线正式文档及相关 API、Test Case、RTM。

## 4. 其它 HIGH 阻断仍保留

- `20261007-KCL-001` 的标示规格与已放行冻结处方浓度不一致（相差 5 倍），现行 Gate 不能视为完全业务合规。
- 历史演示数据中质量检验、仓储确认岗位与实际操作人有不一致，不能覆盖已签名的受控记录。

## 5. 交付边界

- 不改变数据库迁移、已冻结生产批、放行/拒绝决定、eBR 内容或历史签名。
- SYSTEM_ADMIN 仍具有系统全部功能权限，但不因此自动获得个人 QA/取样/QC 培训资格。
- 本轮仅新增审查脚本、报告及针对权限不足的页面展示修复；本机 `main` 工作区未提交未推送。
