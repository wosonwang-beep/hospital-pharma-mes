<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api} from '../../api/http'
import {useControlledRequest} from '../../api/controlled'
import {useAuthStore} from '../../stores/auth'
import {type IncomingRow} from './incomingModel'
import {qualityOperations,qualityActionLabels,permits} from './productionQualityModel'
import QualityCommandForm from './QualityCommandForm.vue'
import IncomingFacts from './IncomingFacts.vue'
const route=useRoute(),router=useRouter(),auth=useAuthStore(),request=useControlledRequest(),{error}=request,record=ref<IncomingRow>({}),selected=ref('')
const plan=computed(()=>route.meta.resource==='production-plans'),base=computed(()=>`/quality/${route.meta.resource}`),title=computed(()=>plan.value?'生产质量计划':'生产检验'),create=computed(()=>route.meta.mode==='create'),edit=computed(()=>route.meta.mode==='edit'),formMode=computed(()=>create.value||edit.value||selected.value)
const op=computed(()=>qualityOperations.find(o=>o.method===(edit.value?'PUT':'POST')&&o.path===base.value+(create.value?'':'/{id}')+(selected.value?'/'+selected.value:'')))
const actions=computed(()=>(plan.value?['approve']:['results','revisions','review','retest']).filter(a=>{const o=qualityOperations.find(o=>o.path===`${base.value}/{id}/${a}`);return o&&auth.can(o.permission)&&permits(record.value,a)}))
const initial=computed(()=>create.value?{mainBatchId:route.query.mainBatchId??'',...(plan.value?{balanceRules:[{basis:'BATCH',checkPoint:'BATCH_COMPLETE',formulaExpr:{dslVersion:1,metric:'DIFFERENCE_PCT',expected:{sum:['CHARGE']},actual:{sum:['OUTPUT']}}}]}:{})}:edit.value?record.value:{...(selected.value==='revisions'?{previousRevisionId:record.value.currentResultRevisionId}:{}),...(selected.value==='review'?{resultRevisionId:record.value.currentResultRevisionId}:{})})
const hiddenFields=computed(()=>{const snap=record.value.specificationSnapshot as IncomingRow|undefined;const item=(snap?.item??snap) as IncomingRow|undefined;return ['results','revisions'].includes(selected.value)?[item?.resultType==='NUMERIC'?'resultText':'resultNumeric']:[]})
const signatureTarget=computed(()=>({objectType:plan.value?'PRODUCTION_QUALITY_PLAN':selected.value==='review'?'PRODUCTION_TEST_REVIEW':'PRODUCTION_TEST_RESULT',objectId:plan.value?String(record.value.id):`${record.value.id}:${record.value.versionNo}`,meaning:plan.value?'APPROVE':'VERIFY'}))
async function load(){request.clear();try{record.value=create.value?{}:await api<IncomingRow>({url:base.value+'/'+route.params.id})}catch(e){await request.failure(e)}}
async function saved(row:IncomingRow){selected.value='';if(create.value||edit.value)await router.replace(base.value+'/'+row.id);else await load()}
watch(()=>route.fullPath,()=>{selected.value='';void load()},{immediate:true})
</script>
<template><main class="admin-page master-page"><header class="admin-page-header"><div><a-button type="link" @click="router.push(base)">← 返回列表</a-button><h1>{{create?'新增':edit?'编辑':''}}{{title}}</h1></div><a-space wrap><a-button v-if="plan&&!create&&!edit&&auth.can('qms:plan:update')&&permits(record,'UPDATE')" @click="router.push(base+'/'+record.id+'/edit')">编辑</a-button><a-button v-for="action in actions" :key="action" @click="selected=action">{{qualityActionLabels[action]}}</a-button></a-space></header><a-alert v-if="error" :message="error" type="error"/><a-card v-if="!create&&!edit" :title="title+'详情'" class="form-section"><IncomingFacts :value="record"/></a-card><a-card v-if="formMode&&op" :title="selected?qualityActionLabels[selected]:title+'信息'"><QualityCommandForm :key="op.schema+route.fullPath" :schema-name="op.schema" :url="base+(create?'':'/'+route.params.id)+(selected?'/'+selected:'')" :method="edit?'PUT':'POST'" :source="{...record,investigationScope:'PRODUCTION'}" :initial-value="initial" :signature-target="signatureTarget" :hidden-fields="hiddenFields" :locked-fields="selected==='review'?['resultRevisionId']:selected==='revisions'?['previousRevisionId']:[]" @saved="saved" @cancel="selected='';(create||edit)&&router.push(base+(edit?'/'+route.params.id:''))"/></a-card></main></template>
