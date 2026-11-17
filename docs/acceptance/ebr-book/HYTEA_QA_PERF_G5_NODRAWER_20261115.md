# MES QA 性能、后端回归与无抽屉 G5 审查记录（2026-11-15）

状态：**QA 性能验收 PASS；后端/前端单元回归 PASS；QA 页面与 PDF 专项 PASS；全部受影响业务页面正式 G5 仍 IN PROGRESS**。执行者：ChatGPT SELF-REVIEW（非独立审查）。业务基线 v1.0.23、Global UI V2、T1–T6。仓库为本机唯一 main，DEV 数据库 hospital_pharma_mes_dev；没有重走生产批 655 流程、没有重新 QA 签名或修改归档。

## 1. QA 接口性能验收

同一个管理员登录会话对真实 `GET /api/v1/qa/batches/655/review-model` 连续发起 22 次请求，前 2 次预热、20 次计入统计，使用客户端高精度单请求耗时。P50=**914.205ms**、P95=**1030.66ms**（最近秩第19个）、最大=**1129.94ms**、最小=847.82ms；20/20 成功，0 业务阻断、0 MariaDB 1205、审核摘要唯一值 1。全部返回 QA_RELEASED 和 8 项质量门禁。**P95≤2000ms PASS。**

结合前轮 `QaReadOnlyReviewLockIT`：持有生产订单、主批、工序、库存、收货、检验行锁情况下 readOnly review 1537ms，在测试门槛 1800ms 内，门禁、阻断码、可用动作、摘要与有锁正式决策查询一致，数量事件数不增加。两项测试分别证明静态性能与并发非阻塞，不能用单次浏览器耗时替代 P95 数据。

本机原始结果：`private-data-audit/hytea-qa-readonly-g5-20261114/qa_real_http_p95_20261115.json`。DEV 合成业务检验/签名不能外推为真实药品检验合格。

## 2. 完整单元回归与真实页面测试

- 后端执行 `mvn -pl backend/mes-boot -am test -q`：100 套件、392 例、386 通过、6 跳过、0 失败、0 错误。包含消除 target 残留已删除审计测试；四个老契约测试修正均复用当前 V077、成品V2与 PrintSourceCatalog 真实接口，生产业务源码不变。修订说明：`docs/development/decisions/MES_BACKEND_REGRESSION_TEST_ALIGNMENT_20261115.md`。
- 前端完整 `npx vitest run --config vite.config.ts --maxWorkers=1`：61 文件、239/239 PASS，未提高原测试超时阈值。包含 `noDrawerUiContract.test.ts`。Vite `npm run build` PASS、Vue类型检查通过；已有 chunk 较大提示未当作错误隐瞒。
- QA 异常恢复模拟浏览器回归：`qa-archive-progressive.spec.ts` 6/6、`mes013-release.spec.ts` 10/10，合计 **16/16 PASS**。覆盖慢 QA/慢归档、请求失败重试、拒绝/过期状态、正式权限、已归档 PDF 哈希校验及错误文件拒绝；模拟请求不计入正式数据库业务动作验证。

## 3. 原生页面与 PDF G5 结果

真实 CUAWright/Chromium 使用管理员现有凭据登录本机5173，后端8080真实API。浏览器在1440、1280、900、375四个视口均实际返回批655的8项门禁、无阻断、两条归档历史、无根文档横向滚动。QA证据查看操作已改为居中 Modal，实测居中x坐标与宽度：(1440: x440/w560)、(1280:x360/w560)、(900:x170/w560)、(375:x12/w351)；每一视口 Esc 关闭成功并返回触发按钮焦点。0 JavaScript 页面异常、0业务写请求。启动会话时有3次/auth/me或/auth/refresh HTTP401，正式登录成功后不构成 QA 路由500故障。

真实网页“下载并校验”：原FINAL清单93/附件133=501399B，SHA256=`62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0`；原REVIEW_COPY清单94/附件134=1161328B，SHA256=`41e734d45cdf7f71901c5b9b5c133400e00460085282970b85388f7642d4b099`。两份与既有存档哈希一致，无重生成、无覆盖、无重签。

现场证据：`private-data-audit/hytea-qa-readonly-g5-20261114/g5_centered_modal_review/` 下的 `g5_real_browser_result.json`、`qa_archive_full_{1440,1280,900,375}.png`、`qa_centered_modal_{1440,1280,900,375}.png`、`archive_summary_focus_keyboard.png` 及两份下载文件。本次已人工查看四个模态框原图和QA页面1440/375原图；与原设计相比居中且操作清楚，无明显遮挡与横向裁切；不将机器DOM测量直接等同于视觉审核。

## 4. 禁止抽屉式操作的工程交付

