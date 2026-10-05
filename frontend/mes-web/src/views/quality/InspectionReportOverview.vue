<script setup lang="ts">
import {computed} from 'vue'
import {useRouter} from 'vue-router'
import {SafetyCertificateOutlined,ExclamationCircleOutlined} from '@ant-design/icons-vue'
import {useAuthStore} from '../../stores/auth'
import {type IncomingRow} from './incomingModel'
import {rows,inspectionSummaryRows,detailDisplay,documentStatus} from './incomingDetailModel'
const props=defineProps<{record:IncomingRow;request?:IncomingRow;lot?:IncomingRow;references:Record<string,{name:string;row:IncomingRow;route?:string}>}>()
const router=useRouter(),auth=useAuthStore()
function name(key:string,id:unknown){return id==null?'—':props.references[`${key}:${id}`]?.name??String(id)}
function time(value:unknown){return value?String(value).replace('T',' ').replace(/\.\d+Z$|Z$/,''):'—'}
const items=computed(()=>rows(props.record.items).map(item=>{
 const source=props.references[`inspectionItemId:${item.inspectionItemId}`]?.row
 const result=(item.result??{}) as IncomingRow
 return {item,source,result,standard:source?inspectionSummaryRows([source])[0]!.standard:'关联标准暂不可读取'}
}))
const sampleIds=computed(()=>[...new Set(items.value.map(i=>i.result.sampleId).filter(v=>v!=null))])
const executionRows=computed(()=>items.value.map(i=>props.references[`testExecutionId:${i.result.testExecutionId}`]?.row).filter((v):v is IncomingRow=>!!v))
const analysts=computed(()=>[...new Set(executionRows.value.map(e=>e.performedBy).filter(v=>v!=null))])
const dates=computed(()=>[...new Set(executionRows.value.map(e=>e.completedAt).filter(v=>v!=null))])
const columns=[{title:'序号',key:'index',width:64},{title:'检验项目',key:'item',width:160},{title:'标准要求',dataIndex:'standard',key:'standard'},{title:'实际结果',key:'result',width:180},{title:'单位',key:'unit',width:90},{title:'判定',key:'conclusion',width:110}]
function open(key:string,id:unknown){const route=props.references[`${key}:${id}`]?.route;if(route)void router.push(route)}
</script>
<!-- UI Template: T3. The report displays only server-selected result references. -->
<template><div class="report-overview">
 <a-card title="基本信息" class="form-section report-facts"><div class="facts-grid">
  <section><dl><dt>请验单</dt><dd><a-button v-if="references[`inspectionRequestId:${record.inspectionRequestId}`]?.route" type="link" @click="open('inspectionRequestId',record.inspectionRequestId)">{{name('inspectionRequestId',record.inspectionRequestId)}}</a-button><span v-else>{{name('inspectionRequestId',record.inspectionRequestId)}}</span></dd><dt>样品编号</dt><dd><span v-if="!sampleIds.length">—</span><template v-for="id in sampleIds" :key="String(id)"><a-button v-if="references[`sampleId:${id}`]?.route" type="link" @click="open('sampleId',id)">{{name('sampleId',id)}}</a-button><span v-else>{{name('sampleId',id)}}</span></template></dd><dt>物料名称</dt><dd>{{(lot?.materialSnapshot as IncomingRow|undefined)?.materialName??'—'}}</dd><dt>物料批号</dt><dd>{{lot?.lotNo??request?.materialLotId??'—'}}</dd></dl></section>
  <section><dl><dt>检验标准</dt><dd>{{name('qcSpecificationVersionId',request?.qcSpecificationVersionId)}}</dd><dt>检验时间（UTC）</dt><dd>{{dates.length?dates.map(time).join('、'):'—'}}</dd><dt>检验人</dt><dd>{{analysts.length?analysts.map(id=>name('performedBy',id)).join('、'):'—'}}</dd><dt>当前状态</dt><dd><a-tag :color="record.status==='APPROVED'?'green':'blue'">{{documentStatus('inspection-reports',record)}}</a-tag></dd></dl></section>
 </div></a-card>
 <a-card title="检验项目与结果" class="form-section report-results"><a-table :columns="columns" :data-source="items" :pagination="false" :row-key="(row:{item:IncomingRow})=>String(row.item.id)" :scroll="{x:800}">
  <template #bodyCell="{column,record:row,index}"><span v-if="column.key==='index'">{{index+1}}</span><span v-else-if="column.key==='item'">{{row.source?.itemName??name('qcSpecificationItemId',row.item.qcSpecificationItemId)}}</span><span v-else-if="column.key==='result'">{{row.result.resultNumeric??row.result.resultText??'—'}}</span><span v-else-if="column.key==='unit'">{{name('resultUnitId',row.result.resultUnitId)}}</span><a-tag v-else-if="column.key==='conclusion'" :color="row.result.effectiveConclusion==='PASS'?'green':row.result.effectiveConclusion==='FAIL'?'red':'default'">{{detailDisplay('effectiveConclusion',row.result.effectiveConclusion)}}</a-tag></template>
  <template #emptyText>暂无报告项目</template>
 </a-table></a-card>
 <a-card title="检验结论" class="form-section"><div class="report-conclusion" :class="{passed:record.overallResult==='PASS',failed:record.overallResult==='FAIL'}"><SafetyCertificateOutlined v-if="record.overallResult==='PASS'"/><ExclamationCircleOutlined v-else/><div><strong>综合判定：{{detailDisplay('overallResult',record.overallResult)}}</strong><p>按请验时冻结的质量标准版本汇总已复核的选定结果。QC合格不代表物料已获QA放行。</p></div></div></a-card>
 <a-card title="审批信息" class="form-section"><div class="approval-grid"><section><h3>复核人</h3><strong>{{name('reviewedBy',record.reviewedBy)}}</strong></section><section><h3>复核时间（UTC）</h3><span>{{time(record.reviewedAt)}}</span></section><section><h3>批准人</h3><strong>{{name('approvedBy',record.approvedBy)}}</strong></section><section><h3>批准时间（UTC）</h3><span>{{time(record.approvedAt)}}</span></section><section><h3>审批状态</h3><a-tag :color="record.status==='APPROVED'?'green':'blue'">{{documentStatus('inspection-reports',record)}}</a-tag></section></div></a-card>
 <div class="report-links"><a-button v-if="auth.can('audit:view')" type="link" @click="router.push({path:'/audit',query:{objectType:'qms_inspection_report',objectId:String(record.id)}})">审计追踪</a-button><a-button v-if="request?.materialLotId&&auth.can('trace:view')" type="link" @click="router.push({path:'/trace',query:{materialLotId:String(request.materialLotId)}})">完整来料追溯</a-button></div>
