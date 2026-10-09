import {api} from '../../api/http'
export type DictionaryKind='SYSTEM'|'BUSINESS'
export type DictionaryStructure='FLAT'|'TREE'
export type DictionaryStatus='ACTIVE'|'INACTIVE'
export interface DictionaryType {id:string;code:string;name:string;kind:DictionaryKind;structure:DictionaryStructure;description:string|null;sortNo:number;status:DictionaryStatus;version:number;itemCount:number;createdAt:string;updatedAt:string}
export interface DictionaryItem {id:string;dictTypeId:string;parentId:string|null;code:string;label:string;description:string|null;sortNo:number;status:DictionaryStatus;isDefault:boolean;version:number}
export interface DictionaryOption {value:string;label:string;parentId:string|null;disabled:boolean}
export interface DictionaryTypePayload {code:string;name:string;kind:DictionaryKind;structure:DictionaryStructure;description:string|null;sortNo:number;status:DictionaryStatus}
export interface DictionaryItemPayload {code:string;label:string;description:string|null;parentId:string|null;sortNo:number;status:DictionaryStatus;isDefault:boolean}
export const statusLabel:Record<DictionaryStatus,string>={ACTIVE:'启用',INACTIVE:'停用'}
export const kindLabel:Record<DictionaryKind,string>={SYSTEM:'系统级',BUSINESS:'业务级'}
export const structureLabel:Record<DictionaryStructure,string>={FLAT:'平级结构',TREE:'树形结构'}
export async function dictionaryOptions(code:string,selectedValue?:string|null):Promise<DictionaryOption[]>{
 return api<DictionaryOption[]>({url:'/dictionary-options/'+encodeURIComponent(code),params:{selectedValue:selectedValue||undefined}})
}
