const {chromium}=require('D:/codex/_project/gmp/hospital-pharma-mes/frontend/mes-web/node_modules/@playwright/test');
const path=require('path');
(async()=>{
const b=await chromium.launch({headless:true});
const page=await b.newPage({viewport:{width:1600,height:1040},deviceScaleFactor:1});
await page.goto('file:///'+path.join(__dirname,'角色权限-设计规范说明.html').replace(/\\/g,'/'),{waitUntil:'load'});
await page.evaluate(()=>document.fonts.ready);
const q=await page.evaluate(()=>({width:document.documentElement.scrollWidth,height:document.documentElement.scrollHeight,imageLoaded:document.querySelector('.preview img').naturalWidth,images:document.images.length}));
console.log(JSON.stringify(q));
if(q.width!==1600||q.height!==1040||q.imageLoaded!==1440)throw Error('SPEC_BOARD_LAYOUT_FAIL');
await page.screenshot({path:path.join(__dirname,'角色权限-设计规范说明-1600x1040.png'),fullPage:false});
await b.close();console.log('SPEC_BOARD_RENDER_PASS');
})().catch(e=>{console.error(e.stack);process.exit(1)});
