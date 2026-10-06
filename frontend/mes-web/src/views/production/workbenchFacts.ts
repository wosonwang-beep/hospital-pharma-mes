type Row=Record<string,unknown>

// A display comparison only. The server still owns completion/quality decisions
// and exact unit conversion; raw observations are never changed here.
function decimal(value:unknown){
 const text=String(value??'')
 if(!/^-?\d+(?:\.\d{1,8})?$/.test(text))return null
 const negative=text.startsWith('-'),[whole,fraction='']=text.replace(/^-/, '').split('.')
 return BigInt(whole!)*100000000n*(negative?-1n:1n)+BigInt(fraction.padEnd(8,'0'))*(negative?-1n:1n)
}
export function parameterRange(row:Row,definition?:Row):{text:string;color:string}{
 if(!definition)return {text:'待核对标准',color:'default'}
 if(definition.lowerLimit==null&&definition.upperLimit==null)return {text:'未设范围',color:'default'}
 if(!definition.unitId||String(row.unitId)!==String(definition.unitId))return {text:'待单位换算',color:'default'}
 const actual=decimal(row.rawValue),lower=definition.lowerLimit==null?null:decimal(definition.lowerLimit),upper=definition.upperLimit==null?null:decimal(definition.upperLimit)
 if(actual==null||definition.lowerLimit!=null&&lower==null||definition.upperLimit!=null&&upper==null)return {text:'待核对数值',color:'default'}
 return lower!=null&&actual<lower||upper!=null&&actual>upper?{text:'超范围',color:'red'}:{text:'范围内',color:'green'}
}
export function deviationInitial(query:Record<string,unknown>):Row{
 const values:Row={investigationScope:'PRODUCTION',investigationKind:'DEVIATION'}
 for(const field of ['mainBatchId','operationExecutionId'])if(typeof query[field]==='string'&&/^[1-9]\d*$/.test(query[field] as string))values[field]=query[field]
 return values
}
