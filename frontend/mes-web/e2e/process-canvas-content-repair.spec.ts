import {test,expect} from '@playwright/test'
import {randomUUID} from 'node:crypto'
test.use({channel:'chrome'})
test('correct only the isolated DEV EBR draft canvas field placement and preserve CPP identity',async({page})=>{
 test.setTimeout(50000)
 await page.goto('/login')
 const login=page.waitForResponse(r=>new URL(r.url()).pathname==='/api/v1/auth/login'&&r.request().method()==='POST')
 await page.getByLabel('账号').fill('admin')
 await page.getByLabel('密码').fill('admin')
 await page.getByRole('button',{name:/登\s*录/}).click()
 const authResponse=await login
 expect(authResponse.status()).toBe(200)
 const token=(await authResponse.json()).data.accessToken as string
 const base='/api/v1/ebr/templates/663'
 const requestHeaders={Authorization:'Bearer '+token}
 async function fetchDraft(){
  const response=await page.request.get(base,{headers:requestHeaders})
  expect(response.status()).toBe(200)
  return (await response.json()).data
 }
 const original=await fetchDraft()
 expect(original.id).toBe('663')
 expect(original.templateCode).toBe('KCL30-OP1-EBR-261106')
 expect(original.status).toBe('DRAFT')
 expect(original.allowedActions).toContain('EDIT')
 const form=original.definition.forms.find((f:{formCode:string})=>f.formCode==='OP1_RECORD')
 expect(form).toBeTruthy()
 expect(form.fields.map((f:{fieldCode:string})=>f.fieldCode)).toEqual(['MIX_TEMP'])
 const oldMain=form.canvasDocument.data.main as Array<Record<string,any>>
 const signature=(d:any)=>JSON.stringify({rules:d.rules,signatureRules:d.signatureRules,reviewRules:d.reviewRules,forms:d.forms.map((f:any)=>({code:f.formCode,fields:f.fields,operationDefId:f.operationDefId}))})
 const beforeSignature=signature(original.definition)
 if(oldMain[0]?.value==='溶液配制记录'&&oldMain.some((e:any)=>e.control?.conceptId==='MIX_TEMP')){
   console.log('CANVAS_ALREADY_REPAIRED=true')
 }else{
   expect(oldMain).toHaveLength(5)
   expect(oldMain[0]?.control?.conceptId).toBe('MIX_TEMP')
   expect(oldMain[1]?.value).toBe('溶液配制记录')
   expect(oldMain[3]?.value).toBe('生产工序表单记录')
   const fieldControl=structuredClone(oldMain[0]!)
   delete fieldControl.rowFlex
   const definition=structuredClone(original.definition)
   definition.schemaVersion='2.0'
   const formDraft=definition.forms.find((f:{formCode:string})=>f.formCode==='OP1_RECORD')
   formDraft.canvasDocument.data.main=[
     {...oldMain[1]},
     {...oldMain[2]},
     {...oldMain[3]},
     {value:'\n\n'},
     {value:'配制温度：',bold:true},
     fieldControl,
     {value:'\n'}
   ]
   expect(signature(definition)).toBe(beforeSignature)
   const saved=await page.request.put(base,{
     headers:{...requestHeaders,'Idempotency-Key':randomUUID(),'If-Match':'"'+original.versionNo+'"'},
     data:{versionNo:original.versionNo,reason:'调整溶液配制记录中温度字段的页面位置',definition,templateName:original.templateName}
   })
   const result=await saved.json()
   console.log('CONTROLLED_CANVAS_FIX_STATUS='+saved.status()+' RESULT='+JSON.stringify({code:result.code,message:result.message}))
   expect(saved.status()).toBe(200)
 }
 const reread=await fetchDraft()
 expect(signature(reread.definition)).toBe(beforeSignature)
 const finalForm=reread.definition.forms.find((f:{formCode:string})=>f.formCode==='OP1_RECORD')
 const main=finalForm.canvasDocument.data.main
 expect(main[0].value).toBe('溶液配制记录')
 expect(main[2].value).toBe('生产工序表单记录')
 expect(main[4].value).toBe('配制温度：')
 expect(main[5].control.conceptId).toBe('MIX_TEMP')
 expect(reread.definition.rules.some((r:{ruleCode:string;cppConfigJson?:string})=>r.ruleCode==='CPP_OP1_RECORD_1'&&!!r.cppConfigJson)).toBe(true)
 console.log('DEV_CANVAS_PLACEMENT_ROUNDTRIP='+JSON.stringify({template:reread.id,versionNo:reread.versionNo,field:main[5].control.conceptId,ruleCount:reread.definition.rules.length}))
})