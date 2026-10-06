import { resources } from '../master/resources'
import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/auth/LoginView.vue'
import ChangePasswordView from '../views/auth/ChangePasswordView.vue'
import UsersView from '../views/admin/UsersView.vue'
import UserDetailView from '../views/admin/UserDetailView.vue'
import RolesView from '../views/admin/RolesView.vue'
import RoleDetailView from '../views/admin/RoleDetailView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
  { path: '/login', name: 'login', component: LoginView, meta: { title: '登录' } },
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', name: 'dashboard', component: DashboardView, meta: { title: '工作台' } },
      { path: 'change-password', name: 'change-password', component: ChangePasswordView, meta: { title: '修改密码' } },
      { path: 'admin/users', name: 'users', component: UsersView, meta: { title: '用户管理', permission: 'iam:user:view' } },
      { path: 'admin/users/create', name: 'user-create', component: UserDetailView, meta: { title: '新增用户', permission: 'iam:user:create', mode: 'create' } },
      { path: 'admin/users/:id', name: 'user-view', component: UserDetailView, meta: { title: '用户详情', permission: 'iam:user:view', mode: 'view' } },
      { path: 'admin/users/:id/edit', name: 'user-edit', component: UserDetailView, meta: { title: '编辑用户', permission: 'iam:user:update', mode: 'edit' } },
      { path: 'admin/roles', name: 'roles', component: RolesView, meta: { title: '角色与权限', permission: 'iam:role:view' } },
      { path: 'admin/roles/create', name: 'role-create', component: RoleDetailView, meta: { title: '新增角色', permission: 'iam:role:create', mode: 'create' } },
      { path: 'admin/roles/:id', name: 'role-view', component: RoleDetailView, meta: { title: '角色详情', permission: 'iam:role:view', mode: 'view' } },
      { path: 'admin/roles/:id/edit', name: 'role-edit', component: RoleDetailView, meta: { title: '编辑角色', permission: 'iam:role:update', mode: 'edit' } },
      {path:'process/products',name:'process-products',component:()=>import('../views/process/ProcessListView.vue'),meta:{title:'产品管理',permission:'master:product:view',resource:'products'}},
      {path:'process/products/create',name:'process-products-create',component:()=>import('../views/process/ProductDetailView.vue'),meta:{title:'产品',permission:'master:product:create',mode:'create'}},
      {path:'process/products/:id',name:'process-products-view',component:()=>import('../views/process/ProductDetailView.vue'),meta:{title:'产品',permission:'master:product:view',mode:'view'}},
      {path:'process/products/:id/edit',name:'process-products-edit',component:()=>import('../views/process/ProductDetailView.vue'),meta:{title:'产品',permission:'master:product:update',mode:'edit'}},
      {path:'process/packages',name:'process-packages',component:()=>import('../views/process/ProcessListView.vue'),meta:{title:'工艺包管理',permission:'process:package:view',resource:'packages'}},
      {path:'process/packages/create',name:'process-packages-create',component:()=>import('../views/process/ProcessDetailView.vue'),meta:{title:'工艺包',permission:'process:package:create',mode:'create'}},
      {path:'process/packages/:id',name:'process-packages-view',component:()=>import('../views/process/ProcessDetailView.vue'),meta:{title:'工艺包',permission:'process:package:view',mode:'view'}},
      {path:'process/packages/:id/edit',name:'process-packages-edit',component:()=>import('../views/process/ProcessDetailView.vue'),meta:{title:'工艺包',permission:'process:package:edit',mode:'edit'}},
      {path:'quality/production-plans',name:'production-plans-list',component:()=>import('../views/quality/ProductionQualityList.vue'),meta:{title:'生产质量计划',permission:'qms:plan:view',resource:'production-plans',mode:'list'}},
      {path:'quality/production-plans/create',name:'production-plans-create',component:()=>import('../views/quality/ProductionQualityDetail.vue'),meta:{title:'生产质量计划',permission:'qms:plan:create',resource:'production-plans',mode:'create'}},
      {path:'quality/production-plans/:id',name:'production-plans-view',component:()=>import('../views/quality/ProductionQualityDetail.vue'),meta:{title:'生产质量计划',permission:'qms:plan:view',resource:'production-plans',mode:'view'}},
      {path:'quality/production-plans/:id/edit',name:'production-plans-edit',component:()=>import('../views/quality/ProductionQualityDetail.vue'),meta:{title:'生产质量计划',permission:'qms:plan:update',resource:'production-plans',mode:'edit'}},
      {path:'quality/production-tests',name:'production-tests-list',component:()=>import('../views/quality/ProductionQualityList.vue'),meta:{title:'生产检验',permission:'qms:test:view',resource:'production-tests',mode:'list'}},
      {path:'quality/production-tests/create',name:'production-tests-create',component:()=>import('../views/quality/ProductionQualityDetail.vue'),meta:{title:'生产检验',permission:'qms:test:record',resource:'production-tests',mode:'create'}},
      {path:'quality/production-tests/:id',name:'production-tests-view',component:()=>import('../views/quality/ProductionQualityDetail.vue'),meta:{title:'生产检验',permission:'qms:test:view',resource:'production-tests',mode:'view'}},
      {path:'qa/batches/:id/review',name:'finished-qa-review',component:()=>import('../views/quality/FinishedReleaseView.vue'),meta:{title:'QA 批审详情',permission:'qa:batch-review',mode:'review'}},
      {path:'qa/batches/:id/release',name:'finished-qa-release',component:()=>import('../views/quality/FinishedReleaseView.vue'),meta:{title:'QA 批放行审核',permission:'qa:release',mode:'release'}},
      {path:'production/batches/:id/balance',name:'batch-balance',component:()=>import('../views/quality/MaterialBalanceView.vue'),meta:{title:'物料平衡与偏差',permission:'balance:view'}},
      {path:'quality/inspection-requests',name:'incoming-inspection-requests-list',component:()=>import('../views/quality/IncomingListView.vue'),meta:{title:'请验单',permission:'qms:inspection-request:view',resource:'inspection-requests',mode:'list'}},
      {path:'quality/inspection-requests/create',name:'incoming-inspection-requests-create',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'请验单',permission:'qms:inspection-request:create',resource:'inspection-requests',mode:'create'}},
      {path:'quality/inspection-requests/:id',name:'incoming-inspection-requests-view',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'请验单',permission:'qms:inspection-request:view',resource:'inspection-requests',mode:'view'}},
      {path:'quality/sampling-tasks',name:'incoming-sampling-tasks-list',component:()=>import('../views/quality/IncomingListView.vue'),meta:{title:'取样记录',permission:'qms:sampling:view',resource:'sampling-tasks',mode:'list'}},
      {path:'quality/sampling-tasks/create',name:'incoming-sampling-tasks-create',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'取样记录',permission:'qms:sampling:create',resource:'sampling-tasks',mode:'create'}},
      {path:'quality/sampling-tasks/:id',name:'incoming-sampling-tasks-view',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'取样记录',permission:'qms:sampling:view',resource:'sampling-tasks',mode:'view'}},
      {path:'quality/sampling-tasks/:id/execute',name:'incoming-sampling-tasks-execute',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'取样记录',permission:'qms:sampling:view',resource:'sampling-tasks',mode:'execute'}},
      {path:'quality/samples',name:'incoming-samples-list',component:()=>import('../views/quality/IncomingListView.vue'),meta:{title:'样品',permission:'qms:test:view',resource:'samples',mode:'list'}},
      {path:'quality/samples/:id',name:'incoming-samples-view',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'样品',permission:'qms:test:view',resource:'samples',mode:'view'}},
      {path:'quality/inspection-tasks',name:'incoming-inspection-tasks-list',component:()=>import('../views/quality/IncomingListView.vue'),meta:{title:'检验记录',permission:'qms:test:view',resource:'inspection-tasks',mode:'list'}},
      {path:'quality/inspection-tasks/create',name:'incoming-inspection-tasks-create',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'检验记录',permission:'qms:test:execute',resource:'inspection-tasks',mode:'create'}},
      {path:'quality/inspection-tasks/:id',name:'incoming-inspection-tasks-view',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'检验记录',permission:'qms:test:view',resource:'inspection-tasks',mode:'view'}},
      {path:'quality/inspection-tasks/:id/execute',name:'incoming-inspection-tasks-execute',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'检验记录',permission:'qms:test:view',resource:'inspection-tasks',mode:'execute'}},
      {path:'quality/inspection-reports',name:'incoming-inspection-reports-list',component:()=>import('../views/quality/IncomingListView.vue'),meta:{title:'检验报告',permission:'qms:report:view',resource:'inspection-reports',mode:'list'}},
      {path:'quality/inspection-reports/create',name:'incoming-inspection-reports-create',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'检验报告',permission:'qms:report:create',resource:'inspection-reports',mode:'create'}},
      {path:'quality/inspection-reports/:id',name:'incoming-inspection-reports-view',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'检验报告',permission:'qms:report:view',resource:'inspection-reports',mode:'view'}},
      {path:'deviations',name:'incoming-deviations-list',component:()=>import('../views/quality/IncomingListView.vue'),meta:{title:'来料调查',permission:'qms:deviation:view',resource:'deviations',mode:'list'}},
      {path:'deviations/create',name:'incoming-deviations-create',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'来料调查',permission:'qms:deviation:create',resource:'deviations',mode:'create'}},
      {path:'deviations/:id',name:'incoming-deviations-view',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'来料调查',permission:'qms:deviation:view',resource:'deviations',mode:'view'}},
      {path:'qa/material-lots/:id/review',name:'incoming-release-review',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'物料放行',permission:'qa:material-release:view',resource:'release',mode:'review'}},
      {path:'qa/material-lots/:id/release',name:'incoming-release-release',component:()=>import('../views/quality/IncomingDetailView.vue'),meta:{title:'物料放行',permission:'qa:material-release:view',resource:'release',mode:'release'}},
      {path:'production/orders',name:'production-orders-list',component:()=>import('../views/production/ProductionListView.vue'),meta:{title:'生产订单',permission:'production:order:view',resource:'orders',mode:'list'}},
      {path:'production/orders/create',name:'production-orders-create',component:()=>import('../views/production/ProductionDetailView.vue'),meta:{title:'生产订单',permission:'production:order:create',resource:'orders',mode:'create'}},
      {path:'production/orders/:id',name:'production-orders-view',component:()=>import('../views/production/ProductionDetailView.vue'),meta:{title:'生产订单',permission:'production:order:view',resource:'orders',mode:'view'}},
      {path:'production/orders/:id/edit',name:'production-orders-edit',component:()=>import('../views/production/ProductionDetailView.vue'),meta:{title:'生产订单',permission:'production:order:update',resource:'orders',mode:'edit'}},
      {path:'production/batches',name:'production-batches-list',component:()=>import('../views/production/ProductionListView.vue'),meta:{title:'生产批',permission:'production:batch:view',resource:'batches',mode:'list'}},
      {path:'production/batches/create',name:'production-batches-create',component:()=>import('../views/production/ProductionDetailView.vue'),meta:{title:'生产批',permission:'production:batch:create',resource:'batches',mode:'create'}},
      {path:'production/batches/:id',name:'production-batches-view',component:()=>import('../views/production/ProductionDetailView.vue'),meta:{title:'生产批',permission:'production:batch:view',resource:'batches',mode:'view'}},
      {path:'production/batches/:id/edit',name:'production-batches-edit',component:()=>import('../views/production/ProductionDetailView.vue'),meta:{title:'生产批',permission:'production:batch:update',resource:'batches',mode:'edit'}},
      {path:'mes/execution/:id/forms/:formId',name:'execution-form',component:()=>import('../views/ebr/RuntimeFormView.vue'),meta:{title:'电子批记录',permission:'ebr:form:view'}},
      {path:'mes/execution/:id',name:'execution-execution',component:()=>import('../views/production/ExecutionWorkbench.vue'),meta:{title:'生产执行',permission:'mes:operation:view',mode:'execution'}},
      {path:'mes/execution/:id/weighing',name:'execution-weighing',component:()=>import('../views/production/ExecutionWorkbench.vue'),meta:{title:'生产执行',permission:'mes:weigh:create',mode:'weighing'}},
      {path:'mes/execution/:id/charge',name:'execution-charge',component:()=>import('../views/production/ExecutionWorkbench.vue'),meta:{title:'生产执行',permission:'mes:charge:create',mode:'charge'}},
      {path:'trace',name:'trace',component:()=>import('../views/production/TraceView.vue'),meta:{title:'完整追溯',permission:'trace:view'}},
      {path:'quality/specifications',name:'qc-specifications',component:()=>import('../views/quality/QcSpecificationListView.vue'),meta:{title:'QC质量标准',permission:'qms:specification:view'}},
      {path:'quality/specifications/create',name:'qc-specification-create',component:()=>import('../views/quality/QcSpecificationDetailView.vue'),meta:{title:'新增QC质量标准',permission:'qms:specification:create',mode:'create'}},
      {path:'quality/specifications/:id',name:'qc-specification-view',component:()=>import('../views/quality/QcSpecificationDetailView.vue'),meta:{title:'QC质量标准详情',permission:'qms:specification:view'}},
      {path:'quality/specification-versions/:id/edit',name:'qc-specification-version',component:()=>import('../views/quality/QcSpecificationVersionView.vue'),meta:{title:'QC质量标准版本',permission:'qms:specification:view'}},
      {path:'ebr/templates',name:'ebr-templates',component:()=>import('../views/ebr/TemplateListView.vue'),meta:{title:'eBR 模板',permission:'ebr:template:view',mode:'',resource:''}},
      {path:'ebr/templates/create',name:'ebr-template-create',component:()=>import('../views/ebr/TemplateDetailView.vue'),meta:{title:'eBR 模板',permission:'ebr:template:create',mode:'create',resource:''}},
      {path:'ebr/templates/:id',name:'ebr-template-view',component:()=>import('../views/ebr/TemplateDetailView.vue'),meta:{title:'eBR 模板',permission:'ebr:template:view',mode:'view',resource:''}},
      {path:'ebr/templates/:id/designer',name:'ebr-template-designer',component:()=>import('../views/ebr/TemplateDetailView.vue'),meta:{title:'eBR 模板',permission:'ebr:template:update',mode:'designer',resource:''}},
      {path:'wms/receipts',name:'wms-receipts',component:()=>import('../views/wms/WmsListView.vue'),meta:{title:'原辅料收货记录',permission:'wms:receipt:view',mode:'',resource:'receipts'}},
      {path:'wms/receipts/create',name:'wms-receipts-create',component:()=>import('../views/wms/ReceiptDetailView.vue'),meta:{title:'原辅料收货记录',permission:'wms:receipt:create',mode:'create',resource:'receipts'}},
      {path:'wms/receipts/:id',name:'wms-receipts-view',component:()=>import('../views/wms/ReceiptDetailView.vue'),meta:{title:'原辅料收货记录',permission:'wms:receipt:view',mode:'view',resource:'receipts'}},
      {path:'wms/receipts/:id/edit',name:'wms-receipts-edit',component:()=>import('../views/wms/ReceiptDetailView.vue'),meta:{title:'原辅料收货记录',permission:'wms:receipt:update',mode:'edit',resource:'receipts'}},
      {path:'wms/inventory',name:'wms-inventory',component:()=>import('../views/wms/WmsManagementListView.vue'),meta:{title:'库存管理',permission:'wms:inventory:view',resource:'inventory'}},
      {path:'wms/requests',name:'wms-requests',component:()=>import('../views/wms/WmsManagementListView.vue'),meta:{title:'领料申请',permission:'wms:request:view',resource:'requests'}},
      {path:'wms/requests/create',name:'wms-requests-create',component:()=>import('../views/wms/MaterialRequestDetailView.vue'),meta:{title:'新增领料申请',permission:'wms:request:create',resource:'requests',mode:'create'}},
      {path:'wms/requests/:id',name:'wms-requests-view',component:()=>import('../views/wms/MaterialRequestDetailView.vue'),meta:{title:'领料申请详情',permission:'wms:request:view',resource:'requests',mode:'view'}},
      {path:'wms/requests/:id/edit',name:'wms-requests-edit',component:()=>import('../views/wms/MaterialRequestDetailView.vue'),meta:{title:'编辑领料申请',permission:'wms:request:update',resource:'requests',mode:'edit'}},
      {path:'wms/returns',name:'wms-returns',component:()=>import('../views/wms/WmsManagementListView.vue'),meta:{title:'退料管理',permission:'wms:issue:view',resource:'returns'}},
      {path:'wms/returns/create',name:'wms-returns-create',component:()=>import('../views/wms/IssueReturnCreateView.vue'),meta:{title:'登记退料',permission:'wms:issue:return',requiredPermissions:['wms:issue:view'],resource:'returns',mode:'create'}},
      {path:'wms/issues',name:'wms-issues',component:()=>import('../views/wms/WmsListView.vue'),meta:{title:'出库管理',permission:'wms:issue:view',mode:'',resource:'issues'}},
      {path:'wms/issues/create',name:'wms-issues-create',component:()=>import('../views/wms/IssueDetailView.vue'),meta:{title:'新增出库单',permission:'wms:issue:create',mode:'create',resource:'issues'}},
      {path:'wms/issues/:id',name:'wms-issues-view',component:()=>import('../views/wms/IssueDetailView.vue'),meta:{title:'出库单详情',permission:'wms:issue:view',mode:'view',resource:'issues'}},
      {path:'wms/issues/:id/edit',name:'wms-issues-edit',component:()=>import('../views/wms/IssueDetailView.vue'),meta:{title:'编辑出库单',permission:'wms:issue:update',mode:'edit',resource:'issues'}},
      {path:'wms/material-lots/:id',name:'wms-material-lot',component:()=>import('../views/wms/MaterialLotView.vue'),meta:{title:'仓储管理',permission:'wms:inventory:view',mode:'view',resource:''}},
      ...resources.flatMap(def => [
        {path:`master/${def.key}`,name:`master-${def.key}`,component:()=>import('../views/master/MasterListView.vue'),meta:{title:def.title,permission:`master:${def.permission}:view`,resource:def.key}},
        {path:`master/${def.key}/create`,name:`master-${def.key}-create`,component:()=>def.key==='materials'?import('../views/master/MaterialDetailView.vue'):import('../views/master/MasterDetailView.vue'),meta:{title:`新增${def.title}`,permission:`master:${def.permission}:create`,resource:def.key,mode:'create'}},
        {path:`master/${def.key}/:id`,name:`master-${def.key}-view`,component:()=>def.key==='materials'?import('../views/master/MaterialDetailView.vue'):import('../views/master/MasterDetailView.vue'),meta:{title:`${def.title}详情`,permission:`master:${def.permission}:view`,resource:def.key,mode:'view'}},
        {path:`master/${def.key}/:id/edit`,name:`master-${def.key}-edit`,component:()=>def.key==='materials'?import('../views/master/MaterialDetailView.vue'):import('../views/master/MasterDetailView.vue'),meta:{title:`编辑${def.title}`,permission:`master:${def.permission}:update`,resource:def.key,mode:'edit'}}
      ]),
      {path:'finished/inbound',name:'finished-inbound',component:()=>import('../views/finished/FinishedListView.vue'),meta:{title:'成品入库申请',permission:'wms:finished-inbound:view',resource:'inbound',mode:'list'}},
      {path:'finished/inbound/create',name:'finished-inbound-create',component:()=>import('../views/finished/FinishedDetailView.vue'),meta:{title:'新增成品入库申请',permission:'wms:finished-inbound:create',resource:'inbound',mode:'create'}},
      {path:'finished/inbound/:id',name:'finished-inbound-view',component:()=>import('../views/finished/FinishedDetailView.vue'),meta:{title:'成品入库申请',permission:'wms:finished-inbound:view',resource:'inbound',mode:'view'}},
      {path:'finished/requests',name:'finished-requests',component:()=>import('../views/finished/FinishedListView.vue'),meta:{title:'成品请验单',permission:'qms:finished-request:view',resource:'requests',mode:'list'}},
      {path:'finished/requests/create',name:'finished-requests-create',component:()=>import('../views/finished/FinishedDetailView.vue'),meta:{title:'新增成品请验单',permission:'qms:finished-request:create',resource:'requests',mode:'create'}},
      {path:'finished/requests/:id',name:'finished-requests-view',component:()=>import('../views/finished/FinishedDetailView.vue'),meta:{title:'成品请验单',permission:'qms:finished-request:view',resource:'requests',mode:'view'}},
      {path:'finished/sampling',name:'finished-sampling',component:()=>import('../views/finished/FinishedListView.vue'),meta:{title:'成品取样记录',permission:'qms:finished-sampling:view',resource:'sampling',mode:'list'}},
      {path:'finished/sampling/:id',name:'finished-sampling-view',component:()=>import('../views/finished/FinishedDetailView.vue'),meta:{title:'成品取样记录',permission:'qms:finished-sampling:view',resource:'sampling',mode:'view'}},
      {path:'finished/reports',name:'finished-reports',component:()=>import('../views/finished/FinishedListView.vue'),meta:{title:'成品检验报告',permission:'qms:finished-report:view',resource:'reports',mode:'list'}},
      {path:'finished/reports/:id',name:'finished-reports-view',component:()=>import('../views/finished/FinishedDetailView.vue'),meta:{title:'成品检验报告',permission:'qms:finished-report:view',resource:'reports',mode:'view'}},
      {path:'finished/inventory',name:'finished-inventory',component:()=>import('../views/wms/WmsManagementListView.vue'),meta:{title:'成品库存',permission:'wms:inventory:view',resource:'inventory',finishedInventory:true}},
      {path:'finished/shipments',name:'finished-shipments',component:()=>import('../views/finished/FinishedListView.vue'),meta:{title:'成品发货出库',permission:'wms:finished-shipment:view',resource:'shipments',mode:'list'}},
      {path:'finished/shipments/create',name:'finished-shipments-create',component:()=>import('../views/finished/FinishedDetailView.vue'),meta:{title:'新增成品发货出库',permission:'wms:finished-shipment:create',resource:'shipments',mode:'create'}},
      {path:'finished/shipments/:id',name:'finished-shipments-view',component:()=>import('../views/finished/FinishedDetailView.vue'),meta:{title:'成品发货出库',permission:'wms:finished-shipment:view',resource:'shipments',mode:'view'}},
      { path: 'audit', name: 'audit', component: () => import('../views/audit/AuditTrailView.vue'), meta: { title: 'GMP Audit Trail', permission: 'audit:view' } },
      { path: 'integration/operations', name: 'integration-operations', component: () => import('../views/integration/IntegrationOperationsView.vue'), meta: { title: 'Integration Inbox / Outbox Operations', permission: 'integration:view' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

router.beforeEach(async to => {
  const auth = useAuthStore()
  await auth.load()
  if (!auth.identity) return to.name === 'login' ? true : { name: 'login', query: { redirect: to.fullPath } }
  if (to.name === 'login') return { name: auth.identity.mustChangePassword ? 'change-password' : 'dashboard' }
  if (auth.identity.mustChangePassword && to.name !== 'change-password') return { name: 'change-password' }
  const permission = to.meta.permission as string | undefined
  if (((to.meta.requiredPermissions as string[]|undefined)??[]).some(p=>!auth.can(p))) return { name: 'dashboard' }
  if (permission && !auth.can(permission)) return { name: 'dashboard' }
  return true
})

export default router
