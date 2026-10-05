import {test,expect} from '@playwright/test'
import {mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
// Browser plugin not available; repository Playwright Chromium renders actual Vue pages.
const output=resolve(process.env.TEMP??'C:/Windows/Temp','mes-ui-v2-phase2a')
mkdirSync(output,{recursive:true})
const pilots=[
 {url:'/master/materials',api:'/materials',title:'物料主数据',id:'materialCode',row:{id:'101',materialCode:'MAT-101',materialName:'样板物料',materialType:'RAW',status:'ACTIVE',allowedActions:['UPDATE']}},
 {url:'/production/batches',api:'/main-batches',title:'生产批',id:'batchNo',row:{id:'102',batchNo:'MB-102',productId:'21',plannedQty:100,unitId:'1',status:'DRAFT'}},
 {url:'/quality/inspection-requests',api:'/quality/inspection-requests',title:'请验单',id:'requestNo',row:{id:'103',requestNo:'IR-103',materialLotId:'201',requestType:'INCOMING',status:'DRAFT'}}
]
for(const pilot of pilots)test(`Phase 2A T1 ${pilot.title}`,async({page,isMobile})=>{
 const errors:string[]=[],queries:URL[]=[]
 page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())})
 await page.route('**/api/v1/**',route=>{
  const url=new URL(route.request().url()),path=url.pathname.replace('/api/v1','')
  const reply=(data:unknown)=>route.fulfill({json:{code:'OK',data,message:'success',traceId:'phase2a'}})
  if(path==='/auth/me')return reply({userId:'8',organizationId:'1',displayName:'UI验证',permissionCodes:['master:material:view','master:material:create','master:material:update','production:batch:view','production:batch:create','qms:inspection-request:view','qms:inspection-request:create','wms:material-lot:view'],mustChangePassword:false})
  if(path===pilot.api){queries.push(url);return reply({items:url.searchParams.get('keyword')==='无结果'?[]:[pilot.row],total:url.searchParams.get('keyword')==='无结果'?0:21,page:Number(url.searchParams.get('page')??0),size:20})}
  return reply({items:[{id:'201',lotNo:'LOT-201',materialId:'101',qualityStatus:'QUARANTINE'}],total:1,page:0,size:20})
 })
 await page.goto(pilot.url)
 const main=page.locator('.t1-query-list')
 await expect(main.getByRole('heading',{name:pilot.title,exact:true})).toBeVisible()
 await expect(main.locator('.query-card')).toBeVisible();await expect(main.locator('.result-card')).toBeVisible()
 await expect(main.locator('.t1-business-id')).toHaveText(String(pilot.row[pilot.id as keyof typeof pilot.row]))
 await expect(main.getByText('共 21 条记录')).toBeVisible()
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
 const columns=await main.locator('.query-form').evaluate(el=>getComputedStyle(el).gridTemplateColumns.split(' ').length)
 expect(columns).toBe(isMobile?1:pilot.api==='/main-batches'?3:4)
 const input=main.locator('.query-form input').first()
 await input.fill('样板查询');await main.getByRole('button',{name:/查\s*询/,exact:true}).click()
 await expect.poll(()=>queries.at(-1)?.searchParams.get('keyword')).toBe('样板查询')
 await main.locator('.table-footer .ant-pagination-item-2').click()
 await expect.poll(()=>queries.at(-1)?.searchParams.get('page')).toBe('1')
 await expect(main.locator('.table-footer')).toContainText('第 2 页')
 await main.getByRole('button',{name:/重\s*置/,exact:true}).click();await expect(input).toHaveValue('')
 await expect.poll(()=>queries.at(-1)?.searchParams.get('page')).toBe('0')
 if(pilot.api==='/main-batches'){
  await main.getByLabel('状态',{exact:true}).selectOption('PENDING_QA')
  await main.getByRole('button',{name:/查\s*询/,exact:true}).click()
  await expect.poll(()=>queries.at(-1)?.searchParams.get('status')).toBe('PENDING_QA')
 }
 await page.screenshot({path:resolve(output,`${pilot.id}-${test.info().project.name}.png`),fullPage:true,animations:'disabled'})
 await input.fill('无结果');await main.getByRole('button',{name:/查\s*询/,exact:true}).click()
 await expect(main.getByText('共 0 条记录')).toBeVisible()
 expect((await main.locator('.ant-table-placeholder').boundingBox())!.height).toBeLessThan(120)
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
 await page.screenshot({path:resolve(output,`${pilot.id}-empty-${test.info().project.name}.png`),fullPage:true,animations:'disabled'})
 expect(errors).toEqual([])
})
