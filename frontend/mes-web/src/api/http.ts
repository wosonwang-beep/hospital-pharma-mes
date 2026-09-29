import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'

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

export interface Page<T> { items: T[]; total: number; page: number; size: number }
export interface Identity { userId: string; organizationId: string; loginName: string; displayName: string; roleCodes: string[]; permissionCodes: string[]; mustChangePassword: boolean }
export interface User { id: string; username: string; displayName: string; status: 'ACTIVE' | 'INACTIVE'; version: number; roleIds: string[] }
export interface Role { id: string; roleCode: string; roleName: string; status: 'ACTIVE' | 'INACTIVE'; version: number; permissionCodes: string[]; menuCodes: string[] }
export interface Permission { id: string; permissionCode: string; permissionName: string; permissionType: 'MENU' | 'ACTION'; routePath?: string; status: string; version: number }

export const http = axios.create({ baseURL: '/api/v1', withCredentials: true })
let accessToken: string | null = null
let renewal: Promise<string> | null = null
let sessionLost: (() => void) | null = null

export function setAccessToken(value: string | null) { accessToken = value }
export function setSessionLostHandler(value: () => void) { sessionLost = value }

http.interceptors.request.use(config => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`
  return config
})

async function refreshToken() {
  renewal ??= axios.post<ApiResponse<{ accessToken: string }>>('/api/v1/auth/refresh', null, { withCredentials: true })
    .then(response => {
      accessToken = response.data.data.accessToken
      return accessToken
    }).finally(() => { renewal = null })
  return renewal
}

http.interceptors.response.use(response => response, async (error: AxiosError) => {
  const original = error.config as (AxiosRequestConfig & { _retried?: boolean }) | undefined
  const authEndpoint = original?.url?.startsWith('/auth/login') || original?.url?.startsWith('/auth/refresh')
  if (error.response?.status === 401 && original && !original._retried && !authEndpoint) {
    original._retried = true
    try {
      const token = await refreshToken()
      original.headers = { ...original.headers, Authorization: `Bearer ${token}` }
      return http(original)
    } catch {
      setAccessToken(null)
      sessionLost?.()
    }
  }
  return Promise.reject(error)
})

export async function api<T>(config: AxiosRequestConfig): Promise<T> {
  return (await http.request<ApiResponse<T>>(config)).data.data
}

export function idempotencyKey() { return crypto.randomUUID() }

export function errorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    if (error.response?.status === 403) return '没有操作权限'
    return (error.response?.data as Partial<ApiErrorBody> | undefined)?.message ?? '请求失败，请稍后重试'
  }
  return '请求失败，请稍后重试'
}
