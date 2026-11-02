import {afterEach,describe,it,expect,vi} from 'vitest'
import {render,screen,fireEvent,cleanup,waitFor} from '@testing-library/vue'
import Antd from 'ant-design-vue'
import {createPinia} from 'pinia'
import {reactive} from 'vue'
import DefinitionDesigner from './DefinitionDesigner.vue'
import {emptyDefinition,clone} from './model'

const boundary=vi.hoisted(()=>({editor:null as any}))
vi.mock('@hufe921/canvas-editor',()=>({
 default:class {
  listener:any={};data:any;range={startIndex:-1,endIndex:-1}
  constructor(_host:any,data:any){this.data=JSON.parse(JSON.stringify(data));boundary.editor=this}
  command={executeMode:vi.fn(),getValue:()=>({version:'1',data:this.data}),getRange:()=>this.range,
   executeSetRange:(startIndex:number,endIndex:number)=>{this.range={startIndex,endIndex}},
   executeInsertControl:(element:any)=>{this.data.main.push(JSON.parse(JSON.stringify(element)))},
   executeSetControlProperties:({conceptId,properties}:any)=>{for(const element of this.data.main)if(element.control?.conceptId===conceptId)Object.assign(element.control,properties)}}
  destroy(){}
 },ControlType:{TEXT:'text'},EditorMode:{DESIGN:'design',FORM:'form',READONLY:'readonly'},ElementType:{CONTROL:'control'},RowFlex:{CENTER:'center'}
}))
vi.mock('../../api/ebrRoles',()=>({ebrRoleOptions:vi.fn().mockResolvedValue({items:[],total:0,page:0,size:100})}))
afterEach(()=>{cleanup();boundary.editor=null})
async function setup(){
 const definition=reactive(emptyDefinition())
 definition.forms.push({formCode:'OBS_RECORD',formName:'操作观察记录',operationDefId:'12',schemaVersion:'1.0',sequenceNo:1,fields:[]})
 render(DefinitionDesigner,{props:{definition,savedDefinition:clone(definition),editable:true,operationChoices:[]},global:{plugins:[Antd,createPinia()],stubs:{ProcessLookup:true}}})
 await waitFor(()=>expect(boundary.editor).toBeTruthy())
 return definition
}
describe('new field identity and keyboard placement',()=>{
 it('establishes a canvas position through keyboard focus before field insertion',async()=>{
  const definition=await setup()
  await fireEvent.focus(screen.getByLabelText('表单画布'))
  await fireEvent.click(screen.getByRole('button',{name:'插入文本输入'}))
  expect(definition.forms[0]!.fields).toHaveLength(1)
  expect(screen.getByText('字段编码')).toBeTruthy()
 })
 it('keeps selection and Canvas identity synchronized when a new field code changes',async()=>{
  const definition=await setup()
  boundary.editor.range={startIndex:0,endIndex:0}
  const add=screen.getByRole('button',{name:'插入文本输入'})
  await fireEvent.pointerDown(add);await fireEvent.click(add)
  const code=screen.getByText('字段编码').parentElement!.querySelector('input')!
  await fireEvent.update(code,'OBS_ADDITIONAL')
  expect(definition.forms[0]!.fields[0]!.fieldCode).toBe('OBS_ADDITIONAL')
  expect(screen.getByText('字段名称')).toBeTruthy()
  const control=boundary.editor.data.main.find((x:any)=>x.control)?.control
  expect(control.conceptId).toBe('OBS_ADDITIONAL')
  expect(control.extension.ebrFieldCode).toBe('OBS_ADDITIONAL')
 })
})
