import type { ObjectDirective } from 'vue'

// Ant Design Vue 4.2.6 initializes uncontrolled Select.open as undefined and
// references a lazily mounted active option even while collapsed. Adapt only
// Select inputs in the owning surface; never change selection or focus.
const cleanups = new WeakMap<HTMLElement, () => void>()
export const selectAccessibility: ObjectDirective<HTMLElement> = {
  mounted(root) {
    const activeIds = new WeakMap<HTMLInputElement, string>()
    const ownedIds = new WeakMap<HTMLInputElement, string>()
    let queued = false
    let disposed = false
    const sync = () => {
      queued = false
      if (disposed) return
      for (const input of root.querySelectorAll<HTMLInputElement>('.ant-select input[role="combobox"]')) {
        const expanded = input.closest('.ant-select')?.classList.contains('ant-select-open') === true
        const state = String(expanded)
        if (input.getAttribute('aria-expanded') !== state) input.setAttribute('aria-expanded', state)
        const owns=input.getAttribute('aria-owns')
        if(owns)ownedIds.set(input,owns)
        const owner=ownedIds.get(input)
        const ownedValid=expanded&&owner&&owner.split(/\s+/).every(id=>document.getElementById(id))
        if(!ownedValid&&owns)input.removeAttribute('aria-owns')
        else if(ownedValid&&!owns)input.setAttribute('aria-owns',owner!)
        const current = input.getAttribute('aria-activedescendant')
        if (current) activeIds.set(input, current)
        const candidate = activeIds.get(input)
        const valid = expanded && candidate && document.getElementById(candidate)
        if (!valid && current) input.removeAttribute('aria-activedescendant')
        else if (valid && !current) input.setAttribute('aria-activedescendant', candidate!)
      }
    }
    const schedule = () => {
      if (!queued) { queued = true; queueMicrotask(sync) }
    }
    // Popups can be teleported outside root. Child-list observation lets a
    // freshly mounted option be linked after the input's render completes.
    const observer = new MutationObserver(schedule)
    observer.observe(root, {subtree:true, childList:true, attributes:true, attributeFilter:['class','aria-expanded','aria-activedescendant','aria-owns']})
    const popups = new MutationObserver(schedule)
    popups.observe(document.body, {subtree:true, childList:true})
    sync()
    cleanups.set(root, () => { disposed = true; observer.disconnect(); popups.disconnect() })
  },
  beforeUnmount(root) { cleanups.get(root)?.(); cleanups.delete(root) },
}
