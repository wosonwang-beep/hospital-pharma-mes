package com.hospital.mes.qc.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import com.hospital.mes.qc.domain.*;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.security.reauth.*;
import com.hospital.mes.security.identity.IdentityDirectory;
import com.hospital.mes.security.password.PasswordService;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.*;
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
 @Autowired IdentityDirectory identities;
 @Autowired PasswordService passwords;
 @Autowired ReauthenticationTokenStore tokenStore;
 private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor principal(){return user("qc-fixture").authorities(permissions.stream().map(SimpleGrantedAuthority::new).toArray(SimpleGrantedAuthority[]::new));}
 private JsonNode response(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,int status) throws Exception {
  return json.readTree(mvc.perform(request.with(principal())).andExpect(status().is(status)).andReturn().getResponse().getContentAsString()).path("data");
 }
 @Test void eightActualHttpOperationsPreserveEtagsClosedDtoPermissionsAndScope() throws Exception {
  String base="/api/v1/quality";
  var root=response(post(base+"/specifications").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).content(json.writeValueAsString(new QcCommands.CreateSpecification(material,"HTTP"+suffix,"HTTP standard","Create"))),201);
  assertThat(root.path("id").isTextual()).isTrue();String id=root.path("id").asText();
  assertThat(response(get(base+"/specifications/"+id),200).path("id").asText()).isEqualTo(id);
  response(get(base+"/specifications").param("materialId",material).param("size","20"),200);
  var draft=response(post(base+"/specifications/"+id+"/versions").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).content(json.writeValueAsString(new QcCommands.CreateVersion(1,items(),"Define"))),201);
  String versionId=draft.path("id").asText();String path=base+"/specification-versions/"+versionId;
  mvc.perform(get(path).with(principal())).andExpect(status().isOk()).andExpect(header().string("ETag","\"0\""));
  response(put(path).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).header("If-Match","\"0\"").content(json.writeValueAsString(new QcCommands.EditVersion(0L,items(),"Edit"))),200);
  mvc.perform(put(path).with(principal()).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).header("If-Match","\"0\"").content(json.writeValueAsString(new QcCommands.EditVersion(0L,items(),"Stale")))).andExpect(status().isConflict());
  as(1,approver);
  var approved=response(post(path+"/approve").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).header("If-Match","\"1\"").content(json.writeValueAsString(Map.of("versionNo",1,"reason","Approve","reauthToken","token"))),200);
  assertThat(approved.path("status").asText()).isEqualTo("APPROVED");
  var retired=response(post(path+"/retire").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).header("If-Match","\"2\"").content(json.writeValueAsString(Map.of("versionNo",2,"reason","Retire","reauthToken","token"))),200);
  assertThat(retired.path("status").asText()).isEqualTo("RETIRED");
  mvc.perform(get(path).with(user("denied"))).andExpect(status().isForbidden());
  mvc.perform(post(base+"/specifications").with(principal()).contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key",key()).content("{\"status\":\"APPROVED\"}")).andExpect(status().isBadRequest());
  as(2,approver);mvc.perform(get(path).with(principal())).andExpect(status().isNotFound());
 }
 @Test void identityUniquenessAndOrphanConstraintsRejectWithoutExtraFacts(){
  var v=draft();String root=v.specificationId();var tx=nested();
  assertThatThrownBy(()->tx.execute(s->service.create(new QcCommands.CreateSpecification(material,"QS"+suffix,"Duplicate","Duplicate"),key()))).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  assertThatThrownBy(()->tx.execute(s->service.createVersion(root,new QcCommands.CreateVersion(1,items(),"Duplicate version"),key()))).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  assertThatThrownBy(()->jdbc.update("INSERT INTO qc_specification(org_id,created_by,updated_by,material_id,specification_code,specification_name) VALUES(1,?,?,9223372036854775807,?,?)",creator,creator,"ORPHAN"+suffix,"Orphan")).isInstanceOf(org.springframework.dao.DataAccessException.class);
  assertThatThrownBy(()->jdbc.update("INSERT INTO qc_specification_version(org_id,created_by,updated_by,specification_id,version_no_business) VALUES(1,?,?,9223372036854775807,1)",creator,creator)).isInstanceOf(org.springframework.dao.DataAccessException.class);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification WHERE created_by=?",Long.class,creator)).isEqualTo(1);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification_version WHERE specification_id=?",Long.class,root)).isEqualTo(1);
 }
 @Test void definitionBoundsExclusivityPrecisionAndRequiredValuesAreRejectedAtomically(){
  var v=draft();var tx=nested();
  List<List<QcCommands.Item>> invalid=new ArrayList<>();invalid.add(List.of());invalid.add(Arrays.asList((QcCommands.Item)null));
  invalid.add(List.of(item("NUMERIC",null,null,unit,null,"METHOD",true)));
  invalid.add(List.of(item("NUMERIC","4","3",unit,null,"METHOD",true)));
  invalid.add(List.of(item("NUMERIC","1.0000001","3",unit,null,"METHOD",true)));
  invalid.add(List.of(item("NUMERIC","1","3",null,null,"METHOD",true)));
  invalid.add(List.of(item("NUMERIC","1","3",unit,"white","METHOD",true)));
  invalid.add(List.of(item("TEXT","1",null,null,"white","METHOD",true)));
  invalid.add(List.of(item("TEXT",null,null,unit,"white","METHOD",true)));
  invalid.add(List.of(item("TEXT",null,null,null," ","METHOD",true)));
  invalid.add(List.of(item("OTHER",null,null,null,"white","METHOD",true)));
  invalid.add(List.of(item("NUMERIC","1","3",unit,null,"",true)));
  invalid.add(List.of(item("NUMERIC","1","3",unit,null,"METHOD",null)));
  invalid.add(List.of(items().get(0),items().get(0)));
  for(var items:invalid){String k=key();assertThatThrownBy(()->tx.execute(s->service.editVersion(v.id(),new QcCommands.EditVersion(0L,items,"Invalid definition"),"\"0\"",k))).isInstanceOf(com.hospital.mes.common.exception.ValidationException.class);assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,k)).isZero();}
  assertThat(service.getVersion(v.id())).isEqualTo(v);
 }
 private QcCommands.Item item(String type,String lo,String hi,String unit,String text,String method,Boolean required){return new QcCommands.Item("ASSAY","Assay",required,type,lo,hi,unit,text,method,"1");}
 private TransactionTemplate nested(){var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);return tx;}
 @Test void realPasswordRedisExpiredBoundAndSingleUseTokensProtectActualApproval(){
  var v=draft();as(1,approver);String password="Unique-QC-test-password-2026";jdbc.update("UPDATE sys_user SET password_hash=? WHERE id=?",passwords.hash(password),approver);
  Instant now=Instant.now();var real=new ReauthenticationService(identities,passwords,tokenStore,Clock.fixed(now,ZoneOffset.UTC));var context=contexts.current();
  var request=new ReauthenticationRequest("QcSpecificationVersion",v.id(),SignatureMeaning.APPROVE,1,password);
  assertThatThrownBy(()->real.issue(new ReauthenticationRequest("QcSpecificationVersion",v.id(),SignatureMeaning.APPROVE,1,"wrong-password"),context)).isInstanceOf(ReauthenticationFailedException.class);
  var binding=new ExpectedReauthenticationBinding(approver,context.sessionId(),1,"QcSpecificationVersion",v.id(),SignatureMeaning.APPROVE,1);
  var expired=real.issue(request,context);var later=new ReauthenticationService(identities,passwords,tokenStore,Clock.fixed(now.plusSeconds(301),ZoneOffset.UTC));
  doAnswer(i->later.consume(i.getArgument(0),i.getArgument(1))).when(reauth).consume(anyString(),any());
  String k=key();assertThatThrownBy(()->nested().execute(s->service.approveVersion(v.id(),new QcCommands.SignVersion(0L,"Expired",""+expired.token()),"\"0\"",k))).isInstanceOf(ReauthenticationTokenInvalidException.class);
  assertThat(service.getVersion(v.id()).status()).isEqualTo("DRAFT");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_signature WHERE object_type='QcSpecificationVersion' AND object_id=?",Long.class,v.id())).isZero();assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,k)).isZero();
  var wrong=real.issue(request,context);assertThatThrownBy(()->real.consume(wrong.token(),new ExpectedReauthenticationBinding(creator,context.sessionId(),1,"QcSpecificationVersion",v.id(),SignatureMeaning.APPROVE,1))).isInstanceOf(ReauthenticationTokenInvalidException.class);assertThatThrownBy(()->real.consume(wrong.token(),binding)).isInstanceOf(ReauthenticationTokenInvalidException.class);
  var valid=real.issue(request,context);doAnswer(i->real.consume(i.getArgument(0),i.getArgument(1))).when(reauth).consume(anyString(),any());
  var approved=service.approveVersion(v.id(),new QcCommands.SignVersion(0L,"Real password and Redis approval",valid.token()),"\"0\"",key());assertThat(approved.status()).isEqualTo("APPROVED");assertThatThrownBy(()->real.consume(valid.token(),binding)).isInstanceOf(ReauthenticationTokenInvalidException.class);
 }
 @Test void everyQcHttpOperationRequiresItsPermissionAndSignedActionsRequireSign() throws Exception {
  var v=draft();String b="/api/v1/quality",root=b+"/specifications/"+v.specificationId(),path=b+"/specification-versions/"+v.id();
  var denied=user("no-permissions");
  for(var r:List.of(get(b+"/specifications"),post(b+"/specifications"),get(root),post(root+"/versions"),get(path),put(path),post(path+"/approve"),post(path+"/retire")))
   mvc.perform(r.with(denied).contentType(MediaType.APPLICATION_JSON).content("{}").header("Idempotency-Key",key()).header("If-Match","\"0\"")).andExpect(status().isForbidden());
  for(String action:List.of("approve","retire"))mvc.perform(post(path+"/"+action).with(user("command-without-sign").authorities(new SimpleGrantedAuthority("qms:specification:"+action))).contentType(MediaType.APPLICATION_JSON).content("{}").header("Idempotency-Key",key()).header("If-Match","\"0\"")).andExpect(status().isForbidden());
  assertThat(service.getVersion(v.id())).isEqualTo(v);
 }
 @Test void namedLifecycleRejectsEveryInvalidTransitionAndRetiredContentMutation(){
  var v=draft();as(1,approver);assertThatThrownBy(()->nested().execute(s->service.retireVersion(v.id(),new QcCommands.SignVersion(0L,"Invalid draft retirement","token"),"\"0\"",key()))).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  var approved=approve(v);assertThatThrownBy(()->nested().execute(s->service.approveVersion(v.id(),new QcCommands.SignVersion(1L,"Cannot approve twice","token"),"\"1\"",key()))).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  var retired=service.retireVersion(v.id(),new QcCommands.SignVersion(1L,"Retire","token"),"\"1\"",key());
  var beforeInvalid=service.getVersion(v.id());
  for(String action:List.of("approve","retire"))assertThatThrownBy(()->nested().execute(s->action.equals("approve")?service.approveVersion(v.id(),new QcCommands.SignVersion(2L,"Illegal","token"),"\"2\"",key()):service.retireVersion(v.id(),new QcCommands.SignVersion(2L,"Illegal","token"),"\"2\"",key()))).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  assertThatThrownBy(()->nested().execute(s->service.editVersion(v.id(),new QcCommands.EditVersion(2L,items(),"Illegal"),"\"2\"",key()))).isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  assertThat(service.getVersion(v.id())).isEqualTo(beforeInvalid);assertThat(verification.verify(1,Long.parseLong(approved.approvalSignatureId()))).isTrue();assertThat(verification.verify(1,Long.parseLong(retired.retirementSignatureId()))).isTrue();
 }
 @Test void expiredRealRedisTokenOnActualQcHttpReturnsUnauthorizedWithoutFacts() throws Exception {
  var v=draft();as(1,approver);String password="Unique-QC-test-password-2026";jdbc.update("UPDATE sys_user SET password_hash=? WHERE id=?",passwords.hash(password),approver);Instant now=Instant.now();
  var real=new ReauthenticationService(identities,passwords,tokenStore,Clock.fixed(now,ZoneOffset.UTC));var token=real.issue(new ReauthenticationRequest("QcSpecificationVersion",v.id(),SignatureMeaning.APPROVE,1,password),contexts.current());var expired=new ReauthenticationService(identities,passwords,tokenStore,Clock.fixed(now.plusSeconds(301),ZoneOffset.UTC));
  doAnswer(i->expired.consume(i.getArgument(0),i.getArgument(1))).when(reauth).consume(anyString(),any());String k=key();
  nested().execute(status->{
   try { mvc.perform(post("/api/v1/quality/specification-versions/"+v.id()+"/approve").with(principal()).contentType(MediaType.APPLICATION_JSON).header("If-Match","\"0\"").header("Idempotency-Key",k).content(json.writeValueAsString(Map.of("versionNo",0,"reason","Expired HTTP signature","reauthToken",token.token())))).andExpect(status().isUnauthorized()).andExpect(jsonPath("code").value("REAUTH_TOKEN_INVALID")); } catch(Exception e) { throw new RuntimeException(e); }
   // MVC resolves the exception; the participating service must still mark rollback.
   assertThat(status.isRollbackOnly()).isTrue();status.setRollbackOnly();return null;
  });
  assertThat(service.getVersion(v.id()).status()).isEqualTo("DRAFT");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_signature WHERE object_type='QcSpecificationVersion' AND object_id=?",Long.class,v.id())).isZero();assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,k)).isZero();
 }
}
