<script setup lang="ts">
import {computed,onBeforeUnmount,ref,useId,watch} from 'vue'
import {theme} from 'ant-design-vue'
import {useAuthStore} from '../../stores/auth'
import {applicable,generate,printErrorMessage,type PrintTemplate} from '../../api/printing'
import PdfPreview from './PdfPreview.vue'
const props=defineProps<{businessId:string;approved:boolean}>(),auth=useAuthStore()
const choices=ref<PrintTemplate[]>([]),selected=ref<string>(),formal=ref(false),busy=ref(false),error=ref(''),open=ref(false),blob=ref<Blob|null>(null),name=ref('检验报告.pdf')
const templateId=useId(),{token}=theme.useToken(),modalZIndex=computed(()=>token.value.zIndexPopupBase+100)
let generation=0
function close(){++generation;open.value=false;blob.value=null;busy.value=false}
async function load(){
 close();formal.value=false;choices.value=[];selected.value=undefined;error.value=''
 if(!auth.can('print:document:generate'))return
 const ticket=generation;busy.value=true
 try{const rows=await applicable();if(ticket===generation){choices.value=rows;selected.value=rows[0]?.id}}
 catch(e){const message=await printErrorMessage(e);if(ticket===generation)error.value=message}
 finally{if(ticket===generation)busy.value=false}
}
watch(()=>[props.businessId,auth.can('print:document:generate')],()=>void load(),{immediate:true})
async function create(){
 if(busy.value||!selected.value)return
 const ticket=++generation,filename=`检验报告-${formal.value?'正式':'草稿'}.pdf`;busy.value=true;error.value=''
 try{const result=await generate(props.businessId,selected.value,formal.value);if(ticket===generation){blob.value=result;name.value=filename;open.value=true}}
 catch(e){const message=await printErrorMessage(e);if(ticket===generation)error.value=message}
 finally{if(ticket===generation)busy.value=false}
}
onBeforeUnmount(()=>{++generation})
</script>
<template><a-card v-if="auth.can('print:document:generate')" title="报告打印" class="print-panel"><a-alert v-if="error" type="error" :message="error" show-icon/><a-space wrap><div class="print-template-pair"><label :for="templateId">打印模板</label><a-select :id="templateId" show-search option-filter-prop="label" v-model:value="selected" :disabled="busy" placeholder="选择已发布的适用模板" :options="choices.map(t=>({value:t.id,label:`${t.templateName} · 第 ${t.templateRevision} 版`}))"/></div><a-radio-group v-model:value="formal" :disabled="busy"><a-radio :value="false">草稿</a-radio><a-radio :value="true" :disabled="!approved">正式</a-radio></a-radio-group><a-button class="print-primary" type="primary" :disabled="!selected" :loading="busy" @click="create">生成并预览 PDF</a-button></a-space><p class="hint">完整报告由服务端读取。正式报告需完成既有审核与电子签名；打印完成后不保存本次打印内容。</p><a-empty v-if="!busy&&!error&&!choices.length" description="尚无已发布并绑定的适用模板"/><a-modal v-model:open="open" title="检验报告 PDF" width="90vw" :footer="null" :z-index="modalZIndex" destroy-on-close @cancel="close"><PdfPreview :blob="blob" :name="name"/></a-modal></a-card></template>
<style scoped>.print-panel{margin-top:16px}.hint{color:#68758a;margin:14px 0;font-size:13px}.print-template-pair{display:flex;align-items:center;gap:12px;min-width:0}.print-template-pair>label{white-space:nowrap;flex:none}.print-template-pair>.ant-select{width:260px;min-width:0;height:auto}.print-panel :deep(.ant-btn){height:auto;min-height:32px;white-space:normal;line-height:1.5715;padding-block:4px}.print-template-pair :deep(.ant-select-selector){height:auto!important;min-height:32px;align-items:center}.print-template-pair :deep(.ant-select-selection-item),.print-template-pair :deep(.ant-select-selection-placeholder){line-height:1.5!important;white-space:normal;overflow:visible;text-overflow:clip}.print-template-pair :deep(.ant-select-selection-placeholder){color:#66778b}.print-primary:not(:disabled){background:#0958d9!important;border-color:#0958d9!important}.print-primary:not(:disabled):hover,.print-primary:not(:disabled):focus-visible{background:#003eb3!important;border-color:#003eb3!important}@media(max-width:600px){.print-template-pair>.ant-select{width:180px}}</style>
