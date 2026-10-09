import {test,expect} from '@playwright/test'
test('dictionary CSV preview/import and TREE editing follow PC design',async({page})=>{
 await page.setViewportSize({width:1440,height:900})
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message))
 const original={id:'3',code:'STORAGE_ENVIRONMENT',name:'存储环境',kind:'BUSINESS',structure:'TREE',description:'仓储环境',sortNo:15,status:'ACTIVE',version:0,itemCount:2,createdAt:'2026-10-08',updatedAt:'2026-10-08'}
 let submitted:any=null
 await page.route('**/api/v1/**',route=>{
  const request=route.request(),u=new URL(request.url()),p=u.pathname
  let data:any={}
  if(p.endsWith('/auth/me'))data={userId:'1',organizationId:'1',loginName:'visual',displayName:'视觉评审',roleCodes:['SYSTEM_ADMIN'],permissionCodes:['iam:dict:view','iam:dict:create','iam:dict:update'],mustChangePassword:false}
  if(p.endsWith('/auth/navigation'))data=[{id:'1',menuCode:'nav_system',title:'系统管理',path:null,children:[{id:'2',menuCode:'nav_dictionaries',title:'数据字典',path:'/admin/dictionaries',permissionCode:'iam:dict:view',children:[]}]}]
  if(p.endsWith('/dictionaries'))data={items:[original],total:1,page:0,size:20}
  if(p.endsWith('/dictionaries/3/items'))data=[{id:'11',dictTypeId:'3',parentId:null,code:'LOW_TEMP',label:'低温',description:null,sortNo:10,status:'ACTIVE',isDefault:false,version:0},{id:'12',dictTypeId:'3',parentId:'11',code:'REFRIGERATED',label:'冷藏',description:null,sortNo:20,status:'ACTIVE',isDefault:false,version:0}]
  if(p.endsWith('/dictionaries/import/preview'))data={accepted:1,rejected:1,items:[{line:2,code:'MES_TYPE_NEW',name:'新的字典',result:'PASS',message:'可导入'},{line:3,code:'STORAGE_ENVIRONMENT',name:'存储环境',result:'CONFLICT',message:'数据库已存在'}]}
  if(p.endsWith('/dictionaries/import')){submitted=request.postDataJSON();data={importedCount:submitted.length,imported:[]}}
  return route.fulfill({json:{code:'OK',message:'Success',data,traceId:'visual-fixture'}})
 })
 await page.goto('/admin/dictionaries')
 await page.getByRole('button',{name:'导入 CSV'}).click()
 await expect(page.getByText('数据字典导入预检')).toBeVisible()
 const csv='字典CODE,字典名称,字典类型,字典结构,描述,排序,状态\r\nMES_TYPE_NEW,新的字典,BUSINESS,FLAT,导入测试,10,ACTIVE\r\nSTORAGE_ENVIRONMENT,存储环境,BUSINESS,TREE,冲突测试,20,ACTIVE\r\n'
 await page.locator('input[type=file]').setInputFiles({name:'字典.csv',mimeType:'text/csv',buffer:Buffer.from(csv,'utf8')})
 await expect(page.getByRole('button',{name:'仅导入 1 条通过项'})).toBeEnabled()
 await page.screenshot({path:'../../design/系统管理/数据字典/review/数据字典-Vue导入预检-模拟数据.png'})
 await page.getByRole('button',{name:'仅导入 1 条通过项'}).click()
 expect(submitted).toHaveLength(1)
 expect(submitted[0].code).toBe('MES_TYPE_NEW')
 await page.getByRole('button',{name:'2 项'}).click()
 await expect(page.getByText(/树形层级/)).toBeVisible()
 await expect(page.getByText('低温（LOW_TEMP）')).toBeVisible()
 await expect(page.getByText('冷藏（REFRIGERATED）')).toBeVisible()
 await page.getByText('低温（LOW_TEMP）').click()
 await expect(page.locator('.dict-item-editor')).toBeVisible()
 const pairs=await page.locator('.dict-item-grid>label').evaluateAll(labels=>labels.map(x=>{
  const a=x.querySelector('.form-field-label')?.getBoundingClientRect(),b=x.querySelector('input,.ant-select-selector,.ant-checkbox')?.getBoundingClientRect()
  return !!a&&!!b&&a.right<=b.left&&a.bottom>b.top&&b.bottom>a.top
 }))
 expect(pairs.every(Boolean)).toBe(true)
 await page.screenshot({path:'../../design/系统管理/数据字典/review/数据字典-Vue树形维护-模拟数据.png'})
 expect(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth)).toBe(false)
 expect(errors).toEqual([])
})
