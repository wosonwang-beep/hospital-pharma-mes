<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { errorMessage } from '../../api/client'
import { useAuthStore } from '../../stores/auth'

const loginName = ref('')
const password = ref('')
const error = ref('')
const busy = ref(false)
const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

async function submit() {
  busy.value = true
  error.value = ''
  try {
    const identity = await auth.login(loginName.value, password.value)
    password.value = ''
    await router.replace(identity?.mustChangePassword ? '/change-password' : String(route.query.redirect || '/'))
  } catch (cause) { error.value = errorMessage(cause) }
  finally { busy.value = false }
}
</script>

<template><main class="auth-page"><form class="auth-card" @submit.prevent="submit">
  <div class="auth-mark">MES</div><h1>医院制剂生产管理系统</h1><p>使用院内员工账号登录</p>
  <label>账号<input v-model.trim="loginName" autocomplete="username" required /></label>
  <label>密码<input v-model="password" type="password" autocomplete="current-password" required /></label>
  <p v-if="error" class="form-error" role="alert">{{ error }}</p>
  <button class="primary-button" :disabled="busy" type="submit">{{ busy ? '登录中…' : '登录' }}</button>
</form></main></template>
