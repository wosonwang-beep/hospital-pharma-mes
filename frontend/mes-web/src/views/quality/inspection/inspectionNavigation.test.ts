import {it,expect,vi} from 'vitest'
import {mount,flushPromises} from '@vue/test-utils'
import {createRouter,createMemoryHistory} from 'vue-router'
import {defineComponent} from 'vue'
import IncomingDetailView from '../IncomingDetailView.vue'

vi.mock('../../../stores/auth',()=>({useAuthStore:()=>({can:()=>true,identity:{userId:'7'}})}))
vi.mock('../../../api/http',async importOriginal=>({...await importOriginal<object>(),api:vi.fn(async()=>({id:'31',inspectionTaskNo:'JY-31',status:'IN_PROGRESS',versionNo:0,allowedActions:[],items:[{id:'41',allowedActions:['executions'],executions:[]}]}))}))

it('keeps the originating list query through view then enter-execution navigation',async()=>{
 const router=createRouter({history:createMemoryHistory(),routes:[{path:'/quality/inspection-tasks/:id',component:IncomingDetailView,meta:{resource:'inspection-tasks',mode:'view'}},{path:'/quality/inspection-tasks/:id/execute',component:IncomingDetailView,meta:{resource:'inspection-tasks',mode:'execute'}}]})
 await router.push('/quality/inspection-tasks/31?keyword=甘油&assignedTo=7&page=2&size=20');await router.isReady()
 const button=defineComponent({emits:['click'],template:'<button @click="$emit(\'click\')"><slot/></button>'})
 const wrapper=mount(IncomingDetailView,{global:{plugins:[router],stubs:{AButton:button,ASpace:{template:'<div><slot/></div>'},IncomingDocument:true,IncomingFacts:true}}})
 await flushPromises()
 const enter=wrapper.findAll('button').find(x=>x.text()==='进入执行')
 expect(enter).toBeDefined();await enter!.trigger('click');await flushPromises()
 expect(router.currentRoute.value.path).toBe('/quality/inspection-tasks/31/execute')
 expect(router.currentRoute.value.query).toEqual({keyword:'甘油',assignedTo:'7',page:'2',size:'20'})
 wrapper.unmount()
})
