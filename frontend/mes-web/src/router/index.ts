import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { canUseAction, canUseRoute } from '../auth/permissions'
import AppLayout from '../layouts/AppLayout.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
import LoginView from '../views/auth/LoginView.vue'

const router = createRouter({ history: createWebHistory(), routes: [
  { path: '/login', name: 'login', component: LoginView },
  { path: '/', component: AppLayout, children: [
    { path: '', name: 'dashboard', component: DashboardView },
    { path: 'change-password', name: 'change-password', component: () => import('../views/auth/ChangePasswordView.vue') },
    { path: 'admin/users', name: 'users', component: () => import('../views/admin/UsersView.vue'), meta: { permission: 'menu:iam:users' } },
    { path: 'admin/users/new', name: 'user-new', component: () => import('../views/admin/UserFormView.vue'), meta: { permission: 'menu:iam:users', action: 'action:iam:user.manage' } },
    { path: 'admin/users/:id/view', name: 'user-view', component: () => import('../views/admin/UserFormView.vue'), meta: { permission: 'menu:iam:users' } },
    { path: 'admin/users/:id/edit', name: 'user-edit', component: () => import('../views/admin/UserFormView.vue'), meta: { permission: 'menu:iam:users', action: 'action:iam:user.manage' } },
    { path: 'admin/roles', name: 'roles', component: () => import('../views/admin/RolesView.vue'), meta: { permission: 'menu:iam:roles' } },
    { path: 'admin/roles/new', name: 'role-new', component: () => import('../views/admin/RoleFormView.vue'), meta: { permission: 'menu:iam:roles', action: 'action:iam:role.manage' } },
    { path: 'admin/roles/:id/view', name: 'role-view', component: () => import('../views/admin/RoleFormView.vue'), meta: { permission: 'menu:iam:roles' } },
    { path: 'admin/roles/:id/edit', name: 'role-edit', component: () => import('../views/admin/RoleFormView.vue'), meta: { permission: 'menu:iam:roles', action: 'action:iam:role.manage' } }
  ] },
  { path: '/:pathMatch(.*)*', redirect: '/' }
] })

router.beforeEach(async to => {
  const auth = useAuthStore()
  await auth.load()
  if (!auth.identity) return to.name === 'login' ? true : { name: 'login', query: { redirect: to.fullPath } }
  if (to.name === 'login') return { name: auth.identity.mustChangePassword ? 'change-password' : 'dashboard' }
  if (auth.identity.mustChangePassword && to.name !== 'change-password') return { name: 'change-password' }
  const permission = to.meta.permission as string | undefined
  if (permission && !canUseRoute(auth.identity.permissionCodes, permission)) return { name: 'dashboard' }
  const action = to.meta.action as string | undefined
  if (permission && action && !canUseAction(auth.identity.permissionCodes, permission, action)) return { name: 'dashboard' }
  return true
})

export default router
