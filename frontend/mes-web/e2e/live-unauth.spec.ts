import { expect, test } from '@playwright/test'

test('real backend is reachable through the frontend proxy and protects administration', async ({ page }) => {
  test.skip(process.env.MES_E2E_LIVE_BACKEND !== 'true', 'Requires a running local backend')
  const pageErrors: string[] = []
  page.on('pageerror', error => pageErrors.push(error.message))

  const response = await page.request.get('/api/v1/admin/users')
  expect(response.status()).toBe(401)

  await page.goto('/admin/users')
  await expect(page).toHaveURL(/\/login\?redirect=/)
  await expect(page.getByRole('heading', { name: '医院制剂生产管理系统' })).toBeVisible()
  expect(pageErrors).toEqual([])
})
