import {isAxiosError} from 'axios'
import {api,http,errorMessage,type Page} from './http'
export type PrintType = 'DOCUMENT' | 'LIST'
export const printTypeLabels:Record<PrintType,string>={DOCUMENT:'单据',LIST:'列表'}
export interface PrintSource {value:string;module:string;label:string}
export interface PrintTemplate {printType:PrintType;id:string;templateCode:string;templateName:string;templateRevision:number;businessType:string;status:string;versionNo:number;previewHash?:string;sourceExecutionMode?:string;sourceId?:string;sourceRevisionId?:string;sourceConfigHash?:string}
export interface PrintArtifact {id:string;reportNo:string;businessVersion:string;templateRevision:number;formal:boolean;createdAt:string;pdfHash:string}
export interface PrintField {key:string;label:string;group:string;description:string;example:string;repeated:boolean;dataType?:string;unitField?:string;datasetKey?:string}
export const templates=(params:Record<string,unknown>)=>api<Page<PrintTemplate>>({url:'/printing/templates',params})
export const applicable=()=>api<PrintTemplate[]>({url:'/printing/applicable',params:{businessType:'INSPECTION_REPORT'}}).then(rows=>rows.filter(row=>row.printType!=='LIST'))
export const history=(businessId:string)=>api<PrintArtifact[]>({url:'/printing/artifacts',params:{businessType:'INSPECTION_REPORT',businessId}})
export function generate(businessId:string,templateVersionId:string,formal:boolean){return http.post<Blob>('/printing/artifacts',{businessType:'INSPECTION_REPORT',businessId,templateVersionId,formal},{responseType:'blob'}).then(response=>response.data)}
export const pdf=async(id:string)=>(await http.get<Blob>(`/printing/artifacts/${id}/pdf`,{responseType:'blob'})).data
export const templatePreview=async(id:string)=>(await http.get<Blob>(`/printing/templates/${id}/preview`,{responseType:'blob'})).data
export function downloadBlob(blob:Blob,name:string){const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download=name;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000)}
// Binary success responses still contain the normal JSON error envelope on failure.
export async function printErrorMessage(error:unknown){
 if(isAxiosError(error)&&error.response?.status!==403&&error.response?.data instanceof Blob){
  const body=error.response.data
  if(body.size<=65536){try{const value=JSON.parse(await body.text());if(typeof value.message==='string'&&value.message.trim())return value.message}catch{/* Keep the shared fallback for non-JSON errors. */}}
 }
 return errorMessage(error)
}
export const stateLabels:Record<string,string>={DRAFT:'草稿',VALIDATED:'已验证',PUBLISHED:'已发布',INACTIVE:'已停用'}
