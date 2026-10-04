import { expect,test,type Page } from '@playwright/test'

const envelope=(data:unknown)=>({code:'OK',message:'success',data,traceId:'mes003-browser'})
const permissions=['master:equipment:view','master:equipment:create','master:equipment:update','master:org:view','master:uom:view','master:qualification:view','audit:view']
async function setup(page:Page,perms=permissions,conflict=false){
 const record={id:'21',orgId:'1',equipmentCode:'EQP-MIX-014',equipmentName:'真空均质乳化机',equipmentType:'生产设备',status:'ACTIVE',calibrationDueDate:'2026-11-18',location:'制剂车间',versionNo:2,updatedAt:'2026-10-03T00:00:00Z',allowedActions:['UPDATE','DISABLE','BEGIN_MAINTENANCE']}
 await page.route('**/api/v1/**',async route=>{
  const req=route.request(),url=new URL(req.url()),path=url.pathname
  if(path==='/api/v1/auth/me')return route.fulfill({json:envelope({userId:'7',organizationId:'1',loginName:'master.tester',displayName:'主数据管理员',roleCodes:['SYSTEM_ADMIN'],permissionCodes:perms,mustChangePassword:false})})
  if(path==='/api/v1/equipment'&&req.method()==='GET')return route.fulfill({json:envelope({items:[record],total:1,page:0,size:20})})
  if(path==='/api/v1/equipment'&&req.method()==='POST'){
   expect(req.headers()['idempotency-key']).toBeTruthy();expect(req.postDataJSON()).not.toHaveProperty('status')
   return route.fulfill({json:envelope({...record,...req.postDataJSON(),id:'22',versionNo:0})})
  }
  if(path.startsWith('/api/v1/equipment/')&&req.method()==='GET')return route.fulfill({json:envelope(record)})
  if(path.startsWith('/api/v1/equipment/')&&req.method()==='PUT'){
   expect(req.headers()['if-match']).toBe('"2"');expect(req.postDataJSON().reason).toBeTruthy()
   if(conflict)return route.fulfill({status:409,json:{code:'VERSION_CONFLICT',message:'Record changed; reload'}})
   return route.fulfill({json:envelope({...record,versionNo:3})})
  }
  return route.fulfill({json:envelope({items:[],total:0,page:0,size:20})})
 })
}
test('equipment query → independent create → detail preserves URL filters',async({page},info)=>{
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())})
 await setup(page);await page.goto('/master/equipment')
 await expect(page.getByRole('heading',{name:'设备与人员资格'})).toBeVisible()
 await expect(page.getByRole('columnheader',{name:/^位置/})).toBeVisible()
 await page.getByLabel('编码 / 关键字').fill('EQP')
 await page.getByRole('button',{name:/查\s*询/}).click();await expect(page).toHaveURL(/keyword=EQP/)
 await page.screenshot({path:info.outputPath('mes003-equipment-query.png'),fullPage:false})
 await page.getByRole('button',{name:'＋ 新增'}).click();await expect(page).toHaveURL(/\/master\/equipment\/create\?/)
 await page.getByLabel('设备编码').fill('EQP-NEW');await page.getByLabel('设备名称').fill('新增设备');await page.getByLabel('设备类型').fill('称量设备')
 await page.getByRole('button',{name:/保\s*存/}).click();await expect(page).toHaveURL(/\/master\/equipment\/22\?/)
 await expect(page.getByRole('heading',{name:'查看设备与人员资格'})).toBeVisible();expect(errors).toEqual([])
})
test('read permission denies direct write route and hides write controls',async({page})=>{
 await setup(page,['master:equipment:view']);await page.goto('/master/equipment')
 await expect(page.getByRole('button',{name:'＋ 新增'})).toHaveCount(0)
 await expect(page.getByRole('button',{name:/编\s*辑/})).toHaveCount(0)
 await page.goto('/master/equipment/create');await expect(page).toHaveURL(/\/$/)
})
test('409 requires reload and preserves entered edit values',async({page})=>{
 await setup(page,permissions,true);await page.goto('/master/equipment/21/edit')
 await page.getByLabel('设备名称').fill('修改后的名称');await page.getByLabel('变更原因').fill('维修后更新')
 await page.getByRole('button',{name:/保\s*存/}).click()
 await expect(page.getByText('记录发生冲突，请重新加载最新版本后再保存。')).toBeVisible()
 await expect(page.getByLabel('设备名称')).toHaveValue('修改后的名称')
 await expect(page.getByRole('button',{name:/保\s*存/})).toBeDisabled()
 await page.getByRole('button',{name:'重新加载'}).click();await expect(page.getByLabel('设备名称')).toHaveValue('真空均质乳化机')
})
