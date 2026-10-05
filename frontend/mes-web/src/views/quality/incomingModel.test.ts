import {describe,it,expect} from 'vitest'
import {commandBody,schemas,initial,selectSigningTarget,reviewResultIds,referenceRows,serverAllows} from './incomingModel'
describe('Incoming controlled command forms',()=>{
 it('fails closed without server actions and never infers actions from status',()=>{
  expect(serverAllows({status:'DRAFT'},'submit')).toBe(false)
  expect(serverAllows({allowedActions:[]},'submit')).toBe(false)
  expect(serverAllows({allowedActions:['submit']},'submit')).toBe(true)
  expect(serverAllows({allowedActions:['submit']},'accept')).toBe(false)
 })
 it('preserves inert original JSON data and rejects executable expressions or arrays',()=>{
  expect(commandBody({type:'object',additionalProperties:true},'{"readings":[1,2],"note":"original"}')).toEqual({readings:[1,2],note:'original'})
  expect(()=>commandBody({type:'object',additionalProperties:true},'window.alert(1)')).toThrow()
  expect(()=>commandBody({type:'object',additionalProperties:true},'[1,2]')).toThrow()
 })
 it('keeps exact decimal strings, strips signing secrets, and enforces required input',()=>{
  const body=commandBody(schemas.TestResultRevisionCommand!,{testExecutionId:'8',resultNumeric:'1234567890123456.12345678',resultUnitId:'1',resultConclusion:'FAIL',reauthToken:'secret'}) as Record<string,unknown>
  expect(body.resultNumeric).toBe('1234567890123456.12345678');expect(body).not.toHaveProperty('reauthToken')
  expect(()=>commandBody(schemas.InspectionReportCommand!,{reason:'Missing request'})).toThrow()
  expect(commandBody(schemas.InspectionReportCommand!,{inspectionRequestId:'1',reason:'Aggregate',items:[{resultNumeric:'1'}]})).toEqual({inspectionRequestId:'1',reason:'Aggregate'})
 })
 it('defaults scope discriminator and initializes safe raw object editors',()=>{
  expect((initial(schemas.IncomingDeviationCommand!) as Record<string,unknown>).investigationScope).toBe('INCOMING_MATERIAL')
  expect(initial({type:'object',additionalProperties:true})).toBe('{}')
 })
 it('chooses exact signature meaning and immediate next result target',()=>{
  const targets=[{objectType:'MATERIAL_RELEASE_DECISION',recordId:'9:3',recordVersion:3,meaning:'RELEASE',action:'release'},{objectType:'MATERIAL_RELEASE_DECISION',recordId:'9:3',recordVersion:3,meaning:'REJECT',action:'release'}]
  expect(selectSigningTarget(targets,'release-decisions','REJECTED')?.meaning).toBe('REJECT')
  expect(selectSigningTarget(targets,'results')).toBeUndefined()
  expect(reviewResultIds({items:[{executions:[{revisions:[{id:'10'},{id:'11'}]},{revisions:[{id:'12'}]}]}]})).toEqual(['11','12'])
 })
 it('shows order/batch numbers while submitting exact source identities',()=>{
  const order={id:'41',orderNo:'PO-2026-041',status:'DRAFT'}
  const option=referenceRows('productionOrderId',[order])[0]!
  expect(option.value).toBe('41');expect(option.label).toBe('PO-2026-041 · 草稿');expect(option.record).toBe(order)
  expect(referenceRows('mainBatchId',[{id:'42',batchNo:'MB-2026-042'}])[0]!.label).toBe('MB-2026-042')
 })
 it('builds references from controlled facts rather than free-form IDs',()=>{
  expect(referenceRows('resultRevisionIds',[{items:[{executions:[{revisions:[{id:'10',resultConclusion:'FAIL'},{id:'11',resultConclusion:'FAIL'}]}]}]}]).map(x=>x.value)).toEqual(['11'])
 })
})
