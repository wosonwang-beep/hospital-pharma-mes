import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api, setAccessToken, setSessionLostHandler, type Identity } from '../api/http'
import { usePlatformAuthContext } from '../auth/PlatformAuthContext'

export const useAuthStore = defineStore('auth', () => {
  const identity = ref<Identity | null>(null)
  let checked = false

  function synchronize(value: Identity | null) {
    identity.value = value
    usePlatformAuthContext().setSnapshot(value ? {
      userId: value.userId, organizationId: value.organizationId, permissions: value.permissionCodes
    } : null)
  }
  function clear() { setAccessToken(null); synchronize(null); checked = true }
  setSessionLostHandler(clear)

  async function load() {
    if (checked) return identity.value
    try { synchronize(await api<Identity>({ url: '/auth/me' })) }
    catch { clear() }
    checked = true
    return identity.value
  }

  async function login(loginName: string, password: string) {
    const token = await api<{ accessToken: string }>({ method: 'POST', url: '/auth/login', data: { loginName, password } })
    setAccessToken(token.accessToken)
    synchronize(await api<Identity>({ url: '/auth/me' }))
    checked = true
    return identity.value
  }

  async function logout() {
    try { await api<void>({ method: 'POST', url: '/auth/logout' }) }
    finally { clear() }
  }

  function can(permission: string) { return identity.value?.permissionCodes.includes(permission) ?? false }
  return { identity, load, login, logout, clear, can }
})
