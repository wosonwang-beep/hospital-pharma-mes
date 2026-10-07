import {test,expect,type Page} from '@playwright/test'
import {mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
import {definitions} from '../src/views/finished/model'
const output=resolve(process.env.FINISHED_UI_EVIDENCE_DIR??'C:/Users/Administrator/.codex/artifacts/finished-ui-2026-10-07/after');mkdirSync(output,{recursive:true})
test.use({viewport:{width:1440,height:900}})
const perms=Object.values(definitions).flatMap(d=>[d.permission+':view',d.permission+':create',...Object.values(d.actions).map(a=>a.permission)]).concat(['ebr:sign','production:batch:view','master:product:view','master:uom:view','wms:inventory:view','qms:sample:view','qms:sample:receive','qms:test:record','qms:test:view','audit:view','trace:view','qa:batch-review','menu:iam:users','qms:specification:view'])
const batch={id:'100',batchNo:'PB-20261006-001',productId:'20',status:'PRODUCTION_COMPLETED',finishedLotId:'61',plannedQty:'10.000000',unitId:'10'},lot={id:'61',lotNo:'FG-20261006-001',materialId:'40',materialSnapshot:{materialType:'FINISHED',materialName:'复方软膏',materialCode:'FG-001',baseUnitId:'10'},qualityStatus:'QUARANTINE',inventoryStatus:'BLOCKED',versionNo:0},unit={id:'10',unitName:'支',unitCode:'EA'},base={orgId:'1',versionNo:1,createdBy:'8',createdAt:'2026-10-06T01:00:00Z',reason:'实际受控业务事实'}
const sampling={...base,id:'301',inspectionRequestId:'201',sampleId:'401',materialLotId:'61',samplingNo:'FS-001',quantity:'1.000000',unitId:'10',sampledBy:'8',sampledAt:'2026-10-06T02:00:00Z',samplingLocation:'成品库A01',samplingMethod:'批准的代表性取样',sample:{id:'401',sampleNo:'SM-001',sampleType:'TEST_SAMPLE',mainBatchId:'100',materialLotId:'61',quantity:'1.000000',unitId:'10',status:'COLLECTED',versionNo:0,allowedActions:['RECEIVE']},allowedActions:[]}
const report={...base,id:'501',inspectionRequestId:'201',reportNo:'FR-001',generationNo:1,status:'GENERATED',overallConclusion:'PASS',generatedBy:'8',generatedAt:'2026-10-06T03:00:00Z',allowedActions:['APPROVE'],summary:{mainBatchId:'100',materialLotId:'61',qcSpecificationVersionId:'601',items:[{standard:{specificationItemId:'701',itemName:'含量测定',testCode:'ASSAY',lowerLimit:'1',upperLimit:'3',unitId:'10'},effectiveResult:{id:'801',resultNumeric:'2.000000',resultConclusion:'PASS'},originalResults:[{id:'801',revisionNo:1,resultNumeric:'2.000000',resultConclusion:'PASS',recordedAt:'2026-10-06T02:30:00Z'}],conclusion:'PASS'}]},reviews:[]}
const inbound={...base,id:'101',requestNo:'FI-001',mainBatchId:'100',productId:'20',materialLotId:'61',quantity:'10.000000',unitId:'10',status:'SUBMITTED',batch,lot,allowedActions:['CONFIRM','CANCEL']},request={...base,id:'201',inspectionRequestNo:'FQ-001',mainBatchId:'100',materialLotId:'61',inboundRequestId:'101',qcSpecificationVersionId:'601',status:'ACCEPTED',samplingRecords:[sampling],reports:[report],batch,allowedActions:['SAMPLE','GENERATE_REPORT']},shipment={...base,id:'901',shipmentNo:'SH-001',mainBatchId:'100',materialLotId:'61',locationId:'11',quantity:'4.000001',unitId:'10',receivingParty:'制剂发药室',status:'DRAFT',batch,lot,allowedActions:['CONFIRM','CANCEL']}
async function fixture(page:Page){const errors:string[]=[],commands:any[]=[],reads:URL[]=[];page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())});const rows:Record<string,any>={inbound,requests:request,sampling,reports:report,shipments:shipment};await page.route('**/api/v1/**',async r=>{const req=r.request(),u=new URL(req.url()),path=u.pathname.replace('/api/v1','');reads.push(u);let data:any={items:[],total:0,page:0,size:20};if(path==='/auth/me')data={userId:'9',organizationId:'1',displayName:'成品质量管理员',permissionCodes:perms,roleCodes:[],mustChangePassword:false};else if(path==='/finished-material-lots/61/chain')data={mainBatchId:'100',lot,receiving:[{...inbound,status:'CONFIRMED'}],finishedQuality:{requests:[request]},decisions:[],shipments:[shipment],access:{receiving:'AVAILABLE',finishedQuality:'AVAILABLE',decisions:'AVAILABLE',shipments:'AVAILABLE'}};else if(path==='/qa/batches/100/review-model')data={mainBatchId:'100',versionNo:7,batchNo:batch.batchNo,status:'PENDING_QA',finishedLots:['61'],gates:['BATCH_QA_STATE','QUALITY_PLAN','EBR_REVIEW','PRODUCTION_QC','MATERIAL_BALANCE','FINISHED_INVENTORY','FINISHED_RECEIPT','FINISHED_INSPECTION_REPORT'].map(code=>({code,passed:true,blockingCodes:[],evidenceIds:[]})),blockingCodes:[],reviewDigest:'a'.repeat(64),allowedActions:[]};else if(path==='/auth/reauth')data={reauthToken:'bound-token'};else if(path==='/main-batches')data={items:[batch],total:1,page:0,size:50};else if(path==='/main-batches/100')data=batch;else if(path==='/units')data={items:[unit],total:1,page:0,size:50};else if(path==='/units/10')data=unit;else if(path==='/locations')data={items:[{id:'11',locationCode:'FG-A01',locationName:'成品库A01'}],total:1,page:0,size:50};else if(path==='/locations/11')data={id:'11',locationName:'成品库A01'};else if(path==='/admin/users/8')data={displayName:'质量员李工'};else if(path==='/quality/specification-versions/601')data={specificationName:'复方软膏成品质量标准',versionNoBusiness:3};else if(path==='/products/20')data={productName:'复方软膏'};else if(path==='/wms/material-lots/61')data=lot;else if(path==='/samples/401')data=sampling.sample;else for(const [key,d] of Object.entries(definitions)){if(path===d.api)data={items:[rows[key]],total:1,page:0,size:20};else if(path.startsWith(d.api+'/'))data=rows[key]}
 if(req.method()!=='GET'){commands.push({path,body:req.postDataJSON(),headers:req.headers()});if(path==='/finished-inbound-requests'){rows.inbound={...inbound,requestNo:req.postDataJSON().requestNo,status:'DRAFT',versionNo:0,allowedActions:['SUBMIT','CANCEL']};if(req.postDataJSON().createInspectionDraft)rows.requests={...request,inspectionRequestNo:req.postDataJSON().inspectionRequestNo,status:'DRAFT',versionNo:0,samplingRecords:[],reports:[],allowedActions:[]};data=rows.inbound}if(path==='/finished-inbound-requests/101/confirm')data={...inbound,status:'CONFIRMED',allowedActions:[]};if(path==='/quality/finished-inspection-reports/501/approve')data={...report,status:'APPROVED',allowedActions:[]};if(path==='/samples/401/receive'){sampling.sample={...sampling.sample,status:'RECEIVED',versionNo:1,allowedActions:[]};data=sampling.sample}}
 await r.fulfill({json:{code:'OK',data,traceId:'finished-pc-controlled-fixture'}})});return {errors,commands,reads}}
