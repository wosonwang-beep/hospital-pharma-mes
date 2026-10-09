<script setup lang="ts">
import {computed,nextTick,onBeforeUnmount,onMounted,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {message,Modal} from 'ant-design-vue'
import Editor,{EditorMode,RowFlex,type IEditorData,type IEditorResult} from '@hufe921/canvas-editor'
import docxPlugin from '../../vendor/canvas-editor-docx'
import {api,http,errorMessage as apiErrorMessage} from '../../api/http'
import {isAxiosError} from 'axios'
import {templatePreview,type PrintField,type PrintTemplate,type PrintType,type PrintSource,printTypeLabels} from '../../api/printing'
import PdfPreview from '../../components/printing/PdfPreview.vue'
import {bindingOf,bindingsIn,detailTable,embedCanvas,exportData,fieldElement,fillExample,initialDocument,readCanvas,restoreBindings,validateBindings,type PrintBinding} from './canvasPrintBinding'

const route=useRoute(),router=useRouter(),busy=ref(false),error=ref(''),types=ref<string[]>([]),fields=ref<PrintField[]>([]),example=ref<Record<string,unknown>>({})
const errorMessage=(cause:unknown)=>!isAxiosError(cause)&&cause instanceof Error?cause.message:apiErrorMessage(cause)
const printType=ref<PrintType>('DOCUMENT'),sources=ref<PrintSource[]>([])
const businessType=ref(''),templateCode=ref(''),templateName=ref(''),fieldSearch=ref(''),dataset=ref(''),picked=ref<string[]>([])
const selectedFieldKey=ref('')
const host=ref<HTMLDivElement|null>(null),previewHost=ref<HTMLDivElement|null>(null),fileInput=ref<HTMLInputElement|null>(null)
const activeBinding=ref<PrintBinding|null>(null),activeControlId=ref(''),boundCount=ref(0),dirty=ref(false),saved=ref<PrintTemplate|null>(null)
const previewOpen=ref(false),pdfOpen=ref(false),pdfBlob=ref<Blob|null>(null),importName=ref(''),fontSize=ref(16),zoom=ref(90),currentBindings=ref<string[]>([])
const autoFit=ref(true)
let editor:Editor|null=null,previewEditor:Editor|null=null,exportEditor:Editor|null=null
let resizeObserver:ResizeObserver|null=null
let selectedTableId=''
const existingId=typeof route.params.id==='string'?route.params.id:null
const sourceOptions=computed(()=>sources.value)
const currentSource=computed(()=>sourceOptions.value.find(source=>source.value===businessType.value))
const modules=computed(()=>[...new Set(sourceOptions.value.map(source=>source.module))])
const documentOptions=computed(()=>sourceOptions.value.filter(source=>source.module===currentSource.value?.module))
const groups=computed(()=>[...new Set(fields.value.filter(f=>f.key!=='items').map(f=>f.group))])
const visibleFields=computed(()=>fields.value.filter(f=>f.key!=='items'&&(!dataset.value||f.group===dataset.value)&&(f.label+f.description).includes(fieldSearch.value)))
const fieldGroups=computed(()=>groups.value.map(group=>({group,fields:visibleFields.value.filter(field=>field.group===group)})).filter(group=>group.fields.length))
const selectedField=computed(()=>fields.value.find(field=>field.key===selectedFieldKey.value))
const inspectedField=computed(()=>fields.value.find(field=>field.key===(activeBinding.value?.fieldKey??selectedFieldKey.value)))
const inspectedGroup=computed(()=>inspectedField.value?.group??'')
const inspectedValue=computed(()=>inspectedField.value?.repeated?(example.value.items as Record<string,unknown>[]|undefined)?.[0]?.[inspectedField.value.key]:example.value[inspectedField.value?.key??''])
const inspectedType=computed(()=>inspectedField.value?.dataType==='NUMBER'||typeof inspectedValue.value==='number'?'数值':typeof inspectedValue.value==='boolean'?'布尔值':'文本')
const detailFields=computed(()=>fields.value.filter(f=>f.repeated&&picked.value.includes(f.key)))
const missing=computed(()=>[...['modeLabel','reportNo'].filter(key=>fields.value.some(f=>f.key===key)&&!currentBindings.value.includes(key)),...(printType.value==='LIST'&&!fields.value.some(f=>f.repeated&&currentBindings.value.includes(f.key))?['items']:[])])
function capture():IEditorResult {if(!editor)throw new Error('编辑器尚未就绪');return editor.command.getValue()}
function updateBindings(){if(!editor)return;const bs=bindingsIn(capture().data);boundCount.value=bs.length;currentBindings.value=bs.map(b=>b.fieldKey)}
function createEditor(data:IEditorData,options?:IEditorResult['options']){
 selectedTableId=''
 resizeObserver?.disconnect()
 editor?.destroy();if(!host.value)return
 if(autoFit.value)zoom.value=Math.max(25,Math.min(90,Math.floor(((host.value.parentElement?.clientWidth??794)-32)/794*100)))
 editor=new Editor(host.value,data,{...options,width:794,height:1123,margins:[60,56,60,56],defaultFont:'Microsoft YaHei',defaultSize:16,scale:zoom.value/100,pageGap:16,control:{placeholderColor:'#1677ff'},pageNumber:{format:'第 {pageNo} 页 / 共 {pageCount} 页',rowFlex:RowFlex.CENTER}})
 editor.use(docxPlugin)
 // A new template starts with its heading and required identifiers. Put the
 // initial insertion point after them so the first detail table follows the header.
 if(!options){editor.command.executeSelectAll();const range=editor.command.getRange();if(range)editor.command.executeSetRange(range.endIndex,range.endIndex)}
 editor.listener.contentChange=()=>{dirty.value=true;updateBindings()}
 editor.listener.controlChange=p=>{activeControlId.value=p?.controlId??'';activeBinding.value=(p?.control?.extension as PrintBinding|undefined)??null}
 editor.listener.pageScaleChange=s=>zoom.value=Math.round(s*100)
 if(host.value.parentElement){resizeObserver=new ResizeObserver(()=>{if(autoFit.value&&editor&&host.value){const scale=Math.max(25,Math.min(90,Math.floor((host.value.parentElement!.clientWidth-32)/794*100)));if(scale!==zoom.value)editor.command.executePageScale(scale/100)}});resizeObserver.observe(host.value.parentElement)}
 updateBindings()
}
async function loadFields(type:string){
 const schema=await api<{definitions:PrintField[];example:Record<string,unknown>}>({url:'/printing/fields',params:{businessType:type,printType:printType.value}})
 fields.value=schema.definitions;example.value=schema.example;dataset.value='';selectedFieldKey.value='';picked.value=fields.value.filter(f=>f.repeated).slice(0,6).map(f=>f.key)
}
async function initialize(){
 busy.value=true;error.value=''
 try{
  sources.value=await api<PrintSource[]>({url:'/printing/sources'});types.value=sources.value.map(source=>source.value)
  let buffer:ArrayBuffer|undefined,document:IEditorResult|undefined
  printType.value=route.query.printType==='LIST'?'LIST':'DOCUMENT'
  businessType.value=String(route.query.businessType??types.value[0]??'')
  templateCode.value=String(route.query.code??'');templateName.value=String(route.query.name??'')
  if(existingId){const metadata=await api<PrintTemplate>({url:`/printing/templates/${existingId}`});printType.value=metadata.printType??'DOCUMENT';saved.value=metadata;businessType.value=metadata.businessType;templateCode.value=metadata.templateCode;templateName.value=metadata.templateName;buffer=(await http.get<ArrayBuffer>(`/printing/templates/${existingId}/docx`,{responseType:'arraybuffer'})).data;const restored=await readCanvas(buffer);if(restored){if(restored.businessType!==businessType.value||restored.printType&&restored.printType!==printType.value)throw new Error('模板编辑数据与保存的业务类型不一致');document=restored.document}}
  if(!types.value.includes(businessType.value))throw new Error('当前业务数据源不可用')
  await loadFields(businessType.value);await nextTick()
  if(document)validateBindings(document.data,fields.value,businessType.value)
  createEditor(document?.data??initialDocument(fields.value,businessType.value,templateName.value),document?.options)
  if(buffer&&!document){await editor!.command.executeImportDocx({arrayBuffer:buffer});editor!.command.executeSetValue(restoreBindings(capture().data,fields.value,businessType.value))}
  validateBindings(capture().data,fields.value,businessType.value);dirty.value=false
 }catch(e){error.value=errorMessage(e)}finally{busy.value=false}
}
async function selectType(type:string){
 if(type===businessType.value)return
 const change=async()=>{busy.value=true;try{await loadFields(type);businessType.value=type;createEditor(initialDocument(fields.value,type,templateName.value));activeBinding.value=null;importName.value='';saved.value=null;dirty.value=true}catch(e){error.value=errorMessage(e)}finally{busy.value=false}}
 if(editor&&capture().data.main.length)Modal.confirm({title:'切换业务数据源',content:'切换后开始新数据源的模板，当前编辑内容会被替换。请先保存需要保留的草稿。',okText:'切换数据源',cancelText:'取消',onOk:change});else await change()
}
async function selectPrintType(value:PrintType){if(value===printType.value)return;Modal.confirm({title:'切换打印类型',content:'单据和列表使用不同的数据结构，切换会重建编辑内容，请先保存需要保留的草稿。',okText:'切换',cancelText:'取消',onOk:async()=>{busy.value=true;error.value='';try{printType.value=value;await loadFields(businessType.value);createEditor(initialDocument(fields.value,businessType.value,templateName.value));activeBinding.value=null;importName.value='';saved.value=null;dirty.value=true}catch(e){error.value=errorMessage(e)}finally{busy.value=false}}})}
function selectModule(module:string){const source=sourceOptions.value.find(option=>option.module===module);if(source)void selectType(source.value)}
function selectField(field:PrintField){selectedFieldKey.value=field.key;activeBinding.value=null;activeControlId.value=''}
function insertSelectedField(){if(selectedField.value)insertField(selectedField.value)}
function insertField(field:PrintField){
 if(!editor)return
 const context=editor.command.getRangeContext()
 if(field.repeated&&!(context?.tableElement?.trList?.[context.trIndex??-1]?.extension as {repeat?:boolean}|undefined)?.repeat){message.warning('请先插入明细表，或把当前表格行设为循环行，再在循环行中插入明细字段');return}
 if(!editor.command.getRange()||editor.command.getRange().startIndex<0)editor.command.executeSetRange(0,0)
 editor.command.executeInsertControl(fieldElement(field,businessType.value));updateBindings();dirty.value=true
}
function insertDetails(){try{if(!editor)return;if(!editor.command.getRange()||editor.command.getRange().startIndex<0)editor.command.executeSetRange(0,0);editor.command.executeInsertElementList([{value:'\n'},detailTable(detailFields.value,businessType.value),{value:'\n'}]);updateBindings();dirty.value=true}catch(e){error.value=errorMessage(e)}}
function rememberTable(event:MouseEvent){selectedTableId=editor?.command.getPositionContextByEvent(event,{isMustDirectHit:false})?.tableInfo?.element.id??''}
function repeatCurrentRow(){
 const context=editor?.command.getRangeContext(),table=selectedTableId?editor?.command.getElementById({id:selectedTableId})[0]:undefined,row=table?.trList?.[context?.trIndex??-1]
 if(!editor||!context?.isTable||!table||!row){message.warning('请先点击 Word 表格中需要自动填充明细的那一行');return}
 row.extension={businessType:businessType.value,dataset:'items',repeat:true}
 editor.command.executeUpdateElementById({id:selectedTableId,properties:{trList:table.trList}})
 dirty.value=true;updateBindings();message.success('当前行已绑定为明细循环行，可在单元格中插入明细字段')
}
function replaceField(key:string){const f=fields.value.find(f=>f.key===key);if(!f||!editor||!activeControlId.value)return;const current=activeBinding.value;if(current&&current.repeated!==f.repeated){message.warning('单据字段与循环行字段不能相互替换');return}const b=bindingOf(fieldElement(f,businessType.value))!;editor.command.executeSetControlExtension({id:activeControlId.value,extension:b});editor.command.executeSetControlProperties({id:activeControlId.value,properties:{conceptId:f.key,placeholder:f.label}});activeBinding.value=b;dirty.value=true;updateBindings()}
async function importWord(event:Event){
 const file=(event.target as HTMLInputElement).files?.[0];if(fileInput.value)fileInput.value.value='';if(!file||!editor)return
 if(!/\.docx$/i.test(file.name)||file.size>5*1024*1024){message.error('请选择 5MB 以内的 .docx Word 文件');return}
 const perform=async()=>{busy.value=true;error.value='';const previous=capture();try{const buffer=await file.arrayBuffer();await editor!.command.executeImportDocx({arrayBuffer:buffer});editor!.command.executeSetValue(restoreBindings(capture().data,fields.value,businessType.value));importName.value=file.name;activeBinding.value=null;dirty.value=true;saved.value=null;updateBindings();message.success('Word 内容已带入编辑器，请选择文字或单元格绑定业务字段')}catch(e){createEditor(previous.data,previous.options);error.value=`Word 导入失败：${errorMessage(e)}`}finally{busy.value=false}}
 if(dirty.value)Modal.confirm({title:'从 Word 导入',content:'导入会替换当前编辑内容。请先保存需要保留的草稿。',okText:'导入并替换',cancelText:'取消',onOk:perform});else await perform()
}
async function save():Promise<PrintTemplate|undefined>{
 error.value='';if(!/^[A-Za-z0-9_-]{1,60}$/.test(templateCode.value)||!templateName.value.trim()){error.value='请填写模板编码（字母、数字、下划线或横线）和模板名称';return}
 busy.value=true
 try{
  const snapshot=capture();validateBindings(snapshot.data,fields.value,businessType.value)
  const container=document.createElement('div');container.style.cssText='position:fixed;left:-10000px;top:0';document.body.appendChild(container)
  let output:Blob
  try{exportEditor=new Editor(container,exportData(snapshot.data),snapshot.options);exportEditor.use(docxPlugin);output=await exportEditor.command.executeExportDocx({fileName:templateCode.value})}finally{exportEditor?.destroy();exportEditor=null;container.remove()}
  const blob=await embedCanvas(output!,{schema:'mes.canvas.print.v1',printType:printType.value,businessType:businessType.value,templateCode:templateCode.value,templateName:templateName.value.trim(),document:snapshot})
  if(blob.size>5*1024*1024)throw new Error('生成的 Word 模板超过 5MB')
  const data=new FormData();Object.entries({templateCode:templateCode.value,templateName:templateName.value.trim(),businessType:businessType.value,printType:printType.value,reason:'Canvas 打印模板维护（系统记录）'}).forEach(([k,v])=>data.append(k,v));data.append('file',blob,`${templateCode.value}.docx`)
  saved.value=await api<PrintTemplate>({url:'/printing/templates',method:'POST',data});dirty.value=false;message.success(`已保存第 ${saved.value.templateRevision} 版草稿`);return saved.value
 }catch(e){error.value=errorMessage(e)}finally{busy.value=false}
}
async function previewFill(){if(!editor)return;try{validateBindings(capture().data,fields.value,businessType.value);previewOpen.value=true;await nextTick();previewEditor?.destroy();if(previewHost.value)previewEditor=new Editor(previewHost.value,fillExample(capture().data,example.value),{...capture().options,mode:EditorMode.READONLY,scale:.8})}catch(e){error.value=errorMessage(e)}}
async function verifyPdf(){if(saved.value?.previewHash&&!dirty.value){busy.value=true;try{pdfBlob.value=await templatePreview(saved.value.id);pdfOpen.value=true}catch(e){error.value=errorMessage(e)}finally{busy.value=false}return}let row=saved.value;if(!row||dirty.value)row=await save()??null;if(!row)return;busy.value=true;try{saved.value=await api<PrintTemplate>({url:`/printing/templates/${row.id}/validate`,method:'POST',data:{versionNo:row.versionNo,reason:'Canvas 模板真实 PDF 验证'}});pdfBlob.value=await templatePreview(row.id);pdfOpen.value=true}catch(e){error.value=errorMessage(e)}finally{busy.value=false}}
function command(name:'executeBold'|'executeItalic'|'executeUnderline'|'executeUndo'|'executeRedo'|'executePageBreak'){editor?.command[name]()}
function align(value:RowFlex){editor?.command.executeRowFlex(value)}
function resize(value:number){autoFit.value=value===0;const scale=value||Math.max(25,Math.min(90,Math.floor(((host.value?.parentElement?.clientWidth??794)-32)/794*100)));zoom.value=scale;editor?.command.executePageScale(scale/100)}
function setFont(value:string){editor?.command.executeFont(value)}
function setSize(value:number){editor?.command.executeSize(value)}
function insertPlainTable(){editor?.command.executeInsertTable(3,3)}
function closeFill(){previewEditor?.destroy();previewEditor=null}
onMounted(()=>void initialize())
onBeforeUnmount(()=>{resizeObserver?.disconnect();editor?.destroy();previewEditor?.destroy();exportEditor?.destroy()})
</script>

<template>
 <main class="admin-page master-page canvas-designer" data-ui-template="T2">
  <header class="admin-page-header designer-heading">
   <div><a-button type="link" class="back" @click="router.push('/admin/print-templates')">← 返回打印模板</a-button><h1>{{existingId?'编辑打印模板 · 新草稿版本':'新增打印模板'}}</h1></div>
   <a-space wrap><a-button :disabled="busy||!businessType" @click="fileInput?.click()">从 Word 导入</a-button><a-button :disabled="busy||!businessType" @click="previewFill">预览填充数据</a-button><a-button type="primary" :loading="busy" @click="save">保存草稿</a-button></a-space>
   <input ref="fileInput" class="file-input" type="file" accept=".docx" @change="importWord"/>
  </header>
  <a-alert v-if="error" type="error" show-icon :message="error" class="notice"/>
  <section class="meta-section" aria-label="模板基本信息">
   <label class="inline-pair"><span>模板编码 <i>*</i></span><a-input v-model:value="templateCode" :maxlength="60" :disabled="!!existingId" placeholder="例如 QC_REPORT_STANDARD" @change="dirty=true"/></label>
   <label class="inline-pair"><span>模板名称 <i>*</i></span><a-input v-model:value="templateName" :maxlength="120" placeholder="请输入模板名称" @change="dirty=true"/></label>
   <div class="inline-pair"><span>适用功能</span><span class="meta-value">{{currentSource?.module??'—'}}</span></div>
   <div class="inline-pair"><span>单据类型</span><span class="meta-value">{{currentSource?.label??'—'}}</span></div>
   <label class="inline-pair"><span>打印类型 <i>*</i></span><a-select :value="printType" :disabled="busy||!businessType" :options="Object.entries(printTypeLabels).map(([value,label])=>({value,label}))" @change="selectPrintType($event as PrintType)"/></label>
   <div class="inline-pair"><span>输出规则</span><span class="meta-value">{{printType==='LIST'?'选中记录汇总为列表':'每条记录生成完整单据'}}</span></div>
  </section>
  <a-spin :spinning="busy">
   <div class="designer-workspace">
    <aside class="designer-side field-panel"><h2>数据字段</h2>
     <div class="panel-pair"><label for="print-module">业务功能</label><a-select id="print-module" :value="currentSource?.module" :options="modules.map(value=>({value,label:value}))" :disabled="busy||!!existingId" @change="selectModule($event as string)"/></div>
     <div class="panel-pair"><label for="print-document">单据类型</label><a-select id="print-document" :value="businessType" :options="documentOptions.map(source=>({value:source.value,label:source.label}))" :disabled="busy||!!existingId" @change="selectType($event as string)"/></div>
     <div class="panel-pair"><label for="print-dataset">数据区域</label><a-select id="print-dataset" v-model:value="dataset" :options="[{value:'',label:'全部区域'},...groups.map(value=>({value,label:value}))]"/></div>
     <div class="panel-pair"><label for="print-field-search">字段搜索</label><a-input id="print-field-search" v-model:value="fieldSearch" allow-clear placeholder="名称 / 说明"/></div>
     <div class="field-list"><details v-for="section in fieldGroups" :key="section.group" open><summary>{{section.group}}</summary><div v-for="field in section.fields" :key="field.key" class="field-row" :class="{selected:field.key===selectedFieldKey}"><input v-if="field.repeated" v-model="picked" type="checkbox" :value="field.key" :aria-label="`选择${field.label}作为明细列`"/><button type="button" :title="field.description" :aria-pressed="field.key===selectedFieldKey" @click="selectField(field)"><span>{{field.label}}</span></button></div></details><a-empty v-if="!visibleFields.length&&!busy" description="没有匹配字段" :image-style="{height:'40px'}"/></div>
     <div class="field-actions"><a-button type="primary" block :disabled="!selectedField||busy" @mousedown.prevent @click="insertSelectedField">插入选中字段</a-button><a-button block :disabled="!detailFields.length||busy" @mousedown.prevent @click="insertDetails">插入{{printType==='LIST'?'记录列表':'明细表'}}（{{detailFields.length}} 列）</a-button><a-button block @mousedown.prevent @click="repeatCurrentRow">将当前表格行设为循环行</a-button></div>
     <p class="hint">选择字段后点击「插入选中字段」，插入到文档光标处。勾选明细列后插入明细表。</p>
    </aside>
    <section class="editor-stage" aria-label="Canvas 打印模板编辑器">
     <div class="editor-toolbar" role="toolbar" aria-label="文档排版工具">
      <a-button size="small" @mousedown.prevent @click="command('executeUndo')">撤销</a-button><a-button size="small" @mousedown.prevent @click="command('executeRedo')">重做</a-button><span class="tool-divider"/>
      <a-select aria-label="字体" default-value="Microsoft YaHei" :options="[{value:'Microsoft YaHei',label:'微软雅黑'},{value:'SimSun',label:'宋体'},{value:'Arial',label:'Arial'}]" style="width:100px" @change="setFont($event as string)"/>
      <a-input-number v-model:value="fontSize" aria-label="字号" :min="8" :max="48" style="width:60px" @change="setSize(Number($event))"/>
      <a-button size="small" aria-label="加粗" @mousedown.prevent @click="command('executeBold')"><b>B</b></a-button><a-button size="small" aria-label="斜体" @mousedown.prevent @click="command('executeItalic')"><i>I</i></a-button><a-button size="small" aria-label="下划线" @mousedown.prevent @click="command('executeUnderline')"><u>U</u></a-button>
      <a-button size="small" @mousedown.prevent @click="align(RowFlex.LEFT)">左对齐</a-button><a-button size="small" @mousedown.prevent @click="align(RowFlex.CENTER)">居中</a-button><a-button size="small" @mousedown.prevent @click="align(RowFlex.RIGHT)">右对齐</a-button><a-button size="small" @mousedown.prevent @click="insertPlainTable">表格</a-button><a-button size="small" @mousedown.prevent @click="command('executePageBreak')">分页</a-button>
     </div>
     <div class="canvas-scroll"><div ref="host" class="canvas-host" @mousedown="rememberTable"/></div>
     <div class="editor-status"><span>A4 竖向 · {{boundCount}} 个字段绑定 · {{dirty?'有未保存修改':saved?`第 ${saved.templateRevision} 版已保存`:'编辑中'}}</span><div><span>缩放</span><a-select :value="autoFit?0:zoom" :options="[{value:0,label:`适应宽度（${zoom}%）`},...[60,75,90,100,125].map(value=>({value,label:value+'%'}))]" style="width:140px" @change="resize(Number($event))"/></div></div>
    </section>
    <aside class="designer-side binding-panel"><h2>绑定属性</h2>
     <template v-if="inspectedField"><dl><dt>字段名称</dt><dd>{{inspectedField.label}}</dd><dt>来源</dt><dd>{{currentSource?.module}} · {{currentSource?.label}} · {{inspectedGroup}}</dd><dt>数据类型</dt><dd>{{inspectedType}}</dd><dt>单位</dt><dd>{{inspectedField.unitField?`同一行「${fields.find(field=>field.key===inspectedField?.unitField)?.label??inspectedField.unitField}」`:'此字段不涉及单位'}}</dd><dt>显示格式</dt><dd>原样显示</dd></dl><label v-if="activeBinding" class="panel-pair"><span>更换字段</span><a-select :value="activeBinding.fieldKey" :options="fields.filter(f=>f.key!=='items'&&f.repeated===activeBinding?.repeated).map(f=>({value:f.key,label:f.label}))" @change="replaceField($event as string)"/></label><p class="hint">{{activeBinding?'字段绑定随模板版本保存。':'当前字段尚未插入，点击「插入选中字段」绑定到文档。'}}</p></template>
     <p v-else class="hint">选择左侧字段或点击文档中的蓝色字段，查看绑定属性。</p>
     <div class="binding-info"><h3>绑定信息</h3><button v-for="group in groups" :key="group" type="button" :class="{active:group===inspectedGroup}" @click="dataset=group">{{group}}<span>›</span></button></div>
     <div class="verification"><h3>模板检查</h3><p v-if="missing.length" class="warning">待插入：{{missing.map(key=>fields.find(f=>f.key===key)?.label??(key==='items'?'记录列表':key)).join('、')}}</p><p v-else class="success">必需标识已绑定</p><a-button block :disabled="busy||!!missing.length" @click="verifyPdf">验证并预览 PDF</a-button><p class="hint">使用后端示例数据生成真实 PDF；验证通过后在模板列表发布。</p></div>
    </aside>
   </div>
  </a-spin>
  <a-modal v-model:open="previewOpen" title="填充预览 · 后端示例数据" width="min(1000px, 94vw)" :footer="null" @after-close="closeFill"><p class="hint">用于检查字段绑定和明细扩展；最终排版以「验证并预览 PDF」结果为准。</p><div class="fill-preview"><div ref="previewHost"/></div></a-modal>
  <a-modal v-model:open="pdfOpen" title="模板 PDF 预览 · 后端示例数据" width="90vw" :footer="null"><PdfPreview :blob="pdfBlob" name="打印模板预览.pdf"/></a-modal>
 </main>
</template>

<style scoped>
.canvas-designer{color:#23364d}.designer-heading{display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap;margin-bottom:16px}.designer-heading h1{font-size:22px;font-weight:600;margin:4px 0 0}.back{padding-left:0}.file-input{display:none}.notice{margin-bottom:16px}.meta-section{background:white;border:1px solid #dce5ed;border-radius:8px;padding:16px;display:grid;grid-template-columns:1fr 1fr;gap:12px 24px;margin-bottom:16px}.inline-pair,.panel-pair{display:flex;align-items:center;gap:8px;flex-wrap:nowrap;min-width:0}.inline-pair>span:first-child{flex:0 0 110px;font-size:13px}.inline-pair>.ant-input,.inline-pair>.ant-select{flex:1;min-width:0}.inline-pair i{color:#d4380d;font-style:normal}.save-state{font-size:13px;color:#66778b;overflow-wrap:anywhere}.designer-workspace{display:grid;grid-template-columns:224px minmax(0,1fr) 220px;gap:16px;align-items:start}.designer-side{background:white;border:1px solid #dce5ed;border-radius:8px;padding:16px;min-width:0}.designer-side h2{font-size:16px;font-weight:600;margin:0 0 16px}.panel-pair{margin:0 0 12px}.panel-pair>label,.panel-pair>span{font-size:13px;flex:0 0 56px}.panel-pair>.ant-input,.panel-pair>.ant-input-affix-wrapper,.panel-pair>.ant-select{flex:1;min-width:0;width:auto}.field-list{height:390px;overflow:auto;margin-top:12px}.field-row{display:flex;align-items:center;gap:8px;border-bottom:1px solid #f0f2f5}.field-row>input{flex:0 0 14px}.field-row button{background:white;border:0;text-align:left;cursor:pointer;color:#23364d;padding:10px 4px;flex:1;min-width:0}.field-row button:hover{background:#eaf3ff;color:#1677ff}.field-row span,.field-row small{display:block}.field-row span{font-size:13px}.field-row small{font-size:12px;color:#66778b;margin-top:4px}.field-actions{display:grid;gap:8px;margin:16px 0 8px}.hint{font-size:12px;color:#66778b;line-height:1.6;margin:8px 0 0}.editor-stage{border:1px solid #dce5ed;border-radius:8px;overflow:hidden;background:#edf1f5;min-width:0}.editor-toolbar{display:flex;align-items:center;gap:4px;flex-wrap:wrap;padding:8px;background:white;border-bottom:1px solid #dce5ed}.tool-divider{height:20px;width:1px;background:#dce5ed;margin:0 4px}.canvas-scroll{height:calc(100vh - 330px);min-height:460px;overflow:auto;padding:16px}.canvas-host{width:max-content;margin:0 auto}.editor-status{background:white;padding:8px 12px;display:flex;justify-content:space-between;gap:8px;align-items:center;font-size:12px;color:#66778b}.editor-status>div{display:flex;align-items:center;gap:8px}.editor-status .ant-select{width:76px}.binding-panel dl{margin:0}.binding-panel dt{font-size:12px;color:#66778b;margin-top:16px}.binding-panel dd{font-size:13px;margin:4px 0 12px;overflow-wrap:anywhere}.verification{margin-top:24px;border-top:1px solid #dce5ed;padding-top:16px}.verification h3{font-size:15px;margin:0 0 8px}.warning{font-size:12px;color:#ad6800}.success{font-size:12px;color:#389e0d}.fill-preview{max-height:70vh;overflow:auto;background:#edf1f5;padding:16px}.fill-preview>div{width:max-content;margin:auto}
.meta-section{grid-template-columns:repeat(4,minmax(0,1fr))}.inline-pair>span:first-child{flex-basis:64px;white-space:nowrap}.meta-value{font-size:13px;overflow-wrap:anywhere}.panel-pair>label,.panel-pair>span{white-space:nowrap}.field-list{height:clamp(120px,calc(100vh - 740px),280px)}.field-list details{margin-bottom:12px}.field-list summary{cursor:pointer;font-weight:600;font-size:13px;padding:8px 0;color:#23364d}.field-row{border:0;border-radius:4px;margin-left:8px}.field-row button{padding:7px 8px;background:transparent}.field-row.selected{background:#eaf3ff}.field-row.selected button{color:#1677ff}.binding-panel dl{display:grid;grid-template-columns:56px minmax(0,1fr);gap:12px 8px}.binding-panel dt,.binding-panel dd{margin:0;font-size:12px;line-height:1.6}.binding-info{margin-top:24px;border-top:1px solid #dce5ed;padding-top:16px}.binding-info h3{font-size:15px;margin:0 0 12px}.binding-info button{display:flex;align-items:center;justify-content:space-between;width:100%;background:#fff;border:1px solid #dce5ed;border-radius:4px;text-align:left;padding:8px 12px;margin-bottom:8px;font-size:13px;color:#23364d;cursor:pointer}.binding-info button.active{background:#eaf3ff;color:#1677ff;border-color:#91caff}
@media(max-width:1200px){.meta-section{grid-template-columns:1fr 1fr}.designer-workspace{grid-template-columns:208px minmax(0,1fr)}.binding-panel{grid-column:1/-1}.binding-panel dl{grid-template-columns:120px 1fr}.canvas-scroll{min-height:440px}.field-list{height:220px}}
@media(max-width:900px){.meta-section{grid-template-columns:1fr}.designer-workspace{grid-template-columns:minmax(0,1fr)}.binding-panel{grid-column:auto}.field-list{height:180px}.canvas-scroll{height:520px}.inline-pair>span:first-child{flex-basis:100px}.editor-status{flex-wrap:wrap}.designer-heading :deep(.ant-space){gap:8px!important}}
</style>
