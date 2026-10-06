<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {FileTextOutlined} from '@ant-design/icons-vue'
import {useRoute,useRouter,onBeforeRouteLeave} from 'vue-router'
import {api,idempotencyKey} from '../../api/http'
import {useControlledRequest} from '../../api/controlled'
import {useAuthStore} from '../../stores/auth'
import {schemas,operations,resources,actionLabels,commandBody,display,initial,selectSigningTarget,reviewResultIds,serverAllows,type SigningTarget,type Schema} from './incomingModel'
import IncomingFormFields from './IncomingFormFields.vue'
import IncomingFacts from './IncomingFacts.vue'
import IncomingDocument from './IncomingDocument.vue'
import {documentNumber,documentStatus} from './incomingDetailModel'
import ProductionDeviationControls from './ProductionDeviationControls.vue'
import ProductionSampleControls from './ProductionSampleControls.vue'
type Row=Record<string,unknown>
type Target=SigningTarget
const route=useRoute(),router=useRouter(),auth=useAuthStore(),request=useControlledRequest(),{busy,error,conflict}=request
const resource=computed(()=>String(route.meta.resource)),qa=computed(()=>resource.value==='release'),create=computed(()=>route.meta.mode==='create'),def=computed(()=>resources[resource.value]),base=computed(()=>qa.value?`/qa/material-lots/${route.params.id}`:resource.value==='deviations'?'/deviations':resource.value==='samples'&&route.query.scope==='PRODUCTION'?'/samples':`/quality/${resource.value}`)
const production=computed(()=>route.query.scope==='PRODUCTION'||record.value?.investigationScope==='PRODUCTION'||record.value?.sampleScope==='PRODUCTION')
const signatureOpen=ref(false),labelResult=ref<Row|null>(null)
const record=ref<Row|null>(null),form=ref<Row>({}),selected=ref<{url:string;schema:string;action:string;source:Row}|null>(null),password=ref(''),submitted=ref(false)
const resultItem=computed(()=>itemRows.value.find(item=>executions(item).some(e=>e.id===selected.value?.source.id)))
const schema=computed(()=>{
 let base=schemas[selected.value?.schema??def.value?.create??'Reason']!
 if(selected.value?.action==='release-decisions')base={...base,properties:{...base.properties,releaseBasis:{...base.properties!.releaseBasis,enum:base.properties!.releaseBasis!.enum!.filter(v=>v!=='INSPECTION_EXEMPT')}}}
 if(!['results','revisions'].includes(selected.value?.action??'')||!resultItem.value)return base
 const properties={...base.properties};const required=[...(base.required??[])]
 if(resultItem.value.resultType==='NUMERIC'){delete properties.resultText;if(form.value.resultConclusion!=='INCONCLUSIVE')required.push('resultNumeric','resultUnitId')}
 else{delete properties.resultNumeric;delete properties.resultUnitId;required.push('resultText')}
 return {...base,properties,required} as Schema
})
const lockedFields=computed(()=>[...(selected.value?.action==='review'&&resource.value==='inspection-tasks'?['resultRevisionIds']:[]),...(['results','revisions'].includes(selected.value?.action??'')?['testExecutionId','resultUnitId']:[]),...(['executions','approved-retests'].includes(selected.value?.action??'')?['performedBy']:[])])
const fieldContext=computed(()=>({...record.value,...selected.value?.source,...form.value}))
const executing=computed(()=>route.meta.mode==='execute')
const itemRows=computed(()=>(record.value?.items??[]) as Row[])
const title=computed(()=>production.value?(resource.value==='deviations'?'生产质量调查':'生产样品'):qa.value?'物料放行':def.value?.title??'来料质量')
const signed=computed(()=>!!schema.value?.properties?.reauthToken)
function available(action:string){return serverAllows(record.value,action)}
const actions=computed(()=>{if(production.value)return [];const names=qa.value?['release-decisions']:def.value?.actions??[];return names.map(action=>({action,op:operations.find(o=>o.path===(qa.value?'/qa/material-lots/{lotId}':`${base.value}/{id}`)+`/${action}`)})).filter(x=>x.op&&auth.can(x.op.permission)&&available(x.action)&&(x.action!=='details'||executing.value))})
function open(action:string,url:string,source:Row=record.value??{}){
 const template=url.replace(/\/[0-9]+(?=\/|$)/g,'/{id}')
 const op=operations.find(o=>o.method==='POST'&&(o.path===template||o.path===template.replace('{id}','{lotId}')||o.path===template.replace('{id}','{revisionId}')))
 if(!op||!auth.can(op.permission)||!serverAllows(source,action))return
 selected.value={url,schema:op.schema,action,source};form.value=initial(schemas[op.schema]!) as Row
 if(['results','revisions'].includes(action)){
  form.value.testExecutionId=source.id
  const item=itemRows.value.find(i=>executions(i).some(e=>e.id===source.id))
  if(item?.unitId)form.value.resultUnitId=item.unitId
  if(action==='revisions'){const all=revisions(source);const last=all[all.length-1];if(last){for(const key of ['resultNumeric','resultText','resultConclusion'])if(last[key]!==null)form.value[key]=last[key]}}
 }
 if(['executions','approved-retests'].includes(action))form.value.performedBy=auth.identity?.userId
 if(action==='review'&&resource.value==='inspection-tasks')form.value.resultRevisionIds=reviewResultIds(record.value??{})
 password.value='';signatureOpen.value=false;request.clear()
}
function executions(item:Row){return (item.executions??[]) as Row[]}
function revisions(execution:Row){return (execution.revisions??[]) as Row[]}
async function load(){busy.value=true;request.clear();try{if(create.value){record.value=null;form.value=initial(schemas[def.value!.create!]!) as Row}else record.value=await api<Row>({url:qa.value?`${base.value}/release-review`:`${base.value}/${route.params.id}`})}catch(e){await request.failure(e)}finally{busy.value=false}}
function target():Target|undefined{
 const action=selected.value?.action??''
 let source=selected.value?.source??record.value??{}
 if(['results','revisions'].includes(action))source=itemRows.value.flatMap(executions).find(e=>String(e.id)===String(form.value.testExecutionId))??source
 return selectSigningTarget((source.signingTargets??record.value?.signingTargets??[]) as Target[],action,form.value.decision)
}
function prepare(){try{commandBody(schema.value,form.value);if(signed.value){if(!target())throw Error('缺少匹配的签名目标，请重新加载记录');signatureOpen.value=true}else void save()}catch(e){error.value=e instanceof Error?e.message:'请检查输入'}}
let pendingFingerprint='',pendingKey=idempotencyKey()
async function save(){busy.value=true;error.value='';try{const body=commandBody(schema.value,form.value) as Row;const url=create.value?base.value:selected.value!.url;const version=record.value?.versionNo as number|undefined;const fingerprint=JSON.stringify({url,body,version});if(fingerprint!==pendingFingerprint){pendingFingerprint=fingerprint;pendingKey=idempotencyKey()}
 if(signed.value){const binding=target();if(!binding)throw Error('缺少匹配的签名目标，请重新加载记录');if(!password.value)throw Error('请输入签名密码');const reauth=await api<{reauthToken:string}>({url:'/auth/reauth',method:'POST',data:{objectType:binding.objectType,objectId:binding.recordId,meaning:binding.meaning,recordVersion:binding.recordVersion,credential:password.value}});body.reauthToken=reauth.reauthToken}
 const result=await api<Row>({url,method:'POST',data:body,headers:{'Idempotency-Key':pendingKey,...(version===undefined?{}:{'If-Match':`"${version}"`})}});if(selected.value?.action==='label')labelResult.value=result;pendingFingerprint='';submitted.value=true;signatureOpen.value=false;selected.value=null;form.value={};if(create.value)await router.replace(`${base.value}/${result.id}`);else await load()
 }catch(e){await request.failure(e)}finally{password.value='';busy.value=false}}
