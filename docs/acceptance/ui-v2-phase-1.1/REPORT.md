# GLOBAL UI V2 PHASE 1.1 REPORT

日期：2026-10-05。状态：**READY FOR ACCEPTANCE**。范围仅 FIX-01..04；Phase 2 **NOT STARTED**。

## Fix Results

| Fix | Result | 验证结论 |
| --- | --- | --- |
| FIX-01 | FIXED | Desktop Material Query Card 实测 **120px**；条件、重置、查询紧凑同行，操作在右侧。空表格区域 **111.5px**。未给整个查询区设置固定高度；窄屏允许自然增高。Mobile 单列实测 256px，标签与控件同行。 |
| FIX-02 | FIXED | T2 实际 form 内容 max-width **1100px**；1680px 宽屏实际 form 宽度 **1100px**，Card 仍铺满内容区。桌面两列，备注全行。手机一列、标签 **95px**，UTC 长标签合理换行，所有控件起点一致。字段顺序未改变。保留原有底部操作区，未引入 Sticky Action 重构。 |
| FIX-03 | FIXED | Evidence Drawer 标题为“电子批记录表单与原始修订”；五个分区为基本信息、原始记录与修订、复核记录、规则执行记录、电子签名记录。递归字段显示中文，空状态为“暂无记录”，ID/修订作为元数据。原数据与修订保留；未知扩展字段保留值并使用“补充信息”，不直接展示 JSON 属性名。 |
| FIX-04 | FIXED | **FINAL PDF 是有效 QA 质量决定后的归档步骤，不是放行前 Gate**。进度/Checklist 仅渲染服务器 Gate，本例 6/6。PDF 独立显示于“QA 决定后归档步骤”，说明“不计入放行前 Gate”；不再伪装为第七个失败 Gate。 |

## Contract Conclusion

已读取 AGENTS.md、Global UI Design System V2、Page Template Standard V2、当前基线指针、批准 DCP-MES-012-013-CONTRACT-001 和 v1.0.16 Completion Contract §5。正式规定：`Final archive is allowed only after a valid finished decision`；PENDING_QA 只能产生 REVIEW_COPY，不能满足 FINAL archive。现有 FinishedReleaseService 返回六类正式 Gate，不含 PDF。

引用：[批准 DCP](../../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/00_DESIGN_CHANGE_DCP-MES-012-013-CONTRACT-001_APPROVED.md)、[完成契约 §5](../../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md)。只改变表现层，不改变正式 QA/签名/归档规则。没有硬编码六个 Gate；计数和百分比仍来自 review-model。

模板：Material List 为 **T1**，Material Create/Edit 为 **T2**，QA Decision 为 **T6**。没有创建 T7。

## Modified Files

实现：

- `frontend/mes-web/src/global-ui-v2.css`：仅物料页密度/宽度、QA 抽屉可读性选择器。
- `frontend/mes-web/src/views/master/MasterListView.vue`：T1 声明、物料专用样式入口。
- `frontend/mes-web/src/views/master/MaterialDetailView.vue`：T2 声明、创建/编辑专用样式入口。
- `frontend/mes-web/src/views/quality/FinishedReleaseView.vue`：T6 声明、PDF 独立归档区域、QA 中文标签入口。
- `frontend/mes-web/src/views/quality/IncomingFacts.vue`：可选纯显示 formatter，向递归子节点传递；其他使用者默认行为不变。
- `frontend/mes-web/src/views/quality/qaEvidencePresentation.ts`：QA 中文显示标签。

测试：

- `frontend/mes-web/src/views/quality/QaEvidenceFacts.test.ts`：原始/修订内容与中文分区、无数据改写、共享 renderer 原行为。
- `frontend/mes-web/e2e/global-ui-v2-refinement.spec.ts`：新截图、尺寸、对齐、中文、真实 Gate 语义与错误检查。
- `frontend/mes-web/e2e/global-ui-v2-phase1.spec.ts`、`frontend/mes-web/e2e/mes013-release.spec.ts`：Gate 数量断言按正式六 Gate 修正，归档不计入 Gate。

记录：本报告、同目录 `screens/before` / `screens/after`、`docs/superpowers/plans/2026-10-05-global-ui-v2-phase-1.1.md`、`MES_TASKS.md`。

