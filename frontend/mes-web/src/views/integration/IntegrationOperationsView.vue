<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { queryIntegrationMessages, retryIntegrationMessage } from '../../api/integration'
import { usePlatformAuthContext } from '../../auth/PlatformAuthContext'
import type { IntegrationMessage } from '../../types/integration'

const authorization = usePlatformAuthContext()
const permitted = computed(() => authorization.can('integration:view'))
const mayRetry = computed(() => permitted.value && authorization.can('integration:retry'))
const messages = ref<IntegrationMessage[]>([])
const pendingTotal = ref(0)
const deadLetterTotal = ref(0)
const loading = ref(false)
const failed = ref(false)
const selected = ref<IntegrationMessage | null>(null)
const retryReason = ref('')
const retryError = ref(false)

async function load() {
  if (!permitted.value) return
  loading.value = true
  failed.value = false
  try {
    const [pending, deadLetter, page] = await Promise.all([
      queryIntegrationMessages({ direction: 'OUTBOX', status: ['PENDING', 'RETRY_WAIT'], page: 0, size: 1 }),
      queryIntegrationMessages({ status: ['DEAD_LETTER'], page: 0, size: 1 }),
      queryIntegrationMessages({ page: 0, size: 50 })
    ])
    pendingTotal.value = pending.total
    deadLetterTotal.value = deadLetter.total
    messages.value = page.items
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
}

function openRetry(message: IntegrationMessage) {
  selected.value = message
  retryReason.value = ''
  retryError.value = false
}

async function confirmRetry() {
  if (!selected.value || !retryReason.value.trim()) return
  retryError.value = false
  try {
    const updated = await retryIntegrationMessage({
      messageRef: selected.value.messageRef,
      reason: retryReason.value.trim(),
      versionNo: selected.value.versionNo,
      idempotencyKey: crypto.randomUUID()
    })
    messages.value = messages.value.map(item => item.messageRef === updated.messageRef ? updated : item)
    selected.value = null
  } catch {
    retryError.value = true
  }
}

onMounted(load)
</script>

<template>
  <main class="platform-page">
    <section v-if="!permitted" class="state-card forbidden" role="alert">无权查看 Integration Operations</section>
    <template v-else>
      <header class="page-heading">
        <div><h1>Integration Inbox / Outbox Operations</h1><p>受控业务界面 · API 驱动状态 · 人工重试受双权限、并发版本与幂等键保护</p></div>
        <button class="secondary-button" type="button" :disabled="loading" @click="load">刷新</button>
      </header>
      <section class="contract-strip" aria-label="页面契约">
        <div><span>REQUIREMENT</span><strong>INT-001</strong></div><div><span>ROUTE</span><strong>/integration/operations</strong></div>
        <div><span>API</span><strong>GET /integration/messages</strong></div><div><span>PERMISSION</span><strong>integration:view</strong></div>
      </section>
      <section class="metric-grid">
        <article class="metric-card"><span>Outbox Pending / Retry Wait</span><strong>{{ loading ? '—' : pendingTotal }}</strong><small>direction=OUTBOX</small></article>
        <article class="metric-card"><span>Dead Letter</span><strong>{{ loading ? '—' : deadLetterTotal }}</strong><small>status=DEAD_LETTER</small></article>
      </section>
      <section class="data-card">
        <header><strong>Inbox / Outbox Operations</strong><span>重试需 integration:view + integration:retry</span></header>
        <div v-if="loading" class="table-state">正在加载集成消息…</div>
        <div v-else-if="failed" class="table-state error" role="alert">集成消息加载失败</div>
        <div v-else-if="messages.length === 0" class="table-state">暂无集成消息</div>
        <div v-else class="table-scroll">
          <table><thead><tr><th>Message Ref</th><th>方向/系统</th><th>Message ID</th><th>状态</th><th>Retry</th><th>Next Retry</th><th>操作</th></tr></thead>
          <tbody><tr v-for="message in messages" :key="message.messageRef">
            <td>{{ message.messageRef }}</td><td>{{ message.direction }} / {{ message.sourceSystem ?? message.targetSystem ?? '—' }}</td>
            <td>{{ message.messageId }}</td><td>{{ message.status }}</td><td>{{ message.retryCount }}</td><td>{{ message.nextRetryAt ?? '—' }}</td>
            <td><button v-if="mayRetry" class="link-button" type="button" :aria-label="`人工重试 ${message.messageRef}`" @click="openRetry(message)">人工重试</button><span v-else>—</span></td>
          </tr></tbody></table>
        </div>
      </section>
      <div v-if="selected" class="dialog-backdrop" role="presentation">
        <section class="retry-dialog" role="dialog" aria-modal="true" aria-labelledby="retry-title">
          <h2 id="retry-title">人工重试 {{ selected.messageRef }}</h2>
          <label for="retry-reason">重试原因</label>
          <textarea id="retry-reason" v-model="retryReason" maxlength="1000" rows="4" />
          <p v-if="retryError" class="error" role="alert">人工重试提交失败</p>
          <footer><button type="button" class="secondary-button" @click="selected = null">取消</button><button type="button" class="primary-button" :disabled="!retryReason.trim()" @click="confirmRetry">确认重试</button></footer>
        </section>
      </div>
    </template>
  </main>
</template>
