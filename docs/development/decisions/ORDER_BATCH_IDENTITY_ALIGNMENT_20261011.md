> 2026-10-11 方案 B 替代说明：本文件中独立成品 lot / 待选 A-B / V054 实施草案内容属于已被替代的未执行历史。当前模型以 [PRODUCT_FINISHED_DIRECT_BATCH_B_20261011.md](PRODUCT_FINISHED_DIRECT_BATCH_B_20261011.md) 及 `docs/architecture/PRODUCT_FINISHED_CONTRACT.md` 为准。旧 UI 源码哈希证据保留；方案 B 后续实际文件摘要在本机 FINAL_SOURCE_MANIFEST.json。OpenAPI/RTM 全量同步与一致性审查尚未完成，禁止据旧草案执行。

# 生产指令 / 生产批次统一业务身份 — 最新领域澄清核查

依据：父对话01a115d2-8ace-7574-9a08-ac48dfc970cc转达用户Sentinel_e3673dc6db488191b321afa742976b8f。最新明确模型：物料保持；产品/生产批次直连成品入库库存发货；成品不生成md_material/md_material_lot；共用仓库/库位/单位；这是领域整改。

当前真实基数（2026-10-11 06:17:37Z，本机DEV只读）：prd_production_order=2，prd_main_batch=2，prd_sub_batch=0，prd_execution_unit=0。

|订单ID|订单号|批次ID|批号|产品|状态|计划量/单位|
|---|---|---|---|---|---|---|
|18|PO-KCL-20261008-003|18|20261008-KCL-003|1|双方DRAFT|1000/ml|
|139|SYNTHETIC_BOOK_ORDER_20261008|142|SYNTHETIC_BOOK_BATCH_20261008|1|双方DRAFT|120000/ml|

实际当前1:1，但schema不是1:1：prd_main_batch.production_order_id NOT NULL FK -> prd_production_order.id，无该列唯一键；订单/org+order_no唯一，批次/org+batch_no唯一。订单139与批次142证明ID并非同一命名空间，不能把orderId直接当mainBatchId。

源码：ProductionService.createOrder独立创建订单；createBatch要求productionOrderId并锁父订单、核对同产品、分配总量；allocated允许同订单多个主批，总量不能超订单。completeOrderIfReady待全部子主批完成且分配总量=订单量才完成订单。因此历史业务确实允许Order 1:N MainBatch；不能以目前两组1:1就擅自添加order唯一约束或合表。

所有执行/eBR/QMS/平衡/QA/成品入库/发运/物料领料预留消费者以main_batch_id为生产事实身份（本次FK扫到20条主批被引用边）；production_order_id仅当前主批的父级兼容关系。API现有/production-orders和/main-batches各自创建/列表/编辑，生产过程状态/执行事实归MainBatch。新成品必须唯一以mainBatchId为入口，不能把两类ID混用。

已批准wms_finished_lot仅定义同一MainBatch的一对一库存属性扩展（UNIQUE(org_id,main_batch_id)，组合FKorg/batch/product），不是新生产/成品业务批次：不提供独立create/rename/重新编号入口、不复制产品或生成物料记录，产品只能从MainBatch获得；技术ID不构成另一业务编号。最新语义要求其批号显示直接取MainBatch.batch_no，用户不能另外输入lotNo。此前冻结草案的lot_no独立文本需在最终模型明确为派生镜像或移除；目前只保存了未执行V054草稿，未创建新表/新成品记录。

需主设计明确的额外结构/入口决策：

1. 建议新成品ledger显式main_batch_id/product_id，直接引用批次/产品，并以组合FK同时约束batch、产品、org、扩展属性及冻结单位；不只依靠ledger->技术lotID间接表达。属性表保留一对一技术键或改以main_batch_id为主键需明确后再改未执行迁移。
2. 统一“生产指令=生产批次”创建入口，推荐MainBatch唯一新写入口；Order仅兼容内部父记录/历史读取。原createBatch必须先选可独立创建Order，无法仅改标签消除两套入口。是否将新指令创建同事务自动建立内部Order、隐藏独立Order新建，以及是否限制新Order一对一，需一条明确新旧API/组织/数量/编辑合同；不得擅自更改历史1:N关系、旧路由权限或订单状态机。

这两点属于最新指令下待冻结的具体结构/接口，不自动重新选择领域模型。相关模型/入口实现暂停，其他已批准部分仅完成文档和初期QC/V2计划代码，整体尚未完成/验收。

当前迁移仍53；DEV材料16，id17/18=0，FINISHED=0；未应用V054，未改数据/账户/触发器，未重启/提交推送。隔离测试账号仍待核实，不能用DEV执行未测迁移。

## 用户目标图及父任务最新纠正 — 优先于旧独立lot草稿

图引用libfile_893928b2534c8191bd41cdc97e8af249。此处依据父任务Library文字提取，未取得像素、未宣称视觉检验：物料md_material→wms_material_receipt→md_material_lot→wms_inventory_ledger；产品md_product→prd_main_batch(生产批次/指令)→成品入库→wms_finished_inventory_ledger。

成品入库复用现有wms_finished_inbound_request，不按图拼写另造wms_finished_inventory_request。新ledger显式main_batch_id/product_id组合FK；不能只用finishedProductLotId间接关联。不独立成品批次、不自增lot ID、不新增批号/UI。旧V054未执行草稿已保留于docs/development/proposals/V054_PRODUCT_FINISHED_SUPERSEDED_DO_NOT_APPLY.sql，已移出Flyway加载目录；不复用执行过的迁移，不应用DEV。

两种最小实现差异，供父任务定案：

A. 一对一技术属性扩展表：保留wms_finished_lot名称但以main_batch_id作为PK/FK，无id/lot_no自增业务身份；org/product组合FK对应既有MainBatch。仅保存生产日、有效期、冻结产品单位/快照/digest、质量/库存状态和Scoped证据元数据。库存流水直接含main_batch_id/product_id，组合FK验证org/batch/product，并用org/batch/unit引用属性表；不再暴露finishedProductLotId独立业务ID。优点是仓储属性更新与主批状态版本职责变化较小；代价是多一张技术属性表、两个受控记录须同事务保持一致。

B. 批次表直接保存属性：不新增wms_finished_lot；prd_main_batch新增finished_manufacture_date、finished_expiry_date、finished_base_unit_id、finished_product_snapshot_json/digest、finished_quality_status、finished_inventory_status，旧列默认NULL、旧历史读取不改。新ledger直接org/main_batch_id/product_id/unit_id；增加必要支持索引+组合FK。库存属性与生产状态共享批次锁/版本，由生产聚合受控端口更新；QA必须把批次最终状态/质量/库存状态合成同一次乐观更新，避免两个服务各用旧version连续写批次。优点是一套batch主体、少一张表、直接对应目标图；代价是需要调整现有ProductionService/WMS/QA状态字段所有权与事务写入。

两方案均不新增物料/物料批次、共用仓库/库位/单位、不新增触发器、不改权限/旧签名；物料链不动。当前未选A/B，不改既有订单表结构、创建入口或旧状态机。后续正式新迁移仅在最新模型冻结后生成；原计划消费者新finishedProductLotId技术分支也需随定案改为直接batch引用，不能保留成另一业务批次。
