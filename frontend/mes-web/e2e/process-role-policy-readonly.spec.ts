import {test,expect} from '@playwright/test'
import {writeFileSync,mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
test.use({channel:'chrome'})
test('read current process grants for existing local MES business roles without modifying permissions',async({page})=>{
 test.setTimeout(55000)
 await page.goto('/login')
 const login=page.waitForResponse(r=>new URL(r.url()).pathname==='/api/v1/auth/login'&&r.request().method()==='POST')
 await page.getByLabel('账号').fill('admin')
 await page.getByLabel('密码').fill('admin')
 await page.getByRole('button',{name:/登\s*录/}).click()
 const auth=await login
 expect(auth.status()).toBe(200)
 const token=(await auth.json()).data.accessToken as string
 const headers={Authorization:'Bearer '+token}
 const responses=await Promise.all([
  page.request.get('/api/v1/users?page=0&size=100',{headers}),
  page.request.get('/api/v1/roles?page=0&size=100',{headers})
 ])
 for(const response of responses)expect(response.status()).toBe(200)
 const users=(await responses[0]!.json()).data
 const roles=(await responses[1]!.json()).data
 console.log('ROLE_POLICY_FIELDS='+JSON.stringify({usersTotal:users.total,rolesTotal:roles.total,firstUserKeys:Object.keys(users.items[0]||{}),firstRoleKeys:Object.keys(roles.items[0]||{})}))
 const chosen=['demo.production','demo.warehouse','demo.sampler','demo.qc.analyst','demo.qc.reviewer','demo.qa','demo.master']
 const businessUsers=users.items.filter((u:any)=>chosen.includes(u.username))
 const roleMap=new Map(roles.items.map((x:any)=>[String(x.id),x]))
 const policyEvidence:Array<{role:string;roleCodes:string[];rights:string[]}> = []
 for(const user of businessUsers){
  const grantRoles=(user.roleIds||[]).map((id:string)=>roleMap.get(String(id)))
  const directRights=(user.permissionCodes||[]).filter((p:string)=>p.startsWith('process:package:'))
  const rights=[...new Set([...directRights,...grantRoles.flatMap((r:any)=>r?.permissionCodes?.filter((p:string)=>p.startsWith('process:package:'))||[])])]
  const item={role:user.username,roleCodes:grantRoles.map((r:any)=>r?.roleCode||''),rights}
  policyEvidence.push(item)
  console.log('PROCESS_ROLE_POLICY='+JSON.stringify(item))
 }
 expect(businessUsers.length).toBe(chosen.length)
 expect(policyEvidence.length).toBe(chosen.length)
 const dir=resolve(process.cwd(),'../../docs/development/process-form-cpp-ebr/20261107-formal-g5')
 mkdirSync(dir,{recursive:true})
 writeFileSync(resolve(dir,'role-policy-evidence.json'),JSON.stringify({createdAt:new Date().toISOString(),scope:'Read-only admin IAM configuration inventory, not seven independent logins',total:policyEvidence.length,roles:policyEvidence},null,2))
})