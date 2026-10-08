export type BookKind='FORM'|'PROCESS_INSTRUCTIONS'|'PRODUCTION_ORDER'|'MATERIAL_ISSUES'|'CHARGES'|'CLEARANCE'|'INSPECTION_REPORTS'|'ATTACHMENTS'
export interface BookMapping{productId:string;packageVersionId:string;ebrTemplateVersionId:string}
export interface BookEntry{code:string;chapter:string;title:string;kind:BookKind;order:number;required:boolean;minCount:number;scope:'BATCH'|'OPERATION';operationCode:string|null;formCode:string|null;printTemplateVersionId:string|null;fields:string[];attachmentIds:string[]}
export interface BookDefinition{varietyCode:string;varietyName:string;mappings:BookMapping[];entries:BookEntry[]}
export interface BookTemplate{id:string;templateCode:string;revision:number;status:string;versionNo:number;definition:BookDefinition;definitionHash:string;createdBy:string}
export interface BookDocument{sourceType:string;sourceId:string;sourceVersion:string;sourceHash:string;title:string;status:string;columns:string[];rows:string[][];formId?:string;executionUnitId?:string;url?:string;signatureIds?:string[]}
export interface BookItem{definition:BookEntry;state:string;notice?:string;documents:BookDocument[]}
export interface BatchBook{mainBatchId:string;versionNo:number;status:string;batchNo:string;productName:string;specification:string;plannedQty:string;unitId:string;unitName:string;varietyName?:string;legacy:boolean;notice?:string;entries:BookItem[];book?:{templateVersionId:string;templateRevision:number;definitionHash:string}}
export interface BookPdf{id:string;pdf_hash:string;source_hash:string;archive_kind:'REVIEW_COPY'|'FINAL';created_at:string;template_version_id:string}
export const bookKinds:Record<BookKind,string>={FORM:'eBR过程表单',PROCESS_INSTRUCTIONS:'冻结工艺指令',PRODUCTION_ORDER:'生产指令',MATERIAL_ISSUES:'领料/出库单',CHARGES:'实际投料记录',CLEARANCE:'清场记录',INSPECTION_REPORTS:'成品检验报告',ATTACHMENTS:'受控附件'}
export const bookStates:Record<string,string>={MISSING:'缺少必需记录',NOT_APPLICABLE:'本批不适用',DRAFT:'草稿',PENDING_REVIEW:'待复核',COMPLETE:'已完成',ACCESS_DENIED:'无来源读取权限'}
export function mayFormal(book:BatchBook){return !book.legacy&&['QA_RELEASED','REJECTED'].includes(book.status)&&book.entries.every(e=>!e.definition.required||e.state==='COMPLETE')}
export function newBookEntry(index:number):BookEntry{return {code:`ENTRY_${index}`,chapter:'过程记录',title:'新目录项',kind:'FORM',order:index,required:true,minCount:1,scope:'OPERATION',operationCode:null,formCode:null,printTemplateVersionId:null,fields:[],attachmentIds:[]}}
