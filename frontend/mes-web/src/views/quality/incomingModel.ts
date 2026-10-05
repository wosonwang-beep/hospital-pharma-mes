import contract from './incoming-contract.json'
import productionContract from './production-quality-contract.json'
export interface Schema{$ref?:string;type?:string|string[];format?:string;enum?:Array<string|number|boolean>;properties?:Record<string,Schema>;required?:string[];items?:Schema;minimum?:number;maximum?:number;maxLength?:number;minItems?:number;additionalProperties?:boolean|Schema}
export const schemas=contract.schemas as unknown as Record<string,Schema>
Object.assign(schemas,productionContract.schemas)
export const operations=contract.operations
export function resolve(schema:Schema):Schema{return schema.$ref?schemas[schema.$ref.split('/').pop()!]!:schema}
export function scalarType(schema:Schema){const s=resolve(schema);return Array.isArray(s.type)?s.type.find(x=>x!=='null'):s.type}
export type IncomingRow=Record<string,unknown>
export function serverAllows(row:IncomingRow|null|undefined,action:string):boolean{return Array.isArray(row?.allowedActions)&&row.allowedActions.includes(action)}
export interface SigningTarget{objectType:string;recordId:string;recordVersion:number;meaning:string;action:string}
export function initial(schema:Schema):unknown{
 const s=resolve(schema),type=scalarType(s)
 if(s.enum?.length===1)return s.enum[0]
 if(type==='object'){
  if(!s.properties)return '{}'
  const value:IncomingRow={}
  for(const [name,field] of Object.entries(s.properties))if(name!=='reauthToken'&&(s.required?.includes(name)||resolve(field).enum?.length===1))value[name]=initial(field)
  return value
 }
 if(type==='array')return []
 if(type==='boolean')return false
 return ''
}
export function commandBody(schema:Schema,value:unknown):unknown{
 const s=resolve(schema),type=scalarType(s)
 if(type==='object'){
  const source=typeof value==='string'?JSON.parse(value):value
  if(!source||typeof source!=='object'||Array.isArray(source))throw Error('结构化内容必须是 JSON 对象')
  if(!s.properties)return JSON.parse(JSON.stringify(source)) as unknown
  const result:IncomingRow={}
  for(const [key,field] of Object.entries(s.properties)){
   if(key==='reauthToken')continue
   const v=(source as IncomingRow)[key]
   if(v===undefined||v===null||v===''){if(s.required?.includes(key))throw Error(`请填写${label(key)}`);continue}
   result[key]=commandBody(field,v)
  }
  return result
 }
 if(type==='array'){
  if(!Array.isArray(value)||value.length<(s.minItems??0))throw Error('请补齐记录明细')
  return value.map(v=>commandBody(s.items!,v))
 }
 if(type==='integer'||type==='number'){
  const n=Number(value)
  if(!Number.isFinite(n)||(type==='integer'&&!Number.isSafeInteger(n))||(s.minimum!==undefined&&n<s.minimum)||(s.maximum!==undefined&&n>s.maximum))throw Error('数值超出允许范围')
  return n
 }
 if(s.enum&&!s.enum.includes(value as string|number|boolean))throw Error('请选择有效选项')
 if(s.format==='date-time'&&typeof value==='string'){
  const date=new Date(value);if(!Number.isFinite(date.valueOf()))throw Error('请输入有效日期时间');return date.toISOString()
 }
 return value
}
const signatureTypes:Record<string,string>={'approve-plan':'SAMPLING_PLAN',complete:'SAMPLING_TASK',dispose:'SAMPLE_DISPOSAL',review:'INSPECTION_TASK',approve:'INSPECTION_REPORT',decide:'DEVIATION_DECISION',close:'DEVIATION_CLOSE',results:'TEST_RESULT_REVISION',revisions:'TEST_RESULT_REVISION','release-decisions':'MATERIAL_RELEASE_DECISION'}
export function selectSigningTarget(targets:SigningTarget[],action:string,decision?:unknown){const meaning=action==='release-decisions'?(decision==='RELEASED'?'RELEASE':decision==='REJECTED'?'REJECT':'APPROVE'):undefined;return targets.find(t=>t.objectType===signatureTypes[action]&&(!meaning||t.meaning===meaning))}
export function reviewResultIds(task:IncomingRow):string[]{return ((task.items??[]) as IncomingRow[]).flatMap(item=>((item.executions??[]) as IncomingRow[]).flatMap(execution=>{const rows=(execution.revisions??[]) as IncomingRow[];return rows.length?[String(rows[rows.length-1]!.id)]:[]}))}
export interface ReferenceOption{value:string;label:string;record:IncomingRow}
export function referenceRows(field:string,rows:IncomingRow[]):ReferenceOption[]{
 let records=rows
 if(['testExecutionId','originalExecutionId','originalResultRevisionId','selectedResultRevisionId','resultRevisionIds'].includes(field)){
  const executions=rows.flatMap(task=>((task.items??[]) as IncomingRow[]).flatMap(item=>(item.executions??[]) as IncomingRow[]))
  records=['testExecutionId','originalExecutionId'].includes(field)?executions:executions.flatMap(execution=>{const rr=(execution.revisions??[]) as IncomingRow[];return field==='resultRevisionIds'?rr.slice(-1):rr})
 }
 return records.map(record=>({value:String(record.id),record,label:[record.requestNo??record.samplingTaskNo??record.sampleNo??record.inspectionTaskNo??record.reportNo??record.deviationNo??record.orderNo??record.batchNo??record.lotNo??record.unitCode??record.displayName??record.equipmentCode??record.itemName??record.id,record.materialName,record.resultConclusion??record.status].filter(v=>v!==undefined&&v!==null).map(display).join(' · ')}))
}
export const referenceFields=new Set(['materialLotId','qcSpecificationVersionId','requestedUnitId','unitId','resultUnitId','inspectionRequestId','inspectionRequestItemId','sampleId','assignedTo','performedBy','instrumentId','approvedInvestigationId','originalExecutionId','testExecutionId','originalResultRevisionId','selectedResultRevisionId','resultRevisionIds','optionalSpecificationItemIds','inspectionReportId','supersedesReportId','supersedesDecisionId'])
export const labels:Record<string,string>={id:'记录编号',materialLotId:'物料批次',qcSpecificationVersionId:'质量标准版本',requestType:'检验类型',reason:'操作原因',requestedQuantity:'请验数量',requestedUnitId:'请验单位',requestedPackageCount:'请验件数',requestedDate:'请验日期',priority:'优先级',approvedInvestigationId:'批准调查记录',inspectionRequestId:'请验单',inspectionRequestItemId:'请验明细',samplingPlan:'取样方案',requiredPackageCount:'要求取样件数',otherSampleName:'其他样品名称',otherSampleReason:'其他样品理由',containerNo:'容器编号',samplingPoint:'取样位置',sampleQuantity:'取样数量',unitId:'单位',sampledAt:'取样时间',packageResealed:'包装已重新密封',samples:'样品分配',sampleType:'样品类型',quantity:'数量',storageCondition:'储存条件',storageLocation:'储存位置',sampleId:'样品',optionalSpecificationItemIds:'可选检验项目',assignedTo:'指派人员',receivedAt:'接收时间',retainedAt:'留样时间',testExecutionId:'检验执行记录',resultNumeric:'原始数值结果',resultText:'原始文本结果',resultUnitId:'结果单位',resultConclusion:'单项结论',reasonForChange:'修改原因',instrumentId:'检验仪器',equipmentId:'检验设备',startedAt:'开始时间',completedAt:'完成时间',performedBy:'检验人',rawData:'原始数据',observation:'观察记录',calculationInput:'计算输入',originalExecutionId:'原始检验执行',approvedRetestInvestigationId:'复检批准调查',investigationId:'调查记录',reviewedResultIds:'复核结果版本',predecessorReportId:'前序检验报告',supersedesReportId:'替代的检验报告',inspectionReportId:'检验报告',decision:'质量决定',releaseBasis:'放行依据',supersedesDecisionId:'替代的放行决定',investigationScope:'调查范围',deviationNo:'偏差编号',deviationType:'偏差类型',severity:'严重程度',description:'情况描述',blocking:'阻断放行',investigationSummary:'调查结论',authorizedRetestCount:'批准复检次数',originalResultDisposition:'原始结果有效性',selectedResultRevisionId:'最终选定结果版本',recordStatus:'记录状态',status:'业务状态',versionNo:'记录版本',createdAt:'创建时间',createdBy:'创建人',updatedAt:'更新时间',updatedBy:'更新人',requestNo:'请验单号',samplingTaskNo:'取样任务号',sampleNo:'样品编号',inspectionTaskNo:'检验记录号',reportNo:'报告编号',overallResult:'综合结论',approvedAt:'批准时间',approvedBy:'批准人',reviewedAt:'复核时间',reviewedBy:'复核人',qualityStatus:'质量状态',inventoryStatus:'库存状态',eligibleForRelease:'是否满足放行条件',gateReasons:'放行检查',reports:'检验报告',investigations:'调查记录',decisions:'放行记录',items:'明细',details:'取样记录',executions:'检验执行记录',results:'原始检验结果',revisions:'结果版本',lowerLimitSnapshot:'标准下限',upperLimitSnapshot:'标准上限',methodCode:'检验方法',methodVersion:'方法版本',effectiveConclusion:'最终有效结论',conclusion:'原始结论',itemName:'检验项目',itemCode:'项目编码',required:'必检项目',receiptId:'收货记录',receiptItemId:'收货明细',signatureId:'电子签名',message:'检查说明',code:'检查代码'}
export const enumLabels:Record<string,string>={DRAFT:'草稿',SUBMITTED:'已提交',ACCEPTED:'QC已接收',IN_PROGRESS:'执行中',COMPLETED:'已完成',PLANNED:'已计划',ASSIGNED:'已指派',CREATED:'已创建',COLLECTED:'已采集',RECEIVED:'已接收',IN_TEST:'检验中',TEST_COMPLETED:'检验完成',RETAINED:'已留样',DISPOSED:'已处置',PENDING_REVIEW:'待复核',QC_PASSED:'检验合格',QC_FAILED:'检验不合格',REVIEWED:'已复核',APPROVED:'已批准',RELEASED:'已放行',REJECTED:'不合格',QUARANTINE:'待验',BLOCKED:'禁止使用',AVAILABLE:'可用',FROZEN:'冻结',PASS:'合格',FAIL:'不合格',INVALID:'已批准无效',INCONCLUSIVE:'无法判定',INITIAL:'首次检验',RETEST:'复检',SUPPLEMENTARY:'补充检验',INVESTIGATION:'调查检验',NORMAL:'正常',URGENT:'紧急',TEST_SAMPLE:'检验样',RETENTION_SAMPLE:'留样',RETEST_SAMPLE:'复检样',OTHER_APPROVED:'其他批准样品',FULL_INSPECTION:'全项检验',INSPECTION_EXEMPT:'批准免检',OTHER_APPROVED_BASIS:'其他批准依据',OTHER_DISPOSITION:'其他处置',OPEN:'待调查',INVESTIGATING:'调查中',DECIDED:'已作决定',CLOSED:'已关闭',AUTHORIZE_RETEST:'批准复检',ACCEPT_WITH_JUSTIFICATION:'有依据接受',REJECT:'拒绝',VALID:'有效',MINOR:'一般',MAJOR:'重大',CRITICAL:'严重',INCOMING_MATERIAL:'来料质量'}
export function label(key:string){return labels[key]??key}
export function display(value:unknown){if(value===null||value===undefined||value==='')return '—';if(typeof value==='boolean')return value?'是':'否';return enumLabels[String(value)]??String(value)}
export const resources:Record<string,{title:string;permission:string;columns:string[];create?:string;actions:string[]}>={
 'inspection-requests':{title:'请验单',permission:'qms:inspection-request',columns:['requestNo','materialLotId','requestType','qcSpecificationVersionId','status','createdAt'],create:'InspectionRequestCommand',actions:['submit','accept']},
 'sampling-tasks':{title:'取样记录',permission:'qms:sampling',columns:['samplingTaskNo','inspectionRequestId','assignedTo','status','completedAt'],create:'SamplingTaskCommand',actions:['approve-plan','assign','start','details','complete']},
 samples:{title:'样品',permission:'qms:test',columns:['sampleNo','materialLotId','sampleType','quantity','unitId','status'],actions:['label','receive','retain','dispose']},
 'inspection-tasks':{title:'检验记录',permission:'qms:test',columns:['inspectionTaskNo','inspectionRequestId','sampleId','assignedTo','status'],create:'InspectionTaskCommand',actions:['assign','start','submit-review','review']},
 'inspection-reports':{title:'检验报告',permission:'qms:report',columns:['reportNo','inspectionRequestId','overallResult','status','approvedAt'],create:'InspectionReportCommand',actions:['review','approve']},
 deviations:{title:'来料调查',permission:'qms:deviation',columns:['deviationNo','investigationScope','materialLotId','severity','status'],create:'IncomingDeviationCommand',actions:['investigate','decide','close']}}
