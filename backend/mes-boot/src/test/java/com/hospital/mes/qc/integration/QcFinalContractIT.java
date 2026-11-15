package com.hospital.mes.qc.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import com.hospital.mes.qc.domain.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.support.TransactionTemplate;

/** Only unique test-owned identities and records; native MariaDB facts roll back. */
@AutoConfigureMockMvc
class QcFinalContractIT extends QcSpecificationIT {
 @Autowired MockMvc mvc;
 @Autowired @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping") org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mappings;
 private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal(){return user("qc-fixture").authorities(permissions.stream().map(SimpleGrantedAuthority::new).toArray(SimpleGrantedAuthority[]::new));}
 private JsonNode response(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,int status) throws Exception {
  return json.readTree(mvc.perform(request.with(principal())).andExpect(status().is(status)).andReturn().getResponse().getContentAsString()).path("data");
 }
 private String base="/api/v1/quality";
 private JsonNode created() throws Exception {
  return response(post(base+"/specifications").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).content(json.writeValueAsString(create("QH"+suffix,"CHINESE_PHARMACOPOEIA","《中国药典》2025年版四部 0301",List.of(material),List.of(),items()))),201);
 }
 @Test void sevenActualHttpOperationsPreserveEtagsClosedDtoAndScope() throws Exception {
  var created=created();String id=created.path("id").asText();
  assertThat(id).isNotEmpty();
  mvc.perform(get(base+"/specifications/"+id).with(principal())).andExpect(status().isOk()).andExpect(header().string("ETag","\"0\""));
  mvc.perform(get(base+"/specifications").with(principal()).param("materialId",material).param("keyword","QH"+suffix).param("status","ACTIVE").param("size","20")).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1));
  String path=base+"/specifications/"+id;
  var edited=response(put(path).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).header("If-Match","\"0\"").content(json.writeValueAsString(new QcCommands.Edit(0L,"QH"+suffix,"氯化钾入厂检验标准","ENTERPRISE_INTERNAL","企标 Q/QC 001-2026",null,"收严限度","ACTIVE","修订"))),200);
  assertThat(edited.path("basisType").asText()).isEqualTo("ENTERPRISE_INTERNAL");
  mvc.perform(put(path).with(principal()).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).header("If-Match","\"0\"").content(json.writeValueAsString(new QcCommands.Edit(0L,"QH"+suffix,"氯化钾入厂检验标准","ENTERPRISE_INTERNAL","过期版本",null,null,"ACTIVE","过期")))).andExpect(status().isConflict());
  String scopePath=path+"/scope";
  var scoped=response(put(scopePath).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).header("If-Match","\"1\"").content(json.writeValueAsString(new QcCommands.ReplaceScope(1L,List.of(material,otherMaterial),List.of(),"扩充适用物料"))),200);
  assertThat(scoped.path("scopes")).hasSize(2);
  String itemsPath=path+"/items";
  var items=response(put(itemsPath).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).header("If-Match","\"2\"").content(json.writeValueAsString(new QcCommands.ReplaceItems(2L,List.of(items().get(0)),"只保留含量"))),200);
  assertThat(items.path("items")).hasSize(1);
  mvc.perform(delete(path).with(user("denied"))).andExpect(status().isForbidden());
  mvc.perform(post(base+"/specifications").with(principal()).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).content("{\"status\":\"APPROVED\"}")).andExpect(status().isBadRequest());
  as(2,creator);mvc.perform(get(path).with(principal())).andExpect(status().isNotFound());
  as(1,creator);
  var deleted=response(delete(path).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).header("If-Match","\"3\"").content(json.writeValueAsString(new QcCommands.Remove(3L,"重复建立"))),200);
  assertThat(deleted.path("deleted").asBoolean()).isTrue();
 }
 @Test void identityUniquenessBasisPairingAndOrphanConstraintsRejectWithoutExtraFacts(){
  var created=standard();String root=id(created);var tx=nested();
  assertThatThrownBy(()->tx.execute(s->service.create(create("QS"+suffix,"OTHER","重复编码",List.of(material),List.of(),items()),key()))).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  assertThatThrownBy(()->tx.execute(s->service.create(create("QB"+suffix,"CHINESE_PHARMACOPOEIA",null,List.of(material),List.of(),items()),key()))).isInstanceOf(com.hospital.mes.common.exception.ValidationException.class);
  assertThatThrownBy(()->tx.execute(s->service.create(create("QC"+suffix,"NOT_A_BASIS","未知依据",List.of(material),List.of(),items()),key()))).isInstanceOf(com.hospital.mes.common.exception.ValidationException.class);
  assertThatThrownBy(()->tx.execute(s->service.create(create("QD"+suffix,"CHINESE_PHARMACOPOEIA","依据",List.of(),List.of(),items()),key()))).isInstanceOf(com.hospital.mes.common.exception.ValidationException.class);
  assertThatThrownBy(()->jdbc.update("INSERT INTO qc_specification_scope(org_id,created_by,updated_by,specification_id,subject_type,subject_id) VALUES(1,?,?,?,'MATERIAL',?)",creator,creator,root,material)).isInstanceOf(org.springframework.dao.DataAccessException.class);
  assertThatThrownBy(()->jdbc.update("INSERT INTO qc_specification_scope(org_id,created_by,updated_by,specification_id,subject_type,subject_id) VALUES(1,?,?,?,?,'MATERIAL',9223372036854775807)",creator,creator,root,"MATERIAL")).isInstanceOf(org.springframework.dao.DataAccessException.class);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification WHERE created_by=?",Long.class,creator)).isEqualTo(1);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification_item WHERE specification_id=?",Long.class,root)).isEqualTo(2);
 }
 @Test void definitionBoundsExclusivityPrecisionAndRequiredValuesAreRejectedAtomically(){
  var created=standard();var tx=nested();
  List<List<QcCommands.Item>> invalid=new ArrayList<>();invalid.add(List.of());
  invalid.add(List.of(item("NUMERIC",null,null,unit,null,"METHOD",true)));
  invalid.add(List.of(item("NUMERIC","4","3",unit,null,"METHOD",true)));
  invalid.add(List.of(item("NUMERIC","1.0000001","3",unit,null,"METHOD",true)));
  invalid.add(List.of(item("NUMERIC","1","3",null,null,"METHOD",true)));
  invalid.add(List.of(item("NUMERIC","1","3",unit,"白色","METHOD",true)));
  invalid.add(List.of(item("TEXT","1",null,null,"白色","METHOD",true)));
  invalid.add(List.of(item("TEXT",null,null,unit,"白色","METHOD",true)));
  invalid.add(List.of(item("TEXT",null,null,null," ","METHOD",true)));
  invalid.add(List.of(item("OTHER",null,null,null,"白色","METHOD",true)));
  invalid.add(List.of(item("NUMERIC","1","3",unit,null,"",true)));
  invalid.add(List.of(item("NUMERIC","1","3",unit,null,"METHOD",null)));
  invalid.add(List.of(items().get(0),items().get(0)));
  invalid.add(List.of(items().get(0),new QcCommands.Item("assay","含量",true,"NUMERIC","1","3",unit,null,"M1","1")));
  for(var items:invalid){String k=key();assertThatThrownBy(()->tx.execute(s->service.replaceItems(id(created),new QcCommands.ReplaceItems(0L,items,"无效定义"),"\"0\"",k))).isInstanceOf(com.hospital.mes.common.exception.ValidationException.class);assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,k)).isZero();}
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification_item WHERE specification_id=?",Long.class,id(created))).isEqualTo(2);
  assertThat(service.get(id(created)).path("items")).hasSize(2);
 }
 private QcCommands.Item item(String type,String lo,String hi,String unit,String text,String method,Boolean required){return new QcCommands.Item("ASSAY","Assay",required,type,lo,hi,unit,text,method,"1");}
 private TransactionTemplate nested(){var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);return tx;}
 @Test void everyQcHttpOperationRequiresItsPermission() throws Exception {
  var created=created();String id=created.path("id").asText(),root=base+"/specifications/"+id;
  var denied=user("no-permissions");
  for(var r:List.of(get(base+"/specifications"),post(base+"/specifications"),get(root),put(root),put(root+"/scope"),put(root+"/items"),delete(root)))
   mvc.perform(r.with(denied).contentType(MediaType.APPLICATION_JSON).content("{}").header("Idempotency-Key",key()).header("If-Match","\"0\"")).andExpect(status().isForbidden());
  mvc.perform(delete(root).with(user("edit-only").authorities(new SimpleGrantedAuthority("qms:specification:edit"))).contentType(MediaType.APPLICATION_JSON).content("{}").header("Idempotency-Key",key()).header("If-Match","\"0\"")).andExpect(status().isForbidden());
  mvc.perform(get(root).with(user("view-only").authorities(new SimpleGrantedAuthority("qms:specification:view")))).andExpect(status().isOk());
  assertThat(service.get(id(created)).path("versionNo").asLong()).isZero();
 }
 @Test void replayedCommandReturnsTheSameStoredResultAndAChangedBodyConflicts() throws Exception {
  var created=created();String id=created.path("id").asText(),path=base+"/specifications/"+id;String k=key();
  var command=new QcCommands.Edit(0L,"QH"+suffix,"氯化钾入厂检验标准","ENTERPRISE_INTERNAL","企标 Q/QC 001-2026",null,"收严限度","ACTIVE","首次提交");
  String first=json.writeValueAsString(command);
  var one=response(put(path).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",k).header("If-Match","\"0\"").content(first),200);
  var replay=response(put(path).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",k).header("If-Match","\"0\"").content(first),200);
  assertThat(replay.path("versionNo").asLong()).isEqualTo(one.path("versionNo").asLong());
  mvc.perform(put(path).with(principal()).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",k).header("If-Match","\"0\"").content(json.writeValueAsString(new QcCommands.Edit(0L,"QH"+suffix,"氯化钾入厂检验标准","ENTERPRISE_INTERNAL","企标 Q/QC 001-2026",null,"收严限度","ACTIVE","不同理由")))).andExpect(status().isConflict());
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification_revision WHERE specification_id=?",Long.class,id(created))).isEqualTo(2);
 }
 @Test void standardIsNoLongerBehindAnApprovalGate() throws Exception {
  var created=created();String id=created.path("id").asText();
  for(String action:List.of("approve","retire","submit")){
   assertThat(mappings.getHandler(new org.springframework.mock.web.MockHttpServletRequest("POST",base+"/specifications/"+id+"/"+action))).isNull();
   mvc.perform(post(base+"/specifications/"+id+"/"+action).with(principal()).contentType(MediaType.APPLICATION_JSON).content("{}").header("Idempotency-Key",key())).andExpect(status().is4xxClientError());
  }
  as(1,editor);
  var edited=service.edit(id(created),new QcCommands.Edit(0L,"QH"+suffix,"氯化钾入厂检验标准","ENTERPRISE_INTERNAL","企标 Q/QC 001-2026",null,null,"ACTIVE","直接修订"),"\"0\"",key());
  assertThat(edited.path("status").asText()).isEqualTo("ACTIVE");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_signature WHERE object_type LIKE 'QcSpecification%' AND object_id=?",Long.class,Long.parseLong(id))).isZero();
 }
}
