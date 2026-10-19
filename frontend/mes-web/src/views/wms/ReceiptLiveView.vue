<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted, onUpdated, nextTick } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { api, errorMessage, type Page } from '../../api/http'
import { useControlledRequest } from '../../api/controlled'
import { useAuthStore } from '../../stores/auth'
import { useReceiptLiveRuntime, SOURCE_CONTRACT, localDate, checkText, checkValue, receiptDisplayQuantity, receiptDisplayTime, receiptRequestPriorityOptions, type RawRecord } from './receiptLiveRuntime'
import WmsLookup from './WmsLookup.vue'
import ProcessLookup from '../process/ProcessLookup.vue'
import IncomingReferencePicker from '../quality/IncomingReferencePicker.vue'
import BatchPrintButton from '../../components/printing/BatchPrintButton.vue'
import ReceiptAttachments from './ReceiptAttachments.vue'
const router=useRouter(), vueRoute=useRoute(), auth=useAuthStore(), controlled=useControlledRequest(), live=useReceiptLiveRuntime()
const loading=ref(false), total=ref(0), activeReceipt=ref<Receipt|null>(null)
const units=computed(()=>live.unitIds)
const unitLabel=(id:string)=>live.label('units',id)
const locationLabel=(id:string)=>live.label('locations',id)
const materialInfo=(id:string)=>live.materialCache[id]
const sourceFor=live.sourceFor
const warehouseLocations=(id:string)=>Object.values(live.locationRows).filter(x=>String(x.warehouseId)===id).map(x=>String(x.id))
const can=(permission:string)=>auth.can(permission)
const canView=can('wms:receipt:view')
function requirePermission(permission:string){if(!can(permission))throw Error('没有此操作权限')}

import {
  SearchOutlined,
  PlusOutlined, ArrowLeftOutlined, FileSearchOutlined,
} from '@ant-design/icons-vue'
import './live/receipt-prototype.css'
import './live/receipt-pages.css'
import './live/receipt-flow.css'

type RecordStatus = 'DRAFT' | 'APPROVED'
type QualityStatus =
  | 'QUARANTINE' | 'PENDING_SAMPLING' | 'SAMPLING' | 'SAMPLED' | 'TESTING'
  | 'PENDING_QC_REVIEW' | 'QC_PASSED' | 'QC_FAILED' | 'PENDING_QA_RELEASE'
  | 'PENDING_DISPOSITION' | 'RELEASED' | 'REJECTED'
type InventoryStatus = 'BLOCKED' | 'AVAILABLE' | 'FROZEN'

interface Item {
  [key:string]:any
  id: string
  material: string
  spec: string
  materialCode?: string
  manufacturerName?: string
  assignmentKey?: string
  saved?: boolean | null
  requestStatus?: string
  lotNo: string
  supplierLotNo: string
  manufacturerLotNo: string
  manufactureDate: string
  expiryDate: string
  retestDate: string
  receivedQty: string
  unit: string
  packageSpec: string
  packageCount: number
  location: string
  container: string
  packageCheckPassed: boolean | null
  sealCheckPassed: boolean | null
  labelCheckPassed: boolean | null
  damageCheckPassed: boolean | null
  contaminationCheckPassed: boolean | null
  lotId?: string
  qualityStatus?: QualityStatus
  inventoryStatus?: InventoryStatus
  requestNo?: string
  requestQualitySpec?: string
  requestQuantity?: string
  requestUnit?: string
  requestPackages?: number
  requestDate?: string
  requestPriority?: string
  requestType?: string
}

interface Receipt {
  [key:string]:any
  id: string
  version?: number
  arrivalDate?: string
  receiptNo: string
  supplier: string
  warehouse: string
  purchaseOrderNo: string
  deliveryNoteNo: string
  transportCheckPassed: boolean | null
  recordStatus: RecordStatus
  createdAt?: string
  receivedAt: string
  createdBy: string
  confirmedAt?: string
  confirmedBy?: string
  reason?: string
  acceptanceText?: string
  items: Item[]
  audit: Array<{ time: string; actor: string; action: string; detail: string }>
}

const statusLabels: Record<RecordStatus, string> = { DRAFT: '草稿', APPROVED: '已确认' }
const qualityLabels: Record<QualityStatus, string> = {
  QUARANTINE: '待验', PENDING_SAMPLING: '待取样', SAMPLING: '取样中', SAMPLED: '已取样',
  TESTING: '检验中', PENDING_QC_REVIEW: '待QC复核', QC_PASSED: 'QC合格', QC_FAILED: 'QC不合格',
  PENDING_QA_RELEASE: '待QA放行', PENDING_DISPOSITION: '待处置', RELEASED: '已放行', REJECTED: '不合格',
}
const inventoryLabels: Record<InventoryStatus, string> = {
  BLOCKED: '禁止生产使用', AVAILABLE: '可用', FROZEN: '冻结',
}
const checkLabels = [
  ['packageCheckPassed', '包装'],
  ['sealCheckPassed', '封签'],
  ['labelCheckPassed', '标签'],
  ['damageCheckPassed', '破损'],
  ['contaminationCheckPassed', '污染'],
] as const

function blankItem(): Item {
  return {
    id: crypto.randomUUID(),
    assignmentKey: crypto.randomUUID(), materialCode: '', manufacturerName: '', saved: false, material: '', spec: '', lotNo: '', supplierLotNo: '', manufacturerLotNo: '',
    manufactureDate: '', expiryDate: '', retestDate: '', receivedQty: '', unit: '',
    packageSpec: '', packageCount: 1, location: '', container: '',
    packageCheckPassed: null, sealCheckPassed: null, labelCheckPassed: null,
    damageCheckPassed: null, contaminationCheckPassed: null,
  }
}


function syncControlNames(){
  document.querySelectorAll<HTMLElement>('.receipt-bpm [aria-label],.bpm-dialog [aria-label]').forEach(wrapper=>{
    if(wrapper.matches('input,select,textarea,button'))return
    const control=wrapper.querySelector<HTMLInputElement>('input[role="combobox"],input[role="spinbutton"]')
    if(control){if(!wrapper.id)wrapper.id='receipt-label-'+control.id;control.setAttribute('aria-labelledby',wrapper.id);if(wrapper.hasAttribute('aria-invalid'))control.setAttribute('aria-invalid',wrapper.getAttribute('aria-invalid')!)}
  })
}
function syncA11y(){syncControlNames();syncErrorAssociations()}
onMounted(()=>nextTick(syncA11y));onUpdated(()=>nextTick(syncA11y))
const storageError=ref('')
const receipts=ref<Receipt[]>([])
const route=computed(()=>({mode:vueRoute.meta.mode==='create'?'create':vueRoute.meta.mode==='edit'?'edit':vueRoute.meta.mode==='view'?'detail':'list',id:String(vueRoute.params.id||'')}))
const current=computed(()=>activeReceipt.value)
const isList=computed(()=>route.value.mode==='list')
const isForm=computed(()=>['create','edit'].includes(route.value.mode))
const formBlocked=computed(()=>loading.value||!can(route.value.mode==='edit'?'wms:receipt:update':'wms:receipt:create')||!!storageError.value||(route.value.mode==='edit'&&(!current.value||!current.value.allowedActions?.includes('UPDATE'))))
function go(path:string){void router.push({path:'/wms/receipts'+(path==='list'?'':'/'+path),query:vueRoute.query})}

