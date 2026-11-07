const {chromium}=require('playwright');
const fs=require('fs'),path=require('path');
const out=path.resolve(__dirname,'../../docs/development/process-form-cpp-ebr/20261106-formal-code/g5-readonly');
fs.mkdirSync(out,{recursive:true});
const base='http://127.0.0.1:5173';
(async()=>{
 const browser=await chromium.launch({channel:'chrome',headless:true});
 const log=[],screens=[],errors=[],authChallenges=[];
 const contexts=[1440,1280,900];
 for(const width of contexts){
  const context=await browser.newContext({viewport:{width,height:900},deviceScaleFactor:1,acceptDownloads:false});
  const page=await context.newPage();
  page.on('pageerror',e=>errors.push({width,message:e.message,url:page.url()}));
  page.on('response',resp=>{if(resp.url().includes('/api/v1/')&&resp.status()>=400){const detail={width,apiStatus:resp.status(),url:resp.url().split('?')[0]};if(resp.status()===401&&resp.url().includes('/api/v1/auth/'))authChallenges.push(detail);else errors.push(detail)}});
  await page.goto(base+'/login');
  if(new URL(page.url()).pathname==='/login'){
   await page.getByLabel('账号').fill('admin');await page.getByLabel('密码').fill('admin');
   await page.getByRole('button',{name:/登\s*录/}).click();await page.waitForURL(u=>u.pathname!=='/login',{timeout:20000});
  }
  async function snap(surface,state='default'){
   await page.waitForTimeout(450);
   const metrics=await page.evaluate(()=>{
    const main=document.querySelector('main.admin-page'),foot=document.querySelector('.workspace-footer');
    const nodes=[...document.querySelectorAll('main.admin-page label')].filter(e=>e.getBoundingClientRect().width>0);
    const labels=nodes.slice(0,35).map(e=>{const label=e.querySelector('.form-field-label')||e.querySelector('span');const control=e.querySelector('input,textarea,select,.ant-select,.ant-picker,.ant-checkbox,.ant-switch');if(!label||!control)return null;const a=label.getBoundingClientRect(),b=control.getBoundingClientRect();return {text:(label.textContent||'').trim().slice(0,24),labelCenter:Math.round(a.y+a.height/2),controlCenter:Math.round(b.y+b.height/2),centerDelta:Math.round(Math.abs(a.y+a.height/2-b.y-b.height/2)),labelLeft:Math.round(a.left),controlLeft:Math.round(b.left)}}).filter(Boolean);
    const docs={width:document.documentElement.scrollWidth,client:document.documentElement.clientWidth,body:document.body.scrollWidth};
    const rect=(x)=>x?{x:Math.round(x.getBoundingClientRect().x),y:Math.round(x.getBoundingClientRect().y),width:Math.round(x.getBoundingClientRect().width),height:Math.round(x.getBoundingClientRect().height)}:null;
    return {url:location.pathname,bodyScroll:docs,main:rect(main),footer:rect(foot),labels,buttons:[...main?.querySelectorAll('button')||[]].filter(b=>b.getBoundingClientRect().width).slice(0,50).map(b=>({text:(b.textContent||'').trim().slice(0,28),title:b.getAttribute('aria-label'),r:rect(b)})),tables:[...main?.querySelectorAll('.ant-table-content')||[]].map(e=>({clientWidth:e.clientWidth,scrollWidth:e.scrollWidth,scrollLeft:e.scrollLeft})),pageTemplate:main?.getAttribute('data-ui-template'),focus:document.activeElement?.outerHTML?.slice(0,150)};
   });
   const file=`${String(width)}-${surface}-${state}.png`;
   await page.screenshot({path:path.join(out,file),fullPage:true,animations:'disabled'});
   screens.push({width,surface,state,file,metrics});console.log('CAPTURED',file, 'DOCUMENT_WIDTH',metrics.bodyScroll.width,'DELTA',metrics.labels.filter(x=>x.centerDelta>8).length);
  }
  await page.goto(base+'/process/packages');await page.locator('main.process-package-list tbody tr:not([aria-hidden="true"])').first().waitFor({timeout:20000});
  await snap('list');
  await page.locator('main.process-package-list .query-form input').first().fill('KCL30-CPP-26110601');
  await page.locator('main.process-package-list .query-form button[type=submit]').click();
  await page.getByRole('row').filter({hasText:'KCL30-CPP-26110601'}).waitFor();await snap('list','filtered');
  await page.goto(base+'/process/packages/create');await page.getByRole('heading',{name:'新增生产工艺'}).waitFor();await snap('create');
  if(await page.getByRole('button',{name:'创建生产工艺'}).isDisabled())await snap('create','required-disabled');else throw new Error('Required fields unexpectedly permit blank create');
  await page.goto(base+'/process/packages/829');await page.locator('main.process-page .basic-card').waitFor();await page.getByText('KCL30-OP1-EBR-261106').waitFor({timeout:20000});await snap('detail','forms');
  await page.getByRole('tab',{name:/适用产品/}).click();await snap('detail','products');
  await page.goto(base+'/process/packages/829/edit');await page.locator('main.process-page .basic-card').waitFor();await page.getByText('KCL30-OP1-EBR-261106').waitFor({timeout:20000});await snap('edit','forms');
  await page.getByRole('tab',{name:/适用产品/}).click();await snap('edit','products');
  await page.goto(base+'/ebr/templates/663/designer?processPackageId=829&formCode=OP1_RECORD&designerTab=cpp&returnTo=/process/packages/829');await page.getByText('配制温度控制范围',{exact:true}).waitFor({timeout:20000});await snap('ebr-cpp','readback');
  await page.goto(base+'/ebr/templates/663/designer?processPackageId=829&formCode=OP1_RECORD&designerTab=form&returnTo=/process/packages/829');await page.locator('.canvas-ebr-designer').waitFor();await snap('ebr-form','canvas');
  await context.close();
 }
 fs.writeFileSync(path.join(out,'browser-capture.json'),JSON.stringify({createdAt:new Date().toISOString(),screens,errors,authChallenges},null,2),'utf8');
 console.log('RESULT',JSON.stringify({screens:screens.length,errors:errors.length,authChallenges:authChallenges.length,overflow:screens.filter(x=>x.metrics.bodyScroll.width>x.width+4).map(x=>x.file),labelOutliers:screens.map(x=>({file:x.file,n:x.metrics.labels.filter(l=>l.centerDelta>8).length})).filter(x=>x.n)}));
 await browser.close();if(errors.length)process.exitCode=1;
})().catch(e=>{console.error(e);process.exitCode=1});