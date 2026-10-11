import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'
import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { createPinia, setActivePinia } from 'pinia'

describe('DCP005 approved authentication renewal contract, isolated transport', () => {
  beforeEach(() => { vi.resetModules(); setActivePinia(createPinia()) })
  afterEach(() => vi.restoreAllMocks())
  const identity = { userId: '1', organizationId: '1', loginName: 'operator', displayName: '操作员', roleCodes: [], permissionCodes: ['print:source:view'], mustChangePassword: false }
  function denied(config: InternalAxiosRequestConfig) {
    return new AxiosError('Unauthorized', 'ERR_BAD_REQUEST', config, {}, { status: 401, statusText: 'Unauthorized', headers: {}, config, data: { code: 'UNAUTHENTICATED' } })
  }
  it('auth/me 401 renews once, retries once successfully and retains identity', async () => {
    const { http } = await import('../../api/http'); const { useAuthStore } = await import('../../stores/auth')
    const renewal = vi.spyOn(axios, 'post').mockResolvedValue({ data: { data: { accessToken: 'isolated-token' } } })
    const calls: InternalAxiosRequestConfig[] = []
    http.defaults.adapter = async config => { calls.push(config); if (calls.length === 1) throw denied(config); return { status: 200, statusText: 'OK', config, headers: {}, data: { data: identity } } }
    const auth = useAuthStore(); await auth.load()
    expect(renewal).toHaveBeenCalledTimes(1); expect(calls).toHaveLength(2)
    expect(calls[1]!.headers.Authorization).toBe('Bearer isolated-token'); expect(auth.identity).toEqual(identity)
  })
  it('failed renewal clears identity and rejects the protected request', async () => {
    const { http, setSessionLostHandler } = await import('../../api/http'); const lost = vi.fn(); setSessionLostHandler(lost)
    vi.spyOn(axios, 'post').mockRejectedValue(new Error('Renewal expired'))
    let calls = 0; http.defaults.adapter = async config => { calls++; throw denied(config) }
    await expect(http.get('/auth/me')).rejects.toMatchObject({ response: { status: 401 } })
    expect(lost).toHaveBeenCalledTimes(1); expect(calls).toBe(1)
  })
  it('unexpected business 401 remaining after retry is surfaced, without a renewal loop', async () => {
    const { http } = await import('../../api/http')
    const renewal = vi.spyOn(axios, 'post').mockResolvedValue({ data: { data: { accessToken: 'isolated-token' } } })
    let calls = 0; http.defaults.adapter = async config => { calls++; throw denied(config) }
    await expect(http.get('/printing/data-sources')).rejects.toMatchObject({ response: { status: 401 } })
    expect(renewal).toHaveBeenCalledTimes(1); expect(calls).toBe(2)
  })
  it('simultaneous initial 401 requests share one renewal and each retry once', async () => {
    const { http } = await import('../../api/http')
    let release!: (value: unknown) => void
    const renewal = vi.spyOn(axios, 'post').mockImplementation(() => new Promise(resolve => { release = resolve }))
    const counts = new Map<string, number>()
    http.defaults.adapter = async config => { const url = config.url!; const count = (counts.get(url) ?? 0) + 1; counts.set(url, count); if (count === 1) throw denied(config); return { status: 200, statusText: 'OK', config, headers: {}, data: {} } }
    const requests = Promise.all([http.get('/auth/me'), http.get('/auth/navigation')])
    await vi.waitFor(() => expect(renewal).toHaveBeenCalledTimes(1)); release({ data: { data: { accessToken: 'isolated-token' } } }); await requests
    expect([...counts.values()]).toEqual([2, 2]); expect(renewal).toHaveBeenCalledTimes(1)
  })
  it('login 401 remains a failure and does not start automatic renewal', async () => {
    const { http } = await import('../../api/http'); const renewal = vi.spyOn(axios, 'post')
    http.defaults.adapter = async config => { throw denied(config) }
    await expect(http.post('/auth/login', {})).rejects.toMatchObject({ response: { status: 401 } }); expect(renewal).not.toHaveBeenCalled()
  })
})
