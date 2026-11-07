import {test,expect} from '@playwright/test'
test.use({channel:'chrome'})
test('inspect the existing EBR canvas document field placement without mutation',async({page})=>{
 test.setTimeout(45000)
 await page.goto('/login')
 if(new URL(page.url()).pathname==='/login'){
   await page.getByLabel('账号').fill('admin')
   await page.getByLabel('密码').fill('admin')
   await page.getByRole('button',{name:/登\s*录/}).click()
   await page.waitForURL(u=>u.pathname!=='/login',{timeout:15000})
 }
 const response=page.waitForResponse(r=>new URL(r.url()).pathname==='/api/v1/ebr/templates/663'&&r.status()===200)
 await page.goto('/ebr/templates/663/designer?processPackageId=829&formCode=OP1_RECORD&designerTab=form&returnTo=/process/packages/829')
 const detail=(await(await response).json()).data
 const form=detail.definition.forms.find((f:{formCode:string})=>f.formCode==='OP1_RECORD')
 expect(form).toBeTruthy()
 const elements=form.canvasDocument?.data?.main||[]
 console.log('CANVAS_MAIN='+JSON.stringify(elements.map((x:Record<string,unknown>,index:number)=>({index,value:x.value,type:x.type,control:x.control,extension:x.extension,rowFlex:x.rowFlex,size:x.size,bold:x.bold,conceptId:x.conceptId}))).slice(0,6500))
 console.log('CANVAS_FIELDS='+JSON.stringify(form.fields.map((f:{fieldCode:string;label:string})=>({code:f.fieldCode,label:f.label}))))
 expect(form.fields).toContainEqual(expect.objectContaining({fieldCode:'MIX_TEMP'}))
})