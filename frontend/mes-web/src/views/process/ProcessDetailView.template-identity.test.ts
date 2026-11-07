import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen, within, waitFor } from '@testing-library/vue'
import Antd from 'ant-design-vue'
import ProcessDetailView from './ProcessDetailView.vue'

const { api, push } = vi.hoisted(() => ({ api: vi.fn(), push: vi.fn() }))
vi.mock('../../api/http', () => ({ api, idempotencyKey: () => 'key', errorMessage: () => '请求失败' }))
vi.mock('../../stores/auth', () => ({ useAuthStore: () => ({ can: (permission: string) => ['ebr:template:view', 'ebr:template:update'].includes(permission) }) }))
vi.mock('vue-router', () => ({
  useRoute: () => ({ meta: {}, params: { id: '1' }, query: {}, fullPath: '/process/packages/1' }),
  useRouter: () => ({ push }),
  onBeforeRouteLeave: vi.fn()
}))
afterEach(() => { cleanup(); vi.clearAllMocks() })

describe('工序表单模板来源', () => {
  const process = { id: '1', packageCode: 'KCL30', status: 'ACTIVE', currentDefinition: { id: '9', route: { routeCode: 'R1', operations: [{ operationCode: 'OP01', operationName: '配制', sequenceNo: 1, parameters: [], ipcDefinitions: [] }] } } }
  it('读取中不把未知的表单数量说成零或未配置，成功空响应后才显示空态', async () => {
    let resolveRead!: (value: unknown) => void
    const pending = new Promise(resolve => { resolveRead = resolve })
    api.mockImplementation(({ url }: { url: string }) => url === '/process-packages/1' ? Promise.resolve(process) : pending)
    render(ProcessDetailView, { global: { plugins: [Antd], stubs: { ProcessLookup: true } } })
    await fireEvent.click(await screen.findByRole('tab', { name: '工序表单' }))
    await screen.findByText('正在加载工序表单…')
    expect(screen.queryByText('0 份表单')).toBeNull()
    expect(screen.queryByText('该工序尚未配置表单')).toBeNull()
    resolveRead({ items: [], total: 0, page: 0, size: 100 })
    expect(await screen.findByText('该工序尚未配置表单')).toBeTruthy()
    expect(screen.getByText('0 份表单')).toBeTruthy()
  })
  it('读取失败不声称未配置，重试实际读取成功后恢复真实表单行', async () => {
    let failRead = true
    api.mockImplementation(async ({ url }: { url: string }) => {
      if (url === '/process-packages/1') return process
      if (url === '/ebr/templates') {
        if (failRead) throw new Error('工序表单读取失败')
        return { items: [{ id: '496' }], total: 1, page: 0, size: 100 }
      }
      return { id: '496', templateName: '配制批记录', templateCode: 'EBR-KCL30-PREPARE', status: 'DRAFT', version: 1, definition: { forms: [{ formCode: 'OP01_RECORD', formName: '配制记录', operationDefId: '12', fields: [] }] }, operationChoices: [{ id: '12', operationCode: 'OP01' }] }
    })
    render(ProcessDetailView, { global: { plugins: [Antd], stubs: { ProcessLookup: true } } })
    await fireEvent.click(await screen.findByRole('tab', { name: '工序表单' }))
    const retry = await screen.findByRole('button', { name: '重试加载工序表单' })
    expect(screen.queryByText('该工序尚未配置表单')).toBeNull()
    expect(screen.queryByText('0 份表单')).toBeNull()
    failRead = false
    await fireEvent.click(retry)
    expect(await screen.findByText('EBR-KCL30-PREPARE')).toBeTruthy()
    await waitFor(() => expect(screen.queryByRole('button', { name: '重试加载工序表单' })).toBeNull())
    expect(screen.getByText('1 份表单')).toBeTruthy()
  })
  it('区分同码的模板、显示历史空名，并按各行进入对应设计或查看入口', async () => {
    const operation = { operationCode: 'OP01', operationName: '配制', sequenceNo: 1, parameters: [], ipcDefinitions: [] }
    const form = { formCode: 'OP01_RECORD', formName: '配制记录', operationDefId: '12', fields: [] }
    const template = (id: string, templateName: string | null, templateCode: string, status: string) => ({
      id, templateName, templateCode, status, version: 1,
      definition: { forms: [form] }, operationChoices: [{ id: '12', operationCode: 'OP01' }]
    })
    api.mockImplementation(async ({ url }: { url: string }) => {
      if (url === '/process-packages/1') return { id: '1', packageCode: 'KCL30', status: 'ACTIVE', currentDefinition: { id: '9', route: { routeCode: 'R1', operations: [operation] } } }
      if (url === '/ebr/templates') return { items: [{ id: '291' }, { id: '496' }], total: 2, page: 0, size: 100 }
      if (url === '/ebr/templates/291') return template('291', '配制批记录', 'EBR-A', 'DRAFT')
      if (url === '/ebr/templates/496') return template('496', null, 'EBR-B', 'EFFECTIVE')
      throw new Error(`Unexpected request: ${url}`)
    })
    const view = render(ProcessDetailView, { global: { plugins: [Antd], stubs: { ProcessLookup: true } } })
    await fireEvent.click(await screen.findByRole('tab', { name: '工序表单' }))
    const first = (await screen.findByText('EBR-A')).closest('tr')!
    const second = (await screen.findByText('EBR-B')).closest('tr')!
    expect(within(first).getByText('配制批记录')).toBeTruthy()
    expect(within(second).getByText('未命名（历史模板）')).toBeTruthy()
    expect(first.getAttribute('data-row-key')).not.toBe(second.getAttribute('data-row-key'))
    await fireEvent.click(within(first).getByRole('button', { name: '设计表单' }))
    expect(push).toHaveBeenLastCalledWith({ path: '/ebr/templates/291/designer', query: { processPackageId: '1', formCode: 'OP01_RECORD', designerTab: 'form', returnTo: '/process/packages/1' } })
    await fireEvent.click(within(first).getByRole('button', { name: '定义CPP规则' }))
    expect(push).toHaveBeenLastCalledWith({ path: '/ebr/templates/291/designer', query: { processPackageId: '1', formCode: 'OP01_RECORD', designerTab: 'cpp', returnTo: '/process/packages/1' } })
    await fireEvent.click(within(second).getByRole('button', { name: '查看表单' }))
    expect(push).toHaveBeenLastCalledWith({ path: '/ebr/templates/496/designer', query: { processPackageId: '1', formCode: 'OP01_RECORD', designerTab: 'form', returnTo: '/process/packages/1' } })
    view.unmount()
  })
})
