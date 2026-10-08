package com.hospital.mes.reporting.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

/**
 * MES-owned safe layout composer. The persisted artifact remains a real DOCX with
 * an embedded, bounded design descriptor, so versions remain immutable and
 * existing source rendering, approval, PDF hashing and archived reprint survive.
 * No executable expression, HTML or arbitrary script is accepted.
 */
@Component
public class NativePrintDesigner {
 public static final String PROPERTY="mes.native.design.v1";
 private static final Set<String> TYPES=Set.of("TITLE","TEXT","FIELD","TABLE","WORD_TABLE","DIVIDER");
 private static final Set<String> ALIGN=Set.of("LEFT","CENTER","RIGHT");
 private final ObjectMapper json;
 public NativePrintDesigner(ObjectMapper json){this.json=json;}

 public JsonNode defaults(){
  try{return json.readTree("""
   {"blocks":[
    {"id":"heading","type":"TITLE","text":"药品检验报告","fontSize":20,"align":"CENTER"},
    {"id":"mode","type":"FIELD","fieldKey":"modeLabel","fontSize":11,"align":"CENTER"},
    {"id":"number","type":"FIELD","fieldKey":"reportNo","fontSize":11,"align":"CENTER"},
    {"id":"request","type":"FIELD","fieldKey":"requestNo","fontSize":11,"align":"LEFT"},
    {"id":"material","type":"FIELD","fieldKey":"materialName","fontSize":11,"align":"LEFT"},
    {"id":"items","type":"TABLE","columns":["sequence","itemName","criteria","result","conclusion","recordedBy"],"fontSize":10,"align":"LEFT"},
    {"id":"conclusion","type":"FIELD","fieldKey":"overallResult","fontSize":11,"align":"LEFT"},
    {"id":"inspector","type":"FIELD","fieldKey":"inspectors","fontSize":11,"align":"LEFT"},
    {"id":"approver","type":"FIELD","fieldKey":"approvedBy","fontSize":11,"align":"LEFT"},
    {"id":"approval-time","type":"FIELD","fieldKey":"approvedAt","fontSize":11,"align":"LEFT"},
    {"id":"signature","type":"FIELD","fieldKey":"signatureReference","fontSize":11,"align":"LEFT"}
   ]}
   """);}catch(Exception e){throw new IllegalStateException(e);}
 }

