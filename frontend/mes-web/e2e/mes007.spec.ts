import {test,expect,type Page} from '@playwright/test'
const envelope=(data:unknown)=>({code:'OK',message:'success',data,traceId:'ebr-browser-mock'})
test('duplicate edited field code is located before saving or server lint',async({page})=>{
 await setup(page);await page.goto('/ebr/templates/31/designer')
 await page.getByRole('button',{name:'添加表单',exact:true}).click()
 await page.getByRole('button',{name:'数值',exact:true}).click()
 await page.getByRole('button',{name:'文本',exact:true}).click()
 await page.getByLabel('字段编码',{exact:true}).fill('FIELD1')
 const writes:string[]=[];page.on('request',r=>{if(r.method()==='PUT'||r.url().endsWith('/lint'))writes.push(r.url())})
 await expect(page.getByRole('button',{name:'运行 Lint',exact:true})).toBeEnabled()
 await page.getByRole('button',{name:'运行 Lint',exact:true}).click()
 await expect(page.getByText('校验未通过',{exact:true})).toBeVisible()
 await expect(page.locator('.ebr-tools')).toContainText('forms[0].fields[1].fieldCode')
 await expect(page.locator('.ebr-tools')).toContainText('重复字段编码：FIELD1')
 expect(writes).toEqual([])
 await expect(page.getByLabel('字段编码',{exact:true})).toHaveValue('FIELD1')
})
async function setup(page:Page,conflict=false,readonly=false,numeric=false){
 const permissions=['ebr:template:view','master:product:view','process:package:view','master:uom:view','audit:view',...(!readonly?['ebr:template:create','ebr:template:update','ebr:designer:edit','ebr:template:submit','ebr:template:approve','ebr:template:publish']:[])]
 let detail:any={id:'31',templateCode:'EBR-1',packageVersionId:'21',version:1,status:'DRAFT',versionNo:0,contentHash:null,effectiveFrom:null,approvedBy:null,approvedAt:null,allowedActions:['EDIT','LINT','SIMULATE','SUBMIT'],definition:{sections:[],forms:[],rules:[],signatureRules:[],reviewRules:[]},operationChoices:[{id:'22',operationCode:'MIX',operationName:'配液'}],versions:[]}
 if(numeric)detail.definition.forms=[{formCode:'FORM1',formName:'设备参数',operationDefId:null,schemaVersion:'1.0',sequenceNo:1,fields:[{fieldCode:'DEVICE',groupCode:null,label:'设备值',fieldType:'INSTRUMENT_VALUE',sourceType:'INSTRUMENT',dataType:'DECIMAL',unitId:null,precisionScale:null,requiredFlag:false,readonlyFlag:false,defaultExpr:'1',placeholder:null,helpText:null,sequenceNo:1,validationJson:'{}',options:[]}]}];
 const get=()=>({...detail,versions:[{...detail,definition:undefined,versions:undefined}]})
 await page.route('**/api/v1/**',async route=>{const req=route.request(),path=new URL(req.url()).pathname,send=(data:unknown)=>route.fulfill({json:envelope(data)}),list=(items:unknown[])=>({items,total:items.length,page:0,size:20})
  if(path.endsWith('/auth/me'))return send({userId:'7',organizationId:'1',displayName:'模板设计员',loginName:'designer',mustChangePassword:false,roleCodes:[],permissionCodes:permissions})
  if(path.endsWith('/process-packages'))return send(list([{id:'2',packageCode:'PKG-1',productName:'测试制剂',selectedVersion:{id:'21',businessVersion:1}}]))
  if(path.endsWith('/process-packages/2'))return send({versions:[{id:'21',businessVersion:1,status:'EFFECTIVE'}]})
  if(path.endsWith('/units'))return send(list([{id:'10',unitCode:'KG',unitName:'千克'}]))
  if(path.endsWith('/ebr/templates'))return req.method()==='GET'?send(list([get()])):send(get())
  if(path.endsWith('/ebr/templates/31')){if(req.method()==='PUT'){expect(req.headers()['if-match']).toBe('"0"');expect(req.headers()['idempotency-key']).toBeTruthy();if(conflict)return route.fulfill({status:409,json:{code:'VERSION_CONFLICT',message:'版本冲突'}});detail={...detail,definition:req.postDataJSON().definition,versionNo:1}}return send(get())}
  if(path.includes('/ebr/versions/31/')){const action=path.split('/').at(-1);expect(req.postDataJSON().reason).toBeTruthy();if(action==='lint')return send({valid:true,issues:[]});if(action==='simulate'){expect(req.postDataJSON().inputs).toEqual([{fieldCode:'DEVICE',values:[12.5]}]);return send({allowed:true,results:[]})}detail={...detail,status:action==='submit'?'SUBMITTED':action==='approve'?'APPROVED':'EFFECTIVE',versionNo:detail.versionNo+1,allowedActions:action==='submit'?['APPROVE']:action==='approve'?['PUBLISH']:[]};return send(get())}
  return route.fulfill({status:404,json:{code:'NOT_FOUND',message:path}})
 })
}
test('independent create designer save lint and lifecycle with inline labels',async({page},info)=>{
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));await setup(page);await page.goto('/ebr/templates/create?keyword=EBR-Test&page=2')
 await page.getByLabel('模板编码',{exact:true}).fill('EBR-1');await page.getByLabel('工艺包',{exact:true}).selectOption('2');await page.getByLabel('工艺版本',{exact:true}).selectOption('21');await page.getByRole('button',{name:'创建模板',exact:true}).click()
 await page.getByRole('button',{name:'添加章节',exact:true}).click();await page.getByLabel('标题',{exact:true}).fill('配液记录');await page.getByRole('button',{name:'添加表单',exact:true}).click();await page.getByLabel('表单名称',{exact:true}).fill('参数记录');await page.getByRole('button',{name:'数值',exact:true}).click();await page.getByLabel('字段名称',{exact:true}).fill('温度')
 await page.getByLabel('操作原因',{exact:true}).fill('创建受控定义');await page.getByRole('button',{name:'保存草稿',exact:true}).click();await expect(page.getByText('草稿已保存',{exact:true})).toBeVisible();await page.getByRole('button',{name:'运行 Lint',exact:true}).click();await expect(page.getByText('校验通过',{exact:true})).toBeVisible()
 const pair=page.locator('.ebr-inspector label').first();const bounds=await pair.evaluate(el=>{const a=el.children[0]!.getBoundingClientRect(),b=el.children[1]!.getBoundingClientRect();return {a:a.x+a.width,b:b.x,dy:Math.abs(a.y-b.y)}});expect(bounds.a).toBeLessThanOrEqual(bounds.b+1);expect(bounds.dy).toBeLessThan(12)
 await page.keyboard.press('Escape');await page.evaluate(()=>window.scrollTo(0,0));await page.screenshot({path:info.outputPath('ebr-designer.png'),fullPage:true});await page.getByRole('button',{name:'提交审批',exact:true}).click();await page.getByRole('button',{name:'批准模板',exact:true}).click();await page.getByRole('button',{name:'发布模板',exact:true}).click();await expect(page.getByRole('button',{name:'保存草稿',exact:true})).toHaveCount(0);expect(errors).toEqual([])
})
test('conflict preserves draft and permission hides authoring',async({page})=>{await setup(page,true);await page.goto('/ebr/templates/31/designer');await page.getByRole('button',{name:'添加表单',exact:true}).click();await page.getByLabel('表单名称',{exact:true}).fill('未保存参数');await page.getByLabel('操作原因',{exact:true}).fill('冲突检查');await page.getByRole('button',{name:'保存草稿',exact:true}).click();await expect(page.getByLabel('表单名称',{exact:true})).toHaveValue('未保存参数');await expect(page.getByText('记录已变化，输入已保留，请重新加载后继续。')).toBeVisible();await page.unroute('**/api/v1/**');await setup(page,false,true);await page.goto('/ebr/templates/31');await expect(page.getByRole('button',{name:'编辑模板',exact:true})).toHaveCount(0)})

test('device simulation sends number and nullable attributes can be cleared',async({page})=>{await setup(page,false,false,true);await page.goto('/ebr/templates/31/designer');await page.getByLabel('默认表达式',{exact:true}).fill('');await page.getByLabel('校验配置 JSON',{exact:true}).fill('');await page.getByLabel('操作原因',{exact:true}).fill('验证设备数值');const saved=page.waitForRequest(r=>r.method()==='PUT'&&r.url().endsWith('/ebr/templates/31'));await page.getByRole('button',{name:'保存草稿',exact:true}).click();const payload=(await saved).postDataJSON();expect(payload.definition.forms[0].fields[0].defaultExpr).toBeNull();expect(payload.definition.forms[0].fields[0].validationJson).toBeNull();await page.getByRole('tab',{name:'模拟运行',exact:true}).click();await page.getByLabel('模拟 DEVICE',{exact:true}).fill('12.5');await page.getByRole('button',{name:'运行模拟',exact:true}).click();await expect(page.getByText('模拟规则允许通过',{exact:true})).toBeVisible()})
