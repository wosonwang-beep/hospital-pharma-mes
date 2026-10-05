import {describe,it,expect} from 'vitest'
import {validateExpression,permits,qualityOperations} from './productionQualityModel'
import {schemas,commandBody} from './incomingModel'
describe('production quality closed contract',()=>{
 it('accepts exact event sums and decimal arithmetic',()=>expect(()=>validateExpression({op:'ADD',left:{sum:['OUTPUT','SAMPLE','LOSS']},right:{constant:'0.00000001'}})).not.toThrow())
 it('rejects executable and additional fields',()=>{for(const value of ['return window.alert(1)',{sum:['OUTPUT'],sql:'select 1'},{constant:'NaN'},{sum:['REVERSAL']}])expect(()=>validateExpression(value)).toThrow()})
 it('enforces depth and node bounds',()=>{let deep:unknown={sum:['CHARGE']};for(let i=0;i<8;i++)deep={op:'ADD',left:deep,right:{constant:'0'}};expect(()=>validateExpression(deep)).toThrow();const tree=(n:number):unknown=>n?{op:'ADD',left:tree(n-1),right:tree(n-1)}:{sum:['CHARGE']};expect(()=>validateExpression(tree(6))).toThrow()})
 it('uses server allowedActions with production command aliases',()=>{expect(permits({allowedActions:['RECORD_RESULT']},'results')).toBe(true);expect(permits({allowedActions:['REVISE']},'revisions')).toBe(true);expect(permits({status:'DRAFT',allowedActions:[]},'approve')).toBe(false)})
 it('keeps exact decimals and closed observation fields',()=>{const b=commandBody({...schemas.ProductionQuantityCommand,required:[]},{versionNo:3,eventType:'LOSS',amount:'0.000001',unitId:'10',sourceRef:'loss-1',reason:'实测',debug:'discard'}) as Record<string,unknown>;expect(b.amount).toBe('0.000001');expect(b).not.toHaveProperty('debug')})
 it('binds existing frozen plan and balance routes',()=>{expect(qualityOperations.find(o=>o.path==='/quality/production-plans/{id}'&&o.method==='PUT')?.schema).toBe('ProductionPlanUpdateCommand');expect(qualityOperations.find(o=>o.path==='/balance-investigations/{id}/approve')?.permission).toBe('balance:approve')})
})
