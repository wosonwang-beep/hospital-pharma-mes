<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {bookApi} from '../../api/ebrBook'
import {useAuthStore} from '../../stores/auth'
import {errorMessage} from '../../api/http'
import PdfPreview from '../../components/printing/PdfPreview.vue'
import {bookEntryGroup,bookStates,mayFormal,type BatchBook,type BookItem,type BookPdf} from './bookModel'

const route=useRoute(),router=useRouter(),auth=useAuthStore()
const book=ref<BatchBook|null>(null),selected=ref(''),busy=ref(false),error=ref('')
const history=ref<BookPdf[]>([]),blob=ref<Blob|null>(null),pdfOpen=ref(false),reason=ref(''),kind=ref<'REVIEW_COPY'|'FINAL'>('REVIEW_COPY')
const current=computed(()=>book.value?.entries.find(e=>e.definition.code===selected.value))
const formalReady=computed(()=>book.value?mayFormal(book.value):false)
const groups=computed(()=>{
 const map=new Map<string,BookItem[]>()
 for(const item of book.value?.entries??[]){const key=bookEntryGroup(item.definition);if(!map.has(key))map.set(key,[]);map.get(key)!.push(item)}
 return [...map.entries()]
})
const completeCount=computed(()=>book.value?.entries.filter(e=>e.state==='COMPLETE').length??0)
const requiredPending=computed(()=>book.value?.entries.filter(e=>e.definition.required&&e.state!=='COMPLETE').length??0)
let generation=0

