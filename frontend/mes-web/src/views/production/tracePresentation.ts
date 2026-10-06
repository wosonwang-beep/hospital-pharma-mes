export interface TraceNode {type:string;id:string;label?:string;status?:string|null}
export interface TraceEdge {sourceType:string;sourceId:string;targetType:string;targetId:string;relation:string}
export const traceTypeNames:Record<string,string>={MAIN_BATCH:'生产批',EXECUTION_UNIT:'执行单元',MATERIAL_LOT:'物料批次',RECEIPT:'收货记录',RECEIPT_ITEM:'收货明细与来源',SUPPLIER:'供应商',MATERIAL_REQUEST:'领料申请',MATERIAL_ISSUE:'出库单',MATERIAL_ISSUE_ITEM:'出库明细',ISSUE_RETURN:'退料记录',INSPECTION_REQUEST:'请验单',SAMPLING_TASK:'取样记录',SAMPLING_DETAIL:'取样明细',SAMPLE:'样品',INSPECTION_TASK:'检验记录',INSPECTION_ITEM:'检验项目',TEST_EXECUTION:'检验执行',RESULT_REVISION:'结果修订',REPORT:'检验报告',RELEASE_DECISION:'质量放行决定',INVESTIGATION:'质量调查',OPERATION:'工序',CHARGE:'投料记录',WEIGHING:'称量记录',SIGNATURE:'电子签名',QC_SPECIFICATION_VERSION:'冻结质量标准版本',INVENTORY_DECISION:'库存控制记录',ATTACHMENT:'随货资料',FINISHED_INBOUND_REQUEST:'成品入库申请与接收',FINISHED_INSPECTION_REQUEST:'成品请验单',FINISHED_SAMPLING_RECORD:'成品取样记录',FINISHED_REPORT:'成品检验报告',FINISHED_REPORT_REVIEW:'成品报告复核',PRODUCTION_TEST:'生产/成品检验记录',RESULT_REVIEW:'检验结果复核',FINISHED_SHIPMENT:'成品发货出库',INVENTORY_LEDGER:'库存流水'}
const paths:Record<string,string>={MAIN_BATCH:'/production/batches',EXECUTION_UNIT:'/mes/execution',MATERIAL_LOT:'/wms/material-lots',RECEIPT:'/wms/receipts',SUPPLIER:'/master/suppliers',MATERIAL_REQUEST:'/wms/requests',MATERIAL_ISSUE:'/wms/issues',INSPECTION_REQUEST:'/quality/inspection-requests',SAMPLING_TASK:'/quality/sampling-tasks',INSPECTION_TASK:'/quality/inspection-tasks',REPORT:'/quality/inspection-reports',INVESTIGATION:'/deviations',FINISHED_INBOUND_REQUEST:'/finished/inbound',FINISHED_INSPECTION_REQUEST:'/finished/requests',FINISHED_SAMPLING_RECORD:'/finished/sampling',FINISHED_REPORT:'/finished/reports',PRODUCTION_TEST:'/quality/production-tests',FINISHED_SHIPMENT:'/finished/shipments'}
const permissions:Record<string,string>={MAIN_BATCH:'production:batch:view',EXECUTION_UNIT:'mes:operation:view',MATERIAL_LOT:'wms:inventory:view',RECEIPT:'wms:receipt:view',SUPPLIER:'master:supplier:view',MATERIAL_REQUEST:'wms:request:view',MATERIAL_ISSUE:'wms:issue:view',INSPECTION_REQUEST:'qms:inspection-request:view',SAMPLING_TASK:'qms:sampling:view',INSPECTION_TASK:'qms:test:view',REPORT:'qms:report:view',INVESTIGATION:'qms:deviation:view',QC_SPECIFICATION_VERSION:'qms:specification:view',FINISHED_INBOUND_REQUEST:'wms:finished-inbound:view',FINISHED_INSPECTION_REQUEST:'qms:finished-request:view',FINISHED_SAMPLING_RECORD:'qms:finished-sampling:view',FINISHED_REPORT:'qms:finished-report:view',PRODUCTION_TEST:'qms:test:view',FINISHED_SHIPMENT:'wms:finished-shipment:view'}
export function traceDestination(node:TraceNode,edges:TraceEdge[],can:(permission:string)=>boolean):string|undefined{
 const parent=(type:string,id:string,sourceType:string)=>edges.find(e=>e.targetType===type&&e.targetId===id&&e.sourceType===sourceType)?.sourceId
 if(node.type==='SAMPLE'){
  const sampling=parent('SAMPLE',node.id,'FINISHED_SAMPLING_RECORD')
  return sampling?can('qms:finished-sampling:view')?`/finished/sampling/${sampling}`:undefined:can('qms:test:view')?`/quality/samples/${node.id}`:undefined
 }
 if(node.type==='QC_SPECIFICATION_VERSION')return can('qms:specification:view')?`/quality/specification-versions/${node.id}/edit`:undefined
 if(node.type==='CHARGE'||node.type==='OPERATION'){
  const operation=node.type==='CHARGE'?parent('CHARGE',node.id,'OPERATION'):node.id
  const unit=operation?parent('OPERATION',operation,'EXECUTION_UNIT'):undefined
  if(!unit||!can(node.type==='CHARGE'?'mes:charge:view':'mes:operation:view'))return undefined
  if(node.type==='CHARGE')return can('mes:charge:create')?`/mes/execution/${unit}/charge`:can('mes:operation:view')?`/mes/execution/${unit}`:undefined
  return `/mes/execution/${unit}`
 }
 if(node.type==='MATERIAL_ISSUE_ITEM'||node.type==='ISSUE_RETURN'){
  const item=node.type==='ISSUE_RETURN'?parent('ISSUE_RETURN',node.id,'MATERIAL_ISSUE_ITEM'):node.id
  const issue=item?parent('MATERIAL_ISSUE_ITEM',item,'MATERIAL_ISSUE'):undefined
  return issue&&can('wms:issue:view')?`/wms/issues/${issue}`:undefined
 }
 if(node.type==='FINISHED_REPORT_REVIEW'){
  const report=parent(node.type,node.id,'FINISHED_REPORT')
  return report&&can('qms:finished-report:view')?`/finished/reports/${report}`:undefined
 }
 if(node.type==='RELEASE_DECISION'){
  const lot=parent(node.type,node.id,'MATERIAL_LOT'),batch=lot?parent('MATERIAL_LOT',lot,'MAIN_BATCH'):undefined
  if(batch&&can('qa:batch-review'))return `/qa/batches/${batch}/review`
  return lot&&can('wms:inventory:view')?`/wms/material-lots/${lot}`:undefined
 }
 return paths[node.type]&&can(permissions[node.type]??'')?`${paths[node.type]}/${node.id}`:undefined
}
