export type BookKind='FORM'|'PROCESS_INSTRUCTIONS'|'PRODUCTION_ORDER'|'MATERIAL_REQUESTS'|'MATERIAL_ISSUES'|'CHARGES'|'CLEARANCE'|'INSPECTION_REQUESTS'|'SAMPLING_RECORDS'|'INSPECTION_RECORDS'|'INSPECTION_REPORTS'|'QUALITY_INVESTIGATIONS'|'MATERIAL_BALANCE'|'QA_DECISIONS'|'ATTACHMENTS'
export type BookFlowType='SYSTEM'|'PROCESS'|'BUSINESS'
export interface BookMapping{productId:string;packageVersionId?:string;processPackageId?:string;ebrTemplateVersionId:string}
export interface BookEntry{
 code:string
 flowType?:BookFlowType
 flowCode?:string
 flowName?:string
 chapter?:string
 title:string
 kind:BookKind
 order:number
 required:boolean
 minCount:number
 scope:'BATCH'|'OPERATION'
 operationCode:string|null
 formCode:string|null
 printTemplateVersionId:string|null
 fields:string[]
 attachmentIds:string[]
 archiveStage?:'PRODUCTION_REVIEW'|'QA_REVIEW'|'POST_RELEASE'|null
 applicability?:string|null
}
export interface BookDefinition{
 templateName?:string
 processPackageId?:string
 ebrTemplateVersionId?:string
 entries:BookEntry[]
 // Legacy retained read-only contract.
 varietyCode?:string
 varietyName?:string
 mappings?:BookMapping[]
}
export interface BookTemplate{id:string;templateCode:string;revision:number;status:string;versionNo:number;definition:BookDefinition;definitionHash:string;createdBy:string}
export interface BookDocument{sourceType:string;sourceId:string;sourceVersion:string;sourceHash:string;title:string;status:string;columns:string[];rows:string[][];formId?:string;executionUnitId?:string;url?:string;signatureIds?:string[]}
export interface BookItem{definition:BookEntry;state:string;notice?:string;documents:BookDocument[]}
export interface BatchBook{mainBatchId:string;versionNo:number;status:string;batchNo:string;productName:string;specification:string;plannedQty:string;unitId:string;unitName:string;templateName?:string;varietyName?:string;legacy:boolean;notice?:string;entries:BookItem[];book?:{templateVersionId:string;templateRevision:number;definitionHash:string}}
export interface BookPdf{id:string;pdf_hash:string;source_hash:string;archive_kind:'REVIEW_COPY'|'FINAL';created_at:string;template_version_id:string}
export const bookKinds:Record<BookKind,string>={FORM:'工序表单',PROCESS_INSTRUCTIONS:'生产工艺记录',PRODUCTION_ORDER:'生产指令',MATERIAL_REQUESTS:'领料申请单',MATERIAL_ISSUES:'出库单',CHARGES:'投料记录',CLEARANCE:'清场记录',INSPECTION_REQUESTS:'请验单',SAMPLING_RECORDS:'取样记录',INSPECTION_RECORDS:'检验记录',INSPECTION_REPORTS:'检验报告',QUALITY_INVESTIGATIONS:'偏差 / OOS / CAPA',MATERIAL_BALANCE:'物料平衡记录',QA_DECISIONS:'QA 放行记录',ATTACHMENTS:'受控附件'}
export const bookStates:Record<string,string>={MISSING:'缺少必需记录',NOT_APPLICABLE:'本批不适用',DRAFT:'草稿',PENDING_REVIEW:'待复核',COMPLETE:'已完成',ACCESS_DENIED:'无来源读取权限'}
export function bookEntryGroup(entry:BookEntry){return entry.flowName||entry.chapter||'未分组流程'}
export function mayFormal(book:BatchBook){return !book.legacy&&['QA_RELEASED','REJECTED'].includes(book.status)&&book.entries.every(e=>!e.definition.required||e.state==='COMPLETE')}
export function newBookEntry(index:number,flowType:BookFlowType='PROCESS',flowCode='PROCESS',flowName='生产工序',kind:BookKind='FORM'):BookEntry{
 return {code:`ENTRY_${index}`,flowType,flowCode,flowName,title:kind==='FORM'?'新工序表单记录':bookKinds[kind],kind,order:index,required:true,minCount:1,scope:kind==='FORM'?'OPERATION':'BATCH',operationCode:null,formCode:null,printTemplateVersionId:null,fields:[],attachmentIds:[],archiveStage:'PRODUCTION_REVIEW',applicability:'始终适用'}
}
