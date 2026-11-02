<script setup lang="ts">
import {computed,nextTick,reactive,ref,watch} from 'vue'
import {useRoute,useRouter,onBeforeRouteLeave} from 'vue-router'
import axios from 'axios'
import {errorMessage} from '../../api/http'
import {qcApi,qcBasisLabel,qcCommandHeaders,qcCreatePayload,qcItemPayload,qcStatuses,type QcDetail,type QcItem,type QcScope,type QcSubjectType} from '../../api/qcSpecifications'
import {useAuthStore} from '../../stores/auth'
import DictionarySelect from '../../components/DictionarySelect.vue'
import QcScopePicker from './QcScopePicker.vue'
import QcSpecificationItems from './QcSpecificationItems.vue'
const route=useRoute(),router=useRouter(),auth=useAuthStore()
const create=computed(()=>route.meta.mode==='create'||route.path.endsWith('/create'))
type Block='basic'|'scope'|'items'
const detail=ref<QcDetail|null>(null),busy=ref(false),error=ref(''),conflictState=ref(false),confirmOpen=ref(false),tab=ref<'basic'|'scope'>('basic'),loaded=ref('')
const form=reactive({specificationCode:'',specificationName:'',basisType:null as string|null,basisReference:'',effectiveDate:'',remark:'',status:'ACTIVE' as 'ACTIVE'|'INACTIVE',materialIds:[] as string[],productIds:[] as string[],reason:''})
const items=ref<QcItem[]>([]),requests=qcCommandHeaders()
const original=ref('')
const dirty=computed(()=>JSON.stringify(snapshot())!==original.value)
const seededMaterials=computed(()=>scopesOf('MATERIAL').map(s=>({id:s.subjectId,name:s.subjectName??`#${s.subjectId}`,code:s.subjectCode??''})))
const seededProducts=computed(()=>scopesOf('PRODUCT').map(s=>({id:s.subjectId,name:s.subjectName??`#${s.subjectId}`,code:s.subjectCode??''})))
function scopesOf(type:QcSubjectType):QcScope[]{return detail.value?.scopes.filter(s=>s.subjectType===type)??[]}
function snapshot(){return {specificationCode:form.specificationCode,specificationName:form.specificationName,basisType:form.basisType,basisReference:form.basisReference,effectiveDate:form.effectiveDate,remark:form.remark,status:form.status,materialIds:[...form.materialIds].sort(),productIds:[...form.productIds].sort(),items:items.value.map(i=>({itemCode:i.itemCode,itemName:i.itemName,required:i.required,resultType:i.resultType,lowerLimit:i.lowerLimit,upperLimit:i.upperLimit,unitId:i.unitId,textAcceptanceCriteria:i.textAcceptanceCriteria,methodCode:i.methodCode,methodVersion:i.methodVersion}))}}
function fill(value:QcDetail){form.specificationCode=value.specificationCode;form.specificationName=value.specificationName;form.basisType=value.basisType||null;form.basisReference=value.basisReference??'';form.effectiveDate=value.effectiveDate??'';form.remark=value.remark??'';form.status=value.status;form.materialIds=scopesOf('MATERIAL').map(s=>s.subjectId);form.productIds=scopesOf('PRODUCT').map(s=>s.subjectId);items.value=value.items.map(i=>({...i,lowerLimit:i.lowerLimit,upperLimit:i.upperLimit}));original.value=JSON.stringify(snapshot())}
function failure(e:unknown){error.value=e instanceof Error&&!axios.isAxiosError(e)?e.message:errorMessage(e);conflictState.value=axios.isAxiosError(e)&&e.response?.status===409}
/** 校验失败时定位到出错区块：基本信息/适用范围切到对应页签，检验项目常显只滚动。 */
function pointTo(block:Block){
  if(block==='basic'||block==='scope')tab.value=block
  void nextTick(()=>{
    const element=document.querySelector<HTMLElement>(`.qc-block-${block}`)
    if(!element)return
    element.scrollIntoView({behavior:'smooth',block:'center'})
    element.classList.add('qc-block-error')
    window.setTimeout(()=>element.classList.remove('qc-block-error'),1600)
  })
}
function localIssue():{message:string;block:Block}|null{
  if(!form.specificationCode.trim())return {message:'请填写标准编码',block:'basic'}
  if(!form.specificationName.trim())return {message:'请填写标准名称',block:'basic'}
  if(!form.basisType)return {message:'请选择标准的依据',block:'basic'}
  if(!form.basisReference.trim())return {message:'请填写依据说明',block:'basic'}
  if(!form.materialIds.length&&!form.productIds.length)return {message:'请至少添加一个适用物料或适用产品',block:'scope'}
  try{qcItemPayload(items.value)}catch(e){return {message:e instanceof Error?e.message:'检验项目不完整',block:'items'}}
  return null
}
async function load(){error.value='';conflictState.value=false;if(create.value){original.value=JSON.stringify(snapshot());loaded.value='create';return}busy.value=true;try{detail.value=await qcApi.detail(String(route.params.id));fill(detail.value);loaded.value=detail.value.id}catch(e){failure(e)}finally{busy.value=false}}
/** 保存入口：先本地校验，编辑态再要求填写变更原因。 */
function save(){error.value='';conflictState.value=false;const issue=localIssue();if(issue){error.value=issue.message;pointTo(issue.block);return}if(create.value){void submit();return}if(!dirty.value){error.value='没有需要保存的修改';return}confirmOpen.value=true}
async function submit(){error.value='';if(!create.value&&!form.reason.trim()){error.value='请填写维护原因';return}confirmOpen.value=false;conflictState.value=false;busy.value=true;try{const reason=form.reason.trim()||'新增标准';if(create.value){const body=qcCreatePayload({...form,basisType:form.basisType??'',effectiveDate:form.effectiveDate,items:items.value,reason});const created=await qcApi.create(body,requests.forRequest('/quality/specifications',body));requests.complete();original.value=JSON.stringify(snapshot());await router.replace(`/quality/specifications/${created.id}`);return}
 const current=detail.value;if(!current)throw Error('标准尚未加载完成');const token=current.versionNo;let latest=current
 const headerChanged=form.specificationCode!==current.specificationCode||form.specificationName!==current.specificationName||(form.basisType??'')!==(current.basisType??'')||form.basisReference!==(current.basisReference??'')||form.effectiveDate!==(current.effectiveDate??'')||form.remark!==(current.remark??'')||form.status!==current.status
 const scopeChanged=JSON.stringify([...form.materialIds].sort())!==JSON.stringify(scopesOf('MATERIAL').map(s=>s.subjectId).sort())||JSON.stringify([...form.productIds].sort())!==JSON.stringify(scopesOf('PRODUCT').map(s=>s.subjectId).sort())
 const itemsChanged=JSON.stringify(snapshot().items)!==JSON.stringify(snapshotOf(current.items))
 if(!headerChanged&&!scopeChanged&&!itemsChanged){error.value='没有需要保存的修改';return}
 if(headerChanged){const url=`/quality/specifications/${current.id}`;const body={recordVersion:token,specificationCode:form.specificationCode.trim(),specificationName:form.specificationName.trim(),basisType:form.basisType,basisReference:form.basisReference.trim(),effectiveDate:form.effectiveDate||null,remark:form.remark.trim()||null,status:form.status,reason};latest=await qcApi.edit(current.id,body,requests.forRequest(url,body,token))}
 if(scopeChanged){const url=`/quality/specifications/${current.id}/scope`;const body={recordVersion:latest.versionNo,materialIds:form.materialIds,productIds:form.productIds,reason};latest=await qcApi.replaceScope(current.id,body,requests.forRequest(url,body,latest.versionNo))}
 if(itemsChanged){const url=`/quality/specifications/${current.id}/items`;const body={recordVersion:latest.versionNo,items:qcItemPayload(items.value),reason};latest=await qcApi.replaceItems(current.id,body,requests.forRequest(url,body,latest.versionNo))}
 requests.complete();detail.value=latest;fill(latest);form.reason=''}catch(e){failure(e)}finally{busy.value=false}}
