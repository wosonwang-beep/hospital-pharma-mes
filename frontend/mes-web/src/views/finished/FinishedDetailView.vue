<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {FileTextOutlined,CheckCircleOutlined,CloseCircleOutlined,InfoCircleOutlined} from '@ant-design/icons-vue'
import {useRoute,useRouter,onBeforeRouteLeave} from 'vue-router'
import {api} from '../../api/http'
import {useControlledRequest} from '../../api/controlled'
import {useAuthStore} from '../../stores/auth'
import IncomingReferencePicker from '../quality/IncomingReferencePicker.vue'
import ReadReference from '../master/ReadReference.vue'
import FinishedReferencePicker from './FinishedReferencePicker.vue'
import {definitions,labels,status,commandBody,commands,canAct,inboundCreateBody,type Row,type Command} from './model'
const route=useRoute(),router=useRouter(),auth=useAuthStore(),request=useControlledRequest(),{error,busy,conflict}=request,record=ref<Row>({}),form=ref<Row>({}),action=ref(''),password=ref(''),signatureOpen=ref(false)
const linkedInspection=ref<Row|null>(null),linkedInspectionError=ref('')
const def=computed(()=>definitions[String(route.meta.resource)]!),create=computed(()=>route.meta.mode==='create'),command=computed<Command|undefined>(()=>create.value?def.value.create:action.value==='RECEIVE_SAMPLE'?commands.reason:def.value.actions[action.value]?.command),actions=computed(()=>Object.entries(def.value.actions).filter(([key,a])=>canAct(record.value,key,auth.can(a.permission)))),sample=computed(()=>record.value.sample as Row|undefined),resultItems=computed(()=>record.value.summary?.items??[])
const signed=computed(()=>command.value?.signed),source=computed(()=>action.value==='RECEIVE_SAMPLE'?sample.value!:record.value)
const factGroups=computed(()=>[
 {title:'来源信息',fields:['mainBatchId','productId','inboundRequestId','inspectionRequestId','materialLotId']},
 {title:route.meta.resource==='sampling'?'取样信息':route.meta.resource==='reports'?'质量标准':'单据信息',fields:['quantity','unitId','locationId','receivingParty','samplingLocation','samplingMethod','qcSpecificationVersionId']}
].map(group=>({...group,fields:group.fields.filter(field=>record.value[field]!=null)})).filter(group=>group.fields.length))
const flowRecords=computed(()=>[
 {title:'创建记录',actor:'createdBy',time:'createdAt'},
 {title:'提交记录',actor:'submittedBy',time:'submittedAt'},
 {title:route.meta.resource==='shipments'?'发货确认':'仓库确认',actor:'confirmedBy',time:'confirmedAt'},
 {title:'QC接收',actor:'acceptedBy',time:'acceptedAt'},
 {title:'取样执行',actor:'sampledBy',time:'sampledAt'},
 {title:'报告生成',actor:'generatedBy',time:'generatedAt'},
 {title:'报告审批',actor:'approvedBy',time:'approvedAt'}
].filter(entry=>record.value[entry.actor]!=null||record.value[entry.time]!=null))
const formGroups=computed(()=>[
 {title:create.value?'基本信息':'执行信息',fields:command.value?.fields.filter(field=>!['locationId','quantity','unitId','receivingParty','reason','samplingMethod'].includes(field))??[]},
 {title:action.value==='SAMPLE'?'取样数量':'数量与库位',fields:command.value?.fields.filter(field=>['locationId','quantity','unitId','receivingParty'].includes(field))??[]},
 {title:action.value==='SAMPLE'?'取样方法与原因':'操作说明',fields:command.value?.fields.filter(field=>['samplingMethod','reason'].includes(field))??[]}
].filter(group=>group.fields.length))
const actionTitle=computed(()=>create.value?'新增'+def.value.title:action.value==='RECEIVE_SAMPLE'?'QC接收样品':def.value.actions[action.value]?.title)
function tone(value:string){return ['PASS','APPROVED','COMPLETED','CONFIRMED','RECEIVED'].includes(value)?'green':['FAIL','CANCELLED'].includes(value)?'red':['SUBMITTED','GENERATED'].includes(value)?'orange':['ACCEPTED','IN_TEST'].includes(value)?'blue':undefined}
function factName(field:string){return field==='mainBatchId'?record.value.batch?.batchNo:field==='materialLotId'?record.value.lot?.lotNo:undefined}
function displayTime(value:unknown){return value==null?'—':String(value).replace('T',' ').replace(/Z$/,' UTC')}
const refFields=new Set(['mainBatchId','locationId','unitId'])
function start(key:string){request.clear();action.value=key;form.value={};password.value=''}
async function load(){request.clear();linkedInspection.value=null;linkedInspectionError.value='';try{record.value=create.value?{}:await api({url:def.value.api+'/'+route.params.id});if(!create.value){const row=record.value;record.value={...row,mainBatchId:row.mainBatchId??row.summary?.mainBatchId??row.sample?.mainBatchId,materialLotId:row.materialLotId??row.summary?.materialLotId,qcSpecificationVersionId:row.qcSpecificationVersionId??row.summary?.qcSpecificationVersionId};}if(!create.value&&route.meta.resource==='inbound'&&auth.can('qms:finished-request:view')){try{const list=await api<{items:Row[]}>({url:definitions.requests!.api,params:{mainBatchId:record.value.mainBatchId,page:0,size:20}});linkedInspection.value=list.items.find(r=>String(r.inboundRequestId)===String(record.value.id))??null}catch{linkedInspectionError.value='关联请验单暂不可读取'}}form.value={...(route.query.mainBatchId?{mainBatchId:String(route.query.mainBatchId)}:{}),...(route.query.inboundRequestId?{inboundRequestId:String(route.query.inboundRequestId)}:{})};action.value=''}catch(e){await request.failure(e)}}
onBeforeRouteLeave(()=>!command.value||!Object.values(form.value).some(Boolean)||window.confirm('离开将放弃未保存的输入，是否继续？'))
function buildBody(){return create.value&&route.meta.resource==='inbound'?inboundCreateBody(form.value):commandBody(command.value!,form.value,create.value?undefined:source.value.versionNo)}
async function prepare(){try{buildBody();if(signed.value)signatureOpen.value=true;else await save()}catch(e){await request.failure(e)}}
async function save(){busy.value=true;try{const body=buildBody();if(signed.value){if(!password.value)throw Error('请输入签名密码');const objectId=record.value.id+':'+record.value.versionNo;const r=await api<{reauthToken:string}>({url:'/auth/reauth',method:'POST',data:{objectType:signed.value.type,objectId,recordVersion:record.value.versionNo,meaning:signed.value.meaning,credential:password.value}});body.signature={reauthToken:r.reauthToken}}
const url=create.value?def.value.api:action.value==='RECEIVE_SAMPLE'?'/samples/'+sample.value!.id+'/receive':def.value.api+'/'+record.value.id+'/'+def.value.actions[action.value]!.path;const a=action.value,row=await request.mutate<Row>(url,'POST',body,create.value?undefined:source.value.versionNo);form.value={};signatureOpen.value=false;action.value='';if(create.value)await router.replace(def.value.path+'/'+row.id);else if(a==='SAMPLE')await router.push('/finished/sampling/'+row.id);else if(a==='GENERATE_REPORT')await router.push('/finished/reports/'+row.id);else await load()}catch(e){await request.failure(e)}finally{busy.value=false;password.value=''}}
function refField(field:string){return field.endsWith('By')?'userId':field==='inspectionRequestId'?'finishedInspectionRequestId':field}
watch(()=>route.fullPath,()=>void load(),{immediate:true})
</script>
<template>
 <main :data-ui-template="create?'T2':'T3'" class="admin-page master-page finished-detail">
  <a-button class="finished-back" @click="router.push({path:route.meta.resource==='inbound'&&route.query.receivingOnly==='true'?'/finished/receiving':def.path,query:route.query})">← 返回列表</a-button>
  <header class="admin-page-header finished-header">
   <div class="finished-object">
    <div class="finished-document-icon" aria-hidden="true"><FileTextOutlined/></div>
    <div><div class="finished-heading"><h1>{{create?'新增'+def.title:def.title}}</h1><strong v-if="!create" class="finished-number">{{record[def.number]}}</strong><a-tag v-if="!create" :color="tone(record.status)">{{status[record.status]??'取样已记录'}}</a-tag></div><p>{{create?'填写业务信息，按现有规则提交受控单据':'成品管理 · '+def.title}}</p></div>
   </div>
   <a-space v-if="!create" wrap class="finished-header-actions"><a-button v-for="[key,a] in actions" :key="key" :type="key==='CONFIRM'||key==='APPROVE'?'primary':'default'" :danger="key==='CANCEL'" @click="start(key)">{{a.title}}</a-button><a-button @click="load">刷新</a-button></a-space>
  </header>
  <a-alert v-if="error" :message="error" type="error" show-icon/>
  <a-alert v-if="conflict" message="记录已变化，请刷新后重新核对。" type="warning"/>

  <a-card v-if="!create&&factGroups.length" title="基本信息" class="form-section finished-facts">
   <div class="finished-fact-groups"><section v-for="group in factGroups" :key="group.title"><h2>{{group.title}}</h2><dl class="finished-fact-list"><template v-for="field in group.fields" :key="field"><dt>{{field==='inboundRequestId'?'关联成品入库申请':labels[field]}}</dt><dd><ReadReference v-if="field.endsWith('Id')" :field="refField(field)" :value="record[field]" fallback="暂无可读业务名称" :name="factName(field)"/><span v-else>{{record[field]}}</span></dd></template></dl></section></div>
   <a-space wrap class="finished-source-actions"><a-button v-if="record.materialLotId&&auth.can('wms:inventory:view')" @click="router.push('/wms/material-lots/'+record.materialLotId)">成品批次与库存追溯</a-button><a-button v-if="record.mainBatchId&&auth.can('qa:batch-review')" @click="router.push('/qa/batches/'+record.mainBatchId+'/review')">QA批审核</a-button><a-button v-if="record.inspectionRequestId&&auth.can('qms:finished-request:view')" @click="router.push('/finished/requests/'+record.inspectionRequestId)">来源成品请验单</a-button></a-space>
  </a-card>
  <a-card v-if="linkedInspection" title="关联成品请验草稿 / 请验单" class="form-section"><div class="finished-related-record"><a-button type="link" @click="router.push('/finished/requests/'+linkedInspection.id)">{{linkedInspection.inspectionRequestNo}}</a-button><a-tag :color="tone(linkedInspection.status)">{{status[linkedInspection.status]}}</a-tag></div><p class="finished-help">仓库确认接收后才能提交请验和执行QC。</p></a-card>
  <a-alert v-if="linkedInspectionError" type="warning" :message="linkedInspectionError" class="form-section"/>
  <a-alert v-if="route.meta.resource==='requests'&&record.status==='DRAFT'" type="info" message="仓库确认接收后才能提交请验和执行QC。" class="form-section"/>
  <a-card v-if="record.samplingRecords?.length" title="取样记录与样品" class="form-section"><a-table :data-source="record.samplingRecords" :columns="[{title:'取样编号',dataIndex:'samplingNo'},{title:'样品',key:'sample'},{title:'位置',dataIndex:'samplingLocation'},{title:'数量',dataIndex:'quantity'},{title:'取样时间（UTC）',key:'time'},{title:'操作',key:'open'}]" row-key="id" :pagination="false" :scroll="{x:750}"><template #bodyCell="{column,record:r}"><ReadReference v-if="column.key==='sample'" field="productionSampleId" :value="r.sampleId" :name="r.sample?.sampleNo" fallback="暂无可读样品编号"/><span v-else-if="column.key==='time'">{{displayTime(r.sampledAt)}}</span><a-button v-else-if="column.key==='open'&&auth.can('qms:finished-sampling:view')" type="link" @click="router.push('/finished/sampling/'+r.id)">查看取样与样品</a-button></template></a-table></a-card>
  <a-card v-if="sample" title="生成的成品样品" class="form-section"><dl class="finished-fact-list finished-sample-facts"><dt>样品编号</dt><dd>{{sample.sampleNo}}</dd><dt>样品类型</dt><dd>{{status[sample.sampleType]}}</dd><dt>样品状态</dt><dd><a-tag :color="tone(sample.status)">{{({COLLECTED:'已取样',RECEIVED:'已接收',IN_TEST:'检验中'} as Record<string,string>)[sample.status]??sample.status}}</a-tag></dd><dt>取样数量</dt><dd>{{sample.quantity}} <ReadReference field="unitId" :value="sample.unitId" fallback="暂无可读单位"/></dd></dl><a-space wrap class="finished-source-actions"><a-button v-if="sample.allowedActions?.includes('RECEIVE')&&auth.can('qms:sample:receive')" @click="start('RECEIVE_SAMPLE')">QC接收样品</a-button><a-button v-if="['RECEIVED','IN_TEST'].includes(sample.status)&&auth.can('qms:test:record')" @click="router.push({path:'/quality/production-tests/create',query:{mainBatchId:sample.mainBatchId,sampleId:sample.id}})">执行成品检验</a-button></a-space></a-card>
  <a-card v-if="record.reports?.length" title="报告历史" class="form-section"><div v-for="r in record.reports" :key="r.id" class="finished-history-row"><a-button v-if="auth.can('qms:finished-report:view')" type="link" @click="router.push('/finished/reports/'+r.id)">{{r.reportNo}}</a-button><span v-else>{{r.reportNo}}</span><a-tag :color="tone(r.status)">{{status[r.status]}}</a-tag><a-tag :color="tone(r.overallConclusion)">{{status[r.overallConclusion]}}</a-tag><time>{{displayTime(r.generatedAt)}}</time></div></a-card>
  <a-card v-if="record.summary" title="检验项目与结果" class="form-section"><p class="finished-help">由当前请验冻结的质量标准和已有检验结果汇总生成。</p><a-table :data-source="resultItems" :columns="[{title:'检验项目',key:'item'},{title:'标准要求',key:'standard'},{title:'实际结果',key:'result'},{title:'单位',key:'unit'},{title:'判定',key:'conclusion'}]" :row-key="(r:Row)=>r.standard.specificationItemId" :pagination="false" :scroll="{x:800}"><template #bodyCell="{column,record:r}"><span v-if="column.key==='item'">{{r.standard.itemName??r.standard.testCode}}</span><span v-else-if="column.key==='standard'">{{r.standard.textAcceptanceCriteria??[r.standard.lowerLimit??'—',r.standard.upperLimit??'—'].join(' ～ ')}}</span><span v-else-if="column.key==='result'">{{r.effectiveResult?.resultNumeric??r.effectiveResult?.resultText??'—'}}</span><ReadReference v-else-if="column.key==='unit'" field="unitId" :value="r.standard.unitId" fallback="暂无可读单位"/><a-tag v-else-if="column.key==='conclusion'" :color="tone(r.conclusion)">{{status[r.conclusion]??'未检验'}}</a-tag></template></a-table></a-card>
  <a-card v-if="record.overallConclusion" title="检验结论" class="form-section"><div class="finished-conclusion" :class="{'conclusion-pass':record.overallConclusion==='PASS','conclusion-fail':record.overallConclusion==='FAIL'}"><CheckCircleOutlined v-if="record.overallConclusion==='PASS'"/><CloseCircleOutlined v-else-if="record.overallConclusion==='FAIL'"/><InfoCircleOutlined v-else/><div><strong>综合判定：{{status[record.overallConclusion]??record.overallConclusion}}</strong><p>结论来自现有检验报告；成品发货资格仍以QA放行及可用库存为准。</p></div></div></a-card>
  <a-card v-if="route.meta.resource==='reports'" title="独立复核与电子签名" class="form-section"><section v-for="r in record.reviews" :key="r.id" class="finished-review"><dl class="finished-fact-list"><dt>复核人</dt><dd><ReadReference field="userId" :value="r.reviewedBy" fallback="暂无可读人员名称"/></dd><dt>复核时间</dt><dd>{{displayTime(r.reviewedAt)}}</dd><dt>复核决定</dt><dd>{{r.reviewDecision==='APPROVE'?'批准':status[r.reviewDecision]??r.reviewDecision??'—'}}</dd><dt>复核原因</dt><dd>{{r.reason??'—'}}</dd></dl><details class="finished-evidence"><summary>本次复核的签名元数据</summary><p>电子签名引用：{{r.signatureId??'—'}}</p></details></section><p v-if="!record.reviews?.length" class="finished-help finished-empty">暂无复核记录</p></a-card>

  <a-card v-if="!create&&flowRecords.length" title="流转记录" class="form-section"><div class="finished-flow"><section v-for="entry in flowRecords" :key="entry.time"><h3>{{entry.title}}</h3><ReadReference field="userId" :value="record[entry.actor]" fallback="暂无可读人员名称"/><time>{{displayTime(record[entry.time])}}</time></section></div></a-card>
  <a-card v-if="!create&&record.reason" title="备注 / 原因" class="form-section"><p class="finished-note">{{record.reason}}</p></a-card>

  <a-card v-if="command" :title="actionTitle" class="form-section finished-command"><form @submit.prevent="prepare">
   <section v-for="group in formGroups" :key="group.title" class="finished-form-group"><h2>{{group.title}}</h2><div class="master-form"><label v-for="field in group.fields" :key="field" :class="{'form-full-row':['reason','samplingMethod'].includes(field)}"><span class="form-field-label">{{labels[field]}} <span class="finished-required">*</span></span><FinishedReferencePicker v-if="field==='inboundRequestId'" v-model="form[field]" :disabled="busy"/><IncomingReferencePicker v-else-if="refFields.has(field)" :field="field" v-model="form[field]" :context="form" :query-filters="field==='mainBatchId'?{status:String(route.meta.resource)==='shipments'?'QA_RELEASED':'PRODUCTION_COMPLETED'}:undefined" :disabled="busy" required :input-label="labels[field]"/><select v-else-if="field==='sampleType'" v-model="form[field]" aria-label="样品类型" class="master-native-input" required><option v-for="s in ['TEST_SAMPLE','RETENTION_SAMPLE','RETEST_SAMPLE','OTHER_APPROVED']" :key="s" :value="s">{{status[s]}}</option></select><textarea v-else-if="['reason','samplingMethod'].includes(field)" v-model="form[field]" class="master-native-input" :aria-label="labels[field]" required maxlength="1000" :disabled="busy"/><input v-else v-model="form[field]" class="master-native-input" :aria-label="labels[field]" required :maxlength="field==='receivingParty'||field==='samplingLocation'?200:80" :disabled="busy"/></label></div></section>
   <section v-if="create&&route.meta.resource==='inbound'&&auth.can('qms:finished-request:create')" class="finished-form-group"><h2>关联成品请验</h2><div class="master-form"><label class="form-full-row"><span class="form-field-label">成品请验</span><span class="finished-checkbox"><input v-model="form.createInspectionDraft" type="checkbox" aria-label="同时生成成品请验草稿" :disabled="busy"/> 同时生成成品请验草稿</span></label><label v-if="form.createInspectionDraft"><span class="form-field-label">成品请验单号 <span class="finished-required">*</span></span><input v-model="form.inspectionRequestNo" class="master-native-input" aria-label="成品请验单号" required maxlength="80" :disabled="busy"/></label><p v-if="form.createInspectionDraft" class="form-full-row finished-help">仅生成关联草稿；仓库确认接收后才能提交请验和执行QC。</p></div></section>
   <div class="finished-actions"><a-button :disabled="busy" @click="form={};action='';create&&router.push(def.path)">取消</a-button><a-button type="primary" html-type="submit" :loading="busy" :disabled="conflict">{{signed?'核对并签名':'提交'}}</a-button></div>
  </form></a-card>
  <details v-if="!create" class="finished-metadata"><summary>更多记录与审计证据</summary><div class="finished-evidence-body"><section v-if="record.summary"><h3>原始结果及修订证据</h3><section v-for="r in resultItems" :key="r.standard.specificationItemId"><h4>{{r.standard.itemName??r.standard.testCode}}</h4><p v-for="result in r.originalResults" :key="result.id">结果引用 {{result.id}} · 修订 {{result.revisionNo}} · {{result.resultNumeric??result.resultText}} · {{status[result.resultConclusion]}} · {{displayTime(result.recordedAt)}}</p></section></section><h3>记录元数据</h3><p>记录引用 {{record.id}} · 记录版本 {{record.versionNo}} · 签名引用 {{record.signatureId??'—'}}</p><p v-if="record.generationNo!=null">报告归档代次 {{record.generationNo}}</p><p v-if="record.sourceDigest||record.evidenceDigest">证据摘要 {{record.sourceDigest??record.evidenceDigest}}</p><a-button v-if="auth.can('audit:view')" type="link" @click="router.push('/audit')">审计追踪</a-button><a-button v-if="record.mainBatchId&&auth.can('trace:view')" type="link" @click="router.push({path:'/trace',query:{mainBatchId:record.mainBatchId}})">完整追溯</a-button></div></details>
  <a-modal v-model:open="signatureOpen" title="电子签名确认" ok-text="签名并提交" :confirm-loading="busy" @ok="save" @cancel="password=''"><p>签名绑定本次业务动作及当前记录版本，请核对原因和原始证据。</p><label><span>签名密码</span><a-input-password v-model:value="password" aria-label="签名密码" autocomplete="current-password"/></label></a-modal>
 </main>
