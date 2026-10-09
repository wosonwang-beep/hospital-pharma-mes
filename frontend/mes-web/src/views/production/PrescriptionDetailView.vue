<script setup lang="ts">
import {computed,onMounted,reactive,ref} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {api,errorMessage,idempotencyKey,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
import ProcessLookup from '../process/ProcessLookup.vue'
import type {Package} from '../process/model'
import {blankPrescription,prescriptionStatus,type ProductionPrescription} from './prescriptionModel'

const route=useRoute(),router=useRouter(),auth=useAuthStore()
const mode=computed(()=>String(route.meta.mode||'view'))
const create=computed(()=>mode.value==='create')
const edit=computed(()=>mode.value==='edit')
const record=ref<ProductionPrescription|null>(null)
const editable=computed(()=>create.value||(edit.value&&record.value?.status==='DRAFT'))
const processes=ref<Package[]>([])
const form=reactive(blankPrescription())
const reason=ref(''),busy=ref(false),error=ref(''),notice=ref('')
let requestKey=idempotencyKey()

function back(){void router.push({path:'/production/prescriptions',query:route.query})}
function sync(row:ProductionPrescription){
 record.value=row
 Object.assign(form,{
  prescriptionCode:row.prescriptionCode,prescriptionName:row.prescriptionName,productId:row.productId,
  processPackageId:row.processPackageId,batchBasisQty:row.batchBasisQty,unitId:row.unitId,
  items:row.items.map(i=>({...i}))
 })
}
async function load(){
 busy.value=true;error.value=''
 try{
  const page=await api<Page<Package>>({url:'/process-packages',params:{status:'ACTIVE',size:100}})
  processes.value=page.items.filter(p=>p.currentDefinition?.status==='EFFECTIVE')
  if(!create.value)sync(await api<ProductionPrescription>({url:'/production-prescriptions/'+String(route.params.id)}))
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
function addItem(){
 const lineNo=Math.max(0,...form.items.map(i=>i.lineNo))+1
 form.items.push({lineNo,materialId:'',requiredQty:'',unitId:'',overagePct:null,critical:false})
}
function removeItem(index:number){form.items.splice(index,1);form.items.forEach((x,i)=>x.lineNo=i+1)}
function createBody(){
 return {
  prescriptionCode:form.prescriptionCode,prescriptionName:form.prescriptionName,productId:form.productId,
  processPackageId:form.processPackageId,batchBasisQty:form.batchBasisQty,unitId:form.unitId,
  items:form.items.map(({lineNo,materialId,requiredQty,unitId,overagePct,critical})=>({lineNo,materialId,requiredQty,unitId,overagePct:overagePct||null,critical}))
 }
}
async function save(){
 busy.value=true;error.value='';notice.value=''
 try{
  if(!form.items.length)throw Error('至少维护一条处方物料')
  if(create.value){
   const created=await api<ProductionPrescription>({url:'/production-prescriptions',method:'POST',headers:{'Idempotency-Key':requestKey},data:createBody()})
   requestKey=idempotencyKey()
   sync(created)
   await router.replace('/production/prescriptions/'+created.id+'/edit')
   notice.value='生产处方草稿已创建'
  }else{
   if(!reason.value.trim())throw Error('请填写修改原因')
   const row=record.value!
   const payload={
    versionNo:row.versionNo,reason:reason.value,prescriptionName:form.prescriptionName,
    processPackageId:form.processPackageId,batchBasisQty:form.batchBasisQty,unitId:form.unitId,
    items:createBody().items
   }
   const updated=await api<ProductionPrescription>({url:'/production-prescriptions/'+row.id,method:'PUT',headers:{'Idempotency-Key':requestKey,'If-Match':'"'+row.versionNo+'"'},data:payload})
   requestKey=idempotencyKey();sync(updated);notice.value='生产处方草稿已保存'
  }
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
async function transition(action:'activate'|'deactivate'){
 if(!record.value)return
 if(!reason.value.trim()){error.value='请填写操作原因';return}
 busy.value=true;error.value='';notice.value=''
 try{
  const row=record.value
  const next=await api<ProductionPrescription>({url:'/production-prescriptions/'+row.id+'/'+action,method:'POST',headers:{'Idempotency-Key':idempotencyKey(),'If-Match':'"'+row.versionNo+'"'},data:{versionNo:row.versionNo,reason:reason.value}})
  sync(next);notice.value=action==='activate'?'生产处方已设为在用':'生产处方已停用'
  if(action==='activate')await router.replace('/production/prescriptions/'+next.id)
 }catch(e){error.value=errorMessage(e)}
 finally{busy.value=false}
}
const selectedProcess=computed(()=>processes.value.find(p=>p.id===form.processPackageId))
onMounted(load)
</script>

<template>
<main :data-ui-template="editable?'T2':'T3'" class="admin-page master-page prescription-detail">
 <header class="admin-page-header">
  <div>
   <a-button type="link" @click="back">← 返回生产处方</a-button>
   <h1>{{create?'新建生产处方':edit?'编辑生产处方':'生产处方详情'}}</h1>
   <p v-if="record">{{record.prescriptionCode}} · {{record.productName}} · {{prescriptionStatus[record.status]}}</p>
  </div>
  <a-space wrap>
   <a-button v-if="!create&&!edit&&record?.status==='DRAFT'&&auth.can('production:prescription:update')" @click="router.push('/production/prescriptions/'+record.id+'/edit')">编辑</a-button>
   <a-button v-if="editable" type="primary" :loading="busy" @click="save">保存草稿</a-button>
   <a-button @click="back">关闭</a-button>
  </a-space>
 </header>
 <a-alert v-if="error" type="error" :message="error" show-icon/>
 <a-alert v-if="notice" type="success" :message="notice" show-icon/>

 <a-card title="处方基本信息" class="form-section">
  <div class="prescription-form">
   <label><span>处方编码</span><a-input v-model:value="form.prescriptionCode" :disabled="!create" maxlength="64"/></label>
   <label><span>处方名称</span><a-input v-model:value="form.prescriptionName" :disabled="!editable" maxlength="200"/></label>
   <label><span>产品</span><ProcessLookup v-model="form.productId" resource="products" label="产品" :disabled="!create"/></label>
   <label><span>生产工艺</span><a-select v-model:value="form.processPackageId" :disabled="!editable" show-search option-filter-prop="label" :options="processes.map(p=>({value:p.id,label:p.packageCode}))"/></label>
   <label><span>标准批量</span><a-input v-model:value="form.batchBasisQty" :disabled="!editable" inputmode="decimal"/></label>
   <label><span>批量单位</span><ProcessLookup v-model="form.unitId" resource="units" label="批量单位" :disabled="!editable"/></label>
   <label v-if="record"><span>状态</span><a-tag :color="record.status==='ACTIVE'?'green':record.status==='DRAFT'?'orange':'default'">{{prescriptionStatus[record.status]}}</a-tag></label>
   <label v-if="record?.effectiveFrom"><span>生效时间</span><span class="read-value">{{record.effectiveFrom}}</span></label>
  </div>
 </a-card>

 <a-card title="配料明细" class="form-section">
  <div class="table-scroll">
   <table class="prescription-table">
    <thead><tr><th>序号</th><th>原辅料</th><th>标准用量</th><th>单位</th><th>超量 %</th><th>关键物料</th><th v-if="editable">操作</th></tr></thead>
    <tbody>
     <tr v-for="(item,index) in form.items" :key="item.id??item.lineNo">
      <td>{{item.lineNo}}</td>
      <td><ProcessLookup v-model="item.materialId" resource="materials" :label="'物料 '+item.lineNo" :disabled="!editable"/></td>
      <td><a-input v-model:value="item.requiredQty" :disabled="!editable" inputmode="decimal"/></td>
      <td><ProcessLookup v-model="item.unitId" resource="units" :label="'物料单位 '+item.lineNo" :disabled="!editable"/></td>
      <td><a-input v-model:value="item.overagePct" :disabled="!editable" inputmode="decimal" placeholder="0"/></td>
      <td><a-checkbox v-model:checked="item.critical" :disabled="!editable"/></td>
      <td v-if="editable"><a-button danger type="link" @click="removeItem(index)">移除</a-button></td>
     </tr>
    </tbody>
   </table>
  </div>
  <a-empty v-if="!form.items.length" description="尚未维护配料明细"/>
  <a-button v-if="editable" @click="addItem">＋ 添加原辅料</a-button>
 </a-card>

 <a-card v-if="record" title="受控操作" class="form-section">
  <div class="prescription-actions">
   <label><span>操作原因</span><a-input v-model:value="reason" maxlength="1000" placeholder="请输入修改、启用或停用原因"/></label>
   <a-space>
    <a-button v-if="record.status==='DRAFT'&&auth.can('production:prescription:activate')" type="primary" :loading="busy" @click="transition('activate')">设为在用</a-button>
    <a-button v-if="record.status==='ACTIVE'&&auth.can('production:prescription:activate')" danger :loading="busy" @click="transition('deactivate')">停用处方</a-button>
   </a-space>
  </div>
 </a-card>

 <a-alert v-if="selectedProcess" type="info" show-icon :message="'当前处方采用生产工艺：'+selectedProcess.packageCode+'。工序和工序表单由生产工艺统一维护。'"/>
</main>
</template>

<style scoped>
.prescription-detail{min-width:0}
.prescription-form{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px 26px}
.prescription-form label,.prescription-actions label{display:flex;align-items:center;gap:10px;min-width:0}
.prescription-form label>span:first-child,.prescription-actions label>span:first-child{flex:0 0 100px;color:var(--mes-ui-secondary);font-size:13px;white-space:nowrap}
.prescription-form :deep(.ant-input),.prescription-form :deep(.ant-select),.prescription-form :deep(.process-lookup),.prescription-actions :deep(.ant-input){flex:1;min-width:0}
.read-value{color:var(--mes-ui-text)}
.prescription-table{width:100%;border-collapse:collapse;min-width:850px}
.prescription-table th,.prescription-table td{border-bottom:1px solid var(--mes-ui-border);padding:10px 8px;text-align:left}
.prescription-table th{background:#f7f9fc;font-weight:600;font-size:13px}
.prescription-table td{font-size:13px}
.prescription-actions{display:flex;justify-content:space-between;gap:16px;align-items:center}
.prescription-actions label{flex:1}
@media(max-width:800px){.prescription-form{grid-template-columns:1fr}.prescription-actions{align-items:stretch;flex-direction:column}}
</style>
