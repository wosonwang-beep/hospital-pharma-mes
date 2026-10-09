import {describe,expect,it} from 'vitest'
import JSZip from 'jszip'
import {ElementType,type IEditorData} from '@hufe921/canvas-editor'
import {bindingsIn,detailTable,embedCanvas,exportData,fieldElement,fillExample,readCanvas,restoreBindings,validateBindings} from './canvasPrintBinding'
import type {PrintField} from '../../api/printing'
const field=(key:string,repeated=false):PrintField=>({key,label:key,group:repeated?'明细':'单据',description:'',example:'',repeated})
const fields=[field('reportNo'),field('modeLabel'),field('itemName',true),field('result',true)]
describe('Canvas print field binding contract',()=>{
 it('keeps control font sizing when the editor serializes styles in control metadata',()=>{
  const element=fieldElement(fields[2]!,'REPORT');element.control!.size=12;element.control!.font='Microsoft YaHei'
  const exported=exportData({main:[element]}).main[0]!
  expect(exported.size).toBe(12);expect(exported.font).toBe('Microsoft YaHei')
  expect(fillExample({main:[element]},{items:[]}).main[0]?.size).toBe(12)
 })
 it('exports stable authorized keys without mutating the editable document',()=>{
  const data:IEditorData={main:[fieldElement(fields[0]!,'REPORT'),detailTable(fields.slice(2),'REPORT')]}
  const copy=exportData(data)
  expect(copy.main[0]?.value).toBe('{{reportNo}}')
  expect(copy.main[0]?.font).toBe('Microsoft YaHei')
  const values=copy.main[1]?.trList?.[1]?.tdList.map(td=>td.value.map(e=>e.value).join(''))
  expect(values).toEqual(['{{items}}[itemName]','[result]'])
  expect(bindingsIn(data)).toHaveLength(3)
  expect(data.main[1]?.trList?.[1]?.tdList[0]?.value).toHaveLength(1)
 })
 it('rejects mixed business sources, unknown fields and detail fields outside a repeat row',()=>{
  expect(()=>validateBindings({main:[fieldElement(fields[0]!,'OTHER')]},fields,'REPORT')).toThrow('当前业务')
  expect(()=>validateBindings({main:[fieldElement(field('unknown'),'REPORT')]},fields,'REPORT')).toThrow('当前业务')
  expect(()=>validateBindings({main:[fieldElement(fields[2]!,'REPORT')]},fields,'REPORT')).toThrow('循环行')
  expect(()=>validateBindings({main:[detailTable(fields.slice(2),'REPORT')]},fields,'REPORT')).not.toThrow()
  const expression=fieldElement(fields[0]!,'REPORT');expression.control!.compute='unsafe()'
  expect(()=>validateBindings({main:[expression]},fields,'REPORT')).toThrow('动态表达式')
 })
 it('fills every detail record and preserves the source table',()=>{
  const data:IEditorData={main:[fieldElement(fields[0]!,'REPORT'),detailTable(fields.slice(2),'REPORT')]}
  const result=fillExample(data,{reportNo:'REPORT-17',items:[{itemName:'项目一',result:'1'},{itemName:'项目二',result:'2'},{itemName:'项目三',result:'3'}]})
  expect(result.main[0]?.value).toBe('REPORT-17');expect(result.main[1]?.trList).toHaveLength(4)
  expect(result.main[1]?.trList?.[3]?.tdList[0]?.value[0]?.value).toBe('项目三')
  expect(data.main[1]?.trList).toHaveLength(2)
 })
 it('restores authorized legacy Word tokens and only marks rows with a loop marker',()=>{
  const input:IEditorData={main:[{value:'编号：{{reportNo}}'},...exportData({main:[detailTable(fields.slice(2),'REPORT')]}).main]}
  const restored=restoreBindings(input,fields,'REPORT');expect(bindingsIn(restored).map(b=>b.fieldKey)).toEqual(['reportNo','itemName','result'])
  expect(()=>validateBindings(restored,fields,'REPORT')).not.toThrow()
  expect(restored.main.find(e=>e.type===ElementType.TABLE)?.trList?.[1]?.tdList[0]?.value.some(e=>e.value.includes('{{items}}'))).toBe(false)
 })
 it('preserves canvas layout and binding metadata in standard DOCX custom properties',async()=>{
  const zip=new JSZip();zip.file('[Content_Types].xml','<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"/>');zip.file('_rels/.rels','<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>')
  zip.file('word/document.xml','<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:tblW w:type="pct" w:w="100%"/><w:t>保留正文 100%</w:t></w:document>')
  zip.file('word/styles.xml','<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:docDefaults><w:rPrDefault/></w:docDefaults></w:styles>')
  zip.file('word/footer1.xml','<w:ftr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:p><w:r><w:t>第 </w:t><w:fldChar w:fldCharType="begin"/><w:instrText>PAGE</w:instrText><w:fldChar w:fldCharType="separate"/><w:fldChar w:fldCharType="end"/><w:t> 页 / 共 </w:t><w:fldChar w:fldCharType="begin"/><w:instrText>NUMPAGES</w:instrText><w:fldChar w:fldCharType="separate"/><w:fldChar w:fldCharType="end"/><w:t> 页</w:t></w:r></w:p></w:ftr>')
  const source=await zip.generateAsync({type:'uint8array'});const input={arrayBuffer:async()=>source.buffer} as Blob
  const document={version:'1.0.4',options:{},data:{main:[fieldElement(fields[0]!,'REPORT')]}}
  const output=await embedCanvas(input,{schema:'mes.canvas.print.v1',businessType:'REPORT',templateCode:'CODE',templateName:'名称',document})
  const buffer=await new Promise<ArrayBuffer>((resolve,reject)=>{const reader=new FileReader();reader.onload=()=>resolve(reader.result as ArrayBuffer);reader.onerror=reject;reader.readAsArrayBuffer(output)})
  expect(await readCanvas(buffer)).toEqual({schema:'mes.canvas.print.v1',businessType:'REPORT',templateCode:'CODE',templateName:'名称',document})
  const result=await JSZip.loadAsync(buffer),xml=await result.file('word/document.xml')!.async('string')
  expect(xml).toContain('w:w="5000"');expect(xml).toContain('保留正文 100%')
  const styles=await result.file('word/styles.xml')!.async('string');expect(styles).toContain('w:styleId="Normal"');expect(styles).toContain('w:default="1"');expect(styles).toContain('w:eastAsia="Microsoft YaHei"')
  const footer=await result.file('word/footer1.xml')!.async('string')
  expect(footer).toContain('w:fldSimple w:instr="PAGE"');expect(footer).toContain('w:fldSimple w:instr="NUMPAGES"');expect(footer).not.toContain('fldChar')
 })
})
