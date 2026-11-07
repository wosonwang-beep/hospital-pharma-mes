import {test,expect} from '@playwright/test'
import {mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
test.use({channel:'chrome'})
test('corrected DEV EBR canvas draws temperature field beneath document title in three PC viewports',async({page})=>{
 test.setTimeout(60000)
 const output=resolve(process.cwd(),'../../docs/development/process-form-cpp-ebr/20261106-formal-code/g5-canvas-corrected')
 mkdirSync(output,{recursive:true})
 await page.goto('/login')
 if(new URL(page.url()).pathname==='/login'){
  await page.getByLabel('账号').fill('admin')
  await page.getByLabel('密码').fill('admin')
  await page.getByRole('button',{name:/登\s*录/}).click()
  await page.waitForURL(u=>u.pathname!=='/login',{timeout:15000})
 }
 for(const width of [1440,1280,900]){
  await page.setViewportSize({width,height:900})
  const response=page.waitForResponse(r=>new URL(r.url()).pathname==='/api/v1/ebr/templates/663'&&r.status()===200)
  await page.goto('/ebr/templates/663/designer?processPackageId=829&formCode=OP1_RECORD&designerTab=form&returnTo=/process/packages/829')
  await expect(page.locator('.canvas-ebr-designer')).toBeVisible()
  const data=(await(await response).json()).data
  const form=data.definition.forms.find((f:{formCode:string})=>f.formCode==='OP1_RECORD')
  const elements=form.canvasDocument.data.main
  expect(elements[0].value).toBe('溶液配制记录')
  expect(elements[2].value).toBe('生产工序表单记录')
  expect(elements[4].value).toBe('配制温度：')
  expect(elements[5].control.conceptId).toBe('MIX_TEMP')
  await page.locator('.canvas-stage').first().waitFor({timeout:10000})
  await page.waitForTimeout(350)
  const metrics=await page.evaluate(()=>({width:document.documentElement.clientWidth,documentWidth:document.documentElement.scrollWidth,canvasCount:document.querySelectorAll('.canvas-stage').length}))
  expect(metrics.documentWidth).toBeLessThanOrEqual(width+1)
  const filepath=resolve(output,width+'-ebr-form-canvas-after.png')
  await page.screenshot({path:filepath,fullPage:true,animations:'disabled'})
  console.log('CANVAS_G5_AFTER='+JSON.stringify({width,metrics,filepath}))
 }
})