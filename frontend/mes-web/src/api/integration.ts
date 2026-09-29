import { http, type ApiResponse } from './http'
import type { Page } from '../types/audit'
import type { IntegrationMessage, IntegrationMessageQuery, RetryIntegrationMessageCommand } from '../types/integration'

export async function queryIntegrationMessages(query: IntegrationMessageQuery = {}): Promise<Page<IntegrationMessage>> {
  const response = await http.get<ApiResponse<Page<IntegrationMessage>>>('/integration/messages', {
    params: query,
    paramsSerializer: { indexes: null }
  })
  return response.data.data
}

export async function retryIntegrationMessage(command: RetryIntegrationMessageCommand): Promise<IntegrationMessage> {
  const response = await http.post<ApiResponse<IntegrationMessage>>(
    `/integration/messages/${encodeURIComponent(command.messageRef)}/retry`,
    { reason: command.reason },
    { headers: { 'Idempotency-Key': command.idempotencyKey, 'If-Match': String(command.versionNo) } }
  )
  return response.data.data
}
