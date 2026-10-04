<script setup lang="ts">
import {blankQcItem,type QcItem} from '../../api/qcSpecifications'
import ProcessLookup from '../process/ProcessLookup.vue'
const props=defineProps<{modelValue:QcItem[];disabled?:boolean}>()
const emit=defineEmits<{'update:modelValue':[QcItem[]]}>()
function add(){emit('update:modelValue',[...props.modelValue,blankQcItem()])}
function remove(index:number){emit('update:modelValue',props.modelValue.filter((_,i)=>i!==index))}
function typeChanged(item:QcItem){if(item.resultType==='TEXT'){item.lowerLimit=null;item.upperLimit=null;item.unitId=null}else item.textAcceptanceCriteria=null}
</script>
<template><section class="qc-items"><a-empty v-if="!modelValue.length" description="尚未维护检验项目"/>
<a-card v-for="(item,index) in modelValue" :key="item.specificationItemId??index" :title="`${index+1}. ${item.itemName||'检验项目'}`" class="form-section">
<template #extra><a-button v-if="!disabled" danger @click="remove(index)">移除项目</a-button></template>
<div class="master-form"><label><span class="form-field-label">项目编码</span><input v-model="item.itemCode" :aria-label="`项目编码 ${index+1}`" class="master-native-input" maxlength="100" :disabled="disabled||!!item.specificationItemId" required/></label>
<label><span class="form-field-label">项目名称</span><input v-model="item.itemName" :aria-label="`项目名称 ${index+1}`" class="master-native-input" maxlength="200" :disabled="disabled" required/></label>
<label><span class="form-field-label">是否必检</span><input v-model="item.required" :aria-label="`是否必检 ${index+1}`" type="checkbox" :disabled="disabled"/></label>
<label><span class="form-field-label">结果类型</span><select v-model="item.resultType" :aria-label="`结果类型 ${index+1}`" class="master-native-input" :disabled="disabled" @change="typeChanged(item)"><option value="NUMERIC">数值</option><option value="TEXT">文本</option></select></label>
<template v-if="item.resultType==='NUMERIC'"><label><span class="form-field-label">下限（含）</span><input v-model="item.lowerLimit" :aria-label="`下限 ${index+1}`" class="master-native-input" inputmode="decimal" :disabled="disabled" placeholder="可留空"/></label>
<label><span class="form-field-label">上限（含）</span><input v-model="item.upperLimit" :aria-label="`上限 ${index+1}`" class="master-native-input" inputmode="decimal" :disabled="disabled" placeholder="可留空"/></label>
<label><span class="form-field-label">单位</span><ProcessLookup v-model="item.unitId" resource="units" :label="`单位 ${index+1}`" :disabled="disabled"/></label></template>
<label v-else class="form-full-row"><span class="form-field-label">文本合格标准</span><textarea v-model="item.textAcceptanceCriteria" :aria-label="`文本合格标准 ${index+1}`" class="master-native-input" maxlength="1000" :disabled="disabled" required/></label>
<label><span class="form-field-label">方法编码</span><input v-model="item.methodCode" :aria-label="`方法编码 ${index+1}`" class="master-native-input" maxlength="100" :disabled="disabled" required/></label>
<label><span class="form-field-label">方法版本</span><input v-model="item.methodVersion" :aria-label="`方法版本 ${index+1}`" class="master-native-input" maxlength="50" :disabled="disabled" required/></label></div></a-card>
<a-button v-if="!disabled" @click="add">＋ 添加检验项目</a-button></section></template>
<style scoped>.qc-items{min-width:0}.qc-items :deep(.process-lookup){min-width:0;display:block;width:100%}.qc-items :deep(.ant-select){width:100%}.qc-items .master-form>label{grid-template-columns:110px minmax(0,1fr)}@media(max-width:560px){.qc-items .master-form>label{grid-template-columns:90px minmax(0,1fr);gap:0 8px}}</style>
