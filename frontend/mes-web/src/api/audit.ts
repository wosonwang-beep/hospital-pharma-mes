import { http, type ApiResponse } from './http'
import type { AuditEvent, AuditEventQuery, Page } from '../types/audit'

export async function queryAuditEvents(query: AuditEventQuery = {}): Promise<Page<AuditEvent>> {
  const response = await http.get<ApiResponse<Page<AuditEvent>>>('/audit-events', { params: query })
  return response.data.data
}
