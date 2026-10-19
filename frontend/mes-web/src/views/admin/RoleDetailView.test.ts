import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/vue'
import { reactive, nextTick } from 'vue'
import Antd from 'ant-design-vue'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import RoleDetailView from './RoleDetailView.vue'

const mocks = vi.hoisted(() => ({
  route: null as any, api: vi.fn(), push: vi.fn(), replace: vi.fn(), leave: vi.fn(), update: vi.fn()
}))
vi.mock('vue-router', () => ({
  useRoute: () => mocks.route, useRouter: () => ({ push: mocks.push, replace: mocks.replace }),
  onBeforeRouteLeave: (fn: any) => mocks.leave(fn), onBeforeRouteUpdate: (fn: any) => mocks.update(fn)
}))
vi.mock('../../stores/auth', () => ({ useAuthStore: () => ({ can: () => true }) }))
vi.mock('../../api/http', () => ({
  api: (...args: any[]) => mocks.api(...args), errorMessage: () => '菜单授权请求失败', idempotencyKey: () => 'unique-role-key'
}))

const baseRole = {
  id: '1', roleCode: 'QA', roleName: '质量岗位', status: 'ACTIVE', version: 4,
  permissionCodes: ['iam:user:view', 'retired:hidden'], menuCodes: [] as string[]
}
const codes = ['iam:user:view', 'iam:user:create', 'iam:user:update', 'iam:user:export']
const permission = (permissionCode: string, id = '1') => ({
  id, permissionCode, permissionName: permissionCode.endsWith(':export') ? '导出用户' :
    permissionCode.endsWith(':view') ? '查看用户' : permissionCode.endsWith(':update') ? '编辑用户' : '新增用户',
  permissionType: 'ACTION', status: 'ACTIVE', version: 0
})
const catalog = codes.map((code, i) => permission(code, String(i + 1)))
const users = {
  id: '10', menuCode: 'users', menuName: '用户管理', routePath: '/admin/users',
  parentId: null, sortNo: 1, status: 'ACTIVE', version: 0,
  permissionCode: 'iam:user:view', permissionCodes: codes
}
const another = {
  ...users, id: '11', menuCode: 'user-read', menuName: '用户查询',
  routePath: '/admin/users/search', sortNo: 2, permissionCodes: ['iam:user:view']
}

function mount() {
  return render(RoleDetailView, { global: { plugins: [Antd], stubs: { transition: false, 'transition-group': false } } })
}
function setup(data: { role?: any; menus?: any[]; permissions?: any[] } = {}) {
  const role = data.role ?? baseRole
  const menuRows = data.menus ?? []
  const rows = data.permissions ?? [catalog[0]]
  mocks.api.mockImplementation(async (r: any) => {
    if (r.url === '/menus') return { items: menuRows, total: menuRows.length, page: 0, size: 100 }
    if (r.url === '/permissions') return { items: rows, total: rows.length, page: 0, size: 100 }
    if (r.method === 'POST' && r.url === '/roles/1/permissions') {
      return { ...role, version: role.version + 1,
        permissionCodes: r.data.permissionCodes, menuCodes: r.data.menuCodes }
    }
    return role
  })
}
async function ready() {
  await screen.findByRole('textbox', { name: '角色名称' })
  await waitFor(() => expect((screen.getByRole('button', { name: /保\s*存$/ }) as HTMLButtonElement).disabled).toBe(false))
}
async function toggleTree(container: Element, label: string) {
  const row = [...container.querySelectorAll('.ant-tree-treenode')].find(x => x.textContent?.includes(label))
  expect(row).toBeTruthy()
  const cb = row!.querySelector('.ant-tree-checkbox')
  expect(cb).toBeTruthy()
  await fireEvent.click(cb!)
}
function writes() { return mocks.api.mock.calls.map(([r]) => r).filter(r => !!r.method) }
function chosenCheckbox(label: RegExp) {
  return screen.getByRole('checkbox', { name: label }) as HTMLInputElement
}

beforeEach(() => {
  vi.clearAllMocks()
  mocks.route = reactive({ params: { id: '1' }, meta: { mode: 'edit' }, query: {} })
  mocks.replace.mockResolvedValue(undefined)
  setup()
})
afterEach(cleanup)

test('pages over 100 permissions, preserves an unavailable historic grant and reads a special code', async () => {
  const hundred = Array.from({ length: 100 }, (_, i) => permission('qms:test:p' + (i + 1), String(i + 1)))
  const last = { ...permission('qms:test:p101', '101'), permissionName: '专项权限101' }
  mocks.api.mockImplementation(async (r: any) => r.url === '/menus'
    ? { items: [], total: 0 }
    : r.url === '/permissions'
      ? { items: r.params.page === 0 ? hundred : [last], total: 101, page: r.params.page, size: 100 }
      : { ...baseRole })
  mount()
  expect(await screen.findByText('已选 2 项 / 可用 101 项')).toBeTruthy()
  expect(mocks.api).toHaveBeenCalledWith(expect.objectContaining({
    url: '/permissions', params: { page: 1, size: 100, status: 'ACTIVE' }
  }))
  await fireEvent.update(screen.getByRole('textbox', { name: '搜索权限' }), 'p101')
  expect(await screen.findByText('专项权限101')).toBeTruthy()
  expect(screen.getByText(/未在可用目录展示/)).toBeTruthy()
})

