# PRODUCT_FINISHED_CONTRACT — Approved bounded contract 2026-10-11

设计状态：DESIGN AUTHORITY APPROVED / FROZEN。批准来源：父对话01a115d2-8ace-7574-9a08-ac48dfc970cc于2026-10-11明确批准报告主模型及七项限定补充；实现状态只维护MES_TASKS.md。本文件在限定范围内补充v1.0.23，所有历史release保持不变。

此稿替代 READONLY_DESIGN_REVIEW.md 第3节只有 QC/计划两个 ALTER 的不完整草案。设计方确认的方向已收到：MainBatch 为成品质量身份，product 从授权同组织批次推导，标准主体明确 MATERIAL/PRODUCT；修复必须覆盖产出和必要库存引用。详细物理模型已按本文件末尾批准补充冻结，禁止实现者更换模型。

## 1. 已取得的真实结构证据

当前 DEV：hospital_pharma_mes_dev，TCP 127.0.0.1:3306，MariaDB 13.0.2，foreign_key_checks=1，成功迁移最大值53。05:54:53Z 实际 information_schema/SHOW CREATE 表证据已保存，05:59:25Z 又只读核查单位、谱系和偏差。物料16，成品物料0，QC标准11，产品2，批次2；两个保留批次均 DRAFT。连接复用本机既有配置，没有输出凭据。

实际 finished/inventory/lot 命名表仅：md_material_lot；wms_inventory_ledger；wms_material_lot_inventory_decision；wms_finished_inbound_request；wms_finished_shipment；四个 qms_finished_* 表。没有独立成品批号、独立成品库存流水或成品余额表。Java 也仅有 MaterialLotEntity / LedgerEntity 对应当前库存。DCP-QC-OBJECT-TYPE-001 第36行的“已确认成品独立流水”是设计方向，不能据此宣称物理实现已存在。

实际表/实体关键段：

```text
md_product / ProductEntity:
  product_code, product_name, dosage_form, specification,
  base_unit_id NOT NULL -> md_unit.id, status ACTIVE/INACTIVE
md_material_lot / MaterialLotEntity:
  material_id NOT NULL -> md_material.id
  lot_no, manufacture_date, expiry_date, receipt_item_id -> wms_material_receipt_item
  material_snapshot_json NOT NULL, requires_incoming_inspection_snapshot NOT NULL
  quality_status, inventory_status
  UNIQUE(org_id,lot_no), UNIQUE(receipt_item_id)
wms_inventory_ledger / LedgerEntity:
  material_lot_id NOT NULL -> md_material_lot.id
  location_id -> wms_location, container_id nullable -> wms_container
  delta_qty DECIMAL(18,6) NOT NULL CHECK <>0, unit_id -> md_unit
  event_type, source_type, source_ref, idempotency_key, occurred_at
  UNIQUE(org_id,idempotency_key)
qms_production_plan:
  main_batch_id NOT NULL -> prd_main_batch.id, UNIQUE(org_id,main_batch_id)
  finished_material_id NOT NULL -> md_material.id
  qc_specification_version_id NOT NULL -> qc_specification_version.id
qc_specification:
  material_id NOT NULL -> md_material.id; 没有 subject_type 或 product_id
prd_main_batch.finished_lot_id -> md_material_lot.id (nullable)
wms_finished_inbound_request / shipment:
  material_lot_id NOT NULL -> md_material_lot.id
  ledger_id nullable -> wms_inventory_ledger.id
qms_finished_inspection_request / sampling_record:
  material_lot_id NOT NULL -> md_material_lot.id
qms_release_decision.finished_lot_id -> md_material_lot.id
  ck_finished_release_target 对 FINISHED_PRODUCT 强制旧 finished_lot_id NOT NULL
mes_quantity_event.material_lot_id / qms_sample.material_lot_id:
  nullable -> md_material_lot.id
```

以上外键 DELETE/UPDATE 均 RESTRICT。完整列、默认、检查、唯一键、被引用边见 CURRENT_PRODUCT_FINISHED_SCHEMA_EVIDENCE.md；原始实际库响应见 PRODUCT_FINISHED_CURRENT_SCHEMA_PRIVATE.json。绝不能把新成品表ID直接放进旧列：新旧自增ID可能碰撞且旧FK绑定不同实体。

