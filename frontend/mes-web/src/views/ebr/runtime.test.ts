import {describe,it,expect} from 'vitest'
import {runtimePayload,runtimeHeaders,type RuntimeField} from '../../api/ebrRuntime'
const field:RuntimeField={fieldCode:'MASS',occurrencePath:'G[0]',label:'重量',type:'NUMBER',value:null,displayValue:'',unitId:'7',required:true,readonly:false,visible:true,validationState:'MISSING',source:'MANUAL',provenance:[],allowedActions:['EDIT']}
describe('runtime eBR command boundaries',()=>{
 it('preserves exact decimals and omits renderer metadata',()=>{expect(runtimePayload([field],{'MASS|G[0]':'123456789012345678.123'},{})).toEqual([{fieldCode:'MASS',occurrencePath:'G[0]',value:'123456789012345678.123',unitId:'7',sourceRef:null}])})
 it('does not submit controlled source or readonly values',()=>{expect(runtimePayload([{...field,source:'INSTRUMENT',allowedActions:[]}],{'MASS|G[0]':'42'},{})).toEqual([])})
 it('ignores reauthentication token when retaining retry identity',()=>{const headers=runtimeHeaders();expect(headers.forRequest('/forms/1/reviews',{formRevision:1,reauthToken:'old'},1)).toEqual(headers.forRequest('/forms/1/reviews',{formRevision:1,reauthToken:'new'},1))})
})