test('view/edit routes and role changes reload the server without stale grants', async () => {
  mocks.route.meta.mode = 'view'
  mount()
  await screen.findByText('已选 2 项 / 可用 1 项')
  expect((screen.getByRole('textbox', { name: '角色名称' }) as HTMLInputElement).disabled).toBe(true)
  mocks.route.meta.mode = 'edit'; await nextTick()
  await waitFor(() => expect((screen.getByRole('textbox', { name: '角色名称' }) as HTMLInputElement).disabled).toBe(false))
  setup({ role: { ...baseRole, id: '2', roleName: '仓储岗位', permissionCodes: [], menuCodes: [] }, permissions: [] })
  mocks.route.params.id = '2'
  await waitFor(() => expect((screen.getByRole('textbox', { name: '角色名称' }) as HTMLInputElement).value).toBe('仓储岗位'))
}, 10000)

test('invalid role name never sends a write', async () => {
  mount(); await ready()
  await fireEvent.update(screen.getByRole('textbox', { name: '角色名称' }), '')
  await fireEvent.click(screen.getByRole('button', { name: /保\s*存$/ }))
  expect(await screen.findByText('请输入角色名称')).toBeTruthy()
  expect(writes()).toHaveLength(0)
}, 10000)

test('first menu selection grants ordinary management, never export and preserves unrelated historic grant', async () => {
  setup({ menus: [users], permissions: catalog })
  const ui = mount(); await ready()
  await toggleTree(ui.container, '用户管理')
  expect(await screen.findByText('已选 4 项 / 可用 4 项')).toBeTruthy()
  expect((screen.getByRole('radio', { name: '管理' }) as HTMLInputElement).checked).toBe(true)
  expect(chosenCheckbox(/导出用户/).checked).toBe(false)
  await fireEvent.click(screen.getByRole('button', { name: /保\s*存$/ }))
  await screen.findByText('角色及权限已保存')
  expect(writes()).toHaveLength(1)
  expect(writes()[0]).toMatchObject({
    method: 'POST', url: '/roles/1/permissions', headers: { 'If-Match': '"4"' },
    data: {
      menuCodes: ['users'],
      permissionCodes: expect.arrayContaining(['iam:user:view', 'iam:user:create', 'iam:user:update', 'retired:hidden'])
    }
  })
  expect(writes()[0].data.permissionCodes).not.toContain('iam:user:export')
})

test('existing view-only selection is never silently upgraded to management on edit', async () => {
  setup({ menus: [users], permissions: catalog, role: {
    ...baseRole, menuCodes: ['users'], permissionCodes: ['iam:user:view', 'retired:hidden']
  } })
  mount(); await ready()
  expect((screen.getByRole('radio', { name: '只查看' }) as HTMLInputElement).checked).toBe(true)
  expect((screen.getByRole('radio', { name: '管理' }) as HTMLInputElement).checked).toBe(false)
  expect(chosenCheckbox(/导出用户/).checked).toBe(false)
  expect(writes()).toHaveLength(0)
})

test('ordinary view preserves independently selected export while removing only management actions', async () => {
  setup({ menus: [users], permissions: catalog })
  const ui = mount(); await ready()
  await toggleTree(ui.container, '用户管理')
  await fireEvent.click(chosenCheckbox(/导出用户/))
  expect(chosenCheckbox(/导出用户/).checked).toBe(true)
  await fireEvent.click(screen.getByRole('radio', { name: '只查看' }))
  expect((screen.getByRole('radio', { name: '只查看' }) as HTMLInputElement).checked).toBe(true)
  expect(chosenCheckbox(/导出用户/).checked).toBe(true)
  await fireEvent.click(screen.getByRole('button', { name: /保\s*存$/ }))
  await screen.findByText('角色及权限已保存')
  expect(writes()[0].data).toMatchObject({
    menuCodes: ['users'],
    permissionCodes: expect.arrayContaining(['iam:user:view', 'iam:user:export', 'retired:hidden'])
  })
  expect(writes()[0].data.permissionCodes).not.toContain('iam:user:create')
  expect(writes()[0].data.permissionCodes).not.toContain('iam:user:update')
})

test('menu deselection removes exclusive grants but protects shared permissions and historic grants', async () => {
  setup({ menus: [users, another], permissions: catalog })
  const ui = mount(); await ready()
  await toggleTree(ui.container, '用户管理')
  await toggleTree(ui.container, '用户查询')
  await toggleTree(ui.container, '用户管理')
  expect(await screen.findByText('已选 2 项 / 可用 4 项')).toBeTruthy()
  await fireEvent.click(screen.getByRole('button', { name: /保\s*存$/ }))
  await screen.findByText('角色及权限已保存')
  expect(writes()[0].data).toMatchObject({
    menuCodes: ['user-read'],
    permissionCodes: expect.arrayContaining(['iam:user:view', 'retired:hidden'])
  })
  expect(writes()[0].data.permissionCodes).not.toContain('iam:user:create')
  expect(writes()[0].data.permissionCodes).not.toContain('iam:user:update')
  expect(writes()[0].data.permissionCodes).not.toContain('iam:user:export')
})

