import { expect, test } from '@playwright/test'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

test('real backend login, password change, and administration pages', async ({ page }) => {
  const initialPassword = process.env.MES_E2E_ADMIN_PASSWORD
  const newPassword = process.env.MES_E2E_NEW_PASSWORD
  const loginName = process.env.MES_E2E_ADMIN_LOGIN || 'admin'
  test.skip(!initialPassword || !newPassword, 'Requires isolated live IAM test credentials')
  const pageErrors: string[] = []
  page.on('pageerror', error => pageErrors.push(error.message))

  await page.goto('/')
  await expect(page.getByRole('heading', { name: '医院制剂生产管理系统' })).toBeVisible()
  await page.getByLabel('账号').fill(loginName)
  await page.getByLabel('密码').fill(initialPassword!)
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/change-password$/)
  await page.getByLabel('当前密码').fill(initialPassword!)
  await page.getByLabel('新密码').fill(newPassword!)
  await page.getByRole('button', { name: '保存新密码' }).click()
  await expect(page).toHaveURL(/\/login$/)

  await page.getByLabel('账号').fill(loginName)
  await page.getByLabel('密码').fill(newPassword!)
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL('http://127.0.0.1:5173/')
  await page.getByText('角色与权限', { exact: true }).first().click()
  await expect(page.getByRole('heading', { name: '角色与权限' })).toBeVisible()
  await expect(page.getByText('SYSTEM_ADMIN', { exact: true })).toBeVisible()

  await page.getByText('用户管理', { exact: true }).first().click()
  await expect(page.getByRole('heading', { name: '用户管理' })).toBeVisible()
  await expect(page.getByText(loginName, { exact: true })).toBeVisible()
  await page.screenshot({ path: join(tmpdir(), 'hospital-pharma-mes-live-iam.png') })
  expect(pageErrors).toEqual([])
  await page.getByRole('button', { name: '退出' }).click()
  await expect(page).toHaveURL(/\/login$/)
})