## 2. FinishedOutputInventoryService 精确调用链

1. ProductionQuantityService.record(:35–52)：事务锁订单/主批、If-Match/批次 IN_PROGRESS；从冻结 qualityPlan.finishedMaterialId 取旧物料；组成 ExecutionOutputInventoryPort.Output(:47)，调用 receiveOutput。
2. ProductionQuantityAdapters(:14–18) 委托 WMS FinishedOutputInventoryService.receive；目前 Output.record 含 primitive materialId，并没有 productId 或 typed product lot。
3. FinishedOutputInventoryService(:20–32)：MANDATORY 同一事务；真实生产/有效期、有效仓库/库位；requireUsable(material)+FINISHED；创建或锁 md_material_lot，冻结 materialSnapshot；用材料 baseUnitId 和材料换算证明精确数量。
4. receive(:33–36) 明确只建立成品批号身份，不 append 物理流水。随后 Execution 写签名 QuantityEvent，ProductionService.quantityEvidenceRecorded 将旧 finished_lot_id 挂到批次，同事务审计和幂等。
5. 反向(:38–53)：新延迟入库链只追加签名观察冲销，无库存变化；历史曾直接 PRODUCTION_OUTPUT 入账的分支才追加精确负流水，禁止已消耗或最终QA状态正常冲销。
6. FinishedGoodsService.snapshot(:40–45) 从 OUTPUT 及对应 REVERSAL 聚合实际净产量；目前同旧 lot 及 materialId 转至材料冻结基本单位。createInbound(:48–50) 要 PRODUCTION_COMPLETED，服务器冻结来源摘要、产品、批号、量/单位。确认(:58–62) 重算摘要，独立人员签名；新链仅确认时写一次 FINISHED_RECEIPT。历史 PRODUCTION_OUTPUT 库存须位置/单位/净量完全相等，仅确认已有实物，不能重加库存。
7. FinishedInspectionService(:52/55/60–67)：批次计划签名有效；草稿可早建，submit/accept/sample 要确认入库；从计划 finishedMaterialId 进行取样换算；累计采样量不得超过确认量；记录和报告从来源继承批号/标准/样品。
8. FinishedReleaseService(:34/57–61) + FinishedQualityDecisionService(:15–26)：锁批次、批号和全部流水；匹配计划材料/实际批号，核对有效期、正实物量、签名QC/eBR/平衡；QA 只修改批号质量及可用状态，不增加库存；已消耗放行库存不可普通追溯拒绝。
9. FinishedGoodsService.shipmentAction(:80–82)：匹配当前同批签名 RELEASED 决定、有效库位、有效期；锁库位流水、减预留；足量后一次负 FINISHED_SHIPMENT，签名/决定/流水引用同事务。
10. EBR/追溯/导航仍读旧 finished_lot_id/materialLotId/旧 ledger。NavigationQueryService(:24–25) 单筛旧列，若只新增成品表而漏掉此投影，新产品批次将从QA/放行入口消失。

逐行原文已保存在 CURRENT_PRODUCT_FINISHED_SOURCE_EVIDENCE.md，24个文件时间/hash在 SUPPLEMENT_SOURCE_HASHES.json。

## 3. 单位、规格和数量的完整来源

