package com.hospital.mes.qc.integration;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.qc.application.*;
import com.hospital.mes.qc.domain.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
/** Native persistent database: all unique actors/materials/QC rows roll back. No shared seed changes. */
@SpringBootTest @ActiveProfiles("ci") @Transactional
class QcSpecificationIT {
 @Autowired QcSpecificationService service; @Autowired QcSpecificationQueryService query;
 @Autowired UnitService units; @Autowired MaterialService materials; @Autowired JdbcTemplate jdbc; @Autowired ObjectMapper json;
 @Autowired AuditEventRepository auditRepository; @Autowired SignatureTransactionService verification; @Autowired PlatformTransactionManager transactions;
 @MockitoBean CurrentPlatformContextResolver contexts; @MockitoBean ReauthenticationPort reauth;
 @MockitoSpyBean AuditApplicationService audit;
 long creator,approver; String suffix,unit,material; Set<String> permissions;
 @BeforeEach void setup(){suffix=UUID.randomUUID().toString().replace("-","").substring(0,12);creator=actor("qc_c"+suffix);approver=actor("qc_a"+suffix);permissions=new HashSet<>(Set.of("qms:specification:view","qms:specification:create","qms:specification:edit","qms:specification:approve","qms:specification:retire","ebr:sign","master:uom:create","master:material:create"));as(1,creator);when(reauth.consume(anyString(),any())).thenReturn(new ConsumedReauthentication(Instant.now(),"PASSWORD"));unit=units.create(new UnitCommands.Create("QU"+suffix,"QC unit","MASS",3),key()).path("id").asText();material=materials.create(json.convertValue(Map.of("materialCode","QM"+suffix,"materialName","QC material","materialType","RAW","baseUnitId",unit,"lotControlled",true),MaterialCommands.Create.class),key()).path("id").asText();}
 long actor(String n){jdbc.update("INSERT INTO sys_user(login_name,login_name_normalized,display_name,password_hash,enabled,must_change_password) VALUES(?,?,?,?,TRUE,FALSE)",n,n,"QC rollback actor","test-only");return jdbc.queryForObject("SELECT id FROM sys_user WHERE login_name_normalized=?",Long.class,n);}
 void as(long org,long user){when(contexts.current()).thenReturn(new CurrentPlatformContext(org,user,Set.of("QC_TEST"),permissions,"qc-session","qc-"+suffix));}
 String key(){return UUID.randomUUID().toString();}
 List<QcCommands.Item> items(){return List.of(new QcCommands.Item("ASSAY","Assay",true,"NUMERIC","1","3",unit,null,"M1","1"),new QcCommands.Item("COLOR","Color",true,"TEXT",null,null,null,"white","M2","1"));}
 QcViews.SpecificationVersionDetail draft(){var root=service.create(new QcCommands.CreateSpecification(material,"QS"+suffix,"QC standard","Create"),key());return service.createVersion(root.id(),new QcCommands.CreateVersion(1,items(),"Define"),key());}
 QcViews.SpecificationVersionDetail approve(QcViews.SpecificationVersionDetail v){as(1,approver);return service.approveVersion(v.id(),new QcCommands.SignVersion(v.versionNo(),"Approve","token"),"\""+v.versionNo()+"\"",key());}
 @Test void approvalPermissionVersionBindingSignerFailureAndChangedReplayPreserveDraft(){
  var v=draft();as(1,approver);var command=new QcCommands.SignVersion(0L,"Approve","token");String commandKey=key();
  var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);
  permissions.remove("qms:specification:approve");as(1,approver);
  assertThatThrownBy(()->tx.execute(s->service.approveVersion(v.id(),command,"\"0\"",commandKey))).isInstanceOf(com.hospital.mes.common.exception.PermissionException.class);
  permissions.add("qms:specification:approve");as(1,approver);
  assertThatThrownBy(()->tx.execute(s->service.approveVersion(v.id(),command,"\"1\"",commandKey)))
      .isInstanceOf(com.hospital.mes.common.exception.ValidationException.class).hasMessage("Body version must match If-Match");
  when(reauth.consume(anyString(),any())).thenThrow(new IllegalStateException("Injected QC signer failure"));
  assertThatThrownBy(()->tx.execute(s->service.approveVersion(v.id(),command,"\"0\"",commandKey))).hasMessage("Injected QC signer failure");
  assertThat(jdbc.queryForObject("SELECT status FROM qc_specification_version WHERE id=?",String.class,v.id())).isEqualTo("DRAFT");
  assertThat(jdbc.queryForObject("SELECT version_no FROM qc_specification_version WHERE id=?",Long.class,v.id())).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_signature WHERE object_type='QcSpecificationVersion' AND object_id=?",Long.class,v.id())).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,commandKey)).isZero();
  doReturn(new ConsumedReauthentication(Instant.now(),"PASSWORD")).when(reauth).consume(anyString(),any());
  var approved=service.approveVersion(v.id(),command,"\"0\"",commandKey);
  assertThat(service.approveVersion(v.id(),command,"\"0\"",commandKey)).isEqualTo(approved);
  assertThatThrownBy(()->tx.execute(s->service.approveVersion(v.id(),new QcCommands.SignVersion(0L,"Changed approval reason","token"),"\"0\"",commandKey)))
      .isInstanceOf(com.hospital.mes.common.exception.ResourceConflictException.class);
  assertThat(verification.verify(1,Long.parseLong(approved.approvalSignatureId()))).isTrue();
 }
 @Test void approvingNextVersionDoesNotRetireOrChangeExistingApprovedStandard(){
  var first=approve(draft());var frozen=query.requireSelectable(1,Long.parseLong(material),Long.parseLong(first.id()));as(1,creator);
  var second=service.createVersion(first.specificationId(),new QcCommands.CreateVersion(2,items(),"Separate next version"),key());
  var approved=approve(second);assertThat(approved.status()).isEqualTo("APPROVED");
  assertThat(jdbc.queryForObject("SELECT status FROM qc_specification_version WHERE id=?",String.class,first.id())).isEqualTo("APPROVED");
  assertThat(query.requireSelectable(1,Long.parseLong(material),Long.parseLong(first.id()))).isEqualTo(frozen);
  assertThat(verification.verify(1,Long.parseLong(first.approvalSignatureId()))).isTrue();
  assertThat(query.requireSelectable(1,Long.parseLong(material),Long.parseLong(approved.id())).specificationVersionId()).isEqualTo(Long.parseLong(approved.id()));
 }
 @Test void signedLifecycleSnapshotsRetirementAndReplay(){var v=draft();as(1,approver);var command=new QcCommands.SignVersion(0L,"Approve","token");String key=key();var approved=service.approveVersion(v.id(),command,"\"0\"",key);assertThat(service.approveVersion(v.id(),command,"\"0\"",key)).isEqualTo(approved);verify(reauth,times(1)).consume(eq("token"),argThat(b->b.objectType().equals("QcSpecificationVersion")&&b.recordVersion()==1));var frozen=query.requireSelectable(1,Long.parseLong(material),Long.parseLong(v.id()));assertThat(frozen.items()).hasSize(2);assertThat(verification.verify(1,Long.parseLong(approved.approvalSignatureId()))).isTrue();var retired=service.retireVersion(v.id(),new QcCommands.SignVersion(1L,"Retire","token"),"\"1\"",key());assertThat(retired.status()).isEqualTo("RETIRED");assertThat(verification.verify(1,Long.parseLong(approved.approvalSignatureId()))).isTrue();assertThat(verification.verify(1,Long.parseLong(retired.retirementSignatureId()))).isTrue();assertThat(query.getHistorical(1,Long.parseLong(v.id()))).isEqualTo(frozen);assertThatThrownBy(()->query.requireSelectable(1,Long.parseLong(material),Long.parseLong(v.id()))).hasMessageContaining("selectable");}
 @Test void draftTombstonesAndAllAuthorsAreRetained(){var v=draft();long editor=actor("qc_e"+suffix);as(1,editor);var edited=service.editVersion(v.id(),new QcCommands.EditVersion(0L,List.of(items().getFirst()),"Remove color"),"\"0\"",key());assertThat(edited.items()).hasSize(1);assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification_item WHERE specification_version_id=? AND active=0",Long.class,v.id())).isEqualTo(1);as(1,creator);service.editVersion(v.id(),new QcCommands.EditVersion(1L,items(),"Restore"),"\"1\"",key());var auditPage=auditRepository.query(1,new AuditEventQuery(editor,"QC_SPEC_VERSION_EDITED","QcSpecificationVersion",v.id(),null,null,null,null,null,0,1));assertThat(auditPage.total()).isGreaterThanOrEqualTo(1);assertThat(auditPage.items()).hasSize(1);assertThat(auditRepository.query(1,new AuditEventQuery(editor,"QC_SPEC_VERSION_EDITED","QcSpecificationVersion",v.id(),null,null,null,null,null,1,1)).items()).isEmpty();as(1,editor);assertThatThrownBy(()->service.approveVersion(v.id(),new QcCommands.SignVersion(2L,"Approval","token"),"\"2\"",key())).hasMessageContaining("author");verifyNoInteractions(reauth);}
 @Test void scopeLocksAndSignedDefinitionsFailClosed(){var v=draft();assertThatThrownBy(()->service.editVersion(v.id(),new QcCommands.EditVersion(1L,items(),"Stale"),"\"1\"",key())).hasMessageContaining("changed");as(2,creator);assertThatThrownBy(()->service.getVersion(v.id())).hasMessageContaining("not found");as(1,creator);var approved=approve(v);assertThatThrownBy(()->service.editVersion(v.id(),new QcCommands.EditVersion(1L,items(),"Change"),"\"1\"",key())).hasMessageContaining("DRAFT");assertThatThrownBy(()->jdbc.update("UPDATE qc_specification_item SET method_version='2' WHERE specification_version_id=?",v.id())).isInstanceOf(org.springframework.dao.DataAccessException.class);assertThatThrownBy(()->jdbc.update("DELETE FROM qc_specification_version WHERE id=?",v.id())).isInstanceOf(org.springframework.dao.DataAccessException.class);assertThat(query.getHistorical(1,Long.parseLong(v.id())).contentHash()).isEqualTo(approved.contentHash());}
 @Test void approvalAuditFailureRollsBackAllEvidence(){var v=draft();as(1,approver);String k=key();doAnswer(i->{var c=(com.hospital.mes.audit.domain.AuditCommand)i.getArgument(0);if(c.action().equals("QC_SPEC_VERSION_APPROVED"))throw new IllegalStateException("injected audit failure");return i.callRealMethod();}).when(audit).append(any());var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED);assertThatThrownBy(()->tx.execute(t->service.approveVersion(v.id(),new QcCommands.SignVersion(0L,"Approval","token"),"\"0\"",k))).hasMessageContaining("injected audit failure");assertThat(jdbc.queryForObject("SELECT status FROM qc_specification_version WHERE id=?",String.class,v.id())).isEqualTo("DRAFT");assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_signature WHERE object_type='QcSpecificationVersion' AND object_id=?",Long.class,v.id())).isZero();assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,k)).isZero();}
}
