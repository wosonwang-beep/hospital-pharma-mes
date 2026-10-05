<script setup lang="ts">
import {computed} from 'vue'
import {useRouter} from 'vue-router'
import {ExclamationCircleOutlined,CheckCircleOutlined} from '@ant-design/icons-vue'
import {useAuthStore} from '../../stores/auth'
import {type IncomingRow} from './incomingModel'
import {rows,inspectionSummaryRows,detailDisplay} from './incomingDetailModel'
const props=defineProps<{record:IncomingRow;request?:IncomingRow;lot?:IncomingRow;references:Record<string,{name:string;row:IncomingRow;route?:string}>;valueLabel:(key:string,value:unknown)=>string}>()
const emit=defineEmits<{viewItem:[IncomingRow]}>()
const router=useRouter(),auth=useAuthStore()
const items=computed(()=>inspectionSummaryRows(rows(props.record.items)))
const executions=computed(()=>rows(props.record.items).flatMap(i=>rows(i.executions)))
const analysts=computed(()=>[...new Set(executions.value.map(e=>String(e.performedBy)).filter(x=>x!=='undefined'&&x!=='null'))])
const investigations=computed(()=>Object.entries(props.references).filter(([key,value])=>['approvedInvestigationId','dispositionInvestigationId'].includes(key.split(':')[0]!)&&value.route).map(([,v])=>v).filter((v,i,all)=>all.findIndex(x=>x.route===v.route)===i))
function name(key:string,id:unknown){return id==null?'—':props.references[`${key}:${id}`]?.name??String(id)}
function number(value:unknown){return value==null||value===''?'—':String(value)}
function time(value:unknown){return value?String(value).replace('T',' ').replace(/\.\d+Z$|Z$/,''):'—'}
function open(key:string,id:unknown){const route=props.references[`${key}:${id}`]?.route;if(route)void router.push(route)}
const conclusion=computed(()=>props.record.status==='QC_FAILED'?'不合格':props.record.status==='QC_PASSED'?'合格':'尚未完成复核')
const reviewStatus=computed(()=>props.record.reviewedAt?'已复核':props.record.status==='PENDING_REVIEW'?'待复核':'未复核')
const columns=[{title:'序号',key:'index',width:64},{title:'检验项目',key:'item',width:170},{title:'标准要求',dataIndex:'standard',key:'standard'},{title:'实际结果',key:'result',width:170},{title:'单位',key:'unit',width:85},{title:'判定',key:'conclusion',width:130},{title:'操作',key:'action',width:110}]
</script>
<!-- T3 subordinate summary. Raw results and all revisions remain in the read-only evidence views. -->
<template>
 <a-card class="inspection-context form-section"><div class="context-grid">
  <section><h3>请验单</h3><a-button v-if="references[`inspectionRequestId:${record.inspectionRequestId}`]?.route" type="link" @click="open('inspectionRequestId',record.inspectionRequestId)">{{name('inspectionRequestId',record.inspectionRequestId)}}</a-button><strong v-else>{{number(record.inspectionRequestId)}}</strong><p>{{detailDisplay('requestType',request?.requestType)}}</p></section>
  <section><h3>样品</h3><a-button v-if="references[`sampleId:${record.sampleId}`]?.route" type="link" @click="open('sampleId',record.sampleId)">{{name('sampleId',record.sampleId)}}</a-button><strong v-else>{{number(record.sampleId)}}</strong><p>{{detailDisplay('sampleType',references[`sampleId:${record.sampleId}`]?.row.sampleType)}}</p></section>
  <section><h3>物料</h3><strong>{{(lot?.materialSnapshot as IncomingRow|undefined)?.materialName??'—'}}</strong><p>批号：{{lot?.lotNo??request?.materialLotId??'—'}}</p></section>
  <section><h3>检验标准</h3><strong>{{name('qcSpecificationVersionId',request?.qcSpecificationVersionId)}}</strong><p>请验时冻结版本</p></section>
  <section><h3>检验人</h3><strong>{{analysts.length?analysts.map(id=>name('performedBy',id)).join('、'):'—'}}</strong><p v-if="record.assignedTo">指派：{{name('assignedTo',record.assignedTo)}}</p></section>
  <section><h3>检验时间（UTC）</h3><template v-if="executions.length"><p v-for="e in executions" :key="String(e.id)" class="execution-time">{{time(e.startedAt)}} — {{time(e.completedAt)}}</p></template><strong v-else>—</strong></section>
 </div></a-card>
 <a-card class="form-section inspection-results"><template #title><span>检验项目与结果 <small>共 {{items.length}} 项</small></span></template>
  <a-table :columns="columns" :data-source="items" :row-key="(row:ReturnType<typeof inspectionSummaryRows>[number])=>String(row.item.id)" :pagination="false" :scroll="{x:850}" :row-class-name="(row:ReturnType<typeof inspectionSummaryRows>[number])=>row.results.some(r=>r.revision.resultConclusion==='FAIL')?'inspection-failed-row':''">
   <template #bodyCell="{column,record:row,index}">
    <span v-if="column.key==='index'">{{index+1}}</span><strong v-else-if="column.key==='item'">{{row.item.itemName??row.item.itemCode}}</strong>
    <template v-else-if="column.key==='result'"><span v-if="!row.results.length">暂无结果</span><div v-for="r in row.results" :key="String(r.execution.id)">{{row.results.length>1?`第${r.execution.attemptNo}次：`:''}}{{number(r.revision.resultNumeric??r.revision.resultText)}}</div></template>
    <span v-else-if="column.key==='unit'">{{name('unitId',row.item.unitId)}}</span>
    <template v-else-if="column.key==='conclusion'"><span v-if="!row.results.length">—</span><div v-for="r in row.results" :key="String(r.execution.id)"><a-tag :color="r.revision.resultConclusion==='FAIL'?'red':r.revision.resultConclusion==='PASS'?'green':'default'">{{detailDisplay('resultConclusion',r.revision.resultConclusion)}}</a-tag><small v-if="r.revision.effectiveConclusion!==r.revision.resultConclusion">{{detailDisplay('effectiveConclusion',r.revision.effectiveConclusion)}}</small></div></template>
    <a-button v-else-if="column.key==='action'" type="link" @click="emit('viewItem',row.item)">查看详情</a-button>
   </template>
   <template #emptyText>暂无检验项目</template>
  </a-table>
 </a-card>
 <a-card title="检验结论与复核" class="form-section"><div class="review-grid">
  <section class="current-conclusion"><ExclamationCircleOutlined v-if="record.status==='QC_FAILED'" class="conclusion-icon failed"/><CheckCircleOutlined v-else-if="record.status==='QC_PASSED'" class="conclusion-icon passed"/><div><h3>当前结论</h3><strong :class="{failed:record.status==='QC_FAILED',passed:record.status==='QC_PASSED'}">{{conclusion}}</strong></div></section>
  <section><h3>关联调查</h3><a-button v-for="i in investigations" :key="i.route" type="link" @click="router.push(i.route!)">{{i.name}}</a-button><span v-if="!investigations.length">—</span></section>
  <section><h3>复核状态</h3><a-tag :color="record.reviewedAt?'green':'blue'">{{reviewStatus}}</a-tag></section>
  <section><h3>复核人</h3><strong>{{name('reviewedBy',record.reviewedBy)}}</strong></section>
  <section><h3>复核时间（UTC）</h3><span>{{time(record.reviewedAt)}}</span></section>
 </div><p v-if="record.status==='QC_PASSED'" class="review-note">QC合格不代表物料已获QA放行。</p></a-card>
 <div v-if="auth.can('audit:view')||auth.can('trace:view')" class="compact-evidence-links"><a-button v-if="auth.can('audit:view')" type="link" @click="router.push({path:'/audit',query:{objectType:'qms_inspection_task',objectId:String(record.id)}})">审计追踪</a-button><a-button v-if="(record.materialLotId??request?.materialLotId)&&auth.can('trace:view')" type="link" @click="router.push({path:'/trace',query:{materialLotId:String(record.materialLotId??request?.materialLotId)}})">完整来料追溯</a-button></div>
