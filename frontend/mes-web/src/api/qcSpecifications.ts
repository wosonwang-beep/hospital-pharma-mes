import {api,idempotencyKey,type Page} from './http'

/** Basic-library status of a QC quality standard. There is no draft/approved/retired cycle. */
export type QcStatus='ACTIVE'|'INACTIVE'
export const qcStatuses:Record<QcStatus,string>={ACTIVE:'启用',INACTIVE:'停用'}
export type QcSubjectType='MATERIAL'|'PRODUCT'
/** Fixed system codes of the QC_STANDARD_BASIS dictionary, shown when the dictionary is unavailable. */
export const qcBasisLabels:Record<string,string>={CHINESE_PHARMACOPOEIA:'《中国药典》',MINISTRY_STANDARD:'局（部）颁标准',DRUG_REGISTRATION_STANDARD:'药品注册标准',NATIONAL_DRUG_STANDARD:'国家药品标准',ENTERPRISE_INTERNAL:'企业内控标准',CUSTOMER_SPECIFICATION:'客户标准',OTHER:'其他依据'}
export interface QcItem {specificationItemId?:string;itemCode:string;itemName:string;required:boolean;resultType:'NUMERIC'|'TEXT';lowerLimit:string|null;upperLimit:string|null;unitId:string|null;textAcceptanceCriteria:string|null;methodCode:string;methodVersion:string}
export interface QcScope {scopeId:string;subjectType:QcSubjectType;subjectId:string;subjectCode:string|null;subjectName:string|null}
export interface QcRevision {revisionNo:string;revisionType:'CREATE'|'UPDATE'|'DELETE';revisionReason:string;revisedBy:string;revisedAt:string}
export interface QcMeta {id:string;orgId:string;createdBy:string;createdAt:string;updatedBy:string;updatedAt:string;versionNo:number}
export interface QcSpecification extends QcMeta {specificationCode:string;specificationName:string;basisType:string;basisReference:string;effectiveDate:string|null;remark:string|null;status:QcStatus;scopes:QcScope[]}
export interface QcDetail extends QcSpecification {items:QcItem[];revisions:QcRevision[]}
export function blankQcItem():QcItem{return {itemCode:'',itemName:'',required:true,resultType:'NUMERIC',lowerLimit:null,upperLimit:null,unitId:null,textAcceptanceCriteria:null,methodCode:'',methodVersion:''}}
export function qcBasisLabel(code:string|null|undefined){return code?(qcBasisLabels[code]??code):'—'}
/** "3 个物料 / 1 个产品" — the applicability is always visible without opening the record. */
export function qcScopeSummary(scopes:QcScope[]|undefined){const materials=scopes?.filter(s=>s.subjectType==='MATERIAL').length??0,products=scopes?.filter(s=>s.subjectType==='PRODUCT').length??0;return materials||products?`${materials} 个物料 / ${products} 个产品`:'未维护适用范围'}
export function qcScopeNames(scopes:QcScope[]|undefined,type:QcSubjectType,limit=3){const picked=(scopes??[]).filter(s=>s.subjectType===type).map(s=>s.subjectName??`#${s.subjectId}`);return picked.length<=limit?picked.join('、'):`${picked.slice(0,limit).join('、')} 等 ${picked.length} 项`}
function text(value:string,max:number){const result=value.trim();if(!result||result.length>max)throw Error(`请输入不超过 ${max} 字的必填内容`);return result}
function decimal(value:string|null){if(value===null||value==='')return null;if(!/^-?[0-9]{1,12}(\.[0-9]{1,6})?$/.test(value))throw Error('数值最多支持 12 位整数和 6 位小数');return value}
export function qcItemPayload(items:QcItem[]){if(!items.length)throw Error('至少需要一个检验项目');const codes=new Set<string>();return items.map(item=>{const itemCode=text(item.itemCode,100);if(codes.has(itemCode.toUpperCase()))throw Error('项目编码不能重复');codes.add(itemCode.toUpperCase());const lowerLimit=decimal(item.lowerLimit),upperLimit=decimal(item.upperLimit),unitId=item.unitId||null,textAcceptanceCriteria=item.textAcceptanceCriteria?.trim()||null;if(item.resultType==='NUMERIC'){if((lowerLimit===null&&upperLimit===null)||!unitId||textAcceptanceCriteria!==null)throw Error('数值项目必须填写至少一个限值及单位，且不能包含文本标准')}else if(lowerLimit!==null||upperLimit!==null||unitId!==null||!textAcceptanceCriteria)throw Error('文本项目只能填写文本合格标准');return {itemCode,itemName:text(item.itemName,200),required:item.required,resultType:item.resultType,lowerLimit,upperLimit,unitId,textAcceptanceCriteria,methodCode:text(item.methodCode,100),methodVersion:text(item.methodVersion,50)}})}
export function qcCreatePayload(input:{specificationCode:string;specificationName:string;basisType:string;basisReference:string;effectiveDate:string;remark:string;materialIds:string[];productIds:string[];items:QcItem[];reason:string}){
 const basisType=input.basisType.trim();if(!basisType)throw Error('请选择标准的依据')
 if(!input.materialIds.length&&!input.productIds.length)throw Error('请至少选择一个适用物料或适用产品')
 return {specificationCode:text(input.specificationCode,100),specificationName:text(input.specificationName,200),basisType,basisReference:text(input.basisReference,300),effectiveDate:input.effectiveDate||null,remark:input.remark.trim()||null,materialIds:input.materialIds,productIds:input.productIds,items:qcItemPayload(input.items),reason:text(input.reason,500)}
}
/** A save keeps one idempotency key per semantic body and sends the current record token back. */
export function qcCommandHeaders(){let previous='',key=idempotencyKey();return {forRequest(url:string,body:object,version?:number):Record<string,string>{const fingerprint=JSON.stringify({url,body,version});if(previous!==fingerprint){previous=fingerprint;key=idempotencyKey()}return {'Idempotency-Key':key,...(version===undefined?{}:{'If-Match':`"${version}"`})}},complete(){previous=''}}}
export const qcApi={
 list:(params:Record<string,unknown>)=>api<Page<QcSpecification>>({url:'/quality/specifications',params}),
 detail:(id:string)=>api<QcDetail>({url:`/quality/specifications/${id}`}),
 create:(body:object,headers:Record<string,string>)=>api<QcDetail>({url:'/quality/specifications',method:'POST',data:body,headers}),
 edit:(id:string,body:object,headers:Record<string,string>)=>api<QcDetail>({url:`/quality/specifications/${id}`,method:'PUT',data:body,headers}),
 replaceScope:(id:string,body:object,headers:Record<string,string>)=>api<QcDetail>({url:`/quality/specifications/${id}/scope`,method:'PUT',data:body,headers}),
 replaceItems:(id:string,body:object,headers:Record<string,string>)=>api<QcDetail>({url:`/quality/specifications/${id}/items`,method:'PUT',data:body,headers}),
 remove:(id:string,body:object,headers:Record<string,string>)=>api<{id:string;deleted:boolean}>({url:`/quality/specifications/${id}`,method:'DELETE',data:body,headers})
}
