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

async function load(){
  busy.value=true
  error.value=''
  try{
    const p=await templates({page:page.value-1,size:20,keyword:keyword.value,status:status.value,businessType:businessType.value,printType:printType.value})
    items.value=p.items
    total.value=p.total
  }catch(e){
    error.value=errorMessage(e)
  }finally{
    busy.value=false
  }
}

onMounted(async()=>{
  try{
    sources.value=await api<PrintSource[]>({url:'/printing/sources'})
    types.value=sources.value.map(source=>source.value)
    await load()
  }catch(e){
    error.value=errorMessage(e)
  }
})

function query(){page.value=1;void load()}
function reset(){keyword.value='';status.value=undefined;businessType.value=undefined;printType.value=undefined;query()}
function edit(row:PrintTemplate){void router.push({path:`/admin/print-templates/${row.id}/design`,query:{businessType:row.businessType,printType:row.printType,code:row.templateCode,name:row.templateName}})}

async function act(){
  if(!command.value)return
  const {row,action}=command.value
  busy.value=true
  try{
    await api({
      url:`/printing/templates/${row.id}/${action}`,
      method:'POST',
      data:action==='bind'
        ?{businessType:row.businessType,enabled:bindingEnabled.value,reason:'打印配置维护（系统记录）'}
        :{versionNo:row.versionNo,reason:'打印配置维护（系统记录）'}
    })
    command.value=null
    message.success('操作完成')
    await load()
  }catch(e){
    error.value=errorMessage(e)
  }finally{
    busy.value=false
  }
}

async function preview(row:PrintTemplate){
  try{
    blob.value=await templatePreview(row.id)
    previewOpen.value=true
  }catch(e){
    error.value=errorMessage(e)
  }
}

const actions:Record<string,string>={validate:'验证并生成 PDF',publish:'发布模板',deactivate:'停用模板',bind:'业务绑定'}
function ask(row:PrintTemplate,action:string){bindingEnabled.value=true;command.value={row,action}}
</script>

