<script setup lang="ts">
import {computed} from 'vue'
import {Select as ASelect} from 'ant-design-vue'
import type {EbrRoleOption} from './types'
const props=defineProps<{roleId?:string|null;legacyCode?:string|null;codeSnapshot?:string|null;nameSnapshot?:string|null;editable:boolean;options:EbrRoleOption[];loading?:boolean}>()
const emit=defineEmits<{'update:roleId':[string];search:[string]}>()
const choices=computed(()=>props.options.filter(r=>r.enabled).map(r=>({value:r.id,label:`${r.name}（${r.code}）`})))
const frozenLabel=computed(()=>props.nameSnapshot?`${props.nameSnapshot}（${props.codeSnapshot??''}）`:props.legacyCode??props.codeSnapshot??'未配置')
</script>
<template>
  <label class="ebr-role-pair">
    <span class="ebr-role-label">角色</span>
    <a-select v-if="editable" :value="roleId??undefined" :options="choices" :loading="loading" size="small" show-search :filter-option="false" aria-label="签署或复核角色" placeholder="请选择角色" @search="emit('search',$event)" @change="emit('update:roleId',String($event))"/>
    <span v-else class="ebr-frozen-role" data-testid="frozen-role">{{frozenLabel}}</span>
  </label>
</template>
<style scoped>
.ebr-role-pair{display:flex;align-items:center;gap:8px;min-width:0;flex-wrap:nowrap}.ebr-role-label{flex:none}.ebr-role-pair :deep(.ant-select){flex:1;min-width:0}.ebr-frozen-role{min-width:0;overflow-wrap:anywhere}
</style>
