<script setup lang="ts">
import {ref,watch} from 'vue'
import {api,errorMessage,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
const props=defineProps<{modelValue?:string;disabled?:boolean;label?:string;all?:boolean}>(),emit=defineEmits<{'update:modelValue':[string];selected:[Record<string,any>]}>()
const auth=useAuthStore(),options=ref<Record<string,any>[]>([]),error=ref('');let ticket=0
async function search(keyword=''){if(!auth.can('wms:request:view'))return;const t=++ticket;try{const pages=await Promise.all((props.all?[undefined]:['SUBMITTED','PARTIALLY_ISSUED']).map(status=>api<Page<Record<string,any>>>({url:'/material-requests',params:{status,keyword,size:100}})));if(t!==ticket)return;options.value=pages.flatMap(p=>p.items)}catch(e){error.value=errorMessage(e)}}
async function choose(value:unknown){const id=value?String(value):'';if(!id){emit('update:modelValue','');return}try{const r=await api<Record<string,any>>({url:`/material-requests/${id}`});emit('update:modelValue',id);emit('selected',r)}catch(e){error.value=errorMessage(e)}}
watch(()=>props.modelValue,async()=>{await search();if(props.modelValue&&!options.value.some(r=>r.id===props.modelValue)&&auth.can('wms:request:view'))try{options.value.push(await api({url:`/material-requests/${props.modelValue}`}))}catch(e){error.value=errorMessage(e)}},{immediate:true})
</script>
<template><span class="request-picker"><a-select :value="modelValue||undefined" :aria-label="label||'领料申请'" :disabled="disabled||!auth.can('wms:request:view')" show-search :filter-option="false" allow-clear @search="search" @change="choose"><a-select-option v-for="r in options" :key="r.id" :value="r.id">{{r.requestNo}} · {{r.batchNo}} · {{r.productName}}</a-select-option></a-select><small v-if="error" role="alert">{{error}}</small></span></template>
<style scoped>.request-picker{width:100%;min-width:0}.ant-select{width:100%}small{display:block;color:#b42318}</style>
