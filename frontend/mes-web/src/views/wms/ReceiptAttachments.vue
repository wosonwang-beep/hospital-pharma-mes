<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import {api,http,idempotencyKey} from '../../api/http'
import {useControlledRequest} from '../../api/controlled'
import {useAuthStore} from '../../stores/auth'
const props=defineProps<{receiptId:string;versionNo:number}>(),emit=defineEmits<{reload:[]}>()
interface Metadata{id:string;fileName:string;mediaType:string;byteLength:number;sha256:string;uploadedBy:string;uploadedAt:string;recordVersion:number;retentionStatus:string}
interface Link{id:string;purpose:string;reason:string;attachment:Metadata}
const auth=useAuthStore(),request=useControlledRequest(),{busy,error,conflict}=request
const rows=ref<Link[]>([]),file=ref<File|null>(null),uploaded=ref<Metadata|null>(null),purpose=ref('COA'),reason=ref('')
let uploadKey=idempotencyKey()
const canLink=computed(()=>auth.can('wms:receipt:update')&&auth.can('attachment:upload'))
const labels:Record<string,string>={COA:'COA',DELIVERY_DOCUMENT:'送货资料',OTHER:'其他资料'}
async function load(){try{rows.value=await api<Link[]>({url:`/wms/receipts/${props.receiptId}/attachments`})}catch(e){await request.failure(e)}}
function select(event:Event){file.value=(event.target as HTMLInputElement).files?.[0]??null;uploaded.value=null;uploadKey=idempotencyKey()}
async function save(){busy.value=true;error.value='';try{
 if(!reason.value.trim())throw Error('请填写关联原因')
 if(!uploaded.value){if(!file.value)throw Error('请选择随货文件');if(file.value.size<1||file.value.size>10485760)throw Error('文件大小须为 1 字节至 10 MiB');const data=new FormData();data.append('file',file.value);uploaded.value=await api<Metadata>({url:'/attachments',method:'POST',data,headers:{'Idempotency-Key':uploadKey}})}
 await request.mutate(`/wms/receipts/${props.receiptId}/attachments`,'POST',{versionNo:props.versionNo,attachmentId:uploaded.value.id,purpose:purpose.value,reason:reason.value},props.versionNo)
 uploaded.value=null;file.value=null;reason.value='';uploadKey=idempotencyKey();await load()
 }catch(e){await request.failure(e)}finally{busy.value=false}}
async function download(row:Link){try{const response=await http.get<Blob>(`/wms/receipts/${props.receiptId}/attachments/${row.attachment.id}/content`,{responseType:'blob'});const url=URL.createObjectURL(response.data);const a=document.createElement('a');a.href=url;a.download=row.attachment.fileName;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000)}catch(e){await request.failure(e)}}
watch(()=>props.receiptId,()=>{uploaded.value=null;file.value=null;reason.value='';request.clear();void load()},{immediate:true})
watch(()=>props.versionNo,()=>request.clear())
</script>
<template><a-card title="随货资料" class="form-section">
 <a-alert v-if="error" :message="error" type="error" show-icon/>
 <a-alert v-if="conflict" message="收货记录已变化，已上传文件和关联输入已保留。请重新加载收货记录后重试关联。" type="warning"><template #action><a-button @click="emit('reload')">重新加载</a-button></template></a-alert>
 <div class="attachment-table"><table><thead><tr><th>文件名</th><th>用途</th><th>大小</th><th>上传人</th><th>上传时间</th><th>SHA256</th><th>操作</th></tr></thead><tbody><tr v-for="row in rows" :key="row.id"><td>{{row.attachment.fileName}}</td><td>{{labels[row.purpose]}}</td><td>{{row.attachment.byteLength}} B</td><td>{{row.attachment.uploadedBy}}</td><td>{{row.attachment.uploadedAt}}</td><td class="hash">{{row.attachment.sha256}}</td><td><a-button type="link" @click="download(row)">下载</a-button></td></tr><tr v-if="!rows.length"><td colspan="7">尚无随货资料</td></tr></tbody></table></div>
 <form v-if="canLink" @submit.prevent="save"><div class="master-form">
 <label><span class="form-field-label">随货文件</span><input type="file" aria-label="随货文件" :disabled="busy||!!uploaded" @change="select"/></label>
 <label><span class="form-field-label">资料用途</span><select v-model="purpose" class="master-native-input" aria-label="资料用途" :disabled="busy"><option v-for="(label,key) in labels" :key="key" :value="key">{{label}}</option></select></label>
 <label class="form-full-row"><span class="form-field-label">关联原因</span><textarea v-model="reason" class="master-native-input" aria-label="关联原因" maxlength="1000" :disabled="busy" required/></label>
 </div><p v-if="uploaded">文件 {{uploaded.fileName}} 已上传，待关联。重试将使用该文件。</p><a-button type="primary" html-type="submit" :loading="busy" :disabled="conflict">{{uploaded?'重试关联':'上传并关联'}}</a-button></form>
</a-card></template>
<style scoped>.attachment-table{overflow-x:auto;margin-bottom:16px}table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:10px 12px;border-bottom:1px solid #e8e8e8;white-space:nowrap}.hash{max-width:240px;overflow-wrap:anywhere;white-space:normal;font-family:monospace}</style>
