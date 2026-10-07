import {describe,it,expect} from 'vitest'
import {completeRows,dailyCounts} from './dashboardReadModel'
describe('dashboard existing readonly facts',()=>{
 it('does not report partial pagination as a complete statistic',()=>{
  expect(completeRows({items:[{productName:'氯化钾溶液'}],total:18,page:0,size:1})).toBeNull()
  expect(completeRows({items:[],total:0,page:0,size:100})).toEqual([])
 })
 it('uses Shanghai business dates at UTC boundaries and ignores missing dates',()=>{
  const days=dailyCounts([{batchNo:'20231218',createdAt:'2026-10-06T16:01:00Z'},{batchNo:'20231215',createdAt:'2026-10-06T15:59:00Z'},{productName:'喉咽茶合剂'},{createdAt:'invalid'}],new Date('2026-10-07T10:00:00Z'))
  expect(days.map(d=>d.label)).toEqual(['10-01','10-02','10-03','10-04','10-05','10-06','10-07'])
  expect(days.map(d=>d.count)).toEqual([0,0,0,0,0,1,1])
 })
})
