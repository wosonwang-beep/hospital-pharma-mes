<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {onBeforeRouteLeave} from 'vue-router'
import {api} from '../../api/http'
import {useControlledRequest} from '../../api/controlled'
import {schemas,commandBody,initial,type IncomingRow} from './incomingModel'
import {validateExpression} from './productionQualityModel'
import IncomingFormFields from './IncomingFormFields.vue'
const props=defineProps<{schemaName:string;url:string;method?:string;source:IncomingRow;initialValue?:IncomingRow;signatureTarget?:{objectType:string;objectId:string;meaning:string};lockedFields?:string[];hiddenFields?:string[]}>()
const emit=defineEmits<{saved:[IncomingRow];cancel:[]}>()
const request=useControlledRequest(),{busy,error,conflict}=request,form=ref<IncomingRow>({}),signatureOpen=ref(false),password=ref('')
const schema=computed(()=>{const s=schemas[props.schemaName]!;const properties={...s.properties};delete properties.signature;for(const field of props.hiddenFields??[])delete properties[field];return {...s,properties,required:s.required?.filter(k=>k!=='signature'&&!props.hiddenFields?.includes(k))}})
let initialSnapshot='',saved=false
onBeforeRouteLeave(()=>saved||JSON.stringify(form.value)===initialSnapshot||window.confirm('离开将放弃未保存的输入，是否继续？'))
const signed=computed(()=>!!schemas[props.schemaName]?.properties?.signature)
watch(()=>[props.url,props.schemaName],()=>{form.value={...(initial(schema.value) as IncomingRow),...props.initialValue,...('versionNo' in props.source?{versionNo:props.source.versionNo}:{})};initialSnapshot=JSON.stringify(form.value);saved=false;request.clear()},{immediate:true})
function body(){const b=commandBody(schema.value,form.value) as IncomingRow;for(const rule of (b.balanceRules??[]) as IncomingRow[]){const f=rule.formulaExpr as IncomingRow;const count={nodes:0};validateExpression(f.expected,1,count);validateExpression(f.actual,1,count)}return b}
function prepare(){try{body();if(signed.value){if(!props.signatureTarget)throw Error('缺少签名绑定，请刷新记录');signatureOpen.value=true}else void save()}catch(e){void request.failure(e)}}
async function save(){busy.value=true;try{const b=body();if(signed.value){if(!password.value||!props.signatureTarget)throw Error('请输入签名密码');const r=await api<{reauthToken:string}>({url:'/auth/reauth',method:'POST',data:{...props.signatureTarget,recordVersion:props.source.versionNo,credential:password.value}});b.signature={reauthToken:r.reauthToken}}
const result=await request.mutate<IncomingRow>(props.url,props.method??'POST',b,props.source.versionNo as number|undefined);signatureOpen.value=false;saved=true;emit('saved',result)}catch(e){await request.failure(e)}finally{password.value='';busy.value=false}}
</script>
<template><a-alert v-if="error" type="error" :message="error" show-icon/><a-alert v-if="conflict" type="warning" message="记录已变化，请核对最新记录后重试。"/><form @submit.prevent="prepare"><IncomingFormFields v-model="form" :schema="schema" :context="{...source,...form}" :locked-fields="['versionNo',...(lockedFields??[])]" :disabled="busy"/><a-space class="command-buttons"><a-button html-type="submit" type="primary" :loading="busy" :disabled="conflict">{{signed?'核对并签名':'提交'}}</a-button><a-button :disabled="busy" @click="emit('cancel')">取消</a-button></a-space></form><a-modal v-model:open="signatureOpen" title="电子签名确认" ok-text="签名并提交" cancel-text="取消" :confirm-loading="busy" @ok="save" @cancel="password=''"><p>请核对操作与原因，签名将绑定当前记录版本。</p><div class="master-form"><label><span class="form-field-label">签名密码</span><input v-model="password" class="master-native-input" type="password" autocomplete="current-password" required aria-label="签名密码" @keydown.enter.prevent="save"/></label></div></a-modal></template>
<style scoped>.command-buttons{margin-top:16px}</style>