test('menu required special permission is prompted, not silently granted or discarded', async () => {
  const releaseMenu = {
    ...users, id: '20', menuCode: 'release', menuName: '成品放行', routePath: '/finished/releases',
    permissionCode: 'qa:release', permissionCodes: ['qa:release', 'qa:batch-review'],
    requiredPermissions: 'qa:batch-review'
  }
  const special = [
    permission('qa:release', '31'),
    permission('qa:batch-review', '32')
  ]
  setup({ menus: [releaseMenu], permissions: special, role: {
    ...baseRole, menuCodes: [], permissionCodes: ['retired:hidden']
  } })
  const ui = mount(); await ready()
  await toggleTree(ui.container, '成品放行')
  expect(chosenCheckbox(/成品放行/).checked).toBe(false)
  expect(chosenCheckbox(/QA批审核/).checked).toBe(false)
  await fireEvent.click(screen.getByRole('button', { name: /保\s*存$/ }))
  expect(await screen.findByText(/缺少必要权限/)).toBeTruthy()
  expect(writes()).toHaveLength(0)
  await fireEvent.click(chosenCheckbox(/成品放行/))
  await fireEvent.click(chosenCheckbox(/QA批审核/))
  await fireEvent.click(screen.getByRole('button', { name: /保\s*存$/ }))
  await screen.findByText('角色及权限已保存')
  expect(writes()).toHaveLength(1)
  expect(writes()[0].data.permissionCodes).toEqual(expect.arrayContaining(['qa:release', 'qa:batch-review', 'retired:hidden']))
  expect(writes()[0].data.menuCodes).toEqual(['release'])
}, 20000)

test('role information partial success retains permission draft and uses updated version on retry', async () => {
  setup({ menus: [users], permissions: catalog })
  const ui = mount(); await ready()
  await toggleTree(ui.container, '用户管理')
  await fireEvent.update(screen.getByRole('textbox', { name: '角色名称' }), '新岗位名称')
  let postFailures = 1
  mocks.api.mockImplementation(async (r: any) => {
    if (r.method === 'PUT') return { ...baseRole, roleName: '新岗位名称', version: 5 }
    if (r.method === 'POST' && postFailures-- > 0) throw new Error('first assignment failed')
    return { ...baseRole, roleName: '新岗位名称', version: 6,
      permissionCodes: r.data.permissionCodes, menuCodes: r.data.menuCodes }
  })
  await fireEvent.click(screen.getByRole('button', { name: /保\s*存$/ }))
  expect(await screen.findByText(/角色信息已保存，权限分配未完成/)).toBeTruthy()
  expect((screen.getByRole('radio', { name: '管理' }) as HTMLInputElement).checked).toBe(true)
  await waitFor(() => expect(screen.getByRole('button', { name: /保\s*存$/ }).classList.contains('ant-btn-loading')).toBe(false))
  await fireEvent.click(screen.getByRole('button', { name: /保\s*存$/ }))
  await screen.findByText('角色及权限已保存')
  expect(writes().map(x => x.method)).toEqual(['PUT', 'POST', 'POST'])
  expect(writes()[2].headers['If-Match']).toBe('"5"')
  expect(writes()[2].data.permissionCodes).toEqual(expect.arrayContaining(['iam:user:create', 'iam:user:update', 'retired:hidden']))
})

test('draft edit is guarded by unsaved changes confirmation', async () => {
  mount(); await ready()
  await fireEvent.update(screen.getByRole('textbox', { name: '角色名称' }), '未保存名称')
  const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false)
  expect(mocks.leave.mock.calls[0]![0]()).toBe(false)
  expect(confirm).toHaveBeenCalled()
  confirm.mockRestore()
})

test('no second visibility checkbox or obsolete CRUD label on approved T2 tree', async () => {
  setup({ menus: [users], permissions: catalog })
  const ui = mount(); await ready()
  expect(ui.container.querySelector('main[data-ui-template="T2"]')).toBeTruthy()
  expect(ui.container.querySelector('.role-permission-table-head')?.textContent).toContain('菜单名称')
  expect(ui.container.querySelector('.role-permission-table-head')?.textContent).toContain('功能权限')
  expect(ui.container.querySelector('.role-permission-table-head')?.textContent).toContain('操作说明')
  expect(ui.container.querySelectorAll('.ant-tree-checkbox').length).toBe(1)
  expect(ui.container.querySelectorAll('.role-special-section').length).toBe(1)
  expect(ui.container.querySelector('.role-tree-scroll')).toBeTruthy()
  expect(screen.getByRole('button', { name: '保存并返回' })).toBeTruthy()
  expect(screen.queryByText('增删改查')).toBeNull()
  expect(screen.getByText(/独立授权，不随普通管理自动勾选/)).toBeTruthy()
  expect(ui.container.querySelectorAll('.role-information-form .ant-form-item').length).toBe(3)
})
