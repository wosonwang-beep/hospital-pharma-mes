import {api,idempotencyKey,type Page} from './http'
export type QcState='DRAFT'|'APPROVED'|'RETIRED'
export const qcStates:Record<QcState,string>={DRAFT:'草稿',APPROVED:'已批准',RETIRED:'已退役'}
export interface QcItem {specificationItemId?:string;itemCode:string;itemName:string;required:boolean;resultType:'NUMERIC'|'TEXT';lowerLimit:string|null;upperLimit:string|null;unitId:string|null;textAcceptanceCriteria:string|null;methodCode:string;methodVersion:string}
export interface QcMeta {id:string;orgId:string;createdBy:string;createdAt:string;updatedBy:string;updatedAt:string;versionNo:number}
export interface QcSpecification extends QcMeta {materialId:string;materialCode:string;materialName:string;specificationCode:string;specificationName:string}
export interface QcVersionSummary extends QcMeta {specificationId:string;versionNoBusiness:number;status:QcState;contentHash:string|null;approvedBy:string|null;approvedAt:string|null;approvalSignatureId:string|null;approvalReason:string|null;retiredBy:string|null;retiredAt:string|null;retirementSignatureId:string|null;retirementReason:string|null}
export interface QcDetail extends QcSpecification {versions:QcVersionSummary[]}
export interface QcVersion extends QcVersionSummary {specificationCode:string;specificationName:string;materialId:string;materialCode:string;materialName:string;items:QcItem[]}
export function blankQcItem():QcItem{return {itemCode:'',itemName:'',required:true,resultType:'NUMERIC',lowerLimit:null,upperLimit:null,unitId:null,textAcceptanceCriteria:null,methodCode:'',methodVersion:''}}
function text(value:string,max:number){const result=value.trim();if(!result||result.length>max)throw Error(`请输入不超过 ${max} 字的必填内容`);return result}
function decimal(value:string|null){if(value===null||value==='')return null;if(!/^-?[0-9]{1,12}(\.[0-9]{1,6})?$/.test(value))throw Error('数值最多支持 12 位整数和 6 位小数');return value}
export function qcItemPayload(items:QcItem[]){if(!items.length)throw Error('至少需要一个检验项目');const codes=new Set<string>();return items.map(item=>{const itemCode=text(item.itemCode,100);if(codes.has(itemCode.toUpperCase()))throw Error('项目编码不能重复');codes.add(itemCode.toUpperCase());const lowerLimit=decimal(item.lowerLimit),upperLimit=decimal(item.upperLimit),unitId=item.unitId||null,textAcceptanceCriteria=item.textAcceptanceCriteria?.trim()||null;if(item.resultType==='NUMERIC'){if((lowerLimit===null&&upperLimit===null)||!unitId||textAcceptanceCriteria!==null)throw Error('数值项目必须填写至少一个限值及单位，且不能包含文本标准')}else if(lowerLimit!==null||upperLimit!==null||unitId!==null||!textAcceptanceCriteria)throw Error('文本项目只能填写文本合格标准');return {itemCode,itemName:text(item.itemName,200),required:item.required,resultType:item.resultType,lowerLimit,upperLimit,unitId,textAcceptanceCriteria,methodCode:text(item.methodCode,100),methodVersion:text(item.methodVersion,50)}})}
/** Retain only the semantic request fingerprint; fresh reauth tokens do not change a retry key. */
export function qcCommandHeaders(){let previous='',key=idempotencyKey();return {forRequest(url:string,body:object,version?:number):Record<string,string>{const {reauthToken:_,...safe}=body as Record<string,unknown>;const fingerprint=JSON.stringify({url,body:safe,version});if(previous!==fingerprint){previous=fingerprint;key=idempotencyKey()}return {'Idempotency-Key':key,...(version===undefined?{}:{'If-Match':`"${version}"`})}},complete(){previous=''}}}
export const qcApi={
 list:(params:Record<string,unknown>)=>api<Page<QcSpecification>>({url:'/quality/specifications',params}),
 detail:(id:string)=>api<QcDetail>({url:`/quality/specifications/${id}`}),
 version:(id:string)=>api<QcVersion>({url:`/quality/specification-versions/${id}`})
}
