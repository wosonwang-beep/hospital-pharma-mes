import {test,expect,type Page} from '@playwright/test'
import {readFileSync,mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
const output=resolve('../../docs/acceptance/wms-request-management-2026-10-06/screenshots');mkdirSync(output,{recursive:true})
test.use({viewport:{width:1440,height:900}})
const batch={id:'100',batchNo:'PB-20261006-001',productId:'20',status:'RELEASED',processSnapshotId:'300',processSnapshot:{snapshot:{process:{formula:{id:'30',items:[{formulaItemId:'31',materialId:'40',requiredQty:'3',unitId:'10'}]}},materials:[{id:'40',materialName:'原料A',materialCode:'MAT-001',baseUnitId:'10'}]}}}
const item={id:'201',formulaItemId:'31',materialId:'40',materialName:'原料A',materialCode:'MAT-001',requestedQty:'3.000000',issuedQty:'0.000000',remainingQty:'3.000000',unitId:'10',unitName:'千克'}
const lot={id:'61',lotNo:'ML-20261006-001',materialId:'40',qualityStatus:'RELEASED',inventoryStatus:'AVAILABLE',materialSnapshot:{materialName:'原料A'}}
const unit={id:'10',unitName:'千克',unitCode:'kg'}
const product={id:'20',productName:'复方软膏',productCode:'PRD-001'}
async function fixture(page:Page,multiple=false){
 const errors:string[]=[],commands:Array<{path:string;body:any;headers:Record<string,string>}>=[]
 page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())})
 const router=readFileSync(new URL('../src/router/index.ts',import.meta.url),'utf8')
 const permissions=[...router.matchAll(/permission:\s*'([^']+)'/g)].map(m=>m[1]!).concat(['wms:request:submit','wms:request:cancel','wms:issue:confirm','wms:issue:return','master:material:view','master:uom:view','master:product:view','audit:view'])
 let request:any={id:'200',requestNo:'RQ-20261006-001',mainBatchId:'100',batchNo:batch.batchNo,productId:'20',productName:product.productName,processSnapshotId:'300',formulaVersionId:'30',status:'DRAFT',versionNo:0,createdAt:'2026-10-06T01:00:00Z',allowedActions:['UPDATE','SUBMIT','CANCEL'],items:[{...item}],linkedIssues:[]}
 if(multiple)request.items.push({...item,id:'202',formulaItemId:'32',materialName:'原料B'})
 let issue:any={id:'400',issueNo:'OUT-20261006-001',mainBatchId:'100',batchNo:batch.batchNo,productId:'20',productName:product.productName,materialRequestId:'200',materialRequestNo:request.requestNo,status:'CONFIRMED',versionNo:1,issuedAt:'2026-10-06T02:00:00Z',allowedActions:['RETURN'],items:[{id:'401',materialLotId:'61',materialRequestItemId:'201',formulaItemId:'31',issuedQty:'3.000000',unitId:'10'}],returns:[]}
 const returned:any={id:'500',issueId:'400',issueItemId:'401',issueNo:issue.issueNo,mainBatchId:'100',batchNo:batch.batchNo,materialId:'40',materialName:'原料A',materialLotId:'61',lotNo:lot.lotNo,quantity:'1.000000',unitId:'10',unitName:'千克',originalIssuedQty:'3.000000',issuedUnitId:'10',issuedUnitName:'千克',returnedQty:'1.000000',remainingReturnQty:'2.000000',reason:'生产剩余退料',returnedBy:'8',returnedAt:'2026-10-06T03:00:00Z',recordVersion:0}
 const paged=(items:any[])=>({items,total:items.length,page:0,size:20})
 await page.route('**/api/v1/**',async r=>{
  const req=r.request(),u=new URL(req.url()),path=u.pathname.replace('/api/v1',''),method=req.method();let data:any=paged([])
  if(method!=='GET'){const body=req.postDataJSON();commands.push({path,body,headers:req.headers()})
   if(path==='/material-requests'){request={...request,requestNo:body.requestNo,items:body.items.map((i:any)=>({...item,...i}))};data=request}
   else if(path==='/material-requests/200'&&method==='PUT'){request={...request,requestNo:body.requestNo,versionNo:request.versionNo+1,items:body.items.map((i:any)=>({...item,...i}))};data=request}
   else if(path.endsWith('/submit')){request={...request,status:'SUBMITTED',versionNo:1,allowedActions:['CANCEL']};data=request}
   else if(path==='/material-issues'){issue={...issue,...body,status:'DRAFT',versionNo:0,allowedActions:['UPDATE','CONFIRM'],items:body.items.map((i:any)=>({...i,id:'401'}))};data=issue}
   else if(path.endsWith('/returns')){issue={...issue,versionNo:2,returns:[returned]};data=issue}
   else data=request
  }else if(path==='/auth/me')data={userId:'8',organizationId:'1',displayName:'仓储管理员',permissionCodes:permissions,roleCodes:[],mustChangePassword:false}
  else if(path==='/wms/inventory')data=paged([{...lot,materialLotId:lot.id,materialName:'原料A',materialCode:'MAT-001',onHandQty:'10.000000',reservedQty:'3.000000',availableQty:'7.000000',unitName:'千克',locationBalances:[{warehouseId:'1',warehouseName:'原辅料仓',locationId:'2',locationName:'A-01',onHandQty:'10.000000',unitName:'千克'}]}])
  else if(path==='/material-requests')data=paged([request]);else if(path==='/material-requests/200')data=request
  else if(path==='/material-issues')data=paged([issue]);else if(path==='/material-issues/400')data=issue
  else if(path==='/material-returns')data=paged([returned])
  else if(path==='/main-batches')data=paged([batch]);else if(path==='/main-batches/100')data=batch
  else if(path==='/wms/material-lots')data=paged([lot,{...lot,id:'62',lotNo:'ML-20261006-002'}]);else if(path==='/wms/material-lots/61')data=lot
  else if(path==='/units')data=paged([unit]);else if(path==='/units/10')data=unit
  else if(path==='/products')data=paged([product]);else if(path==='/products/20')data=product
  else if(path==='/materials')data=paged([{id:'40',materialName:'原料A',materialCode:'MAT-001'}])
  else if(path==='/warehouses')data=paged([{id:'1',warehouseName:'原辅料仓',warehouseCode:'WH-001'}])
  else if(path==='/locations')data=paged([{id:'2',locationName:'A-01',locationCode:'A-01',warehouseId:'1'}])
  await r.fulfill({status:method==='POST'&&['/material-requests','/material-issues'].includes(path)?201:200,json:{code:'OK',data,traceId:'wms-pc-fixture'}})
 });return {errors,commands,submit:()=>{request={...request,status:'SUBMITTED',allowedActions:['CANCEL']}}}
}
async function capture(page:Page,name:string){await page.keyboard.press('Escape');await expect.poll(()=>page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true);await page.screenshot({path:resolve(output,name+'.png'),fullPage:true})}
test('WMS PC inventory requests issues returns share approved menu and truthful facts',async({page})=>{
 const f=await fixture(page)
 for(const [path,title,file] of [['inventory','库存管理','inventory-desktop'],['requests','领料申请','request-list-desktop'],['issues','出库管理','issue-list-desktop'],['returns','退料管理','return-list-desktop']]){
  await page.goto('/wms/'+path);await expect(page.locator('main[data-ui-template="T1"]')).toBeVisible();await expect(page.getByRole('heading',{name:title,exact:true})).toBeVisible();await expect(page.locator('main .ant-table-row')).toHaveCount(1);if(path==='inventory')await expect.poll(()=>page.getByLabel('库位查询',{exact:true}).evaluate(el=>el.getBoundingClientRect().width)).toBeGreaterThan(200);await capture(page,file!)
 }
 const texts=await page.locator('.nav').getByRole('menuitem').allTextContents();const expected=['原辅料收货记录','库存管理','领料申请','出库管理','退料管理'];let previous=-1;for(const text of expected){const index=texts.findIndex(t=>t.trim()===text);expect(index).toBeGreaterThan(previous);previous=index}
 await page.goto('/wms/requests/200?keyword=RQ');await expect(page.getByRole('heading',{name:'RQ-20261006-001',exact:true})).toBeVisible();await expect(page.getByText('原料A',{exact:true})).toBeVisible();await capture(page,'request-detail-desktop');await page.getByRole('button',{name:'← 返回领料申请',exact:true}).click();await expect(page).toHaveURL(/keyword=RQ/)
 expect(f.errors).toEqual([])
})
test('Formal request create and submit send only approved source identities and business commands',async({page})=>{
 const f=await fixture(page);await page.goto('/wms/requests/create');await page.getByLabel('申请单号',{exact:true}).fill('RQ-PC-001');await page.getByLabel('生产批',{exact:true}).selectOption('100');await page.getByLabel('处方物料 1',{exact:true}).selectOption('31');await page.getByLabel('申请数量 1',{exact:true}).fill('3');await page.locator('.ant-select[aria-label="申请单位 1"]').click();await page.locator('.ant-select-dropdown:visible').getByText('千克（kg）',{exact:true}).click();await page.getByLabel('操作原因',{exact:true}).fill('生产领料需求');await capture(page,'request-create-desktop');await page.getByRole('button',{name:'保存领料申请',exact:true}).click();await expect(page).toHaveURL(/\/wms\/requests\/200/)
 expect(f.commands[0]?.body).toEqual({requestNo:'RQ-PC-001',mainBatchId:'100',reason:'生产领料需求',items:[{formulaItemId:'31',requestedQty:'3',unitId:'10'}]});expect(f.commands[0]?.headers['idempotency-key']).toBeTruthy()
 await page.getByLabel('操作原因',{exact:true}).fill('提交生产需求');await page.getByRole('button',{name:'提交领料申请',exact:true}).click();await expect(page.getByText('已提交',{exact:true})).toBeVisible();expect(f.commands[1]?.path).toBe('/material-requests/200/submit');expect(f.commands[1]?.body).toEqual({versionNo:0,reason:'提交生产需求'});expect(f.commands[1]?.headers['if-match']).toBe('"0"');expect(f.errors).toEqual([])
})
test('Linked issue retains request and line identity in existing MaterialIssue command',async({page})=>{
 const f=await fixture(page);f.submit();await page.goto('/wms/issues/create?materialRequestId=200');await page.getByLabel('出库单号',{exact:true}).fill('OUT-PC-001');await page.getByLabel('物料批次 1',{exact:true}).selectOption('61');await page.getByLabel('操作原因',{exact:true}).fill('仓库交接');await capture(page,'issue-create-linked-desktop');await page.getByRole('button',{name:'保存出库单',exact:true}).click();await expect(page.getByRole('heading',{name:'出库单详情',exact:true})).toBeVisible();expect(f.commands[0]?.body).toEqual({materialRequestId:'200',mainBatchId:'100',issueNo:'OUT-PC-001',reason:'仓库交接',items:[{materialLotId:'61',materialRequestItemId:'201',formulaItemId:'31',issuedQty:'3.000000',unitId:'10'}]});await capture(page,'issue-detail-desktop');expect(f.errors).toEqual([])
})
test('Independent return entry displays original and prior quantities and preserves existing return payload',async({page})=>{
 const f=await fixture(page);await page.goto('/wms/returns/create?issueId=400');await expect(page.getByRole('heading',{name:'登记退料',exact:true})).toBeVisible();await expect(page.getByText('ML-20261006-001',{exact:true})).toBeVisible();await page.locator('.ant-select[aria-label="原出库明细"]').click();await page.locator('.ant-select-dropdown:visible').getByText('401 · ML-20261006-001',{exact:true}).click();await page.getByLabel('本次退料量',{exact:true}).fill('1');await page.getByLabel('退料原因',{exact:true}).fill('生产剩余退料');await capture(page,'return-create-desktop');await page.getByRole('button',{name:'提交退料',exact:true}).click();await expect(page).toHaveURL(/\/wms\/returns\?issueId=400/);expect(f.commands[0]?.body).toEqual({versionNo:1,reason:'生产剩余退料',items:[{issueItemId:'401',quantity:'1',unitId:'10'}]});expect(f.commands[0]?.path).toBe('/material-issues/400/returns');expect(f.errors).toEqual([])
})
test('Linked outbound can select a subset and split one demand line between lot allocations',async({page})=>{
 const f=await fixture(page,true);f.submit();await page.goto('/wms/issues/create?materialRequestId=200');await page.getByLabel('出库单号',{exact:true}).fill('OUT-SPLIT-001');
 await page.getByRole('button',{name:'移除本次出库明细 2',exact:true}).click();await expect(page.getByLabel('出库数量 2',{exact:true})).toHaveCount(0);
 await page.getByRole('button',{name:'拆分批次分配 1',exact:true}).click();await page.getByLabel('物料批次 1',{exact:true}).selectOption('61');await page.getByLabel('物料批次 2',{exact:true}).selectOption('62');await page.getByLabel('出库数量 1',{exact:true}).fill('2');await page.getByLabel('出库数量 2',{exact:true}).fill('1');await page.getByLabel('操作原因',{exact:true}).fill('部分申请明细分批出库');await page.getByRole('button',{name:'保存出库单',exact:true}).click();await expect(page.getByRole('heading',{name:'出库单详情',exact:true})).toBeVisible();expect(f.commands[0]?.body.items).toHaveLength(2);expect(f.commands[0]?.body.items.map((i:any)=>i.materialRequestItemId)).toEqual(['201','201']);expect(f.errors).toEqual([])
})
test('Draft edit retains line identity and discarding edits restores server detail facts',async({page})=>{
 const f=await fixture(page);page.on('dialog',d=>d.accept());await page.goto('/wms/requests/200');await page.getByRole('button',{name:/^编\s*辑$/}).click();await page.getByLabel('申请数量 1',{exact:true}).fill('9');await page.goBack();await expect(page.locator('main[data-ui-template="T3"]')).toBeVisible();await expect(page.locator('main .ant-table-row').first()).toContainText('3.000000');await expect(page.locator('main .ant-table-row').first()).not.toContainText('9');
 await page.getByRole('button',{name:/^编\s*辑$/}).click();await page.getByLabel('申请数量 1',{exact:true}).fill('2.5');await page.getByLabel('操作原因',{exact:true}).fill('调整申请数量');await capture(page,'request-edit-desktop');await page.getByRole('button',{name:'保存领料申请',exact:true}).click();await expect(page.locator('main[data-ui-template="T3"]')).toBeVisible();expect(f.commands[0]?.body).toEqual({requestNo:'RQ-20261006-001',versionNo:0,reason:'调整申请数量',items:[{id:'201',formulaItemId:'31',requestedQty:'2.5',unitId:'10'}]});expect(f.commands[0]?.headers['if-match']).toBe('"0"');expect(f.errors).toEqual([])
})
