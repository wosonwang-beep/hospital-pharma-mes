# MES QA批审归档历史页面分离加载修复（2026-11-13）

状态：ARCHIVE SUBSCOPE G5 PASS / WHOLE QA PAGE G5 BLOCKED / NOT ACCEPTED。范围限正式QA批审详情`/qa/batches/:id/review`的只读数据加载与归档历史展示。现有main唯一工作区及已冻结FINAL EBR业务版v01不变。

## 已核实事实
- 真实MES管理员11556分别可GET`/qa/batches/655/review-model`(QA_RELEASED/7/8 gates)与`/main-batches/655/ebr`(原FINAL 93/133、新REVIEW_COPY 94/134)，后台数据与权限正常；前者本机实测约10.33秒，后者约0.42秒。
- 当前`FinishedReleaseView.vue`的`load()`同步顺序为先`await`质量门禁，再`await`完整eBR，UI`archive`仅在第二次返回后赋值，导致慢QA门禁遮挡归档历史。之前CUAWright真实QA界面只见标题/操作栏，历史区域等待18秒超时；原因尚不排除其它UI异步/错误，但至少上述等待串行是已定位的结构缺陷。
- 服务器Flyway正常、8080/5173在线。已核验旧FINAL 93/133文件SHA62902008...68a0，新REVIEW_COPY 94/134文件SHA41e734d...b099、数据摘要f03e05f...7caa均未改变。不得触发PDF生成POST、重复FINAL或QA签名。

## 冻结修复白名单
仅`frontend/mes-web/src/views/quality/FinishedReleaseView.vue`调整读取：并行发起门禁和eBR只读请求，归档结果独立赋值即时展示；独立加载反馈与失败重试；异步路由切换过期结果不得覆盖当前页面。保留签名/QA状态/下载后SHA验证及全部权限条件。允许新增一个仅测试用定向Vitest/Playwright纯模拟API验证并行语义。不得改任何API、DB、角色、既有归档版本、后端业务逻辑。

## G5门禁
先执行TypeScript、相关单测与前端构建；完成CUAWright真实管理员QA页（1440/1280/900及适用视口）两种归档行、客户端下载并校验SHA；归档加载/失败、无权限、跳转失效和刷新等状态完整覆盖，并按mes-ui-ux-audit九维与WCAG2.2准备证据矩阵。若工具安全拦截任何请求，不换通道规避；该项标NOT RUN，不能宣称正式G5/ACCEPTED。

## 真实下载按钮禁用的附加根因（2026-11-13）
CUAWright实测1440、1280、900、375均显示恰好两条合法归档记录（FINAL93与REVIEW_COPY94），页面无横向溢出。然而复测实际下载时，行按钮持续disabled超过40s。检查已冻结前端代码发现所有档案下载动作`download(item)`与QA门禁/归档请求共用全局`busy`；在批审门禁接口较慢，或浏览器连续切换视口产生多个未取消的旧QA读取请求时，虽然archive已提前显示，下载按钮仍被无关QA读取长期禁用。此只读下载不改变QA决定/签名/记录摘要，且独立授权及SHA计算由原`download()`执行。
修复白名单扩充：同一FinishedReleaseView.vue引入独立`downloadingId`加载状态，仅对只读归档下载按钮做互斥，保持其它POST签名/放行/归档生成禁用条件及API权限不变。新增模拟慢QA响应下归档读取/下载仍可用的E2E回归，真实CUAWright实际文件下载和SHA复核。不得删除任何SHA验证或放宽归档读取授权。

## G5 375px表格与键盘焦点修复范围（2026-11-13）
QA归档页存在表格列固定604px内滚动；375px实际截图显示V1/V2及下载操作但归档类型需横向滑动才可见，不利于快速区分FINAL与REVIEW_COPY。真实CUAWright键盘聚焦原始证据`<details><summary>`时computed outlineStyle=`none`，虽键盘Enter可展开，焦点可见性不足。按已批准页面G5范围仅在FinishedReleaseView.vue修改现有版本单元格添加窄屏「最终/审核」文字提示（原版号、完整归档类型列保持）、为摘要summary增加focus-visible轮廓；不调整权限、API/数据库或签名。需再次CUAWright记录真实375视觉和键盘焦点以及主表下载回归。

## G5文字/状态对比度修复（同一页面，不扩业务合同）
自查当前QA页CSS发现正文帮助文字`#8290a5`在白背景仅约3.24:1（普通小字号不满足4.5:1），提示文本`#a86c18`在`#fff8e9`约4.12:1，成功/警告图标色`#4cbd83`/`#e89621`在白背景分别约2.36/2.38:1。仅在Frozen T6现有FinishedReleaseView.vue替换为业务语义保持不变的深色：正文#5d6b81（白背景5.4:1）、提示#87500a（对应浅黄背景6.24:1）、成功#217c55（白背景5.15:1）、警告#936012（白背景5.35:1）。不修改内容/状态机/API/按钮可用条件。通过真实浏览器截图复核，WCAG完整A/AA仍须独立矩阵，不能因这些比率就宣称全部通过。

## 2026-11-13 实施完成／归档专项G5通过／整页G5仍阻断
- FinishedReleaseView对QA门禁GET与eBR GET并行独立读取、独立`archiveLoading/archiveError`，按`loadVersion`隔离失效响应；只读下载由独立`downloadingId`互斥，不再因QA门禁慢而禁用。原放行/签名/生成功能的业务权限、事务/签名请求及文件SHA校验保持不变。
- 375版号增加“最终／审核”短标签、证据summary键盘focus-visible2px轮廓；页面帮助/警告/成功等文本语义色改深，实际对比度见`docs/acceptance/ebr-book/HYTEA_QA_ARCHIVE_G5_SELF_REVIEW_20261113.md`。没有数据库/角色/API修改。
- 浏览器模拟回归新增6/6 PASS（慢QA、慢归档、失败刷新、慢QA时下载、QA异常时归档仍可用、375类型/键盘）；旧QA T6浏览器10/10 PASS（旧测试导航菜单mock数组合同修正后），QA模型Vitest 8/8，vue-tsc PASS，Vite正式build PASS（约1分40秒）。
- CUAWright真实管理账号11556对QA批655的1440/1280/900/375四视口均显示FINAL93与REVIEW_COPY94两条归档，根文档无横向溢出，375类型标签正确，summary键盘轮廓solid2px。真实UI下载FINAL附件133=501399字节，SHA62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0，REVIEW_COPY附件134=1161328字节，SHA41e734d45cdf7f71901c5b9b5c133400e00460085282970b85388f7642d4b099，均与MES manifest一致，0业务写入/0 JS异常。证据`private-data-audit/hytea-pdf-layout-20261113/qa_archive_ui_audit/g5_full_review/`。
- 独立QA门禁GET在实际测试中耗时23.409秒才返回QA_RELEASED/8门禁，另曾因数据库Lock wait 1205出现请求失败；后端`FinishedReleaseService.review()`及`MaterialBalanceService.requireCheckpoint()`调用批次/订单事务锁且含物流同步，非纯快照读取。当前APP账户无PROCESS读取innodb_trx权限，只读排查不提权/杀锁。QA摘要在浏览器最初归档出现时尚不可见，**整页G5=BLOCKED/NOT ACCEPTED**；需另行冻结QA只读门禁性能/锁RED-GREEN专项后再完整签发。不得以成功下载代替QA门禁验收。
- 原批655仍QA_RELEASED/version7，成品库存AVAILABLE；唯一FINAL93/133和REVIEW_COPY94/134均仍存在，原QA签名/来源摘要未更动；无Git提交、推送或worktree。
