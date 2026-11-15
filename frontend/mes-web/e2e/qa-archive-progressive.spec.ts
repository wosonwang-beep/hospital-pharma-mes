import {test,expect,type Page,type Route} from '@playwright/test'
import {createHash} from 'node:crypto'

// All requests in this file are mocked. No DEV batch writes or credentials.
const digest='f'.repeat(64),pdfBytes=Buffer.from('%PDF-1.7 immutable archived evidence'),hash=createHash('sha256').update(pdfBytes).digest('hex')
const wrap=(data:unknown)=>({code:'OK',message:'success',data,traceId:'qa-archive-progressive'})
const archives=()=>({mainBatchId:'655',definitionHash:digest,recordDigest:digest,batch:{productId:'2',plannedQty:'400000',unitId:'3'},forms:[],qcTests:[],balances:[],deviations:[],decisions:[{id:'690',decisionBy:'11569',decision:'RELEASED',decisionAt:'2026-11-13T00:00:00Z',supersedesDecisionId:null}],pdfManifests:[{id:'93',mainBatchId:'655',generationVersion:1,definitionHash:digest,recordDigest:digest,fileId:'133',fileHash:hash,generatedBy:'11556',generatedAt:'2026-11-13T00:00:00Z',archiveKind:'FINAL',releaseDecisionId:'690'},{id:'94',mainBatchId:'655',generationVersion:1,definitionHash:digest,recordDigest:digest,fileId:'134',fileHash:hash,generatedBy:'11556',generatedAt:'2026-11-13T01:00:00Z',archiveKind:'REVIEW_COPY',releaseDecisionId:'690'}],processSnapshot:null,operations:[],charges:[],quantityEvents:[],genealogy:[],signatures:[]})
const qa=()=>({mainBatchId:'655',versionNo:7,batchNo:'HYTEA-20261109-002',status:'QA_RELEASED',finishedLots:[],finishedSchemaVersion:2,gates:[{code:'BATCH_QA_STATE',passed:true,blockingCodes:[],evidenceIds:['655']}],blockingCodes:[],reviewDigest:digest,allowedActions:[]})
type Gate = ()=>void
async function setup(page:Page,mode:'slow-qa'|'slow-archive'|'failed-archive'|'failed-review'='slow-qa'){
 let resumeQa:Gate=()=>{},resumeArchive:Gate=()=>{},failedOnce=false
 const qaWait=new Promise<void>(resolve=>{resumeQa=resolve})
 const archiveWait=new Promise<void>(resolve=>{resumeArchive=resolve})
 await page.route('**/api/v1/**',async (route:Route)=>{
  const path=new URL(route.request().url()).pathname.replace('/api/v1','')
  const send=(data:unknown)=>route.fulfill({json:wrap(data)})
  if(path==='/auth/me')return send({userId:'11556',organizationId:'1',loginName:'archive.reader',displayName:'归档核对',permissionCodes:['qa:batch-review','ebr:form:view','ebr:pdf:generate'],mustChangePassword:false})
  if(path==='/qa/batches/655/review-model'){
   if(mode==='failed-review')return route.fulfill({status:503,json:{code:'QA_GATE_SOURCE_TIMEOUT',message:'质量门禁暂不可读取'}})
   if(mode==='slow-qa')await qaWait
   return send(qa())
  }
  if(path==='/main-batches/655/ebr/pdf/94')return send(archives().pdfManifests[1])
  if(path==='/attachments/134/content')return route.fulfill({contentType:'application/pdf',body:pdfBytes})
  if(path==='/main-batches/655/ebr'){
   if(mode==='slow-archive')await archiveWait
   if(mode==='failed-archive'&&!failedOnce){failedOnce=true;return route.fulfill({status:503,json:{code:'SERVICE_UNAVAILABLE',message:'归档服务暂不可读取'}})}
   return send(archives())
  }
  return send({items:[],total:0})
 })
 return {resumeQa,resumeArchive}
}
test('archive history renders before a slow QA-gate response',async({page})=>{
 const tasks=await setup(page,'slow-qa')
 await page.goto('/qa/batches/655/review')
 await expect(page.locator('.archive-history')).toBeVisible({timeout:10000})
 await expect(page.locator('.archive-history .ant-table-wrapper .ant-table-tbody > tr.ant-table-row')).toHaveCount(2)
 await expect(page.locator('.archive-history').getByText('最终归档',{exact:true})).toBeVisible()
 await expect(page.locator('.archive-history').getByText('审核副本',{exact:true})).toBeVisible()
 await expect(page.locator('.qa-banner')).toHaveCount(0)
 tasks.resumeQa()
 await expect(page.locator('.batch-heading').getByText('HYTEA-20261109-002',{exact:true})).toBeVisible()
 await expect(page.locator('.archive-history .ant-table-wrapper .ant-table-tbody > tr.ant-table-row')).toHaveCount(2)
})

