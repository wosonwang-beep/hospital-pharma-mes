package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.filespecification.*;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import java.io.*;
import java.security.*;
import java.util.*;

/** Business-readable eBR plus embedded original evidence; signatures remain those of the source. */
@org.springframework.stereotype.Component
public class EbrPdfRenderer {
 private final ObjectMapper json;
 private final BookPdfComposer books;
 @org.springframework.beans.factory.annotation.Autowired
 public EbrPdfRenderer(ObjectMapper json,BookPdfComposer books){this.json=json;this.books=books;}
 EbrPdfRenderer(ObjectMapper json){this(json,new BookPdfComposer());}
 private record Row(String left,String right){}
 private static Row line(String label,Object value){return new Row(label,value==null?"—":String.valueOf(value));}
 private static Row section(String label){return new Row(label,null);}
 private static String value(JsonNode n,String key){return n.path(key).isMissingNode()||n.path(key).isNull()?"—":n.path(key).asText();}
 private static String sha(byte[] input){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 public byte[] render(JsonNode model,JsonNode source,String kind){
  try{
   String digest=model.path("recordDigest").asText();
   if(!digest.matches("[0-9a-f]{64}"))throw new IllegalArgumentException("Invalid immutable source digest");
   byte[] raw=json.writerWithDefaultPrettyPrinter().writeValueAsBytes(sort(source));
   String evidenceHash=sha(raw);
   var parts=new ArrayList<BookPdfComposer.Part>();
   add(parts,"批次概览",overview(model));
   add(parts,"工序执行与产量",operations(model));
   for(JsonNode f:model.path("forms"))add(parts,"eBR "+f.path("form").path("formName").asText(),form(model,source,f));
   add(parts,"成品检验与独立复核",qc(model));
   add(parts,"成品入库与物料平衡",receiving(model));
   add(parts,"QA放行与归档追溯",qa(model,digest,evidenceHash));
   add(parts,"原始电子签名证据索引",signatures(model,source));
   var extra=topLevelSource(source);if(extra.size()>1)add(parts,"其他来源业务字段",extra);
   String product=model.path("batch").path("finishedProductSnapshot").path("productName").asText("医院制剂");
   var cover=List.of(product,"批次 "+value(model.path("batch"),"batchNo"),"QA状态 "+value(model.path("batch"),"status"),"证据SHA256: "+digest,"完整原始证据已嵌入source-evidence.json");
   return embed(books.compose(cover,parts,kind),raw,digest,kind,model.path("mainBatchId").asText());
  }catch(IOException ex){throw new IllegalStateException("Readable eBR PDF generation failed",ex);}
 }
 private void add(List<BookPdfComposer.Part> parts,String title,List<Row> rows)throws IOException{parts.add(new BookPdfComposer.Part(title,part(title,rows)));}
 private JsonNode sort(JsonNode n){if(n.isObject()){var obj=json.createObjectNode();var names=new TreeSet<String>();n.fieldNames().forEachRemaining(names::add);for(String name:names)obj.set(name,sort(n.get(name)));return obj;}if(n.isArray()){var arr=json.createArrayNode();n.forEach(x->arr.add(sort(x)));return arr;}return n;}
 private List<Row> overview(JsonNode model){var n=new ArrayList<Row>();JsonNode b=model.path("batch"),p=b.path("finishedProductSnapshot");n.add(section("批次基本资料"));for(String[] pair:new String[][]{{"产品",value(p,"productName")},{"产品规格",value(p,"specification")},{"批次",value(b,"batchNo")},{"批次状态",value(b,"status")},{"质量状态",value(b,"finishedQualityStatus")},{"库存状态",value(b,"finishedInventoryStatus")},{"生产日期",value(b,"finishedManufactureDate")},{"有效期至",value(b,"finishedExpiryDate")},{"生产计划数量",value(b,"plannedQty")},{"记录摘要",value(model,"recordDigest")},{"模板摘要",value(model,"definitionHash")}})n.add(line(pair[0],pair[1]));return n;}
 private List<Row> operations(JsonNode model){var n=new ArrayList<Row>();n.add(section("已执行工序"));for(JsonNode op:model.path("operations"))n.add(line("OP"+String.format("%02d",op.path("operationSeq").asInt()),value(op,"status")+" / 工序ID "+value(op,"id")+" / 操作人 "+value(op,"operatorId")));n.add(section("已签名成品产出/留样"));for(JsonNode ev:model.path("quantityEvents")){if(Set.of("OUTPUT","SAMPLE").contains(ev.path("eventType").asText()))n.add(line(value(ev,"eventType"),value(ev,"amount")+" / 单位ID "+value(ev,"unitId")+" / 事件 "+value(ev,"id")+" / 签名 "+value(ev,"signatureId")));}return n;}
 private static String readableJson(String raw,ObjectMapper json){if(raw==null||raw.isBlank())return "—";try{JsonNode v=json.readTree(raw);if(v.isValueNode())return v.asText();return v.toString();}catch(Exception ignored){return raw;}}
 private List<Row> form(JsonNode model,JsonNode source,JsonNode entry){
  var n=new ArrayList<Row>();JsonNode form=entry.path("form");String code=value(form,"formCode");
  n.add(section("表单与版本"));n.add(line("表单",value(form,"formName")+" / "+code));
  n.add(line("实例 / 工序",value(form,"id")+" / "+value(form,"operationExecutionId")));
  n.add(line("状态 / 修订",value(form,"status")+" / "+value(form,"formRevision")));
  var names=new HashMap<String,String>();
  for(JsonNode def:model.path("processSnapshot").path("snapshot").path("ebr").path("definition").path("forms")){
   if(!code.equals(def.path("formCode").asText()))continue;
   for(JsonNode f:def.path("fields"))names.put(f.path("fieldCode").asText(),f.path("label").asText());
  }
  n.add(section("字段记录 · 全部已保存修订"));
  for(JsonNode v:entry.path("values")){
   String key=value(v,"fieldCode");String title=names.getOrDefault(key,key);
   String origin=readableJson(v.path("normalizedValueJson").asText(),json);
   n.add(line(title+" / 修订"+value(v,"revisionNo"),origin));
   n.add(line("来源与时间",value(v,"sourceType")+" · "+value(v,"recordedAt")+" · 记录人"+value(v,"recordedBy")+" · 记录ID"+value(v,"id")+ (v.path("superseded").asBoolean()?" · 已取代":"")));
   if(v.hasNonNull("changeReason"))n.add(line("修订说明",value(v,"changeReason")));
  }
  n.addAll(formEvidence(source,entry));
  return n;
 }
 private List<Row> formEvidence(JsonNode source,JsonNode entry){
  var rows=new ArrayList<Row>();JsonNode f=entry.path("form");String id=f.path("id").asText();
  rows.add(section("签署与异人复核"));
  for(JsonNode sig:source.path("signatureRecords")){
   if("EBR_FORM_INSTANCE".equals(sig.path("objectType").asText())&&id.equals(sig.path("objectId").asText()))
    rows.add(line("生产签名",value(sig,"id")+" / 状态 "+value(sig,"status")+" / 签名人 "+value(sig,"signedBy")+" / 意义 "+value(sig,"meaning")));
  }
  for(JsonNode review:entry.path("reviews")){
   rows.add(line("独立复核",value(review,"decision")+" / 复核人 "+value(review,"reviewerId")+" / 签名 "+value(review,"signatureId")));
   rows.add(line("复核意见",value(review,"comment")));
   if(review.hasNonNull("invalidationReason"))rows.add(line("复核失效",value(review,"invalidationReason")));
  }
  for(JsonNode check:entry.path("ruleExecutions")){
   rows.add(line("CPP规则",value(check,"ruleCode")+" / "+value(check,"triggerPoint")+" / "+value(check,"result")));
  }
  return rows;
 }
 private List<Row> qc(JsonNode model){var n=new ArrayList<Row>();n.add(section("成品检验 · 原始结果与异人复核"));
  for(JsonNode test:model.path("qcTests")){
   JsonNode spec=test.path("specificationSnapshot");
   n.add(section(value(spec,"itemName")+" / 检验实例 "+value(test,"id")));
   n.add(line("标准与方法",value(spec,"methodCode")+" / "+value(spec,"methodVersion")));
   if("NUMERIC".equals(value(spec,"resultType")))n.add(line("数值限度",value(spec,"lowerLimit")+" 至 "+value(spec,"upperLimit")+" / 单位ID "+value(spec,"unitId")));
   else n.add(line("文本判定标准",value(spec,"textAcceptanceCriteria")));
   for(JsonNode result:test.path("results")){
    n.add(line("结果修订",value(result,"id")+" / 修订"+value(result,"revisionNo")+" / 判定"+value(result,"resultConclusion")));
    n.add(line("记录值",result.hasNonNull("resultNumeric")?value(result,"resultNumeric"):value(result,"resultText")));
    n.add(line("QC原签名",value(result,"signatureId")+" / 人员 "+value(result,"recordedBy")+" / "+value(result,"recordedAt")));
    JsonNode review=result.path("review");if(review.isObject()){
     n.add(line("独立复核",value(review,"disposition")+" / 签名"+value(review,"signatureId")+" / 复核人"+value(review,"reviewedBy")));
     n.add(line("复核说明",value(review,"reason")));
    }
   }
  }
  return n;
 }
 private List<Row> receiving(JsonNode model){var n=new ArrayList<Row>();
  n.add(section("成品入库与样品来源"));
  for(JsonNode receipt:model.path("finishedReceiving")){
   n.add(line("入库单",value(receipt,"requestNo")+" / "+value(receipt,"status")));
   n.add(line("收货数量",value(receipt,"quantity")+" / 单位ID "+value(receipt,"unitId")+" / 库位ID "+value(receipt,"locationId")));
   n.add(line("独立仓库签名",value(receipt,"signatureId")+" / 经办人 "+value(receipt,"confirmedBy")));
  }
  for(JsonNode req:model.path("finishedQuality").path("requests")){
   n.add(line("成品请验",value(req,"inspectionRequestNo")+" / "+value(req,"status")));
   n.add(line("检验依据",value(req,"qcSpecificationVersionId")+" / 接收人 "+value(req,"acceptedBy")));
   for(JsonNode sampling:req.path("samplingRecords"))n.add(line("取样与交接",value(sampling,"samplingNo")+" / 样品 "+value(sampling,"sampleId")+" / 签名 "+value(sampling,"signatureId")));
   for(JsonNode report:req.path("reports"))n.add(line("检验报告",value(report,"reportNo")+" / "+value(report,"status")+" / 签名 "+value(report,"signatureId")));
  }
  n.add(section("物料平衡（全部计算版本）"));
  for(JsonNode balance:model.path("balances")){
   n.add(line("计算版本",value(balance,"id")+" / "+value(balance,"calculationVersion")+" / "+value(balance,"status")));
   n.add(line("发料 / 投料",value(balance,"expectedValue")+" / "+value(balance,"actualValue")));
   n.add(line("差异 / 偏差率",value(balance,"differenceValue")+" / "+value(balance,"differencePct")));
   n.add(line("原始输入摘要",value(balance,"inputDigest")));
  }
  return n;
 }
 private List<Row> qa(JsonNode model,String digest,String appendixHash){var n=new ArrayList<Row>();
  n.add(section("QA放行及原始证据追溯"));
  for(JsonNode decision:model.path("decisions")){
   n.add(line("放行决定",value(decision,"decision")+" / 决定ID "+value(decision,"id")));
   n.add(line("审批人与签名",value(decision,"decisionBy")+" / "+value(decision,"signatureId")));
   n.add(line("审批说明",value(decision,"reason")));
   n.add(line("检验报告引用",value(decision,"finishedInspectionReportId")));
  }
  n.add(line("批次状态",value(model.path("batch"),"status")));
  n.add(line("签名来源摘要",digest));
  n.add(line("完整JSON附件SHA256",appendixHash));
  n.add(line("原始证据附件","source-evidence.json；包含所有原始工序、修订、签名、QC/QA及库存记录"));
  return n;
 }
 private List<Row> signatures(JsonNode model,JsonNode source){var n=new ArrayList<Row>();n.add(section("全部签名证据索引 · 原文存于PDF内嵌JSON"));
  for(JsonNode sig:source.path("signatureRecords")){
   n.add(line("签名ID "+value(sig,"id"),value(sig,"objectType")+" / 对象 "+value(sig,"objectId")+" / 状态 "+value(sig,"status")));
   if(sig.hasNonNull("recordDigest"))n.add(line("签名数据摘要",value(sig,"recordDigest")));
  }
  if(!source.path("signatureRecords").isArray()||source.path("signatureRecords").isEmpty()){
   for(JsonNode sig:model.path("signatures"))n.add(line("签名证据",value(sig,"objectType")+" / "+value(sig,"objectId")+" / 版本 "+value(sig,"recordVersion")));
  }
  return n;
 }
 private byte[] part(String title,List<Row> rows)throws IOException{
  try(var doc=new PDDocument();var stream=getClass().getResourceAsStream("/fonts/NotoSansSC-Regular.ttf")){
   if(stream==null)throw new IllegalStateException("Approved Unicode PDF font unavailable");
   PDType0Font font=PDType0Font.load(doc,stream,true);
   var id=new COSArray();byte[] digest=HexFormat.of().parseHex(sha(title.getBytes(java.nio.charset.StandardCharsets.UTF_8)));id.add(new COSString(digest));id.add(new COSString(digest));doc.getDocument().setDocumentID(id);
   var info=doc.getDocumentInformation();info.setTitle(title);info.setCreator("Hospital Pharmaceutical MES");fixDates(info);
   try(var page=new BusinessPages(doc,font,title)){for(Row row:rows)page.write(row);}
   var out=new ByteArrayOutputStream();doc.save(out);return out.toByteArray();
  }
 }
 private static void fixDates(PDDocumentInformation info){var zero=new GregorianCalendar(TimeZone.getTimeZone("UTC"));zero.setTimeInMillis(0);info.setCreationDate(zero);info.setModificationDate(zero);}
 private static final class BusinessPages implements AutoCloseable{
  private final PDDocument doc;private final PDType0Font font;private final String title;
  private PDPageContentStream pen;private float y;
  BusinessPages(PDDocument doc,PDType0Font font,String title){this.doc=doc;this.font=font;this.title=title;}
  private void page()throws IOException{
   if(pen!=null)pen.close();var sheet=new PDPage(PDRectangle.A4);doc.addPage(sheet);
   pen=new PDPageContentStream(doc,sheet);pen.setNonStrokingColor(13/255f,112/255f,130/255f);pen.addRect(0,PDRectangle.A4.getHeight()-11,PDRectangle.A4.getWidth(),11);pen.fill();
   writeText(title,46,PDRectangle.A4.getHeight()-55,15,17,41,58);y=PDRectangle.A4.getHeight()-84;
  }
  private void writeText(String value,float x,float at,int fontSize,int red,int green,int blue)throws IOException{
   pen.setNonStrokingColor(red/255f,green/255f,blue/255f);pen.beginText();pen.setFont(font,fontSize);pen.newLineAtOffset(x,at);pen.showText(value);pen.endText();
  }
  private List<String> wrap(String text,float limit,int size)throws IOException{
   var lines=new ArrayList<String>();var current=new StringBuilder();float width=0;
   if(text==null||text.isBlank())text="—";
   for(int pos=0;pos<text.length();){int cp=text.codePointAt(pos);pos+=Character.charCount(cp);
    if(cp=='\n'){lines.add(current.toString());current.setLength(0);width=0;continue;}
    if(Character.isISOControl(cp))continue;
    String glyph=new String(Character.toChars(cp));float advance=font.getStringWidth(glyph)*size/1000;
    if(width+advance>limit&&current.length()>0){lines.add(current.toString());current.setLength(0);width=0;}
    current.append(glyph);width+=advance;
   }
   lines.add(current.toString());return lines;
  }
  void write(Row row)throws IOException{
   if(pen==null)page();
   if(row.right()==null){if(y<105)page();writeText(row.left(),46,y,12,13,112,130);y-=23;return;}
   var left=wrap(row.left(),128,9);var right=wrap(row.right(),354,9);
   int count=Math.max(left.size(),right.size());
   for(int i=0;i<count;i++){
    if(y<65)page();
    if(i<left.size())writeText(left.get(i),48,y,9,92,111,132);
    if(i<right.size())writeText(right.get(i),184,y,9,24,46,65);
    y-=14;
   }
   y-=8;
  }
  public void close()throws IOException{if(pen!=null)pen.close();}
 }
 private List<Row> topLevelSource(JsonNode source){var rows=new ArrayList<Row>();rows.add(section("原始来源独立顶层值"));
  if(source.isObject()){var names=new TreeSet<String>();source.fieldNames().forEachRemaining(names::add);for(String key:names){JsonNode v=source.get(key);if(v.isValueNode())rows.add(line(key,v.asText()));}}
  return rows;
 } private byte[] embed(byte[] pdf,byte[] evidence,String digest,String kind,String batch)throws IOException{
  try(var doc=Loader.loadPDF(pdf)){
   var id=new COSArray();byte[] bytes=HexFormat.of().parseHex(digest);id.add(new COSString(bytes));id.add(new COSString(bytes));doc.getDocument().setDocumentID(id);
   var info=doc.getDocumentInformation();info.setTitle("医院制剂 eBR · "+batch+" · "+kind);info.setCreator("Hospital Pharmaceutical MES");info.setProducer("Apache PDFBox 3.0.8");fixDates(info);
   var spec=new PDComplexFileSpecification();spec.setFile("source-evidence.json");spec.setFileUnicode("source-evidence.json");
   var stream=new PDEmbeddedFile(doc,new ByteArrayInputStream(evidence));stream.setSubtype("application/json");stream.setSize(evidence.length);
   var zero=new GregorianCalendar(TimeZone.getTimeZone("UTC"));zero.setTimeInMillis(0);stream.setCreationDate(zero);stream.setModDate(zero);
   spec.setEmbeddedFile(stream);spec.setEmbeddedFileUnicode(stream);
   var embedded=new PDEmbeddedFilesNameTreeNode();embedded.setNames(Map.of("source-evidence.json",spec));
   var names=new PDDocumentNameDictionary(doc.getDocumentCatalog());names.setEmbeddedFiles(embedded);doc.getDocumentCatalog().setNames(names);
   var out=new ByteArrayOutputStream();doc.save(out);return out.toByteArray();
  }
 }
}
