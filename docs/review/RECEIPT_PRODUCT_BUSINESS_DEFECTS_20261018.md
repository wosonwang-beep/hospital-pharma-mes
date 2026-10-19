# 物料收货 T2 — 业务模型缺陷与返工分析（2026-10-18）

状态：**G2 REWORK_REQUIRED / G3 NOT FROZEN / G3.5 NOT READY**。
性质：产品经理业务审查结论；不是批准的设计变更，也不是正式功能验收。不得自行修改已冻结字段、API、数据库或权限。

## 依据
- 本机已核对 `AGENTS.md`、`docs/development/MES_DEV_RULES.md`、`MES_TASKS.md`、`docs/PROJECT_BASELINE.md`、`docs/ui/MES_UI_PAGE_CATALOG_V1.md` 和 MES-008-R2 任务卡。
- `business-product-manager` V2.5 和 `business-product-design` 工作准则：先区分物料主数据、供货来源、实物批次、内部接收批次，才决定页面分组及联动。
- GMP《药品生产质量管理规范（2010年修订）》第106条：物料收货记录应能区分供应商/生产商（如不同）的名称、双方标识的批号（如不同）、接收后内部指定批号或流水号及内用物料代码；第107、111–113条：待验、按批管理、放行。
  官方核对源：https://www.samr.gov.cn/zw/zfxxgk/fdzdgknr/bgt/art/2023/art_d5e1dbaa8f284277a5f6c3e2fc840d00.html
- 实际源码：`backend/mes-masterdata/.../MaterialEntity.java` 包含 materialCode、specification、manufacturerName、baseUnitId/packUnitId；`MaterialSupplierEntity.java` 还有物料-供应商关系上的 manufacturerName。
- `MaterialSupplierService.freezeSource` 冻结 supplierName + manufacturerName；`backend/mes-wms/.../WmsCommands.java` ReceiptItemInput 包含 lotNo、supplierLotNo、manufacturerLotNo；`WmsService.saveItems` 规定 lotNo、supplierLotNo 必填而 manufacturerLotNo 可空。收货确认后新建 MaterialLot、收货库存流水，质量状态 QUARANTINE、库存状态 BLOCKED。
- 失误原型：`mes-demo/src/features/wms/ReceiptPagesPrototype.vue`。

## 经确认的问题
1. **物料选择只带名称**：缺少物料编码、规格、基础/包装单位等有来源的主数据回填。系统不能从“物料”主数据直接推断某一实际到货批次的“生产批号”；应区分物料编码/批号/收货内部编号。用户要求选择物料后自动带出批号，需进一步确认是“系统自动分配内部收货批号”还是“选取既有物料批次”。
2. **供货企业批号被当成所有收货的必填字段**：业务中供货企业可能仅转售厂家物料，不另行赋批号；强制输入“供应商批号”会促使用户虚构。GMP 并未要求供应商每次另造一批号。
3. **生产企业批号称谓不规范且不突出**：拟改用“生产批号（原厂批号）”，作为包装/出厂资料核对的批次身份；不能由选物料自动生成。
4. **缺少生产企业（厂家）信息**：应根据“物料 + 已批准供货企业”关联取得 `manufacturerName`，展示可追溯来源，并处理主数据缺失、包装标示不一致。既有冻结合同没有 ReceiptItemInput.manufacturerName 独立输入；如需保存核实的实物生产企业与主数据不同的情况，须先走有界 DCP。
5. **独立批次与物料编码混淆风险**：internal `lotNo` 由收货实例定义、确认时生成正式物料批记录；需核对医院内部编号规则和同物料不同批/分次到货情况，不能用 `materialCode` 当批号或硬编码示例值。
6. **编辑存在事实丢失**：现有原型编辑任意草稿硬塞一条“氯化钾”，未读原全部明细；违反原样回显、保留原明细和批次追溯要求。

## 产品设计整改规格（待批准，不直接改冻结合约）
- T2 基本信息：收货单号、供货企业、采购/随货单据、仓库、运输检查（草稿保存不要求全部已通过）。
- T2 每批明细：物料选择 → 自动展示物料编码/名称/规格/单位 → 与供货企业批准来源关系核对 → 显示生产企业 → 用户依据包装录入生产批号、生产日期、有效期/复验日期、收货数量及包装件数 → 按已有受控规则分配内部物料批号 → 逐项检查记录。若供货企业确实另有标签批号再记录，不强制编造。
- 物料关联来源缺失或无资格：停用受影响明细确认，原型显示明确错误；单据草稿能否继续编辑以获批状态机为准。
- 不同单位的物料不直接数值相加；同一收货单内多物料批次分别质量管控，确认收货≠检验合格≠QA放行。
- 编辑按 receiptId 读回**全部**已保存批次，维持其行身份/原值，严格执行已存在的记录保留与变更规则；取消、异常、冲突、保存后重读均需独立走查。
- T1/T3 仅在新 T2 联动所需范围增量同步，不重做已确认的视觉布局。

## 冻结合约冲突与审批门禁
- 后端现有 `ReceiptItemInput.supplierLotNo` 必填、`manufacturerLotNo` 可选，原型不能仅改 UI 提示就绕过；需要确认是否明确将现有源批号的存储语义由供货方批号调整为“生产批号”，还是以另外字段保存双方批次信息。
- 自动内部物料批号的生成规则（格式、赋号时点、重复收货和跨批号唯一性）不能凭空指定。
- 生产企业数据来源的优先级（材料主数据 vs 物料-已批准供货关系 vs 到货标签事实），以及与实物不一致时的处置和快照保存方式，需要产品经理依据已批准 SOP 建模。
- 改 DB/API/审核/冻结质量事实之前必须经授权 DCP；只读核对/记录本次返工不构成实施许可。

## 当前门禁与保护
- G2：**REWORK_REQUIRED**。上一轮浏览器 15/15 只证明有限交互，已被本次业务问题推翻其作为设计质量证据的适用性。
- G3：NOT FROZEN；G3.5：NOT READY；G4/G5：本轮未运行。
- 本轮只新增此产品评审记录，**未修改**源代码、数据库、已发布迁移、项目权限/基线和其他未提交文件。
