import {describe,it,expect} from 'vitest'
import {commandBody,commands,definitions,canAct,inboundCreateBody} from './model'
describe('finished controlled contracts',()=>{
 it('builds exact payload preserving decimal text and rejecting missing real source',()=>{expect(()=>commandBody(commands.shipmentCreate!,{shipmentNo:'FG',reason:'实际发货'})).toThrow();expect(commandBody(commands.shipmentCreate!,{shipmentNo:'FG',mainBatchId:'100',locationId:'20',quantity:'0.123456',unitId:'10',receivingParty:'制剂发药室',reason:'实际发货',fake:'do not send'})).toEqual({shipmentNo:'FG',mainBatchId:'100',locationId:'20',quantity:'0.123456',unitId:'10',receivingParty:'制剂发药室',reason:'实际发货'})})
 it('requires both server action and permission; signed approval uses actual report version',()=>{expect(canAct({allowedActions:['APPROVE']},'APPROVE',false)).toBe(false);expect(canAct({allowedActions:[]},'APPROVE',true)).toBe(false);expect(commandBody(commands.approve!,{reason:'独立审批'},7)).toEqual({versionNo:7,reason:'独立审批'});expect(definitions.reports!.create).toBeUndefined()})
})

describe('optional paired finished draft',()=>{
 it('unchecked input retains original payload and drops hidden child fields',()=>expect(inboundCreateBody({requestNo:'FI',mainBatchId:'100',reason:'实际申请',createInspectionDraft:false,inspectionRequestNo:'stale hidden value',qualityStatus:'RELEASED'})).toEqual({requestNo:'FI',mainBatchId:'100',reason:'实际申请'}))
 it('selected option sends only the approved flag and real child number',()=>{expect(inboundCreateBody({requestNo:'FI',mainBatchId:'100',reason:'实际申请',createInspectionDraft:true,inspectionRequestNo:' FQ-001 ',status:'ACCEPTED'})).toEqual({requestNo:'FI',mainBatchId:'100',reason:'实际申请',createInspectionDraft:true,inspectionRequestNo:'FQ-001'});expect(()=>inboundCreateBody({requestNo:'FI',mainBatchId:'100',reason:'实际申请',createInspectionDraft:true})).toThrow('成品请验单号')})
})
