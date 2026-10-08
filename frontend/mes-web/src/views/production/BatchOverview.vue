<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {FileTextOutlined,CheckCircleFilled,SettingOutlined,SafetyCertificateOutlined,ExperimentOutlined,ApartmentOutlined,InboxOutlined,AuditOutlined,EllipsisOutlined,RightOutlined} from '@ant-design/icons-vue'
import {useRoute,useRouter} from 'vue-router'
import {api} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import IncomingFacts from '../quality/IncomingFacts.vue'
import {detailLabel,selectFacts,rows} from '../quality/incomingDetailModel'
import {display,type IncomingRow} from '../quality/incomingModel'
const props=defineProps<{record:IncomingRow;executions:IncomingRow[];reservations:IncomingRow[]}>(),router=useRouter(),route=useRoute(),auth=useAuthStore()
const references=ref<Record<string,IncomingRow>>({}),operations=ref<IncomingRow[]>([]),notices=ref<string[]>([])
const tab=ref(route.query.tab==='ebr'?'ebr':'basic'),evidence=ref('')
let generation=0
watch(()=>[props.record,props.executions],async()=>{
 const ticket=++generation;references.value={};operations.value=[];notices.value=[]
 const refs:Record<string,IncomingRow>={},ops:IncomingRow[]=[],warnings:string[]=[]
 const lookup=async(key:string,id:unknown,url:string,permission:string)=>{if(id==null||!auth.can(permission))return;try{refs[key]=await api<IncomingRow>({url:`${url}/${id}`})}catch{warnings.push('关联信息暂不可读取，保留来源引用。')}}
 await Promise.all([...(auth.can('qa:batch-review')?[api<IncomingRow>({url:`/qa/batches/${props.record.id}/review-model`}).then(r=>{refs.review=r}).catch(()=>{warnings.push('QA审查信息暂不可读取。')})]:[]),...(auth.can('balance:view')?[api<IncomingRow>({url:`/main-batches/${props.record.id}/material-balance`}).then(r=>{refs.balance=r}).catch(()=>{warnings.push('物料平衡信息暂不可读取。')})]:[]),...(auth.can('ebr:form:view')?[api<IncomingRow>({url:`/main-batches/${props.record.id}/ebr`}).then(r=>{refs.archive=r}).catch(()=>{warnings.push('电子批记录证据暂不可读取。')})]:[]),lookup('productId',props.record.productId,'/products','master:product:view'),lookup('productionOrderId',props.record.productionOrderId,'/production-orders','production:order:view'),lookup('unitId',props.record.unitId,'/units','master:uom:view'),...props.executions.map(async e=>{if(!auth.can('mes:operation:view'))return;try{const result=await api<IncomingRow[]>({url:`/execution-units/${e.id}/operations`});ops.push(...rows(result).map(o=>({...o,executionNo:e.executionNo})))}catch{warnings.push('工序进度暂不可读取。')}})])
 if(ticket===generation){references.value=refs;operations.value=ops;notices.value=[...new Set(warnings)]}
},{immediate:true})
const snapshot=computed(()=>((props.record.processSnapshot as IncomingRow|undefined)?.snapshot??{}) as IncomingRow)
const definition=computed(()=>rows(((snapshot.value.process as IncomingRow|undefined)?.route as IncomingRow|undefined)?.operations))
function value(key:string,v:unknown){const r=references.value[key];return r?String(r.productName??r.orderNo??r.unitName??r.id):display(v)}
function businessLabel(key:string){return ({subBatches:'子批计划',reservedQty:'预留数量',quantity:'数量'} as Record<string,string>)[key]??detailLabel(key)}
function operationName(r:IncomingRow){return definition.value.find(d=>String(d.operationDefId)===String(r.operationDefId))?.operationName??r.operationName??`工序 ${r.operationSeq??'—'}`}
function color(status:unknown){return ['COMPLETED','QA_RELEASED','IN_PROGRESS'].includes(String(status))?'green':status==='REJECTED'?'red':'blue'}

