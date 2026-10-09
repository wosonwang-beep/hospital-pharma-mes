import {handleTree} from '../../third-party/ruoyi/handleTree'
export interface MenuEntry { id: string; menuCode: string; menuName: string; routePath: string | null; parentId: string | null; sortNo: number; status: 'ACTIVE' | 'INACTIVE'; version: number; permissionCode?: string | null; requiredPermissions?: string | null; permissionCodes?: string[] }
export interface CatalogNode { key: string; title: string; entry: MenuEntry; children: CatalogNode[] }
export function catalogTree(entries: MenuEntry[]): CatalogNode[] {
 return handleTree([...entries].sort((a,b)=>a.sortNo-b.sortNo).map(entry=>({id:entry.id,parentId:entry.parentId,key:entry.id,title:entry.menuName,entry})))
}
export function descendants(id: string, entries: MenuEntry[]): MenuEntry[] {
  const result: MenuEntry[] = [], seen = new Set<string>()
  function visit(key: string) { if (seen.has(key)) return; seen.add(key); const entry=entries.find(m=>m.id===key); if (entry) result.push(entry); entries.filter(m=>m.parentId===key).forEach(m=>visit(m.id)) }
  visit(id); return result
}
