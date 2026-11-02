<script setup lang="ts">
import {computed,reactive,ref,watch} from 'vue'
import {api} from '../../api/http'
import {blankQcItem,type QcItem} from '../../api/qcSpecifications'
import ProcessLookup from '../process/ProcessLookup.vue'

/** 检验项目：表格 + 行编辑弹窗。取值规则与 api/qcSpecifications.qcItemPayload 完全一致。 */
const props=defineProps<{modelValue:QcItem[];disabled?:boolean}>()
const emit=defineEmits<{'update:modelValue':[QcItem[]]}>()
const columns=[
  {title:'#',key:'index',width:52},
  {title:'项目编码',dataIndex:'itemCode',width:150},
  {title:'项目名称',dataIndex:'itemName'},
  {title:'必检',key:'required',width:74},
  {title:'结果类型',key:'resultType',width:94},
  {title:'合格标准',key:'criteria',width:186},
  {title:'方法',key:'method',width:152},
  {title:'操作',key:'actions',width:112}
]
const rows=computed(()=>props.modelValue.map((item,index)=>({...item,_key:item.specificationItemId??`new-${index}`})))
const units=ref<Record<string,string>>({})
const dialogOpen=ref(false),editingIndex=ref(-1),draft=reactive<QcItem>(blankQcItem()),dialogError=ref('')

function plainNumber(value:string|null){if(value===null||value==='')return '';const parsed=Number(value);return Number.isFinite(parsed)?String(parsed):value}
function criteria(item:QcItem){
  if(item.resultType==='TEXT')return item.textAcceptanceCriteria?.trim()||'—'
  const unit=item.unitId?(units.value[item.unitId]??''):''
  const lower=plainNumber(item.lowerLimit),upper=plainNumber(item.upperLimit)
  const span=lower&&upper?`${lower} ~ ${upper}`:lower?`≥ ${lower}`:upper?`≤ ${upper}`:'未设限值'
  return unit?`${span} ${unit}`:span
}
function method(item:QcItem){return item.methodCode?`${item.methodCode}${item.methodVersion?` v${item.methodVersion}`:''}`:'—'}
function openCreate(){editingIndex.value=-1;Object.assign(draft,blankQcItem());dialogError.value='';dialogOpen.value=true}
function openEdit(index:number){editingIndex.value=index;Object.assign(draft,props.modelValue[index]);dialogError.value='';dialogOpen.value=true}
function typeChanged(){const item=draft;if(item.resultType==='TEXT'){item.lowerLimit=null;item.upperLimit=null;item.unitId=null}else item.textAcceptanceCriteria=null}
function validate(item:QcItem){
  const code=item.itemCode.trim(),name=item.itemName.trim()
  if(!code)return '请填写项目编码'
  if(code.length>100)return '项目编码不能超过 100 字'
  if(props.modelValue.some((row,index)=>index!==editingIndex.value&&row.itemCode.trim().toUpperCase()===code.toUpperCase()))return `项目编码 ${code} 在本标准中已存在`
  if(!name)return '请填写项目名称'
  if(name.length>200)return '项目名称不能超过 200 字'
  const pattern=/^-?[0-9]{1,12}(\.[0-9]{1,6})?$/
  for(const value of [item.lowerLimit,item.upperLimit])if(value!==null&&value!==''&&!pattern.test(value))return '限值最多支持 12 位整数和 6 位小数'
  if(item.resultType==='NUMERIC'){
    if((item.lowerLimit===null||item.lowerLimit==='')&&(item.upperLimit===null||item.upperLimit===''))return '数值项目至少填写一个限值'
    if(!item.unitId)return '数值项目必须选择单位'
    if(item.textAcceptanceCriteria)return '数值项目不能填写文本合格标准'
  }else{
    if(!item.textAcceptanceCriteria?.trim())return '文本项目必须填写文本合格标准'
    if(item.lowerLimit||item.upperLimit||item.unitId)return '文本项目不能填写限值或单位'
  }
  if(!item.methodCode.trim())return '请填写方法编码'
  if(item.methodCode.trim().length>100)return '方法编码不能超过 100 字'
  if(!item.methodVersion.trim())return '请填写方法版本'
  if(item.methodVersion.trim().length>50)return '方法版本不能超过 50 字'
  return ''
}
function apply(){
  const message=validate(draft)
  if(message){dialogError.value=message;return}
  const next=[...props.modelValue]
  if(editingIndex.value<0)next.push({...draft});else next[editingIndex.value]={...draft}
  emit('update:modelValue',next)
  dialogOpen.value=false
}
function remove(index:number){emit('update:modelValue',props.modelValue.filter((_,position)=>position!==index))}

