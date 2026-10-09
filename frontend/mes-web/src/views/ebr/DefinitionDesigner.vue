<script setup lang="ts">
import {computed,nextTick,onBeforeUnmount,onMounted,ref,watch} from 'vue'
import Editor,{ControlType,EditorMode,ElementType,RowFlex,type IEditorResult,type IElement} from '@hufe921/canvas-editor'
import ProcessLookup from '../process/ProcessLookup.vue'
import {fieldLabels,newField} from './model'
import type {EbrDefinition,EbrField,EbrForm,EbrOperationChoice,EbrCanvasDocument} from './types'

const props=defineProps<{definition:EbrDefinition;editable:boolean;operationChoices:EbrOperationChoice[];savedDefinition:EbrDefinition;initialFormCode?:string}>()

const host=ref<HTMLDivElement|null>(null)
const selectedFormIndex=ref(0)
const selectedFieldCode=ref('')
const rightTab=ref('field')
const viewMode=ref<'design'|'fill'>('design')
const fontSize=ref(16)
let editor:Editor|null=null

const forms=computed(()=>props.definition.forms)
const focusedFormMode=computed(()=>!!props.initialFormCode)
const selectedForm=computed(()=>forms.value[selectedFormIndex.value]??null)
function selectInitialForm(){if(!props.initialFormCode)return;const index=forms.value.findIndex(f=>f.formCode===props.initialFormCode);if(index>=0)selectedFormIndex.value=index}
const selectedField=computed(()=>selectedForm.value?.fields.find(f=>f.fieldCode===selectedFieldCode.value)??null)
const allGroups=computed(()=>props.definition.sections.flatMap(s=>s.groups))
const savedForm=computed(()=>selectedForm.value?props.savedDefinition.forms.find(f=>f.formCode===selectedForm.value!.formCode):null)
const savedField=computed(()=>selectedField.value?props.savedDefinition.forms.flatMap(f=>f.fields).some(f=>f.fieldCode===selectedField.value!.fieldCode):false)

const businessFields=[
 {code:'BATCH_NO',label:'批号',type:'TEXT' as const,source:'SYSTEM' as const},
 {code:'PRODUCT_NAME',label:'产品名称',type:'TEXT' as const,source:'SYSTEM' as const},
 {code:'SPECIFICATION',label:'规格',type:'TEXT' as const,source:'SYSTEM' as const},
 {code:'MATERIAL_LOT',label:'物料批次',type:'MATERIAL_LOT' as const,source:'SYSTEM' as const},
 {code:'EQUIPMENT',label:'设备',type:'EQUIPMENT' as const,source:'SYSTEM' as const},
 {code:'OPERATOR',label:'操作人',type:'PERSON' as const,source:'SYSTEM' as const},
 {code:'SIGNATURE',label:'电子签名',type:'SIGNATURE_PLACEHOLDER' as const,source:'MANUAL' as const}
]
const basicFields=[
 {label:'文本输入',type:'TEXT' as const},{label:'数值 + 单位',type:'NUMBER' as const},
 {label:'日期',type:'DATE' as const},{label:'下拉选择',type:'ENUM' as const},
 {label:'复选',type:'BOOLEAN' as const},{label:'公式计算',type:'CALCULATED' as const}
]

function uniqueCode(base:string){
 const used=new Set(props.definition.forms.flatMap(f=>f.fields.map(x=>x.fieldCode)))
 if(!used.has(base))return base
 let n=2
 while(used.has(base+'_'+n))n++
 return base+'_'+n
}
function newForm(){
 if(!props.editable)return
 const used=new Set(forms.value.map(f=>f.formCode));let n=1
 while(used.has('FORM'+n))n++
 props.definition.forms.push({formCode:'FORM'+n,formName:'新工序表单',operationDefId:null,schemaVersion:'1.0',sequenceNo:forms.value.length+1,fields:[],canvasDocument:null})
 selectedFormIndex.value=forms.value.length-1
 selectedFieldCode.value=''
 rightTab.value='form'
}
function removeNewForm(){
 const f=selectedForm.value
 if(!f||savedForm.value||!props.editable)return
 capture()
 props.definition.forms.splice(selectedFormIndex.value,1)
 selectedFormIndex.value=Math.max(0,selectedFormIndex.value-1)
 selectedFieldCode.value=''
}
function addGroup(){
 if(!props.editable)return
 let section=props.definition.sections[0]
 if(!section){
   section={sectionCode:'LAYOUT',title:'表单布局',sequenceNo:1,repeatMode:'NONE',visibilityRuleCode:null,pageBreakFlag:false,groups:[]}
   props.definition.sections.push(section)
 }
 const code=uniqueGroupCode()
 section.groups.push({groupCode:code,title:'字段分组',sequenceNo:section.groups.length+1,layoutColumns:2,repeatMode:'NONE',minOccurs:null,maxOccurs:null})
}
function uniqueGroupCode(){const used=new Set(allGroups.value.map(g=>g.groupCode));let n=1;while(used.has('GRP'+n))n++;return 'GRP'+n}

