import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/vue'
import { reactive, nextTick } from 'vue'
import Antd from 'ant-design-vue'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import RoleDetailView from './RoleDetailView.vue'
const mocks = vi.hoisted(() => ({ route: null as any, api: vi.fn(), push: vi.fn(), replace: vi.fn(), leave: vi.fn(), update: vi.fn() }))
vi.mock('vue-router', () => ({ useRoute: () => mocks.route, useRouter: () => ({ push: mocks.push, replace: mocks.replace }), onBeforeRouteLeave: (fn: any) => mocks.leave(fn), onBeforeRouteUpdate: (fn: any) => mocks.update(fn) }))
vi.mock('../../stores/auth', () => ({ useAuthStore: () => ({ can: () => true }) }))
vi.mock('../../api/http', () => ({ api: (...args: any[]) => mocks.api(...args), errorMessage: () => '测试请求失败', idempotencyKey: () => 'test-key' }))
const baseRole = { id: '1', roleCode: 'QA', roleName: '质量岗位', status: 'ACTIVE', version: 4, permissionCodes: ['iam:user:view', 'retired:hidden'], menuCodes: [] }
const permission = (i: number) => ({ id: String(i), permissionCode: i === 1 ? 'iam:user:view' : `qms:test:p${i}`, permissionName: `权限${i}`, permissionType: 'ACTION', status: 'ACTIVE', version: 0 })
function mount() { return render(RoleDetailView, { global: { plugins: [Antd], stubs: {transition:false, "transition-group":false} } }) }
beforeEach(() => {
  vi.clearAllMocks(); mocks.route = reactive({ params: { id: '1' }, meta: { mode: 'edit' }, query: {} })
  mocks.api.mockImplementation(async (request: any) => request.url === '/menus' ? { items: [], total: 0 } : request.url === '/permissions' ? { items: [permission(1)], total: 1, page: 0, size: 100 } : { ...baseRole })
  mocks.replace.mockResolvedValue(undefined)
})
afterEach(cleanup)
test('loads permissions beyond the first 100 and preserves unlisted existing grants', async () => {
  mocks.api.mockImplementation(async (r: any) => r.url === '/menus' ? { items: [], total: 0 } : r.url === '/permissions' ? { items: r.params.page === 0 ? Array.from({ length: 100 }, (_, i) => permission(i + 1)) : [permission(101)], total: 101, page: r.params.page, size: 100 } : { ...baseRole })
  mount(); await screen.findByText('已选 2 项 / 可用 101 项')
  expect(mocks.api).toHaveBeenCalledWith(expect.objectContaining({ url: '/permissions', params: { page: 1, size: 100, status: 'ACTIVE' } }))
  await fireEvent.update(screen.getByRole('textbox', { name: '搜索权限' }), '权限101')
  expect(await screen.findByText('权限101')).toBeTruthy()
  expect(screen.getByText('已选 2 项 / 可用 101 项')).toBeTruthy()
})
test('view to edit and another role reload data on the reused component', async () => {
  mocks.route.meta.mode = 'view'; mount()
  await screen.findByText('已选 2 项 / 可用 1 项')
  expect((screen.getByRole('textbox', { name: '角色名称' }) as HTMLInputElement).disabled).toBe(true)
  mocks.route.meta.mode = 'edit'; await nextTick()
  await waitFor(() => expect((screen.getByRole('textbox', { name: '角色名称' }) as HTMLInputElement).disabled).toBe(false))
  mocks.api.mockImplementation(async (r: any) => r.url === '/menus' ? { items: [], total: 0 } : r.url === '/permissions' ? { items: [], total: 0, page: 0, size: 100 } : { ...baseRole, id: '2', roleName: '仓储岗位', permissionCodes: [] })
  mocks.route.params.id = '2'
  await waitFor(() => expect((screen.getByRole('textbox', { name: '角色名称' }) as HTMLInputElement).value).toBe('仓储岗位'))
})
test('validates role name before sending any write', async () => {
  mount(); await screen.findByText('已选 2 项 / 可用 1 项'); await waitFor(() => expect((screen.getByRole('button', { name: '保 存' }) as HTMLButtonElement).disabled).toBe(false))
  await fireEvent.update(screen.getByRole('textbox', {name:'角色名称'}),'')
  await fireEvent.click(await screen.findByRole('button', { name: '保 存' }))
  expect(await screen.findByText('请输入角色名称')).toBeTruthy()
  expect(mocks.api.mock.calls.every(([r]) => !r.method)).toBe(true)
})
test('permission-only changes use one assignment with all retained grants and the current version', async () => {
  mount(); await screen.findByText('已选 2 项 / 可用 1 项'); await waitFor(() => expect((screen.getByRole('button', { name: '保 存' }) as HTMLButtonElement).disabled).toBe(false))
  await fireEvent.click(screen.getByRole('radio', { name: '不授权' }))

  mocks.api.mockImplementation(async (r: any) => ({ ...baseRole, version: 5, permissionCodes: r.data.permissionCodes }))
  await fireEvent.click(await screen.findByRole('button', { name: '保 存' }))
  await screen.findByText('角色及权限已保存')
  const writes = mocks.api.mock.calls.map(([r]) => r).filter(r => r.method)
  expect(writes).toHaveLength(1)
  expect(writes[0]).toMatchObject({ method: 'POST', url: '/roles/1/permissions', headers: { 'If-Match': '"4"' }, data: { permissionCodes: ['retired:hidden'], reason: '角色配置维护（系统记录）' } })
})
test('partial failure reports saved metadata and preserves pending selections for retry', async () => {
  mount(); await screen.findByText('已选 2 项 / 可用 1 项'); await waitFor(() => expect((screen.getByRole('button', { name: '保 存' }) as HTMLButtonElement).disabled).toBe(false))
  await fireEvent.update(screen.getByRole('textbox', { name: '角色名称' }), '新岗位名称')
  await fireEvent.click(screen.getByRole('radio', { name: '不授权' }))

  mocks.api.mockImplementation(async (r: any) => { if (r.method === 'PUT') return { ...baseRole, roleName: '新岗位名称', version: 5 }; throw new Error('assign failed') })
  await fireEvent.click(await screen.findByRole('button', { name: '保 存' }))
  await screen.findByText(/角色信息已保存，权限分配未完成/)
  expect(screen.queryByText('角色及权限已保存')).toBeNull()
  expect((screen.getByRole('radio', { name: '不授权' }) as HTMLInputElement).checked).toBe(true)
  mocks.api.mockImplementation(async () => ({ ...baseRole, roleName: '新岗位名称', version: 6, permissionCodes: ['retired:hidden'] }))
  await waitFor(()=>expect(screen.getByRole('button', {name:/保 存$/}).classList.contains('ant-btn-loading')).toBe(false),{timeout:5000})
  await fireEvent.click(screen.getByRole('button', { name: /保 存$/ }))
  await screen.findByText('角色及权限已保存')
  const writes = mocks.api.mock.calls.map(([r]) => r).filter(r => r.method)
  expect(writes.map(r => r.method)).toEqual(['PUT', 'POST', 'POST'])
  expect(writes[2]!.headers['If-Match']).toBe('"5"')
})
test('dirty role changes cannot be lost without a leave confirmation', async () => {
  mount(); await screen.findByText('已选 2 项 / 可用 1 项'); await waitFor(() => expect((screen.getByRole('button', { name: '保 存' }) as HTMLButtonElement).disabled).toBe(false))
  await fireEvent.update(screen.getByRole('textbox', { name: '角色名称' }), '未保存名称')
  const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false)
  expect(mocks.leave.mock.calls[0]![0]()).toBe(false)
  expect(confirm).toHaveBeenCalled()
  confirm.mockRestore()
})
test('switching modules and selecting a visible group preserves other and unavailable grants', async () => {
  mocks.api.mockImplementation(async (r: any) => r.url === '/menus' ? { items: [], total: 0 } : r.url === '/permissions' ? { items: [permission(1), permission(2)], total: 2 } : { ...baseRole })
  mount(); await screen.findByText('已选 2 项 / 可用 2 项')
  await fireEvent.update(screen.getByRole('textbox', {name:'搜索权限'}), '权限2')
  await fireEvent.click(screen.getByRole('checkbox', { name: /权限2.*特殊操作权限/ }))
  expect(screen.getByText('已选 3 项 / 可用 2 项')).toBeTruthy()
  expect(screen.getByText('本次权限变更：新增 1 项，移除 0 项')).toBeTruthy()
  await fireEvent.update(screen.getByRole('textbox', {name:'搜索权限'}), '')
  expect((screen.getByRole('radio', { name: '只查看' }) as HTMLInputElement).checked).toBe(true)

  mocks.api.mockImplementation(async (r: any) => ({ ...baseRole, version: 5, permissionCodes: r.data.permissionCodes }))
  await fireEvent.click(await screen.findByRole('button', { name: '保 存' }))
  await screen.findByText('角色及权限已保存')
  expect(mocks.api.mock.calls.find(([r]) => r.method)?.[0].data.permissionCodes).toEqual(expect.arrayContaining(['iam:user:view', 'retired:hidden', 'qms:test:p2']))
})
test('CRUD preset bundles ordinary actions without granting special export, and readonly preset removes editing only', async () => {
  const catalog = ['iam:user:view', 'iam:user:create', 'iam:user:update', 'iam:user:export'].map((permissionCode, i) => ({ ...permission(i + 1), permissionCode }))
  mocks.api.mockImplementation(async (r: any) => r.url === '/menus' ? { items: [], total: 0 } : r.url === '/permissions' ? { items: catalog, total: 4 } : { ...baseRole })
  mount(); await screen.findByText('已选 2 项 / 可用 4 项')
  await fireEvent.click(screen.getByRole('radio', { name: '增删改查' }))
  expect(screen.getByText('已选 4 项 / 可用 4 项')).toBeTruthy()
  expect((screen.getByRole('checkbox', { name: /导出用户/ }) as HTMLInputElement).checked).toBe(false)
  expect(screen.getByText('本次权限变更：新增 2 项，移除 0 项')).toBeTruthy()
  await fireEvent.click(screen.getByRole('checkbox', { name: /导出用户/ }))
  await fireEvent.click(screen.getByRole('radio', { name: '只查看' }))
  expect(screen.getByText('已选 3 项 / 可用 4 项')).toBeTruthy()
  expect((screen.getByRole('checkbox', { name: /导出用户/ }) as HTMLInputElement).checked).toBe(true)

  mocks.api.mockImplementation(async (r: any) => ({ ...baseRole, version: 5, permissionCodes: r.data.permissionCodes }))
  await fireEvent.click(await screen.findByRole('button', { name: '保 存' }))
  await screen.findByText('角色及权限已保存')
  expect(mocks.api.mock.calls.find(([r]) => r.method)?.[0].data.permissionCodes.sort()).toEqual(['iam:user:export', 'iam:user:view', 'retired:hidden'].sort())
})

