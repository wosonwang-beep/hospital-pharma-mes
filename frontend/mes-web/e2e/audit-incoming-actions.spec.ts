import {test,expect} from '@playwright/test'
import {mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
const out=resolve(process.env.TEMP??'C:/Windows/Temp','mes-audit-incoming-actions');mkdirSync(out,{recursive:true})
const records=[
 ['quality/inspection-requests','qms:inspection-request','submit','提交请验'],
 ['quality/sampling-tasks','qms:sampling','assign','指派'],
 ['quality/samples','qms:test','receive','接收样品'],
 ['quality/inspection-tasks','qms:test','submit-review','提交复核'],
 ['quality/inspection-reports','qms:report','review','复核'],
 ['deviations','qms:deviation','investigate','记录调查'],
 ['qa/material-lots','qa:material-release','release-decisions','QA物料决定'],
] as const
for(const [path,permission,action,label] of records)test(`server actions govern ${path}`,async({page})=>{
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())})
 const labelPattern=new RegExp('^'+label.split('').join('\\s*')+'$')
 let actions:string[]|undefined,canAct=true
 const command=action==='receive'?'qms:sampling:execute':permission+':'+(action==='submit-review'?'execute':action==='release-decisions'?'decide':action)
 await page.route('**/api/v1/**',r=>{
  const p=new URL(r.request().url()).pathname.replace('/api/v1','')
  const data=p==='/auth/me'?{userId:'8',organizationId:'1',displayName:'动作验证',permissionCodes:[permission+':view',...(canAct?[command]:[])],mustChangePassword:false}:{id:'201',status:'DRAFT',versionNo:0,allowedActions:actions}
  return r.fulfill({json:{code:'OK',data,message:'success',traceId:'actions'}})
 })
 await page.goto(`/${path}/201${path==='qa/material-lots'?'/review':''}`);await expect(page.locator('main h1')).toBeVisible()
 await expect(page.locator('main').getByRole('button',{name:labelPattern})).toHaveCount(0)
 actions=[action];await page.reload();await expect(page.locator('main').getByRole('button',{name:labelPattern})).toBeVisible()
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)
 await page.screenshot({path:resolve(out,`${path.split('/').pop()}-${test.info().project.name}.png`),fullPage:true})
 canAct=false;await page.reload();await expect(page.locator('main').getByRole('button',{name:labelPattern})).toHaveCount(0)
 expect(errors).toEqual([])
})
test('nested test evidence uses server actions only',async({page})=>{
 await page.route('**/api/v1/**',r=>{
  const p=new URL(r.request().url()).pathname
  const data=p.endsWith('/auth/me')?{userId:'8',organizationId:'1',displayName:'检验验证',permissionCodes:['qms:test:view','qms:test:execute','qms:test:correct'],mustChangePassword:false}:{id:'201',status:'IN_PROGRESS',versionNo:0,allowedActions:[],items:[{id:'202',itemName:'含量',allowedActions:[],executions:[{id:'203',allowedActions:[],revisions:[{id:'204',resultConclusion:'FAIL',allowedActions:[]}]}]}]}
  return r.fulfill({json:{code:'OK',data,message:'success',traceId:'nested'}})
 })
 await page.goto('/quality/inspection-tasks/201/execute');await expect(page.locator('main h1')).toBeVisible()
 for(const label of ['记录原始检验','记录批准复检','提交原始结果','更正结果（保留原始记录）'])await expect(page.locator('main').getByRole('button',{name:label,exact:true})).toBeHidden()
})
