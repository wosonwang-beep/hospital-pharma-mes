import {describe,it,expect} from 'vitest'
import {initialMaterialForm,materialPayload,supplierPayload,type SupplierLink} from './material'
function form(){return {...initialMaterialForm(),materialCode:'M1',materialName:'Material',materialType:'FINISHED',baseUnitId:'9007199254740993',lotControlled:true}}
const link=(id:string,preferred:boolean):SupplierLink=>({supplierId:id,approved:true,preferred,validTo:null})
describe('basic material and one preferred supplier',()=>{
 it('flat DTO has exact ID and no business version/approval',()=>{const p=materialPayload(form());expect(p.baseUnitId).toBe('9007199254740993');expect(p.requiresIncomingInspection).toBe(true);for(const key of ['data','quality','storage','production','status','versionId','versionNoBusiness','genericName','englishName','aliasName'])expect(p).not.toHaveProperty(key)})
 it('direct edits omit fixed code and validate required basic fields',()=>{const p=materialPayload(form(),true);expect(p).not.toHaveProperty('materialCode');expect(p.materialName).toBe('Material');expect(()=>materialPayload(initialMaterialForm())).toThrow('请填写')})
 it('multiple suppliers have exactly one preferred',()=>{expect(supplierPayload([link('1',true),link('2',false)])).toHaveLength(2);expect(supplierPayload([])).toEqual([])})
 it('rejects zero/multiple preferred and revoked preferred',()=>{expect(()=>supplierPayload([link('1',false)])).toThrow('一个首选');expect(()=>supplierPayload([link('1',true),link('2',true)])).toThrow('一个首选');expect(()=>supplierPayload([{...link('1',true),approved:false}])).toThrow('一个首选')})
 it('strips display metadata and rejects duplicates',()=>{expect(supplierPayload([{...link('1',true),supplierName:'Display only'}])[0]).not.toHaveProperty('supplierName');expect(()=>supplierPayload([link('1',true),link('1',false)])).toThrow('重复')})
})
