<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { errorMessage } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import { CloseCircleFilled } from '@ant-design/icons-vue'
import loginBackground from '../../assets/login/blue-wave-background.png'
import axios from 'axios'

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
  } catch (cause) { error.value = axios.isAxiosError(cause)&&cause.response?.status===401?'账号或密码不正确，请重试':errorMessage(cause) }
  finally { busy.value = false }
}
</script>

<template>
  <main class="login-reference" data-ui-template="Specialized" :style="{backgroundImage:`url(${loginBackground})`}">
    <section class="login-reference-brand" aria-label="医院制剂生产管理系统">
      <h1>医院制剂GMP<br/>生产管理系统</h1>
    </section>
    <form class="login-reference-card" @submit.prevent="submit">
      <h2>登录</h2>
      <label for="login-name">账号</label>
      <a-input id="login-name" v-model:value="loginName" autocomplete="username" required />
      <label for="login-password">密码</label>
      <a-input-password id="login-password" v-model:value="password" autocomplete="current-password" required />
      <div class="login-reference-feedback" aria-live="polite"><p v-if="error" role="alert"><CloseCircleFilled/>{{error}}</p></div>
      <a-button type="primary" html-type="submit" block :loading="busy">登录</a-button>
    </form>
  </main>
</template>

<style scoped>
.login-reference{min-height:100svh;display:flex;align-items:center;justify-content:space-between;gap:5vw;padding:7vh 3.7vw 7vh 14.8vw;background-color:#d9ecff;background-position:center;background-size:cover;color:#122747}
.login-reference-brand{flex:1;min-width:0;padding-right:2vw;transform:translateY(5.5vh)}
.login-reference-brand h1{margin:0;color:white;font-size:clamp(32px,4.2vw,64px);font-weight:700;line-height:1.17;letter-spacing:.01em;text-shadow:0 2px 8px #16509b66}
.login-reference-card{width:39.6vw;max-width:608px;min-height:76vh;flex-shrink:0;border:1px solid #fff;border-radius:10px;padding:clamp(28px,3.9vw,60px);background:#ffffffd9;box-shadow:0 8px 20px #2967b02b;backdrop-filter:blur(12px);display:grid;align-content:center;gap:0;transform:translateY(5.5vh)}
.login-reference-card h2{font-size:clamp(28px,3.2vw,48px);font-weight:700;margin:0 0 clamp(24px,2.4vw,36px);line-height:1.25}
.login-reference-card label{font-size:clamp(13px,1.3vw,18px);margin-bottom:8px;color:#42536c}
.login-reference-card label[for=login-password]{margin-top:clamp(20px,2vw,30px)}
#app .login-reference-card :deep(.ant-input),#app .login-reference-card :deep(.ant-input-affix-wrapper){height:clamp(40px,4vw,60px);border-color:#d6e0ed;border-radius:5px;font-size:clamp(14px,1.3vw,18px);background:#fff}
#app .login-reference-card :deep(.ant-input-affix-wrapper .ant-input){height:auto;min-height:0;padding:0;background:transparent}
.login-reference-feedback{min-height:clamp(44px,4.8vw,72px);display:flex;align-items:center}
.login-reference-feedback p{margin:0;display:flex;gap:8px;align-items:center;color:#d84444;font-size:clamp(12px,1.2vw,17px)}
#app .login-reference-card :deep(.ant-btn-primary:not(:disabled):not(.ant-btn-dangerous)){height:clamp(44px,4.4vw,66px);border-radius:5px;background:#084492;border-color:#084492;box-shadow:0 3px 9px #08449233;font-size:clamp(15px,1.5vw,21px);letter-spacing:.15em}
#app .login-reference-card :deep(.ant-btn-primary:not(:disabled):hover){background:#1263bb;border-color:#1263bb}
@media(max-width:700px){.login-reference{padding:32px 20px;flex-direction:column;justify-content:center;gap:32px}.login-reference-brand{flex:none;padding:0;width:min(100%,420px);transform:none}.login-reference-brand h1{font-size:30px}.login-reference-card{width:min(100%,420px);padding:28px;min-height:0;transform:none}.login-reference-card h2{font-size:28px;margin-bottom:24px}}
</style>
