<script setup lang="ts">
import {computed,onMounted,reactive,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,errorMessage} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import DictionarySelect from '../../components/DictionarySelect.vue'
import {type Department,departmentStatus,departmentTree} from './departmentModel'

const route=useRoute(),router=useRouter(),auth=useAuthStore()
const rows=ref<Department[]>([]),total=ref(0),tree=ref<Department[]>([])
const busy=ref(false),error=ref(''),selected=ref<string|null>(null)
const filters=reactive({code:'',name:'',status:''}),appliedFilters=reactive({code:'',name:'',status:''})
const expandedKeys=ref<string[]>([])
const fullTree=computed(()=>departmentTree(tree.value))
const descendants=computed(()=>{
 const visited=new Set<string>(),walk=(key:string)=>{if(visited.has(key))return;visited.add(key);tree.value.filter(x=>x.parentId===key).forEach(x=>walk(x.id))}
 if(selected.value)walk(selected.value)
 return visited
})
const scopedRows=computed(()=>rows.value.filter(x=>
 (!appliedFilters.code.trim()||x.code.toLowerCase().includes(appliedFilters.code.trim().toLowerCase()))&&
 (!appliedFilters.name.trim()||x.name.includes(appliedFilters.name.trim()))&&
 (!appliedFilters.status||x.status===appliedFilters.status)&&
 (!selected.value||descendants.value.has(x.id))))
const fields=[
 {title:'部门编码',dataIndex:'code',width:130},{title:'部门名称',dataIndex:'name',width:105},
 {title:'上级部门',dataIndex:'parentName',width:102},{title:'所属组织',dataIndex:'organizationName',width:138},
 {title:'负责人',dataIndex:'leaderName',width:90},{title:'状态',key:'status',width:72},
 {title:'排序',dataIndex:'sortNo',width:58},{title:'操作',key:'actions',width:110}
]
async function load(){
 busy.value=true;error.value=''
 try{
  const branches=await api<Department[]>({url:'/departments/tree'})
  rows.value=branches;total.value=branches.length;tree.value=branches
  expandedKeys.value=branches.filter(x=>branches.some(child=>child.parentId===x.id)).map(x=>x.id)
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
function search(){Object.assign(appliedFilters,filters)}
function reset(){Object.assign(filters,{code:'',name:'',status:''});Object.assign(appliedFilters,filters);selected.value=null;void load()}
function view(record:Department){void router.push({path:'/admin/departments/'+record.id,query:route.query})}
onMounted(load)
</script>

<template>
<main class="admin-page master-page t1-query-list departments-page" data-ui-template="T1">
 <header class="admin-page-header">
  <h1>部门管理</h1>
  <a-button v-if="auth.can('iam:department:create')" type="primary" @click="router.push({path:'/admin/departments/create',query:route.query})">＋ 新增部门</a-button>
 </header>
 <a-card class="query-card" title="查询条件">
  <form class="query-form department-query" @submit.prevent="search">
   <label><span class="form-field-label">部门编码</span><a-input v-model:value="filters.code" allow-clear placeholder="请输入部门编码"/></label>
   <label><span class="form-field-label">部门名称</span><a-input v-model:value="filters.name" allow-clear placeholder="请输入部门名称"/></label>
   <label><span class="form-field-label">状态</span><DictionarySelect :model-value="filters.status||null" dict-code="DEPARTMENT_STATUS" placeholder="全部状态" @update:model-value="filters.status=$event??''"/></label>
   <a-space class="dept-query-actions"><a-button @click="reset">重置</a-button><a-button type="primary" html-type="submit">查询</a-button></a-space>
  </form>
 </a-card>
 <a-alert v-if="error" type="error" show-icon :message="error"/>
 <div class="dept-layout">
  <a-card class="dept-tree-card" title="部门结构">
   <a-tree :tree-data="fullTree" :expanded-keys="expandedKeys" @expand="(keys:(string|number)[])=>expandedKeys=keys.map(String)" :selected-keys="selected?[selected]:[]" @select="(keys:(string|number)[])=>selected=keys.length?String(keys[0]):null"/>
   <a-button type="link" @click="selected=null">全部部门</a-button>
  </a-card>
  <a-card class="result-card" title="部门列表">
   <a-table :columns="fields" :data-source="scopedRows" row-key="id" :loading="busy" :pagination="false" :scroll="{x:805}">
    <template #bodyCell="{column,record}">
     <template v-if="column.key==='status'"><a-tag :color="record.status==='ACTIVE'?'success':'default'">{{departmentStatus[record.status as Department['status']]}}</a-tag></template>
     <template v-else-if="column.key==='actions'"><a-button type="link" @click="view(record)">查看</a-button><a-button v-if="auth.can('iam:department:update')" type="link" @click="router.push({path:'/admin/departments/'+record.id+'/edit',query:route.query})">编辑</a-button></template>
     <strong v-else-if="column.dataIndex==='name'">{{record.name}}</strong>
     <span v-else-if="column.dataIndex==='parentName'">{{record.parentName??'—'}}</span>
     <span v-else-if="column.dataIndex==='leaderName'">{{record.leaderName??'—'}}</span>
    </template>
   </a-table>
   <div class="table-footer"><span>共 {{scopedRows.length}} 条</span><span v-if="scopedRows.length!==total">全部 {{total}} 条</span></div>
  </a-card>
 </div>
</main>
</template>

<style scoped>
.department-query{grid-template-columns:repeat(3,minmax(0,1fr))}
.department-query>.dept-query-actions{grid-column:1/-1;justify-self:stretch;justify-content:flex-end}
.dept-layout{display:grid;grid-template-columns:250px minmax(0,1fr);gap:16px;align-items:start;min-width:0}
.dept-layout>*{min-width:0;max-width:100%}
.dept-tree-card{min-height:365px}
@media(max-width:1250px){.dept-layout{grid-template-columns:215px minmax(0,1fr)}}
@media(max-width:900px){.department-query{grid-template-columns:minmax(0,1fr)}.dept-layout{grid-template-columns:minmax(0,1fr)}.dept-tree-card{min-height:0}}
</style>
