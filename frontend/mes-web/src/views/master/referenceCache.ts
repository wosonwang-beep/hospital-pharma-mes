const values = new Map<string,string>()
const pending = new Map<string,Promise<string>>()
let generation = 0

/** Invalidate all read-only business labels at every session boundary. */
export function clearReferenceCache(){
  generation += 1
  values.clear()
  pending.clear()
}

export async function cachedReference(key:string,loader:()=>Promise<string>):Promise<string>{
  const value=values.get(key)
  if(value!==undefined)return value
  const existing=pending.get(key)
  if(existing)return existing
  const current=generation
  const request=loader().then(result=>{
    // An in-flight response from a previous identity must never repopulate the cache.
    if(current===generation&&result)values.set(key,result)
    return result
  }).finally(()=>{
    if(pending.get(key)===request)pending.delete(key)
  })
  pending.set(key,request)
  return request
}
