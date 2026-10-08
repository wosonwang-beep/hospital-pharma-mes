<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { errorMessage } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import { CloseCircleFilled, SafetyCertificateOutlined } from '@ant-design/icons-vue'
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
  busy.value = true
  error.value = ''
  try {
    const identity = await auth.login(loginName.value.trim(), password.value)
    password.value = ''
    await router.replace(identity?.mustChangePassword ? '/change-password' : String(route.query.redirect || '/'))
  } catch (cause) {
    error.value = axios.isAxiosError(cause) && cause.response?.status === 401
      ? '账号或密码不正确，请重试'
      : errorMessage(cause)
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <main class="login-shell" data-ui-template="Specialized">
    <section class="login-brand" :style="{ backgroundImage: `linear-gradient(135deg,rgba(9,54,112,.88),rgba(20,105,196,.58)),url(${loginBackground})` }">
      <div class="login-brand__content">
        <div class="login-brand__badge"><SafetyCertificateOutlined /> GMP MES</div>
        <h1>医院制剂 MES</h1>
        <div class="login-brand__system">生产管理系统</div>
        <p>覆盖物料、生产、质量、电子批记录与批放行的医院制剂数字化执行平台。</p>
        <div class="login-brand__tags">
          <span>生产执行</span>
          <span>质量管理</span>
          <span>电子批记录</span>
        </div>
      </div>
      <div class="login-brand__footer">Hospital Pharmaceutical Manufacturing Execution System</div>
    </section>

    <section class="login-panel" aria-label="系统登录">
      <form class="login-card" @submit.prevent="submit">
        <div class="login-card__eyebrow">WELCOME BACK</div>
        <h2>登录系统</h2>
        <p class="login-card__subtitle">请输入您的系统账号和密码</p>

        <label for="login-name">账号</label>
        <a-input id="login-name" v-model:value="loginName" autocomplete="username" placeholder="请输入账号" required />

        <label for="login-password">密码</label>
        <a-input-password id="login-password" v-model:value="password" autocomplete="current-password" placeholder="请输入密码" required />

        <div class="login-feedback" aria-live="polite">
          <p v-if="error" role="alert"><CloseCircleFilled />{{ error }}</p>
        </div>

        <a-button type="primary" html-type="submit" block :loading="busy">登录</a-button>
        <div class="login-card__note">仅限授权人员访问</div>
      </form>
    </section>
  </main>
</template>

<style scoped>
.login-shell{min-height:100svh;display:grid;grid-template-columns:minmax(0,1.15fr) minmax(520px,.85fr);background:#f7f9fc;color:#15233a}
.login-brand{position:relative;overflow:hidden;display:flex;flex-direction:column;justify-content:center;min-height:100svh;padding:8vh 8vw;background-position:center;background-size:cover;color:#fff}
.login-brand::after{content:"";position:absolute;inset:auto -12vw -22vh 4vw;height:46vh;border:1px solid #ffffff26;border-radius:50%;transform:rotate(-8deg)}
.login-brand__content{position:relative;z-index:1;max-width:680px}
.login-brand__badge{display:inline-flex;align-items:center;gap:8px;padding:8px 12px;border:1px solid #ffffff4a;border-radius:999px;background:#ffffff16;backdrop-filter:blur(10px);font-size:13px;font-weight:600;letter-spacing:.08em}
.login-brand h1{margin:28px 0 4px;font-size:clamp(46px,4.5vw,72px);line-height:1.08;letter-spacing:-.03em;font-weight:700}
.login-brand__system{margin-bottom:22px;font-size:clamp(30px,2.8vw,46px);line-height:1.2;font-weight:500;letter-spacing:.02em;color:#eff6ff}
.login-brand p{max-width:610px;margin:0;color:#eaf3ff;font-size:17px;line-height:1.8}
.login-brand__tags{display:flex;flex-wrap:wrap;gap:10px;margin-top:34px}
.login-brand__tags span{padding:8px 12px;border-radius:8px;background:#ffffff14;border:1px solid #ffffff24;font-size:13px;color:#eef6ff}
.login-brand__footer{position:absolute;z-index:1;left:8vw;bottom:4vh;color:#dceafe;font-size:12px;letter-spacing:.06em}
.login-panel{display:flex;align-items:center;justify-content:center;padding:64px 56px;background:linear-gradient(180deg,#fbfcfe 0%,#f5f8fc 100%)}
.login-card{width:min(100%,420px)}
.login-card__eyebrow{margin-bottom:12px;color:#7b8ba5;font-size:12px;font-weight:700;letter-spacing:.16em}
.login-card h2{margin:0;color:#17243a;font-size:36px;line-height:1.25;font-weight:700;letter-spacing:-.02em}
.login-card__subtitle{margin:10px 0 34px;color:#7a889f;font-size:14px}
.login-card label{display:block;margin:0 0 8px;color:#47556d;font-size:14px;font-weight:500}
.login-card label[for=login-password]{margin-top:20px}
#app .login-card :deep(.ant-input),#app .login-card :deep(.ant-input-affix-wrapper){height:48px;border-color:#d9e1ec;border-radius:8px;background:#fff;font-size:15px;box-shadow:none}
#app .login-card :deep(.ant-input-affix-wrapper .ant-input){height:auto;padding:0;background:transparent}
#app .login-card :deep(.ant-input:hover),#app .login-card :deep(.ant-input-affix-wrapper:hover){border-color:#7aa9df}
#app .login-card :deep(.ant-input:focus),#app .login-card :deep(.ant-input-affix-wrapper-focused){border-color:#2f6fb3;box-shadow:0 0 0 3px #2f6fb31a}
.login-feedback{min-height:46px;display:flex;align-items:center}
.login-feedback p{margin:0;display:flex;align-items:center;gap:7px;color:#cf3f3f;font-size:13px}
#app .login-card :deep(.ant-btn-primary:not(:disabled):not(.ant-btn-dangerous)){height:48px;border-radius:8px;background:#1f5f9f;border-color:#1f5f9f;box-shadow:0 8px 18px #1f5f9f24;font-size:15px;font-weight:600}
#app .login-card :deep(.ant-btn-primary:not(:disabled):hover){background:#174f86;border-color:#174f86}
.login-card__note{margin-top:18px;text-align:center;color:#9aa6b8;font-size:12px}
@media(max-width:960px){.login-shell{grid-template-columns:1fr}.login-brand{display:none}.login-panel{min-height:100svh;padding:36px 24px}.login-card{max-width:440px}}
</style>
