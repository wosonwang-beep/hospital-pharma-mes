<script setup lang="ts">
import {computed} from 'vue'
import {label,display} from './incomingModel'
const props=defineProps<{value:Record<string,unknown>;labelFormatter?:(key:string)=>string}>()
const fields=computed(()=>Object.entries(props.value).filter(([key])=>!['signingTargets','signatureEvidence','orgId','allowedActions','signatureEvidenceJson','dslVersion'].includes(key)))
function object(v:unknown){return v!==null&&typeof v==='object'&&!Array.isArray(v)}
function fieldLabel(key:string){return props.labelFormatter?.(key)??label(key)}
</script>
<template><div class="master-form">
 <template v-for="[key,value] in fields" :key="key">
 <section v-if="Array.isArray(value)" class="form-full-row fact-section"><h3>{{fieldLabel(key)}}</h3><p v-if="!value.length">暂无记录</p><template v-for="(row,index) in value" :key="index"><IncomingFacts v-if="object(row)" :value="row as Record<string,unknown>" :label-formatter="labelFormatter"/><p v-else>{{display(row)}}</p></template></section>
 <section v-else-if="object(value)" class="form-full-row fact-section"><h3>{{fieldLabel(key)}}</h3><IncomingFacts :value="value as Record<string,unknown>" :label-formatter="labelFormatter"/></section>
 <label v-else><span class="form-field-label">{{fieldLabel(key)}}</span><span class="master-read-value" :class="{'error':value==='FAIL','success':value==='PASS'}">{{display(value)}}</span></label>
 </template>
</div></template>
<style scoped>.fact-section{border-top:1px solid #e8e8e8;padding-top:12px}.fact-section>.master-form{padding-bottom:16px}.master-read-value{overflow-wrap:anywhere}.success{color:#389e0d}</style>
