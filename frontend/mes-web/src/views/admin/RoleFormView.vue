<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, errorMessage, type Permission, type Role } from '../../api/client'
import { canUseAction } from '../../auth/permissions'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isNew = computed(() => route.name === 'role-new')
const isView = computed(() => route.name === 'role-view')
const canManage = computed(() => canUseAction(auth.identity?.permissionCodes || [], 'menu:iam:roles', 'action:iam:role.manage'))
const canEdit = computed(() => canManage.value && !isView.value)
const role = ref<Role | null>(null)
const catalog = ref<Permission[]>([])
const roleCode = ref('')
const displayName = ref('')
const error = ref('')
const notice = ref('')
const busy = ref(false)
const modules = computed(() => [...new Set(catalog.value.filter(item => item.enabled).map(item => item.module))])

async function load() {
  if (isNew.value) return
  try {
    role.value = await api<Role>({ url: `/api/v1/admin/roles/${route.params.id}` })
    displayName.value = role.value.displayName
    catalog.value = await api<Permission[]>({ url: '/api/v1/admin/permissions' })
  } catch (cause) { error.value = errorMessage(cause) }
}
async function perform(operation: () => Promise<void>) {
  busy.value = true; error.value = ''; notice.value = ''
  try { await operation() }
  catch (cause) { error.value = errorMessage(cause) }
  finally { busy.value = false }
}
async function save() {
  await perform(async () => {
    if (isNew.value) {
      const created = await api<Role>({ method: 'POST', url: '/api/v1/admin/roles', data: { roleCode: roleCode.value.trim().toUpperCase(), displayName: displayName.value.trim() } })
      await router.replace(`/admin/roles/${created.id}/edit`)
      await load()
      notice.value = '角色已创建，可继续分配权限'
    } else if (role.value) {
      role.value = await api<Role>({ method: 'PATCH', url: `/api/v1/admin/roles/${role.value.id}/name`, data: { displayName: displayName.value.trim(), expectedVersion: role.value.version } })
      notice.value = '角色名称已保存'
    }
  })
}
async function toggleStatus() {
  if (!role.value) return
  await perform(async () => {
    role.value = await api<Role>({ method: 'POST', url: `/api/v1/admin/roles/${role.value!.id}/${role.value!.enabled ? 'disable' : 'enable'}`, data: { expectedVersion: role.value!.version } })
    notice.value = role.value.enabled ? '角色已启用' : '角色已停用'
  })
}
async function changePermission(code: string, grant: boolean) {
  if (!role.value) return
  await perform(async () => {
    await api<void>({ method: grant ? 'POST' : 'DELETE', url: `/api/v1/admin/roles/${role.value!.id}/permissions/${encodeURIComponent(code)}` })
    role.value = await api<Role>({ url: `/api/v1/admin/roles/${role.value!.id}` })
    notice.value = '权限分配已更新'
  })
}
onMounted(load)
</script>

<template><section class="admin-page form-page">
  <div class="form-page-header"><a-button type="link" @click="router.push('/admin/roles')">← 返回角色列表</a-button><h1>{{ isNew ? '新增角色' : isView ? '查看角色' : '编辑角色' }}</h1><a-button v-if="isView && canManage && role" type="primary" @click="router.push(`/admin/roles/${role.id}/edit`)">编辑角色</a-button></div>
  <a-alert v-if="error" type="error" :message="error" show-icon class="form-alert" /><a-alert v-if="notice" type="success" :message="notice" show-icon class="form-alert" />
  <a-card title="基本信息" class="form-section"><a-form layout="vertical" class="detail-form"><a-form-item label="角色编码"><a-input v-if="isNew" v-model:value="roleCode" aria-label="角色编码" :disabled="!canEdit" placeholder="例如 QUALITY_MANAGER" /><a-input v-else :value="role?.roleCode" disabled /></a-form-item><a-form-item label="角色名称"><a-input v-model:value="displayName" aria-label="角色名称" :disabled="!canEdit" placeholder="请输入角色名称" /></a-form-item><a-form-item v-if="role" label="角色状态"><a-tag :color="role.enabled ? 'success' : 'default'">{{ role.enabled ? '启用' : '停用' }}</a-tag></a-form-item><div v-if="canEdit" class="form-actions"><a-button type="primary" :loading="busy" @click="save">保存</a-button><a-button @click="router.push('/admin/roles')">关闭</a-button></div></a-form></a-card>
  <a-card v-if="!isNew && role && canEdit" title="角色操作" class="form-section"><a-popconfirm :title="role.enabled ? '确认停用此角色？' : '确认启用此角色？'" @confirm="toggleStatus"><a-button :disabled="busy">{{ role.enabled ? '停用角色' : '启用角色' }}</a-button></a-popconfirm></a-card>
  <a-card v-if="!isNew && role" :title="isView ? '已分配权限' : '权限分配'" class="form-section"><p class="muted">每个模块分配菜单权限和通用操作权限；操作权限需要同时拥有对应菜单权限。</p><a-empty v-if="!modules.length" description="暂无已注册权限" /><div v-for="module in modules" :key="module" class="permission-group"><h3>{{ module }}</h3><div class="assignment-list"><label v-for="permission in catalog.filter(item => item.enabled && item.module === module)" :key="permission.permissionCode" class="assignment-row"><a-checkbox :checked="role.permissionCodes.includes(permission.permissionCode)" :disabled="!canEdit || busy" @change="changePermission(permission.permissionCode, !role!.permissionCodes.includes(permission.permissionCode))" /><span>{{ permission.displayName }} <small>{{ permission.permissionType === 'MENU' ? '菜单' : '通用操作' }}</small></span></label></div></div></a-card>
</section></template>
