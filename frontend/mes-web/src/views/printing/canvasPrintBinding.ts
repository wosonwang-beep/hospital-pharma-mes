import {ControlType,ElementType,RowFlex,type IEditorData,type IEditorResult,type IElement} from '@hufe921/canvas-editor'
import JSZip from 'jszip'
import type {PrintField} from '../../api/printing'

export interface PrintBinding {businessType:string;fieldKey:string;repeated:boolean;label:string;group:string}
export interface CanvasTemplate {printType?:'DOCUMENT'|'LIST';schema:'mes.canvas.print.v1';businessType:string;templateCode:string;templateName:string;document:IEditorResult}
const PROPERTY='mes.canvas.print.v1'
const escapeXml=(s:string)=>s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')
export function fieldElement(field:PrintField,businessType:string):IElement {
 return {type:ElementType.CONTROL,value:'',color:'#1677ff',control:{type:ControlType.TEXT,value:null,placeholder:field.label,conceptId:field.key,prefix:'【',postfix:'】',extension:{businessType,fieldKey:field.key,repeated:field.repeated,label:field.label,group:field.group} satisfies PrintBinding}}
}
export function bindingOf(element:IElement):PrintBinding|null {
 const b=element.control?.extension as Partial<PrintBinding>|undefined
 return b&&typeof b.businessType==='string'&&typeof b.fieldKey==='string'&&typeof b.repeated==='boolean'?b as PrintBinding:null
}
export function detailTable(fields:PrintField[],businessType:string):IElement {
 if(!fields.length||fields.length>8||fields.some(f=>!f.repeated))throw new Error('请选择 1–8 个当前数据源的明细字段')
 const cell=(value:IElement[])=>({colspan:1,rowspan:1,value})
 return {type:ElementType.TABLE,value:'\n',colgroup:fields.map(()=>({width:650/fields.length})),trList:[
  {height:34,pagingRepeat:true,tdList:fields.map(f=>({...cell([{value:f.label,bold:true,font:'Microsoft YaHei',size:12}]),backgroundColor:'#f3f6fa'}))},
  {height:34,extension:{businessType,dataset:'items',repeat:true},tdList:fields.map(f=>{const element=fieldElement(f,businessType);return cell([{...element,size:12,control:{...element.control!,size:12,font:'Microsoft YaHei'}}])})}
 ]}
}
function walk(data:IEditorData,visitor:(element:IElement,inRepeat:boolean)=>void){
 function elements(list:IElement[],inRepeat=false){for(const e of list){visitor(e,inRepeat);if(e.type===ElementType.TABLE)for(const row of e.trList??[])for(const td of row.tdList)elements(td.value,!!(row.extension as {repeat?:boolean}|undefined)?.repeat);if(e.valueList)elements(e.valueList,inRepeat);if(e.control?.value)elements(e.control.value,inRepeat)}}
 elements(data.header??[]);elements(data.main);elements(data.footer??[])
}
export function bindingsIn(data:IEditorData):PrintBinding[]{const found:PrintBinding[]=[];walk(data,e=>{const b=bindingOf(e);if(b)found.push(b)});return found}
export function validateBindings(data:IEditorData,fields:PrintField[],type:string){
 const keys=new Map(fields.map(f=>[f.key,f]));let count=0
 walk(data,(e,inRepeat)=>{if(++count>20000)throw new Error('模板内容过多');if(e.type===ElementType.BLOCK||e.control?.compute||e.control?.cascade?.length)throw new Error('打印模板不支持活动内容或动态表达式');const b=bindingOf(e);if(!b)return;const field=keys.get(b.fieldKey);if(b.businessType!==type||!field||field.repeated!==b.repeated)throw new Error('模板中存在不属于当前业务数据源的字段，请重新绑定');if(b.repeated&&!inRepeat)throw new Error('明细字段必须放在明细表的循环行中')})
}
/** Prepare an independent export copy; never replace bindings in the live canvas. */
export function exportData(data:IEditorData):IEditorData {
 const copy=structuredClone(data)
 function elements(list:IElement[]):IElement[]{return list.map(e=>{
  e.font??='Microsoft YaHei'
  const b=bindingOf(e)
  if(b){const {control,...style}=e;return {...style,type:ElementType.TEXT,value:b.repeated?`[${b.fieldKey}]`:`{{${b.fieldKey}}}`,size:e.size??control?.size,font:control?.font||e.font||'Microsoft YaHei',color:'#000000'}}
  if(e.type===ElementType.TABLE){for(const row of e.trList??[]){for(const td of row.tdList)td.value=elements(td.value);if((row.extension as {repeat?:boolean}|undefined)?.repeat){const first=row.tdList[0];if(first)first.value.unshift({value:'{{items}}'})}}}
  if(e.valueList)e.valueList=elements(e.valueList)
  return e
 })}
 copy.header=elements(copy.header??[]);copy.main=elements(copy.main);copy.footer=elements(copy.footer??[])
 if(/^第\s*页\s*\/\s*共\s*页$/.test(copy.footer.map(e=>e.value).join('').replace(/\u200b/g,'').trim()))copy.footer=[]
 return copy
}
/** Imported legacy placeholders become controls only when present in the server field catalogue. */
export function restoreBindings(data:IEditorData,fields:PrintField[],businessType:string):IEditorData {
 const copy=structuredClone(data),keys=new Map(fields.map(f=>[f.key,f]))
 function elements(list:IElement[],repeat=false):IElement[]{return list.flatMap(e=>{
  if(e.type===ElementType.TABLE){for(const row of e.trList??[]){const loop=row.tdList.some(td=>td.value.some(v=>v.value.includes('{{items}}')))||(row.extension as {repeat?:boolean}|undefined)?.repeat;if(loop)row.extension={businessType,dataset:'items',repeat:true};for(const td of row.tdList)td.value=elements(td.value,!!loop)};return [e]}
  if(e.valueList){e.valueList=elements(e.valueList,repeat);return [e]}
  if(e.type&&e.type!==ElementType.TEXT)return [e]
  const value=e.value.replace(repeat?/\{\{items}}/g:/(?!)/g,'');const result:IElement[]=[];let last=0
  for(const match of value.matchAll(/\{\{([A-Za-z][A-Za-z0-9]*)}}|\[([A-Za-z][A-Za-z0-9]*)]/g)){const key=match[1]??match[2]!,f=keys.get(key);if(!f||f.repeated!==!!match[2]||f.repeated&&!repeat)continue;const index=match.index!;if(index>last)result.push({...e,value:value.slice(last,index)});result.push({...e,...fieldElement(f,businessType)});last=index+match[0].length}
  if(last<value.length)result.push({...e,value:value.slice(last)});return result.length?result:[{...e,value}]
 })}
 copy.header=elements(copy.header??[]);copy.main=elements(copy.main);copy.footer=elements(copy.footer??[])
 if(/^第\s*页\s*\/\s*共\s*页$/.test(copy.footer.map(e=>e.value).join('').replace(/\u200b/g,'').trim()))copy.footer=[]
 return copy
}
export function initialDocument(fields:PrintField[],businessType:string,title:string):IEditorData {
 const required=fields.filter(f=>!f.repeated&&['reportNo','modeLabel'].includes(f.key))
 return {main:[{value:title||'打印模板',size:24,bold:true,rowFlex:RowFlex.CENTER},{value:'\n'},...required.flatMap(f=>[fieldElement(f,businessType),{value:'\n'}])],header:[],footer:[]}
}
export function fillExample(data:IEditorData,example:Record<string,unknown>):IEditorData {
 const copy=structuredClone(data)
 function elements(list:IElement[],item?:Record<string,unknown>):IElement[]{return list.map(e=>{
  const b=bindingOf(e)
  if(b){const {control,...style}=e;return {...style,type:ElementType.TEXT,value:String((b.repeated?item?.[b.fieldKey]:example[b.fieldKey])??'—'),size:e.size??control?.size,font:control?.font||e.font,color:'#000000'}}
  if(e.type===ElementType.TABLE)e.trList=(e.trList??[]).flatMap(row=>{
   const rows=(row.extension as {repeat?:boolean}|undefined)?.repeat?(Array.isArray(example.items)?example.items:[{}]):[null]
   return rows.map(record=>{const r=structuredClone(row);delete r.id;for(const td of r.tdList){delete td.id;td.value=elements(td.value,record as Record<string,unknown>|undefined)}return r})
  })
  if(e.valueList)e.valueList=elements(e.valueList,item);return e
 })}
 copy.header=elements(copy.header??[]);copy.main=elements(copy.main);copy.footer=elements(copy.footer??[]);return copy
}
export async function embedCanvas(blob:Blob,template:CanvasTemplate):Promise<Blob>{
 const zip=await JSZip.loadAsync(await blob.arrayBuffer());const raw=JSON.stringify(template)
 if(raw.length>2_000_000)throw new Error('模板编辑数据过大')
 const path='docProps/custom.xml',existing=await zip.file(path)?.async('string')
 const parser=new DOMParser(),doc=parser.parseFromString(existing??'<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes"/>','application/xml')
 // docx@8 emits "100%"; the existing JAXB/docx4j consumer expects fiftieths of a percent.
 // Preserve the exact width while serializing it as the interoperable numeric OOXML form.
 const w='http://schemas.openxmlformats.org/wordprocessingml/2006/main'
 // The DOCX exporter omits Normal and document run defaults. docx4j then falls
 // back to Times New Roman even for CJK text. Fill missing defaults only.
 const styleEntry=zip.file('word/styles.xml')
 if(styleEntry){
  const styles=parser.parseFromString(await styleEntry.async('string'),'application/xml')
  const make=(name:string)=>styles.createElementNS(w,`w:${name}`)
  function fonts(parent:Element){if(parent.getElementsByTagNameNS(w,'rFonts').length)return;const node=make('rFonts');for(const script of ['ascii','hAnsi','eastAsia','cs'])node.setAttributeNS(w,`w:${script}`,'Microsoft YaHei');parent.appendChild(node)}
  let defaults=styles.getElementsByTagNameNS(w,'docDefaults')[0];if(!defaults){defaults=make('docDefaults');styles.documentElement.insertBefore(defaults,styles.documentElement.firstChild)}
  let runDefault=defaults.getElementsByTagNameNS(w,'rPrDefault')[0];if(!runDefault){runDefault=make('rPrDefault');defaults.appendChild(runDefault)}
  let properties=runDefault.getElementsByTagNameNS(w,'rPr')[0];if(!properties){properties=make('rPr');runDefault.appendChild(properties)}fonts(properties)
  const paragraphStyles=Array.from(styles.getElementsByTagNameNS(w,'style')).filter(node=>node.getAttributeNS(w,'type')==='paragraph')
  if(!paragraphStyles.some(node=>['1','true'].includes(node.getAttributeNS(w,'default')??''))){
   let normal=paragraphStyles.find(node=>node.getAttributeNS(w,'styleId')==='Normal')
   if(!normal){normal=make('style');normal.setAttributeNS(w,'w:type','paragraph');normal.setAttributeNS(w,'w:styleId','Normal');const name=make('name');name.setAttributeNS(w,'w:val','Normal');normal.appendChild(name);styles.documentElement.appendChild(normal)}
   normal.setAttributeNS(w,'w:default','1');let run=normal.getElementsByTagNameNS(w,'rPr')[0];if(!run){run=make('rPr');normal.appendChild(run)}fonts(run)
  }
  zip.file('word/styles.xml',new XMLSerializer().serializeToString(styles))
 }
 for(const entry of Object.values(zip.files).filter(f=>/^word\/.*\.xml$/.test(f.name)&&!f.dir)){
  const xml=await entry.async('string');if(!xml.includes('%')&&!xml.includes('fldChar'))continue
  const part=parser.parseFromString(xml,'application/xml');let changed=false
  for(const tag of ['tblW','tcW','tblInd'])for(const node of Array.from(part.getElementsByTagNameNS(w,tag))){const width=node.getAttributeNS(w,'w')??'';if(node.getAttributeNS(w,'type')==='pct'&&/^\d+(\.\d+)?%$/.test(width)){node.setAttributeNS(w,'w:w',String(Math.round(parseFloat(width)*50)));changed=true}}
  // Exporter puts PAGE/NUMPAGES and literal text in one run. Split the generated
  // fields into standard fldSimple siblings so the existing FO renderer sees them.
  if(/^word\/footer\d+\.xml$/.test(entry.name))for(const run of Array.from(part.getElementsByTagNameNS(w,'r'))){
   if(!run.getElementsByTagNameNS(w,'fldChar').length)continue
   const output=part.createDocumentFragment(),style=run.getElementsByTagNameNS(w,'rPr')[0];let instruction='',inField=false,contents:Node[]=[]
   const flush=()=>{if(!contents.length)return;const r=part.createElementNS(w,'w:r');if(style)r.appendChild(style.cloneNode(true));for(const n of contents)r.appendChild(n.cloneNode(true));output.appendChild(r);contents=[]}
   for(const node of Array.from(run.childNodes)){
    if(node instanceof Element&&node.localName==='rPr')continue
    if(node instanceof Element&&node.localName==='fldChar'){
     const kind=node.getAttributeNS(w,'fldCharType')
     if(kind==='begin'){flush();inField=true;instruction=''}
     else if(kind==='end'){if(!/^(PAGE|NUMPAGES)$/.test(instruction.trim()))throw new Error('不支持的页脚域');const field=part.createElementNS(w,'w:fldSimple');field.setAttributeNS(w,'w:instr',instruction.trim());output.appendChild(field);inField=false;contents=[]}
    }else if(inField&&node instanceof Element&&node.localName==='instrText')instruction+=node.textContent??''
    else if(!inField)contents.push(node)
   }
   flush();if(inField)throw new Error('页脚域未闭合');run.replaceWith(output);changed=true
  }
  if(changed)zip.file(entry.name,new XMLSerializer().serializeToString(part))
 }
 for(const p of Array.from(doc.getElementsByTagName('property')))if(p.getAttribute('name')===PROPERTY)p.remove()
 const pid=Math.max(1,...Array.from(doc.getElementsByTagName('property')).map(p=>Number(p.getAttribute('pid'))||1))+1
 const prop=parser.parseFromString(`<property xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes" fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" pid="${pid}" name="${PROPERTY}"><vt:lpwstr>${escapeXml(raw)}</vt:lpwstr></property>`,'application/xml')
 doc.documentElement.appendChild(doc.importNode(prop.documentElement,true));zip.file(path,new XMLSerializer().serializeToString(doc))
 const content=parser.parseFromString(await zip.file('[Content_Types].xml')!.async('string'),'application/xml')
 if(!Array.from(content.getElementsByTagName('Override')).some(p=>p.getAttribute('PartName')==='/'+path)){const p=content.createElementNS(content.documentElement.namespaceURI,'Override');p.setAttribute('PartName','/'+path);p.setAttribute('ContentType','application/vnd.openxmlformats-officedocument.custom-properties+xml');content.documentElement.appendChild(p)}
 zip.file('[Content_Types].xml',new XMLSerializer().serializeToString(content))
 const rel=parser.parseFromString(await zip.file('_rels/.rels')!.async('string'),'application/xml')
 if(!Array.from(rel.getElementsByTagName('Relationship')).some(p=>p.getAttribute('Target')===path)){const p=rel.createElementNS(rel.documentElement.namespaceURI,'Relationship');p.setAttribute('Id','rIdMesCanvas');p.setAttribute('Type','http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties');p.setAttribute('Target',path);rel.documentElement.appendChild(p)}
 zip.file('_rels/.rels',new XMLSerializer().serializeToString(rel));return zip.generateAsync({type:'blob',compression:'DEFLATE'})
}
export async function readCanvas(buffer:ArrayBuffer):Promise<CanvasTemplate|null>{
 const zip=await JSZip.loadAsync(buffer),raw=await zip.file('docProps/custom.xml')?.async('string');if(!raw)return null
 const doc=new DOMParser().parseFromString(raw,'application/xml');const p=Array.from(doc.getElementsByTagName('property')).find(n=>n.getAttribute('name')===PROPERTY);if(!p)return null
 if((p.textContent?.length??0)>2_000_000)throw new Error('模板编辑数据过大')
 const saved=JSON.parse(p.textContent??'') as CanvasTemplate
 if(saved.schema!==PROPERTY||!saved.document?.data?.main||!Array.isArray(saved.document.data.main))throw new Error('模板编辑数据不完整')
 return saved
}
