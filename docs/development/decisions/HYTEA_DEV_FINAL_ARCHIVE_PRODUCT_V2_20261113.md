# 批655 FINAL eBR PDF 原版成品物料批引用缺陷

状态：最小缺陷修复 / 本机DEV；用户要求完成成品QC→QA→FINAL归档，依据FINAL BASELINE v1.0.23及已批准的产品与成品库存分离方案。无新增业务字段、权限、迁移、状态或API。

事实：批655 product mode productionSchemaVersion=2，独立成品数据直接关联md_product与mainBatch，finishedLotId=null。已完成正式QA放行，QA release decision690/签名5348，批状态QA_RELEASED、finishedQualityStatus=RELEASED、finishedInventoryStatus=AVAILABLE，八项QA门禁PASS。`EbrArchiveService.generate()` FINAL检验仍无条件调用`wms.materialLot(org,finishedLotId)`读取质量状态，V2 null关联会错误阻断。

修改白名单：仅`backend/mes-release/src/main/java/com/hospital/mes/release/application/EbrArchiveService.java`中FINAL判定的质量状态来源，V2产品模式使用现有`finishedGoods.productStockEvidence`的冻结qualityStatus；旧模式仍校验原`wms.materialLot`，不改其它签名有效性/QA决定/记录摘要/存档幂等/渲染与文件哈希。必要时仅增加定向回归与MES_TASKS证据。

验收：既有Java模块编译及正常Flyway启动，调用正式`POST /main-batches/655/ebr/pdf` kind FINAL，随后GET manifest+archive read model验证decisionId、QA签名、fileId/fileHash/recordDigest、归档次数幂等；不得重签QA/EBR或伪造PDF。

## 2026-11-13 真实API执行结果
- `EbrArchiveService.generate()`的原始其它签名/QA/摘要/幂等逻辑保持不变；产品模式schemaVersion2以现有`FinishedGoodsService.productStockEvidence()`读取成品质量状态，旧finishedLotId物料批方式仍保持原检查。代码SHA256 `91CE1183A747AA3371CFC42E926BAFF1EF1EDCD5D0FA2A2B96AD6B3E22CB4902`（执行时）。mes-release、mes-boot Maven package成功；按本机唯一8080正常Flyway启动，health UP。
- 真实批655状态QA_RELEASED/version7、finishedQualityStatus RELEASED、finishedInventoryStatus AVAILABLE，QA八门禁全PASS。最终POST `/main-batches/655/ebr/pdf` archiveKind FINAL成功：manifest93，file133，generationVersion1，releaseDecisionId690，recordDigest f03e05f883c42844fe16dd34a86423cdd6e0add1fe4651deaaf251a235907caa，definitionHash 4b9aaa431f7a7834ecd57d96836299143fcc06b514088723c84b998f840f456c，原EBR 8份、签名证据43条。正式GET manifest/read-model确认仅1个FINAL，且摘要保持一致。
- 实际通过附件受权GET读取file133二进制文件，前缀%PDF-1.6，MIME application/pdf、大小501399字节，SHA256 62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0与数据库manifest完全相同。用户本机副本位于`C:\Users\Administrator\.codex\tools\cuawright-mcp\evidence\mes-final-655-20261113\final-ebr-batch655.pdf`。
- 使用本机PyMuPDF逐页解析、渲染首/次/末页，发现共317页、文本约758696字；批号HYTEA-20261109-002、产品喉咽茶合剂、QA_RELEASED、FINAL及签名5329/5348等关键证据可提取，各页文字块无越出页面坐标。由于PDF主要以技术JSON展开全部原始证据，文件虽完整可校验，但业务可读性与打印排版尚需单独设计审核；不得宣称G5 UI/打印体验已ACCEPTED或实际药品检验数据真实。
- 完整DEV软件业务闭环通过；所有成品QC结果属于合成DEV场景。未改已冻结V077、审计/签名或既有业务数据；无Git提交推送/worktree。
