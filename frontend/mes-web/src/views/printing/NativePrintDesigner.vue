<script setup lang="ts">
import {computed,onMounted,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {message} from 'ant-design-vue'
import {api,errorMessage} from '../../api/http'
import type {PrintField,PrintTemplate} from '../../api/printing'
import {parseWordTableClipboard,type WordTableCell,type WordTableRow} from './wordTableClipboard'

type Kind='TITLE'|'TEXT'|'FIELD'|'TABLE'|'WORD_TABLE'|'DIVIDER'
type Alignment='LEFT'|'CENTER'|'RIGHT'
interface DesignBlock {id:string;type:Kind;fontSize:number;align:Alignment;text?:string;fieldKey?:string;columns?:string[];widths?:number[];rows?:WordTableRow[]}
interface PrintDesign {blocks:DesignBlock[]}
const route=useRoute(),router=useRouter(),busy=ref(false),error=ref(''),fields=ref<PrintField[]>([])
const design=ref<PrintDesign>({blocks:[]}),templateCode=ref(''),templateName=ref(''),reason=ref('')
const selected=ref<string|null>(null),fieldSearch=ref(''),columnPicker=ref(false)
const pasteTarget=ref<HTMLElement|null>(null),pasteWarnings=ref<string[]>([]),activeTableCell=ref<{blockId:string;row:number;cell:number}|null>(null)
const existingId=computed(()=>typeof route.params.id==='string'?route.params.id:null)
const regular=computed(()=>fields.value.filter(f=>!f.repeated&&f.key!=='items'&&(f.label+f.description+f.group).includes(fieldSearch.value)))
const repeated=computed(()=>fields.value.filter(f=>f.repeated))
const selectedBlock=computed(()=>design.value.blocks.find(b=>b.id===selected.value))
const selectedWordCell=computed<WordTableCell|null>(()=>{
 const focus=activeTableCell.value,block=selectedBlock.value
 return focus&&block?.type==='WORD_TABLE'&&focus.blockId===block.id?block.rows?.[focus.row]?.cells[focus.cell]??null:null
})
const mandatory=new Set(['reportNo','modeLabel'])
const fieldInfo=(key?:string)=>fields.value.find(f=>f.key===key)
const fieldLabel=(key?:string)=>fieldInfo(key)?.label??'选择中文字段'
const alignValue=(block:DesignBlock):'left'|'center'|'right'=>({LEFT:'left',CENTER:'center',RIGHT:'right'}[block.align] as 'left'|'center'|'right')
async function initialize(){
 busy.value=true;error.value=''
 try{
  const schema=await api<{definitions:PrintField[]}>({url:'/printing/fields',params:{businessType:'INSPECTION_REPORT'}})
  fields.value=schema.definitions
  if(existingId.value){
   const result=await api<{template:PrintTemplate;design:PrintDesign}>({url:`/printing/templates/${existingId.value}/design`})
   templateCode.value=result.template.templateCode;templateName.value=result.template.templateName
   design.value=result.design
  }else{
   design.value=await api<PrintDesign>({url:'/printing/designer/default',params:{businessType:'INSPECTION_REPORT'}})
  }
  selected.value=design.value.blocks[0]?.id??null
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
onMounted(()=>void initialize())
function newId(){return 'node-'+crypto.randomUUID().replace(/-/g,'').slice(0,18)}
function add(type:Kind,key?:string){
 const current=design.value.blocks.findIndex(b=>b.id===selected.value)
 const field=type==='FIELD'?fields.value.find(f=>f.key===key&&!f.repeated):undefined
 if(type==='FIELD'&&!field)return
 const block:DesignBlock={id:newId(),type,fontSize:type==='TITLE'?20:type==='TABLE'?10:11,align:type==='TITLE'?'CENTER':'LEFT'}
 if(type==='TITLE')block.text='标题'
 if(type==='TEXT')block.text='请输入文字说明'
 if(type==='FIELD')block.fieldKey=field!.key
 if(type==='TABLE')block.columns=repeated.value.slice(0,6).map(f=>f.key)
 design.value.blocks.splice(current+1,0,block);selected.value=block.id
}
/** Word clipboard markup is parsed into an inert allowlisted table before entering reactive state. */
function pasteWordTable(event:ClipboardEvent){
 event.preventDefault()
 error.value='';pasteWarnings.value=[]
 const source=event.clipboardData?.getData('text/html')??''
 try{
  const {block,warnings}=parseWordTableClipboard(source,newId())
  if(design.value.blocks.length>=80)throw new Error('页面组件数量不能超过 80')
  const index=design.value.blocks.findIndex(item=>item.id===selected.value)
  design.value.blocks.splice(index+1,0,block)
  selected.value=block.id
  activeTableCell.value={blockId:block.id,row:0,cell:0}
  pasteWarnings.value=warnings
  if(pasteTarget.value)pasteTarget.value.textContent=''
  message.success('Word 表格已粘贴，可选择单元格绑定中文业务字段')
 }catch(e){error.value=e instanceof Error?e.message:'无法解析 Word 表格'}
}
function setCellField(key:string|undefined){
 const cell=selectedWordCell.value
 if(!cell)return
 if(!key){delete cell.fieldKey;return}
 const info=fields.value.find(f=>f.key===key&&!f.repeated)
 if(!info){error.value='所选中文字段不在当前业务字段字典中';return}
 cell.fieldKey=info.key
}
function updateCellText(next:string){
 const cell=selectedWordCell.value
 if(!cell)return
 cell.text=next
 delete cell.spans // Edited text cannot retain a stale copied style/run-to-text mapping.
}
function move(block:DesignBlock,delta:number){
 const index=design.value.blocks.findIndex(b=>b.id===block.id),next=index+delta
 if(next<0||next>=design.value.blocks.length)return
 design.value.blocks.splice(index,1);design.value.blocks.splice(next,0,block)
}
function remove(block:DesignBlock){
 if(block.type==='FIELD'&&mandatory.has(block.fieldKey??'')){error.value='报告编号和正式/草稿标记属于必需字段，不能移除';return}
 const index=design.value.blocks.findIndex(b=>b.id===block.id)
 if(index<0)return
 design.value.blocks.splice(index,1)
 selected.value=design.value.blocks[Math.min(index,design.value.blocks.length-1)]?.id??null
}
function toggleColumn(block:DesignBlock,key:string,checked:boolean){
 const value=new Set(block.columns??[])
 if(checked)value.add(key);else value.delete(key)
 block.columns=[...value]
}
function dragStart(event:DragEvent,id:string){event.dataTransfer?.setData('text/plain',id)}
function drop(event:DragEvent,targetId:string){
 const origin=event.dataTransfer?.getData('text/plain')
 if(!origin||origin===targetId)return
 const sourceIndex=design.value.blocks.findIndex(b=>b.id===origin)
 const targetIndex=design.value.blocks.findIndex(b=>b.id===targetId)
 if(sourceIndex<0||targetIndex<0)return
 const [block]=design.value.blocks.splice(sourceIndex,1)
 if(!block)return
 design.value.blocks.splice(design.value.blocks.findIndex(b=>b.id===targetId),0,block)
 selected.value=origin
}
async function save(){
 error.value=''
 if(!/^[A-Za-z0-9_-]{1,60}$/.test(templateCode.value)||!templateName.value.trim()){
  error.value='请填写模板编码（英文字母、数字、下划线或横线）、名称和保存原因';return
 }
 busy.value=true
 try{
  const result=await api<PrintTemplate>({url:'/printing/designer',method:'POST',data:{
   templateCode:templateCode.value,templateName:templateName.value,businessType:'INSPECTION_REPORT',
   reason:'打印配置维护（系统记录）',design:design.value
  }})
  message.success(`已保存第 ${result.templateRevision} 版草稿，请验证 PDF 后发布`)
  await router.push('/admin/print-templates')
 }catch(e){error.value=errorMessage(e)}finally{busy.value=false}
}
</script>

<template>
 <main class="admin-page master-page native-designer" data-ui-template="T2">
  <header class="admin-page-header designer-heading">
   <div>
    <a-button type="link" @click="router.push('/admin/print-templates')">← 返回模板管理</a-button>
    <h1>可视化打印模板设计</h1>
    <p>点击中文字段插入到页面，拖动或用箭头调整顺序。保存时自动生成真正的 Word 模板。</p>
   </div>
   <a-space wrap>
    <a-button :disabled="busy" @click="initialize">重新读取</a-button>
    <a-button type="primary" :loading="busy" :disabled="!design.blocks.length" @click="save">保存为新草稿版本</a-button>
   </a-space>
  </header>
  <a-alert v-if="error" type="error" show-icon :message="error" class="designer-alert"/>
  <a-card size="small" class="designer-meta">
   <a-row :gutter="[16,12]">
    <a-col :xs="24" :md="7"><label>模板编码 <a-input v-model:value="templateCode" maxlength="60" :disabled="!!existingId" placeholder="例如 INSPECT_REPORT"/></label></a-col>
    <a-col :xs="24" :md="8"><label>模板名称 <a-input v-model:value="templateName" maxlength="120" placeholder="例如 原辅料检验报告"/></label></a-col>
    
   </a-row>
  </a-card>
  <a-spin :spinning="busy">
   <div class="designer-workspace">
    <aside class="designer-side toolbox">
     <h2>插入组件</h2>
     <p class="hint">选中页面中的某行后，点击这里即可插入新组件。</p>
     <div class="paste-card">
      <strong>Word 表格复制粘贴</strong>
      <p class="hint">在 Word 中选中表格复制，再点击下面区域按 Ctrl+V。保留支持的合并单元格、列宽、边框、字号及文字格式。</p>
      <div ref="pasteTarget" class="word-paste-target" contenteditable="true" role="textbox" aria-label="在此粘贴 Word 表格" tabindex="0" spellcheck="false" @paste="pasteWordTable"/>
      <a-button size="small" block @click="pasteTarget?.focus()">定位粘贴位置</a-button>
      <a-alert v-if="pasteWarnings.length" type="warning" :message="pasteWarnings.join('；')" show-icon style="margin-top:8px"/>
      <p class="hint">复杂 Word 原文件请使用模板列表的「上传已有 DOCX」，保留 OOXML，不强制转为简化表格。</p>
     </div>
     <div class="quick-add">
      <a-button block @click="add('TITLE')">标题文字</a-button>
      <a-button block @click="add('TEXT')">说明文字</a-button>
      <a-button block @click="add('DIVIDER')">分隔线</a-button>
      <a-button block @click="add('TABLE')">检验项目循环表</a-button>
     </div>
     <h3>中文字段</h3>
     <a-input v-model:value="fieldSearch" allow-clear placeholder="按中文名称搜索字段"/>
     <div class="field-picker">
      <button v-for="field in regular" :key="field.key" type="button" class="field-choice" @click="add('FIELD',field.key)">
       <span>{{field.label}}</span><small>{{field.group}} · {{field.description}}</small>
      </button>
     </div>
    </aside>
    <section class="page-stage" aria-label="打印模板画布">
     <div class="page-info"><span>A4 竖版 · 页面内容预览</span><span>拖动行可排序</span></div>
     <div class="paper" role="list" aria-label="打印页面组件">
      <div v-for="(block,index) in design.blocks" :key="block.id"
       class="paper-block" :class="{selected:block.id===selected}" role="listitem"
       :aria-label="`第${index+1}项 ${block.type==='FIELD'?fieldLabel(block.fieldKey):block.type}`"
       draggable="true" @dragstart="dragStart($event,block.id)" @dragover.prevent @drop.prevent="drop($event,block.id)"
       @click="selected=block.id">
       <div class="block-rail"><span class="block-type">{{block.type==='TITLE'?'标题':block.type==='TEXT'?'文字':block.type==='FIELD'?'字段':block.type==='TABLE'?'明细表':'分隔线'}}</span>
        <div class="block-controls">
         <a-button size="small" type="text" :disabled="index===0" @click.stop="move(block,-1)">↑</a-button>
         <a-button size="small" type="text" :disabled="index===design.blocks.length-1" @click.stop="move(block,1)">↓</a-button>
        </div>
       </div>
       <p v-if="block.type==='TITLE'||block.type==='TEXT'" class="paper-text" :style="{fontSize:block.fontSize+'px',fontWeight:block.type==='TITLE'?700:400,textAlign:alignValue(block)}">{{block.text}}</p>
       <div v-else-if="block.type==='FIELD'" class="paper-field" :style="{fontSize:block.fontSize+'px',textAlign:alignValue(block)}">
        <span>{{fieldLabel(block.fieldKey)}}：</span><span class="field-chip">{{fieldInfo(block.fieldKey)?.example||'自动取值'}}</span>
       </div>
       <div v-else-if="block.type==='TABLE'" class="paper-table-wrapper">
        <table class="paper-table"><thead><tr><th v-for="key in block.columns" :key="key">{{fieldLabel(key)}}</th></tr></thead><tbody>
         <tr v-for="n in 2" :key="n"><td v-for="key in block.columns" :key="key">{{fieldInfo(key)?.example||'—'}}</td></tr>
        </tbody></table>
        <small>打印时按实际检验项目自动扩展，不限制行数</small>
       </div>
       <div v-else-if="block.type==='WORD_TABLE'" class="paper-table-wrapper word-table-wrapper">
        <table class="paper-table imported-word-table">
         <colgroup><col v-for="(width,col) in block.widths" :key="col" :style="{width:width+'%'}"/></colgroup>
         <tbody>
          <tr v-for="(wordRow,r) in block.rows" :key="r" :style="wordRow.heightPt?{height:wordRow.heightPt*1.333+'px'}:{}">
           <td v-for="(cell,c) in wordRow.cells" :key="c" :colspan="cell.colspan" :rowspan="cell.rowspan"
            :class="{'chosen-word-cell':activeTableCell?.blockId===block.id&&activeTableCell?.row===r&&activeTableCell?.cell===c}"
            :style="{backgroundColor:cell.background,color:cell.color,fontSize:(cell.fontSize??block.fontSize)+'px',textAlign:cell.align?.toLowerCase() as 'left'|'center'|'right'|undefined,fontWeight:cell.bold?700:400,borderColor:cell.borderColor,borderWidth:(cell.borderPt??.5)+'pt'}"
            :aria-label="'表格单元格 '+(r+1)+'-'+(c+1)" tabindex="0" @click.stop="selected=block.id;activeTableCell={blockId:block.id,row:r,cell:c}"
            @keydown.enter.stop.prevent="selected=block.id;activeTableCell={blockId:block.id,row:r,cell:c}">
            <template v-if="cell.spans?.length"><span v-for="(part,i) in cell.spans" :key="i" :style="{fontWeight:part.bold?700:400,fontStyle:part.italic?'italic':undefined,textDecoration:part.underline?'underline':undefined,color:part.color,fontSize:(part.fontSize??cell.fontSize??block.fontSize)+'px'}">{{part.text}}</span></template>
            <template v-else>{{cell.text}}</template>
            <span v-if="cell.fieldKey" class="field-chip word-field-chip">{{fieldLabel(cell.fieldKey)}}：{{fieldInfo(cell.fieldKey)?.example}}</span>
           </td>
          </tr>
         </tbody>
        </table>
        <small>复制自 Word 的表格结构；点击单元格可绑定中文业务字段</small>
       </div>
       <div v-else class="paper-divider"/>
      </div>
      <div v-if="!design.blocks.length" class="paper-empty">从左侧选择字段或组件，创建模板内容</div>
      <div class="paper-footer">第 1 页 / 共 N 页（实际页码自动生成）</div>
     </div>
    </section>
    <aside class="designer-side properties">
     <h2>组件设置</h2>
     <template v-if="selectedBlock">
      <div class="property-group"><label>字体大小</label>
       <a-input-number v-model:value="selectedBlock.fontSize" :min="8" :max="28" style="width:100%"/>
      </div>
      <div class="property-group"><label>对齐方式</label>
       <a-radio-group v-model:value="selectedBlock.align" button-style="solid" size="small">
        <a-radio-button value="LEFT">左</a-radio-button><a-radio-button value="CENTER">中</a-radio-button><a-radio-button value="RIGHT">右</a-radio-button>
       </a-radio-group>
      </div>
      <div v-if="selectedBlock.type==='TITLE'||selectedBlock.type==='TEXT'" class="property-group">
       <label>显示文字</label><a-textarea v-model:value="selectedBlock.text" :rows="4" :maxlength="600" show-count/>
      </div>
      <div v-if="selectedBlock.type==='FIELD'" class="property-group">
       <label>中文字段（自动填充）</label>
       <a-select show-search option-filter-prop="label" v-model:value="selectedBlock.fieldKey" style="width:100%" :options="fields.filter(f=>!f.repeated&&f.key!=='items').map(f=>({value:f.key,label:f.label}))"/>
       <p class="hint">{{fieldInfo(selectedBlock.fieldKey)?.description}}</p>
      </div>
      <div v-if="selectedBlock.type==='WORD_TABLE'" class="property-group">
       <label>Word 表格结构</label>
       <p class="hint">{{selectedBlock.rows?.length}} 行 × {{selectedBlock.widths?.length}} 列。合并单元格、列宽和复制样式随模板保存。</p>
       <p class="hint">点击中间表格单元格，设置对应的业务字段。</p>
      </div>
      <template v-if="selectedBlock.type==='WORD_TABLE'&&selectedWordCell">
       <div class="property-group">
        <label>单元格原始文字</label>
        <a-textarea :value="selectedWordCell.text" :rows="3" :maxlength="600" @change="updateCellText(($event.target as HTMLTextAreaElement).value)"/>
       </div>
       <div class="property-group">
        <label>中文业务字段绑定</label>
        <a-select :value="selectedWordCell.fieldKey" allow-clear show-search option-filter-prop="label"
         style="width:100%" placeholder="选中文字段自动填充" :options="fields.filter(f=>!f.repeated&&f.key!=='items').map(f=>({value:f.key,label:f.label}))"
         @change="setCellField($event as string|undefined)"/>
        <p class="hint">保存后仅此单元格插入受控字段标记，不修改其它单元格与原模板版本。</p>
       </div>
       <div class="property-group">
        <label>单元格对齐</label>
        <a-radio-group v-model:value="selectedWordCell.align" size="small">
         <a-radio-button value="LEFT">左</a-radio-button><a-radio-button value="CENTER">中</a-radio-button><a-radio-button value="RIGHT">右</a-radio-button>
        </a-radio-group>
       </div>
      </template>
      <div v-if="selectedBlock.type==='TABLE'" class="property-group">
       <label>循环表格列（1–8列）</label>
       <label v-for="field in repeated" :key="field.key" class="check-col">
        <input type="checkbox" :checked="selectedBlock.columns?.includes(field.key)" :disabled="!selectedBlock.columns?.includes(field.key)&&((selectedBlock.columns?.length??0)>=8)"
         @change="toggleColumn(selectedBlock!,field.key,($event.target as HTMLInputElement).checked)"/> {{field.label}}
       </label>
      </div>
      <a-button block danger :disabled="selectedBlock.type==='FIELD'&&mandatory.has(selectedBlock.fieldKey??'')" @click="remove(selectedBlock)">删除所选组件</a-button>
     </template>
     <a-empty v-else description="请在中间选择一个组件"/>
     <a-divider/>
     <p class="hint">报告编号、正式/草稿标记是必填字段。生成 PDF 前仍须验证模板并由有权限人员发布。</p>
    </aside>
   </div>
  </a-spin>
 </main>
</template>

<style scoped>
.native-designer{--line:#dce5ed;--muted:#66778b;color:#23364d}.designer-heading{display:flex;align-items:center;justify-content:space-between;gap:16px;flex-wrap:wrap}.designer-heading h1{font-size:24px;letter-spacing:-.03em;margin:4px 0 5px}.designer-heading p,.hint{font-size:12px;color:var(--muted);line-height:1.6;margin:4px 0}.designer-alert{margin:12px 0}.designer-meta{margin:16px 0}.designer-meta label,.property-group>label{display:block;font-size:13px;font-weight:600}.designer-meta label :deep(.ant-input){margin-top:6px}.designer-workspace{display:grid;grid-template-columns:220px minmax(340px,1fr) 235px;gap:16px;align-items:start}.designer-side{background:#fff;border:1px solid var(--line);border-radius:12px;padding:16px;min-height:300px}.designer-side h2{font-size:15px;margin:0 0 10px}.designer-side h3{font-size:13px;margin:18px 0 8px}.quick-add{display:grid;gap:7px;margin-top:12px}.field-picker{max-height:360px;overflow:auto;margin-top:10px;display:grid;gap:5px}.field-choice{background:#f8fafc;border:1px solid transparent;border-radius:8px;text-align:left;padding:9px;cursor:pointer;color:#23364d}.field-choice:hover{background:#eef5fd;border-color:#c8d9ea}.field-choice span{display:block;font-size:13px;font-weight:600}.field-choice small{display:block;color:var(--muted);font-size:11px;margin-top:3px}.page-stage{background:#eef2f6;border:1px solid var(--line);border-radius:12px;padding:14px}.page-info{display:flex;justify-content:space-between;color:var(--muted);font-size:12px;margin-bottom:12px}.paper{background:white;border:1px solid #d9e1ea;box-shadow:0 4px 18px #17354c12;margin:0 auto;min-height:735px;max-width:735px;padding:40px 34px 34px;display:flex;flex-direction:column;gap:4px}.paper-block{padding:9px 8px;border:1px dashed transparent;cursor:grab;min-height:40px;border-radius:7px}.paper-block:hover{border-color:#bed2e7;background:#fafdff}.paper-block.selected{border-color:#3b7cc8;background:#f0f6ff}.block-rail{display:flex;justify-content:space-between;align-items:center;min-height:14px}.block-type{color:#8ca0b3;font-size:10px;letter-spacing:.04em}.block-controls{display:flex;gap:0}.paper-text{margin:5px 0;white-space:pre-wrap;overflow-wrap:anywhere}.paper-field{margin:5px 0;word-break:break-word}.field-chip{background:#eaf2fc;color:#225a8f;padding:3px 6px;border-radius:4px;font-weight:500}.paper-table-wrapper{overflow-x:auto;margin-top:8px}.paper-table{width:100%;border-collapse:collapse;font-size:11px;table-layout:fixed}.paper-table th,.paper-table td{border:1px solid #cfdbe6;padding:8px 5px;word-break:break-word;text-align:left}.paper-table th{background:#edf3f9;font-weight:600}.paper-table-wrapper small{display:block;color:var(--muted);margin-top:6px}.paper-divider{height:1px;background:#d7e0e9;margin:12px 0}.paper-footer{margin-top:auto;padding-top:25px;text-align:center;color:var(--muted);font-size:11px}.paper-empty{color:#92a4b6;padding:60px 0;text-align:center}.property-group{margin-bottom:17px}.property-group>label{margin-bottom:7px}.check-col{display:block;padding:5px 0;font-weight:400!important}.check-col input{margin-right:4px}
.paste-card{margin:12px 0 16px;padding:10px;background:#f5f9fe;border:1px solid #d4e3f3;border-radius:10px}.paste-card strong{font-size:12px}.word-paste-target{min-height:55px;max-height:110px;overflow:auto;border:1px dashed #83acd6;background:white;border-radius:7px;padding:12px;margin:10px 0;outline:none}.word-paste-target:empty:before{content:'单击这里，Ctrl+V 粘贴';color:#71849c;font-size:12px}.word-paste-target:focus{border-color:#2e77ba;box-shadow:0 0 0 2px #c9e0f6}.word-table-wrapper .imported-word-table{table-layout:fixed}.word-table-wrapper .imported-word-table td{border-style:solid;white-space:pre-wrap;word-break:break-word;cursor:pointer}.word-table-wrapper .imported-word-table td.chosen-word-cell{outline:2px solid #277fcf;outline-offset:-2px;background-image:linear-gradient(#267ad714,#267ad714)}.word-field-chip{display:inline-block;margin-left:4px;font-size:10px}
@media(max-width:1180px){.designer-workspace{grid-template-columns:180px minmax(300px,1fr)}.properties{grid-column:1/-1}.paper{padding:28px 22px}}
@media(max-width:720px){.designer-workspace{grid-template-columns:minmax(0,1fr)}.properties{grid-column:auto}.page-stage{padding:10px}.paper{min-height:560px;padding:22px 12px}.page-info{flex-wrap:wrap;gap:4px}.designer-side{min-height:0}.field-picker{max-height:180px}}
</style>
