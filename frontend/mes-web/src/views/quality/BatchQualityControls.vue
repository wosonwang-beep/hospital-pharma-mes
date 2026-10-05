<script setup lang="ts">
import {ref,watch,computed} from 'vue'
import {useRouter} from 'vue-router'
import {api,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {useControlledRequest} from '../../api/controlled'
import {display,type IncomingRow} from './incomingModel'
import {qualityOperations} from './productionQualityModel'
import QualityCommandForm from './QualityCommandForm.vue'
import IncomingFacts from './IncomingFacts.vue'
const props=defineProps<{batch:IncomingRow}>(),emit=defineEmits<{refresh:[]}>(),router=useRouter(),auth=useAuthStore(),request=useControlledRequest(),{error}=request,plans=ref<IncomingRow[]>([]),events=ref<IncomingRow[]>([]),selected=ref<IncomingRow|null>(null),recording=ref(false),eventPage=ref(0),hasMore=ref(false)
async function load(){request.clear();try{if(auth.can('qms:plan:view'))plans.value=(await api<Page<IncomingRow>>({url:'/quality/production-plans',params:{mainBatchId:props.batch.id,page:0,size:20}})).items;if(auth.can('balance:view')){eventPage.value=0;await loadEvents()}}catch(e){await request.failure(e)}}
async function loadEvents(append=false){try{const data=await api<Page<IncomingRow>>({url:`/main-batches/${props.batch.id}/quantity-events`,params:{page:eventPage.value,size:100}});events.value=append?[...events.value,...data.items]:data.items;hasMore.value=(eventPage.value+1)*100<data.total}catch(e){await request.failure(e)}}
const mutable=computed(()=>props.batch.status==='IN_PROGRESS')
const schema=computed(()=>qualityOperations.find(o=>o.path===(selected.value?'/quantity-events/{id}/reverse':'/main-batches/{id}/quantity-events')&&o.method==='POST')!.schema)
const target=computed(()=>({objectType:'PRODUCTION_QUANTITY',objectId:`${props.batch.id}:${props.batch.versionNo}`,meaning:'VERIFY'}))
watch(()=>[props.batch.id,props.batch.versionNo],()=>void load(),{immediate:true})
</script>
<template><a-alert v-if="error" type="error" :message="error"/><a-card v-if="auth.can('qms:plan:view')" title="生产质量计划" class="form-section"><a-space wrap><a-button v-for="p in plans" :key="String(p.id)" @click="router.push('/quality/production-plans/'+p.id)">查看质量计划 {{p.id}}</a-button><a-button v-if="!plans.length&&batch.status==='DRAFT'&&auth.can('qms:plan:create')" @click="router.push({path:'/quality/production-plans/create',query:{mainBatchId:String(batch.id)}})">制定质量计划</a-button><a-button v-if="auth.can('balance:view')" @click="router.push('/production/batches/'+batch.id+'/balance')">物料平衡</a-button></a-space><p v-if="!plans.length">尚未建立生产质量计划。</p></a-card><a-card v-if="auth.can('balance:view')" title="产量与数量事实" class="form-section"><a-button v-if="mutable&&auth.can('mes:quantity:record')" @click="recording=true;selected=null">记录产量与损耗</a-button><p v-if="!events.length">暂无数量记录。</p><details v-for="e in events" :key="String(e.id)"><summary>{{display(e.eventType)}} · {{e.amount}} · {{e.sourceRef}}</summary><IncomingFacts :value="e"/><a-button v-if="mutable&&auth.can('mes:quantity:reverse')&&e.sourceType==='PRODUCTION_OBSERVATION'&&!e.reversalOfId&&['OUTPUT','SAMPLE','LOSS','SCRAP','WIP'].includes(String(e.eventType))&&!events.some(x=>x.reversalOfId===e.id)" @click="selected=e;recording=true">冲销数量记录</a-button></details><a-button v-if="hasMore" @click="eventPage++;loadEvents(true)">更多数量记录</a-button><QualityCommandForm v-if="recording" :key="String(selected?.id??'new')" :schema-name="schema" :source="{...batch,mainBatchId:batch.id}" :url="selected?'/quantity-events/'+selected.id+'/reverse':'/main-batches/'+batch.id+'/quantity-events'" :signature-target="target" @saved="recording=false;selected=null;emit('refresh');load()" @cancel="recording=false;selected=null"/></a-card></template>
<style scoped>details{padding:12px 0;border-bottom:1px solid #e8e8e8}summary{cursor:pointer}form{margin-top:16px}</style>
