<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { useControlledRequest } from '../../api/controlled'
import IncomingFacts from '../quality/IncomingFacts.vue'
import IncomingReferencePicker from '../quality/IncomingReferencePicker.vue'
import ReadReference from '../master/ReadReference.vue'
import ProcessLookup from '../process/ProcessLookup.vue'
import { useAuthStore } from '../../stores/auth'
import { display } from '../quality/incomingModel'
import { traceDestination, traceTypeNames as typeNames, type TraceNode } from './tracePresentation'
import { traceRelationLabel } from './traceRelationLabels'

interface Edge { sourceType:string;sourceId:string;targetType:string;targetId:string;relation:string }
type Node = Record<string,unknown>
const auth=useAuthStore(),route=useRoute(),router=useRouter()
const request=useControlledRequest(),{busy,error}=request
const mainBatchId=ref(String(route.query.mainBatchId??''))
const materialLotId=ref(String(route.query.materialLotId??''))
const productId=ref<string|null>(null),materialId=ref<string|null>(null)
const result=ref<{nodes:Node[];edges:Edge[]}|null>(null)
const selected=ref<Node|null>(null),keyword=ref(''),nodeType=ref(''),detailsOpen=ref(false),visibleLimit=ref(18)

async function search(){
 busy.value=true;request.clear();selected.value=null
 try{
  if(Boolean(mainBatchId.value.trim())===Boolean(materialLotId.value.trim()))throw Error('请选择生产批或物料批中的一个进行追溯')
  result.value=await api({url:'/trace',params:{mainBatchId:mainBatchId.value||undefined,materialLotId:materialLotId.value||undefined}})
 }catch(e){result.value=null;await request.failure(e)}finally{busy.value=false}
}
const nodes=computed(()=>result.value?.nodes??[])
const edges=computed(()=>result.value?.edges??[])
function key(n:Node){return String(n.type)+':'+String(n.id)}
function actualFinishedLot(id:string){return edges.value.some(e=>e.sourceType==='MAIN_BATCH'&&e.targetType==='MATERIAL_LOT'&&e.targetId===id)}
function isFinishedDecision(id:string){return edges.value.some(e=>e.sourceType==='MATERIAL_LOT'&&e.targetType==='RELEASE_DECISION'&&e.targetId===id&&actualFinishedLot(e.sourceId))}
const stages=[
 {key:'incoming',title:'来料与供应商',hint:'供应商、收货、来料检验与物料放行',types:['SUPPLIER','RECEIPT','RECEIPT_ITEM','INSPECTION_REQUEST','SAMPLING_TASK','SAMPLING_DETAIL','INSPECTION_TASK','INSPECTION_ITEM','TEST_EXECUTION','REPORT','QC_SPECIFICATION_VERSION']},
 {key:'material',title:'领料与投料',hint:'出库、称量、实际投料与生产准备',types:['MATERIAL_REQUEST','MATERIAL_ISSUE','MATERIAL_ISSUE_ITEM','ISSUE_RETURN','WEIGHING','CHARGE']},
 {key:'production',title:'生产执行',hint:'正式批次、执行单元与实际工序',types:['MAIN_BATCH','EXECUTION_UNIT','OPERATION']},
 {key:'finished',title:'成品检验',hint:'成品批、入库接收、取样、检验与报告',types:['FINISHED_INBOUND_REQUEST','FINISHED_INSPECTION_REQUEST','FINISHED_SAMPLING_RECORD','SAMPLE','PRODUCTION_TEST','RESULT_REVISION','RESULT_REVIEW','FINISHED_REPORT','FINISHED_REPORT_REVIEW']},
 {key:'quality',title:'QA 放行与库存',hint:'独立质量决定与真实库存流水',types:['INVENTORY_LEDGER','INVENTORY_DECISION']},
 {key:'shipment',title:'成品发货',hint:'已确认发货及上下游追溯证据',types:['FINISHED_SHIPMENT']}
]
function stageOf(n:Node){
 const type=String(n.type),id=String(n.id)
 if(type==='MATERIAL_LOT')return actualFinishedLot(id)?'finished':'incoming'
 if(type==='RELEASE_DECISION')return isFinishedDecision(id)?'quality':'incoming'
 return stages.find(s=>s.types.includes(type))?.key
}
const stageData=computed(()=>stages.map((stage,index)=>{
 const records=nodes.value.filter(n=>stageOf(n)===stage.key)
 const priorities:Record<string,string[]>={incoming:['SUPPLIER','MATERIAL_LOT'],material:['CHARGE','MATERIAL_ISSUE'],production:['MAIN_BATCH'],finished:['FINISHED_REPORT','MATERIAL_LOT'],quality:['RELEASE_DECISION'],shipment:['FINISHED_SHIPMENT']}
 const featured=priorities[stage.key]?.map(t=>records.find(n=>n.type===t)).find(Boolean)??records[0]
 return {...stage,index,count:records.length,featured}
}))
const referenceTypes:Record<string,string>={
 MAIN_BATCH:'mainBatchId',MATERIAL_LOT:'materialLotId',RECEIPT:'receiptId',
 MATERIAL_ISSUE:'materialIssueId',EXECUTION_UNIT:'executionUnitId',
 FINISHED_INBOUND_REQUEST:'inboundRequestId',
 FINISHED_INSPECTION_REQUEST:'finishedInspectionRequestId',
 FINISHED_SAMPLING_RECORD:'finishedSamplingRecordId',
 FINISHED_REPORT:'finishedReportId',FINISHED_SHIPMENT:'finishedShipmentId',
 INSPECTION_REQUEST:'inspectionRequestId',INSPECTION_TASK:'inspectionTaskId',REPORT:'inspectionReportId'
}
function referenceField(n:Node){return referenceTypes[String(n.type)]}
function nodeTitle(n:Node){
 const type=String(n.type),raw=String(n.label??'').trim(),fallback=(typeNames[type]??type)+' · '+String(n.id)
 return !raw||/^(main batch|material lot|charge|signature|[A-Z_]+)\s+[0-9]+/i.test(raw)?fallback:raw
}
function featureTitle(n:Node){
 if(n.type==='RELEASE_DECISION')return '质量决定：'+display(n.status)
 if(n.type==='CHARGE')return n.status==='CONFIRMED'?'本批实际投料已确认':'本批投料记录'
 return nodeTitle(n)
}
function destination(n:Node){return traceDestination(n as unknown as TraceNode,edges.value,p=>auth.can(p))}
const filteredNodes=computed(()=>nodes.value.filter(n=>(!nodeType.value||n.type===nodeType.value)&&(!keyword.value||nodeTitle(n).includes(keyword.value)||String(n.id).includes(keyword.value))))
const visibleNodes=computed(()=>filteredNodes.value.slice(0,visibleLimit.value))
watch([nodeType,keyword],()=>{visibleLimit.value=18;selected.value=null})
const positions=computed(()=>new Map(visibleNodes.value.map((n,i)=>[key(n),{x:(i%3)*270+16,y:Math.floor(i/3)*150+16}])))
const lines=computed(()=>edges.value.flatMap(e=>{
 const a=positions.value.get(e.sourceType+':'+e.sourceId),b=positions.value.get(e.targetType+':'+e.targetId)
 return a&&b?[{...e,a,b}]:[]
}))
const graphHeight=computed(()=>Math.max(170,Math.ceil(visibleNodes.value.length/3)*150+16))
const selectedEdges=computed(()=>edges.value.filter(e=>selected.value&&(e.sourceType+':'+e.sourceId===key(selected.value)||e.targetType+':'+e.targetId===key(selected.value))))
function toggleDetails(event:Event){detailsOpen.value=(event.target as HTMLDetailsElement).open}
function gotoSource(){if(selected.value){const path=destination(selected.value);if(path)void router.push(path)}}
onMounted(()=>{if(mainBatchId.value||materialLotId.value)void search()})
</script>

