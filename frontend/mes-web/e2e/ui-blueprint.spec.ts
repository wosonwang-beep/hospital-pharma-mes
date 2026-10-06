import {test,expect,type Page} from '@playwright/test'
import {readFileSync,mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
const output=resolve(process.env.MES_UI_SCREENSHOT_DIR??'../../docs/acceptance/ui-blueprint-2026-10-05');mkdirSync(output,{recursive:true})
const router=readFileSync(new URL('../src/router/index.ts',import.meta.url),'utf8')
const permissions=[...router.matchAll(/permission:\s*'([^']+)'/g)].map(m=>m[1]!)
for(const key of ['material','supplier','org','uom','equipment','qualification'])for(const action of ['view','create','update'])permissions.push(`master:${key}:${action}`)
permissions.push('qms:sample:view','mes:execution:view','iam:role:view','iam:user:view','iam:user:create','iam:user:update','iam:role:create','iam:role:update','iam:permission:view')
export async function emptyFixture(page:Page){
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())})
 await page.route('**/api/v1/**',route=>{
  const path=new URL(route.request().url()).pathname.replace('/api/v1','')
  const data=path==='/auth/me'?{userId:'8',organizationId:'1',displayName:'UI审查员',permissionCodes:permissions,roleCodes:[],mustChangePassword:false}:{items:[],total:0,page:0,size:20}
  return route.fulfill({json:{code:'OK',message:'success',data,traceId:'blueprint'}})
 });return errors
}
const lists=['/admin/users','/admin/roles',...['materials','suppliers','organizations','units','unit-conversions','equipment','qualifications'].map(r=>'/master/'+r),'/process/products','/process/packages','/wms/receipts','/wms/issues',...['inspection-requests','sampling-tasks','samples','inspection-tasks','inspection-reports','specifications','production-plans','production-tests'].map(r=>'/quality/'+r),'/deviations','/production/orders','/production/batches','/ebr/templates']
test('T1 remaining list families share compact queries and bounded results',async({page})=>{
 test.setTimeout(120000);const errors=await emptyFixture(page)
 for(const path of lists){
  await page.goto(path);const main=page.locator('main.admin-page');await expect(main).toHaveAttribute('data-ui-template','T1')
  await expect(main.locator('.query-card')).toBeVisible();await expect(main.locator('.result-card')).toBeVisible()
  await expect(main.getByRole('button',{name:/^查\s*询$/})).toBeVisible();await expect(main.getByRole('button',{name:/^重\s*置$/})).toBeVisible()
  await expect.poll(()=>page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
  await page.screenshot({path:resolve(output,`t1-${path.slice(1).replaceAll('/','-')}-${test.info().project.name}.png`),fullPage:true})
 }
 expect(errors).toEqual([])
})

const creates=['/admin/users/create','/admin/roles/create',...['materials','suppliers','organizations','units','unit-conversions','equipment','qualifications'].map(r=>'/master/'+r+'/create'),'/process/products/create','/process/packages/create','/wms/receipts/create','/wms/issues/create',...['inspection-requests','sampling-tasks','inspection-tasks','inspection-reports','specifications','production-plans','production-tests'].map(r=>'/quality/'+r+'/create'),'/deviations/create','/production/orders/create','/production/batches/create','/ebr/templates/create']
test('T2 create families retain horizontal labels and bounded form width',async({page})=>{
 test.setTimeout(120000);const errors=await emptyFixture(page)
 for(const path of creates){
  await page.goto(path);const main=page.locator('main.admin-page');await expect(main).toHaveAttribute('data-ui-template','T2')
  await expect(main.locator('input,select,textarea,.ant-select').first()).toBeVisible()
  await expect.poll(()=>page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
  const forms=await main.locator('.master-form,.detail-form').evaluateAll(nodes=>nodes.map(n=>n.getBoundingClientRect().width));expect(forms.every(w=>w<=1100)).toBe(true)
  await page.screenshot({path:resolve(output,`t2-${path.slice(1).replaceAll('/','-')}-${test.info().project.name}.png`),fullPage:true})
 }
 expect(errors).toEqual([])
})

test('Specialized trace and audit plus T1 integration expose only existing evidence',async({page})=>{
 const errors=await emptyFixture(page)
 const audit={id:'9',orgId:'1',actorId:'8',actorRole:'QA',action:'SIGN',objectType:'MaterialLot',objectId:'61',oldValueDigest:'previous-digest',newValueDigest:'current-digest',reason:'批次质量审核',clientInfo:'Chromium',occurredAt:'2026-10-05T01:00:00Z',transactionId:'tx-9',requestId:'request-9',source:'API'}
 const message={messageRef:'OUTBOX:7',direction:'OUTBOX',messageId:'release-7',sourceSystem:'MES',targetSystem:'WMS',eventType:'MATERIAL_RELEASED',aggregateType:'MaterialLot',aggregateId:'61',status:'DEAD_LETTER',retryCount:3,nextRetryAt:null,lastErrorCode:'TIMEOUT',lastErrorMessage:'目标系统响应超时',occurredAt:'2026-10-05T01:00:00Z',completedAt:null,versionNo:4}
 await page.route('**/api/v1/audit-events**',r=>r.fulfill({json:{code:'OK',data:{items:[audit],total:1,page:0,size:50}}}))
 await page.route('**/api/v1/integration/messages**',r=>r.fulfill({json:{code:'OK',data:{items:[message],total:1,page:0,size:50}}}))
 await page.route('**/api/v1/trace**',r=>r.fulfill({json:{code:'OK',data:{nodes:[{type:'MATERIAL_LOT',id:'61',label:'ML-20261005-001',status:'RELEASED'},{type:'INSPECTION_REQUEST',id:'71',label:'IR-20261005-001',status:'COMPLETED'},{type:'REPORT',id:'91',label:'TR-20261005-001',status:'APPROVED'}],edges:[{sourceType:'MATERIAL_LOT',sourceId:'61',targetType:'INSPECTION_REQUEST',targetId:'71',relation:'SUBMITTED_FOR_INSPECTION'},{sourceType:'INSPECTION_REQUEST',sourceId:'71',targetType:'REPORT',targetId:'91',relation:'REPORTED_BY'}]}}}))
 for(const [path,name] of [['/trace?materialLotId=61','trace'],['/audit','audit'],['/integration/operations','integration']]){
  await page.goto(path!);await expect(page.locator('main[data-ui-template]')).toBeVisible()
  if(name==='trace')await expect(page.locator('.trace-node')).toHaveCount(3)
  else await expect(page.getByRole('button',{name:'查看详情',exact:true})).toBeVisible()
  await expect.poll(()=>page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
  await page.screenshot({path:resolve(output,`${name}-${test.info().project.name}.png`),fullPage:true})
  if(name==='trace'){await page.locator('.trace-node').first().click();await expect(page.getByRole('button',{name:'查看来源记录',exact:true})).toBeVisible()}
  else {await page.getByRole('button',{name:'查看详情',exact:true}).click();await expect(page.getByRole('dialog')).toBeVisible();await expect(page.getByText(name==='audit'?'previous-digest':'目标系统响应超时',{exact:true})).toBeVisible()}
  await page.screenshot({path:resolve(output,`${name}-drawer-${test.info().project.name}.png`),fullPage:false})
 }
 expect(errors).toEqual([])
})


test('Consistency closeout renders populated IAM master WMS and production facts',async({page})=>{
 const errors=await emptyFixture(page)
 const fixtures:Record<string,unknown>={
 '/roles/1':{id:'1',roleCode:'QA',roleName:'质量审核员',status:'ACTIVE',version:3,permissionCodes:['qms:test:view','audit:view'],menuCodes:[]},
 '/permissions':{items:[{id:'1',permissionCode:'qms:test:view',permissionName:'查看检验记录',permissionType:'MENU',status:'ACTIVE'},{id:'2',permissionCode:'audit:view',permissionName:'查看审计事件',permissionType:'MENU',status:'ACTIVE'}],total:2},
 '/unit-conversions/1':{id:'1',fromUnitId:'10',toUnitId:'11',factor:'1000',materialId:null,versionNo:2,allowedActions:['UPDATE']},
 '/units/10':{id:'10',unitCode:'kg',unitName:'千克'},'/units/11':{id:'11',unitCode:'g',unitName:'克'},
 '/main-batches/100':{id:'100',batchNo:'PB-20261005-001',status:'IN_PROGRESS',productId:'5',processSnapshot:{snapshot:{process:{formula:{items:[{formulaItemId:'31',materialId:'20',requiredQty:'10'}]}},materials:[{id:'20',materialName:'原料A'}]}}},
 '/products/5':{id:'5',productCode:'P-005',productName:'复方软膏'},
 '/wms/material-lots/61':{id:'61',lotNo:'ML-20261005-001'},
 '/production-orders/1':{id:'1',orderNo:'PO-20261005-001',status:'DRAFT',productId:'5',plannedQty:'100',unitId:'10',plannedDate:'2026-10-05',versionNo:2},
 '/material-issues/1':{id:'1',issueNo:'MI-20261005-001',mainBatchId:'100',status:'CONFIRMED',versionNo:1,items:[{id:'1',materialLotId:'61',formulaItemId:'31',issuedQty:'10',unitId:'10'}],returns:[]},
 '/quality/production-tests/1':{id:'1',testCode:'ASSAY',sampleId:'71',performedBy:'8',status:'COMPLETED',currentResultRevisionId:'91',specificationSnapshot:{itemName:'含量测定',lowerLimit:'95',upperLimit:'105',unitId:'10'},results:[{id:'91',resultNumeric:'102.3',resultConclusion:'PASS',recordedAt:'2026-10-05T01:00:00Z',review:{disposition:'ACCEPTED',reviewedAt:'2026-10-05T02:00:00Z'}}],allowedActions:[]},
 '/samples/71':{id:'71',sampleNo:'SM-20261005-001'},'/users/8':{id:'8',displayName:'检验员李工'},
 '/materials/1':{id:'1',materialCode:'MAT-001',materialName:'原料A',materialType:'RAW',baseUnitId:'10',baseUnitName:'千克',requiresIncomingInspection:true,lotControlled:true,status:'ACTIVE',versionNo:1,allowedActions:[]},
 '/materials/1/suppliers':{items:[],versionNo:1,suppliers:[]},'/unit-conversions':{items:[],total:0,page:0,size:20}
 }
 await page.route('**/api/v1/**',r=>{const path=new URL(r.request().url()).pathname.replace('/api/v1','');return Object.hasOwn(fixtures,path)?r.fulfill({json:{code:'OK',data:fixtures[path]}}):r.fallback()})
 for(const [path,name,expected] of [['/','dashboard','工作台'],['/admin/roles/1','iam','质量管理'],['/master/unit-conversions/1','conversion','千克'],['/production/orders/1','order','复方软膏'],['/wms/issues/1','issue','ML-20261005-001'],['/quality/production-tests/1','production-test','102.3'],['/master/materials/1','material','是否入库必验']]){
  await page.goto(path!);if(name==='material')await page.getByRole('tab',{name:'质量控制',exact:true}).click()
  await expect(page.locator('main[data-ui-template]').getByText(expected!,{exact:true}).first()).toBeVisible()
  await expect.poll(()=>page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
  await page.screenshot({path:resolve(output,`closeout-${name}-${test.info().project.name}.png`),fullPage:true})
 }
 expect(errors).toEqual([])
})

test('Issue selectors preserve existing IDs and command contract',async({page})=>{
 const errors=await emptyFixture(page);let submitted:unknown
 const batch={id:'100',batchNo:'PB-20261005-001',processSnapshot:{snapshot:{process:{formula:{items:[{formulaItemId:'31',materialId:'20',requiredQty:'10'}]}},materials:[{id:'20',materialName:'原料A'}]}}}
 await page.route('**/api/v1/main-batches**',r=>r.fulfill({json:{code:'OK',data:new URL(r.request().url()).pathname.endsWith('/100')?batch:{items:[batch],total:1,page:0,size:50}}}))
 await page.route('**/api/v1/wms/material-lots?**',r=>r.fulfill({json:{code:'OK',data:{items:[{id:'61',lotNo:'ML-20261005-001',qualityStatus:'RELEASED'}],total:1}}}))
 await page.route('**/api/v1/units?**',r=>r.fulfill({json:{code:'OK',data:{items:[{id:'10',unitName:'千克',unitCode:'kg'}],total:1}}}))
 await page.route('**/api/v1/material-issues**',r=>{if(r.request().method()==='POST'){submitted=r.request().postDataJSON();return r.fulfill({json:{code:'OK',data:{id:'1'}}})}return r.fulfill({json:{code:'OK',data:{id:'1',mainBatchId:'100',issueNo:'MI-001',status:'DRAFT',versionNo:1,items:[],returns:[]}}})})
 await page.goto('/wms/issues/create');await expect(page.getByRole('heading',{name:'新增出库单',exact:true})).toBeVisible();await expect(page.locator('.nav').getByText('出库管理',{exact:true})).toBeVisible();await page.getByLabel('出库单号',{exact:true}).fill('MI-001');await page.getByLabel('生产批',{exact:true}).selectOption('100')
 await page.getByLabel('物料批次 1',{exact:true}).selectOption('61');await page.getByLabel('处方明细 1',{exact:true}).selectOption('31');await page.getByLabel('出库数量 1',{exact:true}).fill('10')
 await page.locator('.ant-select[aria-label="出库单位 1"]').click();await page.locator('.ant-select-dropdown:visible').getByText('千克（kg）',{exact:true}).click()
 await page.getByLabel('操作原因',{exact:true}).fill('生产领料');await page.evaluate(()=>window.scrollTo(0,0));await page.screenshot({path:resolve(output,`issue-create-${test.info().project.name}.png`),fullPage:true})
 await page.getByRole('button',{name:'保存出库单',exact:true}).click();await expect(page).toHaveURL(/\/wms\/issues\/1$/);await expect(page.getByRole('heading',{name:'出库单详情',exact:true})).toBeVisible()
 expect(submitted).toEqual({mainBatchId:'100',issueNo:'MI-001',items:[{materialLotId:'61',formulaItemId:'31',issuedQty:'10',unitId:'10'}],reason:'生产领料'});expect(errors).toEqual([])
})
test('Password confirmation remains client-only and preserves change-password API',async({page})=>{
 const errors=await emptyFixture(page);const commands:unknown[]=[]
 await page.route('**/api/v1/auth/change-password',r=>{commands.push(r.request().postDataJSON());return r.fulfill({json:{code:'OK',data:null}})})
 await page.goto('/change-password');await page.getByLabel('当前密码',{exact:true}).fill('OldPassword12345');await page.getByLabel('新密码',{exact:true}).fill('NewPassword12345');await page.getByLabel('确认新密码',{exact:true}).fill('OtherPassword12345');await page.getByRole('button',{name:'保存新密码',exact:true}).click()
 await expect(page.getByText('两次输入的新密码不一致',{exact:true})).toBeVisible();expect(commands).toEqual([])
 await page.getByLabel('确认新密码',{exact:true}).fill('NewPassword12345');await page.screenshot({path:resolve(output,`password-${test.info().project.name}.png`),fullPage:true});await page.getByRole('button',{name:'保存新密码',exact:true}).click();await expect(page).toHaveURL(/\/login$/)
 expect(commands).toEqual([{oldPassword:'OldPassword12345',newPassword:'NewPassword12345'}]);expect(errors).toEqual([])
})

test('Authorized qualification input does not acquire user lookup permission dependency',async({page})=>{
 const errors=await emptyFixture(page);let forbiddenLookup=false
 await page.route('**/api/v1/auth/me',r=>r.fulfill({json:{code:'OK',data:{userId:'8',organizationId:'1',displayName:'设备管理员',permissionCodes:['master:qualification:create'],roleCodes:[],mustChangePassword:false}}}))
 await page.route('**/api/v1/users**',r=>{forbiddenLookup=true;return r.fulfill({status:403,json:{code:'FORBIDDEN'}})})
 await page.goto('/master/qualifications/create');const reference=page.getByRole('textbox',{name:'人员',exact:true});await expect(reference).toBeEnabled();await reference.fill('23');await expect(reference).toHaveValue('23');expect(forbiddenLookup).toBe(false);expect(errors).toEqual([])
})
