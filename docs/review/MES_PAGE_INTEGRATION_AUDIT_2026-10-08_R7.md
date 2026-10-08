# MES 页面 / GxP 审查 R7 — QA 独立性、eBR 归档与规格一致性

Date: 2026-10-08. Local `main`, FINAL BASELINE COMPLETE v1.0.23 / UI V2. No commit or push.

## 变更与真实问题

**R7-01 — QA 审核模型错误暴露不可执行动作，已修复**

- 已放行演示批 `20261007-KCL-001`：真实 QA 签署人登录后，原 `GET /api/v1/qa/batches/16/review-model` 返回 `allowedActions=["RELEASE","REJECT"]`，但提交服务 `FinishedReleaseService.decide()` 已根据有效前序决定执行 `QA_INDEPENDENT_REQUIRED`，UI 亦隐藏同一签署人的再次操作。属于服务端只读动作提示与实际业务保护不一致，**不是已证实的实际越权放行**。
- TDD 回归 `FinishedReleaseIT.reviewActionsHonorIndependentQaSupersession`：修改前按预期失败（Expecting empty but was RELEASE/REJECT）；修改后 1/1 PASS。
- 最小修复 `backend/mes-release/.../FinishedReleaseService.java`：生成 `allowedActions` 前读取当前有效决定；当操作者就是其签署人时不提供 RELEASE/REJECT。独立有资格 QA 仍可看到合法操作。不更改签名、命令、权限码、领域状态或已签署历史。
- QA 真实浏览器回归脚本 `qa-role-release-readonly.tmp.spec.ts` 同步校验：`demo.qa` 原签署人 `allowedActions=[]`；管理员 `admin` 因无个人 QA 签署资格也为 `[]`；前端仍不出现同一签署人再次签名按钮。

## 本轮验证

- **Native rollback-only**：精选 `FinishedReleaseIT` 3 项（含独立改判、资格失效阻断和新增 read-model 一致性）+ `EbrArchiveIT` 2 项（最终归档前置 Gate、失效签名、哈希与历史不可变）：**5 PASS, 0 FAIL, 0 ERROR**。Maven exit 0；使用 `test-compile failsafe:integration-test failsafe:verify`，避免在后台运行 JAR 时打包。测试 fixture 继承 `IncomingQualityFixture` 的 `@Transactional` 回滚。
- 停止目标 8080 后端 PID，仅打包同一项目；预先备份旧可执行 JAR 到本地 Temp；`mvn -DskipTests package -q` PASS，生成完整可执行包约 81.86 MB；Java 21 / Spring Boot 后端 PID 1968 已启动，Flyway 32 migrations VALID，版本 V032，原数据不重建。
- **PC 浏览器**：`qa-role-release-readonly`、`ebr-archive-postdecision-review`、`business-consistency` 共 **3 PASS**，没有对业务作写操作。
- **PC + Mobile 受影响综合回归**：8 套脚本，共 **16 PASS / 0 FAIL**；涉及岗位 GET/POST 隔离、质量计划批准入口、成品收发闭环、QA 放行详情、OOS 阻断、批次 QA / 物料平衡稳定性，退出码 0。
- eBR 归档只读专项 PC/Mobile：**2 PASS**；同一生产批只有 1 条历史 QA 决定、0 条 PDF manifests（其中 FINAL 为 0）；页面“待归档”与后端事实一致，多次刷新 `recordDigest` 不变。PDF 是现行批准模型中的 **QA 决定后的独立归档步骤**，并非放行前 Gate。

本轮环境日志：
`C:/Users/Administrator/AppData/Local/Temp/mes-r7-qa-action-it.log`（先红）；
`mes-r7-qa-action-it-after.log`（1 PASS）；
`mes-r7-release-archive-regression.log`（5 PASS）；
`mes-r7-qa-role.log`（2 PASS）；
`mes-r7-ebr-read.log`（2 PASS）；
`mes-r7-browser-after-deploy.log`（3 PASS）；
`mes-r7-full-browser-regression.log`（16 PASS）；
`mes-r7-package.log`（exit 0）。

## 仍待受控处理的真实 GxP 问题

1. **HIGH / 未关闭**：现有已放行演示批标示 `30ml:3g/瓶` → 10 g/100 mL，冻结处方 `20 g / 1000 mL` → 2 g/100 mL（如基准是最终体积）；表观相差五倍。只读计算返回 `consistent:false`，但最终浓度定义/定容需药学批准；见 `DCP-FINISHED-STRENGTH-GATE-001-PROPOSED.md`。已写建议设计范围和负向测试，尚未获 Design Authority 授权。
2. **岗位责任证据**：历史成品检验人/报告编制人显示“李倩（仓储）”，发货确认人为“赵宁（QA）”，生产执行操作人也曾为仓储人员。不得凭角色标签直接断定无资格；后续按真实人员资格、授权和历史审计受控调查，不能伪改签名/人员。
3. **运营待办**：最终 eBR PDF 尚待按有效 QA 决定执行归档；因现有批次规格一致性疑点，**未擅自生成 FINAL PDF**。
4. **设计候选**：`DCP-QA-PLAN-QUALIFICATION-001-PROPOSED.md` 质量计划批准资格 Gate 尚未批准，不隐性加入新的权限/GxP 规则。

## 安全边界

- 本轮仅修改 **QA 已有业务约束的只读 allowedActions 实现**、对应后端事务回归和浏览器测试；未新增/修改 DB schema、API 字段、状态、QA 决定、电子签名、库存流水，未执行持久性 Seed。
- 过去接受的业务与视觉基线保持不变。未提交/推送至 Git；已有其它 working-tree 修改保留。
- 硬性设计变更保持 PROPOSED，不能因 5/5、16/16 或 3/3 PASS 宣称全系统 GxP 最终合规验收。
