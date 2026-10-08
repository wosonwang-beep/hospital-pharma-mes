package com.hospital.mes.reporting.application;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import java.math.BigInteger;
import java.util.*;

/**
 * Emits real OOXML table geometry from a bounded Word clipboard structure:
 * grid widths, horizontal grid spans, vertical merges, cell fill/borders,
 * rich inline runs, row height and approved business field placeholders.
 *
 * Never accepts HTML or an arbitrary OOXML fragment from the client.
 */
final class WordTableComposer {
 private static final int MAX_COLS=12,MAX_ROWS=30,MAX_CELLS=160,MAX_TEXT=600;
 private final Map<String,PrintField> regular;
 private final Set<String> used;
 WordTableComposer(Map<String,PrintField> regular,Set<String> used){
  this.regular=regular;this.used=used;
 }
 private record Anchor(int row,int col,int rows,int cols,JsonNode data){}
 void append(XWPFDocument doc,JsonNode block,int defaultFontSize){
  var rows=block.path("rows");
  var widths=block.path("widths");
  if(!rows.isArray()||rows.isEmpty()||rows.size()>MAX_ROWS
    ||!widths.isArray()||widths.isEmpty()||widths.size()>MAX_COLS)
   throw invalid("Word 粘贴表格限制为 1–30 行、1–12 列");
  int n=rows.size(),m=widths.size();
  double[] colWidths=new double[m];double sum=0;
  for(int i=0;i<m;i++){
   var w=widths.get(i);
   if(!w.isNumber()||!Double.isFinite(w.asDouble())||w.asDouble()<1||w.asDouble()>100)
    throw invalid("Word 列宽百分比无效");
   colWidths[i]=w.asDouble();sum+=colWidths[i];
  }
  if(Math.abs(sum-100)>0.25)throw invalid("Word 列宽总比例须为 100%");
  Anchor[][] owners=new Anchor[n][m];
  int actual=0;
  for(int r=0;r<n;r++){
   var row=rows.get(r);var cells=row.path("cells");
   if(!cells.isArray()||cells.isEmpty()||cells.size()>m)
    throw invalid("Word 表格行结构非法");
   double height=row.path("heightPt").asDouble(0);
   if(row.has("heightPt")&&(height<10||height>150))
    throw invalid("Word 行高无效");
   int col=0;
   for(var data:cells){
    while(col<m&&owners[r][col]!=null)col++;
    int rs=span(data,"rowspan",n),cs=span(data,"colspan",m);
    if(col+cs>m||r+rs>n)throw invalid("Word 合并单元格超出边界");
    for(int rr=r;rr<r+rs;rr++)for(int cc=col;cc<col+cs;cc++)
     if(owners[rr][cc]!=null)throw invalid("Word 合并单元格重叠");
    validateStyle(data,defaultFontSize);
    var anchor=new Anchor(r,col,rs,cs,data);
    for(int rr=r;rr<r+rs;rr++)for(int cc=col;cc<col+cs;cc++)
     owners[rr][cc]=anchor;
    actual++;
    if(actual>MAX_CELLS)throw invalid("Word 单次粘贴最多 160 个单元格");
    col+=cs;
   }
  }
  for(var row:owners)for(var cell:row)if(cell==null)throw invalid("Word 表格存在未填满的合并网格");

  XWPFTable table=doc.createTable(n,m);
  table.setWidth("100%");
  table.setCellMargins(80,90,80,90);
  var grid=table.getCTTbl().getTblGrid();
  if(grid==null)grid=table.getCTTbl().addNewTblGrid();
  int totalTwips=9638,assigned=0;
  for(int c=0;c<m;c++){
   int twips=c==m-1?totalTwips-assigned:(int)Math.round(totalTwips*colWidths[c]/100);
   assigned+=twips;
   if(grid.sizeOfGridColArray()<=c)grid.addNewGridCol().setW(BigInteger.valueOf(twips));
   else grid.getGridColArray(c).setW(BigInteger.valueOf(twips));
  }

  for(int r=0;r<n;r++){
   XWPFTableRow row=table.getRow(r);
   if(rows.get(r).has("heightPt"))row.setHeight((int)Math.round(rows.get(r).path("heightPt").asDouble()*20));
   row.setCantSplitRow(true);
   boolean[] keep=new boolean[m];
   for(int c=0;c<m;c++){
    Anchor a=owners[r][c];
    if(c!=a.col)continue;
    keep[c]=true;
    XWPFTableCell cell=row.getCell(c);
    CTTcPr pr=cell.getCTTc().isSetTcPr()?cell.getCTTc().getTcPr():cell.getCTTc().addNewTcPr();
    if(a.cols>1)pr.addNewGridSpan().setVal(BigInteger.valueOf(a.cols));
    if(a.rows>1)pr.addNewVMerge().setVal(r==a.row?STMerge.RESTART:STMerge.CONTINUE);
    int twips=0;
    for(int k=a.col;k<a.col+a.cols;k++)twips+=(int)Math.round(totalTwips*colWidths[k]/100);
    cell.setWidth(twips+"");
    if(r==a.row)fillCell(cell,a.data,defaultFontSize);
    else{
     cell.removeParagraph(0);
     cell.addParagraph();
    }
   }
   for(int c=m-1;c>=0;c--)if(!keep[c])row.removeCell(c);
  }
 }
 private void fillCell(XWPFTableCell cell,JsonNode data,int fallback){
  var color=data.path("background").asText("");
  if(!color.isEmpty())cell.setColor(color.substring(1));
  cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
  var pr=cell.getCTTc().isSetTcPr()?cell.getCTTc().getTcPr():cell.getCTTc().addNewTcPr();
  String borderColor=data.path("borderColor").asText("#CBD5E1");
  double pt=data.path("borderPt").asDouble(.5);
  var borders=pr.isSetTcBorders()?pr.getTcBorders():pr.addNewTcBorders();
  border(borders.addNewTop(),borderColor,pt);border(borders.addNewRight(),borderColor,pt);
  border(borders.addNewBottom(),borderColor,pt);border(borders.addNewLeft(),borderColor,pt);

  cell.removeParagraph(0);
  XWPFParagraph paragraph=cell.addParagraph();
  paragraph.setAlignment(ParagraphAlignment.valueOf(data.path("align").asText("LEFT")));
  var runs=data.path("spans");
  if(runs.isArray()&&!runs.isEmpty()){
   if(runs.size()>80)throw invalid("Word 单元格富文本片段过多");
   String joined="";
   for(var part:runs){
    var text=requiredText(part,"text");
    joined+=text;
    appendRun(paragraph,text,part,fallback);
   }
   if(!joined.replaceAll("\\s+"," ").trim().equals(data.path("text").asText("").replaceAll("\\s+"," ").trim()))
    throw invalid("Word 富文本片段与单元格内容不一致");
  }else{
   String text=data.path("text").asText("");
   if(!text.isEmpty())appendRun(paragraph,text,data,fallback);
  }
  if(data.hasNonNull("fieldKey")){
   String key=data.path("fieldKey").asText();
   if(!regular.containsKey(key)||key.equals("items"))
    throw invalid("单元格绑定了未经授权的业务字段");
   used.add(key);
   XWPFRun marker=paragraph.createRun();
   marker.setFontFamily("Microsoft YaHei");
   marker.setFontSize(size(data,fallback));
   marker.setColor("215A8F");
   marker.setText("{{"+key+"}}");
  }
 }
 private static void appendRun(XWPFParagraph paragraph,String text,JsonNode style,int fallback){
  if(text.length()>MAX_TEXT||text.contains("{{")||text.contains("}}")||text.contains("[item"))
   throw invalid("单元格文字不允许模板表达式");
  XWPFRun run=paragraph.createRun();
  run.setFontFamily("Microsoft YaHei");
  run.setFontSize(size(style,fallback));
  run.setBold(style.path("bold").asBoolean(false));
  run.setItalic(style.path("italic").asBoolean(false));
  if(style.path("underline").asBoolean(false))run.setUnderline(UnderlinePatterns.SINGLE);
  var color=style.path("color").asText("");
  if(!color.isEmpty())run.setColor(color.substring(1));
  String[] lines=text.split("\\n",-1);
  for(int i=0;i<lines.length;i++){if(i>0)run.addBreak();run.setText(lines[i]);}
 }
 private static void border(CTBorder b,String color,double pt){
  b.setVal(STBorder.SINGLE);
  b.setColor(color.substring(1));
  b.setSz(BigInteger.valueOf(Math.max(2,Math.round(pt*8))));
 }
 private static void validateStyle(JsonNode data,int fallback){
  if(!data.isObject())throw invalid("单元格格式非法");
  var text=data.path("text");
  if(!text.isTextual()||text.asText().length()>MAX_TEXT
   ||text.asText().contains("{{")||text.asText().contains("}}"))
   throw invalid("单元格内容包含非法变量或超过限制");
  size(data,fallback);
  String align=data.path("align").asText("LEFT");
  if(!Set.of("LEFT","CENTER","RIGHT").contains(align))throw invalid("单元格对齐无效");
  for(var field:List.of("background","color","borderColor"))
   if(data.has(field)&&!data.path(field).asText().matches("#[0-9A-Fa-f]{6}"))
    throw invalid("Word 颜色必须是六位十六进制颜色");
  if(data.has("borderPt")&&(data.path("borderPt").asDouble()<.25||data.path("borderPt").asDouble()>4))
   throw invalid("边框宽度必须在 0.25 至 4pt");
  var runs=data.path("spans");
  if(!runs.isMissingNode()&&!runs.isArray())throw invalid("单元格富文本格式无效");
  if(runs.isArray()){
   if(runs.size()>80)throw invalid("单元格富文本片段过多");
   for(var p:runs){size(p,fallback);if(p.has("color")&&!p.path("color").asText().matches("#[0-9A-Fa-f]{6}"))throw invalid("字体颜色无效");}
  }
 }
 private static int size(JsonNode data,int fallback){
  int n=data.path("fontSize").asInt(fallback);
  if(n<8||n>28)throw invalid("字号必须在 8 至 28pt");
  return n;
 }
 private static int span(JsonNode data,String name,int max){
  var v=data.path(name);
  int n=v.isMissingNode()?1:v.asInt(-1);
  if(n<1||n>max||(!v.isMissingNode()&&!v.isIntegralNumber()))
   throw invalid("Word 合并行列数无效");
  return n;
 }
 private static String requiredText(JsonNode data,String name){
  var n=data.path(name);
  if(!n.isTextual())throw invalid("Word 字体片段类型无效");
  return n.asText();
 }
 private static IllegalArgumentException invalid(String message){return new IllegalArgumentException(message);}
}
