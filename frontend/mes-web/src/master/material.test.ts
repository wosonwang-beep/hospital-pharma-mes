import {describe,it,expect} from 'vitest'
import {initialMaterialForm,materialPayload,supplierPayload,type SupplierLink} from './material'
function form(){return {...initialMaterialForm(),materialCode:'M1',materialName:'Material',materialType:'FINISHED',baseUnitId:'9007199254740993',lotControlled:true}}
const link=(id:string,preferred:boolean):SupplierLink=>({supplierId:id,approved:true,preferred,validTo:null})
describe('basic material and one preferred supplier',()=>{
 it('flat DTO has exact ID and no business version/approval',()=>{const p=materialPayload(form());expect(p.baseUnitId).toBe('9007199254740993');expect(p.requiresIncomingInspection).toBe(true);for(const key of ['data','quality','storage','production','status','versionId','versionNoBusiness','genericName','englishName','aliasName'])expect(p).not.toHaveProperty(key)})
 it('hidden UI fields retain original values in the unchanged update contract',()=>{const existing={...form(),manufacturerName:'原厂家',appearance:'原外观',lotControlled:false,effectiveFrom:'2026-01-01T00:00:00',effectiveTo:'2028-01-01T00:00:00'};const p=materialPayload({...existing,materialName:'维护名称'},true);expect(p).toMatchObject({manufacturerName:'原厂家',appearance:'原外观',lotControlled:false,effectiveFrom:'2026-01-01T00:00:00.000Z',effectiveTo:'2028-01-01T00:00:00.000Z'});expect(p.storageCondition).toBeNull()})
 it('direct edits omit fixed code and validate required basic fields',()=>{const p=materialPayload(form(),true);expect(p).not.toHaveProperty('materialCode');expect(p.materialName).toBe('Material');expect(()=>materialPayload(initialMaterialForm())).toThrow('请填写')})
 it('multiple suppliers have exactly one preferred',()=>{expect(supplierPayload([link('1',true),link('2',false)])).toHaveLength(2);expect(supplierPayload([])).toEqual([])})
 it('rejects zero/multiple preferred and revoked preferred',()=>{expect(()=>supplierPayload([link('1',false)])).toThrow('一个首选');expect(()=>supplierPayload([link('1',true),link('2',true)])).toThrow('一个首选');expect(()=>supplierPayload([{...link('1',true),approved:false}])).toThrow('一个首选')})
 it('strips display metadata and rejects duplicates',()=>{expect(supplierPayload([{...link('1',true),supplierName:'Display only'}])[0]).not.toHaveProperty('supplierName');expect(()=>supplierPayload([link('1',true),link('1',false)])).toThrow('重复')})
})
