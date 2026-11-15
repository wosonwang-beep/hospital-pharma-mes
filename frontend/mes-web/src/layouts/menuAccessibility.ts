import type {ObjectDirective} from 'vue'

// Ant Design Vue 4.2.6 SubMenu renders an interactive title div and a nested
// list without roles. Restore their menu semantics; retain its keyboard and
// event handling, DOM order, permission-filtered items and visual structure.
const cleanups=new WeakMap<HTMLElement,()=>void>()
export const menuAccessibility:ObjectDirective<HTMLElement>={
 mounted(root){
  let queued=false,disposed=false
  const sync=()=>{queued=false;if(disposed)return;for(const title of root.querySelectorAll('.ant-menu-submenu-title'))if(title.getAttribute('role')!=='menuitem')title.setAttribute('role','menuitem');for(const list of root.querySelectorAll('ul.ant-menu-sub'))if(list.getAttribute('role')!=='menu')list.setAttribute('role','menu')}
  const observer=new MutationObserver(()=>{if(!queued){queued=true;queueMicrotask(sync)}})
  observer.observe(root,{subtree:true,childList:true,attributes:true,attributeFilter:['role']});sync()
  cleanups.set(root,()=>{disposed=true;observer.disconnect()})
 },
 beforeUnmount(root){cleanups.get(root)?.();cleanups.delete(root)}
}
