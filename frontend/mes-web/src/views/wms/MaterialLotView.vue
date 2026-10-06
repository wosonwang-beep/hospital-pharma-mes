<script setup lang="ts">
import {computed,ref,onMounted} from 'vue'
import {CodeSandboxOutlined} from '@ant-design/icons-vue'
import IncomingFacts from '../quality/IncomingFacts.vue'
import {materialFields} from '../../master/material'
import {detailLabel,selectFacts} from '../quality/incomingDetailModel'
import {useRoute,useRouter} from 'vue-router'
import {api,type Page} from '../../api/http'
import {useControlledRequest} from '../../api/controlled'
import {useAuthStore} from '../../stores/auth'
import {statusLabels} from './model'
import type {MaterialLot,Inventory} from './types'
import WmsLookup from './WmsLookup.vue'
import ReadReference from '../master/ReadReference.vue'
import MaterialLotLineage from './MaterialLotLineage.vue'
import FinishedLotChain from '../finished/FinishedLotChain.vue'
import FunctionalQualityActions from '../quality/FunctionalQualityActions.vue'
import ProcessLookup from '../process/ProcessLookup.vue'
function materialLabel(key:string){return key==='baseUnitName'?'基本单位':materialFields.find(f=>f.key===key)?.label??detailLabel(key)}
function materialValue(key:string,value:unknown){return key==='baseUnitId'?String(record.value?.materialSnapshot?.baseUnitName??value):String(value??'—')}
const route=useRoute(),router=useRouter(),auth=useAuthStore(),request=useControlledRequest(),{error,conflict,busy}=request,record=ref<MaterialLot|null>(null),receiving=ref<Record<string,unknown>>({}),stock=ref<Page<Inventory>>({items:[],total:0,page:0,size:20}),operationOpen=ref(false),mode=ref<'MOVE'|'ADJUST'>('MOVE'),locationId=ref(''),containerId=ref(''),toLocationId=ref(''),toContainerId=ref(''),unitId=ref(''),quantity=ref(''),reason=ref(''),notice=ref('')
async function load(page=0){request.clear();receiving.value={};try{record.value=await api({url:`/wms/material-lots/${route.params.id}`});stock.value=await api({url:'/inventory',params:{materialLotId:route.params.id,page,size:20}})}catch(e){await request.failure(e)}}
function can(action:string){return !!record.value?.allowedActions?.includes(action)&&auth.can(`wms:inventory:${action.toLowerCase()}`)}
function selectStock(row:Inventory,action:'MOVE'|'ADJUST'){operationOpen.value=true;mode.value=action;locationId.value=row.locationId??'';containerId.value=row.containerId??'';unitId.value=row.unitId??'';quantity.value='';notice.value=''}
async function save(){busy.value=true;error.value='';notice.value='';try{if(!reason.value.trim())throw Error('请填写操作原因');const r=record.value!;const body={materialLotId:r.id,versionNo:r.versionNo,unitId:unitId.value,reason:reason.value,...(mode.value==='MOVE'?{fromLocationId:locationId.value,...(containerId.value?{fromContainerId:containerId.value}:{}),toLocationId:toLocationId.value,...(toContainerId.value?{toContainerId:toContainerId.value}:{}),quantity:quantity.value}:{locationId:locationId.value,...(containerId.value?{containerId:containerId.value}:{}),deltaQty:quantity.value})};await request.mutate(`/inventory/${mode.value.toLowerCase()}`,'POST',body,r.versionNo);await load();notice.value=mode.value==='MOVE'?'移库已记录':'库存调整已记录';quantity.value='';reason.value=''}catch(e){await request.failure(e)}finally{busy.value=false}}
const receiptItem=computed(()=>{const items=receiving.value.items;return Array.isArray(items)?items.find((item:Record<string,unknown>)=>String(item.id)===record.value?.receiptItemId) as Record<string,unknown>|undefined:undefined})
const isFinished=computed(()=>record.value?.materialSnapshot?.materialType==='FINISHED')
function useReceiving(value:Record<string,unknown>){if(Array.isArray(value.items)&&value.items.some((item:Record<string,unknown>)=>String(item.id)===record.value?.receiptItemId))receiving.value=value}
onMounted(()=>load())
</script>
<!-- UI Template: T4 Aggregate Detail; lineage is read-only from existing trace source. -->
<template><main class="admin-page master-page lot-page" data-ui-template="T4"><header class="admin-page-header"><div><a-button type="link" @click="router.back()">← 返回</a-button><h1><CodeSandboxOutlined/> 物料批次 360° 视图</h1><p>从收货到放行的全生命周期追溯</p></div><a-space wrap><a-button v-if="record&&!isFinished&&auth.can('qa:material-release:view')" @click="router.push(`/qa/material-lots/${record.id}/review`)">物料放行审查</a-button><a-button v-if="record&&auth.can('trace:view')" @click="router.push({path:'/trace',query:{materialLotId:record.id}})">完整追溯</a-button><a-button v-if="record&&auth.can('audit:view')" @click="router.push({path:'/audit',query:{objectType:'MaterialLot',objectId:record.id}})">审计追踪</a-button></a-space></header><a-alert v-if="error" type="error" :message="error"/><a-alert v-if="conflict" type="warning" message="记录已变化，输入已保留，请重新加载后继续。"><template #action><a-button @click="load()">重新加载</a-button></template></a-alert><a-alert v-if="notice" type="success" :message="notice"/>
<a-card v-if="record" class="form-section lot-summary"><div class="lot-summary-grid"><div class="lot-identity"><CodeSandboxOutlined/><div><strong>{{record.materialSnapshot?.materialName??'物料批次'}}</strong><a-tag :color="record.qualityStatus==='RELEASED'?'green':record.qualityStatus==='REJECTED'?'red':'blue'">{{statusLabels[record.qualityStatus??'']??record.qualityStatus}}</a-tag><p>批号：{{record.lotNo}}</p></div></div><section><small>物料编码</small><strong>{{record.materialSnapshot?.materialCode??record.materialId??'—'}}</strong></section><section><small>规格</small><strong>{{record.materialSnapshot?.specification??record.materialSnapshot?.packSpec??'—'}}</strong></section><section><small>批次数量</small><strong v-if="receiptItem">{{receiptItem.receivedQty}} <span v-if="String(receiptItem.unitId)===String(record.materialSnapshot?.baseUnitId)&&record.materialSnapshot?.baseUnitName">{{record.materialSnapshot.baseUnitName}}</span><ReadReference v-else field="unitId" :value="String(receiptItem.unitId??'')"/></strong><strong v-else>—</strong></section><section><small>收货日期</small><strong>{{receiving.receivedAt?String(receiving.receivedAt).slice(0,10):'—'}}</strong></section><section><small>有效期至</small><strong>{{record.expiryDate??'—'}}</strong></section><section><small>供应商</small><strong><span v-if="record.sourceSnapshot?.supplierName">{{record.sourceSnapshot.supplierName}}</span><ReadReference v-else-if="receiving.supplierId" field="supplierId" :value="String(receiving.supplierId)"/><span v-else>—</span></strong></section></div><div class="master-form"><label><span class="form-field-label">生产厂家</span><span class="master-read-value">{{record.sourceSnapshot?.manufacturerName||'未记录'}}</span></label></div><div id="lot-quality-timeline"/></a-card>
<a-card v-if="record" class="form-section lot-tabs"><a-tabs><a-tab-pane key="overview" tab="全流程"><FinishedLotChain v-if="isFinished" :lot-id="record.id!"/><MaterialLotLineage v-else :lot-id="record.id!" :types="[]" overview timeline-target="#lot-quality-timeline" @receiving="useReceiving"/><div class="lot-fact-panels"><a-card title="基本信息"><IncomingFacts :value="selectFacts(record.materialSnapshot??{},['materialCode','materialName','materialType','specification','packSpec','baseUnitId'])" :label-formatter="materialLabel" :value-formatter="materialValue"/></a-card><a-card title="批次信息"><div class="master-form"><label v-for="[key,label] in [['lotNo','物料批号'],['supplierLotNo','供应商批号'],['manufactureDate','生产日期'],['expiryDate','有效期至'],['retestDate','复验日期']]" :key="key"><span class="form-field-label">{{label}}</span><span class="master-read-value">{{(record as any)[key!]||'—'}}</span></label><label><span class="form-field-label">质量状态</span><span class="master-read-value">{{statusLabels[record.qualityStatus??'']||record.qualityStatus}}</span></label><label><span class="form-field-label">库存状态</span><span class="master-read-value">{{statusLabels[record.inventoryStatus??'']||record.inventoryStatus}}</span></label><label><span class="form-field-label">入库必验快照</span><span class="master-read-value">{{record.requiresIncomingInspectionSnapshot==null?'—':record.requiresIncomingInspectionSnapshot?'是':'否'}}</span></label></div></a-card><a-card title="物料图片" class="lot-image-panel"><p>现有物料档案未提供图片</p></a-card></div><a-alert type="info" message="生产使用资格由物料质量放行、库存状态与冻结、有效期/复验规则及生产使用场景共同控制，以服务端资格校验为准。"/></a-tab-pane>
<a-tab-pane key="receiving" tab="收货信息"><FinishedLotChain v-if="isFinished" :lot-id="record.id!"/><MaterialLotLineage v-else :lot-id="record.id!" :types="['RECEIPT','RECEIPT_ITEM']"/></a-tab-pane>
<a-tab-pane key="quality" tab="质量信息"><FinishedLotChain v-if="isFinished" :lot-id="record.id!"/><MaterialLotLineage v-else :lot-id="record.id!" :types="['INSPECTION_REQUEST','SAMPLING_TASK','SAMPLING_DETAIL','SAMPLE','INSPECTION_TASK','INSPECTION_ITEM','TEST_EXECUTION','RESULT_REVISION','REPORT','RELEASE_DECISION','INVESTIGATION']"/></a-tab-pane>
<a-tab-pane key="stock" tab="库存流转"><div class="table-scroll"><table><thead><tr><th>库位</th><th>容器</th><th>数量</th><th>单位</th><th>操作</th></tr></thead><tbody><tr v-for="(row,i) in stock.items" :key="i"><td><ReadReference field="locationId" :value="row.locationId"/></td><td><ReadReference field="containerId" :value="row.containerId"/></td><td>{{row.quantity}}</td><td><ReadReference field="unitId" :value="row.unitId"/></td><td><a-button v-if="can('MOVE')" type="link" aria-label="移库" @click="selectStock(row,'MOVE')">移库</a-button><a-button v-if="can('ADJUST')" type="link" aria-label="调整" @click="selectStock(row,'ADJUST')">调整</a-button></td></tr></tbody></table></div><a-empty v-if="!stock.total" description="暂无库存记录"/><a-button v-if="can('ADJUST')" @click="operationOpen=true;mode='ADJUST';locationId='';containerId='';unitId=String(record?.materialSnapshot?.baseUnitId??'');quantity=''">新增库存调整</a-button><div class="table-footer"><span>共 {{stock.total}} 条</span><a-pagination :current="stock.page+1" :page-size="stock.size" :total="stock.total" :show-size-changer="false" @change="(p:number)=>load(p-1)"/></div>
<form v-if="operationOpen&&can(mode)" class="master-form lot-operation" @submit.prevent="save"><label><span class="form-field-label">操作</span><span class="master-read-value">{{mode==='MOVE'?'移库':'库存调整'}}</span></label><label><span class="form-field-label">来源库位</span><WmsLookup v-model="locationId" resource="locations" label="来源库位" :disabled="mode==='MOVE'"/></label><label v-if="mode==='ADJUST'"><span class="form-field-label">容器</span><WmsLookup v-model="containerId" resource="containers" label="调整容器"/></label><label v-if="mode==='MOVE'"><span class="form-field-label">目标库位</span><WmsLookup v-model="toLocationId" resource="locations" label="目标库位"/></label><label v-if="mode==='MOVE'"><span class="form-field-label">目标容器</span><WmsLookup v-model="toContainerId" resource="containers" label="目标容器"/></label><label><span class="form-field-label">{{mode==='MOVE'?'移动数量':'调整差额'}}</span><input v-model="quantity" aria-label="库存数量" class="master-native-input" required/></label><label><span class="form-field-label">单位</span><ProcessLookup v-model="unitId" resource="units" label="库存单位"/></label><label class="form-full-row"><span class="form-field-label">操作原因</span><textarea v-model="reason" aria-label="操作原因" class="master-native-input" required/></label><a-space><a-button type="primary" html-type="submit" :disabled="busy||conflict">提交{{mode==='MOVE'?'移库':'调整'}}</a-button><a-button @click="operationOpen=false">取消</a-button></a-space></form>
<FunctionalQualityActions :row="record" kind="lot" @reload="load()"/></a-tab-pane>
<a-tab-pane key="production" tab="使用记录"><MaterialLotLineage :lot-id="record.id!" :types="['OPERATION','CHARGE','WEIGHING']"/></a-tab-pane>
<a-tab-pane key="related-production" tab="相关生产"><MaterialLotLineage :lot-id="record.id!" :types="['MAIN_BATCH','EXECUTION_UNIT']"/></a-tab-pane>
<a-tab-pane key="audit" tab="相关文件"><a-space wrap><a-button v-if="auth.can('trace:view')" @click="router.push({path:'/trace',query:{materialLotId:record.id}})">完整追溯</a-button><a-button v-if="auth.can('audit:view')" @click="router.push({path:'/audit',query:{objectType:'MaterialLot',objectId:record.id}})">审计追踪</a-button></a-space><MaterialLotLineage :lot-id="record.id!" :types="['SIGNATURE','INVENTORY_DECISION']"/></a-tab-pane>
</a-tabs></a-card></main></template>

