# MES 真实集成审查 R9 — 预先资格、独立岗位与新批次放行归档

日期：2026-10-08。本地 main 工作区；业务权限基线 FINAL v1.0.23，UI V2/T1–T6。所有受控历史、DB/Flyway、已签名 QA 决定保留。**未提交/推送。**

## 背景与范围

R8 发现已放行演示批 20261007-KCL-001 的部分资格登记创建时间晚于原始受控操作/QA 签署，且标示强度与冻结处方表观 5 倍不一致；**不在旧历史上补资格、改签或重做放行**。

以前用于历史造数的 `RealisticProductionFinishedSeedIT` 含 `@Commit` 并将通用 `MES_DEMO_GMP` 配置给多个专属岗位 Gate，绝不能直接重新运行。R9 新增独立测试：
`backend/mes-boot/src/test/java/com/hospital/mes/production/RoleSeparatedFreshBatchScenario.java`。

## 已执行的真实事务集成验证

- 在 CI 事务测试内为全新临时人员建立不同资格映射 `R9_PRODUCTION`、`R9_SAMPLING`、`R9_QC_EXECUTE`、`R9_QC_REVIEW`、`R9_QA_RELEASE`，均通过真实 `QualificationService.create` 在业务动作前生成，按原服务 `requireQualified` 验证。
- 用不同 `CurrentPlatformContext` 操作身份经过完整 Service/数据库流程：来料收货检验与 QA 材料放行 → 生产订单/母批/受控工艺/eBR → 生产投料/称量及独立核验 → 生产 QC 取样/结果/独立复核 → 成品仓储独立接收 → 成品 QC 取样/结果/报告/独立审核 → QA 成品放行 → FINAL eBR PDF 归档。
- 生产、仓储、取样、QC 检验、QC 复核、QA 使用**不同身份**；继承的来料夹具中，QC 检验人员另有取样资质，来料最终检验报告由单独合资格的审核身份批准（所涉身份资格真实检查，非所有人员都用单一通用码）。
- 负向验证：仓储身份不具 QC 执行、QA 放行资格；以仓储身份直接调用已有 QA 决定命令，被 `QUALIFICATION_REQUIRED` 拒绝；**没有**留下 QA 决定、新签名或批次状态变化。
- 正向验证：QA 操作者资格记录的数据库 `created_at` **早于**其真实 `decision_at`，放行签名校验成功，库存状态确实变为 RELEASED；生成的 FINAL eBR PDF 绑定当前决定、签名、记录摘要，附件 PDF 二进制文件 SHA-256 与清单哈希一致。
- **真正回滚证据**：使用 Spring `@AfterTransaction`（事务结束后、独立连接）验证新批次、`ebr_pdf_manifest`、临时 QA 用户、其 R9 资格全为 **0 条残留**。最后一次测试批次内标识 47，QA 11728；不是持久业务数据。
- Maven CI 测试：**RoleSeparatedFreshBatchScenario 1/1 PASS，0 FAIL/ERROR，exit 0**；日志 `C:/Users/Administrator/AppData/Local/Temp/mes-r9-negative-qa-fresh-it.log` 有 `R9_FRESH_ROLES_BATCH`、`R9_ROLLBACK_CONFIRMED`。

## 直接受影响的既有保护回归

Maven CI 事务测试 3 项全部通过：
- `FinishedReleaseIT.revokedQaQualificationBlocksReleaseDespiteRetainedPermission`：资格失效，纵有权限亦拒绝放行。
- `EbrArchiveIT.preQaFinalAndInvalidatedFinalSignatureAreBlockedWithoutFiles`：没有有效 QA 决定或失效签名，不得生成 FINAL PDF。
- `MasterResourcesIT.tcQual001MissingExpiredInclusiveAndWrongOrganization`：缺失、过期及跨组织资格拒绝。

整组 **3/3 PASS, 0 FAIL/ERROR，exit 0**，日志 `mes-r9-qualification-archive-regression.log`。连同新链路共 4 项新的/回归 native 测试通过（不是 4 套持久生产批次）。

## 范围与遗留项

- 测试中调用真实 Spring Service、MariaDB、签名/档案服务，并使用独立测试身份；这是**回滚事务集成测试**，不是逐个真实浏览器登录 6 个生产人员，也不是 PROD 或实际放行合规认证。原七岗位真实登录/HTTP 权限测试另有 Playwright 回归。
- 严格保留已知待处理：历史批次冻结处方和标示规格表观 5 倍差异；历史人员资格后补时间线；QA 质量计划批准专属资格 Gate DCP 尚未批准；原历史 FINAL eBR PDF 未归档。
- 此轮不改业务服务/冻结合同、数据库/迁移/审计/签名/历史。只新增回滚 CI 集成测试和本审查记录。

## 真实角色登录与页面集成复测（补充）

