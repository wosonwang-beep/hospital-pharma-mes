import type {DictionaryKind,DictionaryStatus,DictionaryStructure} from './dictionaryModel'
export interface DictionaryImportRow {code:string;name:string;kind:DictionaryKind;structure:DictionaryStructure;description:string|null;sortNo:number;status:DictionaryStatus}
export interface DictionaryImportPreviewRow {line:number;code:string;name:string;result:'PASS'|'CONFLICT';message:string}
export interface DictionaryImportPreview {items:DictionaryImportPreviewRow[];accepted:number;rejected:number}
export const csvHeader=['字典CODE','字典名称','字典类型','字典结构','描述','排序','状态']
export const csvTemplate='\uFEFF'+csvHeader.join(',')+'\r\n'
/** Delimited-text parser: quoted commas, escaped quotes, and quoted CRLF are supported. */
export function parseCsv(source:string):string[][]{
 const data=source.replace(/^\uFEFF/,'')
 const rows:string[][]=[];let row:string[]=[];let field='',quoted=false,closed=false
 for(let i=0;i<data.length;i++){
  const c=data[i]
  if(quoted){if(c==='"'){if(data[i+1]==='"'){field+='"';i++}else{quoted=false;closed=true}}else field+=c;continue}
  if(c==='"'&&field===''){quoted=true;continue}
  if(closed&&c!=='\r'&&c!=='\n'&&c!==',')throw new Error('引号后存在无效字符')
  if(c===','||c==='\r'||c==='\n'){
    row.push(field);field='';closed=false
    if(c!==','){if(row.some(v=>v.trim()))rows.push(row);row=[];if(c==='\r'&&data[i+1]==='\n')i++}
    continue
  }
  if(c==='"')throw new Error('引号必须置于单元格开头')
  if(closed)throw new Error('引号后存在无效字符')
  field+=c
 }
 if(quoted)throw new Error('CSV 引号没有正确闭合')
 if(field||row.length){row.push(field);if(row.some(v=>v.trim()))rows.push(row)}
 return rows
}
export function parseDictionaryImport(source:string):DictionaryImportRow[]{
 const rows=parseCsv(source)
 if(rows.length<2)throw new Error('CSV 必须包含表头和至少一条字典记录')
 if(rows[0]!.length!==csvHeader.length||rows[0]!.some((v,i)=>v.trim()!==csvHeader[i]))throw new Error('导入模板表头不匹配，请先下载模板')
 if(rows.length>201)throw new Error('单次最多导入 200 个字典类型')
 return rows.slice(1).map((r,index)=>{
  if(r.length!==7)throw new Error('第 '+(index+2)+' 行字段数量不是 7 列')
  const [code='',name='',kind='',structure='',description='',sortText='',status='']=r.map(x=>x.trim())
  if(!/^\d{1,6}$/.test(sortText))throw new Error('第 '+(index+2)+' 行排序必须为 0–999999 的整数')
  return {code,name,kind:kind as DictionaryKind,structure:structure as DictionaryStructure,
   description:description||null,sortNo:Number(sortText),status:status as DictionaryStatus}
 })
}
