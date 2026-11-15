import {describe,it,expect} from 'vitest'
import {inspectionIdentityFacts,type ReferenceMap} from './inspectionIdentityFacts'
const record={sampleId:'7',startedAt:'2026-10-19T08:00:00Z'}
const request={receiptId:'1',receiptItemId:'2',requestedQuantity:'1.000',requestedDate:'2026-10-18'}
const lot={receiptItemId:'2',manufacturerLotNo:'P2026101802'}
function refs():ReferenceMap{return {
 'receiptId:1':{name:'收货单',row:{items:[{id:'2',receivedQty:'2.500000',unitId:'kg'}]}},
 'sampleId:7':{name:'检验样',row:{samplingTaskId:'8',quantity:'0.10000000',unitId:'kg',sampledAt:'2026-10-19T07:00:00Z'}},
 'samplingTaskId:8':{name:'取样单',row:{plannedSamplingQuantity:'0.12500000',plannedUnitId:'kg',details:[{sampleQuantity:'0.12500000',unitId:'kg'}]}},
 'unitId:kg':{name:'千克',row:{}},'unitId:g':{name:'克',row:{}}
}}
const find=(data:ReturnType<typeof inspectionIdentityFacts>,name:string)=>data.find(f=>f.label===name)?.value

describe('inspection source identity facts',()=>{
 it('distinguishes original received batch, requested amount, total sampling and test-sample amount',()=>{
  const data=inspectionIdentityFacts(record,request,lot,refs())
  expect(find(data,'物料批量')).toBe('2.500000 千克')
  expect(find(data,'实际总取样量')).toBe('0.125 千克')
  expect(find(data,'本任务检验样量')).toBe('0.10000000 千克')
  expect(find(data,'请验日期')).toBe('2026-10-18')
  expect(find(data,'生产批号')).toBe('P2026101802')
 })
 it('does not fabricate batch quantities when source or unit is unavailable',()=>{
  const data=inspectionIdentityFacts(record,request,lot,{})
  expect(find(data,'物料批量')).toBe('未记录')
  expect(find(data,'实际总取样量')).toBe('未记录')
  expect(find(data,'本任务检验样量')).toBe('未记录')
 })
 it('adds only exact same-unit decimals and preserves different units separately',()=>{
  const r=refs();r['samplingTaskId:8']!.row.details=[{sampleQuantity:'0.10000001',unitId:'kg'},{sampleQuantity:'0.02499999',unitId:'kg'},{sampleQuantity:'2.5',unitId:'g'}]
  expect(find(inspectionIdentityFacts(record,request,lot,r),'实际总取样量')).toBe('0.125 千克 / 2.5 克')
 })
 it('marks missing sampling quantity invalid instead of pretending it is zero',()=>{
  const r=refs();r['samplingTaskId:8']!.row.details=[{unitId:'kg'}]
  expect(find(inspectionIdentityFacts(record,request,lot,r),'实际总取样量')).toContain('数量信息不完整')
 })
})
