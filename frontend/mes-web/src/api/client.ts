import axios, { AxiosError, type AxiosRequestConfig } from 'axios'

export interface ApiEnvelope<T> { code: string; message: string; data: T; traceId: string }
export interface Page<T> { items: T[]; total: number; page: number; size: number }
export interface Identity { userId: number; loginName: string; displayName: string; roleCodes: string[]; permissionCodes: string[]; mustChangePassword: boolean }
export interface User { id: number; loginName: string; displayName: string; enabled: boolean; version: number; roleIds: number[] }
export interface Role { id: number; roleCode: string; displayName: string; enabled: boolean; version: number; permissionCodes: string[] }
export interface Permission { id: number; permissionCode: string; permissionType: string; module: string; displayName: string; enabled: boolean }

const http = axios.create({ withCredentials: true })
let accessToken: string | null = null
let renewal: Promise<string> | null = null
let onSessionLost: (() => void) | null = null

export function setAccessToken(token: string | null) { accessToken = token }
export function setSessionLostHandler(handler: () => void) { onSessionLost = handler }

async function refreshToken(): Promise<string> {
  if (!renewal) {
    renewal = axios.post<ApiEnvelope<{ accessToken: string }>>('/api/v1/auth/refresh', null, { withCredentials: true })
      .then(response => {
        accessToken = response.data.data.accessToken
        return accessToken
      }).finally(() => { renewal = null })
  }
  return renewal
}

http.interceptors.request.use(config => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`
  return config
})

http.interceptors.response.use(response => response, async (error: AxiosError) => {
  const original = error.config as (AxiosRequestConfig & { _retried?: boolean }) | undefined
  if (error.response?.status === 401 && original && !original._retried && !original.url?.startsWith('/api/v1/auth/login')) {
    original._retried = true
    try {
      const token = await refreshToken()
      original.headers = { ...original.headers, Authorization: `Bearer ${token}` } as typeof original.headers
      return http(original)
    } catch {
      accessToken = null
      onSessionLost?.()
    }
  }
  return Promise.reject(error)
})

export async function api<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await http.request<ApiEnvelope<T>>(config)
  return response.data.data
}

export function errorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (error.response?.status === 403) return '没有操作权限'
    const body = error.response?.data as Partial<ApiEnvelope<unknown>> | undefined
    return body?.message || '请求失败，请稍后重试'
  }
  return '请求失败，请稍后重试'
}
