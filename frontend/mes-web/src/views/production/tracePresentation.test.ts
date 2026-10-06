import {describe,it,expect} from 'vitest'
import {traceDestination,traceTypeNames,type TraceEdge} from './tracePresentation'
const e=(sourceType:string,sourceId:string,targetType:string,targetId:string):TraceEdge=>({sourceType,sourceId,targetType,targetId,relation:'actual'})
describe('actual source navigation',()=>{
 it('opens charge workspace through actual operation/unit without requiring a weighing',()=>{
  const edges=[e('EXECUTION_UNIT','10','OPERATION','20'),e('OPERATION','20','CHARGE','30')]
  expect(traceDestination({type:'CHARGE',id:'30'},edges,p=>['mes:charge:view','mes:charge:create'].includes(p))).toBe('/mes/execution/10/charge')
  expect(traceDestination({type:'CHARGE',id:'30'},edges,p=>['mes:charge:view','mes:operation:view'].includes(p))).toBe('/mes/execution/10')
  expect(traceDestination({type:'CHARGE',id:'30'},[],()=>true)).toBeUndefined()
  expect(traceDestination({type:'CHARGE',id:'30'},edges,()=>false)).toBeUndefined()
 })
 it('resolves immutable return to actual original issue and protects finished source links',()=>{
  const edges=[e('MATERIAL_ISSUE','1','MATERIAL_ISSUE_ITEM','2'),e('MATERIAL_ISSUE_ITEM','2','ISSUE_RETURN','3')]
  expect(traceDestination({type:'ISSUE_RETURN',id:'3'},edges,p=>p==='wms:issue:view')).toBe('/wms/issues/1')
  expect(traceDestination({type:'FINISHED_SHIPMENT',id:'4'},[],()=>false)).toBeUndefined()
  expect(traceDestination({type:'FINISHED_SHIPMENT',id:'4'},[],()=>true)).toBe('/finished/shipments/4')
  expect(traceTypeNames.CHARGE).toBe('投料记录')
 })
})
