import {describe,it,expect} from 'vitest'
import {traceRelationLabel} from './traceRelationLabels'
describe('human-readable immutable trace relation display',()=>{
 it('converts common relations without changing source codes',()=>{
  expect(traceRelationLabel('FINISHED_OUTPUT')).toBe('产生本批成品')
  expect(traceRelationLabel('ACTUAL_CHARGE')).toBe('记录实际投料')
  expect(traceRelationLabel('QA_DECISION')).toBe('QA 放行决定')
  expect(traceRelationLabel('SHIPPED_FROM')).toBe('从成品库存发货')
 })
 it('preserves unknown relationship meaning rather than inventing it',()=>{
  expect(traceRelationLabel('UNDEFINED_RELATION')).toBe('undefined relation')
 })
})
