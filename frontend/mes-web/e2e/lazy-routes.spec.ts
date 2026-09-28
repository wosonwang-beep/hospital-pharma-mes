import { expect, test } from '@playwright/test'

test('login does not download administration page modules', async ({ page }) => {
  const adminModules: string[] = []
  page.on('request', request => {
    if (request.url().includes('/src/views/admin/')) adminModules.push(request.url())
  })
  await page.route('**/api/v1/**', route => route.fulfill({ status: 401, contentType: 'application/json', body: '{"code":"UNAUTHORIZED","message":"Unauthorized","data":null,"traceId":"e2e"}' }))

  await page.goto('/login', { waitUntil: 'networkidle' })
  await expect(page.getByRole('heading', { name: '医院制剂生产管理系统' })).toBeVisible()
  expect(adminModules).toEqual([])
})
