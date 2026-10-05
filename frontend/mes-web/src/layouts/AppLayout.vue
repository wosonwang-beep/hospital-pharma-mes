<script setup lang="ts">
import { computed, ref } from 'vue'
import { resources } from '../master/resources'
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
        <a-menu-item v-for="def in resources.filter(r=>r.key!=='unit-conversions'&&auth.can(`master:${r.permission}:view`))" :key="`master-${def.key}`" :class="{'ant-menu-item-selected':route.path.startsWith(`/master/${def.key}`)}"><AppstoreOutlined />{{def.title}}</a-menu-item>
        <a-menu-item v-if="auth.can('master:product:view')" key="process-products" :class="{'ant-menu-item-selected':route.path.startsWith('/process/products')}"><AppstoreOutlined />产品管理</a-menu-item>
        <a-menu-item v-if="auth.can('process:package:view')" key="process-packages" :class="{'ant-menu-item-selected':route.path.startsWith('/process/packages')}"><AppstoreOutlined />工艺包管理</a-menu-item>
        <a-menu-item v-if="auth.can('qms:plan:view')" key="production-plans-list"><AppstoreOutlined />生产质量计划</a-menu-item>
        <a-menu-item v-if="auth.can('qms:test:view')" key="production-tests-list"><AppstoreOutlined />生产检验</a-menu-item>
        <a-menu-item v-if="auth.can('qms:specification:view')" key="qc-specifications"><AppstoreOutlined />QC质量标准</a-menu-item>
        <a-menu-item v-if="auth.can('qms:inspection-request:view')" key="incoming-inspection-requests-list"><AppstoreOutlined />请验单</a-menu-item>
        <a-menu-item v-if="auth.can('qms:sampling:view')" key="incoming-sampling-tasks-list"><AppstoreOutlined />取样记录</a-menu-item>
        <a-menu-item v-if="auth.can('qms:test:view')" key="incoming-samples-list"><AppstoreOutlined />样品</a-menu-item>
        <a-menu-item v-if="auth.can('qms:test:view')" key="incoming-inspection-tasks-list"><AppstoreOutlined />检验记录</a-menu-item>
        <a-menu-item v-if="auth.can('qms:report:view')" key="incoming-inspection-reports-list"><AppstoreOutlined />检验报告</a-menu-item>
        <a-menu-item v-if="auth.can('qms:deviation:view')" key="incoming-deviations-list"><AppstoreOutlined />质量调查</a-menu-item>
        <a-menu-item v-if="auth.can('production:order:view')" key="production-orders-list"><AppstoreOutlined />生产订单</a-menu-item>
        <a-menu-item v-if="auth.can('production:batch:view')" key="production-batches-list"><AppstoreOutlined />生产批</a-menu-item>
        <a-menu-item v-if="auth.can('trace:view')" key="trace"><AppstoreOutlined />追溯查询</a-menu-item>
        <a-menu-item v-if="auth.can('ebr:template:view')" key="ebr-templates" :class="{'ant-menu-item-selected':route.path.startsWith('/ebr/templates')}"><AppstoreOutlined />eBR 模板</a-menu-item>
        <a-menu-item v-if="auth.can('wms:receipt:view')" key="wms-receipts" :class="{'ant-menu-item-selected':route.path.startsWith('/wms/receipts')}"><AppstoreOutlined />收货单</a-menu-item>
        <a-menu-item v-if="auth.can('wms:issue:view')" key="wms-issues" :class="{'ant-menu-item-selected':route.path.startsWith('/wms/issues')}"><AppstoreOutlined />发料单</a-menu-item>
        <a-menu-item v-if="authorization.can('audit:view')" key="audit"><AppstoreOutlined />GMP Audit Trail</a-menu-item>
        <a-menu-item v-if="authorization.can('integration:view')" key="integration-operations"><DesktopOutlined />Integration Operations</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout class="main-shell" :class="{ 'main-shell-collapsed': collapsed }">
      <a-layout-header class="header" :class="{ 'header-collapsed': collapsed }">
        <div class="header-title">
          <a-dropdown placement="bottomLeft" class="mobile-navigation">
            <a-button type="text" aria-label="打开导航"><AppstoreOutlined /></a-button>
            <template #overlay>
              <a-menu @click="navigate">
                <a-menu-item key="dashboard"><HomeOutlined />首页</a-menu-item>
                <a-menu-item v-if="auth.can('iam:user:view')" key="users"><UserOutlined />用户管理</a-menu-item>
                <a-menu-item v-if="auth.can('iam:role:view')" key="roles"><AppstoreOutlined />角色与权限</a-menu-item>
                <a-menu-item v-for="def in resources.filter(r=>r.key!=='unit-conversions'&&auth.can(`master:${r.permission}:view`))" :key="`master-${def.key}`" :class="{'ant-menu-item-selected':route.path.startsWith(`/master/${def.key}`)}"><AppstoreOutlined />{{def.title}}</a-menu-item>
        <a-menu-item v-if="auth.can('master:product:view')" key="process-products" :class="{'ant-menu-item-selected':route.path.startsWith('/process/products')}"><AppstoreOutlined />产品管理</a-menu-item>
        <a-menu-item v-if="auth.can('process:package:view')" key="process-packages" :class="{'ant-menu-item-selected':route.path.startsWith('/process/packages')}"><AppstoreOutlined />工艺包管理</a-menu-item>
        <a-menu-item v-if="auth.can('qms:plan:view')" key="production-plans-list"><AppstoreOutlined />生产质量计划</a-menu-item>
        <a-menu-item v-if="auth.can('qms:test:view')" key="production-tests-list"><AppstoreOutlined />生产检验</a-menu-item>
        <a-menu-item v-if="auth.can('qms:specification:view')" key="qc-specifications"><AppstoreOutlined />QC质量标准</a-menu-item>
        <a-menu-item v-if="auth.can('qms:inspection-request:view')" key="incoming-inspection-requests-list"><AppstoreOutlined />请验单</a-menu-item>
        <a-menu-item v-if="auth.can('qms:sampling:view')" key="incoming-sampling-tasks-list"><AppstoreOutlined />取样记录</a-menu-item>
        <a-menu-item v-if="auth.can('qms:test:view')" key="incoming-samples-list"><AppstoreOutlined />样品</a-menu-item>
        <a-menu-item v-if="auth.can('qms:test:view')" key="incoming-inspection-tasks-list"><AppstoreOutlined />检验记录</a-menu-item>
        <a-menu-item v-if="auth.can('qms:report:view')" key="incoming-inspection-reports-list"><AppstoreOutlined />检验报告</a-menu-item>
        <a-menu-item v-if="auth.can('qms:deviation:view')" key="incoming-deviations-list"><AppstoreOutlined />质量调查</a-menu-item>
        <a-menu-item v-if="auth.can('production:order:view')" key="production-orders-list"><AppstoreOutlined />生产订单</a-menu-item>
        <a-menu-item v-if="auth.can('production:batch:view')" key="production-batches-list"><AppstoreOutlined />生产批</a-menu-item>
        <a-menu-item v-if="auth.can('trace:view')" key="trace"><AppstoreOutlined />追溯查询</a-menu-item>
        <a-menu-item v-if="auth.can('ebr:template:view')" key="ebr-templates" :class="{'ant-menu-item-selected':route.path.startsWith('/ebr/templates')}"><AppstoreOutlined />eBR 模板</a-menu-item>
        <a-menu-item v-if="auth.can('wms:receipt:view')" key="wms-receipts" :class="{'ant-menu-item-selected':route.path.startsWith('/wms/receipts')}"><AppstoreOutlined />收货单</a-menu-item>
        <a-menu-item v-if="auth.can('wms:issue:view')" key="wms-issues" :class="{'ant-menu-item-selected':route.path.startsWith('/wms/issues')}"><AppstoreOutlined />发料单</a-menu-item>
        <a-menu-item v-if="authorization.can('audit:view')" key="audit"><AppstoreOutlined />GMP Audit Trail</a-menu-item>
                <a-menu-item v-if="authorization.can('integration:view')" key="integration-operations"><DesktopOutlined />Integration Operations</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
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