| 内容 | 真正权威来源 | 新产品分支最小规则 |
|---|---|---|
| 产品身份 | prd_main_batch.product_id，同组织锁定批次，再查询 md_product | 不接受客户端 productId；审批/调度复核与冻结值相符 |
| 产品规格 | md_product.specification / dosage_form，当前 ProcessQueryService.productLabels 仅给显示文本 | 冻结展示文本；不能解析为换算或 QC验收标准 |
| 产品基本单位 | md_product.base_unit_id -> md_unit | 计划审批冻结产品快照，含基本单位/规格/摘要；新成品批号固化此单位 |
| 计划生产量 | prd_main_batch.planned_qty/unit_id，生产订单/子批分配规则 | 与产品基本单位须相同或已有同维度通用精确换算；不将计划量当实物库存 |
| 实测产出 | 签名 mes_quantity_event.amount/unit_id，OUTPUT及同源REVERSAL | 转冻结产品基本单位、保留每笔来源和换算证明，正净量才可申请入库 |
| 入库量 | 完成批次实际净OUTPUT，服务器 sourceSnapshot/sourceDigest | 不能客户端任意改量；确认重算来源；确认一次才入账 |
| 取样量 | 签名采样记录quantity/unit_id，当前采样记录18,6；Sample24,8 | 精确换算至确认入库单位后累计<=入库量；目前采样记录不生成物理出库，不隐式改变该业务合同 |
| QA可用量 | 确认入库实物净流水，批次/批号匹配且证据有效 | QA无数量流水，仅 QUARANTINE/BLOCKED -> RELEASED/AVAILABLE 或 REJECTED/BLOCKED |
| 发运量 | 发运申请实际quantity/unit_id，签名确认 | 基本单位精确借记，库位余额充足，确认仅一次，禁止负库存 |
| QC检测限值/单位 | qc_specification_item lower/upper/unit_id/text criteria/method code+version | 复用签名标准版本；检测量纲与库存单位不同，不能用产品规格替代检测项目 |
| 平衡量/单位 | 冻结 balanceRules.unitId + 已确认领料/退料、实投、产出、样品、损失等真事实 | 每笔精确换算；保留规则/输入/转换ID/factor/摘要及原调查签名门禁 |

实际两个产品 KCL30/HYTEA100 的 base_unit_id=3(ml,VOLUME,scale3)；规格分别30ml:3g/瓶、100ml/瓶。批次18/142均产品1、单位ml，计划量1000/120000。当前通用换算仅 kg→g(1000)、g→kg(.001)、L→ml(1000)、ml→L(.001)；没有瓶↔ml、质量↔体积转换。

MasterQueryService(:15–28) 的 nullable material 参数：材料专属优先，否则通用；MasterRules.convert：不同维度且无材料专属证明拒绝；targetScale按 HALF_EVEN返回，成品/平衡调用另验证精确相等，不能静默舍入。新产品始终 material=null，只用同单位或已配置同维度通用换算；不借用产品ID当材料ID、不按规格文字推瓶数、不新增产品跨维度转换设施。

MaterialBalanceService(:84–109) 当前通过数量事件 materialLotId 取 materialId 选择/换算。需增加明确成品来源分支：原料身份仍材料，成品身份PRODUCT；产品绝不进入 materialId。规则材料过滤只能选择真实输入材料。混合来源组应按明确材料/产品主体识别并只用通用换算；产品来源只能通用换算，单材料规则继续原专属换算。跨MASS/VOLUME/COUNT无有效定义仍阻塞，不能为跑通seed虚构因子或放宽平衡检查。批准具体规则时需同时确认这一边界。

## 4. 已批准物理方案

推荐新增**独立、明确产品归属**的成品批号及成品流水，复用现有仓库/库位、事务、审计、签名、幂等设施。理由：实际没有可复用的独立成品实体；DCP已明确独立流水方向；把 md_material_lot 扩成 MATERIAL/PRODUCT/任意质量对象通用多态库会改变来料/WMS材料契约，且所有材料服务目前假定 getMaterialId 非空。独立成品两表避免该更大变更。

不迁移旧批号/旧流水、不移动签名事实，不删旧列/FK，不把旧物料记录映射产品。旧链只读时按原冻结模式解析；新链只能 PRODUCT。新表/新列ID不能复用旧API字段含义。

### 4A. QC主体和标准版本：2张旧表

