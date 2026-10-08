import {api,http} from './http'
import type {BatchBook,BookDefinition,BookPdf,BookTemplate} from '../views/ebr/bookModel'
export const bookApi={
 templates:()=>api<BookTemplate[]>({url:'/ebr/book-templates'}),
 create:(templateCode:string,definition:BookDefinition,reason:string)=>api<BookTemplate>({url:'/ebr/book-templates',method:'POST',headers:{'Idempotency-Key':crypto.randomUUID()},data:{templateCode,definition,reason}}),
 transition:(t:BookTemplate,action:'publish'|'deactivate',reason:string)=>api<BookTemplate>({url:`/ebr/book-templates/${t.id}/${action}`,method:'POST',headers:{'Idempotency-Key':crypto.randomUUID(),'If-Match':`"${t.versionNo}"`},data:{versionNo:t.versionNo,reason}}),
 read:(id:string)=>api<BatchBook>({url:`/main-batches/${id}/book`}),
 history:(id:string)=>api<BookPdf[]>({url:`/main-batches/${id}/book/pdfs`}),
 generate:(book:BatchBook,archiveKind:'REVIEW_COPY'|'FINAL',reason:string)=>api<BookPdf>({url:`/main-batches/${book.mainBatchId}/book/pdfs`,method:'POST',headers:{'Idempotency-Key':crypto.randomUUID(),'If-Match':`"${book.versionNo}"`},data:{versionNo:book.versionNo,archiveKind,reason}}),
 pdf:async(batch:string,record:BookPdf)=>{const {data}=await http.get<ArrayBuffer>(`/main-batches/${batch}/book/pdfs/${record.id}/content`,{responseType:'arraybuffer'});const actual=Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',data))).map(n=>n.toString(16).padStart(2,'0')).join('');if(actual!==record.pdf_hash)throw Error('整册PDF摘要校验失败');return new Blob([data],{type:'application/pdf'})}
}
