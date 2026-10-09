<script setup lang="ts">
import {computed,onMounted,ref,watch} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,errorMessage,type Page} from '../../api/http'
import {bookApi} from '../../api/ebrBook'
import {useAuthStore} from '../../stores/auth'
import type {Package} from '../process/model'
import type {EbrDetail,EbrSummary} from './types'
import {bookKinds,bookEntryGroup,newBookEntry,type BookDefinition,type BookEntry,type BookFlowType,type BookKind,type BookTemplate} from './bookModel'

type FlowNode={key:string;type:BookFlowType;code:string;name:string}
type BusinessOption={key:string;title:string;kind?:BookKind;flowCode:string;flowName:string;archiveStage:'PRODUCTION_REVIEW'|'QA_REVIEW'|'POST_RELEASE';disabled?:boolean;note?:string}

const route=useRoute(),router=useRouter(),auth=useAuthStore()
const mode=computed(()=>String(route.meta.mode||'view'))
const readonly=computed(()=>mode.value==='view')
const id=computed(()=>String(route.params.id||''))
const record=ref<BookTemplate|null>(null)
const definition=ref<BookDefinition>({templateName:'',processPackageId:'',ebrTemplateVersionId:'',entries:[]})
const templateCode=ref(''),reason=ref(''),busy=ref(false),error=ref(''),notice=ref('')
const processes=ref<Package[]>([]),ebrOptions=ref<EbrSummary[]>([]),reference=ref<EbrDetail|null>(null)
const selectedFlowKey=ref('SYSTEM:BASIC'),selectedEntryCode=ref('')
const processModal=ref(false),businessModal=ref(false),previewOpen=ref(false)
const processOperationId=ref(''),selectedFormCodes=ref<string[]>([]),selectedBusinessKeys=ref<string[]>([])
const legacy=computed(()=>!!record.value&&!record.value.definition.processPackageId)
const canEdit=computed(()=>!readonly.value&&!legacy.value&&auth.can('ebr:template:create'))
const statusLabels:Record<string,string>={DRAFT:'草稿',PUBLISHED:'已发布',INACTIVE:'已停用'}

const businessOptions:BusinessOption[]=[
 {key:'ORDER',title:'生产指令',kind:'PRODUCTION_ORDER',flowCode:'PREP',flowName:'生产准备与物料',archiveStage:'PRODUCTION_REVIEW',note:'生产管理原始记录'},
 {key:'MATERIAL_REQUEST',title:'领料申请单',kind:'MATERIAL_REQUESTS',flowCode:'PREP',flowName:'生产准备与物料',archiveStage:'PRODUCTION_REVIEW',note:'WMS 原始领料申请'},
 {key:'MATERIAL_ISSUE',title:'原辅料出库单',kind:'MATERIAL_ISSUES',flowCode:'PREP',flowName:'生产准备与物料',archiveStage:'PRODUCTION_REVIEW',note:'WMS 原始出库记录'},
 {key:'CHARGE',title:'投料记录',kind:'CHARGES',flowCode:'SUMMARY',flowName:'物料平衡及生产总结',archiveStage:'PRODUCTION_REVIEW',note:'生产执行实际投料'},
 {key:'INSPECTION_REQUEST',title:'请验单',kind:'INSPECTION_REQUESTS',flowCode:'QUALITY',flowName:'检验与质量记录',archiveStage:'QA_REVIEW',note:'QC 成品请验原始记录'},
 {key:'SAMPLING',title:'取样记录',kind:'SAMPLING_RECORDS',flowCode:'QUALITY',flowName:'检验与质量记录',archiveStage:'QA_REVIEW',note:'QC 成品取样原始记录'},
 {key:'INSPECTION_RECORD',title:'检验记录',kind:'INSPECTION_RECORDS',flowCode:'QUALITY',flowName:'检验与质量记录',archiveStage:'QA_REVIEW',note:'QC 实际检验及结果记录'},
 {key:'INSPECTION_REPORT',title:'检验报告',kind:'INSPECTION_REPORTS',flowCode:'QUALITY',flowName:'检验与质量记录',archiveStage:'QA_REVIEW',note:'QC 已生成检验报告'},
 {key:'DEVIATION',title:'偏差 / OOS / CAPA',kind:'QUALITY_INVESTIGATIONS',flowCode:'QUALITY',flowName:'检验与质量记录',archiveStage:'QA_REVIEW',note:'QMS 条件关联质量调查'},
 {key:'BALANCE',title:'物料平衡记录',kind:'MATERIAL_BALANCE',flowCode:'SUMMARY',flowName:'物料平衡及生产总结',archiveStage:'QA_REVIEW',note:'QMS 实际物料平衡结果'},
 {key:'QA_RELEASE',title:'QA 放行记录',kind:'QA_DECISIONS',flowCode:'RELEASE',flowName:'批审核与放行',archiveStage:'POST_RELEASE',note:'QA 正式放行决策；放行后归档'},
 {key:'ATTACHMENTS',title:'相关原始凭证与附件',kind:'ATTACHMENTS',flowCode:'ATTACH',flowName:'附件与补充记录',archiveStage:'POST_RELEASE',note:'受控附件原件引用'}
]

