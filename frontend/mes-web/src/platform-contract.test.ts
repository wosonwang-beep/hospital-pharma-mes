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

test('registers only the two frozen MES-001 routes', () => {
  const paths = router.getRoutes().map(route => route.path)
  expect(paths).toContain('/audit')
  expect(paths).toContain('/integration/operations')
  expect(paths).not.toContain('/platform/operations')
})

test('production UI source excludes removed contracts and raw message payloads', () => {
  const source = sourceFiles(join(process.cwd(), 'src')).map(path => readFileSync(path, 'utf8')).join('\n')
  expect(source).not.toContain('/platform/operations')
  expect(source).not.toContain('PLAT-001')
  expect(source).not.toMatch(/\bpayload\b/i)
})