export const actionLabels:Record<string,string>={submit:'提交请验',accept:'QC接收','approve-plan':'批准取样方案',assign:'指派',start:'开始执行',details:'记录取样并生成样品',complete:'完成取样',label:'生成样品标签',receive:'接收样品',retain:'留样',dispose:'样品处置',executions:'记录原始检验','approved-retests':'批准复检执行',results:'提交原始结果',revisions:'更正结果','submit-review':'提交复核',review:'复核',approve:'批准报告',investigate:'记录调查',decide:'调查决定',close:'关闭调查','release-decisions':'QA物料决定'}

Object.assign(labels,{investigationKind:'调查类型',decisionCode:'调查决定',disposition:'处置依据',resultRevisionIds:'复核结果版本',resultConclusion:'原始结论',originalResultRevisionId:'原始结果版本',originalExecutionId:'原始执行记录',originalResult:'原始结果',result:'选定结果',lowerLimit:'标准下限',upperLimit:'标准上限',resultType:'结果类型',textAcceptanceCriteria:'文本合格标准',qcSpecificationItemId:'标准项目',signatureEvidence:'签名证据',recordVersion:'签名绑定版本',meaning:'签名含义',evidenceDigest:'证据摘要',specificationContentHash:'标准内容摘要',requestedBy:'请验人',submittedBy:'提交人',submittedAt:'提交时间',acceptedBy:'接收人',acceptedAt:'接收时间',sampleScope:'样品范围',samplingDetailId:'取样明细',samplingTaskId:'取样任务',detailNo:'明细序号',sampledBy:'取样人',receivedBy:'样品接收人',attemptNo:'执行序号',recordedBy:'记录人',recordedAt:'记录时间',revisionNo:'结果版本',previousRevisionId:'前一结果版本',closedBy:'关闭人',closedAt:'关闭时间',decidedBy:'决定人',decidedAt:'决定时间',closeReason:'关闭依据',decisionBy:'放行决定人',decisionAt:'决定时间',decisionSource:'决定来源'})
Object.assign(enumLabels,{OOS:'超标结果调查',DEVIATION:'偏差调查',VERIFY:'确认',APPROVE:'批准',RELEASE:'放行',USER_QA:'人工 QA 决定',SYSTEM_RULE:'规则免验',PENDING_SAMPLING:'待取样',SAMPLING:'取样中',SAMPLED:'已取样',TESTING:'检验中',PENDING_QC_REVIEW:'待 QC 复核',PENDING_QA_RELEASE:'待 QA 放行',PENDING_DISPOSITION:'待质量处置',NUMERIC:'数值结果',TEXT:'文本结果'})
export const incomingRouteDictionary=Object.freeze(Object.fromEntries(Object.entries(resources).map(([key,value])=>[key,{...value,base:key==='deviations'?'/deviations':`/quality/${key}`}])) )

