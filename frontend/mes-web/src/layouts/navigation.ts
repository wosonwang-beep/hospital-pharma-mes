import { resources } from '../master/resources'

export interface NavigationItem { key: string; title: string; path: string; permission?: string; requiredPermissions?: string[] }
export interface NavigationGroup { key: string; title: string; items: NavigationItem[] }
export const navigationHome: NavigationItem = { key: 'dashboard', title: '首页', path: '/' }

/** Historical approved menu order used by migration fixtures. Runtime shells consume /auth/navigation database trees. */
export const navigationConfiguration: NavigationGroup[] = [
  { key: 'master', title: '基础管理', items: [
    ...resources.map(r => ({ key: `master-${r.key}`, title: r.key === 'equipment' ? '设备' : r.title, path: `/master/${r.key}`, permission: `master:${r.permission}:view` })),
    { key: 'process-products', title: '产品管理', path: '/process/products', permission: 'master:product:view' },
    { key: 'process-packages', title: '生产工艺', path: '/process/packages', permission: 'process:package:view' }
  ] },
  { key: 'wms', title: 'WMS管理', items: [
    { key: 'wms-receipts', title: '原辅料收货记录', path: '/wms/receipts', permission: 'wms:receipt:view' },
    { key: 'wms-inventory', title: '库存管理', path: '/wms/inventory', permission: 'wms:inventory:view' },
    { key: 'wms-requests', title: '领料申请', path: '/wms/requests', permission: 'wms:request:view' },
    { key: 'wms-issues', title: '出库管理', path: '/wms/issues', permission: 'wms:issue:view' },
    { key: 'wms-returns', title: '退料管理', path: '/wms/returns', permission: 'wms:issue:view' }
  ] },
  { key: 'quality', title: '质量管理', items: [
    { key: 'incoming-inspection-requests-list', title: '请验单', path: '/quality/inspection-requests', permission: 'qms:inspection-request:view' },
    { key: 'incoming-sampling-tasks-list', title: '取样记录', path: '/quality/sampling-tasks', permission: 'qms:sampling:view' },
    { key: 'incoming-samples-list', title: '样品', path: '/quality/samples', permission: 'qms:test:view' },
    { key: 'incoming-inspection-tasks-list', title: '检验记录', path: '/quality/inspection-tasks', permission: 'qms:test:view' },
    { key: 'incoming-inspection-reports-list', title: '检验报告', path: '/quality/inspection-reports', permission: 'qms:report:view' },
    { key: 'qc-specifications', title: 'QC质量标准', path: '/quality/specifications', permission: 'qms:specification:view' },
    { key: 'production-plans-list', title: '生产质量计划', path: '/quality/production-plans', permission: 'qms:plan:view' },
    { key: 'production-tests-list', title: '生产检验', path: '/quality/production-tests', permission: 'qms:test:view' },
    { key: 'incoming-deviations-list', title: '质量调查', path: '/deviations', permission: 'qms:deviation:view' }
  ] },
  { key: 'production', title: '生产管理', items: [
    { key: 'production-orders-list', title: '生产订单', path: '/production/orders', permission: 'production:order:view' },
    { key: 'production-batches-list', title: '生产批', path: '/production/batches', permission: 'production:batch:view' },
    { key: 'production-execution', title: '生产执行', path: '/production/execution', permission: 'mes:execution:view', requiredPermissions: ['mes:operation:view'] },
    {key:'ebr-book-templates',title:'批记录册模板',path:'/ebr/book-templates',permission:'ebr:template:view'},
    { key: 'ebr-templates', title: 'eBR模板', path: '/ebr/templates', permission: 'ebr:template:view' },
    { key: 'production-balances', title: '物料平衡', path: '/production/balances', permission: 'balance:view' }
  ] },
  { key: 'finished', title: '成品管理', items: [
    { key: 'finished-inbound', title: '成品入库申请', path: '/finished/inbound', permission: 'wms:finished-inbound:view' },
    { key: 'finished-receiving', title: '成品生产入库（待检）', path: '/finished/receiving', permission: 'wms:finished-inbound:view' },
    { key: 'finished-requests', title: '成品请验', path: '/finished/requests', permission: 'qms:finished-request:view' },
    { key: 'finished-sampling', title: '成品取样', path: '/finished/sampling', permission: 'qms:finished-sampling:view' },
    { key: 'finished-tests', title: '成品检验', path: '/finished/tests', permission: 'qms:test:view' },
    { key: 'finished-qa-reviews', title: 'QA批审核', path: '/finished/qa-reviews', permission: 'qa:batch-review' },
    { key: 'finished-releases', title: '成品放行', path: '/finished/releases', permission: 'qa:release', requiredPermissions: ['qa:batch-review'] },
    { key: 'finished-inventory', title: '成品库存', path: '/finished/inventory', permission: 'wms:inventory:view' },
    { key: 'finished-shipments', title: '成品发货出库', path: '/finished/shipments', permission: 'wms:finished-shipment:view' }
  ] },
  { key: 'system', title: '系统管理', items: [
    { key: 'print-templates', title: '打印模板', path: '/admin/print-templates', permission: 'print:template:view' },
    { key: 'users', title: '用户管理', path: '/admin/users', permission: 'iam:user:view' },
    { key: 'roles', title: '角色与权限', path: '/admin/roles', permission: 'iam:role:view' },
    { key: 'menus', title: '菜单管理', path: '/admin/menus', permission: 'iam:menu:view' },
    { key: 'trace', title: '完整追溯', path: '/trace', permission: 'trace:view' },
    { key: 'audit', title: 'GMP Audit Trail', path: '/audit', permission: 'audit:view' },
    { key: 'integration-operations', title: 'Integration Operations', path: '/integration/operations', permission: 'integration:view' }
  ] }
]

export function permittedNavigation(can: (permission: string) => boolean): NavigationGroup[] {
  return navigationConfiguration.map(group => ({ ...group, items: group.items.filter(item => (!item.permission || can(item.permission)) && (item.requiredPermissions ?? []).every(can)) })).filter(group => group.items.length)
}

export function selectedNavigationKey(path: string, groups: NavigationGroup[]): string | undefined {
  if (path === navigationHome.path) return navigationHome.key
  // Contextual detail/workbench routes highlight their permission-scoped global selectors.
  const sourcePath = path.startsWith('/mes/execution/') ? '/production/execution' : /^\/production\/batches\/[^/]+\/balance$/.test(path) ? '/production/balances' : /^\/qa\/batches\/[^/]+\/review$/.test(path) ? '/finished/qa-reviews' : /^\/qa\/batches\/[^/]+\/release$/.test(path) ? '/finished/releases' : path
  return groups.flatMap(group => group.items).filter(item => sourcePath === item.path || sourcePath.startsWith(item.path + '/')).sort((a, b) => b.path.length - a.path.length)[0]?.key
}
