import {test,expect} from '@playwright/test'
import {resolve} from 'node:path'
// Browser plugin not available; API mocks provide supplemental UI evidence only.
test('QC query horizontal labels and signed version retry preserve binding',async({page},info)=>{
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message))
 const permissions=['qms:specification:view','qms:specification:create','qms:specification:edit','qms:specification:approve','qms:specification:retire','ebr:sign','audit:view']
 let v={id:'21',orgId:'1',specificationId:'20',materialId:'11',materialCode:'RAW-11',materialName:'原辅料',specificationCode:'QC-20',specificationName:'来料质量标准',versionNoBusiness:1,versionNo:0,status:'DRAFT',createdBy:'4',items:[{specificationItemId:'31',itemCode:'ASSAY',itemName:'含量',required:true,resultType:'NUMERIC',lowerLimit:'1.000000',upperLimit:'3.000000',unitId:'10',methodCode:'M1',methodVersion:'1'}]}
 let refreshes=0
 const attempts:Record<string,unknown>[]=[];const bindings:Record<string,unknown>[]=[]
 await page.route('**/api/v1/**',async r=>{const path=new URL(r.request().url()).pathname;const send=(data:unknown)=>r.fulfill({json:{code:'OK',message:'Success',data,traceId:'qc-ui'}})
 if(path.endsWith('/auth/me'))return send({userId:'5',organizationId:'1',loginName:'qc',displayName:'QC审核人',roleCodes:['QC'],permissionCodes:permissions,mustChangePassword:false})
 if(path.endsWith('/auth/refresh')){refreshes++;return send({accessToken:'renewed-session'})}
 if(path.endsWith('/auth/reauth')){bindings.push(r.request().postDataJSON());return send({reauthToken:`fresh-${bindings.length}`})}
 if(path.endsWith('/specification-versions/21/approve')){attempts.push(r.request().postDataJSON());if(attempts.length===1)return r.fulfill({status:401,json:{code:'REAUTH_TOKEN_INVALID',message:'再认证令牌已过期',fieldErrors:[],allowedActions:[]}});v={...v,status:'APPROVED',versionNo:1};return send(v)}
 if(path.endsWith('/specification-versions/21'))return send(v)
 const root={id:'20',materialId:'11',materialCode:'RAW-11',materialName:'原辅料',specificationCode:'QC-20',specificationName:'来料质量标准',versions:[v]}
 if(path.endsWith('/specifications/20'))return send(root)
 if(path.endsWith('/specifications'))return send({items:[root],total:1,page:0,size:20})
 return send({items:[],total:0,page:0,size:20})})
 await page.goto('/quality/specifications');await expect(page.getByRole('heading',{name:'QC质量标准',exact:true})).toBeVisible()
 const label=page.locator('.query-form label').first();await label.locator('input').fill('QC-20');await page.getByRole('button',{name:/^查\s*询$/}).click();await expect(page).toHaveURL(/keyword=QC-20/)
 const aligned=await label.evaluate(el=>{const c=el.querySelector('input')!.getBoundingClientRect(),r=document.createRange();r.selectNodeContents(el.childNodes[0]!);const t=r.getBoundingClientRect();return Math.abs((t.top+t.bottom-c.top-c.bottom)/2)<14&&t.right<=c.left+4});expect(aligned).toBe(true)
 await page.getByRole('button',{name:'查看',exact:true}).click();await page.getByRole('button',{name:'编辑 / 查看',exact:true}).click();await expect(page.getByRole('heading',{name:'来料质量标准 · V1'})).toBeVisible()
 await page.getByLabel('操作原因',{exact:true}).fill('核对全部项目并批准');await page.getByRole('button',{name:'签名批准',exact:true}).click();await page.getByLabel('再认证密码',{exact:true}).fill('test-password');await page.getByRole('button',{name:'确认电子签名',exact:true}).click()
 await expect(page.getByText('再认证令牌已过期').first()).toBeVisible();expect(refreshes).toBe(0);await expect(page.getByLabel('再认证密码',{exact:true})).toHaveValue('');await expect(page.getByLabel('签名原因',{exact:true})).toHaveValue('核对全部项目并批准')
 await page.getByLabel('再认证密码',{exact:true}).fill('fresh-password');await page.getByRole('button',{name:'确认电子签名',exact:true}).click();await expect(page.getByRole('button',{name:'签名退役',exact:true})).toBeVisible()
 expect(attempts.map(a=>a.reauthToken)).toEqual(['fresh-1','fresh-2']);for(const b of bindings){expect(b.objectType).toBe('QcSpecificationVersion');expect(b.objectId).toBe('21');expect(b.meaning).toBe('APPROVE');expect(b.recordVersion).toBe(1)}expect(errors).toEqual([])
 await page.screenshot({path:resolve('../../docs/acceptance/incoming-quality/evidence',`qc-standard-${info.project.name}.png`),fullPage:true})
})

