import {describe,it,expect} from 'vitest'
import {simulationInputs,moveItem,emptyDefinition,newField} from './model'
describe('controlled eBR authoring',()=>{
 it('simulation preserves primitive types and repeated occurrences without evaluation',()=>{
  expect(simulationInputs([{fieldCode:'weight',type:'number',value:'12.5\n13'},{fieldCode:'ok',type:'boolean',value:'true\nfalse'},{fieldCode:'note',type:'string',value:'value(x)'}])).toEqual([{fieldCode:'weight',values:[12.5,13]},{fieldCode:'ok',values:[true,false]},{fieldCode:'note',values:['value(x)']}])
 })
 it('rejects invalid numeric/boolean simulation input',()=>{
  expect(()=>simulationInputs([{fieldCode:'x',type:'number',value:'Infinity'}])).toThrow()
  expect(()=>simulationInputs([{fieldCode:'x',type:'boolean',value:'yes'}])).toThrow()
 })
 it('reordering retains stable IDs and updates sequence only',()=>{
  const rows=[newField('A','TEXT'),newField('B','NUMBER')];moveItem(rows,1,0)
  expect(rows.map(x=>x.fieldCode)).toEqual(['B','A']);expect(rows.map(x=>x.sequenceNo)).toEqual([1,2])
 })
 it('new draft has no fabricated process runtime evidence',()=>{
  expect(emptyDefinition()).toEqual({sections:[],forms:[],rules:[],signatureRules:[],reviewRules:[]})
 })
})