<template>
 <main class="admin-page master-page trace-page" data-ui-template="T4">
  <header class="admin-page-header"><div><h1>完整追溯</h1></div><a-button v-if="materialLotId&&auth.can('wms:inventory:view')" @click="router.push('/wms/material-lots/'+materialLotId)">物料批详情</a-button></header>
  <a-alert v-if="error" type="error" :message="error" show-icon/>
  <a-card class="query-card" title="追溯条件"><form class="query-form" @submit.prevent="search">
   <label v-if="auth.can('production:batch:view')&&auth.can('master:product:view')"><span class="form-field-label">产品</span><ProcessLookup v-model="productId" resource="products" label="追溯产品" allow-clear @update:model-value="mainBatchId=''"/></label>
   <label><span class="form-field-label">生产批</span><IncomingReferencePicker v-if="auth.can('production:batch:view')" v-model="mainBatchId" field="mainBatchId" :context="{}" :query-filters="{productId}"/><input v-else v-model="mainBatchId" class="master-native-input" aria-label="生产批"/></label>
   <label v-if="auth.can('wms:inventory:view')&&auth.can('master:material:view')"><span class="form-field-label">物料</span><ProcessLookup v-model="materialId" resource="materials" label="追溯物料" allow-clear @update:model-value="materialLotId=''"/></label>
   <label><span class="form-field-label">物料批次</span><IncomingReferencePicker v-if="auth.can('wms:inventory:view')" v-model="materialLotId" field="materialLotId" :context="{}" :query-filters="{materialId}"/><input v-else v-model="materialLotId" class="master-native-input" aria-label="物料批次"/></label>
   <a-space><a-button type="primary" html-type="submit" :loading="busy">查询追溯</a-button></a-space>
  </form></a-card>

  <a-card v-if="result" title="业务环节与真实证据" class="trace-results">
   <div class="trace-overview-intro"><strong>{{nodes.length}} 条真实记录 · {{edges.length}} 条实际关系</strong><span>按业务环节展示，只统计已返回证据；具体上下游关系以完整关系图为准。</span></div>
   <div class="trace-stages">
    <article v-for="stage in stageData" :key="stage.key" class="trace-stage">
     <div class="trace-stage-heading"><span class="trace-stage-number">{{String(stage.index+1).padStart(2,'0')}}</span><strong>{{stage.title}}</strong></div>
     <div class="trace-stage-count">{{stage.count}} <small>条业务证据</small></div>
     <p>{{stage.hint}}</p>
     <div v-if="stage.featured" class="trace-stage-example">
      <ReadReference v-if="referenceField(stage.featured)" :field="referenceField(stage.featured)??''" :value="stage.featured.id" :fallback="featureTitle(stage.featured)"/>
      <span v-else>{{featureTitle(stage.featured)}}</span>
     </div>
     <div v-else class="trace-stage-example">本次追溯暂无该环节记录</div>
    </article>
   </div>
   <p v-if="nodes.some(n=>n.type==='SIGNATURE')" class="trace-proof-note">另外 {{nodes.filter(n=>n.type==='SIGNATURE').length}} 条电子签名证据保留于完整关系图和原始证据中，不混入业务环节数量。</p>
   <details class="trace-explorer" @toggle="toggleDetails">
    <summary>展开完整证据关系图（{{nodes.length}} 条记录、{{edges.length}} 条关系）</summary>
    <div v-if="detailsOpen">
     <div class="trace-filters">
      <label>记录类型<a-select show-search option-filter-prop="children" v-model:value="nodeType" class="master-native-input"><a-select-option value="">全部</a-select-option><a-select-option v-for="type in [...new Set(nodes.map(n=>String(n.type)))]" :key="type" :value="type">{{typeNames[type]??type}}</a-select-option></a-select></label>
      <label>编号或名称<input v-model="keyword" class="master-native-input" aria-label="追溯记录过滤"/></label>
      <span class="muted">当前显示 {{visibleNodes.length}} / {{filteredNodes.length}} 个节点、{{lines.length}} 条可见关系</span>
     </div>
     <p v-if="filteredNodes.length===0" class="muted">此条件下暂无关联记录。</p>
     <a-button v-if="visibleNodes.length<filteredNodes.length" class="trace-load-more" @click="visibleLimit+=18">继续展开节点（剩余 {{filteredNodes.length-visibleNodes.length}} 条）</a-button>
     <div v-if="visibleNodes.length" class="trace-scroll">
      <div class="trace-canvas" :style="{height:graphHeight+'px'}">
       <svg width="810" :height="graphHeight" aria-label="业务关联图">
        <defs><marker id="trace-arrow" markerWidth="8" markerHeight="8" refX="7" refY="4" orient="auto"><path d="M0 0 L8 4 L0 8" fill="var(--mes-ui-primary)"/></marker></defs>
        <path v-for="(line,i) in lines" :key="i" :d="'M'+(line.a.x+120)+','+(line.a.y+105)+' C'+(line.a.x+120)+','+(line.a.y+135)+' '+(line.b.x+120)+','+(line.b.y-25)+' '+(line.b.x+120)+','+line.b.y" fill="none" stroke="var(--mes-ui-primary)" stroke-width="1.5" marker-end="url(#trace-arrow)"/>
       </svg>
       <button v-for="node in visibleNodes" :key="key(node)" :aria-label="nodeTitle(node)" class="trace-node" :style="{left:positions.get(key(node))!.x+'px',top:positions.get(key(node))!.y+'px'}" @click="selected=node">
        <small>{{typeNames[String(node.type)]??node.type}}</small>
        <strong><ReadReference v-if="referenceField(node)" :field="referenceField(node)??''" :value="node.id" :fallback="nodeTitle(node)"/><span v-else>{{nodeTitle(node)}}</span></strong>
        <span>{{display(node.status)}}</span>
       </button>
      </div>
     </div>
     <details class="trace-evidence-codes"><summary>查看原始关系编码与完整证据</summary><IncomingFacts :value="result"/></details>
    </div>
   </details>
   <p v-if="!nodes.length" class="muted">当前范围暂无实际关联记录。</p>
  </a-card>
  <a-drawer :open="!!selected" title="来源记录详情" :width="480" @close="selected=null">
   <template v-if="selected">
    <h3><ReadReference v-if="referenceField(selected)" :field="referenceField(selected)??''" :value="selected.id" :fallback="nodeTitle(selected)"/><span v-else>{{nodeTitle(selected)}}</span></h3>
    <dl><dt>记录类型</dt><dd>{{typeNames[String(selected.type)]??selected.type}}</dd><dt>业务状态</dt><dd>{{display(selected.status)}}</dd></dl>
    <a-button v-if="destination(selected)" type="primary" @click="gotoSource">查看来源记录</a-button><p v-else class="muted">当前无可访问的来源详情入口。</p>
    <section class="trace-drawer-relations"><h3>关联关系</h3><p v-for="(edge,i) in selectedEdges" :key="i">{{typeNames[edge.sourceType]??edge.sourceType}} → {{typeNames[edge.targetType]??edge.targetType}}：{{traceRelationLabel(edge.relation)}}</p><p v-if="!selectedEdges.length" class="muted">没有直接关联的上下游记录。</p></section>
    <details class="trace-evidence-codes"><summary>原始来源引用与版本证据</summary><IncomingFacts :value="selected"/></details>
   </template>
  </a-drawer>
 </main>
