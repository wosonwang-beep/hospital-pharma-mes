<script setup lang="ts">
import {computed,ref,reactive,watch} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import axios from 'axios'
import {api,errorMessage,idempotencyKey,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {statusLabel,type MasterRecord} from '../../master/resources'
import {materialFields,materialDisplayFields,materialDisplaySections,initialMaterialForm,materialPayload} from '../../master/material'
import MaterialSuppliers from './MaterialSuppliers.vue'
import MaterialConversions from './MaterialConversions.vue'
interface Unit {id:string;unitCode:string;unitName:string}
const route=useRoute(),router=useRouter(),auth=useAuthStore(),record=ref<MasterRecord|null>(null),form=reactive(initialMaterialForm()),units=ref<Unit[]>([]),error=ref(''),busy=ref(false),conflict=ref(false),reason=ref('')
const activeTab=ref('basic')
const visibleSections=computed(()=>materialDisplaySections.filter(s=>!readonly.value||activeTab.value==='basic'||activeTab.value==='quality'&&s.title==='管理 / 质量属性'))
function sectionFields(keys:string[]){return keys.map(key=>materialDisplayFields.find(f=>f.key===key)!)}
const create=computed(()=>route.meta.mode==='create'),readonly=computed(()=>route.meta.mode==='view')
let pendingKey=idempotencyKey(),pendingBody=''
async function searchUnits(keyword=''){if(!auth.can('master:uom:view'))return;try{const result=await api<Page<Unit>>({url:'/units',params:{keyword,size:100}});const selected=units.value.filter(u=>[form.baseUnitId,form.packUnitId].includes(u.id));units.value=[...result.items,...selected.filter(u=>!result.items.some(n=>n.id===u.id))]}catch(e){error.value=errorMessage(e)}}
async function load(){error.value='';conflict.value=false;record.value=null;Object.assign(form,initialMaterialForm());reason.value='';try{if(!create.value){record.value=await api<MasterRecord>({url:`/materials/${route.params.id}`});for(const f of materialFields){const value=record.value[f.key];form[f.key]=f.kind==='datetime'&&typeof value==='string'?value.replace(/Z$/,''):value??null}units.value=[{id:String(record.value.baseUnitId),unitCode:String(record.value.baseUnitCode??''),unitName:String(record.value.baseUnitName??'')},...(record.value.packUnitId?[{id:String(record.value.packUnitId),unitCode:'',unitName:String(record.value.packUnitName??'')}]:[])]}await searchUnits()}catch(e){error.value=errorMessage(e)}}
async function save(disable=false){busy.value=true;error.value='';try{if(!create.value&&!reason.value.trim())throw new Error('请填写变更原因');const r=record.value;const body=disable?{versionNo:r?.versionNo,reason:reason.value.trim()}:{...materialPayload(form,!create.value),...(!create.value?{versionNo:r?.versionNo,reason:reason.value.trim()}:{})};const url=create.value?'/materials':`/materials/${r?.id}${disable?'/disable':''}`,method=create.value||disable?'POST':'PUT';const encoded=JSON.stringify({url,body});if(encoded!==pendingBody){pendingKey=idempotencyKey();pendingBody=encoded}const updated=await api<MasterRecord>({url,method,data:body,headers:{'Idempotency-Key':pendingKey,...(r?{'If-Match':`"${r.versionNo}"`}:{})}});record.value=updated;reason.value='';pendingBody='';await router.push({path:`/master/materials/${updated.id}`,query:route.query})}catch(e){error.value=e instanceof Error&&!axios.isAxiosError(e)?e.message:errorMessage(e);conflict.value=axios.isAxiosError(e)&&e.response?.status===409}finally{busy.value=false}}
function edit(){void router.push({path:`/master/materials/${record.value?.id}/edit`,query:route.query})}
watch(()=>route.fullPath,load,{immediate:true})
</script>
<!-- UI Template: T2 create/edit; T3 read-only material detail. Global UI V2. -->
<template><main :data-ui-template="readonly?'T3':'T2'" class="admin-page master-page material-detail-page" :class="{'material-form-page':!readonly}"><header class="admin-page-header"><div><a-button type="link" @click="router.push({path:'/master/materials',query:route.query})">← 返回物料列表</a-button><h1>{{create?'新增物料':readonly?'物料基本信息':'维护物料基本信息'}}</h1><p v-if="record" class="material-object-summary"><strong>{{record.materialName}}</strong><span>{{record.materialCode}}</span><a-tag :color="record.status==='ACTIVE'?'green':undefined">{{statusLabel[String(record.status)]}}</a-tag></p></div><a-button v-if="readonly&&auth.can('master:material:update')" type="primary" @click="edit">维护基本信息</a-button></header>
<a-alert v-if="error" type="error" :message="error" show-icon/><a-alert v-if="conflict" type="warning" message="记录已变化，输入已保留，请重新加载后继续。"><template #action><a-button @click="load">重新加载</a-button></template></a-alert>
<a-tabs v-if="readonly" v-model:active-key="activeTab"><a-tab-pane key="basic" tab="基本信息"/><a-tab-pane key="quality" tab="质量控制"/><a-tab-pane key="suppliers" tab="供应商"/><a-tab-pane key="usage" tab="使用范围"/></a-tabs><a-card v-if="!readonly||activeTab==='basic'||activeTab==='quality'"  class="form-section material-basic-card"><form id="material-basic-form" class="master-form" :class="{'material-quality-only':readonly&&activeTab==='quality'}" @submit.prevent="save()"><section v-for="(section,index) in visibleSections" :key="section.title" class="material-field-section" :class="`material-group-${index}`"><h3 class="material-section-title">{{section.title}}</h3><label v-for="f in sectionFields(section.keys)" :key="f.key" :class="{'form-full-row':f.key==='remark'}"><span class="form-field-label">{{f.label}}<span v-if="f.required&&!readonly" class="required-mark"> *</span></span>
<span v-if="readonly" class="master-read-value">{{f.key==='baseUnitId'?record?.baseUnitName:f.key==='packUnitId'?(record?.packUnitName??'—'):form[f.key]===null?'—':typeof form[f.key]==='boolean'?(form[f.key]?'是':'否'):form[f.key]}}</span>
<input v-else-if="f.kind==='boolean'" v-model="form[f.key]" type="checkbox"/>
<a-select v-else-if="f.kind==='unit'" :value="typeof form[f.key]==='string'?String(form[f.key]):undefined" :aria-label="f.label" show-search :filter-option="false" :allow-clear="!f.required" :disabled="!auth.can('master:uom:view')" placeholder="选择单位" @search="searchUnits" @change="(v:unknown)=>form[f.key]=v==null?null:String(v)"><a-select-option v-for="u in units" :key="u.id" :value="u.id">{{u.unitName}} {{u.unitCode?`（${u.unitCode}）`:''}}</a-select-option></a-select>
<textarea v-else-if="f.key==='remark'" :value="String(form[f.key]??'')" class="master-native-input" :maxlength="f.max??undefined" @input="form[f.key]=($event.target as HTMLTextAreaElement).value"/>
<input v-else v-model="form[f.key]" class="master-native-input" :type="f.kind==='datetime'?'datetime-local':'text'" :required="f.required" :maxlength="f.max??undefined" :disabled="!create&&f.key==='materialCode'"/>
</label></section><label v-if="!create&&!readonly" class="form-full-row"><span class="form-field-label">变更原因 <span class="required-mark">*</span></span><textarea v-model="reason" class="master-native-input" required maxlength="1000"/></label></form></a-card>
<template v-if="record"><MaterialConversions class="material-conversions-card" v-if="!readonly||activeTab==='basic'" :material-id="record.id" :base-unit-id="String(record.baseUnitId)" :writable="!readonly"/><MaterialSuppliers v-if="!readonly||activeTab==='suppliers'" :material-id="record.id" :material-name="String(record.materialName??record.materialCode)" :writable="!readonly&&record.status!=='INACTIVE'&&auth.can('master:material:update')" @updated="load"/>
<a-card v-if="readonly&&activeTab==='usage'" title="使用范围" class="form-section"><a-empty description="当前记录未提供使用范围信息"/></a-card><a-card title="审计与停用" class="form-section material-audit-card"><div class="material-audit-controls"><a-button v-if="auth.can('audit:view')" @click="router.push({path:'/audit',query:{objectId:record.id,objectType:'Material'}})">查看审计记录</a-button><template v-if="readonly&&record.status==='ACTIVE'&&auth.can('master:material:disable')"><label class="form-inline-row"><span class="form-field-label">停用原因</span><textarea v-model="reason" class="master-native-input" maxlength="1000"/></label><a-button danger :disabled="busy||conflict" @click="save(true)">停用物料</a-button></template></div></a-card></template>
<p v-if="create" class="muted">保存基本信息后可维护单位换算、多个供应商和首选供应商。</p><footer v-if="!readonly" class="material-form-actions"><a-button @click="router.push('/master/materials')">取消</a-button><a-button type="primary" html-type="submit" form="material-basic-form" :loading="busy" :disabled="conflict">保存基本信息</a-button></footer></main></template>

<style>
.material-detail-page .material-object-summary { display:flex; align-items:center; flex-wrap:wrap; gap:12px; }
.material-detail-page .material-object-summary strong { color:var(--mes-ui-text); font-size:17px; font-weight:600; }
.material-detail-page .material-object-summary .ant-tag { margin:0; }
#app .material-detail-page .material-basic-card .master-form { max-width:1100px; grid-template-columns:repeat(2,minmax(0,1fr)); gap:20px 0; }
.material-detail-page .material-field-section { min-width:0; display:grid; align-content:start; gap:12px; }
.material-detail-page .material-group-0 { grid-column:1; grid-row:1; padding-right:32px; }
.material-detail-page .material-group-1 { grid-column:1; grid-row:2; padding-right:32px; padding-top:16px; border-top:1px solid var(--mes-ui-border); }
.material-detail-page .material-group-2 { grid-column:2; grid-row:1; padding-left:32px; border-left:1px solid var(--mes-ui-border); }
.material-detail-page .material-group-3 { grid-column:2; grid-row:2; padding-left:32px; padding-top:16px; border-left:1px solid var(--mes-ui-border); border-top:1px solid var(--mes-ui-border); }
#app .material-detail-page .material-basic-card .material-section-title { margin:0 0 8px; color:var(--mes-ui-text); font-size:16px; font-weight:600; line-height:24px; border-left:3px solid var(--mes-ui-primary); padding-left:12px; }
#app .material-detail-page .material-field-section>label,
#app .material-detail-page .material-basic-card .master-form>label { display:grid; grid-template-columns:130px minmax(0,1fr); gap:12px; min-width:0; min-height:34px; align-items:center; font-size:14px; color:var(--mes-ui-secondary); }
#app .material-detail-page .material-basic-card .master-read-value { padding:0; line-height:22px; overflow-wrap:anywhere; }
#app .material-detail-page[data-ui-template="T3"] .material-basic-card .master-form { max-width:none; }
#app .material-detail-page[data-ui-template="T3"] .material-field-section { gap:8px; }
#app .material-detail-page[data-ui-template="T3"] .material-field-section .form-field-label { line-height:22px; }
#app .material-detail-page[data-ui-template="T3"] .material-field-section>label { min-height:24px; }
#app .material-detail-page .material-quality-only .material-field-section { grid-column:1/-1; grid-row:auto; padding:0; border:0; }
#app .material-detail-page .material-field-section textarea { min-height:100px; }
#app .material-detail-page .material-field-section input[type="checkbox"] { width:16px; height:16px; margin:0; justify-self:start; }
#app .material-detail-page .material-conversions-card .ant-empty { margin:8px 0; display:flex; align-items:center; justify-content:center; gap:12px; }
#app .material-detail-page .material-conversions-card .ant-empty-image { height:32px; margin:0; }
.material-detail-page .material-audit-controls { display:flex; align-items:center; flex-wrap:wrap; gap:24px; }
#app .material-detail-page .material-audit-controls .form-inline-row { flex:1; margin:0; min-width:300px; }
.material-detail-page .material-form-actions { position:sticky; bottom:0; z-index:10; display:flex; justify-content:flex-end; align-items:center; gap:12px; padding:12px 20px; background:var(--mes-ui-surface); border:1px solid var(--mes-ui-border); border-radius:8px; }
#app .material-detail-page .material-basic-card :is(input,textarea,.ant-select) { scroll-margin-bottom:88px; }
@media(max-width:900px) {
 #app .material-detail-page .material-basic-card .master-form { grid-template-columns:1fr; }
 #app .material-detail-page .material-field-section { grid-column:1; grid-row:auto; padding:0; border:0; }
 #app .material-detail-page .material-field-section+.material-field-section { border-top:1px solid var(--mes-ui-border); padding-top:16px; }
 #app .material-detail-page .material-field-section>label,
 #app .material-detail-page .material-basic-card .master-form>label { grid-template-columns:95px minmax(0,1fr); }
}
</style>
