import {test,expect,type Page} from '@playwright/test'
import {mkdirSync,writeFileSync} from 'node:fs'
import {resolve} from 'node:path'
// Browser plugin not available: use repository Chromium to render the real Vue app.
// Only API responses are mocked; no runtime/auth bypass or business writes are introduced.
const output=resolve('../../docs/acceptance/ui-v2-phase-1.1/screens/after')
mkdirSync(output,{recursive:true})
const perms=['master:material:view','master:material:create','master:material:update','qa:release','qa:batch-review','ebr:form:view','ebr:pdf:generate']
async function fixture(page:Page){
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));page.on('console',m=>{if(m.type()==='error')errors.push(m.text())})
 await page.route('**/api/v1/**',route=>{const path=new URL(route.request().url()).pathname.replace('/api/v1','');const reply=(data:unknown)=>route.fulfill({json:{code:'OK',message:'success',data,traceId:'ui-v2-phase1'}})
 if(path==='/auth/me')return reply({userId:'8',organizationId:'1',displayName:'UI验证',permissionCodes:perms,mustChangePassword:false})
 if(path==='/qa/batches/41/review-model')return reply({mainBatchId:'41',versionNo:7,batchNo:'MB-41',status:'PENDING_QA',finishedLots:['51'],gates:['BATCH_QA_STATE','QUALITY_PLAN','EBR_REVIEW','PRODUCTION_QC','MATERIAL_BALANCE','FINISHED_INVENTORY'].map((code,i)=>({code,passed:true,blockingCodes:[],evidenceIds:[String(i+1)]})),blockingCodes:[],reviewDigest:'a'.repeat(64),allowedActions:['RELEASE','REJECT']})
 if(path==='/main-batches/41/ebr')return reply({mainBatchId:'41',definitionHash:'a'.repeat(64),recordDigest:'a'.repeat(64),batch:{productId:'21',plannedQty:'100',unitId:'1'},forms:[{form:{id:'61'},values:[{revisionNo:1,value:'原始记录'}],reviews:[],ruleExecutions:[],signatures:[]}],qcTests:[],balances:[],deviations:[],decisions:[],pdfManifests:[],processSnapshot:{id:'31'},operations:[],charges:[],quantityEvents:[],genealogy:[],signatures:[]})
 return reply({items:[],total:0,page:0,size:20})
 });return errors
}
async function capture(page:Page,name:string,fullPage=true){await page.screenshot({path:resolve(output,`${name}-${test.info().project.name}.png`),fullPage,scale:'css',animations:'disabled'})}
async function noOverflow(page:Page){expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true)}
async function pairs(page:Page){const input=page.getByLabel('物料名称'),label=input.locator('..').locator('.form-field-label');const a=await input.boundingBox(),b=await label.boundingBox();expect(a&&b&&b.x+b.width<=a.x+2&&Math.max(a.y,b.y)<Math.min(a.y+a.height,b.y+b.height)).toBe(true)}
test('V2 shell and independent T1/T2 visual foundation',async({page,isMobile})=>{
 const errors=await fixture(page);await page.goto('/master/materials');await expect(page.getByRole('heading',{name:'物料主数据'})).toBeVisible();await noOverflow(page)
 const metrics=await page.evaluate(()=>{const header=document.querySelector('.header')!,title=document.querySelector('h1')!,card=document.querySelector('.query-card')!;return {header:header.getBoundingClientRect().height,title:getComputedStyle(title).fontSize,radius:getComputedStyle(card).borderRadius,background:getComputedStyle(header).backgroundColor}})
 expect(metrics.header).toBe(56);expect(metrics.title).toBe(isMobile?'20px':'22px');expect(metrics.radius).toBe('8px');expect(metrics.background).toBe('rgb(255, 255, 255)')
 const density=await page.evaluate(()=>({queryHeight:document.querySelector('.query-card')!.getBoundingClientRect().height,emptyHeight:document.querySelector('.ant-table-placeholder')!.getBoundingClientRect().height,columns:getComputedStyle(document.querySelector('.query-form')!).gridTemplateColumns.split(' ').length}))
 if(!isMobile){expect(density.queryHeight).toBeGreaterThanOrEqual(110);expect(density.queryHeight).toBeLessThanOrEqual(125);expect(density.columns).toBe(4)}else expect(density.columns).toBe(1)
 expect(density.emptyHeight).toBeLessThan(120)
 writeFileSync(resolve(output,`metrics-${test.info().project.name}.json`),JSON.stringify(density,null,2))
 await capture(page,'t1-material-query')
 if(!isMobile){await expect(page.locator('.sidebar')).toHaveCSS('width','228px');await page.getByRole('button',{name:'折叠侧栏'}).click();await expect(page.locator('.main-shell')).toHaveCSS('margin-left','80px');await noOverflow(page);await expect(page.locator('.sidebar .ant-menu-item-selected .anticon')).toBeVisible();await capture(page,'shell-collapsed');await page.getByRole('button',{name:'展开侧栏'}).click();await expect(page.locator('.main-shell')).toHaveCSS('margin-left','228px')}
 else {await page.getByRole('button',{name:'打开导航'}).click();await expect(page.getByRole('menuitem',{name:/物料主数据/})).toBeVisible();await page.getByRole('menuitem',{name:/物料主数据/}).click();await expect(page.locator('.query-form')).toBeVisible()}
 await page.locator('.query-form input').first().fill('UI核对');await page.getByRole('button',{name:/查\s*询/}).click();await expect(page.locator('.query-form input').first()).toHaveValue('UI核对');await page.getByRole('button',{name:/重\s*置/}).click();await expect(page.locator('.query-form input').first()).toHaveValue('')
 await page.goto('/master/materials/create');await expect(page.locator('.master-form')).toBeVisible();await page.getByLabel('物料名称').fill('视觉基础验证');await pairs(page);await noOverflow(page);const formMetrics=await page.locator('.form-section>.ant-card-body>.master-form').evaluate(form=>({maxWidth:getComputedStyle(form).maxWidth,columns:getComputedStyle(form).gridTemplateColumns.split(' ').length,labelWidth:form.querySelector('.form-field-label')!.getBoundingClientRect().width}))
 expect(formMetrics.maxWidth).toBe('1100px');expect(formMetrics.columns).toBe(isMobile?1:2);if(isMobile)expect(formMetrics.labelWidth).toBe(95)
 const alignment=await page.locator('.master-form>label').evaluateAll(labels=>labels.map(label=>{const control=label.querySelector('input,textarea,.ant-select');return control?.getBoundingClientRect().left}));if(isMobile)expect(new Set(alignment).size).toBe(1)
 await capture(page,'t2-material-form')
 if(!isMobile){await page.setViewportSize({width:1680,height:900});await expect(page.locator('.form-section>.ant-card-body>.master-form')).toHaveCSS('max-width','1100px');expect((await page.locator('.form-section>.ant-card-body>.master-form').boundingBox())!.width).toBe(1100);await noOverflow(page);await capture(page,'t2-material-form-wide')}
 writeFileSync(resolve(output,`form-metrics-${test.info().project.name}.json`),JSON.stringify(formMetrics,null,2));expect(errors).toEqual([])
})
test('T6 remains a decision workbench; readable evidence and signed form',async({page})=>{
 const errors=await fixture(page);await page.goto('/qa/batches/41/release');await expect(page.getByRole('heading',{name:'QA 批放行审核'})).toBeVisible();await expect(page.locator('.gate-card')).toHaveCount(6);await noOverflow(page)
 await expect(page.locator('.gate-progress strong')).toHaveText('6 / 6');await expect(page.locator('.qa-checklist').getByText('最终 eBR PDF',{exact:true})).toHaveCount(0);await expect(page.locator('.post-decision-archive').getByText('最终 eBR PDF',{exact:true})).toBeVisible();await expect(page.getByText('以下为质量决定后的归档工作，不计入放行前 Gate。',{exact:true})).toBeVisible()
 const text=await page.locator('.gate-description p').evaluateAll(nodes=>nodes.map(node=>parseFloat(getComputedStyle(node).fontSize)));expect(text.every(size=>size>=12)).toBe(true);await capture(page,'t6-qa-review')
 await page.getByText('eBR 表单与原始修订（1）',{exact:true}).click();await expect(page.getByText('原始记录',{exact:true})).toBeVisible();await expect(page.getByText('电子批记录表单与原始修订',{exact:true})).toBeVisible();for(const title of ['基本信息','原始记录与修订','复核记录','规则执行记录','电子签名记录'])await expect(page.getByRole('heading',{name:title,exact:true})).toBeVisible();for(const raw of ['form','values','reviews','ruleExecutions','signatures','value'])await expect(page.locator('.qa-evidence-drawer').getByText(raw,{exact:true})).toHaveCount(0);await expect.poll(()=>page.locator('.ant-drawer-content-wrapper').evaluate(node=>{const b=node.getBoundingClientRect();return b.left>=0&&b.right<=innerWidth})).toBe(true);await capture(page,'evidence-drawer',false);await page.getByRole('button',{name:'Close',exact:true}).click()
 await page.getByRole('button',{name:'签名并放行',exact:true}).click();await page.getByLabel('操作原因',{exact:true}).fill('核对展示，不提交业务决定');await page.getByRole('button',{name:'核对并签名'}).click();await expect(page.getByRole('dialog')).toBeVisible();await page.getByLabel('签名密码',{exact:true}).fill('not-submitted');await capture(page,'signature-dialog',false);await page.getByRole('button',{name:/取\s*消/,exact:true}).last().click();await expect(page.getByRole('dialog')).toHaveCount(0);expect(errors).toEqual([])
})
