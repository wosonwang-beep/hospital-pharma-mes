<script setup lang="ts">
import {computed,onMounted,reactive,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,errorMessage,idempotencyKey,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import {type DictionaryType,type DictionaryItem,type DictionaryItemPayload,statusLabel,kindLabel,structureLabel} from './dictionaryModel'
import {csvHeader,csvTemplate,parseDictionaryImport,type DictionaryImportRow,type DictionaryImportPreview} from './dictionaryImportCsv'
const router=useRouter(),route=useRoute(),auth=useAuthStore()
const data=ref<Page<DictionaryType>>({items:[],total:0,page:0,size:20}),loading=ref(false),error=ref(''),notice=ref('')
const filters=reactive({code:String(route.query.code??''),name:String(route.query.name??''),kind:String(route.query.kind??''),status:String(route.query.status??''),createdFrom:String(route.query.createdFrom??''),createdTo:String(route.query.createdTo??'')})
const modal=ref(false),chosen=ref<DictionaryType|null>(null),items=ref<DictionaryItem[]>([]),itemLoading=ref(false),saving=ref(false),exporting=ref(false)
const editItem=ref<DictionaryItem|null>(null),showEditor=ref(false),itemError=ref('')
const itemForm=reactive<DictionaryItemPayload>({code:'',label:'',description:null,parentId:null,sortNo:10,status:'ACTIVE',isDefault:false})
const importOpen=ref(false),importBusy=ref(false),importError=ref(''),importName=ref('')
const importRows=ref<DictionaryImportRow[]>([]),importPreview=ref<DictionaryImportPreview|null>(null)
const expandedTreeKeys=ref<string[]>([])
const treeNodes=computed(()=>{
 const make=(parent:string|null,visited:Set<string>):any[]=>items.value.filter(x=>x.parentId===parent&&!visited.has(x.id)).map(x=>{
  const next=new Set(visited);next.add(x.id)
  return {key:x.id,title:`${x.label}（${x.code}）`,disabled:false,children:make(x.id,next)}
 })
 return make(null,new Set())
})
function selectTree(keys:(string|number)[]){
 const target=items.value.find(x=>x.id===String(keys[0]??''))
 if(target)startEdit(target)
}
function templateDownload(){
 const url=URL.createObjectURL(new Blob([csvTemplate],{type:'text/csv;charset=utf-8'}))
 const a=document.createElement('a');a.href=url;a.download='MES_数据字典导入模板.csv';a.click();URL.revokeObjectURL(url)
}
async function chooseImportFile(event:Event){
 importError.value='';importPreview.value=null;importRows.value=[]
 const file=(event.target as HTMLInputElement).files?.[0];importName.value=file?.name??''
 if(!file)return
 if(!file.name.toLowerCase().endsWith('.csv')){importError.value='请使用 CSV 模板；Excel 文件请另存为 UTF-8 CSV';return}
 if(file.size>1024*1024){importError.value='CSV 文件不能超过 1MB';return}
 try{
  importRows.value=parseDictionaryImport(await file.text())
  importBusy.value=true
  importPreview.value=await api<DictionaryImportPreview>({method:'POST',url:'/dictionaries/import/preview',data:importRows.value})
 }catch(e){importError.value=e instanceof Error?e.message:errorMessage(e)}
 finally{importBusy.value=false}
}
async function confirmImport(){
 if(importBusy.value||!importPreview.value?.accepted)return
 const accepted=importPreview.value.items.filter(x=>x.result==='PASS').map(x=>importRows.value[x.line-2])
 importBusy.value=true;importError.value=''
 try{
  const result=await api<{importedCount:number}>({method:'POST',url:'/dictionaries/import',headers:{'Idempotency-Key':idempotencyKey()},data:accepted})
  importOpen.value=false;notice.value=`已导入 ${result.importedCount} 个字典；冲突项未覆盖`;await load(0)
 }catch(e){importError.value=errorMessage(e);importPreview.value=null}
 finally{importBusy.value=false}
}
async function load(page=0) {
 loading.value=true;error.value=''
 try {
  await router.replace({query:{...Object.fromEntries(Object.entries(filters).map(([k,v])=>[k,v||undefined])),page:page||undefined}})
  data.value=await api<Page<DictionaryType>>({url:'/dictionaries',params:{...filters,page,size:20}})
 }catch(e){error.value=errorMessage(e)}finally{loading.value=false}
}
function reset(){Object.assign(filters,{code:'',name:'',kind:'',status:'',createdFrom:'',createdTo:''});void load()}
async function exportAll(){
 exporting.value=true;error.value=''
 try{
  const records:DictionaryType[]=[]
  for(let page=0;page<100;page++){
   const data=await api<Page<DictionaryType>>({url:'/dictionaries',params:{...filters,page,size:100}})
   records.push(...data.items)
   if(records.length>=data.total)break
   if(!data.items.length)throw new Error('导出数据不完整')
   if(page===99)throw new Error('数据量超过单次导出限制')
  }
  const quote=(value:unknown)=>{let v=String(value??'');if(/^[=+@-]/.test(v))v="'"+v;return '"'+v.replace(/"/g,'""')+'"'}
  const lines=[['字典CODE','字典名称','字典类型','字典结构','字典项数量','状态','创建时间','更新时间','字典描述'],...records.map(r=>[r.code,r.name,kindLabel[r.kind],structureLabel[r.structure],r.itemCount,statusLabel[r.status],r.createdAt,r.updatedAt,r.description??''])]
  const csv='\uFEFF'+lines.map(row=>row.map(quote).join(',')).join('\r\n')
  const href=URL.createObjectURL(new Blob([csv],{type:'text/csv;charset=utf-8'}))
  const a=document.createElement('a');a.href=href;a.download='MES_数据字典.csv';a.click();URL.revokeObjectURL(href)
  notice.value='导出文件已生成'
 }catch(e){error.value=errorMessage(e)}finally{exporting.value=false}
}
function open(id?:string,mode='create'){void router.push({path:id?`/admin/dictionaries/${id}${mode==='edit'?'/edit':''}`:'/admin/dictionaries/create',query:route.query})}
async function openItems(row:DictionaryType){
 chosen.value=row;modal.value=true;showEditor.value=false;itemError.value='';await refreshItems()
}
async function refreshItems(){
 if(!chosen.value)return
 itemLoading.value=true
 try{
  items.value=await api<DictionaryItem[]>({url:`/dictionaries/${chosen.value.id}/items`})
  expandedTreeKeys.value=items.value.filter(x=>items.value.some(child=>child.parentId===x.id)).map(x=>x.id)
 }
 catch(e){itemError.value=errorMessage(e)}
 finally{itemLoading.value=false}
}
function startEdit(item?:DictionaryItem) {
 editItem.value=item??null;Object.assign(itemForm,item?{code:item.code,label:item.label,description:item.description,parentId:item.parentId,sortNo:item.sortNo,status:item.status,isDefault:item.isDefault}:
 {code:'',label:'',description:null,parentId:null,sortNo:10,status:'ACTIVE',isDefault:false})
 itemError.value='';showEditor.value=true
}
async function saveItem(){
 if(!chosen.value||saving.value)return
 if(!itemForm.code.trim()||!itemForm.label.trim()){itemError.value='请填写字典项编码与名称';return}
 saving.value=true;itemError.value=''
 try{
  await api<DictionaryItem>({method:editItem.value?'PUT':'POST',
   url:`/dictionaries/${chosen.value.id}/items${editItem.value?'/'+editItem.value.id:''}`,
   headers:{'Idempotency-Key':idempotencyKey(),...(editItem.value?{'If-Match':`"${editItem.value.version}"`}:{})},
   data:{...itemForm,code:itemForm.code.trim(),label:itemForm.label.trim()}})
  showEditor.value=false;notice.value='字典项保存成功';await Promise.all([refreshItems(),refreshPage()])
 }catch(e){itemError.value=errorMessage(e)}finally{saving.value=false}
}
async function refreshPage(){const pg=data.value.page;const fresh=await api<Page<DictionaryType>>({url:'/dictionaries',params:{...filters,page:pg,size:20}});data.value=fresh;chosen.value=fresh.items.find(x=>x.id===chosen.value?.id)??chosen.value}
async function toggle(item:DictionaryItem) {
 if(!chosen.value||saving.value)return
 if(!window.confirm(`确认${item.status==='ACTIVE'?'停用':'启用'}字典项「${item.label}」？停用后新业务不能选择，历史值保持不变。`))return
 saving.value=true;itemError.value=''
 try{
  await api({method:'PUT',url:`/dictionaries/${chosen.value.id}/items/${item.id}`,headers:{'Idempotency-Key':idempotencyKey(),'If-Match':`"${item.version}"`},
   data:{code:item.code,label:item.label,description:item.description,parentId:item.parentId,sortNo:item.sortNo,status:item.status==='ACTIVE'?'INACTIVE':'ACTIVE',isDefault:item.isDefault}})
  await refreshItems()
 }catch(e){itemError.value=errorMessage(e)}finally{saving.value=false}
}
const columns=[{title:'字典 CODE',dataIndex:'code',width:150},{title:'字典名称',dataIndex:'name',width:126},
 {title:'字典类型',key:'kind',width:83},{title:'字典结构',key:'structure',width:83},
 {title:'字典项',key:'itemCount',width:74},{title:'字典描述',dataIndex:'description',ellipsis:true},
 {title:'状态',key:'status',width:75},{title:'创建时间',key:'createdAt',width:103},{title:'更新时间',key:'updatedAt',width:103},{title:'操作',key:'action',width:120}]
onMounted(()=>void load(Number(route.query.page??0)))
</script>
<template>
<main data-ui-template="T1" class="admin-page t1-query-list dictionary-list">
 <header class="admin-page-header"><div><h1>数据字典</h1></div></header>
 <a-card class="query-card" title="查询条件">
  <form class="query-form dict-query" style="display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px" @submit.prevent="load(0)">
   <label><span>字典 CODE</span><a-input v-model:value="filters.code" allow-clear placeholder="请输入 CODE"/></label>
   <label><span>字典名称</span><a-input v-model:value="filters.name" allow-clear placeholder="请输入名称"/></label>
   <label><span>字典类型</span><a-select v-model:value="filters.kind" style="width:100%" :options="[{value:'',label:'全部类型'},{value:'SYSTEM',label:'系统级'},{value:'BUSINESS',label:'业务级'}]"/></label>
   <label><span>状态</span><a-select v-model:value="filters.status" style="width:100%" :options="[{value:'',label:'全部状态'},{value:'ACTIVE',label:'启用'},{value:'INACTIVE',label:'停用'}]"/></label>
   <label><span>创建日期</span><div class="dict-date-range"><input v-model="filters.createdFrom" class="master-native-input" type="date" aria-label="创建开始"/><span>至</span><input v-model="filters.createdTo" class="master-native-input" type="date" aria-label="创建结束"/></div></label>
   <a-space class="dict-actions"><a-button @click="reset">重置</a-button><a-button type="primary" html-type="submit">查询</a-button></a-space>
  </form>
 </a-card>
 <a-alert v-if="error" type="error" :message="error" show-icon/><a-alert v-if="notice" type="success" :message="notice" show-icon closable @close="notice=''"/>
 <a-card title="字典列表" class="result-card">
  <div class="master-toolbar dict-toolbar">
   <a-space><a-button v-if="auth.can('iam:dict:create')" type="primary" @click="open()">＋ 新增字典</a-button><a-button v-if="auth.can('iam:dict:create')" @click="importOpen=true">导入 CSV</a-button><a-button :loading="exporting" @click="exportAll">导出 CSV</a-button></a-space>
   <span>共 {{data.total}} 条</span>
  </div>
  <a-table :columns="columns" :data-source="data.items" row-key="id" :loading="loading" :pagination="false" :scroll="{x:1000}">
   <template #bodyCell="{column,record}">
    <template v-if="column.key==='kind'">{{kindLabel[record.kind as keyof typeof kindLabel]}}</template>
    <template v-else-if="column.key==='structure'">{{structureLabel[record.structure as keyof typeof structureLabel]}}</template>
    <template v-else-if="column.key==='itemCount'"><a-button size="small" @click="openItems(record)">{{record.itemCount}} 项</a-button></template>
    <template v-else-if="column.key==='status'"><a-tag :color="record.status==='ACTIVE'?'success':'default'">{{statusLabel[record.status as keyof typeof statusLabel]}}</a-tag></template>
    <template v-else-if="column.key==='createdAt'">{{record.createdAt?.slice(0,10)??'—'}}</template>
    <template v-else-if="column.key==='updatedAt'">{{record.updatedAt?.slice(0,10)??'—'}}</template>
    <template v-else-if="column.key==='action'">
      <a-button type="link" @click="open(record.id,'view')">查看</a-button>
      <a-button v-if="auth.can('iam:dict:update')" type="link" @click="open(record.id,'edit')">编辑</a-button>
    </template>
    <strong v-else-if="column.dataIndex==='name'">{{record.name}}</strong>
   </template>
  </a-table>
  <div class="table-footer"><span>第 {{data.page+1}} 页</span><a-pagination :current="data.page+1" :page-size="data.size" :total="data.total" :show-size-changer="false" @change="(p:number)=>load(p-1)"/></div>
 </a-card>
 <a-modal v-model:open="modal" :title="`字典项信息　${chosen?.name??''}`" :width="920" :footer="null" :destroy-on-close="true" class="dictionary-modal">
  <a-alert v-if="itemError" type="error" :message="itemError" show-icon/>
  <div class="dict-modal-toolbar"><span>编码：{{chosen?.code}}　·　{{chosen?.structure==='TREE'?'树形结构':'平级结构'}}</span>
   <a-button v-if="auth.can('iam:dict:update')" @click="startEdit()">＋ 新增字典项</a-button>
  </div>
  <div v-if="chosen?.structure==='TREE'" class="dict-tree-box">
    <div class="dict-tree-title">树形层级 · 点击节点编辑；新增时可选择上级</div>
    <a-tree :tree-data="treeNodes" :expanded-keys="expandedTreeKeys" @expand="(keys:(string|number)[])=>expandedTreeKeys=keys.map(String)" @select="selectTree"/>
  </div>
  <a-table :data-source="items" row-key="id" :loading="itemLoading" :pagination="false" :scroll="{x:760,y:250}" size="small">
   <a-table-column title="选项值" data-index="code" :width="150"/>
   <a-table-column title="选项名称" data-index="label" :width="120"/>
   <a-table-column title="字典项描述" data-index="description" :ellipsis="true"/>
   <a-table-column title="状态" :width="90"><template #default="{record}"><a-tag :color="record.status==='ACTIVE'?'success':'default'">{{statusLabel[record.status as keyof typeof statusLabel]}}</a-tag></template></a-table-column>
   <a-table-column title="操作" :width="150"><template #default="{record}"><a-button type="link" :disabled="!auth.can('iam:dict:update')" @click="startEdit(record)">编辑</a-button><a-button type="link" :disabled="!auth.can('iam:dict:update')" @click="toggle(record)">{{record.status==='ACTIVE'?'停用':'启用'}}</a-button></template></a-table-column>
  </a-table>
  <section v-if="showEditor" class="dict-item-editor">
   <div class="dict-item-grid">
    <label><span class="form-field-label">选项编码</span><a-input v-model:value="itemForm.code" :disabled="!!editItem" placeholder="请输入编码"/></label>
    <label><span class="form-field-label">选项名称</span><a-input v-model:value="itemForm.label" placeholder="请输入名称"/></label>
    <label><span class="form-field-label">排序</span><a-input-number v-model:value="itemForm.sortNo" :min="0" :max="999999" style="width:100%"/></label>
    <label><span class="form-field-label">状态</span><a-select v-model:value="itemForm.status" style="width:100%" :options="[{value:'ACTIVE',label:'启用'},{value:'INACTIVE',label:'停用'}]"/></label>
    <label v-if="chosen?.structure==='TREE'"><span class="form-field-label">上级选项</span><a-select v-model:value="itemForm.parentId" allow-clear style="width:100%" :options="[{value:null,label:'根节点'},...items.filter(x=>x.id!==editItem?.id).map(x=>({value:x.id,label:x.label}))]"/></label>
    <label><span class="form-field-label">默认选项</span><a-checkbox v-model:checked="itemForm.isDefault" aria-label="设为默认选项"/></label>
   </div>
   <div class="dict-item-footer"><a-button @click="showEditor=false">取消</a-button><a-button type="primary" :loading="saving" @click="saveItem">保存选项</a-button></div>
  </section>
 </a-modal>
 <a-modal v-model:open="importOpen" class="dictionary-import-modal" title="数据字典导入预检" :width="800" :footer="null" :mask-closable="!importBusy">
   <div class="dict-import-top">
    <span>使用 UTF-8 CSV 模板导入新字典；已有字典不会覆盖。</span>
    <a-button @click="templateDownload">下载模板</a-button>
   </div>
   <label class="dict-import-file"><span>选择文件</span><input type="file" accept=".csv,text/csv" aria-label="上传 CSV" @change="chooseImportFile"/></label>
   <a-alert v-if="importError" type="error" :message="importError" show-icon/>
   <div v-if="importName" class="dict-import-summary">
     {{importName}}　 <template v-if="importPreview">可导入 {{importPreview.accepted}} 条 / 冲突 {{importPreview.rejected}} 条</template>
   </div>
   <a-table v-if="importPreview" :data-source="importPreview.items" row-key="line" :pagination="false" :scroll="{y:260}" size="small">
    <a-table-column title="行号" data-index="line" :width="60"/>
    <a-table-column title="字典 CODE" data-index="code" :width="180"/>
    <a-table-column title="字典名称" data-index="name" :width="145"/>
    <a-table-column title="校验"><template #default="{record}"><a-tag :color="record.result==='PASS'?'success':'warning'">{{record.result==='PASS'?'通过':'冲突'}}</a-tag></template></a-table-column>
    <a-table-column title="校验说明" data-index="message"/>
   </a-table>
   <div class="dict-import-footer">
    <span>冲突项不会导入；正式提交时后端再次校验。</span>
    <a-space><a-button :disabled="importBusy" @click="importOpen=false">取消</a-button>
     <a-button type="primary" :loading="importBusy" :disabled="!importPreview?.accepted" @click="confirmImport">
       仅导入 {{importPreview?.accepted??0}} 条通过项
     </a-button></a-space>
   </div>
 </a-modal>
</main>
</template>
<style scoped>
.dict-query{display:grid;grid-template-columns:repeat(3,minmax(0,1fr))}
.dict-query label{display:grid;grid-template-columns:110px minmax(0,1fr);align-items:center;gap:10px;white-space:nowrap;min-width:0}
.dict-query label>span{text-align:right;line-height:34px}
.dict-actions{grid-column:1/-1;justify-self:end}
.dict-date-range{display:flex;align-items:center;gap:5px;min-width:0;width:100%}.dict-date-range input{min-width:0;flex:1 1 0;width:0;font-size:12px;padding:2px}.dict-date-range span{color:#929baa}
.dict-toolbar{display:flex;justify-content:space-between;align-items:center}
.dict-modal-toolbar{display:flex;align-items:center;justify-content:space-between;margin:12px 0;color:#6b7886;font-size:13px}
.dict-item-editor{border:1px solid #dbe5f1;background:#f8fbff;padding:15px;margin-top:14px;border-radius:4px}
.dict-item-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}
.dict-item-grid label{display:grid;grid-template-columns:100px minmax(0,1fr);align-items:center;gap:10px}
.dict-item-grid .form-field-label{text-align:right}
.dict-item-footer{display:flex;justify-content:flex-end;gap:10px;margin-top:12px}
.dict-tree-box{border:1px solid #e1e8f1;border-radius:4px;padding:10px 16px;max-height:210px;overflow:auto;margin-bottom:12px}
.dict-tree-title{font-size:12px;color:#6c8197;margin-bottom:10px}
.dict-import-top,.dict-import-footer{display:flex;justify-content:space-between;align-items:center;gap:12px;color:#667991;font-size:12px;margin:12px 0}
.dict-import-file{display:flex;align-items:center;gap:16px;margin:14px 0;font-size:13px}
.dict-import-file>span{flex:0 0 100px;text-align:right}.dict-import-file>input{flex:1;min-width:0}
.dict-import-summary{margin:14px 0;font-size:13px;color:#4d6580}
.dict-import-footer{padding-top:12px;border-top:1px solid #edf0f5}
.dictionary-list .dict-query label{grid-template-columns:130px minmax(0,1fr)}
:global(.dictionary-import-modal .ant-modal-content){background:#fff!important;opacity:1!important;backdrop-filter:none!important}
:global(.dictionary-import-modal .ant-modal-body){background:#fff!important}
:global(.dictionary-import-modal .ant-btn-primary:not(:disabled)){background:#2c5cdc!important;border-color:#2c5cdc!important;color:#fff!important}
@media(max-width:1200px){.dict-query{grid-template-columns:repeat(2,minmax(0,1fr))}}
</style>
