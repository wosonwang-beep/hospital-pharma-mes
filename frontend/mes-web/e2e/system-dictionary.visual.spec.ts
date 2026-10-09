import {test,expect} from '@playwright/test'

test('system dictionary PC reference geometry and item modal (fixture visual test)',async({page})=>{
 await page.setViewportSize({width:1440,height:900})
 const errors:string[]=[];page.on('pageerror',error=>errors.push(error.message))
 const example={id:'91',code:'EQUIPMENT_TYPE',name:'设备类型',kind:'BUSINESS',structure:'FLAT',description:'设备分类',sortNo:10,status:'ACTIVE',version:0,itemCount:2}
 await page.route('**/api/v1/**',route=>{
  const p=new URL(route.request().url()).pathname
  let data:unknown={}
  if(p.endsWith('/auth/me'))data={userId:'1',organizationId:'1',loginName:'visual',displayName:'视觉评审',roleCodes:['SYSTEM_ADMIN'],permissionCodes:['iam:dict:view','iam:dict:create','iam:dict:update'],mustChangePassword:false}
  else if(p.endsWith('/auth/navigation'))data=[{id:'1',menuCode:'nav_system',title:'系统管理',name:'系统管理',path:null,children:[{id:'2',menuCode:'nav_dictionaries',title:'数据字典',name:'数据字典',path:'/admin/dictionaries',permissionCode:'iam:dict:view',children:[]}]}]
  else if(p.endsWith('/dictionaries'))data={items:[example],total:1,page:0,size:20}
  else if(p.endsWith('/dictionaries/91/items'))data=[{id:'22',dictTypeId:'91',code:'MIXER',label:'混合设备',description:'混合设备分类',parentId:null,sortNo:10,status:'ACTIVE',isDefault:false,version:0},{id:'23',dictTypeId:'91',code:'WEIGHING',label:'称量设备',description:null,parentId:null,sortNo:20,status:'ACTIVE',isDefault:false,version:0}]
  else if(p.endsWith('/dictionaries/91'))data=example
  return route.fulfill({json:{code:'OK',message:'Success',data,traceId:'fixture-visual'}})
 })
 await page.goto('/admin/dictionaries')
 await expect(page.getByRole('heading',{name:'数据字典',exact:true})).toBeVisible()
 await expect(page.getByText('EQUIPMENT_TYPE')).toBeVisible()
 async function check(selector:string){
  return page.locator(selector).evaluateAll(nodes=>nodes.map(el=>{const label=el.querySelector('span'),control=el.querySelector('input,select,textarea,.ant-select-selector');if(!label||!control)return false;const a=label.getBoundingClientRect(),b=control.getBoundingClientRect();return a.right<b.left&&Math.max(a.top,b.top)<Math.min(a.bottom,b.bottom)}))
 }
 const checks=await check('.dict-query>label')
 console.log('DICT_QUERY_INLINE',checks)
 const rows=await page.locator('.dict-query>label').evaluateAll(nodes=>nodes.map(el=>Math.round(el.getBoundingClientRect().top)))
 console.log('DICT_QUERY_ROWS',rows)
 expect(rows[0]).toBe(rows[1]);expect(rows[1]).toBe(rows[2]);expect(rows[3]).toBeGreaterThan(rows[0]);expect(rows[4]).toBe(rows[3])
 expect(checks.length).toBe(5);expect(checks.every(Boolean)).toBe(true)
 await page.screenshot({path:'../../design/系统管理/数据字典/review/数据字典-Vue查询-模拟数据.png',fullPage:false})
 await page.getByRole('button',{name:'2 项'}).click()
 await expect(page.getByRole('cell',{name:'混合设备',exact:true})).toBeVisible()
 await page.screenshot({path:'../../design/系统管理/数据字典/review/数据字典-Vue字典项-模拟数据.png',fullPage:false})
 await page.getByRole('button',{name:'＋ 新增字典项'}).click()
 const itemChecks=await check('.dict-item-grid>label')
 console.log('DICT_ITEM_INLINE',itemChecks)
 expect(itemChecks.every(Boolean)).toBe(true)
 await page.screenshot({path:'../../design/系统管理/数据字典/review/数据字典-Vue选项编辑-模拟数据.png',fullPage:false})
 await page.keyboard.press('Escape')
 await page.getByRole('button',{name:'＋ 新增字典',exact:true}).click()
 await expect(page.getByRole('heading',{name:'新增数据字典'})).toBeVisible()
 const formChecks=await check('.dict-form>label')
 console.log('DICT_FORM_INLINE',formChecks)
 expect(formChecks.length).toBeGreaterThan(5);expect(formChecks.every(Boolean)).toBe(true)
 await page.screenshot({path:'../../design/系统管理/数据字典/review/数据字典-Vue新增-模拟数据.png',fullPage:false})
 expect(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth)).toBe(false)
 expect(errors).toEqual([])
})
