# 产品成品当前有界契约：方案 B

当前业务设计批准来源为下述 B 冻结记录；旧 A 的独立成品 lot 模型已被明确替代，历史草案保存在 `docs/development/proposals/PRODUCT_FINISHED_CONTRACT_A_SUPERSEDED_20261011.md`，不得用于实施。此设计批准不表示代码、迁移、OpenAPI/RTM 或验收已通过；运行状态仅在 MES_TASKS.md，跨文档一致性尚未 PASS。

# 产品 / 生产指令直接主体：方案 B 冻结记录

APPROVED / FROZEN。批准来源：父对话 `01a115d2-8ace-7574-9a08-ac48dfc970cc` 转交的直接执行授权及方案 B 决定；用户 `Sentinel_24c20de6a49881919d361cb14f8e7d1d`、`Sentinel_4be888de3b1081918e0316defa1b5df4` 要求同步文档、基线、后端和前端。此记录取代未执行的独立成品 lot 草案。历史 v1.0.23 和历史签名内容不改。

## 身份与写入

唯一成品业务主体是 `prd_main_batch.id`，业务批号只用 `batch_no`。不新增 `wms_finished_lot`，不新增 lotNo / finishedProductLotId。产品从同组织 MainBatch 派生，不由客户端重新指定。`prd_main_batch` 增加 `production_schema_version`、成品制造日期、有效期、冻结产品基本单位/快照/摘要、质量和库存状态。第一次有效 OUTPUT 冻结这些属性，后续 OUTPUT 必须完全一致；OUTPUT 不产生库存流水。旧 `finished_lot_id` 仅 schema 1 历史使用。

新生产指令由现有 `/main-batches` 原子创建内部 Order 和 MainBatch；输入复用 batchNo、productId、plannedQty、unitId、plannedDate、subBatchPlan、reason。内部订单号由 `INTERNAL:` 加 batchNo 的 SHA-256 派生，仅内部兼容使用。旧 `/production-orders` POST 拒绝公共创建，历史 GET 和旧分配关系不改。新模式用生成列 `CASE WHEN production_schema_version=2 THEN production_order_id ELSE NULL END` 的组织唯一约束保证新建 1:1，不对历史 1:N 加全局唯一。新批编辑只改同一个内部 Order；不能重新选择/分配订单。公共权限继续复用 `production:batch:create/update`，不新增权限。

## 仓储与质量

仅新增一张 `wms_finished_inventory_ledger`。显式列 org_id/main_batch_id/product_id/unit_id；复合 FK 指向 batch 的组织/产品/冻结基本单位，直接产品 FK；位置仍用现有仓库/库位。入库确认才追加正流水，发货确认追加负流水，各来源唯一及幂等唯一。应用端只有 append 和读取，不提供流水编辑/删除；不声称此保证阻止任意管理员 SQL。复用 `wms_finished_inbound_request`，不另建同义入库表。

消费者用 `finished_schema_version` 区分旧/新事实。新成品事实的 material_lot_id / finished_lot_id 必须 NULL，直接 main_batch_id 与必要 product_id 保证一致；旧历史仍保留原 FK、快照、签名和读模型。来料/material 链不改。QC 的 MATERIAL/PRODUCT 主体互斥、定义 schema 1/2、计划 schema 1/2 保留已批准合同。成品检验、取样、QA、平衡、eBR、归档、追溯、打印均消费同一 MainBatch，不重新解释历史 JSON。

固定写锁顺序：内部 Order → MainBatch → 来源单据 → 成品流水（id 升序）。ProductionService 是 batch 属性唯一写入者；一次数量/QA 命令只作一次 MainBatch 乐观更新，QA 的生产状态、成品质量/库存状态合并在此更新。第一 OUTPUT、独立入库签名、QC、QA 签名、独立审核和冻结证据均保持。

## 数据库与验收

新增表 1；既有表扩展范围：md_product、prd_main_batch、qc_specification、qc_specification_version、qms_production_plan、mes_quantity_event、qms_sample、qms_release_decision、wms_finished_inbound_request、wms_finished_shipment、qms_finished_inspection_request、qms_finished_sampling_record，共 12 张。不用第二张成品批次表，不迁移历史行的事实内容。物理版本实施前再次核对；并发供应商 V054 不得覆盖。

原 DESIGN LOCK 的 UI V2 / T1–T6 和字段修复范围继承；生产创建隐藏内部 orderId，批次作为指令主体；成品只读产品和批号。无需新页面结构。原 60 分钟预算从 06:07 UTC 连续计时，不重置，最多 2 次自动修复。隔离验证是用户本轮明确例外；只用既有本机 MariaDB。不得执行旧 @Commit seed、DEV 迁移、DEV 数据写入、配置/权限变更、服务重启、提交或推送。缺隔离能力记录真实 blocker，仍执行编译和纯单元测试。

当前验收状态只在 MES_TASKS.md；模型批准不代表迁移/功能/真实 UI 已验收。