按用户2026-11-15明确指令及冻结记录 `docs/development/decisions/MES_NO_DRAWER_INTERACTION_20261115_APPROVED.md`，8个Vue页面中的9处活动侧滑抽屉已改为居中Ant Design Vue Modal，保持原入口、权限、字段、保存与证据内容。涉及 eBR 模板、集成消息、批次总览、执行工作台、追溯、QA证据、检验单据及WMS收货质量。静态全src扫描0个活动 `<a-drawer>` / `<Drawer>`；自动防回归 `frontend/mes-web/src/layouts/noDrawerUiContract.test.ts` PASS；规则同步入 `AGENTS.md` 和 `docs/development/MES_DEV_RULES.md`，后续不允许新建抽屉式业务操作。

技术风险：已进入QA实浏览器验证的仅QA证据模态框；其余七个组件虽通过Vue生产构建/239项单测及静态门禁，尚未逐页完成完整视觉与所有状态人工复核。遗留旧技术代码如 `BookTemplateView` 是已退役路由的兼容源，不能把编译通过描述为真实业务页面验收通过。

## 5. 视觉九维检查结论（限QA批审详情 + 证据弹窗）

| 维度 | 状态 | 真实观察与证据边界 |
|---|---|---|
| layout | PASS（QA本页） | 4视口门禁8/8、归档2条，无整页横向溢出；QA原图和DOM宽度 |
| typography | PARTIAL | 标题、状态与正文具有层级且可阅读；尚未全量测量第三方组件各状态的字体对比 |
| spacing_alignment | PASS（QA本页） | QA门禁与右侧摘要在桌面分栏、375纵向排列；四视口Modal水平居中；锚点测量仅覆盖本次读证场景 |
| forms | NOT RUN（真实签名） | 已放行批655不重复签署；签名/生成归档表单未触发任何持久写入，用原模拟回归覆盖流程 |
| tables_lists | PASS（QA本页） | 两条归档、版本和下载操作正常；375操作按钮可见，后台分页保留 |
| query_filters | PASS（QA本页适用性） | QA T6无独立查询卡；证据目录Tabs可选，模拟QA测试覆盖相关过滤行为 |
| component_interaction | PASS（QA专项） | 证据Modal Esc关闭并焦点返还，真实PDF SHA校验，键盘展开原始证据 |
| component_states | PARTIAL | 真实放行/只读及慢加载通过；错误/重试/拒绝/过期靠16项模拟浏览器验证，全部真实服务器故障态未人为制造 |
| wcag22 | PARTIAL | 具语义按钮、键盘Esc、焦点返还、375重排；完整读屏/19项可访问性核验不全 |

**视觉问题后续清单（不改变本轮源业务）**：QA证据弹窗仍展示 `BATCH_QA_STATE` 等技术编码，建议换为中文业务标签或在“技术证据”折叠项内呈现；375证据类别Tab需要保持可发现的滚动提示；页面含英文“Gate Checklist”，建议按全局中文名称规则统一。以上属后续可读性整改，不得遮蔽业务证据或签名来源。

## 6. WCAG 2.2 基础19项状态（QA本页，不是AA认证）

| 条款 | 结论与证据 |
|---|---|
| 1.3.1 | PARTIAL：档案table/Modal结构可见；完整访问树未导出 |
| 1.4.1 | PASS（已观察区域）：放行和归档均配文字，不只靠颜色 |
| 1.4.3 | PARTIAL：正文可读；第三方控件全状态对比尚未测量 |
| 1.4.4 | NOT RUN：真实200%文字缩放未测 |
| 1.4.10 | PASS（已观察区域）：375整页无外层横向溢出、Modal边界完整 |
| 1.4.11 | PARTIAL：按钮及焦点对比未全项仪表化 |
| 2.1.1 | PARTIAL：Modal Esc、summary Enter及下载操作能用键盘；全部控件未测 |
| 2.1.2 | PARTIAL：Esc可退出并返还焦点；未运行完整Tab焦点陷阱审查 |
| 2.4.3 | PARTIAL：Modal开关焦点恢复通过，整页完整焦点顺序未测 |
| 2.4.7 | PARTIAL：截图可见被聚焦查看按钮，未覆盖所有控件 |
| 2.4.11 | PARTIAL：本次模态框不遮挡焦点，其他粘性区未覆盖 |
| 2.5.3 | PASS（已操作按钮）：“查看”“下载并校验”可由标签选择 |
| 2.5.7 | N/A（本页QA证据）：无拖拽式任务，均有点击操作 |
| 2.5.8 | PARTIAL：已操作的查看/下载目标满足基本尺寸；其他目标未逐项测 |
| 3.3.1 | PARTIAL：模拟错误码/刷新路径验证，未做真实写入失败 |
| 3.3.2 | NOT RUN：已归档批次不重做受控签名表单 |
| 3.3.3 | NOT RUN：未逐项执行所有表单验证失败 |
| 4.1.2 | PARTIAL：模态框、表格与按钮有角色名称；全树未验证 |
| 4.1.3 | PARTIAL：异步加载/失败有状态提醒，未做屏幕阅读器朗读核对 |

因此**QA读数与PDF专项可认为通过；全应用G5/WCAG22整体验收仍不得标记 READY 或 ACCEPTED**。尤其是其余改造页面的逐页模态框操作、所有状态/视口截图矩阵和19条全项实审尚需在正式签发时补齐。
