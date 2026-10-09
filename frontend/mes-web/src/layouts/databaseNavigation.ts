export interface MenuNode { id: string; menuCode: string; title: string; path: string | null; permissionCode: string | null; requiredPermissions: string[]; children: MenuNode[] }
export function menuLeaves(nodes: MenuNode[]): MenuNode[] { return nodes.flatMap(node => [...(node.path ? [node] : []), ...menuLeaves(node.children)]) }
export function selectedMenu(path: string, nodes: MenuNode[]): string | undefined {
  const context = path.startsWith('/mes/execution/') ? '/production/execution' : /^\/production\/batches\/[^/]+\/balance$/.test(path) ? '/production/balances' : /^\/qa\/batches\/[^/]+\/review$/.test(path) ? '/finished/qa-reviews' : /^\/qa\/batches\/[^/]+\/release$/.test(path) ? '/finished/releases' : path
  return menuLeaves(nodes).filter(n => n.path === context || (n.path !== '/' && context.startsWith(`${n.path}/`))).sort((a,b) => b.path!.length-a.path!.length)[0]?.id
}
export function menuAncestors(key: string | undefined, nodes: MenuNode[]): string[] {
  for (const node of nodes) { if (node.id === key) return []; const path = menuAncestors(key,node.children); if (path.length || menuLeaves(node.children).some(child => child.id === key)) return [node.id,...path] }
  return []
}
