import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import router from './router'

test('uses the frozen IAM permissions and never restores prototype routes', () => {
  const routes = router.getRoutes()
  const permission = (path: string) => routes.find(route => route.path === path)?.meta.permission
  expect(permission('/admin/users')).toBe('iam:user:view')
  expect(permission('/admin/users/create')).toBe('iam:user:create')
  expect(permission('/admin/users/:id/edit')).toBe('iam:user:update')
  expect(permission('/admin/roles')).toBe('iam:role:view')
  expect(permission('/admin/roles/create')).toBe('iam:role:create')
  expect(permission('/admin/roles/:id/edit')).toBe('iam:role:update')

  const source = readFileSync(join(process.cwd(), 'src/router/index.ts'), 'utf8')
  expect(source).not.toContain('/admin/users/new')
  expect(source).not.toContain('/admin/users/:id/view')
  expect(source).not.toContain('/platform/operations')
})