- Playwright PC + 手机端共 3 套脚本、6 个测试：**6/6 PASS，0 FAIL，exit 0**。范围：7 个真实演示账号跨岗位 GET 权限应答 200/403、生产角色不可批准质量计划、QA 角色可看到合法批准操作，以及历史 eBR 归档的真实只读事实（已放行示例批仍有 1 条 QA 决定、0 条最终 PDF）。
- 本机日志：`C:/Users/Administrator/AppData/Local/Temp/mes-r9-browser-regression.log`，结果为 `6 passed (54.1s)` / `EXIT_CODE=0`。
- 这些账号的 HTTP 权限验证与 R9 全新业务批次的 Java CI 事务身份验证为两种互补证据，不把 mocked test context 误写为实际 6 个账号的浏览器完整写入操作。

## R9 最终验证收口

- 加强 `@AfterTransaction` 校验：除正式生产批、PDF manifest、用户和资格之外，连真实生成的 `gxp_attachment` 文件记录也必须归零。最后一次完整角色分离链测试 1/1 PASS，日志 `mes-r9-final-role-rollback-it.log` 输出 `R9_FRESH_ROLES_BATCH=50` 与 `R9_ROLLBACK_CONFIRMED batch=50`，且 Maven exit 0。**这些 ID 是临时测试数据、已经回滚。**
- 负向 QA Service 命令：仓储角色直接使用合法批次和已生成的 QA 审核摘要提交放行，被 `QUALIFICATION_REQUIRED` 拒绝；决策行数、电子签名数量和 `PENDING_QA` 状态保持不变。然后合格 QA 在同一回滚测试事务中成功放行并生成 FINAL PDF，文件哈希通过。
- 后端安全/归档邻近回归 `FinishedReleaseIT`、`EbrArchiveIT`、`MasterResourcesIT` 各 1 项，总计 3/3 PASS（资格失效阻断、无有效 QA 签名不得 FINAL 归档、资格缺失/过期/跨组织），日志 `mes-r9-qualification-archive-regression.log`，exit 0。
- PC/Mobile 真实浏览器 3 套用例 6/6 PASS，涵盖七岗位权限隔离、质量计划 UI、已放行批 eBR 归档只读；exit 0。
- **R9 本轮已确认 4 项 Native（含新建 1 项）+ 6 项浏览器通过。** 没有重新运行历史 `@Commit` Seed，也没有产生持久的模拟最终放行/最终 PDF。

## 新增 HIGH 发现：生产 QC 结果 Service 资格 Gate 缺口

- 只读源审查与单次回滚诊断验证：`ProductionQualityService.result()` 在拥有 `qms:test:record` 和 `ebr:sign` 模拟权限、却没有 `R9_QC_EXECUTE` 个人资格的仓储测试身份下，允许写入并签署临时生产检验结果；独立资格检查本身则明确报 `QUALIFICATION_REQUIRED`。由此证明该 Service 缺少强制个人 QC 资格 Gate。日志 `mes-r9-production-qc-qualification-probe.log` 为实际结果（单测运行通过，Maven exit 0），由事务回滚，**未影响原历史检验**。
- 这与此前通过的七账户实际登录 RBAC 检查不矛盾：仓储账户实际无 QC 权限，无法通过 HTTP；但具有 QC 权限的用户不能仅因有权限就被视为通过个人资质检查。
- 临时诊断方法已经从源测试类移除，防止将已知异常行为固化为 CI 通过标准；保留独立受控提案 `docs/review/DCP-PRODUCTION-QC-QUALIFICATION-001-PROPOSED.md`，**PROPOSED / NOT AUTHORIZED**。不得在未获设计权威批准前悄悄新增冻结 GxP 规则，后续必须按 DCP 修复并用期待拒绝的负向测试证明。

## CI 运行入口收口

- R9 临时角色分离场景已命名为 `RoleSeparatedFreshBatchScenario.java`（非 Failsafe 默认 `*IT` 后缀），避免从 `FinishedReleaseIT` 继承的现有历史测试被全量 CI 误以新的 R9 资格映射再次重复执行。原有 `FinishedReleaseIT`、`EbrArchiveIT` 等默认回归不改变。
- 专项测试显式运行命令（在仓库根目录）：`mvn.cmd -pl backend/mes-boot -am -Pci-integration '-Dit.test=RoleSeparatedFreshBatchScenario#freshRoleSplitQualifiedChain' '-Dfailsafe.failIfNoSpecifiedTests=false' test-compile failsafe:integration-test failsafe:verify`。
- 最后一次显式运行 `RoleSeparatedFreshBatchScenario` 1/1 PASS，后事务证明确认批次 52、QA actor 11760、PDF manifest/attachment/资格均无残留；本机日志 `mes-r9-explicit-scenario-final.log`，`EXIT_CODE=0`。
- 诊断未认证生产 QC 结果的临时 probe 在留存日志后已移除，默认测试与专项正向测试不包含已知异常行为的“接受为正确”的断言。