async function capture(page:Page,name:string){await page.evaluate(()=>window.scrollTo(0,0));await expect.poll(()=>page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true);await page.screenshot({path:resolve(output,name+'.png'),fullPage:true})}

test('finished detail and create presentation capture',async({page})=>{
 const f=await fixture(page)
 for(const [key,d] of Object.entries(definitions)){
  const id=({inbound:'101',requests:'201',sampling:'301',reports:'501',shipments:'901'} as Record<string,string>)[key]
  await page.goto(d.path+'/'+id)
  await expect(page.locator('main[data-ui-template="T3"]')).toBeVisible()
  await expect(page.locator('main header')).toContainText((({inbound,requests:request,sampling,reports:report,shipments:shipment} as Record<string,any>)[key])[d.number])
  await expect(page.locator('.finished-facts h2').first()).toHaveText('来源信息');await expect(page.locator('.finished-metadata')).not.toHaveAttribute('open','');await capture(page,key+'-detail-desktop')
  if(d.create){await page.goto(d.path+'/create');await expect(page.getByLabel('操作原因',{exact:true})).toBeVisible();await expect(page.locator('.finished-form-group h2').first()).toHaveText('基本信息');await expect(page.locator('.finished-actions')).toBeVisible();expect(await page.locator('.finished-command form').evaluate(e=>e.getBoundingClientRect().width)).toBeLessThanOrEqual(1100);expect(await page.locator('.master-form > label').evaluateAll(elements=>elements.every(e=>{const label=e.querySelector('.form-field-label')?.getBoundingClientRect(),control=e.querySelector('input,select,textarea')?.getBoundingClientRect();return !label||!control||(control.x>=label.right&&label.top+label.height/2>=control.top&&label.top+label.height/2<=control.bottom)}))).toBe(true);await capture(page,key+'-create-desktop')}
 }
 expect(f.errors).toEqual([])
})

