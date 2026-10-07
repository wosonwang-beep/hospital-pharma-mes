import {test,expect} from '@playwright/test'
import {mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
import {navigationConfiguration} from '../src/layouts/navigation'
const output=resolve('../../docs/acceptance/shared-navigation-2026-10-07/screenshots')
test.use({viewport:{width:1440,height:900}})
test('approved entries use scoped reads and real identifiers without writes or default internal IDs',async({page})=>{
 const errors:string[]=[],reads:string[]=[],writes:string[]=[]
 page.on('pageerror',e=>errors.push(e.message));page.on('console',e=>{if(e.type()==='error')errors.push(e.text())})
 const permissions=[...new Set(navigationConfiguration.flatMap(g=>g.items.flatMap(i=>[i.permission!,...(i.requiredPermissions??[])])))].filter(p=>p!=='production:batch:view')
 await page.route('**/api/v1/**',r=>{
  const request=r.request(),u=new URL(request.url()),path=u.pathname.replace('/api/v1','');reads.push(path+u.search);if(request.method()!=='GET')writes.push(path)
  let data:unknown={items:[],total:0,page:0,size:20}
  if(path==='/auth/me')data={userId:'8',organizationId:'1',displayName:'入口验证员',permissionCodes:permissions,mustChangePassword:false}
  if(path==='/navigation/executions')data={items:[{id:'9201',executionNo:'EU-ENTRY-001',mainBatchId:'9101',batchNo:'PB-ENTRY-001',unitType:'MAIN',status:'IN_PROGRESS'}],total:1,page:0,size:20}
  if(path==='/navigation/batches')data={items:[{id:'9101',mainBatchId:'9101',batchNo:'PB-ENTRY-001',productId:'9301',plannedQty:'100.000000',unitId:'9401',status:'PRODUCTION_COMPLETED',finishedLotId:'9501',versionNo:2}],total:1,page:0,size:20}
  if(path==='/quality/finished-tests')data={items:[{id:'9601',testCode:'含量测定',sampleId:'9701',sampleNo:'SM-ENTRY-001',mainBatchId:'9101',batchNo:'PB-ENTRY-001',inspectionRequestId:'9801',inspectionRequestNo:'IR-ENTRY-001',status:'COMPLETED',attemptNo:1}],total:1,page:0,size:20}
  if(path==='/finished-inbound-requests')data={items:[{id:'9901',requestNo:'FG-IN-ENTRY-001',mainBatchId:'9101',materialLotId:'9501',quantity:'100.000000',unitId:'9401',status:'CONFIRMED',confirmedAt:'2026-10-07T10:00:00Z',batch:{batchNo:'PB-ENTRY-001'},lot:{lotNo:'FG-LOT-ENTRY-001',qualityStatus:'QUARANTINE'}}],total:1,page:0,size:20}
  if(path==='/execution-units/9201')data={id:'9201',executionNo:'EU-ENTRY-001',mainBatchId:'9101',status:'IN_PROGRESS',allowedActions:[]}
  if(path==='/execution-units/9201/operations')data=[]
  if(path==='/qa/batches/9101/review-model')data={mainBatchId:'9101',batchNo:'PB-ENTRY-001',versionNo:2,status:'PRODUCTION_COMPLETED',finishedLots:['9501'],gates:[],blockingCodes:[],allowedActions:[]}
  if(path==='/navigation/batches/9101')data={id:'9101',mainBatchId:'9101',batchNo:'PB-ENTRY-001',productId:'9301',plannedQty:'100.000000',unitId:'9401',status:'PRODUCTION_COMPLETED',finishedLotId:'9501',versionNo:2}
  if(path==='/quality/finished-tests/9601')data={id:'9601',testCode:'含量测定',sampleId:'9701',status:'COMPLETED',allowedActions:[],results:[]}
  return r.fulfill({json:{code:'OK',data,traceId:'entry-browser-fixture'}})
 })
 mkdirSync(output,{recursive:true})
 for(const [path,title,label,target] of [
  ['/production/execution','生产执行','进入生产执行','/mes/execution/9201'],
  ['/production/balances','物料平衡','查看物料平衡','/production/batches/9101/balance'],
  ['/finished/qa-reviews','QA批审核','进入QA审核','/qa/batches/9101/review'],
  ['/finished/releases','成品放行','查看放行审核','/qa/batches/9101/release'],
  ['/finished/tests','成品检验','查看检验记录','/finished/tests/9601'],
  ['/finished/receiving','成品生产入库（待检）','查看','/finished/inbound/9901']
 ]){
  await page.goto(path!);await expect(page.locator('main h1')).toHaveText(title!);await expect(page.locator('.result-card .ant-table-row')).toHaveCount(1)
  for(const id of ['9101','9301','9401','9501','9601','9701','9801','9901'])await expect(page.locator('.result-card')).not.toContainText(id)
  expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
  await page.screenshot({path:resolve(output,path!.split('/').pop()+'-entry-desktop.png'),fullPage:true})
  // Stop the next read after proving the actual destination. Accepted detail screens are independently covered.
  await page.locator('.result-card').getByRole('button',{name:label!,exact:true}).click()
  await expect.poll(()=>new URL(page.url()).pathname).toBe(target!)
 }
 expect(reads.some(p=>p.startsWith('/finished-inbound-requests?')&&p.includes('receivingOnly=true'))).toBe(true)
 expect(reads.some(p=>/^\/main-batches(?:\?|$)/.test(p))).toBe(false)
 expect(writes).toEqual([]);expect(errors).toEqual([])
})