## Screenshot Paths

本次用现有 Playwright Chromium 捕获新的 Before/After；不是复用旧截图。普通页面 fullPage，抽屉为视口截图。必需的八张最终截图如下，均已打开检查。

| Step | 页面 / 健康状况 | Desktop | Mobile |
| --- | --- | --- | --- |
| 1 | T1 Material Query — PASS，密度与分页/查询保留 | [截图](screens/after/t1-material-query-chromium-desktop.png) | [截图](screens/after/t1-material-query-chromium-mobile.png) |
| 2 | T2 Material Create — PASS，同行与宽度可读 | [截图](screens/after/t2-material-form-chromium-desktop.png) | [截图](screens/after/t2-material-form-chromium-mobile.png) |
| 3 | T6 QA Review — PASS，Gate 与决定后归档分离 | [截图](screens/after/t6-qa-review-chromium-desktop.png) | [截图](screens/after/t6-qa-review-chromium-mobile.png) |
| 4 | Evidence Drawer — PASS，中文业务表达与原始值可读 | [截图](screens/after/evidence-drawer-chromium-desktop.png) | [截图](screens/after/evidence-drawer-chromium-mobile.png) |

补充：[1680px 宽屏表单](screens/after/t2-material-form-wide-chromium-desktop.png)。其他截图包含侧栏折叠和签名弹窗。Before 对应同名文件位于 `screens/before/`，本轮 Phase 1 原验收截图未改写。

## Validation

- **Desktop Validation: PASS**。1280×720 + form 宽屏 1680×900；查询区 120px，空行 111.5px，form max/实宽 1100px；两列、长文本全行、Gate 6/6、独立归档、中文证据、抽屉关闭、签名打开/取消和查询/重置。
- **Mobile Validation: PASS**。Pixel 5 393×727；查询单列、label 95px、UTC 换行、控件齐头、QA/抽屉中文可读。宽表格保持受控横向滚动，页面无横向溢出。
- **Typecheck: PASS**，`npm run typecheck`。
- **Tests: PASS**，`npx vitest run src/views/quality/QaEvidenceFacts.test.ts src/views/quality/finishedReleaseModel.test.ts src/master/material.test.ts src/master/resources.test.ts`，4 files / **15 tests**，4.17s。
- **Targeted Playwright: PASS**，`npx playwright test e2e/global-ui-v2-refinement.spec.ts e2e/material-layout.spec.ts e2e/mes013-release.spec.ts --workers=2`，**24 tests**，49.9s；新增检查记录 pageerror / console error 均为零。发布前 Before 截图独立捕获，4 cases PASS；不计入最终 24。
- **Build: PASS**，`npm run build`，3399 modules，Vite 8.96s。
- **Review: PASS**，针对四项问题的截图与 diff 自查。逐一比较 Material List/Create、QA 的 `<script>`：除 QA 两个纯视觉 import 外，业务逻辑完全未变。API/finishedReleaseModel/冻结 release 目录无 diff。共享证据 renderer 原过滤策略、原始值和状态样式不变。

测试中的首轮 typecheck 发现 Testing Library 的 role matcher 不支持 `exact` 选项；仅修正测试选项后 typecheck 和测试通过。没有产品行为失败或放宽业务断言。

浏览器实际渲染 Vue，API 使用测试响应。本轮验证不声称 native MariaDB 集成或完整生产运行验收；未执行本机业务写入或全量回归。截图无法单独证明完整无障碍合规，未作此声明。

## Invariants / Remaining Issues

- **Business Contract Changed: NO**
- **Database Changed: NO**，无 migration、数据库操作或 backend 修改。
- **FINAL BASELINE Changed: NO**
- API / DTO / permissions / allowedActions / state machine / signature / QA Release rules 均未改变。
- **Remaining CRITICAL/HIGH UI Issues: 0（本次四项范围）**。
- 剩余 LOW：继承的主 bundle 大于 500kB 的 Vite 提示，约 1.66MB / gzip 513.38kB；本轮未做无关拆包。
- 本机数据库配置为用户已有改动，保留且未纳入本轮。未提交/推送。

已完成四项修正和截图检查，READY FOR ACCEPTANCE；未自动 ACCEPTED，未进入 Phase 2 或新 MES Task。STOP。
