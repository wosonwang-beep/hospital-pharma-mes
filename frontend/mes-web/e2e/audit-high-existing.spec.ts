import {test,expect,type Page} from '@playwright/test'
import {mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
const output=resolve(process.env.TEMP??'C:/Windows/Temp','mes-audit-high-existing');mkdirSync(output,{recursive:true})
async function fixture(page:Page,withTrace=true){
 const errors:string[]=[],queries:URL[]=[],traceQueries:URL[]=[]
 page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())})
 const perms=['production:order:view','production:batch:view','master:product:view','wms:material-lot:view','wms:inventory:view','wms:issue:view','wms:receipt:view','trace:view','audit:view','qa:material-release:view','qms:inspection-request:view','qms:sampling:view','qms:test:view','qms:report:view','qms:deviation:view','mes:operation:view','mes:execution:view']
 const types=['RECEIPT','INSPECTION_REQUEST','SAMPLING_TASK','SAMPLE','INSPECTION_TASK','REPORT','RELEASE_DECISION','MAIN_BATCH','EXECUTION_UNIT','CHARGE','SIGNATURE']
 await page.route('**/api/v1/**',route=>{
  const url=new URL(route.request().url()),path=url.pathname.replace('/api/v1','')
  const reply=(data:unknown)=>route.fulfill({json:{code:'OK',data,message:'success',traceId:'audit-high'}})
  if(path==='/auth/me')return reply({userId:'8',organizationId:'1',displayName:'审查验证',permissionCodes:withTrace?perms:perms.filter(p=>p!=='trace:view'),mustChangePassword:false})
  if(path==='/products')return reply({items:[{id:'21',productCode:'P21',productName:'正式产品'}],total:1,page:0,size:100})
  if(path==='/production-orders'||path==='/main-batches'){
   queries.push(url);return reply({items:[{id:'41',orderNo:'PO-41',batchNo:'MB-41',plannedQty:100,productId:'21',status:'DRAFT',allowedActions:['UPDATE']}],total:21,page:Number(url.searchParams.get('page')??0),size:20})
  }
  if(path==='/production-orders/41'||path==='/main-batches/41')return reply({id:'41',orderNo:'PO-41',batchNo:'MB-41',productId:'21',plannedQty:'100',status:'DRAFT',allowedActions:[]})
  if(path==='/products/21')return reply({id:'21',productName:'正式产品'})
  if(path==='/main-batches/41/execution-units')return reply([{id:'51',executionNo:'EU-51',status:'READY',mainBatchId:'41'}])
  if(path==='/execution-units/51/operations')return reply([{id:'52',operationSeq:1,status:'PENDING'}])
  if(path==='/wms/material-lots/201')return reply({id:'201',lotNo:'LOT-201',qualityStatus:'RELEASED',inventoryStatus:'AVAILABLE',materialId:'20',receiptItemId:'101',supplierLotNo:'SUP-20261005',manufactureDate:'2026-10-05',expiryDate:'2028-10-05',materialSnapshot:{materialName:'原料A',materialCode:'MAT-0001',materialType:'原料',baseUnitId:'11',baseUnitName:'千克',specification:'25 kg/桶'},allowedActions:[]})
  if(path==='/qa/material-lots/201/release-review')return reply({decisions:[{id:'7',decision:'RELEASED',decisionAt:'2026-10-05T16:00:00Z'}]})
  if(path==='/wms/receipts/1')return reply({receiptNo:'GR-20261005-001',receivedAt:'2026-10-05T08:30:00Z',items:[{id:'101',receivedQty:'25.000',unitId:'11'}]})
  if(path==='/quality/inspection-requests/2')return reply({requestNo:'IR-20261005-001',submittedAt:'2026-10-05T09:00:00Z'})
  if(path==='/quality/sampling-tasks/3')return reply({samplingTaskNo:'SM-20261005-001',completedAt:'2026-10-05T09:30:00Z'})
  if(path==='/quality/inspection-tasks/5')return reply({inspectionTaskNo:'IT-20261005-001',reviewedAt:'2026-10-05T14:20:00Z'})
  if(path==='/quality/inspection-reports/6')return reply({reportNo:'TR-20261005-001',approvedAt:'2026-10-05T15:00:00Z'})
  if(path==='/trace'){traceQueries.push(url);return reply({nodes:types.map((type,i)=>({type,id:String(i+1),label:({RECEIPT:'GR-20261005-001',INSPECTION_REQUEST:'IR-20261005-001',SAMPLING_TASK:'SM-20261005-001',INSPECTION_TASK:'IT-20261005-001',REPORT:'TR-20261005-001',RELEASE_DECISION:'质量决定 7'} as Record<string,string>)[type]??type,status:type==='INSPECTION_TASK'?'QC_FAILED':type==='REPORT'?'APPROVED':type==='RELEASE_DECISION'?'RELEASED':'COMPLETED',revision:null})),edges:[]})}
  if(path==='/inventory'||path==='/material-issues')return reply({items:[],total:0,page:0,size:20})
  return reply({items:[],total:0,page:0,size:20})
 });return {errors,queries,traceQueries}
}
for (const allowedActions of [undefined, [], 'RELEASE']) test(`batch commands fail closed for ${JSON.stringify(allowedActions)}`, async ({page}) => {
 await page.route('**/api/v1/**', route => {
  const path = new URL(route.request().url()).pathname.replace('/api/v1', '')
  const data = path === '/auth/me'
   ? {userId:'8',organizationId:'1',displayName:'Production',permissionCodes:['production:batch:view','production:batch:release','production:batch:update'],mustChangePassword:false}
   : path === '/main-batches/41'
    ? {id:'41',batchNo:'MB-41',status:'DRAFT',versionNo:0,allowedActions}
    : {items:[],total:0}
  return route.fulfill({json:{code:'OK',data,message:'success',traceId:'fail-closed'}})
 })
 await page.goto('/production/batches/41')
 await expect(page.getByRole('heading',{name:'生产批 360° 视图',exact:true})).toBeVisible()
 await page.getByRole('button',{name:'更多受控操作',exact:true}).click()
 await expect(page.locator('.batch-controlled-menu')).toBeVisible()
 await expect(page.getByRole('button',{name:'下达生产批',exact:true})).toHaveCount(0)
})

