<script setup lang="ts">
import {computed,onBeforeUnmount,reactive,ref,watch} from 'vue'
import {useRoute,useRouter,onBeforeRouteLeave} from 'vue-router'
import {api,errorMessage,idempotencyKey} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import DictionarySelect from '../../components/DictionarySelect.vue'
import {type Department,type DepartmentForm,type DepartmentOrg,type DepartmentPerson,departmentStatus} from './departmentModel'

const route=useRoute(),router=useRouter(),auth=useAuthStore()
const mode=computed(()=>String(route.meta.mode||'view'))
const create=computed(()=>mode.value==='create'),edit=computed(()=>mode.value==='edit'),readonly=computed(()=>mode.value==='view')
const value=ref<Department|null>(null),tree=ref<Department[]>([]),organizations=ref<DepartmentOrg[]>([]),people=ref<DepartmentPerson[]>([])
const busy=ref(false),error=ref(''),notice=ref(''),baseline=ref('')
const form=reactive<DepartmentForm>({code:'',name:'',organizationId:'',parentId:null,leaderUserId:null,phone:null,description:null,sortNo:10,status:'ACTIVE'})
const orgOptions=computed(()=>organizations.value.map(x=>({value:x.id,label:x.name+'（'+x.code+'）'})))
const personOptions=computed(()=>people.value.map(x=>({value:x.id,label:x.name+'（'+x.username+'）'})))
const potentialParent=computed(()=>tree.value.filter(x=>x.organizationId===form.organizationId&&x.status==='ACTIVE'&&x.id!==value.value?.id&&!isDescendant(x.id,value.value?.id)))
const state=()=>JSON.stringify(form)