</template>
<style scoped>
.inspection-results :deep(.ant-tag-red){background:#ffe7e7;color:#c72b30;border:0;border-radius:4px;padding:3px 12px;font-weight:600}.inspection-results :deep(.ant-tag-green){background:#e1f7e9;color:#23834b;border:0;border-radius:4px;padding:3px 12px;font-weight:600}.current-conclusion .conclusion-icon{padding:10px;background:#fff0f1;border-radius:50%;font-size:24px;box-sizing:content-box}.current-conclusion .conclusion-icon.passed{background:#edf9f0}.inspection-context :deep(.ant-card-body){padding:20px}.review-grid>section:nth-child(3){border-left:1px solid #edf1f6;padding-left:20px}
.context-grid{display:grid;grid-template-columns:1.1fr 1.1fr 1fr 1fr .8fr 1.2fr;gap:16px}.context-grid>section{min-width:0;border-right:1px solid #edf1f6;padding-right:12px}.context-grid>section:last-child{border:0;padding:0}h3{font-size:12px;font-weight:500;color:var(--mes-ui-secondary,#66758a);margin:0 0 8px}strong{font-size:13px;font-weight:600;overflow-wrap:anywhere}.context-grid p{font-size:12px;color:var(--mes-ui-secondary,#66758a);margin:8px 0 0;overflow-wrap:anywhere}.context-grid :deep(.ant-btn-link),.review-grid :deep(.ant-btn-link){padding:0;height:auto;white-space:normal;text-align:left;font-weight:600}.execution-time{color:#25344a!important}.inspection-results small{font-size:12px;color:#66758a;font-weight:400;margin-left:12px}.inspection-results :deep(.inspection-failed-row>td){background:#fff5f5}.inspection-results :deep(.ant-table-cell){font-size:13px;padding:12px 16px}.inspection-results :deep(.ant-tag){margin:0}.inspection-results :deep(td small){display:block;font-size:11px;margin:4px 0 0}.review-grid{display:grid;grid-template-columns:1.1fr 1.5fr 1fr 1fr 1.4fr;gap:20px;align-items:start}.current-conclusion{display:flex;gap:14px;align-items:center}.conclusion-icon{font-size:24px}.failed{color:#cf252e}.passed{color:#248b52}.review-note{font-size:12px;color:#66758a;margin:16px 0 0}.compact-evidence-links{text-align:right;margin:-8px 0 8px}@media(max-width:1100px){.context-grid{grid-template-columns:repeat(3,minmax(0,1fr))}.context-grid>section:nth-child(3){border:0}}@media(max-width:767px){.context-grid,.review-grid{grid-template-columns:repeat(2,minmax(0,1fr));gap:16px}.context-grid>section{border:0;padding:0}.inspection-results :deep(.ant-table-cell){padding:10px 12px}.review-grid>section:last-child{grid-column:1/-1}.compact-evidence-links{text-align:left}}
</style>
