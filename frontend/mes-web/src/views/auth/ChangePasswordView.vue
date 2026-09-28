<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, errorMessage } from '../../api/client'
import { useAuthStore } from '../../stores/auth'

const oldPassword = ref('')
const newPassword = ref('')
const error = ref('')
const busy = ref(false)
const auth = useAuthStore()
const router = useRouter()

async function submit() {
  if (newPassword.value.length < 12) { error.value = '新密码至少 12 个字符'; return }
  busy.value = true
  error.value = ''
  try {
    await api<void>({ method: 'POST', url: '/api/v1/auth/change-password', data: { oldPassword: oldPassword.value, newPassword: newPassword.value } })
    oldPassword.value = ''; newPassword.value = ''
    // The backend revokes the current session after a password change.
    auth.clear()
    await router.replace('/login')
  } catch (cause) { error.value = errorMessage(cause) }
  finally { busy.value = false }
}
</script>

<template><section class="admin-card password-card"><h1>修改密码</h1><p>首次登录或密码重置后，请先设置新密码。</p>
  <form @submit.prevent="submit">
    <label>当前密码<input v-model="oldPassword" type="password" autocomplete="current-password" required /></label>
    <label>新密码<input v-model="newPassword" type="password" autocomplete="new-password" minlength="12" required /></label>
    <p v-if="error" class="form-error" role="alert">{{ error }}</p>
    <button class="primary-button" :disabled="busy" type="submit">保存新密码</button>
  </form>
</section></template>
