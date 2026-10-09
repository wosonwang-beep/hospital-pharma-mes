import {api} from '../../api/http'
export interface Department {id:string;code:string;name:string;organizationId:string;organizationName:string;
 parentId:string|null;parentName:string|null;leaderUserId:string|null;leaderName:string|null;phone:string|null;
 description:string|null;sortNo:number;status:'ACTIVE'|'INACTIVE';version:number;memberCount:number}
export interface DepartmentForm {code:string;name:string;organizationId:string;parentId:string|null;leaderUserId:string|null;
 phone:string|null;description:string|null;sortNo:number;status:'ACTIVE'|'INACTIVE'}
export interface DepartmentOrg {id:string;name:string;code:string}
export interface DepartmentPerson {id:string;name:string;username:string}
export interface DepartmentAssignment {userId:string;departmentId:string|null;departmentName:string|null;version:number}
export const departmentStatus:Record<Department['status'],string>={ACTIVE:'启用',INACTIVE:'停用'}
export function departmentTree(all:Department[],selected:string|null=null){
 const visible=all.filter(x=>x.id!==selected)
 const make=(parent:string|null,seen:Set<string>):any[]=>visible.filter(x=>x.parentId===parent&&!seen.has(x.id)).map(x=>{
  const next=new Set(seen);next.add(x.id)
  return {key:x.id,title:x.name,children:make(x.id,next)}
 })
 return make(null,new Set())
}
export async function getUserDepartment(id:string){return api<DepartmentAssignment>({url:`/users/${id}/department`})}
