import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import './styles.css'
import './list-edit.css'
import './layout-fixes.css'
import './global-ui-v2.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

const pinia = createPinia()
createApp(App).use(pinia).use(router).use(Antd).mount('#app')
