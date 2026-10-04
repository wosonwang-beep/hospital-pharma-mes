import { fireEvent, render, screen, waitFor } from '@testing-library/vue'
import { createPinia, setActivePinia, type Pinia } from 'pinia'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import AuditTrailView from '../audit/AuditTrailView.vue'
import IntegrationOperationsView from '../integration/IntegrationOperationsView.vue'
import { usePlatformAuthContext } from '../../auth/PlatformAuthContext'
import * as auditApi from '../../api/audit'
import * as integrationApi from '../../api/integration'
import type { AuditEvent, Page } from '../../types/audit'

vi.mock('../../api/audit')
vi.mock('../../api/integration')
const auditRoute = vi.hoisted(() => ({ query: {} as Record<string, string> }))
vi.mock('vue-router', () => ({ useRoute: () => auditRoute }))

const page = <T,>(items: T[], total = items.length) => ({ items, page: 0, size: 50, total })

let pinia: Pinia

beforeEach(() => {
  pinia = createPinia()
  setActivePinia(pinia)
  vi.resetAllMocks()
  auditRoute.query = {}
})

describe('GMP Audit Trail', () => {
  test('master detail audit link scopes queries by object type and ID', async () => {
    usePlatformAuthContext().setPermissions(['audit:view'])
    auditRoute.query = { objectType: 'Equipment', objectId: '21' }
    vi.mocked(auditApi.queryAuditEvents).mockResolvedValue(page([]))
    render(AuditTrailView, { global: { plugins: [pinia] } })
    await screen.findByText('暂无审计事件')
    expect(auditApi.queryAuditEvents).toHaveBeenCalledWith({ objectType: 'Equipment', objectId: '21', page: 0, size: 50 })
  })
  test('shows 403 without audit:view and never queries the API', async () => {
    render(AuditTrailView, { global: { plugins: [pinia] } })

    expect(await screen.findByText('无权查看 GMP Audit Trail')).toBeTruthy()
    expect(auditApi.queryAuditEvents).not.toHaveBeenCalled()
  })

  test('renders API-backed total and the frozen audit columns', async () => {
    usePlatformAuthContext().setPermissions(['audit:view'])
    vi.mocked(auditApi.queryAuditEvents)
      .mockResolvedValueOnce(page([], 12))
      .mockResolvedValueOnce(page([{
        id: '9', orgId: '1', actorId: '23', actorRole: 'QA', action: 'SIGN', objectType: 'EBR',
        objectId: '88', oldValueDigest: null, newValueDigest: null, reason: null, clientInfo: null,
        occurredAt: '2026-09-29T01:02:03Z', transactionId: 'tx-1', requestId: 'req-1', source: 'API'
      }]))

    render(AuditTrailView, { global: { plugins: [pinia] } })

    expect(await screen.findByText('12')).toBeTruthy()
    expect(await screen.findByText('23 / QA')).toBeTruthy()
    expect(screen.getByText('Transaction / Request')).toBeTruthy()
    expect(screen.getByText('tx-1 / req-1')).toBeTruthy()
  })

  test('supports empty and error states', async () => {
    usePlatformAuthContext().setPermissions(['audit:view'])
    vi.mocked(auditApi.queryAuditEvents).mockRejectedValue(new Error('offline'))

    render(AuditTrailView, { global: { plugins: [pinia] } })
    expect(await screen.findByText('审计事件加载失败')).toBeTruthy()
  })

  test('shows loading followed by the empty state', async () => {
    usePlatformAuthContext().setPermissions(['audit:view'])
    let resolvePage!: (value: Page<AuditEvent>) => void
    const pending = new Promise<Page<AuditEvent>>(resolve => { resolvePage = resolve })
    vi.mocked(auditApi.queryAuditEvents).mockReturnValue(pending)

    render(AuditTrailView, { global: { plugins: [pinia] } })
    expect(await screen.findByText('正在加载审计事件…')).toBeTruthy()
    resolvePage(page([]))
    expect(await screen.findByText('暂无审计事件')).toBeTruthy()
  })
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
