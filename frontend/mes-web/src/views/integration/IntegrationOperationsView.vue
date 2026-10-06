<script setup lang="ts">
import {Drawer} from 'ant-design-vue'
import { computed, onMounted, ref } from 'vue'
import { queryIntegrationMessages, retryIntegrationMessage } from '../../api/integration'
import { usePlatformAuthContext } from '../../auth/PlatformAuthContext'
import type { IntegrationMessage, IntegrationMessageQuery, IntegrationDirection, IntegrationMessageStatus } from '../../types/integration'

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
const detail=ref<IntegrationMessage|null>(null),direction=ref<IntegrationDirection>('OUTBOX'),filters=ref<Record<string,string>>({}),total=ref(0),pageNumber=ref(0)
const statuses:IntegrationMessageStatus[]=['RECEIVED','PROCESSING','RETRY_WAIT','PROCESSED','PENDING','DISPATCHING','PUBLISHED','DEAD_LETTER']
const statusNames:Record<string,string>={RECEIVED:'已接收',PROCESSING:'处理中',RETRY_WAIT:'等待重试',PROCESSED:'已处理',PENDING:'待发送',DISPATCHING:'发送中',PUBLISHED:'已发送',DEAD_LETTER:'死信'}
function query():IntegrationMessageQuery{const values=Object.fromEntries(Object.entries(filters.value).filter(([k,v])=>v.trim()&&k!=='status').map(([k,v])=>[k,k.startsWith('occurred')?new Date(v+'Z').toISOString():v.trim()]));return {...values,direction:direction.value,...(filters.value.status?{status:[filters.value.status as IntegrationMessageStatus]}:{}),page:pageNumber.value,size:50}}
function reset(){filters.value={};pageNumber.value=0;void load()}
function tab(value:IntegrationDirection){direction.value=value;pageNumber.value=0;void load()}


async function load() {
  if (!permitted.value) return
  loading.value = true
  failed.value = false
  try {
    const [pending, deadLetter, page] = await Promise.all([
      queryIntegrationMessages({ direction: 'OUTBOX', status: ['PENDING', 'RETRY_WAIT'], page: 0, size: 1 }),
      queryIntegrationMessages({ status: ['DEAD_LETTER'], page: 0, size: 1 }),
      queryIntegrationMessages(query())
    ])
    pendingTotal.value = pending.total
    deadLetterTotal.value = deadLetter.total
    messages.value = page.items
    total.value = page.total
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
  <main class="platform-page admin-page master-page t1-query-list" data-ui-template="T1">
    <section v-if="!permitted" class="state-card forbidden" role="alert">无权查看 Integration Operations</section>
    <template v-else>
      <header class="page-heading">
        <div><h1>Integration Inbox / Outbox Operations</h1><p>查询已有集成消息 · 人工重试保留原因、并发版本与幂等保护</p></div>
        <button class="secondary-button" type="button" :disabled="loading" @click="load">刷新</button>
      </header>
      <nav class="console-tabs" aria-label="消息方向"><button v-for="v in ['OUTBOX','INBOX']" :key="v" :aria-pressed="direction===v" @click="tab(v as IntegrationDirection)">{{v==='OUTBOX'?'Outbox 发件箱':'Inbox 收件箱'}}</button></nav>
      <section class="data-card query-card"><form class="query-form" @submit.prevent="pageNumber=0;load()"><label v-for="[field,title] in [['system','系统'],['messageId','消息编号'],['eventType','事件类型'],['aggregateType','业务对象类型'],['aggregateId','业务对象编号'],['occurredFrom','开始时间（UTC）'],['occurredTo','结束时间（UTC）']]" :key="field"><span class="form-field-label">{{title}}</span><input v-model="filters[field!]" :aria-label="title" :type="field!.startsWith('occurred')?'datetime-local':'text'" class="master-native-input"/></label><label><span class="form-field-label">状态</span><select v-model="filters.status" aria-label="状态" class="master-native-input"><option value="">全部</option><option v-for="state in statuses" :key="state" :value="state">{{statusNames[state]}}</option></select></label><div class="query-actions"><button class="primary-button" :disabled="loading">查询</button><button class="secondary-button" type="button" @click="reset">重置</button></div></form></section>
      <section class="metric-grid">
        <article class="metric-card"><span>Outbox Pending / Retry Wait</span><strong>{{ loading ? '—' : pendingTotal }}</strong><small>发件箱待发送或等待重试</small></article>
        <article class="metric-card"><span>Dead Letter</span><strong>{{ loading ? '—' : deadLetterTotal }}</strong><small>进入死信状态的消息</small></article>
      </section>
      <section class="data-card">
        <header><strong>Inbox / Outbox Operations</strong><span>受授权的人工重试</span></header>
        <div v-if="loading" class="table-state">正在加载集成消息…</div>
        <div v-else-if="failed" class="table-state error" role="alert">集成消息加载失败</div>
        <div v-else-if="messages.length === 0" class="table-state">暂无集成消息</div>
        <div v-else class="table-scroll">
          <table><thead><tr><th>Message Ref</th><th>方向/系统</th><th>Message ID</th><th>状态</th><th>Retry</th><th>Next Retry</th><th>操作</th></tr></thead>
          <tbody><tr v-for="message in messages" :key="message.messageRef">
            <td>{{ message.messageRef }}</td><td>{{ message.direction }} / {{ message.sourceSystem ?? message.targetSystem ?? '—' }}</td>
            <td>{{ message.messageId }}</td><td>{{ statusNames[message.status]??message.status }}</td><td>{{ message.retryCount }}</td><td>{{ message.nextRetryAt ?? '—' }}</td>
            <td><button class="link-button" type="button" @click="detail=message">查看详情</button><button v-if="mayRetry" class="link-button" type="button" :aria-label="`人工重试 ${message.messageRef}`" @click="openRetry(message)">人工重试</button><span v-else>—</span></td>
          </tr></tbody></table>
        </div>
      </section>
      <footer class="table-footer"><span>共 {{total}} 条</span><div class="action-group"><button class="secondary-button" :disabled="loading||pageNumber===0" @click="pageNumber--;load()">上一页</button><span>第 {{pageNumber+1}} 页</span><button class="secondary-button" :disabled="loading||(pageNumber+1)*50>=total" @click="pageNumber++;load()">下一页</button></div></footer>
      <Drawer :open="!!detail" title="集成消息详情" :width="560" @close="detail=null"><section v-if="detail" class="evidence-facts" role="dialog" aria-modal="true" aria-label="集成消息详情"><header><h2>集成消息详情</h2><button class="secondary-button" @click="detail=null">关闭</button></header><dl><template v-for="[key,title] in [['messageRef','消息引用'],['messageId','消息编号'],['direction','方向'],['sourceSystem','来源系统'],['targetSystem','目标系统'],['eventType','事件类型'],['aggregateType','业务对象类型'],['aggregateId','业务对象编号'],['status','状态'],['retryCount','尝试次数'],['nextRetryAt','下次重试时间'],['lastErrorCode','错误码'],['lastErrorMessage','错误详情'],['occurredAt','发生时间'],['completedAt','完成时间'],['versionNo','并发版本']]" :key="key"><dt>{{title}}</dt><dd>{{key==='status'?statusNames[detail.status]:detail[key as keyof IntegrationMessage]??'—'}}</dd></template></dl><p class="muted">当前接口提供错误与重试事实，未提供消息载荷。</p></section></Drawer>
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
