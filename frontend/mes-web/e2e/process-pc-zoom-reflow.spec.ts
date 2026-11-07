import {test,expect} from '@playwright/test'
test.use({channel:'chrome'})
test('PC equivalent 200 percent zoom reflows process and EBR routes without clipping primary controls',async({browser})=>{
 test.setTimeout(85000)
 for(const physical of [1440,1280,900]){
  const width=physical/2
  const context=await browser.newContext({baseURL:'http://127.0.0.1:5173',viewport:{width,height:900/2},deviceScaleFactor:2})
  const page=await context.newPage()
  try{
   await page.goto('/login')
   if(new URL(page.url()).pathname==='/login'){
    await page.getByLabel('账号').fill('admin')
    await page.getByLabel('密码').fill('admin')
    await page.getByRole('button',{name:/登\s*录/}).click()
    await page.waitForURL(u=>u.pathname!=='/login',{timeout:15000})
   }
   for(const url of ['/process/packages/829/edit','/process/packages/829']){
    await page.goto(url)
    await expect(page.locator('main.process-page .basic-card')).toBeVisible()
    const metrics=await page.evaluate(()=>{
     const doc=document.documentElement
     const footer=document.querySelector('.workspace-footer')?.getBoundingClientRect()
     const card=document.querySelector('.process-form-card')?.getBoundingClientRect()
     return {scrollWidth:doc.scrollWidth,clientWidth:doc.clientWidth,footerTop:footer?.top,cardBottom:card?.bottom}
    })
    console.log('PC_200_EQUIVALENT='+JSON.stringify({physical,cssViewport:width,url,metrics}))
    expect(metrics.scrollWidth).toBeLessThanOrEqual(metrics.clientWidth+1)
    if(metrics.footerTop!==undefined&&metrics.cardBottom!==undefined)expect(metrics.footerTop).toBeGreaterThanOrEqual(metrics.cardBottom)
   }
   await page.goto('/ebr/templates/663/designer?processPackageId=829&formCode=OP1_RECORD&designerTab=cpp&returnTo=/process/packages/829')
   await expect(page.getByText('20 ≤ 配制温度 ≤ 25')).toBeVisible()
   const m=await page.evaluate(()=>({scrollWidth:document.documentElement.scrollWidth,clientWidth:document.documentElement.clientWidth}))
   console.log('CPP_200_EQUIVALENT='+JSON.stringify({physical,cssViewport:width,m}))
   expect(m.scrollWidth).toBeLessThanOrEqual(m.clientWidth+1)
  }finally{await context.close()}
 }
})
