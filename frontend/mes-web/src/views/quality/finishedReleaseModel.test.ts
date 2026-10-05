import {describe,it,expect} from 'vitest'
import {canDecide,releaseCommand,signingTarget,type FinishedReview} from './finishedReleaseModel'
const review:FinishedReview={mainBatchId:'41',versionNo:7,batchNo:'MB-41',status:'PENDING_QA',finishedLots:['51'],gates:[],blockingCodes:[],reviewDigest:'a'.repeat(64),allowedActions:['RELEASE','REJECT']}
describe('finished QA contract',()=>{
 it('binds RELEASE and REJECT to current composite batch version',()=>{expect(signingTarget(review,'RELEASED')).toEqual({objectType:'QA_RELEASE_DECISION',objectId:'41:7',recordVersion:7,meaning:'RELEASE'});expect(signingTarget(review,'REJECTED').meaning).toBe('REJECT')})
 it('uses server actions even when status appears ready',()=>{expect(canDecide({...review,allowedActions:[]},'RELEASED')).toBe(false);expect(canDecide({...review,allowedActions:['REJECT']},'REJECTED')).toBe(true)})
 it('sends only frozen command fields and exact digest',()=>expect(releaseCommand(review,'RELEASED','51',' 已核对 ','token',null)).toEqual({versionNo:7,reason:'已核对',signature:{reauthToken:'token'},mainBatchId:'41',finishedLotId:'51',decision:'RELEASED',releaseBasis:'FULL_INSPECTION',reviewDigest:'a'.repeat(64)}))
 it('preserves explicit supersession without changing previous decision',()=>expect(releaseCommand(review,'REJECTED','51','重新质量审核','token','81').supersedesDecisionId).toBe('81'))
 it('rejects another batch lot, empty reason and unavailable action',()=>{expect(()=>releaseCommand(review,'RELEASED','99','审核','token',null)).toThrow();expect(()=>releaseCommand(review,'RELEASED','51',' ','token',null)).toThrow();expect(()=>releaseCommand({...review,allowedActions:['REJECT']},'RELEASED','51','审核','token',null)).toThrow()})
})
