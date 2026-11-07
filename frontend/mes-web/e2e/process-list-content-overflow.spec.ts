import {test,expect} from '@playwright/test'
import {mkdirSync} from 'node:fs'
import {resolve} from 'node:path'
test.use({channel:'chrome'})
test('process name and scope keep within columns with full names accessible',async({page})=>{
 test.setTimeout(45000)
 await page.goto('/login')
 if(new URL(page.url()).pathname==='/login'){
  await page.getByLabel('账号').fill('admin')
  await page.getByLabel('密码').fill('admin')
  await page.getByRole('button',{name:/登\s*录/}).click()
  await page.waitForURL(u=>u.pathname!=='/login',{timeout:15000})
 }
 for(const width of [1440,1280,900]){
  await page.setViewportSize({width,height:900})
  await page.goto('/process/packages')
  const row=page.getByRole('row').filter({hasText:'KCL30-CPP-26110601'})
  await expect(row).toBeVisible({timeout:15000})
  const nameCell=row.getByRole('cell').nth(1)
  const data=await nameCell.evaluate(el=>({text:el.textContent,cellWidth:el.clientWidth,cellScrollWidth:el.scrollWidth,html:el.outerHTML.slice(0,680),rect:el.getBoundingClientRect().toJSON()}))
  console.log('PROCESS_NAME_CELL='+JSON.stringify({width,...data}))
  expect(data.text).toContain('氯化钾溶液配制关键参数控制工艺')
  expect(data.cellScrollWidth, 'Complete process name must wrap within its cell').toBeLessThanOrEqual(data.cellWidth+1)
  const output=resolve(process.cwd(),'../../docs/development/process-form-cpp-ebr/20261107-formal-g5')
  mkdirSync(output,{recursive:true})
  await page.screenshot({path:resolve(output,width+'-process-list-name-after.png'),fullPage:true,animations:'disabled'})
 }
})