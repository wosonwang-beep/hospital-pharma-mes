<script setup lang="ts">
import {computed} from 'vue'
import IncomingFacts from './IncomingFacts.vue'
import {sections,selectFacts,detailLabel,rows} from './incomingDetailModel'
import type {IncomingRow} from './incomingModel'
const props=defineProps<{record:IncomingRow;resource:string;valueLabel:(key:string,value:unknown)=>string}>()
// Presentation only. Full immutable evidence remains in IncomingDocument.
const technical=new Set(['id','orgId','versionNo','recordVersion','revisionNo','createdBy','createdAt','updatedBy','updatedAt','signatureEvidence','signingTargets','allowedActions','specificationContentHash','evidenceDigest','receiptItemId','inspectionRequestItemId','samplingDetailId'])
function business(value:unknown):unknown{
 if(Array.isArray(value))return value.map(business)
 if(value&&typeof value==='object')return Object.fromEntries(Object.entries(value).filter(([key])=>!technical.has(key)&&!key.endsWith('SignatureId')&&key!=='signatureId').map(([key,v])=>[key,business(v)]))
 return value
}
const panels=computed(()=>(sections[props.resource]??[]).map(s=>({...s,value:business(selectFacts(props.record,s.keys)) as IncomingRow})).filter(s=>Object.keys(s.value).length))
const samplingDetails=computed(()=>rows(props.record.details))
const samples=computed(()=>samplingDetails.value.flatMap(d=>rows(d.samples)))
const columns=[{title:'容器',dataIndex:'containerNo'},{title:'取样位置',dataIndex:'samplingPoint'},{title:'取样数量',dataIndex:'sampleQuantity'},{title:'单位',dataIndex:'unitId'},{title:'取样人',dataIndex:'sampledBy'},{title:'取样时间',dataIndex:'sampledAt'},{title:'重新密封',dataIndex:'packageResealed'}]
</script>
<!-- UI Template: T3. Existing business facts first; technical mechanisms are secondary evidence. -->
<template><section class="incoming-business-overview">
 <a-card v-for="panel in panels" :key="panel.title" :title="panel.title" class="form-section">
  <template v-if="resource==='sampling-tasks'&&panel.value.details">
   <IncomingFacts :value="selectFacts(panel.value,['startedAt','completedAt'])" :label-formatter="detailLabel" :value-formatter="valueLabel"/>
   <a-table :columns="columns" :data-source="samplingDetails" :pagination="false" :scroll="{x:850}" :row-key="(r:IncomingRow)=>String(r.id)"><template #bodyCell="{column,record:detail}">{{valueLabel(column.dataIndex,detail[column.dataIndex])}}</template><template #emptyText>暂无取样明细</template></a-table>
   <h3>生成样品</h3><a-table :data-source="samples" :pagination="false" :scroll="{x:560}" :row-key="(r:IncomingRow)=>String(r.id)" :columns="[{title:'样品号',dataIndex:'sampleNo'},{title:'类型',dataIndex:'sampleType'},{title:'数量',dataIndex:'quantity'},{title:'单位',dataIndex:'unitId'},{title:'状态',dataIndex:'status'}]"><template #bodyCell="{column,record:sample}">{{valueLabel(column.dataIndex,sample[column.dataIndex])}}</template><template #emptyText>暂无生成样品</template></a-table>
  </template>
  <IncomingFacts v-else :value="panel.value" :label-formatter="detailLabel" :value-formatter="valueLabel"/>
 </a-card>
</section></template>
<style scoped>h3{font-size:14px;font-weight:600;margin:16px 0 12px}.incoming-business-overview{min-width:0}.ant-table-wrapper{margin-top:16px}</style>