</template>

<style scoped>
.trace-page .query-form .process-lookup{min-width:0;flex:1}
.trace-page .query-form :deep(.process-lookup .ant-select){width:100%}
.trace-overview-intro{display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap;margin-bottom:18px;font-size:13px;color:var(--mes-ui-secondary)}
.trace-overview-intro strong{font-size:14px;font-weight:600;color:var(--mes-ui-text)}
.trace-stages{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:14px}
.trace-stage{border:1px solid var(--mes-ui-border);border-radius:8px;padding:16px;background:var(--mes-ui-surface);min-width:0}
.trace-stage-heading{display:flex;align-items:center;gap:10px;color:var(--mes-ui-text);font-size:14px}
.trace-stage-number{display:grid;place-items:center;width:30px;height:30px;border-radius:8px;background:var(--mes-ui-background);color:var(--mes-ui-primary);font-weight:650}
.trace-stage-count{margin:14px 0 4px;font-size:24px;font-weight:650;color:var(--mes-ui-text);line-height:1.2}
.trace-stage-count small{font-weight:400;font-size:12px;color:var(--mes-ui-secondary)}
.trace-stage p{font-size:12px;line-height:1.6;color:var(--mes-ui-secondary);min-height:38px;margin:0}
.trace-stage-example{border-top:1px solid var(--mes-ui-border);margin-top:12px;padding-top:10px;min-height:28px;font-size:13px;color:var(--mes-ui-text);white-space:nowrap;text-overflow:ellipsis;overflow:hidden}
.trace-proof-note{margin:14px 0 0;font-size:12px;color:var(--mes-ui-secondary)}.trace-explorer{margin-top:20px;border-top:1px solid var(--mes-ui-border);padding-top:14px}
.trace-explorer>summary{cursor:pointer;font-size:13px;font-weight:600;color:var(--mes-ui-primary);padding:4px 0}
.trace-filters{display:flex;gap:16px;align-items:center;flex-wrap:wrap;margin:16px 0}
.trace-filters label{display:flex;gap:8px;align-items:center;font-size:13px}
 .trace-load-more{margin-bottom:12px}.trace-scroll{overflow:auto;max-height:650px;border:1px solid var(--mes-ui-border);border-radius:8px;background:var(--mes-ui-background)}
