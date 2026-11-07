import {afterEach,describe,expect,it} from 'vitest'
import {selectAccessibility} from './selectAccessibility'

const flush=()=>new Promise(resolve=>setTimeout(resolve,0))
const mounted=selectAccessibility.mounted as (root:HTMLElement)=>void
const unmount=selectAccessibility.beforeUnmount as (root:HTMLElement)=>void
let root:HTMLElement
afterEach(()=>{if(root)unmount(root);document.body.innerHTML=''})
function setup(){
 root=document.createElement('section')
 root.innerHTML='<div class="ant-select"><input role="combobox" aria-activedescendant="option-0"></div>'
 document.body.append(root);mounted(root)
 return {select:root.firstElementChild!,input:root.querySelector('input')!}
}
describe('Ant Select accessibility adapter',()=>{
 it('describes collapsed state without dangling active option',()=>{
  const {input}=setup()
  expect(input.getAttribute('aria-expanded')).toBe('false')
  expect(input.hasAttribute('aria-activedescendant')).toBe(false)
 })
 it('restores the real active option after a teleported list mounts and tracks navigation',async()=>{
  const {select,input}=setup();select.classList.add('ant-select-open')
  document.body.insertAdjacentHTML('beforeend','<div role="listbox"><div id="option-0" role="option">生产</div><div id="option-1" role="option">IPC</div></div>')
  await flush();expect(input.getAttribute('aria-expanded')).toBe('true')
  expect(input.getAttribute('aria-activedescendant')).toBe('option-0')
  input.setAttribute('aria-activedescendant','option-1');await flush()
  expect(input.getAttribute('aria-activedescendant')).toBe('option-1')
  select.classList.remove('ant-select-open');await flush()
  expect(input.getAttribute('aria-expanded')).toBe('false')
  expect(input.hasAttribute('aria-activedescendant')).toBe(false)
  select.classList.add('ant-select-open');await flush()
  expect(input.getAttribute('aria-activedescendant')).toBe('option-1')
 })
 it('does not write another surface or move focus',async()=>{
  const {input}=setup();const other=document.createElement('input')
  other.setAttribute('role','combobox');document.body.append(other);other.focus()
  await flush();expect(other.hasAttribute('aria-expanded')).toBe(false)
  expect(document.activeElement).toBe(other);expect(input.value).toBe('')
 })
 it('disconnects observers when its surface unmounts',async()=>{
  const {input}=setup();unmount(root);input.removeAttribute('aria-expanded')
  await flush();expect(input.hasAttribute('aria-expanded')).toBe(false)
 })
 it('owns only an existing open popup and restores that relation on reopening',async()=>{
  const {select,input}=setup();input.setAttribute('aria-owns','live-options');await flush();expect(input.hasAttribute('aria-owns')).toBe(false)
  select.classList.add('ant-select-open');await flush();expect(input.hasAttribute('aria-owns')).toBe(false)
  document.body.insertAdjacentHTML('beforeend','<div id="live-options" role="listbox"></div>');await flush();expect(input.getAttribute('aria-owns')).toBe('live-options')
  select.classList.remove('ant-select-open');await flush();expect(input.hasAttribute('aria-owns')).toBe(false)
  select.classList.add('ant-select-open');await flush();expect(input.getAttribute('aria-owns')).toBe('live-options')
 })
})