function controlType(field:EbrField){
 if(field.fieldType==='NUMBER'||field.fieldType==='CALCULATED'||field.fieldType==='INSTRUMENT_VALUE')return ControlType.NUMBER
 if(field.fieldType==='DATE'||field.fieldType==='DATETIME'||field.fieldType==='TIME')return ControlType.DATE
 if(field.fieldType==='ENUM'||field.fieldType==='MULTI_ENUM')return ControlType.SELECT
 if(field.fieldType==='BOOLEAN')return ControlType.CHECKBOX
 return ControlType.TEXT
}
function controlElement(field:EbrField):IElement{
 const values=field.options.map(o=>({code:o.optionCode,value:o.optionLabel}))
 return {
  type:ElementType.CONTROL,value:'',
  control:{
   type:controlType(field),value:null,conceptId:field.fieldCode,placeholder:field.label,
   required:field.requiredFlag,disabled:field.readonlyFlag,
   prefix:'',postfix:field.unitId?' ['+field.unitId+']':'',
   extension:{ebrFieldCode:field.fieldCode,ebrFieldType:field.fieldType,sourceType:field.sourceType},
   valueSets:values.length?values:undefined
  }
 }
}
function seed(form:EbrForm):IEditorResult{
 const main:IElement[]=[
  {value:form.formName,bold:true,size:22,rowFlex:RowFlex.CENTER},
  {value:'\n'},
  {value:'生产工序表单记录',size:12,color:'#7b8798',rowFlex:RowFlex.CENTER},
  {value:'\n\n'}
 ]
 if(form.fields.length){
  form.fields.forEach(field=>{main.push({value:field.label+'：',bold:true},{...controlElement(field)},{value:'\n'})})
 }else main.push({value:'请从左侧字段库插入字段，或使用工具栏插入表格。'})
 return {version:'1.0.4',data:{main},options:{}}
}
function documentFor(form:EbrForm):IEditorResult{
 const value=form.canvasDocument as unknown as IEditorResult|null
 return value?.data?.main?JSON.parse(JSON.stringify(value)):seed(form)
}
function capture(){
 const form=selectedForm.value
 if(!editor||!form||!props.editable)return
 form.canvasDocument=JSON.parse(JSON.stringify(editor.command.getValue())) as unknown as EbrCanvasDocument
}
async function mountEditor(){
 editor?.destroy();editor=null
 await nextTick()
 const form=selectedForm.value
 if(!host.value||!form)return
 editor=new Editor(host.value,documentFor(form).data,{
  mode:props.editable?(viewMode.value==='design'?EditorMode.DESIGN:EditorMode.FORM):EditorMode.READONLY,
  locale:'zhCN',pageMode:'paging' as never,defaultFont:'Microsoft YaHei',defaultSize:16,
  margins:[70,70,70,70],historyMaxRecordCount:100
 })
 editor.listener.contentChange=()=>capture()
 editor.listener.controlChange=payload=>{
   const code=payload.control.conceptId
   if(code&&form.fields.some(f=>f.fieldCode===code)){selectedFieldCode.value=code;rightTab.value='field'}
 }
 editor.command.executeMode(props.editable?(viewMode.value==='design'?EditorMode.DESIGN:EditorMode.FORM):EditorMode.READONLY)
}
function selectForm(index:number){
 capture();selectedFormIndex.value=index;selectedFieldCode.value='';rightTab.value='form'
}
function insertField(type:EbrField['fieldType'],label?:string,codeBase?:string,source?:EbrField['sourceType']){
 const form=selectedForm.value
 if(!form||!props.editable)return
 const code=uniqueCode(codeBase??'FIELD_'+(props.definition.forms.flatMap(f=>f.fields).length+1))
 const field=newField(code,type)
 field.label=label??fieldLabels[type]??'字段'
 if(source)field.sourceType=source
 if(source==='SYSTEM')field.readonlyFlag=true
 field.sequenceNo=form.fields.length+1
 form.fields.push(field)
 selectedFieldCode.value=field.fieldCode
 rightTab.value='field'
 editor?.command.executeInsertControl(controlElement(field))
 capture()
}
function insertBusiness(item:typeof businessFields[number]){insertField(item.type,item.label,item.code,item.source)}
function updateCanvasControl(){
 const f=selectedField.value
 if(!editor||!f)return
 editor.command.executeSetControlProperties({conceptId:f.fieldCode,properties:{
  type:controlType(f),placeholder:f.label,required:f.requiredFlag,disabled:f.readonlyFlag,
  extension:{ebrFieldCode:f.fieldCode,ebrFieldType:f.fieldType,sourceType:f.sourceType},
  valueSets:f.options.map(o=>({code:o.optionCode,value:o.optionLabel}))
 },isSubmitHistory:true})
 capture()
}
function removeNewField(){
 const form=selectedForm.value,f=selectedField.value
 if(!form||!f||savedField.value||!props.editable)return
 editor?.command.executeRemoveControl({conceptId:f.fieldCode})
 form.fields.splice(form.fields.indexOf(f),1)
 form.fields.forEach((x,i)=>x.sequenceNo=i+1)
 selectedFieldCode.value=''
 capture()
}
function addOption(){
 const f=selectedField.value;if(!f||!props.editable)return
 let n=f.options.length+1
 f.options.push({optionCode:'OPT'+n,optionLabel:'选项'+n,optionValue:'OPT'+n,sequenceNo:n,activeFlag:true})
 updateCanvasControl()
}
function toolbar(action:string){
 if(!editor||!props.editable||viewMode.value!=='design')return
 const c=editor.command
 if(action==='undo')c.executeUndo()
 if(action==='redo')c.executeRedo()
 if(action==='bold')c.executeBold()
 if(action==='italic')c.executeItalic()
 if(action==='table')c.executeInsertTable(3,4)
 if(action==='row')c.executeInsertTableBottomRow()
 if(action==='col')c.executeInsertTableRightCol()
 if(action==='merge')c.executeMergeTableCell()
 if(action==='split')c.executeCancelMergeTableCell()
 if(action==='separator')c.executeSeparator([4,2],{lineWidth:1,color:'#b6c0cf'})
 if(action==='page')c.executePageBreak()
 capture()
}
function changeSize(){editor?.command.executeSize(fontSize.value);capture()}
function toggleMode(mode:'design'|'fill'){capture();viewMode.value=mode;editor?.command.executeMode(props.editable?(mode==='design'?EditorMode.DESIGN:EditorMode.FORM):EditorMode.READONLY)}
function addRule(){
 if(!props.editable)return
 let n=props.definition.rules.length+1
 props.definition.rules.push({ruleCode:'RULE'+n,formCode:selectedForm.value?.formCode??null,fieldCode:selectedField.value?.fieldCode??null,ruleType:'VALIDATION',triggerPoint:'ON_SUBMIT',expression:'true',severity:'BLOCK',errorCode:null,messageTemplate:null,deviationTrigger:false,activeFlag:true})
}
function addSignature(){
 if(!props.editable||!selectedForm.value)return
 props.definition.signatureRules.push({objectScope:'FORM',objectCode:selectedForm.value.formCode,meaning:'VERIFY',requiredRole:'',reauthRequired:true,sequenceNo:props.definition.signatureRules.length+1,invalidateOnChange:true})
}
function addReview(){
 if(!props.editable||!selectedForm.value)return
 props.definition.reviewRules.push({objectScope:'FORM',objectCode:selectedForm.value.formCode,reviewType:'VERIFY',requiredRole:'',independentUserRequired:true,sequenceNo:props.definition.reviewRules.length+1})
}