/* list */
const query=reactive({keyword:String(vueRoute.query.keyword||''),status:String(vueRoute.query.recordStatus||''),supplier:String(vueRoute.query.supplierId||''),warehouse:String(vueRoute.query.warehouseId||'')})
const page=ref(Number(vueRoute.query.page||1)),pageSize=20
const pageRows=computed(()=>receipts.value)
async function loadScreen(){
 const epoch=++loadEpoch;loading.value=true;storageError.value='';controlled.clear();activeReceipt.value=null;receipts.value=[];total.value=0
 try{
  await live.units()
  if(route.value.mode==='list'){
   const result=await api<Page<RawRecord>>({url:'/wms/receipts',params:{page:page.value-1,size:pageSize,keyword:query.keyword||undefined,recordStatus:query.status||undefined,supplierId:query.supplier||undefined,warehouseId:query.warehouse||undefined}})
   const mapped=await Promise.all(result.items.map(live.mapReceipt));if(epoch!==loadEpoch)return
   receipts.value=mapped as Receipt[];total.value=result.total;activeReceipt.value=null
  }else if(route.value.mode==='create'){
   activeReceipt.value=null;initForm()
  }else{
   const raw=await api<RawRecord>({url:'/wms/receipts/'+route.value.id});const mapped=await live.mapReceipt(raw);if(epoch!==loadEpoch)return
   activeReceipt.value=mapped as Receipt;await live.locations(mapped.warehouse)
   if(route.value.mode==='edit')initForm()
  }
 }catch(e){if(epoch===loadEpoch){storageError.value=e instanceof Error?e.message:errorMessage(e)}}finally{if(epoch===loadEpoch)loading.value=false}
}
let loadEpoch=0
async function runQuery(){page.value=1;await router.replace({query:{keyword:query.keyword||undefined,recordStatus:query.status||undefined,supplierId:query.supplier||undefined,warehouseId:query.warehouse||undefined,page:'1'}});await loadScreen()}
function resetQuery(){query.keyword='';query.status='';query.supplier='';query.warehouse='';void runQuery()}
function pageChanged(value:number){page.value=value;void router.replace({query:{...vueRoute.query,page:String(value)}})}

function materialSummary(r: Receipt) { return r.items.map(i => i.materialName||i.materialCode).join('、') }
function lotSummary(r: Receipt) { return r.items.map(i => i.lotNo).join('、') }
function totalQty(r: Receipt) { return r.items.map(i => `${i.receivedQty} ${unitLabel(i.unit)}`).join(' / ') }
function openDetail(id: string) { go(id) }
function openEdit(id: string) { go(id + '/edit') }

/* form: business facts and readonly allocated identity */
const form=reactive<Receipt>({id:'',receiptNo:'',supplier:'',warehouse:'',purchaseOrderNo:'',deliveryNoteNo:'',transportCheckPassed:null,recordStatus:'DRAFT',receivedAt:'',createdBy:'',items:[blankItem()],audit:[]})
const errors=ref<string[]>([]), baseline=ref(''), saving=ref(false), activeCheckIndex=ref(0), extraOpen=ref(false)
const selectedItem=computed(()=>form.items[activeCheckIndex.value])
const checkOptions=[{value:'UNKNOWN',label:'未检查'},{value:'PASS',label:'通过'},{value:'FAIL',label:'不通过'}]
const checkCode=(v:boolean|null|undefined)=>v==null?'UNKNOWN':v?'PASS':'FAIL'
const itemUnits=(it:Item)=>[...new Set([...live.unitIds,it.unit].filter(Boolean))]
function initForm(){
 errors.value=[];activeCheckIndex.value=0;extraOpen.value=false
 if(route.value.mode==='edit'&&current.value)Object.assign(form,JSON.parse(JSON.stringify(current.value)))
 else Object.assign(form,{id:'',version:0,inputContract:SOURCE_CONTRACT,receiptNo:'',supplier:'',warehouse:'',purchaseOrderNo:'',deliveryNoteNo:'',acceptanceText:'',transportCheckPassed:null,recordStatus:'DRAFT',arrivalDate:localDate(),receivedAt:'',createdBy:auth.identity?.displayName||'',items:[blankItem()],audit:[]})
 baseline.value=JSON.stringify(form)
}
async function leave(path:string){go(path)}
function cancelForm(){void leave(route.value.mode==='edit'&&current.value?current.value.id:'list')}
onBeforeRouteLeave(()=>!isForm.value||baseline.value===JSON.stringify(form)||new Promise<boolean>(resolve=>Modal.confirm({title:'放弃未保存的修改？',okText:'放弃修改',cancelText:'继续编辑',onOk:()=>resolve(true),onCancel:()=>resolve(false)})))

function clearLotFacts(it:Item){it.manufacturerLotNo='';it.supplierLotNo='';it.manufactureDate='';it.expiryDate='';it.retestDate='';checkLabels.forEach(([k])=>it[k]=null)}
const lineEpoch=new Map<string,number>()
async function selectMaterial(it:Item,id:string|null){
 if(!id||it.saved||it.material===id)return
 const epoch=(lineEpoch.get(it.id)||0)+1;lineEpoch.set(it.id,epoch)
 clearLotFacts(it);it.material=id;it.lotNo='';it.manufacturerName='';it.receivedQty=''
 try{
  const m=await live.material(id);if(lineEpoch.get(it.id)!==epoch)return
  it.materialName=m.name;it.materialCode=m.code;it.spec=m.spec;it.packageSpec=m.pack;it.unit=m.unit
  if(form.supplier){const src=await live.source(id,form.supplier);if(lineEpoch.get(it.id)===epoch)it.manufacturerName=src?.manufacturer||''}
  const result=await controlled.mutate<RawRecord>('/wms/receipts/number-reservations','POST',{materialId:id,clientLineId:it.assignmentKey+':'+id})
  if(lineEpoch.get(it.id)===epoch)it.lotNo=String(result.lotNo)
 }catch(e){errors.value=[errorMessage(e)];await controlled.failure(e);message.error(controlled.error.value)}
}
async function selectSupplier(id:string){
 if(form.supplier===id)return
 const old=form.supplier;form.supplier=id;form.transportCheckPassed=null
 for(const it of form.items){if(old)clearLotFacts(it);it.manufacturerName='';if(it.material&&id)try{const src=await live.source(it.material,id);if(form.supplier===id)it.manufacturerName=src?.manufacturer||''}catch(e){errors.value=[errorMessage(e)]}}
}
async function selectWarehouse(id:string){if(form.warehouse===id)return;form.warehouse=id;form.items.forEach(it=>it.location='');try{await live.locations(id)}catch(e){errors.value=[errorMessage(e)]}}
function selectUnit(it:Item,id:string|null){if(it.unit!==id){it.unit=id||'';it.receivedQty=''}}

