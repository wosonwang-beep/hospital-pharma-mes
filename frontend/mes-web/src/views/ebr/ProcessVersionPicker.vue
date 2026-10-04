<script setup lang="ts">
import {ref,onMounted} from 'vue'
import {api,errorMessage,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
const emit=defineEmits<{'update:modelValue':[string]}>(),props=defineProps<{modelValue:string}>(),auth=useAuthStore(),packages=ref<{id:string;packageCode:string;productName:string}[]>([]),versions=ref<{id:string;businessVersion:number;status:string}[]>([]),packageId=ref(''),error=ref('')
async function selectPackage(){versions.value=[];emit('update:modelValue','');if(!packageId.value)return;try{versions.value=(await api<{versions:typeof versions.value}>({url:`/process-packages/${packageId.value}`})).versions}catch(e){error.value=errorMessage(e)}}
onMounted(async()=>{if(!auth.can('process:package:view')){error.value='需要工艺包查看权限以选择工艺版本';return}try{packages.value=(await api<Page<typeof packages.value[number]>>({url:'/process-packages',params:{size:100}})).items}catch(e){error.value=errorMessage(e)}})
</script>
<template><label><span class="form-field-label">工艺包</span><select v-model="packageId" aria-label="工艺包" class="master-native-input" @change="selectPackage"><option value="">请选择</option><option v-for="p in packages" :key="p.id" :value="p.id">{{p.productName}} · {{p.packageCode}}</option></select></label><label><span class="form-field-label">工艺版本</span><select :value="props.modelValue" name="packageVersionId" aria-label="工艺版本" class="master-native-input" required @change="emit('update:modelValue',($event.target as HTMLSelectElement).value)"><option value="">请选择</option><option v-for="v in versions" :key="v.id" :value="v.id">V{{v.businessVersion}} · {{v.status}}</option></select></label><p v-if="error" class="error form-full-row" role="alert">{{error}}</p></template>
