import {cleanup,render,screen,waitFor} from '@testing-library/vue'
import {afterEach,it,expect,vi} from 'vitest'
import Antd from 'ant-design-vue'
import BatchOverview from './BatchOverview.vue'
const api=vi.hoisted(()=>vi.fn())
vi.mock('../../api/http',()=>({api,errorMessage:String}))
vi.mock('../../stores/auth',()=>({useAuthStore:()=>({can:()=>true})}))
vi.mock('vue-router',()=>({useRoute:()=>({query:{}}),useRouter:()=>({push:vi.fn()})}))
afterEach(()=>{cleanup();vi.clearAllMocks()})
function mount(schema:number){
 api.mockImplementation(async({url}:any)=>url.startsWith('/products/')?{productName:'氯化钾溶液',productCode:'KCL30'}:url.startsWith('/units/')?{unitName:'毫升'}:{orderNo:'INTERNAL:compat',status:'DRAFT'})
 return render(BatchOverview,{props:{record:{id:'230',productionSchemaVersion:schema,productionOrderId:'80',productId:'1',unitId:'3',batchNo:'KCL20261011001',status:'DRAFT',plannedQty:'1000',createdAt:'2026-10-11T08:00:00Z'},executions:[],reservations:[]},global:{plugins:[Antd]}})
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
it('uses one top-level batch record entry when the whole-book route is available',async()=>{
 mount(2)
 expect(screen.getAllByRole('button',{name:/^电子批记录$/})).toHaveLength(1)
 expect(screen.queryAllByRole('button',{name:/^批记录$/})).toHaveLength(0)
})
