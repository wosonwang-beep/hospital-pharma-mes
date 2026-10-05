import {labels,display,schemas,type IncomingRow} from './incomingModel'

// Read presentation only: these names never change commands or persisted facts.
const names:Record<string,string>={
 lowerLimit:'标准下限',upperLimit:'标准上限',textAcceptanceCriteria:'文本判定标准',resultType:'结果类型',required:'必检项目',
 qcSpecificationItemId:'质量标准项目',revisionNo:'结果修订版本',previousRevisionId:'前一结果版本',attemptNo:'检验次数',
 recordedBy:'结果记录人',recordedAt:'结果记录时间',requestedBy:'请验人',submittedBy:'提交人',submittedAt:'提交时间',
 acceptedBy:'QC接收人',acceptedAt:'QC接收时间',sampledBy:'取样人',receivedBy:'样品接收人',samplingTaskId:'来源取样任务',
 samplingDetailId:'来源取样记录',detailNo:'取样明细序号',testCode:'检验项目编码',inspectionItemId:'检验项目记录',
 inspectionTaskId:'检验记录',signatureId:'电子签名编号',reviewSignatureId:'复核签名编号',planSignatureId:'方案批准签名编号',
 completionSignatureId:'取样完成签名编号',disposalSignatureId:'处置签名编号',approvalSignatureId:'报告批准签名编号',
 decisionSignatureId:'调查决定签名编号',closeSignatureId:'调查关闭签名编号',originalResultRevisionId:'原始结果版本',
 dispositionInvestigationId:'结果有效性调查',effectiveConclusion:'该结果版本的有效结论',investigationKind:'调查类型',
 decisionCode:'调查决定',decidedBy:'决定人',decidedAt:'决定时间',closedBy:'关闭人',closedAt:'关闭时间',closeReason:'关闭原因',
 decisionSource:'决定来源',decisionBy:'质量决定人',decisionAt:'质量决定时间',releaseScope:'放行范围',
 specificationContentHash:'冻结标准内容摘要',evidenceDigest:'证据摘要',ruleEvidence:'免验规则依据',evidenceIds:'证据记录引用',
 resultRevisionId:'报告选定结果版本',originalResult:'原始结果',result:'报告选定结果',recordVersion:'签名绑定版本',
 objectType:'签名对象类型',objectId:'签名绑定对象',slot:'签名用途',meaning:'签名含义',canonicalRecord:'签名时的记录快照',
 methodCode:'检验方法',methodVersion:'方法版本',items:'检验项目',executions:'检验执行',revisions:'结果修订历史',
 inventoryStatus:'库存状态',qualityStatus:'质量状态',recordStatus:'记录状态',status:'业务状态',code:'检查代码',message:'阻断说明',
}
export function detailLabel(key:string){return names[key]??labels[key]??key}
export function selectFacts(row:IncomingRow,keys:string[]){return Object.fromEntries(keys.filter(k=>Object.prototype.hasOwnProperty.call(row,k)).map(k=>[k,row[k]]))}
export function rows(value:unknown):IncomingRow[]{return Array.isArray(value)?value.filter(v=>v!==null&&typeof v==='object'&&!Array.isArray(v)):[]}
export function documentNumber(row:IncomingRow){return row.requestNo??row.samplingTaskNo??row.sampleNo??row.inspectionTaskNo??row.reportNo??row.deviationNo??row.materialLotId??row.id}
export const readSchemas:Record<string,string>={'inspection-requests':'InspectionRequest','sampling-tasks':'SamplingTask',samples:'Sample','inspection-tasks':'InspectionTask','inspection-reports':'InspectionReport',deviations:'Deviation',release:'ReleaseReview'}
export const auditTypes:Record<string,string>={'inspection-requests':'qms_inspection_request','sampling-tasks':'qms_sampling_task',samples:'qms_sample','inspection-tasks':'qms_inspection_task','inspection-reports':'qms_inspection_report',deviations:'qms_deviation',release:'MaterialLot'}
export function documentStatus(resource:string,row:IncomingRow){
 const value=row.status??row.qualityStatus,statuses=schemas[readSchemas[resource]??'']?.properties?.status?.enum
 return statuses&&value!==undefined&&!statuses.includes(String(value))?`未识别状态（${value}）`:display(value)
}
export function inspectionSummaryRows(items:IncomingRow[]){
 return items.map(item=>{
  const standard=item.resultType==='TEXT'?display(item.textAcceptanceCriteria):item.lowerLimit!=null&&item.upperLimit!=null?`${item.lowerLimit} – ${item.upperLimit}`:item.lowerLimit!=null?`≥ ${item.lowerLimit}`:item.upperLimit!=null?`≤ ${item.upperLimit}`:'—'
  const results=rows(item.executions).flatMap(execution=>{
   const revisions=rows(execution.revisions).slice().sort((a,b)=>Number(a.revisionNo)-Number(b.revisionNo))
   return revisions.length?[{execution,revision:revisions[revisions.length-1]!}]:[]
  })
  return {item,standard,results}
 })
}
export function signatureEntries(value:unknown,path='本记录'):Array<{context:string;metadata:IncomingRow}>{
 if(!value||typeof value!=='object')return []
 if(Array.isArray(value))return value.flatMap((v,i)=>signatureEntries(v,`${path} · ${i+1}`))
 const row=value as IncomingRow
 const current=rows(row.signatureEvidence).map(metadata=>({context:path,metadata}))
 return [...current,...Object.entries(row).filter(([k])=>!['signatureEvidence','signingTargets'].includes(k)).flatMap(([k,v])=>signatureEntries(v,`${path} / ${detailLabel(k)}`))]
}
export const slotNames:Record<string,string>={SAMPLING_PLAN:'取样方案批准',SAMPLING_TASK:'取样完成',SAMPLE_DISPOSAL:'样品处置',TEST_RESULT_REVISION:'检验结果签名',INSPECTION_TASK:'检验复核',DEVIATION_DECISION:'调查决定',DEVIATION_CLOSE:'调查关闭',INSPECTION_REPORT:'报告批准',MATERIAL_RELEASE_DECISION:'物料质量决定'}
export function detailDisplay(key:string,value:unknown){
 const enums:Record<string,string>={NUMERIC:'数值',TEXT:'文本',USER_QA:'QA人员决定',SYSTEM_RULE:'系统规则',VERIFY:'核验',APPROVE:'批准',RELEASE:'放行',REJECT:'拒绝',OOS:'超标结果',DEVIATION:'偏差'}
 const slots:Record<string,string>={plan:'方案批准',completion:'取样完成',disposal:'样品处置',review:'检验复核',decision:'质量决定',close:'调查关闭',approval:'报告批准'}
 if(key==='recordStatus'&&value==='EFFECTIVE')return '已生效'
 return key==='slot'?slots[String(value)]??'记录签名':key==='objectType'?slotNames[String(value)]??display(value):enums[String(value)]??display(value)
}
export const summaryFields=['requestNo','samplingTaskNo','sampleNo','inspectionTaskNo','reportNo','deviationNo','materialLotId','inspectionRequestId','sampleId','status','recordStatus','qualityStatus','inventoryStatus','versionNo','assignedTo']
export const sections:Record<string,Array<{title:string;keys:string[]}>>={
 'inspection-requests':[
  {title:'请验事实与冻结标准',keys:['receiptId','receiptItemId','materialLotId','qcSpecificationVersionId','specificationContentHash','requestType','requestedQuantity','requestedUnitId','requestedPackageCount','requestedDate','priority','reason','approvedInvestigationId']},
  {title:'提交与QC接收',keys:['requestedBy','submittedBy','submittedAt','acceptedBy','acceptedAt']},
  {title:'请验明细',keys:['items']}],
 'sampling-tasks':[
  {title:'取样方案',keys:['inspectionRequestId','inspectionRequestItemId','samplingPlan','requiredPackageCount','otherSampleName','otherSampleReason','assignedTo','planSignatureId']},
  {title:'取样执行与样品分配',keys:['startedAt','completedAt','details','completionSignatureId']}],
 samples:[{title:'样品身份与来源',keys:['sampleScope','sampleType','materialLotId','samplingTaskId','samplingDetailId','inspectionRequestItemId','sampledAt','quantity','unitId']},
  {title:'接收、储存与处置',keys:['receivedBy','receivedAt','storageLocation','disposalSignatureId']}],
 'inspection-tasks':[{title:'检验指派与复核',keys:['inspectionRequestId','sampleId','assignedTo','startedAt','submittedAt','reviewedBy','reviewedAt','reviewedResultIds','reviewSignatureId']}],
 'inspection-reports':[{title:'报告结论与复核批准',keys:['inspectionRequestId','overallResult','reviewedBy','reviewedAt','approvedBy','approvedAt','approvalSignatureId','supersedesReportId','evidenceDigest']}],
 deviations:[{title:'原始异常事实',keys:['investigationScope','investigationKind','materialLotId','testExecutionId','originalResultRevisionId','severity','description']},
  {title:'调查与质量决定',keys:['investigationSummary','decisionCode','disposition','authorizedRetestCount','decidedBy','decidedAt','decisionSignatureId']},
  {title:'最终选定结果与关闭',keys:['originalResultDisposition','selectedResultRevisionId','closeReason','closedBy','closedAt','closeSignatureId']}],
}