test('QC root create horizontal fields and exact controlled save',async({page},info)=>{
 const writes:Record<string,unknown>[]=[];const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message))
 await page.route('**/api/v1/**',async r=>{const path=new URL(r.request().url()).pathname;const send=(data:unknown)=>r.fulfill({json:{code:'OK',message:'Success',data,traceId:'qc-create-ui'}})
 if(path.endsWith('/auth/me'))return send({userId:'5',organizationId:'1',loginName:'qc',displayName:'QC人员',roleCodes:['QC'],permissionCodes:['qms:specification:view','qms:specification:create','master:material:view'],mustChangePassword:false})
 if(path.endsWith('/masters/materials')||path.endsWith('/master/materials'))return send({items:[{id:'11',materialCode:'RAW-11',materialName:'原辅料',status:'ENABLED'}],total:1,page:0,size:20})
 if(path.endsWith('/quality/specifications')&&r.request().method()==='POST'){writes.push(r.request().postDataJSON());return send({id:'20',specificationCode:'QC-20',specificationName:'来料质量标准',materialId:'11',materialCode:'RAW-11',materialName:'原辅料',versions:[]})}
 if(path.endsWith('/quality/specifications/20'))return send({id:'20',specificationCode:'QC-20',specificationName:'来料质量标准',materialId:'11',materialCode:'RAW-11',materialName:'原辅料',versions:[]})
 return send({items:[{id:'11',materialCode:'RAW-11',materialName:'原辅料',status:'ENABLED'}],total:1,page:0,size:20})})
 await page.goto('/quality/specifications/create');await expect(page.getByRole('heading',{name:'新增QC质量标准'})).toBeVisible()
 await page.getByLabel('标准编码',{exact:true}).fill('QC-20');await page.getByLabel('标准名称',{exact:true}).fill('来料质量标准');await page.getByLabel('操作原因',{exact:true}).fill('建立来料检验标准')
 const label=page.locator('.master-form label').filter({has:page.getByLabel('标准编码',{exact:true})});const sameRow=await label.evaluate(el=>{const a=el.querySelector('.form-field-label')!.getBoundingClientRect(),b=el.querySelector('input')!.getBoundingClientRect();return Math.abs((a.top+a.bottom-b.top-b.bottom)/2)<14&&a.right<=b.left+4});expect(sameRow).toBe(true)
 await page.locator('.process-lookup').getByRole('combobox').click();await page.getByText('原辅料（RAW-11）',{exact:true}).click();await page.getByRole('button',{name:'保存标准',exact:true}).click();await expect(page).toHaveURL(/\/quality\/specifications\/20$/)
 expect(writes).toEqual([{materialId:'11',specificationCode:'QC-20',specificationName:'来料质量标准',reason:'建立来料检验标准'}]);expect(errors).toEqual([]);await page.screenshot({path:resolve('../../docs/acceptance/incoming-quality/evidence',`qc-root-${info.project.name}.png`),fullPage:true})
})
