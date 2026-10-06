<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {api,errorMessage,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
const props=defineProps<{modelValue:unknown;resource:'units'|'materials'|'users'|'organizations';label:string;disabled?:boolean}>(),emit=defineEmits<{'update:modelValue':[string|null]}>(),auth=useAuthStore(),options=ref<Record<string,unknown>[]>([]),error=ref('')
const permission=computed(()=>({units:'master:uom:view',materials:'master:material:view',users:'iam:user:view',organizations:'master:org:view'})[props.resource])
function name(r:Record<string,unknown>){return String(r.unitName??r.materialName??r.displayName??r.orgName??r.id)}
let ticket=0
async function search(keyword=''){
 const current=++ticket;error.value='';if(!auth.can(permission.value))return
 try{const result=await api<Page<Record<string,unknown>>>({url:`/${props.resource}`,params:{keyword,page:0,size:100}});let items=result.items;if(props.modelValue&&!items.some(r=>String(r.id)===String(props.modelValue)))items=[...items,await api<Record<string,unknown>>({url:`/${props.resource}/${props.modelValue}`})];if(ticket===current)options.value=items}catch(e){if(ticket===current)error.value=errorMessage(e)}
}
watch(()=>[props.resource,props.modelValue],()=>void search(),{immediate:true})
</script>
<template><span class="business-reference"><input v-if="!auth.can(permission)||error" :value="modelValue??''" class="master-native-input" :aria-label="label" :disabled="disabled" @input="emit('update:modelValue',($event.target as HTMLInputElement).value||null)"/><a-select v-else :value="modelValue==null?undefined:String(modelValue)" :aria-label="label" show-search :filter-option="false" allow-clear :disabled="disabled" @search="search" @change="(v:unknown)=>emit('update:modelValue',v==null?null:String(v))"><a-select-option v-if="modelValue&&!options.some(r=>String(r.id)===String(modelValue))" :value="String(modelValue)">来源引用 {{modelValue}}</a-select-option><a-select-option v-for="r in options" :key="String(r.id)" :value="String(r.id)">{{name(r)}}</a-select-option></a-select><small v-if="error" role="alert">{{error}}</small></span></template>
<style scoped>.business-reference{min-width:0;width:100%}.ant-select{width:100%}small{display:block;font-size:12px;color:#b42318}</style>
