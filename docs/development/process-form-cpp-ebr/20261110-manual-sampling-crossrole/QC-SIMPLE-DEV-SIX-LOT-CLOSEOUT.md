# 六批原辅料 DEV 检验录入—独立审核—报告—QA 放行闭环执行记录

状态：**DEV SOFTWARE WORKFLOW VERIFIED 6/6**；**NON-CLINICAL / SYNTHETIC RESULTS / NOT ACTUAL LABORATORY QUALITY RELEASE**。
执行日期：2026-11-10。当前业务冻结基线：FINAL BASELINE COMPLETE v1.0.23；用户进一步要求“继续，检验就是录入检验结果，审核就完成，不要搞得太复杂了”。
执行环境：本机 `D:\codex\_project\gmp\hospital-pharma-mes` 单一 main 工作区，localhost:5173→8080，数据库 `hospital_pharma_mes_dev`，原有DEV角色账号。未新建数据库或worktree，未直接SQL修改业务记录、未迁移、未stage/commit/push。

## 严格区分数据身份

检验值使用 `QC-SYNTHETIC-DEV-FIXTURE-MANIFEST.json` 中**为软件联调构造的数值与文字**。它们不是2026年六个原辅料批的真实仪器测量结果，也不是2023年喉咽茶成品历史纸质检验数值。构造数据只用于验证 DEV 的录入、结果签署、独立 QC 复核、报告审批和 QA 决策接口连通性。下表“放行”是 **DEV 系统状态已更新**，不得据此推断任何实际物料合规或可用于真实药品生产。

## 正式 DEV HTTP 操作及持久化读回

| 物料 | 物料批 ID | 请验单 | 取样任务 | QC 检验任务 | 项目签署 | QC 复核状态 | 已批准报告 | DEV QA 决定 | 库存现有数量 |
|---|---:|---:|---:|---:|---:|---|---:|---:|---:|
| 威灵仙 | 1121 | 721 | 689 | 771 | 5/5 | QC_PASSED | 834 | 566 RELEASED | 100.875 kg |
| 野菊花 | 1123 | 722 | 690 | 772 | 5/5 | QC_PASSED | 836 | 567 RELEASED | 49.875 kg |
| 地胆草 | 1124 | 723 | 691 | 773 | 2/2 | QC_PASSED | 838 | 568 RELEASED | 49.875 kg |
| 甘草 | 1122 | 724 | 692 | 774 | 5/5 | QC_PASSED | 840 | 569 RELEASED | 17.875 kg |
| 蔗糖 | 1125 | 725 | 693 | 775 | 1/1 | QC_PASSED | 842 | 570 RELEASED | 31.875 kg |
| 苯甲酸钠 | 1126 | 726 | 694 | 776 | 1/1 | QC_PASSED | 844 | 571 RELEASED | 1.875 kg |

全部 19 个检验项目有一条原始执行和一条结果修订、`PASS`判定及非空电子签名 ID。QC 独立复核人 ID 11568，结果录入者 ID 11567，QA角色 ID 11569；全部六个物料批的DEV `qualityStatus=RELEASED`、`inventoryStatus=AVAILABLE`、`eligibleForRelease=true`。WMS此前真实DEV样品扣减每批0.125kg没有被本轮改变。

## 真实运行轨迹

1. Pilot：先正式创建 item773 的执行记录（execution603），确认签名对象 `TEST_RESULT_REVISION` 和版本。
2. 首轮正式脚本：完成任务771五项DEV结果签署和独立QC复核；在系统自动起草报告833时，reviewer11568因报告`createdBy=11568`导致`INDEPENDENT_REVIEW_REQUIRED`（422）而停止。该错误是实际执行的门禁，没有绕过或删除该自动报告。
3. 复核后改用**已有** `POST /quality/inspection-reports` 由QC分析员11567创建其独立报告，再让QC审核员11568进行报告复核，QA11569按现有功能批准及独立放行。此前自动草稿保留原状，不做SQL删除或篡改创建人。
4. 第二轮脚本 `hytea-qc-simple-dev-closeout-20261110.tmp.spec.ts`：真实5173/8080 API **1/1 PASS**、`HYTEA_SIMPLE_QC_DEV_R2_EXIT=0`；依次完成六批所有剩余DEV项目、独立QC复核、报告批准及QA放行，最终日志 `SIMPLE_QC_DEV_WORKFLOW_FINISHED_6`。
5. 独立只读核验 `hytea-qc-simple-dev-readback-20261110.tmp.spec.ts`：真实API **1/1 PASS**、`HYTEA_QC_DEV_READBACK_EXIT=0`，汇总 `signedResults=19, approvedReports=6, releasedLots=6`；对应六批的报告、签名、状态及库存均读回一致。
6. 本次脚本的原始测试执行版已归档为 `*.tmp.spec.ts.txt`，并去除硬编码的DEV密码，防止默认E2E重复创建结果/再次QA放行。它们不属于常态自动回归套件。

## 已知收尾事项和风险

- 系统在独立QC任务复核时会自动生成当前QC审核人员自己创建的DRAFT报告（如833）；同一审核人再次review会被独立性门禁拒绝。使用已有报告创建接口生成独立作者报告后已完成6/6，未修改代码/权限/数据库结构；自动草稿仍需以后业务文档/界面治理时明确处理，不得删除历史记录。
- 本轮没有真实化验报告/仪器读数可支持19项，所以必须以“合成DEV软件验证”而非“真实原辅料QC检验合格”解释所有放行状态。
- 喉咽茶生产批655的质量计划666独立QA审批、正式下达、八道工序CPP/eBR、成品检验、成品QA放行/入库/归档不在本轮执行范围，仍需后续DEV流程验收。
- 现有QC/QA签名、版本锁、角色独立性、审计、自动判断、报告/放行门禁未绕过。平台实际数据严格限于DEV环境。

## 文件路径

- 本记录：`docs/development/process-form-cpp-ebr/20261110-manual-sampling-crossrole/QC-SIMPLE-DEV-SIX-LOT-CLOSEOUT.md`。
- DEV合成源值与标签：同目录 `QC-SYNTHETIC-DEV-FIXTURE-MANIFEST.json`。
- 用户简化授权操作记录：`docs/development/decisions/DCP-QC-SIMPLE-DEV-ENTRY-20261110.md`。
- 过程及结果脚本：同目录 `hytea-qc-dev-pilot-20261110.tmp.spec.ts.txt`、`hytea-qc-simple-dev-closeout-20261110.tmp.spec.ts.txt`、`hytea-report-creator-readonly-20261110.tmp.spec.ts.txt`、`hytea-qc-simple-dev-readback-20261110.tmp.spec.ts.txt`。