async function load(){
 const ticket=++generation;busy.value=true;error.value=''
 try{
  const value=await bookApi.read(String(route.params.id))
  if(ticket!==generation)return
  book.value=value
  if(!value.entries.some(e=>e.definition.code===selected.value))selected.value=value.entries[0]?.definition.code??''
  history.value=value.legacy?[]:await bookApi.history(value.mainBatchId)
 }catch(e){if(ticket===generation)error.value=errorMessage(e)}
 finally{if(ticket===generation)busy.value=false}
}
async function openPdf(record:BookPdf){
 if(!book.value)return;busy.value=true;error.value=''
 try{blob.value=await bookApi.pdf(book.value.mainBatchId,record);pdfOpen.value=true}
 catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
async function generate(){
 if(!book.value)return
 if(!reason.value.trim()){error.value='请填写电子批记录 PDF 生成原因';return}
 busy.value=true;error.value=''
 try{const record=await bookApi.generate(book.value,kind.value,reason.value.trim());await openPdf(record);await load()}
 catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
function stateClass(state:string){return state==='COMPLETE'?'done':state==='NOT_APPLICABLE'?'na':state==='MISSING'?'missing':'pending'}
function stateIcon(state:string){return state==='COMPLETE'?'●':state==='NOT_APPLICABLE'?'○':state==='MISSING'?'!':'●'}
watch(()=>route.params.id,()=>{blob.value=null;pdfOpen.value=false;selected.value='';void load()},{immediate:true})
</script>

<template>
<main data-ui-template="T4" class="admin-page master-page ebr-batch-page">
 <header class="admin-page-header">
  <div>
   <h1>电子批记录</h1>
   <p v-if="book">{{book.batchNo}} · {{book.productName}} {{book.specification||''}} · {{book.templateName||book.varietyName||'历史 EBR'}}</p>
   <p v-else>以实际生产批次为中心组织工序表单和各业务模块原始记录。</p>
  </div>
  <a-space wrap>
   <a-button @click="router.push('/production/batches/'+route.params.id)">返回生产批</a-button>
   <a-button v-if="book&&auth.can('qa:batch-review')" @click="router.push('/qa/batches/'+book.mainBatchId+'/review')">批记录审核</a-button>
   <a-button :loading="busy" @click="load">刷新记录</a-button>
  </a-space>
 </header>

 <a-alert v-if="error" type="error" :message="error" show-icon/>
 <a-skeleton v-if="busy&&!book" active/>

 <template v-if="book">
  <a-card class="batch-summary">
   <div class="summary-grid">
    <div><span>产品</span><strong>{{book.productName}}</strong><small>{{book.specification||'未提供规格'}}</small></div>
    <div><span>批号</span><strong>{{book.batchNo}}</strong><small>{{book.plannedQty}} {{book.unitName}}</small></div>
    <div><span>批次状态</span><strong>{{({DRAFT:'草稿',IN_PROGRESS:'生产中',COMPLETED:'已完成',QA_RELEASED:'QA已放行',REJECTED:'已拒绝'} as Record<string,string>)[book.status]??book.status}}</strong><small>{{book.book?'模板 V'+book.book.templateRevision:'原 EBR'}}</small></div>
    <div><span>记录完整性</span><strong>{{completeCount}} / {{book.entries.length}}</strong><small>{{requiredPending?requiredPending+' 项必需记录待完成':'必需记录已齐全'}}</small></div>
   </div>
  </a-card>

  <section v-if="book.legacy" class="legacy-box">
   <a-alert type="info" :message="book.status==='DRAFT'?'批次尚未下达，尚未冻结电子批记录模板。':book.notice" show-icon/>
   <a-card title="历史批次">
    <p>此批次继续使用原冻结 eBR 和历史归档证据，不自动套用新的电子批记录模板。</p>
    <a-space wrap>
     <a-button @click="router.push({path:'/production/batches/'+book.mainBatchId,query:{tab:'ebr'}})">查看原电子批记录</a-button>
     <a-button v-if="auth.can('ebr:template:view')&&auth.can('process:package:view')" @click="router.push('/ebr/book-templates')">查看电子批记录模板</a-button>
     <a-button v-if="auth.can('qa:batch-review')" @click="router.push('/qa/batches/'+book.mainBatchId+'/review')">查看审核与归档证据</a-button>
    </a-space>
   </a-card>
  </section>

  <div v-else class="ebr-layout">
   <aside class="ebr-directory" aria-label="电子批记录目录">
    <div class="directory-title">
     <h2>记录目录</h2>
     <div class="legend"><span class="done">● 已完成</span><span class="pending">● 进行中</span><span class="missing">! 待处理</span><span class="na">○ 不适用</span></div>
    </div>
    <section v-for="([group,items],groupIndex) in groups" :key="group" class="flow-group">
     <h3><span>{{String(groupIndex+1).padStart(2,'0')}}</span>{{group}}</h3>
     <button v-for="item in items" :key="item.definition.code" type="button" class="record-node" :class="{active:selected===item.definition.code}" @click="selected=item.definition.code">
      <span class="state-dot" :class="stateClass(item.state)">{{stateIcon(item.state)}}</span>
      <span class="node-main"><b>{{item.definition.title}}</b><small>{{bookStates[item.state]??item.state}} · {{item.documents.length}} 份来源记录</small></span>
     </button>
    </section>
   </aside>

   <section class="record-workspace" aria-label="电子批记录内容">
    <header class="record-header">
     <div><h2>{{current?.definition.title||'请选择记录'}}</h2><p v-if="current">{{bookEntryGroup(current.definition)}} · {{current.definition.required?'必需记录':'条件适用'}} · {{current.definition.scope==='BATCH'?'整批记录':'工序执行记录'}}</p></div>
     <a-tag v-if="current" :color="current.state==='COMPLETE'?'green':current.state==='MISSING'?'red':'orange'">{{bookStates[current.state]??current.state}}</a-tag>
    </header>

    <a-alert v-if="current&&!current.documents.length" :type="current.state==='MISSING'?'warning':'info'" :message="current.notice??bookStates[current.state]" show-icon/>

    <article v-for="doc in current?.documents" :key="doc.sourceType+'-'+doc.sourceId" class="source-document">
     <div class="source-head">
      <div><h3>{{doc.title}}</h3><span>原始来源：{{doc.sourceType}} · 来源版本 {{doc.sourceVersion||'不可变证据'}}</span></div>
      <a-button v-if="doc.url&&doc.sourceType!=='ATTACHMENTS'" :disabled="busy" @click="router.push(doc.url!)">{{doc.formId&&(auth.can('ebr:form:edit')||auth.can('ebr:review:verify')||auth.can('ebr:review:approve'))?'填写 / 复核':'查看原记录'}}</a-button>
     </div>
     <div class="record-table-scroll">
      <table>
       <thead><tr><th v-for="col in doc.columns" :key="col">{{col}}</th></tr></thead>
       <tbody><tr v-for="(row,index) in doc.rows" :key="index"><td v-for="(value,cell) in row" :key="cell">{{value||'未记录'}}</td></tr></tbody>
      </table>
     </div>
     <p v-if="doc.signatureIds" class="signature-note">电子签名记录：{{doc.signatureIds.length?doc.signatureIds.join('、'):'未签署'}}。EBR 只展示原签名证据，不重新生成签名。</p>
    </article>
   </section>
  </div>

  <a-card v-if="!book.legacy" title="EBR 归档与打印" class="archive-card">
   <div class="archive-actions">
    <template v-if="auth.can('ebr:pdf:generate')">
     <label><span>归档类型</span><a-select v-model:value="kind" style="width:170px"><a-select-option value="REVIEW_COPY">审核副本</a-select-option><a-select-option value="FINAL" :disabled="!formalReady">正式归档</a-select-option></a-select></label>
     <label class="reason"><span>生成原因</span><a-input v-model:value="reason" maxlength="1000" placeholder="请输入生成原因"/></label>
     <a-button type="primary" :loading="busy" @click="generate">生成 PDF</a-button>
    </template>
   </div>
   <p class="archive-hint">正式归档要求全部必需记录完成并满足既有 QA 审核条件；后续更正会形成新的归档产物，历史 PDF 保留。</p>
   <a-table :scroll="{x:650}" row-key="id" :data-source="history" :pagination="false" :columns="[{title:'归档产物',dataIndex:'id'},{title:'类型',dataIndex:'archive_kind'},{title:'归档时间',dataIndex:'created_at'},{title:'操作',key:'action'}]">
    <template #bodyCell="{column,record}"><a-button v-if="column.key==='action'" type="link" :disabled="busy" @click="openPdf(record)">在线预览 / 重打</a-button></template>
   </a-table>
  </a-card>
 </template>

 <a-modal v-model:open="pdfOpen" title="电子批记录 PDF" :width="'min(1100px,96vw)'" :footer="null">
  <PdfPreview :blob="blob" :name="'电子批记录-'+(book?.batchNo??'批次')+'.pdf'"/>
 </a-modal>
</main>
</template>

<style scoped>
.ebr-batch-page{display:grid;gap:16px;min-width:0}.summary-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px}.summary-grid>div{display:flex;flex-direction:column;gap:3px;border-right:1px solid var(--mes-ui-border);padding-right:12px}.summary-grid>div:last-child{border-right:0}.summary-grid span,.summary-grid small{font-size:12px;color:var(--mes-ui-secondary)}.summary-grid strong{font-size:16px}.legacy-box{display:grid;gap:16px}.legacy-box p{color:var(--mes-ui-secondary)}.ebr-layout{display:grid;grid-template-columns:300px minmax(0,1fr);gap:16px;align-items:start}.ebr-directory{background:#fff;border:1px solid var(--mes-ui-border);border-radius:8px;padding:14px;max-height:calc(100vh - 180px);overflow:auto;position:sticky;top:12px}.directory-title h2,.record-header h2{font-size:17px;margin:0}.legend{display:flex;flex-wrap:wrap;gap:6px 10px;margin:8px 0 12px;font-size:10px}.done{color:var(--mes-ui-success)}.pending{color:var(--mes-ui-warning)}.missing{color:var(--mes-ui-danger)}.na{color:#8a95a5}.flow-group{border-top:1px solid var(--mes-ui-border);padding-top:9px;margin-top:8px}.flow-group h3{font-size:13px;margin:0 0 5px;display:flex;gap:7px}.flow-group h3 span{font-size:11px;color:var(--mes-ui-secondary)}.record-node{width:100%;display:flex;align-items:flex-start;gap:8px;border:0;background:transparent;border-radius:6px;padding:8px;text-align:left;cursor:pointer;color:var(--mes-ui-text)}.record-node:hover,.record-node.active{background:#edf5ff}.state-dot{width:16px;text-align:center;font-size:11px;margin-top:2px}.node-main{display:flex;flex-direction:column;min-width:0}.node-main b{font-size:12.5px}.node-main small{font-size:10.5px;color:var(--mes-ui-secondary);margin-top:2px}.record-workspace{min-width:0}.record-header{background:#fff;border:1px solid var(--mes-ui-border);border-radius:8px;padding:14px 18px;display:flex;justify-content:space-between;align-items:center}.record-header p{margin:4px 0 0;color:var(--mes-ui-secondary);font-size:12px}.source-document{background:#fff;border:1px solid var(--mes-ui-border);border-radius:8px;padding:20px;margin-top:14px}.source-head{display:flex;justify-content:space-between;gap:12px;align-items:center;margin-bottom:16px}.source-head h3{font-size:16px;margin:0 0 4px}.source-head span,.signature-note{font-size:12px;color:var(--mes-ui-secondary)}.record-table-scroll{overflow:auto}table{border-collapse:collapse;width:100%;font-size:13px}th,td{border:1px solid #9aa5b4;padding:9px;white-space:pre-wrap;overflow-wrap:anywhere;min-width:85px}th{background:#f5f7fa;text-align:left}.archive-actions{display:flex;align-items:center;gap:12px;flex-wrap:wrap}.archive-actions label{display:flex;align-items:center;gap:8px}.archive-actions label>span{color:var(--mes-ui-secondary);font-size:13px}.archive-actions .reason{flex:1;min-width:300px}.archive-actions .reason :deep(.ant-input){flex:1}.archive-hint{font-size:12px;color:var(--mes-ui-secondary);margin:10px 0 14px}
@media(max-width:980px){.summary-grid{grid-template-columns:repeat(2,1fr)}.ebr-layout{grid-template-columns:1fr}.ebr-directory{position:static;max-height:none}.summary-grid>div{border-right:0}}@media(max-width:620px){.summary-grid{grid-template-columns:1fr}.source-head{align-items:flex-start;flex-direction:column}.archive-actions .reason{min-width:100%}}
</style>
