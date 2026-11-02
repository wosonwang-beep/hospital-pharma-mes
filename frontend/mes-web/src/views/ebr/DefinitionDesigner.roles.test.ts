import {afterEach,describe,it,expect,vi} from 'vitest'
import {render,screen,fireEvent,cleanup,waitFor,within} from '@testing-library/vue'
import Antd from 'ant-design-vue'
import {createPinia} from 'pinia'
import {reactive} from 'vue'
import DefinitionDesigner from './DefinitionDesigner.vue'
import {emptyDefinition,definitionForSave,clone} from './model'
import {ebrRoleOptions} from '../../api/ebrRoles'

// Canvas uses native layout unavailable in jsdom. Role controls, Vue state and
// save serialization stay real; only the external Canvas/HTTP boundaries are isolated.
vi.mock('@hufe921/canvas-editor',()=>({
 default:class {listener={};command={executeMode:vi.fn(),getValue:()=>({data:{main:[]}})};destroy(){}},
 ControlType:{},EditorMode:{DESIGN:'design',FORM:'form',READONLY:'readonly'},ElementType:{},RowFlex:{CENTER:'center'}
}))
vi.mock('../../api/ebrRoles',()=>({ebrRoleOptions:vi.fn()}))
afterEach(()=>{cleanup();vi.clearAllMocks()})
const roles=[{id:'404',code:'QUALITY_MANAGER',name:'质量经理',enabled:true}]
function setup(editable=true){
 const definition=reactive(emptyDefinition())
 definition.forms.push({formCode:'STER_RECORD',formName:'灭菌记录',operationDefId:'12',schemaVersion:'1.0',sequenceNo:1,fields:[]})
 vi.mocked(ebrRoleOptions).mockResolvedValue({items:roles,total:1,page:0,size:100})
 const renderDesigner=()=>render(DefinitionDesigner,{props:{definition,savedDefinition:clone(definition),editable,operationChoices:[]},global:{plugins:[Antd,createPinia()],stubs:{ProcessLookup:true}}})
 return {definition,renderDesigner}
}
describe('designer signature and review role integration',()=>{
 it('new signature and review rules select an enabled role ID and serialize a valid schema2 draft',async()=>{
  const {definition,renderDesigner}=setup();renderDesigner()
  await fireEvent.click(screen.getByRole('tab',{name:'规则与签名'}))
  for(const title of ['电子签名规则','复核规则'])await fireEvent.click(screen.getByText(title).parentElement!.querySelector('button')!)
  const selectors=await screen.findAllByRole('combobox',{name:'签署或复核角色'})
  for(const selector of selectors){
   await fireEvent.mouseDown(selector)
   const popup=await waitFor(()=>{
    const dropdown=document.getElementById(selector.getAttribute('aria-controls')!)?.closest('.ant-select-dropdown')
    if(!dropdown)throw Error('当前角色下拉菜单尚未打开')
    return dropdown as HTMLElement
   })
   await fireEvent.click(await within(popup).findByText('质量经理（QUALITY_MANAGER）'))
  }
  const saved=definitionForSave(definition)
  expect(saved.schemaVersion).toBe('2.0')
  expect(saved.signatureRules[0]!.requiredRoleId).toBe('404')
  expect(saved.reviewRules[0]!.requiredRoleId).toBe('404')
  expect(saved.signatureRules[0]).not.toHaveProperty('requiredRole')
  expect(saved.reviewRules[0]).not.toHaveProperty('requiredRole')
 })
 it('readonly templates show the frozen role name without querying current roles',async()=>{
  const {definition,renderDesigner}=setup(false)
  definition.signatureRules.push({objectScope:'FORM',objectCode:'STER_RECORD',meaning:'VERIFY',requiredRoleId:'404',roleCodeSnapshot:'QA',roleNameSnapshot:'原质量复核岗位',reauthRequired:true,sequenceNo:1,invalidateOnChange:true})
  renderDesigner();await fireEvent.click(screen.getByRole('tab',{name:'规则与签名'}))
  expect(await screen.findByText('原质量复核岗位（QA）')).toBeTruthy()
  expect(screen.queryByRole('combobox',{name:'签署或复核角色'})).toBeNull()
  expect(ebrRoleOptions).not.toHaveBeenCalled()
 })
 it('role lookup failures keep configuration and allow retry without a legacy-code fallback',async()=>{
  const {definition,renderDesigner}=setup()
  vi.mocked(ebrRoleOptions).mockRejectedValueOnce(new Error('角色服务暂不可用'))
  renderDesigner();await fireEvent.click(screen.getByRole('tab',{name:'规则与签名'}))
  await fireEvent.click(await screen.findByRole('button',{name:'重试加载角色'}))
  await waitFor(()=>expect(ebrRoleOptions).toHaveBeenCalledTimes(2))
  expect(definition.signatureRules).toEqual([])
  expect(definition.reviewRules).toEqual([])
 })
})
