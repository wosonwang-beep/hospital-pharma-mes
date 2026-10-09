<script setup lang="ts">
import {ref,watch,computed,onBeforeUnmount} from 'vue'
import {api,errorMessage,type Page} from '../../api/http'
import {referenceRows,label,type IncomingRow,type ReferenceOption} from './incomingModel'
const props=defineProps<{field:string;modelValue:unknown;context:IncomingRow;disabled?:boolean;required?:boolean;inputLabel?:string;queryFilters?:{productId?:string|null;materialId?:string|null;materialIds?:string[];status?:string|null}}>()
const emit=defineEmits<{'update:modelValue':[string];select:[IncomingRow]}>()
const options=ref<ReferenceOption[]>([]),loading=ref(false),error=ref(''),search=ref(''),page=ref(0),hasMore=ref(false)
let sequence=0
const contextKey=computed(()=>JSON.stringify([props.context.mainBatchId,props.context.finishedMaterialId,props.context.sampleId,props.context.investigationScope,props.context.materialLotId,props.context.materialId,props.context.inspectionRequestId,props.context.qcSpecificationVersionId,props.context.id,props.context.qcSpecificationItemId,props.context.reports,props.context.decisions,props.context.productId,props.context.processPackageId,props.context.operations,props.context.processSnapshot,props.context.formulaItems,props.queryFilters]))
async function list(url:string,params:Record<string,unknown>={}){const result=await api<Page<IncomingRow>>({url,params:{page:page.value,size:50,keyword:search.value||undefined,...params}});hasMore.value=(page.value+1)*50<result.total;return result.items}
async function load(append=false){const current=++sequence;loading.value=true;error.value='';if(!append)page.value=0;hasMore.value=false
 try{
  if(props.disabled&&props.modelValue){options.value=[{value:String(props.modelValue),label:`已绑定记录 ${props.modelValue}`,record:{id:props.modelValue}}];return}
  const f=props.field,c=props.context;let rows:IncomingRow[]=[];let request:IncomingRow|undefined
  if(c.inspectionRequestId)request=await api<IncomingRow>({url:`/quality/inspection-requests/${c.inspectionRequestId}`})
  const lotId=c.materialLotId??request?.materialLotId
  if(f==='mainBatchId')rows=await list('/main-batches',{productId:props.queryFilters?.productId||undefined,status:props.queryFilters?.status||undefined})
  else if(['finishedMaterialId','materialId'].includes(f))rows=await list('/materials',f==='finishedMaterialId'?{materialType:'FINISHED',status:'ACTIVE'}:{status:'ACTIVE'})
  else if(['qcSpecificationItemId','specificationItemId'].includes(f)){if(c.sampleId){const sample=await api<IncomingRow>({url:`/samples/${c.sampleId}`});const spec=await api<IncomingRow>({url:`/quality/specification-versions/${sample.qcSpecificationVersionId}`});rows=((spec.items??[]) as IncomingRow[]).map(i=>({...i,id:i.specificationItemId??i.id}))}}
  else if(f==='productionTestInstanceId')rows=await list('/quality/production-tests',{mainBatchId:c.mainBatchId})
  else if(f==='locationId')rows=await list('/locations')
  else if(f==='productId')rows=await list('/products',{status:'ACTIVE'})
  else if(f==='productionOrderId')rows=await list('/production-orders')
  else if(f==='processPackageId'){
   const packages=await list('/process-packages',{productId:c.productId});rows=packages.filter(root=>((root.currentDefinition??{}) as IncomingRow).status==='EFFECTIVE').map(root=>({...root,displayName:String(root.packageCode)}))
  }else if(f==='ebrTemplateVersionId')rows=(await list('/ebr/templates',{processPackageId:c.processPackageId,status:'EFFECTIVE'})).map(t=>({...t,displayName:`${t.templateName||'未命名（历史模板）'} · ${t.templateCode} · V${t.version}`}))
  else if(f==='operationExecutionId'){rows=(c.operations??[]) as IncomingRow[];if(!rows.length&&c.mainBatchId){const units=await api<IncomingRow[]>({url:`/main-batches/${c.mainBatchId}/execution-units`});rows=(await Promise.all(units.map(u=>api<IncomingRow[]>({url:`/execution-units/${u.id}/operations`})))).flat()}}
  else if(f==='weighingRecordId')rows=((c.weighings??[]) as IncomingRow[]).filter(w=>w.status==='VERIFIED')
  else if(f==='formulaItemId')rows=((c.formulaItems??[]) as IncomingRow[]).map(i=>({...i,id:i.formulaItemId,displayName:`${i.materialName??i.materialId} · ${i.requiredQty??i.quantity??i.targetQty??''}`}))
  else if(f==='parameterDefId')rows=((c.parameters??[]) as IncomingRow[]).map(i=>({...i,id:i.parameterDefId}))
  else if(['equipmentId','scaleEquipmentId'].includes(f))rows=await list('/equipment',{status:'ACTIVE'})
  else if(f==='materialLotId'){const single=props.queryFilters?.materialId;const ids=single?[single]:[...new Set(props.queryFilters?.materialIds??[])];if(ids.length){const sets=await Promise.all(ids.map(materialId=>list('/wms/material-lots',{materialId})));rows=sets.flat()}else rows=await list('/wms/material-lots');if(c.investigationScope==='INCOMING_MATERIAL')rows=rows.filter(lot=>String((lot.materialSnapshot as IncomingRow|undefined)?.materialType??'')!=='FINISHED')}
  else if(f==='qcSpecificationVersionId'){
   if(!lotId&&!c.finishedMaterialId){options.value=[];return}
   const material=c.finishedMaterialId??(await api<IncomingRow>({url:`/wms/material-lots/${lotId}`})).materialId;const roots=await list('/quality/specifications',{materialId:material})
   const details=await Promise.all(roots.map(root=>api<IncomingRow>({url:`/quality/specifications/${root.id}`})))
   rows=details.flatMap(root=>((root.versions??[]) as IncomingRow[]).filter(v=>v.status==='APPROVED').map(v=>({...v,displayName:`${root.specificationCode} · V${v.versionNoBusiness}`})))
  }else if(['unitId','requestedUnitId','resultUnitId'].includes(f))rows=await list('/units')
  else if(['assignedTo','performedBy'].includes(f))rows=(await list('/users')).filter(u=>u.status==='ACTIVE')
  else if(f==='instrumentId')rows=await list('/equipment',{status:'ACTIVE'})
  else if(f==='inspectionRequestId')rows=await list('/quality/inspection-requests',{materialLotId:lotId})
  else if(f==='inspectionRequestItemId')rows=(request?.items??[]) as IncomingRow[]
  else if(f==='sampleId'){
   if(c.investigationScope==='PRODUCTION'||c.mainBatchId){rows=await list('/samples',{mainBatchId:c.mainBatchId,investigationScope:'PRODUCTION'});rows=rows.filter(s=>['RECEIVED','IN_TEST'].includes(String(s.status)))}else rows=await list('/quality/samples',{materialLotId:lotId})
   const ids=((request?.items??[]) as IncomingRow[]).map(x=>String(x.id))
   if(c.investigationScope!=='PRODUCTION'&&!c.mainBatchId)rows=rows.filter(s=>ids.includes(String(s.inspectionRequestItemId))&&['TEST_SAMPLE','RETEST_SAMPLE'].includes(String(s.sampleType))&&['RECEIVED','TEST_COMPLETED'].includes(String(s.status)))
  }else if(['originalResultRevisionId','selectedResultRevisionId','resultRevisionId'].includes(f)&&c.investigationScope==='PRODUCTION'){const tests=await list('/quality/production-tests',{mainBatchId:c.mainBatchId});const details=await Promise.all(tests.map(t=>api<IncomingRow>({url:`/quality/production-tests/${t.id}`})));rows=details.flatMap(t=>(t.results??[]) as IncomingRow[]);if(f==='originalResultRevisionId')rows=rows.filter(r=>Number(r.revisionNo)===1)}
  else if(f==='approvedInvestigationId'||f==='investigationId'&&c.investigationScope==='PRODUCTION'){const batch=c.mainBatchId??(c.investigationScope==='PRODUCTION'&&c.sampleId?(await api<IncomingRow>({url:`/samples/${c.sampleId}`})).mainBatchId:undefined);rows=(await list('/deviations',{investigationScope:c.investigationScope??'INCOMING_MATERIAL',status:'DECIDED',materialLotId:lotId,mainBatchId:batch})).filter(d=>d.decisionCode==='AUTHORIZE_RETEST'&&(f!=='investigationId'||String(d.productionTestInstanceId)===String(c.id)&&String(d.mainBatchId)===String(batch)&&d.status==='DECIDED'))}
  else if(['inspectionReportId','supersedesReportId'].includes(f))rows=Array.isArray(c.reports)?c.reports as IncomingRow[]:await list('/quality/inspection-reports',{inspectionRequestId:c.inspectionRequestId,materialLotId:lotId})
  else if(f==='supersedesDecisionId')rows=(c.decisions??[]) as IncomingRow[]
  else if(f==='optionalSpecificationItemIds'){
   const version=request?.qcSpecificationVersionId??c.qcSpecificationVersionId
   if(version){const standard=await api<IncomingRow>({url:`/quality/specification-versions/${version}`});rows=((standard.items??[]) as IncomingRow[]).filter(i=>i.required===false).map(i=>({...i,id:i.specificationItemId??i.id}))}
  }else{
   const tasks=await list('/quality/inspection-tasks',{inspectionRequestId:c.inspectionRequestId,materialLotId:lotId})
   rows=referenceRows(f,tasks).map(x=>x.record)
   if(f==='originalResultRevisionId')rows=rows.filter(r=>r.resultConclusion==='FAIL')
   if(f==='originalExecutionId')rows=rows.filter(r=>!r.originalExecutionId)
   if(f==='selectedResultRevisionId'&&c.id){const executions=tasks.flatMap(t=>((t.items??[]) as IncomingRow[]).flatMap(i=>(i.executions??[]) as IncomingRow[]));const allowed=new Set(executions.filter(e=>String(e.approvedInvestigationId)===String(c.id)).map(e=>String(e.id)));rows=rows.filter(r=>allowed.has(String(r.testExecutionId)))}
  }
  if(current!==sequence)return
  if(['inspectionReportId','supersedesReportId'].includes(f))rows=rows.filter(r=>r.status==='APPROVED')
  const next=referenceRows('plain',rows);options.value=append?[...options.value,...next.filter(v=>!options.value.some(x=>x.value===v.value))]:next
 }catch(e){if(current===sequence)error.value=errorMessage(e)}finally{if(current===sequence)loading.value=false}
}
let searchTimer:ReturnType<typeof setTimeout>|undefined
function searchRecords(value:string){search.value=value;clearTimeout(searchTimer);++sequence;loading.value=false;searchTimer=setTimeout(()=>void load(),250)}
function selected(raw:unknown){const value=raw==null?'':String(raw);emit('update:modelValue',value);const row=options.value.find(o=>o.value===value)?.record;if(row)emit('select',row)}
function nextPage(event:UIEvent){const target=event.target as HTMLElement;if(hasMore.value&&!loading.value&&target.scrollTop+target.clientHeight>=target.scrollHeight-24){page.value++;void load(true)}}
const choices=computed(()=>{const found=options.value.filter(o=>!['qcSpecificationItemId','specificationItemId','operationExecutionId','weighingRecordId','formulaItemId','parameterDefId','inspectionRequestItemId','supersedesDecisionId','optionalSpecificationItemIds'].includes(props.field)||!search.value||o.label.toLocaleLowerCase().includes(search.value.trim().toLocaleLowerCase()));return props.modelValue&&!options.value.some(o=>o.value===String(props.modelValue))?[{value:String(props.modelValue),label:`已绑定记录 ${props.modelValue}`},...found]:found})
onBeforeUnmount(()=>{clearTimeout(searchTimer);++sequence})
watch([()=>props.field,contextKey],()=>void load(),{immediate:true})
</script>
<template><div class="incoming-reference"><a-select :value="modelValue?String(modelValue):undefined" :aria-label="inputLabel??label(field)" :placeholder="`输入编号或名称搜索${inputLabel??label(field)}`" :disabled="disabled" :loading="loading" show-search allow-clear :filter-option="false" :options="choices" :default-active-first-option="false" :not-found-content="loading?'正在搜索…':error?'搜索失败，请重新输入':'没有匹配记录'" @search="searchRecords" @change="selected" @popup-scroll="nextPage"/><p v-if="error" role="alert" class="error">{{error}}</p></div></template>
<style scoped>.incoming-reference{min-width:0;flex:1;width:100%}.incoming-reference :deep(.ant-select){width:100%}.error{font-size:12px;color:#b42318;margin:4px 0}</style>
