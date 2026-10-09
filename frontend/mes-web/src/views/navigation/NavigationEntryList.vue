<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, errorMessage, type Page } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import ReadReference from '../master/ReadReference.vue'
import { display } from '../quality/incomingModel'
type Row = Record<string, any>
const route = useRoute(), router = useRouter(), auth = useAuthStore()
const kind = computed(() => String(route.meta.navigationContext))
const execution = computed(() => kind.value === 'execution'), test = computed(() => kind.value === 'finished-tests')
const title = computed(() => String(route.meta.title))
const filters = ref({ keyword: '', status: '' }), rows = ref<Page<Row>>({ items: [], total: 0, page: 0, size: 20 }), busy = ref(false), error = ref('')
const states = computed(() => test.value ? ['READY', 'TESTING', 'COMPLETED'] : execution.value ? ['PENDING', 'READY', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'BLOCKED'] : kind.value === 'balance' ? ['DRAFT', 'RELEASED', 'IN_PROGRESS', 'PRODUCTION_COMPLETED', 'PENDING_QA', 'QA_RELEASED', 'REJECTED'] : ['PRODUCTION_COMPLETED', 'PENDING_QA', 'QA_RELEASED', 'REJECTED'])
const columns = computed(() => execution.value ? [{ title: '执行单元编号', dataIndex: 'executionNo' }, { title: '生产批', dataIndex: 'batchNo' }, { title: '执行类型', dataIndex: 'unitType' }, { title: '状态', key: 'status' }, { title: '操作', key: 'open' }] : test.value ? [{ title: '检验项目', dataIndex: 'testCode' }, { title: '成品请验单', dataIndex: 'inspectionRequestNo' }, { title: '样品编号', dataIndex: 'sampleNo' }, { title: '生产批', dataIndex: 'batchNo' }, { title: '检验次数', dataIndex: 'attemptNo' }, { title: '状态', key: 'status' }, { title: '操作', key: 'open' }] : [{ title: '生产批', dataIndex: 'batchNo' }, { title: '产品', key: 'product' }, { title: '计划数量', dataIndex: 'plannedQty' }, { title: '单位', key: 'unit' }, { title: '状态', key: 'status' }, { title: '操作', key: 'open' }])
let generation = 0
async function load(page = 0) {
 const ticket = ++generation, context = kind.value
 busy.value = true; error.value = ''
 try {
  const query = { page, size: 20, keyword: filters.value.keyword || undefined, status: filters.value.status || undefined }
  await router.replace({ query: Object.fromEntries(Object.entries(query).filter(([, value]) => value !== undefined).map(([key, value]) => [key, String(value)])) })
  if (ticket !== generation) return
  const data = await api<Page<Row>>({ url: context === 'execution' ? '/navigation/executions' : context === 'finished-tests' ? '/quality/finished-tests' : '/navigation/batches', params: { ...query, ...(context === 'execution' || context === 'finished-tests' ? {} : { context }) } })
  if (ticket === generation) rows.value = data
 } catch (failure) { if (ticket === generation) error.value = errorMessage(failure) }
 finally { if (ticket === generation) busy.value = false }
}
function open(row: Row) {
 const path = execution.value ? `/mes/execution/${row.id}` : test.value ? `/finished/tests/${row.id}` : kind.value === 'balance' ? `/production/batches/${row.id}/balance` : `/qa/batches/${row.id}/${kind.value === 'qa-review' ? 'review' : 'release'}`
 void router.push(path)
}
watch(kind, () => { rows.value = { items: [], total: 0, page: 0, size: 20 }; filters.value = { keyword: String(route.query.keyword ?? ''), status: String(route.query.status ?? '') }; void load(Number(route.query.page ?? 0)) }, { immediate: true })
</script>
<template><main class="admin-page master-page t1-query-list" data-ui-template="T1">
 <header class="admin-page-header"><div><h1>{{title}}</h1></div><a-space v-if="test" wrap><a-button v-if="auth.can('qms:finished-sampling:view')" @click="router.push('/finished/sampling')">从成品取样进入检验</a-button><a-button v-if="auth.can('qms:finished-report:view')" @click="router.push('/finished/reports')">检验报告</a-button></a-space></header>
 <a-card class="query-card" title="查询条件"><form class="query-form" @submit.prevent="load()"><label><span class="form-field-label">编号 / 关键字</span><a-input v-model:value="filters.keyword" aria-label="编号 / 关键字"/></label><label><span class="form-field-label">状态</span><a-select show-search option-filter-prop="children" v-model:value="filters.status" allow-clear aria-label="状态"><a-select-option v-for="state in states" :key="state" :value="state">{{display(state)}}</a-select-option></a-select></label><a-space><a-button @click="filters={keyword:'',status:''};load()">重置</a-button><a-button type="primary" html-type="submit">查询</a-button></a-space></form></a-card>
 <a-alert v-if="error" type="error" :message="error" show-icon/>
 <a-card class="result-card" :title="title+'列表'"><a-table :columns="columns" :data-source="rows.items" row-key="id" :loading="busy" :pagination="false" :scroll="{x:900}"><template #bodyCell="{column,record}"><ReadReference v-if="column.key==='product'" field="productId" :value="record.productId" fallback="暂无可读产品名称"/><ReadReference v-else-if="column.key==='unit'" field="unitId" :value="record.unitId" fallback="暂无可读单位名称"/><a-tag v-else-if="column.key==='status'">{{display(record.status)}}</a-tag><a-button v-else-if="column.key==='open'" type="link" @click="open(record)">{{execution?'进入生产执行':kind==='balance'?'查看物料平衡':kind==='qa-review'?'进入QA审核':kind==='release'?'查看放行审核':'查看检验记录'}}</a-button></template></a-table><div class="table-footer"><span>共 {{rows.total}} 条</span><a-pagination :current="rows.page+1" :page-size="rows.size" :total="rows.total" :show-size-changer="false" @change="(page:number)=>load(page-1)"/></div></a-card>
</main></template>
