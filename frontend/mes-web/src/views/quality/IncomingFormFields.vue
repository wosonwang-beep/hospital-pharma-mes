<script setup lang="ts">
import {computed} from 'vue'
import {resolve,scalarType,initial,label,display,referenceFields,type Schema,type IncomingRow} from './incomingModel'
import IncomingReferencePicker from './IncomingReferencePicker.vue'
import BalanceExpressionEditor from './BalanceExpressionEditor.vue'
import type {Expression} from './productionQualityModel'
const props=withDefaults(defineProps<{schema:Schema;modelValue:Record<string,unknown>;disabled?:boolean;prefix?:string;context?:IncomingRow;lockedFields?:string[]}>(),{prefix:'',context:()=>({}),lockedFields:()=>[]})
const emit=defineEmits<{ 'update:modelValue':[Record<string,unknown>] }>()
const fields=computed(()=>Object.entries(resolve(props.schema).properties??{}).filter(([key])=>key!=='reauthToken'))
function set(key:string,value:unknown){emit('update:modelValue',{...props.modelValue,[key]:value})}
function selectedReference(key:string,row:IncomingRow){if(key==='sampleId'&&props.context.investigationScope==='PRODUCTION')emit('update:modelValue',{...props.modelValue,sampleId:String(row.id),specificationItemId:''});if(key==='inspectionRequestId'){const items=(row.items??[]) as IncomingRow[];emit('update:modelValue',{...props.modelValue,inspectionRequestId:String(row.id),...(items.length===1?{inspectionRequestItemId:items[0]!.id}:{})})}if(key==='materialLotId'){emit('update:modelValue',{...props.modelValue,materialLotId:String(row.id),qcSpecificationVersionId:'',approvedInvestigationId:''})}}
function jsonText(v:unknown){return typeof v==='string'?v:JSON.stringify(v??{},null,2)}
function values(key:string):unknown[]{return Array.isArray(props.modelValue[key])?props.modelValue[key] as unknown[]:[]}
function arraySet(key:string,index:number,value:unknown){const all=[...values(key)];all[index]=value;set(key,all)}
function remove(key:string,index:number){set(key,values(key).filter((_,i)=>i!==index))}
function text(event:Event){return (event.target as HTMLInputElement).value}
function required(key:string){return resolve(props.schema).required?.includes(key)}
</script>
<template><div class="master-form incoming-form">
 <template v-for="[key,raw] in fields" :key="key">
 <fieldset v-if="scalarType(raw)==='array'" class="form-full-row"><legend>{{label(key)}}</legend>
  <div v-for="(row,index) in values(key)" :key="index" class="array-row">
   <IncomingFormFields v-if="scalarType(resolve(raw).items!)==='object'" :schema="resolve(raw).items!" :model-value="row as Record<string,unknown>" :prefix="`${prefix}${key}[${index}].`" :disabled="disabled" :context="{...context,...modelValue}" :locked-fields="lockedFields" @update:model-value="v=>arraySet(key,index,v)"/>
   <label v-else><span class="form-field-label">{{label(key)}} {{index+1}}</span><IncomingReferencePicker v-if="referenceFields.has(key)" :field="key" :model-value="row" :context="{...context,...modelValue}" :disabled="disabled||lockedFields.includes(key)" @update:model-value="v=>arraySet(key,index,v)"/><input v-else :value="row" class="master-native-input" :aria-label="`${label(key)} ${index+1}`" :disabled="disabled" @input="arraySet(key,index,text($event))"/></label>
   <a-button :disabled="disabled||lockedFields.includes(key)" @click="remove(key,index)">移除未保存行</a-button>
  </div><a-button :disabled="disabled||lockedFields.includes(key)" @click="set(key,[...values(key),initial(resolve(raw).items!)])">添加{{label(key)}}</a-button>
 </fieldset>
 <fieldset v-else-if="raw.$ref?.endsWith('/BalanceExpression')" class="form-full-row"><legend>{{label(key)}}</legend><BalanceExpressionEditor :model-value="modelValue[key] as Expression" :disabled="disabled" @update:model-value="set(key,$event)"/></fieldset>
 <fieldset v-else-if="scalarType(raw)==='object'&&resolve(raw).properties" class="form-full-row"><legend>{{label(key)}}</legend><IncomingFormFields :schema="raw" :model-value="(modelValue[key]??{}) as Record<string,unknown>" :disabled="disabled" :context="{...context,...modelValue}" :locked-fields="lockedFields" @update:model-value="v=>set(key,v)"/></fieldset>
 <label v-else :class="{'form-full-row':(resolve(raw).maxLength??0)>250||scalarType(raw)==='object'}"><span class="form-field-label">{{label(key)}}<span v-if="required(key)" aria-hidden="true"> *</span></span>
  <IncomingReferencePicker v-if="referenceFields.has(key)" :field="key" :model-value="modelValue[key]" :context="{...context,...modelValue}" :required="required(key)" :disabled="disabled||lockedFields.includes(key)" @update:model-value="v=>set(key,v)" @select="r=>selectedReference(key,r)"/>
  <select v-else-if="resolve(raw).enum" :value="modelValue[key]??''" class="master-native-input" :aria-label="label(key)" :name="prefix+key" :disabled="disabled||resolve(raw).enum?.length===1||lockedFields.includes(key)" :required="required(key)" @change="set(key,text($event))"><option value="">请选择</option><option v-for="option in resolve(raw).enum" :key="String(option)" :value="String(option)">{{display(option)}}</option></select>
  <input v-else-if="scalarType(raw)==='boolean'" :checked="modelValue[key]===true" type="checkbox" :aria-label="label(key)" :name="prefix+key" :disabled="disabled" @change="set(key,($event.target as HTMLInputElement).checked)"/>
  <textarea v-else-if="(resolve(raw).maxLength??0)>250||scalarType(raw)==='object'" :value="scalarType(raw)==='object'?jsonText(modelValue[key]):modelValue[key] as string" class="master-native-input" :aria-label="label(key)" :name="prefix+key" :disabled="disabled" :maxlength="resolve(raw).maxLength" :required="required(key)" @input="set(key,text($event))"/>
  <input v-else :value="modelValue[key]" :type="resolve(raw).format==='date'?'date':resolve(raw).format==='date-time'?'datetime-local':scalarType(raw)==='integer'?'number':'text'" class="master-native-input" :aria-label="label(key)" :name="prefix+key" :disabled="disabled" :min="resolve(raw).minimum" :max="resolve(raw).maximum" :maxlength="resolve(raw).maxLength" :required="required(key)" @input="set(key,text($event))"/>
 </label>
 </template>
</div></template>
<style scoped>fieldset{border:1px solid #e8e8e8;border-radius:6px;padding:16px;min-width:0}legend{font-weight:600;padding:0 6px}.array-row{border-bottom:1px solid #e8e8e8;padding:12px 0;margin-bottom:12px}.array-row>.ant-btn{margin-top:10px}</style>
