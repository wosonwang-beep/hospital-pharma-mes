import type { Permission } from '../../api/http'
export const ordinaryActions = new Set(['view','create','update','edit','delete','remove'])
export function isOrdinaryPermission(p: Permission, viewCode?: string | null) {
  const parts=p.permissionCode.split(':')
  return p.permissionCode===viewCode || parts.length===3 && !['menu','action'].includes(parts[0]!) && ordinaryActions.has(parts[2]!)
}
export function permissionChanges(draft: Permission[], original: Permission[], codes: string[]) {
  return draft.filter(p=>codes.includes(p.permissionCode)).flatMap(p=>{
    const old=original.find(x=>x.permissionCode===p.permissionCode)
    if(old && JSON.stringify([old.permissionName,old.description??'',old.status])===JSON.stringify([p.permissionName,p.description??'',p.status]))return []
    return [{id:old?.id??null,version:old?.version??null,permissionCode:p.permissionCode,permissionName:p.permissionName,description:p.description??'',status:p.status}]
  })
}
