import {test,expect} from '@playwright/test'
import {writeFileSync,mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
test.use({channel:'chrome'})
const roleIds=['demo.production','demo.warehouse','demo.sampler','demo.qc.analyst','demo.qc.reviewer','demo.qa','demo.master']
test('existing seven business identities independently authenticate and obey process read permission',async({browser})=>{
 test.setTimeout(190000)
 const evidence:Array<{login:string;permissions:string[];server:number;page:string;editVisible:boolean}>=[]
 for(const username of roleIds){
  const context=await browser.newContext({baseURL:'http://127.0.0.1:5173'})
  try{
   const page=await context.newPage()
   await page.goto('/login')
   const loginPromise=page.waitForResponse(r=>new URL(r.url()).pathname==='/api/v1/auth/login'&&r.request().method()==='POST')
   await page.getByLabel('账号').fill(username)
   await page.getByLabel('密码').fill('DemoUser@2026')
   await page.getByRole('button',{name:/登\s*录/}).click()
   const loginResponse=await loginPromise
   expect(loginResponse.status(),username+' login HTTP status').toBe(200)
   const token=(await loginResponse.json()).data.accessToken as string
   expect(token).toBeTruthy()
   const headers={Authorization:'Bearer '+token}
   const me=await context.request.get('/api/v1/auth/me',{headers})
   expect(me.status()).toBe(200)
   const permissions:string[]=(await me.json()).data.permissionCodes||[]
   const canView=permissions.includes('process:package:view')
   const canEdit=permissions.includes('process:package:edit')
   const response=await context.request.get('/api/v1/process-packages/829',{headers})
   const status=response.status()
   expect(status,username+' GET must match existing role authorization').toBe(canView?200:403)
   await page.goto('/process/packages/829')
   let editVisible=false
   if(canView){
    await expect(page.locator('main.process-page .basic-card')).toBeVisible({timeout:15000})
    editVisible=await page.getByRole('button',{name:'编辑工艺'}).count()>0
    expect(editVisible,username+' edit must match granted permission').toBe(canEdit)
   }else await expect(page).not.toHaveURL(/\/process\/packages\/829$/, {timeout:10000})
   const result={login:username,permissions:permissions.filter(p=>p.startsWith('process:package:')),server:status,page:new URL(page.url()).pathname,editVisible}
   evidence.push(result)
   console.log('MES_SEVEN_ROLE_SESSION='+JSON.stringify(result))
  }finally{await context.close()}
 }
 expect(evidence.length).toBe(roleIds.length)
 const dir=resolve(process.cwd(),'../../docs/development/process-form-cpp-ebr/20261107-formal-g5')
 mkdirSync(dir,{recursive:true})
 writeFileSync(resolve(dir,'role-login-e2e-evidence.json'),JSON.stringify({createdAt:new Date().toISOString(),kind:'Seven independent local business-user login sessions and actual server GET/UI route checks',total:evidence.length,evidence},null,2))
 console.log('SEVEN_ROLE_SESSIONS='+JSON.stringify({total:evidence.length,allowed:evidence.filter(x=>x.server===200).length,denied:evidence.filter(x=>x.server===403).length}))
})