test('archive loading status is independent of already loaded QA gates',async({page})=>{
 const tasks=await setup(page,'slow-archive')
 await page.goto('/qa/batches/655/review')
 await expect(page.locator('.batch-heading').getByText('HYTEA-20261109-002',{exact:true})).toBeVisible()
 await expect(page.getByRole('status').getByText('正在加载 eBR 归档版本历史…')).toBeVisible()
 await expect(page.locator('.archive-history')).toHaveCount(0)
 tasks.resumeArchive()
 await expect(page.locator('.archive-history .ant-table-wrapper .ant-table-tbody > tr.ant-table-row')).toHaveCount(2)
 await expect(page.getByRole('status').getByText('正在加载 eBR 归档版本历史…')).toHaveCount(0)
})

test('archive retrieval failure gives a visible retry and does not fabricate history',async({page})=>{
 await setup(page,'failed-archive')
 await page.goto('/qa/batches/655/review')
 await expect(page.getByText('eBR 归档版本读取失败，请刷新后重试。',{exact:true})).toBeVisible()
 await expect(page.locator('.archive-history')).toHaveCount(0)
 await page.getByRole('button',{name:'刷新证据'}).click()
 await expect(page.locator('.archive-history .ant-table-wrapper .ant-table-tbody > tr.ant-table-row')).toHaveCount(2)
 await expect(page.getByText('eBR 归档版本读取失败，请刷新后重试。',{exact:true})).toHaveCount(0)
})

test('immutable REVIEW_COPY downloads with SHA verification before slow QA gates finish',async({page})=>{
 const pending=await setup(page,'slow-qa')
 await page.goto('/qa/batches/655/review')
 const archiveRow=page.locator('.archive-history .ant-table-wrapper .ant-table-tbody > tr.ant-table-row').filter({hasText:'审核副本'})
 await expect(archiveRow).toHaveCount(1)
 const downloadButton=archiveRow.getByRole('button',{name:'下载并校验'})
 await expect(downloadButton).toBeEnabled()
 const promise=page.waitForEvent('download',{timeout:15000})
 await downloadButton.click()
 const file=await promise
 expect(file.suggestedFilename()).toBe('eBR-655-REVIEW_COPY-v1.pdf')
 pending.resumeQa()
 await expect(page.locator('.batch-heading').getByText('HYTEA-20261109-002',{exact:true})).toBeVisible()
})

test('QA gate API failure does not suppress immutable archive rows or authorized download',async({page})=>{
 await setup(page,'failed-review')
 await page.goto('/qa/batches/655/review')
 const rows=page.locator('.archive-history .ant-table-wrapper .ant-table-tbody > tr.ant-table-row')
 await expect(rows).toHaveCount(2)
 await expect(page.getByText('质量门禁暂不可读取',{exact:true})).toBeVisible()
 await expect(page.locator('.batch-heading')).toHaveCount(0)
 await expect(rows.filter({hasText:'审核副本'}).getByRole('button',{name:'下载并校验'})).toBeEnabled()
 await expect(page.getByRole('button',{name:'生成 PDF 归档'})).toBeVisible()
})

test('narrow archive table retains visible FINAL and REVIEW_COPY meaning with keyboard evidence access',async({page})=>{
 const tasks=await setup(page,'slow-qa')
 await page.setViewportSize({width:375,height:812})
 await page.goto('/qa/batches/655/review')
 const table=page.locator('.archive-history')
 await expect(table.locator('.ant-table-tbody > tr.ant-table-row')).toHaveCount(2)
 await expect(table.locator('.archive-kind-compact')).toHaveText(['最终','审核'])
 for(const marker of await table.locator('.archive-kind-compact').all())await expect(marker).toBeVisible()
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
 const summary=page.locator('.archive-technical > summary')
 await summary.focus()
 await page.keyboard.press('Enter')
 await expect(page.locator('.archive-technical')).toHaveAttribute('open','')
 tasks.resumeQa()
})