</div></template>
<style scoped>
#app .report-overview :deep(.ant-card-head){min-height:40px;padding-inline:16px}#app .report-overview :deep(.ant-card-body){padding:12px 16px}#app .report-facts :deep(.ant-btn-link){min-height:0;line-height:20px;font-size:13px}#app .inspection-record-page .report-overview :deep(.ant-tag-green){background:#e1f7e9;color:#23834b;border:0}
.facts-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:24px}.facts-grid>section+section{border-left:1px solid #edf1f6;padding-left:24px}dl{display:grid;grid-template-columns:128px minmax(0,1fr);gap:8px 16px;margin:0;font-size:13px}dt{color:#66758a}dd{margin:0;min-width:0;overflow-wrap:anywhere}dd :deep(.ant-btn-link){height:auto;padding:0;white-space:normal;text-align:left}dd>span+span{margin-left:8px}.report-results :deep(.ant-table-cell){padding:10px 12px;font-size:13px}.report-results :deep(.ant-tag-green),.report-facts :deep(.ant-tag-green){background:#e1f7e9;color:#23834b;border:0}.report-conclusion{display:flex;align-items:center;gap:20px;border-radius:8px;padding:16px 20px;background:#f4f7fb}.report-conclusion.passed{background:#eefbf3;color:#23834b}.report-conclusion.failed{background:#fff3f3;color:#c72b30}.report-conclusion>.anticon{font-size:28px}.report-conclusion strong{font-size:16px}.report-conclusion p{margin:4px 0 0;font-size:12px;color:#66758a}.approval-grid{display:grid;grid-template-columns:1fr 1.4fr 1fr 1.4fr 1fr;gap:16px;font-size:13px}.approval-grid h3{font-size:12px;font-weight:400;color:#66758a;margin:0 0 8px}.approval-grid section{min-width:0;overflow-wrap:anywhere}.report-links{text-align:right;margin:-8px 0 8px}@media(max-width:767px){.facts-grid{grid-template-columns:minmax(0,1fr);gap:12px}.facts-grid>section+section{border-left:0;padding-left:0}dl{grid-template-columns:100px minmax(0,1fr);gap:8px 12px}.approval-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.report-conclusion{padding:12px;gap:12px}.report-links{text-align:left}}
</style>
