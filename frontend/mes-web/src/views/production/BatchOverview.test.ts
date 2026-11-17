import {cleanup,render,screen,waitFor} from '@testing-library/vue'
import {afterEach,it,expect,vi} from 'vitest'
import Antd from 'ant-design-vue'
import BatchOverview from './BatchOverview.vue'
const api=vi.hoisted(()=>vi.fn())
vi.mock('../../api/http',()=>({api,errorMessage:String}))
vi.mock('../../stores/auth',()=>({useAuthStore:()=>({can:()=>true})}))
vi.mock('vue-router',()=>({useRoute:()=>({query:{}}),useRouter:()=>({push:vi.fn()})}))
afterEach(()=>{cleanup();vi.clearAllMocks()})
function mount(schema:number,status='DRAFT'){
 api.mockImplementation(async({url}:any)=>url.startsWith('/products/')?{productName:'氯化钾溶液',productCode:'KCL30'}:url.startsWith('/units/')?{unitName:'毫升'}:{orderNo:'INTERNAL:compat',status:'DRAFT'})
 return render(BatchOverview,{props:{record:{id:'230',productionSchemaVersion:schema,productionOrderId:'80',productId:'1',unitId:'3',batchNo:'KCL20261011001',status,plannedQty:'1000',createdAt:'2026-10-11T08:00:00Z'},executions:[],reservations:[]},global:{plugins:[Antd]}})
}
it('shows the direct instruction identity without exposing or fetching its compatibility order',async()=>{
 mount(2)
 await screen.findAllByText('氯化钾溶液')
 expect(screen.queryAllByText('生产工单')).toHaveLength(0)
 expect(screen.queryAllByText('生产订单')).toHaveLength(0)
 expect(screen.queryAllByText('INTERNAL:compat')).toHaveLength(0)
 expect(api.mock.calls.some(([q])=>q.url.startsWith('/production-orders/'))).toBe(false)
 expect(screen.getAllByText('KCL20261011001').length).toBeGreaterThan(0)
})
it('retains compatibility order reading and presentation for historical schema one',async()=>{
 mount(1)
 await waitFor(()=>expect(api).toHaveBeenCalledWith({url:'/production-orders/80'}))
 expect(await screen.findAllByText('INTERNAL:compat')).toHaveLength(2)
 expect(screen.getAllByText('生产工单')).toHaveLength(2)
 expect(screen.getAllByText('生产订单')).toHaveLength(1)
})
it('distinguishes dispatched production batch from QA finished release',async()=>{
 mount(2,'RELEASED')
 await screen.findAllByText('氯化钾溶液')
 expect(screen.getAllByText('已下达').length).toBeGreaterThan(0)
 expect(screen.queryByText('已放行')).toBeNull()
 expect(screen.getAllByText('未放行').length).toBeGreaterThan(0)
})
it('uses one top-level batch record entry when the whole-book route is available',async()=>{
 mount(2)
 expect(screen.getAllByRole('button',{name:/^电子批记录$/})).toHaveLength(1)
 expect(screen.queryAllByRole('button',{name:/^批记录$/})).toHaveLength(0)
})

it('does not show unverified operation totals or raw product IDs during loading',async()=>{
 let completeProduct:(result:any)=>void=()=>{}
 api.mockImplementation(({url}:any)=>url.startsWith('/products/')?new Promise(resolve=>{completeProduct=resolve}):url.startsWith('/units/')?Promise.resolve({unitName:'毫升'}):Promise.resolve({status:'QA_RELEASED',gates:[]}))
 render(BatchOverview,{props:{record:{id:'655',productionSchemaVersion:2,productId:'2',unitId:'3',batchNo:'HYTEA-20261109-002',status:'QA_RELEASED',plannedQty:'400000',createdAt:'2026-11-09T12:00:00Z',processSnapshotId:'1'},executions:[{id:'10',status:'COMPLETED',executionNo:'001'}],reservations:[]},global:{plugins:[Antd]}})
 expect(screen.getAllByText('正在读取产品信息').length).toBeGreaterThan(0)
 expect(screen.queryByText('0 / 0')).toBeNull()
 expect(screen.getAllByText('正在读取').length).toBeGreaterThan(0)
 completeProduct({productName:'喉咽茶合剂',productCode:'HYTEA100',dosageForm:'合剂',specification:'100ml/瓶'})
 await screen.findAllByText('喉咽茶合剂')
 await waitFor(()=>expect(screen.queryByText('正在读取产品信息')).toBeNull())
})