for(const resource of ['orders','batches'])test(`frozen ${resource} query and navigation restore`,async({page})=>{
 const {errors,queries}=await fixture(page)
 const order=resource==='batches'?'&productionOrderId=41':''
 await page.goto(`/production/${resource}?keyword=保留查询&productId=21&plannedDateFrom=2026-10-01&plannedDateTo=2026-10-31&status=DRAFT&page=2${order}`)
 const main=page.locator('main')
 await expect(main.getByLabel('关键字')).toHaveValue('保留查询')
 await expect.poll(()=>queries.find(q=>q.pathname.endsWith(resource==='batches'?'/main-batches':'/production-orders'))?.searchParams.get('page')).toBe('1')
 const query=queries.find(q=>q.pathname.endsWith(resource==='batches'?'/main-batches':'/production-orders'))!
 expect(query.searchParams.get('productId')).toBe('21');expect(query.searchParams.get('plannedDateFrom')).toBe('2026-10-01');expect(query.searchParams.get('plannedDateTo')).toBe('2026-10-31')
 if(resource==='batches')expect(query.searchParams.get('productionOrderId')).toBe('41')
 const options=await main.getByLabel('状态',{exact:true}).locator('option').evaluateAll(nodes=>nodes.map(n=>(n as HTMLOptionElement).value))
 expect(options).toEqual(resource==='batches'?['','DRAFT','RELEASED','IN_PROGRESS','PRODUCTION_COMPLETED','PENDING_QA','QA_RELEASED','REJECTED']:['','DRAFT','IN_PROGRESS','COMPLETED'])
 await main.getByRole('button',{name:'查看',exact:true}).click();await expect(page).toHaveURL(new RegExp(`/production/${resource}/41\\?`))
 await page.getByRole('button',{name:resource==='batches'?'← 返回':'← 返回列表',exact:true}).click();await expect(main.getByLabel('关键字')).toHaveValue('保留查询');await expect(main.locator('.table-footer')).toContainText('第 2 页')
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
 await page.screenshot({path:resolve(output,`${resource}-${test.info().project.name}.png`),fullPage:true,scale:'css'})
 await main.getByRole('button',{name:/重\s*置/,exact:true}).click();await expect(main.getByLabel('关键字')).toHaveValue('');await expect(page).toHaveURL(new RegExp(`/production/${resource}\\?page=1$`))
 expect(errors).toEqual([])
})
test('MaterialLot T4 read chain and valid eligibility guidance',async({page,isMobile})=>{
 if(!isMobile)await page.setViewportSize({width:1280,height:853})
 const {errors}=await fixture(page);await page.goto('/wms/material-lots/201')
 await expect(page.locator('.flow-cards article')).toHaveCount(6)
 await expect(page.locator('.flow-cards').getByText('IT-20261005-001',{exact:true})).toBeVisible()
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
 await expect(page.locator('.lot-summary .quality-chain')).toBeVisible()
 await expect(page.locator('.lot-summary .chain-connector')).toHaveCount(5)
 await expect(page.locator('.lot-summary .chain-marker.has-record')).toHaveCount(6)
 await expect(page.locator('.lot-summary .quality-chain small').last()).toHaveText('2026-10-05')
 await expect(page.locator('.lot-summary')).toContainText('25.000')
 await expect(page.locator('.flow-cards')).toContainText('2026-10-05 14:20')
 await page.screenshot({path:resolve(output,"lot-overview-"+test.info().project.name+".png"),fullPage:true,scale:'css'})
 await expect(page.getByText(/生产使用资格由物料质量放行/)).toBeVisible();await expect(page.getByText(/后续质量模块|本阶段物料批不能/)).toHaveCount(0)
 await page.getByRole('tab',{name:'收货信息',exact:true}).click();await expect(page.locator('.ant-tabs-tabpane-active .lot-lineage .ant-table-row td').first()).toHaveText('收货记录');await expect(page.locator('.ant-tabs-tabpane-active .lot-lineage').getByRole('button',{name:'查看来源记录'})).toBeVisible()
 if(!isMobile)for(const name of ['质量信息','使用记录','相关生产','相关文件']){await page.getByRole('tab',{name,exact:true}).click();await expect(page.locator('.ant-tabs-tabpane-active .lot-lineage .ant-table')).toBeVisible()}
 await expect(page.locator('.ant-tabs-tabpane-active .ant-spin-spinning')).toHaveCount(0)
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
 await page.screenshot({path:resolve(output,`lot-chain-${test.info().project.name}.png`),fullPage:true,scale:'css'})
 expect(errors).toEqual([])
})
test('implemented issue flow is no longer described as unavailable',async({page})=>{
 const {errors}=await fixture(page);await page.goto('/wms/issues');await expect(page.getByText(/出库确认需满足生产批/)).toBeVisible();await expect(page.getByText(/Gate 尚未就绪|出库写入暂不可用/)).toHaveCount(0);expect(errors).toEqual([])
})

