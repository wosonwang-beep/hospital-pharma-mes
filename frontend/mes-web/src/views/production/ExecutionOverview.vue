<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {useRouter} from 'vue-router'
import {ExperimentOutlined,CheckCircleFilled,FileTextOutlined,CheckCircleOutlined,SafetyCertificateOutlined} from '@ant-design/icons-vue'
import {useAuthStore} from '../../stores/auth'
import {api} from '../../api/http'
import IncomingFacts from '../quality/IncomingFacts.vue'
import FunctionalQualityActions from '../quality/FunctionalQualityActions.vue'
import {rows,selectFacts,detailLabel} from '../quality/incomingDetailModel'
import {display} from '../quality/incomingModel'
import {productionActions} from './productionModel'
import {parameterRange} from './workbenchFacts'
type Row=Record<string,unknown>
const props=defineProps<{execution:Row;batch:Row|null;operations:Row[];weighings:Row[];charges:Row[];forms:Row[];actions:(row:Row)=>string[]}>()
const emit=defineEmits<{command:[Row,string];reload:[]}>()
const router=useRouter(),auth=useAuthStore(),chosen=ref(''),tab=ref('execution'),drawer=ref(''),focusedCharge=ref('')
const lookup=ref<Record<string,Row>>({})
function label(kind:string,id:unknown){
 if(id==null)return '—'
 if(kind==='user'&&String(id)===String(auth.identity?.userId))return auth.identity?.displayName??String(id)
 const field=({unit:'unitName',lot:'lotNo',product:'productName',user:'displayName'} as Record<string,string>)[kind]!
 return String(lookup.value[`${kind}:${id}`]?.[field]??id)
}
function unitLabel(id:unknown){const name=label('unit',id);return ({'千克':'kg','摄氏度':'℃'} as Record<string,string>)[name]??name}
function quantityDisplay(value:unknown){return String(value??'—').replace(/^-?\d+(?=\.|$)/,whole=>whole.replace(/\B(?=(\d{3})+(?!\d))/g,','))}
function productField(field:string){return lookup.value[`product:${props.batch?.productId}`]?.[field]??'—'}
function materialField(charge:Row,field:string){return (lookup.value[`lot:${charge.materialLotId}`]?.materialSnapshot as Row|undefined)?.[field]??'—'}
function valueLabel(key:string,value:unknown){if(key==='unitId')return unitLabel(value);if(key==='materialLotId')return label('lot',value);if(['operatorId','chargedBy','verifiedBy'].includes(key))return label('user',value);return display(value)}
function fieldLabel(key:string){return ({operatorId:'操作人',equipmentUsages:'设备使用记录',parameterValues:'工艺参数记录',parameters:'冻结参数定义',signatures:'电子签名记录',charges:'投料记录',materials:'冻结物料信息',gates:'工序校验依据'} as Record<string,string>)[key]??detailLabel(key)}
watch(()=>[props.batch,props.charges,props.weighings,props.operations],async()=>{
 const jobs:Array<Promise<void>>=[],seen=new Set<string>()
 function resolve(kind:string,id:unknown,path:string,permission:string){const key=`${kind}:${id}`;if(id==null||seen.has(key)||lookup.value[key]||!auth.can(permission))return;seen.add(key);jobs.push(api<Row>({url:`${path}/${id}`}).then(row=>{lookup.value[key]=row}).catch(()=>{}))}
 resolve('product',props.batch?.productId,'/products','master:product:view')
 for(const row of [props.batch,...props.charges,...props.weighings,...props.operations.flatMap(o=>rows(o.parameterValues))].filter((r):r is Row=>!!r))resolve('unit',row.unitId,'/units','master:uom:view')
 for(const charge of props.charges){resolve('lot',charge.materialLotId,'/wms/material-lots','wms:inventory:view');resolve('user',charge.chargedBy,'/users','iam:user:view')}
 for(const operation of props.operations)resolve('user',operation.operatorId,'/users','iam:user:view')
 await Promise.all(jobs)
},{immediate:true})
const snapshot=computed(()=>((props.batch?.processSnapshot as Row|undefined)?.snapshot??{}) as Row)
const process=computed(()=>(snapshot.value.process??{}) as Row)
const definitions=computed(()=>rows((process.value.route as Row|undefined)?.operations))
const ordered=computed(()=>props.operations.slice().sort((a,b)=>Number(a.operationSeq)-Number(b.operationSeq)))
const current=computed(()=>ordered.value.find(o=>['IN_PROGRESS','PAUSED'].includes(String(o.status)))??ordered.value.find(o=>o.status!=='COMPLETED')??ordered.value[ordered.value.length-1])
const active=computed(()=>ordered.value.find(o=>String(o.id)===chosen.value)??current.value)
const completed=computed(()=>ordered.value.filter(o=>o.status==='COMPLETED').length)
const definition=computed(()=>definitions.value.find(o=>String(o.operationDefId)===String(active.value?.operationDefId)))
const selectedCharges=computed(()=>props.charges.filter(c=>String(c.operationExecutionId)===String(active.value?.id)))
const primaryCharges=computed(()=>{const charge=selectedCharges.value.find(c=>String(c.id)===focusedCharge.value)??selectedCharges.value[0];return charge?[charge]:[]})
function inspectCharge(charge:Row){focusedCharge.value=String(charge.id);drawer.value='evidence'}
const parameters=computed(()=>rows(active.value?.parameterValues))
const gates=computed(()=>rows(active.value?.gates))
const parameterColumns=[{title:'参数名称',key:'name'},{title:'设定值',key:'limits'},{title:'实际值',key:'value'},{title:'单位',dataIndex:'unitId'},{title:'范围核对',key:'status'}]
const materialColumns=[{title:'#',key:'index',width:36},{title:'物料名称',key:'name'},{title:'批号',dataIndex:'materialLotId'},{title:'规格',key:'specification'},{title:'计划用量',key:'target'},{title:'实际用量',dataIndex:'chargedQty'},{title:'单位',dataIndex:'unitId'},{title:'状态',key:'status'},{title:'操作',key:'action',width:46}]
function parameterDefinition(row:Row){return rows(definition.value?.parameters).find(p=>String(p.parameterDefId)===String(row.parameterDefId))}
function limits(row:Row){const p=parameterDefinition(row);if(p?.lowerLimit!=null&&p?.upperLimit!=null)return `${p.lowerLimit} ～ ${p.upperLimit}`;if(p?.lowerLimit!=null)return `≥ ${p.lowerLimit}`;if(p?.upperLimit!=null)return `≤ ${p.upperLimit}`;return '—'}
function name(row:Row){const d=definitions.value.find(o=>String(o.operationDefId)===String(row.operationDefId));return String(d?.operationName??row.operationName??`工序 ${row.operationDefId}`)}
function time(value:unknown){return value?String(value).replace('T',' ').replace(/\.\d+Z$|Z$/,''):'—'}
function interval(row:Row){const start=row.startedAt?String(row.startedAt).slice(11,16):null,end=row.completedAt?String(row.completedAt).slice(11,16):null;return start?`${start}${end?` – ${end}`:' 开始'}`:'尚未开始'}
function state(value:unknown){return value==='PENDING'?'待开始':value==='IN_PROGRESS'?'进行中':display(value)}
function color(value:unknown){return value==='COMPLETED'?'green':value==='IN_PROGRESS'?'blue':value==='PAUSED'?'orange':'default'}
function choose(row:Row){chosen.value=String(row.id);focusedCharge.value='';tab.value='execution'}
const canRecordDeviation=computed(()=>!!active.value&&auth.can('qms:deviation:create')&&!['QA_RELEASED','REJECTED'].includes(String(props.batch?.status)))
function recordDeviation(){if(canRecordDeviation.value&&active.value)router.push({path:'/deviations/create',query:{scope:'PRODUCTION',mainBatchId:String(props.execution.mainBatchId),operationExecutionId:String(active.value.id)}})}
function complete(){if(active.value&&props.actions(active.value).includes('complete'))emit('command',active.value,'complete')}
watch(()=>props.execution.id,()=>{chosen.value='';focusedCharge.value='';tab.value='execution';drawer.value=''})
</script>
<!-- UI Template: T5. Desktop reference layout; all commands remain owned by ExecutionWorkbench. -->
<template><div class="execution-overview">
 <header class="workbench-heading"><div class="heading-copy"><span class="template-badge">T5</span><div><h1>生产执行工作台</h1><p>按工艺流程执行生产，实时记录关键参数</p></div></div><a-space><a-button aria-label="工艺指令" @click="drawer='instruction'"><ExperimentOutlined/>工艺指令</a-button><a-button aria-label="批记录" @click="drawer='ebr'"><FileTextOutlined/>批记录</a-button><a-button v-if="active&&actions(active).includes('complete')" type="primary" @click="complete">完成当前步骤</a-button></a-space></header>
 <div class="context-panels"><a-card class="production-summary"><div class="production-context"><ExperimentOutlined class="production-icon"/><div class="batch-context"><div><span class="muted">生产批号</span><strong>{{batch?.batchNo??execution.mainBatchId}}</strong><a-tag :color="execution.status==='IN_PROGRESS'?'green':color(execution.status)">{{execution.status==='IN_PROGRESS'?'生产中':display(execution.status)}}</a-tag></div><p><span class="muted">产品</span><b>{{label('product',batch?.productId)}}</b><span class="muted">规格</span>{{productField('specification')}}<span class="muted">计划产量</span>{{quantityDisplay(batch?.plannedQty)}} {{unitLabel(batch?.unitId)}}</p></div></div></a-card><a-card class="current-summary"><div class="progress-context"><div><small>当前工序</small><strong>{{current?`${current.operationSeq}. ${name(current)}`:'暂无工序'}}</strong><span>{{current?interval(current):'暂无记录'}}（UTC）</span></div><div class="progress-value"><span :title="`已完成 ${completed} / ${ordered.length} 工序`">整体进度 {{completed}} / {{ordered.length}}</span><a-progress :percent="ordered.length?Math.round(completed/ordered.length*100):0" :stroke-color="'#36bd7a'"/></div></div></a-card></div>
 <nav class="process-navigation" aria-label="工艺进度"><button v-for="operation in ordered" :key="String(operation.id)" :class="{active:active?.id===operation.id,completed:operation.status==='COMPLETED'}" @click="choose(operation)"><CheckCircleFilled v-if="operation.status==='COMPLETED'" class="finished"/><span v-else class="step-number">{{operation.operationSeq}}</span><div><strong>{{operation.operationSeq}}. {{name(operation)}}</strong><small>{{interval(operation)}}</small><a-tag :color="color(operation.status)">{{state(operation.status)}}</a-tag></div></button><p v-if="!ordered.length">暂无工序记录</p></nav>
 <div class="workbench-grid"><a-card title="工艺步骤" class="step-list"><button v-for="operation in ordered" :key="String(operation.id)" :class="{active:active?.id===operation.id,completed:operation.status==='COMPLETED'}" @click="choose(operation)"><CheckCircleFilled v-if="operation.status==='COMPLETED'" class="finished"/><span v-else class="step-number">{{operation.operationSeq}}</span><span>{{operation.operationSeq}}. {{name(operation)}}</span><a-tag :color="color(operation.status)">{{state(operation.status)}}</a-tag></button></a-card>
 <a-card class="primary-workspace"><a-tabs v-model:active-key="tab">
  <a-tab-pane key="execution" tab="执行记录"><template v-if="selectedCharges.length"><section v-for="charge in primaryCharges" :key="String(charge.id)" class="execution-charge"><h3>投料记录</h3><div class="charge-layout"><dl class="charge-facts"><dt>投料物料</dt><dd>{{materialField(charge,'materialName')}} <small>（{{label('lot',charge.materialLotId)}}）</small></dd><dt>计划用量</dt><dd>{{weighings.find(w=>String(w.id)===String(charge.weighingRecordId))?.targetQty??'—'}} {{unitLabel(charge.unitId)}}</dd><dt>实际用量</dt><dd>{{charge.chargedQty}} {{unitLabel(charge.unitId)}}</dd><dt>投料时间</dt><dd>{{time(charge.chargedAt)}} <small>UTC</small></dd><dt>操作人</dt><dd>{{label('user',charge.chargedBy)}}</dd><dt>操作方法</dt><dd>—</dd><dt>状态</dt><dd><a-tag :color="charge.status==='CONFIRMED'?'green':'default'">{{charge.status==='CONFIRMED'?'已确认':display(charge.status)}}</a-tag></dd></dl></div></section></template><template v-else><h3>{{active?`${name(active)}记录`:'执行记录'}}</h3><IncomingFacts v-if="active" :label-formatter="fieldLabel" :value-formatter="valueLabel" :value="selectFacts(active,['operatorId','startedAt','completedAt'])"/><a-empty v-else description="暂无工序记录"/></template></a-tab-pane>
  <a-tab-pane key="material" tab="物料信息"><IncomingFacts :label-formatter="fieldLabel" :value-formatter="valueLabel" :value="{charges:selectedCharges,materials:snapshot.materials??[]}"/><a-space><a-button v-if="auth.can('mes:weigh:create')" @click="router.push(`/mes/execution/${execution.id}/weighing`)">称量</a-button><a-button v-if="auth.can('mes:charge:create')" @click="router.push(`/mes/execution/${execution.id}/charge`)">投料</a-button></a-space></a-tab-pane>
  <a-tab-pane key="equipment" tab="设备信息"><IncomingFacts :label-formatter="fieldLabel" :value-formatter="valueLabel" :value="{equipmentUsages:active?.equipmentUsages??[]}"/></a-tab-pane>
  <a-tab-pane key="parameters" tab="工艺参数"><IncomingFacts :label-formatter="fieldLabel" :value-formatter="valueLabel" :value="{parameters:definition?.parameters??[],parameterValues:active?.parameterValues??[]}"/></a-tab-pane>
  <a-tab-pane key="ipc" tab="IPC"><FunctionalQualityActions v-if="active" :key="String(active.id)" :row="active" kind="operation" @reload="emit('reload')"/><IncomingFacts v-if="active" :label-formatter="fieldLabel" :value-formatter="valueLabel" :value="selectFacts(active,['ipcInstances','clearanceRecords'])"/></a-tab-pane>
  <a-tab-pane key="exceptions" tab="异常记录"><IncomingFacts :label-formatter="fieldLabel" :value-formatter="valueLabel" :value="{gates:active?.gates??[],gateEvidence:active?.gateEvidence??[]}"/><p class="muted">本区显示现有工序校验与执行证据。</p></a-tab-pane>
 </a-tabs></a-card>
 <a-card class="quick-actions"><div class="operation-state" :class="{'state-warning':gates.length}"><SafetyCertificateOutlined/><div><strong>当前步骤{{state(active?.status)}}</strong><small>{{gates.length?'请核对工序校验依据':'受控操作需业务校验'}}</small></div></div><h3>快捷操作</h3><a-button v-for="action in active?actions(active):[]" :key="action" :aria-label="action==='complete'?'完成当前步骤':productionActions[action]" :type="action==='complete'?'primary':'default'" @click="emit('command',active!,action)"><CheckCircleOutlined v-if="action==='complete'"/>{{action==='complete'?'完成当前步骤':productionActions[action]}}</a-button><a-button v-if="canRecordDeviation" @click="recordDeviation">记录偏差</a-button><a-button @click="tab='ipc'"><ExperimentOutlined/>发起 IPC 检验</a-button><a-button @click="drawer='instruction'"><FileTextOutlined/>查看工艺指令</a-button></a-card></div>
 <div class="support-grid"><a-card title="本工序物料清单"><a-table :data-source="selectedCharges" :columns="materialColumns" :row-key="(r:Row)=>String(r.id)" :pagination="false" :scroll="{x:540}"><template #bodyCell="{column,record:row,index}"><span v-if="column.key==='index'">{{index+1}}</span><span v-else-if="column.key==='name'">{{materialField(row,'materialName')}}</span><span v-else-if="column.key==='specification'">{{materialField(row,'specification')}}</span><span v-else-if="column.dataIndex==='materialLotId'">{{label('lot',row.materialLotId)}}</span><span v-else-if="column.dataIndex==='unitId'">{{unitLabel(row.unitId)}}</span><span v-else-if="column.key==='target'">{{weighings.find(w=>String(w.id)===String(row.weighingRecordId))?.targetQty??'—'}}</span><a-tag v-else-if="column.key==='status'" :color="row.status==='CONFIRMED'?'green':'default'">{{display(row.status)}}</a-tag><a-button v-else-if="column.key==='action'" type="link" size="small" @click="inspectCharge(row)">查看</a-button></template><template #emptyText>暂无本工序投料记录</template></a-table></a-card><a-card title="关键工艺参数"><a-table :data-source="parameters" :columns="parameterColumns" :row-key="(r:Row)=>String(r.id)" :pagination="false" :scroll="{x:340}"><template #bodyCell="{column,record:row}"><span v-if="column.dataIndex==='unitId'">{{unitLabel(row.unitId)}}</span><span v-else-if="column.key==='name'">{{parameterDefinition(row)?.parameterName??row.parameterDefId}}</span><span v-else-if="column.key==='limits'">{{limits(row)}}</span><span v-else-if="column.key==='value'">{{row.rawValue??row.textValue??'—'}}</span><a-tag v-else-if="column.key==='status'" :color="parameterRange(row,parameterDefinition(row)).color" title="仅核对相同单位的冻结范围，工序完成以服务端校验为准">{{parameterRange(row,parameterDefinition(row)).text}}</a-tag></template><template #emptyText>暂无已记录参数</template></a-table></a-card></div>
 <a-drawer :open="!!drawer" :title="drawer==='ebr'?'电子批记录':drawer==='instruction'?'冻结工艺指令':'完整记录与审计证据'" width="680" @close="drawer=''"><template v-if="drawer==='ebr'"><a-button @click="drawer='evidence'">完整记录与审计证据</a-button><IncomingFacts :value="{executionNo:execution.executionNo,processSnapshotId:batch?.processSnapshotId??null}"/><a-button v-for="f in forms" :key="String(f.id)" type="link" @click="router.push(`/mes/execution/${execution.id}/forms/${f.id}`)"><FileTextOutlined/>{{f.formName}} · {{display(f.status)}}</a-button><a-empty v-if="!forms.length" description="此执行单元无表单记录"/></template><template v-else-if="drawer==='instruction'"><IncomingFacts :label-formatter="fieldLabel" :value-formatter="valueLabel" :value="{executionNo:execution.executionNo,processSnapshotId:batch?.processSnapshotId??null}"/><IncomingFacts v-if="definition" :label-formatter="fieldLabel" :value-formatter="valueLabel" :value="definition"/><a-empty v-else description="暂无可读取的冻结工序定义"/></template><template v-else><IncomingFacts v-if="active" :label-formatter="fieldLabel" :value-formatter="valueLabel" :value="active"/><section v-for="charge in selectedCharges" :key="String(charge.id)"><h3>完整投料记录与签名</h3><IncomingFacts :label-formatter="fieldLabel" :value-formatter="valueLabel" :value="charge"/><IncomingFacts :label-formatter="fieldLabel" :value="{signatures:charge.signatureEvidence??[]}"/></section></template></a-drawer>
