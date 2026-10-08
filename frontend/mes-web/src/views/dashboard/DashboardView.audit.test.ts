import {cleanup, render, screen} from '@testing-library/vue'
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import DashboardView from './DashboardView.vue'

type Spy=ReturnType<typeof vi.fn<(...args:any[])=>any>>
const mocks=vi.hoisted(()=>({api:vi.fn(),audit:vi.fn(),can:vi.fn(),push:vi.fn(),init:vi.fn(),instances:[] as {setOption:Spy,resize:Spy,dispose:Spy}[],observers:[] as {callback:()=>void,observe:Spy,disconnect:Spy}[]}))
vi.mock('../../api/http',()=>({api:mocks.api}))
vi.mock('../../api/audit',()=>({queryAuditEvents:mocks.audit}))
vi.mock('../../stores/auth',()=>({useAuthStore:()=>({identity:{displayName:'张三'},can:mocks.can})}))
vi.mock('vue-router',()=>({useRouter:()=>({push:mocks.push,resolve:()=>({meta:{permission:'permitted'}})})}))
vi.mock('echarts/core',()=>({use:vi.fn(),init:mocks.init}))
vi.mock('echarts/charts',()=>({LineChart:{},BarChart:{},PieChart:{}}))
vi.mock('echarts/components',()=>({GridComponent:{},TooltipComponent:{},GraphicComponent:{}}))
vi.mock('echarts/renderers',()=>({CanvasRenderer:{}}))

const page=(items:Record<string,unknown>[]=[])=>( {items,total:items.length,page:0,size:100} )
beforeEach(()=>{
 vi.useFakeTimers();vi.setSystemTime(new Date('2026-10-07T15:59:40Z'))
 vi.clearAllMocks();mocks.instances.length=0;mocks.observers.length=0
 mocks.can.mockReturnValue(true);mocks.api.mockResolvedValue(page());mocks.audit.mockResolvedValue(page())
 mocks.init.mockImplementation(()=>{const instance={setOption:vi.fn(),resize:vi.fn(),dispose:vi.fn()};mocks.instances.push(instance);return instance})
 vi.stubGlobal('ResizeObserver',class {record:{callback:()=>void,observe:Spy,disconnect:Spy};constructor(callback:()=>void){this.record={callback,observe:vi.fn(),disconnect:vi.fn()};mocks.observers.push(this.record)}observe(node:Element){this.record.observe(node)}disconnect(){this.record.disconnect()}})
})
afterEach(()=>{cleanup();vi.unstubAllGlobals();vi.useRealTimers()})
const settle=()=>vi.advanceTimersByTimeAsync(0)

describe('home workbench audit regressions',()=>{
 it('never reads inaccessible business facts or enables their shortcuts',async()=>{
  mocks.can.mockReturnValue(false)
  render(DashboardView);await settle()
  expect(mocks.api).not.toHaveBeenCalled();expect(mocks.audit).not.toHaveBeenCalled()
  expect((screen.getByText('新建生产批次').closest('button') as HTMLButtonElement).disabled).toBe(true)
  expect(screen.getByText('在生产批次').closest('button')?.querySelector('strong')?.textContent).toBe('无权限')
 })
 it('counts the frozen IN_PROGRESS main-batch state and collected test samples, excluding retention',async()=>{
  mocks.api.mockImplementation(({url}:{url:string})=>Promise.resolve(page(url==='/main-batches'?[{status:'IN_PROGRESS'},{status:'PRODUCTION_COMPLETED'}]:url==='/quality/samples'?[{sampleType:'TEST_SAMPLE',status:'COLLECTED'},{sampleType:'RETEST_SAMPLE',status:'RECEIVED'},{sampleType:'RETENTION_SAMPLE',status:'RECEIVED'},{sampleType:'TEST_SAMPLE',status:'IN_TEST'}]:[])))
  render(DashboardView);await settle()
  expect(screen.getByText('在生产批次').closest('button')?.querySelector('strong')?.textContent).toBe('1')
  expect(screen.getByText('待检样品').closest('button')?.querySelector('strong')?.textContent).toBe('2')
 })
 it('does not draw zero-valued trends for source rows with missing creation dates',async()=>{
  mocks.api.mockResolvedValue(page([{batchNo:'20231218',status:'IN_PROGRESS'}]))
  render(DashboardView);await settle()
  const option=mocks.instances[0]?.setOption.mock.lastCall?.[0]
  expect(option.series).toEqual([])
  expect(option.graphic[0].style.text).toBe('暂无可用统计数据')
 })
 it('resizes charts when their container changes and disconnects observers on navigation',async()=>{
  const view=render(DashboardView);await settle()
  expect(mocks.observers.flatMap(o=>o.observe.mock.calls)).toHaveLength(3)
  mocks.observers.forEach(o=>o.callback())
  for(const instance of mocks.instances)expect(instance.resize).toHaveBeenCalled()
  view.unmount()
  for(const observer of mocks.observers)expect(observer.disconnect).toHaveBeenCalledOnce()
  for(const instance of mocks.instances)expect(instance.dispose).toHaveBeenCalledOnce()
  expect(vi.getTimerCount()).toBe(0)
 })
 it('does not initialize charts or attach resize handlers after leaving during a pending read',async()=>{
  let resolve!: (value:ReturnType<typeof page>)=>void
  mocks.api.mockReturnValue(new Promise(done=>{resolve=done}))
  const listener=vi.spyOn(window,'addEventListener')
  const view=render(DashboardView);view.unmount();resolve(page());await settle()
  expect(mocks.init).not.toHaveBeenCalled()
  expect(listener.mock.calls.filter(([name])=>name==='resize')).toEqual([])
  expect(mocks.observers).toHaveLength(0)
  expect(vi.getTimerCount()).toBe(0);listener.mockRestore()
 })
 it('rolls seven-day charts over at Shanghai midnight without rereading or recreating instances',async()=>{
  render(DashboardView);await settle()
  const count=mocks.api.mock.calls.length
  await vi.advanceTimersByTimeAsync(60000)
  expect(mocks.instances[0]?.setOption.mock.lastCall?.[0].xAxis.data.slice(-1)[0]).toBe('10-08')
  expect(mocks.init).toHaveBeenCalledTimes(3)
  expect(mocks.api).toHaveBeenCalledTimes(count)
 })
})