 public JsonNode extract(byte[] bytes){
  try(var doc=new XWPFDocument(new ByteArrayInputStream(bytes))){
   var property=doc.getProperties().getCustomProperties().getProperty(PROPERTY);
   if(property==null||!property.isSetLpwstr())throw new IllegalArgumentException("该 Word 模板未使用 MES 内置设计器创建；原始 DOCX 可继续预览和打印");
   var parsed=json.readTree(property.getLpwstr());
   if(parsed==null||!parsed.isObject())throw new IllegalArgumentException("可视化模板描述损坏");
   return parsed;
  }catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new IllegalArgumentException("无法读取可视化模板",e);}
 }

 public byte[] generate(JsonNode spec,List<PrintField> definitions) {
  if(spec==null||!spec.isObject()||!spec.has("blocks")||!spec.get("blocks").isArray())
   throw new IllegalArgumentException("模板必须包含合法的页面组件");
  var blocks=spec.get("blocks");
  if(blocks.isEmpty()||blocks.size()>80)throw new IllegalArgumentException("模板仅允许 1 至 80 个页面组件");
  var regular=new LinkedHashMap<String,PrintField>();
  var repeated=new LinkedHashMap<String,PrintField>();
  for(var f:definitions)(f.repeated()?repeated:regular).put(f.key(),f);
  Set<String> ids=new HashSet<>(),used=new HashSet<>();
  try(var doc=new XWPFDocument();var out=new ByteArrayOutputStream()){
   var sect=doc.getDocument().getBody().addNewSectPr();
   var pg=sect.addNewPgSz();pg.setW(BigInteger.valueOf(11906));pg.setH(BigInteger.valueOf(16838));
   var margins=sect.addNewPgMar();margins.setTop(BigInteger.valueOf(1134));margins.setBottom(BigInteger.valueOf(1134));
   margins.setLeft(BigInteger.valueOf(1134));margins.setRight(BigInteger.valueOf(1134));
   for(var block:blocks){
    if(!block.isObject())throw new IllegalArgumentException("组件结构非法");
    var id=required(block,"id",64);
    if(!id.matches("[A-Za-z0-9_-]{1,64}")||!ids.add(id))throw new IllegalArgumentException("组件标识必须唯一");
    var type=required(block,"type",12);
    if(!TYPES.contains(type))throw new IllegalArgumentException("不支持的页面组件类型");
    int size=block.path("fontSize").asInt(11);
    if(size<8||size>28)throw new IllegalArgumentException("字体大小限制为 8 至 28");
    String align=block.path("align").asText("LEFT");
    if(!ALIGN.contains(align))throw new IllegalArgumentException("对齐方式无效");
    switch(type){
     case "TITLE","TEXT"->{
      var text=required(block,"text",600);
      if(text.contains("{{")||text.contains("}}")||text.contains("[item"))throw new IllegalArgumentException("请用中文字段选择器插入变量，不能输入表达式");
      var p=doc.createParagraph();p.setAlignment(ParagraphAlignment.valueOf(align));p.setSpacingAfter(type.equals("TITLE")?260:160);
      var run=p.createRun();run.setFontFamily("Microsoft YaHei");run.setFontSize(size);run.setBold(type.equals("TITLE"));run.setText(text);
     }
     case "FIELD"->{
      var key=required(block,"fieldKey",80);
      var f=regular.get(key);
      if(f==null||key.equals("items"))throw new IllegalArgumentException("字段不在当前业务的授权中文字段字典中");
      used.add(key);
      var p=doc.createParagraph();p.setSpacingAfter(150);p.setAlignment(ParagraphAlignment.valueOf(align));
      var r=p.createRun();r.setFontFamily("Microsoft YaHei");r.setFontSize(size);
      r.setText(f.label()+"：{{"+key+"}}");
     }
     case "TABLE"->{
      var cols=block.path("columns");
      if(!cols.isArray()||cols.isEmpty()||cols.size()>8)throw new IllegalArgumentException("循环表格需要选择 1 至 8 个检验字段");
      var keys=new ArrayList<String>();
      for(var keyNode:cols){
       if(!keyNode.isTextual()||!repeated.containsKey(keyNode.asText())||keys.contains(keyNode.asText()))
        throw new IllegalArgumentException("检验项目循环列无效或重复");
       keys.add(keyNode.asText());
      }
      var table=doc.createTable(2,keys.size());table.setWidth("100%");
      table.getRow(0).setRepeatHeader(true);
      for(var row:table.getRows())row.setCantSplitRow(true);
      for(int col=0;col<keys.size();col++){
       var key=keys.get(col);
       setCell(table.getRow(0).getCell(col),repeated.get(key).label(),size,true);
       table.getRow(0).getCell(col).setColor("EAF0F8");
       setCell(table.getRow(1).getCell(col),(col==0?"{{items}}":"")+"["+key+"]",size,false);
      }
      used.add("items");
     }
     case "WORD_TABLE"->new WordTableComposer(regular,used).append(doc,block,size);
     case "DIVIDER"->{
      var p=doc.createParagraph();p.setSpacingAfter(100);
      var r=p.createRun();r.setText("────────────────────────");r.setColor("D5DFEC");
     }
    }
   }
   if(!used.contains("modeLabel")||!used.contains("reportNo"))
    throw new IllegalArgumentException("必须插入「正式/草稿标记」与「报告编号」字段");
   var footer=doc.createFooter(HeaderFooterType.DEFAULT);
   var p=footer.createParagraph();p.setAlignment(ParagraphAlignment.CENTER);
   p.createRun().setText("第 ");p.getCTP().addNewFldSimple().setInstr("PAGE");
   p.createRun().setText(" 页 / 共 ");p.getCTP().addNewFldSimple().setInstr("NUMPAGES");p.createRun().setText(" 页");
   doc.getProperties().getCustomProperties().addProperty(PROPERTY,json.writeValueAsString(spec));
   doc.write(out);
   if(out.size()>DocxGuard.MAX_UPLOAD)throw new IllegalArgumentException("DOCX 超过 5MB 限制");
   return out.toByteArray();
  }catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new IllegalStateException("生成可视化 Word 模板失败",e);}
 }
 private static void setCell(XWPFTableCell cell,String text,int size,boolean bold){
  cell.setText(text);
  for(var p:cell.getParagraphs())for(var r:p.getRuns()){r.setFontFamily("Microsoft YaHei");r.setFontSize(size);r.setBold(bold);}
 }
 private static String required(JsonNode node,String key,int max){
  var value=node.path(key);
  if(!value.isTextual()||value.asText().isBlank()||value.asText().length()>max)throw new IllegalArgumentException(key+" 不能为空或超过长度限制");
  return value.asText();
 }
}