function addItem(){form.items.push(blankItem());activeCheckIndex.value=form.items.length-1}
function removeItem(index:number){const it=form.items[index];if(!it||it.saved||form.items.length<=1)return;form.items.splice(index,1);activeCheckIndex.value=Math.min(activeCheckIndex.value,form.items.length-1)}
function validateRecord(r:Receipt,confirm=false):string[]{
  const out:string[]=[]
  if(!r.receiptNo.trim())out.push('请填写收货单号')
  if(!r.supplier)out.push('请选择供应商')
  if(!r.warehouse)out.push('请选择收货仓库')
  if(!r.arrivalDate||r.arrivalDate>localDate())out.push('请核对到货日期')
  if(!r.items.length)out.push('至少添加一条收货明细')
  const ids=new Set<string>()
  r.items.forEach((it,i)=>{
    const prefix=`第 ${i+1} 行：`
    if(!materialInfo(it.material))out.push(prefix+'请选择物料')
    if(!it.lotNo||ids.has(it.lotNo))out.push(prefix+'内部物料批号缺失或重复，请检查自动赋号')
    ids.add(it.lotNo)
    const source=sourceFor(it.material,r.supplier)
    if(!source||!it.manufacturerName||source.manufacturer!==it.manufacturerName)out.push(prefix+'无匹配的生产企业/供货来源')
    if(!it.manufacturerLotNo.trim())out.push(prefix+'请依据实物填写生产批号')
    if(!/^\d{1,12}(\.\d{1,6})?$/.test(it.receivedQty)||Number(it.receivedQty)<=0)out.push(prefix+'收货数量必须大于0，最多6位小数')
    if(!itemUnits(it).includes(it.unit))out.push(prefix+'请选择物料适用单位')
    if(!warehouseLocations(r.warehouse).includes(it.location))out.push(prefix+'库位必须属于当前仓库')
    if(!Number.isSafeInteger(it.packageCount)||it.packageCount<1)out.push(prefix+'包装件数必须为正整数')
    if(it.manufactureDate&&(it.manufactureDate>localDate()||(it.expiryDate&&it.expiryDate<it.manufactureDate)||(it.retestDate&&it.retestDate<it.manufactureDate)))out.push(prefix+'生产日期/有效期/复验日期不正确')
    if(confirm){if(checkLabels.some(([k])=>it[k]!==true))out.push(prefix+'到货检查尚未全部通过');if((it.expiryDate&&it.expiryDate<=localDate())||(it.retestDate&&it.retestDate<=localDate()))out.push(prefix+'已达有效期或复验期')}
  })
  if(confirm&&r.transportCheckPassed!==true)out.push('运输检查未完成或未通过')
  return out
}
function baseError(term:string){return errors.value.find(e=>!e.startsWith('第 ')&&e.includes(term))||''}
function rowError(index:number, term:string):string {
  return errors.value.find(e=>e.startsWith(`第 ${index+1} 行：`)&&e.includes(term))?.replace(`第 ${index+1} 行：`,'') || ''
}
function errorControl(text:string): HTMLElement | null {
  const row=/^第 (\d+) 行：/.exec(text)
  const rowFields=[['收货数量','收货数量'],['生产批号','生产批号'],['生产企业','物料名称'],['内部物料批号','物料名称'],['物料适用单位','收货单位'],['库位','存放库位'],['包装件数','包装件数'],['生产日期','生产日期'],['请选择物料','物料名称']]
  const baseFields=[['收货单号','收货单号'],['供应商','供应商'],['仓库','收货仓库'],['到货日期','到货日期']]
  const entry=(row?rowFields:baseFields).find(([term])=>text.includes(term!))
  if(!entry)return null
  const label=entry[1]!+(row?' '+row[1]:'')
  const host=document.querySelector<HTMLElement>('.receipt-main [aria-label="'+label+'"]')
  return host?.matches('input,textarea,select')?host:host?.querySelector<HTMLElement>('input,textarea,select')||null
}
function syncErrorAssociations(){
  document.querySelectorAll<HTMLElement>('[data-receipt-error]').forEach(el=>{el.removeAttribute('aria-describedby');el.removeAttribute('data-receipt-error');el.setAttribute('aria-invalid','false')})
  errors.value.forEach((text,i)=>{const el=errorControl(text);if(el){const old=el.getAttribute('aria-describedby');el.setAttribute('aria-invalid','true');el.setAttribute('data-receipt-error','true');el.setAttribute('aria-describedby',(old?old+' ':'')+'receipt-error-'+i)}})
}
function focusError(text:string){
  const row=/^第 (\d+) 行：/.exec(text);if(row)activeCheckIndex.value=Number(row[1])-1
  const target=errorControl(text);target?.scrollIntoView({block:'center',inline:'center'});target?.focus()
}
async function saveDraft(){
 if(saving.value||formBlocked.value)return
 errors.value=validateRecord(form);if(errors.value.length){message.error('请核对标注的问题，输入已保留');return}
 if(route.value.mode==='edit'&&!form.reason?.trim()){errors.value=['请填写修改原因'];return}
 saving.value=true
 try{
  const editing=route.value.mode==='edit';const body={...live.payload(form),...(editing?{versionNo:form.version,reason:form.reason}:{})}
  const result=await controlled.mutate<RawRecord>(editing?'/wms/receipts/'+form.id:'/wms/receipts',editing?'PUT':'POST',body,editing?form.version:undefined)
  baseline.value=JSON.stringify(form);message.success('收货草稿已保存');go(String(result.id))
 }catch(e){await controlled.failure(e);errors.value=[controlled.error.value]}finally{saving.value=false}
}

/* detail actions */
const confirmOpen = ref(false)
const confirmReason = ref('')
const requestOpen = ref(false)
const requestDetailOpen = ref(false)
const qualityOpen = ref(false)
const activeItem = ref<Item | null>(null)
const requestErrors=ref<string[]>([]), requestBusy=ref(false)
watch(requestErrors,()=>nextTick(()=>{if(!requestOpen.value||!requestErrors.value.length)return;const alert=document.querySelector<HTMLElement>('.bpm-request-errors');alert?.closest('.ant-modal-body')?.scrollTo({top:0});alert?.scrollIntoView({block:'nearest'});alert?.setAttribute('tabindex','-1');alert?.focus({preventScroll:true})}))
const requestForm = reactive({
  qcSpecificationVersionId: '', requestedQuantity: '', requestedUnitId: '',
  requestedPackageCount: 1, requestedDate: localDate(), priority: 'NORMAL',
  requestType: 'INITIAL', reason: '',
})
const confirmErrors=ref<string[]>([])
function openConfirm(){if(!can('wms:receipt:confirm'))return;confirmReason.value='';confirmErrors.value=[];confirmOpen.value=true}
async function confirmReceipt(){
 if(!current.value||!current.value.allowedActions?.includes('CONFIRM')||saving.value)return
 confirmErrors.value=[];if(!confirmReason.value.trim()){confirmErrors.value=['请填写确认原因'];return}
 saving.value=true
 try{await controlled.mutate('/wms/receipts/'+current.value.id+'/confirm','POST',{versionNo:current.value.version,reason:confirmReason.value},current.value.version);confirmOpen.value=false;await loadScreen();message.success('收货已确认，质量与库存状态以服务端结果为准')}
 catch(e){await controlled.failure(e);confirmErrors.value=[controlled.error.value]}finally{saving.value=false}
}

