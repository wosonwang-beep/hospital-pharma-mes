<script setup lang="ts">
import {computed,reactive,ref,watch} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {useControlledRequest} from '../../api/controlled'
import './productionModel'
import {label,display,schemas} from '../quality/incomingModel'
import ProcessLookup from '../process/ProcessLookup.vue'
import IncomingReferencePicker from '../quality/IncomingReferencePicker.vue'
const route=useRoute(),router=useRouter(),auth=useAuthStore(),request=useControlledRequest(),{busy,error}=request
const rows=ref<Record<string,unknown>[]>([]),total=ref(0),page=ref(1)
const query=reactive({keyword:'',status:'',productId:'',productionOrderId:'',plannedDateFrom:'',plannedDateTo:''})
const batch=computed(()=>route.meta.resource==='batches'),title=computed(()=>batch.value?'生产批':'生产订单')
const base=computed(()=>batch.value?'/main-batches':'/production-orders'),ui=computed(()=>batch.value?'/production/batches':'/production/orders'),permission=computed(()=>batch.value?'production:batch':'production:order')
const states=computed(()=>schemas[batch.value?'MainBatch':'Order']?.properties?.status?.enum??[])
function stateLabel(value:unknown){return ({PENDING_QA:'待 QA 审核',QA_RELEASED:'已 QA 放行'} as Record<string,string>)[String(value)]??display(value)}
const columns=computed(()=>[batch.value?'batchNo':'orderNo','productId','plannedQty','unitId','plannedDate','status'].map(k=>({title:label(k),key:k,dataIndex:k})).concat([{title:'操作',key:'actions',dataIndex:'actions'}]))
async function load(){busy.value=true;request.clear();try{
 const params={...Object.fromEntries(Object.entries(query).filter(([k,v])=>v&&(batch.value||k!=='productionOrderId'))),page:page.value-1,size:20}
 const result=await api<Page<Record<string,unknown>>>({url:base.value,params});rows.value=result.items;total.value=result.total
}catch(e){await request.failure(e)}finally{busy.value=false}}
async function search(next=1){page.value=next;await router.replace({query:{...Object.fromEntries(Object.entries(query).filter(([k,v])=>v&&(batch.value||k!=='productionOrderId'))),page:String(page.value)}});await load()}
function reset(){for(const key of Object.keys(query) as (keyof typeof query)[])query[key]='';void search(1)}
function open(id?:string){void router.push({path:id?`${ui.value}/${id}`:`${ui.value}/create`,query:route.query})}
watch(base,()=>{for(const key of Object.keys(query) as (keyof typeof query)[])query[key]=String(route.query[key]??'');const saved=Number(route.query.page??1);page.value=Number.isInteger(saved)&&saved>0?saved:1;void load()},{immediate:true})
</script>
<!-- UI Template: T1 Query/List. Frozen UI-ORD-Q/UI-BAT-Q filters and independent routes. -->
<template><main class="admin-page master-page t1-query-list production-query-page">
 <header class="admin-page-header"><div><h1>{{title}}</h1><p>查询计划与状态 · 查询条件随导航保留</p></div><a-button v-if="auth.can(`${permission}:create`)" type="primary" @click="open()">新增{{title}}</a-button></header>
 <a-alert v-if="error" type="error" :message="error" show-icon/>
 <a-card title="查询条件" class="query-card"><form class="query-form" @submit.prevent="search(1)">
  <label><span class="form-field-label">{{batch?'批号 / 关键字':'订单号 / 关键字'}}</span><input v-model="query.keyword" class="master-native-input" aria-label="关键字"/></label>
  <label><span class="form-field-label">产品</span><ProcessLookup v-model="query.productId" resource="products" label="产品查询" allow-clear/></label>
  <label v-if="batch"><span class="form-field-label">生产订单</span><IncomingReferencePicker field="productionOrderId" v-model="query.productionOrderId" :context="{}"/></label>
  <label><span class="form-field-label">计划日期从</span><input v-model="query.plannedDateFrom" type="date" class="master-native-input" aria-label="计划日期从"/></label>
  <label><span class="form-field-label">计划日期至</span><input v-model="query.plannedDateTo" type="date" class="master-native-input" aria-label="计划日期至"/></label>
  <label><span class="form-field-label">状态</span><select v-model="query.status" class="master-native-input" aria-label="状态"><option value="">全部状态</option><option v-for="state in states" :key="String(state)" :value="String(state)">{{stateLabel(state)}}</option></select></label>
  <a-space><a-button @click="reset">重置</a-button><a-button type="primary" html-type="submit" :loading="busy">查询</a-button></a-space>
 </form></a-card>
 <a-card title="数据列表" class="result-card"><div class="master-toolbar"><span>共 {{total}} 条记录</span></div>
  <a-table :columns="columns" :data-source="rows" row-key="id" :loading="busy" :scroll="{x:850}" :pagination="false"><template #bodyCell="{column,record}"><a-button v-if="column.key==='actions'" type="link" @click="open(String(record.id))">查看</a-button><strong v-else-if="column.key==='batchNo'||column.key==='orderNo'" class="t1-business-id">{{display(record[column.key])}}</strong><span v-else>{{column.key==='status'?stateLabel(record[column.key]):display(record[column.key])}}</span></template></a-table>
  <div class="table-footer"><span>第 {{page}} 页</span><a-pagination :current="page" :total="total" :page-size="20" :show-size-changer="false" @change="(p:number)=>search(p)"/></div>
 </a-card>
</main></template>
<style scoped>.query-form .process-lookup{min-width:0;flex:1}.query-form :deep(.process-lookup .ant-select){width:100%}</style>
