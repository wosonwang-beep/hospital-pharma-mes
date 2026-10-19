from pathlib import Path
import re,hashlib,json
R=Path(r'D:\codex\_project\gmp\hospital-pharma-mes');Q=R.parent/'mes-demo';E=R/'docs/review/evidence/RECEIPT_DEVELOPMENT_20261018';P=Q/'src/features/wms/ReceiptFlowPrototype.vue';s=P.read_text(encoding='utf-8')
assert not (R/'frontend/mes-web/src/views/wms/ReceiptLiveView.vue').exists(), 'Already integrated: edit current source directly; do not rerun historical conversion'
assert hashlib.sha256(P.read_bytes()).hexdigest()=='f539cd92a68f1ba9f84073297d4fdf2177a880dfd6f25a6cb6e8655eb8ebedca','Prototype changed; do not overwrite'
def rep(a,b,count=1):
 global s
 assert s.count(a)==count,(a[:90],s.count(a));s=s.replace(a,b)
def section(a,b,new):
 global s
 i=s.index(a);j=s.index(b,i);s=s[:i]+new+'\n'+s[j:]
rep('onBeforeUnmount, ','')
rep("import { materialCatalog, materialInfo, sourceFor, warehouseLocations, containerOptions, localDate, assignPreviewLot, readReceipts, writeReceipts, checkText, checkValue, productionExamples, prototypeCan } from './receiptBusiness'",'''import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { api, errorMessage, type Page } from '../../api/http'
import { useControlledRequest } from '../../api/controlled'
import { useAuthStore } from '../../stores/auth'
import { useReceiptLiveRuntime, SOURCE_CONTRACT, localDate, checkText, checkValue, type RawRecord } from './receiptLiveRuntime'
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
''')
for name in ['receipt-prototype.css','receipt-pages.css','receipt-flow.css']:rep("import './"+name+"'","import './live/"+name+"'")
rep('interface Item {\n  id: number','interface Item {\n  [key:string]:any\n  id: string')
rep('interface Receipt {','interface Receipt {\n  [key:string]:any')
section('const suppliers =','function blankItem()', '')
rep('id: Date.now() + Math.floor(Math.random() * 10000),','id: crypto.randomUUID(),')
section('function seedReceipts()', 'function syncControlNames()', '')
section("const storageError = ref('')",'/* list */',r'''const storageError=ref('')
const receipts=ref<Receipt[]>([])
const route=computed(()=>({mode:vueRoute.meta.mode==='create'?'create':vueRoute.meta.mode==='edit'?'edit':vueRoute.meta.mode==='view'?'detail':'list',id:String(vueRoute.params.id||'')}))
const current=computed(()=>activeReceipt.value)
const isList=computed(()=>route.value.mode==='list')
const isForm=computed(()=>['create','edit'].includes(route.value.mode))
const formBlocked=computed(()=>loading.value||!can(route.value.mode==='edit'?'wms:receipt:update':'wms:receipt:create')||!!storageError.value||(route.value.mode==='edit'&&(!current.value||!current.value.allowedActions?.includes('UPDATE'))))
function go(path:string){void router.push({path:'/wms/receipts'+(path==='list'?'':'/'+path),query:vueRoute.query})}
''')
section('const query =','function materialSummary',r'''const query=reactive({keyword:String(vueRoute.query.keyword||''),status:String(vueRoute.query.recordStatus||''),supplier:String(vueRoute.query.supplierId||''),warehouse:String(vueRoute.query.warehouseId||'')})
const page=ref(Number(vueRoute.query.page||1)),pageSize=20
const pageRows=computed(()=>receipts.value)
const filtered=computed(()=>receipts.value)
async function loadScreen(){
 const epoch=++loadEpoch;loading.value=true;storageError.value='';controlled.clear()
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
''')
s=s.replace('r.items.map(i => i.material).join',"r.items.map(i => i.materialName||i.materialCode).join")
s=s.replace('`${i.receivedQty} ${i.unit}`','`${i.receivedQty} ${unitLabel(i.unit)}`')
rep("createdBy:'张建华',items:[blankItem()]","createdBy:'',items:[blankItem()]",2)
rep("const itemUnits=(it:Item)=>materialInfo(it.material)?.units || []","const itemUnits=(it:Item)=>[...new Set([...live.unitIds,it.unit].filter(Boolean))]")
section('function nowText()', 'function clearLotFacts',r'''function initForm(){
 errors.value=[];activeCheckIndex.value=0;extraOpen.value=false
 if(route.value.mode==='edit'&&current.value)Object.assign(form,JSON.parse(JSON.stringify(current.value)))
 else Object.assign(form,{id:'',version:0,inputContract:SOURCE_CONTRACT,receiptNo:'',supplier:'',warehouse:'',purchaseOrderNo:'',deliveryNoteNo:'',acceptanceText:'',transportCheckPassed:null,recordStatus:'DRAFT',arrivalDate:localDate(),receivedAt:'',createdBy:auth.identity?.displayName||'',items:[blankItem()],audit:[]})
 baseline.value=JSON.stringify(form)
}
async function leave(path:string){go(path)}
function cancelForm(){void leave(route.value.mode==='edit'&&current.value?current.value.id:'list')}
onBeforeRouteLeave(()=>!isForm.value||baseline.value===JSON.stringify(form)||new Promise<boolean>(resolve=>Modal.confirm({title:'放弃未保存的修改？',okText:'放弃修改',cancelText:'继续编辑',onOk:()=>resolve(true),onCancel:()=>resolve(false)})))
''')
section('function selectMaterial(', 'function addItem()',r'''const lineEpoch=new Map<string,number>()
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
''')
section('function readLatest()', '/* detail actions */',r'''async function saveDraft(){
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
''')
section('function confirmReceipt()', 'function openRequest',r'''async function confirmReceipt(){
 if(!current.value||!current.value.allowedActions?.includes('CONFIRM')||saving.value)return
 confirmErrors.value=[];if(!confirmReason.value.trim()){confirmErrors.value=['请填写确认原因'];return}
 saving.value=true
 try{await controlled.mutate('/wms/receipts/'+current.value.id+'/confirm','POST',{versionNo:current.value.version,reason:confirmReason.value},current.value.version);confirmOpen.value=false;await loadScreen();message.success('收货已确认，质量与库存状态以服务端结果为准')}
 catch(e){await controlled.failure(e);confirmErrors.value=[controlled.error.value]}finally{saving.value=false}
}
''')
section('function submitRequest()', 'function onQualityVisibility',r'''async function submitRequest(){
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
''')
# Remove fake print handler; authoritative component takes over.
s=re.sub(r"function printReceipt\(\) \{[^\n]+\}\n",'',s)
# Remove independent prototype navigation chrome; keep unchanged business surfaces inside the formal MES shell.
i=s.index('  <aside class="receipt-nav">');j=s.index('    <main class="receipt-main">',i);s=s[:i]+'  <div class="receipt-workspace">\n'+s[j:]
s=s.replace('class="receipt-prototype receipt-bpm"','class="receipt-prototype receipt-bpm receipt-live"')
rep('    <main class="receipt-main">','''    <main class="receipt-main" :aria-busy="loading">
      <a-alert v-if="controlled.conflict.value" type="warning" message="记录已变化，输入已保留；重新加载前请确认未保存内容。"><template #action><a-button @click="loadScreen">重新加载</a-button></template></a-alert>
      <a-spin v-if="loading" tip="正在读取收货记录"/>''')