</div></template>
<style scoped>
.execution-overview{width:100%;min-width:0}.workbench-heading{display:flex;justify-content:space-between;align-items:center;gap:16px;margin-bottom:16px}.heading-copy{display:flex;align-items:center;gap:16px}.template-badge{display:grid;place-items:center;width:40px;height:40px;border-radius:6px;background:#1677ff;color:#fff;font-size:22px;font-weight:600}.heading-copy h1{margin:0}.heading-copy p{margin:0;color:#78869b;font-size:13px}.context-panels{display:grid;grid-template-columns:minmax(0,1.2fr) minmax(0,1fr);gap:12px;margin-bottom:0}.production-context{display:flex;align-items:center;gap:16px}.production-icon{display:grid;place-items:center;width:56px;height:56px;flex-shrink:0;font-size:30px;background:#eaf3ff;color:#1677ff;border-radius:8px}.batch-context{min-width:0;font-size:13px}.batch-context>div{display:flex;align-items:center;gap:12px}.batch-context strong{font-size:15px}.batch-context p{display:flex;align-items:center;flex-wrap:wrap;gap:8px 12px;margin:8px 0 0;font-size:12px}.batch-context b{font-weight:500}.progress-context{display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1.3fr);align-items:center;gap:12px}.progress-context strong,.progress-context small,.progress-context span{display:block}.progress-context small,.progress-context span{font-size:12px;color:#78869b;line-height:20px}.progress-context strong{font-size:14px}.progress-value>small{font-size:12px}.process-navigation{display:flex;align-items:start;background:#fff;border:1px solid #e8edf3;border-radius:8px;margin:0 0 12px;padding:20px 12px 12px;overflow-x:auto;min-width:0}.process-navigation button{display:flex;align-items:start;gap:8px;flex:1;min-width:130px;border:0;background:transparent;cursor:pointer;text-align:left;font:inherit;padding:0 8px;position:relative}.process-navigation strong{font-size:12px;font-weight:600;white-space:nowrap}.process-navigation small{display:block;font-size:12px;color:#78869b;line-height:22px}.process-navigation .ant-tag{margin-top:6px}.step-number{display:inline-flex;justify-content:center;align-items:center;border-radius:50%;width:24px;height:24px;flex-shrink:0;background:#b5bfd2;color:#fff;font-weight:600;font-size:13px}.finished{font-size:24px;color:#30bd77}.active .step-number{background:#1677ff}.process-navigation .active .step-number{box-shadow:0 0 0 8px #edf5ff}.workbench-grid{display:grid;grid-template-columns:minmax(160px,.8fr) minmax(0,2.4fr) minmax(200px,1fr);gap:8px;align-items:stretch;margin-bottom:8px}.workbench-grid>*{min-width:0}.step-list button{width:100%;display:flex;align-items:center;gap:8px;border:0;border-radius:6px;padding:9px 4px;background:transparent;cursor:pointer;text-align:left;font:inherit;font-size:12px;white-space:nowrap}.step-list button.active{background:#edf4ff;color:#1677ff}.step-list .ant-tag{margin-left:auto;margin-right:0;padding-inline:5px;font-size:12px}.step-list .step-number,.step-list .finished{width:20px;height:20px;font-size:12px}.step-list .finished{font-size:20px}.execution-charge h3,.quick-actions h3,.primary-workspace h3{font-size:13px;font-weight:600;margin:0 0 12px;line-height:20px}.primary-workspace h3::before,.quick-actions h3::before{content:'';display:inline-block;width:3px;height:14px;border-radius:2px;background:#1677ff;vertical-align:middle;margin-right:8px}.charge-layout{display:grid;grid-template-columns:minmax(0,1.3fr) minmax(150px,1fr);gap:12px}.charge-facts{display:grid;grid-template-columns:80px minmax(0,1fr);gap:7px 8px;margin:0;font-size:12px;line-height:18px}.charge-facts dt{color:#78869b}.charge-facts dd{margin:0;overflow-wrap:anywhere}.charge-facts small{font-size:12px}.operation-state{display:flex;align-items:center;gap:10px;background:#f0faf5;border-radius:6px;padding:12px;margin-bottom:14px;min-height:64px;color:#319b68}.operation-state>.anticon{font-size:28px}.operation-state strong{font-size:13px}.operation-state small{display:block;font-size:12px;color:#78869b;margin-top:4px}.operation-state.state-warning{background:#fff7eb;color:#bd811d}.quick-actions .ant-btn{display:block;width:100%;margin-top:8px}.support-grid{display:grid;grid-template-columns:minmax(0,1.45fr) minmax(0,1fr);gap:8px}.support-grid>*{min-width:0}.evidence-link{padding-left:0;margin-top:8px;font-size:12px}
@media(min-width:901px){
 .workbench-heading{min-height:48px;margin:0 0 13px 12px}
 .context-panels{grid-template-columns:minmax(0,.96fr) minmax(0,1fr);gap:12px}
 .progress-context strong,.progress-context small,.progress-context span{line-height:18px}
 .charge-facts{gap:4px 8px;font-size:12px;line-height:18px}
 .charge-layout{align-items:start}
 .charge-facts{align-content:start}
 .charge-layout{grid-template-columns:minmax(0,1fr);gap:12px}
 .workbench-grid{grid-template-columns:minmax(0,.8fr) minmax(0,2.4fr) minmax(0,1fr);gap:8px;margin-bottom:8px}
 .primary-workspace{position:relative;min-height:254px}
 .process-navigation{min-height:98px;padding:20px 18px 12px;margin-bottom:13px}
 .support-grid{grid-template-columns:minmax(0,1.428fr) minmax(0,1fr)}
 .step-list button{padding-block:6px;position:relative}
 .operation-state{min-height:64px;padding:10px;margin-bottom:12px}
 .operation-state small{line-height:18px}
 .quick-actions h3{margin-bottom:8px}
 .quick-actions .ant-btn{margin-top:6px}
 #app .quick-actions :deep(.ant-btn){min-height:28px;height:28px}
 .progress-value :deep(.ant-progress){margin:0;line-height:1}
 .quick-actions :deep(.ant-tooltip-disabled-compatible-wrapper){display:block!important;width:100%}
 .quick-actions :deep(.ant-tooltip-disabled-compatible-wrapper .ant-btn){width:100%}
 #app .execution-overview :deep(.ant-card){margin-bottom:0}
 #app .execution-overview :deep(.ant-card-body){padding:12px}
 #app .execution-overview :deep(.ant-card-head){min-height:36px;padding:0 12px;border-bottom:0}
 #app .execution-overview :deep(.ant-card-head-title){padding:8px 0 0;font-size:13px;line-height:20px}
 #app .execution-overview :deep(.ant-card-head-title)::before{height:13px}
 #app .context-panels :deep(.ant-card-body){padding:9px 16px}
 #app .primary-workspace :deep(.ant-card-body){padding:0 12px 12px}
 #app .primary-workspace :deep(.ant-tabs-nav){margin-bottom:8px}
 .primary-workspace h3{margin-bottom:8px}
 #app .primary-workspace :deep(.ant-tabs-tab){font-size:12px;padding:10px 0}
 #app .primary-workspace :deep(.ant-tabs-tab)+:deep(.ant-tabs-tab){margin-left:18px}
 #app .primary-workspace :deep(.master-form){grid-template-columns:1fr;gap:6px}
 #app .primary-workspace :deep(.master-form>label){grid-template-columns:80px minmax(0,1fr);font-size:12px;gap:8px}
 #app .primary-workspace :deep(.master-read-value){padding:0;line-height:20px}
 #app .primary-workspace :deep(.form-field-label){font-size:12px;line-height:20px;padding:0;text-align:left}
 #app .support-grid :deep(.ant-table-cell){font-size:12px;padding:7px 6px;white-space:nowrap}
 #app .support-grid :deep(.ant-tag){font-size:12px;padding:0 4px;margin:0}
 #app .support-grid :deep(.ant-card-body){padding-top:6px}
 #app .execution-overview :deep(.ant-btn){font-size:12px}
 #app .execution-overview :deep(.ant-tag){line-height:20px;font-size:12px;border:0}
}

@media(min-width:1200px){
.workbench-grid{grid-template-columns:204px minmax(0,1fr) 252px}
}
@media(min-width:901px){
#app .heading-copy h1{font-size:20px;line-height:28px}
.process-navigation button{gap:14px}
.process-navigation button:not(:last-of-type)::after{content:'';position:absolute;top:11px;left:100px;right:-10px;height:3px;background:#dde3ed}
.process-navigation button.completed::after{background:#30bd77}
.process-navigation .step-number,.process-navigation .finished{position:relative;z-index:1}
.step-list button:not(:last-child)::after{content:'';position:absolute;left:13px;top:26px;bottom:-6px;width:2px;background:#dde3ed}
.step-list button.completed::after{background:#30bd77}
.step-list .step-number,.step-list .finished{position:relative;z-index:1}
.operation-state>.anticon{font-size:30px;color:inherit}
#app .support-grid :deep(.ant-btn-link){background:#eef5ff;min-height:22px;height:22px;padding:0 5px;line-height:22px}
#app .support-grid :deep(.ant-table-cell){padding:5px 6px}
#app .execution-overview :deep(.ant-tag){border-radius:4px;padding-inline:8px}
}

@media(min-width:901px){
.charge-facts{grid-template-columns:100px minmax(0,1fr);gap:4px 8px}
.charge-facts dd{font-weight:500}
#app .step-list :deep(.ant-card-body){padding:4px 8px 12px}
.step-list button{padding-inline:12px}
.step-list button:not(:last-child)::after{left:21px}
#app .quick-actions :deep(.ant-card-body){padding:14px 16px}
.operation-state{min-height:58px;margin-bottom:8px}
.quick-actions h3{margin-bottom:4px}
}

@media(min-width:901px){
#app .support-grid :deep(.ant-card-body){padding-bottom:4px}
#app .support-grid :deep(.ant-card-head){min-height:28px}
#app .support-grid :deep(.ant-card-head-title){padding:4px 0 0}
#app .support-grid :deep(.ant-table-cell){padding:2px 6px;line-height:20px}
#app .support-grid :deep(.ant-tag){line-height:20px}
}
</style>
