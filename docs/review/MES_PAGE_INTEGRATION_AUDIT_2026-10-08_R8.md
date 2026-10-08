# MES 真实审查 R8 — 当前 eBR PDF 归档状态与历史人员资质

日期：2026-10-08。工作区：`main`，FINAL BASELINE v1.0.23；保留所有已签名生产记录、QA 决定、库存流水及用户既有未提交修改。

## 修复：历史 FINAL PDF 不能冒充当前最终归档

- 发现：`FinishedReleaseView.vue` 的 `hasFinal` 原来只检查任一历史 `archiveKind=FINAL`，在 QA 决定被独立改判或现行证据摘要变化之后，仍可能错误展示“已归档”。
- 最小变更：使用 `finishedReleaseModel.ts` 的 `effectiveQaDecision`，按 `supersedesDecisionId` 构建有效决定，不依赖列表返回顺序；`hasCurrentFinalArchive` 必须同时匹配 `FINAL`、当前 `recordDigest`、有效 `releaseDecisionId`。
- `FinishedReleaseView.vue` 的 `latestDecision` 同样按有效决定计算，使人员独立性提示与后续决定选择不再误用乱序历史记录。
- 历史最终 PDF 仍显示在版本历史中并可沿用既有下载/哈希校验流程；**不删除、不改写旧 PDF，也不改后端冻结契约**。
- TDD：首轮新增归档状态测试红灯（缺少函数）；修复后绿灯。新增乱序 QA 决定用例首轮红灯，修复后绿灯。
- 单元：28 份前端测试文件，共 **104/104 PASS**；TypeScript + Vite 正式构建 **PASS**（既有 bundle 大小警告仍在）。
- 真实浏览器：`ebr-archive-current-state.tmp.spec.ts` 使用只读、客户端模拟的先旧版 FINAL、后新增当前 FINAL 的情境，PC/Mobile 正确从“待归档”变更为“已归档”；`ebr-archive-postdecision-review.tmp.spec.ts` 真实读取历史 QA 决定，确认当前生产批 1 项 QA 决定、0 项 FINAL PDF、证据摘要稳定。
- 合并复测 3 个脚本、PC/Mobile 共 **6/6 PASS**；先前有一次手机端资格导航脚本超时，已按移动菜单改写并重新跑完整 6 项至 0 失败。最终记录 `C:/Users/Administrator/AppData/Local/Temp/mes-r8-clean-e2e.log`（exit 0）。

## 历史资格时间线：新的 HIGH 演示证据风险

- 使用现有 admin、真实 `GET /api/v1/qualifications` 和批次 `GET /api/v1/main-batches/16/ebr` 关联；完全只读，PC/Mobile 均 PASS。
- 历史工序 `20261007-KCL-001` 于 `2026-10-07T15:02:47Z` 由操作者 `11565` 执行；目前其唯一列出的 `MES_DEMO_GMP` 资格 `createdAt=2026-10-07T22:24:23.247Z`，在工序执行之后。
- 历史 QA 决定的 `decisionBy=11569`，`decisionAt=2026-10-07T15:02:49.612Z`；此人的 `MES_DEMO_QA_RELEASE` 资格 `createdAt=2026-10-07T22:11:27.361Z`，也晚于该历史 QA 签署。
- 资格的 `validFrom=2026-09-07` 不能代替实际创建/批准的时点证据。当前本机配置对取样、QC 执行、QC 复核、QA 放行分别要求不同专属资质；通用 GMP 不能替代。
- 不能据此确定系统外培训/授权绝对不存在，但**当前数据库资格无法为历史演示签名当时的胜任能力背书**。同一事实已追加到 `MES_DEMO_ROLE_EVIDENCE_INCONSISTENCY_2026-10-08.md`。
- 需要质量调查与外部授权证据核对，禁止通过回填、重签、改 actorId 或改既有证据时间掩盖差异。真实演示应新建正确资格与责任分离链的独立新批次，而非对受控历史补造记录。

## 未关闭问题

1. 历史制剂标示 `30ml:3g` 与冻结处方 `20 g/1000 mL` 表观差 5 倍。待药学/质量设计审查，不擅自加入全新 QA Release Gate：`DCP-FINISHED-STRENGTH-GATE-001-PROPOSED.md`。
2. 历史已签 QA 决定所关联的人员资质登记晚于签署：HIGH，待受控处置。
3. 当前示例批 `20261007-KCL-001` 最终 eBR PDF 仍待归档；基于上述数据不一致，未擅自生成 FINAL。
4. `DCP-QA-PLAN-QUALIFICATION-001-PROPOSED.md` 未获设计授权。

本轮**未**修改数据库、Flyway、电子签名、原始实验结果、QA 决定、历史 PDF 或库存。未提交或推送 Git。
