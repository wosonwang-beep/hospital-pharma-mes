import {describe,it,expect} from 'vitest'
import {deviationInitial,parameterRange} from './workbenchFacts'
describe('workbench factual presentation',()=>{
 it('compares exact decimal boundaries without float rounding',()=>{
  const def={unitId:'10',lowerLimit:'9007199254740993.00000001',upperLimit:'9007199254740993.00000003'}
  expect(parameterRange({unitId:'10',rawValue:'9007199254740993.00000001'},def).text).toBe('范围内')
  expect(parameterRange({unitId:'10',rawValue:'9007199254740993.00000004'},def).text).toBe('超范围')
  expect(parameterRange({unitId:'10',rawValue:'-0.00000002'},{unitId:'10',lowerLimit:'-0.00000001'}).text).toBe('超范围')
 })
 it('never invents conversions, standards or conclusions for invalid data',()=>{
  expect(parameterRange({unitId:'11',rawValue:'52'},{unitId:'10',upperLimit:'60'}).text).toBe('待单位换算')
  expect(parameterRange({rawValue:'25'},{}).text).toBe('未设范围')
  expect(parameterRange({rawValue:'25'}).text).toBe('待核对标准')
  expect(parameterRange({unitId:'10',rawValue:'NaN'},{unitId:'10',upperLimit:'60'}).text).toBe('待核对数值')
 })
 it('only prefills supported scope/kind and valid context identifiers',()=>{
  expect(deviationInitial({mainBatchId:'100',operationExecutionId:'103',description:'injected'})).toEqual({investigationScope:'PRODUCTION',investigationKind:'DEVIATION',mainBatchId:'100',operationExecutionId:'103'})
  expect(deviationInitial({mainBatchId:['100'],operationExecutionId:'-1'})).toEqual({investigationScope:'PRODUCTION',investigationKind:'DEVIATION'})
 })
})