test('report separates effective conclusion, original FAIL evidence and actual review signatures',async({page})=>{
 const f=await fixture(page)
 const reviewed={...report,status:'APPROVED',allowedActions:[],approvedBy:'8',approvedAt:'2026-10-06T04:00:00Z',reviews:[{id:'702',reviewDecision:'APPROVE',reviewedBy:'8',reviewedAt:'2026-10-06T04:00:00Z',signatureId:'sig-702',reason:'核对冻结质量标准及原始证据'}],summary:{...report.summary,items:[{...report.summary.items[0],originalResults:[{id:'original-fail',revisionNo:0,resultNumeric:'0.5',resultConclusion:'FAIL',recordedAt:'2026-10-06T02:00:00Z'},...report.summary.items[0]!.originalResults]}]}}
 await page.route('**/api/v1/quality/finished-inspection-reports/501',r=>r.fulfill({json:{code:'OK',data:reviewed}}))
 await page.goto('/finished/reports/501')
 await expect(page.locator('.finished-conclusion')).toContainText('综合判定：合格')
 await expect(page.locator('.finished-review')).toContainText('核对冻结质量标准及原始证据');await expect(page.locator('.finished-review')).toContainText('批准')
 await expect(page.getByRole('button',{name:'独立QC审批',exact:true})).toHaveCount(0)
 await expect(page.getByLabel('实际结果',{exact:true})).toHaveCount(0)
 await page.locator('.finished-metadata > summary').click()
 await expect(page.locator('.finished-evidence-body')).toContainText('original-fail')
 await expect(page.locator('.finished-evidence-body')).toContainText('不合格')
 await page.locator('.finished-evidence summary').click()
 await expect(page.locator('.finished-review')).toContainText('sig-702')
 await capture(page,'report-original-evidence-desktop')
 await page.setViewportSize({width:768,height:1024})
 await capture(page,'report-narrow-usability')
 expect(f.errors).toEqual([]);expect(f.commands).toEqual([])
})

test('warehouse action retains signed command and grouped fields; optional inspection remains atomic',async({page})=>{
 const f=await fixture(page)
 await page.goto('/finished/inbound/101')
 await page.getByRole('button',{name:'仓库确认接收',exact:true}).click()
 await expect(page.locator('.finished-command')).toContainText('数量与库位')
 await page.getByLabel('库位',{exact:true}).selectOption('11')
 await page.getByLabel('操作原因',{exact:true}).fill('仓库核对后确认接收')
 await capture(page,'inbound-receive-action-desktop')
 await page.getByRole('button',{name:'核对并签名',exact:true}).click()
 await page.getByLabel('签名密码',{exact:true}).fill('fixture-secret')
 await page.getByRole('button',{name:'签名并提交',exact:true}).click()
 await expect.poll(()=>f.commands.some(c=>c.path.endsWith('/confirm'))).toBe(true)
 const confirmed=f.commands.find(c=>c.path.endsWith('/confirm'))
 expect(confirmed.body).toEqual({versionNo:1,locationId:'11',reason:'仓库核对后确认接收',signature:{reauthToken:'bound-token'}})
 expect(confirmed.headers['if-match']).toBe('"1"')
 expect(f.commands.find(c=>c.path==='/auth/reauth').body).toEqual({objectType:'FINISHED_WAREHOUSE_RECEIPT',objectId:'101:1',recordVersion:1,meaning:'VERIFY',credential:'fixture-secret'})
 await page.goto('/finished/inbound/create')
 await page.getByLabel('入库申请单号',{exact:true}).fill('FI-VISUAL-001')
 await page.getByLabel('生产批',{exact:true}).selectOption('100')
 await page.getByLabel('操作原因',{exact:true}).fill('同时生成关联草稿')
 await page.getByRole('checkbox',{name:'同时生成成品请验草稿'}).check()
 await page.getByLabel('成品请验单号',{exact:true}).fill('FQ-VISUAL-001')
 await capture(page,'inbound-optional-inspection-create-desktop')
 await page.getByRole('button',{name:/提\s*交/}).click()
 await expect(page).toHaveURL(/finished\/inbound\/101/)
 expect(f.commands.find(c=>c.path==='/finished-inbound-requests').body).toEqual({requestNo:'FI-VISUAL-001',mainBatchId:'100',reason:'同时生成关联草稿',createInspectionDraft:true,inspectionRequestNo:'FQ-VISUAL-001'})
 await expect(page.getByRole('button',{name:'FQ-VISUAL-001',exact:true})).toBeVisible()
 expect(f.errors).toEqual([])
})

test('permission-limited readonly details hide controlled actions and internal default references',async({page})=>{
 const f=await fixture(page)
 await page.route('**/api/v1/auth/me',r=>r.fulfill({json:{code:'OK',data:{userId:'9',organizationId:'1',displayName:'只读用户',permissionCodes:['qms:finished-report:view'],roleCodes:[],mustChangePassword:false}}}))
 await page.goto('/finished/reports/501')
 await expect(page.locator('.finished-number')).toHaveText('FR-001')
 await expect(page.locator('.finished-facts')).toContainText('暂无可读业务名称')
 await expect(page.locator('.finished-facts')).not.toContainText('来源引用')
 await expect(page.getByRole('button',{name:'独立QC审批',exact:true})).toHaveCount(0)
 await expect(page.getByRole('button',{name:'QA批审核',exact:true})).toHaveCount(0)
 await expect(page.locator('.finished-command')).toHaveCount(0)
 await expect(page.locator('.finished-metadata')).not.toHaveAttribute('open','')
 expect(f.commands).toEqual([]);expect(f.errors).toEqual([])
})
