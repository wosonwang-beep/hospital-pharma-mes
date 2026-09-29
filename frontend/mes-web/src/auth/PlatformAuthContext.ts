import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export interface PlatformAuthSnapshot {
  userId: string
  organizationId: string
  permissions: string[]
}

/** Compatibility adapter for MES-001 views; MES-002 populates it only from the authenticated identity. */
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
