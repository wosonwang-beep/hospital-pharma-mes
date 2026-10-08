import {describe,it,expect} from 'vitest'
import {parseWordTableClipboard} from './wordTableClipboard'

describe('bounded Microsoft Word table clipboard importer',()=>{
 it('preserves merged horizontal and vertical cells, widths, colors and rich text',()=>{
  const {block,warnings}=parseWordTableClipboard(`
  <html><body><table style="border-collapse:collapse">
   <colgroup><col style="width:30%"><col style="width:20%"><col style="width:50%"></colgroup>
   <tr style="height:27pt"><td colspan="2" style="border:1pt solid #335577;background-color:#EAF3FF;text-align:center">
      <span style="font-size:12pt;color:#113355"><b>医院制剂</b>检验记录</span>
    </td><td rowspan="2" style="background-color:rgb(240,240,240)">审批人</td></tr>
   <tr><td style="border:1px solid #333333">品名</td><td style="color:red"><i>氯化钾</i></td></tr>
  </table></body></html>`,'word-1')
  expect(block.id).toBe('word-1')
  expect(block.widths).toEqual([30,20,50])
  expect(block.rows).toHaveLength(2)
  expect(block.rows[0]!.heightPt).toBe(27)
  expect(block.rows[0]!.cells[0]!).toMatchObject({
    colspan:2,rowspan:1,align:'CENTER',background:'#EAF3FF',
    borderColor:'#335577',borderPt:1
  })
  expect(block.rows[0]!.cells[0]!.spans?.[0]).toMatchObject({
    text:'医院制剂',bold:true,fontSize:12,color:'#113355'
  })
  expect(block.rows[0]!.cells[1]!.rowspan).toBe(2)
  expect(block.rows[1]!.cells).toHaveLength(2)
  expect(block.rows[1]!.cells[1]!.spans?.[0]?.italic).toBe(true)
  expect(warnings).toBeDefined()
 })
 it('reads Word MSO spans from clipboard HTML without evaluating HTML or expression markup',()=>{
  const r=parseWordTableClipboard(`<table><tbody><tr>
   <td style="width:40%;background:#f0f0f0"><p class="MsoNormal"><span style="font-family:宋体;font-size:10pt">检验人：</span><b>张三</b></p></td>
   <td style="width:60%">审批时间</td>
  </tr></tbody></table>`)
  expect(r.block.rows[0]!.cells[0]!.text).toBe('检验人：张三')
  expect(r.block.rows[0]!.cells[0]!.spans?.some(s=>s.text==='张三'&&s.bold)).toBe(true)
  expect(r.block.rows[0]!.cells[0]!.background).toBe('#F0F0F0')
 })
 it('rejects dangerous and oversized input; never retains HTML, JS or arbitrary placeholders',()=>{
  expect(()=>parseWordTableClipboard('plain paragraph')).toThrow('没有 Word 表格')
  expect(()=>parseWordTableClipboard('<table><tr><td>{{unsafe}}</td></tr></table>')).toThrow('不允许')
  expect(()=>parseWordTableClipboard('<table><tr><td colspan="13">invalid</td></tr></table>')).toThrow('超出')
  expect(()=>parseWordTableClipboard('<table><tr><td>safe<table><tr><td>x</td></tr></table></td></tr></table>')).toThrow('嵌套')
  expect(()=>parseWordTableClipboard('x'.repeat(210000))).toThrow('200 KB')
 })
 it('ignores active markup and keeps only harmless text',()=>{
  const {block}=parseWordTableClipboard(`<table><tr><td>报告<script>fetch('https://evil.test')</script>编号</td></tr></table>`)
  expect(block.rows[0]!.cells[0]!.text).toBe('报告编号')
  expect(JSON.stringify(block)).not.toContain('evil.test')
 })
})
