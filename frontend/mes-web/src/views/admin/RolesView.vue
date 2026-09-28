<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, errorMessage, type Page, type Role } from '../../api/client'
import { canUseAction } from '../../auth/permissions'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const canManage = computed(() => canUseAction(auth.identity?.permissionCodes || [], 'menu:iam:roles', 'action:iam:role.manage'))
const roles = ref<Page<Role>>({ items: [], total: 0, page: 0, size: 20 })
const keyword = ref('')
const status = ref('all')
const appliedKeyword = ref('')
const appliedStatus = ref('all')
const loading = ref(false)
const error = ref('')
const columns = [
  { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode' },
  { title: '角色名称', dataIndex: 'displayName', key: 'displayName' },
  { title: '状态', dataIndex: 'enabled', key: 'enabled', width: 120 },
  { title: '操作', key: 'actions', width: 160 }
]

async function load(page = 0) {
  loading.value = true
  error.value = ''
  try {
    roles.value = await api<Page<Role>>({ url: '/api/v1/admin/roles', params: {
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
  <div class="admin-page-header"><div><h1>角色与权限</h1><p>查询角色，进入独立页面维护角色资料与功能权限。</p></div><a-button v-if="canManage" type="primary" @click="router.push('/admin/roles/new')">新增角色</a-button></div>
  <a-card class="query-card" title="查询条件" :bordered="false">
    <form class="query-form" @submit.prevent="search"><label>编码或名称<a-input v-model:value="keyword" aria-label="编码或名称" placeholder="请输入角色编码或名称" allow-clear /></label><label>角色状态<a-select v-model:value="status" aria-label="角色状态" style="width: 160px"><a-select-option value="all">全部</a-select-option><a-select-option value="enabled">启用</a-select-option><a-select-option value="disabled">停用</a-select-option></a-select></label><div class="query-actions"><a-button type="primary" html-type="submit">查询</a-button><a-button @click="reset">重置</a-button></div></form>
  </a-card>
  <a-card class="result-card" title="角色列表" :bordered="false"><a-alert v-if="error" type="error" :message="error" show-icon class="result-error" />
    <a-table :columns="columns" :data-source="roles.items" :loading="loading" :pagination="false" row-key="id" :scroll="{ x: 640 }">
      <template #bodyCell="{ column, record }"><template v-if="column.key === 'enabled'"><a-tag :color="record.enabled ? 'success' : 'default'">{{ record.enabled ? '启用' : '停用' }}</a-tag></template><template v-else-if="column.key === 'actions'"><a-button type="link" @click="router.push(`/admin/roles/${record.id}/view`)">查看</a-button><a-button v-if="canManage" type="link" @click="router.push(`/admin/roles/${record.id}/edit`)">编辑</a-button></template></template>
    </a-table>
    <div class="table-footer"><span>共 {{ roles.total }} 条</span><a-pagination :current="roles.page + 1" :page-size="roles.size" :total="roles.total" :show-size-changer="false" @change="(page: number) => load(page - 1)" /></div>
  </a-card>
</section></template>
