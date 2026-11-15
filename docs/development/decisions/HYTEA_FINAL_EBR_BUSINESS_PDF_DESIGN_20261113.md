# 喉咽茶合剂 FINAL eBR 业务版预览 v01 — 待人工确认

状态：APPROVED / FROZEN v01 VISUAL DIRECTION / IMPLEMENTATION IN PROGRESS。遵循AGENTS.md、MES_DEV_RULES.md与PROJECT_BASELINE v1.0.23；当前“继续”不等于批准具体新版打印合同。

## 真实问题与已有复用
当前FINAL manifest93/file133实际317页、758696文字字符，SHA256 `62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0`。`EbrPdfRenderer.java`将全量证据有序JSON逐行输出，PDF可验证但不适合业务查阅。`BookPdfComposer`已有封面、中文目录、书签、页脚、保留原记录PDF与摘要的成熟能力，优先复用而不新增打印引擎、业务单据或API。

## 四页PDF排版候选
预览：`private-data-audit/hytea-pdf-layout-20261113/HYTEA_655_FINAL_BUSINESS_PREVIEW_v01.pdf`，SHA256 `bd67762feb042c4a26e18e9c887726b90a1d1e982c5f430c74b9e8f96b68ca99`。仅使用批655已核验DEV记录：final-business-chain-655、final-ebr-655-verified、op06-op08-verified、finished-output-655、finished-qc-655-dev-observations JSON。
- 1/4 批次/产品、3980瓶入库+20瓶留样=4000瓶总产、状态链、DEV六个月期限。
- 2/4 OP01至OP08/eBR822至829、原生产签名、物料平衡406，239.4kg发料与投料相等。
- 3/4 标准1419的8项QC原结果及区间、11567签名与11568独立复核、报告98/批准5347。
- 4/4 QA 8门禁PASS、放行决定690/签名5348、原FINAL清单93/附件133和完整源记录/文件双SHA256。

所有数据均为合成DEV软件流程输入；四页摘要预览不是原始证据全集，不代替法定批记录。原始317页附件133、QA决议和签名均保持不变。

## 源绑定与不可变边界
- 产品/批次/QA状态/签名来自final-business-chain-655.json；产量与六个月DEV期限来自finished-output-655-20261112.json。
- OP01～OP08、eBR822～829与原生产签名来自op06-op08-verified-20261112.json；正式版必须展示完整修订、CPP规则、异人复核、失效沿革。
- QC八项合成结果及冻结标准1419来自finished-qc-655-dev-observations.json，关联结果839～846与复核；预览长文本有限省略，正式版必须以完整字段与可追溯原始记录为准。
- QA八门禁、决定690/签名5348、FINAL清单93/文件133及完整双SHA256均来自已核验DEV证据，不得覆盖原归档。

## 工程接续门禁（本轮未获具体版式确认）
1. 以BookPdfComposer封面/目录/书签和原源记录PDF合并为主；正文分8工序、成品QC、QA与库存章节；业务摘要与不可变完整原始证据两层共存，不能把四页原型当作合规全量批记录。
2. 须用户确认预览v01源PDF SHA及对应截图后才能冻结具体DESIGN LOCK；然后才允许修改受控EbrPdfRenderer、打包、运行真实非破坏性REVIEW_COPY与正式G5 UI审查；原FINAL93/附件133/hash永远保持不变。
3. 定向验证：确定性PDF字节、Unicode长文字分页、源证据/版本/签名完整、QA阻断前不可FINAL、重复归档幂等、跨组织拒绝、来源变化后的新摘要、1440/1280/900/375相应界面与打印预览截图；不可跳过失败门禁或把自动检查代替人工审查。

## 本轮渲染证据
- 4页A4预览SHA256 bd67762feb042c4a26e18e9c887726b90a1d1e982c5f430c74b9e8f96b68ca99；本机PyMuPDF 4/4文本/页边界检查通过。
- `private-data-audit/hytea-pdf-layout-20261113/`中保留生成脚本、preview-page-01..04.png、preview-contact-sheet.png、layout-preview-manifest.json和preview-visual-check.json；已人工查看4页拼图。
- 原FINAL文件SHA256 62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0再次验证不变；本轮未连业务POST、未重打FINAL或修改签名/数据库。

## 用户对v01版式的明确批准（2026-11-13）
用户收到四页v01 PDF文件、四页排版示意及对应源SHA后明确回复「确认批准」。以PDF `private-data-audit/hytea-pdf-layout-20261113/HYTEA_655_FINAL_BUSINESS_PREVIEW_v01.pdf`、SHA256 `bd67762feb042c4a26e18e9c887726b90a1d1e982c5f430c74b9e8f96b68ca99`及本设计文档作为**具体v01业务阅读版式的DESIGN LOCK**。批准范围为复用BookPdfComposer、基于原来源构建业务章节/目录/签名追溯并保留全部原证据。不得篡改历史FINAL file133、manifest93、旧SHA、原记录、签名、质量结论；新生成只能创建新文件/新manifest或非破坏性的REVIEW_COPY，不覆盖历史。此批准不是正式G5/真实药品质量验收，也不授权扩大API/数据库/角色权限。