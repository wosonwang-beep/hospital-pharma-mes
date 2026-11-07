import {test,expect} from '@playwright/test'
test.use({channel:'chrome'})
test('process workspace tabs respond to keyboard arrows and product picker restores focus after Escape',async({page})=>{
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
  await page.goto('/process/packages/829/edit')
 await expect(page.locator('.workspace-footer [role="status"][aria-live="polite"]')).toContainText('当前配置已生效')
 const ops=page.getByRole('tab',{name:/工序与记录表单/})
 const products=page.getByRole('tab',{name:/适用产品/})
 await expect(ops).toHaveAttribute('aria-selected','true')
 await ops.focus()
 await page.keyboard.press('ArrowRight')
 await expect(products).toHaveAttribute('aria-selected','true')
 await expect(products).toBeFocused()
 const keyboardFocusStyle=await products.evaluate(el=>({outlineWidth:getComputedStyle(el).outlineWidth,outlineStyle:getComputedStyle(el).outlineStyle,outlineColor:getComputedStyle(el).outlineColor}))
 console.log('KEYBOARD_FOCUS_STYLE='+JSON.stringify(keyboardFocusStyle))
 expect(parseFloat(keyboardFocusStyle.outlineWidth)).toBeGreaterThanOrEqual(2)
 expect(keyboardFocusStyle.outlineStyle).not.toBe('none')
 await page.keyboard.press('Home')
 await expect(ops).toHaveAttribute('aria-selected','true')
 await expect(ops).toBeFocused()
 await page.keyboard.press('End')
 await expect(products).toBeFocused()
 const picker=page.getByRole('button',{name:/选择产品/}).last()
 await picker.focus()
 await page.keyboard.press('Enter')
 const dialog=page.getByRole('dialog',{name:'选择适用产品'})
 await expect(dialog).toBeVisible()
 await expect(dialog.getByRole('button',{name:/确\s*认/})).toBeVisible()
 await expect(dialog.getByRole('button',{name:/取\s*消/})).toBeVisible()
 await expect(dialog.getByRole('combobox',{name:'适用产品',exact:true})).toBeFocused()
 const focusTrace:string[]=[]
 for(let i=0;i<8;i++){
  await page.keyboard.press('Tab')
  const state=await page.evaluate(()=>{
   const current=document.activeElement
   const modal=document.querySelector('.ant-modal-wrap[role="dialog"]')
   return {inside:!!(modal&&current&&modal.contains(current)),tag:current?.tagName,role:current?.getAttribute('role'),text:(current?.textContent||'').trim().slice(0,32)}
  })
  focusTrace.push(state.tag+':'+state.role+':'+state.inside)
  expect(state.inside,'Keyboard focus must remain in open product dialog').toBe(true)
 }
 console.log('PRODUCT_MODAL_FOCUS_TRAP='+JSON.stringify(focusTrace))
 console.log('FOCUS_AFTER_OPEN='+JSON.stringify(await page.evaluate(()=>document.activeElement?.outerHTML?.slice(0,230))))
 await dialog.getByRole('button',{name:/取\s*消/}).focus()
 await page.keyboard.press('Escape')
 await expect(dialog).toBeHidden()
 await expect(picker).toBeFocused()
 console.log('PROCESS_KEYBOARD_A11Y_PASS='+width)
 }
})