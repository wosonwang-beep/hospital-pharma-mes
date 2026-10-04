<script setup lang="ts">
import {computed,ref} from 'vue'
import {api,idempotencyKey} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {useControlledRequest} from '../../api/controlled'
import IncomingFacts from './IncomingFacts.vue'
import {labels,enumLabels} from './incomingModel'
Object.assign(labels,{inventoryDecisions:'库存限制决定',previousDecisionId:'前序决定',previousInventoryStatus:'原库存状态',resultingInventoryStatus:'决定后库存状态',decidedBy:'决定人',decidedAt:'决定时间',clearanceRecords:'清场记录',clearanceRecordId:'清场记录',equipmentUsageId:'设备绑定',outcome:'执行结果',performedBy:'执行人',performedAt:'执行时间',review:'独立复核',decision:'复核决定',ipcInstances:'过程检验',ipcCode:'IPC 编码',definitionSnapshot:'冻结检验标准',currentResultRevisionId:'当前结果修订',ipcInstanceId:'IPC 实例',resultConclusion:'结果结论',reasonForChange:'修订原因',disposition:'有效性决定',methodReference:'检验方法及版本',expectedText:'预期结果',resultType:'结果类型'})
Object.assign(enumLabels,{FREEZE:'冻结',UNFREEZE:'解除冻结',CONFIRMED:'已确认',INVALIDATED:'已批准失效',NUMERIC:'数值',TEXT:'文本'})
type Row=Record<string,unknown>
const props=defineProps<{row:object;kind:'lot'|'operation'}>(),emit=defineEmits<{reload:[]}>()
const data=computed(()=>props.row as Row),auth=useAuthStore(),request=useControlledRequest(),{busy,error,conflict}=request
const action=ref(''),instance=ref<Row|null>(null),usage=ref(''),outcome=ref('PASS'),disposition=ref('CONFIRMED'),decision=ref('APPROVED'),numeric=ref(''),text=ref(''),reason=ref(''),password=ref('')
const ipc=computed(()=>(data.value.ipcInstances??[]) as Row[]),clearance=computed(()=>(data.value.clearanceRecords??[]) as Row[]),bindings=computed(()=>(data.value.equipmentUsages??[]) as Row[])
const current=computed(()=>{const rows=clearance.value.filter(r=>String(r.equipmentUsageId??'')===usage.value);return rows[rows.length-1]})
const currentResult=computed(()=>{const rows=(instance.value?.results??[]) as Row[];return rows[rows.length-1]}),definition=computed(()=>(instance.value?.definitionSnapshot??{}) as Row)
let pendingFingerprint='',pendingKey='',pendingBody:Row|null=null
function open(name:string,row?:Row){request.clear();action.value=name;instance.value=row??null;usage.value=bindings.value.length?String(bindings.value[0]?.id):'';outcome.value='PASS';decision.value='APPROVED';disposition.value='CONFIRMED';reason.value='';password.value='';numeric.value='';text.value='';pendingFingerprint='';pendingBody=null}
function facts(row:Row){const {allowedActions,...visible}=row;return visible}
function can(permission:string){return auth.can(permission)&&auth.can('ebr:sign')}
async function save(){busy.value=true;try{
 if(!reason.value.trim())throw Error('请填写操作原因')
 const source=instance.value??data.value,version=Number(source.versionNo),id=String(source.id)
 const body:Row={versionNo:version,reason:reason.value.trim()};let url='',objectType='',objectId=`${id}:${version}`,meaning='APPROVE'
 if(action.value==='freeze'||action.value==='unfreeze'){url=`/material-lots/${id}/${action.value}`;objectType='INVENTORY_DECISION'}
 else if(action.value==='record-clearance'){url=`/operations/${id}/record-clearance`;objectType='CLEARANCE_RECORD';meaning='VERIFY';body.equipmentUsageId=usage.value||null;body.previousRecordId=current.value?String(current.value.id):null;body.outcome=outcome.value}
 else if(action.value==='review-clearance'){if(!current.value)throw Error('请先选择有清场记录的范围');url=`/operations/${id}/review-clearance`;objectType='CLEARANCE_REVIEW';body.clearanceRecordId=String(current.value.id);body.decision=decision.value}
 else if(action.value==='submit-result'){url=`/ipc/${id}/submit-result`;objectType='IPC_RESULT';meaning='VERIFY';body.previousRevisionId=source.currentResultRevisionId?String(source.currentResultRevisionId):null;if(definition.value.resultType==='NUMERIC')body.resultNumeric=numeric.value;else body.resultText=text.value}
 else {if(!currentResult.value)throw Error('当前检验结果不存在');url=`/ipc/${id}/review-result`;objectType='IPC_REVIEW';body.resultRevisionId=String(currentResult.value.id);body.disposition=disposition.value;objectId=`${body.resultRevisionId}:${version}`}
 const fingerprint=JSON.stringify({url,body})
 if(fingerprint!==pendingFingerprint){if(!password.value)throw Error('请输入签名密码');const reauth=await api<{reauthToken:string}>({url:'/auth/reauth',method:'POST',data:{objectType,objectId,recordVersion:version,meaning,credential:password.value}});body.signature={reauthToken:reauth.reauthToken};pendingFingerprint=fingerprint;pendingKey=idempotencyKey();pendingBody=body}
 await api({url,method:'POST',data:pendingBody,headers:{'If-Match':`"${version}"`,'Idempotency-Key':pendingKey}});action.value='';pendingBody=null;pendingFingerprint='';emit('reload')
 }catch(e){await request.failure(e)}finally{busy.value=false;password.value=''}}
