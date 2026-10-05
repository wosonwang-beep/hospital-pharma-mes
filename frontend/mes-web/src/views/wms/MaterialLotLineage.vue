<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {useRouter} from 'vue-router'
import {api,errorMessage} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {display} from '../quality/incomingModel'
interface Node {type:string;id:string;label:string;status?:string|null;revision?:number|null}
interface Edge {sourceType:string;sourceId:string;targetType:string;targetId:string;relation:string}
const props=defineProps<{lotId:string;types:string[]}>(),auth=useAuthStore(),router=useRouter()
const graph=ref<{nodes:Node[];edges:Edge[]}>({nodes:[],edges:[]}),busy=ref(false),error=ref('')
let request=0
async function load(){const ticket=++request;graph.value={nodes:[],edges:[]};error.value='';if(!auth.can('trace:view'))return;busy.value=true;try{const result=await api<{nodes:Node[];edges:Edge[]}>({url:'/trace',params:{materialLotId:props.lotId}});if(ticket===request)graph.value=result}catch(e){if(ticket===request)error.value=errorMessage(e)}finally{if(ticket===request)busy.value=false}}
const rows=computed(()=>graph.value.nodes.filter(n=>props.types.includes(n.type)))
const destinations:Record<string,{path:string;permission:string}>={RECEIPT:{path:'/wms/receipts',permission:'wms:receipt:view'},INSPECTION_REQUEST:{path:'/quality/inspection-requests',permission:'qms:inspection-request:view'},SAMPLING_TASK:{path:'/quality/sampling-tasks',permission:'qms:sampling:view'},SAMPLE:{path:'/quality/samples',permission:'qms:test:view'},INSPECTION_TASK:{path:'/quality/inspection-tasks',permission:'qms:test:view'},REPORT:{path:'/quality/inspection-reports',permission:'qms:report:view'},INVESTIGATION:{path:'/deviations',permission:'qms:deviation:view'},MAIN_BATCH:{path:'/production/batches',permission:'production:batch:view'},EXECUTION_UNIT:{path:'/mes/execution',permission:'mes:operation:view'}}
function destination(n:Node,visited=new Set<string>()):string|undefined{
 const key=n.type+':'+n.id;if(visited.has(key))return;visited.add(key)
 const direct=destinations[n.type];if(direct)return auth.can(direct.permission)?`${direct.path}/${n.id}`:undefined
 if(n.type==='RELEASE_DECISION')return auth.can('qa:material-release:view')?`/qa/material-lots/${props.lotId}/review`:undefined
 const parentTypes:Record<string,string>={RECEIPT_ITEM:'RECEIPT',SAMPLING_DETAIL:'SAMPLING_TASK',INSPECTION_ITEM:'INSPECTION_TASK',TEST_EXECUTION:'INSPECTION_ITEM',RESULT_REVISION:'TEST_EXECUTION',OPERATION:'EXECUTION_UNIT',CHARGE:'OPERATION',WEIGHING:'CHARGE'}
 const parentType=parentTypes[n.type];if(!parentType)return
 const edge=graph.value.edges.find(e=>e.targetType===n.type&&e.targetId===n.id&&e.sourceType===parentType)
 if(n.type==='RECEIPT_ITEM'){const receipt=graph.value.edges.find(e=>e.sourceType===n.type&&e.sourceId===n.id&&e.targetType==='RECEIPT');if(receipt){const node=graph.value.nodes.find(p=>p.type==='RECEIPT'&&p.id===receipt.targetId);if(node)return destination(node,visited)}}
 if(edge){const parent=graph.value.nodes.find(p=>p.type===edge.sourceType&&p.id===edge.sourceId);if(parent)return destination(parent,visited)}
}
const names:Record<string,string>={RECEIPT:'收货记录',RECEIPT_ITEM:'收货明细',INSPECTION_REQUEST:'请验单',SAMPLING_TASK:'取样记录',SAMPLING_DETAIL:'取样明细',SAMPLE:'样品',INSPECTION_TASK:'检验记录',INSPECTION_ITEM:'检验项目',TEST_EXECUTION:'检验执行',RESULT_REVISION:'检验结果修订',REPORT:'检验报告',RELEASE_DECISION:'物料放行决定',INVESTIGATION:'质量调查',MAIN_BATCH:'生产批',EXECUTION_UNIT:'执行单元',OPERATION:'工序',CHARGE:'投料记录',WEIGHING:'称量记录',SIGNATURE:'电子签名',INVENTORY_DECISION:'库存控制决定'}
function description(n:Node){const name=names[n.type];if(!name)return n.label;if(n.label===n.type)return name;return n.label.startsWith(n.type+' ')?name+n.label.slice(n.type.length):n.label}
watch(()=>props.lotId,()=>void load(),{immediate:true})
</script>
<!-- Read-only T4 subordinate view: existing trace graph is the sole lineage source. -->
<template><section class="lot-lineage">
 <a-alert v-if="!auth.can('trace:view')" type="info" message="完整关联记录需要追溯查询权限，请联系管理员。" show-icon/>
 <a-alert v-else-if="error" type="error" :message="error" show-icon><template #action><a-button @click="load">重试</a-button></template></a-alert>
 <a-table v-else :loading="busy" :data-source="rows" :pagination="false" :scroll="{x:600}" :row-key="(n:Node)=>n.type+':'+n.id" :columns="[{title:'记录类型',key:'type'},{title:'记录 ID',dataIndex:'id'},{title:'来源说明',dataIndex:'label'},{title:'状态',key:'status'},{title:'修订版本',dataIndex:'revision'},{title:'操作',key:'actions'}]">
  <template #bodyCell="{column,record}"><span v-if="column.dataIndex==='label'">{{description(record)}}</span><span v-else-if="column.key==='type'">{{names[record.type]??record.type}}</span><span v-else-if="column.key==='status'">{{display(record.status)}}</span><template v-else-if="column.key==='actions'"><a-button v-if="destination(record)" type="link" @click="router.push(destination(record)!)">查看来源记录</a-button><span v-else>—</span></template></template>
  <template #emptyText><span>暂无关联记录</span></template>
 </a-table>
 <p class="section-hint">仅聚合已有追溯记录；受控操作请进入对应业务页面。</p>
</section></template>
<style scoped>.section-hint{margin:12px 0 0;color:var(--mes-ui-secondary);font-size:12px}</style>
