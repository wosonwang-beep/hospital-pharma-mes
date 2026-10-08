import {beforeEach,describe,expect,it,vi} from 'vitest'
import {cachedReference,clearReferenceCache} from './referenceCache'

beforeEach(()=>clearReferenceCache())

describe('role-scoped reference label cache',()=>{
 it('coalesces duplicate reads within the same organization and actor',async()=>{
  const loader=vi.fn(async()=> '氯化钾溶液')
  const [a,b]=await Promise.all([
   cachedReference('1:9:productId:1',loader),
   cachedReference('1:9:productId:1',loader)
  ])
  expect([a,b]).toEqual(['氯化钾溶液','氯化钾溶液'])
  expect(loader).toHaveBeenCalledTimes(1)
  expect(await cachedReference('1:9:productId:1',loader)).toBe('氯化钾溶液')
  expect(loader).toHaveBeenCalledTimes(1)
 })
 it('does not share labels between different users or organizations',async()=>{
  const one=vi.fn(async()=> 'A机构物料')
  const another=vi.fn(async()=> 'B机构物料')
  expect(await cachedReference('1:9:materialId:5',one)).toBe('A机构物料')
  expect(await cachedReference('2:11:materialId:5',another)).toBe('B机构物料')
  expect(one).toHaveBeenCalledTimes(1)
  expect(another).toHaveBeenCalledTimes(1)
 })
 it('does not allow a prior session inflight read to repopulate a cleared cache',async()=>{
  let resolveOld!:(value:string)=>void
  const oldLoader=()=>new Promise<string>(resolve=>{resolveOld=resolve})
  const old=cachedReference('1:9:materialId:5',oldLoader)
  clearReferenceCache()
  const newLoader=vi.fn(async()=> '新会话名称')
  const updated=await cachedReference('1:9:materialId:5',newLoader)
  resolveOld('旧会话名称')
  expect(await old).toBe('旧会话名称')
  expect(updated).toBe('新会话名称')
  expect(await cachedReference('1:9:materialId:5',newLoader)).toBe('新会话名称')
  expect(newLoader).toHaveBeenCalledTimes(1)
 })
})
