<script setup lang="ts">
import {computed,onBeforeUnmount,ref,useId,watch} from 'vue'
import {theme} from 'ant-design-vue'
import {api,http} from '../../api/http'
import {type PrintTemplate,printTypeLabels,printErrorMessage} from '../../api/printing'
import {useAuthStore} from '../../stores/auth'
import PdfPreview from './PdfPreview.vue'
const props=defineProps<{businessType:string;recordIds:string[]}>()
const auth=useAuthStore(),open=ref(false),busy=ref(false),error=ref(''),choices=ref<PrintTemplate[]>([]),selected=ref<string>(),formal=ref(false),blob=ref<Blob|null>(null)
const template=computed(()=>choices.value.find(row=>row.id===selected.value))
const captured=ref<string[]>([]),capturedType=ref(''),templateId=useId(),{token}=theme.useToken()
const modalZIndex=computed(()=>token.value.zIndexPopupBase+100)
const popupContainer=(trigger:HTMLElement)=>trigger.parentElement??document.body
let generation=0
function close(){++generation;open.value=false;busy.value=false;blob.value=null;error.value='';choices.value=[];selected.value=undefined;formal.value=false}
async function show(){
 if(busy.value)return
 const ticket=++generation;open.value=true;error.value='';blob.value=null;formal.value=false;choices.value=[];selected.value=undefined;captured.value=[...props.recordIds];capturedType.value=props.businessType;busy.value=true
 try{const rows=await api<PrintTemplate[]>({url:'/printing/applicable',params:{businessType:capturedType.value}});if(ticket===generation&&open.value){choices.value=rows;selected.value=rows[0]?.id}}
 catch(e){const message=await printErrorMessage(e);if(ticket===generation&&open.value)error.value=message}
 finally{if(ticket===generation)busy.value=false}
}
async function generate(){
 if(busy.value||!selected.value||!open.value)return
 const ticket=generation;error.value='';busy.value=true;blob.value=null
 try{const result=(await http.post<Blob>('/printing/batches',{businessType:capturedType.value,recordIds:captured.value,templateVersionId:selected.value,formal:formal.value&&template.value?.printType==='DOCUMENT'},{responseType:'blob'})).data;if(ticket===generation&&open.value)blob.value=result}
 catch(e){const message=await printErrorMessage(e);if(ticket===generation&&open.value)error.value=message}
 finally{if(ticket===generation)busy.value=false}
}
watch(()=>props.businessType,close)
onBeforeUnmount(()=>{++generation})
</script>
<template>
 <a-button v-if="auth.can('print:document:generate')" :disabled="!recordIds.length||recordIds.length>100" @click="show">打印选中记录{{recordIds.length?`（${recordIds.length}）`:''}}</a-button>
 <a-modal v-model:open="open" title="打印选中记录" width="90vw" :footer="null" :z-index="modalZIndex" destroy-on-close @cancel="close">
  <a-alert v-if="error" :message="error" type="error" show-icon/>
  <div class="print-controls">
   <div class="print-pair"><label :for="templateId">打印模板</label><a-select :id="templateId" v-model:value="selected" :loading="busy" :disabled="busy" :get-popup-container="popupContainer" placeholder="请选择已发布模板" :options="choices.map(row=>({value:row.id,label:`${row.templateName} · ${printTypeLabels[row.printType]} · V${row.templateRevision}`}))" @change="blob=null;formal=false"/></div>
   <span>已选 {{captured.length}} 条 · {{template?.printType==='LIST'?'汇总为列表': '每条记录生成完整单据，分别分页'}}</span>
   <a-checkbox v-if="capturedType==='INSPECTION_REPORT'&&template?.printType==='DOCUMENT'" v-model:checked="formal" :disabled="busy" @change="blob=null">正式报告（逐条校验审批及签名）</a-checkbox>
   <a-button class="print-primary" type="primary" :loading="busy" :disabled="!selected" @click="generate">生成打印预览</a-button>
  </div>
  <a-empty v-if="!busy&&!error&&!choices.length" description="当前功能没有已发布并启用绑定的模板，请先在打印模板中发布并绑定。"/>
  <PdfPreview v-if="blob" :blob="blob" name="选中记录打印.pdf"/>
 </a-modal>
</template>
<style scoped>
.print-controls{display:flex;flex-wrap:wrap;align-items:center;gap:16px;margin:16px 0}.print-pair{display:flex;flex-wrap:nowrap;align-items:center;gap:12px;flex:1 1 400px;min-width:0}.print-pair>label{white-space:nowrap;flex:0 0 56px}.print-pair>.ant-select{flex:1;min-width:0;height:auto}.print-controls>span{font-size:13px;color:#66778b}
.print-controls :deep(.ant-btn){height:auto;min-height:32px;white-space:normal;line-height:1.5715;padding-block:4px}.print-pair :deep(.ant-select-selector){height:auto!important;min-height:32px;align-items:center}.print-pair :deep(.ant-select-selection-item),.print-pair :deep(.ant-select-selection-placeholder){line-height:1.5!important;white-space:normal;overflow:visible;text-overflow:clip}.print-pair :deep(.ant-select-selection-placeholder){color:#66778b}.print-primary:not(:disabled){background:#0958d9!important;border-color:#0958d9!important}.print-primary:not(:disabled):hover,.print-primary:not(:disabled):focus-visible{background:#003eb3!important;border-color:#003eb3!important}
</style>
