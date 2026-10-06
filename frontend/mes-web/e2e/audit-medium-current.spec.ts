import {test,expect,type Page} from '@playwright/test'
import {mkdirSync,readFileSync} from 'node:fs'
import {resolve} from 'node:path'
const output=resolve('../../docs/acceptance/audit-medium-2026-10-06');mkdirSync(output,{recursive:true})
const fullPermissions=['production:order:view','production:order:update','production:batch:view','production:batch:update','master:product:view','master:material:view','wms:inventory:view','wms:receipt:view','trace:view','audit:view','qms:inspection-request:view','iam:user:view']
async function fixture(page:Page,permissions=fullPermissions){
 const errors:string[]=[],reads:URL[]=[],writes:string[]=[]
 page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())})
 await page.route('**/api/v1/**',r=>{
  const url=new URL(r.request().url()),path=url.pathname.replace('/api/v1','');reads.push(url);if(r.request().method()!=='GET')writes.push(path)
  let data:unknown={items:[],total:0,page:0,size:20}
  if(path==='/auth/me')data={userId:'8',organizationId:'1',displayName:'MEDIUM复核员',permissionCodes:permissions,mustChangePassword:false}
  else if(path==='/products')data={items:[{id:'21',productCode:'P-21',productName:'复方软膏'}],total:1}
  else if(path==='/materials')data={items:[{id:'20',materialCode:'M-20',materialName:'原料A'}],total:1}
  else if(path==='/main-batches'||path==='/production-orders')data={items:[
   {id:'100',batchNo:'PB-001',orderNo:'PO-001',status:'DRAFT',productId:'21',plannedQty:'100',allowedActions:['EDIT']},
   {id:'101',batchNo:'PB-002',orderNo:'PO-002',status:'DRAFT',allowedActions:[]},
   {id:'102',batchNo:'PB-003',orderNo:'PO-003',status:'IN_PROGRESS',allowedActions:['EDIT']}
  ],total:3,page:Number(url.searchParams.get('page')??0),size:20}
  else if(path==='/production-orders/100'||path==='/main-batches/100')data={id:'100',batchNo:'PB-001',orderNo:'PO-001',status:'DRAFT',productId:'21',plannedQty:'100',unitId:'10',plannedDate:'2026-10-06',versionNo:1,allowedActions:['EDIT']}
  else if(path==='/wms/material-lots')data={items:[{id:'61',lotNo:'ML-001',materialName:'原料A',qualityStatus:'QUARANTINE'}],total:1,page:0,size:50}
  else if(path==='/trace')data={nodes:[{type:'MAIN_BATCH',id:'100',label:'PB-001',status:'DRAFT'},{type:'MATERIAL_LOT',id:'61',label:'ML-001',status:'QUARANTINE'}],edges:[]}
  return r.fulfill({json:{code:'OK',data,message:'success',traceId:'medium-current'}})
 });return {errors,reads,writes}
}
async function healthy(page:Page,errors:string[]){expect(errors).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)}
test('MEDIUM-01 direct Edit uses permission server EDIT and DRAFT and preserves list context',async({page})=>{
 const {errors,writes}=await fixture(page);page.on('dialog',dialog=>void dialog.accept())
 for(const resource of ['orders','batches']){
  await page.goto(`/production/${resource}?keyword=保留&page=2`)
  const main=page.locator('main.admin-page'),editable=main.locator('tr').filter({hasText:resource==='orders'?'PO-001':'PB-001'})
  await expect(editable.getByRole('button',{name:'编辑',exact:true})).toBeVisible()
  for(const no of ['002','003'])await expect(main.locator('tr').filter({hasText:(resource==='orders'?'PO-':'PB-')+no}).getByRole('button',{name:'编辑',exact:true})).toHaveCount(0)
  await page.screenshot({path:resolve(output,`${resource}-edit-list-${test.info().project.name}.png`),fullPage:true})
  await editable.getByRole('button',{name:'编辑',exact:true}).click();await expect(page).toHaveURL(new RegExp(`/production/${resource}/100/edit\\?`));expect(new URL(page.url()).searchParams.get('keyword')).toBe('保留');expect(new URL(page.url()).searchParams.get('page')).toBe('2')
  await expect(page.getByRole('heading',{name:resource==='orders'?'编辑生产订单':'编辑生产批',exact:true})).toBeVisible()
  await page.getByRole('button',{name:/返回列表/}).click();await expect(page.getByLabel('关键字',{exact:true})).toHaveValue('保留');await expect(main.locator('.table-footer')).toContainText('第 2 页')
 }
 await healthy(page,errors);expect(writes).toEqual([])
})
test('MEDIUM-01 view-only identity has no list Edit and existing edit route guard holds',async({page})=>{
 const {errors}=await fixture(page,fullPermissions.filter(p=>!p.endsWith(':update')))
 for(const resource of ['orders','batches']){await page.goto(`/production/${resource}`);await expect(page.locator('main.admin-page').getByRole('button',{name:'查看',exact:true})).toHaveCount(3);await expect(page.locator('main.admin-page').getByRole('button',{name:'编辑',exact:true})).toHaveCount(0);await page.goto(`/production/${resource}/100/edit`);await expect(page).toHaveURL(/\/$/)}
 await healthy(page,errors)
})
test('MEDIUM-02 business roots use product material keyword filters and exactly one root',async({page})=>{
 const {errors,reads,writes}=await fixture(page);await page.goto('/trace')
 await page.locator('.ant-select[aria-label="追溯产品"]').click();await page.locator('.ant-select-dropdown:visible').getByText('复方软膏（P-21）',{exact:true}).click()
 await expect.poll(()=>reads.some(u=>u.pathname.endsWith('/main-batches')&&u.searchParams.get('productId')==='21')).toBe(true)
 const batchPicker=page.getByLabel('生产批',{exact:true}).locator('..');await batchPicker.locator('summary').click();await batchPicker.getByLabel('查找生产批',{exact:true}).fill('PB-001');await batchPicker.getByRole('button',{name:/^查\s*找$/,exact:true}).click()
 await expect.poll(()=>reads.some(u=>u.pathname.endsWith('/main-batches')&&u.searchParams.get('keyword')==='PB-001'&&u.searchParams.get('productId')==='21')).toBe(true)
 await page.getByLabel('生产批',{exact:true}).selectOption('100');await page.getByRole('button',{name:'查询追溯',exact:true}).click();await expect(page.locator('.trace-node')).toHaveCount(2)
 expect(reads.filter(u=>u.pathname.endsWith('/trace')).at(-1)?.searchParams.get('mainBatchId')).toBe('100')
 await page.locator('.ant-select[aria-label="追溯物料"]').click();await page.locator('.ant-select-dropdown:visible').getByText('原料A（M-20）',{exact:true}).click()
 await expect.poll(()=>reads.some(u=>u.pathname.endsWith('/wms/material-lots')&&u.searchParams.get('materialId')==='20')).toBe(true)
 const lotPicker=page.getByLabel('物料批次',{exact:true}).locator('..');await lotPicker.locator('summary').click();await lotPicker.getByLabel('查找物料批次',{exact:true}).fill('ML-001');await lotPicker.getByRole('button',{name:/^查\s*找$/,exact:true}).click()
 await expect.poll(()=>reads.some(u=>u.pathname.endsWith('/wms/material-lots')&&u.searchParams.get('keyword')==='ML-001'&&u.searchParams.get('materialId')==='20')).toBe(true)
 await page.getByLabel('物料批次',{exact:true}).selectOption('61');const count=reads.filter(u=>u.pathname.endsWith('/trace')).length;await page.getByRole('button',{name:'查询追溯',exact:true}).click();await expect(page.getByText('请选择生产批或物料批中的一个进行追溯',{exact:true})).toBeVisible();expect(reads.filter(u=>u.pathname.endsWith('/trace'))).toHaveLength(count)
 await page.getByLabel('生产批',{exact:true}).selectOption('');await page.getByRole('button',{name:'查询追溯',exact:true}).click();await expect.poll(()=>reads.filter(u=>u.pathname.endsWith('/trace')).length).toBe(count+1)
 const query=reads.filter(u=>u.pathname.endsWith('/trace')).at(-1)!;expect(query.searchParams.get('materialLotId')).toBe('61');expect(query.searchParams.has('mainBatchId')).toBe(false);expect(query.searchParams.has('materialId')).toBe(false);expect(query.searchParams.has('productId')).toBe(false)
 await page.evaluate(()=>window.scrollTo(0,0));await page.screenshot({path:resolve(output,`trace-business-roots-${test.info().project.name}.png`),fullPage:true});await healthy(page,errors);expect(writes).toEqual([])
})
test('MEDIUM-03 and 04 grouped desktop mobile menus share definitions and permission filtering',async({page,isMobile})=>{
 const source=readFileSync(new URL('../src/layouts/AppLayout.vue',import.meta.url),'utf8');expect(source.match(/const navigationGroups\s*=/g)).toHaveLength(1);expect(source.match(/group in navigationGroups/g)).toHaveLength(2)
 const {errors}=await fixture(page);await page.goto('/production/orders')
 if(isMobile)await page.getByRole('button',{name:'打开导航',exact:true}).click()
 const menu=isMobile?page.locator('.ant-dropdown:visible'):page.locator('.sidebar')
 const titles=['基础主数据','仓库管理','生产管理','质量管理','系统管理'];for(const title of titles)await expect(menu.locator('.ant-menu-item-group-title,.ant-dropdown-menu-item-group-title').getByText(title,{exact:true})).toBeVisible()
 const labels=['物料主数据','收货单','生产订单','生产批','请验单','用户管理','完整追溯','GMP Audit Trail'];for(const text of labels)await expect(menu.getByText(text,{exact:true})).toBeVisible()
 const visibleLabels=await menu.locator('.ant-menu-item,.ant-dropdown-menu-item').allTextContents();expect(visibleLabels.map(s=>s.trim())).toEqual(['首页','物料主数据','产品管理','收货单','生产订单','生产批','请验单','用户管理','完整追溯','GMP Audit Trail'])
 await page.screenshot({path:resolve(output,`grouped-navigation-${test.info().project.name}.png`),fullPage:true})
 await menu.getByText('完整追溯',{exact:true}).click();await expect(page).toHaveURL(/\/trace$/);await healthy(page,errors)
 await page.route('**/api/v1/auth/me',r=>r.fulfill({json:{code:'OK',data:{userId:'9',organizationId:'1',displayName:'只读生产人员',permissionCodes:['production:order:view'],mustChangePassword:false}}}));await page.goto('/production/orders')
 if(isMobile)await page.getByRole('button',{name:'打开导航',exact:true}).click()
 for(const title of ['基础主数据','仓库管理','质量管理','系统管理'])await expect(menu.getByText(title,{exact:true})).toHaveCount(0)
 await expect(menu.getByText('生产订单',{exact:true})).toBeVisible();await healthy(page,errors)
})
