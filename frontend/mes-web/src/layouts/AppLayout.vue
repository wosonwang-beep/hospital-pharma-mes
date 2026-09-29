<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePlatformAuthContext } from '../auth/PlatformAuthContext'
import { useAuthStore } from '../stores/auth'
import {
  AppstoreOutlined,
  DesktopOutlined,
  HomeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined
} from '@ant-design/icons-vue'

const collapsed = ref(window.matchMedia('(max-width: 900px)').matches)
const route = useRoute()
const router = useRouter()
const authorization = usePlatformAuthContext()
const auth = useAuthStore()
const title = computed(() => String(route.meta.title ?? '工作台'))
const selectedKeys = computed(() => [String(route.name ?? 'dashboard')])
const usersSelected = computed(() => route.path.startsWith('/admin/users'))
const rolesSelected = computed(() => route.path.startsWith('/admin/roles'))

function navigate({ key }: { key: string }) {
  void router.push({ name: key })
}
async function logout() { await auth.logout(); await router.replace('/login') }
</script>

<template>
  <a-layout class="shell">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible width="268" theme="light" class="sidebar">
      <div class="brand" :class="{ 'brand-collapsed': collapsed }">
        <span class="brand-mark">+</span><span>{{ collapsed ? '' : '医院制剂 MES' }}</span>
      </div>
      <a-menu mode="inline" :selectedKeys="selectedKeys" class="nav" @click="navigate">
        <a-menu-item key="dashboard"><HomeOutlined />首页</a-menu-item>
        <a-menu-item v-if="auth.can('iam:user:view')" key="users" aria-label="用户管理" :class="{ 'ant-menu-item-selected': usersSelected }"><UserOutlined />用户管理</a-menu-item>
        <a-menu-item v-if="auth.can('iam:role:view')" key="roles" aria-label="角色与权限" :class="{ 'ant-menu-item-selected': rolesSelected }"><AppstoreOutlined />角色与权限</a-menu-item>
        <a-menu-item v-if="authorization.can('audit:view')" key="audit"><AppstoreOutlined />GMP Audit Trail</a-menu-item>
        <a-menu-item v-if="authorization.can('integration:view')" key="integration-operations"><DesktopOutlined />Integration Operations</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout class="main-shell" :class="{ 'main-shell-collapsed': collapsed }">
      <a-layout-header class="header">
        <div class="header-title">
          <a-button type="text" class="sidebar-trigger" :aria-label="collapsed ? '展开侧栏' : '折叠侧栏'" :aria-expanded="!collapsed" @click="collapsed = !collapsed">
            <MenuUnfoldOutlined v-if="collapsed" />
            <MenuFoldOutlined v-else />
          </a-button>
          <span>{{ title }}</span><span class="environment-chip">GMP · 验证环境</span>
        </div>
        <div class="user"><span class="status-dot" />系统运行正常<a-avatar><UserOutlined /></a-avatar><span>{{ auth.identity?.displayName }}</span><button class="header-link" @click="router.push('/change-password')">改密</button><button class="header-link" @click="logout">退出</button></div>
      </a-layout-header>
      <a-layout-content class="content">
        <a-breadcrumb><a-breadcrumb-item><HomeOutlined /> 首页</a-breadcrumb-item><a-breadcrumb-item>{{ title }}</a-breadcrumb-item></a-breadcrumb>
        <RouterView />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