<template>
  <main class="admin-page t1-query-list print-template-list" data-ui-template="T1">
    <header class="admin-page-header">
      <div>
        <h1>打印模板</h1>
      </div>
      <a-button
        v-if="auth.can('print:template:manage')"
        type="primary"
        @click="router.push('/admin/print-templates/new')"
      >
        ＋ 新增打印模板
      </a-button>
    </header>

    <a-alert v-if="error" type="error" :message="error" show-icon />

    <a-card title="查询条件" class="query-card">
      <form class="query-form print-template-query" @submit.prevent="query">
        <label>
          <span class="form-field-label">名称或编码</span>
          <a-input v-model:value="keyword" placeholder="请输入模板名称或编码" allow-clear />
        </label>
        <label>
          <span class="form-field-label">业务数据源</span>
          <a-select
            v-model:value="businessType"
            placeholder="全部数据源"
            allow-clear
            :options="types.map(value=>({value,label:typeLabel(value)}))"
          />
        </label>
        <label>
          <span class="form-field-label">状态</span>
          <a-select
            v-model:value="status"
            placeholder="全部状态"
            allow-clear
            :options="Object.entries(stateLabels).map(([value,label])=>({value,label}))"
          />
        </label>
        <label>
          <span class="form-field-label">打印类型</span>
          <a-select
            v-model:value="printType"
            placeholder="全部类型"
            allow-clear
            :options="Object.entries(printTypeLabels).map(([value,label])=>({value,label}))"
          />
        </label>
        <a-space class="query-actions">
          <a-button type="primary" html-type="submit" :loading="busy">查询</a-button>
          <a-button @click="reset">重置</a-button>
        </a-space>
      </form>
    </a-card>

    <a-card title="模板列表" class="result-card">
      <a-table
        :data-source="items"
        row-key="id"
        :loading="busy"
        :pagination="false"
        :scroll="{x:1050}"
      >
        <a-table-column title="模板名称" data-index="templateName" :width="220" />
        <a-table-column title="模板编码" data-index="templateCode" :width="180" />
        <a-table-column title="业务数据源" :width="240">
          <template #default="{record}">{{typeLabel(record.businessType)}}</template>
        </a-table-column>
        <a-table-column title="打印类型" :width="100">
          <template #default="{record}">{{printTypeLabels[record.printType as keyof typeof printTypeLabels]}}</template>
        </a-table-column>
        <a-table-column title="版本" :width="80">
          <template #default="{record}">V{{record.templateRevision}}</template>
        </a-table-column>
        <a-table-column title="状态" :width="100">
          <template #default="{record}">
            <a-tag :color="record.status==='PUBLISHED'?'green':record.status==='VALIDATED'?'blue':undefined">
              {{stateLabels[record.status]??record.status}}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="操作" :width="230" fixed="right">
          <template #default="{record}">
            <a-space>
              <a-button v-if="auth.can('print:template:manage')" type="link" @click="edit(record)">编辑</a-button>
              <a-button type="link" :disabled="!record.previewHash" @click="preview(record)">PDF 预览</a-button>
              <a-dropdown v-if="auth.can('print:template:manage')||auth.can('print:template:publish')">
                <a-button type="link">更多 ▾</a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item
                      v-if="auth.can('print:template:manage')&&['DRAFT','VALIDATED'].includes(record.status)"
                      @click="ask(record,'validate')"
                    >
                      验证并生成 PDF
                    </a-menu-item>
                    <a-menu-item
                      v-if="auth.can('print:template:publish')&&record.status==='VALIDATED'"
                      @click="ask(record,'publish')"
                    >
                      发布
                    </a-menu-item>
                    <a-menu-item
                      v-if="auth.can('print:template:publish')&&record.status==='PUBLISHED'"
                      @click="ask(record,'bind')"
                    >
                      业务绑定
                    </a-menu-item>
                    <a-menu-item
                      v-if="auth.can('print:template:publish')&&record.status==='PUBLISHED'"
                      danger
                      @click="ask(record,'deactivate')"
                    >
                      停用
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </a-table-column>
      </a-table>

      <div class="table-scroll-hint" role="note">表格可左右滑动查看完整列</div>
      <div class="table-footer">
        <span role="status" aria-live="polite">共 {{ total }} 条</span>
        <a-pagination
          :current="page"
          :page-size="20"
          :total="total"
          :show-size-changer="false"
          @change="(p:number)=>{page=p;void load()}"
        />
      </div>
    </a-card>

    <a-modal
      :open="!!command"
      :title="command?actions[command.action]:''"
      :confirm-loading="busy"
      @cancel="command=null"
      @ok="act"
    >
      <p>{{command?.row.templateName}} · 第 {{command?.row.templateRevision}} 版</p>
      <p v-if="command?.action==='validate'">使用后端示例数据校验字段并生成真实 PDF。</p>
      <a-form-item v-if="command?.action==='bind'" label="业务绑定状态">
        <a-radio-group v-model:value="bindingEnabled">
          <a-radio :value="true">启用</a-radio>
          <a-radio :value="false">停用绑定</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-modal>

    <a-modal
      v-model:open="previewOpen"
      title="模板 PDF 预览（示例数据）"
      width="90vw"
      :footer="null"
    >
      <PdfPreview :blob="blob" name="模板预览.pdf" />
    </a-modal>
  </main>
</template>

<style scoped>
.print-template-query{
  grid-template-columns:repeat(3,minmax(0,1fr));
}
.print-template-query .form-field-label{
  width:88px;
  text-align:right;
  flex:0 0 88px;
}
.print-template-query>.query-actions{
  grid-column:1/-1!important;
  width:100%;
  justify-self:stretch!important;
  justify-content:flex-end!important;
}
.print-template-list :deep(.ant-table-cell .ant-btn-link){
  padding-inline:4px;
}
.table-scroll-hint{
  display:none;
  margin-top:8px;
  font-size:12px;
  color:var(--mes-ui-secondary);
  text-align:right;
}
@media(max-width:1200px){
  .print-template-query{grid-template-columns:repeat(2,minmax(0,1fr))}
}
@media(max-width:900px){
  .print-template-query{grid-template-columns:1fr}
  .print-template-query .form-field-label{width:95px;flex-basis:95px}
  .table-scroll-hint{display:block}
}
</style>
