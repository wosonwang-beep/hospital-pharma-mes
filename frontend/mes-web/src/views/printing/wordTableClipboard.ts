/**
 * Bounded, inert Microsoft Word / WPS clipboard HTML parser.
 *
 * Only semantic text, a selected set of table geometry and visible formatting
 * are carried into the MES schema. No HTML is stored, rendered as v-html or
 * posted to the server. Server validation must independently enforce all bounds.
 */
export interface WordTextSpan {
 text:string
 bold?:boolean
 italic?:boolean
 underline?:boolean
 color?:string
 fontSize?:number
}
export interface WordTableCell {
 text:string
 colspan:number
 rowspan:number
 background?:string
 color?:string
 borderColor?:string
 borderPt?:number
 align?:'LEFT'|'CENTER'|'RIGHT'
 fontSize?:number
 bold?:boolean
 italic?:boolean
 spans?:WordTextSpan[]
 fieldKey?:string
}
export interface WordTableRow {heightPt?:number;cells:WordTableCell[]}
export interface WordTableBlock {
 id:string
 type:'WORD_TABLE'
 fontSize:number
 align:'LEFT'|'CENTER'|'RIGHT'
 widths:number[]
 rows:WordTableRow[]
}
export interface WordClipboardResult {
 block:WordTableBlock
 warnings:string[]
}
const forbidden=/(\{\{|\}\}|<script|javascript:)/i
const IGNORE='script,style,iframe,object,embed,svg,canvas,form,link,meta'
function toColor(raw:string|null|undefined):string|undefined {
 if(!raw)return
 const value=raw.trim().toLowerCase()
 if(/^#[a-f0-9]{6}$/.test(value))return value.toUpperCase()
 if(/^#[a-f0-9]{3}$/.test(value))return '#'+value.slice(1).split('').map(c=>c+c).join('').toUpperCase()
 const rgb=value.match(/^rgba?\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})(?:\s*,[^)]*)?\)$/)
 if(rgb){
  const vals=rgb.slice(1,4).map(Number)
  if(vals.every(n=>n>=0&&n<=255))return '#'+vals.map(n=>n.toString(16).padStart(2,'0')).join('').toUpperCase()
 }
 const names:Record<string,string>={black:'#000000',white:'#FFFFFF',red:'#FF0000',blue:'#0000FF',gray:'#808080',grey:'#808080',navy:'#000080',green:'#008000',silver:'#C0C0C0',yellow:'#FFFF00'}
 return names[value]
}
const getStyle=(el:Element,key:string)=>el instanceof HTMLElement?el.style.getPropertyValue(key):''
function toPoints(raw:string|undefined):number|undefined {
 if(!raw)return
 const s=raw.trim().toLowerCase()
 const m=s.match(/^(\d+(?:\.\d+)?)(px|pt|cm|mm|in)?$/)
 if(!m)return
 const n=Number(m[1])
 const factor:Record<string,number>={px:.75,pt:1,cm:72/2.54,mm:72/25.4,in:72}
 const v=n*(factor[m[2]||'px']||.75)
 return Number.isFinite(v)?v:undefined
}
function fontSizeOf(el:Element):number|undefined{
 const n=toPoints(getStyle(el,'font-size'))
 return n?Math.max(8,Math.min(28,Math.round(n))):undefined
}
function alignOf(raw:string):'LEFT'|'CENTER'|'RIGHT'|undefined{
 const s=raw.toLowerCase()
 return s.includes('center')?'CENTER':s.includes('right')?'RIGHT':s.includes('left')?'LEFT':undefined
}
function textOf(el:Element):string {
 // textContent only: no HTML parsing after this point. Remove active elements.
 const clone=el.cloneNode(true) as Element
 clone.querySelectorAll(IGNORE).forEach(node=>node.remove())
 return (clone.textContent??'').replace(/[\u00a0\u200b]/g,' ').replace(/\s+/g,' ').trim()
}
function spansOf(el:Element):WordTextSpan[]{
 const spans:WordTextSpan[]=[]
 const visit=(node:Node,style:Omit<WordTextSpan,'text'>)=>{
  if(node.nodeType===Node.TEXT_NODE){
   const text=(node.textContent??'').replace(/\u00a0/g,' ')
   if(text.trim())spans.push({text,...style})
   return
  }
  if(node.nodeType!==Node.ELEMENT_NODE)return
  const child=node as Element
  if(child.matches(IGNORE))return
  if(child.tagName==='BR'){spans.push({text:'\n',...style});return}
  const size=fontSizeOf(child)
  const weight=getStyle(child,'font-weight')
  const color=toColor(getStyle(child,'color'))
  const next={
   ...style,
   ...((child.tagName==='B'||child.tagName==='STRONG'||/^(bold|[6-9]00)$/.test(weight))?{bold:true}:{}),
   ...((child.tagName==='I'||child.tagName==='EM'||getStyle(child,'font-style')==='italic')?{italic:true}:{}),
   ...((child.tagName==='U'||getStyle(child,'text-decoration').includes('underline'))?{underline:true}:{}),
   ...(color?{color}:{}),
   ...(size?{fontSize:size}:{})
  }
  child.childNodes.forEach(n=>visit(n,next))
 }
 el.childNodes.forEach(n=>visit(n,{}))
 return spans.slice(0,80)
}
function widthPercent(raw:string|undefined):number|undefined{
 if(!raw)return
 const s=raw.trim()
 if(s.endsWith('%')){
  const n=Number(s.slice(0,-1))
  return n>0&&n<=100?n:undefined
 }
 const pt=toPoints(s)
 return pt?pt:undefined
}
function normalizeWidths(vals:number[],count:number):number[]{
 const used=vals.filter(v=>Number.isFinite(v)&&v>0)
 if(used.length!==count)return Array.from({length:count},()=>Math.round(10000/count)/100)
 const sum=vals.reduce((a,b)=>a+b,0)
 if(sum<=0)return Array.from({length:count},()=>Math.round(10000/count)/100)
 const adjusted=vals.map(v=>Math.round(v/sum*10000)/100)
 adjusted[count-1]=Math.round((100-adjusted.slice(0,-1).reduce((a,b)=>a+b,0))*100)/100
 return adjusted
}
export function parseWordTableClipboard(html:string,id='pasted-table'):WordClipboardResult{
 if(!html||html.length>200_000)throw new Error('Word 表格内容为空或超过 200 KB 限额')
 const page=new DOMParser().parseFromString(html,'text/html')
 const table=page.querySelector('table')
 if(!table)throw new Error('剪贴板中没有 Word 表格；请在 Word 中选中整个表格再复制')
 if(table.querySelector('table'))throw new Error('暂不支持嵌套 Word 表格，请先拆分后粘贴')
 const trs=Array.from(table.querySelectorAll('tr')).filter(tr=>tr.closest('table')===table)
 if(trs.length===0||trs.length>30)throw new Error('一次只能粘贴 1 至 30 行 Word 表格')
 const maxCols=12
 const covered=Array.from({length:trs.length},()=>Array(maxCols).fill(false) as boolean[])
 const widths=Array(maxCols).fill(0) as number[]
 const warnings:string[]=[]
 const rows:WordTableRow[]=[]
 let count=0,cols=0
 for(const [r,tr] of trs.entries()){
  const cells=Array.from(tr.querySelectorAll('td,th')).filter(c=>c.closest('tr')===tr)
  const out:WordTableCell[]=[]
  let c=0
  for(const cell of cells){
   while(c<maxCols&&covered[r]![c])c++
   const colspan=Math.max(1,Number(cell.getAttribute('colspan')||1))
   const rowspan=Math.max(1,Number(cell.getAttribute('rowspan')||1))
   if(!Number.isInteger(colspan)||!Number.isInteger(rowspan)||colspan>maxCols||rowspan>trs.length-r||c+colspan>maxCols)
    throw new Error('Word 表格合并单元格超出支持范围')
   for(let rr=r;rr<r+rowspan;rr++)for(let cc=c;cc<c+colspan;cc++){
    if(covered[rr]![cc])throw new Error('Word 表格单元格存在重叠合并')
    covered[rr]![cc]=true
   }
   const original=textOf(cell)
   if(original.length>600||forbidden.test(original))throw new Error('Word 单元格内容过长或包含不允许的模板表达式')
   count++
   if(count>160)throw new Error('一次最多粘贴 160 个 Word 单元格')
   const align=alignOf(getStyle(cell,'text-align')||cell.getAttribute('align')||'')
   const background=toColor(getStyle(cell,'background-color')||getStyle(cell,'background')||cell.getAttribute('bgcolor'))
   const color=toColor(getStyle(cell,'color'))
   const borderRaw=getStyle(cell,'border')||getStyle(cell,'border-top')
   const borderColor=toColor(getStyle(cell,'border-color')||borderRaw.match(/#[a-fA-F0-9]{3,6}|rgb\([^)]*\)/)?.[0])
   const borderPt=toPoints(borderRaw.match(/\d+(?:\.\d+)?(?:px|pt|cm|mm)/)?.[0]||'')
   const fontSize=fontSizeOf(cell)
   const spans=spansOf(cell)
   out.push({
    text:original,colspan,rowspan,
    ...(background?{background}:{}),...(color?{color}:{}),
    ...(borderColor?{borderColor}:{}),...(borderPt?{borderPt:Math.min(4,Math.max(.25,borderPt))}:{}),
    ...(align?{align}:{}),...(fontSize?{fontSize}:{}),
    ...((cell.tagName==='TH'||getStyle(cell,'font-weight').includes('bold'))?{bold:true}:{}),
    ...(spans.length?{spans}:{})
   })
   if(r===0){
    const w=widthPercent(getStyle(cell,'width')||cell.getAttribute('width')||undefined)
    if(w)for(let cc=c;cc<c+colspan;cc++)widths[cc]=w/colspan
   }
   c+=colspan
   cols=Math.max(cols,c)
  }
  const height=toPoints(getStyle(tr,'height')||tr.getAttribute('height')||undefined)
  rows.push({...((height&&height>=10)?{heightPt:Math.min(height,150)}:{}),cells:out})
 }
 if(cols<1||cols>maxCols)throw new Error('Word 表格列数必须在 1 至 12 之间')
 for(let r=0;r<trs.length;r++)for(let c=0;c<cols;c++)
  if(!covered[r]![c])throw new Error('Word 表格行或合并区域不完整')
 const colEls=Array.from(table.querySelectorAll('colgroup > col')).filter(x=>x.closest('table')===table)
 if(colEls.length===cols){
  for(const [i,el] of colEls.entries()){
   const w=widthPercent(getStyle(el,'width')||el.getAttribute('width')||undefined)
   if(w)widths[i]=w
  }
 }
 if(widths.slice(0,cols).some(w=>!w))warnings.push('部分列宽没有由 Word 剪贴板提供，已按比例补齐')
 const effective=widths.slice(0,cols).map(w=>w||100/cols)
 if(rows.some(row=>row.cells.some(cell=>!cell.borderColor)))warnings.push('部分 Word 特殊边框样式无法从剪贴板提取，需以 PDF 对照检查')
 if(table.querySelector('img,svg,shape'))warnings.push('表格内图片未导入，请单独上传受控图片')
 return {block:{id,type:'WORD_TABLE',fontSize:10,align:'LEFT',widths:normalizeWidths(effective,cols),rows},warnings}
}