- qc_specification：新增 subject_type VARCHAR(20) NOT NULL DEFAULT 'MATERIAL'；product_id BIGINT NULL FK md_product(id) RESTRICT；material_id 从 NOT NULL 改可空，旧FK保留。
- CHECK：`(subject_type='MATERIAL' AND material_id IS NOT NULL AND product_id IS NULL) OR (subject_type='PRODUCT' AND material_id IS NULL AND product_id IS NOT NULL)`。不使用QC_OBJECT_TYPE五类枚举表示标准主体。
- qc_specification_version：新增 definition_schema_version SMALLINT NOT NULL DEFAULT 1、CHECK IN(1,2)。所有现存版本=1；PRODUCT新版本=2；服务禁止主体/类型变更并验证版本模式与主体一致。复用现有版本号、DRAFT/APPROVED/RETIRED、检测项、独立审核、批准/退役签名；不创建第二套产品标准/版本表。
- 旧 MATERIAL定义/内容hash/signable/历史Snapshot严格走当前 QcDefinitions.definition(:24)/snapshot(:31)原结构，不能加 subjectType/productId:null/definitionSchemaVersion 等字段改变canonical。PRODUCT V2新canonical显式definitionSchemaVersion=2、subjectType='PRODUCT'、productId、原标准版本/项目内容；新产品投影与旧 SpecificationSnapshot分支区分，primitive long materialId不能承接null/product。
- 11条旧标准全部MATERIAL。不得自动关联产品，不重签或重算历史摘要。

### 4B. 计划：1张旧表

- qms_production_plan：新增 plan_schema_version SMALLINT NOT NULL DEFAULT 1，CHECK IN(1,2)；finished_material_id 改可空但旧FK保留。
- CHECK：`(plan_schema_version=1 AND finished_material_id IS NOT NULL) OR (plan_schema_version=2 AND finished_material_id IS NULL)`。原行按V1原结构读取；新计划只V2，不允许创建新legacy FINISHED-material计划。
- 不新增独立可编辑 product_id。已有 main_batch_id 是唯一业务身份；审批冻结JSON保存 productId/productSnapshot/baseUnitId，以及明确FINISHED_PRODUCT+MainBatch对象及主体PRODUCT标准快照。审批/调度校验锁定batch当前产品与冻结产品一致，处理 DRAFT批次产品变更后的陈旧计划，不能仅创建时检查。

### 4C. 两个新增库存表

`wms_finished_lot`（批准名称）
- Scoped字段：id BIGINT PK，org_id、created_by/at、updated_by/at、version_no；保持现有组织及乐观锁规则。
- main_batch_id BIGINT NOT NULL FK prd_main_batch、product_id BIGINT NOT NULL FK md_product；UNIQUE(org_id,main_batch_id)，UNIQUE(org_id,lot_no)。同一批次唯一成品批号；产品只能由该同组织batch获得。
- lot_no VARCHAR(100) NOT NULL；manufacture_date DATE NOT NULL；expiry_date DATE NOT NULL，CHECK expiry_date>manufacture_date；运行时仍检查真实生产日和当前有效期。
- base_unit_id BIGINT NOT NULL FK md_unit；product_snapshot_json LONGTEXT NOT NULL CHECK JSON_VALID；product_snapshot_digest CHAR(64) NOT NULL（小写hex）。快照来自批准计划/调度冻结产品，批号不受后续主数据改动漂移。
- quality_status VARCHAR(30) CHECK IN('QUARANTINE','RELEASED','REJECTED')；inventory_status VARCHAR(30) CHECK IN('BLOCKED','AVAILABLE','FROZEN')；新产出为QUARANTINE/BLOCKED。不新增状态、冻结动作或来料字段。
- 产品归属/批次/批号/单位/快照建成后不可普通修改，不开放删除；QA状态变更仍专门命令、审计、签名和版本锁。

