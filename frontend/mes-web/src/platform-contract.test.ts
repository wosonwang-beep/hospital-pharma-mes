import { readFileSync, readdirSync, statSync } from 'node:fs'
import { join } from 'node:path'
import router from './router'

function sourceFiles(directory: string): string[] {
  return readdirSync(directory).flatMap(name => {
    const path = join(directory, name)
    if (name.endsWith('.test.ts')) return []
    return statSync(path).isDirectory() ? sourceFiles(path) : [path]
  })
}

test('preserves MES-001 routes and adds only frozen MES-002 administration routes', () => {
  const paths = router.getRoutes().map(route => route.path)
  expect(paths).toContain('/audit')
  expect(paths).toContain('/integration/operations')
  expect(paths).toEqual(expect.arrayContaining([
    '/admin/users', '/admin/users/create', '/admin/users/:id', '/admin/users/:id/edit',
    '/admin/roles', '/admin/roles/create', '/admin/roles/:id', '/admin/roles/:id/edit'
  ]))
  expect(paths).not.toContain('/platform/operations')
})

test('production UI source excludes removed contracts and raw message payloads', () => {
  const source = sourceFiles(join(process.cwd(), 'src')).map(path => readFileSync(path, 'utf8')).join('\n')
  expect(source).not.toContain('/platform/operations')
  expect(source).not.toContain('PLAT-001')
  expect(source).not.toMatch(/\bpayload\b/i)
})
