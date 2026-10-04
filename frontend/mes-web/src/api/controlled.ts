import {ref,nextTick} from 'vue'
import axios from 'axios'
import {api,errorMessage,idempotencyKey} from './http'
export function useControlledRequest(){
 const error=ref(''),conflict=ref(false),busy=ref(false),fields=ref<{field?:string;path?:string;message?:string}[]>([]);let previous='',key=idempotencyKey()
 function clear(){error.value='';conflict.value=false;fields.value=[]}
 async function failure(e:unknown){error.value=e instanceof Error&&!axios.isAxiosError(e)?e.message:errorMessage(e);conflict.value=axios.isAxiosError(e)&&e.response?.status===409;fields.value=axios.isAxiosError(e)?e.response?.data?.fieldErrors??[]:[];await nextTick();const name=fields.value[0]?.field??fields.value[0]?.path;if(name)document.querySelector<HTMLElement>(`[name="${CSS.escape(name)}"]`)?.focus()}
 async function mutate<T>(url:string,method:string,body:unknown,version?:number):Promise<T>{const fingerprint=JSON.stringify({url,method,body,version});if(previous!==fingerprint){previous=fingerprint;key=idempotencyKey()}const result=await api<T>({url,method,data:body,headers:{'Idempotency-Key':key,...(version===undefined?{}:{'If-Match':`"${version}"`})}});previous='';return result}
 return {error,conflict,busy,fields,clear,failure,mutate}
}