function openRequest(item: Item) {
  if(!can('qms:inspection-request:create')||current.value?.recordStatus!=='APPROVED'||!item.lotId||item.requestNo){message.warning('当前批次不能重复发起请验');return}
  activeItem.value = item
  requestErrors.value=[]
  requestForm.qcSpecificationVersionId = item.requestQualitySpec || ''
  requestForm.requestedQuantity = item.requestQuantity || item.receivedQty
  requestForm.requestedUnitId = item.requestUnit || item.unit
  requestForm.requestedPackageCount = item.requestPackages || item.packageCount
  requestForm.requestedDate = item.requestDate || localDate()
  requestForm.priority = item.requestPriority || 'NORMAL'
  requestForm.requestType = item.requestType || 'INITIAL'
  requestForm.reason = ''
  requestOpen.value = true
}
function showRequest(item: Item) { if(!can('qms:inspection-request:view'))return;activeItem.value = item; requestDetailOpen.value = true }
async function submitRequest(){
 if(requestBusy.value||!activeItem.value||!current.value)return
 requestErrors.value=[]
 if(!requestForm.qcSpecificationVersionId||!requestForm.reason.trim()){requestErrors.value=['请选择适用质量标准并填写请验原因'];return}
 requestBusy.value=true
 try{await controlled.mutate('/quality/inspection-requests','POST',{materialLotId:activeItem.value.lotId,...requestForm});requestOpen.value=false;await loadScreen();message.success('请验草稿已保存，未自动提交或放行')}
 catch(e){await controlled.failure(e);requestErrors.value=[controlled.error.value]}finally{requestBusy.value=false}
}
async function refreshAttachments(){await loadScreen()}
function viewAudit(){if(current.value)void router.push({path:'/audit',query:{objectType:'MaterialReceipt',objectId:current.value.id}})}
watch(()=>vueRoute.fullPath,()=>void loadScreen(),{immediate:true})

function onQualityVisibility(open:boolean){if(!open)nextTick(()=>document.querySelector<HTMLElement>('[aria-label="批次质量情况"]')?.focus())}
function qualityTag(status?: QualityStatus) {
  if (!status) return 'default'
  if (['RELEASED'].includes(status)) return 'green'
  if (['REJECTED', 'QC_FAILED'].includes(status)) return 'red'
  if (['QC_PASSED', 'PENDING_QA_RELEASE'].includes(status)) return 'blue'
  return 'orange'
}
function inventoryTag(status?: InventoryStatus) {
  return status === 'AVAILABLE' ? 'green' : status === 'FROZEN' ? 'blue' : 'orange'
}
</script>

