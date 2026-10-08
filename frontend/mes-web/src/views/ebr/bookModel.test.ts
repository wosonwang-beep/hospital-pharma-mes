import {describe,it,expect} from 'vitest'
import {mayFormal,newBookEntry,type BatchBook} from './bookModel'
describe('受控整册目录',()=>{
 it('正式要求真实QA状态及所有必需项完成',()=>{const book={legacy:false,status:'QA_RELEASED',entries:[{definition:{required:true},state:'DRAFT'}]} as BatchBook;expect(mayFormal(book)).toBe(false);book.entries[0]!.state='COMPLETE';expect(mayFormal(book)).toBe(true);book.status='IN_PROGRESS';expect(mayFormal(book)).toBe(false)})
 it('旧批次不能静默套用新模板',()=>expect(mayFormal({legacy:true,status:'QA_RELEASED',entries:[]} as unknown as BatchBook)).toBe(false))
 it('新目录项不默认确认正常且不隐式设整批',()=>{const entry=newBookEntry(2);expect(entry.scope).toBe('OPERATION');expect(entry.fields).toEqual([]);expect(entry.formCode).toBeNull()})
})
