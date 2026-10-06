<script setup lang="ts">
import {ref,watch} from 'vue'
import {api} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
const props=defineProps<{value:unknown;field:string;name?:unknown;fallback?:string}>(),auth=useAuthStore(),resolved=ref('')
const routes:Record<string,[string,string,string[]]>={
 inboundRequestId:['finished-inbound-requests','wms:finished-inbound:view',['requestNo']],finishedInspectionRequestId:['quality/finished-inspection-requests','qms:finished-request:view',['inspectionRequestNo']],
 productionSampleId:['samples','qms:sample:view',['sampleNo']],
 sampleId:['quality/samples','qms:test:view',['sampleNo']],qcSpecificationVersionId:['quality/specification-versions','qms:specification:view',['specificationName','versionNoBusiness']],packageVersionId:['process-packages','process:package:view',['packageCode','businessVersion']],
 productId:['products','master:product:view',['productName','productCode']],materialId:['materials','master:material:view',['materialName','materialCode']],finishedMaterialId:['materials','master:material:view',['materialName','materialCode']],
 mainBatchId:['main-batches','production:batch:view',['batchNo']],productionOrderId:['production-orders','production:order:view',['orderNo']],materialLotId:['wms/material-lots','wms:inventory:view',['lotNo']],
 userId:['admin/users','menu:iam:users',['displayName','username']],parentId:['organizations','master:org:view',['orgName','orgCode']],
 unitId:['units','master:uom:view',['unitName','unitCode']],fromUnitId:['units','master:uom:view',['unitName','unitCode']],toUnitId:['units','master:uom:view',['unitName','unitCode']],
 locationId:['locations','wms:inventory:view',['locationName','locationCode']],containerId:['containers','wms:inventory:view',['containerCode']]
}
let ticket=0
watch(()=>[props.value,props.field,props.name],async()=>{const current=++ticket;resolved.value='';const def=routes[props.field];if(props.name||props.value==null||!def||!auth.can(def[1]))return;try{if(props.field==='packageVersionId'){const page=await api<{items:Record<string,unknown>[]}>({url:'/process-packages',params:{page:0,size:100}});for(const pkg of page.items){const version=((pkg.versions??[]) as Record<string,unknown>[]).find(v=>String(v.id)===String(props.value));if(version&&current===ticket){resolved.value=`${pkg.packageCode??''} · V${version.businessVersion}`;return}}return}const row=await api<Record<string,unknown>>({url:`/${def[0]}/${props.value}`});if(current===ticket){const name=def[2].map(k=>row[k]).find(v=>v!=null&&v!=='');resolved.value=name==null?'':(props.field==='qcSpecificationVersionId'||props.field==='packageVersionId')?`V${row.versionNoBusiness??row.businessVersion??name}`:String(name)}}catch{/* A read-only reference remains visible when lookup access fails. */}},{immediate:true})
</script>
<template><span>{{name||resolved||(value==null||value===''?'—':fallback??`来源引用 ${value}`)}}</span></template>
