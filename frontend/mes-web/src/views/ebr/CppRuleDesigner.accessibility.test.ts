import {afterEach,describe,it,expect} from 'vitest'
import {render,screen,cleanup,fireEvent} from '@testing-library/vue'
import Antd from 'ant-design-vue'
import {createPinia} from 'pinia'
import {reactive} from 'vue'
import CppRuleDesigner from './CppRuleDesigner.vue'
import {emptyDefinition} from './model'
import type {EbrField} from './types'

afterEach(cleanup)
function setup(editable=true){
 const definition=reactive(emptyDefinition())
 const field:EbrField={fieldCode:'KCL_ASSAY',groupCode:null,label:'氯化钾含量',fieldType:'NUMBER',sourceType:'MANUAL',dataType:'DECIMAL',unitId:null,precisionScale:2,requiredFlag:true,readonlyFlag:false,defaultExpr:null,placeholder:null,helpText:null,sequenceNo:1,validationJson:null,options:[]}
 const form={formCode:'FILTER_RECORD',formName:'过滤记录',operationDefId:'12',schemaVersion:'1.0',sequenceNo:1,fields:[field]}
 definition.forms.push(form)
 const config={kind:'CPP',name:'含量范围',description:'含量范围应符合规定',rootLogic:'AND',groups:[{id:'G1',logic:'AND',conditions:[{id:'C1',fieldCode:'KCL_ASSAY',operator:'>=',value:'9.5'}]}],actions:['MARK_EXCEPTION','BLOCK_SUBMIT']}
 definition.rules.push({ruleCode:'CPP_FILTER_1',formCode:form.formCode,fieldCode:null,ruleType:'VALIDATION',triggerPoint:'ON_CHANGE',expression:"value('KCL_ASSAY') >= 9.5",severity:'BLOCK',errorCode:'CPP_RULE_FAILED',messageTemplate:'含量不符合要求',deviationTrigger:false,activeFlag:true,cppConfigJson:JSON.stringify(config)})
 render(CppRuleDesigner,{props:{definition,form,editable},global:{plugins:[Antd,createPinia()]}})
 return definition
}

describe('CPP input label association',()=>{
 it('names the actual combobox inputs without relying on a labelled wrapper',()=>{
  setup()
  for(const name of ['条件组1逻辑','条件1字段','条件1运算符']){
   const input=screen.getByRole('combobox',{name}) as HTMLInputElement
   expect(input.tagName).toBe('INPUT')
   expect(input.labels?.length).toBe(1)
  }
 })
 it('keeps distinct label targets when another condition group is added',async()=>{
  setup()
  await fireEvent.click(screen.getByText('添加条件组',{exact:true}).closest('button')!)
  const combos=screen.getAllByRole('combobox').filter(e=>e.closest('.cpp-builder')) as HTMLInputElement[]
  expect(combos).toHaveLength(6)
  expect(new Set(combos.map(e=>e.id)).size).toBe(6)
  expect(combos.every(e=>e.labels?.length===1)).toBe(true)
  expect(screen.getByRole('combobox',{name:'条件组2逻辑'})).toBeTruthy()
 })
 it('preserves accessible names on the existing readonly inputs',()=>{
  setup(false)
  for(const name of ['条件组1逻辑','条件1字段','条件1运算符']){
   const input=screen.getByRole('combobox',{name}) as HTMLInputElement
   expect(input.disabled).toBe(true)
  }
 })
})
