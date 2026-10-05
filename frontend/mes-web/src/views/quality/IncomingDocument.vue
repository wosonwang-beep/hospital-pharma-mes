<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {useRouter} from 'vue-router'
import {api,errorMessage,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import IncomingFacts from './IncomingFacts.vue'
import InspectionRecordOverview from './InspectionRecordOverview.vue'
import InspectionReportOverview from './InspectionReportOverview.vue'
import {schemas,type IncomingRow} from './incomingModel'
import {detailLabel,detailDisplay,documentNumber,documentStatus,readSchemas,auditTypes,rows,sections,selectFacts,signatureEntries,slotNames,summaryFields} from './incomingDetailModel'

const props=defineProps<{record:IncomingRow;resource:string}>()
const router=useRouter(),auth=useAuthStore()
const references=ref<Record<string,{name:string;row:IncomingRow;route?:string}>>({}),lookupNotices=ref<string[]>([])
const evidence=ref<IncomingRow|null>(null),evidenceOpen=ref(false),evidenceBusy=ref(false),evidenceError=ref('')
const compact=computed(()=>['inspection-tasks','inspection-reports'].includes(props.resource)),moreOpen=ref(false),selectedItem=ref<IncomingRow|null>(null)
const qa=computed(()=>props.resource==='release')
const rootSchema=readSchemas
const missing=computed(()=>{
 const schema=schemas[rootSchema[props.resource]??'']
 return (schema?.required??[]).filter(k=>!['orgId','signingTargets'].includes(k)&&!Object.prototype.hasOwnProperty.call(props.record,k))
})
const invalidStatus=computed(()=>{
 const statuses=schemas[rootSchema[props.resource]??'']?.properties?.status?.enum
 return !!statuses&&props.record.status!==undefined&&!statuses.includes(String(props.record.status))
})
const summary=computed(()=>selectFacts(props.record,summaryFields))
const signatures=computed(()=>signatureEntries(props.record))
const request=computed(()=>props.resource==='inspection-requests'?props.record:references.value[`inspectionRequestId:${props.record.inspectionRequestId}`]?.row)
const lotId=computed(()=>props.record.materialLotId??request.value?.materialLotId)
const lot=computed(()=>references.value[`materialLotId:${lotId.value}`]?.row)
const standard=computed(()=>request.value?selectFacts(request.value,['qcSpecificationVersionId','specificationContentHash']):{})
const linked=computed(()=>Object.values(references.value).filter((r,i,all)=>r.route&&all.findIndex(x=>x.route===r.route)===i))
const readFields=['createdBy','createdAt','updatedBy','updatedAt','versionNo','evidenceDigest']
function facts(row:IncomingRow,keys:string[]){return selectFacts(row,keys)}
function valueLabel(key:string,value:unknown){
 if(key==='status'&&invalidStatus.value&&value===props.record.status)return documentStatus(props.resource,props.record)
 const ref=references.value[`${key}:${value}`]
 return ref?`${ref.name}（ID ${value}）`:detailDisplay(key,value)
}
const mappings:Record<string,{url:string;permission:string;route?:string}>={
 inspectionRequestId:{url:'/quality/inspection-requests',permission:'qms:inspection-request:view',route:'/quality/inspection-requests'},
 sampleId:{url:'/quality/samples',permission:'qms:test:view',route:'/quality/samples'},
 samplingTaskId:{url:'/quality/sampling-tasks',permission:'qms:sampling:view',route:'/quality/sampling-tasks'},
 materialLotId:{url:'/wms/material-lots',permission:'wms:inventory:view',route:'/wms/material-lots'},
 receiptId:{url:'/wms/receipts',permission:'wms:receipt:view',route:'/wms/receipts'},
 qcSpecificationVersionId:{url:'/quality/specification-versions',permission:'qms:specification:view'},
 inspectionReportId:{url:'/quality/inspection-reports',permission:'qms:report:view',route:'/quality/inspection-reports'},
 supersedesReportId:{url:'/quality/inspection-reports',permission:'qms:report:view',route:'/quality/inspection-reports'},
 approvedInvestigationId:{url:'/deviations',permission:'qms:deviation:view',route:'/deviations'},
 dispositionInvestigationId:{url:'/deviations',permission:'qms:deviation:view',route:'/deviations'},
 investigationId:{url:'/deviations',permission:'qms:deviation:view',route:'/deviations'},
}
for(const key of ['assignedTo','performedBy','requestedBy','submittedBy','acceptedBy','sampledBy','receivedBy','reviewedBy','approvedBy','recordedBy','decidedBy','closedBy','decisionBy','createdBy','updatedBy'])mappings[key]={url:'/users',permission:'iam:user:view'}
for(const key of ['unitId','requestedUnitId','resultUnitId'])mappings[key]={url:'/units',permission:'master:uom:view'}
mappings.instrumentId={url:'/equipment',permission:'master:equipment:view'}
function referenceName(row:IncomingRow){
 const snap=row.materialSnapshot as IncomingRow|undefined
 return String(row.requestNo??row.samplingTaskNo??row.sampleNo??row.reportNo??row.deviationNo??row.receiptNo??row.lotNo??row.displayName??row.unitName??row.unitCode??row.equipmentName??row.equipmentCode??(row.versionNoBusiness!==undefined?`质量标准 V${row.versionNoBusiness}`:snap?.materialName)??row.id)
}
let generation=0
watch(()=>props.record,async record=>{
 const current=++generation,found:typeof references.value={},notices=new Set<string>(),seen=new Set<string>(),cache=new Map<string,Promise<IncomingRow>>()
 references.value={};lookupNotices.value=[]
 moreOpen.value=false;selectedItem.value=null
 async function visit(value:unknown):Promise<void>{
  if(!value||typeof value!=='object')return
  if(Array.isArray(value)){await Promise.all(value.map(visit));return}
  await Promise.all(Object.entries(value).map(async([key,v])=>{
   const map=mappings[key]
   if(map&&typeof v==='string'&&/^[1-9][0-9]*$/.test(v)){
    const identity=`${key}:${v}`;if(seen.has(identity))return;seen.add(identity)
    if(!auth.can(map.permission)){notices.add(`${detailLabel(key)}：无关联读取权限，保留原始ID。`);return}
    try{
     const url=`${map.url}/${v}`;let pending=cache.get(url);if(!pending){pending=api<IncomingRow>({url});cache.set(url,pending)}
     const row=await pending;found[identity]={name:referenceName(row),row,route:map.route?`${map.route}/${v}`:undefined}
     // Follow only request lineage; never replace signed item snapshots with a current standard.
     if(key==='inspectionRequestId')await visit(selectFacts(row,['materialLotId','qcSpecificationVersionId','receiptId']))
     if(key==='qcSpecificationVersionId')for(const item of rows(row.items)){const id=item.specificationItemId??item.id;found[`qcSpecificationItemId:${id}`]={name:String(item.itemName??item.itemCode??id),row:item}}
    }catch{notices.add(`${detailLabel(key)}：关联记录暂不可读取，保留原始ID。`)}
   }else if(key!=='signatureEvidence'&&key!=='signingTargets')await visit(v)
  }))
 }
 await visit(record)
 // Resolve existing execution/result references from the existing org-scoped task read model.
 if(['deviations','inspection-reports'].includes(props.resource)&&auth.can('qms:test:view')){
  const requestRow=found[`inspectionRequestId:${record.inspectionRequestId}`]?.row
  const filter=record.inspectionRequestId?{inspectionRequestId:record.inspectionRequestId}:record.materialLotId?{materialLotId:record.materialLotId}:undefined
  if(filter)try{
   let page=0,total=0
   do{
    const result=await api<Page<IncomingRow>>({url:'/quality/inspection-tasks',params:{...filter,page,size:50}});total=result.total
    for(const task of result.items)for(const item of rows(task.items)){
     if(props.resource==='inspection-reports') { await visit(selectFacts(item,['unitId'])); for(const e of rows(item.executions))await visit(selectFacts(e,['performedBy'])) }
     const target={name:String(item.itemName??item.itemCode??item.id),row:item,route:`/quality/inspection-tasks/${task.id}`}
     found[`inspectionItemId:${item.id}`]=target
     for(const execution of rows(item.executions)){
      for(const key of ['testExecutionId','originalExecutionId'])found[`${key}:${execution.id}`]={name:`${task.inspectionTaskNo} · 执行 ${execution.attemptNo}`,row:execution,route:target.route}
      for(const revision of rows(execution.revisions))for(const key of ['originalResultRevisionId','selectedResultRevisionId','resultRevisionId','previousRevisionId','reviewedResultIds'])found[`${key}:${revision.id}`]={name:`${task.inspectionTaskNo} · 结果版本 ${revision.revisionNo} · ${detailDisplay('resultConclusion',revision.resultConclusion)}`,row:revision,route:target.route}
     }
    }
    page++
   }while(page*50<total&&current===generation)
   if(requestRow)await visit(selectFacts(requestRow,['qcSpecificationVersionId']))
  }catch{notices.add('关联检验记录暂不可读取，保留原始引用。')}
 }
 if(current===generation){references.value=found;lookupNotices.value=[...notices]}
},{immediate:true})
async function openEvidence(metadata:IncomingRow){
 if(!auth.can('audit:view'))return
 evidenceOpen.value=true;evidenceBusy.value=true;evidenceError.value='';evidence.value=null
 try{evidence.value=await api<IncomingRow>({url:`/quality/signature-evidence/${metadata.signatureId}`})}catch(e){evidenceError.value=errorMessage(e)}finally{evidenceBusy.value=false}
}
const gateNames:Record<string,string>={INVENTORY_FROZEN:'物料批库存已冻结',EXPIRED:'物料批已过有效期',APPROVED_REPORT_REQUIRED:'缺少唯一有效的已批准检验报告',APPROVED_PASS_REPORT_REQUIRED:'检验报告尚未批准或综合结论不合格',REPORT_LOT_MISMATCH:'检验报告与当前物料批不一致',REPORT_SUPERSEDED:'检验报告已被后续报告替代',RECEIPT_INCOMPLETE:'收货记录尚未完成',INVESTIGATION_OPEN:'存在尚未关闭的来料调查',REPORT_NOT_APPROVED:'缺少已批准检验报告',REPORT_REQUIRED:'缺少所需检验报告',REPORT_EVIDENCE_CHANGED:'报告关联证据已发生变化',REPORT_EVIDENCE_INVALID:'报告关联证据不满足要求',LOT_FROZEN:'物料批已冻结',LOT_EXPIRED:'物料批已过有效期',RETEST_DUE:'物料批已到复验期',QUALITY_STATE:'当前质量状态不允许放行'}
function gateText(row:IncomingRow){return gateNames[String(row.code)]??(row.message!==row.code?String(row.message):`放行受阻，检查代码：${row.code}`)}
</script>

<!-- Ordinary documents select T3; the existing material decision route selects T6. -->
<template><div class="incoming-document" :data-ui-template="qa?'T6':'T3'">
 <a-alert v-if="missing.length||invalidStatus" type="warning" show-icon message="详情响应不完整或状态不符合当前契约，请重新加载并核对数据来源。" :description="invalidStatus?`未识别业务状态：${record.status}`:`缺少：${missing.map(detailLabel).join('、')}`"/>
 <InspectionRecordOverview v-if="resource==='inspection-tasks'" :record="record" :request="request" :lot="lot" :references="references" :value-label="valueLabel" @view-item="selectedItem=$event"/>
 <InspectionReportOverview v-if="resource==='inspection-reports'" :record="record" :request="request" :lot="lot" :references="references"/>
 <button v-if="compact" class="evidence-toggle" :aria-expanded="moreOpen" @click="moreOpen=!moreOpen"><span><strong>更多记录与审计证据</strong><small>包含结果修订历史、原始数据、检验方法、仪器信息、电子签名、Audit Trail等</small></span><span>{{moreOpen?'收起':'展开'}}</span></button>
 <div v-show="!compact||moreOpen">
 <a-card v-if="!compact" title="关键事实" class="form-section"><IncomingFacts :value="summary" :label-formatter="detailLabel" :value-formatter="valueLabel"/>
  <div v-if="lot" class="related-context"><span>内部批号：{{lot.lotNo}}</span><span>供应商批号：{{lot.supplierLotNo??'—'}}</span><span>质量状态：{{detailDisplay('qualityStatus',lot.qualityStatus)}}</span><span>库存状态：{{detailDisplay('inventoryStatus',lot.inventoryStatus)}}</span></div>
  <a-space wrap class="related-context"><a-button v-for="r in linked" :key="r.route" type="link" @click="router.push(r.route!)">{{r.name}}</a-button><a-button v-if="lotId&&auth.can('trace:view')" type="link" @click="router.push({path:'/trace',query:{materialLotId:String(lotId)}})">完整来料追溯</a-button><a-button v-if="auth.can('audit:view')" type="link" @click="router.push({path:'/audit',query:{objectType:auditTypes[resource],objectId:String(record.id??record.materialLotId)}})">审计追踪</a-button></a-space>
  <details v-if="lookupNotices.length"><summary>关联信息读取说明</summary><p v-for="notice in lookupNotices" :key="notice">{{notice}}</p></details>
 </a-card>
 <a-card v-for="section in sections[resource]??[]" :key="section.title" :title="section.title" class="form-section"><IncomingFacts :value="facts(record,section.keys)" :label-formatter="detailLabel" :value-formatter="valueLabel"/></a-card>
 <a-card v-if="Object.keys(standard).length" title="请验时冻结的质量标准版本" class="form-section"><IncomingFacts :value="standard" :label-formatter="detailLabel" :value-formatter="valueLabel"/></a-card>
 <template v-if="resource==='inspection-tasks'">
  <a-card title="检验项目、标准快照与执行结果" class="form-section"><p v-if="!rows(record.items).length">暂无检验项目</p>
   <section v-for="item in rows(record.items)" :key="String(item.id)" class="document-section"><h3>{{item.itemCode}} · {{item.itemName}}</h3>
    <IncomingFacts :value="facts(item,['qcSpecificationItemId','required','resultType','lowerLimit','upperLimit','unitId','textAcceptanceCriteria','methodCode','methodVersion'])" :label-formatter="detailLabel" :value-formatter="valueLabel"/>
    <p v-if="!rows(item.executions).length">暂无检验执行记录</p>
    <section v-for="execution in rows(item.executions)" :key="String(execution.id)" class="document-section"><h4>检验执行 {{execution.id}} · 第 {{execution.attemptNo}} 次</h4>
     <IncomingFacts :value="facts(execution,['originalExecutionId','approvedInvestigationId','instrumentId','performedBy','startedAt','completedAt','rawData','observation','calculationInput'])" :label-formatter="detailLabel" :value-formatter="valueLabel"/>
     <h4>原始结果与修订历史</h4><p v-if="!rows(execution.revisions).length">暂无检验结果</p>
     <section v-for="revision in rows(execution.revisions)" :key="String(revision.id)" class="result-version"><h4>{{Number(revision.revisionNo)===1?'原始结果':'修订结果'}} · 版本 {{revision.revisionNo}} · {{detailDisplay('resultConclusion',revision.resultConclusion)}}</h4>
      <IncomingFacts :value="facts(revision,['id','testCode','previousRevisionId','resultNumeric','resultText','resultUnitId','resultConclusion','effectiveConclusion','dispositionInvestigationId','reasonForChange','recordedBy','recordedAt','signatureId'])" :label-formatter="detailLabel" :value-formatter="valueLabel"/>
     </section>
    </section>
   </section>
  </a-card>
 </template>
 <a-card v-if="resource==='inspection-reports'" title="项目汇总：原始结果与报告选定结果" class="form-section"><p>报告引用既有检验结果；原始失败事实与调查处置分别保留。</p><p v-if="!rows(record.items).length">暂无报告项目</p>
  <section v-for="item in rows(record.items)" :key="String(item.id)" class="document-section"><IncomingFacts :value="facts(item,['qcSpecificationItemId','inspectionItemId','resultRevisionId','originalResultRevisionId','investigationId'])" :label-formatter="detailLabel" :value-formatter="valueLabel"/>
   <IncomingFacts v-if="references[`inspectionItemId:${item.inspectionItemId}`]" :value="facts(references[`inspectionItemId:${item.inspectionItemId}`]!.row,['itemCode','itemName','required','resultType','lowerLimit','upperLimit','unitId','textAcceptanceCriteria','methodCode','methodVersion'])" :label-formatter="detailLabel" :value-formatter="valueLabel"/>
   <div class="result-comparison"><section><h3>原始结果</h3><IncomingFacts :value="(item.originalResult??{}) as IncomingRow" :label-formatter="detailLabel" :value-formatter="valueLabel"/></section><section><h3>报告选定结果</h3><IncomingFacts :value="(item.result??{}) as IncomingRow" :label-formatter="detailLabel" :value-formatter="valueLabel"/></section></div>
  </section>
 </a-card>
 <a-card v-if="resource==='deviations'" title="原始结果与最终选定结果证据" class="form-section"><div class="result-comparison"><section v-for="key in ['originalResultRevisionId','selectedResultRevisionId']" :key="key"><h3>{{detailLabel(key)}}</h3><IncomingFacts v-if="references[`${key}:${record[key]}`]" :value="references[`${key}:${record[key]}`]!.row" :label-formatter="detailLabel" :value-formatter="valueLabel"/><p v-else>{{record[key]?`关联结果 ${record[key]} 暂不可读取，请从关联检验记录查看。`:'暂无记录'}}</p></section></div></a-card>
 <template v-if="qa"><div class="decision-grid"><a-card title="放行条件与阻断检查" class="form-section"><a-alert :type="record.eligibleForRelease?'success':'warning'" :message="record.eligibleForRelease?'当前满足放行条件，仍须执行授权质量决定':'当前不满足放行条件'" show-icon/>
   <p v-if="!rows(record.gateReasons).length">当前暂无阻断原因。</p><section v-for="gate in rows(record.gateReasons)" :key="String(gate.code)" class="document-section"><p>{{gateText(gate)}}</p><IncomingFacts :value="facts(gate,['code','evidenceIds'])" :label-formatter="detailLabel" :value-formatter="valueLabel"/></section>
  </a-card><a-card title="质量决定摘要" class="form-section"><IncomingFacts :value="facts(record,['materialLotId','qualityStatus','inventoryStatus','eligibleForRelease','versionNo','evidenceDigest'])" :label-formatter="detailLabel" :value-formatter="valueLabel"/><p>QC检验合格与QA物料放行是独立决定。</p><slot name="decision-actions"/></a-card></div>
  <a-card v-for="[key,title] in [['reports','检验报告证据'],['investigations','来料调查证据'],['decisions','物料质量决定历史']]" :key="key" :title="title" class="form-section"><p v-if="!rows(record[key!]).length">暂无记录</p><section v-for="row in rows(record[key!])" :key="String(row.id)" class="document-section"><h3>{{documentNumber(row)}}</h3><a-button v-if="key==='reports'&&auth.can('qms:report:view')" type="link" @click="router.push(`/quality/inspection-reports/${row.id}`)">查看检验报告</a-button><a-button v-if="key==='investigations'&&auth.can('qms:deviation:view')" type="link" @click="router.push(`/deviations/${row.id}`)">查看来料调查</a-button><a-button v-if="key==='decisions'&&auth.can('audit:view')" type="link" @click="router.push({path:'/audit',query:{objectType:'qms_release_decision',objectId:String(row.id)}})">查看质量决定审计</a-button><IncomingFacts :value="row" :label-formatter="detailLabel" :value-formatter="valueLabel"/></section></a-card>
 </template>
 <a-card title="电子签名记录" class="form-section"><p v-if="!signatures.length">暂无电子签名记录</p><section v-for="(entry,index) in signatures" :key="`${entry.metadata.signatureId}:${index}`" class="document-section"><h3>{{slotNames[String(entry.metadata.objectType)]??'电子签名'}}</h3><p class="muted">{{entry.context}}</p><IncomingFacts :value="entry.metadata" :label-formatter="detailLabel" :value-formatter="valueLabel"/><a-button v-if="auth.can('audit:view')" @click="openEvidence(entry.metadata)">查看签名证据</a-button><p v-else>签名快照需要审计查看权限。</p></section></a-card>
 <a-card title="记录元数据" class="form-section"><IncomingFacts :value="facts(record,readFields)" :label-formatter="detailLabel" :value-formatter="valueLabel"/></a-card>
 <details v-if="compact&&lookupNotices.length"><summary>关联信息读取说明</summary><p v-for="notice in lookupNotices" :key="notice">{{notice}}</p></details>
 </div>
 <a-drawer :open="!!selectedItem" :title="`${selectedItem?.itemName??'检验项目'} · 完整检验详情`" width="min(900px, 100vw)" @close="selectedItem=null"><IncomingFacts v-if="selectedItem" :value="selectedItem" :label-formatter="detailLabel" :value-formatter="valueLabel"/><section v-for="(entry,index) in signatureEntries(selectedItem)" :key="index" class="document-section"><h3>{{slotNames[String(entry.metadata.objectType)]??'电子签名'}}</h3><IncomingFacts :value="entry.metadata" :label-formatter="detailLabel" :value-formatter="valueLabel"/><a-button v-if="auth.can('audit:view')" @click="openEvidence(entry.metadata)">查看签名证据</a-button></section></a-drawer>
 <a-drawer v-model:open="evidenceOpen" title="电子签名证据（只读）" width="min(720px, 100vw)"><a-spin v-if="evidenceBusy"/><a-alert v-if="evidenceError" type="error" :message="evidenceError"/><IncomingFacts v-if="evidence" :value="evidence" :label-formatter="detailLabel" :value-formatter="valueLabel"/></a-drawer>
</div></template>
<style scoped>
.evidence-toggle{width:100%;display:flex;align-items:center;justify-content:space-between;gap:16px;text-align:left;background:#fff;border:1px solid var(--mes-ui-border,#e8edf3);border-radius:10px;padding:20px;color:#26364d;margin-bottom:16px;cursor:pointer;font:inherit}.evidence-toggle strong{display:block;font-size:14px;font-weight:600}.evidence-toggle small{display:block;font-size:12px;color:#66758a;margin-top:6px}.evidence-toggle:focus-visible{outline:2px solid #1677ff;outline-offset:2px}
.incoming-document{min-width:0}.document-section{border-top:1px solid var(--mes-border,#e8edf3);padding-top:16px;margin-top:16px;min-width:0}.document-section:first-child{border-top:0;margin-top:0;padding-top:0}h3,h4{font-size:15px;font-weight:600;margin:0 0 12px}.result-version{margin-top:16px;padding:16px;border:1px solid var(--mes-border,#e8edf3);border-radius:8px;min-width:0}.result-comparison,.decision-grid{display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1fr);gap:16px}.result-comparison>section{min-width:0}.related-context{display:flex;flex-wrap:wrap;gap:12px;margin-top:12px}.muted,details{color:#596778;font-size:12px}p{overflow-wrap:anywhere}details{margin-top:12px}details summary{cursor:pointer}@media(max-width:767px){.result-comparison,.decision-grid{grid-template-columns:minmax(0,1fr)}.result-version{padding:12px}}
</style>
