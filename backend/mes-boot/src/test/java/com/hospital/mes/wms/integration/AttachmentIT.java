package com.hospital.mes.wms.integration;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.wms.application.*;
import com.hospital.mes.wms.domain.WmsCommands.*;
import java.util.*;
import java.time.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Native persistent DEV: unique test-owned fixtures; transaction rollback preserves the ledger. */
@SpringBootTest @ActiveProfiles("ci") @AutoConfigureMockMvc @Transactional
class AttachmentIT {
 @Autowired WmsService service;@Autowired WmsQueryService queries;@Autowired MaterialService materials;@Autowired UnitService units;@Autowired UnitConversionService conversions;@Autowired SupplierService suppliers;@Autowired MaterialSupplierService links;
 @Autowired ObjectMapper json;@Autowired JdbcTemplate jdbc;@Autowired MockMvc mvc;@Autowired PlatformTransactionManager transactions;
 @Autowired com.hospital.mes.audit.attachment.application.AttachmentService attachments;
 @Autowired WmsAttachmentService attachmentLinks;
 @MockitoBean CurrentPlatformContextResolver contexts;@MockitoSpyBean AuditApplicationService audit;
 String suffix,unit,material,supplier,warehouse,location,destination;long actor;Set<String> permissions;
 @BeforeEach void setup(TestInfo info){
  suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);permissions=new HashSet<>();for(String p:List.of("master:material:","master:supplier:","master:uom:","wms:inventory:","wms:receipt:","wms:issue:","wms:reservation:"))for(String a:List.of("view","create","update","disable","confirm","return","move","adjust"))permissions.add(p+a);
  permissions.add("attachment:upload");permissions.add("attachment:view");
  String name="wms_"+suffix;jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",name,name,"WMS rollback actor","test-only");actor=jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,name);as(1);
  unit=units.create(new UnitCommands.Create("WU"+suffix,"kg","MASS",6),key()).path("id").asText();material=materials.create(json.convertValue(Map.of("materialCode","WM"+suffix,"materialName","Raw salt","materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();
  var s=suppliers.create(new SupplierCommands.Create("WS"+suffix,"Approved source",LocalDate.now(ZoneOffset.UTC).plusYears(1)),key());supplier=s.path("id").asText();suppliers.command(supplier,new SupplierCommands.Update("QUALIFY",0L,"Qualification evidence",null,null),null,key());links.assign(material,new SupplierCommands.Assign(0L,"Approved source",List.of(new SupplierCommands.Relationship(supplier,true,true,null))),null,key());
  warehouse=service.maintenance("Warehouse",null,new WarehouseCreate("WH"+suffix,"Warehouse","RAW"),null,key()).path("id").asText();location=service.maintenance("Location",null,new LocationCreate(warehouse,"A"+suffix,"Area A"),null,key()).path("id").asText();destination=service.maintenance("Location",null,new LocationCreate(warehouse,"B"+suffix,"Area B"),null,key()).path("id").asText();
 }
 void as(long org){when(contexts.current()).thenReturn(new CurrentPlatformContext(org,actor,Set.of("WMS_TEST"),permissions,"test","wms-"+suffix));}
 String key(){return UUID.randomUUID().toString();}
 ReceiptItemInput item(String lot,String qty,String u,boolean checks){return new ReceiptItemInput(material,lot,"SLOT"+suffix,null,LocalDate.now(ZoneOffset.UTC).minusDays(1).toString(),LocalDate.now(ZoneOffset.UTC).plusYears(1).toString(),null,qty,u,null,1L,location,null,checks,true,true,true,true);}
 ReceiptCreate receipt(boolean checks){return new ReceiptCreate("WR"+suffix,supplier,null,null,warehouse,true,List.of(item("WL"+suffix,"5",unit,checks)));}
 JsonNode received(){return service.createReceipt(receipt(true),key(),true);}
 String lot(JsonNode r){return r.path("items").get(0).path("materialLotId").asText();}
 long count(String table){return jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE created_by=?",Long.class,actor);}
 @Test void emptyAndOversizedDeliveryDocumentsLeaveNoAttachmentOrIdempotencyFacts(){
  long before=jdbc.queryForObject("SELECT COUNT(*) FROM gxp_attachment WHERE uploaded_by=?",Long.class,actor);
  for(byte[] bytes:List.of(new byte[0],new byte[(int)com.hospital.mes.audit.attachment.domain.AttachmentRules.MAX_BYTES+1])){
   String commandKey=key();var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);
   assertThatThrownBy(()->tx.execute(s->attachments.upload("COA.pdf","application/pdf",bytes,commandKey))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("1..10485760 bytes");
   assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_attachment WHERE uploaded_by=?",Long.class,actor)).isEqualTo(before);
   assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,commandKey)).isZero();
  }
 }


 @Test void exactBytesReplayAndApprovedReceiptAppend(){
  byte[] bytes=new byte[]{0,1,2,13,10,(byte)255};String k=key();
  var file=attachments.upload("../COA.pdf","application/pdf",bytes,k);
  assertThat(attachments.upload("../COA.pdf","application/pdf",bytes,k)).isEqualTo(file);
  assertThat(attachments.download(file.id()).content()).containsExactly(bytes);
  assertThat(file.sha256()).isEqualTo(com.hospital.mes.audit.attachment.domain.AttachmentRules.digest(bytes));
  assertThat(file.fileName()).isEqualTo("COA.pdf");
  assertThatThrownBy(()->attachments.upload("../COA.pdf","application/pdf",new byte[]{3},k)).hasMessageContaining("different file");
  var receipt=received();String id=receipt.path("id").asText();long version=receipt.path("versionNo").asLong();
  var beforeLink=service.get("Receipt",id).deepCopy();
  var command=new WmsAttachmentService.Link(version,file.id(),"COA","Original supplier certificate");String linkKey=key();
  var link=attachmentLinks.link(id,command,"\""+version+"\"",linkKey);
  assertThat(attachmentLinks.link(id,command,"\""+version+"\"",linkKey)).isEqualTo(link);
  assertThat(service.get("Receipt",id)).isEqualTo(beforeLink);
  assertThat(attachmentLinks.download(id,file.id()).content()).containsExactly(bytes);
  assertThatThrownBy(()->attachmentLinks.link(id,command,"\""+version+"\"",key())).hasMessageContaining("already linked");
  assertThatThrownBy(()->jdbc.update("UPDATE gxp_attachment SET file_name='changed' WHERE id=?",file.id())).isInstanceOf(org.springframework.dao.DataAccessException.class);
  assertThatThrownBy(()->jdbc.update("DELETE FROM wms_receipt_attachment WHERE id=?",link.id())).isInstanceOf(org.springframework.dao.DataAccessException.class);
 }
 @Test void organizationSourceAccessAndStaleLinkFailClosed(){
  var file=attachments.upload("COA.txt","text/plain",new byte[]{1},key());var receipt=received();String id=receipt.path("id").asText();
  assertThatThrownBy(()->attachmentLinks.link(id,new WmsAttachmentService.Link(999L,file.id(),"COA","Stale"),"\"999\"",key())).hasMessageContaining("Reload");
  as(2);assertThatThrownBy(()->attachments.get(file.id())).isInstanceOf(NoSuchElementException.class);assertThatThrownBy(()->attachments.download(file.id())).isInstanceOf(NoSuchElementException.class);
  assertThatThrownBy(()->attachmentLinks.list(id)).isInstanceOf(NoSuchElementException.class);as(1);
  permissions.remove("attachment:view");long other=actor+1000000;
  when(contexts.current()).thenReturn(new CurrentPlatformContext(1,other,Set.of(),permissions,"test","other"));
  assertThatThrownBy(()->attachmentLinks.link(id,new WmsAttachmentService.Link(receipt.path("versionNo").asLong(),file.id(),"COA","Not mine"),"\""+receipt.path("versionNo").asLong()+"\"",key())).hasMessageContaining("source attachment");
 }
 @Test void auditFailureRollsBackAttachmentAndIdempotency(){
  String k=key();var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);
  doAnswer(i->{var a=(com.hospital.mes.audit.domain.AuditCommand)i.getArgument(0);if(a.action().equals("ATTACHMENT_UPLOADED"))throw new IllegalStateException("injected attachment audit failure");return i.callRealMethod();}).when(audit).append(any());
  assertThatThrownBy(()->tx.execute(t->attachments.upload("COA.txt","text/plain",new byte[]{1},k))).hasMessageContaining("injected attachment audit failure");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_attachment WHERE uploaded_by=?",Long.class,actor)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,k)).isZero();
 }
 @Test void actualMultipartHttpPreservesBytesReplayPermissionsAndSizeFailures() throws Exception {
  byte[] bytes="Original supplier COA\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
  var file=new org.springframework.mock.web.MockMultipartFile("file","COA.txt","text/plain",bytes);
  String commandKey=key();var principal=user("fixture").authorities(new SimpleGrantedAuthority("attachment:upload"),new SimpleGrantedAuthority("attachment:view"));
  var response=mvc.perform(multipart("/api/v1/attachments").file(file).with(principal).header("Idempotency-Key",commandKey)).andExpect(status().isOk()).andExpect(jsonPath("data.id").isString()).andReturn();
  String id=json.readTree(response.getResponse().getContentAsString()).path("data").path("id").asText();
  mvc.perform(multipart("/api/v1/attachments").file(file).with(principal).header("Idempotency-Key",commandKey)).andExpect(status().isOk()).andExpect(jsonPath("data.id").value(id));
  mvc.perform(get("/api/v1/attachments/"+id+"/content").with(principal)).andExpect(status().isOk()).andExpect(header().string("X-Content-Type-Options","nosniff")).andExpect(content().bytes(bytes));
  long before=jdbc.queryForObject("SELECT COUNT(*) FROM gxp_attachment WHERE uploaded_by=?",Long.class,actor);
  for(byte[] invalid:List.of(new byte[0],new byte[(int)com.hospital.mes.audit.attachment.domain.AttachmentRules.MAX_BYTES+1])){
   String invalidKey=key();mvc.perform(multipart("/api/v1/attachments").file(new org.springframework.mock.web.MockMultipartFile("file","COA.txt","text/plain",invalid)).with(principal).header("Idempotency-Key",invalidKey)).andExpect(status().isBadRequest());
   assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,invalidKey)).isZero();
  }
  mvc.perform(multipart("/api/v1/attachments").file(file).with(user("denied")).header("Idempotency-Key",key())).andExpect(status().isForbidden());
  mvc.perform(multipart("/api/v1/attachments").file(file).with(principal)).andExpect(status().isBadRequest());
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_attachment WHERE uploaded_by=?",Long.class,actor)).isEqualTo(before);
 }
}
