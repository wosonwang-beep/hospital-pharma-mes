const {chromium}=require('D:/codex/_project/gmp/hospital-pharma-mes/frontend/mes-web/node_modules/@playwright/test');
const path=require('path');
(async()=>{
 const browser=await chromium.launch({headless:true});
 const base='file:///'+path.join(__dirname,'数据字典-ProductDesign.html').replace(/\\/g,'/');
 let total=0;
 for(const width of [1280,1440,1536]){
  const page=await browser.newPage({viewport:{width,height:900},deviceScaleFactor:1});
  for(const view of ['list','create','edit']){
   await page.goto(base+'?view='+view,{waitUntil:'load'});
   const checks=await page.evaluate(()=>{
    const pairs=[...document.querySelectorAll('.filter-grid>label,.form-grid>.form-cell')].map(el=>{
      const name=el.querySelector('.field-label'),input=el.querySelector('input,select,textarea,.range');
      if(!name||!input)return {label:name?.textContent||'MISSING',ok:false,reason:'missing label or control'};
      const a=name.getBoundingClientRect(),b=input.getBoundingClientRect();
      const overlap=Math.min(a.bottom,b.bottom)-Math.max(a.top,b.top);
      const sameRow=a.right<b.left-3 && overlap>=Math.min(a.height,b.height)*0.4;
      return {label:name.textContent.trim(),ok:sameRow,dy:Math.round(Math.abs((a.top+a.bottom)/2-(b.top+b.bottom)/2)),left:Math.round(a.left),controlLeft:Math.round(b.left),overlap:Math.round(overlap)};
    });
    return {pairs,overflow:document.documentElement.scrollWidth>innerWidth};
   });
   total+=checks.pairs.length;
   console.log('ALIGNMENT '+width+' '+view+' '+JSON.stringify(checks));
   if(checks.overflow||checks.pairs.some(x=>!x.ok))throw Error('FAILED '+width+' '+view);
  }
  await page.goto(base+'?view=items',{waitUntil:'load'});
  await page.locator('[data-action="editItem"]').first().click();
  const itemChecks=await page.evaluate(()=>[...document.querySelectorAll('.inline-pair')].map(el=>{
   const a=el.querySelector('span').getBoundingClientRect(),b=el.querySelector('input,select').getBoundingClientRect();
   const overlap=Math.min(a.bottom,b.bottom)-Math.max(a.top,b.top);
   return {name:el.querySelector('span').textContent,ok:a.right<b.left-3&&overlap>=a.height*.4};
  }));
  total+=itemChecks.length;
  console.log('ITEM_EDITOR_ALIGNMENT '+width+' '+JSON.stringify(itemChecks));
  if(itemChecks.length!==4||itemChecks.some(x=>!x.ok))throw Error('Item editor labels not horizontal '+width);
  await page.close();
 }
 await browser.close();console.log('HORIZONTAL_LABEL_PASS '+total+' field pairs');
})().catch(e=>{console.error('ASSERTION_FAILED '+e.stack);process.exit(1)});
