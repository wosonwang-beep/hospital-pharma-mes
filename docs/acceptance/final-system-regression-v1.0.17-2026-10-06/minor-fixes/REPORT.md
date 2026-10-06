# v1.0.17 最终回归 — 授权小问题修复

授权依据：用户于 2026-10-06 指示“小问题授权修复”。本轮限于明显实现 Bug、测试事务边界、fixture 选择与过期测试断言。前轮禁止新增 Permission/Migration、修改 Frozen Contract 的要求继续适用。

## 结果

**已完成本轮可在冻结合同内处理的修复；全系统最终验收仍未通过。** 最终受影响原生集成集合 27 项：24 PASS、3 FAIL；三项失败来自两个既有权限代码在当前 sys_permission 中未注册，不掩盖失败，也不把角色能力探测替代正式角色 UAT。

| 验证 | 结果 |
|---|---|
| FinishedReleaseIT 放行正负向、HTTP、签名、原子性、审计/outbox 回滚 | 10 PASS |
| EbrArchiveIT QA 后 FINAL、不可变摘要、文件/manifest 回滚与读取授权 | 4 PASS |
| 最后管理员权限测试 | 1 PASS；不再排除，整体回滚 |
| AuthFilterIT | 3 PASS |
| 九类能力身份探测 | 6 PASS、3 FAIL（两个缺失权限；只读/操作员重复同一权限） |
| mes-release 及依赖 reactor 单元测试 | 163 PASS；32.873 秒 |
| 前端单元测试 | 19 文件、72 PASS |
| 前端 typecheck / build | PASS；build 15.81 秒 |
| QA Desktop/Mobile E2E | 20 PASS；签名载荷、hash 下载校验和原始历史断言保留 |
| 受影响引用/来料/IAM Desktop/Mobile E2E | 20 PASS |
| 技术证据展开追加检查 | 2 PASS；属于上述 QA 用例的增强重跑，不重复累加用例数 |
| 旧 Compose / verification script 断言 | 6 PASS |
| 必需 Repository / CI / CI negative gate | PASS |
| 独立复核 | 两项复核发现已修复；最终无新增 CRITICAL/HIGH/MEDIUM |
| 显式 Flyway validate（测后） | PASS；V001～V026 SUCCESS；SYSTEM_ADMIN 仍为 106 项权限 |

本轮只执行受影响回归；此前完整浏览器 193 PASS / 3 PC-only SKIP 及 496/497 项原生历史结果保留在父报告中，不将历史全量与本轮子集拼接成一次全量 PASS。

## 实现修复

1. **QA T6 默认展示**：产品/单位缺少读取能力时显示明确不可读取提示；成品批显示可读 lotNo 或顺序关联标签，原 ID 仍用作命令值。归档生成者按现有用户读取授权解析名称；默认表格不显示 hash，摘要与原始内部引用完整保存在主动展开的技术证据区。签名提示不显示 versionNo，实际版本/摘要/签名载荷不变。前序决定从既有决定列表选择，显示时间及决定结果，提交仍为原 ID。T6 页面结构、六 Gate、QA 后归档顺序均不变。
2. **人员只读引用**：修正到既有 /admin/users/{id}，使用既有 menu:iam:users 权限；新增的可选 fallback 只在 QA 使用。没有新增权限、接口或放宽授权。
3. **QA 阻断读取 500 根因**：已冻结工艺但未满足 eBR/Balance 的批次，预期 ComplianceException 被捕获后，子服务仍标记外层事务 rollback-only，导致 UnexpectedRollbackException。现在将整组 eBR 检查及 Balance 检查放在 savepoint：预期拒绝回滚检查局部变化并保留 blockingCodes，未知异常仍传播且整请求回滚。QA 读取已验证 200、六个 Gate、非空阻断码、空 allowedActions；没有放宽放行条件。

## 测试修复与数据库保护

- 最后管理员方法保留原 409、关系数和 enabled 断言。外层测试事务整体回滚，每个失败 HTTP 请求用 NESTED/savepoint 建立请求回滚边界；不手动强制 rollback 掩盖业务错误，也不提交共享 SYSTEM_ADMIN 权限变化。
- 放行 HTTP 用例原来在同一测试事务内先执行 genericSign 拒绝，再尝试成功放行；前次拒绝使整体 rollback-only，无法创建后续 savepoint。该负向请求改用既有 NESTED rejected helper，原 4xx、无决定、批状态、最终 201/schema 断言保留，并要求真实事务拒绝。
- QA probe 不再选择无工艺快照的草稿批；仅选已有 snapshot 批。角色 probe 加外层整体回滚，因为 GET Gate 检查可能同步数量事件和审计记录，不能以“GET + SELECT”推断零写入。Redis 会话始终 finally 撤销。
- 旧脚本测试现在验证 .env ignored 且不被 Git 跟踪，符合持久库政策；验证 verify.ps1 → verify-repository.ps1 → verify-compose.ps1 委托链，Docker UNAVAILABLE 断言保留。必需 CI 合同未削弱。
- 没有 reset/drop/rebuild/repair，没有新增或修改 Migration，没有删除历史 GxP 记录。没有改动用户本机配置。执行中产生的旧验收截图已复制为本轮证据后恢复历史文件。

## 尚未关闭

- **DEPENDENCY_NOT_READY：production:batch:view、mes:weigh:view 注册缺失。** 三项原生探测保留失败。补齐将涉及持久权限/迁移处理，超出沿用的禁止范围，本次不自行执行。
- 八类正式业务角色尚未配置，仍需正式角色的 Menu/Route/Action/API 授权矩阵验收；测试能力身份不能代替。
- MariaDB 13 超出当前 Flyway 声明的测试支持范围，以及前端 bundle size 提示，保留 LOW 技术债务；本轮未做版本升级或无关性能重构。

没有改变 FINAL BASELINE COMPLETE v1.0.17、Global UI V2 权威、MES ACCEPTED 状态；没有实施 UI V3 或四项未授权 DCP 扩展。没有提交或推送。当前工作树仍有失败的权限注册探测，不能标记为完整 CI/最终回归通过。

[逐用例原生结果](evidence/native-targeted.csv) / [Desktop QA](evidence/qa-refinement-chromium-desktop.png) / [Mobile QA](evidence/qa-refinement-chromium-mobile.png)。

后续仅针对权限注册缺失的授权修复已完成，原 3 项失败恢复通过，见 [权限注册修复记录](../permission-registration/REPORT.md)。本页保留对应运行时的历史结果。
