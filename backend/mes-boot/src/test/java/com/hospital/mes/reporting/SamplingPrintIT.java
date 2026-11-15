package com.hospital.mes.reporting;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.common.exception.ComplianceException;
import com.hospital.mes.common.exception.PermissionException;
import com.hospital.mes.incoming.IncomingQualityFixture;
import com.hospital.mes.reporting.application.NativePrintDesigner;
import com.hospital.mes.reporting.application.PrintDataProvider;
import com.hospital.mes.reporting.application.PrintDataSourceService;
import com.hospital.mes.reporting.application.PrintService;
import com.hospital.mes.system.api.SystemDictionaryController;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/** Real MariaDB source lifecycle plus DOCX rendering; inherited transaction rolls back. */
class SamplingPrintIT extends IncomingQualityFixture {
 @Autowired PrintService printing;
 @Autowired PrintDataSourceService sources;
 @Autowired NativePrintDesigner designer;
 @Autowired SystemDictionaryController dictionaries;

 @Test @WithMockUser(authorities="iam:dict:update") void registeredProjectionValidatesPublishesBindsAndRendersAnExplicitPlannedSamplingPdf()throws Exception{
  rights();ensureBusinessType();var generated=createGenerated();String samplingId=id(generated.path("samplingTask"));PrintDataProvider provider=printing.provider("SAMPLING_RECORD");publishSource(provider,samplingId);
  var design=json.readTree("""
   {"blocks":[
    {"id":"heading","type":"TITLE","text":"来料取样单","fontSize":20,"align":"CENTER"},
    {"id":"mode","type":"FIELD","fieldKey":"modeLabel","fontSize":11,"align":"CENTER"},
    {"id":"number","type":"FIELD","fieldKey":"reportNo","fontSize":11,"align":"CENTER"},
    {"id":"request","type":"FIELD","fieldKey":"requestNo","fontSize":11,"align":"LEFT"},
    {"id":"material","type":"FIELD","fieldKey":"materialName","fontSize":11,"align":"LEFT"},
    {"id":"lot","type":"FIELD","fieldKey":"lotNo","fontSize":11,"align":"LEFT"},
    {"id":"batch","type":"FIELD","fieldKey":"batchQuantity","fontSize":11,"align":"LEFT"},
    {"id":"plan","type":"FIELD","fieldKey":"plannedSamplingQuantity","fontSize":11,"align":"LEFT"},
    {"id":"status","type":"FIELD","fieldKey":"samplingStatus","fontSize":11,"align":"LEFT"},
    {"id":"rows","type":"TABLE","datasetKey":"items","columns":["sequence","containerNo","location","actualQuantity","unit","packageResealed","detailSampledAt","detailSampledBy"],"fontSize":9,"align":"LEFT"}
   ]}
   """);
  byte[] docx=designer.generate(design,provider.fieldDefinitions());assertThat(docx).isNotEmpty();
  var template=printing.upload("SAMPLING_RECORD_DEFAULT","来料取样单默认模板","SAMPLING_RECORD","DOCUMENT",docx,"构造样张模板，非真实业务事实");template=printing.validate(template.path("id").asText(),template.path("versionNo").asLong(),"校验取样字段和版式");template=printing.publish(template.path("id").asText(),template.path("versionNo").asLong(),"发布受控取样模板");printing.bind(template.path("id").asText(),"SAMPLING_RECORD",true,"绑定取样业务类型");
  String templateId=template.path("id").asText();

  assertThat(provider.load(samplingId,false).data()).containsEntry("modeLabel","执行前取样计划（未取样）").containsEntry("samplingStatus","未取样");
  byte[] pdf=printing.generate("SAMPLING_RECORD",samplingId,templateId,false);

  try(var document=Loader.loadPDF(pdf)){String text=new PDFTextStripper().getText(document);assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);assertThat(text).contains("SM-","IR-");}
  permissions.remove("qms:sampling:view");as(analyst);assertThatThrownBy(()->printing.generate("SAMPLING_RECORD",samplingId,templateId,false)).isInstanceOf(PermissionException.class).hasMessageContaining("Permission required");
  permissions.add("qms:sampling:view");when(contexts.current()).thenReturn(new CurrentPlatformContext(2,analyst,Set.of("QC"),permissions,"sampling-print-org","sampling-print-org"));assertThatThrownBy(()->provider.load(samplingId,false)).isInstanceOf(NoSuchElementException.class);
 }

 private JsonNode createGenerated(){var source=jdbc.queryForMap("SELECT CAST(ri.receipt_id AS CHAR) receipt_id,CAST(ri.id AS CHAR) item_id FROM wms_material_receipt_item ri JOIN md_material_lot ml ON ml.receipt_item_id=ri.id WHERE ml.id=?",lot);var request=service.createInitialRequestFromReceipt((String)source.get("receipt_id"),(String)source.get("item_id"),key());return service.confirmAndGenerate(id(request),body("qcSpecificationVersionId",version,"plannedSamplingQuantity","0.125","plannedSamplingUnitId",unit,"requiredPackageCount",1,"samplingPlan","按批准方案抽取一件","reason","确认首次请验计划"),token(request),key());}
 private void rights(){permissions.addAll(Set.of("qms:sampling:view","print:source:view","print:source:manage","print:source:publish","print:template:view","print:template:manage","print:template:publish","print:document:generate","iam:dict:update"));as(analyst);}
 private void ensureBusinessType(){Long type=jdbc.queryForObject("SELECT id FROM sys_dict_type WHERE org_id=1 AND dict_code='PRINT_BUSINESS_DOCUMENT'",Long.class);Long parent=jdbc.queryForObject("SELECT id FROM sys_dict_item WHERE org_id=1 AND dict_type_id=? AND item_code='QUALITY'",Long.class,type);dictionaries.createItem(Long.toString(type),UUID.randomUUID().toString(),new SystemDictionaryController.ItemRequest("SAMPLING_RECORD","来料取样单","来料取样记录统一打印",Long.toString(parent),25,"ACTIVE",false));}
 private void publishSource(PrintDataProvider provider,String samplingId){
  JsonNode old=null;try{old=sources.getMetadata(provider.businessType());}catch(ComplianceException|NoSuchElementException ignored){}
  long expected=old==null?0:old.path("versionNo").asLong();var metadata=json.createObjectNode().put("expectedVersion",expected).put("authorizationPolicyRef","provider."+provider.businessType()).put("reason","取样打印源事务回滚验证");metadata.putArray("compatibleMainCatalogIds").add("provider."+provider.businessType());metadata.putArray("fixedTypeFilters");sources.putMetadata(provider.businessType(),metadata,Long.toString(expected),UUID.randomUUID().toString());
  var create=json.createObjectNode().put("sourceCode","SR"+suffix).put("sourceName","取样记录受控投影").put("reason","取样打印源事务回滚验证");create.putArray("businessTypes").add(provider.businessType());create.set("configuration",configuration(provider));var source=sources.save(null,create,null,UUID.randomUUID().toString());String sourceId=source.path("id").asText();var validation=json.createObjectNode().put("expectedVersion",source.path("versionNo").asLong()).put("reason","读取当前真实取样计划");validation.putArray("samples").addObject().put("businessType",provider.businessType()).put("recordId",samplingId);var proof=sources.validate(sourceId,validation,source.path("versionNo").asText(),UUID.randomUUID().toString());assertThat(proof.path("result").asText()).isEqualTo("PASS");var publish=json.createObjectNode().put("expectedVersion",source.path("versionNo").asLong()).put("revisionId",source.path("draft").path("id").asText()).put("configHash",source.path("draft").path("configHash").asText()).put("validationId",proof.path("id").asText()).put("reason","发布取样记录受控投影");sources.publish(sourceId,publish,source.path("versionNo").asText(),UUID.randomUUID().toString());
 }
 private ObjectNode configuration(PrintDataProvider provider){var n=json.createObjectNode();n.put("schemaVersion",1).put("sourceName","取样记录受控投影").put("mainCatalogId","provider."+provider.businessType());n.putArray("parameterDeclarations").addObject().put("name","recordId").put("source","DOCUMENT_ID").putNull("contextKey");var join=n.putArray("joins").addObject();join.put("nodeId","lines").put("label","取样明细").put("parentNodeId","main").put("catalogId","provider."+provider.businessType()+".items").put("knownRelationId","provider."+provider.businessType()+".itemsRelation").put("cardinality","1:N").put("datasetKey","items").put("displayOrder",0);join.putArray("keys").addObject().put("parentFieldId","id").put("targetFieldId","id");var fields=n.putArray("fields");int order=0;for(var field:provider.fieldDefinitions()){var item=fields.addObject();item.put("fieldId","F"+field.key()).put("fieldKey",field.key()).put("label",field.label()).put("dataType",field.dataType()).put("displayOrder",order++).put("dataGroup",field.repeated()?"items":"MAIN").put("missing","EMPTY");if(field.repeated())item.put("datasetKey","items");else item.putNull("datasetKey");item.putObject("sourcePath").put("nodeId",field.repeated()?"lines":"main").put("catalogFieldId",field.key());}return n;}
}
