<script setup lang="ts">
import {computed,onMounted,ref} from 'vue'
import {useRouter} from 'vue-router'
import {api,errorMessage,type Page} from '../../api/http'
import {bookApi} from '../../api/ebrBook'
import {useAuthStore} from '../../stores/auth'
import type {Package} from '../process/model'
import type {BookTemplate} from './bookModel'

const router=useRouter(),auth=useAuthStore()
const rows=ref<BookTemplate[]>([]),processes=ref<Package[]>([])
const keyword=ref(''),processId=ref(''),status=ref('')
const busy=ref(false),error=ref(''),page=ref(1),size=10
const statusLabels:Record<string,string>={DRAFT:'草稿',PUBLISHED:'已发布',INACTIVE:'已停用'}
const filtered=computed(()=>{
 const q=keyword.value.trim().toLowerCase()
 return rows.value.filter(row=>{
  const name=row.definition.templateName||row.definition.varietyName||row.templateCode
  const pid=row.definition.processPackageId||row.definition.mappings?.[0]?.processPackageId||''
  return (!q||[name,row.templateCode].some(v=>String(v||'').toLowerCase().includes(q)))&&(!processId.value||pid===processId.value)&&(!status.value||row.status===status.value)
 })
})
const paged=computed(()=>filtered.value.slice((page.value-1)*size,page.value*size))
function processName(row:BookTemplate){
 const id=row.definition.processPackageId||row.definition.mappings?.[0]?.processPackageId
 if(!id)return '历史工艺版本'
 const p=processes.value.find(x=>x.id===id)
 return p?.packageCode||('#'+id)
}
function templateName(row:BookTemplate){return row.definition.templateName||row.definition.varietyName||'未命名模板'}
function open(id?:string,mode:'view'|'edit'='edit'){
 if(!id){void router.push('/ebr/book-templates/create');return}
 void router.push('/ebr/book-templates/'+id+(mode==='edit'?'/edit':''))
}
function reset(){keyword.value='';processId.value='';status.value='';page.value=1}
async function load(){
 busy.value=true;error.value=''
 try{
  const [templates,processPage]=await Promise.all([
   bookApi.templates(),
   api<Page<Package>>({url:'/process-packages',params:{size:100}})
  ])
  rows.value=templates
  processes.value=processPage.items
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
onMounted(load)
</script>

<template>
<main data-ui-template="T1" class="admin-page master-page t1-query-list ebr-template-list">
 <header class="admin-page-header">
  <div><h1>电子批记录模板</h1><p>独立管理完整电子批记录的生产工序、业务流程和表单记录组织。</p></div>
  <a-button v-if="auth.can('ebr:template:create')" type="primary" @click="open()">＋ 新建模板</a-button>
 </header>

 <a-alert v-if="error" type="error" :message="error" show-icon/>
 <a-card title="查询条件" class="query-card">
  <form class="query-form ebr-query" @submit.prevent="page=1">
   <label><span class="form-field-label">模板名称</span><a-input v-model:value="keyword" allow-clear placeholder="请输入模板名称或编码"/></label>
   <label><span class="form-field-label">关联工艺</span><a-select v-model:value="processId" allow-clear show-search option-filter-prop="label" placeholder="请选择" :options="processes.map(p=>({value:p.id,label:p.packageCode}))"/></label>
   <label><span class="form-field-label">状态</span><a-select v-model:value="status" allow-clear placeholder="请选择" :options="Object.entries(statusLabels).map(([value,label])=>({value,label}))"/></label>
   <a-space><a-button @click="reset">重置</a-button><a-button type="primary" html-type="submit">查询</a-button></a-space>
  </form>
 </a-card>

 <a-card title="模板列表" class="result-card">
  <a-table :loading="busy" :data-source="paged" row-key="id" :pagination="false" :scroll="{x:1000}">
   <a-table-column title="序号" width="70"><template #default="{index}">{{(page-1)*size+index+1}}</template></a-table-column>
   <a-table-column title="模板名称" width="210"><template #default="{record}"><a-button type="link" class="name-link" @click="open(record.id,'view')">{{templateName(record)}}</a-button></template></a-table-column>
   <a-table-column title="关联工艺" width="180"><template #default="{record}">{{processName(record)}}</template></a-table-column>
   <a-table-column title="适用产品范围" width="150"><template #default="{record}">{{record.definition.processPackageId?'由生产处方关联':'历史产品映射'}}</template></a-table-column>
   <a-table-column title="版本" data-index="revision" width="90"/>
   <a-table-column title="状态" width="105"><template #default="{record}"><a-tag :color="record.status==='PUBLISHED'?'green':record.status==='DRAFT'?'orange':'default'">{{statusLabels[record.status]||record.status}}</a-tag></template></a-table-column>
   <a-table-column title="创建人" data-index="createdBy" width="100"/>
   <a-table-column title="操作" width="150" fixed="right"><template #default="{record}"><a-space :size="2"><a-button type="link" @click="open(record.id,'view')">查看</a-button><a-button v-if="auth.can('ebr:template:create')" type="link" @click="open(record.id,'edit')">编辑</a-button></a-space></template></a-table-column>
   <template #emptyText><a-empty description="暂无电子批记录模板"/></template>
  </a-table>
  <div class="table-footer"><span>共 {{filtered.length}} 条</span><a-pagination v-model:current="page" :page-size="size" :total="filtered.length" :show-size-changer="false"/></div>
 </a-card>
</main>
</template>

<style scoped>
.ebr-template-list{max-width:100%;min-width:0}
.ebr-query label{min-width:280px}
.ebr-query :deep(.ant-input),.ebr-query :deep(.ant-select){width:190px}
.name-link{padding-left:0;font-weight:600}
@media(max-width:900px){.ebr-query label{min-width:0}.ebr-query :deep(.ant-input),.ebr-query :deep(.ant-select){width:min(220px,55vw)}}
</style>
