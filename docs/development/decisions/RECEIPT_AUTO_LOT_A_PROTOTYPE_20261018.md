# 收货原型整改：内部批号自动分配（A）与生产来源

2026-10-18。授权来源：用户明确“选择 A：系统按批准的编号规则自动生成本次收货的内部物料批号。继续审查并修正生产企业、生产批号和物料收货原型。”

## 批准范围与未决事项
- 已批准：内部物料批号由系统分配、只读；选择物料自动带出编码/规格/单位；生产企业按物料与供货来源关联展示；生产批号按实物录入，不伪造供应商批号；继续整改隔离原型。
- 未批准/未查到：具体内部编号格式、流水域、赋号事务与作废策略、正式 DTO/DB 迁移。检索 docs/development、architecture、ui、v1.0.23 以及 WMS/System 源码，未找到物料收货自动编号规则；现有 lotNo 是人工传入。不可将“采用自动编号”误记为批准任意具体格式。
- 本轮原型使用单独的 PROTOTYPE_ONLY 编号适配器，返回自然业务格式的示例值，页面环境提示示例未核实正式批准；正式适配器在无批准来源时必须阻断。不能用前端流水作为生产环境编号服务。
- 不修改正式 MES 代码、DB、Flyway、API、权限、质量放行或冻结历史。G3.5 仍 NOT READY；本轮不是正式业务集成。

## 已读取依据
AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、Global UI V2、T1/T2/T3；business-product-manager V2.5、user-first-design、code-implementation-fidelity、end-to-end-ownership、hospital-mes Profile；business-product-design；mes-ui-ux-audit、ui-ux-pro-max；实际 ReceiptFlowPrototype / 旧 T1/T2/T3 / 统一流程提交记录；MaterialEntity、MaterialSupplierService、WmsService、WmsRules、WmsCommands。
GMP 原文核对：国家市场监管总局《药品生产质量管理规范》第104、106、107、111–113、182条，https://www.samr.gov.cn/zw/zfxxgk/fdzdgknr/bgt/art/2023/art_d5e1dbaa8f284277a5f6c3e2fc840d00.html 。本轮未获得2023指南完整正文，不将出版介绍当已阅读全文。

## 产品规格（原型级）
REQ-A01：选择物料→分配本收货明细内部批号；只读；同一明细重复操作、保存和编辑不重新赋号；不同新明细分别编号。格式仅由隔离适配器演示。
REQ-A02：物料编码、规格、包装与允许单位来自物料对象，不要求重复输入；生产企业来自具体物料—供货来源，不以供应商名称作兜底。
REQ-A03：生产批号独立于内部批号，按标签录入；删除供应商批号必填及生产商批号用语。旧后端 supplierLotNo 不偷换语义，正式兼容仍须 G3 变更。
REQ-A04：更换物料/供货来源后失效的生产企业、生产批号、日期与检查结果不得残留；不存在有效来源须明确提示。仓库变化清理不匹配库位，单位按物料过滤。
REQ-A05：新增、查看、编辑共用一个原型数据源；保存全部行，按 URL 读取正确收货单；刷新可读回；已确认单不可经手写编辑 URL 改为草稿；已保存行遵循保留规则。
REQ-A06：草稿可保存未完成检查，但未检查/不通过不允许确认。确认后不自动 QA 放行；不伪造检验结果。主数据缺失不默认已核验。
REQ-A07：数量逐条带单位展示，不将不同物料、不同单位盲目相加；不虚构附件和他人签名。
REQ-A08：保留现有统一流程视觉、T1/T2/T3；将旧 /wms/receipts 地址接到已存在的 ReceiptFlowPrototype，旧源文件保留，不制造第三套原型。新增按钮仍位于明细标题右侧，标签与控件同行。

## 写入白名单及测试
仅 mes-demo 的 ReceiptFlowPrototype.vue、receipt-flow.css、PrototypeRoot.vue、独立 receiptBusiness.ts、定向验证脚本与证据目录；正式仓库仅本授权记录、审查报告、MES_TASKS 当前状态增量。
保留本轮修改前源文件、SHA与截图。不覆盖旧测试报告，旧测试中的人工批号/供应商批号断言不再代表本次批准需求；新测试必须验证正向保存、回显、只读编号、生产企业、来源变化、单位、重复、已确认编辑拒绝及检查确认负例。
本次为用户明确批准的定向返工；此前失败证据保留，不清零历史修复。自动实施至验证限10分钟，新发现同根因一次修复后重复则停止。未完成矩阵保留 NOT RUN，不称 G2 全量验收。