<style scoped>
#app .lot-page>.admin-page-header{background:transparent;border:0;padding:0 0 8px;box-shadow:none}
.lot-page h1>.anticon{color:#1677ff;background:#e9f2ff;border-radius:8px;padding:12px;font-size:26px;margin-right:12px}
.lot-summary-grid{display:grid;grid-template-columns:1.8fr repeat(6,minmax(0,1fr));gap:12px;align-items:center}
.lot-summary-grid strong{display:block;font-size:13px;overflow-wrap:anywhere}
.lot-summary-grid small{display:block;font-size:12px;color:#66758a;margin-bottom:8px}
.lot-identity{display:flex;gap:12px;align-items:center}
.lot-identity>.anticon{padding:12px;background:#e9f2ff;color:#1677ff;font-size:26px;border-radius:8px;flex:0 0 auto}
.lot-identity strong{display:inline;font-size:18px;margin-right:8px}.lot-identity p{margin:8px 0 0;font-size:13px}
.lot-summary :deep(.ant-tag){border:0}.lot-fact-panels{display:grid;grid-template-columns:1fr 1.1fr .8fr;gap:12px;margin:12px 0;align-items:stretch}
#app .lot-page .lot-fact-panels :deep(.master-form){grid-template-columns:1fr;gap:4px}
#app .lot-page .lot-fact-panels :deep(.master-form>label){grid-template-columns:86px minmax(0,1fr);gap:8px}
#app .lot-page .lot-fact-panels :deep(.master-read-value){padding:0;font-size:13px;line-height:22px}
#app .lot-page .lot-fact-panels :deep(.form-field-label){padding:0;font-size:13px;line-height:22px;text-align:left}
.lot-image-panel p{color:#66758a;font-size:12px;margin:0}.lot-operation{margin-top:20px}.lot-page :deep(.process-lookup .ant-select){width:100%}
@media(min-width:1000px){
#app .lot-page>.admin-page-header{min-height:0;align-items:center;margin-bottom:12px}
#app .lot-page .admin-page-header>div:first-child{display:block}
#app .lot-page .admin-page-header h1{display:flex;align-items:center;margin:4px 0 2px}
#app .lot-page .admin-page-header p{margin:0 0 0 62px}
#app .lot-page .admin-page-header>div:first-child>.ant-btn{height:28px;min-height:28px;padding:0 8px}
#app .lot-page .lot-summary>.ant-card-body{padding:16px}
#app .lot-page .lot-tabs>.ant-card-body{padding:0 8px 8px}
.lot-tabs :deep(.ant-tabs-nav){margin-bottom:12px}.lot-tabs :deep(.ant-tabs-tab){padding:12px 8px}.lot-tabs :deep(.ant-tabs-tab+.ant-tabs-tab){margin-left:12px}
#app .lot-page .lot-fact-panels .ant-card-head{min-height:40px;padding:0 12px}
#app .lot-page .lot-fact-panels .ant-card-body{padding:12px}
}
@media(max-width:999px){.lot-summary-grid{grid-template-columns:repeat(3,minmax(0,1fr))}.lot-identity{grid-column:1/-1}.lot-fact-panels{grid-template-columns:1fr 1fr}}
@media(max-width:767px){.lot-summary-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.lot-fact-panels{grid-template-columns:minmax(0,1fr)}}
</style>
