import {describe,it,expect} from 'vitest'
import {detailLabel,detailDisplay,documentStatus,signatureEntries,selectFacts,inspectionSummaryRows} from './incomingDetailModel'
import {labels} from './incomingModel'
describe('Incoming controlled document presentation',()=>{
 it('keeps business labels stable despite another module changing the shared dictionary',()=>{
  const previous=labels.required;labels.required='必须称量'
  expect(detailLabel('required')).toBe('必检项目');labels.required=previous!
 })
 it('exposes signature metadata from original and final results without rewriting either fact',()=>{
  const original={id:'1',resultConclusion:'FAIL',signatureEvidence:[{signatureId:'101'}]}
  const final={id:'2',resultConclusion:'PASS',signatureEvidence:[{signatureId:'102'}]}
  const record={items:[{originalResult:original,result:final}],signatureEvidence:[{signatureId:'103'}]}
  const before=JSON.stringify(record)
  expect(signatureEntries(record).map(e=>e.metadata.signatureId)).toEqual(['103','101','102'])
  expect(signatureEntries(record)[1]!.context).toContain('原始结果')
  expect(JSON.stringify(record)).toBe(before)
 })
 it('selects present fields only, preserves zero/null/decimal values and does not fabricate missing facts',()=>{
  expect(selectFacts({versionNo:0,resultNumeric:'95.000000',reviewedAt:null},['versionNo','resultNumeric','reviewedAt','completedAt'])).toEqual({versionNo:0,resultNumeric:'95.000000',reviewedAt:null})
 })
 it('uses business wording for signature meanings and distinguishes original from effective conclusion',()=>{
  expect(detailDisplay('meaning','VERIFY')).toBe('核验')
  expect(detailDisplay('slot','review')).toBe('检验复核')
  expect(detailLabel('effectiveConclusion')).toBe('该结果版本的有效结论')
 })
 it('does not present a foreign DRAFT state as a valid inspection task state',()=>{
  expect(documentStatus('inspection-tasks',{status:'DRAFT'})).toBe('未识别状态（DRAFT）')
  expect(documentStatus('inspection-requests',{status:'DRAFT'})).toBe('草稿')
 })
 it('keeps distinct executions and raw FAIL visible rather than selecting a retest PASS as the item fact',()=>{
  const item={id:'11',itemName:'含量',resultType:'NUMERIC',lowerLimit:'95.000000',upperLimit:'105.000000',executions:[{id:'21',attemptNo:1,revisions:[{id:'31',revisionNo:1,resultNumeric:'90.000000',resultConclusion:'FAIL',effectiveConclusion:'INVALID'}]},{id:'22',attemptNo:2,revisions:[{id:'32',revisionNo:1,resultNumeric:'99.000000',resultConclusion:'PASS',effectiveConclusion:'PASS'}]}]}
  const before=JSON.stringify(item),result=inspectionSummaryRows([item])
  expect(result[0]!.standard).toBe('95.000000 – 105.000000')
  expect(result[0]!.results.map(r=>r.revision.resultConclusion)).toEqual(['FAIL','PASS'])
  expect(JSON.stringify(item)).toBe(before)
 })
})