Object.assign(labels,{originalData:'原始记录',instrumentEvidence:'仪器使用依据',performedBy:'实际检验人',planSignatureId:'取样方案签名',completionSignatureId:'取样完成签名',disposalSignatureId:'处置签名',reviewSignatureId:'检验复核签名',approvalSignatureId:'报告批准签名',decisionSignatureId:'调查决定签名',closeSignatureId:'调查关闭签名',inspectionTaskId:'检验任务',inspectionItemId:'检验项目记录',testCode:'项目编码',inspectionReportId:'检验报告',supersedesDecisionId:'前序决定',ruleEvidence:'免验规则依据',ruleCode:'规则编码',ruleVersion:'规则版本',evaluatedAt:'判断时间',receiptChecksPassed:'收货检查通过',requiresIncomingInspectionSnapshot:'收货时检验政策'})

Object.assign(labels,{evidenceIds:'关联证据',objectType:'签名对象类型',objectId:'签名对象编号',recordId:'绑定记录编号',action:'操作',canonicalRecord:'签名记录快照',boundEvidence:'绑定业务证据',orgId:'组织',approvedInvestigationId:'批准调查记录',qcSpecificationId:'质量标准',specificationVersionId:'质量标准版本',unitIdSnapshot:'标准单位',resultTypeSnapshot:'结果类型',requiredSnapshot:'必检要求',textAcceptanceCriteriaSnapshot:'文本合格标准'})
