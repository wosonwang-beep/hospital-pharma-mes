import type {Page} from '../../api/http'
/** Never present a partial paginated read as a complete business statistic. */
export function completeRows<T>(page:Page<T>):T[]|null {return page.items.length===page.total?page.items:null}
export function inProductionCount(rows:Record<string,unknown>[]|null) {return rows?.filter(row=>row.status==='IN_PROGRESS').length??null}
export function pendingTestSampleCount(rows:Record<string,unknown>[]|null) {
 return rows?.filter(row=>['TEST_SAMPLE','RETEST_SAMPLE'].includes(String(row.sampleType))&&['CREATED','COLLECTED','RECEIVED'].includes(String(row.status))).length??null
}
export interface InventoryStatusSummary {available:number;frozen:number;blocked:number;other:number;total:number}
export function inventoryStatusSummary(rows:Record<string,unknown>[]|null):InventoryStatusSummary|null {
 if(rows===null)return null
 const summary:InventoryStatusSummary={available:0,frozen:0,blocked:0,other:0,total:rows.length}
 for(const row of rows){const status=String(row.inventoryStatus??'');if(status==='AVAILABLE')summary.available++;else if(status==='FROZEN')summary.frozen++;else if(status==='BLOCKED')summary.blocked++;else summary.other++}
 return summary
}
export function abnormalInventoryCount(rows:Record<string,unknown>[]|null){const summary=inventoryStatusSummary(rows);return summary===null?null:summary.frozen+summary.blocked+summary.other}
export function businessDay(date:Date) {return new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Shanghai',year:'numeric',month:'2-digit',day:'2-digit'}).format(date)}
/** Undated source records cannot be silently interpreted as zero activity. */
export function datedTrend(rows:Record<string,unknown>[]|null,now:Date) {
 if(rows===null||rows.some(row=>typeof row.createdAt!=='string'||Number.isNaN(Date.parse(row.createdAt))))return null
 return dailyCounts(rows,now)
}
export function dailyCounts(rows:Record<string,unknown>[],now:Date) {
 const day=new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Shanghai',year:'numeric',month:'2-digit',day:'2-digit'})
 return Array.from({length:7},(_,index)=>{
  const date=new Date(now.getTime()-(6-index)*86400000),key=day.format(date)
  return {label:key.slice(5),count:rows.filter(row=>typeof row.createdAt==='string'&&!Number.isNaN(Date.parse(row.createdAt))&&day.format(new Date(row.createdAt))===key).length}
 })
}