s=s.replace('filtered.length','total')
s=s.replace('@change="(p: number) => page = p"','@change="pageChanged"').replace('@change="(p:number)=>page=p"','@change="pageChanged"')
# Controlled entity widgets use identifiers directly, not reverse-lookup by potentially duplicate names.
s=re.sub(r'<a-select v-model:value="query.supplier".*?/>', '<WmsLookup v-model="query.supplier" resource="suppliers" label="供应商"/>',s)
s=re.sub(r'<a-select v-model:value="query.warehouse".*?/>', '<WmsLookup v-model="query.warehouse" resource="warehouses" label="仓库"/>',s)
s=re.sub(r'<a-select :value="form.supplier\|\|undefined".*?/>', '<WmsLookup :model-value="form.supplier" resource="suppliers" label="供应商" @update:model-value="selectSupplier"/>',s)
s=re.sub(r'<a-select :value="form.warehouse\|\|undefined".*?/>', '<WmsLookup :model-value="form.warehouse" resource="warehouses" label="收货仓库" @update:model-value="selectWarehouse"/>',s)
# Regex with arrow tokens uses non-greedy across the full tag.
s=re.sub(r'<a-select :value="it.material\|\|undefined".*?/>' , '<ProcessLookup :model-value="it.material" resource="materials" :label="\'物料名称 \'+(i+1)" :disabled="!!it.saved" @update:model-value="v=>selectMaterial(it,v)"/>',s)
s=re.sub(r'<a-select :value="it.unit\|\|undefined".*?/>','<ProcessLookup :model-value="it.unit" resource="units" :label="\'收货单位 \'+(i+1)" @update:model-value="v=>selectUnit(it,v)"/>',s)
s=re.sub(r'<a-select v-model:value="it.location".*?/>','<WmsLookup v-model="it.location" resource="locations" :warehouse-id="form.warehouse" :label="\'存放库位 \'+(i+1)"/>',s)
s=re.sub(r'<a-select v-model:value="selectedItem.container".*?/>','<WmsLookup v-model="selectedItem.container" resource="containers" label="收货容器"/>',s)
s=re.sub(r'<a-select v-model:value="requestForm.qcSpecificationVersionId".*?/>','<IncomingReferencePicker field="qcSpecificationVersionId" v-model="requestForm.qcSpecificationVersionId" input-label="质量标准版本" :context="{materialLotId:activeItem.lotId,materialId:activeItem.material}"/>',s)
s=s.replace("units.map(x => ({ value: x, label: x }))","units.map(x => ({ value: x, label: unitLabel(x) }))")
# Display labels from frozen source facts or live response mappings.
s=s.replace('{{ row.supplier }}','{{ row.supplierName }}').replace('{{ row.warehouse }}','{{ row.warehouseName }}')
s=s.replace('{{ current.supplier }}','{{ current.supplierName }}').replace('{{current.supplier}}','{{current.supplierName}}')
s=s.replace('{{ current.warehouse }}','{{ current.warehouseName }}').replace('{{current.warehouse}}','{{current.warehouseName}}')
s=s.replace('{{ it.material }}','{{ it.materialName }}').replace('{{it.material}}','{{it.materialName}}')
s=s.replace('{{ activeItem.material }}','{{ activeItem.materialName }}').replace('{{selectedItem.material}}','{{selectedItem.materialName}}')
s=s.replace("(x.material||'未选物料')","(x.materialName||'未选物料')")
s=s.replace('{{ it.unit }}','{{ unitLabel(it.unit) }}').replace('{{it.unit}}','{{unitLabel(it.unit)}}')
s=s.replace('{{ activeItem.requestUnit }}','{{ unitLabel(activeItem.requestUnit||\'\') }}').replace('{{ it.location }}','{{ locationLabel(it.location) }}')
rep('<a-button @click="printReceipt" aria-label="打印收货单"><PrinterOutlined /> 打印收货单</a-button>', '<BatchPrintButton business-type="WMS_RECEIPT" :record-ids="[current.id]"/>')
# Replace only original fake attachments and audit presenter, retaining region arrangement.
a='<section class="receipt-card"><div class="receipt-card-head"><h2>附件信息</h2></div><div class="receipt-aux-row"><FileTextOutlined/> 暂无附件；未上传不生成虚假文件记录</div></section>'
if a in s:s=s.replace(a,'<section class="receipt-card"><ReceiptAttachments :receipt-id="current.id" :version-no="current.version||0" @reload="refreshAttachments"/></section>')
s=s.replace('<h2>操作记录</h2><span>按时间倒序</span>','<h2>操作记录</h2><a-button v-if="can(\'audit:view\')" type="link" @click="viewAudit">查看审计追踪</a-button>')
s=s.replace('确认后，收货单状态变为<strong>已确认</strong>，每条物料生成待验物料批，库存状态为<strong>禁止生产使用</strong>。','确认后生成物料批与收货流水。质量和库存状态按该物料是否必验及已批准放行规则执行，不由页面直接判定。')
s=s.replace('当前原型只生成本地请验单号，不调用 QMS 接口，不改变 QA 放行状态。','保存至正式请验模块，状态为草稿；不会自动提交、接受、取样或放行。')
s=re.sub(r'<p class="receipt-proto-note">.*?</p>','',s)
s=s.replace('当前原型身份','当前身份')
# Arrival acceptance notes and controlled amendment reason were in design, not overloaded into one backend field.
a='          <div class="receipt-form-footer bpm-footer">'
if a not in s:a='          <div class="bpm-footer receipt-form-footer">'
# Search reliable last footer beginning.
pos=s.index('<div class="receipt-form-footer bpm-footer">') if '<div class="receipt-form-footer bpm-footer">' in s else s.index('<div class="bpm-footer')
s=s[:pos]+'''<div class="bpm-live-extra"><label class="bpm-field"><span>验收信息</span><a-textarea v-model:value="form.acceptanceText" aria-label="验收信息" :rows="2" maxlength="1000"/></label><label v-if="route.mode==='edit'" class="bpm-field"><span>修改原因 *</span><a-textarea v-model:value="form.reason" aria-label="修改原因" :rows="2" maxlength="1000"/></label></div>'''+s[pos:]
# Remove imports and declarations proven unused after template adaptation.
s=s.replace('  HomeOutlined, DatabaseOutlined, InboxOutlined, ExperimentOutlined, AppstoreOutlined,\n','').replace('  SafetyCertificateOutlined, SettingOutlined, BellOutlined, SearchOutlined, UserOutlined,\n','  SearchOutlined,\n').replace('PrinterOutlined, ','')
if s.count('FileTextOutlined')==1:s=s.replace('  FileTextOutlined,\n','')
s=s.replace("const filtered=computed(()=>receipts.value)\n",'')
s=s.replace("const itemUnits=(it:Item)=>[...new Set([...live.unitIds,it.unit].filter(Boolean))]","const itemUnits=(it:Item)=>[...new Set([...live.unitIds,it.unit].filter(Boolean))]")
assert 'seedReceipts' not in s and 'sessionStorage' not in s and 'prototypeCan' not in s and 'writeReceipts' not in s
# Fail closed rather than keep fixture name arrays in a live view.
for term in ['suppliers.map','warehouses.map','materials.map','qualitySpecs.','containerOptions']:
 assert term not in s,'Unmapped entity selector: '+term
