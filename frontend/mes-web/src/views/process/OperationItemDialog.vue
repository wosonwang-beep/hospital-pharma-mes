<script setup lang="ts">
import {ref,watch} from 'vue'
import type {Parameter,IpcDefinition} from './model'
import ProcessLookup from './ProcessLookup.vue'
import {selectAccessibility as vSelectAccessibility} from '../ebr/selectAccessibility'
const props=defineProps<{open:boolean;kind:'PARAMETER'|'IPC';value:Parameter|IpcDefinition|null;persisted?:boolean}>()
const emit=defineEmits<{cancel:[];save:[Parameter|IpcDefinition]}>()
const vFocusError={mounted:(el:HTMLElement)=>el.focus()}
const draft=ref<Record<string,any>>({}),error=ref('')
watch(()=>props.open,open=>{if(open){error.value='';draft.value=props.value?JSON.parse(JSON.stringify(props.value)):props.kind==='IPC'?{ipcCode:'',name:'',required:true,resultType:'NUMERIC',lowerLimit:null,upperLimit:null,expectedText:null,unitId:null,methodReference:''}:{parameterCode:'',parameterName:'',acquisitionMode:'MANUAL',unitId:null,lowerLimit:null,upperLimit:null}}})
function changeType(e:Event){const next=(e.target as HTMLSelectElement).value;if(next===draft.value.resultType)return;if((draft.value.lowerLimit!=null||draft.value.upperLimit!=null||draft.value.expectedText||draft.value.unitId)&&!window.confirm('切换结果类型将清除不兼容的标准和单位，是否继续？')){(e.target as HTMLSelectElement).value=draft.value.resultType;return}Object.assign(draft.value,{resultType:next,lowerLimit:null,upperLimit:null,expectedText:null,unitId:null})}
function scaled(value:string,scale:number){const [whole,fraction='']=value.replace(/^-/,'').split('.');const amount=BigInt(whole!)*10n**BigInt(scale)+BigInt(fraction.padEnd(scale,'0'));return value.startsWith('-')?-amount:amount}
function save(){
 const code=props.kind==='IPC'?'ipcCode':'parameterCode',name=props.kind==='IPC'?'name':'parameterName'
 if(!String(draft.value[code]??'').trim()||!String(draft.value[name]??'').trim()){error.value='请填写编码和名称';return}
 const value=JSON.parse(JSON.stringify(draft.value)),scale=props.kind==='IPC'?8:6,integerDigits=props.kind==='IPC'?16:12
 for(const key of ['lowerLimit','upperLimit']){
  const raw=value[key]==null?'':String(value[key]).trim();value[key]=raw||null
  if(raw&&(!new RegExp(`^-?\\d+(\\.\\d{1,${scale}})?$`).test(raw)||raw.replace(/^-?0*/,'').split('.')[0]!.length>integerDigits)){error.value=`上下限最多${integerDigits}位整数、${scale}位小数`;return}
 }
 if(value.lowerLimit!=null&&value.upperLimit!=null&&scaled(value.lowerLimit,scale)>scaled(value.upperLimit,scale)){error.value='下限不能大于上限';return}
 if(props.kind==='IPC'){
  if(!String(value.methodReference??'').trim()){error.value='请填写方法及版本引用';return}
  if(value.resultType==='NUMERIC'&&value.lowerLimit==null&&value.upperLimit==null){error.value='数值检验至少填写一个上下限';return}
  if(value.resultType==='TEXT'&&!String(value.expectedText??'').trim()){error.value='请填写预期结果';return}
 }
 emit('save',value)
}
</script>
<template><a-modal :open="open" :title="kind==='IPC'?'维护检验项目':'维护工艺参数'" :width="680" centered @cancel="emit('cancel')"><a-alert v-if="error" v-focus-error tabindex="-1" type="error" role="alert" :message="error" show-icon/><form class="item-form" v-select-accessibility @submit.prevent="save"><label><span>编码</span><a-input v-if="kind==='IPC'" v-model:value="draft.ipcCode" :disabled="persisted" maxlength="64"/><a-input v-else v-model:value="draft.parameterCode" :disabled="persisted" maxlength="64"/></label><label><span>名称</span><a-input v-if="kind==='IPC'" v-model:value="draft.name" maxlength="200"/><a-input v-else v-model:value="draft.parameterName" maxlength="200"/></label><template v-if="kind==='IPC'"><div class="lookup-row"><span>必检项目</span><a-checkbox v-model:checked="draft.required" aria-label="必检项目">是</a-checkbox></div><label><span>结果类型</span><select :value="draft.resultType" @change="changeType"><option value="NUMERIC">数值</option><option value="TEXT">文本</option></select></label></template><label v-else><span>采集模式</span><select v-model="draft.acquisitionMode"><option value="MANUAL">手工</option><option value="AUTO">自动</option><option value="HYBRID">混合</option></select></label><template v-if="kind==='PARAMETER'||draft.resultType==='NUMERIC'"><label><span>下限</span><a-input v-model:value="draft.lowerLimit"/></label><label><span>上限</span><a-input v-model:value="draft.upperLimit"/></label><div class="lookup-row"><span>单位</span><ProcessLookup v-model="draft.unitId" resource="units" label="单位" allow-clear/></div></template><label v-else><span>预期结果</span><a-input v-model:value="draft.expectedText" maxlength="1000"/></label><label v-if="kind==='IPC'"><span>方法及版本引用</span><a-input v-model:value="draft.methodReference" maxlength="200"/></label></form><template #footer><a-button type="primary" @click="save">保存</a-button><a-button @click="emit('cancel')">取消</a-button></template></a-modal></template>
<style scoped>.item-form{display:grid;gap:16px;margin-top:16px}.item-form>label,.lookup-row{display:grid;grid-template-columns:112px minmax(0,1fr);align-items:center;gap:10px}.item-form>label>span:first-child,.lookup-row>span{text-align:right}select{height:32px;width:100%;border:1px solid #d9d9d9;border-radius:6px;padding:4px 8px}.lookup-row :deep(.ant-select){width:100%}</style>