function snapshotOf(list:QcItem[]){return list.map(i=>({itemCode:i.itemCode,itemName:i.itemName,required:i.required,resultType:i.resultType,lowerLimit:i.lowerLimit,upperLimit:i.upperLimit,unitId:i.unitId,textAcceptanceCriteria:i.textAcceptanceCriteria,methodCode:i.methodCode,methodVersion:i.methodVersion}))}
onBeforeRouteLeave(()=>!dirty.value||window.confirm('离开将放弃未保存内容，是否继续？'))
watch(()=>route.fullPath,load,{immediate:true})
</script>
<template>
<main data-ui-template="T2" class="admin-page master-page qc-editor">
  <header class="admin-page-header">
    <div>
      <a-button type="link" @click="router.push('/quality/specifications')">← 返回标准列表</a-button>
      <h1>{{create?'新增QC质量标准':(detail?.specificationName??'QC质量标准')}}</h1>
      <p v-if="detail">{{detail.specificationCode}} · {{qcBasisLabel(detail.basisType)}} · {{qcStatuses[detail.status]??detail.status}}</p>
    </div>
    <a-space>
      <span v-if="dirty" class="qc-dirty"><i class="qc-dot"/>有未保存的修改</span>
      <a-button type="primary" :loading="busy" :disabled="!auth.can(create?'qms:specification:create':'qms:specification:edit')" @click="save">保存标准</a-button>
    </a-space>
  </header>
  <a-alert v-if="error" type="error" :message="error" show-icon/>
  <a-alert v-if="conflictState" type="warning" message="该标准已被他人修改，请重新加载后再保存" show-icon/>

  <a-tabs v-model:active-key="tab" class="qc-tabs">
  <a-tab-pane key="basic" tab="基本信息">
  <a-card class="form-section qc-block-basic">
    <form class="master-form" @submit.prevent="save">
      <label><span class="form-field-label">标准编码</span><input v-model="form.specificationCode" class="master-native-input" aria-label="标准编码" maxlength="100" :disabled="busy" required/></label>
      <label><span class="form-field-label">标准名称</span><input v-model="form.specificationName" class="master-native-input" aria-label="标准名称" maxlength="200" :disabled="busy" required/></label>
      <label><span class="form-field-label">标准的依据</span><DictionarySelect v-model="form.basisType" dict-code="QC_STANDARD_BASIS" placeholder="请选择标准的依据" :disabled="busy" required/></label>
      <label><span class="form-field-label">依据说明</span><input v-model="form.basisReference" class="master-native-input" aria-label="依据说明" maxlength="300" placeholder="如《中国药典》2025年版四部 0301" :disabled="busy" required/></label>
      <label><span class="form-field-label">生效日期</span><input v-model="form.effectiveDate" type="date" class="master-native-input" aria-label="生效日期" :disabled="busy"/></label>
      <label><span class="form-field-label">状态</span><a-select v-model:value="form.status" aria-label="状态" :disabled="busy"><a-select-option v-for="(label,state) in qcStatuses" :key="state" :value="state">{{label}}</a-select-option></a-select></label>
      <label class="form-full-row"><span class="form-field-label">备注</span><textarea v-model="form.remark" class="master-native-input" aria-label="备注" maxlength="1000" :disabled="busy"/></label>
    </form>
  </a-card>
  </a-tab-pane>

  <a-tab-pane key="scope" tab="适用范围">
  <a-card class="form-section qc-block-scope">
    <p class="qc-hint">一个标准可以同时适用于多个物料和多个产品；下游请验时按物料或产品匹配。</p>
    <div class="qc-scope-grid">
      <section class="qc-scope-section"><h3>适用物料</h3><QcScopePicker v-model="form.materialIds" resource="materials" label="适用物料" :disabled="busy" :seeded="seededMaterials"/></section>
      <section class="qc-scope-section"><h3>适用产品</h3><QcScopePicker v-model="form.productIds" resource="products" label="适用产品" :disabled="busy" :seeded="seededProducts"/></section>
    </div>
  </a-card>
  </a-tab-pane>
  </a-tabs>

  <QcSpecificationItems class="qc-block-items" v-model="items" :disabled="busy"/>

  <a-modal :open="confirmOpen" title="确认保存标准" ok-text="确认保存" :confirm-loading="busy" :mask-closable="false" @ok="submit" @cancel="confirmOpen=false">
    <p class="qc-confirm-tip">保存后本次改动会记入维护记录，请说明变更原因。</p>
    <label class="master-form"><span class="form-field-label">维护原因</span><textarea v-model="form.reason" class="master-native-input" aria-label="维护原因" maxlength="500" placeholder="如：按2025年版药典修订含量测定限度"/></label>
  </a-modal>
</main>
</template>
<style scoped>
.qc-editor{padding-bottom:24px}
:deep(.process-lookup){display:block;min-width:0;width:100%}
:deep(.ant-select){width:100%}
.qc-hint{margin:0 0 14px;color:#667085}
.qc-scope-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px 28px}
.qc-scope-section{min-width:0;border:1px solid #ebeef5;border-radius:4px;padding:14px 16px 4px;background:#fbfcfe}
.qc-scope-section h3{margin:0 0 12px;font-size:14px;font-weight:600;color:#303133}
.qc-block-error{outline:2px solid #f56c6c;outline-offset:2px;transition:outline-color .2s}
.qc-dirty{display:inline-flex;align-items:center;gap:6px;font-size:13px;color:#b26a00;white-space:nowrap}
.qc-dot{width:7px;height:7px;border-radius:50%;background:#e6a23c;display:inline-block}
.qc-confirm-tip{margin:0 0 14px;color:#606266}
.qc-tabs{margin-top:4px}
.qc-block-items{margin-top:16px}
.master-read-value{overflow-wrap:anywhere}
@media(max-width:900px){.qc-scope-grid{grid-template-columns:1fr}}
</style>