<template>
<div class="receipt-prototype receipt-bpm receipt-live" :class="{ 'receipt-t12': isList || isForm }" :data-ui-template="isList ? 'T1' : isForm ? 'T2' : 'T3'">
  <div class="receipt-workspace">
    <section class="receipt-main" :aria-busy="loading">
      <a-alert v-if="controlled.conflict.value" type="warning" message="记录已变化，输入已保留；重新加载前请确认未保存内容。"><template #action><a-button @click="loadScreen">重新加载</a-button></template></a-alert>
      <a-spin v-if="loading" tip="正在读取收货记录"/>
      <a-alert v-if="storageError" type="error" :message="storageError" show-icon/>
      <!-- T1 -->
      <a-alert v-if="!canView" type="error" message="当前身份没有收货记录查看权限"/>
      <template v-else-if="isList">
        <div class="receipt-titlebar receipt-page-head">
          <div><div class="receipt-title"><h1>收货管理</h1></div><span class="receipt-muted">原辅料收货记录 · 收货单与物料批质量状态分离</span></div>
          <a-button v-if="can('wms:receipt:create')" type="primary" @click="go('create')"><PlusOutlined /> 新增收货单</a-button>
        </div>
        <section class="receipt-card">
          <div class="receipt-card-head"><h2>查询条件</h2></div>
          <form class="receipt-query-fields" @submit.prevent="runQuery">
            <label class="receipt-form-pair"><span>单号 / 关键字</span><a-input v-model:value="query.keyword" aria-label="单号或关键字" allow-clear placeholder="收货单号、批号、物料" /></label>
            <label class="receipt-form-pair"><span>记录状态</span><a-select v-model:value="query.status" aria-label="记录状态" :options="[{value:'',label:'全部状态'},{value:'DRAFT',label:'草稿'},{value:'APPROVED',label:'已确认'}]" /></label>
            <label class="receipt-form-pair"><span>供应商</span><WmsLookup v-model="query.supplier" resource="suppliers" label="供应商"/></label>
            <label class="receipt-form-pair"><span>仓库</span><WmsLookup v-model="query.warehouse" resource="warehouses" label="仓库"/></label>
            <div class="receipt-query-actions"><a-button @click="resetQuery">重置</a-button><a-button type="primary" html-type="submit"><SearchOutlined /> 查询</a-button></div>
          </form>
        </section>
        <section class="receipt-card">
          <div class="receipt-card-head"><h2>收货记录</h2><span class="receipt-muted">共 {{ total }} 条</span></div>
          <div class="receipt-table-overflow" tabindex="0" aria-label="明细表格，可横向滚动">
            <table class="receipt-table receipt-list-table">
              <thead><tr><th>收货单号</th><th>供应商</th><th>物料 / 批次</th><th>收货数量</th><th>收货仓库</th><th>记录状态</th><th>收货日期</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="row in pageRows" :key="row.id">
                  <td><button class="receipt-link receipt-strong-link" @click="openDetail(row.id)">{{ row.receiptNo }}</button></td>
                  <td>{{ row.supplierName }}</td>
                  <td><b>{{ materialSummary(row) }}</b><small>{{ lotSummary(row) }} · {{ row.items.length }} 批</small></td>
                  <td>{{ totalQty(row) }}</td>
                  <td>{{ row.warehouseName }}</td>
                  <td><a-tag :color="row.recordStatus === 'APPROVED' ? 'green' : 'default'">{{ statusLabels[row.recordStatus] }}</a-tag></td>
                  <td>{{ receiptDisplayTime(row.receivedAt) }}</td>
                  <td class="receipt-actions-cell"><span class="receipt-row-actions"><button class="receipt-link" @click="openDetail(row.id)">查看</button><button v-if="row.recordStatus === 'DRAFT' && can('wms:receipt:update')" class="receipt-link" @click="openEdit(row.id)">编辑</button></span></td>
                </tr>

              </tbody>
            </table>
          </div>
          <div v-if="!pageRows.length" class="receipt-live-empty" role="status">未找到符合条件的收货记录，请调整查询条件</div>
          <div class="receipt-list-foot"><span>共 {{ total }} 条记录</span><a-pagination :current="page" :page-size="pageSize" :total="total" :show-size-changer="false" size="small" @change="pageChanged" /></div>
        </section>
      </template>

      <!-- T2: original mockup region order, compact row table, separate checks, one bottom save -->
      <template v-else-if="isForm">
        <div class="receipt-titlebar bpm-form-title"><div><a-button @click="cancelForm"><ArrowLeftOutlined/> 返回收货记录</a-button><h1>{{route.mode==='edit'?'编辑收货单':'新增收货单'}}</h1></div><a-tag>草稿</a-tag></div>
        <a-alert v-if="formBlocked" type="error" message="该收货单不存在、没有编辑权限、存储不可用或已经确认，不能进入编辑。"/>
        <template v-else>
        <a-alert v-if="errors.length" class="bpm-errors" type="error" show-icon><template #message>输入已保留，请核对</template><template #description><div v-for="(e,errorIndex) in errors" :key="e" :id="'receipt-error-'+errorIndex"><button type="button" class="receipt-link bpm-error-link" @click="focusError(e)">{{e}}</button></div></template></a-alert>
        <form @submit.prevent="saveDraft">
          <section class="receipt-card" data-region="basic">
            <div class="receipt-card-head"><h2>收货基本信息</h2></div>
            <div class="bpm-basic-grid">
              <label class="bpm-field"><span>收货单号</span><div class="bpm-control"><a-input v-model:value="form.receiptNo" aria-label="收货单号" :aria-invalid="!!baseError('收货单号')" maxlength="80"/><small v-if="baseError('收货单号')" class="bpm-error">{{baseError('收货单号')}}</small></div></label>
              <label class="bpm-field"><span>到货日期 <em>*</em></span><a-input v-model:value="form.arrivalDate" aria-label="到货日期" type="date"/></label>
              <label class="bpm-field"><span>运输检查</span><a-select :value="checkCode(form.transportCheckPassed)" @change="(v:string)=>form.transportCheckPassed=checkValue(v)" aria-label="运输检查" :options="checkOptions"/></label>
              <label class="bpm-field"><span>供应商 <em>*</em></span><WmsLookup :model-value="form.supplier" resource="suppliers" label="供应商" @update:model-value="selectSupplier"/></label>
              <label class="bpm-field"><span>采购单号</span><a-input v-model:value="form.purchaseOrderNo" aria-label="采购单号"/></label><div></div>
              <label class="bpm-field"><span>送货单号</span><a-input v-model:value="form.deliveryNoteNo" aria-label="送货单号"/></label>
              <label class="bpm-field"><span>仓库 <em>*</em></span><WmsLookup :model-value="form.warehouse" resource="warehouses" label="收货仓库" @update:model-value="selectWarehouse"/></label>
            </div>
          </section>
          <section class="receipt-card" data-region="lines">
            <div class="receipt-card-head"><h2>收货明细</h2><a-button type="primary" @click="addItem"><PlusOutlined/> 添加收货明细</a-button></div>
            <p class="receipt-live-scroll-hint">明细表可左右滚动查看全部信息；点击校验提示可定位到对应字段。</p>
            <div class="bpm-scroll" tabindex="0" aria-label="收货明细表，可横向滚动">
              <table class="bpm-lines"><colgroup><col style="width:2.6778%"><col style="width:5.6904%"><col style="width:8.7866%"><col style="width:5.4393%"><col style="width:9.9582%"><col style="width:11.6318%"><col style="width:8.9540%"><col style="width:6.3598%"><col style="width:5.6904%"><col style="width:4.6862%"><col style="width:6.7782%"><col style="width:8.6192%"><col style="width:8.6192%"><col style="width:6.1088%"></colgroup>
                <thead><tr><th>序号</th><th>物料编码</th><th>物料名称</th><th>规格</th><th>内部物料批号</th><th>生产企业</th><th>生产批号</th><th>收货数量</th><th>单位</th><th>包装件数</th><th>库位</th><th>生产日期</th><th>有效期至</th><th>操作</th></tr></thead>
                <tbody><tr v-for="(it,i) in form.items" :key="it.id" :class="{selected:activeCheckIndex===i,invalid:errors.some(e=>e.startsWith(`第 ${i+1} 行：`))}" :data-row="i+1">
                  <td><button type="button" class="receipt-link" :aria-label="'核对第'+(i+1)+'行'" @click="activeCheckIndex=i">{{i+1}}</button></td>
                  <td>{{it.materialCode||'—'}}</td>
                  <td><ProcessLookup :model-value="it.material" resource="materials" :label="'物料名称 '+(i+1)" :disabled="!!it.saved" @update:model-value="v=>selectMaterial(it,v)"/></td>
                  <td>{{it.spec||'—'}}</td>
                  <td><input :value="it.lotNo" :aria-label="'内部物料批号 '+(i+1)" :title="it.lotNo" readonly placeholder="选物料后自动分配" class="bpm-readonly"/></td>
                  <td><span :aria-label="'生产企业 '+(i+1)">{{it.manufacturerName||'待匹配供货来源'}}</span><small v-if="it.material&&!it.manufacturerName" class="bpm-error">请选择匹配的供应商</small></td>
                  <td><input v-model="it.manufacturerLotNo" :aria-label="'生产批号 '+(i+1)" :aria-invalid="!!rowError(i,'生产批号')" placeholder="按实物录入" maxlength="100"/><small v-if="rowError(i,'生产批号')" class="bpm-error">{{rowError(i,'生产批号')}}</small></td>
                  <td><input v-model="it.receivedQty" :aria-label="'收货数量 '+(i+1)" :aria-invalid="!!rowError(i,'收货数量')" inputmode="decimal" placeholder="0.000"/><small v-if="rowError(i,'收货数量')" class="bpm-error">{{rowError(i,'收货数量')}}</small></td>
                  <td><ProcessLookup :model-value="it.unit" resource="units" :label="'收货单位 '+(i+1)" @update:model-value="v=>selectUnit(it,v)"/></td>
                  <td><a-input-number v-model:value="it.packageCount" :aria-label="'包装件数 '+(i+1)" :min="1" :precision="0" :controls="false"/></td>
                  <td><WmsLookup v-model="it.location" resource="locations" :warehouse-id="form.warehouse" :label="'存放库位 '+(i+1)"/></td>
                  <td><input v-model="it.manufactureDate" :aria-label="'生产日期 '+(i+1)" type="date"/></td>
                  <td><input v-model="it.expiryDate" :aria-label="'有效期至 '+(i+1)" type="date"/></td>
                  <td><button class="receipt-link" type="button" @click="activeCheckIndex=i;extraOpen=true">编辑</button><button v-if="!it.saved&&form.items.length>1" class="receipt-link" type="button" @click="removeItem(i)">删除</button><span v-else-if="it.saved" class="bpm-muted">已保留</span></td>
                </tr></tbody>
              </table>
            </div>
          </section>
          <section class="receipt-card" data-region="quality">
            <div class="receipt-card-head"><h2>到货质量核对</h2><label class="bpm-field bpm-check-picker"><span>当前批次</span><a-select v-model:value="activeCheckIndex" aria-label="当前核对批次" :options="form.items.map((x,i)=>({value:i,label:(i+1)+' · '+(x.materialName||'未选物料')+' · '+(x.lotNo||'尚未赋号')}))"/></label></div>
            <div class="bpm-quality-layout">
              <div><div v-if="selectedItem" class="bpm-check-grid">
                <label class="bpm-check-card"><span>运输条件</span><select :value="checkCode(form.transportCheckPassed)" @change="form.transportCheckPassed=checkValue(($event.target as HTMLSelectElement).value)" aria-label="核对运输条件"><option v-for="o in checkOptions" :key="o.value" :value="o.value">{{o.label}}</option></select></label>
                <label v-for="[key,label] in checkLabels" :key="key" class="bpm-check-card"><span>{{label==='破损'?'无破损':label==='污染'?'无污染':label==='标签'?'标签信息':label+'完整'}}</span><select :value="checkCode(selectedItem?.[key])" @change="selectedItem && (selectedItem[key]=checkValue(($event.target as HTMLSelectElement).value))" :aria-label="label+'检查'"><option v-for="o in checkOptions" :key="o.value" :value="o.value">{{o.label}}</option></select></label>
              </div><p class="bpm-check-note">检查按当前物料批次分别记录。未检查或不通过时可保存草稿，不得确认收货。</p></div>
              <aside class="bpm-business-note"><b>收货业务提示</b><p>收货确认不等于质量放行。</p><p>内部批号由系统分配，生产批号依据实物核对。</p><p>确认后的必验物料保持待验，未放行不得生产使用。</p></aside>
            </div>
          </section>
          <div class="bpm-live-extra"><label class="bpm-field"><span>验收信息</span><a-textarea v-model:value="form.acceptanceText" aria-label="验收信息" :rows="2" maxlength="1000"/></label><label v-if="route.mode==='edit'" class="bpm-field"><span>修改原因 *</span><a-textarea v-model:value="form.reason" aria-label="修改原因" :rows="2" maxlength="1000"/></label></div><div class="receipt-form-footer bpm-footer"><span class="bpm-muted">按批次核对来源、生产批号与数量</span><a-space><a-button @click="cancelForm">取消</a-button><a-button type="primary" html-type="submit" :loading="saving">保存草稿</a-button></a-space></div>
        </form></template>
      </template>

      <!-- T3 -->
      <template v-else-if="current">
        <div class="receipt-titlebar receipt-page-head">
          <div><button class="receipt-link receipt-back" aria-label="返回收货列表" @click="go('list')"><ArrowLeftOutlined /> 返回收货列表</button><div class="receipt-title"><h1>收货单详情</h1><strong class="bpm-document-no">{{current.receiptNo}}</strong><a-tag :color="current.recordStatus === 'APPROVED' ? 'green' : 'default'">{{ statusLabels[current.recordStatus] }}</a-tag></div><span class="receipt-muted">{{ current.receiptNo }} · 收货单状态与物料批质量/库存状态分离</span></div>
          <a-space><BatchPrintButton business-type="WMS_RECEIPT" :record-ids="[current.id]"/><a-button v-if="current.recordStatus === 'DRAFT' && current.allowedActions?.includes('CONFIRM') && can('wms:receipt:confirm')" type="primary" @click="openConfirm">确认收货</a-button><a-button v-if="current.recordStatus === 'APPROVED'" @click="qualityOpen = true" aria-label="批次质量情况"><FileSearchOutlined /> 批次质量情况</a-button><a-button v-if="current.recordStatus === 'DRAFT' && current.allowedActions?.includes('UPDATE') && can('wms:receipt:update')" @click="openEdit(current.id)">编辑收货单</a-button></a-space>
        </div>
        <section class="receipt-card bpm-facts-card">
          <div class="receipt-card-head"><h2>基本信息</h2></div>
          <div class="bpm-detail-facts">
            <div class="bpm-fact-group">
              <div class="bpm-fact"><label>收货单号</label><span>{{current.receiptNo}}</span></div>
              <div class="bpm-fact"><label>供应商</label><span>{{current.supplierName}}</span></div>
              <div class="bpm-fact"><label>送货单号</label><span>{{current.deliveryNoteNo||'—'}}</span></div>
              <div class="bpm-fact"><label>到货日期</label><span>{{current.arrivalDate||'未记录'}}</span></div>
            </div>
            <div class="bpm-fact-group">
              <div class="bpm-fact"><label>收货状态</label><span><a-tag :color="current.recordStatus==='APPROVED'?'green':'default'">{{statusLabels[current.recordStatus]}}</a-tag></span></div>
              <div class="bpm-fact"><label>仓库</label><span>{{current.warehouseName}}</span></div>
              <div class="bpm-fact"><label>运输检查</label><span>{{checkText(current.transportCheckPassed)}}</span></div>
              <div class="bpm-fact"><label>采购单号</label><span>{{current.purchaseOrderNo||'—'}}</span></div>
            </div>
            <div class="bpm-fact-group bpm-provenance">
              <div class="bpm-fact"><label>验收信息</label><span class="bpm-acceptance" :title="current.acceptanceText||'未填写'">{{current.acceptanceText||'未填写'}}</span></div>
              <div class="bpm-provenance-line"><div class="bpm-fact"><label>创建人</label><span>{{current.createdBy||'—'}}</span></div><div class="bpm-fact"><label>创建时间</label><span>{{receiptDisplayTime(current.createdAt||current.receivedAt)}}</span></div></div>
              <div class="bpm-provenance-line"><div class="bpm-fact"><label>确认人</label><span>{{current.confirmedBy||'尚未确认'}}</span></div><div class="bpm-fact"><label>确认时间</label><span>{{current.confirmedAt?receiptDisplayTime(current.confirmedAt):'尚未确认'}}</span></div></div>
            </div>
          </div>
        </section>

        <section class="receipt-card">
          <div class="receipt-card-head receipt-material-head"><div><h2>物料明细与质量状态</h2><p>请验、取样、检验、QA 放行是物料批级别的独立流程；本页不提供 QA 放行</p></div><div><a-tag color="blue">共 {{ current.items.length }} 批</a-tag><a-tag v-if="current.recordStatus === 'APPROVED'" color="orange">质量放行在 QMS/QA 完成</a-tag></div></div>
          <p class="bpm-table-scroll-hint">窗口较窄时，可在表格内左右滚动；操作列固定在右侧。</p>
          <div class="receipt-table-overflow" tabindex="0" aria-label="明细表格，可横向滚动">
            <table class="receipt-table receipt-flow-table">
              <colgroup><col style="width:2.5%"><col style="width:5%"><col style="width:6.5%"><col style="width:5%"><col style="width:9.5%"><col style="width:11%"><col style="width:8.5%"><col style="width:4.5%"><col style="width:3%"><col style="width:4.5%"><col style="width:6%"><col style="width:5%"><col style="width:8%"><col style="width:6%"><col style="width:8.5%"><col style="width:6%"></colgroup>
              <thead><tr><th>序号</th><th>物料编码</th><th>物料名称</th><th>规格</th><th>内部物料批号</th><th>生产企业</th><th>生产批号</th><th>数量</th><th>单位</th><th>包装件数</th><th>质量 / 库存</th><th>是否已请验</th><th>请验单号</th><th>有效期至</th><th>到货检查</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="(it,rowIndex) in current.items" :key="it.id">
                  <td>{{rowIndex+1}}</td><td>{{it.materialCode}}</td><td><b>{{it.materialName}}</b></td><td>{{it.spec||'—'}}</td>
                  <td>{{it.lotNo}}</td><td>{{it.manufacturerName||'未记录'}}</td><td>{{it.manufacturerLotNo||'未记录生产批号'}}</td>
                  <td class="bpm-quantity">{{receiptDisplayQuantity(it.receivedQty)}}</td><td>{{unitLabel(it.unit)}}</td><td>{{it.packageCount}}</td>
                  <td><div class="receipt-flow-quality"><a-tag v-if="it.qualityStatus" :color="qualityTag(it.qualityStatus)">{{qualityLabels[it.qualityStatus]}}</a-tag><a-tag v-if="it.inventoryStatus" :color="inventoryTag(it.inventoryStatus)">{{inventoryLabels[it.inventoryStatus]}}</a-tag><span v-if="!it.qualityStatus" class="receipt-muted">未生成物料批</span></div></td>
                  <td><a-tag v-if="it.requestNo" color="blue">{{it.requestStatus==='DRAFT'?'请验草稿':'已请验'}}</a-tag><a-tag v-else color="default">未请验</a-tag></td>
                  <td>{{it.requestNo||'—'}}</td><td>{{it.expiryDate||'—'}}</td>
                  <td class="bpm-detail-checks"><details class="bpm-check-disclosure"><summary :aria-label="it.material+'到货检查明细：'+(checkLabels.some(([k])=>it[k]===false)?'不通过':checkLabels.some(([k])=>it[k]==null)?'待核对':'已通过')">{{checkLabels.some(([k])=>it[k]===false)?'不通过':checkLabels.some(([k])=>it[k]==null)?'待核对':'已通过'}}</summary><div><span v-for="[key,label] in checkLabels" :key="key" class="receipt-check-tag" :class="it[key]==null?'pending':it[key]?'ok':'bad'">{{label}}：{{checkText(it[key])}}</span><small>生产日期：{{it.manufactureDate||'—'}}</small><small>库位：{{it.location||'—'}}</small></div></details></td>
                  <td><button v-if="current.recordStatus === 'APPROVED' && !it.requestNo && can('qms:inspection-request:create')" class="receipt-link" @click="openRequest(it)">发起请验</button><button v-else-if="it.requestNo && can('qms:inspection-request:view')" class="receipt-link" @click="showRequest(it)">查看请验</button><span v-else class="receipt-muted">{{current.recordStatus==='DRAFT'?'先确认收货':'无操作权限'}}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="receipt-table-foot"><span>共 {{ current.items.length }} 条物料批次记录</span><span>QC 合格 ≠ QA 放行；未放行批次库存保持不可用</span></div>
        </section>

        <div class="receipt-bottom">
          <section class="receipt-card"><ReceiptAttachments :receipt-id="current.id" :version-no="current.version||0" @reload="refreshAttachments"/></section>
          <section class="receipt-card"><div class="receipt-card-head"><h2>操作记录</h2><a-button v-if="can('audit:view')" type="link" @click="viewAudit">查看审计追踪</a-button></div><div class="bpm-audit-wrap"><table class="bpm-audit-table"><thead><tr><th>时间</th><th>操作人</th><th>操作内容</th></tr></thead><tbody><tr v-for="(a,i) in current.audit" :key="i"><td>{{a.time}}</td><td>{{a.actor}}</td><td>{{a.action}}<small>{{a.detail}}</small></td></tr></tbody></table></div></section>
        </div>
      </template>

      <a-empty v-else description="收货单不存在或已被移除"><a-button @click="go('list')">返回收货列表</a-button></a-empty>
      
    </section>
  </div>

  <a-modal v-model:open="extraOpen" title="收货明细补充" :footer="null" :width="620" wrap-class-name="bpm-dialog">
    <div v-if="selectedItem" class="bpm-extra-fields"><p>{{selectedItem.materialName}} · {{selectedItem.lotNo}}</p>
      <label class="bpm-field"><span>包装规格</span><a-input v-model:value="selectedItem.packageSpec" aria-label="补充包装规格"/></label>
      <label class="bpm-field"><span>复验日期</span><a-input v-model:value="selectedItem.retestDate" type="date" aria-label="复验日期"/></label>
      <label class="bpm-field"><span>容器</span><WmsLookup v-model="selectedItem.container" resource="containers" label="收货容器"/></label>
      <label class="bpm-field"><span>供货方另行标识</span><a-input v-model:value="selectedItem.supplierLotNo" aria-label="供货方另行标识" placeholder="仅在随货文件确有不同标识时记录"/></label>
      <a-button type="primary" @click="extraOpen=false">完成</a-button>
    </div>
  </a-modal>
  <!-- confirm modal -->
  <a-modal v-model:open="confirmOpen" title="确认收货" :footer="null" :width="580" wrap-class-name="bpm-dialog"><a-alert v-if="confirmErrors.length" type="error" :message="confirmErrors.join('；')"/>
    <p>确认后生成物料批与收货流水。质量和库存状态按该物料是否必验及已批准放行规则执行，不由页面直接判定。</p>
    <a-alert type="info" show-icon message="确认收货不等于质量放行；请验、取样、检验、QA 放行是后续独立流程。" />
    <label class="receipt-modal-field"><span>确认原因 <em>*</em></span><a-textarea v-model:value="confirmReason" aria-label="确认原因" :rows="3" placeholder="记录确认依据，如到货核对、运输检查、随货文件核对结果" /></label>
    <div class="receipt-modal-actions"><a-button @click="confirmOpen = false">取消</a-button><a-button type="primary" @click="confirmReceipt">确认收货</a-button></div>
  </a-modal>

  <!-- request modal -->
  <a-modal v-model:open="requestOpen" title="发起物料批请验" :footer="null" :width="650" wrap-class-name="bpm-dialog">
    <a-alert v-if="requestErrors.length" role="alert" class="bpm-request-errors" type="error" :message="requestErrors.join('；')"/>
    <div v-if="activeItem" class="receipt-modal">
      <p>仅针对当前物料批次发起请验；请验不等于检验合格或 QA 放行。</p>
      <div class="receipt-modal-grid">
        <label class="receipt-modal-field"><span>物料批次</span><b>{{ activeItem.materialName }} · {{ activeItem.lotNo }}</b></label>
        <label class="receipt-modal-field"><span>质量标准版本 <em>*</em></span><IncomingReferencePicker field="qcSpecificationVersionId" v-model="requestForm.qcSpecificationVersionId" input-label="质量标准版本" :context="{materialLotId:activeItem.lotId,materialId:activeItem.material}"/></label>
        <label class="receipt-modal-field"><span>请验数量 <em>*</em></span><a-input v-model:value="requestForm.requestedQuantity" aria-label="请验数量" type="number" /></label>
        <label class="receipt-modal-field"><span>请验单位 <em>*</em></span><a-select v-model:value="requestForm.requestedUnitId" aria-label="请验单位" :options="units.map(x => ({ value: x, label: unitLabel(x) }))" /></label>
        <label class="receipt-modal-field"><span>请验件数 <em>*</em></span><a-input-number v-model:value="requestForm.requestedPackageCount" aria-label="请验件数" :min="1" :precision="0" style="width:100%" /></label>
        <label class="receipt-modal-field"><span>请验日期 <em>*</em></span><a-input v-model:value="requestForm.requestedDate" aria-label="请验日期" type="date" /></label>
        <label class="receipt-modal-field"><span>优先级</span><a-select v-model:value="requestForm.priority" aria-label="优先级" :options="receiptRequestPriorityOptions" /></label>
        <label class="receipt-modal-field"><span>请验类型</span><a-select v-model:value="requestForm.requestType" aria-label="请验类型" :options="[{value:'INITIAL',label:'首次请验'}]" /></label>
        <label class="receipt-modal-field receipt-modal-full"><span>请验原因 <em>*</em></span><a-textarea v-model:value="requestForm.reason" aria-label="请验原因" :rows="2" placeholder="说明请验依据" /></label>
      </div>
      <a-alert type="info" show-icon message="保存至正式请验模块，状态为草稿；不会自动提交、接受、取样或放行。" />
      <div class="receipt-modal-actions"><a-button @click="requestOpen = false">取消</a-button><a-button type="primary" :loading="requestBusy" @click="submitRequest">确认请验</a-button></div>
    </div>
  </a-modal>

  <!-- request detail -->
  <a-modal v-model:open="requestDetailOpen" title="请验单信息" :footer="null" :width="480" wrap-class-name="bpm-dialog">
    <div v-if="activeItem" class="receipt-modal">
      <div><label>请验单号</label><b>{{ activeItem.requestNo }}</b></div>
      <div><label>物料批次</label>{{ activeItem.materialName }} · {{ activeItem.lotNo }}</div>
      <div><label>质量标准</label>{{ activeItem.requestQualitySpec }}</div><div><label>请验状态</label>{{activeItem.requestStatus==='DRAFT'?'草稿，未提交':'已有请验记录'}}</div>
      <div><label>请验数量</label>{{ receiptDisplayQuantity(activeItem.requestQuantity) }} {{ unitLabel(activeItem.requestUnit||'') }} / {{ activeItem.requestPackages }} 件</div>
      <div><label>质量状态</label><a-tag v-if="activeItem.qualityStatus" :color="qualityTag(activeItem.qualityStatus)">{{ qualityLabels[activeItem.qualityStatus] }}</a-tag></div>
      <a-alert type="info" show-icon message="请验、取样、检验、报告和 QA 放行是不同业务动作；当前页面不提供放行。" />
    </div>
  </a-modal>

  <!-- quality drawer -->
  <a-drawer v-model:open="qualityOpen" title="批次质量情况" :width="440" root-class-name="bpm-quality-drawer" @after-open-change="onQualityVisibility">
    <p class="receipt-drawer-tip">各批次独立请验和质量管理，不以收货单状态统一判定放行；QA 放行在物料批次 360/物料放行页面完成。</p>
    <div v-for="it in current?.items || []" :key="it.id" class="receipt-drawer-row">
      <div><b>{{ it.materialName }}</b><a-tag v-if="it.qualityStatus" :color="qualityTag(it.qualityStatus)">{{ qualityLabels[it.qualityStatus] }}</a-tag></div>
      <small>{{ it.lotNo }} · {{ it.requestNo || '尚未请验' }} · {{ it.inventoryStatus ? inventoryLabels[it.inventoryStatus] : '未生成物料批' }}</small>
    </div>
  </a-drawer>