`wms_finished_inventory_ledger`
- 同Scoped行字段，但append-only；finished_product_lot_id BIGINT NOT NULL FK wms_finished_lot；location_id BIGINT NOT NULL FK现有wms_location；unit_id BIGINT NOT NULL FK md_unit；quantity delta_qty DECIMAL(18,6) NOT NULL CHECK <>0。
- event_type VARCHAR(30) 仅 FINISHED_RECEIPT / FINISHED_SHIPMENT；inbound_request_id BIGINT NULL FK现有入库表、shipment_id BIGINT NULL FK现有发运表；CHECK收货正数且仅inbound引用，或发运负数且仅shipment引用。不要无类型object_id库存模型。
- idempotency_key VARCHAR(100) NOT NULL，occurred_at DATETIME(3) NOT NULL；UNIQUE(org_id,idempotency_key)、UNIQUE(org_id,inbound_request_id)、UNIQUE(org_id,shipment_id)。每申请确认唯一入账；源表签名/确认信息承担原审计语义。
- 全部新流水存冻结成品基本单位。可用支持唯一键 `wms_finished_lot(org_id,id,base_unit_id)` + 组合FK `(org_id,finished_product_lot_id,unit_id)` 强制同组织/同单位。全部新FK RESTRICT，不能CASCADE；本次不新增、删除或削弱任何触发器。新流水通过受控追加服务、事务、约束、审计签名及不可变实体保证应用层不可变；不宣称数据库可阻止任意SQL改写。
- 不创建可独立编辑余额表；库存按本表同org/lot/location求和，追加与来源确认同事务且持批次/lot/来源/流水锁。新收货和发运只写新表，原物料领料/预留/称量继续旧ledger。

批准强制组合FK：md_product增加UNIQUE(org_id,id)；prd_main_batch增加UNIQUE(org_id,id,product_id)。wms_finished_lot以(org_id,product_id)引用md_product(org_id,id)，以(org_id,main_batch_id,product_id)引用prd_main_batch(org_id,id,product_id)。新lot增加UNIQUE(org_id,id,base_unit_id)，新ledger以(org_id,finished_product_lot_id,unit_id)引用它。所有规则DELETE/UPDATE RESTRICT；账户/权限不变。因此去重实际影响为2张新表+12张既有表（原11张中已包含prd_main_batch，另加md_product支持索引）。

### 4D. 必要下游：8张旧表的有类型追加列

所有新 `finished_product_lot_id BIGINT NULL` FK wms_finished_lot(id) RESTRICT。旧FK列保持原含义。强制互斥，不允许只置空而缺失新成品身份。

| 表 | 精确新增/可空变更 | 新约束 |
|---|---|---|
| prd_main_batch | 新 finished_product_lot_id；旧 finished_lot_id已可空 | 两者不得同时非空；新产品写路径只新列；lot必须属于同batch/产品 |
| mes_quantity_event | 新 finished_product_lot_id；旧 material_lot_id已可空 | 两者不得同时非空；新列只允许OUTPUT/SAMPLE/LOSS/SCRAP/WIP/对应REVERSAL；材料领料/实投保持旧列 |
| qms_sample | 新 finished_product_lot_id；旧 material_lot_id已可空 | 两者互斥；新列仅PRODUCTION；INCOMING_MATERIAL原所有必填约束保留且新列必须空；过程样品可继续仅batch |
| qms_release_decision | 新 finished_product_lot_id；旧 finished_lot_id可空 | FINISHED_PRODUCT必须旧/新lot恰一；替换ck_finished_release_target的旧lot单一要求为此恰一，其余QA/签名/inspection/basis条件完整保留；incoming新列必须空 |
| wms_finished_inbound_request | 新 finished_product_lot_id、finished_ledger_id(FK新ledger)；旧material_lot_id改可空，ledger_id原可空 | 旧/新lot恰一；旧lot不能新ledger、新lot不能旧ledger；新链CONFIRMED必须新ledger，旧多流水确认历史允许原ledger_id空 |
| wms_finished_shipment | 同上 | 旧/新lot恰一；ledger归属模式互斥；新链CONFIRMED必须新ledger；原release/signature确认约束保留 |
| qms_finished_inspection_request | 新 finished_product_lot_id；旧material_lot_id改可空 | 旧/新lot恰一；mainBatch/inbound/标准/组织/冻结对象一致，沿来源继承不可客户端替换 |
| qms_finished_sampling_record | 同上 | 旧/新lot恰一；沿请验和Sample继承，同源；原签名非空约束保留 |

因此最小完整建议是 **2新表 + 11旧表受控扩展**（QC根/版本、计划、8个消费者），不是原报告两条ALTER。

