<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,type Page} from '../../api/http'
import {useControlledRequest} from '../../api/controlled'
import {useAuthStore} from '../../stores/auth'
import {resources,label,display,operations,schemas} from './incomingModel'
import IncomingReferencePicker from './IncomingReferencePicker.vue'
const route=useRoute(),router=useRouter(),auth=useAuthStore(),request=useControlledRequest(),{error,busy}=request
const resource=computed(()=>String(route.meta.resource)),def=computed(()=>resources[resource.value]!),base=computed(()=>resource.value==='deviations'?'/deviations':`/quality/${resource.value}`)
const keyword=ref(''),status=ref(''),materialLotId=ref(''),page=ref(1),rows=ref<Record<string,unknown>[]>([]),total=ref(0)
const statusSchema:Record<string,string>={'inspection-requests':'InspectionRequest','sampling-tasks':'SamplingTask',samples:'Sample','inspection-tasks':'InspectionTask','inspection-reports':'InspectionReport',deviations:'Deviation'}
const states=computed(()=>schemas[statusSchema[resource.value]??'']?.properties?.status?.enum??[])
async function changePage(p:{current?:number}){page.value=p.current??1;await search()}
async function search(){await router.replace({query:{...(keyword.value?{keyword:keyword.value}:{}),...(status.value?{status:status.value}:{}),...(materialLotId.value?{materialLotId:materialLotId.value}:{}),page:String(page.value)}});await load()}
const columns=computed(()=>[...def.value.columns.map(key=>({title:label(key),key,dataIndex:key})),{title:'操作',key:'actions'}])
const canCreate=computed(()=>operations.some(o=>o.path===base.value&&o.method==='POST'&&auth.can(o.permission)))
async function load(){busy.value=true;try{const data=await api<Page<Record<string,unknown>>>({url:base.value,params:{page:page.value-1,size:20,keyword:keyword.value||undefined,status:status.value||undefined,materialLotId:materialLotId.value||undefined,...(resource.value==='deviations'?{investigationScope:'INCOMING_MATERIAL'}:{})}});rows.value=data.items;total.value=data.total}catch(e){await request.failure(e)}finally{busy.value=false}}
watch(resource,()=>{page.value=Math.max(1,Number(route.query.page??1));keyword.value=String(route.query.keyword??'');status.value=String(route.query.status??'');materialLotId.value=String(route.query.materialLotId??'');void load()},{immediate:true})
</script>
<template><main class="admin-page master-page"><header class="admin-page-header"><h1>{{def.title}}</h1><a-button v-if="canCreate" type="primary" @click="router.push({path:`${base}/create`,query:route.query})">新增{{def.title}}</a-button></header>
 <a-alert v-if="error" type="error" :message="error" show-icon/>
 <a-card class="form-section"><form class="master-form incoming-query" @submit.prevent="page=1;search()"><label><span class="form-field-label">关键字</span><input v-model="keyword" class="master-native-input" aria-label="关键字"/></label><label><span class="form-field-label">状态</span><select v-model="status" class="master-native-input" aria-label="状态"><option value="">全部状态</option><option v-for="state in states" :key="String(state)" :value="String(state)">{{display(state)}}</option></select></label><label><span class="form-field-label">物料批次</span><IncomingReferencePicker field="materialLotId" v-model="materialLotId" :context="{}"/></label><a-space class="form-full-row"><a-button type="primary" html-type="submit" :loading="busy">查询</a-button><a-button @click="keyword='';status='';materialLotId='';page=1;search()">重置</a-button></a-space></form></a-card>
 <a-card><a-table :columns="columns" :data-source="rows" row-key="id" :loading="busy" :scroll="{x:900}" :pagination="{current:page,total,pageSize:20,showSizeChanger:false}" @change="changePage"><template #bodyCell="{column,record}"><a-button v-if="column.key==='actions'" type="link" @click="router.push({path:`${base}/${record.id}`,query:route.query})">查看</a-button><span v-else>{{display(record[column.key])}}</span></template></a-table></a-card>
</main></template>

<style scoped>@media(min-width:1024px){.incoming-query{grid-template-columns:repeat(3,minmax(0,1fr))}}</style>
