<script setup lang="ts">
import type {Rule} from './model'
withDefaults(defineProps<{modelValue:Rule|null;disabled?:boolean;depth?:number}>(),{depth:0})
const emit=defineEmits<{'update:modelValue':[Rule|null]}>()
const labels:Record<string,string>={EBR_COMPLETE:'记录填写完成',PARAMETERS_WITHIN_LIMITS:'参数满足范围',REVIEW_COMPLETE:'记录复核完成',ALL:'全部条件满足',ANY:'任一条件满足'}
function selected(kind:string){emit('update:modelValue',kind?{kind,...(['ALL','ANY'].includes(kind)?{rules:[{kind:'EBR_COMPLETE'}]}:{})}:null)}
function child(rule:Rule,index:number,value:Rule|null){const rules=[...(rule.rules??[])];if(value)rules[index]=value;else rules.splice(index,1);if(rules.length)emit('update:modelValue',{...rule,rules})}
</script>
<template><div class="completion-rule"><select aria-label="完成条件" :disabled="disabled" :value="modelValue?.kind??''" @change="selected(($event.target as HTMLSelectElement).value)"><option value="">沿用默认完成检查</option><option v-for="(label,key) in labels" :key="key" :value="key" :disabled="depth>=7&&['ALL','ANY'].includes(key)">{{label}}</option></select><div v-if="modelValue?.rules" class="children"><div v-for="(rule,i) in modelValue.rules" :key="i"><CompletionRuleEditor :model-value="rule" :disabled="disabled" :depth="depth+1" @update:model-value="child(modelValue!,i,$event)"/><button v-if="!disabled&&modelValue.rules.length>1" type="button" @click="child(modelValue!,i,null)">移除条件</button></div><button v-if="!disabled&&modelValue.rules.length<20" type="button" @click="emit('update:modelValue',{...modelValue,rules:[...modelValue.rules,{kind:'EBR_COMPLETE'}]})">添加条件</button></div></div></template>
<style scoped>.completion-rule{display:grid;gap:8px;min-width:0}select,button{min-height:32px;border:1px solid var(--mes-ui-border,#d9d9d9);border-radius:6px;padding:4px 8px;background:#fff;color:inherit;width:100%}.children{display:grid;gap:12px;margin-left:12px;padding-left:12px;border-left:2px solid #dfe7f1}.children>div{display:grid;gap:6px}</style>
