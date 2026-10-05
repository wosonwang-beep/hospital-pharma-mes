package com.hospital.mes.production;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.qms.application.MaterialBalanceService;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.*;
class MaterialBalanceIT extends ProductionQuantityIT {
 @Autowired MaterialBalanceService balances;
 @Autowired com.hospital.mes.qms.application.ProductionQualityService productionQuality;
 @BeforeEach void balancePermissions(){permissions.addAll(java.util.Set.of("balance:investigate","balance:approve"));as(author);}
 @Override JsonNode planBody(JsonNode batch){var plan=(ObjectNode)super.planBody(batch);var rule=(ObjectNode)plan.path("balanceRules").get(0);((ObjectNode)rule.path("formulaExpr")).set("expected",json.valueToTree(Map.of("sum",java.util.List.of("OUTPUT"))));return plan;}
 JsonNode observation(JsonNode batch,String source,String amount){return quantities.record(id(batch),body("versionNo",batch.path("versionNo").asLong(),"eventType","LOSS","amount",amount,"unitId",unit,"sourceRef",source,"reason","Measured loss","signature",Map.of("reauthToken","token")),token(batch),key());}
 JsonNode calculate(JsonNode batch){return balances.recalculate(id(batch),body("versionNo",batch.path("versionNo").asLong(),"reason","Calculate actual source evidence"),token(batch),key());}
 @Test void missingCoverageStalenessAndImmutableSuccessorCalculationsAreEnforced(){
  var batch=approvedBatch();quantities.record(id(batch),output(batch,"O-"+suffix,"10"),token(batch),key());var root=production.batch(id(batch));
  blockedCode("BALANCE_INPUT_MISSING",()->calculate(root));observation(root,"L0-"+suffix,"0");var calculated=calculate(production.batch(id(batch)));
  assertThat(calculated.path("results").get(0).path("status").asText()).isEqualTo("PASS");balances.requireCheckpoint(1,Long.parseLong(id(batch)),"BATCH_COMPLETE",null);
  observation(production.batch(id(batch)),"L1-"+suffix,"2");blockedCode("BALANCE_RESULT_STALE",()->balances.requireCheckpoint(1,Long.parseLong(id(batch)),"BATCH_COMPLETE",null));
  var next=calculate(production.batch(id(batch)));assertThat(next.path("results")).hasSize(2);assertThat(next.path("results").get(0).path("status").asText()).isEqualTo("PASS");assertThat(next.path("results").get(1).path("status").asText()).isEqualTo("FAIL");
  assertThatThrownBy(()->jdbc.update("UPDATE mes_balance_result SET status='PASS' WHERE id=?",next.path("results").get(1).path("id").asText())).isInstanceOf(org.springframework.dao.DataAccessException.class);
 }
 @Test void independentExceptionIsBoundToExactResultAndCannotSurviveNewInputs(){
  var batch=approvedBatch();quantities.record(id(batch),output(batch,"O-"+suffix,"10"),token(batch),key());observation(production.batch(id(batch)),"L-"+suffix,"2");var view=calculate(production.batch(id(batch)));var result=view.path("results").get(0);var current=production.batch(id(batch));
  as(analyst);var investigation=balances.createInvestigation(id(result),body("versionNo",current.path("versionNo").asLong(),"investigationText","Documented measured loss investigation","reason","Investigate discrepancy"),token(current),key());
  investigation=balances.investigate(id(investigation),body("versionNo",investigation.path("versionNo").asLong(),"investigationText","Cause evaluated with actual evidence","reason","Investigation completed"),token(investigation),key());
  var approval=body("versionNo",investigation.path("versionNo").asLong(),"decision","ACCEPT_EXCEPTION","reason","Independent justified exception","signature",Map.of("reauthToken","token"));var finalInvestigation=investigation;
  blockedCode("INDEPENDENT_REVIEW_REQUIRED",()->balances.approve(id(finalInvestigation),approval,token(finalInvestigation),key()));
  as(qa);var approved=balances.approve(id(investigation),approval,token(investigation),key());assertThat(verifier.verify(1,approved.path("signatureId").asLong())).isTrue();balances.requireCheckpoint(1,Long.parseLong(id(batch)),"BATCH_COMPLETE",null);
  assertThat(jdbc.queryForObject("SELECT status FROM mes_balance_result WHERE id=?",String.class,id(result))).isEqualTo("FAIL");
  as(author);observation(production.batch(id(batch)),"NEW-"+suffix,"0");blockedCode("BALANCE_RESULT_STALE",()->balances.requireCheckpoint(1,Long.parseLong(id(batch)),"BATCH_COMPLETE",null));
 }
 @Test void unreferencedQuantityNeedsNoConversionButInvalidatesDigestAndLogisticsAreCountedOnce(){
  var batch=approvedBatch();quantities.record(id(batch),output(batch,"O-"+suffix,"10"),token(batch),key());observation(production.batch(id(batch)),"L-"+suffix,"0");
  var first=calculate(production.batch(id(batch)));assertThat(first.path("results").get(0).path("status").asText()).isEqualTo("PASS");
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_quantity_event WHERE main_batch_id=? AND event_type='ISSUE'",Long.class,id(batch))).isEqualTo(1);
  var countUnit=units.create(new com.hospital.mes.masterdata.application.UnitCommands.Create("PC"+suffix,"Measured pieces","COUNT",0),key());var current=production.batch(id(batch));
  quantities.record(id(batch),body("versionNo",current.path("versionNo").asLong(),"eventType","WIP","amount","1","unitId",id(countUnit),"sourceRef","WIP-"+suffix,"reason","Independent measured outstanding units","signature",Map.of("reauthToken","token")),token(current),key());
  blockedCode("BALANCE_RESULT_STALE",()->balances.requireCheckpoint(1,Long.parseLong(id(batch)),"BATCH_COMPLETE",null));
  var second=calculate(production.batch(id(batch)));assertThat(second.path("results").get(1).path("status").asText()).isEqualTo("PASS");balances.requireCheckpoint(1,Long.parseLong(id(batch)),"BATCH_COMPLETE",null);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_quantity_event WHERE main_batch_id=? AND event_type='ISSUE'",Long.class,id(batch))).isEqualTo(1);
 }
 @Test void productionCompletionUsesActualBalanceGateAndPreservesParentOnFailure(){
  var batch=approvedBatch();var eid=batch.path("executionUnits").get(0).path("id").asText();var operation=executionService.operations(eid).getFirst();
  var running=executionService.start(id(operation),body("reason","Execute actual route"),token(operation),key());recordAndSubmitOperationForm(eid);executionService.complete(id(running),body("reason","Actual operation completed"),token(running),key());
  blockedCode("BALANCE_RESULT_MISSING",()->production.completeProduction(id(batch),body("reason","No calculated balance"),token(production.batch(id(batch))),key()));
  quantities.record(id(batch),output(production.batch(id(batch)),"O-"+suffix,"10"),token(production.batch(id(batch))),key());observation(production.batch(id(batch)),"L-"+suffix,"0");calculate(production.batch(id(batch)));
  var complete=production.completeProduction(id(batch),body("reason","Actual completion gates satisfied"),token(production.batch(id(batch))),key());assertThat(complete.path("status").asText()).isEqualTo("PRODUCTION_COMPLETED");
  blocked(()->production.submitQa(id(batch),body("reason","Finished QC is still missing"),token(complete),key()));
  assertThat(production.batch(id(batch)).path("status").asText()).isEqualTo("PRODUCTION_COMPLETED");
  productionQuality.createDeviation(body("investigationScope","PRODUCTION","mainBatchId",id(batch),"investigationKind","DEVIATION","severity","CRITICAL","description","New critical finding before QA submission","reason","Mandatory production quality finding"),key());
  blockedCode("PRODUCTION_CRITICAL_UNRESOLVED",()->production.submitQa(id(batch),body("reason","Critical finding must block the real QA entry"),token(complete),key()));
  assertThat(production.batch(id(batch)).path("status").asText()).isEqualTo("PRODUCTION_COMPLETED");
 }
 @Test void competingCheckpointCannotBypassRootLockAndLeavesNoPersistentFacts() throws Exception {
  var batch=approvedBatch();long batchId=Long.parseLong(id(batch));long before=jdbc.queryForObject("SELECT COUNT(*) FROM mes_quantity_event WHERE main_batch_id=?",Long.class,batchId);
  // Read-uncommitted exposes this rollback-only fixture identity to the contender;
  // FOR UPDATE still requires the actual order/batch owner lock and must wait.
  try(var worker=java.util.concurrent.Executors.newSingleThreadExecutor()){
   var future=worker.submit(()->{
    var tx=new org.springframework.transaction.support.TransactionTemplate(transactions);tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);tx.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
    return tx.execute(status->{int original=jdbc.queryForObject("SELECT @@innodb_lock_wait_timeout",Integer.class);jdbc.execute("SET SESSION innodb_lock_wait_timeout=1");try{balances.requireCheckpoint(1,batchId,"QA_RELEASE",null);return (RuntimeException)null;}catch(RuntimeException failure){return failure;}finally{status.setRollbackOnly();jdbc.execute("SET SESSION innodb_lock_wait_timeout="+original);}});
   });
   assertThat(future.get(10,java.util.concurrent.TimeUnit.SECONDS)).isInstanceOfSatisfying(com.hospital.mes.common.exception.ResourceConflictException.class,e->assertThat(e.code()).isEqualTo("CONCURRENT_MODIFICATION"));
  }
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mes_quantity_event WHERE main_batch_id=?",Long.class,batchId)).isEqualTo(before);
  assertThat(production.batch(id(batch)).path("status").asText()).isEqualTo("IN_PROGRESS");
 }
}
