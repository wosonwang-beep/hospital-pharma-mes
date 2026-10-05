package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.cos.*;
import java.io.*;
import java.util.*;
/** Server PDF from actual immutable source evidence. Embedded licensed Unicode font, never browser screenshots. */
@org.springframework.stereotype.Component
public class EbrPdfRenderer {
 private final ObjectMapper json;public EbrPdfRenderer(ObjectMapper json){this.json=json;}
 public byte[] render(JsonNode model,JsonNode source,String kind){try(var doc=new PDDocument();var fontStream=getClass().getResourceAsStream("/fonts/NotoSansSC-Regular.ttf")){if(fontStream==null)throw new IllegalStateException("Licensed Chinese font missing");var font=PDType0Font.load(doc,fontStream,true);String digest=model.path("recordDigest").asText();var id=new COSArray();byte[] digestBytes=java.util.HexFormat.of().parseHex(digest);id.add(new COSString(digestBytes));id.add(new COSString(digestBytes));doc.getDocument().setDocumentID(id);var info=doc.getDocumentInformation();info.setTitle("医院制剂电子批生产记录 eBR");info.setCreator("Hospital Pharmaceutical MES");info.setProducer("Apache PDFBox 3.0.8");var fixed=new GregorianCalendar(TimeZone.getTimeZone("UTC"));fixed.setTimeInMillis(0);info.setCreationDate(fixed);info.setModificationDate(fixed);List<String> lines=new ArrayList<>();lines.add("医院制剂电子批生产记录 eBR");lines.add(kind.equals("FINAL")?"正式归档 FINAL":"审核副本 REVIEW COPY — 不构成最终归档");lines.add("批次 / MainBatch: "+model.path("mainBatchId").asText());lines.add("定义摘要 / Definition SHA256: "+model.path("definitionHash").asText());lines.add("原始证据摘要 / Record SHA256: "+digest);lines.add("原始表单、修订与失效沿革、检验、物料平衡、偏差 CAPA、QA 决定及签名审计证据");
  // Sorted JSON is an unambiguous factual appendix; it preserves original FAIL and retest lineage.
  var sorted=json.copy().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,true);String evidence=sorted.writerWithDefaultPrettyPrinter().writeValueAsString(sort(source));lines.addAll(Arrays.asList(evidence.split("\\R")));try(var writer=new Pages(doc,font)){for(String line:lines)writer.write(line);}var out=new ByteArrayOutputStream();doc.save(out);return out.toByteArray();}catch(IOException e){throw new IllegalStateException("PDF generation failed",e);}}
 private JsonNode sort(JsonNode n){if(n.isObject()){var o=json.createObjectNode();var keys=new TreeSet<String>();n.fieldNames().forEachRemaining(keys::add);keys.forEach(k->o.set(k,sort(n.get(k))));return o;}if(n.isArray()){var a=json.createArrayNode();n.forEach(x->a.add(sort(x)));return a;}return n;}
 private static final class Pages implements AutoCloseable {
  private final PDDocument doc;private final PDType0Font font;private PDPageContentStream stream;private float y;private int page;
  Pages(PDDocument doc,PDType0Font font){this.doc=doc;this.font=font;}
  private void next()throws IOException{if(stream!=null)stream.close();var p=new PDPage(PDRectangle.A4);doc.addPage(p);stream=new PDPageContentStream(doc,p);y=PDRectangle.A4.getHeight()-40;page++;text("eBR — 医院制剂 / 第 "+page+" 页",40,y);y-=22;}
  void write(String raw)throws IOException{String line=raw.replace('\t',' ');StringBuilder part=new StringBuilder();float width=0;for(int offset=0;offset<line.length();){int cp=line.codePointAt(offset);offset+=Character.charCount(cp);if(Character.isISOControl(cp))continue;String ch=new String(Character.toChars(cp));try{float w=font.getStringWidth(ch)*8/1000;if(width+w>PDRectangle.A4.getWidth()-80&&!part.isEmpty()){emit(part.toString());part.setLength(0);width=0;}part.append(ch);width+=w;}catch(IllegalArgumentException missing){throw new IllegalStateException("Font cannot represent source character U+"+Integer.toHexString(cp),missing);}}emit(part.toString());}
  private void emit(String line)throws IOException{if(stream==null||y<40)next();text(line,40,y);y-=12;}
  private void text(String line,float x,float yy)throws IOException{stream.beginText();stream.setFont(font,8);stream.newLineAtOffset(x,yy);stream.showText(line);stream.endText();}
  public void close()throws IOException{if(stream!=null)stream.close();}
 }
}
