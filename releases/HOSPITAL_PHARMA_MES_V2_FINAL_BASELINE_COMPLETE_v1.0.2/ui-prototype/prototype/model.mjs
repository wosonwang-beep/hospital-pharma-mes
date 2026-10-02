export const materialRoutes = Object.freeze({
  query: '/master/materials',
  create: '/master/materials/create',
  view: '/master/materials/MAT-00028',
  edit: '/master/materials/MAT-00028/edit',
});

const baseScenarios = [
  ['UI-AUD-Q', 'MES-001-R2', 'GMP Audit Trail', '/audit', 'AUD-001', 'audit:view', 'GET /audit-events', 'audit-query'],
  ['UI-INT-OPS', 'MES-001-R2', 'Integration Inbox / Outbox Operations', '/integration/operations', 'INT-001', 'integration:view', 'GET /integration/messages', 'integration-ops'],
  ['UI-IAM-USR-Q', 'MES-002-R2', '用户与角色权限', '/admin/users', 'IAM-001', 'iam:user:view', 'GET /users', 'query'],
  ['UI-EQP-Q', 'MES-003-R2', '设备与人员资格', '/master/equipment', 'MD-EQP-001', 'master:equipment:view', 'GET /equipment', 'query'],
  ['UI-MAT-Q', 'MES-004-R2', '物料主数据', materialRoutes.query, 'MD-MAT-001', 'master:material:view', 'GET /materials', 'query'],
  ['UI-SUP-Q', 'MES-005-R2', '批准供应商', '/master/suppliers', 'MD-SUP-001', 'master:supplier:view', 'GET /suppliers', 'query'],
  ['UI-PKG-V', 'MES-006-R2', '产品与工艺包', '/process/packages/PP-2026-014', 'PROC-001', 'process:package:view', 'GET /process-packages/{id}', 'view'],
  ['UI-EBR-DESIGNER', 'MES-007-R2', 'eBR 模板设计器', '/ebr/templates/EBR-014/designer', 'EBR-001', 'ebr:designer:edit', 'GET /ebr/templates/{id}', 'designer'],
  ['UI-WMS-REC-Q', 'MES-008-R2', '收货单查询', '/wms/receipts', 'WMS-001', 'wms:receipt:view', 'UNRESOLVED: no frozen GET receipt-query operation', 'query'],
  ['UI-BATCH-V', 'MES-009-R2', '生产批详情', '/production/batches/MB-20260928-006', 'PRD-002', 'production:batch:view', 'GET /main-batches/{id}', 'batch'],
  ['UI-EXEC-W', 'MES-010-R2', '工序执行工作台', '/mes/execution/EU-240928-03', 'PRD-004,MES-OP-001', 'mes:operation:view', 'GET /execution-units/{id}/operations', 'execution'],
  ['UI-WGH-W', 'MES-011-R2', '称量工作台', '/mes/execution/EU-240928-03/weighing', 'MES-WGH-001', 'mes:weigh:create', 'GET /execution-units/{id}/weighings; POST /weighings', 'weighing'],
  ['UI-BAL-V', 'MES-012-R2', '物料平衡与偏差', '/production/batches/MB-20260928-006/balance', 'BAL-001,BAL-002,BAL-003', 'balance:view', 'GET /main-batches/{id}/material-balance', 'balance'],
  ['UI-QA-RELEASE', 'MES-013-R2', 'QA 批放行审核', '/qa/batches/MB-20260928-006/release', 'QA-REL-001', 'qa:batch-review', 'GET /qa/batches/{id}/review-model', 'release'],
];

export const scenarios = Object.freeze(baseScenarios.map(([id, task, title, route, requirement, permission, api, mode]) => Object.freeze({
  id, task, title, route, requirement, permission, api, mode, screenshot: `${id}.png`,
})));

const materialScenario = scenarios.find((item) => item.id === 'UI-MAT-Q');

export function resolveScenario(route) {
  const normalized = route || scenarios[0].route;
  if (normalized === materialRoutes.create) return { ...materialScenario, id: 'UI-MAT-C', title: '新增物料', route: normalized, mode: 'create', permission: 'master:material:create', api: 'POST /materials' };
  if (normalized === materialRoutes.view) return { ...materialScenario, id: 'UI-MAT-V', title: '物料详情', route: normalized, mode: 'view' };
  if (normalized === materialRoutes.edit) return { ...materialScenario, id: 'UI-MAT-E', title: '编辑物料', route: normalized, mode: 'edit', permission: 'master:material:update', api: 'PUT /materials/{id}' };
  return scenarios.find((item) => item.route === normalized) || scenarios[0];
}

export function canPerform(context, permission, action) {
  return context.permissions.includes(permission) && context.allowedActions.includes(action);
}
