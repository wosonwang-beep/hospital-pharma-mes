/**
 * 仅负责追溯关系的展示。业务边 ID/关系码仍使用 API 原值，
 * 不能把中文文案写回受控追溯数据。
 */
const relations:Record<string,string>={
 FINISHED_OUTPUT:'产生本批成品',
 WAREHOUSE_RECEIPT:'成品入库接收',
 SIGNED_EVIDENCE:'关联电子签名',
 RECEIPT_STOCK_FACT:'产生入库流水',
 FINISHED_INSPECTION:'发起成品检验',
 SAMPLING:'执行取样',
 CREATED_SAMPLE:'形成样品',
 DERIVED_REPORT:'汇总形成检验报告',
 INDEPENDENT_REPORT_REVIEW:'独立复核报告',
 SOURCE_RESULT:'报告引用检验结果',
 ORIGINAL_RESULT:'生成原始检验结果',
 INDEPENDENT_RESULT_REVIEW:'独立复核结果',
 TESTS_SAMPLE:'检验对应样品',
 QA_DECISION:'QA 放行决定',
 BASED_ON_REPORT:'决定依据检验报告',
 PHYSICAL_STOCK_MOVEMENT:'实际库存移动',
 SHIPPED_FROM:'从成品库存发货',
 AUTHORIZED_BY:'发货基于放行决定',
 SHIPMENT_DEBIT_FACT:'形成发货扣减流水',
 EXECUTES:'执行生产批任务',
 HAS_OPERATION:'包含生产工序',
 ACTUAL_CHARGE:'记录实际投料',
 CONSUMED_FROM:'投料消耗原料批',
 WEIGHING_EVIDENCE:'关联称量记录',
 WEIGHED_FROM:'从物料批称量',
 VERIFIED_BY_SIGNATURE:'称量复核签名',
 RECEIVED_AS:'形成收货批次',
 PART_OF:'所属业务单据',
 INSPECTED_UNDER:'按请验单检验',
 FROZEN_STANDARD:'采用冻结质量标准',
 SAMPLING_TASK:'安排取样任务',
 SAMPLING_POSITION:'执行取样点位',
 TEST_TASK:'生成检验任务',
 REQUIRED_ITEM:'包含必检项目',
 TEST_ATTEMPT:'实际检验执行',
 RESULT_REVISION:'生成结果版本',
 REPORT:'形成检验报告',
 FINAL_RESULT:'确定最终检验结果',
 QUALITY_DECISION:'质量放行决定',
 FROZEN_SUPPLY_SOURCE:'冻结合格供货来源',
 HANDOVER_ITEM:'关联发料明细',
 HANDOVER_LOT:'关联领料批次',
 WAREHOUSE_HANDOVER:'仓储发料交接'
}
export function traceRelationLabel(code:string):string{
 return relations[code]??code.replace(/_/g,' ').toLowerCase()
}
