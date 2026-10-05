import {render,screen} from '@testing-library/vue'
import {describe,it,expect} from 'vitest'
import IncomingFacts from './IncomingFacts.vue'

describe('Incoming read-only business facts',()=>{
 it('labels the actual frozen QC snapshot fields',()=>{
  render(IncomingFacts,{props:{value:{lowerLimit:'95.000000',upperLimit:'105.000000',textAcceptanceCriteria:'澄清',revisionNo:2}}})
  expect(screen.getByText('标准下限')).toBeTruthy()
  expect(screen.getByText('标准上限')).toBeTruthy()
  expect(screen.getByText('文本判定标准')).toBeTruthy()
  expect(screen.getByText('结果修订版本')).toBeTruthy()
 })
 it('resolves references in nested results without changing the original fact',()=>{
  const value={items:[{unitId:'11',executions:[{performedBy:'8',revisions:[{resultConclusion:'FAIL',reasonForChange:'原始事实'}]}]}]}
  render(IncomingFacts,{props:{value,valueFormatter:(key:string,v:unknown)=>key==='unitId'?'毫克（ID 11）':key==='performedBy'?'检验员（ID 8）':undefined}})
  expect(screen.getByText('毫克（ID 11）')).toBeTruthy()
  expect(screen.getByText('检验员（ID 8）')).toBeTruthy()
  expect(screen.getByText('不合格')).toBeTruthy()
  expect(value.items[0]!.executions[0]!.revisions[0]!.resultConclusion).toBe('FAIL')
 })
})