const product=computed(()=>references.value.productId??{})
const unit=computed(()=>String(references.value.unitId?.unitName??recordUnit()))
function recordUnit(){return props.record.unitId??'—'}
const executionDone=computed(()=>props.executions.filter(e=>e.status==='COMPLETED').length)
const operationDone=computed(()=>operations.value.filter(e=>e.status==='COMPLETED').length)
const progress=computed(()=>operations.value.length?Math.round(operationDone.value/operations.value.length*100):null)
const activeExecution=computed(()=>props.executions.find(e=>e.status==='IN_PROGRESS')??props.executions[0])
const archive=computed(()=>references.value.archive??{})
const balanceResults=computed(()=>rows(references.value.balance?.results).filter(r=>!rows(references.value.balance?.results).some(n=>n.balanceRuleId===r.balanceRuleId&&Number(n.calculationVersion)>Number(r.calculationVersion))))
const qcGate=computed(()=>rows(references.value.review?.gates).find(g=>g.code==='PRODUCTION_QC'))
const qaDone=computed(()=>['QA_RELEASED','REJECTED'].includes(String(props.record.status)))
function date(v:unknown){return v?String(v).slice(0,10):'—'}
function stamp(v:unknown){if(!v)return '—';const d=new Date(String(v));if(Number.isNaN(d.getTime()))return String(v);return new Intl.DateTimeFormat('zh-CN',{timeZone:'Asia/Shanghai',year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit',hour12:false}).format(d)}
function formatQty(v:unknown){const n=Number(v);return Number.isFinite(n)?new Intl.NumberFormat('zh-CN',{maximumFractionDigits:6}).format(n):display(v)}
function state(v:unknown){return ({IN_PROGRESS:'生产中',PENDING:'待开始',RELEASED:'已放行',PRODUCTION_COMPLETED:'生产完成',PENDING_QA:'待 QA 审核',QA_RELEASED:'已 QA 放行',REJECTED:'不合格'} as Record<string,string>)[String(v)]??display(v)}
const stages=computed(()=>[
 {title:'生产订单',date:date(references.value.productionOrderId?.createdAt),done:references.value.productionOrderId?.status==='COMPLETED',active:false,text:references.value.productionOrderId?(references.value.productionOrderId.status==='COMPLETED'?'已完成':'未完成'):'未读取',icon:FileTextOutlined},
 {title:'批创建',date:date(props.record.createdAt),done:!!props.record.createdAt,active:false,text:props.record.createdAt?'已创建':'日期未提供',icon:FileTextOutlined},
 {title:'物料准备',date:'—',done:false,active:false,text:props.reservations.length?`预留 ${props.reservations.length} 条`:'暂无预留',icon:InboxOutlined},
 {title:'生产执行',date:props.record.startedAt?date(props.record.startedAt):'—',done:!!props.record.completedAt,active:props.record.status==='IN_PROGRESS',text:props.record.completedAt?'已完成':state(props.record.status),icon:SettingOutlined},
 {title:'质量审核',date:'—',done:qcGate.value?.passed===true,active:false,text:qcGate.value?(qcGate.value.passed?'条件已满足':'条件未满足'):'未读取',icon:AuditOutlined},
 {title:'QA 放行',date:date(rows(archive.value.decisions).slice(-1)[0]?.decisionAt),done:props.record.status==='QA_RELEASED',active:false,text:qaDone.value?state(props.record.status):'未放行',icon:SafetyCertificateOutlined}
])
const modules=computed(()=>[
 {title:'执行单元',icon:FileTextOutlined,theme:'blue',value:`${executionDone.value} / ${props.executions.length}`,note:'已完成 / 已读取',percent:props.executions.length?Math.round(executionDone.value/props.executions.length*100):null,key:'execution'},
 {title:'工序进度',icon:ApartmentOutlined,theme:'green',value:`${operationDone.value} / ${operations.value.length}`,note:'已完成 / 已读取',percent:progress.value,key:'execution'},
 {title:'物料准备',icon:InboxOutlined,theme:'purple',value:`${props.reservations.length} 条`,note:'物料预留记录',percent:null,key:'materials'},
 {title:'质量状态',icon:ExperimentOutlined,theme:'orange',value:qcGate.value?(qcGate.value.passed?'条件已满足':'条件未满足'):'未读取',note:'IPC / QC 与质量调查',percent:null,key:'quality'},
 {title:'物料平衡',icon:SafetyCertificateOutlined,theme:'cyan',value:balanceResults.value.length?`${balanceResults.value.filter(r=>r.status==='PASS').length} / ${balanceResults.value.length}`:'暂无结果',note:'通过 / 当前结果',percent:null,key:'balance'},
 {title:'QA 放行',icon:SafetyCertificateOutlined,theme:'green',value:qaDone.value?state(props.record.status):'未放行',note:'独立质量决定',percent:null,key:'release'}
])
function openModule(key:string){if(key==='balance'&&auth.can('balance:view'))void router.push(`/production/batches/${props.record.id}/balance`);else if(key==='release'){if(auth.can('qa:batch-review'))qa();else evidence.value='release'}else tab.value=key}
function enterExecution(){if(activeExecution.value&&auth.can('mes:operation:view'))void router.push(`/mes/execution/${activeExecution.value.id}`)}
function qa(){void router.push(`/qa/batches/${props.record.id}/review`)}
const basic=computed(()=>[
 ['生产批号',props.record.batchNo],['产品名称',product.value.productName??props.record.productId],['产品编码',product.value.productCode],['剂型',product.value.dosageForm],['规格',product.value.specification],['计划产量',`${formatQty(props.record.plannedQty)} ${unit.value}`],['生产工单',references.value.productionOrderId?.orderNo??props.record.productionOrderId],['批次状态',state(props.record.status)],
 ['计划日期',date(props.record.plannedDate)],['实际开始',stamp(props.record.startedAt)],['实际完成',stamp(props.record.completedAt)],['创建时间',stamp(props.record.createdAt)]
])
</script>
<!-- UI Template: T4; screenshot-based PC aggregate, existing sources and owning command pages. -->
<template><section class="batch-overview" data-ui-template="T4">
 <a-button class="batch-back" @click="router.push({path:'/production/batches',query:route.query})">← 返回</a-button>
 <header class="batch-heading"><div><FileTextOutlined class="heading-icon"/><section><h1>生产批 360° 视图</h1><p>从生产计划到质量放行的全生命周期视图</p></section></div><a-space><a-button v-if="auth.can('ebr:form:view')&&auth.can('production:batch:view')&&auth.can('master:product:view')" class="batch-book-entry" @click="router.push(`/production/batches/${record.id}/book`)">打开生产批记录册</a-button><a-button v-if="record.status==='PRODUCTION_COMPLETED'&&auth.can('wms:finished-inbound:create')" @click="router.push({path:'/finished/inbound/create',query:{mainBatchId:String(record.id)}})">成品入库申请</a-button><a-button v-if="record.finishedLotId&&auth.can('wms:inventory:view')" @click="router.push('/wms/material-lots/'+record.finishedLotId)">成品库存与质量链</a-button><a-button v-if="auth.can('ebr:form:view')" @click="tab='ebr'"><FileTextOutlined/>批记录</a-button><a-button v-if="auth.can('balance:view')" @click="openModule('balance')">物料平衡</a-button><a-button v-if="auth.can('qa:batch-review')" @click="qa">QA审核</a-button><a-button v-if="activeExecution&&auth.can('mes:operation:view')" type="primary" @click="enterExecution">进入生产执行</a-button><a-dropdown><a-button aria-label="更多受控操作"><EllipsisOutlined/></a-button><template #overlay><div class="batch-controlled-menu"><slot name="actions"/><a-button @click="evidence='snapshot'">冻结工艺与证据</a-button><a-space wrap><a-button v-if="auth.can('trace:view')" @click="router.push({path:'/trace',query:{mainBatchId:String(record.id)}})">完整追溯</a-button><a-button v-if="auth.can('audit:view')" @click="router.push({path:'/audit',query:{objectType:'MainBatch',objectId:String(record.id)}})">审计追踪</a-button></a-space></div></template></a-dropdown></a-space></header>
 <a-card class="batch-summary" aria-label="批次摘要"><div class="summary-grid"><div class="summary-product"><FileTextOutlined class="product-document"/><section><h2>{{product.productName??record.productId}}</h2><p><span>生产批号</span><strong>{{record.batchNo}}</strong><a-tag :color="color(record.status)">{{state(record.status)}}</a-tag></p><dl><dt>产品编码</dt><dd>{{product.productCode??'—'}}</dd><dt>剂型</dt><dd>{{product.dosageForm??'—'}}</dd><dt>规格</dt><dd>{{product.specification??'—'}}</dd><dt>计划产量</dt><dd>{{formatQty(record.plannedQty)}} {{unit}}</dd><dt>生产工单</dt><dd class="span-three">{{references.productionOrderId?.orderNo??record.productionOrderId}}</dd></dl></section></div><dl class="summary-times"><dt>计划日期</dt><dd>{{date(record.plannedDate)}}</dd><dt>实际开始</dt><dd>{{stamp(record.startedAt)}}</dd><dt>实际完成</dt><dd>{{stamp(record.completedAt)}}</dd><dt>创建时间</dt><dd>{{stamp(record.createdAt)}}</dd></dl><div class="overall-progress"><a-progress type="circle" :percent="progress??0" :size="90"><template #format>{{progress==null?'—':progress+'%'}}</template></a-progress><section><strong>整体进度</strong><a-tag color="blue">{{state(record.status)}}</a-tag><small>{{operationDone}} / {{operations.length}} 工序已完成</small></section></div></div></a-card>
 <a-card class="batch-lifecycle"><h3>生产全生命周期</h3><ol><li v-for="(stage,index) in stages" :key="stage.title" :class="{complete:stage.done,active:stage.active}"><CheckCircleFilled v-if="stage.done" class="lifecycle-marker"/><component :is="stage.icon" v-else class="lifecycle-marker"/><RightOutlined v-if="index<stages.length-1" class="lifecycle-arrow"/><strong>{{stage.title}}</strong><small>{{stage.date}}</small><a-tag :color="stage.done?'green':stage.active?'blue':'default'">{{stage.text}}</a-tag></li></ol></a-card>
 <a-card class="batch-modules"><h3>关键模块概览</h3><div class="module-grid"><section v-for="module in modules" :key="module.title" class="module-card" :class="module.theme"><header><component :is="module.icon"/><strong>{{module.title}}</strong></header><b>{{module.value}}</b><small>{{module.note}}</small><a-progress :percent="module.percent??0" :show-info="module.percent!=null" :stroke-color="({blue:'#1677ff',green:'#30bd77',purple:'#9864e7',orange:'#edaa3c',cyan:'#23a4bc'} as Record<string,string>)[module.theme]"/><a-button type="link" @click="openModule(module.key)">查看详情 →</a-button></section></div></a-card>
 <div class="batch-bottom"><a-card class="batch-tabs"><a-tabs v-model:active-key="tab">
 <a-tab-pane key="basic" tab="基本信息"><div class="basic-grid"><dl v-for="part in [basic.slice(0,8),basic.slice(8)]" :key="String(part[0]?.[0])"><template v-for="[label,v] in part" :key="String(label)"><dt>{{label}}</dt><dd>{{v??'—'}}</dd></template></dl></div></a-tab-pane>
 <a-tab-pane key="materials" tab="物料列表"><a-empty v-if="!reservations.length" description="暂无物料预留"/><IncomingFacts v-for="r in reservations" :key="String(r.id)" :value="selectFacts(r,['materialLotId','reservedQty','quantity','unitId','status'])" :label-formatter="businessLabel"/></a-tab-pane>
 <a-tab-pane key="execution" tab="生产执行"><h3>执行单元</h3><a-table :data-source="executions" row-key="id" :pagination="false" :columns="[{title:'执行单元',dataIndex:'executionNo'},{title:'类型',dataIndex:'unitType'},{title:'状态',dataIndex:'status'},{title:'操作',key:'action'}]"><template #bodyCell="{column,record:r}"><a-button v-if="column.key==='action'&&auth.can('mes:operation:view')" type="link" @click="router.push(`/mes/execution/${r.id}`)">进入执行</a-button><span v-else>{{display(r[column.dataIndex])}}</span></template></a-table><h3>工序进度</h3><a-table :data-source="operations" row-key="id" :pagination="false" :scroll="{x:650}" :columns="[{title:'执行单元',dataIndex:'executionNo'},{title:'工序',key:'name'},{title:'状态',dataIndex:'status'},{title:'开始时间',dataIndex:'startedAt'},{title:'完成时间',dataIndex:'completedAt'}]"><template #bodyCell="{column,record:r}"><span>{{column.key==='name'?operationName(r):display(r[column.dataIndex])}}</span></template></a-table><h3>子批计划</h3><IncomingFacts :value="{subBatches:rows(record.subBatches).map(r=>selectFacts(r,['subBatchNo','plannedQty','status']))}"/></a-tab-pane>
 <a-tab-pane key="quality" tab="质量控制"><slot name="quality"/></a-tab-pane>
 <a-tab-pane key="balance" tab="物料平衡"><IncomingFacts :value="{results:balanceResults}"/><a-button v-if="auth.can('balance:view')" @click="openModule('balance')">查看物料平衡</a-button></a-tab-pane>
 <a-tab-pane key="files" tab="文件记录"><IncomingFacts :value="{pdfManifests:archive.pdfManifests??[]}"/></a-tab-pane>
 <a-tab-pane key="ebr" tab="电子批记录"><IncomingFacts :value="{forms:archive.forms??[]}"/><a-button v-if="auth.can('qa:batch-review')" @click="qa">查看批审与归档证据</a-button></a-tab-pane>
 <a-tab-pane key="changes" tab="变更与偏差"><IncomingFacts :value="{deviations:auth.can('qms:deviation:view')?archive.deviations??[]:[]}"/><p v-if="!auth.can('qms:deviation:view')">需要质量调查查看权限。</p></a-tab-pane>
 <a-tab-pane key="related" tab="相关批次"><IncomingFacts :value="{subBatches:record.subBatches??[]}"/></a-tab-pane>
 </a-tabs><a-alert v-for="notice in notices" :key="notice" :message="notice" type="info"/></a-card><aside><a-card class="batch-notes"><h3>生产批次备注</h3><p>现有生产批契约未提供备注。</p></a-card><a-card class="batch-files"><h3>相关文件</h3><template v-if="rows(archive.pdfManifests).length"><a-button v-for="pdf in rows(archive.pdfManifests)" :key="String(pdf.id)" type="link" @click="tab='files'"><FileTextOutlined/>eBR PDF V{{pdf.generationVersion}} · {{date(pdf.generatedAt)}}</a-button></template><p v-else>暂无可读取的归档文件</p><p class="muted">产品图片、任意文件上传尚无本批次契约。</p></a-card></aside></div>
<a-drawer :open="!!evidence" :title="evidence==='snapshot'?'冻结工艺与证据':'QA放行'" width="680" @close="evidence=''"><template v-if="evidence==='snapshot'"><details open><summary>查看冻结工艺快照</summary><IncomingFacts :value="{processSnapshot:record.processSnapshot}"/></details></template><template v-else><p>生产完成与QA放行是独立受控阶段。</p><a-tag :color="color(record.status)">{{state(record.status)}}</a-tag><p>需要QA批审权限才能查看放行证据。</p></template></a-drawer></section></template>
<style scoped>
.batch-overview{min-width:0}.batch-back{margin-bottom:10px}.batch-heading{display:flex;align-items:center;justify-content:space-between;margin-bottom:14px;gap:12px}.batch-heading>div{display:flex;gap:18px;align-items:center}.heading-icon{font-size:30px;color:#1677ff;background:#e6f2ff;border-radius:8px;padding:12px}.batch-heading h1{font-size:22px;font-weight:700;line-height:30px;margin:0}.batch-heading p{font-size:12px;color:#718099;margin:0}.batch-controlled-menu{background:#fff;border:1px solid #e8edf3;padding:16px;border-radius:8px;max-width:500px}.batch-overview h3{font-size:13px;font-weight:600;margin:0 0 10px;line-height:18px}.batch-overview h3::before{content:'';display:inline-block;vertical-align:middle;width:3px;height:13px;background:#1677ff;border-radius:2px;margin-right:8px}.batch-overview :deep(.ant-card){border-color:#e8edf3;border-radius:8px;margin-bottom:10px}.batch-overview :deep(.ant-card-body){padding:12px}.summary-grid{display:grid;grid-template-columns:1.65fr 1fr 1fr;align-items:center;min-height:112px;gap:20px}.summary-product{display:flex;align-items:center;gap:18px}.product-document{font-size:48px;color:#1677ff;background:#edf4fa;border-radius:6px;padding:25px}.summary-product section{flex:1}.summary-product h2{font-size:16px;font-weight:700;margin:0 0 6px}.summary-product p{display:flex;align-items:center;gap:14px;margin:0 0 5px;font-size:12px}.summary-product p span,.summary-grid dt{color:#70809a}.summary-product strong{font-size:14px}.summary-product dl{display:grid;grid-template-columns:68px 1fr 68px 1fr;gap:4px 8px;font-size:12px;line-height:18px;margin:0}.summary-grid dd{margin:0}.span-three{grid-column:span 3}.summary-times{display:grid;grid-template-columns:65px 1fr;gap:4px 8px;margin:0;font-size:12px;border-left:1px solid #f0f2f5;padding-left:18px}.overall-progress{display:flex;align-items:center;gap:16px;border-left:1px solid #f0f2f5;padding-left:20px}.overall-progress section{display:grid;gap:8px}.overall-progress strong{font-size:13px}.overall-progress small{font-size:12px;color:#7b89a0}.batch-lifecycle ol{display:flex;list-style:none;margin:0;padding:0 0 2px}.batch-lifecycle li{flex:1;display:flex;align-items:center;flex-direction:column;position:relative;gap:5px;font-size:12px}.lifecycle-marker{font-size:26px;color:#aeb9cc;background:#eff3f8;border-radius:50%;padding:3px}.batch-lifecycle li::after{content:'';position:absolute;height:2px;top:15px;left:calc(50% + 24px);right:calc(-50% + 24px);background:#e0e5ed}.batch-lifecycle li:last-child::after{display:none}.batch-lifecycle .complete::after{background:#39be80}.complete .lifecycle-marker{color:#30bd77;background:#eaf9ef}.active .lifecycle-marker{color:white;background:#1677ff}.batch-lifecycle small{color:#7b89a0}.module-grid{display:grid;grid-template-columns:repeat(6,minmax(0,1fr));gap:10px}.module-card{background:#f4f8ff;border:1px solid #e9eef5;border-radius:6px;padding:12px 10px 0;min-width:0}.module-card.green{background:#f0faf5}.module-card.purple{background:#f7f3ff}.module-card.orange{background:#fff9ef}.module-card.cyan{background:#f0fafc}.module-card header{display:flex;gap:10px;align-items:center;font-size:12px;margin-bottom:8px}.module-card header .anticon{font-size:23px;color:#1677ff}.module-card.green header .anticon{color:#32b574}.module-card.purple header .anticon{color:#9864e7}.module-card.orange header .anticon{color:#edaa3c}.module-card.cyan header .anticon{color:#23a4bc}.module-card b{font-size:16px;line-height:20px;display:block}.module-card small{font-size:12px;color:#75849b;display:block}.module-card :deep(.ant-progress){margin:4px 0}.module-card :deep(.ant-btn){width:100%;height:26px;min-height:26px;padding:0;font-size:12px;border-top:1px solid #edf1f6}.batch-bottom{display:grid;grid-template-columns:minmax(0,2fr) minmax(0,1fr);gap:10px}.batch-bottom>*{min-width:0}.batch-tabs :deep(.ant-card-body){padding:0 12px 12px}.batch-tabs :deep(.ant-tabs-nav){margin-bottom:10px}.batch-tabs :deep(.ant-tabs-tab){font-size:12px;padding:8px 0}.batch-tabs :deep(.ant-tabs-tab + .ant-tabs-tab){margin-left:14px}.basic-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px}.basic-grid dl{display:grid;grid-template-columns:90px 1fr;gap:6px 8px;font-size:12px;margin:0;line-height:18px}.basic-grid dt{color:#6f7e95}.basic-grid dd{margin:0;overflow-wrap:anywhere}.basic-grid dl+dl{border-left:1px solid #f0f2f5;padding-left:20px}.batch-notes p,.batch-files p{font-size:12px;color:#718099;margin:0;line-height:20px}.batch-files :deep(.ant-btn){display:block;padding:0;font-size:12px}.batch-tabs :deep(.ant-table-cell){font-size:12px;padding:8px}.batch-overview :deep(.ant-tag){font-size:12px;line-height:18px;margin:0;border:0;border-radius:3px}.batch-heading :deep(.ant-btn){font-size:12px}.batch-summary :deep(.ant-tag-green){color:#238c58;background:#e0f8e9}.batch-tabs{overflow:hidden}.muted{margin-top:12px!important}
@media(max-width:900px){.summary-grid{grid-template-columns:1fr}.module-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.batch-bottom{grid-template-columns:1fr}.batch-heading{flex-wrap:wrap}.batch-lifecycle{overflow:auto}.batch-lifecycle ol{min-width:650px}.basic-grid{grid-template-columns:1fr}.basic-grid dl+dl{border:0;padding:0}.summary-product{flex-wrap:wrap}}

@media(min-width:901px){
#app .batch-overview .batch-back{min-height:26px;height:26px;margin-bottom:8px;font-size:12px;padding-inline:12px}
#app .batch-overview .batch-heading{margin-bottom:15px}
#app .batch-overview .heading-icon{font-size:26px;padding:11px}
#app .batch-overview :deep(.ant-card-body){padding:10px 12px}
#app .batch-overview .batch-summary :deep(.ant-card-body){padding:12px}
#app .batch-overview .summary-grid{min-height:113px}
#app .batch-overview .batch-lifecycle h3,#app .batch-overview .batch-modules h3{margin-bottom:8px}
#app .batch-overview .batch-lifecycle li{gap:3px;line-height:18px}
#app .batch-overview .lifecycle-marker{font-size:24px;padding:2px}
#app .batch-overview .batch-lifecycle li::after{top:13px}
#app .batch-overview .batch-lifecycle small{line-height:16px}
#app .batch-overview :deep(.ant-tag){min-height:18px;line-height:18px;font-size:12px;padding:0 6px}
#app .batch-overview .module-card{padding:10px 10px 0}
#app .batch-overview .module-card header{height:28px;margin-bottom:6px}
#app .batch-overview .module-card small{line-height:16px}
#app .batch-overview .module-card :deep(.ant-progress){margin:4px 0;line-height:1}
#app .batch-overview .module-card :deep(.ant-btn){height:26px;min-height:26px;font-size:12px;line-height:24px;padding:0}
#app .batch-overview .batch-tabs :deep(.ant-card-body){padding:0 12px 12px}
#app .batch-overview .batch-tabs :deep(.ant-tabs-tab){font-size:12px;padding:8px 0}
#app .batch-overview .batch-tabs :deep(.ant-tabs-tab + .ant-tabs-tab){margin-left:12px}
#app .batch-overview .batch-tabs :deep(.ant-tabs-nav){margin-bottom:10px}
}
@media(min-width:901px){
#app .batch-overview .summary-product h2{line-height:20px;margin-bottom:4px}
#app .batch-overview .summary-product p{line-height:18px;margin-bottom:4px}
#app .batch-overview .batch-lifecycle h3{margin-bottom:6px}
#app .batch-overview .batch-lifecycle ol{padding-bottom:1px}
#app .batch-overview .module-card{padding-top:8px}
#app .batch-overview .module-card header{height:24px;margin-bottom:4px}
#app .batch-overview .batch-modules h3{margin-bottom:7px}
}
@media(min-width:901px){
#app .batch-overview .lifecycle-arrow{position:absolute;top:10px;right:calc(-50% + 20px);font-size:8px;color:#e0e5ed;z-index:1}
#app .batch-overview .complete .lifecycle-arrow{color:#39be80}
#app .batch-overview .active .lifecycle-marker{background:#1677ff;color:#fff}
}
@media(min-width:901px){
#app .batch-overview .basic-grid{align-items:start}
#app .batch-overview .basic-grid dl{line-height:16px;gap:4px 8px}
#app .batch-overview .overall-progress :deep(.ant-progress-text){font-weight:700}
}
</style>
