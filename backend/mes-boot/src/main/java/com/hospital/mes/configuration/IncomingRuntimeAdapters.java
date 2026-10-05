package com.hospital.mes.configuration;

import com.hospital.mes.ebr.application.EbrRuntimeService;
import com.hospital.mes.execution.application.*;
import com.hospital.mes.production.application.*;
import com.hospital.mes.qms.application.MaterialBalanceService;
import com.hospital.mes.qms.application.ProductionQualityService;
import com.hospital.mes.common.exception.ComplianceException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingRuntimeAdapters {
 @Bean ExecutionRuntimePort executionRuntime(EbrRuntimeService runtime,ExecutionQueryService query,ProductionQueryService production,ObjectProvider<MaterialBalanceService> balances,ObjectProvider<ProductionQualityService> quality) {
  return new ExecutionRuntimePort() {
   public void initializeOperation(long org,long actor,long batch,long execution,long operation,long definition,JsonNode snapshot) { runtime.initializeOperation(org,actor,batch,execution,operation,definition,snapshot); }
   public void requireOperationComplete(long org,long operation) {
    runtime.requireOperationComplete(org,operation);
    var actual=query.operation(org,operation);requireQuality(quality.getObject().blockingCodes(org,actual.mainBatchId(),false));
    if(production.batch(org,actual.mainBatchId()).snapshot().hasNonNull("qualityPlan"))balances.getObject().requireCheckpoint(org,actual.mainBatchId(),"OPERATION_COMPLETE",operation);
   }
   public void requireOperationReview(long org,long operation) { runtime.requireOperationReview(org,operation); }
  };
 }
 @Bean ProductionRuntimeGate productionRuntimeGate(ExecutionService execution,ExecutionQueryService query,EbrRuntimeService runtime,ProductionQueryService production,ObjectProvider<MaterialBalanceService> balances,ObjectProvider<ProductionQualityService> quality) {
  return new ProductionRuntimeGate() {
   public void requireInitialized(long org,long batch,List<Long> ids) {
    var actual=production.executionIds(org,batch);
    if(ids.isEmpty() || !new java.util.HashSet<>(actual).equals(new java.util.HashSet<>(ids))) throw new ComplianceException("DEPENDENCY_NOT_READY","Actual execution set is incomplete");
    for(long id:ids) { execution.requireInitialized(org,id); var operations=query.operationIds(org,id); if(operations.isEmpty()) throw new ComplianceException("DEPENDENCY_NOT_READY","Actual operations are missing"); for(long operation:operations)runtime.requireInitialized(org,operation); }
   }
   public void requireProductionComplete(long org,long batch) {
    var operations=query.batchOperationIds(org,batch);
    if(operations.isEmpty()) throw new ComplianceException("BATCH_EXECUTION_INCOMPLETE","Actual operations are missing");
    for(long operation:operations) {
     if(!"COMPLETED".equals(query.operation(org,operation).operationStatus())) throw new ComplianceException("BATCH_EXECUTION_INCOMPLETE","All actual operations must be complete");
     var evidence=runtime.evaluateOperation(org,operation);
     if(!evidence.complete()) throw new ComplianceException("EBR_OPERATION_INCOMPLETE",String.join(",",evidence.blockingCodes()));
    }
    requireQuality(quality.getObject().blockingCodes(org,batch,false));
    balances.getObject().requireCheckpoint(org,batch,"BATCH_COMPLETE",null);
   }
   public void requireQaSubmissionReady(long org,long batch) {
    requireProductionComplete(org,batch);
    for(long operation:query.batchOperationIds(org,batch))runtime.requireOperationReview(org,operation);
    requireQuality(quality.getObject().blockingCodes(org,batch,true));
    balances.getObject().requireCheckpoint(org,batch,"QA_RELEASE",null);
    if(production.qualityIdentity(org,batch).finishedLotId()==null)throw new ComplianceException("FINISHED_OUTPUT_REQUIRED","Actual batch-owned finished output is required before QA");
   }
  };
 }
 private static void requireQuality(List<String> codes){if(!codes.isEmpty())throw new ComplianceException(codes.getFirst(),String.join(",",codes));}
}
