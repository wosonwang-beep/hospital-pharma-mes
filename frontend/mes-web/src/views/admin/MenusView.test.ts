import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/vue'
import Antd from 'ant-design-vue'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import MenusView from './MenusView.vue'
const mocks = vi.hoisted(() => ({ api: vi.fn() }))
vi.mock('../../stores/auth', () => ({ useAuthStore: () => ({ can: () => true }) }))
vi.mock('../../api/http', () => ({ api: (...args: any[]) => mocks.api(...args), errorMessage: () => '菜单请求失败', idempotencyKey: () => 'menu-test-key' }))
vi.mock('vue-router', () => ({onBeforeRouteLeave:()=>{},useRouter:()=>({resolve:()=>({matched:[{path:'/master/materials'}]})})}))
const menu = { id: '1', menuCode: 'master:material:view', menuName: '物料主数据', routePath: '/master/materials', permissionCode: 'master:material:view', permissionCodes: ['master:material:view'], parentId: null, sortNo: 1, status: 'ACTIVE', version: 3 }
beforeEach(() => { vi.clearAllMocks(); mocks.api.mockImplementation(async(r:any)=>({items:r.url==='/permissions'?[{id:'1',permissionCode:'master:material:view',permissionName:'查看物料',permissionType:'ACTION',status:'ACTIVE'}]:[menu],total:1})) })
afterEach(cleanup)
test('loads the database menu tree without issuing mutations', async () => {
  render(MenusView, { global: { plugins: [Antd] } })
  expect(await screen.findByText('物料主数据')).toBeTruthy()
  expect(mocks.api.mock.calls.every(([r]) => !r.method)).toBe(true)
})
test('edit validates name, uses optimistic version and retains the form on conflict', async () => {
  render(MenusView, { global: { plugins: [Antd] } })
  await screen.findByText('物料主数据'); await fireEvent.click(screen.getByRole('button', { name: /编\s*辑/ }))
  await fireEvent.update(await screen.findByRole('textbox',{name:'菜单名称'}),'')
  await fireEvent.click(await screen.findByRole('button', { name: '保存菜单配置' }))
  expect(await screen.findByText('请填写编码、名称、菜单页面与查看权限')).toBeTruthy()
  expect(mocks.api.mock.calls.every(([r]) => !r.method)).toBe(true)
  await fireEvent.update(screen.getByRole('textbox', { name: '菜单名称' }), '物料资料')

  mocks.api.mockRejectedValue(new Error('version conflict'))
  await fireEvent.click(screen.getByRole('button', { name: '保存菜单配置' }))
  await screen.findByText('菜单请求失败')
  await waitFor(() => expect((screen.getByRole('textbox', { name: '菜单名称' }) as HTMLInputElement).value).toBe('物料资料'))
  expect(mocks.api.mock.calls.find(([r]) => r.method)?.[0]).toMatchObject({ method: 'PUT', url: '/menus/1', headers: { 'If-Match': '"3"' }, data: { menuName: '物料资料', reason: '菜单配置维护（系统记录）' } })
}, 15000)

test('new child permission retries binding without creating a duplicate or losing existing menu functions',async()=>{
  const created={id:'2',permissionCode:'master:material:export',permissionName:'导出物料',permissionType:'ACTION',status:'ACTIVE'}
  let failures=1
  mocks.api.mockImplementation(async(r:any)=>{
    if(!r.method)return {items:r.url==='/permissions'?[{...created,permissionCode:'master:material:view',permissionName:'查看物料'}]:[menu],total:1}
    if(r.url==='/permissions')return created
    if(failures-- >0)throw new Error('conflict')
    return {...menu,version:4,permissionCodes:r.data.permissionCodes}
  })
  render(MenusView,{global:{plugins:[Antd]}})
  await fireEvent.click(await screen.findByRole('button',{name:'详情与子权限'}))
  await fireEvent.click(await screen.findByRole('button',{name:'新增子权限'}))
  await fireEvent.update(await screen.findByRole('textbox',{name:'子权限名称'}),'导出物料')
  await fireEvent.update(screen.getByRole('textbox',{name:'子权限编码'}),'master:material:export')

  await fireEvent.click(screen.getByRole('button',{name:'保存子权限'}))
  await screen.findByText(/权限定义已创建，菜单关联未完成/)
  await fireEvent.click(await screen.findByRole('button',{name:'保存子权限'}))
  await screen.findByText('子权限已关联到菜单')
  const writes=mocks.api.mock.calls.map(([r])=>r).filter(r=>r.method)
  expect(writes.map(r=>r.url)).toEqual(['/permissions','/menus/1/permissions','/menus/1/permissions'])
  expect(writes[2].data.permissionCodes).toEqual(['master:material:view','master:material:export'])
  expect(writes[2].headers['If-Match']).toBe('"3"')
},15000)
