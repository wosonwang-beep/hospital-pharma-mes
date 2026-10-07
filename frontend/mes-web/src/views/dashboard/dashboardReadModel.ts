import type {Page} from '../../api/http'
/** Never present a partial paginated read as a complete business statistic. */
export function completeRows<T>(page:Page<T>):T[]|null {return page.items.length===page.total?page.items:null}
export function dailyCounts(rows:Record<string,unknown>[],now:Date) {
 const day=new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Shanghai',year:'numeric',month:'2-digit',day:'2-digit'})
 return Array.from({length:7},(_,index)=>{
  const date=new Date(now.getTime()-(6-index)*86400000),key=day.format(date)
  return {label:key.slice(5),count:rows.filter(row=>typeof row.createdAt==='string'&&!Number.isNaN(Date.parse(row.createdAt))&&day.format(new Date(row.createdAt))===key).length}
 })
}
