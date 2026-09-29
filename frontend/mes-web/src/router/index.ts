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
  if (permission && !auth.can(permission)) return { name: 'dashboard' }
  return true
})

export default router
