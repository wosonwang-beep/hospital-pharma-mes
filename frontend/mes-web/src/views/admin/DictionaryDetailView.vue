<script setup lang="ts">
import {computed,reactive,ref,watch} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,errorMessage,idempotencyKey} from '../../api/http'
import {type DictionaryType,type DictionaryItem,type DictionaryTypePayload,kindLabel,structureLabel,statusLabel} from './dictionaryModel'
const route=useRoute(),router=useRouter()
const mode=computed(()=>String(route.meta.mode??'view'))
const creating=computed(()=>mode.value==='create'),readonly=computed(()=>mode.value==='view')
const record=ref<DictionaryType|null>(null),items=ref<DictionaryItem[]>([]),error=ref(''),busy=ref(false),notice=ref('')
const form=reactive<DictionaryTypePayload>({code:'',name:'',kind:'BUSINESS',structure:'FLAT',description:null,sortNo:10,status:'ACTIVE'})
function back(){void router.push({path:'/admin/dictionaries',query:route.query})}
async function load(){
 error.value='';record.value=null;items.value=[]
 Object.assign(form,{code:'',name:'',kind:'BUSINESS',structure:'FLAT',description:null,sortNo:10,status:'ACTIVE'})
 if(creating.value)return
 try {
  record.value=await api<DictionaryType>({url:`/dictionaries/${route.params.id}`})
  Object.assign(form,{code:record.value.code,name:record.value.name,kind:record.value.kind,structure:record.value.structure,description:record.value.description,sortNo:record.value.sortNo,status:record.value.status})
  if(readonly.value)items.value=await api<DictionaryItem[]>({url:`/dictionaries/${route.params.id}/items`})
 }catch(e){error.value=errorMessage(e)}
}
async function save(){
 if(busy.value)return
 error.value=''
 if(!form.code.trim()||!form.name.trim()){error.value='请填写字典编码和字典名称';return}
 busy.value=true
 try{
  const result=await api<DictionaryType>({method:creating.value?'POST':'PUT',
   url:creating.value?'/dictionaries':`/dictionaries/${record.value?.id}`,
   headers:{'Idempotency-Key':idempotencyKey(),...(record.value?{'If-Match':`"${record.value.version}"`}:{})},
   data:{...form,code:form.code.trim(),name:form.name.trim()}})
  notice.value='字典已保存'
  await router.push({path:`/admin/dictionaries/${result.id}`,query:route.query})
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
watch(()=>route.fullPath,load,{immediate:true})
</script>
<template>
<main :data-ui-template="readonly?'T3':'T2'" class="admin-page dictionary-detail">
 <header class="admin-page-header">
  <div><a-button type="link" @click="back">← 返回数据字典</a-button><h1>{{creating?'新增数据字典':readonly?'数据字典详情':'编辑数据字典'}}</h1></div>
  <a-button v-if="readonly&&record" type="primary" @click="router.push({path:`/admin/dictionaries/${record.id}/edit`,query:route.query})">编辑</a-button>
 </header>
 <a-alert v-if="error" type="error" :message="error" show-icon/><a-alert v-if="notice" type="success" :message="notice" show-icon/>
 <a-card title="字典基本信息" class="form-section">
  <form class="master-form dict-form" @submit.prevent="save">
   <label><span class="form-field-label">字典 CODE <i>*</i></span>
    <span v-if="readonly" class="master-read-value">{{form.code}}</span>
    <a-input v-else v-model:value="form.code" :disabled="!creating" placeholder="例如 EQUIPMENT_TYPE" :maxlength="64"/>
   </label>
   <label><span class="form-field-label">字典名称 <i>*</i></span>
    <span v-if="readonly" class="master-read-value">{{form.name}}</span>
    <a-input v-else v-model:value="form.name" :maxlength="100" placeholder="请输入名称"/>
   </label>
   <label><span class="form-field-label">字典类型</span>
    <span v-if="readonly" class="master-read-value">{{kindLabel[form.kind]}}</span>
    <a-select v-else v-model:value="form.kind" :disabled="!creating" :options="[{value:'SYSTEM',label:'系统级'},{value:'BUSINESS',label:'业务级'}]"/>
   </label>
   <label><span class="form-field-label">字典结构</span>
    <span v-if="readonly" class="master-read-value">{{structureLabel[form.structure]}}</span>
    <a-select v-else v-model:value="form.structure" :options="[{value:'FLAT',label:'平级结构'},{value:'TREE',label:'树形结构'}]"/>
   </label>
   <label><span class="form-field-label">显示排序</span>
    <span v-if="readonly" class="master-read-value">{{form.sortNo}}</span>
    <a-input-number v-else v-model:value="form.sortNo" :min="0" :max="999999" style="width:100%"/>
   </label>
   <label><span class="form-field-label">状态</span>
    <span v-if="readonly" class="master-read-value">{{statusLabel[form.status]}}</span>
    <a-select v-else v-model:value="form.status" :options="[{value:'ACTIVE',label:'启用'},{value:'INACTIVE',label:'停用'}]"/>
   </label>
   <label class="form-full-row"><span class="form-field-label">字典描述</span>
    <span v-if="readonly" class="master-read-value">{{form.description||'—'}}</span>
    <a-textarea v-else v-model:value="form.description" :rows="3" :maxlength="500"/>
   </label>
   <a-space v-if="!readonly"><a-button @click="back">取消</a-button><a-button type="primary" html-type="submit" :loading="busy">保存字典</a-button></a-space>
  </form>
 </a-card>
 <a-alert v-if="!readonly" type="info" message="字典和选项编码被业务引用后不可随意变更；停用不会删除历史业务数据。" show-icon/>
 <a-card v-if="readonly" title="字典项" class="result-card">
  <a-table :data-source="items" row-key="id" size="small" :pagination="false">
   <a-table-column title="选项编码" data-index="code"/><a-table-column title="名称" data-index="label"/>
   <a-table-column title="描述" data-index="description"/>
   <a-table-column title="状态"><template #default="{record:item}">{{statusLabel[item.status as keyof typeof statusLabel]}}</template></a-table-column>
  </a-table>
  <div class="dict-footer"><a-button @click="back">返回列表管理字典项</a-button></div>
 </a-card>
</main>
</template>
<style scoped>
.dictionary-detail .dict-form{max-width:1020px}
.dictionary-detail .dict-form>label{grid-template-columns:130px minmax(0,1fr);align-items:center}
.dictionary-detail .dict-form>label .form-field-label{text-align:right;padding-right:0;white-space:nowrap}
.dictionary-detail i{color:#e15b62;font-style:normal}
.dictionary-detail .dict-form>.ant-space{justify-content:flex-end;padding-left:0}
.dict-footer{margin-top:12px;text-align:right}
</style>
