import {it,expect} from 'vitest'
import {createInspectionLoader} from './inspectionAdapter'

it('keeps server paging and hides write capabilities without the matching existing permission',async()=>{
 const sent:unknown[]=[]
 const load=createInspectionLoader(async query=>{sent.push(query);return {items:[{id:'31',sampleName:'药用甘油',lotNo:'2026100003',sampleNo:'YP-0121',inspectionTaskNo:'JY-31',requestType:'INITIAL',status:'IN_PROGRESS',assignedTo:'7',assignedName:'陈珊',requestedDate:'2026-10-19',sampledAt:null,overallResult:null,reportNo:null,reportId:null,allowedActions:['view','execute','review']}],total:42,page:2,size:20,inspectors:[{value:'7',label:'陈珊'}]}},permission=>permission==='qms:test:view')
 const result=await load({keyword:'甘油',status:'IN_PROGRESS',assignedTo:'7',requestType:'INITIAL',page:2,size:20})
 expect(sent).toEqual([{keyword:'甘油',status:'IN_PROGRESS',assignedTo:'7',requestType:'INITIAL',page:2,size:20}])
 expect(result.total).toBe(42)
 expect(result.items[0]?.allowedActions).toEqual(['view'])
 expect(result.inspectors).toEqual([{value:'7',label:'陈珊'}])
})

it('does not invent missing server capabilities even for a privileged user',async()=>{
 const load=createInspectionLoader(async()=>({items:[{id:'31',allowedActions:[]} as never],total:1,page:0,size:20,inspectors:[]}),()=>true)
 expect((await load({keyword:'',status:'',assignedTo:'',requestType:'',page:0,size:20})).items[0]?.allowedActions).toEqual([])
})
