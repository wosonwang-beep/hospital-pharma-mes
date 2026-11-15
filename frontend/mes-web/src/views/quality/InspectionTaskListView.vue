<script setup lang="ts">
import {ref,onBeforeUnmount} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,errorMessage} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import InspectionTaskList from './inspection/InspectionTaskList.vue'
import {parseInspectionQuery,permits,type InspectionQuery,type InspectionRow} from './inspection/inspectionListModel'
import {createInspectionLoader,type InspectionApiPage} from './inspection/inspectionAdapter'
const route=useRoute(),router=useRouter(),auth=useAuthStore()
const inspectors=ref<{value:string;label:string}[]>([])
const params=new URLSearchParams()
for(const [key,value] of Object.entries(route.query))if(typeof value==='string')params.set(key,value)
const initialQuery=parseInspectionQuery(params)
let generation=0
const loader=createInspectionLoader(query=>api<InspectionApiPage>({url:'/quality/inspection-tasks',params:{...query,projection:'t1'}}),permission=>auth.can(permission))
function queryParams(query:InspectionQuery){return {keyword:query.keyword,status:query.status,assignedTo:query.assignedTo,requestType:query.requestType,page:String(query.page),size:String(query.size)}}
async function loadPage(query:InspectionQuery){
 const current=++generation
 await router.replace({query:queryParams(query)})
 try{const data=await loader(query);if(current===generation)inspectors.value=data.inspectors;return data}
 catch(error){throw new Error(errorMessage(error))}
}
function open(row:InspectionRow,action:string){
 if(!permits(row,action))return
 void router.push({path:`/quality/inspection-tasks/${encodeURIComponent(row.id)}${action==='execute'?'/execute':''}`,query:{...route.query}})
}
onBeforeUnmount(()=>{++generation})
</script>
<template><InspectionTaskList class="formal-inspection-adapter" :load-page="loadPage" :inspectors="inspectors" :initial-query="initialQuery" @open="open"/></template>
<style>
/* Restore the approved component's compact metrics against the existing #app query rules.
   This adapter is deliberately local; the shared Global UI V2 CSS and source prototype stay unchanged. */
#app .formal-inspection-adapter{--mes-ui-primary:#2c5cdc}
#app .formal-inspection-adapter .ant-btn-primary:not(:disabled):not(.ant-btn-dangerous):hover{background:#2c5cdc;border-color:#2c5cdc}
#app .formal-inspection-adapter .inspection-query{gap:10px 12px;font-size:13px}
#app .formal-inspection-adapter .inspection-query label{font-size:13px;flex:none}
#app .formal-inspection-adapter .inspection-query .form-field-label{font-size:13px}
#app .formal-inspection-adapter .inspection-query .ant-btn{font-size:13px}
#app .formal-inspection-adapter .ant-btn-primary{background:#2c5cdc;border-color:#2c5cdc}
#app .formal-inspection-adapter .ant-btn-link,#app .formal-inspection-adapter .ant-pagination-item-active a{color:#2c5cdc}
</style>
