# HYTEA 655 QA归档历史页 G5 自审记录

**环境**：2026-11-13、本机 `hospital_pharma_mes_dev`、唯一 `main`、真实页面`http://127.0.0.1:5173/qa/batches/655/review`，正常后端8080。**执行人：ChatGPT SELF-REVIEW，非独立QA**。

**状态：归档历史专项 G5 功能 PASS；QA整页 G5 = BLOCKED / NOT ACCEPTED。** 不能把用户对v01排版批准、UI读取/下载通过等同于整个QA批审G5签发或真实药品放行验收。

## 已通过的真实功能门禁

| 项目 | 实际验证 | 证据 |
|---|---|---|
| 归档版本历史 | 1440/1280/900/375四视口均显示FINAL93与REVIEW_COPY94两条真实记录 | `private-data-audit/hytea-pdf-layout-20261113/qa_archive_ui_audit/g5_full_review/qa_archive_full_{width}.png` |
| 前端并行加载 | 归档数据独立于耗时的QA门禁GET；慢QA、慢归档、归档读取失败/刷新恢复均通过模拟浏览器回归 | `e2e/qa-archive-progressive.spec.ts` |
| 两种归档的实际下载 | FINAL附件133：501399字节；REVIEW_COPY附件134：1161328字节，均通过真实页面“下载并校验”后SHA比对 | `g5_full_review/g5_real_browser_result.json` |
| FINAL 133 SHA256 | `62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0`，原件未覆盖 | `g5_full_review/qa655-最终归档.pdf` |
| REVIEW_COPY 134 SHA256 | `41e734d45cdf7f71901c5b9b5c133400e00460085282970b85388f7642d4b099`，独立新文件 | `g5_full_review/qa655-审核副本.pdf` |
| 原QA浏览器行为回归 | 10/10 PASS，原QA放行、拒绝、过期冲突、权限负例、下载防篡改、证据抽屉等模拟API用例 | `e2e/mes013-release.spec.ts` |
| 后台签名与批次 | 本轮仅GET/HEAD/auth登录和刷新，业务写入0；QA决定690/签名5348、原两份归档不变 | `g5_real_browser_result.json` |
| 窄屏操作与键盘 | 375px“最终／审核”文字可见、下载按钮67×24px、原始证据summary可键盘Enter展开且focus-visible为2px solid | 375截图、`archive_summary_focus_keyboard.png`与JSON |

## 未解决的独立阻断

**G5-HIGH-01：QA批审门禁摘要间歇失败。** 实际浏览器在部分读取中只看到标题/归档版本历史，`review`摘要不出现或显示“请求失败，请稍后重试”。后端日志在相近时间出现MariaDB `1205 Lock wait timeout exceeded`；源代码`FinishedReleaseService.review()`是`@Transactional`，读取时调用`ProductionQueryService.lockBatch()`，后者会锁生产订单和批次；物料平衡`requireCheckpoint()`也调用批次锁并执行同步。因此QA状态读取并不是纯快照读，有锁竞争风险。此处尚未改动冻结QA门禁和放行证据计算，不可通过缓存或跳过门禁伪造已通过状态。需单独按设计门禁和RED/GREEN修复只读快照计算，再复测完整页面，方可关闭G5-HIGH-01。

## G5九维自审矩阵

