> 2026-10-11 方案 B 替代说明：本文件中独立成品 lot / 待选 A-B / V054 实施草案内容属于已被替代的未执行历史。当前模型以 [PRODUCT_FINISHED_DIRECT_BATCH_B_20261011.md](decisions/PRODUCT_FINISHED_DIRECT_BATCH_B_20261011.md) 及 `docs/architecture/PRODUCT_FINISHED_CONTRACT.md` 为准。旧 UI 源码哈希证据保留；方案 B 后续实际文件摘要在本机 FINAL_SOURCE_MANIFEST.json。OpenAPI/RTM 全量同步与一致性审查尚未完成，禁止据旧草案执行。

# DCP-PRODUCT-FINISHED-ROOT-001

DESIGN AUTHORITY APPROVED / FROZEN — 2026-10-11。
批准来源：父对话01a115d2-8ace-7574-9a08-ac48dfc970cc明确批准 PRODUCT_FINISHED_BOUNDED_CONTRACT_REVIEW 全文主模型及七项补充，授权代码、追加迁移和隔离本地验证；DEV应用/服务重启/提交推送不在本轮授权。

v1.0.23历史release不改，仅此受控补充范围优先：QC MATERIAL/PRODUCT互斥及schema1/2、新生产计划V2、批次派生产品、独立成品lot/ledger、必要12张既有表受控扩展及旧链历史兼容。不自动映射11条旧标准、不再制造FINISHED物料、不重构QMS或UI、不改incoming/权限/旧GxP事实。

完整数据库/领域/状态/签名/集成合同：[PRODUCT_FINISHED_CONTRACT](../architecture/PRODUCT_FINISHED_CONTRACT.md)。
API：[OpenAPI](../api/product-finished.openapi.yaml)。
页面设计锁：[DESIGN LOCK](decisions/PRODUCT_FINISHED_DESIGN_LOCK_20261011.md)。
测试/RTM：[matrix](PRODUCT_FINISHED_RTM_20261011.csv)。运行状态仅MES_TASKS.md。

用户明确要求本次先隔离测试库验证迁移，覆盖此前单DEV默认环境限制；仍同一main源码、现有本机MariaDB，不修改DEV配置/权限、不用第二工作树。新流水不新增触发器，应用层不可变保证不能声称具备DB任意SQL改写阻断。未核实隔离账号时报告DEPENDENCY_NOT_READY，绝不应用DEV替代。

最新用户领域模型/目标图优先澄清：成品直连产品+MainBatch，不独立成品批次；旧独立lot自增方案暂停。复用既有成品入库表，ledger须显式main_batch_id/product_id。属性位置A/B与统一新写指令入口由父任务定案，见[身份核查及最小差异](decisions/ORDER_BATCH_IDENTITY_ALIGNMENT_20261011.md)。旧V054已移出Flyway至proposals，不得执行或部署当前未完成代码。
