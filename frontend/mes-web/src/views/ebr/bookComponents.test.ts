import {describe,it,expect} from 'vitest'
import {appendRecordComponent} from './bookComponents'
import {emptyDefinition} from './model'
import type {EbrForm} from './types'
describe('批记录可复用组件',()=>{
 function setup(){const d=emptyDefinition();const f:EbrForm={formCode:'PREPARE',formName:'配制',operationDefId:null,schemaVersion:'1.0',sequenceNo:1,fields:[]};d.forms.push(f);return {d,f}}
 it('检查结果没有默认正常或默认勾选',()=>{const {d,f}=setup();appendRecordComponent(d,f,'CHECKS');expect(f.fields.every(v=>v.defaultExpr===null)).toBe(true);expect(f.fields.find(v=>v.fieldCode.endsWith('_ACTUAL'))?.options).toHaveLength(2)})
 it('重复组件不重复字段编码并允许多行',()=>{const {d,f}=setup();appendRecordComponent(d,f,'CHECKS');appendRecordComponent(d,f,'CHECKS');expect(new Set(f.fields.map(v=>v.fieldCode)).size).toBe(f.fields.length);expect(d.sections.every(s=>s.groups[0]?.repeatMode==='LIST')).toBe(true)})
 it('投料仅保留来源引用，不创建第二个实际扣库存数量',()=>{const {d,f}=setup();appendRecordComponent(d,f,'MATERIAL_REFERENCES');expect(f.fields.map(v=>v.fieldCode)).toEqual([expect.stringMatching(/_CHARGE$/),expect.stringMatching(/_NOTE$/)]);expect(f.fields.some(v=>v.fieldType==='CALCULATED')).toBe(false)})
})
