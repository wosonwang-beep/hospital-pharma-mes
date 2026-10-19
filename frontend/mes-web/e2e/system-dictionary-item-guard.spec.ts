import {test,expect} from '@playwright/test'

test('dictionary unsaved item survives row/tree selection and save feedback stays in dialog (fixture)',async({page})=>{
 const items=[{id:'22',dictTypeId:'91',code:'MIXER',label:'混合设备',description:null,parentId:null,sortNo:10,status:'ACTIVE',isDefault:false,version:0}]
 const submitted:unknown[]=[]
 await page.route('**/api/v1/**',async route=>{
  const request=route.request(),path=new URL(request.url()).pathname
  const dictionary={id:'91',code:'EQUIPMENT_TYPE',name:'设备类型',kind:'BUSINESS',structure:'TREE',description:'设备分类',sortNo:10,status:'ACTIVE',version:0,itemCount:items.length}
  let data:unknown={}
  if(path.endsWith('/auth/me'))data={userId:'1',organizationId:'1',loginName:'visual',displayName:'视觉评审',roleCodes:['SYSTEM_ADMIN'],permissionCodes:['iam:dict:view','iam:dict:create','iam:dict:update'],mustChangePassword:false}
  else if(path.endsWith('/auth/navigation'))data=[]
  else if(path.endsWith('/dictionaries/91/items')){
   if(request.method()==='POST'){
    const payload=request.postDataJSON();submitted.push(payload)
    const item={id:'23',dictTypeId:'91',version:0,...payload};items.push(item);data=item
   }else data=items
  }else if(path.endsWith('/dictionaries'))data={items:[dictionary],total:1,page:0,size:20}
  await route.fulfill({json:{code:'OK',message:'Success',data,traceId:'fixture-item-guard'}})
 })
 await page.goto('/admin/dictionaries')
 await page.getByRole('button',{name:'管理字典项（1）',exact:true}).click()
 const modal=page.locator('.dictionary-modal'),editor=modal.locator('.dict-item-editor')
 await modal.getByRole('cell',{name:'混合设备',exact:true}).waitFor()
 const add=modal.getByRole('button',{name:'＋ 新增字典项',exact:true})
 await add.click()
 const code=editor.getByPlaceholder('请输入编码'),name=editor.getByPlaceholder('请输入名称')
 await code.fill('GRANULATOR');await name.fill('制粒设备')
 const row=modal.locator('tr[data-row-key]').first()
 await expect(row.getByRole('button',{name:'编辑',exact:true})).toBeDisabled()
 await expect(row.getByRole('button',{name:'停用',exact:true})).toBeDisabled()
 await modal.locator('.ant-tree-title').filter({hasText:'混合设备'}).click()
 await expect(code).toHaveValue('GRANULATOR');await expect(name).toHaveValue('制粒设备')
 await expect(editor.getByRole('heading',{name:'新增字典项',exact:true})).toBeVisible()
 await editor.getByRole('button',{name:'取 消',exact:true}).click()
 await expect(row.getByRole('button',{name:'编辑',exact:true})).toBeEnabled()
 await add.click();await code.fill('GRANULATOR');await name.fill('制粒设备')
 await editor.getByRole('button',{name:'新增字典项',exact:true}).click()
 await expect(modal.getByText('字典项保存成功',{exact:true})).toBeVisible()
 await expect(modal.getByRole('cell',{name:'制粒设备',exact:true})).toBeVisible()
 await expect(page.getByRole('button',{name:'管理字典项（2）',exact:true})).toBeVisible()
 expect(submitted).toHaveLength(1)
 expect(submitted[0]).toMatchObject({code:'GRANULATOR',label:'制粒设备'})
 await add.click()
 await expect(modal.getByText('字典项保存成功',{exact:true})).toHaveCount(0)
})
