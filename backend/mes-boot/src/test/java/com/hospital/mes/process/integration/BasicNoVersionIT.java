package com.hospital.mes.process.integration;

import com.fasterxml.jackson.databind.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.process.application.*;
import com.hospital.mes.ebr.application.EbrService;
import com.hospital.mes.ebr.domain.EbrCommands;
import static com.hospital.mes.process.domain.ProcessCommands.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Every fixture is unique and rolled back in the single native DEV database. */
@SpringBootTest @ActiveProfiles("ci") @Transactional
class BasicNoVersionIT {
 @Autowired com.hospital.mes.production.application.ProductionService production;
 @MockitoBean com.hospital.mes.ebr.application.EbrQueryService publishedEbr;
 @MockitoBean com.hospital.mes.production.application.ProductionMaterialPolicyService policies;
 @Autowired MasterMutation mutations;
 @Autowired UnitService units; @Autowired MaterialService materials; @Autowired ProcessService process;
 @Autowired ProcessQueryService query; @Autowired EbrService ebr; @Autowired JdbcTemplate db; @Autowired ObjectMapper json;
 @MockitoBean CurrentPlatformContextResolver contexts; @MockitoSpyBean AuditApplicationService audit;
 String suffix,unit,material; long actor;
 String key(){return UUID.randomUUID().toString();}
 @BeforeEach void setup(){
  suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);
  String login="basic_nv_"+suffix;
  db.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,'test-only',TRUE,FALSE)",login,login,"Rollback no-version fixture");
  actor=db.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,login);
  var permissions=new HashSet<String>();permissions.add("ebr:designer:edit");for(String prefix:List.of("master:uom:","master:material:","master:product:","process:package:","ebr:template:","production:order:","production:batch:","production:unit:","ebr:form:","mes:operation:"))for(String action:List.of("view","create","update","edit","publish","release"))permissions.add(prefix+action);
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of("ROLLBACK_TEST"),permissions,"test-session",key()));
  unit=units.create(new UnitCommands.Create("NVU"+suffix,"No-version unit","MASS",3),key()).path("id").asText();
  material=materials.create(json.convertValue(Map.of("materialCode","NVM"+suffix,"materialName","Rollback material","materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();
 }
 JsonNode packageRow(){var product=process.createProduct(new ProductCreate("NVP"+suffix,"Rollback product",null,null,unit),key());return process.createPackage(new PackageCreate(product.path("id").asText(),"NVPK"+suffix),key());}
 CurrentDefinitionSave definition(String qty){var f=new FormulaSave(null,null,"NVF"+suffix,"10",unit,List.of(new FormulaLine(1,material,qty,unit,"0",true)));var r=new RouteSave(null,null,"NVR"+suffix,List.of(new Operation("MIX","Mix",1,null,null,List.of(),null,false,List.of())));return new CurrentDefinitionSave(null,null,null,f,r);}
 @Test void basicWritesRequireNoVersionAndProduceNoAudit(){
  var updated=units.command(unit,new UnitCommands.Update("UPDATE",null,null,"Changed","MASS",3),null,key());
  assertThat(updated.has("versionNo")).isFalse();
  var stale=units.command(unit,new UnitCommands.Update("UPDATE",null,999L,"Last save","MASS",3),"\"998\"",key());
  assertThat(stale.path("unitName").asText()).isEqualTo("Last save");
  assertThat(db.queryForObject("SELECT version_no FROM md_unit WHERE id=?",Long.class,Long.parseLong(unit))).isZero();
  verify(audit,never()).append(any());
 }
 @Test void repeatedProcessSavesKeepOneCurrentDefinitionAndNoRevisions(){
  var row=packageRow();String id=row.path("id").asText();
  var first=process.saveCurrentDefinition(id,definition("2"),null,key());
  var frozen=query.requireCurrentIdentified(1,Long.parseLong(id)).deepCopy();
  var second=process.saveCurrentDefinition(id,definition("3"),"\"999\"",key());
  assertThat(first.path("currentDefinition").path("id")).isEqualTo(second.path("currentDefinition").path("id"));
  assertThat(second.has("versions")).isFalse();assertThat(second.has("selectedVersion")).isFalse();assertThat(second.has("versionNo")).isFalse();
  assertThat(db.queryForObject("SELECT COUNT(*) FROM proc_package_version WHERE package_id=?",Long.class,Long.parseLong(id))).isZero();
  assertThat(frozen.path("formula").path("items").get(0).path("requiredQty").asText()).isEqualTo("2");
  assertThat(query.requireCurrentIdentified(1,Long.parseLong(id)).path("formula").path("items").get(0).path("requiredQty").asText()).isEqualTo("3");
  verify(audit,never()).append(any());
 }
 @Test void newEbrBindingUsesPackageAndRetainsItsOwnAudit(){
  var row=packageRow();String id=row.path("id").asText();process.saveCurrentDefinition(id,definition("2"),null,key());
  var template=ebr.create(new EbrCommands.Create(id,"NVEBR"+suffix,"Test eBR template"),key());
  assertThat(template.path("processPackageId").asText()).isEqualTo(id);
  assertThat(template.path("packageVersionId").isNull()).isTrue();
  assertThat(template.path("operationChoices").size()).isEqualTo(1);
  verify(audit).append(any());
 }
 @Test void legacyRevisionWritesAreRejectedWithoutChangingEvidence(){
  long before=db.queryForObject("SELECT COUNT(*) FROM proc_package_version",Long.class);
  var row=packageRow();assertThatThrownBy(()->process.createVersion(row.path("id").asText(),new VersionCreate(null,null,null),null,key())).isInstanceOf(UnsupportedOperationException.class);
  assertThat(db.queryForObject("SELECT COUNT(*) FROM proc_package_version",Long.class)).isEqualTo(before);
 }
 @Test void batchReleaseFreezesCurrentContentAndKeepsProductionAuditAndConcurrency() throws Exception {
  var pkg=packageRow();String pid=pkg.path("id").asText();process.saveCurrentDefinition(pid,definition("2"),null,key());
  var template=ebr.create(new EbrCommands.Create(pid,"NVBATCH"+suffix,"Test eBR template"),key());String tid=template.path("id").asText();
  var published=json.createObjectNode().put("processPackageId",pid).put("templateVersionId",tid).put("templateCode","NVBATCH"+suffix).put("version",1).put("schemaVersion","1.0");
  var empty=published.putObject("definition");for(String field:List.of("sections","forms","rules","signatureRules","reviewRules"))empty.putArray(field);
  published.put("definitionHash",mutations.digest(published));
  // New rollback-only producer fixture; not a real approval or live acceptance.
  db.update("UPDATE ebr_template_version SET status='EFFECTIVE',content_hash=? WHERE id=? AND template_code=?",published.path("definitionHash").asText(),Long.parseLong(tid),"NVBATCH"+suffix);
  when(publishedEbr.requirePublished(1,Long.parseLong(tid))).thenReturn(published);
  when(policies.requirePolicy(anyLong(),anyLong(),anyLong())).thenReturn(json.createObjectNode().put("required",true).put("precision","0.001").put("tolerancePct","0.5").put("policyVersion","ROLLBACK_ONLY"));
  var order=production.createOrder(json.valueToTree(Map.of("orderNo","NVO"+suffix,"productId",pkg.path("productId").asText(),"plannedQty","10","unitId",unit,"plannedDate","2026-10-09","reason","Rollback test")),key());
  var batch=production.createBatch(json.valueToTree(Map.of("batchNo","NVB"+suffix,"productionOrderId",order.path("id").asText(),"productId",pkg.path("productId").asText(),"plannedQty","10","unitId",unit,"plannedDate","2026-10-09","reason","Rollback test")),key());
  var stale=json.valueToTree(Map.of("versionNo",999,"reason","Rollback test","processPackageId",pid,"ebrTemplateVersionId",tid));
  assertThatThrownBy(()->production.releaseBatch(batch.path("id").asText(),stale,null,key())).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  var request=json.valueToTree(Map.of("versionNo",batch.path("versionNo").asLong(),"reason","Rollback test","processPackageId",pid,"ebrTemplateVersionId",tid));
  var released=production.releaseBatch(batch.path("id").asText(),request,null,key());
  long snapshot=released.path("processSnapshotId").asLong();String before=db.queryForObject("SELECT snapshot_json FROM prd_process_snapshot WHERE id=?",String.class,snapshot);
  process.saveCurrentDefinition(pid,definition("4"),null,key());
  assertThat(db.queryForObject("SELECT snapshot_json FROM prd_process_snapshot WHERE id=?",String.class,snapshot)).isEqualTo(before);
  assertThat(json.readTree(before).path("process").path("formula").path("items").get(0).path("requiredQty").asText()).isEqualTo("2");
  assertThat(db.queryForObject("SELECT process_package_id FROM prd_process_snapshot WHERE id=?",Long.class,snapshot)).isEqualTo(Long.parseLong(pid));
  assertThat(db.queryForObject("SELECT package_version_id FROM prd_process_snapshot WHERE id=?",Long.class,snapshot)).isNull();
  verify(audit,atLeastOnce()).append(any());
 }

 // Synthetic archival rows are created only inside this rollback transaction; no signed history is edited.
 JsonNode archivalTemplate(String legacyCode){
  var p=packageRow();String pid=p.path("id").asText();process.saveCurrentDefinition(pid,definition("2"),null,key());
  long currentOp=Long.parseLong(query.currentOperations(1,Long.parseLong(pid)).getFirst().id());
  var t=ebr.create(new EbrCommands.Create(pid,"ARCHIVE"+suffix,"Test eBR template"),key());String tid=t.path("id").asText();
  var form=new EbrCommands.Form("FORM","Archived form",Long.toString(currentOp),"1.0",1,List.of());
  t=ebr.save(tid,new EbrCommands.Save(0L,"Rollback fixture",new EbrCommands.Definition(List.of(),List.of(form),List.of(),List.of(),List.of())),null,key());
  db.update("INSERT INTO proc_package_version(org_id,created_by,updated_by,package_id,version,status) VALUES(1,?,?,?,1,'DRAFT')",actor,actor,Long.parseLong(pid));
  long legacy=db.queryForObject("SELECT id FROM proc_package_version WHERE package_id=?",Long.class,Long.parseLong(pid));
  db.update("INSERT INTO proc_route_version(org_id,created_by,updated_by,package_version_id,route_code,version) VALUES(1,?,?,?,'SYNTHETIC_ARCHIVE',1)",actor,actor,legacy);
  long routeId=db.queryForObject("SELECT id FROM proc_route_version WHERE package_version_id=?",Long.class,legacy);
  db.update("INSERT INTO proc_operation_def(org_id,created_by,updated_by,route_version_id,operation_code,operation_name,sequence_no,predecessor_codes_json) VALUES(1,?,?,?,?, 'Archived operation',1,'[]')",actor,actor,routeId,legacyCode);
  long legacyOp=db.queryForObject("SELECT id FROM proc_operation_def WHERE route_version_id=?",Long.class,routeId);
  db.update("UPDATE ebr_form_def SET operation_def_id=? WHERE template_version_id=?",legacyOp,Long.parseLong(tid));
  db.update("UPDATE ebr_template_version SET process_package_id=NULL,package_version_id=?,status='EFFECTIVE',content_hash=REPEAT('a',64) WHERE id=?",legacy,Long.parseLong(tid));
  return ebr.get(tid);
 }
 @Test void copyingArchivalEbrCreatesCurrentBindingAndKeepsPredecessorUnchanged(){
  var source=archivalTemplate("MIX");String id=source.path("id").asText();var original=source.deepCopy();
  var child=ebr.version(id,new EbrCommands.VersionCreate(source.path("versionNo").asLong(),"Copy for current maintenance",true),null,key());
  assertThat(child.hasNonNull("processPackageId")).isTrue();assertThat(child.hasNonNull("packageVersionId")).isFalse();
  String current=query.currentOperations(1,child.path("processPackageId").asLong()).getFirst().id();
  assertThat(child.path("definition").path("forms").get(0).path("operationDefId").asText()).isEqualTo(current);
  assertThat((JsonNode)((com.fasterxml.jackson.databind.node.ObjectNode)ebr.get(id)).without("versions")).isEqualTo(((com.fasterxml.jackson.databind.node.ObjectNode)original).without("versions"));assertThat(child.path("version").asInt()).isEqualTo(2);
 }
 @Test void archivalCopyFailsClosedWhenItsOperationIsMissingFromCurrentRoute(){
  var source=archivalTemplate("REMOVED");var original=source.deepCopy();String id=source.path("id").asText();
  assertThatThrownBy(()->ebr.version(id,new EbrCommands.VersionCreate(source.path("versionNo").asLong(),"Current binding must be reviewed",true),null,key())).hasMessageContaining("Historical operation missing");
  assertThat(ebr.get(id)).isEqualTo(original);
 }
}
