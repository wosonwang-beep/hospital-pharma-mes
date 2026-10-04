<script setup lang="ts">
import {computed,ref,onMounted} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api} from '../../api/http'
import {useControlledRequest} from '../../api/controlled'
import './productionModel'
import IncomingFacts from '../quality/IncomingFacts.vue'
const route=useRoute(),router=useRouter(),request=useControlledRequest(),{busy,error}=request,mainBatchId=ref(String(route.query.mainBatchId??'')),materialLotId=ref(String(route.query.materialLotId??'')),result=ref<Record<string,unknown>|null>(null)
async function search(){busy.value=true;try{if(Boolean(mainBatchId.value.trim())===Boolean(materialLotId.value.trim()))throw Error("请选择生产批或物料批中的一个进行追溯");result.value=await api({url:'/trace',params:{mainBatchId:mainBatchId.value||undefined,materialLotId:materialLotId.value||undefined}})}catch(e){await request.failure(e)}finally{busy.value=false}}
const nodes=computed(()=>(result.value?.nodes??[]) as Record<string,unknown>[])
function destination(n:Record<string,unknown>){if(n.type==='QC_SPECIFICATION_VERSION')return `/quality/specification-versions/${n.id}/edit`;const paths:Record<string,string>={MAIN_BATCH:'/production/batches',EXECUTION_UNIT:'/mes/execution',MATERIAL_LOT:'/wms/material-lots',RECEIPT:'/wms/receipts',INSPECTION_REQUEST:'/quality/inspection-requests',SAMPLING_TASK:'/quality/sampling-tasks',SAMPLE:'/quality/samples',INSPECTION_TASK:'/quality/inspection-tasks',REPORT:'/quality/inspection-reports',INVESTIGATION:'/deviations'};return paths[String(n.type)]?`${paths[String(n.type)]}/${n.id}`:undefined}
onMounted(()=>{if(mainBatchId.value||materialLotId.value)void search()})
</script>
<template><main class="admin-page master-page"><header class="admin-page-header"><h1>完整追溯</h1><a-button v-if="materialLotId" @click="router.push(`/wms/material-lots/${materialLotId}`)">物料批详情</a-button></header><a-alert v-if="error" type="error" :message="error"/><a-card class="form-section"><form class="master-form" @submit.prevent="search"><label><span class="form-field-label">生产批</span><input v-model="mainBatchId" class="master-native-input" aria-label="生产批"/></label><label><span class="form-field-label">物料批次</span><input v-model="materialLotId" class="master-native-input" aria-label="物料批次"/></label><a-button type="primary" html-type="submit" :loading="busy">查询追溯</a-button></form></a-card><a-card v-if="result" title="实际业务记录与关系"><a-space wrap class="form-section"><template v-for="n in nodes" :key="`${n.type}:${n.id}`"><a-button v-if="destination(n)" type="link" @click="router.push(destination(n)!)">{{n.label}}</a-button></template></a-space><IncomingFacts :value="result"/></a-card></main></template>
