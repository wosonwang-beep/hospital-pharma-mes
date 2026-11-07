import {afterEach,describe,it,expect,vi} from 'vitest'
import {render,screen,fireEvent,cleanup} from '@testing-library/vue'
import Antd from 'ant-design-vue'
import OperationItemDialog from './OperationItemDialog.vue'
afterEach(()=>{cleanup();vi.restoreAllMocks()})
const parameter={parameterCode:'STIR_TIME',parameterName:'搅拌时间',acquisitionMode:'MANUAL',unitId:null,lowerLimit:'10',upperLimit:'20'}
async function setup(value=parameter){const view=render(OperationItemDialog,{props:{open:false,kind:'PARAMETER',value},global:{plugins:[Antd],stubs:{ProcessLookup:true}}});await view.rerender({open:true});return view}
describe('current operation item draft isolation',()=>{
 it('cancel keeps the parent parameter unchanged',async()=>{const value={...parameter};const view=await setup(value);await fireEvent.update(screen.getByLabelText('名称'),'配制搅拌时间');await fireEvent.click(screen.getByRole('button',{name:'取 消'}));expect(value).toEqual(parameter);expect(view.emitted('save')).toBeUndefined();expect(view.emitted('cancel')).toHaveLength(1)})
 it('rejects reversed numeric bounds without submitting a partial value',async()=>{const view=await setup();await fireEvent.update(screen.getByLabelText('下限'),'30');await fireEvent.click(screen.getByRole('button',{name:'保 存'}));expect(screen.getByRole('alert').textContent).toContain('下限不能大于上限');expect(view.emitted('save')).toBeUndefined()})
 it('saves an isolated current parameter with its acquisition and unit identity',async()=>{const view=await setup();await fireEvent.update(screen.getByLabelText('名称'),'配制搅拌时间');await fireEvent.click(screen.getByRole('button',{name:'保 存'}));expect((view.emitted('save') as unknown[][]|undefined)?.[0]?.[0]).toEqual({...parameter,parameterName:'配制搅拌时间'});expect(parameter.parameterName).toBe('搅拌时间')})
 it('keeps parameter decimal precision consistent with the server contract',async()=>{const view=await setup();await fireEvent.update(screen.getByLabelText('下限'),'0.0000001');await fireEvent.click(screen.getByRole('button',{name:'保 存'}));expect(screen.getByRole('alert').textContent).toContain('6位小数');expect(view.emitted('save')).toBeUndefined()})
 it('compares IPC bounds exactly at DECIMAL(24,8) precision',async()=>{const value={ipcCode:'PRESSURE',name:'过滤压力',required:true,resultType:'NUMERIC',lowerLimit:'9999999999999999.00000002',upperLimit:'9999999999999999.00000001',expectedText:null,unitId:null,methodReference:'过滤压力检查规程'};const view=render(OperationItemDialog,{props:{open:false,kind:'IPC',value},global:{plugins:[Antd],stubs:{ProcessLookup:true}}});await view.rerender({open:true});await fireEvent.click(screen.getByRole('button',{name:'保 存'}));expect(screen.getByRole('alert').textContent).toContain('下限不能大于上限');expect(view.emitted('save')).toBeUndefined()})
})
