import { expect, test } from '@playwright/test'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

test('administrator logs in and creates an employee from the management page', async ({ page }) => {
  const pageErrors: string[] = []
  page.on('pageerror', error => pageErrors.push(error.message))
  let signedIn = false
  let created = false
  let roleCreated = false
  const user = { id: 2, loginName: 'nurse01', displayName: '测试员工', enabled: true, version: 0, roleIds: [] }
  const role = { id: 3, roleCode: 'NURSE', displayName: '护士', enabled: true, version: 0, permissionCodes: [] }
  await page.route('**/api/v1/**', async route => {
    const url = new URL(route.request().url())
    const path = url.pathname
    const respond = (data: unknown, status = 200) => route.fulfill({ status, contentType: 'application/json', body: JSON.stringify({ code: status === 200 ? 'OK' : 'UNAUTHORIZED', message: status === 200 ? 'Success' : 'Unauthorized', data, traceId: 'e2e' }) })
    if (path === '/api/v1/auth/me') return respond(signedIn ? { userId: 1, loginName: 'admin', displayName: '管理员', roleCodes: ['SYSTEM_ADMIN'], permissionCodes: ['menu:home', 'menu:iam:users', 'action:iam:user.manage', 'menu:iam:roles', 'action:iam:role.manage'], mustChangePassword: false } : null, signedIn ? 200 : 401)
    if (path === '/api/v1/auth/refresh') return respond(null, 401)
    if (path === '/api/v1/auth/login') { signedIn = true; return respond({ accessToken: 'browser-test-token', expiresInSeconds: 900 }) }
    if (path === '/api/v1/admin/users' && route.request().method() === 'POST') { created = true; return respond({ user, temporaryPassword: 'temporary-password-123' }) }
    if (path === '/api/v1/admin/users') {
      const matches = created && (!url.searchParams.get('keyword') || url.searchParams.get('keyword') === 'nurse01')
      return respond({ items: matches ? [user] : [], total: matches ? 1 : 0, page: 0, size: 20 })
    }
    if (path === '/api/v1/admin/users/2') return respond(user)
    if (path === '/api/v1/admin/roles' && route.request().method() === 'POST') { roleCreated = true; return respond(role) }
    if (path === '/api/v1/admin/roles') return respond({ items: roleCreated ? [role] : [], total: roleCreated ? 1 : 0, page: 0, size: Number(url.searchParams.get('size') || 20) })
    if (path === '/api/v1/admin/roles/3') return respond(role)
    if (path === '/api/v1/admin/permissions') return respond([{ id: 1, permissionCode: 'menu:home', permissionType: 'MENU', module: 'home', displayName: '首页', enabled: true }])
    return respond(null, 404)
  })

  await page.goto('/')
  await expect(page.getByRole('heading', { name: '医院制剂生产管理系统' })).toBeVisible()
  await page.getByLabel('账号').fill('admin')
  await page.getByLabel('密码').fill('correct-password')
  await page.getByRole('button', { name: '登录' }).click()
  await page.getByText('用户管理', { exact: true }).first().click()
  await expect(page.getByRole('heading', { name: '用户管理' })).toBeVisible()
  await page.locator('button').filter({ hasText: '新增用户' }).click()
  await expect(page).toHaveURL(/\/admin\/users\/new$/)
  await page.getByLabel('登录账号').fill('nurse01')
  await page.getByLabel('员工姓名').fill('测试员工')
  await page.getByRole('button', { name: /保\s*存/ }).click()
  await expect(page.getByText('临时密码（仅显示一次）')).toBeVisible()
  await page.getByRole('button', { name: '继续分配角色' }).click()
  await expect(page).toHaveURL(/\/admin\/users\/2\/edit$/)
  await page.getByRole('button', { name: '← 返回用户列表' }).click()
  await expect(page).toHaveURL(/\/admin\/users$/)
  await expect(page.getByText('nurse01', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '查看', exact: true }).click()
  await expect(page).toHaveURL(/\/admin\/users\/2\/view$/)
  await expect(page.getByRole('heading', { name: '查看用户' })).toBeVisible()
  await expect(page.getByRole('button', { name: /保\s*存/ })).toHaveCount(0)
  await page.getByRole('button', { name: '← 返回用户列表' }).click()
  await page.getByRole('button', { name: '编辑', exact: true }).click()
  await expect(page).toHaveURL(/\/admin\/users\/2\/edit$/)
  await expect(page.getByRole('button', { name: /保\s*存/ })).toBeVisible()
  await page.getByRole('button', { name: '← 返回用户列表' }).click()
  await page.getByLabel('账号或姓名').fill('不存在')
  await page.getByRole('button', { name: /查\s*询/ }).click()
  await expect(page.getByText('共 0 条')).toBeVisible()
  await page.getByText('角色与权限', { exact: true }).first().click()
  await expect(page.getByRole('heading', { name: '角色与权限' })).toBeVisible()
  await page.locator('button').filter({ hasText: '新增角色' }).click()
  await expect(page).toHaveURL(/\/admin\/roles\/new$/)
  await page.getByLabel('角色编码').fill('NURSE')
  await page.getByLabel('角色名称').fill('护士')
  await page.getByRole('button', { name: /保\s*存/ }).click()
  await expect(page).toHaveURL(/\/admin\/roles\/3\/edit$/)
  await expect(page.getByText('权限分配')).toBeVisible()
  await expect(page.getByRole('button', { name: /关\s*闭/ })).toBeVisible()
  await page.screenshot({ path: join(tmpdir(), 'hospital-pharma-mes-iam-browser.png') })
  expect(pageErrors).toEqual([])
})
