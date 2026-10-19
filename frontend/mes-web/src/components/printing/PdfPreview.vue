<script setup lang="ts">
import {defineAsyncComponent,onUnmounted,ref,watch} from 'vue'
import {downloadBlob} from '../../api/printing'
const PdfDocumentPreview=defineAsyncComponent(()=>import('./PdfDocumentPreview.vue'))
const props=defineProps<{blob:Blob|null;name:string}>(),url=ref('')
watch(()=>props.blob,b=>{if(url.value)URL.revokeObjectURL(url.value);url.value=b?URL.createObjectURL(b):''},{immediate:true})
onUnmounted(()=>{if(url.value)URL.revokeObjectURL(url.value)})
function print(){const w=window.open(url.value,'_blank','noopener');if(!w)return}
</script>
<template><div class="pdf-tools"><a-button :disabled="!blob" @click="blob&&downloadBlob(blob,name)">下载 PDF</a-button><a-button :disabled="!blob" @click="print">打开 PDF / 打印</a-button><span>预览、下载和打印均使用同一份 PDF；在 PDF 阅读器中选择打印。</span></div><PdfDocumentPreview v-if="blob" :blob="blob"/></template>
<style scoped>.pdf-tools{display:flex;gap:12px;align-items:center;flex-wrap:wrap;margin-bottom:12px}.pdf-tools span{color:#68758a;font-size:12px}.pdf-tools :deep(.ant-btn){height:auto;min-height:32px;white-space:normal;line-height:1.5715;padding-block:4px}</style>