</div>
</template>

<style scoped>
.receipt-list-table th:nth-child(1),.receipt-list-table td:nth-child(1){min-width:170px;white-space:nowrap}
.receipt-list-table th:nth-child(2),.receipt-list-table td:nth-child(2){min-width:190px}
.receipt-list-table th:nth-child(3),.receipt-list-table td:nth-child(3){min-width:250px}
.receipt-list-table th:nth-child(8),.receipt-list-table td:nth-child(8){min-width:110px;white-space:nowrap}
.receipt-flow-table{table-layout:fixed;min-width:0}
.receipt-flow-table th,.receipt-flow-table td{padding:11px 8px;white-space:normal;word-break:break-word;font-size:12px}
.receipt-flow-table th{font-size:11px}
.receipt-flow-table td small{display:block;margin-top:3px;color:#8fa0b2;font-size:10.5px;line-height:1.35}
.receipt-flow-quality{display:flex;flex-direction:column;gap:3px;align-items:flex-start}
.receipt-flow-quality .ant-tag{margin:0;font-size:10px;line-height:17px;padding:0 5px}
.receipt-check-tag{display:inline-block;margin:1px 3px 1px 0;padding:0 4px;border-radius:3px;font-size:10px;line-height:17px;background:#e9f7ee;color:#25744b}
.receipt-check-tag.bad{background:#fdecec;color:#b33a3a}
.receipt-modal-field{display:flex;flex-direction:row;align-items:center;gap:12px;margin:10px 0;color:#5d7084;font-size:13px}.receipt-modal-field>span{flex:0 0 100px}.receipt-modal-field>:last-child{flex:1;min-width:0}
.receipt-modal-field em{color:#d04a4a;font-style:normal}
.receipt-modal-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 16px}
.receipt-modal-full{grid-column:1/-1}
.receipt-modal-actions{display:flex;justify-content:flex-end;gap:8px;margin-top:14px}
.receipt-drawer-tip{font-size:12px;color:#7c8b9d;line-height:1.6}
.receipt-drawer-row{padding:10px 0;border-bottom:1px solid #edf2f7}
.receipt-drawer-row>div{display:flex;gap:8px;align-items:center}
.receipt-drawer-row small{display:block;color:#8a99aa;margin-top:3px}

.receipt-live .bpm-quantity{white-space:nowrap;word-break:normal;font-variant-numeric:tabular-nums}

.receipt-live .receipt-actions-cell{display:table-cell;vertical-align:middle}
.receipt-live .receipt-row-actions{display:inline-flex;align-items:center;gap:10px}
.receipt-live .receipt-live-empty{padding:28px 16px;text-align:center;color:var(--mes-ui-secondary,#465c75);font-size:13px;overflow-wrap:anywhere}
.receipt-live .receipt-live-scroll-hint{font-size:12px;color:var(--mes-ui-secondary,#465c75);padding:0 20px;margin:0 0 10px}
.receipt-live .bpm-scroll{scroll-padding-right:100px;scroll-padding-left:12px}
@media(min-width:1586px){.receipt-live .receipt-live-scroll-hint{display:none}}
</style>

