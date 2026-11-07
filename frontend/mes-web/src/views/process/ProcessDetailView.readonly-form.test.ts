import {afterEach,it,expect,vi} from 'vitest'
import {render,screen,fireEvent,cleanup} from '@testing-library/vue'
import Antd from 'ant-design-vue'
import ProcessDetailView from './ProcessDetailView.vue'
const {api,push}=vi.hoisted(()=>({api:vi.fn(),push:vi.fn()}))
vi.mock('../../api/http',()=>({api,idempotencyKey:()=> 'key',errorMessage:()=> '请求失败'}))
vi.mock('../../stores/auth',()=>({useAuthStore:()=>({can:(p:string)=>p==='ebr:template:view'})}))
vi.mock('vue-router',()=>({useRoute:()=>({meta:{mode:'view'},params:{id:'1'},query:{},fullPath:'/process/packages/1'}),useRouter:()=>({push}),onBeforeRouteLeave:vi.fn()}))
afterEach(()=>{cleanup();vi.clearAllMocks();sessionStorage.clear()})
it('opens the existing view route for a form reader without granting designer permission',async()=>{
 api.mockImplementation(async({url}:{url:string})=>url==='/process-packages/1'?{id:'1',packageCode:'KCL30',status:'ACTIVE',currentDefinition:{id:'9',route:{routeCode:'R1',operations:[{operationCode:'OP1',operationName:'配制',sequenceNo:1,parameters:[],ipcDefinitions:[]}]}}}:url==='/ebr/templates'?{items:[{id:'496'}],total:1,page:0,size:100}:{id:'496',templateCode:'EBR-KCL',templateName:'配制记录',version:1,status:'EFFECTIVE',operationChoices:[{id:'12',operationCode:'OP1'}],definition:{forms:[{formCode:'PREPARE_RECORD',formName:'配制记录',operationDefId:'12',fields:[]}]}})
 render(ProcessDetailView,{global:{plugins:[Antd],stubs:{ProcessLookup:true}}});await fireEvent.click(await screen.findByRole('tab',{name:'工序表单'}));await fireEvent.click(await screen.findByRole('button',{name:'查看表单'}));expect(push).toHaveBeenCalledWith({path:'/ebr/templates/496',query:{processPackageId:'1',formCode:'PREPARE_RECORD',designerTab:'form',returnTo:'/process/packages/1'}});expect(screen.queryByRole('button',{name:'设计表单'})).toBeNull()
})
