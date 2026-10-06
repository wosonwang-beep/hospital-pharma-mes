<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {useRouter} from 'vue-router'
import {api} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import IncomingFacts from '../quality/IncomingFacts.vue'
import {detailLabel,selectFacts,rows} from '../quality/incomingDetailModel'
import {display,type IncomingRow} from '../quality/incomingModel'
const props=defineProps<{record:IncomingRow;executions:IncomingRow[];reservations:IncomingRow[]}>(),router=useRouter(),auth=useAuthStore()
const references=ref<Record<string,IncomingRow>>({}),operations=ref<IncomingRow[]>([]),notices=ref<string[]>([])
let generation=0
watch(()=>[props.record,props.executions],async()=>{
 const ticket=++generation;references.value={};operations.value=[];notices.value=[]
 const refs:Record<string,IncomingRow>={},ops:IncomingRow[]=[],warnings:string[]=[]
 const lookup=async(key:string,id:unknown,url:string,permission:string)=>{if(id==null||!auth.can(permission))return;try{refs[key]=await api<IncomingRow>({url:`${url}/${id}`})}catch{warnings.push('关联信息暂不可读取，保留来源引用。')}}
 await Promise.all([lookup('productId',props.record.productId,'/products','master:product:view'),lookup('productionOrderId',props.record.productionOrderId,'/production-orders','production:order:view'),lookup('unitId',props.record.unitId,'/units','master:uom:view'),...props.executions.map(async e=>{if(!auth.can('mes:operation:view'))return;try{const result=await api<IncomingRow[]>({url:`/execution-units/${e.id}/operations`});ops.push(...rows(result).map(o=>({...o,executionNo:e.executionNo})))}catch{warnings.push('工序进度暂不可读取。')}})])
 if(ticket===generation){references.value=refs;operations.value=ops;notices.value=[...new Set(warnings)]}
},{immediate:true})
const snapshot=computed(()=>((props.record.processSnapshot as IncomingRow|undefined)?.snapshot??{}) as IncomingRow)
const definition=computed(()=>rows(((snapshot.value.process as IncomingRow|undefined)?.route as IncomingRow|undefined)?.operations))
function value(key:string,v:unknown){const r=references.value[key];return r?String(r.productName??r.orderNo??r.unitName??r.id):display(v)}
function businessLabel(key:string){return ({subBatches:'子批计划',reservedQty:'预留数量',quantity:'数量'} as Record<string,string>)[key]??detailLabel(key)}
function operationName(r:IncomingRow){return definition.value.find(d=>String(d.operationDefId)===String(r.operationDefId))?.operationName??r.operationName??`工序 ${r.operationSeq??'—'}`}
function color(status:unknown){return ['COMPLETED','QA_RELEASED'].includes(String(status))?'green':status==='REJECTED'?'red':'blue'}
</script>
<!-- UI Template: T4. Bounded read-only views; writes stay with ProductionDetailView. -->
<template><section class="batch-overview" data-ui-template="T4">
 <a-card title="批次摘要" class="form-section"><IncomingFacts :value="selectFacts(record,['batchNo','status','productId','plannedQty','unitId','plannedDate','productionOrderId','releasedAt','startedAt','completedAt'])" :label-formatter="businessLabel" :value-formatter="value"/></a-card>
 <a-card class="form-section"><a-tabs>
  <a-tab-pane key="execution" tab="执行与工序"><h3>执行单元</h3><a-table :data-source="executions" row-key="id" :pagination="false" :scroll="{x:480}" :columns="[{title:'执行单元',dataIndex:'executionNo'},{title:'类型',dataIndex:'unitType'},{title:'状态',dataIndex:'status'},{title:'操作',key:'action'}]"><template #bodyCell="{column,record:r}"><a-button v-if="column.key==='action'&&auth.can('mes:operation:view')" type="link" @click="router.push(`/mes/execution/${r.id}`)">进入执行</a-button><a-tag v-else-if="column.dataIndex==='status'" :color="color(r.status)">{{r.status==='PENDING'?'待开始':display(r.status)}}</a-tag><span v-else>{{display(r[column.dataIndex])}}</span></template><template #emptyText>暂无执行单元</template></a-table>
   <h3>工序进度</h3><a-table :data-source="operations" row-key="id" :pagination="false" :scroll="{x:650}" :columns="[{title:'执行单元',dataIndex:'executionNo'},{title:'工序',key:'name'},{title:'状态',dataIndex:'status'},{title:'开始时间',dataIndex:'startedAt'},{title:'完成时间',dataIndex:'completedAt'}]"><template #bodyCell="{column,record:r}"><span v-if="column.key==='name'">{{operationName(r)}}</span><a-tag v-else-if="column.dataIndex==='status'" :color="color(r.status)">{{r.status==='PENDING'?'待开始':display(r.status)}}</a-tag><span v-else>{{display(r[column.dataIndex])}}</span></template><template #emptyText>暂无可读取的工序进度</template></a-table>
   <h3>子批计划</h3><IncomingFacts :value="{subBatches:rows(record.subBatches).map(r=>selectFacts(r,['subBatchNo','plannedQty','status']))}" :label-formatter="businessLabel"/>
  </a-tab-pane>
  <a-tab-pane key="materials" tab="物料准备"><a-empty v-if="!reservations.length" description="暂无物料预留"/><IncomingFacts v-for="r in reservations" :key="String(r.id)" :value="selectFacts(r,['materialLotId','reservedQty','quantity','unitId','status'])" :label-formatter="businessLabel"/></a-tab-pane>
  <a-tab-pane key="quality" tab="质量与物料平衡"><slot name="quality"/></a-tab-pane>
  <a-tab-pane key="release" tab="QA放行"><p>生产完成与QA放行是独立受控阶段。</p><a-tag :color="color(record.status)">{{display(record.status)}}</a-tag><a-button v-if="auth.can('qa:batch-review')" @click="router.push(`/qa/batches/${record.id}/review`)">QA 批审</a-button></a-tab-pane>
  <a-tab-pane key="snapshot" tab="冻结工艺与证据"><details><summary>查看冻结工艺快照</summary><IncomingFacts :value="{processSnapshot:record.processSnapshot}" :label-formatter="businessLabel"/></details><a-space wrap><a-button v-if="auth.can('trace:view')" @click="router.push({path:'/trace',query:{mainBatchId:String(record.id)}})">完整追溯</a-button><a-button v-if="auth.can('audit:view')" @click="router.push({path:'/audit',query:{objectType:'MainBatch',objectId:String(record.id)}})">审计追踪</a-button></a-space></a-tab-pane>
 </a-tabs><a-alert v-for="notice in notices" :key="notice" :message="notice" type="info"/></a-card>
</section></template>
<style scoped>.batch-overview{min-width:0}h3{font-size:15px;font-weight:600;margin:16px 0 12px}h3:first-child{margin-top:0}details{margin-bottom:16px}summary{cursor:pointer;font-weight:600}.ant-table-wrapper{margin-bottom:16px}</style>
