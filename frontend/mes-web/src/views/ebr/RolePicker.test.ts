import {afterEach,describe,it,expect} from 'vitest'
import {render,screen,fireEvent,cleanup} from '@testing-library/vue'
import RolePicker from './RolePicker.vue'
import {definitionForSave,emptyDefinition,prepareDraftRoleIds,frozenRoleLabel} from './model'
afterEach(cleanup)
const options=[{id:'290',code:'MES_QC_REVIEWER',name:'QC复核岗位',enabled:true},{id:'291',code:'MES_QA',name:'QA岗位',enabled:false}]
describe('role ID control',()=>{
 it('keeps the saved Chinese snapshot visible when current options fail',()=>{
  const view=render(RolePicker,{props:{editable:true,options:[],roleId:'290',codeSnapshot:'OLD',nameSnapshot:'原复核岗位'}})
  expect(screen.getByText('原复核岗位（OLD）')).toBeTruthy()
  expect(view.emitted()['update:roleId']).toBeUndefined()
 })
 it('retains a newly selected role label across a filtered or failed option reload',async()=>{
  const view=render(RolePicker,{props:{editable:true,roleId:'290',codeSnapshot:'OLD',nameSnapshot:'原复核岗位',options:[...options.map(r=>({...r,enabled:true}))]}})
  await view.rerender({roleId:'291',options:[]})
  expect(screen.getByText('QA岗位（MES_QA）')).toBeTruthy()
  expect(screen.queryByText('原复核岗位（OLD）')).toBeNull()
  expect(view.emitted()['update:roleId']).toBeUndefined()
 })
 it('shows Chinese names and emits the selected ID',async()=>{
  const view=render(RolePicker,{props:{editable:true,options}})
  await fireEvent.mouseDown(screen.getByRole('combobox'))
  await fireEvent.click(await screen.findByText('QC复核岗位（MES_QC_REVIEWER）'))
  expect(view.emitted()['update:roleId']).toEqual([['290']])
  expect(screen.queryByText('QA岗位（MES_QA）')).toBeNull()
 })
 it('reads frozen display without loading current options',()=>{
  render(RolePicker,{props:{editable:false,options:[],roleId:'290',codeSnapshot:'OLD',nameSnapshot:'原复核岗位'}})
  expect(screen.getByTestId('frozen-role').textContent).toBe('原复核岗位（OLD）')
  expect(screen.queryByRole('combobox')).toBeNull()
 })
 it('keeps legacy published code readonly',()=>{
  render(RolePicker,{props:{editable:false,options,legacyCode:'HISTORICAL'}})
  expect(screen.getByTestId('frozen-role').textContent).toBe('HISTORICAL')
 })
 it('serializes only IDs and strips server snapshots',()=>{
  const d=emptyDefinition();d.signatureRules=[{objectScope:'FORM',objectCode:'F',meaning:'VERIFY',requiredRoleId:'290',roleCodeSnapshot:'OLD',roleNameSnapshot:'Old',reauthRequired:true,sequenceNo:1,invalidateOnChange:true}]
  const saved=definitionForSave(d)
  expect(saved.schemaVersion).toBe('2.0');expect(saved.signatureRules[0]!.requiredRoleId).toBe('290')
  expect(saved.signatureRules[0]).not.toHaveProperty('roleCodeSnapshot');expect(saved.signatureRules[0]).not.toHaveProperty('roleNameSnapshot')
  expect(d.signatureRules[0]!.roleCodeSnapshot).toBe('OLD')
 })
 it('blocks code-only schema2 saves',()=>{
  const d=emptyDefinition();d.reviewRules=[{objectScope:'FORM',objectCode:'F',reviewType:'VERIFY',requiredRole:'MES_QC_REVIEWER',independentUserRequired:true,sequenceNo:1}]
  expect(()=>definitionForSave(d)).toThrow('role ID')
 })
 it('converts a draft only through unique enabled role options',()=>{
  const d=emptyDefinition();delete d.schemaVersion;d.reviewRules=[{objectScope:'FORM',objectCode:'F',reviewType:'VERIFY',requiredRole:'MES_QC_REVIEWER',independentUserRequired:true,sequenceNo:1}]
  prepareDraftRoleIds(d,options);expect(d.reviewRules[0]!.requiredRoleId).toBe('290');expect(d.schemaVersion).toBe('2.0')
 })
 it('does not guess an ambiguous or disabled legacy reference',()=>{
  const d=emptyDefinition();delete d.schemaVersion;d.reviewRules=[{objectScope:'FORM',objectCode:'F',reviewType:'VERIFY',requiredRole:'MES_QA',independentUserRequired:true,sequenceNo:1}]
  prepareDraftRoleIds(d,options);expect(d.reviewRules[0]!.requiredRoleId).toBeUndefined();expect(()=>definitionForSave(d)).toThrow()
 })
 it('renders runtime history from frozen fields',()=>{expect(frozenRoleLabel({roleCodeSnapshot:'OLD',roleNameSnapshot:'Old label'})).toBe('Old label (OLD)')})
})
