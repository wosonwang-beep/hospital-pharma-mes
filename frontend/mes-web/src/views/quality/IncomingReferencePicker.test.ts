import {render,screen,waitFor} from '@testing-library/vue'
import {it,expect,vi} from 'vitest'
import IncomingReferencePicker from './IncomingReferencePicker.vue'
const api=vi.hoisted(()=>vi.fn())
vi.mock('../../api/http',()=>({api,errorMessage:String}))
it('offers approved package versions without filtering version status by product ACTIVE status',async()=>{
 api.mockImplementation(async(config:{url:string;params?:Record<string,unknown>})=>{
  if(config.url==='/process-packages')return config.params?.status==='ACTIVE'?{items:[],total:0}:{items:[{id:5}],total:1}
  return {packageCode:'KCL30',versions:[{id:10,businessVersion:1,status:'EFFECTIVE'},{id:11,businessVersion:2,status:'DRAFT'}]}
 })
 render(IncomingReferencePicker,{props:{field:'packageVersionId',modelValue:'',context:{productId:1},inputLabel:'工艺包版本'},global:{stubs:{'a-button':true}}})
 await waitFor(()=>expect(screen.getByRole('option',{name:'KCL30 · V1 · EFFECTIVE'})).toBeTruthy())
 expect(screen.queryByRole('option',{name:'KCL30 · V2 · DRAFT'})).toBeNull()
 expect(api).toHaveBeenCalledWith(expect.objectContaining({url:'/process-packages',params:expect.objectContaining({productId:1})}))
})
