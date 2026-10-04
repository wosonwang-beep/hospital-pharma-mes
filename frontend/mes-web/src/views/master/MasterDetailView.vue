<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import axios from 'axios'
import { api,errorMessage,idempotencyKey } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import { resource,statusLabel,actionLabel,commandBody,type MasterRecord } from '../../master/resources'
const route=useRoute(),router=useRouter(),auth=useAuthStore()
const def=computed(()=>resource(String(route.meta.resource))),create=computed(()=>route.meta.mode==='create'),readonly=computed(()=>route.meta.mode==='view')
const form=reactive<Record<string,string|number|null>>({}),record=ref<MasterRecord|null>(null),reason=ref(''),error=ref(''),busy=ref(false),conflict=ref(false)
const auditTypes:Record<string,string>={organizations:'Organization',units:'Unit','unit-conversions':'UnitConversion',equipment:'Equipment',qualifications:'Qualification',suppliers:'Supplier'}
let pendingKey=idempotencyKey(),pendingBody=''
async function load(){error.value='';conflict.value=false;record.value=null;for(const k of Object.keys(form))delete form[k];reason.value='';try{if(!create.value)record.value=await api<MasterRecord>({url:`/${def.value.key}/${route.params.id}`});for(const f of def.value.fields){const value=record.value?.[f.key];form[f.key]=typeof value==='string'||typeof value==='number'?value:f.kind==='number'?0:null}}catch(e){error.value=errorMessage(e)}}
async function save(action='UPDATE'){busy.value=true;error.value='';try{
 const body=commandBody(def.value,form,!create.value,action,reason.value)
 const encoded=JSON.stringify({body,version:record.value?.versionNo});if(encoded!==pendingBody){pendingKey=idempotencyKey();pendingBody=encoded}
 const updated=await api<MasterRecord>({url:`/${def.value.key}${create.value?'':`/${route.params.id}`}`,method:create.value?'POST':'PUT',data:body,headers:{'Idempotency-Key':pendingKey,...(record.value?{'If-Match':`"${record.value.versionNo}"`}:{})}})
 record.value=updated;pendingBody='';await router.push({path:`/master/${def.value.key}/${updated.id}`,query:route.query})
}catch(e){error.value=e instanceof Error&&!axios.isAxiosError(e)?e.message:errorMessage(e);conflict.value=axios.isAxiosError(e)&&e.response?.status===409;await Promise.resolve();document.querySelector<HTMLElement>('.master-page [aria-invalid="true"]')?.focus()}finally{busy.value=false}}
function back(){void router.push({path:`/master/${def.value.key}`,query:route.query})}
watch(()=>route.fullPath,load,{immediate:true})
</script>
<template><main class="admin-page master-page"><header class="admin-page-header"><div><a-button type="link" @click="back">← 返回{{def.title}}列表</a-button><h1>{{create?'新增':readonly?'查看':'编辑'}}{{def.title}}</h1><p v-if="record">ID {{record.id}} · 版本 {{record.versionNo}} · {{statusLabel[String(record.status??record.qualificationStatus??'')]??record.status??record.qualificationStatus}}</p></div><a-button v-if="readonly&&record?.allowedActions.includes('UPDATE')&&auth.can(`master:${def.permission}:update`)" type="primary" @click="router.push({path:`/master/${def.key}/${record?.id}/edit`,query:route.query})">编辑</a-button></header>
<a-alert v-if="error" type="error" :message="error" show-icon/><a-alert v-if="conflict" type="warning" message="记录发生冲突，请重新加载最新版本后再保存。"><template #action><a-button @click="load">重新加载</a-button></template></a-alert>
<a-card title="基本信息" class="form-section"><form class="master-form" @submit.prevent="save()"><label v-for="f in def.fields" :key="f.key"><span class="form-field-label">{{f.label}}<span v-if="f.required" class="required-mark"> *</span></span>
<span v-if="readonly" class="master-read-value">{{form[f.key]===null?'—':statusLabel[String(form[f.key])]??form[f.key]}}</span>
<select v-else-if="f.kind==='select'" v-model="form[f.key]" class="master-native-input" :required="f.required" :disabled="!create&&f.immutable"><option :value="null">请选择</option><option v-for="v in f.choices" :key="v" :value="v">{{statusLabel[v]??v}}</option></select>
<input v-else-if="f.kind==='number'" v-model.number="form[f.key]" class="master-native-input" type="number" min="0" max="12" step="1" :required="f.required"/>
<input v-else v-model="form[f.key]" class="master-native-input" :type="f.kind==='date'?'date':'text'" :required="f.required" :maxlength="f.max" :disabled="!create&&f.immutable"/>
</label><label v-if="!create&&!readonly" class="form-full-row"><span class="form-field-label">变更原因 <span class="required-mark">*</span></span><textarea v-model="reason" class="master-native-input" required maxlength="1000"/></label>
<a-space v-if="!readonly"><a-button type="primary" html-type="submit" :loading="busy" :disabled="conflict">保存</a-button><a-button @click="back">取消</a-button><a-button v-for="action in record?.allowedActions.filter(v=>v!=='UPDATE')??[]" :key="action" :disabled="busy||conflict" @click="save(action)">{{actionLabel[action]??action}}</a-button></a-space></form></a-card>
<a-card v-if="readonly&&record" title="版本与审计" class="form-section"><p>组织 {{record.orgId}} · 更新时间 {{record.updatedAt}} · 版本 {{record.versionNo}}</p><a-button v-if="auth.can('audit:view')" @click="router.push({path:'/audit',query:{objectId:record.id,objectType:auditTypes[def.key]}})">查看审计记录</a-button></a-card></main></template>