</script>
<template>
<a-card :title="kind==='lot'?'库存冻结管理':'清场与过程检验'" class="form-section">
 <a-alert v-if="error" type="error" :message="error" show-icon/><a-alert v-if="conflict" type="warning" message="记录已变化。请重新加载后核对当前记录。"/>
 <template v-if="kind==='lot'"><IncomingFacts :value="{inventoryDecisions:data.inventoryDecisions??[]}"/><a-space><a-button v-if="data.inventoryStatus==='AVAILABLE'&&can('qa:material-inventory:freeze')" @click="open('freeze')">冻结库存</a-button><a-button v-if="data.inventoryStatus==='FROZEN'&&can('qa:material-inventory:unfreeze')" @click="open('unfreeze')">解除冻结</a-button></a-space></template>
 <template v-else><IncomingFacts :value="{clearanceRecords:clearance}"/><a-space v-if="data.clearanceRequired===true&&['PENDING','READY','PAUSED'].includes(String(data.status))"><a-button v-if="can('mes:clearance:record')" @click="open('record-clearance')">记录清场</a-button><a-button v-if="clearance.length&&can('mes:clearance:review')" @click="open('review-clearance')">独立复核清场</a-button></a-space><a-card v-for="item in ipc" :key="String(item.id)" :title="String(item.ipcCode)" class="form-section"><IncomingFacts :value="facts(item)"/><a-space><a-button v-if="(item.allowedActions as string[]).includes('SUBMIT_RESULT')&&can('qms:ipc:record')" @click="open('submit-result',item)">记录 IPC 结果</a-button><a-button v-if="(item.allowedActions as string[]).includes('REVIEW_RESULT')&&can('qms:ipc:review')" @click="open('review-result',item)">独立复核 IPC</a-button></a-space></a-card></template>
 <form v-if="action" @submit.prevent="save"><div class="master-form">
 <label v-if="action.includes('clearance')"><span class="form-field-label">清场范围</span><select v-model="usage" class="master-native-input" aria-label="清场范围" :disabled="busy"><option v-if="!bindings.length" value="">当前工序</option><option v-for="binding in bindings" :key="String(binding.id)" :value="String(binding.id)">设备 {{binding.equipmentId}} · 绑定 {{binding.id}}</option></select></label>
 <label v-if="action==='record-clearance'"><span class="form-field-label">清场结果</span><select v-model="outcome" class="master-native-input" aria-label="清场结果"><option value="PASS">通过</option><option value="FAIL">不通过</option></select></label>
 <label v-if="action==='review-clearance'"><span class="form-field-label">复核决定</span><select v-model="decision" class="master-native-input" aria-label="清场复核决定"><option value="APPROVED">批准</option><option value="REJECTED">驳回</option></select></label>
 <label v-if="action==='submit-result'&&definition.resultType==='NUMERIC'"><span class="form-field-label">原始数值结果</span><input v-model="numeric" class="master-native-input" aria-label="IPC 原始数值结果" required/></label>
 <label v-if="action==='submit-result'&&definition.resultType==='TEXT'"><span class="form-field-label">原始文本结果</span><input v-model="text" class="master-native-input" aria-label="IPC 原始文本结果" maxlength="1000" required/></label>
 <label v-if="action==='review-result'"><span class="form-field-label">结果有效性</span><select v-model="disposition" class="master-native-input" aria-label="IPC 结果有效性"><option value="CONFIRMED">确认原始结果</option><option value="INVALIDATED">批准结果失效</option></select></label>
 <label class="form-full-row"><span class="form-field-label">操作原因</span><textarea v-model="reason" class="master-native-input" aria-label="操作原因" maxlength="1000" required/></label>
 <label><span class="form-field-label">签名密码</span><input v-model="password" type="password" autocomplete="current-password" class="master-native-input" aria-label="签名密码"/></label>
 </div><a-space><a-button type="primary" html-type="submit" :loading="busy" :disabled="conflict">签名并提交</a-button><a-button :disabled="busy" @click="action=''">取消</a-button></a-space></form>
</a-card>
</template>
