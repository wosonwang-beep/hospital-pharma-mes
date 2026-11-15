import { beforeEach, expect, test, vi } from 'vitest'
import { http } from './http'
import { queryIntegrationMessages, retryIntegrationMessage } from './integration'

beforeEach(() => vi.restoreAllMocks())

test('serializes repeated integration statuses according to OpenAPI form/explode', async () => {
  const data = { items: [], page: 0, size: 1, total: 0 }
  const get = vi.spyOn(http, 'get').mockResolvedValue({ data: { code: 'OK', message: 'OK', data, traceId: 't' } })

  await queryIntegrationMessages({ status: ['PENDING', 'RETRY_WAIT'], size: 1 })
  expect(get).toHaveBeenCalledWith('/integration/messages', {
    params: { status: ['PENDING', 'RETRY_WAIT'], size: 1 },
    paramsSerializer: { indexes: null }
  })
})

test('sends retry reason with mandatory idempotency and version headers', async () => {
  const message = { messageRef: 'OUTBOX:7' }
  const post = vi.spyOn(http, 'post').mockResolvedValue({ data: { code: 'OK', message: 'OK', data: message, traceId: 't' } })

  await retryIntegrationMessage({ messageRef: 'OUTBOX:7', reason: 'operator approved', versionNo: 4, idempotencyKey: 'idem-1' })
  expect(post).toHaveBeenCalledWith('/integration/messages/OUTBOX%3A7/retry', { reason: 'operator approved' }, {
    headers: { 'Idempotency-Key': 'idem-1', 'If-Match': '"4"' }
  })
})