</template>
<style scoped>
.finished-back{margin-bottom:12px}
#app .finished-header{gap:16px;padding:16px 20px}
.finished-object{display:flex;align-items:center;gap:16px;min-width:0}
.finished-document-icon{display:grid;place-items:center;flex-shrink:0;width:52px;height:52px;background:#eaf3ff;color:var(--mes-ui-primary);border-radius:8px;font-size:26px}
.finished-heading{display:flex;align-items:center;flex-wrap:wrap;gap:12px}
#app .finished-heading h1{margin:0}
.finished-number{font-size:20px;overflow-wrap:anywhere;color:var(--mes-ui-text)}
#app .finished-object p{margin:4px 0 0}
.finished-header-actions{flex-shrink:0}
.finished-fact-groups{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:24px}
.finished-fact-groups>section+section{border-left:1px solid var(--mes-ui-border);padding-left:24px}
.finished-detail h2,.finished-detail h3{font-size:14px;line-height:22px;font-weight:600;margin:0 0 12px;color:var(--mes-ui-text)}
.finished-fact-list{display:grid;grid-template-columns:130px minmax(0,1fr);gap:12px 16px;margin:0;font-size:14px;line-height:22px}
.finished-fact-list dt{color:var(--mes-ui-secondary);font-weight:400}
.finished-fact-list dd{margin:0;min-width:0;color:var(--mes-ui-text);overflow-wrap:anywhere;white-space:pre-wrap}
.finished-source-actions{margin-top:20px}
.finished-related-record,.finished-history-row{display:flex;align-items:center;flex-wrap:wrap;gap:8px}
.finished-related-record :deep(.ant-btn),.finished-history-row :deep(.ant-btn){padding-left:0}
.finished-history-row+.finished-history-row{border-top:1px solid var(--mes-ui-border);padding-top:12px;margin-top:12px}
.finished-help{font-size:13px;line-height:20px;color:var(--mes-ui-secondary);margin:0 0 12px}
.finished-related-record+.finished-help{margin:8px 0 0}
.finished-empty{margin:0}
.finished-sample-facts{grid-template-columns:130px minmax(0,1fr) 130px minmax(0,1fr)}
.finished-conclusion{display:flex;gap:16px;align-items:center;padding:16px;border-radius:8px;background:#f7f9fc;color:var(--mes-ui-secondary)}
.finished-conclusion :deep(.anticon){font-size:28px}
.finished-conclusion strong{font-size:16px}
.finished-conclusion p{margin:4px 0 0;font-size:13px;line-height:20px}
.conclusion-pass{background:#f0fbf5;color:var(--mes-ui-success)}
.conclusion-fail{background:#fff2f3;color:var(--mes-ui-danger)}
.finished-review+.finished-review{margin-top:16px;padding-top:16px;border-top:1px solid var(--mes-ui-border)}
.finished-flow{display:grid;grid-template-columns:repeat(auto-fit,minmax(210px,1fr));gap:16px}
.finished-flow>section{border-left:2px solid var(--mes-ui-border);padding-left:12px;font-size:13px;color:var(--mes-ui-text)}
.finished-flow h3{margin-bottom:4px}
.finished-flow time{display:block;margin-top:4px;color:var(--mes-ui-secondary);font-size:12px}
.finished-note{white-space:pre-wrap;overflow-wrap:anywhere;margin:0;line-height:22px}
.finished-command form{max-width:1100px}
.finished-form-group+.finished-form-group{margin-top:24px;padding-top:20px;border-top:1px solid var(--mes-ui-border)}
#app .finished-detail .master-form{max-width:1100px;gap:16px 24px}
#app .finished-detail .master-form label{grid-template-columns:130px minmax(0,1fr);color:var(--mes-ui-text);align-items:start}
#app .finished-detail .master-form .form-full-row{grid-column:1/-1}
#app .finished-detail .master-form label:has(textarea){align-items:start}
#app .finished-detail .form-field-label{padding-top:6px}
#app .finished-detail textarea{min-height:80px;resize:vertical}
.finished-required{color:var(--mes-ui-danger)}
.finished-checkbox{display:flex;align-items:center;gap:8px;min-height:34px}
.finished-checkbox input{width:16px;height:16px;accent-color:var(--mes-ui-primary)}
.finished-actions{display:flex;justify-content:flex-end;gap:8px;margin-top:24px;padding-top:16px;border-top:1px solid var(--mes-ui-border)}
.finished-metadata{font-size:13px;color:var(--mes-ui-secondary);overflow-wrap:anywhere;margin-top:16px}
.finished-evidence{margin-top:12px;font-size:12px;color:var(--mes-ui-secondary)}
.finished-evidence summary{cursor:pointer}
.finished-evidence-body{padding:16px 20px;background:var(--mes-ui-surface);border:1px solid var(--mes-ui-border);border-radius:8px}
.finished-evidence-body h4{margin:12px 0 8px;font-size:13px}
.finished-evidence-body p{line-height:22px}
@media(max-width:1000px){.finished-header{flex-wrap:wrap}.finished-header-actions{flex-shrink:1}.finished-number{font-size:18px}}
@media(max-width:767px){.finished-fact-groups{grid-template-columns:1fr;gap:20px}.finished-fact-groups>section+section{border-left:0;padding-left:0;border-top:1px solid var(--mes-ui-border);padding-top:16px}.finished-sample-facts{grid-template-columns:100px minmax(0,1fr)}.finished-fact-list{grid-template-columns:100px minmax(0,1fr)}#app .finished-detail .master-form label{grid-template-columns:100px minmax(0,1fr)}.finished-document-icon{width:40px;height:40px;font-size:22px}.finished-object{gap:12px}}
</style>