const processLabel=computed(()=>processes.value.find(p=>p.id===definition.value.processPackageId)?.packageCode||'未选择')
const operationFlows=computed<FlowNode[]>(()=>reference.value?.operationChoices.map(o=>({key:'PROCESS:'+o.operationCode,type:'PROCESS',code:o.operationCode,name:o.operationName}))??[])
const fixedFlows:FlowNode[]=[
 {key:'SYSTEM:BASIC',type:'SYSTEM',code:'BASIC',name:'批次基本信息'},
 {key:'BUSINESS:PREP',type:'BUSINESS',code:'PREP',name:'生产准备与物料'},
 {key:'BUSINESS:QUALITY',type:'BUSINESS',code:'QUALITY',name:'检验与质量记录'},
 {key:'BUSINESS:SUMMARY',type:'BUSINESS',code:'SUMMARY',name:'物料平衡及生产总结'},
 {key:'BUSINESS:RELEASE',type:'BUSINESS',code:'RELEASE',name:'批审核与放行'},
 {key:'BUSINESS:ATTACH',type:'BUSINESS',code:'ATTACH',name:'附件与补充记录'}
]
const currentFlow=computed<FlowNode>(()=>[...fixedFlows,...operationFlows.value].find(f=>f.key===selectedFlowKey.value)??fixedFlows[0]!)
const currentEntries=computed(()=>definition.value.entries.filter(e=>(e.flowType+':'+e.flowCode)===selectedFlowKey.value).sort((a,b)=>a.order-b.order))
const selectedEntry=computed(()=>definition.value.entries.find(e=>e.code===selectedEntryCode.value)??null)
const selectedOperation=computed(()=>reference.value?.operationChoices.find(o=>o.id===processOperationId.value)??null)
const availableForms=computed(()=>reference.value?.definition.forms.filter(f=>f.operationDefId===processOperationId.value)??[])
const usedForms=computed(()=>new Set(definition.value.entries.filter(e=>e.kind==='FORM').map(e=>e.formCode)))
const isExistingDraft=computed(()=>record.value?.status==='DRAFT')

function groupCount(type:BookFlowType,code:string){return definition.value.entries.filter(e=>e.flowType===type&&e.flowCode===code).length}
function selectFlow(flow:FlowNode){selectedFlowKey.value=flow.key;selectedEntryCode.value=''}
function selectEntry(entry:BookEntry){selectedEntryCode.value=entry.code}
function nextOrder(){return Math.max(0,...definition.value.entries.map(e=>e.order))+1}
function uniqueEntryCode(prefix='REC'){let n=1,code=prefix+'_'+n;const used=new Set(definition.value.entries.map(e=>e.code));while(used.has(code)){n++;code=prefix+'_'+n}return code}
function close(){void router.push('/ebr/book-templates')}

