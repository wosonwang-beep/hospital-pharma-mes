<script setup lang="ts">
import { ref } from 'vue'
import {
  AppstoreOutlined,
  DatabaseOutlined,
  DesktopOutlined,
  DownOutlined,
  HomeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SettingOutlined,
  UserOutlined
} from '@ant-design/icons-vue'

const collapsed = ref(false)
</script>

<template>
  <a-layout class="shell">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible width="268" theme="light" class="sidebar">
      <div class="brand" :class="{ 'brand-collapsed': collapsed }">
        {{ collapsed ? 'MES' : '医院制剂生产管理系统' }}
      </div>
      <a-menu mode="inline" :selectedKeys="['home']" class="nav">
        <a-menu-item key="home"><HomeOutlined />首页</a-menu-item>
        <a-menu-item key="desk"><AppstoreOutlined />工作台</a-menu-item>
        <a-sub-menu key="system"><template #icon><SettingOutlined /></template><template #title>系统管理</template></a-sub-menu>
        <a-sub-menu key="base"><template #icon><DatabaseOutlined /></template><template #title>基础配置</template></a-sub-menu>
        <a-sub-menu key="monitor"><template #icon><DesktopOutlined /></template><template #title>系统监控</template></a-sub-menu>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <div class="header-title">
          <a-button type="text" class="sidebar-trigger" :aria-label="collapsed ? '展开侧栏' : '折叠侧栏'" :aria-expanded="!collapsed" @click="collapsed = !collapsed">
            <MenuUnfoldOutlined v-if="collapsed" />
            <MenuFoldOutlined v-else />
          </a-button>
          <span>工作台</span>
        </div>
        <div class="user"><a-avatar><UserOutlined /></a-avatar><span>Administrator</span><DownOutlined /></div>
      </a-layout-header>
      <a-layout-content class="content">
        <a-breadcrumb><a-breadcrumb-item><HomeOutlined /> 首页</a-breadcrumb-item><a-breadcrumb-item>工作台</a-breadcrumb-item></a-breadcrumb>
        <RouterView />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
