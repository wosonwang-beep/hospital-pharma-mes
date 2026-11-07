import {afterEach,beforeEach,describe,it,expect,vi} from 'vitest'
import {cleanup,render,screen,fireEvent,waitFor} from '@testing-library/vue'
import Antd from 'ant-design-vue'
import ProcessDetailView from './ProcessDetailView.vue'

const {api,replace,route}=vi.hoisted(()=>({api:vi.fn(),replace:vi.fn(),route:{meta:{mode:'edit'},params:{id:'1'},query:{},fullPath:'/process/packages/1/edit'}}))
vi.mock('../../api/http',()=>({api,errorMessage:()=> '请求失败',idempotencyKey:()=> 'operation-key'}))
vi.mock('../../stores/auth',()=>({useAuthStore:()=>({can:()=>true})}))
vi.mock('vue-router',()=>({useRoute:()=>route,useRouter:()=>({replace,push:vi.fn()}),onBeforeRouteLeave:vi.fn()}))
afterEach(()=>{cleanup();vi.clearAllMocks()})
beforeEach(()=>{route.meta.mode='edit'})
function setup(type:'PRODUCTION'|'IPC'='IPC'){
 const operation={operationCode:'OP01',operationName:'中间检验',sequenceNo:1,requiredRole:null,completionRule:null,predecessorCodes:[],requiredEquipmentType:null,clearanceRequired:false,parameters:[],ipcDefinitions:[{ipcCode:'LEGACY_TEMP',name:'历史关键参数',required:true,resultType:'NUMBER',lowerLimit:'121',upperLimit:'125',expectedText:null,unitId:null,methodReference:'旧方法'}],operationType:type}
 const pkg={id:'1',packageCode:'KCL30-PROC',packageName:'氯化钾溶液制备工艺',processVersion:'V1.0',effectiveApprovedDate:'2026-06-01',description:'制备工艺说明',status:'ACTIVE',productName:'氯化钾溶液',productId:'p1',productIds:['p1'],applicableProducts:[{id:'p1',productId:'p1',productCode:'KCL30',productName:'氯化钾溶液',specification:'30ml:3g/瓶',dosageForm:'溶液剂',status:'ACTIVE'}],currentDefinition:{id:'9',status:'EFFECTIVE',formula:null,route:{routeCode:'KCL30-ROUTE',operations:[operation]}}}
 api.mockImplementation(async ({url}:{url:string})=>{
  if(url==='/ebr/templates')return {items:[],total:0,page:0,size:100}
  if(url==='/products')return {items:[{id:'p1',productCode:'KCL30',productName:'氯化钾溶液',dosageForm:'溶液剂',specification:'30ml:3g/瓶',baseUnitId:'u1',status:'ACTIVE',allowedActions:[]}],total:1,page:0,size:100}
  return pkg
 })
 render(ProcessDetailView,{global:{plugins:[Antd],stubs:{ProcessLookup:true}}})
}

describe('formal production process ordinary-operation and product-scope UI',()=>{
 it('shows legacy IPC operations as ordinary operations without an IPC-specific UI',async()=>{
  setup('IPC')
  await screen.findByRole('tab',{name:/工序与记录表单/})
  expect(screen.getAllByText('中间检验').length).toBeGreaterThan(0)
  expect(screen.queryByText('工序类型')).toBeNull()
  expect(screen.queryByText('IPC')).toBeNull()
  expect(screen.queryByText('检验项目与判定标准')).toBeNull()
  expect(screen.queryByText('新增检验项目')).toBeNull()
 })
 it('preserves legacy IPC data when saving unrelated current process metadata',async()=>{
  setup('IPC')
  const version=await screen.findByRole('textbox',{name:'版本'})
  await fireEvent.update(version,'V2.0')
  await fireEvent.click(screen.getByRole('button',{name:'保存并生效'}))
  await waitFor(()=>expect(api.mock.calls.some(([r])=>r.method==='PUT')).toBe(true))
  const request=api.mock.calls.find(([r])=>r.method==='PUT')![0]
  expect(request.url).toBe('/process-packages/1/current-definition')
  expect(request.data.basicInfo.processVersion).toBe('V2.0')
  expect(request.data.productIds).toEqual(['p1'])
  expect(request.data.route.operations[0].operationType).toBe('IPC')
  expect(request.data.route.operations[0].ipcDefinitions).toEqual([{ipcCode:'LEGACY_TEMP',name:'历史关键参数',required:true,resultType:'NUMBER',lowerLimit:'121',upperLimit:'125',expectedText:null,unitId:null,methodReference:'旧方法'}])
 })
 it('does not offer a special operation type when adding a new operation',async()=>{
  setup('PRODUCTION')
  await screen.findByRole('tab',{name:/工序与记录表单/})
  await fireEvent.click(screen.getByText('＋ 添加工序',{exact:true}).closest('button')!)
  expect(screen.queryByText('工序类型')).toBeNull()
  expect(screen.queryByText('过程检验（IPC）')).toBeNull()
  expect(screen.getAllByText('新工序').length).toBeGreaterThan(0)
 })
 it('shows multiple-product scope as a separate workspace tab',async()=>{
  setup('PRODUCTION')
  await fireEvent.click(await screen.findByRole('tab',{name:/适用产品/}))
  expect(screen.getAllByText('适用产品').length).toBeGreaterThan(0)
  expect(screen.getByText('氯化钾溶液')).toBeTruthy()
  expect(screen.getByText('KCL30')).toBeTruthy()
 })
})
