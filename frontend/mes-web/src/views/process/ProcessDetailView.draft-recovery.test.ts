import {afterEach,it,expect,vi} from 'vitest'
import {render,screen,fireEvent,cleanup} from '@testing-library/vue'
import Antd from 'ant-design-vue'
import ProcessDetailView from './ProcessDetailView.vue'
const {api,push,leave}=vi.hoisted(()=>({api:vi.fn(),push:vi.fn(),leave:vi.fn()}))
vi.mock('../../api/http',()=>({api,idempotencyKey:()=> 'key',errorMessage:()=> '保存失败'}))
vi.mock('../../stores/auth',()=>({useAuthStore:()=>({can:()=>true})}))
vi.mock('vue-router',()=>({useRoute:()=>({meta:{mode:'create'},params:{},query:{},fullPath:'/process/packages/create'}),useRouter:()=>({push}),onBeforeRouteLeave:leave}))
afterEach(()=>{cleanup();vi.restoreAllMocks();vi.clearAllMocks()})
it('protects new parent input on route cancellation and window close',async()=>{
 const view=render(ProcessDetailView,{global:{plugins:[Antd],stubs:{ProcessLookup:true}}})
 const confirm=vi.spyOn(window,'confirm').mockReturnValue(false),guard=leave.mock.calls[0]![0]
 expect(guard()).toBe(true)
 await fireEvent.update(screen.getByLabelText('工艺名称'),'氯化钾溶液配制工艺')
 expect(guard()).toBe(false)
 expect(confirm).toHaveBeenCalledOnce()
 const closing=new Event('beforeunload',{cancelable:true});window.dispatchEvent(closing);expect(closing.defaultPrevented).toBe(true)
 expect((screen.getByLabelText('工艺名称') as HTMLInputElement).value).toBe('氯化钾溶液配制工艺')
 view.unmount();const after=new Event('beforeunload',{cancelable:true});window.dispatchEvent(after);expect(after.defaultPrevented).toBe(false)
})
it('retains the new draft after a failed create request',async()=>{
 api.mockRejectedValue(new Error('保存失败'))
 render(ProcessDetailView,{global:{plugins:[Antd],stubs:{ProcessLookup:true}}})
 await fireEvent.update(screen.getByLabelText('工艺编码'),'KCL30-COMPOUND')
 await fireEvent.update(screen.getByLabelText('工艺名称'),'氯化钾溶液配制工艺')
 await fireEvent.submit(document.querySelector('form')!)
 expect(await screen.findByRole('alert')).toBeTruthy()
 expect((screen.getByLabelText('工艺编码') as HTMLInputElement).value).toBe('KCL30-COMPOUND')
 expect((screen.getByLabelText('工艺名称') as HTMLInputElement).value).toBe('氯化钾溶液配制工艺')
 expect(push).not.toHaveBeenCalled()
})
