package com.hospital.mes.process.integration;

import com.fasterxml.jackson.databind.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.ebr.application.*;
import com.hospital.mes.ebr.domain.EbrCommands;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.process.application.*;
import com.hospital.mes.production.application.*;
import static com.hospital.mes.process.domain.ProcessCommands.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class ProductionPrescriptionIT {
 @Autowired ProcessService process;
 @Autowired ProcessQueryService processQuery;
 @Autowired PrescriptionService prescriptions;
 @Autowired ProductionService production;
 @Autowired ProductionQueryService productionQuery;
 @Autowired UnitService units;
 @Autowired MaterialService materials;
 @Autowired EbrService ebr;
 @Autowired MasterMutation mutations;
 @Autowired JdbcTemplate jdbc;
 @Autowired ObjectMapper json;
 @MockitoBean CurrentPlatformContextResolver contexts;
 @MockitoBean EbrQueryService publishedEbr;
 @MockitoBean ProductionMaterialPolicyService policies;

 String suffix,unit,material,product,processId; long actor; Set<String> permissions;
 String key(){return UUID.randomUUID().toString();}
 @BeforeEach void setup(){
  suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);
  String login="rx_"+suffix;
  jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,'test-only',TRUE,FALSE)",login,login,"Prescription rollback fixture");
  actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,login);
  permissions=new HashSet<>();
  for(String prefix:List.of("master:uom:","master:material:","master:product:","process:package:","production:prescription:","production:order:","production:batch:","ebr:template:","ebr:form:","mes:operation:"))
   for(String action:List.of("view","create","update","edit","activate","publish","release"))permissions.add(prefix+action);
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of("PRESCRIPTION_TEST"),permissions,"test-session",key()));
  unit=units.create(new UnitCommands.Create("RXU"+suffix,"Prescription unit","MASS",3),key()).path("id").asText();
  material=materials.create(json.convertValue(Map.of("materialCode","RXM"+suffix,"materialName","Prescription material","materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();
  product=process.createProduct(new ProductCreate("RXP"+suffix,"Prescription product",null,null,unit),key()).path("id").asText();
  var pkg=process.createPackage(new PackageCreate(null,"RXP-PROC-"+suffix),key());
  processId=pkg.path("id").asText();
  var route=new RouteSave(null,null,"RXR"+suffix,List.of(new Operation("MIX","Mix",1,null,null,List.of(),null,false,List.of())));
  process.saveCurrentDefinition(processId,new CurrentDefinitionSave(null,null,null,null,route),null,key());
 }
 PrescriptionCreate command(String code,String qty){
  return new PrescriptionCreate(code,"Prescription "+code,product,processId,"10",unit,List.of(new PrescriptionItem(1,material,qty,unit,"0",true)));
 }
 JsonNode activate(JsonNode draft){
  return prescriptions.activate(draft.path("id").asText(),new PrescriptionAction(draft.path("versionNo").asLong(),"Activate prescription"),null,key());
 }
 @Test void genericProcessAndSingleActivePrescriptionAreEnforced(){
  var current=processQuery.requireCurrentIdentified(1,Long.parseLong(processId));
  assertThat(current.hasNonNull("productId")).isFalse();
  assertThat(current.hasNonNull("formulaId")).isFalse();
  assertThat(current.path("route").path("operations")).hasSize(1);

  var first=prescriptions.create(command("RXA"+suffix,"2"),key());
  var active=activate(first);
  assertThat(active.path("status").asText()).isEqualTo("ACTIVE");
  var frozen=prescriptions.requireActive(1,Long.parseLong(product));
  assertThat(frozen.path("processPackageId").asText()).isEqualTo(processId);
  assertThat(frozen.path("items").get(0).path("formulaItemId").asLong()).isPositive();
  assertThat(frozen.path("items").get(0).path("requiredQty").asText()).isEqualTo("2");

  var second=prescriptions.create(command("RXB"+suffix,"3"),key());
  assertThatThrownBy(()->activate(second)).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class)
   .hasMessageContaining("active prescription");
 }
 @Test void batchReleaseFreezesPrescriptionAndProcessWithoutLegacyFormula(){
  var draft=prescriptions.create(command("RXF"+suffix,"2"),key());
  var active=activate(draft);
  var template=ebr.create(new EbrCommands.Create(processId,"RXEBR"+suffix,"Prescription process form"),key());
  String templateId=template.path("id").asText();
  var published=json.createObjectNode().put("processPackageId",processId).put("templateVersionId",templateId).put("templateCode","RXEBR"+suffix).put("version",1).put("schemaVersion","1.0");
  var definition=published.putObject("definition");for(String field:List.of("sections","forms","rules","signatureRules","reviewRules"))definition.putArray(field);
  published.put("definitionHash",mutations.digest(published));
  jdbc.update("UPDATE ebr_template_version SET status='EFFECTIVE',content_hash=? WHERE id=?",published.path("definitionHash").asText(),Long.parseLong(templateId));
  when(publishedEbr.requirePublished(1,Long.parseLong(templateId))).thenReturn(published);
  when(policies.requirePolicy(anyLong(),anyLong(),anyLong())).thenReturn(json.createObjectNode().put("required",true).put("precision","0.001").put("tolerancePct","0.5").put("policyVersion","PRESCRIPTION_TEST"));

  var order=production.createOrder(json.valueToTree(Map.of("orderNo","RXO"+suffix,"productId",product,"plannedQty","10","unitId",unit,"plannedDate","2026-10-09","reason","Prescription test")),key());
  var batch=production.createBatch(json.valueToTree(Map.of("batchNo","RXB"+suffix,"productionOrderId",order.path("id").asText(),"productId",product,"plannedQty","10","unitId",unit,"plannedDate","2026-10-09","reason","Prescription test")),key());
  var request=json.valueToTree(Map.of("versionNo",batch.path("versionNo").asLong(),"reason","Prescription test","prescriptionId",active.path("id").asText(),"ebrTemplateVersionId",templateId));
  var released=production.releaseBatch(batch.path("id").asText(),request,null,key());

  long snapshotId=released.path("processSnapshotId").asLong();
  var snapshot=productionQuery.batch(1,Long.parseLong(batch.path("id").asText())).snapshot();
  assertThat(snapshot.path("prescription").path("prescriptionId").asText()).isEqualTo(active.path("id").asText());
  assertThat(snapshot.path("prescription").path("items").get(0).path("requiredQty").asText()).isEqualTo("2");
  assertThat(snapshot.path("process").hasNonNull("formulaId")).isFalse();
  assertThat(jdbc.queryForObject("SELECT prescription_id FROM prd_process_snapshot WHERE id=?",Long.class,snapshotId)).isEqualTo(active.path("id").asLong());
  assertThat(jdbc.queryForObject("SELECT formula_version_id FROM prd_process_snapshot WHERE id=?",Long.class,snapshotId)).isNull();

  String retained=jdbc.queryForObject("SELECT snapshot_json FROM prd_process_snapshot WHERE id=?",String.class,snapshotId);
  var route2=new RouteSave(null,null,"RXR2"+suffix,List.of(new Operation("MIX","Mix changed",1,null,null,List.of(),null,false,List.of())));
  process.saveCurrentDefinition(processId,new CurrentDefinitionSave(null,null,null,null,route2),null,key());
  assertThat(jdbc.queryForObject("SELECT snapshot_json FROM prd_process_snapshot WHERE id=?",String.class,snapshotId)).isEqualTo(retained);
 }
}
