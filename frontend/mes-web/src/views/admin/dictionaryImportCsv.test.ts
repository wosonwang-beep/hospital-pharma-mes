import {describe,expect,it} from 'vitest'
import {csvTemplate,parseCsv,parseDictionaryImport} from './dictionaryImportCsv'
describe('dictionary CSV import',()=>{
 it('reads the supplied UTF-8 Excel-compatible template',()=>{
  const rows=parseDictionaryImport(csvTemplate+'EQUIPMENT_CATEGORY,设备分类,BUSINESS,FLAT,设备分类示例,10,ACTIVE\r\n')
  expect(rows).toHaveLength(1)
  expect(rows[0]).toMatchObject({code:'EQUIPMENT_CATEGORY',name:'设备分类',kind:'BUSINESS',structure:'FLAT',sortNo:10,status:'ACTIVE'})
 })
 it('supports quoted commas, line breaks and escaped double quotes',()=>{
  expect(parseCsv('字典,说明\r\n"设备,分类","第一行\n""第二行"""')).toEqual([['字典','说明'],['设备,分类','第一行\n"第二行"']])
 })
 it('rejects malformed structure and out-of-range sort',()=>{
  expect(()=>parseDictionaryImport('wrong,header\r\nABC')).toThrow('表头不匹配')
  expect(()=>parseDictionaryImport((csvTemplate+'EQUIPMENT_CATEGORY,设备分类,BUSINESS,FLAT,示例,-1,ACTIVE\r\n'))).toThrow('排序')
  expect(()=>parseCsv('a,"unterminated')).toThrow('引号')
 })
})
