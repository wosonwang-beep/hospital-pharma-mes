import {test,expect} from '@playwright/test'
import {mkdirSync,readFileSync} from 'node:fs'
import {resolve} from 'node:path'
const contract=JSON.parse(readFileSync(new URL('../src/views/quality/incoming-contract.json',import.meta.url),'utf8'))

const output=resolve(process.env.TEMP??'C:/Windows/Temp','mes-incoming-t3-detail');mkdirSync(output,{recursive:true})
type Row=Record<string,unknown>
const schemas=contract.schemas as unknown as Record<string,{required?:string[]}>
const meta={orgId:'1',createdBy:'8',createdAt:'2026-10-05T08:00:00Z',updatedBy:'8',updatedAt:'2026-10-05T09:00:00Z',versionNo:2,allowedActions:[],signingTargets:[],signatureEvidence:[]}
function record(schema:string,value:Row):Row{
 const base:Row={...meta};for(const key of schemas[schema]?.required??[])if(!(key in base))base[key]=null
 return {...base,...value}
}
const signature=(id:string,type:string)=>{
 const objectId:Record<string,string>={'901':'221:1','902':'222:1','903':'201','904':'31','905':'31','906':'71','907':'91'}
 const slots:Record<string,string>={SAMPLING_PLAN:'plan',SAMPLING_TASK:'completion',INSPECTION_TASK:'review',DEVIATION_CLOSE:'close',INSPECTION_REPORT:'approval'}
 return {slot:slots[type]??'',signatureId:id,objectType:type,objectId:objectId[id]??'201',recordVersion:1,meaning:['SAMPLING_PLAN','DEVIATION_CLOSE','INSPECTION_REPORT'].includes(type)?'APPROVE':'VERIFY'}
}
const original=record('TestResultRevision',{id:'301',sampleId:'41',inspectionItemId:'211',testExecutionId:'221',testCode:'ASSAY',revisionNo:1,previousRevisionId:null,resultNumeric:'90.000000',resultText:null,resultUnitId:'11',resultConclusion:'FAIL',effectiveConclusion:'INVALID',dispositionInvestigationId:'71',reasonForChange:null,recordedBy:'8',recordedAt:'2026-10-05T08:30:00Z',signatureId:'901',signatureEvidence:[signature('901','TEST_RESULT_REVISION')]})
const selected=record('TestResultRevision',{...original,id:'302',inspectionItemId:'212',testExecutionId:'222',resultNumeric:'99.000000',resultConclusion:'PASS',effectiveConclusion:'PASS',dispositionInvestigationId:null,signatureId:'902',signatureEvidence:[signature('902','TEST_RESULT_REVISION')]})
const item=record('InspectionItem',{id:'211',inspectionTaskId:'201',qcSpecificationItemId:'511',itemCode:'ASSAY',itemName:'含量测定',required:true,resultType:'NUMERIC',lowerLimit:'95.000000',upperLimit:'105.000000',unitId:'11',textAcceptanceCriteria:null,methodCode:'HPLC-01',methodVersion:'2.0',executions:[record('TestExecution',{id:'221',inspectionItemId:'211',attemptNo:1,originalExecutionId:null,approvedInvestigationId:null,instrumentId:'81',performedBy:'8',startedAt:'2026-10-05T08:00:00Z',completedAt:'2026-10-05T08:30:00Z',rawData:{reading:'90.000000'},observation:'原始峰面积偏低',calculationInput:{weight:'1.000000'},revisions:[original]})]})
const task=record('InspectionTask',{id:'201',inspectionTaskNo:'IT-20261005-001',inspectionRequestId:'21',sampleId:'41',assignedTo:'8',status:'QC_FAILED',recordStatus:'REVIEWED',startedAt:'2026-10-05T08:00:00Z',submittedAt:'2026-10-05T08:40:00Z',reviewedBy:'9',reviewedAt:'2026-10-05T09:00:00Z',reviewedResultIds:['301'],reviewSignatureId:'903',signatureEvidence:[signature('903','INSPECTION_TASK')],items:[item]})
const retest={...task,id:'202',inspectionTaskNo:'IT-20261005-002',status:'QC_PASSED',reviewedResultIds:['302'],items:[{...item,id:'212',inspectionTaskId:'202',executions:[{...(item.executions as Row[])[0],id:'222',inspectionItemId:'212',originalExecutionId:'221',approvedInvestigationId:'71',revisions:[selected]}]}]}
const request=record('InspectionRequest',{id:'21',requestNo:'IR-20261005-001',materialLotId:'61',receiptId:'62',receiptItemId:'63',qcSpecificationVersionId:'51',specificationContentHash:'a'.repeat(64),requestType:'INITIAL',status:'COMPLETED',recordStatus:'EFFECTIVE',requestedQuantity:'10.000000',requestedUnitId:'11',requestedPackageCount:2,requestedDate:'2026-10-05',priority:'NORMAL',requestedBy:'8',reason:'到货请验',submittedBy:'8',submittedAt:'2026-10-05T07:00:00Z',acceptedBy:'9',acceptedAt:'2026-10-05T07:20:00Z',items:[{id:'22',inspectionRequestId:'21',materialLotId:'61',itemNo:1}]})
const sample=record('Sample',{id:'41',sampleNo:'SM-20261005-001',sampleScope:'INCOMING_MATERIAL',sampleType:'TEST_SAMPLE',materialLotId:'61',samplingTaskId:'31',samplingDetailId:'32',inspectionRequestItemId:'22',status:'TEST_COMPLETED',quantity:'10.000000',unitId:'11',sampledAt:'2026-10-05T07:40:00Z',receivedBy:'8',receivedAt:'2026-10-05T07:50:00Z',storageLocation:'QC样品柜A'})
const sampling=record('SamplingTask',{id:'31',samplingTaskNo:'ST-20261005-001',inspectionRequestId:'21',inspectionRequestItemId:'22',samplingPlan:'按批准方案从两个容器取样',requiredPackageCount:2,status:'COMPLETED',recordStatus:'EFFECTIVE',assignedTo:'8',planSignatureId:'904',completionSignatureId:'905',signatureEvidence:[signature('904','SAMPLING_PLAN'),signature('905','SAMPLING_TASK')],startedAt:'2026-10-05T07:30:00Z',completedAt:'2026-10-05T07:45:00Z',details:[{id:'32',samplingTaskId:'31',detailNo:1,containerNo:'C-001',samplingPoint:'上层',sampleQuantity:'10.000000',unitId:'11',sampledAt:'2026-10-05T07:40:00Z',sampledBy:'8',packageResealed:true,samples:[sample]}]})
const investigation=record('Deviation',{id:'71',deviationNo:'OOS-20261005-001',investigationScope:'INCOMING_MATERIAL',investigationKind:'OOS',materialLotId:'61',testExecutionId:'221',originalResultRevisionId:'301',severity:'MAJOR',status:'CLOSED',description:'原始含量结果低于标准',investigationSummary:'完成仪器调查并批准复检',decisionCode:'AUTHORIZE_RETEST',authorizedRetestCount:1,originalResultDisposition:'INVALID',selectedResultRevisionId:'302',closeReason:'原始结果经调查批准无效，保留原始失败记录',decidedBy:'9',closedBy:'9',decidedAt:'2026-10-05T10:00:00Z',closedAt:'2026-10-05T11:00:00Z',signatureEvidence:[signature('906','DEVIATION_CLOSE')]})
const report=record('InspectionReport',{id:'91',reportNo:'RPT-20261005-001',inspectionRequestId:'21',status:'APPROVED',recordStatus:'APPROVED',overallResult:'PASS',evidenceDigest:'b'.repeat(64),reviewedBy:'9',approvedBy:'10',reviewedAt:'2026-10-05T12:00:00Z',approvedAt:'2026-10-05T12:30:00Z',signatureEvidence:[signature('907','INSPECTION_REPORT')],items:[{id:'92',qcSpecificationItemId:'511',inspectionItemId:'212',resultRevisionId:'302',originalResultRevisionId:'301',investigationId:'71',result:selected,originalResult:original}]})
const release=record('ReleaseReview',{materialLotId:'61',qualityStatus:'PENDING_QA_RELEASE',inventoryStatus:'FROZEN',eligibleForRelease:false,gateReasons:[{code:'INVENTORY_FROZEN',message:'INVENTORY_FROZEN',evidenceIds:[]}],reports:[report],investigations:[investigation],decisions:[],evidenceDigest:'c'.repeat(64)})
const cases=[
 {path:'/quality/inspection-requests/21',record:request,text:'请验事实与冻结标准'},
 {path:'/quality/sampling-tasks/31',record:sampling,text:'取样执行与样品分配'},
 {path:'/quality/samples/41',record:sample,text:'样品身份与来源'},
 {path:'/quality/inspection-tasks/201',record:task,text:'检验结论与复核'},
 {path:'/quality/inspection-reports/91',record:report,text:'检验结论'},
 {path:'/deviations/71',record:investigation,text:'原始异常事实'},
 {path:'/qa/material-lots/61/review',record:release,text:'放行条件与阻断检查'},
]
const permissions=['qms:inspection-request:view','qms:sampling:view','qms:test:view','qms:report:view','qms:deviation:view','qa:material-release:view','wms:inventory:view','wms:receipt:view','qms:specification:view','iam:user:view','master:uom:view','master:equipment:view','audit:view','trace:view']
for(const scenario of cases)test(`complete read-model detail ${scenario.path}`,async({page})=>{
 if(test.info().project.name==='chromium-desktop')await page.setViewportSize({width:1280,height:853})
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())})
 await page.route('**/api/v1/**',route=>{
  const url=new URL(route.request().url()),path=url.pathname.replace('/api/v1','');let data:unknown
  if(path==='/auth/me')data={userId:'8',organizationId:'1',displayName:'来料质量审核员',permissionCodes:permissions,mustChangePassword:false}
  else if(path==='/quality/inspection-tasks')data={items:[task,retest],total:2,page:0,size:50}
  else if(path.startsWith('/quality/signature-evidence/')){const m=signature(path.split('/').pop()!,'INSPECTION_TASK');data={objectType:m.objectType,objectId:m.objectId,recordVersion:m.recordVersion,canonicalRecord:{reviewedResultIds:['301'],reviewedBy:'9',meaning:m.meaning},evidenceIds:['301']}}
  else if(path.startsWith('/users/'))data={id:path.split('/').pop(),displayName:path.endsWith('/8')?'检验员李工':path.endsWith('/10')?'审批人赵主任':'复核员王工'}
  else if(path.startsWith('/units/'))data={id:'11',unitName:'毫克',unitCode:'mg'}
  else if(path.startsWith('/equipment/'))data={id:'81',equipmentName:'高效液相色谱仪',equipmentCode:'EQ-081'}
  else if(path==='/wms/material-lots/61')data={id:'61',lotNo:'ML-20261005-001',supplierLotNo:'SUP-001',materialSnapshot:{materialName:'原料A'},qualityStatus:'PENDING_QA_RELEASE',inventoryStatus:scenario.path.startsWith('/qa/')?'FROZEN':'AVAILABLE'}
  else if(path==='/wms/receipts/62')data={id:'62',receiptNo:'RCV-20261005-001'}
  else if(path==='/quality/specification-versions/51')data={id:'51',versionNoBusiness:3,status:'RETIRED',items:[{id:'511',itemName:'含量测定'}]}
  else if(path==='/quality/inspection-requests/21')data=request
  else if(path==='/quality/sampling-tasks/31')data=sampling
  else if(path==='/quality/samples/41')data=sample
  else if(path==='/quality/inspection-reports/91')data=report
  else if(path==='/deviations/71')data=investigation
  else if(path==='/qa/material-lots/61/release-review')data=release
  else if(path==='/quality/inspection-tasks/201')data=task
  else data={items:[],total:0,page:0,size:50}
  return route.fulfill({json:{code:'OK',data,message:'success',traceId:'detail-presentation'}})
 })
 await page.goto(scenario.path);await expect(page.getByText(scenario.text,{exact:true})).toBeVisible()
 await expect(page.getByText('详情响应不完整或状态不符合当前契约，请重新加载并核对数据来源。',{exact:true})).toHaveCount(0)
 await expect(page.getByRole('button',{name:'审计追踪',exact:true})).toBeVisible()
 if(scenario.record.inspectionRequestId&&!scenario.path.includes('inspection-tasks')&&!scenario.path.includes('inspection-reports')&&!scenario.path.startsWith('/qa/'))await expect(page.getByText('IR-20261005-001',{exact:true}).first()).toBeVisible()
 if(!scenario.path.includes('inspection-tasks')&&!scenario.path.includes('inspection-reports')&&!scenario.path.startsWith('/qa/')){await expect(page.getByText('记录元数据',{exact:true})).toBeHidden();await expect(page.getByText('电子签名记录',{exact:true})).toBeHidden();if(scenario.path.startsWith('/deviations/')){await page.getByRole('button',{name:/更多记录与审计证据/}).click();await expect(page.getByText('原始结果与最终选定结果证据',{exact:true})).toBeVisible();await page.getByRole('button',{name:/更多记录与审计证据/}).click()}}
 if(scenario.path.includes('inspection-tasks')){
  await expect(page.getByRole('button',{name:'IR-20261005-001',exact:true})).toBeVisible()
  await expect(page.getByText('95.000000 – 105.000000',{exact:true})).toBeVisible()
  await expect(page.getByText('原始峰面积偏低',{exact:true})).toBeHidden()
  await expect(page.getByRole('button',{name:'提交复核',exact:true})).toHaveCount(0)
 }
 if(scenario.path.includes('inspection-reports')){await expect(page.getByText('90.000000',{exact:true})).toBeHidden();await expect(page.getByRole('table').getByText('99.000000',{exact:true})).toBeVisible();await expect(page.getByText('95.000000 – 105.000000',{exact:true})).toBeVisible()}
 if(scenario.path.startsWith('/qa/')){await expect(page.getByText('物料批库存已冻结',{exact:true})).toBeVisible();await expect(page.locator('[data-ui-template="T6"]').first()).toBeVisible()}
 await expect.poll(()=>page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
 const name=scenario.path.startsWith('/deviations/')?'investigations':scenario.path.split('/')[2]!
 await page.screenshot({path:resolve(output,`${name}-${test.info().project.name}.png`),fullPage:true})
 if(scenario.path.includes('inspection-reports')){
  await page.screenshot({path:resolve(output,`report-preview-${test.info().project.name}.png`),fullPage:false})
  await page.getByRole('button',{name:/更多记录与审计证据/}).click()
  await expect(page.getByText('90.000000',{exact:true})).toBeVisible()
  await expect(page.getByText('项目汇总：原始结果与报告选定结果',{exact:true})).toBeVisible()
  await page.screenshot({path:resolve(output,`report-evidence-${test.info().project.name}.png`),fullPage:true})
 }
 if(scenario.path.includes('inspection-tasks'))await page.screenshot({path:resolve(output,`inspection-preview-${test.info().project.name}.png`),fullPage:false})
 if(scenario.path.includes('inspection-tasks')){
  await page.getByRole('button',{name:'查看详情',exact:true}).first().click()
  await expect(page.getByText('含量测定 · 完整检验详情',{exact:true})).toBeVisible()
  await expect(page.getByText('原始峰面积偏低',{exact:true}).last()).toBeVisible()
  await expect(page.getByText('高效液相色谱仪（ID 81）',{exact:true}).last()).toBeVisible()
  await page.screenshot({path:resolve(output,`inspection-item-${test.info().project.name}.png`),fullPage:false})
  await page.getByRole('button',{name:'Close',exact:true}).click()
  await page.getByRole('button',{name:/更多记录与审计证据/}).click()
  await expect(page.getByText('检验项目、标准快照与执行结果',{exact:true})).toBeVisible()
  await page.screenshot({path:resolve(output,`inspection-evidence-${test.info().project.name}.png`),fullPage:true})
  await page.getByRole('button',{name:'查看签名证据',exact:true}).first().click()
  await expect(page.getByText('电子签名证据（只读）',{exact:true})).toBeVisible()
  await expect(page.getByText('签名时的记录快照',{exact:true})).toBeVisible()
  expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
  await page.screenshot({path:resolve(output,`signature-${test.info().project.name}.png`),fullPage:true})
  await page.getByRole('button',{name:'Close',exact:true}).click()
 }
 await page.getByRole('button',{name:'审计追踪',exact:true}).click()
 await expect(page).toHaveURL(/\/audit\?/)
 const audit=new URL(page.url()),types:Record<string,string>={'inspection-requests':'qms_inspection_request','sampling-tasks':'qms_sampling_task',samples:'qms_sample','inspection-tasks':'qms_inspection_task','inspection-reports':'qms_inspection_report',investigations:'qms_deviation','material-lots':'MaterialLot'}
 expect(audit.searchParams.get('objectType')).toBe(types[name])
 expect(audit.searchParams.get('objectId')).toBe(String(scenario.record.id??scenario.record.materialLotId))
 expect(errors).toEqual([])
})

