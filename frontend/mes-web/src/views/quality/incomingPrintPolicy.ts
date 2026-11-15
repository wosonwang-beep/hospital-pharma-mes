export function samplingPrintVisible(resource:string,can:(permission:string)=>boolean){
 return resource==='sampling-tasks'&&can('print:document:generate')&&can('qms:sampling:view')
}
