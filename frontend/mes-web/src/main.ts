import { createApp } from 'vue'
import 'ant-design-vue/dist/reset.css'
import './styles.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { setSessionLostHandler } from './api/client'
import { useAuthStore } from './stores/auth'
import { registerAntDesign } from './ui/antDesign'
const pinia = createPinia()
setSessionLostHandler(() => { useAuthStore(pinia).clear(); void router.replace('/login') })
registerAntDesign(createApp(App).use(pinia).use(router)).mount('#app')
