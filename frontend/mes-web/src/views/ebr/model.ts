import type {EbrDefinition,EbrField,EbrInput,EbrIssue} from './types'
export const states:Record<string,string>={DRAFT:'草稿',SUBMITTED:'已提交',APPROVED:'已批准',EFFECTIVE:'已生效',WITHDRAWN:'已撤回'}
export const fieldTypes=['NUMBER','TEXT','TEXTAREA','ENUM','MULTI_ENUM','BOOLEAN','DATE','TIME','DATETIME','BARCODE','MATERIAL_LOT','CONTAINER','EQUIPMENT','PERSON','ATTACHMENT','IMAGE','TIMER','CALCULATED','INSTRUMENT_VALUE','SIGNATURE_PLACEHOLDER'] as const
export const fieldLabels:Record<string,string>={NUMBER:'数值',TEXT:'文本',TEXTAREA:'多行文本',ENUM:'单选',MULTI_ENUM:'多选',BOOLEAN:'布尔',DATE:'日期',TIME:'时间',DATETIME:'日期时间',BARCODE:'条码',MATERIAL_LOT:'物料批',CONTAINER:'容器',EQUIPMENT:'设备',PERSON:'人员',ATTACHMENT:'附件',IMAGE:'图片',TIMER:'计时器',CALCULATED:'计算值',INSTRUMENT_VALUE:'仪器值',SIGNATURE_PLACEHOLDER:'签名占位'}
export const triggers=['ON_CHANGE','ON_SAVE','ON_SUBMIT','ON_OPERATION_COMPLETE','ON_BATCH_CLOSE']
export const emptyDefinition=():EbrDefinition=>({sections:[],forms:[],rules:[],signatureRules:[],reviewRules:[]})
export const clone=<T>(value:T):T=>JSON.parse(JSON.stringify(value))
export function duplicateFieldIssues(definition:EbrDefinition):EbrIssue[]{
 const seen=new Set<string>(),issues:EbrIssue[]=[]
 definition.forms.forEach((form,fi)=>form.fields.forEach((field,vi)=>{
  if(seen.has(field.fieldCode))issues.push({path:`forms[${fi}].fields[${vi}].fieldCode`,code:'DUPLICATE_CODE',message:`重复字段编码：${field.fieldCode}`})
  seen.add(field.fieldCode)
 }))
 return issues
}
export function newField(code:string,type:EbrField['fieldType']):EbrField{return {fieldCode:code,groupCode:null,label:fieldLabels[type]!,fieldType:type,sourceType:type==='CALCULATED'?'DERIVED':type==='INSTRUMENT_VALUE'?'INSTRUMENT':'MANUAL',dataType:type==='NUMBER'?'DECIMAL':'STRING',unitId:null,precisionScale:null,requiredFlag:false,readonlyFlag:false,defaultExpr:null,placeholder:null,helpText:null,sequenceNo:1,validationJson:null,options:[]}}
export function moveItem<T extends {sequenceNo:number}>(rows:T[],from:number,to:number){if(from<0||to<0||from>=rows.length||to>=rows.length)return;rows.splice(to,0,rows.splice(from,1)[0]!);rows.forEach((r,i)=>r.sequenceNo=i+1)}
export interface SimulationRow {fieldCode:string;type:'string'|'number'|'boolean';value:string}
export function simulationInputs(rows:SimulationRow[]):EbrInput[]{return rows.filter(r=>r.value!=='').map(r=>({fieldCode:r.fieldCode,values:r.value.split('\n').map(v=>{if(r.type==='string')return v;if(r.type==='number'){if(!v.trim()||!Number.isFinite(Number(v)))throw Error(`${r.fieldCode}：请输入有限数值`);return Number(v)}if(!['true','false'].includes(v))throw Error(`${r.fieldCode}：布尔值仅支持 true / false`);return v==='true'})}))}
