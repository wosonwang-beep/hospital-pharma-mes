<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, errorMessage, type Page, type Role, type User } from '../../api/client'
import { canUseAction, canUseRoute } from '../../auth/permissions'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isNew = computed(() => route.name === 'user-new')
const isView = computed(() => route.name === 'user-view')
const canManage = computed(() => canUseAction(auth.identity?.permissionCodes || [], 'menu:iam:users', 'action:iam:user.manage'))
const canEdit = computed(() => canManage.value && !isView.value)
const canReadRoles = computed(() => canUseRoute(auth.identity?.permissionCodes || [], 'menu:iam:roles'))
const user = ref<User | null>(null)
const roles = ref<Role[]>([])
const loginName = ref('')
const displayName = ref('')
const temporaryPassword = ref('')
const error = ref('')
const notice = ref('')
const busy = ref(false)

async function load() {
  try {
    if (!isNew.value) {
      user.value = await api<User>({ url: `/api/v1/admin/users/${route.params.id}` })
      displayName.value = user.value.displayName
    }
    if (canReadRoles.value && !isNew.value) {
      const all: Role[] = []
      let page = 0
      let result: Page<Role>
      do {
        result = await api<Page<Role>>({ url: '/api/v1/admin/roles', params: { page, size: 100 } })
        all.push(...result.items)
        page++
      } while (all.length < result.total)
      roles.value = all
    }
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
      const created = await api<{ user: User; temporaryPassword: string }>({ method: 'POST', url: '/api/v1/admin/users', data: { loginName: loginName.value.trim(), displayName: displayName.value.trim() } })
      user.value = created.user
      temporaryPassword.value = created.temporaryPassword
    } else if (user.value) {
      user.value = await api<User>({ method: 'PATCH', url: `/api/v1/admin/users/${user.value.id}/profile`, data: { displayName: displayName.value.trim(), expectedVersion: user.value.version } })
      notice.value = '用户资料已保存'
    }
  })
}
async function toggleStatus() {
  if (!user.value) return
  await perform(async () => {
    user.value = await api<User>({ method: 'POST', url: `/api/v1/admin/users/${user.value!.id}/${user.value!.enabled ? 'disable' : 'enable'}`, data: { expectedVersion: user.value!.version } })
    notice.value = user.value.enabled ? '账号已启用' : '账号已停用'
  })
}
async function resetPassword() {
  if (!user.value) return
  await perform(async () => {
    temporaryPassword.value = (await api<{ value: string }>({ method: 'POST', url: `/api/v1/admin/users/${user.value!.id}/password-reset` })).value
  })
}
async function changeRole(roleId: number, grant: boolean) {
  if (!user.value) return
  await perform(async () => {
    await api<void>({ method: grant ? 'POST' : 'DELETE', url: `/api/v1/admin/users/${user.value!.id}/roles/${roleId}` })
    user.value = await api<User>({ url: `/api/v1/admin/users/${user.value!.id}` })
    notice.value = '角色分配已更新'
  })
}
async function continueRoles() {
  if (!user.value) return
  await router.replace(`/admin/users/${user.value.id}/edit`)
  temporaryPassword.value = ''
  await load()
}
onMounted(load)
</script>

<template><section class="admin-page form-page">
  <div class="form-page-header"><a-button type="link" @click="router.push('/admin/users')">← 返回用户列表</a-button><h1>{{ isNew ? '新增用户' : isView ? '查看用户' : '编辑用户' }}</h1><a-button v-if="isView && canManage && user" type="primary" @click="router.push(`/admin/users/${user.id}/edit`)">编辑用户</a-button></div>
  <a-alert v-if="error" type="error" :message="error" show-icon class="form-alert" /><a-alert v-if="notice" type="success" :message="notice" show-icon class="form-alert" />
  <a-card v-if="temporaryPassword" title="临时密码（仅显示一次）" class="form-section"><a-alert type="warning" show-icon message="请通过院内安全渠道交给员工。关闭此页后无法再次查看。" /><p class="temporary-secret">{{ temporaryPassword }}</p><a-space><a-button @click="router.push('/admin/users')">返回用户列表</a-button><a-button v-if="isNew && user" type="primary" @click="continueRoles">继续分配角色</a-button><a-button v-if="!isNew" @click="temporaryPassword = ''">已记录，关闭</a-button></a-space></a-card>
  <template v-else><a-card title="基本信息" class="form-section"><a-form layout="vertical" class="detail-form"><a-form-item label="登录账号"><a-input v-if="isNew" v-model:value="loginName" aria-label="登录账号" :disabled="!canEdit" placeholder="请输入员工登录账号" /><a-input v-else :value="user?.loginName" disabled /></a-form-item><a-form-item label="员工姓名"><a-input v-model:value="displayName" aria-label="员工姓名" :disabled="!canEdit" placeholder="请输入员工姓名" /></a-form-item><a-form-item v-if="!isNew && user" label="账号状态"><a-tag :color="user.enabled ? 'success' : 'default'">{{ user.enabled ? '启用' : '停用' }}</a-tag></a-form-item><div v-if="canEdit" class="form-actions"><a-button type="primary" :loading="busy" @click="save">保存</a-button><a-button @click="router.push('/admin/users')">关闭</a-button></div></a-form></a-card>
    <a-card v-if="!isNew && user && canEdit" title="账号操作" class="form-section"><a-space wrap><a-popconfirm :title="user.enabled ? '确认停用此账号？' : '确认启用此账号？'" @confirm="toggleStatus"><a-button :disabled="busy">{{ user.enabled ? '停用账号' : '启用账号' }}</a-button></a-popconfirm><a-popconfirm title="确认重置密码？现有登录会话将失效。" @confirm="resetPassword"><a-button :disabled="busy">重置密码</a-button></a-popconfirm></a-space></a-card>
    <a-card v-if="!isNew && user" :title="isView ? '已分配角色' : '角色分配'" class="form-section"><p v-if="!canReadRoles" class="muted">当前账号没有角色目录查看权限。</p><a-empty v-else-if="!roles.length" description="暂无可分配角色" /><div v-else class="assignment-list"><label v-for="role in roles" :key="role.id" class="assignment-row"><a-checkbox :checked="user.roleIds.includes(role.id)" :disabled="!canEdit || busy || !role.enabled" @change="changeRole(role.id, !user!.roleIds.includes(role.id))" /><span>{{ role.displayName }} <small>{{ role.roleCode }}</small></span></label></div></a-card>
  </template>
</section></template>
