import { expect, test, type Page } from '@playwright/test'

const envelope = (data: unknown) => ({ code: 'OK', message: 'success', data, traceId: 'e2e-trace' })

async function mockApi(page: Page) {
  let loggedIn = false
  await page.route('**/api/v1/**', async route => {
    const request = route.request()
    const path = new URL(request.url()).pathname
    if (path === '/api/v1/auth/login' && request.method() === 'POST') {
      loggedIn = true
      return route.fulfill({ json: envelope({ accessToken: 'e2e-token', expiresInSeconds: 900 }) })
    }
    if (path === '/api/v1/auth/me' && !loggedIn)
      return route.fulfill({ status: 401, json: { code: 'UNAUTHORIZED' } })
    if (path === '/api/v1/auth/me') return route.fulfill({ json: envelope({
      userId: '7', organizationId: '1', loginName: 'qa.admin', displayName: '质量管理员',
      roleCodes: ['SYSTEM_ADMIN'], permissionCodes: [
        'iam:user:view','iam:user:create','iam:user:update','iam:role:view','iam:role:create','iam:role:update','iam:permission:view',
        'audit:view','integration:view','integration:retry'
      ], mustChangePassword: false
    }) })
    if (path === '/api/v1/auth/refresh') return route.fulfill({ status: 401, json: { code: 'UNAUTHORIZED' } })
    if (path === '/api/v1/users') return route.fulfill({ json: envelope({ items: [
      { id: '21', username: 'operator.chen', displayName: '陈操作员', status: 'ACTIVE', version: 2, roleIds: ['1'] }
    ], total: 1, page: 0, size: 20 }) })
    if (path === '/api/v1/roles') return route.fulfill({ json: envelope({ items: [
      { id: '1', roleCode: 'SYSTEM_ADMIN', roleName: '系统管理员', status: 'ACTIVE', version: 1, permissionCodes: [], menuCodes: [] }
    ], total: 1, page: 0, size: 20 }) })
    if (path === '/api/v1/permissions') return route.fulfill({ json: envelope({ items: [], total: 0, page: 0, size: 100 }) })
    return route.fulfill({ status: 404, json: { code: 'NOT_FOUND' } })
  })
}

test('login exposes frozen IAM navigation and independent user routes', async ({ page }, testInfo) => {
  const errors: string[] = []
  page.on('console', message => {
    if (message.type() === 'error' && !message.text().includes('401 (Unauthorized)')) errors.push(message.text())
  })
  await mockApi(page)
  await page.goto('/login')
  await expect(page.getByRole('heading', { name: '医院制剂 MES' })).toBeVisible()
  await page.getByLabel('账号').fill('qa.admin')
  await page.getByLabel('密码').fill('Valid passphrase 12345')
  await page.getByRole('button', { name: /登\s*录/ }).click()
  await expect(page).toHaveURL(/\/$/)
  await expect(page.getByRole('menuitem', { name: '用户管理' })).toBeVisible()
  await page.getByRole('menuitem', { name: '用户管理' }).click()
  await expect(page).toHaveURL(/\/admin\/users$/)
  await expect(page.getByText('operator.chen')).toBeVisible()
  await page.getByRole('button', { name: '新增用户' }).click()
  await expect(page).toHaveURL(/\/admin\/users\/create$/)
  await expect(page.getByRole('heading', { name: '新增用户' })).toBeVisible()
  await page.screenshot({ path: testInfo.outputPath('mes002-user-create.png'), fullPage: false })
  expect(errors).toEqual([])
})
