import {createRequire} from 'node:module'
import {mkdir,writeFile} from 'node:fs/promises'
import path from 'node:path'
const require=createRequire(path.resolve('frontend/mes-web/package.json'))
const {chromium}=require('playwright')
const output=path.resolve('docs/review/evidence/role-id-signing-20261011/component-screenshots')
await mkdir(output,{recursive:true})
const browser=await chromium.launch({headless:true})
const results=[]
try{
 const page=await browser.newPage()
 await page.route('**/*',route=>['GET','HEAD'].includes(route.request().method())?route.continue():route.abort())
 await page.goto('http://127.0.0.1:5173',{waitUntil:'domcontentloaded'})
 const source=await (await page.request.get('http://127.0.0.1:5173/src/views/ebr/RolePicker.vue')).text()
 const vueUrl=source.match(/from\s+["']([^"']*\/vue\.js[^"']*)["']/)?.[1]
 if(!vueUrl)throw Error('Actual Vite Vue module unavailable')
 async function mount(mode){await page.evaluate(async({vueUrl,mode})=>{
  window.__roleApp?.unmount()
  const {createApp,h,ref}=await import(vueUrl)
  const {default:Picker}=await import('/src/views/ebr/RolePicker.vue')
  document.body.innerHTML='<main style="padding:20px"><h2>角色组件验证</h2><p>当前源代码组件 · 隔离选项 · 无 DEV 写入</p><div id="role-proof" style="width:320px;max-width:100%"></div></main>'
  const selected=ref('290');window.__selectedRoleId='290'
  // createApp returns App; mount returns the component public instance. Preserve App lifecycle.
  window.__roleApp=createApp({setup(){return()=>h(Picker,{roleId:selected.value,editable:mode==='edit',legacyCode:mode==='legacy'?'DEMO_MES_QC_REVIEWER':null,codeSnapshot:mode==='denied'?'MES_QA':null,nameSnapshot:mode==='denied'?'质量保证审核员':null,options:[{id:'290',code:'MES_QC_REVIEWER',name:'质量检验复核员',enabled:true},{id:'291',code:'MES_QA',name:'质量保证审核员',enabled:true}],'onUpdate:roleId':value=>{selected.value=value;window.__selectedRoleId=value}})}})
  if(typeof window.__roleApp.unmount!=='function')throw Error('Vue App lifecycle unavailable')
  window.__roleApp.mount('#role-proof')
 },{vueUrl,mode})}
 for(const width of [1440,375]){
  await page.setViewportSize({width,height:700});await mount('edit')
  await page.locator('#role-proof .ant-select').waitFor({state:'visible'})
  const geometry=await page.evaluate(()=>{const label=document.querySelector('.ebr-role-label').getBoundingClientRect();const control=document.querySelector('.ebr-role-pair .ant-select').getBoundingClientRect();return{horizontal:Math.abs(label.y+label.height/2-control.y-control.height/2)<3,noOverflow:control.right<=window.innerWidth,labelY:label.y,controlY:control.y}})
  if(!geometry.horizontal||!geometry.noOverflow)throw Error('Inline geometry failed')
  await page.screenshot({path:path.join(output,'role-picker-'+width+'.png'),fullPage:true})
  const interception=await page.evaluate(()=>{const input=document.querySelector('#role-proof input[role="combobox"]');const r=input.getBoundingClientRect();const top=document.elementFromPoint(r.x+r.width/2,r.y+r.height/2);return{topClass:top?.className,insideSameSelector:!!top?.closest('#role-proof .ant-select-selector')}})
  if(!interception.insideSameSelector)throw Error('External obstruction detected; no forced click allowed')
  await page.locator('#role-proof .ant-select-selector').click()
  await page.getByText('质量保证审核员（MES_QA）',{exact:true}).click()
  if(await page.evaluate(()=>window.__selectedRoleId)!=='291')throw Error('Actual selection did not emit ID291')
  const saved=await page.evaluate(async()=>{const {definitionForSave}=await import('/src/views/ebr/model.ts');return definitionForSave({schemaVersion:'2.0',sections:[],forms:[],rules:[],signatureRules:[{objectScope:'FORM',objectCode:'F',meaning:'VERIFY',requiredRoleId:window.__selectedRoleId,roleCodeSnapshot:'OLD',roleNameSnapshot:'Frozen',reauthRequired:true,sequenceNo:1,invalidateOnChange:true}],reviewRules:[]})})
  if(saved.signatureRules[0].requiredRoleId!=='291'||'roleCodeSnapshot' in saved.signatureRules[0]||'roleNameSnapshot' in saved.signatureRules[0])throw Error('ID-only save payload failed')
  await page.screenshot({path:path.join(output,'role-picker-selected-'+width+'.png'),fullPage:true})
  results.push({width,...geometry,clickDiagnosis:interception,selectionId:'291',clientSaveId:'291',serverSnapshotsExcluded:true,noHttpSave:true})
  for(const mode of ['legacy','denied']){
   await mount(mode)
   if(await page.locator('#role-proof [role="combobox"]').count()!==0)throw Error(mode+' exposed an editing control')
   const expected=mode==='legacy'?'DEMO_MES_QC_REVIEWER':'质量保证审核员（MES_QA）'
   if(await page.getByTestId('frozen-role').textContent()!==expected)throw Error(mode+' frozen display mismatch')
   await page.screenshot({path:path.join(output,'role-picker-'+mode+'-readonly-'+width+'.png'),fullPage:true})
   results.push({width,mode,editingControlCount:0,frozenDisplay:expected})
  }
 }
 await writeFile(path.join(output,'component-evidence.json'),JSON.stringify({scope:'actual source-backed isolated component fixture; NOT real MES page/API acceptance; denied mode verifies component prop behavior only',DEVWrites:false,results},null,2))
 console.log(JSON.stringify({output,results}))
}finally{await browser.close()}