watch(()=>selectedFormIndex.value,()=>void mountEditor())
watch(()=>props.editable,()=>void mountEditor())
watch(()=>props.initialFormCode,()=>{capture();selectInitialForm();void mountEditor()})
onMounted(()=>{selectInitialForm();void mountEditor()})
onBeforeUnmount(()=>{capture();editor?.destroy()})
</script>

<template>
  <section data-ui-template="T2" class="canvas-ebr-designer">
    <header class="designer-topbar">
      <div>
        <strong>表单模板设计器</strong>
        <span v-if="selectedForm">{{selectedForm.formName}} · {{selectedForm.formCode}}</span>
      </div>
      <div class="mode-switch">
        <button type="button" :class="{active:viewMode==='design'}" :disabled="!editable" @click="toggleMode('design')">设计模式</button>
        <button type="button" :class="{active:viewMode==='fill'}" @click="toggleMode('fill')">填写预览</button>
      </div>
    </header>

    <div class="designer-toolbar">
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('undo')">↶</a-button>
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('redo')">↷</a-button>
      <span class="tool-sep"></span>
      <a-select v-model:value="fontSize" size="small" :disabled="!editable||viewMode!=='design'" style="width:78px" :options="[12,14,16,18,20,22,24,28].map(value=>({value,label:value+'px'}))" @change="changeSize"/>
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('bold')"><b>B</b></a-button>
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('italic')"><i>I</i></a-button>
      <span class="tool-sep"></span>
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('table')">插入表格</a-button>
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('row')">＋行</a-button>
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('col')">＋列</a-button>
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('merge')">合并</a-button>
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('split')">拆分</a-button>
      <span class="tool-sep"></span>
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('separator')">分隔线</a-button>
      <a-button size="small" :disabled="!editable||viewMode!=='design'" @click="toolbar('page')">分页</a-button>
    </div>

    <div class="designer-grid">
      <aside class="palette">
        <div class="panel-title">{{focusedFormMode?'组件库':'表单列表'}} <a-button v-if="editable&&!focusedFormMode" type="link" size="small" @click="newForm">＋新增</a-button></div>
        <template v-if="!focusedFormMode">
          <button v-for="(form,index) in forms" :key="form.formCode" type="button" class="form-item" :class="{active:index===selectedFormIndex}" @click="selectForm(index)">
            <strong>{{form.formName}}</strong><small>{{form.formCode}} · {{operationChoices.find(o=>o.id===form.operationDefId)?.operationName||'未关联工序'}}</small>
          </button>
          <a-empty v-if="!forms.length" :image="false" description="请先新增工序表单"/>
        </template>

        <template v-if="selectedForm">
          <div class="panel-title field-title">基础字段</div>
          <div class="palette-grid">
            <button v-for="item in basicFields" :key="item.type" type="button" :disabled="!editable" @click="insertField(item.type,item.label)">{{item.label}}</button>
          </div>
          <div class="panel-title field-title">业务字段</div>
          <div class="field-list">
            <button v-for="item in businessFields" :key="item.code" type="button" :disabled="!editable" @click="insertBusiness(item)">
              <span>{{item.label}}</span><small>{{item.source==='SYSTEM'?'自动取值':'受控填写'}}</small>
            </button>
          </div>
          <div class="panel-title field-title">布局</div>
          <div class="palette-grid">
            <button type="button" :disabled="!editable" @click="toolbar('table')">表格</button>
            <button type="button" :disabled="!editable" @click="toolbar('separator')">分隔线</button>
            <button type="button" :disabled="!editable" @click="addGroup">字段分组</button>
          </div>
        </template>
      </aside>

      <main class="canvas-stage">
        <div v-if="selectedForm" ref="host" class="canvas-host"></div>
        <a-empty v-else description="新增表单后即可使用 Canvas Editor 设计"/>
      </main>

      <aside class="inspector">
        <a-tabs v-model:active-key="rightTab" size="small">
          <a-tab-pane key="field" tab="字段属性">
            <div v-if="selectedField" class="property-form">
              <label><span>字段名称</span><a-input v-model:value="selectedField.label" :disabled="!editable" @change="updateCanvasControl"/></label>
              <label><span>字段编码</span><a-input v-model:value="selectedField.fieldCode" :disabled="!editable||savedField"/></label>
              <label><span>字段类型</span><a-select v-model:value="selectedField.fieldType" :disabled="!editable" :options="Object.entries(fieldLabels).map(([value,label])=>({value,label}))" @change="updateCanvasControl"/></label>
              <label><span>数据来源</span><a-select v-model:value="selectedField.sourceType" :disabled="!editable" :options="[{value:'MANUAL',label:'人工填写'},{value:'SYSTEM',label:'系统自动'},{value:'BARCODE',label:'扫码'},{value:'INSTRUMENT',label:'仪器采集'},{value:'DERIVED',label:'计算派生'}]" @change="updateCanvasControl"/></label>
              <label><span>数据类型</span><a-input v-model:value="selectedField.dataType" :disabled="!editable"/></label>
              <label><span>单位</span><ProcessLookup v-model="selectedField.unitId" resource="units" label="" allow-clear :disabled="!editable"/></label>
              <label><span>必填</span><a-switch v-model:checked="selectedField.requiredFlag" :disabled="!editable" @change="updateCanvasControl"/></label>
              <label><span>只读</span><a-switch v-model:checked="selectedField.readonlyFlag" :disabled="!editable" @change="updateCanvasControl"/></label>
              <label><span>字段分组</span><a-select v-model:value="selectedField.groupCode" allow-clear :disabled="!editable" :options="allGroups.map(g=>({value:g.groupCode,label:g.title||g.groupCode}))"/></label>
              <label><span>占位提示</span><a-input v-model:value="selectedField.placeholder" :disabled="!editable" @change="updateCanvasControl"/></label>
              <label><span>校验 JSON</span><a-textarea v-model:value="selectedField.validationJson" :disabled="!editable" :rows="3" placeholder='如 {"min":18,"max":26}'/></label>
              <template v-if="['ENUM','MULTI_ENUM'].includes(selectedField.fieldType)">
                <div class="option-box">
                  <div v-for="(option,index) in selectedField.options" :key="option.optionCode" class="option-row">
                    <a-input v-model:value="option.optionLabel" :disabled="!editable" @change="updateCanvasControl"/>
                    <a-button v-if="editable" danger type="text" @click="selectedField.options.splice(index,1);updateCanvasControl()">×</a-button>
                  </div>
                  <a-button v-if="editable" block @click="addOption">＋ 添加选项</a-button>
                </div>
              </template>
              <a-button v-if="editable&&!savedField" danger block @click="removeNewField">删除未保存字段</a-button>
            </div>
            <div v-else class="empty-hint">点击 Canvas 中的动态字段，或从左侧字段库插入字段。</div>
          </a-tab-pane>

          <a-tab-pane key="form" tab="表单属性">
            <div v-if="selectedForm" class="property-form">
              <label><span>表单名称</span><a-input v-model:value="selectedForm.formName" :disabled="!editable"/></label>
              <label><span>表单编码</span><a-input v-model:value="selectedForm.formCode" :disabled="!editable||!!savedForm"/></label>
              <label><span>关联工序</span><a-select v-model:value="selectedForm.operationDefId" :disabled="!editable" :options="operationChoices.map(o=>({value:o.id,label:o.operationName+'（'+o.operationCode+'）'}))"/></label>
              <label><span>Schema</span><a-input v-model:value="selectedForm.schemaVersion" :disabled="!editable"/></label>
              <a-button v-if="editable&&!savedForm" danger block @click="removeNewForm">删除未保存表单</a-button>
            </div>
          </a-tab-pane>

          <a-tab-pane key="rules" tab="规则与签名">
            <div class="rules-panel">
              <div class="rules-head"><strong>校验/计算规则</strong><a-button v-if="editable" size="small" @click="addRule">＋</a-button></div>
              <div v-for="rule in definition.rules" :key="rule.ruleCode" class="rule-card">
                <div><b>{{rule.ruleCode}}</b><span>{{rule.ruleType}}</span></div>
                <a-input v-model:value="rule.expression" :disabled="!editable" size="small"/>
              </div>
              <div class="rules-head"><strong>电子签名规则</strong><a-button v-if="editable" size="small" @click="addSignature">＋</a-button></div>
              <div v-for="(rule,index) in definition.signatureRules" :key="index" class="rule-card">
                <div><b>{{rule.objectCode}}</b><span>{{rule.meaning}}</span></div>
                <a-input v-model:value="rule.requiredRole" :disabled="!editable" size="small" placeholder="角色编码"/>
              </div>
              <div class="rules-head"><strong>复核规则</strong><a-button v-if="editable" size="small" @click="addReview">＋</a-button></div>
              <div v-for="(rule,index) in definition.reviewRules" :key="index" class="rule-card">
                <div><b>{{rule.objectCode}}</b><span>{{rule.reviewType}}</span></div>
                <a-input v-model:value="rule.requiredRole" :disabled="!editable" size="small" placeholder="角色编码"/>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.canvas-ebr-designer{border:1px solid var(--mes-ui-border);border-radius:8px;background:#fff;overflow:hidden}
.designer-topbar{height:52px;display:flex;align-items:center;justify-content:space-between;padding:0 14px;border-bottom:1px solid var(--mes-ui-border);background:#fff}
.designer-topbar>div:first-child{display:flex;align-items:baseline;gap:10px}.designer-topbar strong{font-size:16px}.designer-topbar span{font-size:12px;color:var(--mes-ui-secondary)}
.mode-switch{display:flex;border:1px solid var(--mes-ui-border);border-radius:6px;overflow:hidden}.mode-switch button{border:0;background:#fff;padding:6px 13px;color:var(--mes-ui-secondary);cursor:pointer}.mode-switch button.active{background:var(--mes-ui-primary);color:#fff}.mode-switch button:disabled{opacity:.45}
.designer-toolbar{min-height:46px;display:flex;align-items:center;gap:6px;padding:7px 12px;border-bottom:1px solid var(--mes-ui-border);background:#fbfcfe;overflow-x:auto}.tool-sep{height:24px;width:1px;background:var(--mes-ui-border);margin:0 3px}
.designer-grid{display:grid;grid-template-columns:220px minmax(600px,1fr) 320px;min-height:720px}
.palette{border-right:1px solid var(--mes-ui-border);padding:12px;background:#fcfdff;overflow:auto}.panel-title{font-size:12px;font-weight:650;color:var(--mes-ui-secondary);display:flex;align-items:center;justify-content:space-between;padding:4px 4px 9px}.field-title{margin-top:14px;border-top:1px solid var(--mes-ui-border);padding-top:13px}
.form-item{width:100%;display:block;text-align:left;border:1px solid transparent;background:transparent;border-radius:6px;padding:8px;margin:2px 0;cursor:pointer;color:var(--mes-ui-text)}.form-item:hover,.form-item.active{background:#eaf2ff;border-color:#cfe0fb}.form-item strong,.form-item small{display:block}.form-item small{margin-top:3px;color:var(--mes-ui-secondary);font-size:11px;white-space:normal}
.palette-grid{display:grid;grid-template-columns:1fr 1fr;gap:7px}.palette-grid button,.field-list button{border:1px solid var(--mes-ui-border);background:#fff;border-radius:6px;min-height:34px;font:inherit;font-size:12px;color:var(--mes-ui-text);cursor:pointer}.palette-grid button:hover,.field-list button:hover{border-color:#91caff;background:#f0f7ff}.palette-grid button:disabled,.field-list button:disabled{opacity:.45;cursor:not-allowed}
.field-list{display:grid;gap:5px}.field-list button{display:flex;align-items:center;justify-content:space-between;padding:7px 9px}.field-list small{color:var(--mes-ui-secondary);font-size:10px}
.canvas-stage{background:#eef2f7;padding:18px 8px;overflow:auto;min-width:0}.canvas-host{min-height:680px}.canvas-host :deep(canvas){max-width:none}
.inspector{border-left:1px solid var(--mes-ui-border);padding:8px 12px;background:#fff;overflow:auto}.property-form{display:flex;flex-direction:column;gap:11px}.property-form>label{display:flex;align-items:center;gap:8px;min-width:0}.property-form>label>span{flex:0 0 92px;color:var(--mes-ui-secondary);font-size:12px;white-space:nowrap}.property-form>label :deep(.ant-input),.property-form>label :deep(.ant-select),.property-form>label :deep(.ant-input-number),.property-form>label :deep(.process-lookup){flex:1;min-width:0}.empty-hint{color:var(--mes-ui-secondary);font-size:12px;line-height:1.8;padding:30px 8px;text-align:center}
.option-box{border:1px solid var(--mes-ui-border);border-radius:6px;padding:8px}.option-row{display:flex;gap:4px;margin-bottom:6px}
.rules-panel{display:grid;gap:8px}.rules-head{display:flex;justify-content:space-between;align-items:center;margin-top:8px;padding-top:8px;border-top:1px solid var(--mes-ui-border)}.rule-card{border:1px solid var(--mes-ui-border);border-radius:6px;padding:8px}.rule-card>div{display:flex;justify-content:space-between;gap:8px;font-size:11px;margin-bottom:5px}.rule-card span{color:var(--mes-ui-secondary)}
@media(max-width:1180px){.designer-grid{grid-template-columns:190px minmax(480px,1fr)}.inspector{grid-column:1/-1;border-left:0;border-top:1px solid var(--mes-ui-border);max-height:none}.property-form{display:grid;grid-template-columns:repeat(2,minmax(0,1fr))}}
@media(max-width:760px){.designer-grid{grid-template-columns:1fr}.palette{border-right:0;border-bottom:1px solid var(--mes-ui-border)}.canvas-stage{padding:8px;min-height:520px}.property-form{grid-template-columns:1fr}.designer-topbar{align-items:flex-start;height:auto;gap:10px;padding:10px;flex-direction:column}.designer-toolbar{position:sticky;top:0;z-index:2}}
</style>
