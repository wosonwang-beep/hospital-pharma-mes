import {afterEach,beforeEach,describe,it,expect,vi} from 'vitest'
import {cleanup,render,screen,within,fireEvent,waitFor} from '@testing-library/vue'
import Antd from 'ant-design-vue'
import ProcessDetailView from './ProcessDetailView.vue'

const {api,replace,route}=vi.hoisted(()=>({api:vi.fn(),replace:vi.fn(),route:{meta:{mode:'edit'},params:{id:'1'},query:{},fullPath:'/process/packages/1/edit'}}))
vi.mock('../../api/http',()=>({api,errorMessage:()=> '请求失败',idempotencyKey:()=> 'operation-key'}))
vi.mock('../../stores/auth',()=>({useAuthStore:()=>({can:()=>true})}))
vi.mock('vue-router',()=>({useRoute:()=>route,useRouter:()=>({replace,push:vi.fn()}),onBeforeRouteLeave:vi.fn()}))
afterEach(()=>{cleanup();vi.clearAllMocks()})
beforeEach(()=>{route.meta.mode='edit'})
function setup(type?:'PRODUCTION'|'IPC'){
 const operation={operationCode:'OP01',operationName:'溶液配制',sequenceNo:1,requiredRole:null,completionRule:null,predecessorCodes:[],requiredEquipmentType:null,clearanceRequired:false,parameters:[],ipcDefinitions:[],...(type?{operationType:type}:{})}
 const pkg={id:'1',packageCode:'KCL30-PROC',status:'ACTIVE',productName:'通用生产工艺',currentDefinition:{id:'9',status:'EFFECTIVE',formula:null,route:{routeCode:'KCL30-ROUTE',operations:[operation]}}}
 api.mockImplementation(async ({url}:{url:string})=>url==='/ebr/templates'?{items:[],total:0,page:0,size:100}:pkg)
 render(ProcessDetailView,{global:{plugins:[Antd],stubs:{ProcessLookup:true}}})
}
describe('persisted operation type controls',()=>{
 it('keeps legacy unmarked without guessing from code or inspection definitions',async()=>{
  setup()
  const group=await screen.findByRole('radiogroup',{name:'工序类型 1'})
  expect((within(group).getByRole('radio',{name:'生产工序'}) as HTMLInputElement).checked).toBe(false)
  expect((within(group).getByRole('radio',{name:'过程检验（IPC）'}) as HTMLInputElement).checked).toBe(false)
  expect(within(group).getByText('历史未标注')).toBeTruthy()
 })
 it('saves an explicit choice through the existing current-definition request',async()=>{
  setup('PRODUCTION')
  const group=await screen.findByRole('radiogroup',{name:'工序类型 1'})
  await fireEvent.click(within(group).getByRole('radio',{name:'过程检验（IPC）'}))
  await fireEvent.click(screen.getByRole('button',{name:'保存并生效'}))
  await waitFor(()=>expect(api.mock.calls.some(([r])=>r.method==='PUT')).toBe(true))
  const request=api.mock.calls.find(([r])=>r.method==='PUT')![0]
  expect(request.url).toBe('/process-packages/1/current-definition')
  expect(request.data.route.operations[0].operationType).toBe('IPC')
  expect(request.data.route.operations[0].ipcDefinitions).toEqual([])
  expect(request.data.route.operations[0].predecessorCodes).toEqual([])
 })
 it('defaults only a newly added operation to production',async()=>{
  setup()
  await screen.findByRole('radiogroup',{name:'工序类型 1'})
  await fireEvent.click(screen.getByText('＋ 添加工序',{exact:true}).closest('button')!)
  const second=await screen.findByRole('radiogroup',{name:'工序类型 2'})
  expect((within(second).getByRole('radio',{name:'生产工序'}) as HTMLInputElement).checked).toBe(true)
  expect((within(screen.getByRole('radiogroup',{name:'工序类型 1'})).getByRole('radio',{name:'生产工序'}) as HTMLInputElement).checked).toBe(false)
 })
 it('shows the saved type with disabled inputs in a readonly view',async()=>{
  route.meta.mode='view';setup('IPC')
  const group=await screen.findByRole('radiogroup',{name:'工序类型 1'})
  for(const radio of within(group).getAllByRole('radio'))expect((radio as HTMLInputElement).disabled).toBe(true)
  expect((within(group).getByRole('radio',{name:'过程检验（IPC）'}) as HTMLInputElement).checked).toBe(true)
 })
})
