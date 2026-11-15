<script setup lang="ts">
import {reactive,onMounted} from 'vue'
import {createListController,createListState,permits,type InspectionRow,type InspectionQuery,type InspectionPage} from './inspectionListModel'
type Option={value:string;label:string}
const props=defineProps<{loadPage:(query:InspectionQuery)=>Promise<InspectionPage>;inspectors:Option[];initialQuery?:Partial<InspectionQuery>}>()
const emit=defineEmits<{open:[row:InspectionRow,action:string]}>()
const state=reactive(createListState())
for(const key of ['keyword','status','assignedTo','requestType'] as const)state.draft[key]=props.initialQuery?.[key]??''
state.applied={...state.draft};state.page=(props.initialQuery?.page??0)+1;state.size=props.initialQuery?.size??20
const controller=createListController(state,props.loadPage)
const statuses=[{value:'',label:'全部状态'},{value:'CREATED',label:'待分派'},{value:'ASSIGNED',label:'待检验'},{value:'IN_PROGRESS',label:'检验中'},{value:'PENDING_REVIEW',label:'待复核'},{value:'QC_PASSED',label:'复核通过'},{value:'QC_FAILED',label:'复核不通过'}]
const types=[{value:'',label:'全部类型'},{value:'INITIAL',label:'首次检验'},{value:'RETEST',label:'复检'},{value:'SUPPLEMENTARY',label:'补充检验'},{value:'INVESTIGATION',label:'调查检验'}]
const columns=[{title:'样品名称',key:'sampleName',dataIndex:'sampleName',width:150},{title:'物料批号',key:'lotNo',dataIndex:'lotNo',width:104},{title:'样品编号',key:'sampleNo',dataIndex:'sampleNo',width:142},{title:'检验单类型',key:'requestType',width:82},{title:'任务状态',key:'status',width:88},{title:'检验人',key:'assignedName',dataIndex:'assignedName',width:62},{title:'请验 / 取样日期',key:'dates',width:112},{title:'综合判定',key:'overallResult',width:80},{title:'检验单号（报告号）',key:'reportNo',width:146},{title:'操作',key:'actions',width:100,fixed:'right' as const}]
const display=(options:Option[],value:string)=>options.find(x=>x.value===value)?.label??'未知'
const date=(value:string|null)=>value?value.slice(0,10):'—'
const result=(row:InspectionRow)=>row.overallResult==='PASS'?'合格':row.overallResult==='FAIL'?'不合格':'待判定'
onMounted(()=>controller.reload())
defineExpose({state,controller})
</script>
<template>
<main class="inspection-list-page" data-ui-template="T1">
 <header class="inspection-page-heading"><h1>检验记录</h1></header>
 <a-card title="查询条件" class="inspection-query-card">
 <form class="query-form inspection-query" @submit.prevent="controller.query">
  <label><span class="form-field-label">样品/批号</span><a-input v-model:value="state.draft.keyword" aria-label="样品或批号" placeholder="输入名称、批号或编号" allow-clear :maxlength="200"/></label>
  <label><span class="form-field-label">任务状态</span><a-select v-model:value="state.draft.status" aria-label="任务状态" :options="statuses"/></label>
  <label><span class="form-field-label">检验人</span><a-select v-model:value="state.draft.assignedTo" aria-label="检验人" show-search option-filter-prop="label" :options="[{value:'',label:'全部人员'},...inspectors]"/></label>
  <label><span class="form-field-label">检验单类型</span><a-select v-model:value="state.draft.requestType" aria-label="检验单类型" :options="types"/></label>
  <div class="inspection-query-actions"><a-button type="primary" html-type="submit" :loading="state.busy">查询</a-button><a-button @click="controller.reset">重置</a-button></div>
 </form>
 </a-card>
 <a-card title="检验任务列表" class="inspection-result-card">
  <template #extra><span role="status" aria-live="polite" aria-atomic="true">{{state.busy?'正在查询':state.error?'查询未完成':'共 '+state.total+' 条'}}</span></template>
  <a-alert v-if="state.error" type="error" role="alert" :message="state.error" description="查询条件已保留。请重试；若仍失败，请联系管理员。" show-icon><template #action><a-button @click="controller.reload">重试</a-button></template></a-alert>
  <p class="inspection-scroll-guide">样品名称、批号优先核对；窄屏可左右滚动查看其余信息。</p>
  <a-table :columns="columns" :data-source="state.rows" row-key="id" :loading="state.busy" :pagination="false" :scroll="{x:1066}">
   <template #emptyText><div class="inspection-empty">{{state.error?'未取得查询结果':'暂无符合条件的检验记录'}}<p v-if="!state.error">请调整样品名称、批号或任务状态，或重置条件。</p><a-button v-if="!state.error" @click="controller.reset">重置条件</a-button></div></template>
   <template #bodyCell="{column,record}">
    <strong v-if="column.key==='sampleName'||column.key==='lotNo'" :title="record[column.key]??''">{{record[column.key]||(column.key==='sampleName'?'来源未提供':'—')}}</strong>
    <span v-else-if="column.key==='requestType'">{{display(types,record.requestType)}}</span>
    <span v-else-if="column.key==='status'">{{display(statuses,record.status)}}</span>
    <span v-else-if="column.key==='dates'" class="inspection-dates"><span :title="'请验日期 '+date(record.requestedDate)">{{date(record.requestedDate)}} /</span><span :title="'取样日期 '+date(record.sampledAt)">{{date(record.sampledAt)}}</span></span>
    <span v-else-if="column.key==='overallResult'" :class="{'inspection-fail':record.overallResult==='FAIL'}">{{result(record)}}</span>
    <span v-else-if="column.key==='reportNo'" :title="record.reportNo??''">{{record.reportNo||'未生成'}}</span>
    <div v-else-if="column.key==='actions'" class="inspection-row-actions">
     <a-button v-if="permits(record,'view')" type="link" :aria-label="'查看 '+(record.sampleName??'样品')+' '+record.lotNo" @click="emit('open',record,'view')">查看</a-button>
     <a-button v-if="permits(record,'execute')" type="link" :aria-label="'填写 '+record.sampleName+' '+record.lotNo" @click="emit('open',record,'execute')">填写</a-button>
     <a-button v-if="permits(record,'review')" type="link" :aria-label="'复核 '+record.sampleName+' '+record.lotNo" @click="emit('open',record,'review')">复核</a-button>
    </div>
    <span v-else :title="record[column.key]??''">{{record[column.key]||'—'}}</span>
   </template>
  </a-table>
  <footer class="inspection-pagination"><span>第 {{state.page}} 页</span><a-pagination :current="state.page" :total="state.total" :page-size="state.size" :show-size-changer="false" :disabled="state.busy" @change="controller.changePage"/></footer>
 </a-card>
