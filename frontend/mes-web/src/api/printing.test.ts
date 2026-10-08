import {describe,it,expect,vi,beforeEach} from 'vitest'
const request=vi.hoisted(()=>vi.fn())
vi.mock('./http',()=>({api:request,http:{get:request}}))
import {generate,history,pdf} from './printing'
import {permittedNavigation} from '../layouts/navigation'
describe('controlled business printing contract',()=>{
 beforeEach(()=>request.mockReset())
 it('sends only business and published-template identifiers plus requested mode',async()=>{request.mockResolvedValue({id:'artifact'});await generate('report-17','template-8',true);expect(request).toHaveBeenCalledWith({url:'/printing/artifacts',method:'POST',data:{businessType:'INSPECTION_REPORT',businessId:'report-17',templateVersionId:'template-8',formal:true}})})
 it('reads archived versions and the stored binary rather than regenerating for viewing',async()=>{request.mockResolvedValueOnce([]).mockResolvedValueOnce({data:new Blob(['test'])});await history('report-17');await pdf('artifact');expect(request.mock.calls.map(x=>x[0].url??x[0])).toEqual(['/printing/artifacts','/printing/artifacts/artifact/pdf']);expect(request.mock.calls[1]?.[1]).toEqual({responseType:'blob'})})
 it('exposes the shared menu only with its dedicated view permission',()=>{expect(permittedNavigation(()=>false).flatMap(g=>g.items).some(i=>i.key==='print-templates')).toBe(false);expect(permittedNavigation(p=>p==='print:template:view').flatMap(g=>g.items).map(i=>i.key)).toEqual(['print-templates'])})
})