async function loadEbrOptions(){
 ebrOptions.value=[];reference.value=null
 if(!definition.value.processPackageId)return
 const page=await api<Page<EbrSummary>>({url:'/ebr/templates',params:{processPackageId:definition.value.processPackageId,status:'EFFECTIVE',size:100}})
 ebrOptions.value=page.items
 if(!definition.value.ebrTemplateVersionId&&page.items.length===1)definition.value.ebrTemplateVersionId=page.items[0]!.id
 await loadReference()
}
async function loadReference(){
 reference.value=null
 if(!definition.value.ebrTemplateVersionId)return
 reference.value=await api<EbrDetail>({url:'/ebr/templates/'+definition.value.ebrTemplateVersionId})
 if(!operationFlows.value.some(f=>f.key===selectedFlowKey.value)&&selectedFlowKey.value.startsWith('PROCESS:'))selectedFlowKey.value='SYSTEM:BASIC'
}
async function load(){
 busy.value=true;error.value=''
 try{
  const processPage=await api<Page<Package>>({url:'/process-packages',params:{size:100}})
  processes.value=processPage.items
  if(id.value){
   record.value=await bookApi.get(id.value)
   templateCode.value=record.value.templateCode
   if(record.value.definition.processPackageId)definition.value=JSON.parse(JSON.stringify(record.value.definition))
   else{
    definition.value={templateName:record.value.definition.varietyName||'历史电子批记录模板',entries:record.value.definition.entries.map((e,index)=>({...e,flowType:'BUSINESS',flowCode:'LEGACY_'+(index+1),flowName:e.chapter||'历史目录'}))}
   }
  }
  await loadEbrOptions()
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
async function changeProcess(){
 definition.value.ebrTemplateVersionId=''
 definition.value.entries=[]
 selectedFlowKey.value='SYSTEM:BASIC';selectedEntryCode.value=''
 try{await loadEbrOptions()}catch(e){error.value=errorMessage(e)}
}
async function save(){
 if(!canEdit.value)return
 error.value='';notice.value=''
 if(!templateCode.value.trim()||!definition.value.templateName?.trim()){error.value='请填写模板编码和模板名称';return}
 if(!definition.value.processPackageId||!definition.value.ebrTemplateVersionId){error.value='请选择关联生产工艺及其已生效工序表单版本';return}
 if(!definition.value.entries.length){error.value='电子批记录模板至少需要一条表单记录';return}
 if(!reason.value.trim()){error.value='请填写本次版本原因';return}
 busy.value=true
 try{
  const created=await bookApi.create(templateCode.value.trim(),definition.value,reason.value.trim())
  record.value=created
  notice.value='已保存新的受控草稿版本。'
  await router.replace('/ebr/book-templates/'+created.id+'/edit')
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
async function publish(){
 if(!record.value||record.value.status!=='DRAFT'||!auth.can('ebr:template:publish'))return
 if(!reason.value.trim()){error.value='发布前请填写操作原因';return}
 busy.value=true;error.value=''
 try{record.value=await bookApi.transition(record.value,'publish',reason.value.trim());notice.value='电子批记录模板已发布并可供新生产批次冻结使用。'}
 catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}

function openProcessForms(){
 if(!reference.value){error.value='请先选择已生效的工序表单版本';return}
 processOperationId.value=reference.value.operationChoices[0]?.id||''
 selectedFormCodes.value=[]
 processModal.value=true
}
function addProcessForms(){
 const op=selectedOperation.value;if(!op)return
 for(const code of selectedFormCodes.value){
  if(usedForms.value.has(code))continue
  const form=reference.value?.definition.forms.find(f=>f.formCode===code);if(!form)continue
  const entry=newBookEntry(nextOrder(),'PROCESS',op.operationCode,op.operationName,'FORM')
  entry.code=uniqueEntryCode('FORM')
  entry.title=form.formName;entry.operationCode=op.operationCode;entry.formCode=form.formCode;entry.fields=form.fields.map(f=>f.fieldCode)
  entry.archiveStage='PRODUCTION_REVIEW';entry.applicability='始终适用'
  definition.value.entries.push(entry)
 }
 selectedFlowKey.value='PROCESS:'+op.operationCode
 processModal.value=false
}
function openBusiness(){selectedBusinessKeys.value=[];businessModal.value=true}
function addBusiness(){
 for(const key of selectedBusinessKeys.value){
  const item=businessOptions.find(o=>o.key===key);if(!item?.kind||item.disabled)continue
  const entry=newBookEntry(nextOrder(),'BUSINESS',item.flowCode,item.flowName,item.kind)
  entry.code=uniqueEntryCode(item.kind)
  entry.title=item.title;entry.archiveStage=item.archiveStage;entry.applicability=item.key==='ATTACHMENTS'?'按需生成':'始终适用'
  definition.value.entries.push(entry)
 }
 businessModal.value=false
}
function removeEntry(entry:BookEntry){if(!canEdit.value)return;definition.value.entries.splice(definition.value.entries.indexOf(entry),1);selectedEntryCode.value=''}
function moveEntry(entry:BookEntry,delta:number){
 const ordered=[...definition.value.entries].sort((a,b)=>a.order-b.order)
 const from=ordered.indexOf(entry),to=from+delta;if(from<0||to<0||to>=ordered.length)return
 const other=ordered[to]!,order=entry.order;entry.order=other.order;other.order=order
}
function sourceLabel(entry:BookEntry){return entry.kind==='FORM'?'工序表单':bookKinds[entry.kind]}
function generationLabel(entry:BookEntry){return entry.kind==='FORM'?'自动引用':entry.kind==='ATTACHMENTS'?'按需关联':'业务关联'}
function previewGroups(){
 const map=new Map<string,BookEntry[]>()
 for(const e of [...definition.value.entries].sort((a,b)=>a.order-b.order)){const key=bookEntryGroup(e);if(!map.has(key))map.set(key,[]);map.get(key)!.push(e)}
 return [...map.entries()]
}
watch(()=>definition.value.ebrTemplateVersionId,async()=>{try{await loadReference()}catch(e){error.value=errorMessage(e)}})
onMounted(load)
</script>

<template>
<main data-ui-template="T2" class="admin-page master-page ebr-template-editor">
 <header class="admin-page-header editor-header">
  <div>
   <a-button type="link" @click="close">← 电子批记录模板</a-button>
   <h1>{{definition.templateName||'新建电子批记录模板'}}</h1>
   <p>
    <a-tag v-if="record">V{{record.revision}}</a-tag>
    <a-tag :color="record?.status==='PUBLISHED'?'green':'orange'">{{record?statusLabels[record.status]||record.status:'新建'}}</a-tag>
    <span>关联工艺：{{processLabel}}</span>
   </p>
  </div>
  <a-space wrap>
   <a-button @click="previewOpen=true">预览</a-button>
   <a-button v-if="canEdit" :loading="busy" @click="save">保存</a-button>
   <a-button v-if="canEdit&&isExistingDraft&&auth.can('ebr:template:publish')" type="primary" :loading="busy" @click="publish">发布</a-button>
   <a-button @click="close">关闭</a-button>
  </a-space>
 </header>

 <a-alert v-if="legacy" type="warning" show-icon message="这是历史产品映射/目录结构，只读保留。新电子批记录模板按生产工艺、工序、业务流程和表单记录建立。"/>
 <a-alert v-if="error" type="error" show-icon :message="error"/>
 <a-alert v-if="notice" type="success" show-icon :message="notice"/>

 <a-card title="模板基本信息" class="form-section meta-card">
  <div class="meta-form">
   <label><span>模板编码</span><a-input v-model:value="templateCode" :disabled="readonly||!!record" maxlength="64"/></label>
   <label><span>模板名称</span><a-input v-model:value="definition.templateName" :disabled="!canEdit" maxlength="120"/></label>
   <label><span>关联生产工艺</span><a-select v-model:value="definition.processPackageId" :disabled="!canEdit||!!record" show-search option-filter-prop="label" :options="processes.map(p=>({value:p.id,label:p.packageCode}))" @change="changeProcess"/></label>
   <label><span>工序表单版本</span><a-select v-model:value="definition.ebrTemplateVersionId" :disabled="!canEdit||!!record" show-search option-filter-prop="label" :options="ebrOptions.map(t=>({value:t.id,label:(t.templateName||t.templateCode)+' · V'+t.version}))"/></label>
  </div>
 </a-card>

 <section class="composition">
  <aside class="directory-pane">
   <div class="pane-title"><strong>目录结构</strong></div>
   <button class="flow-row" :class="{active:selectedFlowKey==='SYSTEM:BASIC'}" @click="selectFlow(fixedFlows[0]!)"><span>01</span><b>批次基本信息</b><em>{{groupCount('SYSTEM','BASIC')}}</em></button>
   <button class="flow-row" :class="{active:selectedFlowKey==='BUSINESS:PREP'}" @click="selectFlow(fixedFlows[1]!)"><span>02</span><b>生产准备与物料</b><em>{{groupCount('BUSINESS','PREP')}}</em></button>
   <div class="process-tree">
    <div class="process-parent"><span>03</span><b>生产工序记录</b></div>
    <button v-for="op in operationFlows" :key="op.key" class="operation-row" :class="{active:selectedFlowKey===op.key}" @click="selectFlow(op)"><span>›</span><b>{{op.code}} {{op.name}}</b><em>{{groupCount('PROCESS',op.code)}}</em></button>
   </div>
   <button class="flow-row" :class="{active:selectedFlowKey==='BUSINESS:QUALITY'}" @click="selectFlow(fixedFlows[2]!)"><span>04</span><b>检验与质量记录</b><em>{{groupCount('BUSINESS','QUALITY')}}</em></button>
   <button class="flow-row" :class="{active:selectedFlowKey==='BUSINESS:SUMMARY'}" @click="selectFlow(fixedFlows[3]!)"><span>05</span><b>物料平衡及生产总结</b><em>{{groupCount('BUSINESS','SUMMARY')}}</em></button>
   <button class="flow-row" :class="{active:selectedFlowKey==='BUSINESS:RELEASE'}" @click="selectFlow(fixedFlows[4]!)"><span>06</span><b>批审核与放行</b><em>{{groupCount('BUSINESS','RELEASE')}}</em></button>
   <button class="flow-row" :class="{active:selectedFlowKey==='BUSINESS:ATTACH'}" @click="selectFlow(fixedFlows[5]!)"><span>07</span><b>附件与补充记录</b><em>{{groupCount('BUSINESS','ATTACH')}}</em></button>
  </aside>

  <section class="records-pane">
   <header class="records-head">
    <div><strong>{{currentFlow.name}}</strong><p>{{currentFlow.type==='PROCESS'?'来自关联生产工艺的工序，可引用该工序的一份或多份表单记录。':'业务流程只引用原业务模块记录，不重复创建单据。'}}</p></div>
    <a-space v-if="canEdit">
     <a-button v-if="currentFlow.type==='PROCESS'" @click="openProcessForms">＋ 添加工序表单</a-button>
     <a-button v-else @click="openBusiness">＋ 添加业务单据</a-button>
    </a-space>
   </header>
   <a-table :data-source="currentEntries" row-key="code" :pagination="false" size="middle" :scroll="{x:720}">
    <a-table-column title="序号" data-index="order" width="70"/>
    <a-table-column title="记录名称" width="210"><template #default="{record:entry}"><a-button type="link" class="record-link" @click="selectEntry(entry)">{{entry.title}}</a-button></template></a-table-column>
    <a-table-column title="来源类型" width="130"><template #default="{record:entry}">{{sourceLabel(entry)}}</template></a-table-column>
    <a-table-column title="生成方式" width="115"><template #default="{record:entry}">{{generationLabel(entry)}}</template></a-table-column>
    <a-table-column title="必需" width="70"><template #default="{record:entry}"><a-tag :color="entry.required?'green':'default'">{{entry.required?'是':'否'}}</a-tag></template></a-table-column>
    <a-table-column title="操作" width="170"><template #default="{record:entry}"><a-space :size="0"><a-button type="link" @click="selectEntry(entry)">配置</a-button><a-button v-if="canEdit" type="link" @click="moveEntry(entry,-1)">上移</a-button><a-button v-if="canEdit" type="link" @click="moveEntry(entry,1)">下移</a-button></a-space></template></a-table-column>
    <template #emptyText><a-empty description="当前工序/流程尚未配置表单记录"/></template>
   </a-table>
  </section>

  <aside class="property-pane">
   <div class="pane-title"><strong>记录属性</strong></div>
   <div v-if="selectedEntry" class="property-form">
    <label><span>记录名称</span><a-input v-model:value="selectedEntry.title" :disabled="!canEdit"/></label>
    <label><span>来源类型</span><a-input :value="sourceLabel(selectedEntry)" disabled/></label>
    <label><span>生成方式</span><a-input :value="generationLabel(selectedEntry)" disabled/></label>
    <label><span>是否必需</span><a-switch v-model:checked="selectedEntry.required" :disabled="!canEdit" @change="selectedEntry.minCount=selectedEntry.required?Math.max(1,selectedEntry.minCount):0"/></label>
    <label><span>记录数量</span><a-input-number v-model:value="selectedEntry.minCount" :disabled="!canEdit" :min="selectedEntry.required?1:0" :max="1000"/></label>
    <label><span>归档阶段</span><a-select v-model:value="selectedEntry.archiveStage" :disabled="!canEdit" :options="[{value:'PRODUCTION_REVIEW',label:'生产审核前'},{value:'QA_REVIEW',label:'QA审核前'},{value:'POST_RELEASE',label:'放行后归档'}]"/></label>
    <label><span>适用条件</span><a-input v-model:value="selectedEntry.applicability" :disabled="!canEdit" placeholder="如：始终适用 / 发生偏差时"/></label>
    <template v-if="selectedEntry.kind==='FORM'">
     <label><span>工序编码</span><a-input v-model:value="selectedEntry.operationCode" disabled/></label>
     <label><span>表单编码</span><a-input v-model:value="selectedEntry.formCode" disabled/></label>
     <label><span>实例范围</span><a-select v-model:value="selectedEntry.scope" :disabled="!canEdit" :options="[{value:'OPERATION',label:'每个工序执行实例'},{value:'BATCH',label:'整批一份'}]"/></label>
    </template>
    <a-button v-if="canEdit" danger block @click="removeEntry(selectedEntry)">移除此记录</a-button>
   </div>
   <div v-else class="property-empty">选择中间的表单记录后配置属性。</div>
  </aside>
 </section>

 <a-card v-if="canEdit" title="版本原因" class="form-section reason-card">
  <label><span>操作原因</span><a-textarea v-model:value="reason" :rows="2" maxlength="1000" placeholder="填写本次模板新版本或发布原因"/></label>
 </a-card>

 <a-modal v-model:open="processModal" title="添加工序表单" width="720px" ok-text="确定" @ok="addProcessForms">
  <div class="modal-form"><label><span>选择工序</span><a-select v-model:value="processOperationId" :options="reference?.operationChoices.map(o=>({value:o.id,label:o.operationCode+' '+o.operationName}))"/></label></div>
  <a-table :data-source="availableForms" row-key="formCode" :pagination="false" size="small">
   <a-table-column title="" width="50"><template #default="{record:form}"><a-checkbox :checked="selectedFormCodes.includes(form.formCode)" :disabled="usedForms.has(form.formCode)" @change="(e:any)=>selectedFormCodes=e.target.checked?[...selectedFormCodes,form.formCode]:selectedFormCodes.filter(c=>c!==form.formCode)"/></template></a-table-column>
   <a-table-column title="表单名称" data-index="formName"/>
   <a-table-column title="表单编码" data-index="formCode"/>
   <a-table-column title="字段数" width="90"><template #default="{record:form}">{{form.fields.length}}</template></a-table-column>
  </a-table>
 </a-modal>

 <a-modal v-model:open="businessModal" title="添加业务单据" width="760px" ok-text="确定" @ok="addBusiness">
  <a-alert type="info" show-icon message="EBR 只引用各模块已经产生的原始业务记录，不在此重新创建业务单据。"/>
  <div class="business-list">
   <label v-for="item in businessOptions" :key="item.key" :class="{disabled:item.disabled}">
    <a-checkbox :checked="selectedBusinessKeys.includes(item.key)" :disabled="item.disabled" @change="(e:any)=>selectedBusinessKeys=e.target.checked?[...selectedBusinessKeys,item.key]:selectedBusinessKeys.filter(k=>k!==item.key)"/>
    <span><b>{{item.title}}</b><small>{{item.note||'已接入原始业务记录'}}</small></span>
   </label>
  </div>
 </a-modal>

 <a-modal v-model:open="previewOpen" title="电子批记录模板预览" width="820px" :footer="null">
  <div class="preview-title"><strong>{{definition.templateName||'未命名模板'}}</strong><span>{{processLabel}}</span></div>
  <div v-for="[group,items] in previewGroups()" :key="group" class="preview-group">
   <h4>{{group}} <small>{{items.length}} 项</small></h4>
   <div v-for="item in items" :key="item.code" class="preview-row"><span>{{item.order}}. {{item.title}}</span><a-tag>{{sourceLabel(item)}}</a-tag><em>{{item.required?'必需':'条件适用'}}</em></div>
  </div>
 </a-modal>
</main>
</template>

<style scoped>
.ebr-template-editor{max-width:100%;min-width:0}.editor-header p{display:flex;align-items:center;gap:8px}.meta-card{margin-bottom:16px}.meta-form{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px 24px}.meta-form label,.modal-form label,.reason-card label,.property-form label{display:flex;align-items:center;gap:8px;min-width:0}.meta-form label>span,.modal-form label>span,.reason-card label>span,.property-form label>span{flex:0 0 110px;color:var(--mes-ui-secondary);font-size:13px;white-space:nowrap}.meta-form :deep(.ant-input),.meta-form :deep(.ant-select),.modal-form :deep(.ant-select),.property-form :deep(.ant-input),.property-form :deep(.ant-select),.property-form :deep(.ant-input-number){flex:1;min-width:0}.composition{display:grid;grid-template-columns:220px minmax(460px,1fr) 300px;background:#fff;border:1px solid var(--mes-ui-border);border-radius:8px;overflow:hidden;min-height:580px;margin-bottom:16px}.directory-pane{padding:12px;border-right:1px solid var(--mes-ui-border);background:#fcfdff}.pane-title{height:36px;display:flex;align-items:center;border-bottom:1px solid var(--mes-ui-border);margin-bottom:8px}.flow-row,.operation-row{width:100%;border:0;background:transparent;border-radius:6px;display:grid;grid-template-columns:28px 1fr 28px;gap:4px;align-items:center;text-align:left;padding:8px 7px;color:var(--mes-ui-text);cursor:pointer}.flow-row:hover,.operation-row:hover,.flow-row.active,.operation-row.active{background:#eaf2ff;color:var(--mes-ui-primary)}.flow-row span,.process-parent span{font-size:12px;color:var(--mes-ui-secondary)}.flow-row b,.operation-row b,.process-parent b{font-size:13px;font-weight:600}.flow-row em,.operation-row em{font-size:11px;font-style:normal;color:var(--mes-ui-secondary);text-align:right}.process-parent{display:grid;grid-template-columns:28px 1fr;gap:4px;padding:9px 7px;margin-top:2px}.operation-row{padding-left:22px;grid-template-columns:18px 1fr 24px}.records-pane{padding:12px;min-width:0}.records-head{min-height:54px;display:flex;align-items:flex-start;justify-content:space-between;gap:12px;border-bottom:1px solid var(--mes-ui-border);margin-bottom:12px}.records-head strong{font-size:15px}.records-head p{margin:3px 0 10px;font-size:12px;color:var(--mes-ui-secondary)}.record-link{padding-left:0;white-space:normal;text-align:left}.property-pane{padding:12px;border-left:1px solid var(--mes-ui-border)}.property-form{display:flex;flex-direction:column;gap:13px}.property-form label>span{flex-basis:92px}.property-empty{padding:34px 8px;color:var(--mes-ui-secondary);text-align:center;font-size:12px}.reason-card label>span{flex-basis:110px}.reason-card :deep(textarea){flex:1}.business-list{display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-top:14px}.business-list label{display:flex;gap:8px;border:1px solid var(--mes-ui-border);border-radius:7px;padding:10px}.business-list label.disabled{background:#f7f8fa;color:#8993a3}.business-list span{display:flex;flex-direction:column}.business-list small{font-size:11px;color:var(--mes-ui-secondary);margin-top:3px}.preview-title{display:flex;justify-content:space-between;border-bottom:1px solid var(--mes-ui-border);padding-bottom:12px}.preview-group{padding:12px 0;border-bottom:1px solid var(--mes-ui-border)}.preview-group h4{margin:0 0 6px}.preview-group h4 small{color:var(--mes-ui-secondary);font-weight:400}.preview-row{display:grid;grid-template-columns:1fr 120px 80px;align-items:center;padding:6px 12px}.preview-row em{font-style:normal;color:var(--mes-ui-secondary);font-size:12px}
@media(max-width:1180px){.composition{grid-template-columns:190px minmax(400px,1fr)}.property-pane{grid-column:1/-1;border-left:0;border-top:1px solid var(--mes-ui-border)}.property-form{display:grid;grid-template-columns:repeat(2,minmax(0,1fr))}}
@media(max-width:760px){.meta-form,.composition,.property-form,.business-list{grid-template-columns:1fr}.directory-pane{border-right:0;border-bottom:1px solid var(--mes-ui-border)}.records-head{flex-direction:column}.preview-row{grid-template-columns:1fr}}
</style>
