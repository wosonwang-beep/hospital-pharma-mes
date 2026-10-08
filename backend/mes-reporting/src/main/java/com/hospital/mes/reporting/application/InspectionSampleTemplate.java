package com.hospital.mes.reporting.application;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
/** A non-business fixture; callers must never substitute example data for a business snapshot. */
public final class InspectionSampleTemplate {
 private InspectionSampleTemplate(){}
 public static List<PrintField> dictionary(){return List.of(
 new PrintField("reportNo","报告编号","报告信息","系统检验报告编号","R-20261008",false),
 new PrintField("businessVersion","业务版本","报告信息","归档所用报告版本","3",false),
 new PrintField("modeLabel","正式/草稿标记","报告信息","必需标记；由后端审批状态控制","草稿 · 非正式报告",false),
 new PrintField("requestNo","请验单号","报告信息","报告关联请验单","REQ-001",false),
 new PrintField("overallResult","综合结论","报告信息","保留系统原始结论","PASS",false),
 new PrintField("materialName","物料名称","物料信息","来源为批次物料快照","药用辅料",false),
 new PrintField("lotNo","物料批号","物料信息","报告来源批号","LOT-001",false),
 new PrintField("inspectors","检验人","签署信息","实际执行人员；不生成签名图片","检验员（ID 20）",false),
 new PrintField("approvedBy","审批人","签署信息","正式件采用已校验审批证据","审批人（ID 21）",false),
 new PrintField("approvedAt","审批时间","签署信息","记录内审批时间","2026-10-08T09:00:00Z",false),
 new PrintField("signatureReference","电子签名证据编号","签署信息","引用系统证据，不伪造手写签名","101",false),
 new PrintField("sequence","序号","检验项目","循环明细序号","1",true),
 new PrintField("itemName","检验项目名称","检验项目","报告完整检验项目","外观",true),
 new PrintField("criteria","标准要求","检验项目","检验项目受控标准","符合规定",true),
 new PrintField("result","检验结果","检验项目","服务端选用结果及单位","99.5 %",true),
 new PrintField("originalResult","原始结果","检验项目","保留原始结果和结论","99.0 / FAIL",true),
 new PrintField("conclusion","项目结论","检验项目","选用结果结论","PASS",true),
 new PrintField("recordedBy","项目检验人","检验项目","实际执行人员","检验员",true),
 new PrintField("recordedAt","结果记录时间","检验项目","服务端结果记录时间","2026-10-08T08:30:00Z",true));}
 public static Set<String> fields(){return Set.of("reportNo","businessVersion","modeLabel","requestNo","materialName","lotNo","overallResult","approvedBy","approvedAt","signatureReference","inspectors","items");}
 public static Set<String> itemFields(){return Set.of("sequence","itemName","criteria","result","conclusion","recordedBy","recordedAt","originalResult");}
 public static byte[] create(){try(var d=new XWPFDocument();var out=new ByteArrayOutputStream()){
  var sect=d.getDocument().getBody().addNewSectPr();var size=sect.addNewPgSz();size.setW(BigInteger.valueOf(11906));size.setH(BigInteger.valueOf(16838));var margins=sect.addNewPgMar();margins.setTop(BigInteger.valueOf(1134));margins.setBottom(BigInteger.valueOf(1134));margins.setLeft(BigInteger.valueOf(1134));margins.setRight(BigInteger.valueOf(1134));
  var title=d.createParagraph();title.setAlignment(ParagraphAlignment.CENTER);var tr=title.createRun();tr.setBold(true);tr.setFontSize(20);tr.setFontFamily("Microsoft YaHei");tr.setText("药品检验报告");
  paragraph(d,"{{modeLabel}} · 报告编号 {{reportNo}} · 业务版本 {{businessVersion}}").setAlignment(ParagraphAlignment.CENTER);
  paragraph(d,"请验单号：{{requestNo}}");paragraph(d,"物料名称：{{materialName}}    批号：{{lotNo}}");
  var table=d.createTable(2,6);table.setWidth("100%");int[] widths={520,1350,2900,1650,700,2518};var grid=table.getCTTbl().getTblGrid();if(grid==null)grid=table.getCTTbl().addNewTblGrid();while(grid.sizeOfGridColArray()>0)grid.removeGridCol(0);for(int width:widths)grid.addNewGridCol().setW(BigInteger.valueOf(width));String[] titles={"序号","检验项目","标准/要求","检验结果","结论","检验人/日期"};
  String[] detail={"{{items}}[sequence]","[itemName]","[criteria]","[result]（原始：[originalResult]）","[conclusion]","[recordedBy] / [recordedAt]"};
  table.getRow(0).setRepeatHeader(true);
  for(int row=0;row<2;row++)for(int col=0;col<6;col++){var cell=table.getRow(row).getCell(col);cell.setWidth(""+widths[col]);cell.setText(row==0?titles[col]:detail[col]);if(row==0)cell.setColor("EAF0F8");for(var p:cell.getParagraphs())for(var r:p.getRuns()){r.setFontFamily("Microsoft YaHei");r.setFontSize(10);if(row==0)r.setBold(true);}}
  for(var row:table.getRows())row.setCantSplitRow(true);
  var result=paragraph(d,"综合结论：{{overallResult}}");result.setKeepNext(true);
  paragraph(d,"检验人：{{inspectors}}").setKeepNext(true);
  paragraph(d,"审批人：{{approvedBy}}    审批时间：{{approvedAt}}").setKeepNext(true);
  paragraph(d,"电子签名证据编号：{{signatureReference}}（以系统签名证据为准）");
  var footer=d.createFooter(HeaderFooterType.DEFAULT);var fp=footer.createParagraph();fp.setAlignment(ParagraphAlignment.CENTER);fp.createRun().setText("第 ");fp.getCTP().addNewFldSimple().setInstr("PAGE");fp.createRun().setText(" 页 / 共 ");fp.getCTP().addNewFldSimple().setInstr("NUMPAGES");fp.createRun().setText(" 页");for(var run:fp.getRuns()){run.setFontFamily("Microsoft YaHei");run.setFontSize(9);}
  d.write(out);return out.toByteArray();
 }catch(IOException e){throw new IllegalStateException(e);}}
 private static XWPFParagraph paragraph(XWPFDocument d,String text){var p=d.createParagraph();p.setSpacingAfter(120);var r=p.createRun();r.setFontFamily("Microsoft YaHei");r.setFontSize(11);r.setText(text);return p;}
 public static Map<String,Object> example(int count){var data=new LinkedHashMap<String,Object>();for(String field:fields())data.put(field,"示例");data.put("modeLabel","模板验证样张 · 非业务文件");data.put("reportNo","DEMO-20261008");data.put("businessVersion","0");data.put("materialName","药用辅料中文长名称示例");data.put("approvedBy","未审批");data.put("approvedAt","—");data.put("signatureReference","无（示例不含真实签名）");var items=new ArrayList<Map<String,String>>();for(int i=1;i<=count;i++)items.add(Map.of("sequence",""+i,"itemName","检验项目"+i,"criteria","符合受控检验标准规定；中文长文本分页验证。".repeat(3),"result","符合规定","conclusion","合格","recordedBy","示例检验员","recordedAt","2026-10-08","originalResult","符合规定"));data.put("items",items);return data;}
}
