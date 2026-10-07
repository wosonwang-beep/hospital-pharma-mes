<script setup lang="ts">
import { computed, ref } from 'vue'
import { navigationHome, permittedNavigation, selectedNavigationKey } from './navigation'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import ExecutionReferenceNavigation from './ExecutionReferenceNavigation.vue'
import {
  AppstoreOutlined,
  HomeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SearchOutlined,
  BellOutlined,
  DownOutlined,
  UserOutlined
} from '@ant-design/icons-vue'

const collapsed = ref(window.matchMedia('(max-width: 900px)').matches)
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const title = computed(() => String(route.meta.title ?? '工作台'))
const navigationGroups = computed(() => permittedNavigation(auth.can))
const selectedKey = computed(() => selectedNavigationKey(route.path, navigationGroups.value))
const selectedKeys = computed(() => selectedKey.value ? [selectedKey.value] : [])
const batchReference = computed(()=>route.name==='production-batches-view')
const executionWorkbench = computed(() => route.name === 'execution-execution'||batchReference.value)

function navigate({ key }: { key: string }) {
  void router.push({ name: key })
}
async function logout() { await auth.logout(); await router.replace('/login') }
</script>

<template>
  <a-layout class="shell" :class="{'execution-desktop-shell':executionWorkbench,'batch-reference-shell':batchReference}">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible :width="executionWorkbench?180:228" theme="light" class="sidebar">
      <div class="brand" :class="{ 'brand-collapsed': collapsed }">
        <span class="brand-mark">+</span><span>{{ collapsed ? '' : '医院制剂 MES' }}</span>
      </div>
      <ExecutionReferenceNavigation v-if="executionWorkbench&&!collapsed" :groups="navigationGroups" :home="navigationHome" :selected-key="selectedKey" @navigate="navigate"/>
      <a-menu mode="inline" :selectedKeys="selectedKeys" class="nav" :class="{'execution-original-navigation':executionWorkbench&&!collapsed}" @click="navigate">
        <a-menu-item :key="navigationHome.key"><HomeOutlined />{{navigationHome.title}}</a-menu-item>
        <a-menu-item-group v-for="group in navigationGroups" :key="group.key" :title="group.title"><a-menu-item v-for="item in group.items" :key="item.key" :aria-label="item.key==='users'||item.key==='roles'?item.title:undefined" :class="{'ant-menu-item-selected':selectedKey===item.key}"><AppstoreOutlined/>{{item.title}}</a-menu-item></a-menu-item-group>
      </a-menu>
    </a-layout-sider>
    <a-layout class="main-shell" :class="{ 'main-shell-collapsed': collapsed }">
      <a-layout-header class="header" :class="{ 'header-collapsed': collapsed }">
        <div class="header-title">
          <a-dropdown placement="bottomLeft" class="mobile-navigation" :trigger="['click']">
            <a-button type="text" aria-label="打开导航"><AppstoreOutlined /></a-button>
            <template #overlay>
              <a-menu :selectedKeys="selectedKeys" @click="navigate">
                <a-menu-item :key="navigationHome.key"><HomeOutlined />{{navigationHome.title}}</a-menu-item>
                <a-menu-item-group v-for="group in navigationGroups" :key="group.key" :title="group.title"><a-menu-item v-for="item in group.items" :key="item.key">{{item.title}}</a-menu-item></a-menu-item-group>
              </a-menu>
            </template>
          </a-dropdown>
          <a-button type="text" class="sidebar-trigger" :aria-label="collapsed ? '展开侧栏' : '折叠侧栏'" :aria-expanded="!collapsed" @click="collapsed = !collapsed">
            <MenuUnfoldOutlined v-if="collapsed" />
            <MenuFoldOutlined v-else />
          </a-button>
          <a-breadcrumb v-if="executionWorkbench" class="execution-header-breadcrumb"><a-breadcrumb-item>首页</a-breadcrumb-item><a-breadcrumb-item>生产管理</a-breadcrumb-item><a-breadcrumb-item>{{batchReference?'生产批次':'生产执行'}}</a-breadcrumb-item></a-breadcrumb>
          <span>{{ title }}</span><span class="environment-chip">GMP · 验证环境</span>
        </div>
        <div v-if="executionWorkbench" class="execution-header-tools"><a-tooltip title="当前契约未提供全局搜索"><a-input disabled placeholder="搜索工单、批号、物料…" class="execution-header-search"><template #prefix><SearchOutlined/></template></a-input></a-tooltip><a-tooltip title="系统运行正常"><BellOutlined class="execution-system-icon"/></a-tooltip><a-dropdown><a-button type="text" class="execution-user"><a-avatar size="small"><UserOutlined/></a-avatar>{{auth.identity?.displayName}}<DownOutlined/></a-button><template #overlay><a-menu><a-menu-item @click="router.push('/change-password')">改密</a-menu-item><a-menu-item @click="logout">退出</a-menu-item></a-menu></template></a-dropdown></div>
        <div class="user" :class="{'execution-original-user':executionWorkbench}"><span class="status-dot" />{{executionWorkbench?'系统运行正常':'已登录'}}<a-avatar><UserOutlined /></a-avatar><span>{{ auth.identity?.displayName }}</span><button class="header-link" @click="router.push('/change-password')">改密</button><button class="header-link" @click="logout">退出</button></div>
      </a-layout-header>
      <a-layout-content class="content">
        <a-breadcrumb><a-breadcrumb-item><HomeOutlined /> 首页</a-breadcrumb-item><a-breadcrumb-item>{{ title }}</a-breadcrumb-item></a-breadcrumb>
        <RouterView />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.sidebar :deep(.ant-layout-sider-children){display:flex;flex-direction:column;height:100%}
