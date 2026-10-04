package com.hospital.mes.production;
import static org.assertj.core.api.Assertions.*;
import com.hospital.mes.qc.domain.QcCommands;
import com.hospital.mes.common.exception.MesException;
import java.util.*;
import java.time.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.transaction.support.TransactionTemplate;
/** Reuses expressly approved retained native fixtures and audited reject cleanup. */
class QcProducerConcurrencyIT extends NativeConcurrencyIT {
 @ParameterizedTest @ValueSource(booleans={true,false})
 void editAndApprovalSerializeWithoutSigningStaleDefinition(boolean approvalFirst) throws Exception {
  prepareGate("RESERVE");as(author);var original=qc.getVersion(version);var draft=qc.createVersion(original.specificationId(),new QcCommands.CreateVersion(2,List.of(new QcCommands.Item("ASSAY","Assay",true,"NUMERIC","1","3",unit,null,"METHOD","1")),"Race draft"),key());
  commitFixture();as(author);var editor=actorContext.get();as(approver);var approving=actorContext.get();String editKey=key(),approveKey=key();
  java.util.concurrent.Callable<com.fasterxml.jackson.databind.JsonNode> edit=()->json.valueToTree(qc.editVersion(draft.id(),new QcCommands.EditVersion(0L,List.of(new QcCommands.Item("ASSAY","Assay changed",true,"NUMERIC","1","3",unit,null,"METHOD","2")),"Edit"),"\"0\"",editKey));
  java.util.concurrent.Callable<com.fasterxml.jackson.databind.JsonNode> approve=()->json.valueToTree(qc.approveVersion(draft.id(),new QcCommands.SignVersion(0L,"Approve","token"),"\"0\"",approveKey));
  var r=race("SELECT id FROM qc_specification_version WHERE id=? FOR UPDATE",draft.id(),approvalFirst?approving:editor,approvalFirst?approve:edit,approvalFirst?editor:approving,approvalFirst?edit:approve);
  assertThat(r.firstError()).isNull();assertThat(r.secondError()).isInstanceOf(MesException.class);
  actorContext.set(approvalFirst?editor:approving);assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->{try{return (approvalFirst?edit:approve).call();}catch(RuntimeException e){throw e;}catch(Exception e){throw new RuntimeException(e);}})).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("RECORD_CHANGED"));
  assertThat(jdbc.queryForObject("SELECT status FROM qc_specification_version WHERE id=?",String.class,draft.id())).isEqualTo(approvalFirst?"APPROVED":"DRAFT");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gxp_signature WHERE object_type='QcSpecificationVersion' AND object_id=?",Long.class,draft.id())).isEqualTo(approvalFirst?1:0);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,approvalFirst?editKey:approveKey)).isZero();
 }
 @ParameterizedTest @ValueSource(booleans={true,false})
 void requestAndRetirementSerializeWhileHistoricalSnapshotRemainsValid(boolean retirementFirst) throws Exception {
  prepareGate("RESERVE");var standard=qc.getVersion(version);long before=jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_request WHERE material_lot_id=?",Long.class,lot);
  var command=body("materialLotId",lot,"qcSpecificationVersionId",version,"requestType","INITIAL","reason","Concurrent frozen request","requestedQuantity","1","requestedUnitId",unit,"requestedPackageCount",1,"requestedDate",LocalDate.now(ZoneOffset.UTC).toString(),"priority","NORMAL");
  commitFixture();as(analyst);var requesting=actorContext.get();as(approver);var retiring=actorContext.get();String requestKey=key(),retireKey=key();
  java.util.concurrent.Callable<com.fasterxml.jackson.databind.JsonNode> request=()->service.createRequest(command,requestKey);
  java.util.concurrent.Callable<com.fasterxml.jackson.databind.JsonNode> retire=()->json.valueToTree(qc.retireVersion(version,new QcCommands.SignVersion(standard.versionNo(),"Retire","token"),"\""+standard.versionNo()+"\"",retireKey));
  var r=race("SELECT id FROM qc_specification_version WHERE id=? FOR UPDATE",version,retirementFirst?retiring:requesting,retirementFirst?retire:request,retirementFirst?requesting:retiring,retirementFirst?request:retire);
  assertThat(r.firstError()).isNull();
  if(retirementFirst){assertThat(r.secondError()).isInstanceOf(MesException.class);actorContext.set(requesting);assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->service.createRequest(command,requestKey))).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("QC_SPEC_NOT_SELECTABLE"));assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,requestKey)).isZero();}
  else{if(r.secondError()!=null){actorContext.set(retiring);new TransactionTemplate(transactions).execute(s->{try{return retire.call();}catch(RuntimeException e){throw e;}catch(Exception e){throw new RuntimeException(e);}});}assertThat(r.first().path("qcSpecificationVersionId").asText()).isEqualTo(version);}
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_request WHERE material_lot_id=?",Long.class,lot)).isEqualTo(before+(retirementFirst?0:1));
  assertThat(jdbc.queryForObject("SELECT status FROM qc_specification_version WHERE id=?",String.class,version)).isEqualTo("RETIRED");assertThat(verifier.verify(1,Long.parseLong(standard.approvalSignatureId()))).isTrue();
 }
}
