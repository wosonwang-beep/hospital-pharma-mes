# 产品成品方案 B 实施阶段报告

结论：**BLOCKED / NOT READY FOR ACCEPTANCE / NOT ACCEPTED**。2026-10-11 06:44 UTC。本轮从 06:07 UTC 起连续计时，原预算 60 分钟；已使用两次自动修复，依 MES_DEV_RULES.md §6A 在隔离迁移失败后停止实现/重测，不重置预算、不自动改号、不改变断言。

## 1. 基线与授权

真实读取 AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md、docs/PROJECT_BASELINE.md、docs/development/database-and-validation-strategy.md、本轮 DCP/契约/设计锁及受影响源码。历史 FINAL BASELINE v1.0.23 与已执行迁移不改。本轮批准方案 B 见 [直接批次设计锁](../development/decisions/PRODUCT_FINISHED_DIRECT_BATCH_B_20261011.md)：产品 + MainBatch，无独立成品批次；独立流水；统一生产指令主体。用户授权现有页面字段/关系修复与隔离验证，不包含 DEV 应用、重启、推送或旧 @Commit seed。

## 2. 已落盘与设计差异

已写 QC MATERIAL/PRODUCT 主体、schema 1/2 定义与 V2 计划；统一 MainBatch 原子创建内部 Order，公共 Order POST 拒绝，历史分配关系保留；批次成品属性、首次 OUTPUT 冻结、独立库存流水、入库/发货新分支、检验/取样/QA、导航/追溯/eBR 新读取；现有 Vue 页面字段、产品标准筛选、只读产品、统一指令创建、成品直连入口。新 `finishedProductLotId` 字段已移除。

新增一张 wms_finished_inventory_ledger，迁移稿扩展 12 张既有表。库存不使用新独立 lot；原材料链及旧 ID 字段只保留原含义。未改变历史签名、审计、JSON 或归档内容。现有 UI V2 / T1–T6 复用，未宣称视觉验收。

仍未完成全链闭合：两处 RealisticProductionFinishedSeedIT 的旧物料依赖尚未由本任务整改（demo 文件存在其他任务并发改动，未覆盖）；PRODUCT 历史标准查询保护/专用读取、旧 DRAFT 批次与新 V2 计划模式边界、内部 Order 动作投影、数量表单按 OUTPUT 禁止旧 lot 字段的呈现及打印/浏览器真实验收仍需审核补齐。未运行原种子。

## 3. 修改范围与迁移阻塞

实际文件清单/摘要存本机证据目录的 FINAL_SOURCE_MANIFEST.json。保护 supplier/master 的并发改动、application-local.yml / application-ci.yml 和所有用户未提交数据。

V055__product_finished_direct_batch.sql 为未执行草稿。实施时最高成功值为 V054；随后另一任务新增同号 V055__qms_inspection_object_binding.sql。两文件同时进入 target/classes，Flyway 在 validate 阶段报 `Found more than one migration with version 055`，尚未执行本轮升级 SQL。两个原始摘要均保存，不隐藏/修改另一任务文件、不使用 repair。需要父任务协调物理版本与两个补充的组合升级顺序，再授权有限续作。

## 4. 验收证据

| 门禁 | 实际结果 |
|---|---|
| Java 21 后端 compile | PASS：20 模块，7.411 秒；命令 `mvn -o -pl backend/mes-boot -am -DskipTests compile` |
| Vue TypeScript | PASS：`npm run typecheck`，退出 0 |
| QC 证据 / 规则单元 | PASS：5 + 6 = 11，0 失败/错误/跳过 |
| 成品规则 / 新流水单元 | PASS：2 + 3 = 5，0 失败/错误/跳过 |
| 隔离迁移 IT | ERROR：1 测试，重复 V055；17.844 秒整个定向测试命令；未进入 migration/fixture |
| 全链业务、并发/幂等、HTTP、真实 UI | NOT RUN；不能以 compile/unit 代替 |
| 综合审查、跨文档一致性 | 尚未 PASS；上述未完成边界必须闭合 |

两次修复：第 1 轮复用实际 FinishedGoodsRules.ship；第 2 轮复用 MasterQueryService.unitName。随后编译通过；本次迁移错误是并发占号冲突，不继续第三轮自动修复。

本机隔离 schema：test_mes_product_finished_20261011_0640，同一 native MariaDB 127.0.0.1:3306，唯一空 schema 经工具审批创建。使用 DEV 只读事务快照恢复；文件 4,946,334 字节，SHA-256 `c03f4def603c7288a4fe9359eb4f4b0f14ea0e54575256b021dd5bf9b802e8ea`，恢复退出 0。未安装软件或改变网络/账户/权限。隔离测试只使用环境传递本机既有凭据，不输出秘密。

证据目录：`C:\Users\Administrator\AppData\Local\Temp\mes-quality-plan-root-review-20261011`，含编译/typecheck日志、targeted-tests-result.json、BLOCKED_FINAL_VALIDATION.json、isolated-upgrade-preparation.json、两个 V055 摘要。备份私有 SQL 留本机，不进 Git。

## 5. 数据与结论

最后独立只读核验 DEV：最高成功迁移 54；foreign_key_checks=1；物料 16；id17/18=0；FINISHED 物料=0；产品 2。本任务没有 DEV 写入/迁移，没有重启或提交推送。隔离库保持恢复的 V054，用于后续经协调的验证；不自动删除该库。

必须保持 BLOCKED。不能宣称独立成品全链已实现、迁移通过、已部署、视觉通过或可验收。父任务需先处理 V055 并发冲突及工程门禁续作，再完成列出的源码/测试/文档闭合。
