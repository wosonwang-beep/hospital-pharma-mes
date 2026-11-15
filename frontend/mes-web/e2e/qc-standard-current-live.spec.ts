import {test,expect} from '@playwright/test'
test.use({channel:'chrome'})
test('formal QC standard basic-library list and live detail contract',async({page})=>{
 test.setTimeout(65000)
 await page.goto('/login')
 if(new URL(page.url()).pathname==='/login'){
  await page.getByLabel('账号').fill('admin')
  await page.getByLabel('密码').fill('admin')
  await page.getByRole('button',{name:/登\s*录/}).click()
  await page.waitForURL(u=>u.pathname!=='/login',{timeout:18000})
 }
 const failures:Array<{status:number;url:string}>=[]
 page.on('response',res=>{if(res.url().includes('/api/v1/')&&res.status()>=400&&!res.url().includes('/auth/'))failures.push({status:res.status(),url:res.url()})})
 await page.goto('/quality/specifications')
 await expect(page.getByRole('heading',{name:'QC质量标准'})).toBeVisible()
 await expect(page.getByText('检验标准基础信息库',{exact:false})).toBeVisible()
 const rows=page.locator('main.t1-query-list tbody tr:not(.ant-table-placeholder):not(.ant-table-measure-row)')
 await expect(rows.first()).toBeVisible({timeout:20000})
 console.log('QC_BASIC_LIBRARY_ROWS='+await rows.count())
 const texts=await rows.allInnerTexts()
 const forbidden=/(E2E|DBG|DEMO|TEST|测试|演示|模拟|调试)/i
 const candidates=texts.map((line,index)=>({line,index})).filter(x=>!forbidden.test(x.line))
 console.log('QC_DEV_DATA_NONCONFORMING_ROWS='+texts.filter(line=>forbidden.test(line)).length)
 expect(candidates.length).toBeGreaterThan(0)
 const first=rows.nth(candidates[0]!.index)
 console.log('QC_BUSINESS_ROW='+JSON.stringify((await first.innerText()).slice(0,450)))
 const open=first.getByRole('button',{name:/编辑|查看/})
 await open.click()
 await page.waitForURL(url=>/\/quality\/specifications\/\d+$/.test(url.pathname))
 await expect(page.locator('main.qc-editor')).toBeVisible()
 console.log('QC_DETAIL_LABELS='+JSON.stringify((await page.locator('main.qc-editor label').allTextContents()).slice(0,35)))
 console.log('QC_DETAIL_TABS='+JSON.stringify(await page.getByRole('tab').allTextContents()))
 console.log('QC_DETAIL_BUTTONS='+JSON.stringify((await page.locator('main.qc-editor button').allTextContents()).slice(0,25)))
 const noLegacy=await page.getByRole('tab',{name:/版本|审批|签名/}).count()
 expect(noLegacy).toBe(0)
 await expect(page.getByText('维护记录',{exact:true})).toBeVisible()
 const deletion=page.getByRole('button',{name:'删除标准'})
 await expect(deletion).toBeVisible()
 let writes=0
 page.on('request',req=>{if(req.method()==='DELETE'&&req.url().includes('/quality/specifications/'))writes++})
 await deletion.click()
 const dialog=page.getByRole('dialog',{name:'确认删除QC质量标准'})
 await expect(dialog).toBeVisible()
 const confirm=dialog.getByRole('button',{name:'确认删除'})
 await expect(confirm).toBeDisabled()
 await dialog.getByRole('textbox',{name:'删除原因'}).fill('标准修订并经业务评估替代')
 await expect(confirm).toBeEnabled()
 await dialog.getByRole('button',{name:/取\s*消/}).click()
 await expect(dialog).toBeHidden()
 expect(writes).toBe(0)
 expect(failures).toEqual([])
 console.log('QC_DELETE_MODAL_NO_WRITE_PASS=true')
})
test('QC standard detail honors existing read-only role grants without modifying standards',async({browser})=>{
 test.setTimeout(100000)
 const candidates=['demo.qc.analyst','demo.qc.reviewer','demo.qa']
 let viewers=0
 for(const username of candidates){
  const context=await browser.newContext({baseURL:'http://127.0.0.1:5173'})
  try{
   const page=await context.newPage()
   await page.goto('/login')
   const login=page.waitForResponse(r=>new URL(r.url()).pathname==='/api/v1/auth/login'&&r.request().method()==='POST')
   await page.getByLabel('账号').fill(username)
   await page.getByLabel('密码').fill('DemoUser@2026')
   await page.getByRole('button',{name:/登\s*录/}).click()
   const response=await login
   expect(response.status()).toBe(200)
   const token=(await response.json()).data.accessToken as string
   const headers={Authorization:'Bearer '+token}
   const me=await context.request.get('/api/v1/auth/me',{headers})
   const permissions:string[]=(await me.json()).data.permissionCodes||[]
   const read=permissions.includes('qms:specification:view')
   const edit=permissions.includes('qms:specification:edit')
   const remove=permissions.includes('qms:specification:delete')
   const list=await context.request.get('/api/v1/quality/specifications?size=1&page=0',{headers})
   expect(list.status()).toBe(read?200:403)
   if(read){
    const items=(await list.json()).data.items
    if(items.length){
     await page.goto('/quality/specifications/'+items[0].id)
     await page.locator('main.qc-editor').waitFor()
     await expect(page.getByRole('textbox',{name:'标准名称'})).toBeDisabled({timeout:8000}).catch(async e=>{if(!edit)throw e})
     if(!edit){
      await expect(page.getByRole('textbox',{name:'标准编码'})).toBeDisabled()
      await expect(page.getByRole('button',{name:'保存标准'})).toBeDisabled()
      await expect(page.getByRole('button',{name:/添加检验项目/})).toHaveCount(0)
      viewers++
     }
     if(!remove)await expect(page.getByRole('button',{name:'删除标准'})).toHaveCount(0)
    }
   }
   console.log('QC_ROLE_ACCESS='+JSON.stringify({role:username,read,edit,remove,listStatus:list.status()}))
  }finally{await context.close()}
 }
 console.log('QC_READ_ONLY_ROLES_ACTUALLY_VERIFIED='+viewers)
})
