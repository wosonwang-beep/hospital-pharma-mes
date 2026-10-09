<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import DatabaseMenu from './DatabaseMenu.vue'
import { menuLeaves, menuAncestors, selectedMenu, type MenuNode } from './databaseNavigation'
import { api, errorMessage } from '../api/http'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { message } from 'ant-design-vue'
import dashboardLogo from '../assets/login/hospital-flower-mark.png'
import {
  AppstoreOutlined,
  HomeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SearchOutlined,
  BellOutlined,
  DownOutlined,
  UserOutlined,
  SettingOutlined, QuestionCircleOutlined, HomeFilled, CodeSandboxOutlined, MedicineBoxOutlined, AuditOutlined, ClusterOutlined, ReconciliationOutlined
} from '@ant-design/icons-vue'

const collapsed = ref(window.matchMedia('(max-width: 900px)').matches)
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const loggingOut = ref(false)
watch(() => auth.identity, (identity, previous) => {
  if (previous && !identity && !loggingOut.value) {
    void router.replace({ path: '/login', query: { redirect: route.fullPath } })
  }
})
const title = computed(() => String(route.meta.title ?? '工作台'))
const navigationNodes = ref<MenuNode[]>([]), navigationError = ref('')
let navigationGeneration = 0
async function loadNavigation() {
  const generation = ++navigationGeneration
  if (!auth.identity) { navigationNodes.value = []; return }
  try { const nodes = await api<MenuNode[]>({ url: '/auth/navigation' }); if (generation === navigationGeneration) { navigationNodes.value = nodes; navigationError.value = '' } }
  catch (cause) { if (generation === navigationGeneration) { navigationNodes.value = []; navigationError.value = errorMessage(cause) } }
}
watch(() => auth.identity, loadNavigation, { immediate: true })
onMounted(() => window.addEventListener('mes-navigation-changed', loadNavigation))
onUnmounted(() => { ++navigationGeneration; window.removeEventListener('mes-navigation-changed', loadNavigation) })
const selectedKey = computed(() => selectedMenu(route.path, navigationNodes.value))
const selectedKeys = computed(() => selectedKey.value ? [selectedKey.value] : [])
const batchReference = computed(()=>route.name==='production-batches-view')
const executionWorkbench = computed(() => route.name === 'execution-execution'||batchReference.value)
const dashboard = computed(()=>route.name==='dashboard')
const dashboardSearch = ref('')
const dashboardMenuOptions = computed(()=>dashboardSearch.value.trim()?menuLeaves(navigationNodes.value).filter(item=>item.title.includes(dashboardSearch.value.trim())).map(item=>({value:item.path,label:item.title})):[])
const openGroups = ref<string[]>([])
watch([selectedKey, navigationNodes], ([key, nodes]) => {
  const allIds = new Set<string>(); const visit = (items: MenuNode[]) => items.forEach(item => { allIds.add(item.id); visit(item.children) }); visit(nodes)
  openGroups.value = [...new Set([...openGroups.value.filter(value => allIds.has(value)), ...menuAncestors(key,nodes)])]
}, { immediate: true })

function navigate({ key }: { key: string }) {
  const destination = menuLeaves(navigationNodes.value).find(item => item.id === key)?.path
  if (destination) void router.push(destination)
}
async function logout() {
  loggingOut.value = true
  try { await auth.logout() }
  catch { message.warning('本机已退出，服务器会话注销未完成') }
  finally {
    try { await router.replace('/login') }
    finally { loggingOut.value = false }
  }
}
function selectDashboardMenu(value:unknown){dashboardSearch.value='';void router.push(String(value))}
</script>

