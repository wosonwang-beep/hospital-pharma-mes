import {test,expect} from '@playwright/test'

test('department PC T1/T2/T3 and horizontal fields',async({page})=>{
 await page.setViewportSize({width:1440,height:900})
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message))
 const samples=[
  {id:'20',code:'PHARMACY',name:'药剂科',organizationId:'24',organizationName:'医院制剂室',
   parentId:null,parentName:null,leaderUserId:'1',leaderName:'系统管理员',phone:'020-88012345',description:'业务部门',
   sortNo:10,status:'ACTIVE',version:0,memberCount:1},
  {id:'21',code:'PREPARATION',name:'制剂室',organizationId:'24',organizationName:'医院制剂室',
   parentId:'20',parentName:'药剂科',leaderUserId:null,leaderName:null,phone:null,description:'生产职能',
   sortNo:20,status:'ACTIVE',version:0,memberCount:0}
 ]
 await page.route('**/api/v1/**',route=>{
  const u=new URL(route.request().url()),p=u.pathname
  let data:unknown={}
  if(p.endsWith('/auth/me'))data={userId:'1',organizationId:'1',loginName:'visual',displayName:'视觉评审',
    roleCodes:['SYSTEM_ADMIN'],permissionCodes:['iam:department:view','iam:department:create','iam:department:update','iam:user:view','iam:user:update'],mustChangePassword:false}
  else if(p.endsWith('/auth/navigation'))data=[{id:'1',menuCode:'nav_system',title:'系统管理',path:null,
    children:[{id:'2',menuCode:'nav_departments',title:'部门管理',path:'/admin/departments',permissionCode:'iam:department:view',children:[]}]}]
  else if(p.endsWith('/departments/tree'))data=samples
  else if(p.endsWith('/department-organizations'))data=[{id:'24',name:'医院制剂室',code:'HOSPITAL_PREPARATION'}]
  else if(p.endsWith('/department-users'))data=[{id:'1',name:'系统管理员',username:'admin'}]
  else if(p.endsWith('/departments'))data={items:samples,total:2,page:0,size:100}
  else if(p.endsWith('/departments/20'))data=samples[0]
  return route.fulfill({json:{code:'OK',message:'Success',data,traceId:'dept-visual-fixture'}})
 })
 await page.goto('/admin/departments')
 await expect(page.getByRole('heading',{name:'部门管理',exact:true})).toBeVisible()
 await expect(page.getByText('PREPARATION')).toBeVisible()
 await expect(page.locator('.dept-tree-card .ant-tree-title').getByText('药剂科',{exact:true})).toBeVisible()
 await expect(page.locator('.dept-tree-card .ant-tree-title').getByText('制剂室',{exact:true})).toBeVisible()
 const actions=await page.locator('.dept-layout .ant-table-tbody tr.ant-table-row').first().locator('button').evaluateAll(buttons=>buttons.map(b=>({x:b.getBoundingClientRect().right,visible:b.getBoundingClientRect().right<=innerWidth&&b.getBoundingClientRect().width>0})))
 console.log('DEPARTMENT_ACTION_VISIBILITY',JSON.stringify(actions))
 expect(actions.length).toBeGreaterThan(0);expect(actions.every(x=>x.visible)).toBe(true)
 const positions=await page.locator('.department-query>label').evaluateAll(labels=>labels.map(node=>{
  const a=node.querySelector('.form-field-label')?.getBoundingClientRect(),b=node.querySelector('input,.ant-select-selector')?.getBoundingClientRect()
  return {top:Math.round(node.getBoundingClientRect().top),inline:!!a&&!!b&&a.right<=b.left&&a.bottom>b.top&&b.bottom>a.top}
 }))
 console.log('DEPARTMENT_QUERY_INLINE',JSON.stringify(positions))
 expect(positions).toHaveLength(3);expect(positions.every(x=>x.inline)).toBe(true)
 expect(new Set(positions.map(x=>x.top)).size).toBe(1)
 await page.screenshot({path:'../../design/系统管理/部门管理/部门管理-Vue查询-模拟数据.png'})
 await page.getByRole('button',{name:'＋ 新增部门'}).click()
 await expect(page.getByRole('dialog',{name:'新增部门'})).toBeVisible()
 await page.waitForTimeout(420)
 const fields=await page.locator('.department-form>label').evaluateAll(labels=>labels.map(node=>{
  const a=node.querySelector('.form-field-label')?.getBoundingClientRect(),b=node.querySelector('input,textarea,.ant-select-selector,.ant-input-number')?.getBoundingClientRect()
  return {name:node.textContent?.trim().slice(0,8),ok:!!a&&!!b&&a.right<=b.left&&a.bottom>b.top&&b.bottom>a.top}
 }))
 console.log('DEPARTMENT_FORM_INLINE',JSON.stringify(fields))
 expect(fields).toHaveLength(9);expect(fields.every(x=>x.ok)).toBe(true)
 await page.screenshot({path:'../../design/系统管理/部门管理/部门管理-Vue新增-模拟数据.png'})
 await page.locator('.dept-buttons button').first().click()
 await page.getByRole('button',{name:'查看'}).first().click()
 await expect(page.getByRole('heading',{name:'部门详情'})).toBeVisible()
 await page.screenshot({path:'../../design/系统管理/部门管理/部门管理-Vue查看-模拟数据.png'})
 expect(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth)).toBe(false)
 expect(errors).toEqual([])
})
