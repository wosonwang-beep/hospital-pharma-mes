import {api,http,type Page} from './http'
export interface PrintTemplate {id:string;templateCode:string;templateName:string;templateRevision:number;businessType:string;status:string;versionNo:number;previewHash?:string}
export interface PrintArtifact {id:string;reportNo:string;businessVersion:string;templateRevision:number;formal:boolean;createdAt:string;pdfHash:string}
export interface PrintField {key:string;label:string;group:string;description:string;example:string;repeated:boolean}
export const templates=(params:Record<string,unknown>)=>api<Page<PrintTemplate>>({url:'/printing/templates',params})
export const applicable=()=>api<PrintTemplate[]>({url:'/printing/applicable',params:{businessType:'INSPECTION_REPORT'}})
export const history=(businessId:string)=>api<PrintArtifact[]>({url:'/printing/artifacts',params:{businessType:'INSPECTION_REPORT',businessId}})
export function generate(businessId:string,templateVersionId:string,formal:boolean){return api<PrintArtifact>({url:'/printing/artifacts',method:'POST',data:{businessType:'INSPECTION_REPORT',businessId,templateVersionId,formal}})}
export const pdf=async(id:string)=>(await http.get<Blob>(`/printing/artifacts/${id}/pdf`,{responseType:'blob'})).data
export const templatePreview=async(id:string)=>(await http.get<Blob>(`/printing/templates/${id}/preview`,{responseType:'blob'})).data
export function downloadBlob(blob:Blob,name:string){const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download=name;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000)}
export const stateLabels:Record<string,string>={DRAFT:'草稿',VALIDATED:'已验证',PUBLISHED:'已发布',INACTIVE:'已停用'}
