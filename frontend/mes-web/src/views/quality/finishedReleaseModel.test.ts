import {describe,it,expect} from 'vitest'
import {canDecide,canPresentDecision,effectiveQaDecision,hasCurrentFinalArchive,releaseCommand,signingTarget,type FinishedReview,type BatchArchive} from './finishedReleaseModel'
const review:FinishedReview={mainBatchId:'41',versionNo:7,batchNo:'MB-41',status:'PENDING_QA',finishedLots:['51'],gates:[],blockingCodes:[],reviewDigest:'a'.repeat(64),allowedActions:['RELEASE','REJECT']}
describe('finished QA contract',()=>{
 it('binds RELEASE and REJECT to current composite batch version',()=>{expect(signingTarget(review,'RELEASED')).toEqual({objectType:'QA_RELEASE_DECISION',objectId:'41:7',recordVersion:7,meaning:'RELEASE'});expect(signingTarget(review,'REJECTED').meaning).toBe('REJECT')})
 it('uses server actions even when status appears ready',()=>{expect(canDecide({...review,allowedActions:[]},'RELEASED')).toBe(false);expect(canDecide({...review,allowedActions:['REJECT']},'REJECTED')).toBe(true)})
 it('sends only frozen command fields and exact digest',()=>expect(releaseCommand(review,'RELEASED','51',' 已核对 ','token',null)).toEqual({versionNo:7,reason:'已核对',signature:{reauthToken:'token'},mainBatchId:'41',finishedLotId:'51',decision:'RELEASED',releaseBasis:'FULL_INSPECTION',reviewDigest:'a'.repeat(64)}))
 it('prevents the original QA signer from seeing a self-supersession action',()=>{
   const released={...review,status:'QA_RELEASED'}
   const archive={decisions:[{id:'88',decisionBy:'7'}]} as unknown as BatchArchive
   expect(canPresentDecision(released,archive,'7')).toBe(false)
   expect(canPresentDecision(released,archive,'8')).toBe(true)
   expect(canPresentDecision(released,null,'8')).toBe(false)
   expect(canPresentDecision(review,null,'7')).toBe(true)
   expect(canPresentDecision({...review,allowedActions:[]},archive,'8')).toBe(false)
 })
 it('identifies the effective QA decision by supersession links even when rows are unordered',()=>{
   const older={id:'88',decisionBy:'7',supersedesDecisionId:null} as BatchArchive['decisions'][number]
   const current={id:'89',decisionBy:'8',supersedesDecisionId:'88'} as BatchArchive['decisions'][number]
   const unsorted={decisions:[current,older]} as BatchArchive
   expect(effectiveQaDecision(unsorted)?.id).toBe('89')
   expect(canPresentDecision({...review,status:'QA_RELEASED'},unsorted,'8')).toBe(false)
   expect(canPresentDecision({...review,status:'QA_RELEASED'},unsorted,'7')).toBe(true)
   expect(effectiveQaDecision({...unsorted,decisions:[]})).toBeNull()
 })
 it('preserves explicit supersession without changing previous decision',()=>expect(releaseCommand(review,'REJECTED','51','重新质量审核','token','81').supersedesDecisionId).toBe('81'))
 it('rejects another batch lot, empty reason and unavailable action',()=>{expect(()=>releaseCommand(review,'RELEASED','99','审核','token',null)).toThrow();expect(()=>releaseCommand(review,'RELEASED','51',' ','token',null)).toThrow();expect(()=>releaseCommand({...review,allowedActions:['REJECT']},'RELEASED','51','审核','token',null)).toThrow()})
 it('recognizes only the FINAL archive of the current QA decision and record digest',()=>{
   const prior={id:'88',supersedesDecisionId:null} as BatchArchive['decisions'][number]
   const newer={id:'89',supersedesDecisionId:'88'} as BatchArchive['decisions'][number]
   const oldFinal={archiveKind:'FINAL',recordDigest:'a'.repeat(64),releaseDecisionId:'88'} as BatchArchive['pdfManifests'][number]
   const currentFinal={archiveKind:'FINAL',recordDigest:'b'.repeat(64),releaseDecisionId:'89'} as BatchArchive['pdfManifests'][number]
   const archive={recordDigest:'b'.repeat(64),decisions:[newer,prior],pdfManifests:[oldFinal]} as BatchArchive
   expect(hasCurrentFinalArchive(archive)).toBe(false)
   expect(hasCurrentFinalArchive({...archive,pdfManifests:[oldFinal,currentFinal]})).toBe(true)
   expect(hasCurrentFinalArchive({...archive,pdfManifests:[{...currentFinal,releaseDecisionId:'88'}]})).toBe(false)
   expect(hasCurrentFinalArchive({...archive,pdfManifests:[{...currentFinal,recordDigest:'a'.repeat(64)}]})).toBe(false)
   expect(hasCurrentFinalArchive({...archive,decisions:[prior],recordDigest:'a'.repeat(64),pdfManifests:[oldFinal]})).toBe(true)
   expect(hasCurrentFinalArchive({...archive,decisions:[]})).toBe(false)
   expect(hasCurrentFinalArchive(null)).toBe(false)
 })
})
