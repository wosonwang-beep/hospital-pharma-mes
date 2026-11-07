package com.hospital.mes.demo;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.process.application.*;
import com.hospital.mes.masterdata.application.UnitService;
import com.hospital.mes.masterdata.application.UnitCommands;
import com.hospital.mes.process.domain.ProcessCommands.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Native DEV database. Only uniquely named fixtures; all business writes roll back. */
@SpringBootTest @ActiveProfiles("ci") @AutoConfigureMockMvc @Transactional
class ProcessOperationTypePersistenceIT {
 @Autowired ProcessService service;
 @Autowired UnitService units;
 @Autowired ProcessQueryService query;
 @Autowired ObjectMapper json;
 @Autowired JdbcTemplate jdbc;
 @Autowired MockMvc mvc;
 @Autowired PlatformTransactionManager transactions;
 @MockitoBean CurrentPlatformContextResolver contexts;
 String suffix,code,login,id;
 final List<String> keys=new ArrayList<>();
 final Set<String> permissions=Set.of("process:package:create","process:package:edit","process:package:view","master:product:create","master:product:view","master:uom:create","master:uom:view");

 @BeforeEach void fixture() {
  suffix=UUID.randomUUID().toString().replace("-", "").substring(0,12);
  code="KCL-ROUTE-"+suffix; login="gy-maint-"+suffix;
  BusinessSeedPayloadGuard.check(Map.of("packageCode",code,"loginName",login,"displayName","工艺维护员","routeCode","KCL30-ROUTE","operations",List.of(Map.of("operationCode","OP01","operationName","溶液配制"),Map.of("operationCode","IPC01","operationName","中间产品检验"))));
  jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",login,login,"工艺维护员","!locked");
  long actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,login);
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,actor,Set.of("PROCESS_ENGINEER"),permissions,"local-session","operation-type-"+suffix));
  id=service.createPackage(new PackageCreate(null,code),key()).path("id").asText();
 }
 String key(){String k=UUID.randomUUID().toString(); keys.add(k);return k;}
 ObjectNode route(String first,String second) {
  var route=json.createObjectNode().put("routeCode","KCL30-ROUTE");
  var ops=route.putArray("operations");
  operation(ops,"OP01","溶液配制",1,List.of(),first);
  operation(ops,"IPC01","中间产品检验",2,List.of("OP01"),second);
  return route;
 }
 void operation(ArrayNode rows,String code,String name,int sequence,List<String> predecessors,String type) {
  var op=rows.addObject().put("operationCode",code).put("operationName",name).put("sequenceNo",sequence).put("clearanceRequired",false);
  op.set("predecessorCodes",json.valueToTree(predecessors)); op.putArray("parameters");op.putArray("ipcDefinitions");
  if(type!=null)op.put("operationType",type);
 }
 JsonNode save(ObjectNode route) {
  var dto=json.convertValue(route,RouteSave.class);
  return service.saveCurrentDefinition(id,new CurrentDefinitionSave(null,null,null,null,dto),null,key());
 }
 JsonNode operations(JsonNode p){return p.path("currentDefinition").path("route").path("operations");}
 JsonNode reread(){return service.getPackage(id,null);}
 void nested(Runnable work){var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);tx.execute(s->{work.run();return null;});}
 @AfterTransaction void allFixtureRowsRolledBack() {
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM proc_package WHERE package_code=?",Long.class,code)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE login_name_normalized=?",Long.class,login)).isZero();
  for(String key:keys)assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,key)).isZero();
 }

 @Test void currentMetadataPersistsAndLegacyRouteOnlySaveRetainsIt() {
  BusinessSeedPayloadGuard.check(Map.of("packageName","氯化钾溶液制备工艺","description","维护配制与过程检验要求"));
  var dto=json.convertValue(route("PRODUCTION","IPC"),RouteSave.class);
  service.saveCurrentDefinition(id,new CurrentDefinitionSave(null,null,null,null,dto,new PackageBasicInfo("氯化钾溶液制备工艺","维护配制与过程检验要求","V1.2","2026-06-01")),null,key());
  assertThat(reread().path("packageName").asText()).isEqualTo("氯化钾溶液制备工艺");
  assertThat(reread().path("description").asText()).isEqualTo("维护配制与过程检验要求");
  assertThat(reread().path("processVersion").asText()).isEqualTo("V1.2");
  assertThat(reread().path("effectiveApprovedDate").asText()).isEqualTo("2026-06-01");
  save(route("PRODUCTION","IPC"));
  assertThat(reread().path("packageName").asText()).isEqualTo("氯化钾溶液制备工艺");
  assertThat(reread().path("processVersion").asText()).isEqualTo("V1.2");
  assertThat(reread().path("effectiveApprovedDate").asText()).isEqualTo("2026-06-01");
  service.saveCurrentDefinition(id,new CurrentDefinitionSave(null,null,null,null,dto,new PackageBasicInfo("氯化钾溶液制备工艺",null)),null,key());
  assertThat(jdbc.queryForObject("SELECT description FROM proc_package WHERE id=?",String.class,Long.parseLong(id))).isNull();
  assertThat(service.packages(0,100,"氯化钾溶液制备",Map.of()).items()).anyMatch(p->p.path("id").asText().equals(id));
 }
 @Test void oneProcessCanPersistMultipleApplicableProductsAndFreezeScope() {
  var unit=units.create(new UnitCommands.Create("PROCUNIT"+suffix,"毫升","VOLUME",3),key()).path("id").asText();
  var first=service.createProduct(new ProductCreate("KCL30-"+suffix,"氯化钾溶液","溶液剂","30ml:3g/瓶",unit),key()).path("id").asText();
  var second=service.createProduct(new ProductCreate("BORIC-"+suffix,"硼酸溶液","溶液剂","100ml/瓶",unit),key()).path("id").asText();
  var dto=json.convertValue(route("PRODUCTION","IPC"),RouteSave.class);
  var basic=new PackageBasicInfo("氯化钾溶液制备工艺","适用于两种制剂产品","V1.2","2026-06-01");
  var saved=service.saveCurrentDefinition(id,new CurrentDefinitionSave(null,null,null,null,dto,basic,List.of(first,second)),null,key());
  assertThat(saved.path("productIds").isArray()).isTrue();
  assertThat(saved.path("productIds").size()).isEqualTo(2);
  assertThat(List.of(saved.path("productIds").get(0).asText(),saved.path("productIds").get(1).asText())).containsExactlyInAnyOrder(first,second);
  assertThat(saved.path("applicableProducts").size()).isEqualTo(2);
  assertThat(saved.path("productId").isNull()).isTrue();
  var originalAssociationIds=jdbc.queryForList("SELECT id FROM proc_package_product WHERE org_id=? AND package_id=? ORDER BY product_id",Long.class,1,Long.parseLong(id));
  service.saveCurrentDefinition(id,new CurrentDefinitionSave(null,null,null,null,dto,null,List.of(first,second)),null,key());
  assertThat(jdbc.queryForList("SELECT id FROM proc_package_product WHERE org_id=? AND package_id=? ORDER BY product_id",Long.class,1,Long.parseLong(id))).isEqualTo(originalAssociationIds);
  assertThat(service.packages(0,100,"",Map.of("productId",second)).items()).anyMatch(p->p.path("id").asText().equals(id));
  var snapshot=service.currentSnapshot(1,Long.parseLong(id),false);
  assertThat(snapshot.path("applicableProductIds").get(0).asText()).isEqualTo(first);
  assertThat(snapshot.path("applicableProductIds").get(1).asText()).isEqualTo(second);
  assertThat(reread().path("processVersion").asText()).isEqualTo("V1.2");
  assertThat(reread().path("effectiveApprovedDate").asText()).isEqualTo("2026-06-01");
 }
 @Test void invalidMetadataRollsBackRouteAndNameTogether() {
  save(route("PRODUCTION","IPC"));
  var before=reread().deepCopy();
  var changed=route("IPC","PRODUCTION");
  assertThatThrownBy(()->nested(()->service.saveCurrentDefinition(id,new CurrentDefinitionSave(null,null,null,null,json.convertValue(changed,RouteSave.class),new PackageBasicInfo(" ","不能部分写入")),null,key()))).hasMessageContaining("packageName");
  assertThat(reread()).isEqualTo(before);
 }
 @Test void realControllerPutAndGetRetainEachOperationType() throws Exception {
  var body=json.createObjectNode().set("route",route("PRODUCTION","IPC"));
  mvc.perform(put("/api/v1/process-packages/"+id+"/current-definition").with(user(login).authorities(new SimpleGrantedAuthority("process:package:edit"))).header("Idempotency-Key",key()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(body)))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.currentDefinition.route.operations[0].operationType").value("PRODUCTION"))
   .andExpect(jsonPath("$.data.currentDefinition.route.operations[1].operationType").value("IPC"));
  mvc.perform(get("/api/v1/process-packages/"+id).with(user(login).authorities(new SimpleGrantedAuthority("process:package:view"))))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.currentDefinition.route.operations[1].operationType").value("IPC"));
  assertThat(query.requireCurrent(1,Long.parseLong(id)).path("route").path("operations").get(1).path("operationType").asText()).isEqualTo("IPC");
 }
 @Test void omittedAndNullFieldsPreserveStoredTypesAndUnmarkedLegacyRows() {
  save(route(null,"IPC"));
  assertThat(operations(reread()).get(0).has("operationType")).isFalse();
  var originalIds=jdbc.queryForList("SELECT o.id FROM proc_operation_def o JOIN proc_route_version r ON r.id=o.route_version_id JOIN proc_current_definition c ON c.id=r.current_definition_id WHERE c.package_id=? ORDER BY o.sequence_no",Long.class,Long.parseLong(id));
  var request=route(null,null); ((ObjectNode)request.path("operations").get(1)).putNull("operationType");
  save(request);
  assertThat(operations(reread()).get(0).has("operationType")).isFalse();
  assertThat(operations(reread()).get(1).path("operationType").asText()).isEqualTo("IPC");
  assertThat(jdbc.queryForList("SELECT o.id FROM proc_operation_def o JOIN proc_route_version r ON r.id=o.route_version_id JOIN proc_current_definition c ON c.id=r.current_definition_id WHERE c.package_id=? ORDER BY o.sequence_no",Long.class,Long.parseLong(id))).isEqualTo(originalIds);
 }
 @Test void invalidTypeLeavesNoPartialUpdateAndDatabaseConstraintAlsoRejectsIt() {
  save(route("PRODUCTION","IPC")); var before=reread();
  assertThatThrownBy(()->nested(()->save(route("IPC","OTHER")))).hasMessageContaining("operationType");
  assertThat(reread()).isEqualTo(before);
  long operation=jdbc.queryForObject("SELECT o.id FROM proc_operation_def o JOIN proc_route_version r ON r.id=o.route_version_id JOIN proc_current_definition c ON c.id=r.current_definition_id WHERE c.package_id=? AND o.operation_code='IPC01'",Long.class,Long.parseLong(id));
  for(String invalid:List.of("OTHER","ipc","IPC "))
   assertThatThrownBy(()->nested(()->jdbc.update("UPDATE proc_operation_def SET operation_type=? WHERE id=?",invalid,operation))).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
  assertThat(reread()).isEqualTo(before);
 }
 @Test void routeWithoutAnyInspectionAndUnmarkedOldPayloadRemainUsable() {
  var route=route("PRODUCTION",null); ((ArrayNode)route.path("operations")).remove(1);
  assertThat(save(route).path("currentDefinition").path("status").asText()).isEqualTo("EFFECTIVE");
  ((ObjectNode)route.path("operations").get(0)).remove("operationType");
  save(route);
  assertThat(operations(reread()).get(0).path("operationType").asText()).isEqualTo("PRODUCTION");
 }
}