.trace-canvas{position:relative;min-width:810px}
.trace-canvas svg{position:absolute;inset:0;pointer-events:none}
.trace-node{position:absolute;width:240px;height:105px;border:1px solid var(--mes-ui-border);border-radius:8px;background:var(--mes-ui-surface);text-align:left;padding:12px;cursor:pointer;color:var(--mes-ui-text)}
.trace-node:hover,.trace-node:focus-visible{border-color:var(--mes-ui-primary);outline:2px solid #dbeafe}
.trace-node strong{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:14px;margin:8px 0}
.trace-node small,.trace-node span{color:var(--mes-ui-secondary);font-size:12px}
.trace-drawer-relations{margin:20px 0 12px;padding-top:14px;border-top:1px solid var(--mes-ui-border)}.trace-drawer-relations h3{font-size:14px;margin-bottom:10px}.trace-drawer-relations p{font-size:13px;line-height:1.7;margin:0 0 9px;color:var(--mes-ui-secondary)}.trace-evidence-codes{margin-top:12px;font-size:12px;color:var(--mes-ui-secondary)}
.trace-evidence-codes summary{cursor:pointer}
.muted{color:var(--mes-ui-secondary);font-size:12px}
dd{margin:4px 0 16px;overflow-wrap:anywhere}
dt{color:var(--mes-ui-secondary)}
@media(max-width:1000px){.trace-stages{grid-template-columns:repeat(2,minmax(0,1fr))}}
@media(max-width:767px){.trace-stages{grid-template-columns:1fr}.trace-filters label{width:100%}.trace-filters input,.trace-filters select{flex:1;min-width:0}}
</style>
