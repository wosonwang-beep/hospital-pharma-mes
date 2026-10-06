import {test,expect} from '@playwright/test'
import {resolve} from 'node:path'
import {mkdirSync} from 'node:fs'
const output=resolve('../../docs/acceptance/workbench-gap-followup-2026-10-06');mkdirSync(output,{recursive:true})
test('T5 operation navigation preserves command ownership and existing evidence',async({page,isMobile},info)=>{
 test.skip(isMobile,'用户已批准 T5 PC-only；本例校验冻结的 PC 参考，不实施移动端 T5。')
 if(info.project.name==='chromium-desktop')await page.setViewportSize({width:1280,height:720})
 let createPermission=false,created:Record<string,unknown>|null=null
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())})
 const names=['称量','投料','混合','灌装','IPC 检验','清场']
 const operations=names.map((_,i)=>({id:String(102+i),executionUnitId:'101',operationDefId:String(12+i),operationSeq:i+1,status:i===0?'COMPLETED':i===1?'IN_PROGRESS':'PENDING',startedAt:i<2?'2026-10-05T08:00:00Z':null,completedAt:i===0?'2026-10-05T08:30:00Z':null,operatorId:i<2?'5':null,equipmentUsages:[],parameterValues:i===1?[{id:'301',parameterDefId:'201',rawValue:'25',textValue:null,unitId:'11',sourceMode:'MANUAL',equipmentId:null,sourceMessageId:null,capturedAt:'2026-10-05T08:45:00Z'},{id:'302',parameterDefId:'202',rawValue:'52',unitId:'13',sourceMode:'MANUAL',capturedAt:'2026-10-05T08:45:00Z'},{id:'303',parameterDefId:'203',textValue:'08:45',unitId:null,sourceMode:'MANUAL',capturedAt:'2026-10-05T08:45:00Z'}]:[],gates:[],gateEvidence:[],versionNo:4,allowedActions:[]}))
 const execution={id:'101',mainBatchId:'100',subBatchId:null,unitType:'DIRECT',executionNo:'PB-20261005-001/DIRECT',status:'IN_PROGRESS',versionNo:0,allowedActions:[]}
 const batch={id:'100',batchNo:'PB-20261005-001',productId:'9',plannedQty:'10000',unitId:'12',status:'IN_PROGRESS',processSnapshotId:'130',processSnapshot:{id:'130',snapshot:{process:{route:{operations:names.map((operationName,i)=>({operationDefId:String(12+i),operationName,parameters:i===1?[{parameterDefId:'201',parameterName:'投料温度'},{parameterDefId:'202',parameterName:'环境湿度',upperLimit:'60',unitId:'13'},{parameterDefId:'203',parameterName:'投料时间'}]:[]}))}},materials:[],ebr:{}}}}
 await page.route('**/api/v1/**',r=>{
  const path=new URL(r.request().url()).pathname.replace('/api/v1','');let data:unknown=[]
  if(path==='/deviations'&&r.request().method()==='POST'){created=r.request().postDataJSON();return r.fulfill({json:{code:'OK',data:{id:'701',...created},message:'success'}})}
  if(path==='/deviations/701')data={id:'701',investigationScope:'PRODUCTION',mainBatchId:'100',operationExecutionId:'103',allowedActions:[],capas:[]}
  if(path==='/auth/me')data={userId:'5',organizationId:'1',displayName:'操作员张三',permissionCodes:['mes:execution:view','mes:operation:view','mes:operation:complete','mes:weigh:view','mes:charge:view','ebr:form:view','production:batch:view','master:product:view','master:uom:view','wms:inventory:view','wms:receipt:view','wms:issue:view','production:order:view','process:package:view','trace:view','master:material:view','master:supplier:view','master:equipment:view','qms:plan:view','qms:test:view','qms:specification:view','qms:inspection-request:view','qms:sampling:view','qms:report:view','qms:deviation:view',...(createPermission?['qms:deviation:create']:[]),'iam:role:view'],mustChangePassword:false}
  else if(path==='/execution-units/101')data=execution
  else if(path==='/main-batches/100')data=batch
  else if(path==='/main-batches')data={items:[batch],total:1,page:0,size:20}
  else if(path==='/main-batches/100/execution-units')data=[execution]
  else if(path==='/quality/production-tests')data={items:[],total:0,page:0,size:20}
  else if(path==='/execution-units/101/operations')data=operations
  else if(path==='/execution-units/101/material-charges')data=[{id:'401',operationExecutionId:'103',materialLotId:'30',weighingRecordId:'501',chargedQty:'10.020',unitId:'10',chargedBy:'5',verifiedBy:'6',chargedAt:'2026-10-05T08:45:00Z',status:'CONFIRMED',gateEvidence:[],signatureEvidence:[],versionNo:0},{id:'402',operationExecutionId:'103',materialLotId:'31',weighingRecordId:'502',chargedQty:'1.000',unitId:'10',chargedBy:'5',chargedAt:'2026-10-05T08:46:00Z',status:'CONFIRMED'},{id:'403',operationExecutionId:'103',materialLotId:'32',weighingRecordId:'503',chargedQty:'0.500',unitId:'10',chargedBy:'5',chargedAt:'2026-10-05T08:47:00Z',status:'CONFIRMED'}]
  else if(path==='/execution-units/101/weighings')data=[{id:'501',targetQty:'10.000',actualQty:'10.020',unitId:'10',status:'VERIFIED'},{id:'502',targetQty:'1.000',actualQty:'1.000',unitId:'10',status:'VERIFIED'},{id:'503',targetQty:'0.500',actualQty:'0.500',unitId:'10',status:'VERIFIED'}]
  else if(path==='/products/9')data={id:'9',productName:'复方软膏',specification:'50 g/支'}
  else if(path==='/units/11')data={id:'11',unitName:'摄氏度'}
  else if(path==='/units/13')data={id:'13',unitName:'%'}
  else if(path==='/units/12')data={id:'12',unitName:'支'}
  else if(path==='/units/10')data={id:'10',unitName:'千克'}
  else if(path==='/wms/material-lots/30')data={id:'30',lotNo:'ML-20261005-001',materialSnapshot:{materialName:'原料A',specification:'25 kg/桶'}}
  else if(path==='/wms/material-lots/31')data={id:'31',lotNo:'ML-20261005-002',materialSnapshot:{materialName:'原料B',specification:'10 kg/桶'}}
  else if(path==='/wms/material-lots/32')data={id:'32',lotNo:'ML-20261005-003',materialSnapshot:{materialName:'辅料C',specification:'5 kg/袋'}}
  return r.fulfill({json:{code:'OK',data,message:'success',traceId:'t5-visual'}})
 })
 await page.goto('/mes/execution/101')
 await expect(page.getByRole('heading',{name:'生产执行工作台',exact:true})).toBeVisible()
 await expect(page.locator('.progress-value')).toContainText('整体进度 1 / 6')
 await expect(page.getByTitle('已完成 1 / 6 工序',{exact:true})).toBeVisible()
 await expect(page.getByText('复方软膏',{exact:false})).toBeVisible()
 const completion=page.locator('.quick-actions').getByRole('button',{name:'完成当前步骤',exact:true})
 await expect(completion).toBeVisible()
 await expect(page.getByRole('tab')).toHaveText(['执行记录','物料信息','设备信息','工艺参数','IPC','异常记录'])
 await expect(page.locator('.charge-facts').getByText('操作员张三',{exact:true})).toBeVisible()
 await expect(page.locator('.execution-user')).toContainText('操作员张三')
 await expect(page.locator('.batch-context')).toContainText('50 g/支')
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
 if(info.project.name==='chromium-desktop'){const box=await page.locator('.execution-workbench').boundingBox();expect(box!.x).toBe(191)}
 const columns=await page.locator('.workbench-grid').evaluate(el=>Array.from(el.children).map(c=>{const r=c.getBoundingClientRect();return {x:r.x,y:r.y,width:r.width,height:r.height}}))
 expect(columns[0]!.y).toBe(columns[1]!.y);expect(columns[1]!.y).toBe(columns[2]!.y)
 expect(columns[0]!.x+columns[0]!.width).toBeLessThan(columns[1]!.x)
 expect(columns[1]!.x+columns[1]!.width).toBeLessThan(columns[2]!.x)
 expect(columns[1]!.height).toBeCloseTo(254,0)
 // Geometry is measured from the supplied 1280x720 reference, not inferred from CSS.
 const referenceGeometry=await page.evaluate(()=>({sidebar:document.querySelector('.sidebar')!.getBoundingClientRect().width,header:document.querySelector('.header')!.getBoundingClientRect().height,workspace:document.querySelector('.workbench-grid')!.getBoundingClientRect().y,support:document.querySelector('.support-grid')!.getBoundingClientRect().y}))
 expect(referenceGeometry.sidebar).toBe(180);expect(referenceGeometry.header).toBe(38)
 expect(Math.abs(referenceGeometry.workspace-298)).toBeLessThanOrEqual(2)
 expect(Math.abs(referenceGeometry.support-560)).toBeLessThanOrEqual(2)
 await expect(page.locator('.instruction-preview')).toHaveCount(0)
 await expect(page.locator('.charge-photo')).toHaveCount(0)
 await expect(page.getByRole('button',{name:'记录偏差',exact:true})).toHaveCount(0)
 await expect(page.locator('.support-grid')).toContainText('范围内')
 await expect(page.locator('.support-grid')).toContainText('未设范围')
 await expect(page.locator('.support-grid .ant-table-tbody').first().locator('tr.ant-table-row')).toHaveCount(3)
 await expect(page.locator('.execution-charge')).toHaveCount(1)
 const supportBottom=await page.locator('.support-grid').evaluate(el=>el.getBoundingClientRect().bottom)
 expect(supportBottom).toBeLessThanOrEqual(710)
 await page.screenshot({path:resolve(output,'workbench-pc-1280x720.png')})
 await page.screenshot({path:resolve(output,`workbench-${info.project.name}.png`),fullPage:true})
 await page.locator('.support-grid').getByRole('button',{name:'查看',exact:true}).nth(1).click()
 await expect(page.getByText('完整记录与审计证据',{exact:true})).toBeVisible()
 await page.getByRole('button',{name:'Close',exact:true}).click()
 await expect(page.locator('.charge-facts')).toContainText('原料B')
 await page.locator('.workbench-heading').getByRole('button',{name:'批记录',exact:true}).click()
 await page.getByRole('button',{name:'完整记录与审计证据',exact:true}).click()
 await expect(page.getByText('完整记录与审计证据',{exact:true})).toBeVisible()
 await expect(page.getByText('完整投料记录与签名',{exact:true})).toHaveCount(3)
 await page.getByRole('button',{name:'Close',exact:true}).click()
 await completion.click()
 await expect(page.getByLabel('操作原因',{exact:true})).toBeVisible()
 await page.getByRole('button',{name:/^取\s*消$/}).click()
 await page.getByRole('tab',{name:'IPC',exact:true}).click()
 await page.getByRole('navigation',{name:'工艺进度'}).getByRole('button').nth(2).click()
 await expect(page.getByRole('button',{name:'完成当前步骤',exact:true})).toHaveCount(0)
 await page.getByRole('tab',{name:'工艺参数',exact:true}).click()
 await page.locator('.workbench-heading').getByRole('button',{name:'工艺指令',exact:true}).click()
 await expect(page.getByText('冻结工艺指令',{exact:true})).toBeVisible()
 await page.getByRole('button',{name:'Close',exact:true}).click()
 createPermission=true
 await page.reload()
 await expect(page.getByRole('button',{name:'记录偏差',exact:true})).toBeVisible()
 await page.screenshot({path:resolve(output,'workbench-with-deviation-pc.png')})
 await page.getByRole('button',{name:'记录偏差',exact:true}).click()
 await expect(page).toHaveURL(/deviations\/create.*mainBatchId=100.*operationExecutionId=103/)
 await expect(page.getByLabel('生产批',{exact:true})).toHaveValue('100')
 await expect(page.getByLabel('工序执行',{exact:true})).toHaveValue('103')
 await page.getByLabel('严重程度',{exact:true}).selectOption('MINOR')
 await page.getByLabel('情况描述',{exact:true}).fill('工序投料观察到异常，建立调查')
 await page.getByLabel('操作原因',{exact:true}).fill('记录实际观察')
 let unsavedPrompt=false
 page.once('dialog',async dialog=>{unsavedPrompt=true;await dialog.dismiss()})
 await page.getByRole('button',{name:'← 返回',exact:true}).click()
 await expect.poll(()=>unsavedPrompt).toBe(true)
 await expect(page).toHaveURL(/deviations\/create/)
 await expect(page.locator('.incoming-reference .error')).toHaveCount(0)
 await page.screenshot({path:resolve(output,'deviation-context-pc.png'),fullPage:true})
 await page.getByRole('button',{name:/^提\s*交$/}).click()
 await expect.poll(()=>created).toMatchObject({investigationScope:'PRODUCTION',mainBatchId:'100',operationExecutionId:'103',investigationKind:'DEVIATION',severity:'MINOR',description:'工序投料观察到异常，建立调查',reason:'记录实际观察'})
 await expect(page).toHaveURL(/deviations\/701/)
 batch.status='QA_RELEASED'
 await page.goto('/mes/execution/101')
 await expect(page.getByRole('heading',{name:'生产执行工作台',exact:true})).toBeVisible()
 await expect(page.getByRole('button',{name:'记录偏差',exact:true})).toHaveCount(0)
 expect(errors).toEqual([])
})
