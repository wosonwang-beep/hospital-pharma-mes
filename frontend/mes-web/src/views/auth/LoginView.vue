<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { errorMessage } from '../../api/http'
import { useAuthStore } from '../../stores/auth'

const loginName = ref('')
const password = ref('')
const busy = ref(false)
const error = ref('')
const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

async function submit() {
  busy.value = true; error.value = ''
  try {
    const identity = await auth.login(loginName.value.trim(), password.value)
    password.value = ''
    await router.replace(identity?.mustChangePassword ? '/change-password' : String(route.query.redirect || '/'))
  } catch (cause) { error.value = errorMessage(cause) }
  finally { busy.value = false }
}
</script>

<template><main class="auth-page" data-ui-template="Specialized"><form class="auth-card" @submit.prevent="submit">
  <div class="auth-mark">+</div><h1>医院制剂 MES</h1><p>使用受控院内员工账号登录</p>
  <label>账号<a-input v-model:value="loginName" autocomplete="username" required /></label>
  <label>密码<a-input-password v-model:value="password" autocomplete="current-password" required /></label>
  <a-alert v-if="error" type="error" :message="error" show-icon />
  <a-button type="primary" html-type="submit" block :loading="busy">登录</a-button>
</form></main></template>
