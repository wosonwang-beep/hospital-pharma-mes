package com.hospital.mes.ebr.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.ebr.application.EbrService;
import com.hospital.mes.ebr.domain.EbrCommands.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.process.application.*;
import com.hospital.mes.process.domain.ProcessCommands;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Unique fixture records are written only inside the test transaction and rolled back. */
@SpringBootTest @ActiveProfiles("ci") @Transactional @AutoConfigureMockMvc
class CppControlledRoundTripIT {
 @Autowired EbrService ebr; @Autowired ProcessService process; @Autowired ProcessQueryService processQuery;
 @Autowired UnitService units; @Autowired MaterialService materials; @Autowired JdbcTemplate jdbc;
 @Autowired ObjectMapper json; @Autowired MockMvc mvc; @MockitoBean CurrentPlatformContextResolver contexts;
 String templateCode, packageCode, productCode, unitCode, materialCode, roleCode, templateId;
 final List<String> requestKeys=new ArrayList<>();
 String key(){String value=UUID.randomUUID().toString();requestKeys.add(value);return value;}

 @Test void cppDefinitionPersistsAndReadsBack() throws Exception {
  var payload=json.readTree(Path.of("..","..","docs","development","process-form-cpp-ebr","20261029","cpp-roundtrip-payload.json").toFile());
  String suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);
  templateCode=payload.path("templateCodePrefix").asText()+suffix;
  packageCode=payload.path("packageCodePrefix").asText()+suffix;
  productCode=payload.path("productCodePrefix").asText()+suffix;
  unitCode=payload.path("unitCodePrefix").asText()+suffix;
  materialCode=payload.path("materialCodePrefix").asText()+suffix;
  roleCode=payload.path("roleCodePrefix").asText()+suffix;
  jdbc.update("INSERT INTO sys_role(role_code,display_name,enabled,version) VALUES(?,?,TRUE,0)",roleCode,payload.path("roleName").asText());
  String roleId=jdbc.queryForObject("SELECT id FROM sys_role WHERE role_code=?",Long.class,roleCode).toString();
  long actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,"admin");
  Set<String> permissions=new HashSet<>();
  for(String prefix:List.of("master:product:","master:uom:","master:material:","process:package:","ebr:template:"))
   for(String action:List.of("view","create","update","edit","submit","approve","publish"))permissions.add(prefix+action);
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of(),permissions,"local-context",key()));
  String unit=units.create(new UnitCommands.Create(unitCode,payload.path("unitName").asText(),"MASS",3),key()).path("id").asText();
  String material=materials.create(json.convertValue(Map.of("materialCode",materialCode,"materialName",payload.path("materialName").asText(),"materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();
  var product=process.createProduct(new ProcessCommands.ProductCreate(productCode,payload.path("productName").asText(),null,null,unit),key());
  var pkg=process.createPackage(new ProcessCommands.PackageCreate(product.path("id").asText(),packageCode),key());
  String packageId=pkg.path("id").asText();
  process.saveCurrentDefinition(packageId,new ProcessCommands.CurrentDefinitionSave(null,null,null,
   new ProcessCommands.FormulaSave(0L,payload.path("formulaReason").asText(),payload.path("formulaCodePrefix").asText()+suffix,"10",unit,List.of(new ProcessCommands.FormulaLine(1,material,"1",unit,"0",true))),
   new ProcessCommands.RouteSave(1L,payload.path("formulaReason").asText(),payload.path("routeCodePrefix").asText()+suffix,List.of(new ProcessCommands.Operation(payload.path("operationCode").asText(),payload.path("operationName").asText(),1,null,null,List.of(),null,false,List.of())))),null,key());
  String operationId=processQuery.currentOperations(1,Long.parseLong(packageId)).getFirst().id();
  var created=ebr.create(new Create(packageId,templateCode,payload.path("templateName").asText()),key());
  templateId=created.path("id").asText();
  var field=new Field("WEIGHT","GROUP_TL",payload.path("fieldName").asText(),"NUMBER","MANUAL","DECIMAL",unit,3,true,false,null,null,null,1,null,List.of());
  String config=json.writeValueAsString(Map.of("kind","CPP","name",payload.path("ruleName").asText(),"description",payload.path("ruleDescription").asText(),"rootLogic","AND","groups",List.of(Map.of("id","G1","logic","AND","conditions",List.of(Map.of("id","C1","fieldCode","WEIGHT","operator",">=","value","1"),Map.of("id","C2","fieldCode","WEIGHT","operator","<=","value","10")))),"actions",List.of("MARK_EXCEPTION","BLOCK_SUBMIT")));
  var rule=new Rule("CPP_WEIGHT","FORM_TL",null,"VALIDATION","ON_CHANGE","((value('WEIGHT') >= 1) && (value('WEIGHT') <= 10))","BLOCK","CPP_RULE_FAILED",payload.path("ruleMessage").asText(),false,true,config);
  var signature=new SignatureRule("FORM","FORM_TL","VERIFY",null,true,1,true,roleId,null,null);
  var review=new ReviewRule("FORM","FORM_TL","VERIFY",null,true,1,roleId,null,null);
  var definition=new Definition(List.of(new Section("SECTION_TL",payload.path("sectionName").asText(),1,"NONE",null,false,List.of(new Group("GROUP_TL",payload.path("groupName").asText(),1,2,"LIST",1,5)))),List.of(new Form("FORM_TL",payload.path("formName").asText(),operationId,"1.0",1,List.of(field))),List.of(rule),List.of(signature),List.of(review),"2.0");
  var saved=ebr.save(templateId,new Save(0L,payload.path("saveReason").asText(),definition),null,key());
  String stored=jdbc.queryForObject("SELECT cpp_config_json FROM ebr_rule_def WHERE template_version_id=? AND rule_code=?",String.class,templateId,"CPP_WEIGHT");
  JsonNode read=ebr.get(templateId).path("definition").path("rules").get(0);
  assertThat(json.readTree(stored)).isEqualTo(json.readTree(config));
  assertThat(json.readTree(read.path("cppConfigJson").asText())).isEqualTo(json.readTree(config));
  assertThat(read.path("expression").asText()).isEqualTo(rule.expression());
  assertThat(saved.path("definition").path("rules").get(0).path("cppConfigJson").asText()).isEqualTo(config);
  JsonNode loadedDefinition=ebr.get(templateId).path("definition");
  for(String kind:List.of("signatureRules","reviewRules")){
   JsonNode loadedRole=loadedDefinition.path(kind).get(0);
   assertThat(loadedRole.path("requiredRoleId").asText()).isEqualTo(roleId);
   assertThat(loadedRole.path("roleCodeSnapshot").asText()).isEqualTo(roleCode);
   assertThat(loadedRole.path("roleNameSnapshot").asText()).isEqualTo(payload.path("roleName").asText());
  }
  String requestKey=key();
  var savedHttp=mvc.perform(put("/api/v1/ebr/templates/"+templateId)
   .with(user("operator").authorities(new SimpleGrantedAuthority("ebr:template:update")))
   .header("If-Match","\"1\"").header("Idempotency-Key",requestKey)
   .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(new Save(1L,payload.path("saveReason").asText(),definition))))
   .andExpect(status().isOk()).andReturn();
  assertThat(json.readTree(savedHttp.getResponse().getContentAsByteArray()).path("data").path("versionNo").asLong()).isEqualTo(2);
  var loadedHttp=mvc.perform(get("/api/v1/ebr/templates/"+templateId)
   .with(user("operator").authorities(new SimpleGrantedAuthority("ebr:template:view"))))
   .andExpect(status().isOk()).andReturn();
  JsonNode httpDefinition=json.readTree(loadedHttp.getResponse().getContentAsByteArray()).path("data").path("definition");
  assertThat(json.readTree(httpDefinition.path("rules").get(0).path("cppConfigJson").asText())).isEqualTo(json.readTree(config));
  assertThat(httpDefinition.path("signatureRules").get(0).path("requiredRoleId").asText()).isEqualTo(roleId);
  assertThat(httpDefinition.path("reviewRules").get(0).path("requiredRoleId").asText()).isEqualTo(roleId);
  var replay=mvc.perform(put("/api/v1/ebr/templates/"+templateId)
   .with(user("operator").authorities(new SimpleGrantedAuthority("ebr:template:update")))
   .header("If-Match","\"1\"").header("Idempotency-Key",requestKey)
   .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(new Save(1L,payload.path("saveReason").asText(),definition))))
   .andExpect(status().isOk()).andReturn();
  assertThat(json.readTree(replay.getResponse().getContentAsByteArray()).path("data"))
   .isEqualTo(json.readTree(savedHttp.getResponse().getContentAsByteArray()).path("data"));
  mvc.perform(put("/api/v1/ebr/templates/"+templateId)
   .with(user("operator").authorities(new SimpleGrantedAuthority("ebr:template:update")))
   .header("If-Match","\"1\"").header("Idempotency-Key",key())
   .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(new Save(1L,payload.path("saveReason").asText(),definition))))
   .andExpect(status().isConflict());
  mvc.perform(get("/api/v1/ebr/templates/"+templateId)
   .with(user("operator").authorities(new SimpleGrantedAuthority("ebr:designer:edit"))))
   .andExpect(status().isForbidden());
  assertThat(ebr.get(templateId).path("versionNo").asLong()).isEqualTo(2);
 }

 @AfterTransaction void fixtureWasRolledBack(){
  if(templateCode==null)return;
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ebr_template_version WHERE template_code=?",Long.class,templateCode)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM proc_package WHERE package_code=?",Long.class,packageCode)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_product WHERE product_code=?",Long.class,productCode)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_unit WHERE unit_code=?",Long.class,unitCode)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM md_material WHERE material_code=?",Long.class,materialCode)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_role WHERE role_code=?",Long.class,roleCode)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_audit_event WHERE object_type=? AND object_id=?",Long.class,"EbrTemplateVersion",templateId)).isZero();
  for(String requestKey:requestKeys)
   assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,requestKey)).isZero();
 }
}
