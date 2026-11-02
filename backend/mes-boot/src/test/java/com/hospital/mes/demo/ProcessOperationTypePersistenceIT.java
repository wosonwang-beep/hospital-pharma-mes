package com.hospital.mes.demo;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.process.application.*;
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
 @Autowired ProcessQueryService query;
 @Autowired ObjectMapper json;
 @Autowired JdbcTemplate jdbc;
 @Autowired MockMvc mvc;
 @Autowired PlatformTransactionManager transactions;
 @MockitoBean CurrentPlatformContextResolver contexts;
 String suffix,code,login,id;
 final List<String> keys=new ArrayList<>();
 final Set<String> permissions=Set.of("process:package:create","process:package:edit","process:package:view");

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
