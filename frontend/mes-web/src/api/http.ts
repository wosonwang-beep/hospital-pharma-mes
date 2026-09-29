import axios from 'axios'

export interface ApiResponse<T> {
  code: string
  message: string
  data: T
  traceId: string
}

export interface ApiErrorBody {
  requestId: string
  code: string
  message: string
  fieldErrors: Array<Record<string, unknown>>
  allowedActions: string[]
}

export const http = axios.create({ baseURL: '/api/v1' })