test('incomplete inspection response is visibly identified and does not masquerade as a valid draft',async({page})=>{
 await page.route('**/api/v1/**',r=>r.fulfill({json:{code:'OK',data:r.request().url().endsWith('/auth/me')?{userId:'8',organizationId:'1',displayName:'只读检验员',permissionCodes:['qms:test:view'],mustChangePassword:false}:{id:'201',status:'DRAFT',versionNo:0,allowedActions:[]},message:'success',traceId:'incomplete'}}))
 await page.goto('/quality/inspection-tasks/201')
 await expect(page.getByText('详情响应不完整或状态不符合当前契约，请重新加载并核对数据来源。',{exact:true})).toBeVisible()
 await expect(page.getByText('未识别状态（DRAFT）',{exact:true}).first()).toBeVisible()
 await expect(page.getByRole('button',{name:'提交复核',exact:true})).toHaveCount(0)
})

test('read-only role keeps original IDs without requesting forbidden master or signature APIs',async({page})=>{
 const requests:string[]=[]
 await page.route('**/api/v1/**',r=>{
  const path=new URL(r.request().url()).pathname.replace('/api/v1','');requests.push(path)
  return r.fulfill({json:{code:'OK',data:path==='/auth/me'?{userId:'8',organizationId:'1',displayName:'只读检验员',permissionCodes:['qms:test:view'],mustChangePassword:false}:path==='/quality/samples/41'?sample:task,message:'success',traceId:'read-only'}})
 })
 await page.goto('/quality/inspection-tasks/201')
 await expect(page.getByText('检验结论与复核',{exact:true})).toBeVisible()
 await page.getByRole('button',{name:/更多记录与审计证据/}).click()
 await page.getByText('关联信息读取说明',{exact:true}).click()
 await expect(page.getByText('指派人员：无关联读取权限，保留原始ID。',{exact:true})).toBeVisible()
 await expect(page.getByRole('button',{name:'查看签名证据',exact:true})).toHaveCount(0)
 // Sample reads share qms:test:view and remain permitted; IAM/WMS/signature reads do not.
 expect(requests.every(p=>['/auth/me','/quality/inspection-tasks/201','/quality/samples/41'].includes(p))).toBe(true)
})
