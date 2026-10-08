# MES 页面与 GxP 审查 R10 — 生产 QC 独立复核展示及资格缺口跟踪

审查日期：2026-10-08；本地 main 工作区，FINAL BASELINE COMPLETE v1.0.23、Global UI V2、页面模板 T1–T6。**无冻结 GxP/API/DB/状态/签名合同变化，不提交不推送。**

## 本轮问题修复：生产检验页面错误显示可自行复核的操作

**发现**：`ProductionQualityDetail.vue` 在展示“独立复核”操作时，只检查服务端 `allowedActions=REVIEW` 与 RBAC 权限，没有像对应的 `ProductionQualityService.review` 提交规则那样检查 `currentResultRevisionId` 的 `recordedBy` 是否为当前登录人员。于是原始记录人可能看到最后必然被服务端拒绝的“独立复核”按钮，浪费操作并误导职责隔离。

**最小修复**：
- `frontend/mes-web/src/views/quality/productionQualityModel.ts`：增加只读显示帮助函数 `canPresentIndependentQualityReview`；先检查已有 `REVIEW` 动作，再根据**当前原始结果版本**的 `recordedBy` 与当前用户 ID 比较，缺失当前结果/身份时 fail closed。
- `frontend/mes-web/src/views/quality/ProductionQualityDetail.vue`：仅在原有权限、服务端允许动作基础上对 `review` 增加上述显示判断。其余操作、表单、T2/T3 布局不变。
- 不更改后端审核提交的 `INDEPENDENT_REVIEW_REQUIRED`、QC 原始/修订证据、签名，或已放行历史记录。

**先红后绿验证**：
- `productionQualityModel.test.ts` 加入同一记录的多版本结果、不同签署人、缺失数据、不被授权操作的案例。新增测试第一次因为函数缺失失败；修复后该文件 7/7 PASS。
- 全前端 Vitest **105/105 PASS**，运行日志 `C:/Users/Administrator/AppData/Local/Temp/mes-r10-front-unit.log`；`npm run build`（含 vue-tsc）成功，日志 `mes-r10-front-build.log`。仍有原有 Vite 大 chunk 非阻断警告。
- 后端原始检验结果／独立复核测试 `ProductionQualityIT.actualSignedPassRequiresIndependentReviewAndOriginalFailCannotBeCorrectedToPass` PASS，Maven exit 0，日志 `mes-r10-production-qc-native.log`。
- PC + Mobile Playwright 专项 `e2e/production-qc-independent-ui.tmp.spec.ts`：真实 `demo.qc.reviewer` 登录读取实际成品检验记录，测试侧仅对**GET 响应**模拟“当前版本为本人记录 / 他人记录”，验证独立复核按钮分别隐藏和显示；没有任何业务写操作。首次两端的测试脚本等待列表 URL 超时；修正为等待真实页面 heading 后再跑，**2/2 PASS，EXIT_CODE=0**。证据日志 `mes-r10-qc-self-review-e2e-fixed.log`。

## HIGH 质量风险：服务端 QC 个人资格 Gate 仍未补齐

R9 已用回滚 Spring Service+MariaDB 诊断证明：即使`IncomingActorAdapter.requireQualified(..., "test-execute")` 对仓储测试身份返回 `QUALIFICATION_REQUIRED`，只要模拟赋予 QC 操作权限及电子签名权限，`ProductionQualityService.result`仍可以记录并签署生产 QC 结果。属于**权限与个人资格双重校验缺失**。

- 现有真实仓储演示账号依然因 RBAC 无 QC 权限而返回 403；这不能替代对任何有 QC 权限的人员的个人资格检查。
- 冻结基线 `AGENTS.md` 要求改变 GxP 强制资格 Gate 前先获批准；本轮只修复已批准独立复核规则的**页面展示一致性**，不能用界面隐藏按钮代替服务端个人资格控制。
- 设计变更证据与详细负向验收矩阵：`docs/review/DCP-PRODUCTION-QC-QUALIFICATION-001-PROPOSED.md`，状态 **PROPOSED / NOT AUTHORIZED**，待批准后实施全链路签名/权限/质量文档同步，不回填历史资质。

## 其它未关闭受控问题

1. 已放行演示批 `20261007-KCL-001` 产品标示与冻结处方表观浓度相差五倍（需核实实际定容和结构化规格）；`DCP-FINISHED-STRENGTH-GATE-001-PROPOSED.md`。
2. 历史演示检验操作人与 QA 签署时点早于系统中专用资格记录创建时点。需保留原始证据和资质创建日志，核查系统外当时实际授权，不得回填历史。
3. 旧批最终 eBR PDF 仍待受控质量处置后生成；新回滚测试批已验证最终 PDF 生成、签名和 SHA-256 哈希。
4. QA 质量计划批准资格 Gate DCP 仍未批准。

## 工作区与运行服务

仅限现有 QC 页面及其模型测试的小范围改动；前后端原有运行服务、Redis 6379、MariaDB 3306 保持使用，未清空数据库、未重新运行任何 @Commit Seed、未更改原历史批次。

## R10 最终回归与环境收口

- PC/Mobile 9 套 Playwright 文件共 **18/18 PASS，0 FAIL，exit 0**（`mes-r10-pc-mobile-merged.log`，`18 passed (1.2m)`）。范围：7 岗位 GET/POST 200/403、质量计划岗位按钮、成品入库/库存/检验/报告/QA/发货出库、来料 OOS QA 阻断、生产批 QA + 物料平衡接口连续 200、QC 本人/他人独立复核展示。QC 角色浏览器临时模拟仅影响 GET 响应，未修改服务端 QC 结果或既有电子签名。
- 后端 `ProductionQualityIT` 原始结果、独立复核、权限/组织隔离、审计回滚及合法修订/OOS 三项 **3/3 PASS，0 FAIL/ERROR，Maven exit 0**；`mes-r10-qc-expanded-native.log`。
- 全量前端 `npm.cmd test -- --run` **105/105 PASS**，正式 `npm.cmd run build`（vue-tsc + Vite）PASS，只有历史 bundle 大小警告，详见 Temp 下 `mes-r10-front-unit.log` 和 `mes-r10-front-build.log`。
- 本轮仅现有 QC 页面提示的独立性一致性修复，不改变原始 QC `allowedActions` 后端权限合同；**生产 QC 服务端个人资格校验 HIGH 缺口仍保留为待批准 DCP**，不可误报为已修复。历史规格/人员资格/旧最终归档其它 HIGH 证据仍 OPEN。
