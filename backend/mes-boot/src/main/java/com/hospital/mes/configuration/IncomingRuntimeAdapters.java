package com.hospital.mes.configuration;

import com.hospital.mes.ebr.application.EbrRuntimeService;
import com.hospital.mes.execution.application.*;
import com.hospital.mes.production.application.*;
import com.hospital.mes.common.exception.ComplianceException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IncomingRuntimeAdapters {
 @Bean ExecutionRuntimePort executionRuntime(EbrRuntimeService runtime) {
  return new ExecutionRuntimePort() {
   public void initializeOperation(long org,long actor,long batch,long execution,long operation,long definition,JsonNode snapshot) { runtime.initializeOperation(org,actor,batch,execution,operation,definition,snapshot); }
   public void requireOperationComplete(long org,long operation) { runtime.requireOperationComplete(org,operation); }
   public void requireOperationReview(long org,long operation) { runtime.requireOperationReview(org,operation); }
  };
 }
 @Bean ProductionRuntimeGate productionRuntimeGate(ExecutionService execution,ExecutionQueryService query,EbrRuntimeService runtime,ProductionQueryService production) {
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
   }
   public void requireQaSubmissionReady(long org,long batch) {
    requireProductionComplete(org,batch);
    throw new ComplianceException("DEPENDENCY_NOT_READY","Production quality, material balance and finished-product QA contracts are required before submission");
   }
  };
 }
}
