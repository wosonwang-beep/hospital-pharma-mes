<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, errorMessage, type Page, type User } from '../../api/client'
import { canUseAction } from '../../auth/permissions'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const canManage = computed(() => canUseAction(auth.identity?.permissionCodes || [], 'menu:iam:users', 'action:iam:user.manage'))
const users = ref<Page<User>>({ items: [], total: 0, page: 0, size: 20 })
const keyword = ref('')
const status = ref('all')
const appliedKeyword = ref('')
const appliedStatus = ref('all')
const loading = ref(false)
const error = ref('')
const columns = [
  { title: '登录账号', dataIndex: 'loginName', key: 'loginName' },
  { title: '员工姓名', dataIndex: 'displayName', key: 'displayName' },
  { title: '状态', dataIndex: 'enabled', key: 'enabled', width: 120 },
  { title: '操作', key: 'actions', width: 160 }
]

async function load(page = 0) {
  loading.value = true
  error.value = ''
  try {
    users.value = await api<Page<User>>({ url: '/api/v1/admin/users', params: {
      page, size: 20, keyword: appliedKeyword.value || undefined,
      enabled: appliedStatus.value === 'all' ? undefined : appliedStatus.value === 'enabled'
    } })
  } catch (cause) { error.value = errorMessage(cause) }
  finally { loading.value = false }
}
function search() { appliedKeyword.value = keyword.value.trim(); appliedStatus.value = status.value; void load(0) }
function reset() { keyword.value = ''; status.value = 'all'; search() }
onMounted(() => load())
</script>

<template><section class="admin-page">
  <div class="admin-page-header"><div><h1>用户管理</h1><p>查询院内员工账号，进入独立页面维护资料与角色。</p></div><a-button v-if="canManage" type="primary" @click="router.push('/admin/users/new')">新增用户</a-button></div>
  <a-card class="query-card" title="查询条件" :bordered="false">
    <form class="query-form" @submit.prevent="search"><label>账号或姓名<a-input v-model:value="keyword" aria-label="账号或姓名" placeholder="请输入账号或姓名" allow-clear /></label><label>账号状态<a-select v-model:value="status" aria-label="账号状态" style="width: 160px"><a-select-option value="all">全部</a-select-option><a-select-option value="enabled">启用</a-select-option><a-select-option value="disabled">停用</a-select-option></a-select></label><div class="query-actions"><a-button type="primary" html-type="submit">查询</a-button><a-button @click="reset">重置</a-button></div></form>
  </a-card>
  <a-card class="result-card" title="用户列表" :bordered="false"><a-alert v-if="error" type="error" :message="error" show-icon class="result-error" />
    <a-table :columns="columns" :data-source="users.items" :loading="loading" :pagination="false" row-key="id" :scroll="{ x: 640 }">
      <template #bodyCell="{ column, record }"><template v-if="column.key === 'enabled'"><a-tag :color="record.enabled ? 'success' : 'default'">{{ record.enabled ? '启用' : '停用' }}</a-tag></template><template v-else-if="column.key === 'actions'"><a-button type="link" @click="router.push(`/admin/users/${record.id}/view`)">查看</a-button><a-button v-if="canManage" type="link" @click="router.push(`/admin/users/${record.id}/edit`)">编辑</a-button></template></template>
    </a-table>
    <div class="table-footer"><span>共 {{ users.total }} 条</span><a-pagination :current="users.page + 1" :page-size="users.size" :total="users.total" :show-size-changer="false" @change="(page: number) => load(page - 1)" /></div>
  </a-card>
</section></template>