.sidebar .brand{flex-shrink:0}
.sidebar .nav{flex:1;min-height:0;overflow-y:auto}
.execution-header-breadcrumb{display:none}
.execution-header-tools{display:none}
/* PC execution reference only. Other routes and mobile shell retain their baseline. */
@media(min-width:901px){
 #app .execution-desktop-shell{--mes-ui-sidebar-width:180px;--mes-ui-header-height:38px}
 #app .execution-desktop-shell .brand{font-size:14px;padding:0 20px;gap:8px}
 #app .execution-desktop-shell .brand-mark{width:24px;height:24px;font-size:22px;border-radius:5px}
 #app .execution-desktop-shell .sidebar-trigger{height:24px;width:20px;min-height:24px;font-size:14px!important;padding:0}
 #app .execution-desktop-shell .header{padding-inline:16px!important}
 #app .execution-desktop-shell .header-title{gap:8px}
 #app .execution-desktop-shell .execution-original-navigation,#app .execution-desktop-shell .execution-original-user{display:none}
 .execution-header-tools{display:flex;align-items:center;gap:20px}
 #app .execution-desktop-shell :deep(.execution-header-search){width:228px;height:28px;min-height:28px;background:white;font-size:12px}
 .execution-system-icon{font-size:17px;color:#536881}
 #app .execution-desktop-shell .execution-user{height:32px;display:flex;align-items:center;gap:8px;font-size:12px;font-weight:600}
 #app .execution-desktop-shell :deep(.execution-user .ant-avatar){background:#e8f3ff;color:#1677ff}
 #app .execution-desktop-shell :deep(.execution-header-breadcrumb){display:block;margin:0;font-weight:400}
 #app .execution-desktop-shell .header-title>span{display:none}
 #app .execution-desktop-shell>.main-shell>.content{padding:12px 18px 12px 11px}
 #app .execution-desktop-shell>.main-shell>.content>:deep(.ant-breadcrumb){display:none}
 #app .execution-desktop-shell :deep(.execution-workbench){margin-top:0}
}
@media(min-width:901px){#app .batch-reference-shell>.main-shell>.content{padding:12px 14px 12px 18px}}
</style>
