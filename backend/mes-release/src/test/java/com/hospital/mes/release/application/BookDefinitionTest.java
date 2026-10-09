package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookDefinitionTest {
 private final ObjectMapper json=new ObjectMapper();

 private String legacy(){return """
 {"varietyCode":"SYNTHETIC","varietyName":"合成制剂品种","mappings":[{"productId":"1","packageVersionId":"2","ebrTemplateVersionId":"3"},{"productId":"4","packageVersionId":"5","ebrTemplateVersionId":"6"}],"entries":[{"code":"PREPARE","chapter":"过程记录","title":"配制记录","kind":"FORM","order":1,"required":true,"minCount":1,"scope":"OPERATION","operationCode":"PREPARE","formCode":"F_PREPARE","fields":["ACTUAL"],"attachmentIds":[]},{"code":"FILL","chapter":"过程记录","title":"分装记录","kind":"FORM","order":2,"required":true,"minCount":1,"scope":"BATCH","operationCode":"FILL","formCode":"F_FILL","fields":["ACTUAL"],"attachmentIds":[]}]}
 """;}

 private String modern(){return """
 {"templateName":"片剂批生产记录","processPackageId":"12","ebrTemplateVersionId":"30","entries":[
  {"code":"FORM_S30","flowType":"PROCESS","flowCode":"S30","flowName":"压片","title":"压片操作记录","kind":"FORM","order":1,"required":true,"minCount":1,"scope":"OPERATION","operationCode":"S30","formCode":"FORM_PRESS","fields":["ENV_TEMP"],"attachmentIds":[],"archiveStage":"PRODUCTION_REVIEW","applicability":"始终适用"},
  {"code":"QC_REPORT","flowType":"BUSINESS","flowCode":"QUALITY","flowName":"检验与质量记录","title":"检验报告","kind":"INSPECTION_REPORTS","order":2,"required":true,"minCount":1,"scope":"BATCH","operationCode":null,"formCode":null,"printTemplateVersionId":null,"fields":[],"attachmentIds":[],"archiveStage":"QA_REVIEW","applicability":"始终适用"}
 ]}
 """;}

 @Test void legacyProductMappingsRemainReadable()throws Exception{
  var d=BookDefinition.parse(json.readTree(legacy()),json);
  assertFalse(d.modern());
  assertEquals(2,d.mappings().size());
  assertEquals(java.util.Set.of("F_FILL"),d.batchFormCodes());
  assertEquals("合成制剂品种",d.displayName());
 }

 @Test void modernTemplateBindsProcessNotProduct()throws Exception{
  var d=BookDefinition.parse(json.readTree(modern()),json);
  assertTrue(d.modern());
  assertEquals("12",d.processPackageId());
  assertEquals("30",d.ebrTemplateVersionId());
  assertTrue(d.mappings().isEmpty());
  assertEquals("片剂批生产记录",d.displayName());
  assertEquals("压片",d.entries().get(0).directoryGroup());
 }

 @Test void modernFormRecordMustBelongToProductionOperation()throws Exception{
  var n=json.readTree(modern());
  ((com.fasterxml.jackson.databind.node.ObjectNode)n.path("entries").get(0)).put("flowType","BUSINESS");
  assertThrows(IllegalArgumentException.class,()->BookDefinition.parse(n,json));
 }

 @Test void modernTemplateRejectsProductBinding()throws Exception{
  var n=(com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(modern());
  n.put("productId","99");
  assertThrows(IllegalArgumentException.class,()->BookDefinition.parse(n,json));
 }

 @Test void unknownTopLevelCannotBecomeExecutableConfiguration()throws Exception{
  assertThrows(IllegalArgumentException.class,()->BookDefinition.parse(((com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(modern()).deepCopy()).put("sql","SELECT *"),json));
 }

 @Test void duplicateProductsAndFormCodesRejectedForLegacy()throws Exception{
  var n=json.readTree(legacy());
  ((com.fasterxml.jackson.databind.node.ObjectNode)n.path("mappings").get(1)).put("productId","1");
  assertThrows(IllegalArgumentException.class,()->BookDefinition.parse(n,json));
 }

 @Test void templateRequiresExplicitStableFormFields()throws Exception{
  var n=json.readTree(modern());
  ((com.fasterxml.jackson.databind.node.ObjectNode)n.path("entries").get(0)).putArray("fields");
  assertThrows(IllegalArgumentException.class,()->BookDefinition.parse(n,json));
 }

 @Test void arbitrarySourceRejected()throws Exception{
  var n=json.readTree(modern());
  ((com.fasterxml.jackson.databind.node.ObjectNode)n.path("entries").get(0)).put("kind","SQL");
  assertThrows(IllegalArgumentException.class,()->BookDefinition.parse(n,json));
 }
}
