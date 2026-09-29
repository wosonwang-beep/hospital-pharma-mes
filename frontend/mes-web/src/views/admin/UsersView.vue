<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, errorMessage, type Page, type Role, type User } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
const route = useRoute(); const router = useRouter(); const auth = useAuthStore()
const data = ref<Page<User>>({ items: [], total: 0, page: 0, size: 20 })
const roles = ref<Role[]>([])
const queryText = (value: unknown) => typeof value === 'string' ? value : ''
const keyword = ref(queryText(route.query.keyword)); const status = ref(queryText(route.query.status) || undefined); const roleId = ref(queryText(route.query.roleId) || undefined)
const loading = ref(false); const error = ref('')
const columns = [{ title:'账号', dataIndex:'username', sorter:(a:User,b:User)=>a.username.localeCompare(b.username) }, { title:'姓名', dataIndex:'displayName', sorter:(a:User,b:User)=>a.displayName.localeCompare(b.displayName) }, { title:'状态', key:'status', sorter:(a:User,b:User)=>a.status.localeCompare(b.status) }, { title:'角色', key:'roles' }, { title:'最后登录', key:'lastLogin', sorter:(a:User,b:User)=>(a.lastLoginAt??'').localeCompare(b.lastLoginAt??'') }, { title:'更新时间', key:'updatedAt', sorter:(a:User,b:User)=>a.updatedAt.localeCompare(b.updatedAt) }, { title:'操作', key:'actions' }]
function displayTime(value?: string) { return value ? new Date(value.endsWith('Z') ? value : `${value}Z`).toLocaleString('zh-CN', { hour12: false }) : '从未登录' }
async function load(page = Number(route.query.page ?? 0)) {
  loading.value = true; error.value = ''
  try {
    await router.replace({ query: { keyword: keyword.value || undefined, status: status.value, roleId: roleId.value, page: page || undefined } })
    data.value = await api<Page<User>>({ url: '/users', params: { page, size: 20, keyword: keyword.value || undefined, status: status.value, roleId: roleId.value } })
  }
  catch (cause) { error.value = errorMessage(cause) } finally { loading.value = false }
}
function reset() { keyword.value = ''; status.value = undefined; roleId.value = undefined; void load(0) }
onMounted(async () => { if (auth.can('iam:role:view')) roles.value = (await api<Page<Role>>({ url:'/roles', params:{ page:0, size:100 } })).items; await load() })
</script>
<template><main class="admin-page"><header class="admin-page-header"><div><h1>用户管理</h1><p>查询员工账号并在独立页面维护资料与角色。</p></div>
  <a-button v-if="auth.can('iam:user:create')" type="primary" @click="router.push({path:'/admin/users/create',query:route.query})">新增用户</a-button></header>
  <a-card title="查询条件" class="query-card"><form class="query-form" @submit.prevent="load(0)"><label>账号或姓名<a-input v-model:value="keyword" allow-clear /></label>
  <label>状态<a-select v-model:value="status" allow-clear style="width:160px"><a-select-option value="ACTIVE">启用</a-select-option><a-select-option value="INACTIVE">停用</a-select-option></a-select></label>
  <label v-if="auth.can('iam:role:view')">角色<a-select v-model:value="roleId" allow-clear style="width:220px"><a-select-option v-for="role in roles" :key="role.id" :value="role.id">{{ role.roleName }}</a-select-option></a-select></label>
  <a-space><a-button type="primary" html-type="submit">查询</a-button><a-button @click="reset">重置</a-button></a-space></form></a-card>
  <a-alert v-if="error" type="error" :message="error" show-icon /><a-card title="用户列表" class="result-card"><a-table :columns="columns" :data-source="data.items" row-key="id" :loading="loading" :pagination="false">
  <template #bodyCell="{ column, record }"><template v-if="column.key==='roles'">{{ record.roleNames.length ? record.roleNames.join('、') : '未分配' }}</template><template v-else-if="column.key==='status'"><a-tag :color="record.status==='ACTIVE'?'success':'default'">{{ record.status==='ACTIVE'?'启用':'停用' }}</a-tag></template>
  <template v-else-if="column.key==='lastLogin'">{{ displayTime(record.lastLoginAt) }}</template><template v-else-if="column.key==='updatedAt'">{{ displayTime(record.updatedAt) }}</template>
  <template v-else-if="column.key==='actions'"><a-button type="link" @click="router.push({path:`/admin/users/${record.id}`,query:route.query})">查看</a-button><a-button v-if="auth.can('iam:user:update')" type="link" @click="router.push({path:`/admin/users/${record.id}/edit`,query:route.query})">编辑</a-button></template></template></a-table>
  <div class="table-footer"><span>共 {{ data.total }} 条</span><a-pagination :current="data.page+1" :page-size="data.size" :total="data.total" :show-size-changer="false" @change="(p:number)=>load(p-1)" /></div></a-card>
</main></template>
