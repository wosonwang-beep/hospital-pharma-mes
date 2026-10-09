<script setup lang="ts">
import {onMounted,ref} from 'vue'
import {useRouter} from 'vue-router'
import {message} from 'ant-design-vue'
import {api,errorMessage} from '../../api/http'
import {templates,templatePreview,stateLabels,type PrintTemplate,type PrintSource,printTypeLabels} from '../../api/printing'
import {useAuthStore} from '../../stores/auth'
import PdfPreview from '../../components/printing/PdfPreview.vue'
const auth=useAuthStore(),router=useRouter(),items=ref<PrintTemplate[]>([]),total=ref(0),page=ref(1),keyword=ref(''),status=ref<string>(),businessType=ref<string>(),types=ref<string[]>([]),sources=ref<PrintSource[]>([]),printType=ref<string>(),busy=ref(false),error=ref('')
const command=ref<{row:PrintTemplate;action:string}|null>(null),bindingEnabled=ref(true),previewOpen=ref(false),blob=ref<Blob|null>(null)
const typeLabel=(type:string)=>{const source=sources.value.find(row=>row.value===type);return source?`${source.module} · ${source.label}`:type}
async function load(){busy.value=true;error.value='';try{const p=await templates({page:page.value-1,size:20,keyword:keyword.value,status:status.value,businessType:businessType.value,printType:printType.value});items.value=p.items;total.value=p.total}catch(e){error.value=errorMessage(e)}finally{busy.value=false}}
onMounted(async()=>{try{sources.value=await api<PrintSource[]>({url:'/printing/sources'});types.value=sources.value.map(source=>source.value);await load()}catch(e){error.value=errorMessage(e)}})
function query(){page.value=1;void load()}
function reset(){keyword.value='';status.value=undefined;businessType.value=undefined;printType.value=undefined;query()}
function edit(row:PrintTemplate){void router.push({path:`/admin/print-templates/${row.id}/design`,query:{businessType:row.businessType,printType:row.printType,code:row.templateCode,name:row.templateName}})}
async function act(){if(!command.value)return;const {row,action}=command.value;busy.value=true;try{await api({url:`/printing/templates/${row.id}/${action}`,method:'POST',data:action==='bind'?{businessType:row.businessType,enabled:bindingEnabled.value,reason:'打印配置维护（系统记录）'}:{versionNo:row.versionNo,reason:'打印配置维护（系统记录）'}});command.value=null;message.success('操作完成');await load()}catch(e){error.value=errorMessage(e)}finally{busy.value=false}}
async function preview(row:PrintTemplate){try{blob.value=await templatePreview(row.id);previewOpen.value=true}catch(e){error.value=errorMessage(e)}}
const actions:Record<string,string>={validate:'验证并生成 PDF',publish:'发布模板',deactivate:'停用模板',bind:'业务绑定'}
function ask(row:PrintTemplate,action:string){bindingEnabled.value=true;command.value={row,action}}
</script>
<template>
 <section class="admin-page master-page template-page" data-ui-template="T1">
  <header class="admin-page-header heading"><div><h1>打印模板</h1><p>按业务数据源设计模板，业务页面打印时选择适用版本。</p></div><a-button v-if="auth.can('print:template:manage')" type="primary" @click="router.push('/admin/print-templates/new')">＋ 新增打印模板</a-button></header>
  <a-alert v-if="error" type="error" :message="error" show-icon class="notice"/>
  <section class="query-section" aria-label="打印模板查询条件">
   <h2>查询条件</h2><form class="query-form" @submit.prevent="query">
    <label class="query-pair"><span>名称或编码</span><a-input v-model:value="keyword" placeholder="模板名称 / 编码" allow-clear/></label>
    <label class="query-pair"><span>业务数据源</span><a-select v-model:value="businessType" placeholder="全部数据源" allow-clear :options="types.map(value=>({value,label:typeLabel(value)}))"/></label>
    <label class="query-pair status-pair"><span>状态</span><a-select v-model:value="status" placeholder="全部状态" allow-clear :options="Object.entries(stateLabels).map(([value,label])=>({value,label}))"/></label>
    <label class="query-pair"><span>打印类型</span><a-select v-model:value="printType" placeholder="全部类型" allow-clear :options="Object.entries(printTypeLabels).map(([value,label])=>({value,label}))"/></label>
    <a-space class="query-actions"><a-button @click="reset">重置</a-button><a-button type="primary" html-type="submit" :loading="busy">查询</a-button></a-space>
   </form>
  </section>
  <section class="result-section" aria-label="打印模板列表"><h2>模板列表</h2>
   <a-table :data-source="items" row-key="id" :loading="busy" :pagination="{current:page,total,pageSize:20,showSizeChanger:false,showTotal:(n:number)=>`共 ${n} 个模板版本`,onChange:(p:number)=>{page=p;void load()}}" :scroll="{x:1050}">
    <a-table-column title="模板名称" data-index="templateName" :width="220"/><a-table-column title="模板编码" data-index="templateCode" :width="180"/><a-table-column title="业务数据源" :width="240"><template #default="{record}">{{typeLabel(record.businessType)}}</template></a-table-column><a-table-column title="打印类型" :width="100"><template #default="{record}">{{printTypeLabels[record.printType as keyof typeof printTypeLabels]}}</template></a-table-column><a-table-column title="版本" :width="80"><template #default="{record}">V{{record.templateRevision}}</template></a-table-column>
    <a-table-column title="状态" :width="100"><template #default="{record}"><a-tag :color="record.status==='PUBLISHED'?'green':record.status==='VALIDATED'?'blue':undefined">{{stateLabels[record.status]??record.status}}</a-tag></template></a-table-column>
    <a-table-column title="操作" :width="230" fixed="right"><template #default="{record}"><a-space>
     <a-button v-if="auth.can('print:template:manage')" type="link" @click="edit(record)">编辑</a-button><a-button type="link" :disabled="!record.previewHash" @click="preview(record)">PDF 预览</a-button>
     <a-dropdown v-if="auth.can('print:template:manage')||auth.can('print:template:publish')"><a-button type="link">更多 ▾</a-button><template #overlay><a-menu>
      <a-menu-item v-if="auth.can('print:template:manage')&&['DRAFT','VALIDATED'].includes(record.status)" @click="ask(record,'validate')">验证并生成 PDF</a-menu-item>
      <a-menu-item v-if="auth.can('print:template:publish')&&record.status==='VALIDATED'" @click="ask(record,'publish')">发布</a-menu-item>
      <a-menu-item v-if="auth.can('print:template:publish')&&record.status==='PUBLISHED'" @click="ask(record,'bind')">业务绑定</a-menu-item>
      <a-menu-item v-if="auth.can('print:template:publish')&&record.status==='PUBLISHED'" danger @click="ask(record,'deactivate')">停用</a-menu-item>
     </a-menu></template></a-dropdown>
    </a-space></template></a-table-column>
   </a-table>
  </section>
  <a-modal :open="!!command" :title="command?actions[command.action]:''" :confirm-loading="busy" @cancel="command=null" @ok="act"><p>{{command?.row.templateName}} · 第 {{command?.row.templateRevision}} 版</p><p v-if="command?.action==='validate'">使用后端示例数据校验字段并生成真实 PDF。</p><a-form-item v-if="command?.action==='bind'" label="业务绑定状态"><a-radio-group v-model:value="bindingEnabled"><a-radio :value="true">启用</a-radio><a-radio :value="false">停用绑定</a-radio></a-radio-group></a-form-item></a-modal>
  <a-modal v-model:open="previewOpen" title="模板 PDF 预览（示例数据）" width="90vw" :footer="null"><PdfPreview :blob="blob" name="模板预览.pdf"/></a-modal>
 </section>