当前 mes_genealogy.input_material_lot_id NOT NULL仍为真实输入；output_lot_id可空且当前源码没有setOutputLotId写入者。新链可按既有main_batch_id连接批次新成品lot，在只读谱系/eBR冻结时输出明确产品批号，不要求新增谱系写入业务或回填旧output_lot_id。qms_deviation生产调查本就通过mainBatch/productionTestInstance/originalResult链定位Sample，createDeviation没有写material_lot_id；来料material_lot及原约束不改，不为新链添加不必要多态偏差列。

qms_finished_inspection_report/review、QC结果修订/审核、电子签名及审计表不需实体FK改造；它们通过现有request/sample/test关系和冻结JSON承载新来源。旧归档PDF/打印/报表历史继续旧事实，绝不重生成覆盖；新归档必须区分新lot/新ledger读取。完整真实md_material_lot被引用边已列附录，未将材料专用reservation/issue/weighing/receipt等表统统改为多态。

## 5. 已批准API及集成变化

- 原生产计划路径/权限不变；V2 POST/PUT只接mainBatchId、qcSpecificationVersionId、balanceRules、reason（更新原versionNo/If-Match）；productId/finishedMaterialId未知字段拒绝。响应与新冻结内容明确planSchemaVersion=2、inspectionObjectType=FINISHED_PRODUCT、inspectionObjectId=mainBatchId、派生productId/产品信息。
- QC根新命令subjectType=MATERIAL时仅materialId；PRODUCT时仅productId；不得两ID同传/都空。material查询/来料requireSelectable保持；追加产品标准明确查询/校验分支，仅同组织同产品APPROVED签名版本。版本生命周期和权限复用。
- 新成品产出/入库/请验基于mainBatchId/来源ID推导产品与新lot，不接受客户端指定另一个产品或使用旧materialLotId伪装成品。新增finishedProductLotId/finishedLedgerId用于新投影与适用命令；旧finishedLotId/materialLotId/ledgerId只保留原legacy含义，不能无判别复用新表ID。
- ProductionQueryService.QualityIdentity、ExecutionOutputInventoryPort/boot adapter、WMS成品查询和release port增加明确新产品批号分支；单靠 long lotId 自动查两表不可接受。原料接口不改。
- 收货/发运当前严格字段、version、签名、来源摘要、幂等、独立人、状态门禁全部保留；不新增submit到只有DRAFT->APPROVED的生产计划。
- MaterialBalanceService/导航/EBR/trace、成品UI引用显示都需同步到新typed identity；旧对象canonical读路径独立保留，新签名引用使用明确FinishedProductLot，而非MaterialLot字符串伪装。
- 未实现INTERMEDIATE/ENVIRONMENT/PROCESS resolver仍拒绝正式提交；本次不设计监测点/通用请验新业务，不改incoming。

## 6. 不变的库存数量规则 / 兼容策略

新成品量：`netOutput = Σ实际签名OUTPUT + Σ对应签名REVERSAL`，同批、同新lot，每笔转冻结产品基本单位。OUTPUT不增加库存，生产中观察冲销不减物理库存。正净量完成生产后冻结入库来源；确认摘要相等才 `+netOutput` 一次。确认收货后仍隔离/不可用；请验/QC/采样不自行放行或产生出库；QA只决定可用性；签名发运才 `-debit` 一次。`physicalBalance(org,lot,location)=Σ新ledger.delta_qty`，不能负数，不能靠单据DRAFT量计算库存。

新成品不接入原物料预留/领料/称量；禁止将其交给MaterialQueryService。当前发运减材料预留只属于legacy分支；新产品分支没有材料预留，不能查询相同数字旧lot ID的预留。材料物流不会改动产品成品流水。若要新增成品专属预留/移动/冻结执行接口，须另外明确范围，不能此轮预实现；原受控库存状态拒绝逻辑继续保留。

读兼容靠实际列/schema版本与冻结内容分支，不推测ID、不搬迁旧历史。已有签名/hash/canonical/ProcessSnapshot/历史eBR/PDF维持原字节；新增表是新记录，新写ONLY PRODUCT。迁移新增列给旧行确定legacy默认，只添加模式元数据，不UPDATE既有业务JSON/签名；数据库/接口/RTM/UI/测试/集成文件须同一受控补充一致。

