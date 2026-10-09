<script setup lang="ts">
import {ref,watch} from 'vue'
import {dictionaryOptions,type DictionaryOption} from '../views/admin/dictionaryModel'
const props=defineProps<{dictCode:string;modelValue:string|number|null|undefined;placeholder?:string;disabled?:boolean;required?:boolean}>()
const emit=defineEmits<{(event:'update:modelValue',value:string|null):void}>()
const items=ref<DictionaryOption[]>([]),loading=ref(false),error=ref('')
async function load(){
 loading.value=true;error.value=''
 try{items.value=await dictionaryOptions(props.dictCode,props.modelValue==null?null:String(props.modelValue))}catch(e){error.value=e instanceof Error?e.message:'字典加载失败'}finally{loading.value=false}
}
watch([()=>props.dictCode,()=>props.modelValue],load,{immediate:true})
</script>
<template>
<div class="dict-select-control">
 <a-select show-search option-filter-prop="label" :value="modelValue==null?undefined:String(modelValue)"
  :options="items.map(item=>({value:item.value,label:item.label,disabled:item.disabled}))"
  :placeholder="placeholder??'请选择'" :loading="loading" :disabled="disabled||!!error" :allow-clear="!required"
  style="width:100%" @change="(value:unknown)=>emit('update:modelValue',value==null?null:String(value))"/>
 <small v-if="error" class="dict-lookup-error">字典加载失败，请重试</small>
 <small v-else-if="!loading&&!items.length" class="dict-lookup-error">尚未配置可用字典项</small>
</div>
</template>
<style scoped>
.dict-select-control{min-width:0;width:100%}
.dict-lookup-error{color:#b36d2b;font-size:11px;display:block;margin-top:3px}
</style>
