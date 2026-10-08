import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'
import { api, setAccessToken, setSessionLostHandler, type Identity } from '../api/http'
import { usePlatformAuthContext } from '../auth/PlatformAuthContext'
import { clearReferenceCache } from '../views/master/referenceCache'

export const useAuthStore = defineStore('auth', () => {
  const identity = ref<Identity | null>(null)
  let checked = false

  function synchronize(value: Identity | null) {
    const previous = identity.value
    if(!value||!previous||value.userId!==previous.userId||value.organizationId!==previous.organizationId)clearReferenceCache()
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
    clearReferenceCache()
    const token = await api<{ accessToken: string }>({ method: 'POST', url: '/auth/login', data: { loginName, password } })
    setAccessToken(token.accessToken)
    synchronize(await api<Identity>({ url: '/auth/me' }))
    checked = true
    return identity.value
  }

  async function logout() {
    try { await api<void>({ method: 'POST', url: '/auth/logout' }) }
    catch (cause) { if (!axios.isAxiosError(cause) || cause.response?.status !== 401) throw cause }
    finally { clear() }
  }

  function can(permission: string) { return identity.value?.permissionCodes.includes(permission) ?? false }
  return { identity, load, login, logout, clear, can }
})
