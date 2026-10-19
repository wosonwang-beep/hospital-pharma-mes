<script setup lang="ts">
import {nextTick,onBeforeUnmount,onMounted,ref,watch} from 'vue'
import type {PDFDocumentLoadingTask} from 'pdfjs-dist'
import type {PDFViewer} from 'pdfjs-dist/web/pdf_viewer.mjs'
import workerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url'
import 'pdfjs-dist/web/pdf_viewer.css'

const props=defineProps<{blob:Blob}>()
const viewport=ref<HTMLDivElement>(),pages=ref<HTMLDivElement>()
const loading=ref(true),error=ref(''),pageCount=ref(0),renderedPages=ref<number[]>([])
let generation=0,dispose=()=>{}

async function load(blob:Blob){
 const ticket=++generation
 dispose();loading.value=true;error.value='';pageCount.value=0;renderedPages.value=[]
 const abort=new AbortController()
 let task:PDFDocumentLoadingTask|undefined,viewer:PDFViewer|undefined,resize:ResizeObserver|undefined
 dispose=()=>{abort.abort();resize?.disconnect();viewer?.setDocument(null);void task?.destroy()}
 try{
  await nextTick()
  // The viewer expects the core module to initialize first. Both are local lazy chunks.
  const pdf=await import('pdfjs-dist')
  const {PDFViewer,EventBus}=await import('pdfjs-dist/web/pdf_viewer.mjs')
  if(ticket!==generation||!viewport.value||!pages.value)return
  pdf.GlobalWorkerOptions.workerSrc=workerUrl
  const eventBus=new EventBus()
  const options={container:viewport.value,viewer:pages.value,eventBus,annotationMode:pdf.AnnotationMode.DISABLE,annotationEditorMode:pdf.AnnotationEditorType.NONE,enableAutoLinking:false,enableDetailCanvas:false,maxCanvasPixels:4096*4096,abortSignal:abort.signal}
  viewer=new PDFViewer(options)
  eventBus.on('pagesinit',()=>{if(ticket===generation&&viewer)viewer.currentScaleValue='page-width'})
  eventBus.on('pagerendered',({pageNumber,error:renderError}:{pageNumber:number;error?:unknown})=>{
   if(ticket!==generation)return
   loading.value=false
   if(renderError)error.value='PDF 预览未能显示，请下载 PDF 后查看。'
   else renderedPages.value=[...new Set([...renderedPages.value,pageNumber])]
  })
  let width=viewport.value.clientWidth
  resize=new ResizeObserver(()=>{const next=viewport.value?.clientWidth;if(viewer&&next&&next!==width){width=next;viewer.currentScaleValue='page-width'}})
  resize.observe(viewport.value)
  const data=new Uint8Array(await blob.arrayBuffer())
  if(ticket!==generation)return
  task=pdf.getDocument({data,enableXfa:false,useSystemFonts:true})
  const document=await task.promise
  if(ticket!==generation)return
  pageCount.value=document.numPages
  viewer.setDocument(document)
 }catch{
  if(ticket===generation){loading.value=false;error.value='PDF 预览未能显示，请下载 PDF 后查看。'}
 }
}
onMounted(()=>watch(()=>props.blob,load,{immediate:true}))
onBeforeUnmount(()=>{++generation;dispose()})
</script>

<template>
 <p v-if="loading" role="status" aria-live="polite" class="pdf-status">正在显示 PDF…</p>
 <a-alert v-if="error" :message="error" type="error" show-icon class="pdf-status"/>
 <div class="pdf-frame" :data-page-count="pageCount" :data-rendered-pages="renderedPages.join(',')">
  <div ref="viewport" class="pdf-viewport" role="region" aria-label="PDF 文档预览" :aria-busy="loading" tabindex="0"><div ref="pages" class="pdfViewer"/></div>
 </div>
</template>

<style scoped>
.pdf-status{margin:0 0 12px;color:var(--mes-ui-secondary)}
.pdf-frame{position:relative;width:100%;height:65vh;border:1px solid var(--mes-ui-control-border);border-radius:6px;overflow:hidden;background:var(--mes-ui-background)}
.pdf-viewport{position:absolute;inset:0;overflow:auto;outline-offset:-3px}
.pdf-viewport:focus-visible{outline:2px solid var(--mes-ui-primary)}
</style>
