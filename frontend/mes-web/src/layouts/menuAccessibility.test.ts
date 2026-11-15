import {afterEach,expect,it,vi} from 'vitest'
import {menuAccessibility} from './menuAccessibility'
const mount=menuAccessibility.mounted as (el:HTMLElement)=>void,unmount=menuAccessibility.beforeUnmount as (el:HTMLElement)=>void
const flush=()=>new Promise(resolve=>setTimeout(resolve,0))
let root:HTMLElement
afterEach(()=>{if(root)unmount(root);document.body.innerHTML=''})
it('restores native submenu semantics without changing labels, selection or events',async()=>{
 root=document.createElement('nav');root.innerHTML='<ul role="menu"><li role="none"><div class="ant-menu-submenu-title" aria-expanded="true">基础管理</div><ul class="ant-menu-sub"><li role="menuitem" data-key="process">生产工艺</li></ul></li></ul>';document.body.append(root)
 const handler=vi.fn();root.querySelector('[data-key]')!.addEventListener('click',handler);mount(root)
 expect(root.querySelector('.ant-menu-submenu-title')!.getAttribute('role')).toBe('menuitem');expect(root.querySelector('.ant-menu-sub')!.getAttribute('role')).toBe('menu');expect(root.textContent).toBe('基础管理生产工艺');(root.querySelector('[data-key]') as HTMLElement).click();expect(handler).toHaveBeenCalledOnce()
 root.querySelector('.ant-menu-submenu-title')!.removeAttribute('role');await flush();expect(root.querySelector('.ant-menu-submenu-title')!.getAttribute('role')).toBe('menuitem')
 unmount(root);root.querySelector('.ant-menu-submenu-title')!.removeAttribute('role');await flush();expect(root.querySelector('.ant-menu-submenu-title')!.hasAttribute('role')).toBe(false)
})
