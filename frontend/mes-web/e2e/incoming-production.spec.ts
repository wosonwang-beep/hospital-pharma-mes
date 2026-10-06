import {test,expect,type Page,type TestInfo} from '@playwright/test'
import {join} from 'node:path'
import {tmpdir} from 'node:os'
import {readFileSync} from 'node:fs'
const contract=JSON.parse(readFileSync(new URL('../src/views/production/production-contract.json',import.meta.url),'utf8'))
// Browser plugin not available: repository Playwright is the authorized fallback.
// These are closed-contract UI fixtures, not evidence of real production/quality eligibility.
type Row=Record<string,unknown>
const timestamp='2026-10-03T01:00:00Z'
const base=(id:string)=>({id,versionNo:0,createdAt:timestamp,updatedAt:timestamp,allowedActions:[] as string[]})
const envelope=(data:unknown)=>({code:'OK',message:'success',data,traceId:'production-ui-fixture'})
test('batch list opens independent detail with release confined to detail',async({page})=>{
 const errors=await setup(page,'release');await page.goto('/production/batches')
 await expect(page.getByText('BROWSER-BATCH',{exact:true})).toBeVisible()
 await expect(page.getByRole('button',{name:'下达生产批',exact:true})).toHaveCount(0)
 const pair=page.locator('.query-form label').first();const positions=await pair.evaluate(el=>{const a=el.children[0]!.getBoundingClientRect(),b=el.children[1]!.getBoundingClientRect();return {right:a.right,left:b.left,dy:Math.abs(a.y-b.y)}})
 expect(positions.right).toBeLessThanOrEqual(positions.left+1);expect(positions.dy).toBeLessThan(12)
 await page.getByRole('button',{name:'查看',exact:true}).click();await expect(page).toHaveURL(/\/production\/batches\/100$/)
 await page.getByRole('button',{name:'更多受控操作',exact:true}).hover();await expect(page.locator('.batch-controlled-menu')).toBeVisible();await expect(page.getByRole('button',{name:'下达生产批',exact:true})).toBeVisible();expect(errors).toEqual([])
})
const paged=(items:unknown[])=>({items,total:items.length,page:0,size:50})
function closed(name:string,row:Row){const schema=(contract.schemas as Record<string,{properties?:Row;required?:string[];additionalProperties?:boolean}>)[name]!;for(const key of schema.required??[])expect(row,`${name}.${key}`).toHaveProperty(key);if(schema.additionalProperties===false)for(const key of Object.keys(row))expect(Object.keys(schema.properties??{}),`${name} unexpected ${key}`).toContain(key);return row}
async function setup(page:Page,mode:'release'|'verify'|'charge'){
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));page.on('console',message=>{if(['error','warning'].includes(message.type())&&!/Failed to load resource.*422/.test(message.text()))errors.push(message.text())})
 const permissions=['production:batch:view','production:batch:release','mes:execution:view','mes:operation:view','mes:weigh:view','mes:weigh:create','mes:weigh:verify','mes:charge:view','mes:charge:create','ebr:sign','trace:view','process:package:view','ebr:template:view','wms:inventory:view','master:uom:view']
 const execution=closed('ExecutionUnit',{...base('101'),mainBatchId:'100',subBatchId:null,unitType:'DIRECT',executionNo:'BROWSER-BATCH/DIRECT',status:'IN_PROGRESS'})
 const operation=closed('Operation',{...base('102'),versionNo:4,executionUnitId:'101',operationDefId:'12',operationSeq:1,status:'IN_PROGRESS',startedAt:timestamp,completedAt:null,operatorId:'5',equipmentUsages:[],parameterValues:[],gates:[],gateEvidence:[]})
 const policy={organizationId:'1',materialId:'20',baseUnitId:'10',required:true,precision:'0.001',tolerancePct:'0.5',policyVersion:'DEPLOYMENT-1',configurationHash:'a'.repeat(64)}
 const snapshot=closed('ProcessSnapshot',{id:'130',packageVersionId:'11',formulaVersionId:'13',routeVersionId:'14',ebrTemplateVersionId:'21',snapshotHash:'b'.repeat(64),snapshot:{organizationId:'1',process:{formula:{items:[{formulaItemId:'15',materialId:'20',requiredQty:'2',unitId:'10'}]},route:{operations:[{operationDefId:'12',parameters:[]}]}},ebr:{},materials:[{id:'20',baseUnitId:'10',materialName:'受控原料',weighingPolicy:policy}]}})
 let batch=closed('MainBatch',{...base('100'),batchNo:'BROWSER-BATCH',productionOrderId:'90',productId:'9',plannedQty:'10',unitId:'10',plannedDate:'2026-10-03',processSnapshotId:mode==='release'?null:'130',processSnapshot:mode==='release'?null:snapshot,status:mode==='release'?'DRAFT':'IN_PROGRESS',releasedAt:mode==='release'?null:timestamp,startedAt:mode==='release'?null:timestamp,completedAt:null,subBatches:[],executionUnits:mode==='release'?[]:[execution],allowedActions:mode==='release'?['RELEASE']:['RESERVATIONS']})
 let weighing=closed('Weighing',{...base('103'),executionUnitId:'101',materialLotId:'30',formulaItemId:'15',targetQty:'2',actualQty:'2',unitId:'10',weighedBy:'5',verifiedBy:mode==='charge'?'6':null,status:mode==='charge'?'VERIFIED':'CONFIRMED',scaleEquipmentId:'40',equipmentEvidence:{equipmentId:'40',versionNo:0,equipmentType:'CONFIGURED_SCALE',status:'ACTIVE',calibrationDueDate:'2026-12-31',checkedAt:timestamp,policy:{policyVersion:'INCOMING-WEIGH-1',independentVerificationRequired:true,signatureRequired:true,scaleEquipmentTypes:['CONFIGURED_SCALE'],configurationHash:'c'.repeat(64)}},gateEvidence:[],signatureEvidence:[],allowedActions:mode==='charge'?[]:['VERIFY']})
 const charges:Row[]=[];let verifyAttempts=0;let reauthCalls=0
 await page.route('**/api/v1/**',async route=>{
  const request=route.request(),url=new URL(request.url()),path=url.pathname.replace('/api/v1',''),send=(data:unknown)=>route.fulfill({json:envelope(data)})
  if(path==='/auth/me')return send({userId:'6',organizationId:'1',loginName:'reviewer',displayName:'独立复核员',roleCodes:[],permissionCodes:permissions,mustChangePassword:false})
  if(path==='/main-batches/100'&&request.method()==='GET')return send(batch)
  if(path==='/main-batches'&&request.method()==='GET')return send(paged([batch]))
  if(path==='/production-orders'&&request.method()==='GET')return send(paged([]))
  if(path==='/main-batches/100/execution-units')return send(mode==='release'&&batch.status==='DRAFT'?[]:[execution])
  if(path==='/process-packages')return send(paged([{id:'10',productId:'9',packageCode:'PROC-A',status:'ACTIVE'}]))
  if(path==='/process-packages/10')return send({id:'10',packageCode:'PROC-A',versions:[{id:'11',businessVersion:2,status:'EFFECTIVE'},{id:'99',businessVersion:3,status:'DRAFT'}]})
  if(path==='/ebr/templates')return send(paged([{id:'21',packageVersionId:'11',templateCode:'EBR-A',version:1,status:'EFFECTIVE',contentHash:'d'.repeat(64),effectiveFrom:timestamp,approvedBy:'6',approvedAt:timestamp,versionNo:4,allowedActions:[]}]))
  if(path==='/main-batches/100/release'){
   expect(request.postDataJSON()).toEqual({packageVersionId:'11',ebrTemplateVersionId:'21',reason:'按已发布工艺下达',versionNo:0});expect(request.headers()['if-match']).toBe('"0"');expect(request.headers()['idempotency-key']).toBeTruthy()
   batch=closed('MainBatch',{...batch,versionNo:1,status:'RELEASED',processSnapshotId:'130',processSnapshot:snapshot,releasedAt:timestamp,executionUnits:[execution],allowedActions:['START','RESERVATIONS']});return send(batch)
  }
  if(path==='/execution-units/101')return send(execution)
  if(path==='/execution-units/101/operations')return send([operation])
  if(path==='/execution-units/101/weighings')return send([weighing])
  if(path==='/execution-units/101/material-charges')return send(charges)
  if(path==='/wms/material-lots')return send(paged([{id:'30',materialId:'20',lotNo:'LOT-APPROVED',qualityStatus:'RELEASED',inventoryStatus:'AVAILABLE'}]))
  if(path==='/units/10')return send({id:'10',unitCode:'KG',unitName:'千克',dimension:'MASS',precisionScale:6,status:'ACTIVE',versionNo:0})
  if(path==='/units')return send(paged([{id:'10',unitCode:'KG',unitName:'千克',dimension:'MASS',precisionScale:6,status:'ACTIVE',versionNo:0}]))
  if(path==='/auth/reauth'){reauthCalls++;expect(request.postDataJSON()).toMatchObject({objectType:'WEIGHING_VERIFICATION',objectId:'103:VERIFY:0',recordVersion:0,meaning:'VERIFY',credential:'private-test-password'});return send({reauthToken:`test-only-${reauthCalls}`})}
  if(path==='/weighings/103/verify'){
   verifyAttempts++;expect(request.postDataJSON()).toMatchObject({versionNo:0,reason:'独立检查称量及设备',reauthToken:`test-only-${reauthCalls}`});expect(request.headers()['idempotency-key']).toBeTruthy()
   if(verifyAttempts===1)return route.fulfill({status:422,json:{code:'QUALIFICATION_REQUIRED',message:'当前复核资质不满足要求',data:null,traceId:'production-ui-fixture'}})
   weighing=closed('Weighing',{...weighing,versionNo:1,status:'VERIFIED',verifiedBy:'6',allowedActions:[],signatureEvidence:[{action:'VERIFY',objectType:'WEIGHING_VERIFICATION',objectId:'103:VERIFY:0',recordVersion:0,canonicalRecord:{id:'103'},evidenceIds:['WeighingRecord:103'],signatureId:'150'}]});return send(weighing)
  }
  if(path==='/material-charges'){
   expect(request.postDataJSON()).toEqual({executionUnitId:'101',operationExecutionId:'102',materialLotId:'30',weighingRecordId:'103',chargedQty:'2',unitId:'10',versionNo:4,reason:'按已复核记录投料'});expect(request.headers()['if-match']).toBe('"4"')
   const charge=closed('MaterialCharge',{...base('104'),executionUnitId:'101',operationExecutionId:'102',materialLotId:'30',weighingRecordId:'103',chargedQty:'2',unitId:'10',chargedBy:'6',verifiedBy:'6',chargedAt:timestamp,status:'CONFIRMED',gateEvidence:[],signatureEvidence:[],allowedActions:['REVERSE']});charges.push(charge);return route.fulfill({status:201,json:envelope(charge)})
  }
  if(path==='/trace'){expect(url.searchParams.get('materialLotId')).toBe('30');return send({nodes:[{type:'MAIN_BATCH',id:'100',label:'实际生产批 BROWSER-BATCH',status:'IN_PROGRESS',revision:null},{type:'MATERIAL_LOT',id:'30',label:'已放行来料 LOT-APPROVED',status:'RELEASED',revision:null},{type:'CHARGE',id:'104',label:'实际投料 104',status:'CONFIRMED',revision:null}],edges:[{sourceType:'MAIN_BATCH',sourceId:'100',targetType:'CHARGE',targetId:'104',relation:'ACTUAL_CHARGE'},{sourceType:'CHARGE',sourceId:'104',targetType:'MATERIAL_LOT',targetId:'30',relation:'CONSUMED_FROM'}]})}
  return route.fulfill({status:404,json:{code:'NOT_FOUND',message:path}})
 })
 return errors
}
async function evidence(page:Page,info:TestInfo,name:string,errors:string[]){await expect(page.locator('h1')).toBeVisible();await expect(page.locator('vite-error-overlay')).toHaveCount(0);expect(await page.title()).not.toBe('');expect(errors).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth+2)).toBe(true);await page.screenshot({path:join(tmpdir(),`mes-production-${name}-${info.project.name}.png`),fullPage:false})}
async function inline(page:Page){const labels=page.locator('.incoming-form>label');expect(await labels.count()).toBeGreaterThan(0);expect(await labels.first().evaluate(el=>{const label=el.children[0]!.getBoundingClientRect(),control=el.children[1]!.getBoundingClientRect();return label.right<=control.left+2&&Math.abs(label.top-control.top)<14})).toBe(true)}

