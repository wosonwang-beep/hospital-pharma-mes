<script setup lang="ts">
import {computed,reactive,ref,watch,nextTick} from 'vue'
import {message} from 'ant-design-vue'
import axios from 'axios'
import {api,errorMessage,idempotencyKey,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {supplierPayload,type SupplierLink} from '../../master/material'

interface Supplier {id:string;supplierCode:string;supplierName:string;qualificationStatus:string;enabled:boolean;validTo:string|null}
const props=defineProps<{materialId:string;materialName?:string;writable:boolean}>()
const auth=useAuthStore(),items=ref<SupplierLink[]>([]),options=ref<Supplier[]>([])
const canWrite=computed(()=>props.writable&&auth.can('master:material:update'))
const modalOpen=ref(false),editingId=ref<string|null>(null),busy=ref(false),error=ref(''),modalError=ref(''),conflict=ref(false)
const discardPrompt=ref(false),revokeOpen=ref(false),revokeId=ref<string|null>(null),revokeError=ref('')
const draft=reactive({supplierId:'',manufacturerName:'',approved:true,preferred:false,validTo:''})
const original=ref('')
const dirty=computed(()=>JSON.stringify(draft)!==original.value)
const selectedSupplier=computed(()=>items.value.find(x=>x.supplierId===draft.supplierId))
const selectedQualification=computed(()=>options.value.find(s=>s.id===draft.supplierId)?.qualificationStatus??selectedSupplier.value?.qualificationStatus??'—')
const selectOptions=computed(()=>{
 const fromOptions=options.value.map(s=>({value:s.id,label:s.supplierName+'（'+s.supplierCode+'）'}))
 const current=selectedSupplier.value
 if(current&&!fromOptions.some(s=>s.value===current.supplierId))fromOptions.unshift({value:current.supplierId,label:(current.supplierName??current.supplierCode??current.supplierId)+'（'+(current.supplierCode??'')+'）'})
 return fromOptions
})
let pendingKey=idempotencyKey(),pendingBody=''
let returnFocus:HTMLElement|null=null
function restoreFocus(){if(returnFocus?.isConnected)returnFocus.focus();returnFocus=null}
async function search(keyword=''){
 if(!auth.can('master:supplier:view'))return
 try{
  const p=await api<Page<Supplier>>({url:'/suppliers',params:{keyword,qualificationStatus:'合格',size:100}})
  options.value=p.items.filter(s=>s.enabled&&(s.id===draft.supplierId||!items.value.some(x=>x.supplierId===s.id)))
 }catch(e){if(modalOpen.value)modalError.value=errorMessage(e);else error.value=errorMessage(e)}
}
async function load(){
 error.value='';conflict.value=false
 try{
  const result=await api<{suppliers:SupplierLink[]}>({url:'/materials/'+props.materialId+'/suppliers'})
  items.value=result.suppliers.map(x=>({...x}))
  await search()
 }catch(e){error.value=errorMessage(e)}
}
function openAdd(event?:MouseEvent){
 if(!canWrite.value||!auth.can('master:supplier:view'))return
 returnFocus=event?.currentTarget instanceof HTMLElement?event.currentTarget:(document.activeElement instanceof HTMLElement?document.activeElement:null)
 editingId.value=null
 Object.assign(draft,{supplierId:'',manufacturerName:'',approved:true,preferred:!items.value.some(x=>x.approved),validTo:''})
 original.value=JSON.stringify(draft);modalError.value='';discardPrompt.value=false;conflict.value=false
 pendingKey=idempotencyKey();pendingBody='';modalOpen.value=true
 void search()
}
function openEdit(row:SupplierLink,event?:MouseEvent){
 if(!canWrite.value)return
 returnFocus=event?.currentTarget instanceof HTMLElement?event.currentTarget:(document.activeElement instanceof HTMLElement?document.activeElement:null)
 editingId.value=row.supplierId
 Object.assign(draft,{supplierId:row.supplierId,manufacturerName:row.manufacturerName??'',approved:row.approved,preferred:row.preferred,validTo:row.validTo??''})
 original.value=JSON.stringify(draft);modalError.value='';discardPrompt.value=false;conflict.value=false
 pendingKey=idempotencyKey();pendingBody='';modalOpen.value=true
 void search()
}
function cancel(){
 if(busy.value)return
 if(dirty.value){discardPrompt.value=true;return}
 modalOpen.value=false
}
function discard(){discardPrompt.value=false;modalOpen.value=false;modalError.value=''}
async function persist(next:SupplierLink[]){
 const data={reason:'基础资料维护（系统记录）',suppliers:supplierPayload(next)}
 const encoded=JSON.stringify(data)
 if(encoded!==pendingBody){pendingKey=idempotencyKey();pendingBody=encoded}
 await api({url:'/materials/'+props.materialId+'/suppliers',method:'PUT',data,headers:{'Idempotency-Key':pendingKey}})
 pendingBody=''
 await load()
}
async function save(){
 if(!canWrite.value||busy.value)return
 modalError.value='';conflict.value=false
 const current=items.value.find(x=>x.supplierId===draft.supplierId)
 const chosen=options.value.find(x=>x.id===draft.supplierId)
 if(!draft.supplierId||(!current&&!chosen)){modalError.value='请选择已合格且启用的供应商';return}
 if(!editingId.value&&!chosen){modalError.value='请选择已合格且启用的供应商';return}
 if(draft.manufacturerName.length>200){modalError.value='生产厂家不能超过200字';return}
 if(!editingId.value&&current){modalError.value='该供应商已关联，请编辑已有关系';return}
 const item:SupplierLink={
  ...(current??{}),supplierId:draft.supplierId,
  supplierCode:chosen?.supplierCode??current?.supplierCode,
  supplierName:chosen?.supplierName??current?.supplierName,
  qualificationStatus:chosen?.qualificationStatus??current?.qualificationStatus,
  manufacturerName:draft.manufacturerName.trim()||null,
  approved:draft.approved,preferred:draft.approved&&draft.preferred,validTo:draft.validTo||null
 }
 let next=editingId.value?items.value.map(x=>x.supplierId===editingId.value?item:{...x}):[...items.value.map(x=>({...x})),item]
 if(item.preferred)next=next.map(x=>({...x,preferred:x.supplierId===item.supplierId}))
 busy.value=true
 try{
  await persist(next)
  modalOpen.value=false
  message.success('供应商关系已保存')
 }catch(e){
  modalError.value=e instanceof Error&&!axios.isAxiosError(e)?e.message:errorMessage(e)
  conflict.value=axios.isAxiosError(e)&&e.response?.status===409
  if(conflict.value){pendingBody='';pendingKey=idempotencyKey()}
 }finally{busy.value=false}
}
function requestRevoke(item:SupplierLink){
 if(!canWrite.value||!item.approved)return
 revokeId.value=item.supplierId;revokeError.value='';revokeOpen.value=true
}
async function revoke(){
 if(!canWrite.value||!revokeId.value||busy.value)return
 const current=items.value.find(x=>x.supplierId===revokeId.value)
 if(!current)return
 const next=items.value.map(x=>x.supplierId===current.supplierId?{...x,approved:false,preferred:false}:{...x})
 if(next.some(x=>x.approved)&&!next.some(x=>x.preferred&&x.approved)){
  revokeError.value='请先将另一家有效供应商设为首选，再撤销当前关系'
  return
 }
 revokeError.value='';busy.value=true
 try{
  await persist(next)
  revokeOpen.value=false
  message.success('供应商关系已撤销，历史记录仍保留')
 }catch(e){revokeError.value=e instanceof Error&&!axios.isAxiosError(e)?e.message:errorMessage(e)}
 finally{busy.value=false}
}
watch(modalOpen,(isOpen)=>{if(isOpen)void nextTick().then(()=>requestAnimationFrame(()=>document.querySelector<HTMLInputElement>('#supplier-modal-form .ant-select-selection-search-input')?.focus()));else{const target=returnFocus;if(target)window.setTimeout(()=>{if(!modalOpen.value&&target.isConnected)target.focus()},450)}})
watch(()=>props.materialId,()=>{modalOpen.value=false;revokeOpen.value=false;void load()},{immediate:true})
</script>

<template>
<a-card title="供应商与首选供应商" class="form-section material-supplier-section">
 <template #extra><a-button v-if="canWrite&&auth.can('master:supplier:view')" type="primary" @click="(event:MouseEvent)=>openAdd(event)">＋ 添加供应商</a-button></template>
 <a-alert v-if="error" type="error" :message="error" show-icon/>
 <div class="table-scroll supplier-desktop"><table><thead><tr><th>供应商</th><th>物料</th><th>生产厂家</th><th>资格状态</th><th>关系有效</th><th>首选供应商</th><th>关系有效截止日期（UTC）</th><th v-if="canWrite">操作</th></tr></thead>
 <tbody><tr v-for="item in items" :key="item.supplierId"><td>{{item.supplierName??item.supplierId}} <small>{{item.supplierCode}}</small></td><td>{{materialName??'—'}}</td><td>{{item.manufacturerName||'—'}}</td><td>{{item.qualificationStatus||'—'}}</td><td><a-tag :color="item.approved?'success':'default'">{{item.approved?'有效':'已撤销'}}</a-tag></td><td><a-tag :color="item.preferred?'blue':'default'">{{item.preferred?'是':'否'}}</a-tag></td><td>{{item.validTo??'—'}}</td><td v-if="canWrite" class="supplier-row-actions"><a-button type="link" @click="(event:MouseEvent)=>openEdit(item,event)">编辑</a-button><a-button type="link" danger :disabled="!item.approved" @click="requestRevoke(item)">撤销</a-button></td></tr></tbody></table></div>
 <div class="supplier-mobile"><div v-for="item in items" :key="item.supplierId" class="supplier-mobile-item">
  <strong>{{item.supplierName??item.supplierId}}</strong>
  <span>生产厂家：{{item.manufacturerName||'—'}}</span>
  <span>资格：{{item.qualificationStatus||'—'}}　关系：{{item.approved?'有效':'已撤销'}}　首选：{{item.preferred?'是':'否'}}</span>
  <span>有效截止日期：{{item.validTo??'—'}}</span>
  <div v-if="canWrite" class="supplier-row-actions"><a-button type="link" @click="(event:MouseEvent)=>openEdit(item,event)">编辑</a-button><a-button type="link" danger :disabled="!item.approved" @click="requestRevoke(item)">撤销</a-button></div>
 </div></div>
 <a-empty v-if="!items.length" description="暂无供应商关系"/>
 <a-modal :open="modalOpen&&canWrite" centered :width="'min(620px, calc(100vw - 24px))'" :closable="!busy" :mask-closable="false" :title="editingId?'编辑供应商关联':'新增供应商关联'" @cancel="cancel" @after-close="restoreFocus">
  <a-alert v-if="modalError" type="error" :message="modalError" show-icon class="supplier-modal-alert"/>
  <a-alert v-if="conflict" type="warning" message="关系已发生变化，当前输入已保留；请核对后重试。" show-icon class="supplier-modal-alert"/>
  <form id="supplier-modal-form" class="supplier-modal-fields" @submit.prevent="save">
   <label><span>供应商 <em>*</em></span><a-select :value="draft.supplierId||undefined" aria-label="供应商" show-search :filter-option="false" :disabled="!!editingId||!auth.can('master:supplier:view')" :options="selectOptions" @search="search" @change="(v:unknown)=>draft.supplierId=String(v??'')"/></label>
   <label><span>生产厂家</span><a-input v-model:value="draft.manufacturerName" aria-label="生产厂家" :maxlength="200"/></label>
   <label><span>资格状态</span><a-input :value="selectedQualification" aria-label="资格状态" disabled/></label>
   <label><span>关系有效</span><a-switch v-model:checked="draft.approved" aria-label="关系有效" @change="(v:boolean)=>{if(!v)draft.preferred=false}"/></label>
   <label><span>首选供应商</span><a-switch v-model:checked="draft.preferred" aria-label="首选供应商" :disabled="!draft.approved"/></label>
   <label><span>关系有效截止日期（UTC）</span><input v-model="draft.validTo" type="date" class="master-native-input" aria-label="关系有效截止日期"/></label>
  </form>
  <div v-if="discardPrompt" class="supplier-discard"><strong>放弃未保存的修改？</strong><div><a-button type="primary" @click="discardPrompt=false">继续编辑</a-button><a-button danger @click="discard">放弃更改</a-button></div></div>
  <template #footer><div class="supplier-modal-actions"><a-button type="primary" :loading="busy" @click="save">保存</a-button><a-button :disabled="busy" @click="cancel">取消</a-button></div></template>
 </a-modal>
 <a-modal :open="revokeOpen&&canWrite" centered :width="'min(480px, calc(100vw - 24px))'" title="撤销供应商关系" :mask-closable="false" :closable="!busy" @cancel="revokeOpen=false">
  <p>撤销后保留原有关系记录。若当前供应商是首选，请先指定另一家有效供应商为首选。</p>
  <a-alert v-if="revokeError" type="error" show-icon :message="revokeError"/>
  <template #footer><div class="supplier-modal-actions"><a-button type="primary" danger :loading="busy" @click="revoke">确认撤销</a-button><a-button :disabled="busy" @click="revokeOpen=false">取消</a-button></div></template>
 </a-modal>
</a-card>
</template>

<style scoped>
.supplier-modal-fields{display:grid;gap:14px;margin:14px 0}
.supplier-modal-fields>label{display:grid;grid-template-columns:160px minmax(0,1fr);align-items:center;gap:12px;min-height:36px}
.supplier-modal-fields>label>span{text-align:right;white-space:nowrap}
.supplier-modal-fields :deep(.ant-select),.supplier-modal-fields :deep(.ant-input),.supplier-modal-fields .master-native-input{width:100%;min-width:0}
.supplier-modal-fields em{font-style:normal;color:#d4380d}
.supplier-modal-actions{display:flex;justify-content:flex-end;gap:12px}
.supplier-modal-alert{margin-bottom:12px}
.supplier-discard{background:#fff7e6;border:1px solid #ffd591;border-radius:6px;padding:12px;margin-top:12px;display:flex;justify-content:space-between;gap:10px;align-items:center}
.supplier-discard>div{display:flex;gap:8px}
.supplier-row-actions{white-space:nowrap}
.supplier-mobile{display:none}
@media(max-width:700px){
 .supplier-desktop{display:none}
 .supplier-mobile{display:grid;gap:10px}
 .supplier-mobile-item{border:1px solid var(--mes-ui-border,#e2e8f0);border-radius:6px;padding:12px;display:grid;gap:5px}
 .supplier-modal-fields>label{grid-template-columns:108px minmax(0,1fr);gap:8px}
 .supplier-discard{flex-direction:column;align-items:flex-start}
}
</style>