function can(permission:string){return auth.can(permission)}
onBeforeRouteLeave(()=>submitted.value||!Object.keys(form.value).length||window.confirm('离开将放弃未保存的输入，是否继续？'))
watch(()=>route.fullPath,()=>{selected.value=null;labelResult.value=null;submitted.value=false;void load()},{immediate:true})
</script>
<!-- UI Template: T2 for create, T3 for incoming documents, T6 for material release review. -->
<template><main class="admin-page master-page" :class="{'inspection-record-page':['inspection-tasks','inspection-reports'].includes(resource)&&!create&&!production}" :data-ui-template="create?'T2':qa?'T6':'T3'"><header class="admin-page-header"><div><a-button type="link" @click="router.push({path:qa?`/wms/material-lots/${route.params.id}`:base,query:route.query})">← 返回</a-button><h1><FileTextOutlined v-if="resource==='inspection-reports'&&!create" class="report-heading-icon"/>{{create?'新增':''}}{{title}}<template v-if="record&&['inspection-tasks','inspection-reports'].includes(resource)&&!production"> <span class="inspection-number">{{documentNumber(record)}}</span> <a-tag :color="(resource==='inspection-reports'?record.overallResult==='FAIL':record.status==='QC_FAILED')?'red':(resource==='inspection-reports'?record.overallResult==='PASS':record.status==='QC_PASSED')?'green':'blue'">{{resource==='inspection-reports'?display(record.overallResult):documentStatus(resource,record)}}</a-tag></template></h1><p v-if="record&&['inspection-tasks','inspection-reports'].includes(resource)&&!production" class="inspection-subtitle">来料检验 · 质量控制</p><p v-if="record&&!production&&!qa&&!['inspection-tasks','inspection-reports'].includes(resource)" class="object-identity">{{documentNumber(record)}} <a-tag>{{documentStatus(resource,record)}}</a-tag></p></div><a-space wrap><a-button v-for="a in qa?[]:actions" :key="a.action" :type="(resource==='inspection-tasks'&&a.action==='submit-review')||(resource==='inspection-reports'&&a.action==='approve')?'primary':'default'" @click="open(a.action,`${qa?base:`${base}/${route.params.id}`}/${a.action}`)">{{actionLabels[a.action]}}</a-button><a-button v-if="!create&&!executing&&['sampling-tasks','inspection-tasks'].includes(resource)&&(serverAllows(record,'details')||itemRows.some(item=>serverAllows(item,'executions')||serverAllows(item,'approved-retests')||executions(item).some(e=>serverAllows(e,'results')||serverAllows(e,'revisions'))))&&can(resource==='sampling-tasks'?'qms:sampling:execute':'qms:test:execute')" type="primary" @click="router.push(`${base}/${route.params.id}/execute`)">进入执行</a-button></a-space></header>
 <a-alert v-if="error" type="error" :message="error" show-icon/><a-alert v-if="conflict" type="warning" message="记录已变化。输入已保留，请核对最新记录后重试。"><template #action><a-button @click="load">重新加载</a-button></template></a-alert>
 <a-card v-if="labelResult" title="样品标签" class="form-section"><IncomingFacts :value="labelResult"/></a-card><IncomingDocument v-if="record&&!production" :record="record" :resource="resource"><template #decision-actions><a-space wrap><a-button v-for="a in actions" :key="a.action" @click="open(a.action,`${base}/${a.action}`)">{{actionLabels[a.action]}}</a-button></a-space></template></IncomingDocument><a-card v-else-if="record" :title="`${title}详情`" class="form-section"><IncomingFacts :value="record"/></a-card>
 <template v-if="executing&&resource==='inspection-tasks'"><a-card v-for="item in itemRows" :key="String(item.id)" :title="String(item.itemName??item.id)" class="form-section"><a-space wrap><a-button v-if="serverAllows(item,'executions')&&can('qms:test:execute')" @click="open('executions',`/quality/inspection-items/${item.id}/executions`,item)">记录原始检验</a-button><a-button v-if="serverAllows(item,'approved-retests')&&can('qms:test:execute')" @click="open('approved-retests',`/quality/inspection-items/${item.id}/approved-retests`,item)">记录批准复检</a-button></a-space><div v-for="execution in executions(item)" :key="String(execution.id)"><p>检验执行 {{execution.id}}</p><a-button v-if="serverAllows(execution,'results')&&can('qms:test:execute')" @click="open('results',`/quality/inspection-items/${item.id}/results`,execution)">提交原始结果</a-button><a-button v-for="revision in revisions(execution).slice(-1)" v-show="serverAllows(revision,'revisions')&&serverAllows(execution,'revisions')&&can('qms:test:correct')" :key="String(revision.id)" @click="open('revisions',`/quality/test-results/${revision.id}/revisions`,execution)">更正结果（保留原始记录）</a-button></div></a-card></template>
 <ProductionDeviationControls v-if="production&&resource==='deviations'" :record="record??{}" :create="create" @saved="r=>create?router.replace({path:'/deviations/'+r.id,query:{scope:'PRODUCTION'}}):load()"/><ProductionSampleControls v-if="production&&resource==='samples'&&record" :record="record" @saved="load"/>
 <a-card v-if="!production&&(create||selected)" :title="create?`新增${title}`:actionLabels[selected!.action]" class="form-section"><p v-if="create&&resource==='inspection-reports'">根据请验单的全部必检项目自动汇总已复核结果，并保留原始结果和调查依据。</p><form @submit.prevent="prepare"><IncomingFormFields v-model="form" :schema="schema" :disabled="busy" :context="fieldContext" :locked-fields="lockedFields"/><a-space><a-button type="primary" html-type="submit" :loading="busy" :disabled="conflict">{{signed?'核对并签名':'提交'}}</a-button><a-button v-if="!create" :disabled="busy" @click="selected=null;form={}">取消</a-button></a-space></form></a-card>
