import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { canUseAction, canUseRoute } from '../auth/permissions'
import AppLayout from '../layouts/AppLayout.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
import LoginView from '../views/auth/LoginView.vue'
import ChangePasswordView from '../views/auth/ChangePasswordView.vue'
import UsersView from '../views/admin/UsersView.vue'
import RolesView from '../views/admin/RolesView.vue'
import UserFormView from '../views/admin/UserFormView.vue'
import RoleFormView from '../views/admin/RoleFormView.vue'

const router = createRouter({ history: createWebHistory(), routes: [
  { path: '/login', name: 'login', component: LoginView },
  { path: '/', component: AppLayout, children: [
    { path: '', name: 'dashboard', component: DashboardView },
    { path: 'change-password', name: 'change-password', component: ChangePasswordView },
    { path: 'admin/users', name: 'users', component: UsersView, meta: { permission: 'menu:iam:users' } },
    { path: 'admin/users/new', name: 'user-new', component: UserFormView, meta: { permission: 'menu:iam:users', action: 'action:iam:user.manage' } },
    { path: 'admin/users/:id/view', name: 'user-view', component: UserFormView, meta: { permission: 'menu:iam:users' } },
    { path: 'admin/users/:id/edit', name: 'user-edit', component: UserFormView, meta: { permission: 'menu:iam:users', action: 'action:iam:user.manage' } },
    { path: 'admin/roles', name: 'roles', component: RolesView, meta: { permission: 'menu:iam:roles' } },
    { path: 'admin/roles/new', name: 'role-new', component: RoleFormView, meta: { permission: 'menu:iam:roles', action: 'action:iam:role.manage' } },
    { path: 'admin/roles/:id/view', name: 'role-view', component: RoleFormView, meta: { permission: 'menu:iam:roles' } },
    { path: 'admin/roles/:id/edit', name: 'role-edit', component: RoleFormView, meta: { permission: 'menu:iam:roles', action: 'action:iam:role.manage' } }
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
