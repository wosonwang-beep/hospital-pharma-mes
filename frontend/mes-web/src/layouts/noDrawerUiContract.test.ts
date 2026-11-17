import { describe, expect, it } from 'vitest'
import { readdirSync, readFileSync } from 'node:fs'
import { join, resolve } from 'node:path'

const root = resolve(process.cwd(), 'src')
function vueFiles(dir: string): string[] {
  return readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const path = join(dir, entry.name)
    return entry.isDirectory() ? vueFiles(path) : entry.isFile() && entry.name.endsWith('.vue') ? [path] : []
  })
}

describe('MES approved no-side-drawer interaction contract', () => {
  it('uses T1–T6 pages, independent routes or centered dialogs instead of sliding drawers', () => {
    const violations = vueFiles(root).filter(path => /<a-drawer\b|<Drawer\b/i.test(readFileSync(path, 'utf8')))
    expect(violations).toEqual([])
  })
})