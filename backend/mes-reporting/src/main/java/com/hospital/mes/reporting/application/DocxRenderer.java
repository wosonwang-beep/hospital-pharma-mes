package com.hospital.mes.reporting.application;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import java.io.*;
import java.util.*;
import org.springframework.stereotype.Component;
@Component
public class DocxRenderer {
 private final DocxGuard guard;
 public DocxRenderer(DocxGuard guard){this.guard=guard;}
 public byte[] render(byte[] template,Map<String,Object> data,Set<String> fields,Set<String> itemFields){
  guard.validate(template,fields,itemFields);
  template=ContentControlFields.normalize(template,fields,itemFields);
  var config=Configure.builder().useDefaultEL(true).bind("items",new LoopRowTableRenderPolicy(true)).build();
  try(var rendered=XWPFTemplate.compile(new ByteArrayInputStream(template),config).render(data);var out=new ByteArrayOutputStream()){
   rendered.write(out);return out.toByteArray();
  }catch(Exception e){throw new IllegalArgumentException("模板渲染失败",e);}
 }
}
