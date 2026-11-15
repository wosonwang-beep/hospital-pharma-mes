# HYTEA 批655 可读 eBR PDF v01：用户确认与当前验收状态

**设计授权：USER APPROVED v01（再次明确“确定批准”）**。批准范围：按设计冻结SHA `bd67762feb042c4a26e18e9c887726b90a1d1e982c5f430c74b9e8f96b68ca99`的业务排版方向复用BookPdfComposer，实现可读正文、目录及完整原始证据内嵌附件。用户的确认**不等于G5/正式产品验收PASS**。依据AGENTS.md、MES_DEV_RULES.md、PROJECT_BASELINE v1.0.23与已批准设计锁。原main唯一工作区，不擅自Git提交、推送或建worktree。

## 功能与档案完整性（通过）
- MES服务正式生成REVIEW_COPY manifest94、file134；27页，1161328字节，SHA256 `41e734d45cdf7f71901c5b9b5c133400e00460085282970b85388f7642d4b099`，源recordDigest仍为`f03e05f883c42844fe16dd34a86423cdd6e0add1fe4651deaaf251a235907caa`。
- 实际PDF字节从MES附件GET读回并与manifest校验一致，目录/QA/八道工序/检验/签名文字可提取，27页文本块没有越出A4边界。source-evidence.json附件824602字节，8份工序表单、46条签名记录、14条数量事件及成品QC/QA/收货/平衡原始数据均可解析。源数据为合成DEV业务流程数据，不是真实生产或实验室检测。
- 原FINAL manifest93、file133的SHA `62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0`已再验，原归档不可变、未覆盖/重签；当前只新增REVIEW_COPY，不产生第二份FINAL。
- Java定向回归：BookPdfComposerTest 6例含1 SKIP；EbrPdfBusinessLayoutTest 1 PASS；EbrPdfRendererTest 1 PASS，共7 PASS/1 SKIP/0 FAIL。标准Java Maven构建并按正常Flyway启动，HTTP health UP。

## G5界面审查（IN PROGRESS，不能标PASS）
- 阅读了mes-ui-ux-audit技能（SHA `34C2D2B3CEE4896AA6E7C29DF86BBE9ED8B6ECC2646AA63E45397476D1D64B2A`）、ui-ux-pro-max（SHA `8B827FE03B349AEEA99A7D6CF4CCD892E6065B672859FDB11E13A1919453EDDC`），以及Global UI Design System V2。
- 尝试使用CUAWright相同浏览器环境、管理员账号、正式5173/8080，在QA批审详情`/qa/batches/655/review`校验FINAL/REVIEW_COPY的两个版本行及“下载并校验”。1440视口页面只加载了标题和按钮栏，归档历史未渲染；用例等待历史区域超时。截图`private-data-audit/hytea-pdf-layout-20261113/qa_archive_ui_audit/qa_archive_diag_1440.png`。
- 为进一步区分授权、UI时序及API失败而准备的浏览器网络诊断调用被工具层安全检查拦截，未执行；没有换工具/SQL/替代通道绕过。原因尚未确认，不把此项写成MES业务接口403或PDF损坏。

## 九维 G5 审查矩阵（自查，尚未完成）
| 审查维度 | 本轮状态与证据 | 待完成动作 |
|---|---|---|
| 排版布局 | PDF27页文本边界自动检查PASS；QA实际浏览器页档案历史未出现 | QA归档页重新截图及全局布局审核 |
| 字体层级 | PDF中文字体可抽取，标题/章节明确；实际QA页面未全部呈现 | 原始尺寸逐页核对字体对比与标题语义 |
| 留白与对齐 | PDF页块无越界；27页总览已目视检查，部分页面留白较大 | 记录页面级问题/建议并确认不影响证据完整性 |
| 表单规范 | QA归档弹窗未进入 | 冻结类型/原因/确认/取消/错误提示和横向标签验收 |
| 表格列表 | 服务端确有两个manifest；浏览器实际归档表格未显示 | QA历史表逐行、下载操作、空/长状态复核 |
| 查询与筛选 | 本页主要为批审详情；页面查询控件适用性尚未核对 | 根据T6详细设计记载存在/不存在证据 |
| 组件交互 | CUAWright已登录并浏览QA页面，档案行下载路径未验证 | 键盘/鼠标/失效摘要/重试操作走通 |
| 组件多状态 | QA已放行状态只在API证明；浏览器完整页尚未加载 | 归档存在、异常、无权限、加载/重试的真实状态截图 |
| WCAG 2.2 | 尚未执行完整键盘/焦点/颜色/语义/屏幕阅读器检测 | 按A/AA矩阵逐项执行，必要时标明未覆盖项 |

**结论：G5 = IN PROGRESS / NOT ACCEPTED。** 当前通过的是PDF功能/文件证据、源码定向回归，并非完整正式页面验收。文件原始真实生产/质量来源也未做客户验收。

## 下一项精准排障
1. 在获准的正常CUAWright环境下，恢复QA历史页面的真实业务请求日志证据（不要规避本轮安全检查拦截），区分登录会话与页面异步加载、权限/空数据、运行时异常。
2. 仅在确认真实根因后冻结最小修复白名单，复测1440/1280/900等桌面视口及适用的移动兼容性，补全九维矩阵、截图和交互日志。
3. 验证页面对manifest93 FINAL与manifest94 REVIEW_COPY双行展示、下载后客户端SHA校验及QA证据变化后正确失效。不得修改任何签署业务数据或追加重复FINAL。

## 2026-11-13 13:10 QA归档历史加载及真实下载专项G5复核
- 已定位前端串行QA门禁GET先阻塞`/main-batches/655/ebr`历史加载：QA门禁单次实测10.33s，本轮进一步实测23.409s、在相邻日志中存在MariaDB lock wait timeout1205；eBR archive GET只需约0.42s。已在原T6页面最小并行分离读取、独立`archiveLoading/archiveError/downloadingId`；无需增加权限或签名/数据库变更。
- 完整真实CUAWright在1440、1280、900、375px验证归档两行（FINAL93/133与REVIEW_COPY94/134）及动作可见，无根文档横向溢出；通过真实页面“下载并校验”按钮下载旧FINAL 501399字节和新审核副本1161328字节，二者SHA精确匹配MES历史manifest。无业务写请求、未重签/生成任何新归档。
- 新增的QA归档加载/失败恢复/下载/375键盘专项模拟浏览器6/6 PASS；原QA操作浏览器10/10 PASS，模型单测8/8 PASS，vue-tsc及Vite生产构建PASS。表格375加可读“最终／审核”标签，归档证据summary可键盘打开且有2px solid焦点轮廓，对弱对比文字进行最小色值修复。
- 但QA完整门禁摘要是含有生产订单/批次锁及物料平衡同步的事务计算GET，仍可能出现超时/锁冲突；本轮屏幕历史可加载时门禁有时尚未呈现。业务归档G5子域PASS，**整页正式G5仍BLOCKED/NOT ACCEPTED**；严禁用QA决定690或归档file134作为页面全验收替代。
- 完整自审矩阵（九维、WCAG22基础19条、真实证据路径、未覆盖项及后续RED/GREEN建议）见`docs/acceptance/ebr-book/HYTEA_QA_ARCHIVE_G5_SELF_REVIEW_20261113.md`。原FINAL file133与本机main冻结合同均未覆盖，未Git提交推送或新worktree。
