import {describe,it,expect,vi,beforeEach} from 'vitest'
const request=vi.hoisted(()=>vi.fn())
vi.mock('./http',()=>({api:request,http:{get:request,post:request}}))
import {generate,history,pdf} from './printing'
import {permittedNavigation} from '../layouts/navigation'
describe('controlled business printing contract',()=>{
 beforeEach(()=>request.mockReset())
 it('returns the PDF directly using only authorized business/template identifiers',async()=>{const blob=new Blob(['%PDF-current']);request.mockResolvedValue({data:blob});expect(await generate('11','68',true)).toBe(blob);expect(request).toHaveBeenCalledWith('/printing/artifacts',{businessType:'INSPECTION_REPORT',businessId:'11',templateVersionId:'68',formal:true},{responseType:'blob'});expect(request).toHaveBeenCalledTimes(1)})
 it('reads archived versions and the stored binary rather than regenerating for viewing',async()=>{request.mockResolvedValueOnce([]).mockResolvedValueOnce({data:new Blob(['test'])});await history('report-17');await pdf('artifact');expect(request.mock.calls.map(x=>x[0].url??x[0])).toEqual(['/printing/artifacts','/printing/artifacts/artifact/pdf']);expect(request.mock.calls[1]?.[1]).toEqual({responseType:'blob'})})
 it('exposes the shared menu only with its dedicated view permission',()=>{expect(permittedNavigation(()=>false).flatMap(g=>g.items).some(i=>i.key==='print-templates')).toBe(false);expect(permittedNavigation(p=>p==='print:template:view').flatMap(g=>g.items).map(i=>i.key)).toEqual(['print-templates'])})
})
