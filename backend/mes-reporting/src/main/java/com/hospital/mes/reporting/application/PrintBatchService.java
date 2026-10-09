package com.hospital.mes.reporting.application;

import com.hospital.mes.reporting.domain.PrintType;
import com.hospital.mes.reporting.infrastructure.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.common.exception.ComplianceException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.*;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.io.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
import java.io.ByteArrayOutputStream;
import com.hospital.mes.reporting.application.PrintDataProvider.PrintSnapshot;

/** Template-owned output shape. Never accepts browser-supplied business facts. */
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class PrintBatchService {
 private static final long MAX_PDF_BYTES=25L*1024*1024;
 private final PrintService printing;private final PrintTemplateVersionMapper templates;private final PrintBindingMapper bindings;
 private final ScopedStore<PrintBatch> store;private final MasterMutation mutations;private final ObjectMapper json;
 private final DocxRenderer renderer;private final PdfConverter converter;
 public PrintBatchService(PrintService printing,PrintTemplateVersionMapper templates,PrintBindingMapper bindings,PrintBatchMapper batches,MasterMutation mutations,ObjectMapper json,DocxRenderer renderer,PdfConverter converter){this.printing=printing;this.templates=templates;this.bindings=bindings;this.store=new ScopedStore<>(batches,List.of(),Map.of());this.mutations=mutations;this.json=json;this.renderer=renderer;this.converter=converter;}
 @Transactional public JsonNode generate(String type,List<String> recordIds,String templateId,boolean formal){
  var c=mutations.context("print:document:generate");
  if(recordIds==null||recordIds.isEmpty()||recordIds.size()>100)throw new IllegalArgumentException("请选择1至100条记录");
  var ids=recordIds.stream().map(id->Long.toString(MasterMutation.id(id))).toList();
  if(new HashSet<>(ids).size()!=ids.size())throw new IllegalArgumentException("选中记录不能重复");
  var template=new ScopedStore<>(templates,List.of(),Map.of()).get(c.organizationId(),MasterMutation.id(templateId));
  if(!"PUBLISHED".equals(template.getStatus())||!type.equals(template.getBusinessType())||bindings.selectCount(new QueryWrapper<PrintBinding>().eq("org_id",c.organizationId()).eq("business_type",type).eq("template_version_id",template.getId()).eq("enabled",true))!=1)throw new ComplianceException("PRINT_STATE_INVALID","请选择当前业务已发布并启用绑定的模板");
  if(!PrintService.hash(template.getDocx()).equals(template.getContentHash()))throw new ComplianceException("PRINT_ARCHIVE_CORRUPT","模板内容摘要不匹配");
  var kind=PrintType.parse(template.getPrintType());
  if(kind==PrintType.LIST&&formal)throw new IllegalArgumentException("列表汇总不作为正式签署单据，请使用单据模板");
  var provider=printing.provider(type);ids.forEach(provider::authorizeRead);
  var artifactIds=new ArrayList<String>();var sources=new LinkedHashMap<String,Object>();byte[] pdf;
  // All business locks use the same ascending order, while output retains the user's order.
  var ordered=ids.stream().sorted(Comparator.comparingLong(Long::parseLong)).toList();
  var snapshots=new HashMap<String,PrintSnapshot>();for(var id:ordered)snapshots.put(id,provider.load(id,formal));
  if(kind==PrintType.DOCUMENT){
   var artifacts=new HashMap<String,JsonNode>();for(var id:ordered)artifacts.put(id,printing.generate(type,id,templateId,formal));
   try(var out=new ByteArrayOutputStream()){
    var merger=new PDFMergerUtility();merger.setDestinationStream(out);
    var buffers=new ArrayList<RandomAccessReadBuffer>();
    try{long inputBytes=0;for(var id:ids){var artifact=artifacts.get(id);String artifactId=artifact.path("id").asText();artifactIds.add(artifactId);sources.put(id,artifact);byte[] bytes=printing.pdf(artifactId);inputBytes+=bytes.length;if(inputBytes>MAX_PDF_BYTES)throw new ComplianceException("PRINT_OUTPUT_TOO_LARGE","本次单据 PDF 合计超过25MB，请减少勾选记录");var buffer=new RandomAccessReadBuffer(bytes);buffers.add(buffer);merger.addSource(buffer);}merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());if(out.size()>MAX_PDF_BYTES)throw new ComplianceException("PRINT_OUTPUT_TOO_LARGE","本次输出超过25MB，请减少勾选记录");pdf=out.toByteArray();}finally{for(var buffer:buffers)buffer.close();}
   }catch(java.io.IOException e){throw new IllegalStateException("单据 PDF 合并失败",e);}
  }else{
   var records=new ArrayList<Map<String,Object>>();for(var id:ids){var snapshot=snapshots.get(id);records.add(snapshot.data());sources.put(id,Map.of("businessVersion",snapshot.businessVersion(),"reportNo",snapshot.reportNo(),"data",snapshot.data()));}
   template=new ScopedStore<>(templates,List.of(),Map.of()).lock(c.organizationId(),template.getId());
   if(!"PUBLISHED".equals(template.getStatus())||bindings.selectCount(new QueryWrapper<PrintBinding>().eq("org_id",c.organizationId()).eq("template_version_id",template.getId()).eq("enabled",true))!=1)throw new ComplianceException("PRINT_STATE_INVALID","模板已停用或解除绑定，请刷新");
   var schema=PrintSchema.of(provider,kind);var data=PrintSchema.listData(provider,records,"LIST-"+UUID.randomUUID());
   pdf=converter.convert(renderer.render(template.getDocx(),data,schema.fields(),schema.itemFields()));
  }
  var snapshot=Map.of("recordIds",ids,"sources",sources,"sourceArtifactIds",artifactIds,"templateVersionId",templateId,"templateContentHash",template.getContentHash(),"printType",kind.name(),"formal",formal);
  var row=new PrintBatch();row.setBusinessType(type);row.setTemplateVersionId(template.getId());row.setPrintType(kind.name());row.setFormal(formal);
  row.setRecordIdsJson(write(ids));row.setSourceArtifactIdsJson(write(artifactIds));row.setSnapshotJson(write(snapshot));row.setSnapshotHash(mutations.digest(snapshot));row.setPdf(pdf);row.setPdfHash(PrintService.hash(pdf));store.insert(row,c.organizationId(),c.actorId());
  var result=view(row,ids.size());mutations.auditSnapshot(c,"PRINT_BATCH_ARCHIVE","PrintBatch",row.getId(),null,result,"Selected records rendered using template print type",UUID.randomUUID().toString());return result;
 }
 public byte[] pdf(String id){
  var c=mutations.context("print:document:generate");var row=store.get(c.organizationId(),MasterMutation.id(id));
  try{var snapshot=json.readTree(row.getSnapshotJson());if(!mutations.digest(snapshot).equals(row.getSnapshotHash())||!json.readTree(row.getRecordIdsJson()).equals(snapshot.path("recordIds"))||!json.readTree(row.getSourceArtifactIdsJson()).equals(snapshot.path("sourceArtifactIds")))throw new ComplianceException("PRINT_ARCHIVE_CORRUPT","批量打印快照摘要不匹配");for(var record:snapshot.path("recordIds"))printing.provider(row.getBusinessType()).authorizeRead(record.asText());}catch(java.io.IOException e){throw new ComplianceException("PRINT_ARCHIVE_CORRUPT","批量打印快照不可读");}
  if(!PrintService.hash(row.getPdf()).equals(row.getPdfHash()))throw new ComplianceException("PRINT_ARCHIVE_CORRUPT","批量打印 PDF 摘要不匹配");return row.getPdf().clone();
 }
 private JsonNode view(PrintBatch row,int count){return json.createObjectNode().put("id",row.getId().toString()).put("businessType",row.getBusinessType()).put("templateVersionId",row.getTemplateVersionId().toString()).put("printType",row.getPrintType()).put("recordCount",count).put("formal",row.getFormal()).put("snapshotHash",row.getSnapshotHash()).put("pdfHash",row.getPdfHash());}
 private String write(Object value){try{return json.writeValueAsString(value);}catch(java.io.IOException e){throw new IllegalStateException(e);}}
}
