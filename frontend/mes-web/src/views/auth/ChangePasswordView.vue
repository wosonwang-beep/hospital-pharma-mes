<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, errorMessage } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
const confirmedPassword=ref('')
const oldPassword = ref(''); const newPassword = ref(''); const error = ref(''); const busy = ref(false)
const auth = useAuthStore(); const router = useRouter()
async function submit() {
  if (newPassword.value.length < 12) { error.value = '新密码至少 12 个字符'; return }
  if(newPassword.value!==confirmedPassword.value){error.value='两次输入的新密码不一致';return}
  busy.value = true; error.value = ''
  try {
    await api<void>({ method: 'POST', url: '/auth/change-password', data: { oldPassword: oldPassword.value, newPassword: newPassword.value } })
    auth.clear(); await router.replace('/login')
  } catch (cause) { error.value = errorMessage(cause) }
  finally { busy.value = false }
}
</script>
<template><section data-ui-template="T2" class="admin-card password-card"><h1>修改密码</h1><p>首次登录或密码重置后必须先设置新密码。</p>
  <form @submit.prevent="submit"><label><span class="form-field-label">当前密码</span><a-input-password v-model:value="oldPassword" required /></label>
  <label><span class="form-field-label">新密码</span><a-input-password v-model:value="newPassword" minlength="12" required /></label>
  <label><span class="form-field-label">确认新密码</span><a-input-password v-model:value="confirmedPassword" minlength="12" required /></label><p class="muted">新密码至少 12 个字符。</p>
  <a-alert v-if="error" type="error" :message="error" show-icon /><a-button type="primary" html-type="submit" :loading="busy">保存新密码</a-button></form>
</section></template>
