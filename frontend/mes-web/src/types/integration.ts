export type IntegrationDirection = 'INBOX' | 'OUTBOX'
export type IntegrationMessageStatus =
  | 'RECEIVED' | 'PROCESSING' | 'RETRY_WAIT' | 'PROCESSED'
  | 'PENDING' | 'DISPATCHING' | 'PUBLISHED' | 'DEAD_LETTER'

export interface IntegrationMessage {
  messageRef: string
  direction: IntegrationDirection
  messageId: string
  sourceSystem: string | null
  targetSystem: string | null
  eventType: string | null
  aggregateType: string | null
  aggregateId: string | null
  status: IntegrationMessageStatus
  retryCount: number
  nextRetryAt: string | null
  lastErrorCode: string | null
  lastErrorMessage: string | null
  occurredAt: string
  completedAt: string | null
  versionNo: number
}

export interface IntegrationMessageQuery {
  direction?: IntegrationDirection
  system?: string
  messageId?: string
  eventType?: string
  aggregateType?: string
  aggregateId?: string
  status?: IntegrationMessageStatus[]
  occurredFrom?: string
  occurredTo?: string
  page?: number
  size?: number
}

export interface RetryIntegrationMessageCommand {
  messageRef: string
  reason: string
  versionNo: number
  idempotencyKey: string
}
