<script setup lang="ts">
import {ref,watch} from 'vue'
import {api,errorMessage,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
const props=defineProps<{modelValue:string|null;resource:'units'|'materials'|'products';label:string;disabled?:boolean;allowClear?:boolean}>(),emit=defineEmits<{ 'update:modelValue':[string|null] }>(),auth=useAuthStore(),options=ref<{id:string;label:string}[]>([]),error=ref('')
const permission=()=>props.resource==='units'?'master:uom:view':props.resource==='materials'?'master:material:view':'master:product:view'
function option(r:Record<string,unknown>){const p=props.resource==='units'?'unit':props.resource==='materials'?'material':'product';return {id:String(r.id),label:`${r[p+'Name']}（${r[p+'Code']}）`}}
let request=0
async function search(keyword=''){if(!auth.can(permission()))return;const ticket=++request;try{const page=await api<Page<Record<string,unknown>>>({url:`/${props.resource}`,params:{keyword,size:100}});if(ticket!==request)return;const next=page.items.map(option);options.value=[...next,...options.value.filter(o=>o.id===props.modelValue&&!next.some(n=>n.id===o.id))]}catch(e){error.value=errorMessage(e)}}
watch(()=>[props.resource,props.modelValue],async()=>{await search();if(props.modelValue&&!options.value.some(o=>o.id===props.modelValue)){if(!auth.can(permission())){options.value.push({id:props.modelValue,label:`#${props.modelValue}`});return}try{options.value.push(option(await api({url:`/${props.resource}/${props.modelValue}`})))}catch(e){error.value=errorMessage(e)}}},{immediate:true})
</script>
<template><span class="process-lookup"><a-select :value="modelValue||undefined" :aria-label="label" :disabled="disabled||!auth.can(permission())" :allow-clear="allowClear" show-search :filter-option="false" @search="search" @change="(v:unknown)=>emit('update:modelValue',v?String(v):null)"><a-select-option v-for="o in options" :key="o.id" :value="o.id">{{o.label}}</a-select-option></a-select><small v-if="error" role="alert">{{error}}</small></span></template>
