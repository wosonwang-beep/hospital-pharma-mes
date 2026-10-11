const {chromium}=require('D:/codex/_project/gmp/hospital-pharma-mes/frontend/mes-web/node_modules/@playwright/test');
const path=require('path');
(async()=>{
const browser=await chromium.launch({headless:true});
const page=await browser.newPage({viewport:{width:1440,height:900},deviceScaleFactor:1});
const file='file:///'+path.join(__dirname,'角色权限-ProductDesign.html').replace(/\\/g,'/');
await page.goto(file,{waitUntil:'load'});
await page.evaluate(()=>document.fonts.ready);
const report=await page.evaluate(()=>{
const pairs=[...document.querySelectorAll('.role-field,.grant-filter>label:not(.only-selected)')].map(el=>{
const l=el.querySelector('.field-label').getBoundingClientRect();
const x=el.querySelector('input,select').getBoundingClientRect();
const delta=Math.abs((l.top+l.bottom)/2-(x.top+x.bottom)/2);
return {name:el.querySelector('.field-label').textContent.trim(),horizontal:l.right<x.left,delta:Number(delta.toFixed(2)),pass:l.right<x.left&&delta<=5};
});
const nav=[...document.querySelectorAll('.nav-domain')].map(el=>el.textContent.trim());
return {title:document.querySelector('h1')?.textContent.trim(),template:document.querySelector('[data-ui-template]')?.dataset.uiTemplate,nav,fieldChecks:pairs,documentWidth:document.documentElement.scrollWidth,viewport:innerWidth,pageHeight:document.documentElement.scrollHeight};
});
console.log(JSON.stringify(report,null,2));
if(report.title!=='编辑角色'||report.template!=='T2')throw Error('T2 title/template mismatch');
if(report.fieldChecks.some(x=>!x.pass))throw Error('LABEL_CONTROL_PAIR_FAIL');
if(report.documentWidth>1440)throw Error('HORIZONTAL_OVERFLOW');
if(!['首页','基础管理','WMS管理','质量管理','生产管理','成品管理','系统管理'].every((x,i)=>report.nav[i].includes(x)))throw Error('UNIFIED_MENU_ORDER_FAIL');
await page.screenshot({path:path.join(__dirname,'角色权限-完整PC设计图-1440x900.png'),fullPage:false});
await browser.close();console.log('ROLE_DESIGN_RENDER_PASS');
})().catch(e=>{console.error(e.stack);process.exit(1)});
