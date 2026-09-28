import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api, setAccessToken, type Identity } from '../api/client'

export const useAuthStore = defineStore('auth', () => {
  const identity = ref<Identity | null>(null)
  let checked = false

  function clear() {
    identity.value = null
    setAccessToken(null)
    checked = true
  }

  async function load() {
    if (checked) return identity.value
    try {
      identity.value = await api<Identity>({ url: '/api/v1/auth/me' })
    } catch {
      clear()
    }
    checked = true
    return identity.value
  }

  async function login(loginName: string, password: string) {
    const token = await api<{ accessToken: string }>({ method: 'POST', url: '/api/v1/auth/login', data: { loginName, password } })
    setAccessToken(token.accessToken)
    identity.value = await api<Identity>({ url: '/api/v1/auth/me' })
    checked = true
    return identity.value
  }

  async function logout() {
    try { await api<void>({ method: 'POST', url: '/api/v1/auth/logout' }) }
    finally { clear() }
  }

  return { identity, clear, load, login, logout }
})