</template>
<style scoped>
.heading{display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap;margin-bottom:16px}.heading h1{font-size:22px;font-weight:600;margin:0 0 8px}.heading p{font-size:13px;color:#66778b;margin:0}.notice{margin-bottom:16px}.query-section,.result-section{background:white;border:1px solid #dce5ed;border-radius:8px;margin-bottom:16px}.query-section h2,.result-section h2{font-size:16px;font-weight:600;border-bottom:1px solid #dce5ed;padding:16px 20px;margin:0}.query-form{display:flex;align-items:center;flex-wrap:wrap;gap:16px 24px;padding:16px 20px}.query-pair{display:flex;align-items:center;flex-wrap:nowrap;gap:12px;flex:1 1 300px;min-width:0}.query-pair>span{white-space:nowrap;flex:0 0 78px;text-align:right;font-size:13px;color:#66778b}.query-pair>.ant-input-affix-wrapper,.query-pair>.ant-select{flex:1;min-width:0}.status-pair{flex:1 1 240px}.query-actions{margin-left:auto}.result-section :deep(.ant-table-wrapper){padding:16px 20px}.result-section :deep(.ant-table-cell .ant-btn-link){padding:0 4px}
@media(max-width:900px){.query-pair{flex:1 1 100%}.status-pair{flex-basis:100%}.query-form{gap:12px;padding:16px}.query-pair>span{flex-basis:78px}.result-section :deep(.ant-table-wrapper){padding:12px}.heading p{line-height:1.6}}
</style>
