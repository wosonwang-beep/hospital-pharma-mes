import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [{
    path: '/',
    component: AppLayout,
    children: [
      { path: '', name: 'dashboard', component: DashboardView, meta: { title: '工作台' } },
      { path: 'audit', name: 'audit', component: () => import('../views/audit/AuditTrailView.vue'), meta: { title: 'GMP Audit Trail', permission: 'audit:view' } },
      { path: 'integration/operations', name: 'integration-operations', component: () => import('../views/integration/IntegrationOperationsView.vue'), meta: { title: 'Integration Inbox / Outbox Operations', permission: 'integration:view' } }
    ]
  }]
})
