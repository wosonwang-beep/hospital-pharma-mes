<script setup lang="ts">
import { computed, ref } from 'vue'
import { resources } from '../master/resources'
import { useRoute, useRouter } from 'vue-router'
import { usePlatformAuthContext } from '../auth/PlatformAuthContext'
import { useAuthStore } from '../stores/auth'
import ExecutionReferenceNavigation from './ExecutionReferenceNavigation.vue'
import {
  AppstoreOutlined,
  DesktopOutlined,
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
const authorization = usePlatformAuthContext()
const auth = useAuthStore()
const title = computed(() => String(route.meta.title ?? '工作台'))
const selectedKeys = computed(() => [String(route.name ?? 'dashboard')])
const usersSelected = computed(() => route.path.startsWith('/admin/users'))
const rolesSelected = computed(() => route.path.startsWith('/admin/roles'))
const executionWorkbench = computed(() => route.name === 'execution-execution')

function navigate({ key }: { key: string }) {
  void router.push({ name: key })
}
async function logout() { await auth.logout(); await router.replace('/login') }
</script>

<template>
  <a-layout class="shell" :class="{'execution-desktop-shell':executionWorkbench}">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible :width="executionWorkbench?180:228" theme="light" class="sidebar">
      <div class="brand" :class="{ 'brand-collapsed': collapsed }">
        <span class="brand-mark">+</span><span>{{ collapsed ? '' : '医院制剂 MES' }}</span>
      </div>
      <ExecutionReferenceNavigation v-if="executionWorkbench&&!collapsed"/>
      <a-menu mode="inline" :selectedKeys="selectedKeys" class="nav" :class="{'execution-original-navigation':executionWorkbench&&!collapsed}" @click="navigate">
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
          <a-breadcrumb v-if="executionWorkbench" class="execution-header-breadcrumb"><a-breadcrumb-item>首页</a-breadcrumb-item><a-breadcrumb-item>生产管理</a-breadcrumb-item><a-breadcrumb-item>生产执行</a-breadcrumb-item></a-breadcrumb>
          <span>{{ title }}</span><span class="environment-chip">GMP · 验证环境</span>
        </div>
        <div v-if="executionWorkbench" class="execution-header-tools"><a-tooltip title="当前契约未提供全局搜索"><a-input disabled placeholder="搜索工单、批号、物料…" class="execution-header-search"><template #prefix><SearchOutlined/></template></a-input></a-tooltip><a-tooltip title="系统运行正常"><BellOutlined class="execution-system-icon"/></a-tooltip><a-dropdown><a-button type="text" class="execution-user"><a-avatar size="small"><UserOutlined/></a-avatar>{{auth.identity?.displayName}}<DownOutlined/></a-button><template #overlay><a-menu><a-menu-item @click="router.push('/change-password')">改密</a-menu-item><a-menu-item @click="logout">退出</a-menu-item></a-menu></template></a-dropdown></div>
        <div class="user" :class="{'execution-original-user':executionWorkbench}"><span class="status-dot" />系统运行正常<a-avatar><UserOutlined /></a-avatar><span>{{ auth.identity?.displayName }}</span><button class="header-link" @click="router.push('/change-password')">改密</button><button class="header-link" @click="logout">退出</button></div>
      </a-layout-header>
      <a-layout-content class="content">
        <a-breadcrumb><a-breadcrumb-item><HomeOutlined /> 首页</a-breadcrumb-item><a-breadcrumb-item>{{ title }}</a-breadcrumb-item></a-breadcrumb>
        <RouterView />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped>
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
 #app .execution-desktop-shell>.main-shell>.content{padding:12px 18px 12px 13px}
 #app .execution-desktop-shell>.main-shell>.content>:deep(.ant-breadcrumb){display:none}
 #app .execution-desktop-shell :deep(.execution-workbench){margin-top:0}
}
</style>
