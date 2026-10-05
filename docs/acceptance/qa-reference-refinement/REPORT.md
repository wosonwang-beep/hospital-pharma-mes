# QA 批放行审核 — 图片参考调整

2026-10-05，READY FOR ACCEPTANCE。参考为本轮用户附图 `1-照片-1.jpg`，沿用 Global UI V2 和 T6；不进入 Phase 2。

## 修改

- 收紧页头、批次/Gate 横幅和 Checklist；左侧六项正式 Gate，右侧批次摘要与允许动作；下方 PDF 历史和证据目录并列。
- 统一卡片标题、通过标记、元数据、状态中文表达和证据类别图标。保持 V2 最小可读字体与手机单列。
- PDF 独立“QA 决定后归档步骤”移入 PDF 历史卡片，压缩不必要留白。正式 Gate 计数仍来自服务器。
- PDF 表格给时间、标签与操作留足列宽，保持受控横向滚动，避免挤压重叠。
- 证据抽屉保留 Phase 1.1 的中文分区和原始修订，签名/归档操作仍走原实现。

本轮实现文件：`frontend/mes-web/src/views/quality/FinishedReleaseView.vue`、`frontend/mes-web/src/global-ui-v2.css`。测试新增 `frontend/mes-web/e2e/qa-reference-refinement.spec.ts`，现有 `mes013-release.spec.ts` 将状态断言改为同一正式状态的中文呈现。

## 与图片的契约差异

图片示例把最终 PDF 当作第七个放行条件，并显示先归档才能 QA 放行。v1.0.16 / 批准 DCP §5 规定 FINAL 必须在有效 QA 决定后生成；决定前只有 REVIEW_COPY。本轮保持正确规则：示例六 Gate 6/6，PDF 为决定后步骤，不阻断本应允许的质量决定。

截图中的产品名称、规格、产品照片、批次时间和摘要编辑按钮没有在当前 QA read-model 中形成可用的对应展示/操作契约。本轮没有伪造这些信息、增加字段/API 或增加未授权编辑。全局导航/搜索/通知未纳入本轮 QA 页面修改。

## 验证与截图

- 独立 typecheck PASS；2 个相关前端测试文件 / 7 tests PASS。
- 18 项相关桌面/手机 Chromium 用例 PASS，34.1s，覆盖放行/签名/原始证据/归档/冲突/阻断/哈希校验。
- 状态中文和图标复查 6 PASS，20.2s；表格列宽最终复查 4 PASS，18.3s。仅重跑受影响用例，没有全量回归。
- 最终 build PASS，3399 modules，Vite 15.85s。继承 LOW：主 bundle 约1.66MB 的提示。
- 桌面/手机页面、抽屉和签名截图已检查，无页面横向溢出，定向视觉用例无 console/page error。API 使用测试响应，未执行 native DB 写入。
- 与 HEAD 对比，QA `<script>` 除纯展示 imports 外业务逻辑相同。数据库、migration、API、DTO、权限、allowedActions、签名、QA规则与 FINAL BASELINE 无变化。

| Step | 状况 | Desktop | Mobile |
| --- | --- | --- | --- |
| 1. QA 审核及历史 | PASS，紧凑 T6 双列/手机单列 | [截图](screens/qa-with-history-chromium-desktop.png) | [截图](screens/qa-with-history-chromium-mobile.png) |
| 2. 中文证据抽屉 | PASS，原始事实可读 | [截图](screens/evidence-drawer-chromium-desktop.png) | [截图](screens/evidence-drawer-chromium-mobile.png) |
| 3. 电子签名 | PASS，现有受控操作 | [截图](screens/signature-dialog-chromium-desktop.png) | [截图](screens/signature-dialog-chromium-mobile.png) |

本轮没有声称 1:1 复刻业务示例数据或完整无障碍认证。图片与实际页面差异受正式业务契约和 Global UI V2 可读字号约束。

Scoped review：未解决 CRITICAL/HIGH 0。先前 Phase 1.1 验证和截图保留为历史；本记录是 QA 表现层最新增量。其他修改及两份本机数据库配置保留，没有提交/推送，未自动 ACCEPTED，未启动新 MES 任务。


## 批次摘要边框增量 — 2026-10-05

用户明确仅调整批次摘要边框。global-ui-v2.css 使用浅色1px卡片边框及标题分隔线；去掉继承 table 单元格边线，内部每行只保留一条浅分隔线，最后一行无底线。无字段/业务行为变更，无迁移。桌面/手机两项 Chromium PASS，14.2s；最新截图为 screens/t6-qa-review-chromium-desktop.png 和 screens/t6-qa-review-chromium-mobile.png（历史表格样例截图仍保留为此前增量）。Scoped review 无未解决 HIGH。READY FOR ACCEPTANCE，未提交或推送。