| 维度 | 结论 | 实际观察、未完成项 |
|---|---|---|
| layout | **PARTIAL** | 4视口根文档无横向溢出，归档卡片完整；QA门禁摘要在慢GET期间缺失，不是完整批审页面截图 |
| typography | **PARTIAL** | 标题、归档列、按钮语义清楚；将11–12px弱对比色#8290a5改为#5d6b81（对白底约5.4:1），仍未逐项计算全部AntD状态样式 |
| spacing_alignment | **PASS（归档专项）** | 1440/1280/900/375卡片与主表无裁切，操作列可见；完整QA Gate区域由于后端慢未完成复核 |
| forms | **NOT RUN** | 当前归档下载不需表单；因用户要求不得重复QA签署或追加FINAL，真实签名/归档生成表单未在当前已放行批次执行，仅旧模拟契约测试 |
| tables_lists | **PASS（归档专项）** | 两行版本数据准确、版本/时间/动作正确；375加入“最终／审核”文字（类型完整列仍可内滚动查阅），按钮可见 |
| query_filters | **PASS（适用性检查）** | QA批审详情不含独立查询表单；证据目录按类别Tab过滤，原T6浏览器回归覆盖其选择行为；不虚构查询条件 |
| component_interaction | **PASS（归档专项）** | FINAL及REVIEW_COPY两条真实下载均SHA校验通过，summary键盘Enter可展开，浏览器未产生业务写请求 |
| component_states | **FAIL（整页）** | 模拟慢QA/慢归档/归档失败与刷新恢复通过；实际QA门禁GET偶发锁竞争，真实屏幕在部分等待中缺少QA摘要，需真实失败与重试全状态回归 |
| wcag22 | **PARTIAL / NOT ACCEPTED** | 已验证语义表格、可访问按钮名、键盘summary、focus-visible轮廓、375重排以及部分对比度；未完成全屏读屏、全部焦点序列/对比及错误态操作 |

## 可追溯的视觉与交互修复

- 主体归档GET与QA门禁GET已分离并发；`archiveLoading`的`role=status`展示独立加载，失败提示可刷新；旧请求按`loadVersion`阻止跨批次覆盖当前页面。
- 下载通过独立`downloadingId`互斥，保留当前用户`ebr:form:view`授权、实际附件GET、浏览器`crypto.subtle.digest('SHA-256')`校验；QA签名与生成归档仍走原权限及busy门禁。
- 375宽度版号单元格额外展示“最终／审核”，不变更正式归档类型、PDF引用、生成版本或原类型列。
- summary新增`focus-visible`2px实体轮廓；键盘Tab定位并Enter展开已在真实页面验证。帮助文本色#5d6b81/白底5.4:1，浅黄提示字#87500a/#fff8e9为6.24:1，成功#217c55/白底5.15:1、警告#936012/白底5.35:1。不能因局部颜色达标声称全页对比完全达标。
- 原本10项T6 QA模拟浏览器回归2项报`nodes.flatMap is not a function`，根因旧测试把`/auth/navigation`按通用分页对象模拟，违背实际数组合同；仅修正测试桩后10/10 PASS，未修改导航正式应用代码。

## WCAG 2.2 A/AA 基础准则（本轮实际覆盖，19项）

标记PASS仅适用于已明确核验的归档区域，不推导为整个页面或读屏器一致性PASS。N/A必须有实际不存在的交互类型说明，未做则记NOT RUN。

| 准则 | 结果 | 依据与后续 |
|---|---|---|
| 1.3.1 信息及关系 | PARTIAL | 实际档案历史为具TH/TD的表格、按钮/版本标签结构；全QA错误表单未审 |
| 1.4.1 颜色使用 | PASS（归档） | FINAL/REVIEW_COPY均有文字，质量结果有文本状态，不单凭标签颜色 |
| 1.4.3 文字对比 | PARTIAL | 小字号灰字原3.24:1已修正为5.4:1；未计算全部AntD第三方状态 |
| 1.4.4 文字缩放 | NOT RUN | 未实测浏览器200%文本缩放、400%与平台字体缩放 |
| 1.4.10 重排 | PASS（归档） | 375根文档无横向溢出；复杂数据表在内部滚动、版号已显示类型，动作列可见 |
| 1.4.11 非文本对比 | PARTIAL | 成功/警告图标色已加深，仍未对所有按钮边框及焦点计算完整比率 |
| 2.1.1 键盘 | PARTIAL | summary可键盘Enter，下载按钮有可聚焦语义；未覆盖全部控件 |
| 2.1.2 无键盘陷阱 | NOT RUN | 未测试所有抽屉、签名弹窗的Tab/Shift+Tab焦点退出 |
| 2.4.3 焦点顺序 | NOT RUN | 未记录整页从顶部至页尾的完整Tab次序 |
| 2.4.7 焦点可见 | PARTIAL | 真实summary键盘聚焦2px solid焦点轮廓PASS，其他交互仍未审完 |
| 2.4.11 焦点不被遮挡 | NOT RUN | 未测全部焦点位置与粘性区域覆盖情况 |
| 2.5.3 标签含于名称 | PASS（归档） | “下载并校验”按钮可由角色和可见文本准确获取；两条均可调用 |
| 2.5.7 拖动替代 | N/A（归档） | 真实归档历史表不含任何拖拽操作，下载为普通按钮 |
| 2.5.8 目标尺寸 | PARTIAL | 归档下载按钮实际67x24px满足最低边界；其他小图标/分页目标未逐项测量 |
| 3.3.1 错误识别 | PARTIAL | 归档失败显示可读错误提示与刷新入口；正式签名表单错误未在本轮写入测试中执行 |
| 3.3.2 标签/说明 | NOT RUN | 签名/生成归档表单因原FINAL冻结未做真实破坏性操作 |
| 3.3.3 错误建议 | NOT RUN | 仅在模拟服务故障测试恢复，未覆盖所有表单校验建议 |
| 4.1.2 名称/角色/值 | PARTIAL | 归档表格、按钮、Tab、summary具有DOM语义，未完成全页面可访问树检查 |
| 4.1.3 状态消息 | PARTIAL | 归档加载有`role=status`，失败有alert；未进行屏幕阅读器实际语音验证 |

