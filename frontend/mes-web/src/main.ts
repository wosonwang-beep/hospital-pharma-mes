import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import './styles.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { injectedPlatformAuthSnapshot, usePlatformAuthContext } from './auth/PlatformAuthContext'

const pinia = createPinia()
const snapshot = injectedPlatformAuthSnapshot()
if (snapshot) usePlatformAuthContext(pinia).setSnapshot(snapshot)
createApp(App).use(pinia).use(router).use(Antd).mount('#app')
