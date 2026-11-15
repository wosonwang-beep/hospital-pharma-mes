<script setup lang="ts">
import {ref,watch,computed,nextTick} from 'vue'
import axios from 'axios'
import {api,errorMessage,idempotencyKey,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import type {MasterRecord} from '../../master/resources'
interface Unit {id:string;unitName:string;unitCode:string}
interface Conversion extends MasterRecord {fromUnitId:string;toUnitId:string;factor:string}
const props=defineProps<{materialId:string;baseUnitId:string;writable:boolean}>(),auth=useAuthStore(),items=ref<Conversion[]>([]),units=ref<Unit[]>([]),editing=ref<Conversion|null>(null),open=ref(false),from=ref(''),to=ref(''),factor=ref(''),reason=ref(''),error=ref(''),busy=ref(false),conflict=ref(false),page=ref(0),total=ref(0)
const canWrite=computed(()=>props.writable&&auth.can(`master:uom:${editing.value?'update':'create'}`))
const original=ref(''),discardPrompt=ref(false),dirty=computed(()=>JSON.stringify({from:from.value,to:to.value,factor:factor.value})!==original.value)
function cancel(){if(busy.value)return;if(dirty.value){discardPrompt.value=true;return}open.value=false}
function discard(){discardPrompt.value=false;open.value=false}
let pendingKey=idempotencyKey(),pendingBody=''
let returnFocus:HTMLElement|null=null
function restoreFocus(){if(returnFocus?.isConnected)returnFocus.focus();returnFocus=null}
function unitName(id:string){return units.value.find(x=>x.id===id)?.unitName??id}
async function load(p=0){if(!auth.can('master:uom:view'))return;error.value='';try{const [rows,u]=await Promise.all([api<Page<Conversion>>({url:'/unit-conversions',params:{materialId:props.materialId,page:p,size:20}}),api<Page<Unit>>({url:'/units',params:{size:100}})]);items.value=rows.items;page.value=rows.page;total.value=rows.total;units.value=u.items;for(const id of new Set([props.baseUnitId,...rows.items.flatMap(r=>[r.fromUnitId,r.toUnitId])]))if(!units.value.some(u=>u.id===id))units.value.push(await api<Unit>({url:`/units/${id}`}))}catch(e){error.value=errorMessage(e)}}
async function search(keyword=''){try{const p=await api<Page<Unit>>({url:'/units',params:{keyword,size:100}});const selected=units.value.filter(u=>[from.value,to.value].includes(u.id));units.value=[...p.items,...selected.filter(u=>!p.items.some(n=>n.id===u.id))]}catch(e){error.value=errorMessage(e)}}
function start(row:Conversion|null=null,event?:MouseEvent){if(!props.writable||!auth.can(`master:uom:${row?'update':'create'}`))return;returnFocus=event?.currentTarget instanceof HTMLElement?event.currentTarget:(document.activeElement instanceof HTMLElement?document.activeElement:null);editing.value=row;from.value=row?.fromUnitId??props.baseUnitId;to.value=row?.toUnitId??'';factor.value=row?.factor??'';reason.value='';error.value='';conflict.value=false;discardPrompt.value=false;original.value=JSON.stringify({from:from.value,to:to.value,factor:factor.value});open.value=true}
async function save(){busy.value=true;error.value='';try{if(!from.value||!to.value||!factor.value.trim())throw new Error('请选择原单位、目标单位并填写换算因子');if(from.value===to.value)throw new Error('原单位与目标单位不能相同');if(!Number.isFinite(Number(factor.value))||Number(factor.value)<=0)throw new Error('换算因子必须大于零');if(items.value.some(x=>x.fromUnitId===from.value&&x.toUnitId===to.value&&x.id!==editing.value?.id))throw new Error('这组单位换算关系已经存在');const data={fromUnitId:from.value,toUnitId:to.value,factor:factor.value,materialId:props.materialId,...(editing.value?{action:'UPDATE',reason:'基础资料维护（系统记录）'}:{})};const encoded=JSON.stringify(data);if(encoded!==pendingBody){pendingKey=idempotencyKey();pendingBody=encoded}await api({url:`/unit-conversions${editing.value?`/${editing.value.id}`:''}`,method:editing.value?'PUT':'POST',data,headers:{'Idempotency-Key':pendingKey}});pendingBody='';open.value=false;await load(page.value)}catch(e){error.value=e instanceof Error&&!axios.isAxiosError(e)?e.message:errorMessage(e);conflict.value=axios.isAxiosError(e)&&e.response?.status===409;if(conflict.value){pendingBody='';pendingKey=idempotencyKey()}}finally{busy.value=false}}
watch(open,(isOpen)=>{if(isOpen)void nextTick().then(()=>requestAnimationFrame(()=>document.querySelector<HTMLInputElement>('#conversion-modal-form .ant-select-selection-search-input')?.focus()));else{const target=returnFocus;if(target)window.setTimeout(()=>{if(!open.value&&target.isConnected)target.focus()},450)}})
watch(()=>props.materialId,()=>{open.value=false;void load()},{immediate:true})
</script>
<template><a-card title="物料单位换算" class="form-section"><template #extra><a-button v-if="writable&&auth.can('master:uom:create')" type="primary" @click="(event:MouseEvent)=>start(null,event)">＋ 添加单位换算</a-button></template><template v-if="auth.can('master:uom:view')"><a-alert v-if="error" type="error" :message="error"/><a-alert v-if="conflict" type="warning" message="换算已被修改，请重新载入该行后编辑。"/>
<div class="table-scroll"><table><thead><tr><th>原单位</th><th>目标单位</th><th>换算因子</th><th v-if="writable">操作</th></tr></thead><tbody><tr v-for="row in items" :key="row.id"><td>{{unitName(row.fromUnitId)}}</td><td>{{unitName(row.toUnitId)}}</td><td>{{row.factor}}</td><td v-if="writable"><a-button v-if="auth.can('master:uom:update')" @click="(event:MouseEvent)=>start(row,event)">编辑换算</a-button></td></tr></tbody></table></div><a-empty v-if="!items.length" description="暂无物料专属换算"/><a-pagination v-if="total>20" :current="page+1" :total="total" :page-size="20" :show-size-changer="false" @change="(p:number)=>load(p-1)"/>
<a-modal :open="open&&canWrite" centered :width="'min(560px, calc(100vw - 24px))'" :mask-closable="false" :closable="!busy" :title="editing?'编辑单位换算':'新增单位换算'" @cancel="cancel" @after-close="restoreFocus">
 <a-alert v-if="error" type="error" show-icon :message="error" class="child-modal-alert"/>
 <a-alert v-if="conflict" type="warning" show-icon message="记录发生冲突，当前输入已保留，请核对后重试。" class="child-modal-alert"/>
 <form id="conversion-modal-form" class="child-modal-fields" @submit.prevent="save">
  <label><span>原单位 <em>*</em></span><a-select v-model:value="from" aria-label="原单位" show-search :filter-option="false" @search="search"><a-select-option v-for="u in units" :key="u.id" :value="u.id">{{u.unitName}}（{{u.unitCode}}）</a-select-option></a-select></label>
  <label><span>目标单位 <em>*</em></span><a-select v-model:value="to" aria-label="目标单位" show-search :filter-option="false" @search="search"><a-select-option v-for="u in units" :key="u.id" :value="u.id">{{u.unitName}}（{{u.unitCode}}）</a-select-option></a-select></label>
  <label><span>换算因子 <em>*</em></span><a-input v-model:value="factor" aria-label="换算因子" inputmode="decimal"/></label>
 </form>
 <div v-if="discardPrompt" class="child-discard"><strong>放弃未保存的修改？</strong><div><a-button type="primary" @click="discardPrompt=false">继续编辑</a-button><a-button danger @click="discard">放弃更改</a-button></div></div>
 <template #footer><div class="child-modal-actions"><a-button type="primary" :loading="busy" @click="save">保存</a-button><a-button :disabled="busy" @click="cancel">取消</a-button></div></template>
</a-modal></template><p v-else class="muted">需要单位查看权限才能查看换算。</p></a-card></template>
<style scoped>
.child-modal-fields{display:grid;gap:14px;margin:12px 0}
.child-modal-fields>label{display:grid;grid-template-columns:112px minmax(0,1fr);gap:10px;align-items:center;min-height:36px}
.child-modal-fields>label>span{text-align:right;white-space:nowrap}
.child-modal-fields :deep(.ant-select),.child-modal-fields :deep(.ant-input){width:100%;min-width:0}
.child-modal-fields em{font-style:normal;color:#d4380d}
.child-modal-alert{margin-bottom:12px}
.child-modal-actions{display:flex;justify-content:flex-end;gap:12px}
.child-discard{background:#fff7e6;border:1px solid #ffd591;padding:12px;border-radius:6px;display:flex;justify-content:space-between;gap:10px;align-items:center;margin-top:12px}
.child-discard>div{display:flex;gap:8px}
@media(max-width:600px){.child-modal-fields>label{grid-template-columns:96px minmax(0,1fr);gap:8px}.child-discard{flex-direction:column;align-items:flex-start}}
</style>