**WCAG 2.2 完整等级：NOT ASSESSED。** 不能声称满足整体AA；本轮是具证据的基础自查。

## 本轮完成的回归与构建

- 前端`finishedReleaseModel.test.ts`：8/8 PASS；`vue-tsc -b` PASS；Vite生产构建PASS（约1分40秒，仍有应用已有大chunk警告，非此次编译错误）。
- 新增`e2e/qa-archive-progressive.spec.ts`：6/6 PASS：慢QA优先显示历史、慢归档有独立状态、归档错误可刷新、慢QA时下载已授权REVIEW_COPY、QA接口503不压制归档下载、375px“最终/审核”+键盘summary。
- 原有`e2e/mes013-release.spec.ts`：10/10 PASS。旧模拟接口`/auth/navigation`未提供正式数组形状产生2个无关JS错误；修复测试桩而非修改正式导航后全部通过。涉及原QA签署、拒绝负例、冲突、只读、PDF hash防篡改、证据Tab/抽屉等。
- CUAWright同环境独立浏览器：`private-data-audit/hytea-pdf-layout-20261113/qa_archive_ui_audit/g5_full_review/g5_real_browser_result.json`。四视口各两个归档行，页面documentWidth等于viewportWidth，无业务POST/PUT/PATCH/DELETE、0 JS pageerror。真实按两行下载：旧FINAL133字节501399且SHA `62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0`；新REVIEW_COPY134字节1161328且SHA `41e734d45cdf7f71901c5b9b5c133400e00460085282970b85388f7642d4b099`。真实键盘summary/Enter展开且focus-visible solid 2px，375版号下附有“最终／审核”可读标识。
- 实际QA门禁接口`GET /qa/batches/655/review-model`在一次单独只读测试中返回`QA_RELEASED`、8门禁无阻断，但请求耗时**23.409秒**。另有真实CUAWright页面在等待窗口中QA摘要不出现以及偶发通用错误；应用日志在本轮出现MariaDB 1205 `Lock wait timeout exceeded`。不要把“能下载归档”写成“QA门禁读取性能/完整页面状态已通过”。
- 进一步SQL只读锁会话排障需PROCESS privilege，当前MES DEV账户不可读取`information_schema.innodb_trx`，未提权/杀连接/执行SQL修改。后端观察到`FinishedReleaseService.review`开启事务并调用生产订单/主批锁、物料平衡检查亦同步/上锁；建议另立冻结范围明确的只读快照/RED-GREEN优化任务，严格保持实际QA决定路径锁与recordDigest合同。

## 正式判定及下一任务

**归档PDF查看与两份真实下载：G5专项 PASS。整体 QA 批审详情：G5 BLOCKED / NOT ACCEPTED。** 未完成的19条WCAG基础项及完整QA门禁/异常恢复交互不能以本次文件完整性通过代替。G5-HIGH-01 QA读接口可能持有批锁造成长等待，需先研究最小不改变签名和放行合同的方案、审批后修复及完整浏览器复验；同时保留样例批655、QA签名5348、归档FINAL93/133与REVIEW_COPY94/134原哈希。
