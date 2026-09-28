<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { canUseRoute } from '../auth/permissions'
import {
  HomeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined
} from '@ant-design/icons-vue'

const collapsed = ref(false)
const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const canUsers = computed(() => canUseRoute(auth.identity?.permissionCodes || [], 'menu:iam:users'))
const canRoles = computed(() => canUseRoute(auth.identity?.permissionCodes || [], 'menu:iam:roles'))
const title = computed(() => ({ users: '用户管理', 'user-new': '新增用户', 'user-view': '查看用户', 'user-edit': '编辑用户', roles: '角色与权限', 'role-new': '新增角色', 'role-view': '查看角色', 'role-edit': '编辑角色', 'change-password': '修改密码' }[String(route.name)] || '工作台'))
const selectedMenu = computed(() => route.path.startsWith('/admin/users') ? 'users' : route.path.startsWith('/admin/roles') ? 'roles' : 'dashboard')
async function logout() { await auth.logout(); await router.replace('/login') }
</script>

<template>
  <a-layout class="shell">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible width="268" theme="light" class="sidebar">
      <div class="brand" :class="{ 'brand-collapsed': collapsed }">
        {{ collapsed ? 'MES' : '医院制剂生产管理系统' }}
      </div>
      <a-menu mode="inline" :selectedKeys="[selectedMenu]" class="nav" @click="({ key }: { key: string }) => router.push({ name: key })">
        <a-menu-item key="dashboard"><HomeOutlined />首页</a-menu-item>
        <a-menu-item v-if="canUsers" key="users"><UserOutlined />用户管理</a-menu-item>
        <a-menu-item v-if="canRoles" key="roles">角色与权限</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <div class="header-title">
          <a-button type="text" class="sidebar-trigger" :aria-label="collapsed ? '展开侧栏' : '折叠侧栏'" :aria-expanded="!collapsed" @click="collapsed = !collapsed">
            <MenuUnfoldOutlined v-if="collapsed" />
            <MenuFoldOutlined v-else />
          </a-button>
          <span>{{ title }}</span>
        </div>
        <div class="user"><a-avatar><UserOutlined /></a-avatar><span>{{ auth.identity?.displayName }}</span><button class="text-button" @click="router.push('/change-password')">改密</button><button class="text-button" @click="logout">退出</button></div>
      </a-layout-header>
      <a-layout-content class="content">
        <a-breadcrumb><a-breadcrumb-item><HomeOutlined /> 首页</a-breadcrumb-item><a-breadcrumb-item>{{ title }}</a-breadcrumb-item></a-breadcrumb>
        <RouterView />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