out=R/'frontend/mes-web/src/views/wms/ReceiptLiveView.vue';out.write_text(s,encoding='utf-8')
liveDir=out.parent/'live';liveDir.mkdir(exist_ok=True)
for name in ['receipt-prototype.css','receipt-pages.css','receipt-flow.css']:(liveDir/name).write_bytes((Q/'src/features/wms'/name).read_bytes())
with (liveDir/'receipt-flow.css').open('a',encoding='utf-8') as f:f.write('\n/* Adapt source-approved business surface to the existing official app shell. */\n.receipt-live.receipt-prototype{display:block;width:100%;min-height:0}.receipt-live .receipt-workspace{width:100%;min-width:0}.receipt-live .receipt-main{padding:0}.receipt-live .bpm-live-extra{padding:16px 20px;background:#fff;margin-bottom:16px;display:grid;gap:12px}.receipt-live .wms-lookup,.receipt-live .process-lookup{width:100%;min-width:0}\n')
route=R/'frontend/mes-web/src/router/index.ts';old=route.read_text(encoding='utf-8');(E/'router-before.ts').write_text(old,encoding='utf-8')
lines=old.splitlines()
for i,line in enumerate(lines):
 if "path:'wms/receipts" in line:lines[i]=re.sub("import\('\.\./views/wms/(WmsListView|ReceiptDetailView)\.vue'\)","import('../views/wms/ReceiptLiveView.vue')",line)
route.write_text('\n'.join(lines)+'\n',encoding='utf-8')
(E/'frontend-source-map.json').write_text(json.dumps({'prototype':str(P),'prototype_sha256':hashlib.sha256(P.read_bytes()).hexdigest(),'formal_view':str(out),'copied_css':[str(liveDir/n) for n in ['receipt-prototype.css','receipt-pages.css','receipt-flow.css']],'phase':'LIVE_API_DEVELOPMENT_NOT_FINAL_ACCEPTANCE'},indent=2),encoding='utf-8')
print('FORMAL_RECEIPT_ROUTES_AND_SOURCE_ADAPTER_WRITTEN')
