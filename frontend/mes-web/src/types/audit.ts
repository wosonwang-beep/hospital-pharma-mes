export type AuditSource = 'API' | 'SCHEDULER' | 'INTEGRATION' | 'SYSTEM'

export interface AuditEvent {
  id: string
  orgId: string
  actorId: string
  actorRole: string | null
  action: string
  objectType: string
  objectId: string
  oldValueDigest: string | null
  newValueDigest: string | null
  reason: string | null
  clientInfo: string | null
  occurredAt: string
  transactionId: string
  requestId: string | null
  source: AuditSource
}

export interface AuditEventQuery {
  actorId?: string
  action?: string
  objectType?: string
  objectId?: string
  source?: AuditSource
  transactionId?: string
  requestId?: string
  occurredFrom?: string
  occurredTo?: string
  page?: number
  size?: number
}

export interface Page<T> {
  items: T[]
  page: number
  size: number
  total: number
}

