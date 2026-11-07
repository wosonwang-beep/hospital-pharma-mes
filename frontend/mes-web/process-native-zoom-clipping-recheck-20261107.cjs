const {chromium}=require('playwright');
const fs=require('fs'),path=require('path'),os=require('os'),crypto=require('crypto');
const repo=path.resolve(__dirname,'../..');
const dir=path.join(repo,'docs/development/process-form-cpp-ebr/20261107-formal-g5/native-zoom-investigation');
const extension=dir;
const tmp=fs.mkdtempSync(path.join(os.tmpdir(),'mes-zoom-g5-'));
const chrome=path.join(process.env.LOCALAPPDATA,'ms-playwright','chromium-1248','chrome-win64','chrome.exe');
const sha=p=>crypto.createHash('sha256').update(fs.readFileSync(p)).digest('hex');
(async()=>{
 let context;
 const report={createdAt:new Date().toISOString(),browser:'Installed Playwright Chromium 1248',method:'Native chrome.tabs.setZoom(2) via temporary isolated MV3 extension; not CSS emulation',screens:[],errors:[],sourceHash:sha(path.join(repo,'frontend/mes-web/src/views/process/ProcessDetailView.vue'))};
 try{
  context=await chromium.launchPersistentContext(tmp,{executablePath:chrome,channel:'chromium',headless:true,viewport:{width:1440,height:900},args:['--disable-extensions-except='+extension,'--load-extension='+extension]});
  let sw=context.serviceWorkers()[0];
  if(!sw)sw=await context.waitForEvent('serviceworker',{timeout:6000});
  if(!sw?.url().startsWith('chrome-extension://'))throw Error('Isolated native zoom extension not loaded');
  const page=context.pages()[0]||await context.newPage();
  page.on('pageerror',e=>report.errors.push({type:'pageerror',message:e.message,url:page.url()}));
  page.on('response',res=>{if(res.url().includes('/api/v1/')&&res.status()>=400&&!res.url().includes('/auth/'))report.errors.push({type:'api',url:res.url().split('?')[0],status:res.status()})});
  await page.goto('http://127.0.0.1:5173/login');
  if(new URL(page.url()).pathname==='/login'){
   await page.getByLabel('账号').fill('admin');await page.getByLabel('密码').fill('admin');
   await page.getByRole('button',{name:/登\s*录/}).click();
   await page.waitForURL(u=>u.pathname!=='/login',{timeout:15000});
  }
  report.zoom=await sw.evaluate(async()=>{
    const tabs=await chrome.tabs.query({});
    const tab=tabs.find(t=>t.url?.includes('127.0.0.1:5173'));
    if(!tab?.id)throw Error('No isolated MES app tab');
    const original=await chrome.tabs.getZoom(tab.id);
    await chrome.tabs.setZoom(tab.id,2);
    const real=await chrome.tabs.getZoom(tab.id);
    return {tabId:tab.id,before:original,after:real};
  });
  if(report.zoom.after!==2)throw Error('Chrome did not confirm native 200% zoom');
  for(const physical of [1440,1280,900]){
   await page.setViewportSize({width:physical,height:900});
   const layout=await page.evaluate(()=>({innerWidth:window.innerWidth,dpr:window.devicePixelRatio,outerWidth:window.outerWidth,visualScale:window.visualViewport?.scale}));
   if(layout.innerWidth!==physical/2||layout.dpr!==2)throw Error('Native zoom layout did not actually change: '+JSON.stringify({physical,layout}));
   const targets=[
     {id:'process-list',url:'/process/packages',ready:async()=>page.getByRole('row').filter({hasText:'KCL30-CPP-26110601'}).waitFor({timeout:15000})},
     {id:'process-detail',url:'/process/packages/829',ready:async()=>page.locator('main.process-page .basic-card').waitFor({timeout:15000})},
     {id:'process-edit',url:'/process/packages/829/edit',ready:async()=>page.locator('main.process-page .basic-card').waitFor({timeout:15000})},
     {id:'ebr-cpp',url:'/ebr/templates/663/designer?processPackageId=829&formCode=OP1_RECORD&designerTab=cpp',ready:async()=>page.getByText('配制温度控制范围',{exact:true}).waitFor({timeout:15000})},
     {id:'ebr-canvas',url:'/ebr/templates/663/designer?processPackageId=829&formCode=OP1_RECORD&designerTab=form',ready:async()=>page.locator('.canvas-stage').waitFor({timeout:15000})}
   ];
   for(const item of targets){
    await page.goto('http://127.0.0.1:5173'+item.url);
    await item.ready();
    await page.waitForTimeout(250);
    const metrics=await page.evaluate(()=>{
      const d=document.documentElement;
      const selectors=['main.process-page','.basic-card','.basic-card .master-form','.basic-card .master-form>label','.basic-card .master-form input','.workspace','.workspace-body','.operation-detail','.cpp-rule-designer','.cpp-builder','.cpp-step','.business-preview'];
      const geometry=Object.fromEntries(selectors.map(selector=>[selector,[...document.querySelectorAll(selector)].slice(0,2).map(n=>{const rect=n.getBoundingClientRect(),style=getComputedStyle(n);return {left:Math.round(rect.left),right:Math.round(rect.right),width:Math.round(rect.width),scrollWidth:n.scrollWidth,clientWidth:n.clientWidth,overflowX:style.overflowX,minWidth:style.minWidth}})]));
      const clipped=[...document.querySelectorAll('main .basic-card input,main .basic-card textarea,main .cpp-step input,main .cpp-rule-designer .business-preview,main .workspace-heading button')].filter(n=>{const r=n.getBoundingClientRect();return r.width>10&&r.left<innerWidth&&r.right>innerWidth+10}).slice(0,18).map(n=>{const r=n.getBoundingClientRect();return {type:n.tagName,className:String(n.className).slice(0,65),text:String(n.textContent||n.value||'').slice(0,28),left:Math.round(r.left),right:Math.round(r.right),width:Math.round(r.width)}});
      const footer=document.querySelector('.workspace-footer')?.getBoundingClientRect();
      const form=document.querySelector('.process-form-card')?.getBoundingClientRect();
      return {innerWidth:innerWidth,dpr:devicePixelRatio,scrollWidth:d.scrollWidth,clientWidth:d.clientWidth,footerTop:footer?Math.round(footer.top):null,formBottom:form?Math.round(form.bottom):null,geometry,clipped};
    });
    const passed=metrics.scrollWidth<=metrics.clientWidth+1&&metrics.clipped.length===0&&(metrics.footerTop===null||metrics.formBottom===null||metrics.footerTop>=metrics.formBottom);
    const filename=physical+'-'+item.id+'-native200-clipcheck.png';
    await page.screenshot({path:path.join(dir,filename),fullPage:true,scale:'device',animations:'disabled'});
    if(true){
     const session=await context.newCDPSession(page);
     const captured=await session.send('Page.captureScreenshot',{format:'png',fromSurface:true,captureBeyondViewport:false});
     fs.writeFileSync(path.join(dir,physical+'-'+item.id+'-native200-cdp.png'),Buffer.from(captured.data,'base64'));
     await session.detach();
    }
    report.screens.push({physical,kind:item.id,filename,metrics,passed});
    console.log('NATIVE_ZOOM_G5='+JSON.stringify({physical,kind:item.id,passed,metrics}));
   }
   await page.goto('http://127.0.0.1:5173/process/packages/829/edit');
   await page.getByRole('tab',{name:/适用产品/}).click();
   const opener=page.getByRole('button',{name:/选择产品/}).last();
   await opener.focus();await page.keyboard.press('Enter');
   const dialog=page.getByRole('dialog',{name:'选择适用产品'});
   await dialog.waitFor({timeout:10000});
   await dialog.getByRole('combobox',{name:'适用产品',exact:true}).waitFor();
   const beforeEsc=await page.evaluate(()=>{const modal=document.querySelector('.ant-modal-wrap[role="dialog"]');return !!(modal&&document.activeElement&&modal.contains(document.activeElement))});
   await page.screenshot({path:path.join(dir,physical+'-product-modal-native200.png'),fullPage:true,scale:'device',animations:'disabled'});
   const modalSession=await context.newCDPSession(page);
   const modalCaptured=await modalSession.send('Page.captureScreenshot',{format:'png',fromSurface:true,captureBeyondViewport:false});
   fs.writeFileSync(path.join(dir,physical+'-product-modal-native200-cdp.png'),Buffer.from(modalCaptured.data,'base64'));
   await modalSession.detach();
   await page.keyboard.press('Escape');
   await dialog.waitFor({state:'hidden',timeout:5000});
   const focusRestored=await opener.evaluate(el=>el===document.activeElement);
   const metrics=await page.evaluate(()=>({innerWidth:innerWidth,scrollWidth:document.documentElement.scrollWidth,clientWidth:document.documentElement.clientWidth}));
   const passed=beforeEsc&&focusRestored&&metrics.scrollWidth<=metrics.clientWidth+1;
   report.screens.push({physical,kind:'product-modal',filename:physical+'-product-modal-native200.png',metrics,passed,focusRestored,beforeEsc});
   console.log('NATIVE_ZOOM_G5='+JSON.stringify({physical,kind:'product-modal',passed,metrics,focusRestored,beforeEsc}));
  }
  report.pass=report.zoom.after===2&&report.errors.length===0&&report.screens.every(s=>s.passed)&&report.screens.length===18;
  report.status=report.pass?'PASS FOR NATIVE CHROME TAB ZOOM TARGETED G5':'FAIL -- DO NOT COUNT AS G5 PASS';
  console.log('NATIVE_ZOOM_G5_RESULT='+JSON.stringify({status:report.status,n:report.screens.length,errors:report.errors}));
 }catch(e){
  report.pass=false;report.status='FAIL / INCOMPLETE';report.error=String(e);
  console.error('NATIVE_ZOOM_G5_ERROR='+String(e));
 }finally{
  fs.writeFileSync(path.join(dir,'native-zoom-clipping-recheck.json'),JSON.stringify(report,null,2));
  await context?.close().catch(()=>{});
  fs.rmSync(tmp,{recursive:true,force:true});
  if(!report.pass)process.exitCode=1;
 }
})().catch(e=>{console.error(e);process.exitCode=1});
