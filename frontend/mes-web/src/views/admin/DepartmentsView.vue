<script setup lang="ts">
import {computed,onMounted,reactive,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,errorMessage,idempotencyKey,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {type Department,type DepartmentForm,type DepartmentOrg,type DepartmentPerson,departmentStatus,departmentTree} from './departmentModel'
const route=useRoute(),router=useRouter(),auth=useAuthStore()
const rows=ref<Department[]>([]),total=ref(0),tree=ref<Department[]>([]),organizations=ref<DepartmentOrg[]>([]),people=ref<DepartmentPerson[]>([])
const busy=ref(false),saving=ref(false),error=ref(''),formError=ref(''),notice=ref('')
const modal=ref(false),editing=ref<Department|null>(null),selected=ref<string|null>(null),page=ref(1)
const filters=reactive({code:'',name:'',status:''})
const form=reactive<DepartmentForm>({code:'',name:'',organizationId:'',parentId:null,leaderUserId:null,phone:null,description:null,sortNo:10,status:'ACTIVE'})
const fullTree=computed(()=>departmentTree(tree.value))
const expandedKeys=ref<string[]>([])
const descendants=computed(()=>{
 const visited=new Set<string>(),walk=(key:string)=>{if(visited.has(key))return;visited.add(key);tree.value.filter(x=>x.parentId===key).forEach(x=>walk(x.id))}
 if(selected.value)walk(selected.value)
 return visited
})
const scopedRows=computed(()=>rows.value.filter(x=>
  (!filters.code.trim()||x.code.toLowerCase().includes(filters.code.trim().toLowerCase()))&&
  (!filters.name.trim()||x.name.includes(filters.name.trim()))&&
  (!filters.status||x.status===filters.status)&&
  (!selected.value||descendants.value.has(x.id))))
const potentialParent=computed(()=>tree.value.filter(x=>x.organizationId===form.organizationId&&x.status==='ACTIVE'&&x.id!==editing.value?.id&&!isDescendant(x.id,editing.value?.id)))
const orgOptions=computed(()=>organizations.value.map(x=>({value:x.id,label:x.name+'（'+x.code+'）'})))
const personOptions=computed(()=>people.value.map(x=>({value:x.id,label:x.name+'（'+x.username+'）'})))
const fields=[{title:'部门编码',dataIndex:'code',width:130},{title:'部门名称',dataIndex:'name',width:105},
 {title:'上级部门',dataIndex:'parentName',width:102},{title:'所属组织',dataIndex:'organizationName',width:138},
 {title:'负责人',dataIndex:'leaderName',width:90},{title:'状态',key:'status',width:72},
 {title:'排序',dataIndex:'sortNo',width:58},{title:'操作',key:'actions',width:110}]
async function load(){
 busy.value=true;error.value=''
 try{
  const branches=await api<Department[]>({url:'/departments/tree'})
  rows.value=branches;total.value=branches.length;tree.value=branches
  expandedKeys.value=branches.filter(x=>branches.some(child=>child.parentId===x.id)).map(x=>x.id)
 }catch(e){error.value=errorMessage(e)}finally{busy.value=false}
}
function reset(){Object.assign(filters,{code:'',name:'',status:''});selected.value=null;void load()}
function isDescendant(id:string,ancestor:string|undefined){
 if(!ancestor)return false
 const seen=new Set<string>();let current=tree.value.find(x=>x.id===id)
 while(current?.parentId&&!seen.has(current.id)){if(current.parentId===ancestor)return true;seen.add(current.id);current=tree.value.find(x=>x.id===current!.parentId)}
 return false
}
async function loadReferences(){
 try{
  const [orgs,users]=await Promise.all([
   api<DepartmentOrg[]>({url:'/department-organizations'}),api<DepartmentPerson[]>({url:'/department-users'})])
  organizations.value=orgs;people.value=users
 }catch(e){formError.value=errorMessage(e)}
}
async function open(item?:Department){
 editing.value=item??null
 Object.assign(form,item?{code:item.code,name:item.name,organizationId:item.organizationId,parentId:item.parentId,
  leaderUserId:item.leaderUserId,phone:item.phone,description:item.description,sortNo:item.sortNo,status:item.status}:
  {code:'',name:'',organizationId:'',parentId:null,leaderUserId:null,phone:null,description:null,sortNo:10,status:'ACTIVE'})
 formError.value='';modal.value=true
 await loadReferences()
}
async function save(){
 if(saving.value)return
 formError.value=''
 if(!form.code.trim()||!form.name.trim()||!form.organizationId){formError.value='请填写部门编码、名称并选择所属组织';return}
 saving.value=true
 try{
  await api<Department>({url:editing.value?'/departments/'+editing.value.id:'/departments',
   method:editing.value?'PUT':'POST',
   headers:{'Idempotency-Key':idempotencyKey(),...(editing.value?{'If-Match':'"'+editing.value.version+'"'}:{})},
   data:{...form,code:form.code.trim(),name:form.name.trim()}})
  modal.value=false;notice.value='部门配置已保存';await load()
 }catch(e){formError.value=errorMessage(e)}finally{saving.value=false}
}
function view(record:Department){void router.push({path:'/admin/departments/'+record.id,query:route.query})}
onMounted(load)
</script>
<template><main class="admin-page departments-page" data-ui-template="T1">
<header class="admin-page-header"><h1>部门管理</h1><a-button v-if="auth.can('iam:department:create')" type="primary" @click="open()">＋ 新增部门</a-button></header>
<a-card class="query-card" title="查询条件">
 <form class="query-form department-query" @submit.prevent="load">
  <label><span class="form-field-label">部门编码</span><a-input v-model:value="filters.code" allow-clear placeholder="请输入部门编码"/></label>
  <label><span class="form-field-label">部门名称</span><a-input v-model:value="filters.name" allow-clear placeholder="请输入部门名称"/></label>
  <label><span class="form-field-label">状态</span><a-select v-model:value="filters.status" :options="[{value:'',label:'全部状态'},{value:'ACTIVE',label:'启用'},{value:'INACTIVE',label:'停用'}]"/></label>
  <a-space class="dept-query-actions"><a-button @click="reset">重置</a-button><a-button type="primary" html-type="submit">查询</a-button></a-space>
 </form>
</a-card>
<a-alert v-if="error" type="error" show-icon :message="error"/><a-alert v-if="notice" type="success" show-icon :message="notice" closable @close="notice=''"/>
<div class="dept-layout">
 <a-card class="dept-tree-card" title="部门结构">
  <a-tree :tree-data="fullTree" :expanded-keys="expandedKeys" @expand="(keys:(string|number)[])=>expandedKeys=keys.map(String)" :selected-keys="selected?[selected]:[]" @select="(keys:(string|number)[])=>selected=keys.length?String(keys[0]):null"/>
  <a-button type="link" @click="selected=null">全部部门</a-button>
 </a-card>
 <a-card class="result-card" title="部门列表">
  <a-table :columns="fields" :data-source="scopedRows" row-key="id" :loading="busy" :pagination="{pageSize:20,showSizeChanger:false}" :scroll="{x:805}">
   <template #bodyCell="{column,record}">
    <template v-if="column.key==='status'"><a-tag :color="record.status==='ACTIVE'?'success':'default'">{{departmentStatus[record.status as Department['status']]}}</a-tag></template>
    <template v-else-if="column.key==='actions'"><a-button type="link" @click="view(record)">查看</a-button><a-button v-if="auth.can('iam:department:update')" type="link" @click="open(record)">编辑</a-button></template>
    <strong v-else-if="column.dataIndex==='name'">{{record.name}}</strong>
    <span v-else-if="column.dataIndex==='parentName'">{{record.parentName??'—'}}</span>
    <span v-else-if="column.dataIndex==='leaderName'">{{record.leaderName??'—'}}</span>
   </template>
  </a-table>
  <div class="dept-count">共 {{total}} 条部门记录</div>
 </a-card>
</div>
<a-modal v-model:open="modal" class="department-edit-modal" :title="editing?'编辑部门':'新增部门'" :width="800" :footer="null" :mask-closable="!saving">
 <a-alert v-if="formError" :message="formError" type="error" show-icon/>
 <form class="department-form" @submit.prevent="save">
  <label><span class="form-field-label">部门编码 *</span><a-input v-model:value="form.code" :disabled="!!editing" :maxlength="64" placeholder="请输入部门编码"/></label>
  <label><span class="form-field-label">部门名称 *</span><a-input v-model:value="form.name" :maxlength="120" placeholder="请输入部门名称"/></label>
  <label><span class="form-field-label">上级部门</span><a-select v-model:value="form.parentId" show-search allow-clear option-filter-prop="label" :options="[{value:null,label:'无上级部门'},...potentialParent.map(x=>({value:x.id,label:x.name}))]"/></label>
  <label><span class="form-field-label">所属组织 *</span><a-select v-model:value="form.organizationId" show-search option-filter-prop="label" :options="orgOptions" @change="()=>form.parentId=null"/></label>
  <label><span class="form-field-label">负责人</span><a-select v-model:value="form.leaderUserId" show-search allow-clear option-filter-prop="label" :options="personOptions" :filter-option="false" @search="async (term:string)=>{try{people=await api<DepartmentPerson[]>({url:'/department-users',params:{keyword:term}})}catch{}}"/></label>
  <label><span class="form-field-label">联系电话</span><a-input v-model:value="form.phone" :maxlength="40"/></label>
  <label><span class="form-field-label">状态</span><a-select v-model:value="form.status" :options="[{value:'ACTIVE',label:'启用'},{value:'INACTIVE',label:'停用'}]"/></label>
  <label><span class="form-field-label">显示排序</span><a-input-number v-model:value="form.sortNo" :min="0" :max="999999" style="width:100%"/></label>
  <label class="dept-wide"><span class="form-field-label">备注</span><a-textarea v-model:value="form.description" :rows="3" :maxlength="500"/></label>
  <div class="dept-buttons"><a-button @click="modal=false">取消</a-button><a-button type="primary" html-type="submit" :loading="saving">保存部门</a-button></div>
 </form>
</a-modal>
</main></template>
<style scoped>
.department-query{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px 16px}
.department-query>label{display:grid;grid-template-columns:130px minmax(0,1fr);align-items:center;gap:12px;min-width:0}
.department-query .form-field-label{text-align:right;white-space:nowrap}
.dept-query-actions{grid-column:1/-1;justify-self:end}
.dept-layout{display:grid;grid-template-columns:250px minmax(0,1fr);gap:16px;align-items:start}
.dept-tree-card{min-height:365px}
.dept-count{text-align:right;font-size:12px;color:#8798aa;padding:10px}
.department-form{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:17px 20px;padding-top:15px}
.department-form>label{display:grid;grid-template-columns:130px minmax(0,1fr);align-items:center;gap:12px;min-width:0}
.department-form>label>.form-field-label{text-align:right;white-space:nowrap}
.department-form>.dept-wide{grid-column:1/-1}
.dept-buttons{grid-column:1/-1;display:flex;justify-content:flex-end;gap:10px;padding-top:12px;border-top:1px solid #e8eef5}
@media(max-width:1250px){.dept-layout{grid-template-columns:215px minmax(0,1fr)}}
:global(.department-edit-modal .ant-modal-content),:global(.department-edit-modal .ant-modal-body),:global(.department-edit-modal .ant-modal-header){background:#fff!important;backdrop-filter:none!important}
</style>
