<script setup lang="ts">
import { computed, ref } from 'vue'
import { resources } from '../master/resources'
import { useRoute, useRouter } from 'vue-router'
import { usePlatformAuthContext } from '../auth/PlatformAuthContext'
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
const authorization = usePlatformAuthContext()
const auth = useAuthStore()
const title = computed(() => String(route.meta.title ?? '工作台'))
const selectedKeys = computed(() => [String(route.name ?? 'dashboard')])
const navigationGroups=computed(()=>[
 {title:'基础主数据',items:[...resources.filter(r=>r.key!=='unit-conversions'&&auth.can(`master:${r.permission}:view`)).map(r=>({key:`master-${r.key}`,title:r.title,path:`/master/${r.key}`})),...(auth.can('master:product:view')?[{key:'process-products',title:'产品管理',path:'/process/products'}]:[]),...(auth.can('process:package:view')?[{key:'process-packages',title:'工艺包管理',path:'/process/packages'}]:[])]},
 {title:'仓储管理',items:[{key:'wms-receipts',title:'原辅料收货记录',path:'/wms/receipts',permission:'wms:receipt:view'},{key:'wms-inventory',title:'库存管理',path:'/wms/inventory',permission:'wms:inventory:view'},{key:'wms-requests',title:'领料申请',path:'/wms/requests',permission:'wms:request:view'},{key:'wms-issues',title:'出库管理',path:'/wms/issues',permission:'wms:issue:view'},{key:'wms-returns',title:'退料管理',path:'/wms/returns',permission:'wms:issue:view'}].filter(i=>auth.can(i.permission))},
 {title:'生产管理',items:[{key:'production-orders-list',title:'生产订单',path:'/production/orders',permission:'production:order:view'},{key:'production-batches-list',title:'生产批',path:'/production/batches',permission:'production:batch:view'},{key:'ebr-templates',title:'eBR 模板',path:'/ebr/templates',permission:'ebr:template:view'}].filter(i=>auth.can(i.permission))},
 {title:'质量管理',items:[{key:'production-plans-list',title:'生产质量计划',path:'/quality/production-plans',permission:'qms:plan:view'},{key:'production-tests-list',title:'生产检验',path:'/quality/production-tests',permission:'qms:test:view'},{key:'qc-specifications',title:'QC质量标准',path:'/quality/specifications',permission:'qms:specification:view'},{key:'incoming-inspection-requests-list',title:'请验单',path:'/quality/inspection-requests',permission:'qms:inspection-request:view'},{key:'incoming-sampling-tasks-list',title:'取样记录',path:'/quality/sampling-tasks',permission:'qms:sampling:view'},{key:'incoming-samples-list',title:'样品',path:'/quality/samples',permission:'qms:test:view'},{key:'incoming-inspection-tasks-list',title:'检验记录',path:'/quality/inspection-tasks',permission:'qms:test:view'},{key:'incoming-inspection-reports-list',title:'检验报告',path:'/quality/inspection-reports',permission:'qms:report:view'},{key:'incoming-deviations-list',title:'质量调查',path:'/deviations',permission:'qms:deviation:view'}].filter(i=>auth.can(i.permission))},
 {title:'系统管理',items:[{key:'users',title:'用户管理',path:'/admin/users',permission:'iam:user:view'},{key:'roles',title:'角色与权限',path:'/admin/roles',permission:'iam:role:view'},{key:'trace',title:'完整追溯',path:'/trace',permission:'trace:view'},{key:'audit',title:'GMP Audit Trail',path:'/audit',permission:'audit:view'},{key:'integration-operations',title:'Integration Operations',path:'/integration/operations',permission:'integration:view'}].filter(i=>authorization.can(i.permission))}
].filter(g=>g.items.length))
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
      <ExecutionReferenceNavigation v-if="executionWorkbench&&!collapsed"/>
      <a-menu mode="inline" :selectedKeys="selectedKeys" class="nav" :class="{'execution-original-navigation':executionWorkbench&&!collapsed}" @click="navigate">
        <a-menu-item key="dashboard"><HomeOutlined />首页</a-menu-item>
        <a-menu-item-group v-for="group in navigationGroups" :key="group.title" :title="group.title"><a-menu-item v-for="item in group.items" :key="item.key" :aria-label="item.key==='users'||item.key==='roles'?item.title:undefined" :class="{'ant-menu-item-selected':route.path.startsWith(item.path)}"><AppstoreOutlined/>{{item.title}}</a-menu-item></a-menu-item-group>
      </a-menu>
    </a-layout-sider>
    <a-layout class="main-shell" :class="{ 'main-shell-collapsed': collapsed }">
      <a-layout-header class="header" :class="{ 'header-collapsed': collapsed }">
        <div class="header-title">
          <a-dropdown placement="bottomLeft" class="mobile-navigation" :trigger="['click']">
            <a-button type="text" aria-label="打开导航"><AppstoreOutlined /></a-button>
            <template #overlay>
              <a-menu @click="navigate">
                <a-menu-item key="dashboard"><HomeOutlined />首页</a-menu-item>
                <a-menu-item-group v-for="group in navigationGroups" :key="group.title" :title="group.title"><a-menu-item v-for="item in group.items" :key="item.key">{{item.title}}</a-menu-item></a-menu-item-group>
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
