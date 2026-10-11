import {describe,it,expect} from 'vitest'
import type {PrintField} from '../../api/printing'
import {detailTable,exportData,fillExample,restoreBindings,validateBindings} from './canvasPrintBinding'
const field=(key:string,datasetKey:string):PrintField=>({key,label:key,description:'',group:datasetKey,example:'',repeated:true,dataType:'TEXT',datasetKey})
describe('independent configured-source datasets',()=>{
 it('exports distinct loop keys and fills each dataset independently',()=>{
  const fields=[field('leftName','leftRows'),field('rightName','rightRows')]
  const source={main:[detailTable([fields[0]!],'TYPE'),detailTable([fields[1]!],'TYPE')]}
  const out=exportData(source)
  expect(out.main[0]!.trList![1]!.tdList[0]!.value[0]!.value).toBe('{{leftRows}}')
  expect(out.main[1]!.trList![1]!.tdList[0]!.value[0]!.value).toBe('{{rightRows}}')
  const preview=fillExample(source,{leftRows:[{leftName:'L1'},{leftName:'L2'}],rightRows:[{rightName:'R1'},{rightName:'R2'},{rightName:'R3'}]})
  expect(preview.main[0]!.trList).toHaveLength(3);expect(preview.main[1]!.trList).toHaveLength(4)
  expect(()=>validateBindings(restoreBindings(out,fields,'TYPE'),fields,'TYPE')).not.toThrow()
 })
 it('rejects a loop table mixing two collection identities',()=>{
  expect(()=>detailTable([field('leftName','leftRows'),field('rightName','rightRows')],'TYPE')).toThrow('一个明细集合')
 })
 it('rejects a stable field rebound to a different dataset',()=>{
  const source={main:[detailTable([field('stableName','oldRows')],'TYPE')]}
  expect(()=>validateBindings(source,[field('stableName','newRows')],'TYPE')).toThrow('所属集合')
 })
})
