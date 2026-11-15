package com.hospital.mes.production;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.assertj.core.api.Assertions.*;
import com.hospital.mes.qc.domain.QcCommands;
import com.hospital.mes.common.exception.MesException;
import java.util.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
/** Reuses expressly approved retained native fixtures and audited reject cleanup. */
@EnabledIfSystemProperty(named="mes.retained-concurrency-fixtures",matches="true")
class QcProducerConcurrencyIT extends NativeConcurrencyIT {
 @Test
 void concurrentStandardEditsSerializeOnOneVersionWithNoLostUpdate() throws Exception {
  prepareGate("RESERVE");as(author);var original=qc.get(authorStandard());String standardId=id(original);long versionNo=original.path("versionNo").asLong();
  commitFixture();as(author);var editor=actorContext.get();as(approver);var reviewer=actorContext.get();String firstKey=key(),secondKey=key();
  java.util.concurrent.Callable<com.fasterxml.jackson.databind.JsonNode> first=()->qc.edit(standardId,new QcCommands.Edit(versionNo,"RM-KCL-STD"+suffix,"氯化钾入厂检验标准","ENTERPRISE_INTERNAL","企业内控标准 Q/QC 001-2026",null,"收严含量下限","ACTIVE","Concurrent first rewrite"),"\""+versionNo+"\"",firstKey);
  java.util.concurrent.Callable<com.fasterxml.jackson.databind.JsonNode> second=()->qc.edit(standardId,new QcCommands.Edit(versionNo,"RM-KCL-STD"+suffix,"氯化钾入厂检验标准","ENTERPRISE_INTERNAL","企业内控标准 Q/QC 002-2026",null,"收严含量上限","ACTIVE","Concurrent second rewrite"),"\""+versionNo+"\"",secondKey);
  var r=race("SELECT id FROM qc_specification WHERE id=? FOR UPDATE",standardId,editor,first,reviewer,second);
  assertThat(r.firstError()).isNull();assertThat(r.secondError()).isInstanceOf(MesException.class);
  actorContext.set(reviewer);
  assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->{try{return second.call();}catch(RuntimeException e){throw e;}catch(Exception e){throw new RuntimeException(e);}})).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("RECORD_CHANGED"));
  assertThat(jdbc.queryForObject("SELECT version_no FROM qc_specification WHERE id=?",Long.class,standardId)).isEqualTo(versionNo+1);
  assertThat(jdbc.queryForObject("SELECT basis_reference FROM qc_specification WHERE id=?",String.class,standardId)).isEqualTo("企业内控标准 Q/QC 001-2026");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,secondKey)).isZero();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qc_specification_revision WHERE specification_id=?",Long.class,standardId)).isEqualTo(2);
 }
 @Test
 void requestAndInactivationSerializeWhileTheFrozenSnapshotRemainsValid() throws Exception {
  prepareGate("RESERVE");String standardId=authorStandard();long versionNo=qc.get(standardId).path("versionNo").asLong();
  long before=jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_request WHERE material_lot_id=?",Long.class,lot);
  var command=body("materialLotId",lot,"qcSpecificationVersionId",standardId,"requestType","INITIAL","reason","Concurrent frozen request","requestedQuantity","1","requestedUnitId",unit,"requestedPackageCount",1,"requestedDate",LocalDate.now(ZoneOffset.UTC).toString(),"priority","NORMAL");
  commitFixture();as(analyst);var requesting=actorContext.get();as(author);var editing=actorContext.get();String requestKey=key(),editKey=key();
  java.util.concurrent.Callable<com.fasterxml.jackson.databind.JsonNode> request=()->service.createRequest(command,requestKey);
  java.util.concurrent.Callable<com.fasterxml.jackson.databind.JsonNode> inactivate=()->qc.edit(standardId,new QcCommands.Edit(versionNo,"RM-KCL-STD"+suffix,"氯化钾入厂检验标准","ENTERPRISE_INTERNAL","企业内控标准 Q/QC 001-2026",null,"停止执行","INACTIVE","Concurrent inactivation"),"\""+versionNo+"\"",editKey);
  var r=race("SELECT id FROM qc_specification WHERE id=? FOR UPDATE",standardId,editing,inactivate,requesting,request);
  if(r.firstError()==null){
   assertThat(jdbc.queryForObject("SELECT status FROM qc_specification WHERE id=?",String.class,standardId)).isEqualTo("INACTIVE");
   actorContext.set(requesting);
   assertThat(r.secondError()).isInstanceOf(MesException.class);
   assertThatThrownBy(()->new TransactionTemplate(transactions).execute(s->service.createRequest(command,requestKey))).isInstanceOfSatisfying(MesException.class,e->assertThat(e.code()).isEqualTo("QC_SPEC_NOT_SELECTABLE"));
   assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM platform_idempotency_record WHERE idempotency_key=?",Long.class,requestKey)).isZero();
  }
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM qms_inspection_request WHERE material_lot_id=?",Long.class,lot)).isEqualTo(before);
 }
 private String authorStandard(){
  as(author);
  return id(qc.create(new QcCommands.Create("RM-KCL-STD"+suffix,"氯化钾入厂检验标准","ENTERPRISE_INTERNAL","企业内控标准 Q/QC 001-2026",null,"来料检验内控标准",List.of(material),List.of(),List.of(new QcCommands.Item("ASSAY","Assay",true,"NUMERIC","1","3",unit,null,"METHOD","1")),"Concurrent fixture standard"),key()));
 }
}
