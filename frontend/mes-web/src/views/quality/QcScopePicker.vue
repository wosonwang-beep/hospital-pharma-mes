<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {api,errorMessage,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'

/** 适用范围列表 + 「添加」选择弹窗（DCP-QC-SPEC-BASIC-LIBRARY-001 的三条 2026-11-02 交互收窄）。 */
type Subject={id:string;name:string;code:string}
const props=defineProps<{modelValue:string[];resource:'materials'|'products';label:string;disabled?:boolean;seeded?:Subject[]}>()
const emit=defineEmits<{'update:modelValue':[string[]]}>()
const auth=useAuthStore()
const known=ref<Subject[]>([])
const pickerOpen=ref(false),keyword=ref(''),candidates=ref<Subject[]>([]),page=ref(0),total=ref(0),busy=ref(false),error=ref(''),checked=ref<string[]>([])
const permission=()=>props.resource==='materials'?'master:material:view':'master:product:view'
const canPick=computed(()=>auth.can(permission()))
const noun=computed(()=>props.label.replace(/^适用/,''))
const rows=computed<Subject[]>(()=>props.modelValue.map(id=>known.value.find(k=>k.id===id)??{id,name:`#${id}`,code:''}))
const columns=[{title:'名称',dataIndex:'name'},{title:'编码',dataIndex:'code',width:180},{title:'操作',key:'actions',width:90}]
const pickColumns=[{title:'名称',dataIndex:'name'},{title:'编码',dataIndex:'code',width:200}]

function collect(list:Record<string,unknown>[]){const p=props.resource==='materials'?'material':'product';const next:Subject[]=list.map(r=>({id:String(r.id),name:String(r[p+'Name']??''),code:String(r[p+'Code']??'')}));const merged=new Map(known.value.map(k=>[k.id,k]));next.forEach(n=>merged.set(n.id,n));known.value=[...merged.values()]}

let resolving=false
async function resolve(){if(!canPick.value)return;const missing=props.modelValue.filter(id=>!known.value.some(k=>k.id===id));if(!missing.length||resolving)return;resolving=true;try{for(const id of missing){try{const row=await api<Record<string,unknown>>({url:`/${props.resource}/${id}`});collect([row])}catch{known.value=[...known.value,{id,name:`#${id}`,code:''}]}}}finally{resolving=false}}

let ticket=0
async function loadCandidates(){if(!canPick.value)return;busy.value=true;error.value='';const current=++ticket;try{const result=await api<Page<Record<string,unknown>>>({url:`/${props.resource}`,params:{keyword:keyword.value||undefined,page:page.value,size:10}});if(current!==ticket)return;collect(result.items);candidates.value=result.items.map(r=>{const p=props.resource==='materials'?'material':'product';return {id:String(r.id),name:String(r[p+'Name']??''),code:String(r[p+'Code']??'')}});total.value=result.total}catch(e){error.value=errorMessage(e)}finally{if(current===ticket)busy.value=false}}

function openPicker(){if(!canPick.value)return;keyword.value='';page.value=0;checked.value=[...props.modelValue];pickerOpen.value=true;void loadCandidates()}
function confirmPick(){const next=[...props.modelValue];checked.value.forEach(id=>{if(!next.includes(id))next.push(id)});emit('update:modelValue',next);pickerOpen.value=false}
function remove(id:string){emit('update:modelValue',props.modelValue.filter(value=>value!==id))}

watch(()=>props.seeded,value=>{if(!value?.length)return;const merged=new Map(known.value.map(k=>[k.id,k]));value.forEach(s=>merged.set(s.id,s));known.value=[...merged.values()]},{immediate:true})
watch(()=>props.modelValue,()=>{void resolve()},{immediate:true})
</script>
<template>
<div class="qc-scope">
  <div class="qc-scope-bar"><span class="qc-scope-count">已选 {{modelValue.length}} 个{{noun}}</span><a-button v-if="!disabled" type="primary" size="small" :disabled="!canPick" @click="openPicker">＋ 添加{{noun}}</a-button></div>
  <p v-if="!canPick" class="qc-scope-hint">当前账号没有{{noun}}查看权限，无法维护适用范围。</p>
  <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="id" size="small" :scroll="{x:420}">
    <template #bodyCell="{column,record}">
      <template v-if="column.key==='actions'"><a-button v-if="!disabled" type="link" danger size="small" @click="remove(record.id)">移除</a-button></template>
      <span v-else-if="column.dataIndex==='code'" class="qc-scope-code">{{record.code||'—'}}</span>
      <span v-else>{{record.name}}</span>
    </template>
    <template #emptyText><span class="qc-scope-empty">尚未添加{{label}}</span></template>
  </a-table>
  <a-modal :open="pickerOpen" :title="`选择${label}`" :width="760" ok-text="确定" cancel-text="取消" :mask-closable="false" @ok="confirmPick" @cancel="pickerOpen=false">
    <div class="qc-picker-search"><a-input v-model:value="keyword" :placeholder="`搜索${noun}名称或编码`" allow-clear @press-enter="page=0;loadCandidates()" @change="page=0;loadCandidates()"/><a-button @click="loadCandidates">查询</a-button></div>
    <a-alert v-if="error" type="error" :message="error" show-icon/>
    <a-table :columns="pickColumns" :data-source="candidates" :loading="busy" :pagination="false" row-key="id" size="small" :scroll="{x:420}" :row-selection="{selectedRowKeys:checked,onChange:(keys:unknown[])=>checked=keys.map(String)}" :custom-row="(record:Subject)=>({onClick:(event:MouseEvent)=>{if((event.target as HTMLElement).closest('.ant-table-selection-column'))return;const index=checked.indexOf(record.id);checked=index>=0?checked.filter(id=>id!==record.id):[...checked,record.id]}})">
      <template #bodyCell="{column,record}"><span v-if="column.dataIndex==='code'" class="qc-scope-code">{{record.code}}</span><span v-else>{{record.name}}</span></template>
    </a-table>
    <div class="qc-picker-footer"><span>共 {{total}} 条，已选 {{checked.length}} 项</span><a-pagination :current="page+1" :page-size="10" :total="total" :show-size-changer="false" size="small" @change="(value:number)=>{page=value-1;loadCandidates()}"/></div>
  </a-modal>
</div>
</template>
<style scoped>
.qc-scope{min-width:0}
.qc-scope-bar{display:flex;align-items:center;justify-content:space-between;gap:12px;margin:0 0 10px}
.qc-scope-count{color:#606266;font-size:13px}
.qc-scope-hint{margin:0 0 10px;color:#b26a00;font-size:12px}
.qc-scope-code{color:#606266;font-variant-numeric:tabular-nums}
.qc-scope-empty{color:#909399;font-size:13px}
.qc-picker-search{display:flex;gap:8px;margin-bottom:12px}
.qc-picker-footer{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-top:12px;color:#909399;font-size:12px}
.qc-picker-footer :deep(.ant-pagination){margin:0}
.qc-scope :deep(.ant-table-tbody>tr){cursor:default}
</style>
