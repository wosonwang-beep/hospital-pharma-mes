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
async function setup(editable=true){
 const definition=reactive(emptyDefinition())
 definition.forms.push({formCode:'OBS_RECORD',formName:'操作观察记录',operationDefId:'12',schemaVersion:'1.0',sequenceNo:1,fields:[]})
 const view=render(DefinitionDesigner,{props:{definition,savedDefinition:clone(definition),editable,operationChoices:[]},global:{plugins:[Antd,createPinia()],stubs:{ProcessLookup:true}}})
 await waitFor(()=>expect(boundary.editor).toBeTruthy())
 return view
}
describe('font-size native label',()=>{
 it('names the real input in editable and readonly states',async()=>{
  const view=await setup()
  const first=screen.getByRole('combobox',{name:'字号'}) as HTMLInputElement
  expect(first.tagName).toBe('INPUT')
  expect(first.labels?.[0]?.textContent).toBe('字号')
  expect(first.disabled).toBe(false)
  await view.rerender({editable:false})
  const readonly=screen.getByRole('combobox',{name:'字号'}) as HTMLInputElement
  expect(readonly.id).toBe(first.id)
  expect(readonly.disabled).toBe(true)
 })
})
