# 喉咽茶合剂生产批655：DEV软件业务全链验证（2026-11-13）

**验收分类：DEV BUSINESS WORKFLOW PASS / FORMAL G5 NOT ACCEPTED。** 全部数据为明确定义的合成DEV流程输入，并非真实采购、现场生产、药品实验室检验或正式质量放行。环境只限本机hospital_pharma_mes_dev，Git唯一main。冻结业务基线v1.0.23及已批准DCP有效，未改其业务状态机。

## 1. 全链正式API结果

| 环节 | 正式对象及结果 |
|---|---|
| 生产批 | 655 / HYTEA-20261109-002，`QA_RELEASED`，finishedQualityStatus `RELEASED`、finishedInventoryStatus `AVAILABLE` |
| 工序/eBR | OP01–OP08（779–786）全部`COMPLETED`；eBR 822–829全部`VERIFIED`，八份原岗位签名及独立复核保留 |
| IPC | IPC13 / FILL_VOLUME，DEV输入100ml，QC11567记录、QC11568复核，`COMPLETED/PASS` |
| 包装来源 | 合成DEV物料批1354瓶、1355标签，物料QC及QA软件流程`RELEASED/AVAILABLE` |
| 成品产出 | 352 `OUTPUT` 398000ml（3980瓶），353 `SAMPLE` 2000ml（20瓶）；合计400000ml（4000瓶） |
| 有效期 | 用户批准DEV六个月规则：2026-11-12至2027-05-12；仅联调假设 |
| 物料平衡 | 406/PASS；ISSUE 239.4kg、CHARGE 239.4kg，差异0kg/0%，容差±0.5% |
| 成品待检入库 | 126 / FG-IN-HYTEA-655-20261112，`CONFIRMED`，仓库11565独立签名5329，库存流水3、库位7，398000ml |
| 成品请验 | 108 / FQ-HYTEA-655-20261113，QC11567接受，`COMPLETED` |
| 取样交接 | 取样107、签名5330，样品1640；分析员QC接收后八项完成，样品`TEST_COMPLETED` |
| 八项QC | 原始检验实例514–521、结果修订839–846均`PASS`，分析员11567签署、复核员11568逐项异人`CONFIRMED` |
| QC报告 | 98 / FR-HYTEA-655-20261113，`APPROVED/PASS`，独立复核签名5347 |
| QA最终放行 | release decision690、QA11569签名5348，`RELEASED`；8项QA门禁全部PASS |
| FINAL eBR | manifest93、file133、generationVersion1、归档种类`FINAL`，绑定决定690；eBR表单8、签名证据43条 |

## 2. 完整性证据及质量门禁

- QA的`BATCH_QA_STATE`、`QUALITY_PLAN`、`EBR_REVIEW`、`PRODUCTION_QC`、`MATERIAL_BALANCE`、`FINISHED_INVENTORY`、`FINISHED_RECEIPT`、`FINISHED_INSPECTION_REPORT`八道门禁均通过，blockingCodes为空；批次最终状态通过正式API再次独立读回。
- FINAL eBR PDF附件133真实下载得到501399字节`application/pdf`，%PDF-1.6；SHA256 `62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0`，与MES存档清单93 fileHash一致。记录摘要`f03e05f883c42844fe16dd34a86423cdd6e0add1fe4651deaaf251a235907caa`，定义摘要`4b9aaa431f7a7834ecd57d96836299143fcc06b514088723c84b998f840f456c`，存档后摘要不变且FINAL仅1项。
- 本机PDF副本在`C:\Users\Administrator\.codex\tools\cuawright-mcp\evidence\mes-final-655-20261113\final-ebr-batch655.pdf`；PyMuPDF页数317、抽取文本758696字，批号/产品/QA决定及签名5329和5348可识别，各页文字块坐标均未越界。由于原生Renderer主要把技术JSON全量写入PDF，业务可读性与打印排版**不满足正式G5体验验收要求**，需要另行独立设计审核，不能把`FINAL`状态与UI/打印`ACCEPTED`混淆。
- V077在用户明确批准的一次性DEV恢复后success=1，Flyway全量77/77有效，原八份eBR签名策略正常迁移并保留，数据库触发器0。正常Flyway启用启动后执行本轮QC/QA/PDF操作。

## 3. 本轮最小源码缺陷修复

- `SecurityConfiguration`仅补充原有ProductionQualityController的样品接收、检验结果和异人复核精确路由，复用已存在的qms:sample/qms:test权限码；未新增权限、角色或数据库结构。修复前11567有权限但POST receive被HTTP403拒绝；重新正常启动后样品1640正式RECEIVED且八项结果全部生成。
- `EbrArchiveService`修正FINAL的schemaV2产品质量状态读取：V2从现有`finishedGoods.productStockEvidence`检查QA质量状态，legacy成品物料批分支原样保留，不跳过签名/QA决定/摘要/文件哈希。实际生成FINAL manifest93验证通过。mes-security、mes-release、mes-boot构建均成功。
- 原有大量未提交文件均保留，main单一工作区，不创建worktree、不执行Git stage/commit/push。不存在新增Flyway迁移或后续repair。

## 4. 后续非阻断项目

- **G5/打印体验**：317页技术JSON不适合业务人员快速查阅，需要按原有生产工序/表单/业务单据组织更易读的FINAL PDF模板，经用户单独确认设计后实现；不得修改已归档93的源摘要或伪造历史文件。
- **真实生产验收**：合成DEV输入不能替代实际取样、仪器原始结果、供应商资格及QA实物放行，真实客户验收需另行执行。
- **UI巡检**：仓库角色的单位/人员只读引用仍有部分显示缺口，需按最小只读投影治理；不能擅加IAM角色权限。

## 5. 原始证据

`private-data-audit/hytea-resume-20261111/`：`final-business-chain-655.json`、`finished-inbound-126-confirmed.json`、`finished-request-655-accepted.json`、`finished-sampling-108-received.json`、`finished-qc-655-dev-observations.json`、`finished-qc-655-eight-item-progress.json`、`finished-qc-report-655-approved.json`、`qa-release-655.json`、`final-ebr-655-verified.json`；本机CUAWright证据目录`evidence/mes-final-655-20261113`含PDF、SHA校验JSON、页级文本和渲染截图。
