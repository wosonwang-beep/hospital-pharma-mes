import { fireEvent, render, screen, waitFor } from '@testing-library/vue'
import { createPinia, setActivePinia, type Pinia } from 'pinia'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import IntegrationOperationsView from '../integration/IntegrationOperationsView.vue'
import { usePlatformAuthContext } from '../../auth/PlatformAuthContext'
import * as integrationApi from '../../api/integration'

vi.mock('../../api/integration')
const page = <T,>(items: T[], total = items.length) => ({ items, page: 0, size: 50, total })

let pinia: Pinia
beforeEach(() => {
  pinia = createPinia()
  setActivePinia(pinia)
  vi.resetAllMocks()
})
describe('Integration Inbox / Outbox Operations', () => {
  test('requires integration:view', async () => {
    render(IntegrationOperationsView, { global: { plugins: [pinia] } })

    expect(await screen.findByText('无权查看 Integration Operations')).toBeTruthy()
    expect(integrationApi.queryIntegrationMessages).not.toHaveBeenCalled()
  })

  test('shows only contract fields and hides retry without integration:retry', async () => {
    usePlatformAuthContext().setPermissions(['integration:view'])
    vi.mocked(integrationApi.queryIntegrationMessages).mockResolvedValue(page([{
      messageRef: 'OUTBOX:7', direction: 'OUTBOX', messageId: 'm-7', sourceSystem: null,
      targetSystem: 'LIMS', eventType: 'RESULT_READY', aggregateType: 'BATCH', aggregateId: '42',
      status: 'DEAD_LETTER', retryCount: 8, nextRetryAt: null, lastErrorCode: 'TIMEOUT',
      lastErrorMessage: 'Timed out', occurredAt: '2026-09-29T01:02:03Z', completedAt: null, versionNo: 4
    }]))

    render(IntegrationOperationsView, { global: { plugins: [pinia] } })

    expect(await screen.findByText('OUTBOX:7')).toBeTruthy()
    expect(screen.queryByRole('button', { name: '人工重试 OUTBOX:7' })).toBeNull()
    expect(screen.queryByText(/commandBody/i)).toBeNull()
  })

  test('submits reason, version and idempotency key for manual retry', async () => {
    usePlatformAuthContext().setPermissions(['integration:view', 'integration:retry'])
    const message = {
      messageRef: 'OUTBOX:7', direction: 'OUTBOX' as const, messageId: 'm-7', sourceSystem: null,
      targetSystem: 'LIMS', eventType: 'RESULT_READY', aggregateType: 'BATCH', aggregateId: '42',
      status: 'DEAD_LETTER' as const, retryCount: 8, nextRetryAt: null, lastErrorCode: 'TIMEOUT',
      lastErrorMessage: 'Timed out', occurredAt: '2026-09-29T01:02:03Z', completedAt: null, versionNo: 4
    }
    vi.mocked(integrationApi.queryIntegrationMessages).mockResolvedValue(page([message]))
    vi.mocked(integrationApi.retryIntegrationMessage).mockResolvedValue({ ...message, status: 'RETRY_WAIT', versionNo: 5 })

    render(IntegrationOperationsView, { global: { plugins: [pinia] } })
    await fireEvent.click(await screen.findByRole('button', { name: '人工重试 OUTBOX:7' }))
    await fireEvent.update(screen.getByLabelText('重试原因'), '经运维确认后重试')
    await fireEvent.click(screen.getByRole('button', { name: '确认重试' }))

    await waitFor(() => expect(integrationApi.retryIntegrationMessage).toHaveBeenCalledTimes(1))
    const submitted = vi.mocked(integrationApi.retryIntegrationMessage).mock.calls[0]?.[0]
    expect(submitted).toMatchObject({
      messageRef: 'OUTBOX:7', reason: '经运维确认后重试', versionNo: 4
    })
    expect(submitted?.idempotencyKey).toBeTruthy()
  })

  test('inbox direction and status filter are submitted without new actions',async()=>{
    usePlatformAuthContext().setPermissions(['integration:view'])
    vi.mocked(integrationApi.queryIntegrationMessages).mockResolvedValue(page([]))
    render(IntegrationOperationsView,{global:{plugins:[pinia]}})
    await screen.findByText('暂无集成消息')
    await fireEvent.click(screen.getByRole('button',{name:'Inbox 收件箱'}))
    await fireEvent.update(screen.getByLabelText('状态'),'PROCESSED')
    await fireEvent.click(screen.getByRole('button',{name:'查询'}))
    await waitFor(()=>expect(integrationApi.queryIntegrationMessages).toHaveBeenCalledWith({direction:'INBOX',status:['PROCESSED'],page:0,size:50}))
  })
  test('supports the integration empty and error states', async () => {
    usePlatformAuthContext().setPermissions(['integration:view'])
    vi.mocked(integrationApi.queryIntegrationMessages).mockResolvedValue(page([]))
    const view = render(IntegrationOperationsView, { global: { plugins: [pinia] } })
    expect(await screen.findByText('暂无集成消息')).toBeTruthy()

    view.unmount()
    vi.mocked(integrationApi.queryIntegrationMessages).mockRejectedValue(new Error('offline'))
    render(IntegrationOperationsView, { global: { plugins: [pinia] } })
    expect(await screen.findByText('集成消息加载失败')).toBeTruthy()
  })
})