let resolving=false
async function resolveUnits(){
  if(resolving)return
  const missing=[...new Set(props.modelValue.map(item=>item.unitId).filter((id):id is string=>!!id))].filter(id=>!units.value[id])
  if(!missing.length)return
  resolving=true
  try{for(const id of missing){try{const row=await api<Record<string,unknown>>({url:`/units/${id}`});units.value={...units.value,[id]:String(row.unitName??'')}}catch{units.value={...units.value,[id]:`#${id}`}}}}finally{resolving=false}
}
watch(()=>props.modelValue,()=>{void resolveUnits()},{immediate:true})
</script>
<template>
<a-card title="检验项目" class="form-section qc-items">
  <template #extra><a-button v-if="!disabled" type="primary" @click="openCreate">＋ 添加检验项目</a-button></template>
  <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="_key" size="small" :scroll="{x:820}">
    <template #bodyCell="{column,record,index}">
      <template v-if="column.key==='index'">{{index+1}}</template>
      <template v-else-if="column.key==='required'"><a-tag :color="record.required?'blue':'default'">{{record.required?'必检':'非必检'}}</a-tag></template>
      <template v-else-if="column.key==='resultType'">{{record.resultType==='NUMERIC'?'数值':'文本'}}</template>
      <template v-else-if="column.key==='criteria'"><span class="qc-criteria">{{criteria(record)}}</span></template>
      <template v-else-if="column.key==='method'"><span class="qc-method">{{method(record)}}</span></template>
      <template v-else-if="column.key==='actions'">
        <a-space v-if="!disabled" :size="4"><a-button type="link" size="small" @click="openEdit(index)">编辑</a-button><a-popconfirm title="确定移除该检验项目？" ok-text="移除" cancel-text="取消" @confirm="remove(index)"><a-button type="link" size="small" danger>移除</a-button></a-popconfirm></a-space>
        <span v-else class="qc-readonly">只读</span>
      </template>
      <span v-else>{{record[column.dataIndex]}}</span>
    </template>
    <template #emptyText><span class="qc-empty">尚未维护检验项目，点右上角「添加检验项目」新增。</span></template>
  </a-table>
  <a-modal :open="dialogOpen" :title="editingIndex<0?'添加检验项目':'编辑检验项目'" :width="760" :mask-closable="false" :ok-text="editingIndex<0?'添加':'保存'" cancel-text="取消" @ok="apply" @cancel="dialogOpen=false">
    <a-alert v-if="dialogError" type="error" :message="dialogError" show-icon class="qc-dialog-alert"/>
    <div class="item-form">
      <label><span class="form-field-label">项目编码</span><input v-model="draft.itemCode" class="master-native-input" aria-label="项目编码" maxlength="100" :disabled="editingIndex>=0" placeholder="如 ASSAY"/></label>
      <p v-if="editingIndex>=0" class="item-hint">项目编码是业务键，已保存的项目不能改编码；如需更换请新增项目后移除旧项目。</p>
      <label><span class="form-field-label">项目名称</span><input v-model="draft.itemName" class="master-native-input" aria-label="项目名称" maxlength="200" placeholder="如 含量测定"/></label>
      <label><span class="form-field-label">是否必检</span><span class="item-check"><a-checkbox v-model:checked="draft.required">必检项目</a-checkbox></span></label>
      <label><span class="form-field-label">结果类型</span><a-select v-model:value="draft.resultType" aria-label="结果类型" @change="typeChanged"><a-select-option value="NUMERIC">数值</a-select-option><a-select-option value="TEXT">文本</a-select-option></a-select></label>
      <template v-if="draft.resultType==='NUMERIC'">
        <label><span class="form-field-label">下限（含）</span><input v-model="draft.lowerLimit" class="master-native-input" aria-label="下限" inputmode="decimal" placeholder="可留空"/></label>
        <label><span class="form-field-label">上限（含）</span><input v-model="draft.upperLimit" class="master-native-input" aria-label="上限" inputmode="decimal" placeholder="可留空"/></label>
        <label><span class="form-field-label">单位</span><ProcessLookup v-model="draft.unitId" resource="units" label="单位"/></label>
      </template>
      <label v-else class="item-full"><span class="form-field-label">文本合格标准</span><textarea v-model="draft.textAcceptanceCriteria" class="master-native-input" aria-label="文本合格标准" maxlength="1000" placeholder="如 应呈白色结晶性粉末，无异物"/></label>
      <label><span class="form-field-label">方法编码</span><input v-model="draft.methodCode" class="master-native-input" aria-label="方法编码" maxlength="100" placeholder="如 ChP-0301"/></label>
      <label><span class="form-field-label">方法版本</span><input v-model="draft.methodVersion" class="master-native-input" aria-label="方法版本" maxlength="50" placeholder="如 2025"/></label>
    </div>
  </a-modal>
</a-card>
</template>
<style scoped>
.qc-items{min-width:0}
.qc-criteria,.qc-method{overflow-wrap:anywhere}
.qc-empty,.qc-readonly{color:#909399;font-size:13px}
.qc-dialog-alert{margin-bottom:16px}
.item-form{display:grid;grid-template-columns:1fr;gap:14px;width:100%}
.item-form>label{display:grid;grid-template-columns:120px minmax(0,1fr);align-items:center;gap:0 12px;color:#606266;font-size:14px}
.item-form>.item-full{grid-template-columns:120px minmax(0,1fr);align-items:start}
.item-form :deep(.master-native-input),.item-form :deep(.ant-select),.item-form :deep(.process-lookup){width:100%;min-width:0}
.item-form textarea.master-native-input{min-height:72px;resize:vertical}
.item-form .item-check{display:flex;align-items:center;min-height:34px}
.item-hint{grid-column:1/-1;margin:-8px 0 0 132px;color:#8c6d1f;font-size:12px;line-height:18px}
</style>
