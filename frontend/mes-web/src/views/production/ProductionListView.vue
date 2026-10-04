<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {useControlledRequest} from '../../api/controlled'
import './productionModel'
import {label,display} from '../quality/incomingModel'
const route=useRoute(),router=useRouter(),auth=useAuthStore(),request=useControlledRequest(),{busy,error}=request,rows=ref<Record<string,unknown>[]>([]),total=ref(0),page=ref(1),keyword=ref(''),status=ref('')
const batch=computed(()=>route.meta.resource==='batches'),title=computed(()=>batch.value?'生产批':'生产订单'),base=computed(()=>batch.value?'/main-batches':'/production-orders'),ui=computed(()=>batch.value?'/production/batches':'/production/orders'),permission=computed(()=>batch.value?'production:batch':'production:order')
const columns=computed(()=>[batch.value?'batchNo':'orderNo','productId','plannedQty','unitId','plannedDate','status'].map(k=>({title:label(k),key:k,dataIndex:k})).concat([{title:'操作',key:'actions',dataIndex:'actions'}]))
async function load(){busy.value=true;try{const result=await api<Page<Record<string,unknown>>>({url:base.value,params:{page:page.value-1,size:20,keyword:keyword.value||undefined,status:status.value||undefined}});rows.value=result.items;total.value=result.total}catch(e){await request.failure(e)}finally{busy.value=false}}
watch(base,()=>{page.value=1;void load()},{immediate:true})
</script>
<template><main class="admin-page master-page"><header class="admin-page-header"><h1>{{title}}</h1><a-button v-if="auth.can(`${permission}:create`)" type="primary" @click="router.push(`${ui}/create`)">新增{{title}}</a-button></header><a-alert v-if="error" type="error" :message="error"/><a-card class="form-section"><form class="master-form" @submit.prevent="page=1;load()"><label><span class="form-field-label">关键字</span><input v-model="keyword" class="master-native-input" aria-label="关键字"/></label><label><span class="form-field-label">状态</span><input v-model="status" class="master-native-input" aria-label="状态"/></label><a-space><a-button type="primary" html-type="submit" :loading="busy">查询</a-button><a-button @click="keyword='';status='';page=1;load()">重置</a-button></a-space></form></a-card><a-card><a-table :columns="columns" :data-source="rows" row-key="id" :loading="busy" :scroll="{x:850}" :pagination="{current:page,total,pageSize:20,showSizeChanger:false}" @change="(p:{current?:number})=>{page=p.current??1;load()}"><template #bodyCell="{column,record}"><a-button v-if="column.key==='actions'" type="link" @click="router.push(`${ui}/${record.id}`)">查看</a-button><span v-else>{{display(record[column.key])}}</span></template></a-table></a-card></main></template>