test('checking a visible menu defaults ordinary and special grants, deselection retains unrelated grants', async () => {
  const codes=['iam:user:view','iam:user:create','iam:user:update','iam:user:export']
  const menu={id:'10',menuCode:'users',menuName:'用户管理',routePath:'/admin/users',parentId:null,sortNo:1,status:'ACTIVE',version:0,permissionCode:codes[0],permissionCodes:codes}
  mocks.api.mockImplementation(async(r:any)=>r.url==='/menus'?{items:[menu],total:1}:r.url==='/permissions'?{items:codes.map((permissionCode,i)=>({...permission(i+1),permissionCode})),total:4}:{...baseRole})
  const result=mount();await screen.findByText('已选 2 项 / 可用 4 项')
  await waitFor(()=>expect(result.container.querySelector('.ant-tree-checkbox')).toBeTruthy())
  const treeCheck=result.container.querySelector('.ant-tree-checkbox')!
  await fireEvent.click(treeCheck)
  expect(await screen.findByText('已选 5 项 / 可用 4 项')).toBeTruthy()
  expect((screen.getByRole('checkbox',{name:/导出用户/}) as HTMLInputElement).checked).toBe(true)
  await fireEvent.click(screen.getByRole('radio',{name:'只查看'}))
  expect(screen.getByText('已选 3 项 / 可用 4 项')).toBeTruthy()

  mocks.api.mockImplementation(async(r:any)=>({...baseRole,version:5,permissionCodes:r.data.permissionCodes,menuCodes:r.data.menuCodes}))
  await fireEvent.click(screen.getByRole('button',{name:'保 存'}));await screen.findByText('角色及权限已保存')
  expect(mocks.api.mock.calls.find(([r])=>r.method)?.[0].data).toMatchObject({menuCodes:['users'],permissionCodes:expect.arrayContaining(['iam:user:view','iam:user:export','retired:hidden'])})
},15000)
