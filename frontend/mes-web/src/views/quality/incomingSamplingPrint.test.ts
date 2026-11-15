import {describe,expect,it} from 'vitest'

import {samplingPrintVisible} from './incomingPrintPolicy'

describe('sampling print visibility',()=>{
 it('requires both document generation and sampling read permission',()=>{
  expect(samplingPrintVisible('sampling-tasks',permission=>['print:document:generate','qms:sampling:view'].includes(permission))).toBe(true)
  expect(samplingPrintVisible('sampling-tasks',permission=>permission==='print:document:generate')).toBe(false)
  expect(samplingPrintVisible('sampling-tasks',permission=>permission==='qms:sampling:view')).toBe(false)
  expect(samplingPrintVisible('inspection-reports',()=>true)).toBe(false)
 })
})
