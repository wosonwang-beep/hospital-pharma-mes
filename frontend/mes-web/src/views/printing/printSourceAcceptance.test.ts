import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'
import { createApp, defineComponent, h } from 'vue'
import { RouterView } from 'vue-router'
import Antd from 'ant-design-vue'

const probe = vi.hoisted(() => ({ rights: new Set<string>(), identity: true, pending: null as Promise<void> | null, api: vi.fn() }))
vi.mock('../../stores/auth', () => ({ useAuthStore: () => ({
  get identity() { return probe.identity ? { mustChangePassword: false } : null },
  can: (right: string) => probe.rights.has(right), load: async () => { await probe.pending }
}) }))
vi.mock('../../api/http', () => ({ api: probe.api, errorMessage: () => 'request failed', idempotencyKey: () => 'isolated-contract' }))
vi.mock('../../layouts/AppLayout.vue', () => ({ default: defineComponent({ setup: () => () => h(RouterView) }) }))
vi.mock('../dashboard/DashboardView.vue', () => ({ default: defineComponent({ setup: () => () => h('div', 'Home') }) }))

describe('DCP005 approved pre-mount route denial; actual router and source component', () => {
  let app: ReturnType<typeof createApp> | undefined
  let root: HTMLElement
  let mounts = 0
  beforeEach(() => {
    vi.resetModules(); probe.rights.clear(); probe.identity = true; probe.pending = null; probe.api.mockReset(); mounts = 0
    history.replaceState({}, '', '/'); root = document.createElement('div'); document.body.append(root)
    probe.api.mockImplementation(async ({ url }: { url: string }) => {
      if (url.endsWith('/catalogs')) return { entities: [] }
      if (url.endsWith('/authorization-policies') || url.endsWith('/document-types')) return []
      if (url.endsWith('/data-sources')) return { items: [], total: 0 }
      return { id: '21', sourceCode: 'ReceiptSource', businessTypes: [], versionNo: 1, status: 'ENABLED', validation: null,
        published: { configuration: { schemaVersion: 1, sourceName: '收货单', mainCatalogId: '', parameterDeclarations: [], joins: [], fields: [] } } }
    })
  })
  afterEach(() => { app?.unmount(); app = undefined; root.remove() })
  async function start(path: string) {
    const router = (await import('../../router')).default
    app = createApp({ render: () => h(RouterView) })
    app.mixin({ beforeCreate() { if (this.$options.__name === 'PrintDataSourcesView') mounts++ } })
    app.use(Antd); app.use(router); app.mount(root)
    await router.push(path); await router.isReady()
    await new Promise(resolve => setTimeout(resolve, 20))
    return router
  }
  it.each(['/admin/print-data-sources', '/admin/print-data-sources/new', '/admin/print-data-sources/21', '/admin/print-data-sources/21/edit'])
    ('no view permission blocks %s before component creation or business data calls', async path => {
      const router = await start(path)
      expect(router.currentRoute.value.name).toBe('dashboard'); expect(mounts).toBe(0); expect(probe.api).not.toHaveBeenCalled()
    })
  it('view-only can mount actual read-only detail and invokes the instrumented API', async () => {
    probe.rights.add('print:source:view'); const router = await start('/admin/print-data-sources/21')
    expect(router.currentRoute.value.name).toBe('print-source-view'); expect(mounts).toBe(1); expect(probe.api).toHaveBeenCalled()
  })
  it('view-only cannot mount edit', async () => {
    probe.rights.add('print:source:view'); const router = await start('/admin/print-data-sources/21/edit')
    expect(router.currentRoute.value.name).toBe('dashboard'); expect(mounts).toBe(0); expect(probe.api).not.toHaveBeenCalled()
  })
  it('manage-only cannot bypass required view', async () => {
    probe.rights.add('print:source:manage'); const router = await start('/admin/print-data-sources/21/edit')
    expect(router.currentRoute.value.name).toBe('dashboard'); expect(mounts).toBe(0); expect(probe.api).not.toHaveBeenCalled()
  })
  it('publish-only cannot bypass view or manage', async () => {
    probe.rights.add('print:source:publish'); const router = await start('/admin/print-data-sources/21/edit')
    expect(router.currentRoute.value.name).toBe('dashboard'); expect(mounts).toBe(0); expect(probe.api).not.toHaveBeenCalled()
  })
  it('view and manage can mount actual edit, proving the denial probe is active', async () => {
    probe.rights.add('print:source:view'); probe.rights.add('print:source:manage'); const router = await start('/admin/print-data-sources/21/edit')
    expect(router.currentRoute.value.name).toBe('print-source-edit'); expect(mounts).toBe(1); expect(probe.api).toHaveBeenCalled()
  })
  it('awaits identity resolution before either mounting or reading protected data', async () => {
    let release!: () => void; probe.pending = new Promise<void>(resolve => { release = resolve })
    const operation = start('/admin/print-data-sources/21/edit')
    await new Promise(resolve => setTimeout(resolve, 30)); expect(mounts).toBe(0); expect(probe.api).not.toHaveBeenCalled()
    release(); const router = await operation
    expect(router.currentRoute.value.name).toBe('dashboard'); expect(mounts).toBe(0); expect(probe.api).not.toHaveBeenCalled()
  })
  it('unauthenticated direct access stops before mount and routes to login', async () => {
    probe.identity = false; const router = await start('/admin/print-data-sources/21/edit')
    expect(router.currentRoute.value.name).toBe('login'); expect(mounts).toBe(0); expect(probe.api).not.toHaveBeenCalled()
  })
})
