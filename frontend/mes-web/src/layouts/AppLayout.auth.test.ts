import {cleanup,fireEvent,render,screen} from '@testing-library/vue'
import {nextTick,reactive} from 'vue'
import {afterEach,beforeEach,describe,expect,it,vi} from 'vitest'
import AppLayout from './AppLayout.vue'

const mocks=vi.hoisted(()=>({auth:null as any,replace:vi.fn(),push:vi.fn(),warning:vi.fn()}))
vi.mock('../stores/auth',()=>({useAuthStore:()=>mocks.auth}))
vi.mock('vue-router',()=>({useRoute:()=>({name:'wms-issues',path:'/wms/issues',fullPath:'/wms/issues?page=2',meta:{title:'出库管理'}}),useRouter:()=>({replace:mocks.replace,push:mocks.push})}))
vi.mock('ant-design-vue',()=>({message:{warning:mocks.warning}}))
const slot={template:'<div><slot/></div>'}
const options={global:{stubs:{ALayout:slot,ALayoutSider:slot,ALayoutHeader:slot,ALayoutContent:slot,AMenu:slot,AMenuItem:slot,AMenuItemGroup:slot,ASubMenu:slot,AInput:slot,AAutoComplete:slot,ABreadcrumb:slot,ABreadcrumbItem:slot,AAvatar:slot,ATooltip:slot,AButton:slot,ADropdown:slot,RouterView:{template:'<div>受控业务详情</div>'}}}}
beforeEach(()=>{
 vi.clearAllMocks();vi.stubGlobal('matchMedia',()=>({matches:false}))
 mocks.auth=reactive({identity:{displayName:'系统管理员'},can:()=>false,logout:vi.fn()})
 mocks.replace.mockResolvedValue(undefined)
})
afterEach(()=>{cleanup();vi.unstubAllGlobals()})

describe('authenticated shell session loss',()=>{
 it('removes controlled content and returns to login with the original route after session loss',async()=>{
  render(AppLayout,options)
  expect(screen.getByText('受控业务详情')).toBeTruthy()
  mocks.auth.identity=null;await nextTick()
  expect(screen.queryByText('受控业务详情')).toBeNull()
  expect(mocks.replace).toHaveBeenCalledWith({path:'/login',query:{redirect:'/wms/issues?page=2'}})
 })
 it('does not redirect while no previously authenticated identity has been established',async()=>{
  mocks.auth.identity=null;render(AppLayout,options);await nextTick()
  expect(mocks.replace).not.toHaveBeenCalled()
  expect(screen.queryByText('受控业务详情')).toBeNull()
 })
 it('still navigates away and warns when server logout fails, without duplicate session-loss redirects',async()=>{
  mocks.auth.logout.mockImplementation(async()=>{mocks.auth.identity=null;throw new Error('Server unavailable')})
  render(AppLayout,options);await fireEvent.click(screen.getByRole('button',{name:'退出'}));await nextTick()
  expect(mocks.warning).toHaveBeenCalledWith('本机已退出，服务器会话注销未完成')
  expect(mocks.replace).toHaveBeenCalledExactlyOnceWith('/login')
 })
 it('navigates once on successful explicit logout',async()=>{
  mocks.auth.logout.mockImplementation(async()=>{mocks.auth.identity=null})
  render(AppLayout,options);await fireEvent.click(screen.getByRole('button',{name:'退出'}));await nextTick()
  expect(mocks.warning).not.toHaveBeenCalled()
  expect(mocks.replace).toHaveBeenCalledExactlyOnceWith('/login')
 })
})
