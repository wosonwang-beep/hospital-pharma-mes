<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { queryAuditEvents } from '../../api/audit'
import { usePlatformAuthContext } from '../../auth/PlatformAuthContext'
import type { AuditEvent } from '../../types/audit'

const authorization = usePlatformAuthContext()
const permitted = computed(() => authorization.can('audit:view'))
const events = ref<AuditEvent[]>([])
const todayTotal = ref(0)
const loading = ref(false)
const failed = ref(false)

function utcDayRange(now = new Date()) {
  const from = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate()))
  const to = new Date(from)
  to.setUTCDate(to.getUTCDate() + 1)
  return { occurredFrom: from.toISOString(), occurredTo: to.toISOString() }
}

async function load() {
  if (!permitted.value) return
  loading.value = true
  failed.value = false
  try {
    const [today, page] = await Promise.all([
      queryAuditEvents({ ...utcDayRange(), page: 0, size: 1 }),
      queryAuditEvents({ page: 0, size: 50 })
    ])
    todayTotal.value = today.total
    events.value = page.items
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <main class="platform-page">
    <section v-if="!permitted" class="state-card forbidden" role="alert">无权查看 GMP Audit Trail</section>
    <template v-else>
      <header class="page-heading">
        <div><h1>GMP Audit Trail</h1><p>受控业务界面 · 数据为原型演示 · 所有动作遵循权限与 allowedActions</p></div>
        <button class="secondary-button" type="button" :disabled="loading" @click="load">刷新</button>
      </header>
      <section class="contract-strip" aria-label="页面契约">
        <div><span>REQUIREMENT</span><strong>AUD-001</strong></div><div><span>ROUTE</span><strong>/audit</strong></div>
        <div><span>API</span><strong>GET /audit-events</strong></div><div><span>PERMISSION</span><strong>audit:view</strong></div>
      </section>
      <section class="metric-grid one">
        <article class="metric-card"><span>今日审计事件</span><strong>{{ loading ? '—' : todayTotal }}</strong><small>GET /audit-events · UTC日范围</small></article>
      </section>
      <section class="data-card">
        <header><strong>AuditEvent 查询</strong><span>固定排序 occurred_at DESC, id DESC</span></header>
        <div v-if="loading" class="table-state">正在加载审计事件…</div>
        <div v-else-if="failed" class="table-state error" role="alert">审计事件加载失败</div>
        <div v-else-if="events.length === 0" class="table-state">暂无审计事件</div>
        <div v-else class="table-scroll">
          <table><thead><tr><th>时间</th><th>Actor / Role</th><th>Action</th><th>Object</th><th>Transaction / Request</th><th>Source</th></tr></thead>
          <tbody><tr v-for="event in events" :key="event.id">
            <td>{{ event.occurredAt }}</td><td>{{ event.actorId }} / {{ event.actorRole ?? '—' }}</td><td>{{ event.action }}</td>
            <td>{{ event.objectType }} / {{ event.objectId }}</td><td>{{ event.transactionId }} / {{ event.requestId ?? '—' }}</td><td>{{ event.source }}</td>
          </tr></tbody></table>
        </div>
      </section>
    </template>
  </main>
</template>
