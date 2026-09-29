import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export interface PlatformAuthSnapshot {
  userId: string
  organizationId: string
  permissions: string[]
}

declare global {
  interface Window {
    __MES_PLATFORM_AUTH_CONTEXT__?: PlatformAuthSnapshot
  }
}

export function injectedPlatformAuthSnapshot(): PlatformAuthSnapshot | null {
  const value = window.__MES_PLATFORM_AUTH_CONTEXT__
  if (!value || !Array.isArray(value.permissions)) return null
  return { userId: value.userId, organizationId: value.organizationId, permissions: [...value.permissions] }
}

/** MES-001 boundary only. MES-002 owns populating this context from the real login lifecycle. */
export const usePlatformAuthContext = defineStore('platform-authorization', () => {
  const snapshot = ref<PlatformAuthSnapshot | null>(null)
  const permissions = computed(() => new Set(snapshot.value?.permissions ?? []))

  function setSnapshot(value: PlatformAuthSnapshot | null) {
    snapshot.value = value ? { ...value, permissions: [...value.permissions] } : null
  }

  function setPermissions(value: string[]) {
    setSnapshot({ userId: '', organizationId: '', permissions: value })
  }

  function can(permission: string) {
    return permissions.value.has(permission)
  }

  return { snapshot, setSnapshot, setPermissions, can }
})
