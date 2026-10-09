const {chromium}=require('D:/codex/_project/gmp/hospital-pharma-mes/frontend/mes-web/node_modules/@playwright/test');
const path=require('path');(async()=>{
 const b=await chromium.launch({headless:true}),page=await b.newPage({viewport:{width:1440,height:900},deviceScaleFactor:1});
 const base='file:///'+path.join(__dirname,'部门管理-ProductDesign.html').replace(/\\/g,'/');
 for(const [scene,label] of [['list','查询'],['create','新增'],['edit','编辑'],['detail','查看']]){
  await page.goto(base+'?view='+scene,{waitUntil:'load'});
  const state=await page.evaluate(()=>{
   const fields=[...document.querySelectorAll('.dept-filter>label,.dept-form-grid>.form-cell')];
   const pairs=fields.map(el=>{const a=el.querySelector('.field-label')?.getBoundingClientRect(),b=el.querySelector('input,select,textarea')?.getBoundingClientRect();return {ok:!!a&&!!b&&a.right<b.left&&a.bottom>b.top&&b.bottom>a.top,tag:el.textContent?.trim().slice(0,18)}});
   return {title:document.querySelector('h1')?.textContent,fields:pairs,overflow:document.documentElement.scrollWidth>innerWidth,modal:!!document.querySelector('[role=dialog]')};
  });
  console.log(scene,JSON.stringify(state));
  if(state.overflow||state.fields.some(x=>!x.ok))throw Error('DESIGN_ALIGNMENT_FAIL '+scene);
  if(['create','edit'].includes(scene)&&!state.modal)throw Error('Missing form dialog');
  await page.screenshot({path:path.join(__dirname,'部门管理-'+label+'.png'),fullPage:false});
 }
 await b.close();console.log('DEPARTMENT_DESIGN_RENDER_PASS 4 pages');
})().catch(e=>{console.error('DESIGN_FAILED '+e.stack);process.exit(1)});
