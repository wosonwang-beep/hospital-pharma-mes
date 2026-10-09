<script setup lang="ts">
import {ref,onMounted} from 'vue'
import {api,errorMessage,type Page} from '../../api/http'
import {useAuthStore} from '../../stores/auth'
const emit=defineEmits<{'update:modelValue':[string]}>(),props=defineProps<{modelValue?:string;required?:boolean}>(),auth=useAuthStore(),packages=ref<{id:string;packageCode:string;productName:string}[]>([]),error=ref('')
onMounted(async()=>{if(!auth.can('process:package:view')){error.value='需要生产工艺查看权限';return}try{packages.value=(await api<Page<typeof packages.value[number]>>({url:'/process-packages',params:{size:100}})).items}catch(e){error.value=errorMessage(e)}})
</script>
<template><label><span class="form-field-label">生产工艺</span><a-select show-search option-filter-prop="children" :value="props.modelValue" name="processPackageId" aria-label="生产工艺" class="master-native-input" :required="props.required!==false" @change="emit('update:modelValue',String($event??''))"><a-select-option value="">请选择</a-select-option><a-select-option v-for="p in packages" :key="p.id" :value="p.id">{{p.productName}} · {{p.packageCode}}</a-select-option></a-select></label><p v-if="error" class="error form-full-row" role="alert">{{error}}</p></template>