DDL须新physical Flyway版本，当前候选V054仅供审查，实施前重读成功最大值/checksum及工作树，避免其他任务已占用。不得改V014/V025/V030/V053、repair或弱化签名触发器。附加列/FK/check要按先新lot、新ledger/消费者列、再交叉FK顺序，历史每行须提前满足保留分支，否则停下。应用层可回退读旧链，但新成品数据存在后不能假称旧二进制可无损继续业务；回退需受控保留新数据并停止新写，不能删表回滚。

## 7. 后续验证范围与当前未执行事项

- 实施前重核29+24源文件hash、Git差异、实际schema成功迁移值；保护用户local/ci配置和全部已有未跟踪证据。
- 迁移前后旧11标准定义/hash/签名、产品/材料全部行、历史snapshot/EBR归档摘要不变；新约束正反例、FK/同组织/互斥/状态/并发/幂等。
- 只用唯一新测试记录或事务回滚：在无FINISHED material条件下，产品标准->batch计划批准->调度->产出->净量收货->QC/QA->发运；库存确认前0、确认后净产量、QA量不变、发运单次减量且不负、无新孤儿。旧材料incoming/标准canonical及历史读取回归。
- 两处 RealisticProductionFinishedSeedIT（demo、production）均@Commit/硬编码17，demo还normalize该物料类型。必要修复删除17/18依赖及FINISHED normalize，不改现有产品/原料记录，使用真实批次派生产品与独立新增产品标准；**绝不能运行原seed**。先回滚fixture验证；是否执行改后持久seed另须明确，不能拿既有@Commit当当前允许写库。
- UI沿现有模板和同水平标签/控件；若成为实质设计改造仍需精确Vue原型hash/DESIGN LOCK审批及真实5173/8080验收。此轮没有浏览器验收或PASS声明。

以下为批准实施限定，不把设计批准当作测试通过或DEV部署批准。运行状态见MES_TASKS.md。

## 8. 批准补充（优先于上文原审查建议）

1. 主模型固定；新lot org+batch+product和新ledger org+lot+unit组合FK必需。来源单据org/lot由服务器受权查询及事务锁复核，禁止前端填写派生身份。
2. ledger与来源双向FK是同事务：先锁订单/批次，再lot，再来源，再按ID升序锁流水；插入ledger后更新来源CONFIRMED和对应finishedLedgerId。CHECK只限制本行，来源唯一/幂等唯一防重；不同org/lot ledger及负余额拒绝。不实现跨行触发器。
3. 不新增、删除或削弱任何触发器；不改变数据库账户/权限。应用层不可变保证及其任意SQL保护局限必须明确。
4. OUTPUT不入库存；确认入库唯一正流水；QA不改量；发运唯一负流水。新链不查旧材料预留。仅相同或既有同维度精确通用换算，采样继续不产生物理库存流水。
5. 历史canonical/hash/signature/snapshot/archive原样读取，不把新ID写旧列。新读取同步EBR/导航/谱系/打印；不重生成历史文件。
6. 两处种子移除17/18及FINISHED normalize；先独立回滚fixture全链/负例；不运行原@Commit种子、不在DEV制造FINISHED物料、不改既有产品。其他未实施质量对象拒绝。
7. UI不重构：现有生产计划批次→只读产品→产品标准，同水平标签/现有组件/权限/路由；准确契约见decisions/PRODUCT_FINISHED_DESIGN_LOCK_20261011.md。禁止无关页面。
8. 最新用户明确批准本次隔离测试库验证，构成本次对默认单DEV测试环境的有界例外：只允许用于迁移与回滚验证，不创建第二开发/运行环境，不连接PROD，不复制工作树。本机访问方式尚未核实，不能以DEV替代未测迁移。不得直接应用DEV、不擅自重启、不提交推送。
9. 复杂跨模块迁移任务单次实施预算60分钟，自动修复最多2轮；按阶段记录证据，到预算/重复失败即BLOCKED，不自动续期开新任务。

表影响计数说明：11张字段扩展表包含prd_main_batch；其新增支持索引不重复计表，md_product额外增加支持索引，去重共12张已有表及2张新增表。
