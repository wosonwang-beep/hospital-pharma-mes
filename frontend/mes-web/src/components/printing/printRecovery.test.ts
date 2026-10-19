import {afterEach,beforeEach,describe,expect,it,vi} from 'vitest'
import {cleanup,fireEvent,render,screen,waitFor} from '@testing-library/vue'
import Antd from 'ant-design-vue'
const probe=vi.hoisted(()=>({allowed:true,api:vi.fn(),post:vi.fn()}))
vi.mock('../../stores/auth',()=>({useAuthStore:()=>({can:()=>probe.allowed})}))
vi.mock('../../api/http',()=>({api:probe.api,http:{post:probe.post},errorMessage:()=> '请求失败，请稍后重试'}))
vi.mock('./PdfPreview.vue',()=>({default:{props:['blob','name'],template:'<div data-testid="pdf-result">PDF result</div>'}}))
import BatchPrintButton from './BatchPrintButton.vue'
import InspectionPrintPanel from './InspectionPrintPanel.vue'
import {printErrorMessage} from '../../api/printing'

const choices=[{id:'69',templateName:'收货记录打印',templateRevision:2,printType:'DOCUMENT',businessType:'WMS_RECEIPT'}]
const mount=()=>render(BatchPrintButton,{props:{businessType:'WMS_RECEIPT',recordIds:['22']},global:{plugins:[Antd]}})
async function open(){await fireEvent.click(screen.getByRole('button',{name:'打印选中记录（1）'}));await waitFor(()=>expect(screen.getByRole('button',{name:'生成打印预览'}).hasAttribute('disabled')).toBe(false))}
const deferred=<T,>()=>{let resolve!:(value:T)=>void;const promise=new Promise<T>(r=>{resolve=r});return {promise,resolve}}
beforeEach(()=>{probe.allowed=true;probe.api.mockReset();probe.post.mockReset();probe.api.mockResolvedValue(choices)})
afterEach(cleanup)

describe('printing recovery and permission component contracts (not real-user acceptance)',()=>{
 it('does not show print controls or query templates without print permission',()=>{
  probe.allowed=false;mount()
  expect(screen.queryByRole('button',{name:'打印选中记录（1）'})).toBeNull()
  expect(probe.api).not.toHaveBeenCalled()
 })
 it('prevents duplicate submission even when click handlers run before loading disables the button',async()=>{
  const result=deferred<{data:Blob}>();probe.post.mockReturnValue(result.promise)
  mount();await open();const button=screen.getByRole('button',{name:'生成打印预览'})
  await fireEvent.click(button);await fireEvent.click(button)
  expect(probe.post).toHaveBeenCalledTimes(1)
  result.resolve({data:new Blob(['%PDF-current'])})
  await screen.findByTestId('pdf-result')
 })
 it('ignores a previous print result that arrives after cancel and reopen',async()=>{
  const result=deferred<{data:Blob}>();probe.post.mockReturnValue(result.promise)
  mount();await open();await fireEvent.click(screen.getByRole('button',{name:'生成打印预览'}))
  await fireEvent.click(document.querySelector('.ant-modal-close')!)
  await open();result.resolve({data:new Blob(['%PDF-previous'])})
  await waitFor(()=>expect(screen.queryByTestId('pdf-result')).toBeNull())
  expect(screen.getByRole('button',{name:'生成打印预览'}).hasAttribute('disabled')).toBe(false)
 })
 it('ignores an older applicable-template response after cancel and a new open',async()=>{
  const first=deferred<unknown[]>();probe.api.mockReturnValueOnce(first.promise).mockResolvedValueOnce(choices)
  mount();await fireEvent.click(screen.getByRole('button',{name:'打印选中记录（1）'}))
  await fireEvent.click(document.querySelector('.ant-modal-close')!)
  await open();first.resolve([])
  await waitFor(()=>expect(screen.getByRole('button',{name:'生成打印预览'}).hasAttribute('disabled')).toBe(false))
 })
 it('keeps the report template label and disables generation with no published report template',async()=>{
  probe.api.mockResolvedValue([])
  render(InspectionPrintPanel,{props:{businessId:'11',approved:true},global:{plugins:[Antd]}})
  await waitFor(()=>expect(screen.getByRole('combobox',{name:'打印模板'})).toBeTruthy())
  const button=await screen.findByRole('button',{name:'生成并预览 PDF'})
  expect(button.hasAttribute('disabled')).toBe(true)
  expect(probe.post).not.toHaveBeenCalled()
 })
 it('reads the server JSON message inside a binary error response',async()=>{
  const body=new Blob(['{"message":"当前单据不存在"}'],{type:'application/json'})
  // jsdom Blob lacks text(); browsers expose it. Supply that browser API only.
  Object.defineProperty(body,'text',{value:async()=>'{"message":"当前单据不存在"}'})
  expect(await printErrorMessage({isAxiosError:true,response:{status:404,data:body}})).toBe('当前单据不存在')
 })
 it('uses the safe shared fallback for non-JSON binary errors',async()=>{
  const body=new Blob(['unavailable'])
  Object.defineProperty(body,'text',{value:async()=>'unavailable'})
  expect(await printErrorMessage({isAxiosError:true,response:{status:500,data:body}})).toBe('请求失败，请稍后重试')
 })
 it('does not misreport a network failure as a missing template and recovers on reopen',async()=>{
  probe.api.mockRejectedValueOnce(new Error('network unavailable')).mockResolvedValueOnce(choices)
  mount();await fireEvent.click(screen.getByRole('button',{name:'打印选中记录（1）'}))
  await screen.findByText('请求失败，请稍后重试')
  expect(screen.queryByText('当前功能没有已发布并启用绑定的模板，请先在打印模板中发布并绑定。')).toBeNull()
  await fireEvent.click(document.querySelector('.ant-modal-close')!)
  await open()
  expect(screen.queryByText('请求失败，请稍后重试')).toBeNull()
 })
})