</main>
</template>
<style scoped>
.inspection-list-page{font-family:Inter,system-ui,"Microsoft YaHei",sans-serif;color:#303133;min-width:0}
.inspection-page-heading{background:white;border:1px solid #ebeef5;border-radius:4px;padding:16px 20px;margin-bottom:16px}.inspection-page-heading h1{font-size:22px;line-height:30px;font-weight:650;margin:0}
.inspection-list-page :deep(.ant-card){border-color:#ebeef5;border-radius:4px;margin-bottom:16px}.inspection-list-page :deep(.ant-card-head){min-height:48px;padding:0 20px;font-size:15px}.inspection-list-page :deep(.ant-card-head-title){font-weight:600;border-left:3px solid #2c5cdc;padding:0 0 0 10px;margin:16px 0}.inspection-list-page :deep(.ant-card-extra){font-size:13px;color:#606266}.inspection-query-card :deep(.ant-card-body){padding:14px 20px}
.inspection-query{display:flex;align-items:center;gap:10px 12px;flex-wrap:wrap}.inspection-query label{display:flex;align-items:center;gap:8px;white-space:nowrap;margin:0}.form-field-label{font-size:13px;color:#606266}
.inspection-query :deep(.ant-input-affix-wrapper){width:182px;height:34px;border-color:#8793a3;border-radius:4px}.inspection-query :deep(.ant-select){width:100px;height:34px}.inspection-query :deep(.ant-select-selector){border-color:#8793a3!important;border-radius:4px!important}.inspection-query-actions{display:flex;gap:8px;align-items:center}
.inspection-list-page :deep(.ant-btn){font-size:13px;border-radius:4px}.inspection-list-page :deep(.ant-btn-primary){background:#2c5cdc}
.inspection-result-card :deep(.ant-card-body){padding:0 20px 20px}.inspection-scroll-guide{font-size:12px;color:#606b7a;margin:0;padding:8px 0}
.inspection-list-page :deep(.ant-table-cell){padding:10px 8px!important;font-size:13px;line-height:18px;white-space:normal;overflow-wrap:anywhere}.inspection-list-page :deep(th.ant-table-cell){background:#f5f7fa;color:#606266;font-weight:600}.inspection-list-page :deep(.ant-table-thead>tr>th:before){display:none!important}
.inspection-dates{display:flex;flex-direction:column;font-variant-numeric:tabular-nums}.inspection-row-actions{display:flex;gap:4px;white-space:nowrap}.inspection-row-actions :deep(.ant-btn){height:30px;padding:0;color:#2c5cdc}.inspection-fail{color:#a61d24}
.inspection-pagination{display:flex;align-items:center;justify-content:space-between;margin-top:16px;padding-top:16px;border-top:1px solid #ebeef5;font-size:12px;color:#606b7a}.inspection-empty{padding:24px;color:#606266}.inspection-empty p{font-size:13px}
.inspection-list-page :deep(button:focus-visible),.inspection-list-page :deep(input:focus-visible){outline:2px solid #2c5cdc;outline-offset:2px}
@media(max-width:480px){.inspection-query :deep(.ant-input-affix-wrapper){width:180px}.inspection-list-page :deep(.ant-table-cell-fix-right){position:static!important}.inspection-list-page :deep(.ant-table-cell-fix-right-first:after){display:none}}
</style>