function back(){void router.push({path:'/admin/departments',query:route.query})}
function isDescendant(id:string,ancestor:string|undefined){
 if(!ancestor)return false
 const seen=new Set<string>();let current=tree.value.find(x=>x.id===id)
 while(current?.parentId&&!seen.has(current.id)){if(current.parentId===ancestor)return true;seen.add(current.id);current=tree.value.find(x=>x.id===current!.parentId)}
 return false
}
async function loadReferences(){
 const [branches,orgs,users]=await Promise.all([
  api<Department[]>({url:'/departments/tree'}),
  api<DepartmentOrg[]>({url:'/department-organizations'}),
  api<DepartmentPerson[]>({url:'/department-users'})
 ])
 tree.value=branches;organizations.value=orgs;people.value=users
}
async function searchPeople(term:string){try{people.value=await api<DepartmentPerson[]>({url:'/department-users',params:{keyword:term}})}catch{/* keep current options */}}
async function load(){
 busy.value=true;error.value='';notice.value=''
 try{
  await loadReferences()
  if(create.value){
   value.value=null
   Object.assign(form,{code:'',name:'',organizationId:'',parentId:null,leaderUserId:null,phone:null,description:null,sortNo:10,status:'ACTIVE'})
  }else{
   value.value=await api<Department>({url:'/departments/'+route.params.id})
   Object.assign(form,{code:value.value.code,name:value.value.name,organizationId:value.value.organizationId,parentId:value.value.parentId,leaderUserId:value.value.leaderUserId,phone:value.value.phone,description:value.value.description,sortNo:value.value.sortNo,status:value.value.status})
  }
  baseline.value=state()
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
async function save(){
 if(busy.value)return
 error.value='';notice.value=''
 if(!form.code.trim()||!form.name.trim()||!form.organizationId){error.value='请填写部门编码、名称并选择所属组织';return}
 busy.value=true
 try{
  const current=value.value
  const result=await api<Department>({
   url:create.value?'/departments':'/departments/'+current!.id,
   method:create.value?'POST':'PUT',
   headers:{'Idempotency-Key':idempotencyKey(),...(current?{'If-Match':'"'+current.version+'"'}:{})},
   data:{...form,code:form.code.trim(),name:form.name.trim()}
  })
  value.value=result
  baseline.value=state()
  notice.value=create.value?'部门已创建':'部门已保存'
  await router.replace({path:'/admin/departments/'+result.id,query:route.query})
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
onBeforeRouteLeave(()=>readonly.value||baseline.value===state()||window.confirm('离开将放弃未保存的部门信息，是否继续？'))
watch(()=>route.fullPath,load,{immediate:true})
onBeforeUnmount(()=>{people.value=[]})
</script>

<template>
<main :data-ui-template="readonly?'T3':'T2'" class="admin-page master-page department-details">
 <header class="admin-page-header">
  <div><a-button type="link" @click="back">← 返回部门列表</a-button><h1>{{create?'新增部门':edit?'编辑部门':'部门详情'}}</h1></div>
  <a-button v-if="readonly&&value&&auth.can('iam:department:update')" type="primary" @click="router.push({path:'/admin/departments/'+value.id+'/edit',query:route.query})">编辑</a-button>
 </header>
 <a-alert v-if="error" type="error" :message="error" show-icon/>
 <a-alert v-if="notice" type="success" :message="notice" show-icon/>

 <a-card v-if="readonly&&value" class="form-section" title="部门基本信息">
  <div class="master-form">
   <label><span class="form-field-label">部门编码</span><strong class="master-read-value">{{value.code}}</strong></label>
   <label><span class="form-field-label">部门名称</span><strong class="master-read-value">{{value.name}}</strong></label>
   <label><span class="form-field-label">上级部门</span><strong class="master-read-value">{{value.parentName||'—'}}</strong></label>
   <label><span class="form-field-label">所属组织</span><strong class="master-read-value">{{value.organizationName}}</strong></label>
   <label><span class="form-field-label">负责人</span><strong class="master-read-value">{{value.leaderName||'—'}}</strong></label>
   <label><span class="form-field-label">联系电话</span><strong class="master-read-value">{{value.phone||'—'}}</strong></label>
   <label><span class="form-field-label">状态</span><span><a-tag :color="value.status==='ACTIVE'?'success':'default'">{{departmentStatus[value.status]}}</a-tag></span></label>
   <label><span class="form-field-label">显示排序</span><strong class="master-read-value">{{value.sortNo}}</strong></label>
   <label class="form-full-row"><span class="form-field-label">备注</span><strong class="master-read-value">{{value.description||'—'}}</strong></label>
  </div>
 </a-card>

 <template v-else>
  <a-card class="form-section" title="部门基本信息">
   <form class="master-form" @submit.prevent="save">
    <label><span class="form-field-label">部门编码 *</span><a-input v-model:value="form.code" :disabled="edit" :maxlength="64" placeholder="请输入部门编码"/></label>
    <label><span class="form-field-label">部门名称 *</span><a-input v-model:value="form.name" :maxlength="120" placeholder="请输入部门名称"/></label>
    <label><span class="form-field-label">上级部门</span><a-select v-model:value="form.parentId" show-search allow-clear option-filter-prop="label" :options="[{value:null,label:'无上级部门'},...potentialParent.map(x=>({value:x.id,label:x.name}))]"/></label>
    <label><span class="form-field-label">所属组织 *</span><a-select v-model:value="form.organizationId" show-search option-filter-prop="label" :options="orgOptions" @change="()=>form.parentId=null"/></label>
    <label><span class="form-field-label">负责人</span><a-select v-model:value="form.leaderUserId" show-search allow-clear option-filter-prop="label" :options="personOptions" :filter-option="false" @search="searchPeople"/></label>
    <label><span class="form-field-label">联系电话</span><a-input v-model:value="form.phone" :maxlength="40"/></label>
    <label><span class="form-field-label">状态</span><DictionarySelect :model-value="form.status" dict-code="DEPARTMENT_STATUS" required @update:model-value="form.status=($event??'ACTIVE') as DepartmentForm['status']"/></label>
    <label><span class="form-field-label">显示排序</span><a-input-number v-model:value="form.sortNo" :min="0" :max="999999" style="width:100%"/></label>
    <label class="form-full-row"><span class="form-field-label">备注</span><a-textarea v-model:value="form.description" :rows="3" :maxlength="500"/></label>
    <a-space><a-button @click="back">取消</a-button><a-button type="primary" html-type="submit" :loading="busy">{{create?'创建部门':'保存部门'}}</a-button></a-space>
   </form>
  </a-card>
 </template>
</main>
</template>
