import type {IncomingRow} from './incomingModel'
import {rows} from './incomingDetailModel'
export type ReferenceMap=Record<string,{name:string;row:IncomingRow;route?:string}>
export interface InspectionIdentityFact {label:string;value:string;source:string}
const present=(v:unknown)=>v!==undefined&&v!==null&&v!==''
const shown=(v:unknown)=>present(v)?String(v):'未记录'
const decimal=(v:unknown):bigint=>{const s=String(v);if(!/^\d+(\.\d{1,8})?$/.test(s))throw Error('Invalid quantity');const [a,b='']=s.split('.');return BigInt(a!)*100000000n+BigInt(b.padEnd(8,'0'))}
const formatted=(v:bigint)=>{const whole=v/100000000n,rest=String(v%100000000n).padStart(8,'0').replace(/0+$/,'');return String(whole)+(rest?'.'+rest:'')}
/** Read-only provenance: receipt batch amount is never replaced by requested/test-sample amount. */
export function inspectionIdentityFacts(record:IncomingRow,request:IncomingRow|undefined,lot:IncomingRow|undefined,refs:ReferenceMap):InspectionIdentityFact[]{
 const sample=refs[`sampleId:${record.sampleId}`]?.row
 const sampling=refs[`samplingTaskId:${sample?.samplingTaskId}`]?.row
 const receipt=refs[`receiptId:${request?.receiptId}`]?.row
 const receiptItemId=request?.receiptItemId??lot?.receiptItemId
 const item=present(receiptItemId)?rows(receipt?.items).find(i=>String(i.id)===String(receiptItemId)):undefined
 const unit=(id:unknown)=>refs[`unitId:${id}`]?.name??refs[`requestedUnitId:${id}`]?.name??refs[`plannedUnitId:${id}`]?.name??(present(id)?'单位待读取':'未记录')
 const amount=(q:unknown,u:unknown)=>present(q)?`${shown(q)} ${unit(u)}`:'未记录'
 let actual='未取样'
 const details=rows(sampling?.details)
 if(!sampling)actual='未记录'
 else if(details.length){
  try{const groups=new Map<string,bigint>();for(const d of details){if(!present(d.unitId))throw Error('Missing unit');const key=String(d.unitId);groups.set(key,(groups.get(key)??0n)+decimal(d.sampleQuantity))}actual=[...groups].map(([id,q])=>`${formatted(q)} ${unit(id)}`).join(' / ')}
  catch{actual='数量信息不完整，请核对取样记录'}
 }
 return [
  {label:'物料批量',value:amount(item?.receivedQty,item?.unitId),source:'wms_material_receipt_item.received_qty / unit_id'},
  {label:'计划取样量',value:amount(sampling?.plannedSamplingQuantity,sampling?.plannedUnitId),source:'qms_sampling_task.planned_sampling_quantity / planned_unit_id'},
  {label:'实际总取样量',value:actual,source:'qms_sampling_detail.sample_quantity grouped by original unit_id'},
  {label:'本任务检验样量',value:amount(sample?.quantity,sample?.unitId),source:'qms_sample.quantity / unit_id, not total sampling amount'},
  {label:'请验日期',value:shown(request?.requestedDate),source:'qms_inspection_request.requested_date'},
  {label:'取样日期（UTC）',value:present(sample?.sampledAt)?String(sample!.sampledAt).slice(0,10):'未记录',source:'qms_sample.sampled_at'},
  {label:'检验日期（UTC）',value:present(record.startedAt)?String(record.startedAt).slice(0,10):'未开始',source:'qms_inspection_task.started_at'},
  {label:'生产批号',value:shown(lot?.manufacturerLotNo),source:'wms receipt manufacturer lot read projection'}
 ]
}
