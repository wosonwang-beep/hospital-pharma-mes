import type {InspectionPage,InspectionQuery} from './inspectionListModel'
export interface InspectionApiPage extends InspectionPage {inspectors:{value:string;label:string}[]}
export function createInspectionLoader(transport:(query:InspectionQuery)=>Promise<InspectionApiPage>,can:(permission:string)=>boolean){
 return async(query:InspectionQuery):Promise<InspectionApiPage>=>{
  const data=await transport(query)
  const permissions:Record<string,string>={view:'qms:test:view',execute:'qms:test:execute',review:'qms:test:review'}
  return {...data,items:data.items.map(row=>({...row,allowedActions:(row.allowedActions??[]).filter(action=>!!permissions[action]&&can(permissions[action]!))}))}
 }
}