<a-modal v-model:open="signatureOpen" title="电子签名确认" :confirm-loading="busy" ok-text="签名并提交" cancel-text="取消" @ok="save" @cancel="password=''">
 <p>签名含义：{{display(target()?.meaning)}}；绑定记录 {{target()?.recordId}}，版本 {{target()?.recordVersion}}。</p>
 <p>请核对当前操作及原因。提交后签名与业务证据一同保存。</p>
 <div class="master-form"><label class="form-full-row"><span class="form-field-label">签名密码</span><input v-model="password" type="password" autocomplete="current-password" class="master-native-input" aria-label="签名密码" :disabled="busy" required @keydown.enter.prevent="save"/></label></div>
 </a-modal></main></template>
<style scoped>
#app .inspection-record-page>.admin-page-header :deep(.ant-tag-green){background:#e1f7e9;color:#23834b;border:0;padding:3px 10px;font-weight:600}
.report-heading-icon{font-size:30px;color:#1677ff;background:#e8f1ff;border-radius:8px;padding:12px}
#app .inspection-record-page>.admin-page-header{background:transparent;border:0;box-shadow:none;padding:4px 0 16px}#app .inspection-record-page>.admin-page-header>div:has(>.ant-btn-link)>p{margin-left:68px}#app .inspection-record-page>.admin-page-header :deep(.ant-tag-red){background:#ffe9e9;border:0;padding:3px 10px;font-size:12px;font-weight:600;border-radius:4px}@media(max-width:767px){#app .inspection-record-page>.admin-page-header>div:has(>.ant-btn-link)>p{margin-left:0}}
.inspection-record-page>.admin-page-header{background:transparent;border:0;box-shadow:none;padding:4px 0 16px}.inspection-record-page>.admin-page-header>div{display:flex;align-items:center;flex-wrap:wrap;gap:12px}.inspection-record-page h1{display:flex;align-items:center;flex-wrap:wrap;gap:12px}.inspection-number{font-size:18px;font-weight:600}.inspection-subtitle{flex-basis:100%;color:#66758a;font-size:12px;margin:0 0 0 68px}@media(max-width:767px){.inspection-record-page>.admin-page-header>div{gap:8px}.inspection-number{font-size:15px}.inspection-subtitle{margin-left:0}}
.signature-input{margin:16px 0}form>.ant-space{margin-top:16px}.object-identity{display:flex;flex-wrap:wrap;align-items:center;gap:8px;margin:8px 0 0;font-weight:600}.object-identity>span{font-size:12px;font-weight:400}</style>
