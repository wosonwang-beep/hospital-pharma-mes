<script setup lang="ts">
import {ref,watch} from 'vue'
import {api,errorMessage,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
const props=defineProps<{modelValue:string|undefined;resource:'suppliers'|'warehouses'|'locations'|'containers';label:string;disabled?:boolean;warehouseId?:string}>(),emit=defineEmits<{'update:modelValue':[string]}>(),auth=useAuthStore(),options=ref<{id:string;label:string}[]>([]),error=ref('')
const permission=()=>props.resource==='suppliers'?'master:supplier:view':'wms:inventory:view'
function option(r:Record<string,unknown>){const prefix=props.resource==='suppliers'?'supplier':props.resource==='warehouses'?'warehouse':props.resource==='locations'?'location':'container';return {id:String(r.id),label:`${r[prefix+'Name']??r[prefix+'Code']}（${r[prefix+'Code']}）`}}
let ticket=0
async function search(keyword=''){if(!auth.can(permission()))return;const current=++ticket;try{const result=await api<Page<Record<string,unknown>>>({url:`/${props.resource}`,params:{keyword,size:100,...(props.resource==='locations'?{warehouseId:props.warehouseId}:{} )}});if(current!==ticket)return;options.value=[...result.items.map(option),...options.value.filter(o=>o.id===props.modelValue&&!result.items.some(n=>String(n.id)===o.id))]}catch(e){error.value=errorMessage(e)}}
watch(()=>[props.resource,props.modelValue,props.warehouseId],async()=>{await search();if(props.modelValue&&!options.value.some(o=>o.id===props.modelValue)&&auth.can(permission()))try{options.value.push(option(await api({url:`/${props.resource}/${props.modelValue}`})))}catch(e){error.value=errorMessage(e)}},{immediate:true})
</script>
<template><span class="wms-lookup"><a-select :value="modelValue||undefined" :aria-label="label" show-search :filter-option="false" allow-clear :disabled="disabled||!auth.can(permission())" @search="search" @change="(v:unknown)=>emit('update:modelValue',v?String(v):'')"><a-select-option v-for="o in options" :key="o.id" :value="o.id">{{o.label}}</a-select-option></a-select><small v-if="error" role="alert">{{error}}</small></span></template>
<style scoped>.wms-lookup{min-width:0;width:100%}.wms-lookup .ant-select{width:100%}.wms-lookup small{display:block;color:#b42318;font-size:11px}</style>
