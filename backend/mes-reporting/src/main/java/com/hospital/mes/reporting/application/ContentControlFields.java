package com.hospital.mes.reporting.application;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
/** Flatten MES content controls in the rendered derivative only. The stored source DOCX remains intact. */
public final class ContentControlFields {
 private static final String W="http://schemas.openxmlformats.org/wordprocessingml/2006/main";
 private ContentControlFields(){}
 public static String placeholder(String tag,Set<String> fields,Set<String> items){if(tag.startsWith("mes-field:")){String key=tag.substring(10);if(!fields.contains(key)||key.equals("items"))throw new IllegalArgumentException("未知字段控件");return "{{"+key+"}}";}if(tag.startsWith("mes-item:")){String key=tag.substring(9);if(!items.contains(key))throw new IllegalArgumentException("未知明细控件");return "["+key+"]";}if(tag.equals("mes-loop:items"))return "{{items}}";if(tag.equals("mes-table:items"))return null;if(tag.startsWith("mes-"))throw new IllegalArgumentException("未知MES控件");return null;}
 public static byte[] normalize(byte[] source,Set<String> fields,Set<String> items){try(var in=new ZipInputStream(new ByteArrayInputStream(source));var bytes=new ByteArrayOutputStream();var out=new ZipOutputStream(bytes)){ZipEntry e;while((e=in.getNextEntry())!=null){byte[] content=in.readAllBytes();if(e.getName().startsWith("word/")&&e.getName().endsWith(".xml")){
   var f=DocumentBuilderFactory.newInstance();f.setNamespaceAware(true);f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");var doc=f.newDocumentBuilder().parse(new ByteArrayInputStream(content));var found=doc.getElementsByTagNameNS(W,"sdt");var controls=new ArrayList<Element>();for(int i=0;i<found.getLength();i++)controls.add((Element)found.item(i));boolean changed=false;
   for(var control:controls){var props=control.getElementsByTagNameNS(W,"tag");if(props.getLength()==0)continue;String tag=((Element)props.item(0)).getAttributeNS(W,"val");String value=placeholder(tag,fields,items);if(value==null)continue;var c=control.getElementsByTagNameNS(W,"sdtContent");if(c.getLength()!=1)throw new IllegalArgumentException("字段控件结构无效");var text=((Element)c.item(0)).getElementsByTagNameNS(W,"t");if(text.getLength()==0)throw new IllegalArgumentException("字段控件缺少文本");for(int i=0;i<text.getLength();i++)text.item(i).setTextContent(i==0?value:"");Node parent=control.getParentNode(),contents=c.item(0);while(contents.hasChildNodes())parent.insertBefore(contents.getFirstChild(),control);parent.removeChild(control);changed=true;}
   if(changed){var tf=TransformerFactory.newInstance();tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,"");var rendered=new ByteArrayOutputStream();tf.newTransformer().transform(new DOMSource(doc),new StreamResult(rendered));content=rendered.toByteArray();}
  }out.putNextEntry(new ZipEntry(e.getName()));out.write(content);out.closeEntry();}out.finish();return bytes.toByteArray();}catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new IllegalArgumentException("字段控件映射失败",e);}}
}