<template>
  <a-layout class="shell global-navigation-shell" :class="{'execution-desktop-shell':executionWorkbench,'batch-reference-shell':batchReference,'dashboard-desktop-shell':dashboard}">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible :width="256" theme="light" class="sidebar">
      <div class="brand" :class="{ 'brand-collapsed': collapsed }">
        <img :src="dashboardLogo" class="dashboard-brand-logo" alt=""/><span v-if="!collapsed" class="dashboard-brand-copy"><strong>医院制剂生产管理系统</strong><small>Hospital Pharma MES</small></span>
      </div>
      <a-menu mode="inline" v-model:open-keys="openGroups" :selectedKeys="selectedKeys" class="nav" @click="navigate">
        <DatabaseMenu :nodes="navigationNodes" />
      </a-menu>
    </a-layout-sider>
    <a-layout class="main-shell" :class="{ 'main-shell-collapsed': collapsed }">
      <a-layout-header class="header" :class="{ 'header-collapsed': collapsed }">
        <div class="header-title" :class="{'dashboard-header-title':dashboard}">
          <a-dropdown placement="bottomLeft" class="mobile-navigation" :trigger="['click']">
            <a-button type="text" aria-label="打开导航"><AppstoreOutlined /></a-button>
            <template #overlay>
              <a-menu :selectedKeys="selectedKeys" @click="navigate">
                <DatabaseMenu :nodes="navigationNodes" />
              </a-menu>
            </template>
          </a-dropdown>
          <a-button type="text" class="sidebar-trigger" :aria-label="collapsed ? '展开侧栏' : '折叠侧栏'" :aria-expanded="!collapsed" @click="collapsed = !collapsed">
            <MenuUnfoldOutlined v-if="collapsed" />
            <MenuFoldOutlined v-else />
          </a-button>
          <a-auto-complete v-model:value="dashboardSearch" class="dashboard-menu-search" :options="dashboardMenuOptions" @select="selectDashboardMenu"><a-input placeholder="搜索菜单…" aria-label="搜索可访问菜单"><template #prefix><SearchOutlined/></template></a-input></a-auto-complete>

        </div>

        <div class="dashboard-header-user"><a-tooltip title="暂无通知数据"><BellOutlined/></a-tooltip><a-tooltip title="业务入口按当前账号权限显示"><QuestionCircleOutlined/></a-tooltip><a-avatar :size="40"><UserOutlined/></a-avatar><a-dropdown :trigger="['click']"><a-button type="text" class="dashboard-account"><span><strong>{{auth.identity?.displayName}}</strong><small>{{auth.identity?.loginName}}</small></span><DownOutlined/></a-button><template #overlay><a-menu><a-menu-item @click="router.push('/change-password')">修改密码</a-menu-item><a-menu-item @click="logout">退出登录</a-menu-item></a-menu></template></a-dropdown></div>
        <div class="user mobile-header-user"><span class="status-dot" />已登录<a-avatar><UserOutlined /></a-avatar><span>{{ auth.identity?.displayName }}</span><button class="header-link" @click="router.push('/change-password')">改密</button><button class="header-link" @click="logout">退出</button></div>
      </a-layout-header>
      <a-layout-content class="content">
        <a-alert v-if="navigationError" type="error" :message="`菜单加载失败：${navigationError}`" show-icon><template #action><a-button size="small" @click="loadNavigation">重试</a-button></template></a-alert>
        <a-breadcrumb><a-breadcrumb-item><HomeOutlined /> 首页</a-breadcrumb-item><a-breadcrumb-item>{{ title }}</a-breadcrumb-item></a-breadcrumb>
        <RouterView v-if="auth.identity" />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.sidebar :deep(.ant-layout-sider-children){display:flex;flex-direction:column;height:100%}
.sidebar .brand{flex-shrink:0}
.sidebar .nav{flex:1;min-height:0;overflow-y:auto}
@media(min-width:901px){
 #app .execution-desktop-shell>.main-shell>.content{padding:12px 18px 12px 11px}
 #app .execution-desktop-shell>.main-shell>.content>:deep(.ant-breadcrumb){display:none}
 #app .execution-desktop-shell :deep(.execution-workbench){margin-top:0}
}
@media(min-width:901px){#app .batch-reference-shell>.main-shell>.content{padding:12px 14px 12px 18px}}
</style>
<style scoped src="./dashboard-shell.css"/>
