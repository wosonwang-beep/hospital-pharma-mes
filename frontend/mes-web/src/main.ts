import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import './styles.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { setSessionLostHandler } from './api/client'
import { useAuthStore } from './stores/auth'
const pinia = createPinia()
setSessionLostHandler(() => { useAuthStore(pinia).clear(); void router.replace('/login') })
createApp(App).use(pinia).use(router).use(Antd).mount('#app')
