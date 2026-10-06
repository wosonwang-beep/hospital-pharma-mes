<script setup lang="ts">
import {Drawer} from 'ant-design-vue'
import { computed, watch, ref } from 'vue'
import { useRoute } from 'vue-router'
import { queryAuditEvents } from '../../api/audit'
import { usePlatformAuthContext } from '../../auth/PlatformAuthContext'
import type { AuditEvent, AuditEventQuery } from '../../types/audit'

const authorization = usePlatformAuthContext()
const route = useRoute()
const objectFilter = computed(() => ({
  objectId: typeof route.query.objectId === 'string' ? route.query.objectId : undefined,
  objectType: typeof route.query.objectType === 'string' ? route.query.objectType : undefined
}))
const permitted = computed(() => authorization.can('audit:view'))
const events = ref<AuditEvent[]>([])
const todayTotal = ref(0)
const loading = ref(false)
const failed = ref(false)
const selected = ref<AuditEvent | null>(null), total = ref(0), pageNumber = ref(0)
const filters = ref<Record<string,string>>({})
const filterFields=[['actorId','操作人'],['action','业务动作'],['objectType','对象类型'],['objectId','对象编号'],['transactionId','事务编号'],['requestId','请求编号'],['occurredFrom','开始时间（UTC）'],['occurredTo','结束时间（UTC）']]
function query(): AuditEventQuery {const values=Object.fromEntries(Object.entries(filters.value).filter(([,v])=>v.trim()).map(([k,v])=>[k,k.startsWith('occurred')?new Date(v+'Z').toISOString():v.trim()]));return {...objectFilter.value,...values,page:pageNumber.value,size:50}}
function reset(){filters.value={};pageNumber.value=0;void load()}


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
      queryAuditEvents({ ...utcDayRange(), ...objectFilter.value, page: 0, size: 1 }),
      queryAuditEvents(query())
    ])
    todayTotal.value = today.total
    events.value = page.items
    total.value = page.total
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
}

watch(() => route.query, load, { immediate: true })
</script>

<template>
  <main class="platform-page admin-page master-page" data-ui-template="Specialized">
    <section v-if="!permitted" class="state-card forbidden" role="alert">无权查看 GMP Audit Trail</section>
    <template v-else>
      <header class="page-heading">
        <div><h1>GMP Audit Trail</h1><p>查询已有审计事件与变更证据 · 只读</p></div>
        <button class="secondary-button" type="button" :disabled="loading" @click="load">刷新</button>
      </header>
      <section class="data-card query-card"><form class="query-form" @submit.prevent="pageNumber=0;load()"><label v-for="[field,title] in filterFields" :key="field"><span class="form-field-label">{{title}}</span><input v-model="filters[field!]" :aria-label="title" :type="field!.startsWith('occurred')?'datetime-local':'text'" class="master-native-input"/></label><label><span class="form-field-label">来源</span><select v-model="filters.source" aria-label="来源" class="master-native-input"><option value="">全部</option><option v-for="s in ['API','SCHEDULER','INTEGRATION','SYSTEM']" :key="s">{{s}}</option></select></label><div class="query-actions"><button class="primary-button" :disabled="loading">查询</button><button class="secondary-button" type="button" @click="reset">重置</button></div></form></section>
      <section class="metric-grid one">
        <article class="metric-card"><span>今日审计事件</span><strong>{{ loading ? '—' : todayTotal }}</strong><small>当前对象范围 · UTC 日</small></article>
      </section>
      <section class="data-card">
        <header><strong>AuditEvent 查询</strong><span>按发生时间倒序</span></header>
        <div v-if="loading" class="table-state">正在加载审计事件…</div>
        <div v-else-if="failed" class="table-state error" role="alert">审计事件加载失败</div>
        <div v-else-if="events.length === 0" class="table-state">暂无审计事件</div>
        <div v-else class="table-scroll">
          <table><thead><tr><th>时间</th><th>Actor / Role</th><th>Action</th><th>Object</th><th>Transaction / Request</th><th>Source</th><th>操作</th></tr></thead>
          <tbody><tr v-for="event in events" :key="event.id">
            <td>{{ event.occurredAt }}</td><td>{{ event.actorId }} / {{ event.actorRole ?? '—' }}</td><td>{{ event.action }}</td>
            <td>{{ event.objectType }} / {{ event.objectId }}</td><td>{{ event.transactionId }} / {{ event.requestId ?? '—' }}</td><td>{{ event.source }}</td><td><button class="link-button" @click="selected=event">查看详情</button></td>
          </tr></tbody></table>
        </div>
      </section>
      <footer class="table-footer"><span>共 {{total}} 条</span><div class="action-group"><button class="secondary-button" :disabled="loading||pageNumber===0" @click="pageNumber--;load()">上一页</button><span>第 {{pageNumber+1}} 页</span><button class="secondary-button" :disabled="loading||(pageNumber+1)*50>=total" @click="pageNumber++;load()">下一页</button></div></footer>
      <Drawer :open="!!selected" title="审计事件详情" :width="560" @close="selected=null"><section v-if="selected" class="evidence-facts" role="dialog" aria-modal="true" aria-label="审计事件详情"><header><h2>审计事件详情</h2><button class="secondary-button" @click="selected=null">关闭</button></header><dl><template v-for="[key,title] in [['occurredAt','发生时间'],['actorId','操作人'],['actorRole','角色'],['action','业务动作'],['objectType','对象类型'],['objectId','对象编号'],['reason','操作原因'],['oldValueDigest','原值摘要'],['newValueDigest','新值摘要'],['clientInfo','客户端信息'],['transactionId','事务编号'],['requestId','请求编号'],['source','来源'],['id','事件标识']]" :key="key"><dt>{{title}}</dt><dd>{{selected[key as keyof AuditEvent]??'—'}}</dd></template></dl><p class="muted">此记录提供变更摘要；接口未提供原始值或完整载荷。</p></section></Drawer>
    </template>
  </main>
</template>