test('MaterialLot aggregation respects trace permission',async({page})=>{
 const {errors,traceQueries}=await fixture(page,false);await page.goto('/wms/material-lots/201');await page.getByRole('tab',{name:'收货信息',exact:true}).click();await expect(page.locator('.ant-tabs-tabpane-active').getByText('完整关联记录需要追溯查询权限，请联系管理员。')).toBeVisible();expect(traceQueries).toHaveLength(0);expect(errors).toEqual([])
})

test('Production Batch T4 separates business context from frozen evidence',async({page})=>{
 const {errors}=await fixture(page);await page.goto('/production/batches/41')
 await expect(page.locator('[data-ui-template="T4"]').first()).toBeVisible()
 await expect(page.getByLabel('批次摘要',{exact:true})).toBeVisible()
 await page.getByRole('tab',{name:'生产执行',exact:true}).click()
 await expect(page.getByRole('heading',{name:'正式产品',exact:true})).toBeVisible()
 await expect(page.getByRole('table').first().getByText('EU-51',{exact:true})).toBeVisible()
 await expect(page.getByRole('button',{name:'进入执行',exact:true})).toBeVisible()
 await expect.poll(()=>page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
 await page.screenshot({path:resolve(output,`batch-t4-${test.info().project.name}.png`),fullPage:true})
 await page.getByRole('button',{name:'更多受控操作',exact:true}).click()
 await page.getByRole('button',{name:'冻结工艺与证据',exact:true}).click()
 await expect(page.getByText('查看冻结工艺快照',{exact:true})).toBeVisible()
 expect(errors).toEqual([])
})
