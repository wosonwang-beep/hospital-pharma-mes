<script setup lang="ts">
import {computed,ref} from 'vue'
import {api,errorMessage,http} from '../../api/http'
import {type PrintTemplate,printTypeLabels} from '../../api/printing'
import {useAuthStore} from '../../stores/auth'
import PdfPreview from './PdfPreview.vue'
const props=defineProps<{businessType:string;recordIds:string[]}>()
const auth=useAuthStore(),open=ref(false),busy=ref(false),error=ref(''),choices=ref<PrintTemplate[]>([]),selected=ref<string>(),formal=ref(false),blob=ref<Blob|null>(null)
const template=computed(()=>choices.value.find(row=>row.id===selected.value))
const captured=ref<string[]>([])
async function show(){open.value=true;error.value='';blob.value=null;formal.value=false;captured.value=[...props.recordIds];busy.value=true;try{choices.value=await api({url:'/printing/applicable',params:{businessType:props.businessType}});selected.value=choices.value[0]?.id}catch(e){error.value=errorMessage(e)}finally{busy.value=false}}
async function generate(){if(!selected.value)return;error.value='';busy.value=true;blob.value=null;try{const batch=await api<{id:string}>({url:'/printing/batches',method:'POST',data:{businessType:props.businessType,recordIds:captured.value,templateVersionId:selected.value,formal:formal.value&&template.value?.printType==='DOCUMENT'}});blob.value=(await http.get<Blob>(`/printing/batches/${batch.id}/pdf`,{responseType:'blob'})).data}catch(e){error.value=errorMessage(e)}finally{busy.value=false}}
</script>
<template>
 <a-button v-if="auth.can('print:document:generate')" :disabled="!recordIds.length||recordIds.length>100" @click="show">打印选中记录{{recordIds.length?`（${recordIds.length}）`:''}}</a-button>
 <a-modal v-model:open="open" title="打印选中记录" width="90vw" :footer="null">
  <a-alert v-if="error" :message="error" type="error" show-icon/>
  <div class="print-controls">
   <label class="print-pair"><span>打印模板</span><a-select v-model:value="selected" :loading="busy" placeholder="请选择已发布模板" :options="choices.map(row=>({value:row.id,label:`${row.templateName} · ${printTypeLabels[row.printType]} · V${row.templateRevision}`}))" @change="blob=null;formal=false"/></label>
   <span>已选 {{captured.length}} 条 · {{template?.printType==='LIST'?'汇总为列表': '每条记录生成完整单据，分别分页'}}</span>
   <a-checkbox v-if="businessType==='INSPECTION_REPORT'&&template?.printType==='DOCUMENT'" v-model:checked="formal" @change="blob=null">正式报告（逐条校验审批及签名）</a-checkbox>
   <a-button type="primary" :loading="busy" :disabled="!selected" @click="generate">生成打印预览</a-button>
  </div>
  <a-empty v-if="!busy&&!choices.length" description="当前功能没有已发布并启用绑定的模板，请先在打印模板中发布并绑定。"/>
  <PdfPreview v-if="blob" :blob="blob" name="选中记录打印.pdf"/>
 </a-modal>
</template>
<style scoped>
.print-controls{display:flex;flex-wrap:wrap;align-items:center;gap:16px;margin:16px 0}.print-pair{display:flex;flex-wrap:nowrap;align-items:center;gap:12px;flex:1 1 400px;min-width:0}.print-pair>span{white-space:nowrap;flex:0 0 56px}.print-pair>.ant-select{flex:1;min-width:0}.print-controls>span{font-size:13px;color:#66778b}
</style>
