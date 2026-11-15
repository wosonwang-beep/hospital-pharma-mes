export interface InspectionFilters {keyword:string;status:string;assignedTo:string;requestType:string}
export interface InspectionQuery extends InspectionFilters {page:number;size:number}
export interface InspectionRow {
 id:string;sampleName:string|null;lotNo:string|null;sampleNo:string|null;inspectionTaskNo:string;
 requestType:string;status:string;assignedTo:string|null;assignedName:string|null;
 requestedDate:string|null;sampledAt:string|null;overallResult:'PASS'|'FAIL'|null;
 reportNo:string|null;reportId:string|null;allowedActions:string[]
}
export interface InspectionPage {items:InspectionRow[];total:number;page:number;size:number}
export interface ListState {draft:InspectionFilters;applied:InspectionFilters;page:number;size:number;total:number;rows:InspectionRow[];busy:boolean;error:string}
export function parseInspectionQuery(params:URLSearchParams):InspectionQuery{
 const integer=(key:string,fallback:number,min:number,max:number)=>{const value=params.get(key);if(value===null||!/^\d+$/.test(value))return fallback;const n=Number(value);return Number.isSafeInteger(n)&&n>=min&&n<=max?n:fallback}
 return {keyword:(params.get('keyword')??'').trim().slice(0,200),status:params.get('status')??'',assignedTo:params.get('assignedTo')??'',requestType:params.get('requestType')??'',page:integer('page',0,0,1000000),size:integer('size',20,1,100)}
}
const defaults=():InspectionFilters=>({keyword:'',status:'',assignedTo:'',requestType:''})
export function createListState():ListState{return {draft:defaults(),applied:defaults(),page:1,size:20,total:0,rows:[],busy:false,error:''}}
export function permits(row:Pick<InspectionRow,'allowedActions'>,action:string){return Array.isArray(row.allowedActions)&&row.allowedActions.includes(action)}
export function createListController(state:ListState,fetcher:(query:InspectionQuery)=>Promise<InspectionPage>){
 let generation=0
 async function reload(){
  const ticket=++generation
  state.busy=true;state.error='';state.rows=[]
  try{
   const data=await fetcher({...state.applied,page:state.page-1,size:state.size})
   if(ticket!==generation)return
   state.rows=data.items;state.total=data.total
  }catch(error){if(ticket===generation){state.rows=[];state.total=0;state.error=error instanceof Error?error.message:'加载失败，请重试'}}
  finally{if(ticket===generation)state.busy=false}
 }
 async function query(){state.applied={...state.draft,keyword:state.draft.keyword.trim()};state.page=1;await reload()}
 async function reset(){Object.assign(state.draft,defaults());await query()}
 async function changePage(page:number){state.page=Math.max(1,Math.floor(page));await reload()}
 return {query,reset,changePage,reload}
}
