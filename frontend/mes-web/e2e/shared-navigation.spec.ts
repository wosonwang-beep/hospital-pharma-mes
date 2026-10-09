import { test, expect, type Page } from '@playwright/test'
import { mkdirSync } from 'node:fs'
import { resolve } from 'node:path'
import { navigationConfiguration, navigationHome } from '../src/layouts/navigation'

const output = resolve('../../docs/acceptance/shared-navigation-2026-10-07/screenshots')
const fullPermissions = [...new Set(navigationConfiguration.flatMap(group => group.items.flatMap(item => [item.permission!, ...(item.requiredPermissions??[])])))].concat(['mes:operation:view'])
const titles = navigationConfiguration.map(group => group.title)
test.use({ viewport: { width: 1440, height: 900 } })

async function fixture(page: Page, permissions = fullPermissions) {
  const errors: string[] = []
  const writes: string[] = []
  page.on('pageerror', error => errors.push(error.message))
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
  await page.route('**/api/v1/**', route => {
    const request = route.request(), path = new URL(request.url()).pathname.replace('/api/v1', '')
    if (request.method() !== 'GET') writes.push(path)
    let data: unknown = { items: [], total: 0, page: 0, size: 20 }
    if (path === '/auth/navigation') data = [{id:'home',menuCode:'nav_home',title:navigationHome.title,path:'/',permissionCode:'menu:home',requiredPermissions:[],children:[]},...navigationConfiguration.map(group=>({id:group.key,menuCode:`nav_${group.key}`,title:group.title,path:null,permissionCode:null,requiredPermissions:[],children:group.items.filter(item=>(!item.permission || permissions.includes(item.permission)) && (item.requiredPermissions??[]).every(code=>permissions.includes(code))).map(item=>({id:item.key,menuCode:`nav_${item.key}`,title:item.title,path:item.path,permissionCode:item.permission??null,requiredPermissions:item.requiredPermissions??[],children:[]}))})).filter(group=>group.children.length)]
    if (path === '/auth/me') data = { userId: '8', organizationId: '1', displayName: '导航验证员', permissionCodes: permissions, mustChangePassword: false }
    if (path === '/main-batches/100') data = { id: '100', batchNo: 'PB-NAV-001', productId: '20', status: 'IN_PROGRESS', plannedQty: '100', unitId: '10', versionNo: 1, allowedActions: [], processSnapshot: { snapshot: { process: { route: { operations: [] }, formula: { items: [] } } } } }
    if (path === '/execution-units/200') data = { id: '200', executionUnitNo: 'EU-NAV-001', mainBatchId: '100', status: 'IN_PROGRESS', allowedActions: [] }
    if (path === '/execution-units/200/operations'||path === '/main-batches/100/execution-units') data = []
    return route.fulfill({ json: { code: 'OK', data, traceId: 'navigation-ui-fixture' } })
  })
  return { errors, writes }
}

test('home, ordinary, Production Batch and T5 share the same menu presentation and destinations', async ({ page }) => {
  mkdirSync(output, { recursive: true })
  const result = await fixture(page)
  for (const [name, path] of [['home', '/'], ['ordinary', '/master/materials'], ['batch-t4', '/production/batches/100'], ['execution-t5', '/mes/execution/200']]) {
    await page.goto(path!)
    const menu = page.locator('.sidebar .nav')
    await expect(menu).toBeVisible()
    await expect(menu.locator('.ant-menu-submenu-title')).toHaveText(titles)
    await expect(page.locator('.execution-reference-navigation')).toHaveCount(0)
    await expect(page.locator('.sidebar')).toHaveCSS('width', '256px')
    await expect(page.locator('.dashboard-brand-copy strong')).toHaveText('医院制剂生产管理系统')
    await expect(menu.locator('.ant-menu-item .anticon-appstore')).toHaveCount(0)
    await expect(menu.locator('.ant-menu-item-group')).toHaveCount(0)
    await expect(menu.getByText(navigationHome.title, { exact: true })).toBeVisible()
    {
      for (const title of titles) {
        const header = menu.locator('.ant-menu-submenu-title').filter({ hasText: title })
        if (await header.getAttribute('aria-expanded') !== 'true') await header.click()
      }
    }
    await expect(menu.locator('.ant-menu-item')).toHaveText([navigationHome.title, ...navigationConfiguration.flatMap(group => group.items.map(item => item.title))])
    if (name === 'batch-t4' || name === 'execution-t5') {
      await expect(menu.locator('.ant-menu-item-selected')).toHaveText(name==='execution-t5'?'生产执行':'生产批')
      // Close non-current groups for a readable screenshot of the production shell.
      for (const title of titles.filter(title => title !== '生产管理')) await menu.locator('.ant-menu-submenu-title').filter({ hasText: title }).click()
    }
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
    await page.screenshot({ path: resolve(output, `${name}-desktop.png`), fullPage: true })
    const finished = menu.locator('.ant-menu-submenu-title').filter({ hasText: '成品管理' })
    if (await finished.getAttribute('aria-expanded') !== 'true') await finished.click()
    await menu.getByText('成品入库申请', { exact: true }).click()
    await expect(page).toHaveURL(/\/finished\/inbound/)
    const master = menu.locator('.ant-menu-submenu-title').filter({ hasText: '基础管理' })
    if (await master.getAttribute('aria-expanded') !== 'true') await master.click()
    await menu.getByText('单位换算', { exact: true }).click()
    await expect.poll(() => new URL(page.url()).pathname).toBe('/master/unit-conversions')
  }
  expect(result.errors).toEqual([])
  expect(result.writes).toEqual([])
})

test('restricted user gets the same permission-filtered domains on ordinary and contextual shells', async ({ page }) => {
  const result = await fixture(page, ['production:batch:view', 'mes:execution:view', 'mes:operation:view', 'wms:finished-inbound:view'])
  for (const path of ['/', '/production/batches', '/production/batches/100', '/mes/execution/200']) {
    await page.goto(path)
    const menu = page.locator('.sidebar .nav')
    await expect(menu.locator('.ant-menu-submenu-title')).toHaveText(['生产管理', '成品管理'])
    await expect(menu.getByText('系统管理', { exact: true })).toHaveCount(0)
    await expect(menu.getByText('成品发货出库', { exact: true })).toHaveCount(0)
  }
  expect(result.errors).toEqual([])
  expect(result.writes).toEqual([])
})
