<script setup lang="ts">
import {computed,ref,useId,watch} from 'vue'
import {Select as ASelect} from 'ant-design-vue'
import type {EbrRoleOption} from './types'
const props=defineProps<{roleId?:string|null;legacyCode?:string|null;codeSnapshot?:string|null;nameSnapshot?:string|null;editable:boolean;options:EbrRoleOption[];loading?:boolean}>()
const emit=defineEmits<{'update:roleId':[string];search:[string]}>()
const inputId=useId()
const knownLabels=ref<Record<string,string>>({})
watch(()=>[props.codeSnapshot,props.nameSnapshot],()=>{if(props.roleId&&props.nameSnapshot)knownLabels.value[props.roleId]=`${props.nameSnapshot}（${props.codeSnapshot??''}）`},{immediate:true})
watch(()=>props.options,rows=>{for(const r of rows)if(r.enabled)knownLabels.value[r.id]=`${r.name}（${r.code}）`},{deep:true,immediate:true})
const choices=computed(()=>{
 const live=props.options.filter(r=>r.enabled).map(r=>({value:r.id,label:`${r.name}（${r.code}）`,disabled:false}))
 const id=props.roleId,label=id?knownLabels.value[id]:undefined
 return id&&label&&!live.some(r=>r.value===id)?[{value:id,label,disabled:true},...live]:live
})
const frozenLabel=computed(()=>props.nameSnapshot?`${props.nameSnapshot}（${props.codeSnapshot??''}）`:props.legacyCode??props.codeSnapshot??'未配置')
</script>
<template>
  <div class="ebr-role-pair">
    <label v-if="editable" class="ebr-role-label" :for="inputId"><span aria-hidden="true">角色</span><span class="ebr-role-accessible-label">签署或复核角色</span></label>
    <span v-else class="ebr-role-label">角色</span>
    <a-select v-if="editable" :id="inputId" :value="roleId??undefined" :options="choices" :loading="loading" size="small" show-search :filter-option="false" placeholder="请选择角色" @search="emit('search',$event)" @change="emit('update:roleId',String($event))"/>
    <span v-else class="ebr-frozen-role" data-testid="frozen-role">{{frozenLabel}}</span>
  </div>
</template>
<style scoped>
.ebr-role-pair{display:flex;align-items:center;gap:8px;min-width:0;flex-wrap:nowrap}.ebr-role-label{flex:none}.ebr-role-pair :deep(.ant-select){flex:1;min-width:0}.ebr-frozen-role{min-width:0;overflow-wrap:anywhere}.ebr-role-accessible-label{position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip-path:inset(50%);white-space:nowrap;border:0}
</style>
