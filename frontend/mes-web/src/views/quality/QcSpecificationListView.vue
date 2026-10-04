<script setup lang="ts">
import {ref,onMounted} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {errorMessage,type Page} from '../../api/http'
import {qcApi,qcStates,type QcSpecification} from '../../api/qcSpecifications'
import {useAuthStore} from '../../stores/auth'
import ProcessLookup from '../process/ProcessLookup.vue'
const route=useRoute(),router=useRouter(),auth=useAuthStore(),keyword=ref(String(route.query.keyword??'')),materialId=ref<string|null>(route.query.materialId?String(route.query.materialId):null),versionStatus=ref(String(route.query.versionStatus??'')),busy=ref(false),error=ref('')
const data=ref<Page<QcSpecification>>({items:[],total:0,page:0,size:20})
const columns=[{title:'标准编码',dataIndex:'specificationCode'},{title:'标准名称',dataIndex:'specificationName'},{title:'物料',key:'material'},{title:'更新时间',dataIndex:'updatedAt'},{title:'操作',key:'actions'}]
async function load(page=0){busy.value=true;error.value='';try{const params={page,size:20,keyword:keyword.value,materialId:materialId.value||undefined,versionStatus:versionStatus.value||undefined};data.value=await qcApi.list(params);await router.replace({query:Object.fromEntries(Object.entries(params).filter(([,v])=>v!==undefined&&v!=='').map(([k,v])=>[k,String(v)]))})}catch(e){error.value=errorMessage(e)}finally{busy.value=false}}
function reset(){keyword.value='';materialId.value=null;versionStatus.value='';void load()}
onMounted(()=>load(Number(route.query.page??0)))
</script>
<template><main class="admin-page master-page"><header class="admin-page-header"><div><h1>QC质量标准</h1><p>物料检验标准 · 受控版本</p></div><a-button v-if="auth.can('qms:specification:create')" type="primary" @click="router.push('/quality/specifications/create')">＋ 新增标准</a-button></header>
<a-card title="查询条件" class="query-card"><form class="query-form" @submit.prevent="load()"><label>编码 / 名称<a-input v-model:value="keyword" allow-clear maxlength="200"/></label><label>物料<ProcessLookup v-model="materialId" resource="materials" label="筛选物料" allow-clear/></label><label>版本状态<a-select v-model:value="versionStatus" allow-clear><a-select-option v-for="(label,state) in qcStates" :key="state" :value="state">{{label}}</a-select-option></a-select></label><a-space><a-button @click="reset">重置</a-button><a-button type="primary" html-type="submit">查询</a-button></a-space></form></a-card>
<a-alert v-if="error" type="error" :message="error" show-icon/><a-card title="标准列表" class="result-card"><a-table :columns="columns" :data-source="data.items" :loading="busy" :pagination="false" row-key="id" :scroll="{x:900}"><template #bodyCell="{column,record}"><template v-if="column.key==='material'">{{record.materialName}}（{{record.materialCode}}）</template><a-button v-else-if="column.key==='actions'" type="link" @click="router.push(`/quality/specifications/${record.id}`)">查看</a-button></template></a-table><div class="table-footer"><span>共 {{data.total}} 条</span><a-pagination :current="data.page+1" :page-size="data.size" :total="data.total" :show-size-changer="false" @change="(page:number)=>load(page-1)"/></div></a-card></main></template>
<style scoped>.query-form :deep(.process-lookup){display:block;min-width:0;flex:1}.query-form :deep(.ant-select){width:100%}</style>