test('batch release selects published process and effective eBR identities',async({page},info)=>{const errors=await setup(page,'release');await page.goto('/production/batches/100');await page.getByRole('button',{name:'更多受控操作',exact:true}).hover();await expect(page.locator('.batch-controlled-menu')).toBeVisible();await page.getByRole('button',{name:'下达生产批',exact:true}).click();await page.getByLabel('工艺包版本',{exact:true}).selectOption('11');await expect(page.getByLabel('工艺包版本',{exact:true}).locator('option[value="99"]')).toHaveCount(0);await page.getByLabel('eBR模板版本',{exact:true}).selectOption('21');await page.getByLabel('操作原因',{exact:true}).fill('按已发布工艺下达');await inline(page);await evidence(page,info,'release',errors);await page.getByRole('button',{name:/^提\s*交$/}).click();await expect(page.getByRole('button',{name:'下达生产批',exact:true})).toHaveCount(0);await page.getByRole('tab',{name:'生产执行',exact:true}).click();await expect(page.locator('.ant-tabs-tabpane-active .ant-table-wrapper').filter({has:page.getByRole('columnheader',{name:'操作',exact:true})}).getByText('BROWSER-BATCH/DIRECT',{exact:true})).toBeVisible();await page.getByRole('button',{name:'进入执行',exact:true}).click();await expect(page).toHaveURL(/\/mes\/execution\/101$/)})
test('independent signed weighing verify preserves 422 input and retries fresh credential',async({page},info)=>{const errors=await setup(page,'verify');await page.goto('/mes/execution/101/weighing');await page.getByRole('button',{name:'独立复核',exact:true}).click();await page.getByLabel('操作原因',{exact:true}).fill('独立检查称量及设备');await page.getByLabel('签名密码',{exact:true}).fill('private-test-password');await page.getByRole('button',{name:'签名并复核',exact:true}).click();await expect(page.getByText('当前复核资质不满足要求',{exact:true})).toBeVisible();await expect(page.getByLabel('操作原因',{exact:true})).toHaveValue('独立检查称量及设备');await expect(page.getByLabel('签名密码',{exact:true})).toHaveValue('');await inline(page);await evidence(page,info,'verify-422',errors);await page.getByLabel('签名密码',{exact:true}).fill('private-test-password');await page.getByRole('button',{name:'签名并复核',exact:true}).click();await expect(page.getByRole('button',{name:'独立复核',exact:true})).toHaveCount(0)})
test('charge uses operation concurrency token then navigates bidirectional trace links',async({page},info)=>{const errors=await setup(page,'charge');await page.goto('/mes/execution/101/charge');await page.getByRole('button',{name:'新增投料',exact:true}).click();await page.getByLabel('工序执行',{exact:true}).selectOption('102');await page.getByLabel('物料批次',{exact:true}).selectOption('30');await page.getByLabel('称量记录',{exact:true}).selectOption('103');await page.getByLabel('投料数量',{exact:true}).fill('2');await page.getByLabel('单位',{exact:true}).selectOption('10');await page.getByLabel('操作原因',{exact:true}).fill('按已复核记录投料');await inline(page);await page.getByRole('button',{name:/^提\s*交$/}).click();await page.getByRole('button',{name:'追溯来料质量链',exact:true}).click();await expect(page).toHaveURL(/\/trace\?materialLotId=30$/);await expect(page.getByRole('button',{name:'已放行来料 LOT-APPROVED',exact:true})).toBeVisible();await evidence(page,info,'trace',errors);await page.getByRole('button',{name:'实际生产批 BROWSER-BATCH',exact:true}).click();await page.getByRole('button',{name:'查看来源记录',exact:true}).click();await expect(page).toHaveURL(/\/production\/batches\/100$/)})
