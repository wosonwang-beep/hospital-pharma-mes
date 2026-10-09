<script setup lang="ts">
import {onMounted,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,errorMessage,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import ProcessLookup from '../process/ProcessLookup.vue'
import {prescriptionStatus,type ProductionPrescription} from './prescriptionModel'

const route=useRoute(),router=useRouter(),auth=useAuthStore()
const keyword=ref(String(route.query.keyword??'')),status=ref(String(route.query.status??'')),productId=ref(String(route.query.productId??''))
const data=ref<Page<ProductionPrescription>>({items:[],total:0,page:0,size:20})
const busy=ref(false),error=ref('')
async function load(page=0){
 busy.value=true;error.value=''
 try{
  const params={keyword:keyword.value||undefined,status:status.value||undefined,productId:productId.value||undefined,page,size:20}
  await router.replace({query:Object.fromEntries(Object.entries(params).filter(([,v])=>v!==undefined).map(([k,v])=>[k,String(v)]))})
  data.value=await api({url:'/production-prescriptions',params})
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
function reset(){keyword.value='';status.value='';productId.value='';void load()}
function open(id?:string,edit=false){
 const path=id?'/production/prescriptions/'+id+(edit?'/edit':''):'/production/prescriptions/create'
 void router.push({path,query:route.query})
}
onMounted(()=>load(Number(route.query.page??0)))
</script>

<template>
<main data-ui-template="T1" class="admin-page master-page t1-query-list prescription-list">
 <header class="admin-page-header">
  <div><h1>生产处方</h1><p>独立维护产品配料、标准批量及所采用的生产工艺。</p></div>
  <a-button v-if="auth.can('production:prescription:create')" type="primary" @click="open()">＋ 新建生产处方</a-button>
 </header>
 <a-card title="查询条件" class="query-card">
  <form class="query-form prescription-query" @submit.prevent="load()">
   <label><span class="form-field-label">处方名称 / 编码</span><a-input v-model:value="keyword" allow-clear placeholder="请输入处方名称或编码"/></label>
   <label><span class="form-field-label">产品</span><ProcessLookup v-model="productId" resource="products" label="产品" allow-clear/></label>
   <label><span class="form-field-label">状态</span><a-select v-model:value="status" allow-clear placeholder="全部状态" :options="Object.entries(prescriptionStatus).map(([value,label])=>({value,label}))"/></label>
   <a-space><a-button @click="reset">重置</a-button><a-button type="primary" html-type="submit">查询</a-button></a-space>
  </form>
 </a-card>
 <a-alert v-if="error" type="error" :message="error" show-icon/>
 <a-card title="处方列表" class="result-card">
  <a-table row-key="id" :loading="busy" :data-source="data.items" :pagination="false" :scroll="{x:980}">
   <a-table-column title="处方编码" data-index="prescriptionCode" width="180"/>
   <a-table-column title="处方名称" data-index="prescriptionName" width="220"/>
   <a-table-column title="产品" data-index="productName" width="180"/>
   <a-table-column title="生产工艺" data-index="processCode" width="190"/>
   <a-table-column title="标准批量" width="140"><template #default="{record}">{{record.batchBasisQty}} <span class="muted">#{{record.unitId}}</span></template></a-table-column>
   <a-table-column title="状态" width="100"><template #default="{record}"><a-tag :color="record.status==='ACTIVE'?'green':record.status==='DRAFT'?'orange':'default'">{{prescriptionStatus[record.status]??record.status}}</a-tag></template></a-table-column>
   <a-table-column title="操作" width="150" fixed="right"><template #default="{record}"><a-button type="link" @click="open(record.id)">查看</a-button><a-button v-if="record.status==='DRAFT'&&auth.can('production:prescription:update')" type="link" @click="open(record.id,true)">编辑</a-button></template></a-table-column>
  </a-table>
  <div class="table-footer"><span>共 {{data.total}} 条</span><a-pagination :current="data.page+1" :page-size="data.size" :total="data.total" :show-size-changer="false" @change="(p:number)=>load(p-1)"/></div>
 </a-card>
</main>
</template>
<style scoped>
.prescription-query label{min-width:300px}
.prescription-query :deep(.ant-input),.prescription-query :deep(.ant-select),.prescription-query :deep(.process-lookup){width:205px}
.muted{color:var(--mes-ui-secondary);font-size:12px}
</style>
