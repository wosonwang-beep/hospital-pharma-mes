package com.hospital.mes.reporting.application;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.springframework.stereotype.Component;
@Component
public class DocxGuard {
 public static final int MAX_UPLOAD=5*1024*1024;
 private static final int MAX_EXPANDED=40*1024*1024,MAX_PART=8*1024*1024;
 private static final String W="http://schemas.openxmlformats.org/wordprocessingml/2006/main";
 public Set<String> validate(byte[] bytes,Set<String> fields,Set<String> itemFields) {
  return validate(bytes,fields,itemFields,true,Map.of("items",itemFields));
 }
 public void validateStructure(byte[] bytes){validate(bytes,Set.of(),Set.of(),false,Map.of());}
 public Set<String> validate(byte[] bytes,Set<String> fields,Map<String,Set<String>> datasets){var items=new HashSet<String>();datasets.values().forEach(items::addAll);return validate(bytes,fields,items,true,datasets);}
 private Set<String> validate(byte[] bytes,Set<String> fields,Set<String> itemFields,boolean placeholders,Map<String,Set<String>> datasets){
  if(bytes.length==0||bytes.length>MAX_UPLOAD)throw invalid("DOCX 最大 5 MB");
  var tags=new HashSet<String>();var names=new HashSet<String>();int total=0;
  try(var zip=new ZipInputStream(new ByteArrayInputStream(bytes))){
   ZipEntry entry;byte[] buffer=new byte[8192];
   while((entry=zip.getNextEntry())!=null){
    String name=entry.getName(),lower=name.toLowerCase(Locale.ROOT);
    if(!names.add(name)||names.size()>1000||name.contains("..")||name.startsWith("/")||name.contains("\\"))throw invalid("无效 DOCX 包结构");
    if(lower.contains("vba")||lower.contains("macro")||lower.contains("embeddings/")||lower.contains("activex/")||lower.contains("altchunk")||lower.endsWith(".bin"))throw invalid("禁止宏、嵌入对象和活动内容");
    var part=new ByteArrayOutputStream();int size=0,n;
    while((n=zip.read(buffer))!=-1){size+=n;total+=n;if(size>MAX_PART||total>MAX_EXPANDED)throw invalid("DOCX 解压限额超出");part.write(buffer,0,n);}
    if(entry.getCompressedSize()>0&&size>1024*1024&&size/entry.getCompressedSize()>100)throw invalid("DOCX 压缩比异常");
    if(lower.endsWith(".xml")||lower.endsWith(".rels")){
     var factory=DocumentBuilderFactory.newInstance();factory.setNamespaceAware(true);
     factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
     factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
     factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
     factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
     var xml=factory.newDocumentBuilder().parse(new ByteArrayInputStream(part.toByteArray()));
     var all=xml.getElementsByTagName("*");
     for(int i=0;i<all.getLength();i++){
      var el=(Element)all.item(i);String local=el.getLocalName();
      if("External".equalsIgnoreCase(el.getAttribute("TargetMode")))throw invalid("禁止外部引用");
      if("Relationship".equals(local)&&(el.getAttribute("Target").matches("(?i)^[a-z][a-z0-9+.-]*:.*")||el.getAttribute("Target").startsWith("//")||el.getAttribute("Target").startsWith("\\")))throw invalid("禁止关系中的绝对外部URI");
      if(Set.of("altChunk","object","oleObject","attachedTemplate","dataBinding").contains(local))throw invalid("禁止活动或链接内容");
      if("Override".equals(local)&&el.getAttribute("ContentType").toLowerCase(Locale.ROOT).contains("macro"))throw invalid("禁止宏");
      if(placeholders&&"tag".equals(local)&&W.equals(el.getNamespaceURI())){String tag=el.getAttributeNS(W,"val");String placeholder=ContentControlFields.placeholder(tag,fields,itemFields,datasets);if(placeholder!=null)tags.add(tag.substring(tag.indexOf(':')+1));}
      if("fldSimple".equals(local))checkField(el.getAttributeNS(W,"instr"));
      if("instrText".equals(local))checkField(el.getTextContent());
     }
     if(placeholders)validateDatasetScopes(xml,datasets);
     var paragraphs=xml.getElementsByTagNameNS(W,"p");
     for(int i=0;placeholders&&i<paragraphs.getLength();i++){
      var texts=((Element)paragraphs.item(i)).getElementsByTagNameNS(W,"t");var joined=new StringBuilder();
      for(int j=0;j<texts.getLength();j++)joined.append(texts.item(j).getTextContent());
      String text=joined.toString();var match=Pattern.compile("\\{\\{([^{}]*)}}|\\[([A-Za-z][^\\[\\]]*)]").matcher(text);
      while(match.find()){String key=match.group(1)!=null?match.group(1):match.group(2);Set<String> allowed=match.group(1)!=null?fields:itemFields;if(!key.matches("[A-Za-z][A-Za-z0-9]*")||!allowed.contains(key))throw invalid("非法占位符: "+key);tags.add(key);}
      if(match.replaceAll("").contains("{{")||match.replaceAll("").contains("}}"))throw invalid("占位符未闭合");
     }
    } else if(lower.startsWith("word/media/")&&!lower.matches(".*\\.(png|jpe?g)$"))throw invalid("只允许 PNG/JPEG 图片");
   }
   if(!names.contains("word/document.xml")||!names.contains("[Content_Types].xml"))throw invalid("必须上传真实 DOCX");
  }catch(IllegalArgumentException e){throw e;}catch(Exception e){throw invalid("无效或不安全的 DOCX");}
  return Set.copyOf(tags);
 }
 private static void validateDatasetScopes(Document doc,Map<String,Set<String>> datasets){
  var tables=doc.getElementsByTagNameNS(W,"tbl");for(int i=0;i<tables.getLength();i++){var table=(Element)tables.item(i);var groups=new HashSet<String>();var used=new HashSet<String>();var text=new StringBuilder();var texts=table.getElementsByTagNameNS(W,"t");for(int t=0;t<texts.getLength();t++)text.append(texts.item(t).getTextContent());var placeholders=Pattern.compile("\\{\\{([A-Za-z][A-Za-z0-9]*)}}|\\[([A-Za-z][A-Za-z0-9]*)]").matcher(text);while(placeholders.find()){String group=placeholders.group(1),field=placeholders.group(2);if(group!=null&&datasets.containsKey(group))groups.add(group);if(field!=null)used.add(field);}var tags=table.getElementsByTagNameNS(W,"tag");for(int t=0;t<tags.getLength();t++){String tag=((Element)tags.item(t)).getAttributeNS(W,"val");if(tag.startsWith("mes-loop:"))groups.add(tag.substring(9));if(tag.startsWith("mes-table:"))groups.add(tag.substring(10));if(tag.startsWith("mes-item:"))used.add(tag.substring(9));}if(groups.size()>1)throw invalid("一个循环表只能绑定一个集合");if(!used.isEmpty()){if(groups.size()!=1)throw invalid("明细字段必须在声明集合的循环表中");String group=groups.iterator().next();if(!datasets.containsKey(group)||!datasets.get(group).containsAll(used))throw invalid("循环表包含其他集合字段");}}
 }
 private static void checkField(String field){if(!field.strip().matches("(?i)(PAGE|NUMPAGES)"))throw invalid("只允许页码域，禁止 DDE/链接/表达式域");}
 private static IllegalArgumentException invalid(String message){return new IllegalArgumentException(message);}
}
