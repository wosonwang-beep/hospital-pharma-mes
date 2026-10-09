<script setup lang="ts">
import {ref,watch} from 'vue'
import {api} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {cachedReference} from './referenceCache'
const props=defineProps<{value:unknown;field:string;name?:unknown;fallback?:string}>(),auth=useAuthStore(),resolved=ref('')
const routes:Record<string,[string,string,string[]]>={
 inboundRequestId:['finished-inbound-requests','wms:finished-inbound:view',['requestNo']],finishedInspectionRequestId:['quality/finished-inspection-requests','qms:finished-request:view',['inspectionRequestNo']],finishedSamplingRecordId:['quality/finished-sampling-records','qms:finished-sampling:view',['samplingNo']],finishedReportId:['quality/finished-inspection-reports','qms:finished-report:view',['reportNo']],finishedShipmentId:['finished-shipments','wms:finished-shipment:view',['shipmentNo']],receiptId:['wms/receipts','wms:receipt:view',['receiptNo']],materialIssueId:['material-issues','wms:issue:view',['issueNo']],executionUnitId:['execution-units','mes:execution:view',['executionNo']],
 productionSampleId:['samples','qms:sample:view',['sampleNo']],
 inspectionRequestId:['quality/inspection-requests','qms:inspection-request:view',['requestNo']],samplingTaskId:['quality/sampling-tasks','qms:sampling:view',['samplingTaskNo']],inspectionTaskId:['quality/inspection-tasks','qms:test:view',['inspectionTaskNo']],inspectionReportId:['quality/inspection-reports','qms:report:view',['reportNo']],
 sampleId:['quality/samples','qms:test:view',['sampleNo']],qcSpecificationVersionId:['quality/specification-versions','qms:specification:view',['specificationName','versionNoBusiness']],processPackageId:['process-packages','process:package:view',['packageCode']],
 productId:['products','master:product:view',['productName','productCode']],materialId:['materials','master:material:view',['materialName','materialCode']],finishedMaterialId:['materials','master:material:view',['materialName','materialCode']],
 mainBatchId:['main-batches','production:batch:view',['batchNo']],productionOrderId:['production-orders','production:order:view',['orderNo']],materialLotId:['wms/material-lots','wms:inventory:view',['lotNo']],
 userId:['admin/users','menu:iam:users',['displayName','username']],parentId:['organizations','master:org:view',['orgName','orgCode']],
 unitId:['units','master:uom:view',['unitName','unitCode']],fromUnitId:['units','master:uom:view',['unitName','unitCode']],toUnitId:['units','master:uom:view',['unitName','unitCode']],
 locationId:['locations','wms:inventory:view',['locationName','locationCode']],containerId:['containers','wms:inventory:view',['containerCode']]
}
let ticket=0
watch(()=>[props.value,props.field,props.name,auth.identity?.userId,auth.identity?.organizationId,auth.identity?.permissionCodes.join(',')],async()=>{const current=++ticket;resolved.value='';const def=routes[props.field];if(props.name||props.value==null||!def||!auth.can(def[1]))return;const cacheKey=`${auth.identity?.organizationId}:${auth.identity?.userId}:${props.field}:${String(props.value)}`;try{const value=await cachedReference(cacheKey,async()=>{if(props.field==='packageVersionId'){const page=await api<{items:Record<string,unknown>[]}>({url:'/process-packages',params:{page:0,size:100}});for(const pkg of page.items){const version=((pkg.versions??[]) as Record<string,unknown>[]).find(v=>String(v.id)===String(props.value));if(version)return `${pkg.packageCode??''} · V${version.businessVersion}`}return ''}const row=await api<Record<string,unknown>>({url:`/${def[0]}/${props.value}`});const name=def[2].map(k=>row[k]).find(v=>v!=null&&v!=='');return name==null?'':props.field==='qcSpecificationVersionId'?`${row.specificationName??name} · V${row.versionNoBusiness??''}`:String(name)});if(current===ticket)resolved.value=value}catch{/* A read-only reference remains visible when lookup access fails. */}},{immediate:true})
</script>
<template><span>{{name||resolved||(value==null||value===''?'—':fallback??`来源引用 ${value}`)}}</span></template>
