import {cleanup,fireEvent,render,screen,waitFor} from '@testing-library/vue'
import {afterEach,it,expect,vi} from 'vitest'
import Antd from 'ant-design-vue'
import IncomingReferencePicker from './IncomingReferencePicker.vue'
const api=vi.hoisted(()=>vi.fn())
vi.mock('../../api/http',()=>({api,errorMessage:String}))
afterEach(()=>{cleanup();vi.clearAllMocks()})
const mount=(props:any)=>render(IncomingReferencePicker,{props,global:{plugins:[Antd],stubs:{transition:false,'transition-group':false}}})
it('offers current effective process packages without revision lookup',async()=>{
 api.mockResolvedValue({items:[{id:5,packageCode:'KCL30',currentDefinition:{status:'EFFECTIVE'}},{id:6,packageCode:'DRAFT-PACKAGE',currentDefinition:{status:'DRAFT'}}],total:2})
 mount({field:'processPackageId',modelValue:'',context:{productId:1},inputLabel:'生产工艺'})
 await waitFor(()=>expect(api).toHaveBeenCalledWith(expect.objectContaining({url:'/process-packages',params:expect.objectContaining({productId:1})})))
 await fireEvent.mouseDown(screen.getByRole('combobox'))
 expect(await screen.findByText('KCL30')).toBeTruthy()
 expect(screen.queryByText('DRAFT-PACKAGE')).toBeNull()
 expect(api.mock.calls.every(([config])=>!config.url.includes('/process-versions'))).toBe(true)
})
it('searches in the selection input, preserves context filters, selects the real record and clears it',async()=>{
 api.mockImplementation(async(config:any)=>({items:config.params.keyword?[{id:7,batchNo:'KCL-007',status:'DRAFT'}]:[],total:1}))
 const view=mount({field:'mainBatchId',modelValue:'',context:{},queryFilters:{productId:'2'}})
 await fireEvent.update(screen.getByRole('combobox'),'KCL')
 await waitFor(()=>expect(api).toHaveBeenCalledWith(expect.objectContaining({url:'/main-batches',params:expect.objectContaining({keyword:'KCL',productId:'2'})})))
 await fireEvent.click(await screen.findByText('KCL-007 · 草稿'))
 expect(view.emitted()['update:modelValue']).toEqual([['7']])
 expect(view.emitted().select).toEqual([[{id:7,batchNo:'KCL-007',status:'DRAFT'}]])
 expect(screen.queryByText('查找其他记录')).toBeNull()
})
it('ignores a delayed response for an earlier search',async()=>{
 let resolveOld:(value:any)=>void=()=>{}
 api.mockImplementation((config:any)=>config.params.keyword==='old'?new Promise(resolve=>{resolveOld=resolve}):Promise.resolve({items:config.params.keyword==='new'?[{id:8,batchNo:'new-008'}]:[],total:1}))
 mount({field:'mainBatchId',modelValue:'',context:{}})
 await fireEvent.update(screen.getByRole('combobox'),'old')
 await waitFor(()=>expect(api).toHaveBeenCalledWith(expect.objectContaining({params:expect.objectContaining({keyword:'old'})})))
 await fireEvent.update(screen.getByRole('combobox'),'new')
 expect(await screen.findByText('new-008')).toBeTruthy()
 resolveOld({items:[{id:9,batchNo:'old-009'}],total:1})
 await waitFor(()=>expect(screen.queryByText('old-009')).toBeNull())
})

it('identifies effective eBR choices by name, code and revision while retaining their record IDs',async()=>{
 api.mockResolvedValue({items:[{id:'31',templateCode:'MIX',templateName:'配液记录',version:2,status:'EFFECTIVE'},{id:'32',templateCode:'OLD',templateName:null,version:1,status:'EFFECTIVE'}],total:2})
 const view=mount({field:'ebrTemplateVersionId',modelValue:'',context:{processPackageId:'5'},inputLabel:'eBR模板版本'})
 await waitFor(()=>expect(api).toHaveBeenCalledWith(expect.objectContaining({url:'/ebr/templates',params:expect.objectContaining({processPackageId:'5',status:'EFFECTIVE'})})))
 await fireEvent.mouseDown(screen.getByRole('combobox'))
 expect(await screen.findByText('未命名（历史模板） · OLD · V1 · 生效')).toBeTruthy()
 await fireEvent.click(await screen.findByText('配液记录 · MIX · V2 · 生效'))
 expect(view.emitted()['update:modelValue']).toEqual([['31']])
})
